package work.yj1211.live.utils.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.yj1211.live.utils.Constant;
import work.yj1211.live.utils.UrlUtil;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

public class HttpRequest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);
    private static PoolingHttpClientConnectionManager sConnectionManager = null;
    /**
     * 默认的 User-Agent
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";

    public static HttpRequest create(String url) {
        return new HttpRequest(url);
    }

    private String url;
    private HttpContentType contentType;
    private String encoding = Constant.ENCODING_UTF8;
    private Map<String, String> headers;
    private String body;
    private boolean isForBytes;
    /**
     * 可以传get参数，也可以传post key-value形式参数
     */
    private Map<String, Object> paramMap = new HashMap<>();

    private int retryCount = 1;
    private long retryDelay = 0;
    private int connectTimeout = 10 * 1000;
    private int socketTimeout = 10 * 1000;
    private boolean needPrintLog = true;
    private Function<HttpResponse, Boolean> validator;

    private HttpRequest(String url) {
        this.url = url;
        headers = new HashMap<>();
        headers.put("USER-AGENT", USER_AGENT);
    }

    /**
     * 设置 body
     */
    public HttpRequest setBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * 添加GET或POST请求参数
     */
    public HttpRequest appendParameter(String key, Object value) {
        this.paramMap.put(key, value);
        return this;
    }

    /**
     * 添加多个GET或POST请求参数
     */
    public HttpRequest appendParameters(Map<String, Object> postParam) {
        this.paramMap.putAll(postParam);
        return this;
    }

    /**
     * 设置 content type，无默认值。
     */
    public HttpRequest setContentType(HttpContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 同时设置 connectTimeout 和 socketTimeout，单位毫秒，默认 10 秒。
     */
    public HttpRequest setTimeout(int timeout) {
        this.connectTimeout = timeout;
        this.socketTimeout = timeout;
        return this;
    }

    /**
     * 设置 http 连接的超时时间，单位毫秒，默认 10 秒。
     */
    public HttpRequest setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置 http 读取响应的超时时间，单位毫秒，默认 10 秒。
     */
    public HttpRequest setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }


    /**
     * 当请求失败时的重试次数，默认不重试。
     */
    public HttpRequest setRetryCount(int retryCount) {
        this.retryCount = retryCount > 0 ? retryCount : this.retryCount;
        return this;
    }

    /**
     * 重试延迟毫秒，默认0
     */
    public HttpRequest setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay > 0 ? retryDelay : this.retryDelay;
        return this;
    }

    /**
     * 添加 header
     */
    public HttpRequest putHeader(String key, String value) {
        // header name 统一使用大写
        key = key.toUpperCase();
        headers.put(key, value);
        return this;
    }

    /**
     * 添加多条 header
     */
    public HttpRequest putHeaders(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        return this;
    }


    /**
     * 发送请求时，对 body 的编码方式，默认 UTF-8。
     */
    public HttpRequest setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * 设置校验器
     */
    public HttpRequest setValidator(Function<HttpResponse, Boolean> validator) {
        this.validator = validator;
        return this;
    }

    public HttpRequest closeLog() {
        needPrintLog = false;
        return this;
    }

    public HttpRequest forBytes() {
        isForBytes = true;
        return this;
    }

    /**
     * 以 get 的 method 发送此请求
     */
    public HttpResponse get() {
        return sendRequest(HttpMethod.GET);
    }

    /**
     * 以 post 的 method 发送此请求
     */
    public HttpResponse post() {
        return sendRequest(HttpMethod.POST);
    }

    /**
     * 设置 content-type 为 application/json，并发送 post 请求。
     */
    public HttpResponse postJson() {
        setContentType(HttpContentType.JSON);
        return sendRequest(HttpMethod.POST);
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public String getBody() {
        return body;
    }

    private HttpResponse sendRequest(HttpMethod method) {
        HttpResponse result = null;
        // 重试
        for (int remainingCount = this.retryCount; remainingCount >= 0; remainingCount--) {

            result = doHttp(method);

            if (needPrintLog) {
                result.log();
            }

            if (HttpResponse.CODE_ERROR != result.getCode()) {
                break;
            }

            if (retryDelay > 0) {
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException e) {
                    LOG.warn("thread sleep error , {}", e.getMessage());
                }
            }
        }

        return result;
    }

    private HttpResponse doHttp(HttpMethod method) {
        HttpResponse response;

        long startTime = System.currentTimeMillis();
        try {

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setSocketTimeout(socketTimeout)
                            .setConnectTimeout(connectTimeout)
                            .build())

                    .setConnectionManager(sConnectionManager)
                    .build();

            HttpUriRequest httpUriRequest;
            switch (method) {
                case POST:
                    httpUriRequest = new HttpPost(url);

                    if (CollectionUtils.isNotEmpty(paramMap.keySet()) && !HttpContentType.JSON.equals(contentType)) {

                        // 键值对参数
                        List<NameValuePair> pairList = new ArrayList<>();
                        paramMap.forEach((key, value) -> pairList.add(new BasicNameValuePair(key, String.valueOf(value))));
                        ((HttpPost) httpUriRequest).setEntity(new UrlEncodedFormEntity(pairList, StandardCharsets.UTF_8));

                    } else {
                        // 如果body为空，就从Map转换成JSON串赋值给body
                        if (StringUtils.isEmpty(body) && HttpContentType.JSON.equals(contentType)) {
                            body = JSONObject.toJSONString(paramMap);
                        }

                        // 普通字符串参数
                        StringEntity entity = new StringEntity(body, encoding);
                        if (StringUtils.isNotEmpty(body)) {

                            if (contentType == null) {
                                throw new RuntimeException("请为 " + method.name() + " 请求设置 content-type。");
                            }
                            entity.setContentType(this.contentType.getValue());

                        }
                        ((HttpPost) httpUriRequest).setEntity(entity);
                    }

                    break;
                case GET:
                    paramMap.forEach((key, value) -> url = UrlUtil.appendQueryParameter(url, key, String.valueOf(value)));
                default:
                    httpUriRequest = new HttpGet(url);

            }

            headers.forEach(httpUriRequest::setHeader);

            StringBuilder sb = new StringBuilder("[");
            paramMap.forEach((key, value) -> sb.append(key).append(" = ").append(value).append(", "));
            sb.append("]");

            LOG.info("Request : URL = {}, headers = {}, encoding = {}, Content-Type = {}, paramMap = {}, body = {}", url, headers, encoding, contentType, sb.toString(), body);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpUriRequest)) {

                response = HttpResponse.create(this, System.currentTimeMillis() - startTime)
                        .message(httpResponse.getStatusLine().getReasonPhrase())
                        .statusCode(httpResponse.getStatusLine().getStatusCode())
                        .header(httpResponse);

                if (isForBytes) {
                    response.body(EntityUtils.toByteArray(httpResponse.getEntity()));
                } else {
                    response.body(EntityUtils.toString(httpResponse.getEntity(), encoding));
                }

                if (validator != null && !validator.apply(response)) {
                    // 校验不通过
                    response.error(new IllegalStateException("validate fail"));
                }
            } catch (Exception e) {
                response = HttpResponse.create(this, System.currentTimeMillis() - startTime).error(e);
            }

        } catch (Exception e) {
            response = HttpResponse.create(this, System.currentTimeMillis() - startTime).error(e);
        }

        return response;
    }

    static {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

            sConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            sConnectionManager.setMaxTotal(300);
            sConnectionManager.setDefaultMaxPerRoute(20);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOG.error("create HttpClientConnectionManager error", e);
        }
    }

    /**
     * do a Http Get.
     *
     * @param url
     * @param headers
     * @param listCookie
     * @return content, mostly a html page
     * @throws IOException
     */
    public static String getContent(String url, HashMap<String, String> headers, List<HttpCookie> listCookie) {
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
                // System.out.println(entry.getKey()+ " : " +entry.getValue());
            }
            // 设置Cookie
            if (listCookie != null) {
                StringBuilder sb = new StringBuilder();
                for (HttpCookie cookie : listCookie) {
                    sb.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
                }
                String cookie = sb.toString();
                if (cookie.endsWith("; ")) {
                    cookie = cookie.substring(0, cookie.length() - 2);
                }
                // System.out.println(cookie);
                conn.setRequestProperty("Cookie", cookie);
            }
            connectWithHostKnown(conn);
            String encoding = conn.getContentEncoding();
            InputStream ism = conn.getInputStream();
            if (encoding != null && encoding.contains("gzip")) {// 首先判断服务器返回的数据是否支持gzip压缩，
                // System.out.println(encoding);
                // 如果支持则应该使用GZIPInputStream解压，否则会出现乱码无效数据
                ism = new GZIPInputStream(ism);
            }
//			ism = new ChunkedInputStream(ism);
//			ism = new DeflaterInputStream(ism);
//			ism = new InflaterInputStream(new InflateWithHeaderInputStream(ism));
//			ism = new GZIPInputStream(ism);
//			ism = new ZipInputStream(ism);
            in = new BufferedReader(new InputStreamReader(ism, "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                // line = new String(line.getBytes(), "UTF-8");
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        // printCookie(manager.getCookieStore());
        return result.toString();
    }

    static int hostUnknownCnt = 0;
    private static void connectWithHostKnown(HttpURLConnection conn) throws IOException, InterruptedException {
        try {
            conn.connect();
            hostUnknownCnt = 0;
        } catch (UnknownHostException e) {
            hostUnknownCnt++;
            if(hostUnknownCnt <= 10) {
                Thread.sleep(1000);
                connectWithHostKnown(conn);
            }else {
                hostUnknownCnt = 0;
                throw e;
            }
        }
    }
}
