package catrpc.constant;

public class VersionConstant {

    //当前版本号
    public static final byte VERSION = 1;

    //不同版本可能不同的扩展头长度
    public static final byte EXTENT_HEAD_SIZE = 0;

    //固有的头长度
    public static final byte HEAD_SIZE = 17;

    public static final int MEX_MESSAGE_SIZE = 1024*5;

    //zk节点的路径
    public static final String ZK_REGISTER_ROOT_PATH = "/catrpc";

}
