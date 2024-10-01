package catrpc.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class RPCRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;


    private String requestId; // 请求的Id, 唯一标识该请求
    private String interfaceName; // 接口名称
    private String serviceVersion; // 版本
    private String methodName; // 方法名称
    private Class<?>[] parameterTypes; // 参数类型
    private Object[] parameters; // 具体参数

    public String getServiceName(){


        return (serviceVersion==null||serviceVersion=="")?(interfaceName+":"+"default"):(interfaceName+":"+serviceVersion);

    }

}
