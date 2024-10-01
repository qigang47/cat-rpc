package catrpc.transport.loadbalance.loadbalancerimpl;

import catrpc.message.RPCRequest;
import catrpc.transport.loadbalance.LoadBalancer;
import lombok.Getter;

import java.util.List;

// RoundRobinLoadBalancer 实现轮询负载均衡
public class RoundRobinLoadBalancer implements LoadBalancer {
    @Getter
    private static final RoundRobinLoadBalancer instance = new RoundRobinLoadBalancer();

    private RoundRobinLoadBalancer() {}

    //TODO
    @Override
    public String selectServiceAddress(List<String> validServiceAddr, RPCRequest request) {
        return null;
    }
}