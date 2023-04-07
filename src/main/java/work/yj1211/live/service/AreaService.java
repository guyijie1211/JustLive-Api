package work.yj1211.live.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.model.platformArea.AreaTypeIndex;
import work.yj1211.live.model.platformArea.AreaTypeIndexService;
import work.yj1211.live.service.mysql.AreaInfoIndexService;
import work.yj1211.live.service.mysql.AreaInfoService;
import work.yj1211.live.service.platforms.BasePlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guyijie
 * @date 2023/4/7 12:31
 **/
@Slf4j
@Service
public class AreaService {
    @Autowired
    private AreaInfoIndexService areaInfoIndexService;
    @Autowired
    private AreaTypeIndexService areaTypeIndexService;
    @Autowired
    private AreaInfoService areaInfoService;

    private final List<BasePlatform> platformList;
    @Autowired
    public AreaService(List<BasePlatform> platforms){
        platformList = platforms;
    }

    /**
     * 批量处理数量
     */
    private int batchNum = 500;

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
    void saveOrUpdateBatchByPlatform(List<AreaInfo> areaList, String platform) {
        log.info("获取到【{}】分类信息【{}】条", platform, areaList.size());

        List<AreaInfo> updateList = new ArrayList<>();
        List<AreaInfo> insertList = new ArrayList<>();

        AreaInfo areaInfoSelect = new AreaInfo();
        areaInfoSelect.setPlatform(platform);
//        Map<String, Integer> areaInfoList = areaInfoService.list(new QueryWrapper<>(areaInfoSelect)).stream().collect(Collectors.toMap(areaInfo -> {}));
//        areaList.forEach(areaNew -> {
//            if (areaInfoList.contains(areaNew.getPlatform() + areaNew.getAreaName())) {
//                areaNew.setId();
//                updateList.add(areaNew);
//            } else {
//                insertList.add(areaNew);
//            }
//        });

        areaInfoService.saveBatch(insertList, batchNum);
        areaInfoService.updateBatchById(updateList, batchNum);

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
