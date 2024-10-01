package catrpc.transport.client.proxy;

import catrpc.message.RPCRequest;
import catrpc.message.RPCResponse;
import catrpc.transport.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ClientProxyHandler implements InvocationHandler {

    private final NettyRpcClient client = NettyRpcClient.getClient();
    private final String version;

    public ClientProxyHandler(String v){
        version = v;
    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        log.info("invoked method: [{}]", method.getName());
        RPCRequest request = new RPCRequest();

        request.setRequestId(UUID.randomUUID().toString());
        request.setMethodName(method.getName());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setServiceVersion(version);

        CompletableFuture<RPCResponse<Object>> completableFuture = client.send(request);
        //这里阻塞等待结果
        RPCResponse<Object> response = completableFuture.get();

        //异常检查等操作放在之前的流程，这里如果拿到了结果直接返回

        return response.getResult();
    }
}
