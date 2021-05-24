package work.yj1211.live.utils.http;

public enum HttpContentType {

    JSON("application/json"),
    FORM("application/x-www-form-urlencoded"),
    TEXT("text/plain");

    private String contentType;

    HttpContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getValue() {
        return contentType;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
