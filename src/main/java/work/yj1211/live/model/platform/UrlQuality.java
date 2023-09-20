package work.yj1211.live.model.platform;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class UrlQuality implements Comparable<UrlQuality> {
    /**
     * 显示名称(原画)
     */
    private String qualityName;
    /**
     * 优先级(越大越优先)
     */
    private int priority;
    /**
     * 直播源地址
     */
    private String playUrl;
    /**
     * 直播源类型(flv/hls)
     */
    private String urlType;
    /**
     * 线路(线路1,线路2)
     */
    private String sourceName;

    @Override
    public int compareTo(@NotNull UrlQuality o) {
        return o.priority - this.priority;
    }
}
