package catrpc.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MessageConstant {

    /**
     * Magic number. Verify RpcMessage
     */
    public static final byte[] MAGIC_NUMBER = {(byte)'c',(byte) 'a',(byte) 't',(byte) 't'};
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //version information
    //public static final byte VERSION = 1;

    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    //public static final byte HEAD_LENGTH = 16;
    public static final String PING = "ping...";
    public static final String PONG = "pong...";

    public static final byte SERIALIZER_KRYO = 10;

    public static final byte COMPRESS_GZIP = 20;

    public static final byte LOADBALANCE_ROUND_ROBIN = 30;
    public static final byte LOADBALANCE_RANDOM = 31;

    public static final int RESPONSE_CODE_FAIL = 400;
    public static final int RESPONSE_CODE_SUCESS = 200;
}
