package catrpc.transport.loadbalance.loadbalancerimpl;

import catrpc.message.RPCRequest;
import catrpc.transport.loadbalance.LoadBalancer;
import lombok.Getter;

import java.util.List;
import java.util.Random;

// RandomLoadBalancer 实现随机负载均衡

public class RandomLoadBalancer implements LoadBalancer {
    @Getter
    private static final RandomLoadBalancer instance = new RandomLoadBalancer();

    private RandomLoadBalancer() {}

    @Override
    public String selectServiceAddress(List<String> validServiceAddr, RPCRequest request) {
        Random random = new Random();
        return validServiceAddr.get(random.nextInt(validServiceAddr.size()));
    }
}