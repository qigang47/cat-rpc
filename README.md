# catPRC-Framework
一个基于Netty和Zookeeper的轻量级分布式RPC框架，提供高性能的远程服务。 

-特性：
- 基于高性能网络通信框架Netty，完整封装底层通信细节，可以像本地调用一样使用远程服务  
- 基于Zookeeper实现注册中心  
- 整合至Spring，通过注解即可使用，配置简单，代码无侵入性  
- 插件化编解码、序列化、压缩，高度可扩展，使用配置文件避免硬编码  
- 纯异步， 接口完全支持CompletableFuture类型返回值  
- 优雅下线，无感知下线  
- 预热机制，优雅上线  （待完善）
- 安全的重试机制，提高系统容错率  （待完善）
- 自适应的负载均衡设计，根据节点的稳定性动态调整负载均衡权重  （待完善）
- 流量从服务消费方直达提供方，避免经过nginx等中间服务器做负载均衡导致流量放大

