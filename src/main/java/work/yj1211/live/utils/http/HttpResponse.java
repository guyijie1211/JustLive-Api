package work.yj1211.live.utils.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponse {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResponse.class);
    public static final int CODE_ERROR = -1;
    /**
     * 从开始发送请求到响应结束的耗时（毫秒）
     */
    private long spend;

    /**
     * http 状态码
     */
    private int code;

    /**
     * 请求失败时的异常
     */
    private Exception error;
    private String message;
    private HttpRequest request;
    private String body;
    private byte[] bodyBytes;
    private Map<String, String> headers = new HashMap<>();
    private String encoding;
    private String contentType;

    private static final Pattern PATTERN_MATCH_CHARSET = Pattern.compile("(?<=charset=)[a-z0-9\\-]+", Pattern.CASE_INSENSITIVE);

    private HttpResponse() {
    }

    public static HttpResponse create() {
        return create(null, 0);
    }

    static HttpResponse create(HttpRequest httpRequest, long spendTime) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setRequest(httpRequest);
        httpResponse.setSpend(spendTime);
        return httpResponse;
    }

    HttpResponse message(String message) {
        this.message = message;
        return this;
    }

    HttpResponse statusCode(int statusCode) {
        this.code = statusCode;
        return this;
    }

    public HttpResponse body(String body) {
        this.body = body;
        return this;
    }

    HttpResponse body(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
        return this;
    }

    HttpResponse header(org.apache.http.HttpResponse response) {
        for (Header item : response.getAllHeaders()) {
            headers.put(item.getName(), item.getValue());
            // 从 header 的 content-type 声明中获取编码
            if (item.getName().equalsIgnoreCase(HeaderKey.CONTENT_TYPE)) {
                Matcher matcher = PATTERN_MATCH_CHARSET.matcher(item.getValue());
                if (matcher.find()) {
                    this.encoding = matcher.group();
                }
            }
        }
        return this;
    }

    HttpResponse error(Exception error) {
        this.code = CODE_ERROR;
        this.error = error;
        this.message = error.getLocalizedMessage();
        return this;
    }

    /**
     * 将 body 以字符串的形式读取
     */
    public String getBody() {

        if (bodyBytes == null) {
            return body;
        }

        String contentType = headers.get(HeaderKey.CONTENT_TYPE);
        this.contentType = Optional.ofNullable(contentType).orElse("");
        if (StringUtils.isNotEmpty(contentType) && contentType.toLowerCase().contains("image")) {
            return Base64.encodeBase64String(bodyBytes);
        }

        if (StringUtils.isEmpty(this.encoding)) {
            body = Strings.fromUTF8ByteArray(this.bodyBytes);
        } else {
            try {
                body = new String(this.bodyBytes, this.encoding);
            } catch (UnsupportedEncodingException e) {
                LOG.error("错误的编码：" + this.encoding);
            }
        }

        return body;
    }

    public JSONObject getBodyJson() {
        if (success()) {
            return JSONObject.parseObject(body);
        }
        return JSONObject.parseObject(null);
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * 响应是否成功
     */
    public boolean success() {
        return error == null;
    }

    /**
     * 当前实体是否为缓存实体(一般通过create()构造缓存实体)
     */
    public boolean isCacheEntity() {
        return request == null;
    }

    /**
     * 将当前响应对象输出到日志
     */
    public void log() {
        if (success()) {
            LOG.info("http success({} ms) for {} ({} : {}), Body : {}", spend, request.getUrl(), code, message, getBody().replace(System.lineSeparator(), ""));
        } else {
            LOG.error("http fail({} ms) for {} ({} : {}), Body : {}", spend, request.getUrl(), code, message, getBody(), error);
        }
    }

    public long getSpend() {
        return spend;
    }

    public int getCode() {
        return code;
    }

    public Exception getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }

    public static final class HeaderKey {
        public static final String CONTENT_TYPE = "Content-Type";
    }
}
