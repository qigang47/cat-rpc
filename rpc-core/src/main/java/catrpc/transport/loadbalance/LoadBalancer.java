package catrpc.transport.loadbalance;

import catrpc.message.RPCRequest;

import java.util.List;

public interface LoadBalancer {


    //TODO 注意一下是否需要同步？
    String selectServiceAddress(List<String> validServiceAddr, RPCRequest request);



}
