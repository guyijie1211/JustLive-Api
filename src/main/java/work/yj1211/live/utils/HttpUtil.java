package work.yj1211.live.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import work.yj1211.live.utils.http.HttpRequest;

import java.io.IOException;
import java.util.Map;

public class HttpUtil {
    /**
     * get请求
     * @return
     */
    public static String doGet(String url) {
        return HttpRequest.create(url)
                .get().getBody();
    }

    public static String doGetWithHeaders(String url, Map<String, String> headers) {
        return HttpRequest.create(url).putHeaders(headers)
                .get().getBody();
    }
}
