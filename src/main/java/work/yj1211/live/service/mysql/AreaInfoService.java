package work.yj1211.live.service.mysql;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.AreaInfoMapper;
import work.yj1211.live.model.platformArea.AreaInfo;

/**
 * @author YJ211
 */
@Slf4j
@Service
public class AreaInfoService extends ServiceImpl<AreaInfoMapper, AreaInfo> {
    /**
     * 根据平台名，删除表里的分区信息
     * @param platform 平台名
     */
    public void removeAreasByPlatform(String platform) {
        log.info("删除【{}】分类信息", platform);
        QueryWrapper<AreaInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("platform", platform);
        this.remove(queryWrapper);
    }
}
