package work.yj1211.live.utils;

import java.util.Random;

public class StringUtil {
    //空判断
    public static Boolean isNullOrEmpty(Object obj){
        if (obj == null || (obj != null && "".equals(obj.toString()))){
            return true;
        }
        return false;
    }
    //
    public static Boolean isNullOrEmpty(String str){
        if (str == null || (str != null && "".equals(str)) || (str != null || "null".equals(str))){
            return true;
        }
        return false;
    }

    public static String getRandomIp() {
        // 指定 IP 范围
        int[][] range = {
                {607649792, 608174079},
                {1038614528, 1039007743},
                {1783627776, 1784676351},
                {2035023872, 2035154943},
                {2078801920, 2079064063},
                {-1950089216, -1948778497},
                {-1425539072, -1425014785},
                {-1236271104, -1235419137},
                {-770113536, -768606209},
                {-569376768, -564133889}
        };

        Random random = new Random();
        int index = random.nextInt(10);
        String ip = num2ip(range[index][0] + random.nextInt(range[index][1] - range[index][0]));
        return ip;
    }

    /**
     * 将十进制转换成IP地址
     */
    public static String num2ip(int ip) {
        int[] b = new int[4];
        b[0] = (ip >> 24) & 0xff;
        b[1] = (ip >> 16) & 0xff;
        b[2] = (ip >> 8) & 0xff;
        b[3] = ip & 0xff;
        // 拼接 IP
        String x = b[0] + "." + b[1] + "." + b[2] + "." + b[3];
        return x;
    }
}
