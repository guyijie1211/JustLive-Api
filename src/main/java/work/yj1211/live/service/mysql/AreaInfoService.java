package work.yj1211.live.service.mysql;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.AreaInfoMapper;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.model.platformArea.AreaTypeIndex;
import work.yj1211.live.model.platformArea.AreaTypeIndexService;
import work.yj1211.live.service.platforms.BasePlatform;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author YJ211
 */
@Slf4j
@Service
public class AreaInfoService extends ServiceImpl<AreaInfoMapper, AreaInfo> {
    @Autowired
    private AreaInfoIndexService areaInfoIndexService;
    @Autowired
    private AreaTypeIndexService areaTypeIndexService;

    private final List<BasePlatform> platformList;
    @Autowired
    public AreaInfoService(List<BasePlatform> platforms){
        platformList = platforms;
    }

    /**
     * 刷新所有平台分区
     */
    public void refreshAreasAll() {
        Map<String, String> areaTypeNameMap = getAreaTypeNameMap();

        platformList.forEach(platform -> {
            List<AreaInfo> areaInfoList = platform.getAreaList();
        });
    }

    /**
     * 根据平台更新分类信息
     * @param areaList 平台分类列表
     * @param platform 平台
     */
     void saveBatchByPlatform(List<AreaInfo> areaList, String platform) {
        log.info("获取到【{}】分类信息【{}】条", platform, areaList.size());
        removeAreasByPlatform(platform);
    }

    /**
     * 根据平台名，删除表里的分区信息
     * @param platform 平台名
     */
    private void removeAreasByPlatform(String platform) {
        log.info("删除【{}】分类信息", platform);
        QueryWrapper<AreaInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("platform", platform);
        this.remove(queryWrapper);
    }

    /**
     * 获取AreaTypeName映射关系
     * @return Map<平台+areaTypePlatform, areaTypeIndex>
     */
    public Map<String, String> getAreaTypeNameMap() {
        List<AreaTypeIndex> areaTypeIndexList = areaTypeIndexService.list();
        return areaTypeIndexList.stream().collect(
                Collectors.toMap(areaTypeIndex -> areaTypeIndex.getPlatform() + areaTypeIndex.getAreaTypePlatform(), AreaTypeIndex::getAreaType)
        );
    }
}
