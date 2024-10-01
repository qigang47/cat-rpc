package catrpc.transport.codec;

import catrpc.constant.MessageConstant;
import catrpc.constant.VersionConstant;
import catrpc.exception.CodecException;
import catrpc.message.RPCMessage;
import catrpc.message.RPCRequest;
import catrpc.message.RPCResponse;
import catrpc.transport.codec.compress.Compress;
import catrpc.transport.codec.compress.CompressFatory;
import catrpc.transport.codec.serialize.Serializer;
import catrpc.transport.codec.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Arrays;

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
public class MessageDecoder extends LengthFieldBasedFrameDecoder {


    public MessageDecoder(){


        this(VersionConstant.MEX_MESSAGE_SIZE,5, 4, -9, 0);

    }


    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        Object decoded = super.decode(ctx, in);
        System.out.println("decoded:"+decoded);
        if (decoded!=null){
            ByteBuf buf = (ByteBuf)decoded;
            checkMagicNumberAndVersion(buf);

            int fullLen = buf.readInt();
            //跳过扩展头部
            buf.readByte();
            Byte type = buf.readByte();
            Byte compressType = buf.readByte();
            Byte serializerType = buf.readByte();
            int requestId = buf.readInt();

            RPCMessage message = new RPCMessage();


            if (type == MessageConstant.HEARTBEAT_REQUEST_TYPE) {
                message.setData(MessageConstant.PING);
                return message;
            }
            if (type == MessageConstant.HEARTBEAT_RESPONSE_TYPE) {
                message.setData(MessageConstant.PONG);
                return message;
            }

            //如果消息体不为零
            if(fullLen-VersionConstant.HEAD_SIZE>0){
                byte[] byteArr = new byte[fullLen-VersionConstant.HEAD_SIZE];
                buf.readBytes(byteArr);
                Serializer serializer = SerializerFactory.getInstance(serializerType);
                Compress compress = CompressFatory.getInstance(compressType);
                byteArr = compress.decompress(byteArr);

                if(type == MessageConstant.REQUEST_TYPE){

                    RPCRequest request = serializer.deserialize(byteArr,RPCRequest.class);
                    message.setData(request);
                }else {

                    RPCResponse response = serializer.deserialize(byteArr, RPCResponse.class);
                    message.setData(response);
                }

            }
            return message;


        }

        return null;

    }




    private void checkMagicNumberAndVersion(ByteBuf in) {

        int len = 4;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != MessageConstant.MAGIC_NUMBER[i]) {
                throw new CodecException("Decode erro ,Unknown magic code: " + Arrays.toString(tmp));
            }
        }
        byte version = in.readByte();
        if (version != VersionConstant.VERSION) {
            throw new CodecException("Decode erro ,version isn't compatible" + version);
        }

    }



}
