package catrpc.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/*
*
* 读取文件配置
*
*
* */

public class ConfigLoader {
    public static Properties readPropertiesFile(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
            System.out.println("读取配置文件位置："+rpcConfigPath);
        }
        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            System.out.println("ConfigLoader erro! 读取配置文件失败");
            properties = null;
        }
        return properties;
    }

    //TODO
    public Properties loadYamlConfig(String configFile) throws IOException {

        return null;
    }
}