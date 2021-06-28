package work.yj1211.live.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BanInfo implements Serializable {
    private String type;
    private String content;
    private String platform;
}
