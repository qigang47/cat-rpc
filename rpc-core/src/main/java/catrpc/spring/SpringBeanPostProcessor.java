package catrpc.spring;

import catrpc.registry.ServiceRegistry;
import catrpc.spring.annotation.RpcReference;
import catrpc.spring.annotation.RpcService;
import catrpc.transport.client.NettyRpcClient;
import catrpc.transport.client.proxy.ClientProxyHandler;
import catrpc.transport.server.NettyRpcServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;


@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceRegistry serviceRegistry;
    private final NettyRpcClient rpcClient;

    public SpringBeanPostProcessor() {
       rpcClient = NettyRpcClient.getClient();
       serviceRegistry = new ServiceRegistry();
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());

            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);

            //TODO 这里用的name对吗？
            //注解那里定义了version默认值是default
            String serviceName = bean.getClass().getInterfaces()[0].getCanonicalName()+":"+rpcService.version();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
            NettyRpcServer.addService(serviceName, bean);
            serviceRegistry.registerService(serviceName,inetSocketAddress);

        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {

                ClientProxyHandler clientProxyHandler = new ClientProxyHandler(rpcReference.version());

                Object clientProxy = clientProxyHandler.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}
