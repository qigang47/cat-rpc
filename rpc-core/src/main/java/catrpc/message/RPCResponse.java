package catrpc.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class RPCResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    private String requestId; // 表示对该 requestId 的请求进行响应
    private T result;
    //响应信息
    private String message;

    //响应码
    private int code;
}
