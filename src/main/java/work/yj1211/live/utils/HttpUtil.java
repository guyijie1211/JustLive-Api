package work.yj1211.live.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import work.yj1211.live.utils.http.HttpRequest;

import java.io.IOException;

public class HttpUtil {
    /**
     * get请求
     * @return
     */
    public static String doGet(String url) {
        return HttpRequest.create(url)
                .get().getBody();
    }
}
