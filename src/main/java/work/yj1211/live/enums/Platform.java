package work.yj1211.live.enums;

/**
 * @author guyijie1211
 * @date 2023/3/28 16:41
 **/
public enum Platform {
    /**
     * 斗鱼
     */
    DOUYU("douyu"),
    /**
     * 虎牙
     */
    HUYA("huya"),
    /**
     * 哔哩哔哩直播
     */
    BILIBILI("bilibili"),
    /**
     * 网易cc
     */
    CC("cc");

    private  String name;

    Platform(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
