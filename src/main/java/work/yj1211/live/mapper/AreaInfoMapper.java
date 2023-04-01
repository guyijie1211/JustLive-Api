package work.yj1211.live.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import work.yj1211.live.model.platformArea.AreaInfo;

/**
 * @author guyijie
 * @date 2023/3/29 18:55
 **/
@Mapper
@Repository
public interface AreaInfoMapper extends BaseMapper<AreaInfo> {

}
