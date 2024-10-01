package catrpc.exception;

public class RpcTransportException extends RuntimeException{
    public RpcTransportException(String msg) {
        super(msg);
    }
}
