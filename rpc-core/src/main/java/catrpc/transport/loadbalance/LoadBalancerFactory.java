package catrpc.transport.loadbalance;

import catrpc.constant.ConfigConstant;
import catrpc.constant.MessageConstant;
import catrpc.transport.loadbalance.loadbalancerimpl.RandomLoadBalancer;
import catrpc.transport.loadbalance.loadbalancerimpl.RoundRobinLoadBalancer;

//TODO 每个策略都创建了实例 可优化
public class LoadBalancerFactory {

    private LoadBalancerFactory(){

    }



    public static LoadBalancer getInstance(byte type){
        if (type == MessageConstant.LOADBALANCE_ROUND_ROBIN) {
            return RoundRobinLoadBalancer.getInstance();
        } else if (type == MessageConstant.LOADBALANCE_RANDOM) {
            return RandomLoadBalancer.getInstance();
        }else {
            // 其他未知策略，返回默认实现
            return RandomLoadBalancer.getInstance();
        }

    }

    //默认策略
    public static LoadBalancer getLoadBalancerInstance(){
        return getInstance(ConfigConstant.LOADBALANCE);
    }





}
