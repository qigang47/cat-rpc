package catrpc.transport.client;

import catrpc.constant.ConfigConstant;
import catrpc.constant.MessageConstant;
import catrpc.message.RPCMessage;
import catrpc.message.RPCResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ClientInboundHandler extends ChannelInboundHandlerAdapter {

    //获取到上下文信息
    private NettyRpcClient client;
    public ClientInboundHandler(){

        this.client = NettyRpcClient.getClient();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RPCMessage) {
                RPCMessage tmp = (RPCMessage) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == MessageConstant.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == MessageConstant.RESPONSE_TYPE) {
                    //如果没有对应的请求，就忽略这个回应  因为channelread方法是单线程的，不用加锁
                    if(NettyRpcClient.checkIfExitingUnprocessedRequest(((RPCResponse<Object>)tmp.getData()).getRequestId())){
                        RPCResponse<Object> rpcResponse = (RPCResponse<Object>) tmp.getData();
                        //这里把结果传递给completablefuture对象，此时invoke方法中阻塞住的get方法可以获取到结果
                        NettyRpcClient.setResponseMessage(rpcResponse);
                    }else {
                        log.info("忽略了一条重复的响应");
                    }
                }
            }
    }

    //当IdleStateHandler传递事件下来，并且是写空闲事件的时候，发一个心跳信息防止断连
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = client.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RPCMessage rpcMessage = new RPCMessage();

                rpcMessage.setMessageType(MessageConstant.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setCompress(ConfigConstant.COMPRESS);
                rpcMessage.setCodec(ConfigConstant.SERIALIZER);
                rpcMessage.setData(MessageConstant.PING);

                //如果心跳检测失败则关闭channel
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    //这里做统一异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("netty 客户端异常 ：", cause);
        cause.printStackTrace();
        ctx.close();
    }



}
