package catrpc.message;

import lombok.Data;

@Data
public class RPCMessage {


    private Object data;


    //TODO 使用枚举？ 如何由用户配置？
    private byte messageType;
    private byte codec;
    private byte compress;


    //跟随request和response
    private int requestId;


}
