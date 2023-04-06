package work.yj1211.live.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import work.yj1211.live.model.platformArea.AreaTypeIndex;

/**
 * @Author: YJ1211
 * @Data:2023/4/5 15:17
 */
@Mapper
@Repository
public interface AreaTypeIndexMapper extends BaseMapper<AreaTypeIndex> {
}
