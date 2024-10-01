package catrpc.exception;

public class RpcSerializationException extends RuntimeException{
    public RpcSerializationException(String msg){
        super(msg);
    }
}
