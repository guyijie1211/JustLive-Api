package work.yj1211.live.model.platformArea;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author guyijie1211
 */
@TableName("area_info")
@Data
public class AreaInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("platform")
    private String platform;
    @TableField("area_type")
    private String areaType;
    @TableField("type_name")
    private String typeName;
    @TableField("area_id")
    private String areaId;
    @TableField("area_name")
    private String areaName;
    @TableField("area_pic")
    private String areaPic;
    @TableField("short_name")
    private String shortName;
    @TableField("index_area")
    private Integer indexArea;
    @TableField("index_type")
    private String indexType;
}
