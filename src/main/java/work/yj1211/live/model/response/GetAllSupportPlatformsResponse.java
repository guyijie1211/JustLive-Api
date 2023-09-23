package work.yj1211.live.model.response;

import lombok.Data;

import java.util.List;

@Data
public class GetAllSupportPlatformsResponse {
    private List<PlatformInfo> platformList;

    @Data
    public static class PlatformInfo {
        private String name;
        private String code;
        private String logoImage;
        private Boolean androidDanmuSupport;
    }
}
