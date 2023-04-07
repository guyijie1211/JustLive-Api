package work.yj1211.live.model.platformArea;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
    @TableId
    @TableField("platform")
    private String platform;
    @TableField("area_type")
    private String areaType;
    @TableId
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
    @TableField("index")
    private int indexId;
    @TableField("index_type")
    private String indexType;
}
