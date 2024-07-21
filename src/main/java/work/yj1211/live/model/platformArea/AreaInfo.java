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
    /**
     * 分区类型id(123)
     */
    @TableField("area_type")
    private String areaType;
    /**
     * 分区类型名(网游竞技)
     */
    @TableField("type_name")
    private String typeName;
    /**
     * 分区id(123)
     */
    @TableField("area_id")
    private String areaId;
    /**
     * 分区名(Apex)
     */
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
