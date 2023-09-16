package work.yj1211.live.enums;

/**
 * @author guyijie1211
 * @date 2023/3/28 16:41
 **/
public enum Platform {
    /**
     * 斗鱼
     */
    DOUYU("douyu", "斗鱼直播", "https://imgsrc.baidu.com/forum/pic/item/297a0bfc1e178a823f26bdc2e103738da977e81c.jpg"),
    /**
     * 虎牙
     */
    HUYA("huya", "虎牙直播", "https://imgsrc.baidu.com/forum/pic/item/902397dda144ad3469d79d09dea20cf430ad85d7.jpg"),
    /**
     * 哔哩哔哩直播
     */
    BILIBILI("bilibili", "哔哩哔哩", ""),
//    /**
//     * 抖音直播
//     */
//    DOUYIN("douyin", "抖音直播", "https://imgsrc.baidu.com/forum/pic/item/0ff41bd5ad6eddc48041796937dbb6fd536633fa.jpg"),
    /**
     * 网易cc
     */
    CC("cc", "网易CC", "https://imgsrc.baidu.com/forum/pic/item/0ff41bd5ad6eddc48041796937dbb6fd536633fa.jpg");

    private String code;
    private String name;
    private String logoImage;

    Platform(String code, String name, String logoImage) {
        this.code = code;
        this.name = name;
        this.logoImage = logoImage;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLogoImage() {
        return logoImage;
    }
}
