package work.yj1211.live.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlUtil {

    /**
     * 为 url 添加 get 参数
     *
     * @return 返回拼装之后的 url
     */
    public static String appendQueryParameter(String url, String key, String value) {
        if (url.contains("?")) {
            if (!url.endsWith("&")) {
                url += "&";
            }
        } else {
            url += "?";
        }

        url += key + "=" + encode(value);

        return url;
    }

    /**
     * 对 url 参数编码
     */
    public static String encode(String str, String charsetName) {
        try {
            return URLEncoder.encode(str, charsetName);
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }

    /**
     * 使用 UTF8 编码对 url 参数编码
     */
    public static String encode(String str) {
        return encode(str, "UTF-8");
    }


}
