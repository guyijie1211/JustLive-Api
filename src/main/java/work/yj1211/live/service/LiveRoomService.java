package work.yj1211.live.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.mapper.RoomMapper;
import work.yj1211.live.mapper.UserMapper;
import work.yj1211.live.model.app.UpdateInfo;
import work.yj1211.live.model.platform.LiveRoomInfo;
import work.yj1211.live.model.platform.Owner;
import work.yj1211.live.model.platform.SimpleRoomInfo;
import work.yj1211.live.model.platform.UrlQuality;
import work.yj1211.live.model.platformArea.AreaInfo;
import work.yj1211.live.model.platformArea.AreaInfoIndex;
import work.yj1211.live.model.response.GetAllSupportPlatformsResponse;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.thread.AsyncService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveRoomService{
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private AreaService areaService;

    private final Map<String, BasePlatform> platformMap;
    @Autowired
    public LiveRoomService(List<BasePlatform> platforms){
        platformMap = platforms.stream().collect(Collectors.toMap(BasePlatform::getPlatformCode, Function.identity(), (oldV, newV)-> newV));
    }

    /**
     * 获取总推荐
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommend(int page, int size){
        List<LiveRoomInfo> list = Collections.synchronizedList(new ArrayList<>());
        // 有几个平台就开几个线程
        int threadCount = Platform.values().length;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 遍历平台 提交获取推荐列表的任务
        platformMap.values().forEach(platform -> {
            executorService.execute(() -> list.addAll(platform.getRecommend(page, size)));
        });
        executorService.shutdown();
        try {
            // 阻塞，直到线程池里所有任务结束
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("获取总推荐报错:", e);
        }
        return list;
    }

    /**
     * 根据平台获取总推荐房间列表
     * @param platform
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendByPlatform(String platform, int page, int size){
        return platformMap.get(platform).getRecommend(page, size);
    }

    /**
     * 根据平台和分区获取推荐房间列表
     * @param platform
     * @param area
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendByPlatformArea(String platform, String area, int page, int size){
        return platformMap.get(platform).getAreaRoom(area, page, size);
    }

    /**
     * 获取真实直播地址
     * @param platform
     * @param roomId
     * @return
     */
    public Map<String, String> getRealUrl(String platform, String roomId){
        Map<String, String> urls = new HashMap<>();
        platformMap.get(platform).getRealUrl(urls, roomId);
        return urls;
    }

    /**
     * 获取真实直播地址
     * @param platform
     * @param roomId
     * @return
     */
    public Map<String, List<UrlQuality>> getRealUrlMultiSource(String platform, String roomId){
        return platformMap.get(platform).getRealUrl(roomId);
    };

    /**
     * 获取用户关注的所有房间信息
     * @param uid     用户uid
     * @return
     */
    public List<LiveRoomInfo> getRoomsByUid(String uid){
        List<LiveRoomInfo> roomList = new ArrayList<>();
        List<SimpleRoomInfo> simpleRoomInfoList = roomMapper.getRoomsByUid(uid);
        // 有几个平台就开几个线程
        int threadCount = Platform.values().length;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (SimpleRoomInfo simpleRoomInfo : simpleRoomInfoList) {
//            asyncService.addRoomInfo(uid, simpleRoomInfo.getPlatform(), simpleRoomInfo.getRoomId(), countDownLatch, roomList);
            // 遍历平台 提交获取推荐列表的任务
            BasePlatform platform = platformMap.get(simpleRoomInfo.getPlatform());
            executorService.execute(() -> roomList.add(platform.getRoomInfo(simpleRoomInfo.getRoomId())));
        }
        executorService.shutdown();
        try {
            // 阻塞，直到线程池里所有任务结束
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("getRoomsByUid报错:", e);
        }
        return roomList;
    }

    /**
     * 获取单个直播间信息
     * @param platform
     * @param roomId
     * @return
     */
    public LiveRoomInfo getRoomInfo(String uid, String platform, String roomId){
        LiveRoomInfo roomInfo = platformMap.get(platform).getRoomInfo(roomId);
        int isFollowed = roomMapper.ifIsFollowed(uid, platform, roomId);
        roomInfo.setIsFollowed((isFollowed == 0) ? 0 : 1);
        return roomInfo;
    }

    /**
     * 刷新所有平台分类
     */
    public void refreshArea(){
       areaService.refreshAreasAll();
    }

    /**
     * 刷新平台分类
     */
    public void refreshAreaByPlatform(String platform){
        areaService.refreshAreaByPlatform(platform);
    }

    /**
     * 刷新平台分类的缓存
     * @return 属性数据
     */
    public String refreshUpdate(){
        JSONObject jsonObject;
        try {
            jsonObject = JSONUtil.readJSONObject(FileUtil.file(Global.getUpdateFilePath()), StandardCharsets.UTF_8);
            Global.updateInfo = jsonObject.toBean(UpdateInfo.class);
            return JSONUtil.toJsonStr(jsonObject);
        } catch (Exception e) {
            log.error("刷新app新版本信息失败", e);
        }
        return null;
    }

    /**
     * 获取指定平台的分区信息
     * @param platform
     * @return
     */
    public List<List<AreaInfo>> getAreaMap(String platform){
        return areaService.getAllAreasByPlatform(platform);
    }

    /**
     * 获取总的分区列表
     * @return
     */
    public List<List<AreaInfoIndex>> getAllAreaMap(){
        return Global.AreaIndexList;
    }

    /**
     * 获取分区中所有平台的推荐列表
     * @param area
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendByAreaAll(String areaType, String area, int page, int size){
        List<LiveRoomInfo> list = Collections.synchronizedList(new ArrayList<>());
        // 获取映射分区对应的所有平台的分区
        Map<String, AreaInfo> platformAreaMap = areaService.getPlatformAreaMap(areaType, area);
        // 有几个平台就开几个线程
        ExecutorService executorService = Executors.newFixedThreadPool(platformAreaMap.size());
        // 遍历平台 提交获取推荐列表的任务
        platformAreaMap.forEach((platform, areaInfo) -> {
            executorService.execute(() -> list.addAll(getRecommendByPlatformArea(platform, areaInfo.getAreaName(), page, size)));
        });

        executorService.shutdown();
        try {
            // 阻塞，直到线程池里所有任务结束
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("getRecommendByAreaAll报错:", e);
        }
        return list;
    }

    /**
     * 搜索
     * @param platform 搜索的目标平台，"all"为搜索所有平台
     * @param keyWords 搜索关键字
     * @param uid 用户id
     * @return
     */
    public List<Owner> search(String platform, String keyWords, String uid){
        // 不允许未注册用户调用搜索, 防刷
        if (userMapper.findByUid(uid) == null) {
            return null;
        }
        // 去除关键字中的空格
        String finalKeyWords = keyWords.replaceAll(" ","");
        List<Owner> list = new ArrayList<>();
        try {
            if ("all".equalsIgnoreCase(platform)) {
                platformMap.values().forEach(basePlatform -> {
                    list.addAll(basePlatform.search(finalKeyWords));
                });
            } else {
                list.addAll(platformMap.get(platform).search(finalKeyWords));
            }
        } catch (Exception e) {
            log.error(StrUtil.format("搜索错误,keyword:{},平台:{}",keyWords,platform), e);
        }
        return list;
    }

    /**
     * 获取所有支持的平台
     *
     * @return
     */
    public GetAllSupportPlatformsResponse getAllSupportPlatforms() {
        GetAllSupportPlatformsResponse response = new GetAllSupportPlatformsResponse();
        List<GetAllSupportPlatformsResponse.PlatformInfo> platformList = new ArrayList<>();
        response.setPlatformList(platformList);
        Arrays.stream(Platform.values()).forEach(platform -> {
            GetAllSupportPlatformsResponse.PlatformInfo platformInfo = new GetAllSupportPlatformsResponse.PlatformInfo();
            platformInfo.setCode(platform.getCode());
            platformInfo.setName(platform.getName());
            platformInfo.setLogoImage(platform.getLogoImage());
            platformInfo.setAndroidDanmuSupport(platform.getAndroidDanmuSupport());
            platformList.add(platformInfo);
        });
        return response;
    }
}
