package catrpc.constant;

import catrpc.utils.CheckUtil;
import catrpc.utils.ConfigLoader;

import java.util.Properties;

public class ConfigConstant {

    public static final String DEFAULT_PROPERTY_FILENAME = "catrpc.properties";

    public static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    public static final byte SERIALIZER;
    public static final byte COMPRESS;

    public static final byte LOADBALANCE;
    public static final String ZOOKEEPER_ADDRESS;

    static {

        Properties properties = ConfigLoader.readPropertiesFile(DEFAULT_PROPERTY_FILENAME);

        //没配置文件就都使用默认值
        if(properties == null){
            SERIALIZER = MessageConstant.SERIALIZER_KRYO;
            COMPRESS = MessageConstant.COMPRESS_GZIP;
            ZOOKEEPER_ADDRESS = DEFAULT_ZOOKEEPER_ADDRESS;
            LOADBALANCE = MessageConstant.LOADBALANCE_RANDOM;
        }else {

            String serializer = properties.getProperty("serializer");

            if("kryo".equals(serializer)){
                SERIALIZER = MessageConstant.SERIALIZER_KRYO;
            }else if("jdk".equals(serializer)){
                SERIALIZER = MessageConstant.SERIALIZER_KRYO;
            }else {
                //默认值
                SERIALIZER = MessageConstant.SERIALIZER_KRYO;
            }

            String compress = properties.getProperty("compress");

            if("gzip".equals(compress)){
                COMPRESS = MessageConstant.COMPRESS_GZIP;
            }else{
                COMPRESS = MessageConstant.COMPRESS_GZIP;
            }


            String zk = properties.getProperty("zookeeper");
            if (zk!=null&& CheckUtil.isValidIPAndPort(zk)){
                ZOOKEEPER_ADDRESS = zk;
            }else {
                ZOOKEEPER_ADDRESS = DEFAULT_ZOOKEEPER_ADDRESS;
            }

            String lb = properties.getProperty("loadbalance");
            if ("random".equals(lb)){
                LOADBALANCE = MessageConstant.LOADBALANCE_RANDOM;
            }else if ("round_robin".equals(lb)){
                LOADBALANCE = MessageConstant.LOADBALANCE_ROUND_ROBIN;
            }else {
                LOADBALANCE = MessageConstant.LOADBALANCE_RANDOM;
            }
        }

    }






}
