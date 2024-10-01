package catrpc.registry;

import catrpc.message.RPCRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class testDiscovery {


    public static void main(String[] args) throws UnknownHostException {


        String host = InetAddress.getLocalHost().getHostAddress();
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.registerService("newService:default",new InetSocketAddress(host,7777));


        RPCRequest request = new RPCRequest();

        request.setInterfaceName("newService");

        ServiceDiscovery s = new ServiceDiscovery();
        try {
            s.lookupService(request);
        } catch (Exception e) {
            new RuntimeException(e);
        }

        while (true){

        }

    }
}
