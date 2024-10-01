package catrpc.utils;

public class CheckUtil {

    public static boolean isValidIPAndPort(String input) {
        // 正则表达式匹配 IPv4 地址和端口号
        String ipPortPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
                + ":"
                + "([0-9]{1,5})$";

        // 使用正则表达式进行匹配
        return input.matches(ipPortPattern) && Integer.parseInt(input.substring(input.lastIndexOf(":") + 1)) <= 65535;
    }
}
