package catrpc.exception;

public class RpcRegistryException extends RuntimeException{

    public RpcRegistryException(String message){

        super("A RpcRegistryException:"+message);

    }
}
