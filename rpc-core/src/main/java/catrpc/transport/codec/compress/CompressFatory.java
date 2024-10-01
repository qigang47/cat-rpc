package catrpc.transport.codec.compress;

import catrpc.constant.ConfigConstant;
import catrpc.constant.MessageConstant;
import catrpc.transport.codec.compress.compressimpl.GzipCompress;

public class CompressFatory {


    public static Compress getInstance(){
        return getInstance(ConfigConstant.COMPRESS);
    }

    //根据不同的类型给不同的实现
    public static Compress getInstance(Byte type){

        if (type == MessageConstant.COMPRESS_GZIP){
            return GzipCompress.getGzipCompress();
        }

        //还有其他的类型实现...

        return GzipCompress.getGzipCompress();
    }




}
