package catrpc.registry;

import catrpc.exception.RpcRegistryException;
import catrpc.message.RPCRequest;
import catrpc.transport.loadbalance.LoadBalancer;
import catrpc.transport.loadbalance.LoadBalancerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;
@Slf4j
public class ServiceDiscovery {


    private LoadBalancer loadBalance;

    public ServiceDiscovery(){
        loadBalance= LoadBalancerFactory.getLoadBalancerInstance();
    }

    public InetSocketAddress lookupService(RPCRequest rpcRequest){


        String rpcServiceName = rpcRequest.getServiceName();

        //这里是使用默认的配置文件名
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);

        if (serviceUrlList==null|| serviceUrlList.isEmpty()) {
            throw new RpcRegistryException("Service Not Found !");
        }

        // load balancing
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Using service address:[{}] after load balance.", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);

    }

}
