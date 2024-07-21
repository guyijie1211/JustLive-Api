package work.yj1211.live.enums;

public enum PlayUrlType {
    FLV("flv"),
    HLS("hls");

    public String getTypeName() {
        return typeName;
    }

    private String typeName;

    PlayUrlType(String typeName) {
        this.typeName = typeName;
    }
}
