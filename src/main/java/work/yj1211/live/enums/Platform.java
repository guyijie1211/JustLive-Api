package work.yj1211.live.enums;

/**
 * @author guyijie1211
 * @date 2023/3/28 16:41
 **/
public enum Platform {
    /**
     * 斗鱼
     */
    DOUYU("douyu", "斗鱼直播"),
    /**
     * 虎牙
     */
    HUYA("huya", "虎牙直播"),
    /**
     * 哔哩哔哩直播
     */
    BILIBILI("bilibili", "哔哩哔哩"),
    /**
     * 网易cc
     */
    CC("cc", "网易CC");

    private String code;
    private String name;

    Platform(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
