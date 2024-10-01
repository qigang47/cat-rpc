package catrpc.transport.server;

import catrpc.constant.ConfigConstant;
import catrpc.constant.MessageConstant;
import catrpc.exception.RPCInvokeException;
import catrpc.message.RPCMessage;
import catrpc.message.RPCRequest;
import catrpc.message.RPCResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

            if (msg instanceof RPCMessage) {
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RPCMessage) msg).getMessageType();
                RPCMessage rpcMessage = new RPCMessage();
                rpcMessage.setCodec(ConfigConstant.SERIALIZER);
                rpcMessage.setCompress(ConfigConstant.COMPRESS);
                if (messageType == MessageConstant.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(MessageConstant.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(MessageConstant.PONG);
                } else {
                    RPCRequest rpcRequest = (RPCRequest) ((RPCMessage) msg).getData();
                    // Execute the target method (the method the client needs to execute) and return the method result
                    Object result = handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(MessageConstant.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RPCResponse<Object> rpcResponse = new RPCResponse<>();
                        rpcResponse.setResult(result);
                        //这里设立id与请求一致
                        rpcResponse.setRequestId(rpcRequest.getRequestId());
                        //标识码 200成功
                        rpcResponse.setCode(MessageConstant.RESPONSE_CODE_SUCESS);
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RPCResponse<Object> rpcResponse = new RPCResponse<>();
                        rpcResponse.setCode(MessageConstant.RESPONSE_CODE_FAIL);
                        //这里设立id与请求一致
                        rpcResponse.setRequestId(rpcRequest.getRequestId());
                        rpcResponse.setMessage("服务器错误");
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }else {
                //直接忽略
                log.info("忽略非法的请求消息类型");
            }

    }


    public Object handle(RPCRequest rpcRequest) {

        Object service = NettyRpcServer.getService(rpcRequest.getServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    //真正的执行服务方法
    private Object invokeTargetMethod(RPCRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RPCInvokeException("调用实际方法时错误");
        }
        return result;
    }


    //读空闲，由于心跳检测的存在，可以推断出客户端已经断连或者故障，断开channel连接
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    //统一异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }

}
