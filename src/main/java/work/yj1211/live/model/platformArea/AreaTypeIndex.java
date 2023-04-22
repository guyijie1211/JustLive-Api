package work.yj1211.live.model.platformArea;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
* @Author: guyijie1211
* @Data:2023/4/5 15:16

*/
@Data
@TableName("area_type_index")
public class AreaTypeIndex {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String areaType;

    private String platform;

    private String areaTypePlatform;
}