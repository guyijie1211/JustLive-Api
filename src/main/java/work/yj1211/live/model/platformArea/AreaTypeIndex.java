package work.yj1211.live.model.platformArea;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
* @Author: YJ1211
* @Data:2023/4/5 15:16

*/
@Data
@TableName("area_type_index")
public class AreaTypeIndex {
    private Integer id;

    private String areaType;

    private String platform;

    private String areaTypePlatform;
}