package work.yj1211.live.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String md5String(String s) throws NoSuchAlgorithmException {
        byte[] bs = MessageDigest.getInstance("MD5").digest(s.getBytes());
        StringBuilder sb = new StringBuilder(40);
        for (byte x : bs) {
            if ((x & 0xff) >> 4 == 0) {
                sb.append("0").append(Integer.toHexString(x & 0xff));
            } else {
                sb.append(Integer.toHexString(x & 0xff));
            }
        }
        return sb.toString();
    }
}
