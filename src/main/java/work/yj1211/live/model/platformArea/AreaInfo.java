package work.yj1211.live.model.platformArea;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author YJ1211
 */
@TableName("area_info")
@Data
public class AreaInfo {
    @TableField("id")
    private int id;
    @TableField("platform")
    private String platform;
    @TableField("areaType")
    private String areaType;
    @TableField("typeName")
    private String typeName;
    @TableField("areaId")
    private String areaId;
    @TableField("areaName")
    private String areaName;
    @TableField("areaPic")
    private String areaPic;
    @TableField("shortName")
    private String shortName;
}
