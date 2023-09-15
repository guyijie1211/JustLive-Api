package work.yj1211.live.model.platformArea;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
    * 平台分区映射后的分区
 * @author YJ211
 */
@Data
@TableName("area_info_index")
public class AreaInfoIndex {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 分区类型名
    */
    private String typeName;
    /**
    * 分区名
    */
    private String areaName;
    /**
    * 优先级(数值越大越优先)
    */
    private Long priority;
    /**
     * 分区图片链接
     */
    private String areaPic;
}