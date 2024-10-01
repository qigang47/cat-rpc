package catrpc.transport.codec;


import catrpc.constant.MessageConstant;
import catrpc.constant.VersionConstant;
import catrpc.message.RPCMessage;
import catrpc.transport.codec.compress.Compress;
import catrpc.transport.codec.compress.CompressFatory;
import catrpc.transport.codec.serialize.Serializer;
import catrpc.transport.codec.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.concurrent.atomic.AtomicInteger;

/*
*
*
*
* +-------------+----------------+--------------+-------------+-------------+--------------+-------------+------------+
| Magic Code  | Version   | Full Length    | Extended Header Size    | Msg Type    | Compress     | Codec       | Request ID  |
+-------------+----------------+--------------+-------------+-------------+--------------+-------------+--------------+
| (4 bytes)   | (1 byte)  | (4 bytes)      | (1 bytes)               | (1 byte)    | (1 byte)     | (1 byte)    | (4 bytes)   |
+-------------+----------------+--------------+-------------+-------------+--------------+-------------+--------------+
|                                                                                                                     |
|                                      Message Body                                                                   |
|                                                                                                                     |
|                                                                                                                     |
+------------------------------------------+---------------------------------------------------------------------------+
*
*/
public class MessageEncoder extends MessageToByteEncoder<RPCMessage> {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);


    //TODO 不同版本的encode方法需要不同实现,例如对于读扩展头部的逻辑
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RPCMessage rpcMessage, ByteBuf byteBuf) throws Exception {

        byteBuf.writeBytes(MessageConstant.MAGIC_NUMBER);
        byteBuf.writeByte(VersionConstant.VERSION);
        int p = byteBuf.writerIndex();
        //空出长度统计的4个字节
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        //这个版本的实现没有扩展头长度
        byteBuf.writeByte(VersionConstant.EXTENT_HEAD_SIZE);
        byteBuf.writeByte(rpcMessage.getMessageType());
        byteBuf.writeByte(rpcMessage.getCompress());
        byteBuf.writeByte(rpcMessage.getCodec());
        byteBuf.writeInt(ATOMIC_INTEGER.getAndIncrement());
        byte[] bodyBytes = null;
        if(!(rpcMessage.getMessageType()==MessageConstant.HEARTBEAT_REQUEST_TYPE||
                rpcMessage.getMessageType()==MessageConstant.HEARTBEAT_RESPONSE_TYPE)){

            Serializer serializer = SerializerFactory.getInstance();
            bodyBytes = serializer.serialize(rpcMessage.getData());
            Compress compress = CompressFatory.getInstance();
            bodyBytes = compress.compress(bodyBytes);

        }

        int p2 = byteBuf.writerIndex();
        if (bodyBytes!=null){
            byteBuf.writeBytes(bodyBytes);
            p2 = byteBuf.writerIndex();
            //返回去写长度
            byteBuf.writerIndex(p);
            byteBuf.writeInt(bodyBytes.length+VersionConstant.HEAD_SIZE+VersionConstant.EXTENT_HEAD_SIZE);
        }else {
            //返回去写长度
            byteBuf.writerIndex(p);
            byteBuf.writeInt(VersionConstant.HEAD_SIZE+VersionConstant.EXTENT_HEAD_SIZE);
        }

        //这里应该不用重置写指针到末尾吧
        byteBuf.writerIndex(p2);
    }
}
