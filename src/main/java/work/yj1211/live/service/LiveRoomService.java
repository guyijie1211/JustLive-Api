package work.yj1211.live.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.mapper.AllRoomsMapper;
import work.yj1211.live.mapper.RoomMapper;
import work.yj1211.live.mapper.UserMapper;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.platForms.*;
import work.yj1211.live.utils.thread.AsyncService;
import work.yj1211.live.vo.*;
import work.yj1211.live.vo.platformArea.AreaInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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

    private final Map<String,BasePlatform> platformMap;
    @Autowired
    public LiveRoomService(List<BasePlatform> platforms){
        platformMap = platforms.stream().collect(Collectors.toMap(BasePlatform::getType, Function.identity(), (oldV, newV)-> newV));
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
    };

    /**
     * 获取用户关注的所有房间信息
     * @param uid     用户uid
     * @return
     */
    public List<LiveRoomInfo> getRoomsByUid(String uid){
        List<LiveRoomInfo> roomList = new ArrayList<>();
        List<SimpleRoomInfo> simpleRoomInfoList = roomMapper.getRoomsByUid(uid);
        CountDownLatch countDownLatch = new CountDownLatch(simpleRoomInfoList.size());
        for(SimpleRoomInfo simpleRoomInfo : simpleRoomInfoList){
            asyncService.addRoomInfo(uid, simpleRoomInfo.getPlatform(), simpleRoomInfo.getRoomId(), countDownLatch, roomList);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            roomList.clear();
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
     * 刷新平台分类的缓存
     * @return
     */
    public void refreshArea(){
        platformMap.values().forEach(BasePlatform::refreshArea);
    }

    /**
     * 刷新平台分类的缓存
     * @return 属性数据
     */
    public String refreshUpdate(){
        JSONObject jsonObject = JSONUtil.readJSONObject(FileUtil.file(Global.getUpdateFilePath()), StandardCharsets.UTF_8);;
        Global.updateInfo = jsonObject.toBean(UpdateInfo.class);
        return JSON.toJSONString(jsonObject);
    }

    /**
     * 获取指定平台的分区信息
     * @param platform
     * @return
     */
    public List<List<AreaInfo>> getAreaMap(String platform){
        return Global.platformAreaMap.get(platform);
    }

    /**
     * 获取总的分区列表
     * @return
     */
    public List<List<AreaInfo>> getAllAreaMap(){
        List<List<AreaInfo>> result = new ArrayList<>();
        Map<String, Map<String, Map<String, AreaInfo>>> allMap = Global.AllAreaMap;
        List<String> areaTypeSortList = Global.AreaTypeSortList;
        Map<String, List<String>> areaInfoSortMap = Global.AreaInfoSortMap;

        Iterator<String> it = areaTypeSortList.iterator();
        while(it.hasNext()) {
            String areaType = it.next();
            List<String> areaInfoList = areaInfoSortMap.get(areaType);
            Iterator<String> infoIt = areaInfoList.iterator();
            List<AreaInfo> resultList = new ArrayList<>();
            while(infoIt.hasNext()){
                String areaInfo = infoIt.next();
                Map<String, AreaInfo> areaInfoMap = allMap.get(areaType).get(areaInfo);
                if (areaInfoMap.containsKey("douyu")){
                    resultList.add(areaInfoMap.get("douyu"));
                } else if (areaInfoMap.containsKey("bilibili")){
                    resultList.add(areaInfoMap.get("bilibili"));
                } else if (areaInfoMap.containsKey("huya")){
                    resultList.add(areaInfoMap.get("huya"));
                } else if (areaInfoMap.containsKey("cc")){
                }
            }
            if (resultList.size()<1){
                continue;
            }
            result.add(resultList);
        }
        return  result;
    }

    /**
     * 获取分区中所有平台的推荐列表
     * @param area
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendByAreaAll(String areaType, String area, int page, int size){
        // TODO
        List<LiveRoomInfo> list = new ArrayList<>();
        class MyThread implements Runnable {
            private String platform;
            private List<LiveRoomInfo> list;
            private String area;
            public MyThread(String platform, String area, List<LiveRoomInfo> list){
                this.platform = platform;
                this.list = list;
                this.area = area;
            }
            @Override
            public void run() {
                list.addAll(getRecommendByPlatformArea(platform, area, page, size));
            }
        }
        String areaTypeKey = areaType.substring(0,2);
        Map<String, AreaInfo> map = Global.AllAreaMap.get(areaTypeKey).get(area);
        if (Global.EgameCateMapVer.containsKey(area)){
            Thread t = new Thread(new MyThread("egame", Global.EgameCateMapVer.get(area), list));
            t.start();
            try {
                t.join();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        for (Map.Entry<String, AreaInfo> entry : map.entrySet()) {
            if ("douyu".equals(entry.getKey())){
                Thread t = new Thread(new MyThread("douyu", area, list));
                t.start();
                try {
                    t.join();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if ("bilibili".equals(entry.getKey())){
                Thread t = new Thread(new MyThread("bilibili", area, list));
                t.start();
                try {
                    t.join();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if ("huya".equals(entry.getKey())){
                Thread t = new Thread(new MyThread("huya", area, list));
                t.start();
                try {
                    t.join();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if ("cc".equals(entry.getKey())){
                Thread t = new Thread(new MyThread("cc", area, list));
                t.start();
                try {
                    t.join();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

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
}
