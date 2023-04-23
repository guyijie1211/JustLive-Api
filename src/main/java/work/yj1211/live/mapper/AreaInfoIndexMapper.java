package work.yj1211.live.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import work.yj1211.live.model.platformArea.AreaInfoIndex;

/**
 * @author YJ211
 */
@Mapper
@Repository
public interface AreaInfoIndexMapper extends BaseMapper<AreaInfoIndex> {
}
