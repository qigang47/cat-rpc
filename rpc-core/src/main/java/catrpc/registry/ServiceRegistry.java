package catrpc.registry;

import catrpc.constant.VersionConstant;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

public class ServiceRegistry {

    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress){

        String servicePath = VersionConstant.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();


        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);


    }


}
