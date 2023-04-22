package work.yj1211.live.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.model.platformArea.AreaInfoIndex;
import work.yj1211.live.model.platformArea.AreaTypeIndex;
import work.yj1211.live.model.platformArea.AreaTypeIndexService;
import work.yj1211.live.service.mysql.AreaInfoIndexService;
import work.yj1211.live.service.mysql.AreaInfoService;
import work.yj1211.live.service.platforms.BasePlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guyijie1211
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
    private final int batchNum = 500;

    /**
     * 刷新所有平台分区
     */
    public void refreshAreasAll() {
        // 刷新所有平台分区
        platformList.forEach(platform -> {
            saveOrUpdateBatchByPlatform(platform.getAreaList(), platform.getPlatformName());
        });
    }

    private Map<String, String> douyuAreaNameMap;

    /**
     * 根据平台更新分类信息
     * @param areaList 平台分类列表
     * @param platform 平台
     */
    public void saveOrUpdateBatchByPlatform(List<AreaInfo> areaList, String platform) {
        Map<String, String> areaTypeNameMap = getAreaTypeNameMap();
        Map<String, Integer> areaIndexMap = getAreaIndexMap();
        log.info("获取到【{}】分类信息【{}】条", platform, areaList.size());
        // 更新列表
        List<AreaInfo> updateList = new ArrayList<>();
        // 插入列表
        List<AreaInfo> insertList = new ArrayList<>();
        // 查询所有表中分类信息
        QueryWrapper<AreaInfo> areaSelect = new QueryWrapper<>();
        areaSelect.eq("platform", platform);
        List<AreaInfo> areaInfoList = areaInfoService.list(areaSelect);
        Map<String, Integer> areaInfoMap = areaInfoList.stream()
                .collect(Collectors.toMap(areaInfo -> areaInfo.getPlatform() + areaInfo.getAreaId(), AreaInfo::getId, (a1, a2) -> a1));

        areaList.forEach(areaNew -> {
            // 写入映射后的分区类型
            String areaTypeIndexString = areaNew.getPlatform() + areaNew.getTypeName();
            if (!areaTypeNameMap.containsKey(areaTypeIndexString)) { // 没有设置过这个类型映射，跳过
                log.error("platform:【{}】typeName:【{}】没有配置过这个类型映射，尽快在area_type_index表添加配置!!", areaNew.getPlatform(), areaNew.getTypeName());
                return;
            }

            // 判断是更新还是新增
            String areaIndexString = areaNew.getPlatform() + areaNew.getAreaId();
            if (areaInfoMap.containsKey(areaIndexString)) { // 已存在, 更新
                areaNew.setId(areaInfoMap.get(areaIndexString));
                updateList.add(areaNew);
            } else { // 新分区, 插入
                areaNew.setId(0);
                areaNew.setIndexType(areaTypeNameMap.get(areaTypeIndexString));
                if (areaIndexMap.containsKey(areaNew.getAreaName())) { // 存在同名的映射后分区
                    areaNew.setIndexArea(areaIndexMap.get(areaNew.getAreaName()));
                } else { // 未映射过的分区
                    // 插入行的映射信息
                    AreaInfoIndex areaIndexNew = new AreaInfoIndex();
                    areaIndexNew.setAreaName(areaNew.getAreaName());
                    areaIndexNew.setTypeName(areaTypeNameMap.get(areaTypeIndexString));
                    areaIndexNew.setPriority(System.currentTimeMillis()); // 优先级用时间戳，越早插入表的分区越优先
                    areaIndexNew.setAreaPic(areaNew.getAreaPic());
                    areaInfoIndexService.save(areaIndexNew);
                    // 给areaNew设置新的插入的映射的id
                    areaNew.setIndexArea(areaIndexNew.getId());
                }
                insertList.add(areaNew);
            }
        });
        areaInfoService.saveBatch(insertList, batchNum);
        areaInfoService.updateBatchById(updateList, batchNum);
    }

    /**
     * 获取所有分区列表
     * @return
     */
    public List<List<AreaInfoIndex>> getAllAreas() {
        List<List<AreaInfoIndex>> resultList = new ArrayList<>();
        // 获取index表所有分区, 并转成Map<areaType,List<areaInfoIndex>> 形式
        List<AreaInfoIndex> areaInfoIndexList = areaInfoIndexService.list();
        Map<String, List<AreaInfoIndex>> resultMap = new HashMap<>();
        areaInfoIndexList.forEach(areaInfoIndex -> {
            String typeName = areaInfoIndex.getTypeName();
            if (resultMap.containsKey(typeName)) {
                resultMap.get(typeName).add(areaInfoIndex);
            } else {
                List<AreaInfoIndex> list = new ArrayList<>();
                list.add(areaInfoIndex);
                resultMap.put(areaInfoIndex.getTypeName(), list);
            }
        });
        // 获取areaTypeIndex的顺序,并按照顺序返回结果
        QueryWrapper<AreaTypeIndex> wrapper = new QueryWrapper<>();
        wrapper.select("distinct area_type");
        List<AreaTypeIndex> typeList =  areaTypeIndexService.list(wrapper);
        typeList.forEach(type->{
            if (resultMap.containsKey(type.getAreaType())) {
                resultList.add(resultMap.get(type.getAreaType()));
            } else {
                resultList.add(new ArrayList<>());
            }
        });
        return resultList;
    }

    /**
     * 根据平台,获取所有分区列表
     * @param platform 平台名
     * @return
     */
    public List<List<AreaInfo>> getAllAreasByPlatform(String platform) {
        List<List<AreaInfo>> resultList = new ArrayList<>();
        // 获取平台的所有分区, 并转成Map<areaType,List<areaInfoIndex>> 形式
        LambdaQueryWrapper<AreaInfo> areaInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        areaInfoLambdaQueryWrapper.eq(AreaInfo::getPlatform, platform);
        List<AreaInfo> areaInfoList = areaInfoService.list(areaInfoLambdaQueryWrapper);
        // 转成Map
        Map<String, List<AreaInfo>> resultMap = new HashMap<>();
        areaInfoList.forEach(areaInfo -> {
            String typeName = areaInfo.getTypeName();
            if (resultMap.containsKey(typeName)) {
                resultMap.get(typeName).add(areaInfo);
            } else {
                List<AreaInfo> list = new ArrayList<>();
                list.add(areaInfo);
                resultMap.put(areaInfo.getTypeName(), list);
            }
        });
        // 获取areaType的顺序,并按照顺序返回结果
        QueryWrapper<AreaTypeIndex> wrapper = new QueryWrapper<>();
        wrapper.select("distinct area_type_platform").eq("platform", platform);
        List<AreaTypeIndex> typeList =  areaTypeIndexService.list(wrapper);
        typeList.forEach(type->{
            if (resultMap.containsKey(type.getAreaTypePlatform())) {
                resultList.add(resultMap.get(type.getAreaTypePlatform()));
            } else {
                resultList.add(new ArrayList<>());
            }
        });
        return resultList;
    }

    /**
     * 获取映射分区对应的所有平台的分区
     * @param areaType 映射后的分区类型
     * @param areaName 映射后的分区
     * @return Map< platform, AreaInfo>
     */
    public Map<String, AreaInfo> getPlatformAreaMap(String areaType, String areaName) {
        // 获取areaType, areaName 对应的映射id
        LambdaQueryWrapper<AreaInfoIndex> queryIndexWrapper = new LambdaQueryWrapper<>();
        queryIndexWrapper.eq(AreaInfoIndex::getTypeName, areaType).eq(AreaInfoIndex::getAreaName, areaName);
        AreaInfoIndex areaInfoIndex = areaInfoIndexService.getOne(queryIndexWrapper);

        // 根据映射id获取所有平台的分区信息
        LambdaQueryWrapper<AreaInfo> queryInfoWrapper = new LambdaQueryWrapper<>();
        queryInfoWrapper.eq(AreaInfo::getIndexArea, areaInfoIndex.getId());
        List<AreaInfo> resultList = areaInfoService.list(queryInfoWrapper);

        return resultList.stream().collect(Collectors.toMap(AreaInfo::getPlatform, Function.identity()));
    }

    /**
     * 获取斗鱼的分区名映射
     * @return
     */
    public Map<String, String> getDouyuAreaNameMap() {
        if (CollectionUtil.isEmpty(douyuAreaNameMap)) {
            QueryWrapper<AreaInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("distinct area_name, area_id").eq("platform", "douyu");
            List<AreaInfo> areaInfoList = areaInfoService.list(queryWrapper);
            douyuAreaNameMap = areaInfoList.stream().collect(Collectors.toMap(AreaInfo::getAreaId, AreaInfo::getAreaName));
        }
        return  douyuAreaNameMap;
    }

    /**
     * 获取AreaTypeName映射关系
     * @return Map<平台+areaTypePlatform, areaType>
     */
    private Map<String, String> getAreaTypeNameMap() {
        List<AreaTypeIndex> areaTypeIndexList = areaTypeIndexService.list();
        return areaTypeIndexList.stream().collect(
                Collectors.toMap(areaTypeIndex -> areaTypeIndex.getPlatform() + areaTypeIndex.getAreaTypePlatform(), AreaTypeIndex::getAreaType)
        );
    }

    /**
     * 获取Area映射关系
     * @return Map< areaName, id>
     */
    private Map<String, Integer> getAreaIndexMap() {
        List<AreaInfoIndex> areaIndexList = areaInfoIndexService.list();
        return areaIndexList.stream().collect(
                Collectors.toMap(AreaInfoIndex::getAreaName, AreaInfoIndex::getId)
        );
    }
}
