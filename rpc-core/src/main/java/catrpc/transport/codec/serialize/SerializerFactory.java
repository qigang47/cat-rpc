package catrpc.transport.codec.serialize;

import catrpc.constant.ConfigConstant;
import catrpc.constant.MessageConstant;
import catrpc.transport.codec.serialize.serializerImpl.KryoSerializer;

public final class SerializerFactory {





    private SerializerFactory(){}


    public static Serializer getInstance(Byte serializerType){

        if (serializerType == MessageConstant.SERIALIZER_KRYO){
            return KryoSerializer.getKryoSerializer();
        }

        //还有更多待实现....

        return KryoSerializer.getKryoSerializer();

    }

    public static Serializer getInstance(){
        return getInstance(ConfigConstant.SERIALIZER);
    }

}
