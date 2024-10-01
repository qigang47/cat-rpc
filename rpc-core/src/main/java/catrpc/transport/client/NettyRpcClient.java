package catrpc.transport.client;

import catrpc.constant.ConfigConstant;
import catrpc.constant.MessageConstant;
import catrpc.exception.RpcTransportException;
import catrpc.message.RPCMessage;
import catrpc.message.RPCRequest;
import catrpc.message.RPCResponse;
import catrpc.registry.ServiceDiscovery;
import catrpc.transport.codec.MessageDecoder;
import catrpc.transport.codec.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
@Slf4j
public class NettyRpcClient {

    //单例
    @Getter
    private static final NettyRpcClient client = new NettyRpcClient();
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    //一个请求id和completabelfuture的映射
    private static final Map<String, CompletableFuture<RPCResponse<Object>>> completableFutureMap= new ConcurrentHashMap<>();;

    //用于管理channel
    private static final Map<String, Channel> channelMap = new ConcurrentHashMap<>();;

    //TODO 要用单例吗
    private final ServiceDiscovery serviceDiscovery;

    //设成private防止外部调用
    private NettyRpcClient() {

        serviceDiscovery = new ServiceDiscovery();

        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // 5秒连接超时.
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 5秒一次心跳检测
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new MessageEncoder());
                        p.addLast(new MessageDecoder());
                        p.addLast(new ClientInboundHandler());
                    }
                });
    }

    public Channel doConnect(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("连接 [{}] 成功！", inetSocketAddress.toString());
                    completableFuture.complete(future.channel());
                } else {
                    throw new IllegalStateException();
                }
            }
        });
        return completableFuture.get();
    }

    public Channel getChannel(InetSocketAddress i) throws ExecutionException, InterruptedException {

        String iName = i.toString();
        Channel c = null;
        if(channelMap.containsKey(iName)){
            c = channelMap.get(iName);
            if(c!=null&&c.isActive()){
                return c;
            }
        }
        c = doConnect(i);
        channelMap.put(iName,c);
        return c;
    }


    public CompletableFuture<RPCResponse<Object>> send(RPCRequest request) throws ExecutionException, InterruptedException {

        // 等待存放结果
        CompletableFuture<RPCResponse<Object>> responseFuture = new CompletableFuture<>();
        // 服务发现
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(request);
        // 获取channel连接
        Channel channel = getChannel(inetSocketAddress);

        if(channel!=null&&channel.isActive()){

            RPCMessage message = new RPCMessage();
            message.setData(request);
            message.setMessageType(MessageConstant.REQUEST_TYPE);
            message.setCodec(ConfigConstant.SERIALIZER);
            message.setCompress(ConfigConstant.COMPRESS);
            //message.setRequestId();
            //TODO 这里应该设置messageid吗

            //设置结果的映射,这里直接执行后handler会设置结果到responseFuture，如果异常了就在这里设置
            addUnprocessedResponseFuture(request,responseFuture);
            channel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", message);
                } else {
                    future.channel().close();
                    responseFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });



        }else {

            throw new RpcTransportException("channel 无效");
        }
        return responseFuture;

    }

    //用来把请求id跟未来放服务端返回结果的completable对象做映射
    public void addUnprocessedResponseFuture(RPCRequest request,CompletableFuture<RPCResponse<Object>> responseFuture){
        completableFutureMap.put(request.getRequestId(),responseFuture );

    }

    //TODO 这里应该catch这个异常，在因为超时而多次发送请求的情况下，可能会出现这样的多次回复，怎么解决呢？ 是不是可以检查到没有对应id直接丢弃
    public static void setResponseMessage(RPCResponse<Object> response){
        CompletableFuture<RPCResponse<Object>> f = completableFutureMap.remove(response.getRequestId());
        if(f==null) throw new RpcTransportException("未找到与响应对应的请求 请求可能已经被处理完毕");
        f.complete(response);
    }

    public static boolean checkIfExitingUnprocessedRequest(String requestId){
        return completableFutureMap.containsKey(requestId);
    }


}
