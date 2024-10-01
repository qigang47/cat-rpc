package catrpc.transport.codec.serialize.serializerImpl;

import catrpc.exception.RpcSerializationException;
import catrpc.message.RPCRequest;
import catrpc.message.RPCResponse;
import catrpc.transport.codec.serialize.Serializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
@Slf4j
public class KryoSerializer implements Serializer {


    @Getter
    private static final KryoSerializer kryoSerializer = new KryoSerializer();
    /**
     * Because Kryo is not thread safe. So, use ThreadLocal to store Kryo objects
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RPCResponse.class);
        kryo.register(RPCRequest.class);
        kryo.register(Class.class);
        kryo.register(Class[].class);
        kryo.register(Object[].class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {

        System.out.println(111);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {

            System.out.println(obj);
            System.out.println("data类型："+obj.getClass());

            Kryo kryo = kryoThreadLocal.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        }catch (Exception e) {
            System.out.println(e.getClass());
            throw new RpcSerializationException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        } catch (Exception e) {
            throw new RpcSerializationException("Deserialization failed");
        }
    }

}
