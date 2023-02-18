package work.yj1211.live.service;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.AllRoomsMapper;
import work.yj1211.live.mapper.RoomMapper;
import work.yj1211.live.utils.Constant;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.platForms.*;
import work.yj1211.live.utils.thread.AsyncService;
import work.yj1211.live.vo.*;
import work.yj1211.live.vo.platformArea.AreaInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static work.yj1211.live.utils.Constant.*;

@Service
public class LiveRoomService {

    private static final Logger logger = LoggerFactory.getLogger(LiveRoomService.class);

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private AllRoomsMapper allRoomsMapper;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private Bilibili bilibili;

    /**
     * 获取总推荐
     *
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommend(int page, int size) {
        List<LiveRoomInfo> list = Collections.synchronizedList(new ArrayList<>());
        class MyThread implements Runnable {
            private String platform;
            private List<LiveRoomInfo> list;

            public MyThread(String platform, List<LiveRoomInfo> list) {
                this.platform = platform;
                this.list = list;
            }

            @Override
            public void run() {
                list.addAll(getRecommendByPlatform(platform, page, size));
            }
        }

        Thread t1 = new Thread(new MyThread(BILIBILI, list));
        Thread t2 = new Thread(new MyThread(DOU_YU, list));
        Thread t3 = new Thread(new MyThread(HU_YA, list));
        Thread t4 = new Thread(new MyThread(WANGYI_CC, list));
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取总推荐(数据库获取)
     *
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendFromLocal(int page, int size) {
        return allRoomsMapper.getRecommendRooms(page, size);
    }

    /**
     * 根据平台获取总推荐房间列表
     *
     * @param platform
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendByPlatform(String platform, int page, int size) {
        List<LiveRoomInfo> list = null;
        if (BILIBILI.equals(platform)) {
            list = bilibili.getRecommend(page, size);
        }
        if (DOU_YU.equals(platform)) {
            list = Douyu.getRecommend(page, size);
        }
        if (HU_YA.equals(platform)) {
            //传的不是20的话，得改代码
            list = Huya.getRecommend(page, size);
        }
        if (WANGYI_CC.equals(platform)) {
            list = CC.getRecommend(page, size);
        }
        return list;
    }

    /**
     * 根据平台和分区获取推荐房间列表
     *
     * @param platform
     * @param area
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendByPlatformArea(String platform, String area, int page, int size) {
        List<LiveRoomInfo> list = null;
        if (BILIBILI.equals(platform)) {
            list = bilibili.getAreaRoom(area, page, size);
        }
        if (DOU_YU.equals(platform)) {
            list = Douyu.getAreaRoom(area, page, size);
        }
        if (HU_YA.equals(platform)) {
            list = Huya.getAreaRoom(area, page, size);
        }
        if (WANGYI_CC.equals(platform)) {
            list = CC.getAreaRoom(area, page, size);
        }
        return list;
    }

    /**
     * 获取真实直播地址
     *
     * @param platForm
     * @param roomId
     * @return
     */
    public Map<String, String> getRealUrl(String platForm, String roomId) {
        Map<String, String> urls = new HashMap<>();
        if (BILIBILI.equals(platForm)) {
            bilibili.getRealUrl(urls, roomId);
        }
        if (DOU_YU.equals(platForm)) {
            Douyu.get_real_url(urls, roomId);
        }
        if (HU_YA.equals(platForm) || "huyaTest".equals(platForm)) {
            FixHuya.getRealUrl(urls, roomId);
        }
        if (WANGYI_CC.equals(platForm)) {
            CC.getRealUrl(urls, roomId);
        }
        return urls;
    }

    /**
     * 获取用户关注的所有房间信息
     *
     * @param uid 用户uid
     * @return
     */
    public List<LiveRoomInfo> getRoomsByUid(String uid) {
        List<LiveRoomInfo> roomList = Collections.synchronizedList(new ArrayList<>());
        List<SimpleRoomInfo> simpleRoomInfoList = roomMapper.getRoomsByUid(uid);
        CountDownLatch countDownLatch = new CountDownLatch(simpleRoomInfoList.size());
        for (SimpleRoomInfo simpleRoomInfo : simpleRoomInfoList) {
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
     *
     * @param platForm
     * @param roomId
     * @return
     */
    public LiveRoomInfo getRoomInfo(String uid, String platForm, String roomId) {
        LiveRoomInfo roomInfo = null;
        if (BILIBILI.equals(platForm)) {
            roomInfo = bilibili.getSingleRoomInfo(roomId);
        }
        if (DOU_YU.equals(platForm)) {
            roomInfo = Douyu.getRoomInfo(roomId);
        }
        if (HU_YA.equals(platForm)) {
            roomInfo = Huya.getRoomInfo(roomId);
        }
        if (WANGYI_CC.equals(platForm)) {
            roomInfo = CC.getRoomInfo(roomId);
        }
        int isFollowed = roomMapper.ifIsFollowed(uid, platForm, roomId);
        roomInfo.setIsFollowed((isFollowed == 0) ? 0 : 1);
        return roomInfo;
    }

    /**
     * 刷新平台分类的缓存
     *
     * @return
     */
    public void refreshArea() {
        Douyu.refreshArea();
        bilibili.refreshArea();
        Huya.refreshArea();
        CC.refreshArea();
    }

    /**
     * 刷新平台分类的缓存
     *
     * @return
     */
    public String refreshUpdate() {
        String readResult = readTxtFile(Global.getUpdateFilePath());
        UpdateInfo updateInfo;
        try {
            updateInfo = JSON.parseObject(readResult, UpdateInfo.class);
        } catch (Exception e) {
            return readResult;
        }
        Global.updateInfo = updateInfo;
        return JSON.toJSONString(updateInfo);
    }

    private String readTxtFile(String filePath) {
        StringBuilder readResult = new StringBuilder();
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            // 判断文件是否存在
            if (file.isFile() && file.exists()) {
                BufferedReader bufferedReader = null;
                try (
                        InputStreamReader read = new InputStreamReader(
                                new FileInputStream(file), encoding)) {
                    bufferedReader = new BufferedReader(read);
                }//考虑到编码格式

                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    readResult.append(lineTxt);
                }
            } else {
                readResult.append("找不到指定的文件");
            }
        } catch (Exception e) {
            readResult.append("读取文件内容出错");
        }

        return readResult.toString();
    }

    /**
     * 获取指定平台的分区信息
     *
     * @param platform
     * @return
     */
    public List<List<AreaInfo>> getAreaMap(String platform) {
        return Global.platformAreaMap.get(platform);
    }

    /**
     * 获取总的分区列表
     *
     * @return
     */
    public List<List<AreaInfo>> getAllAreaMap() {
        List<List<AreaInfo>> result = new ArrayList<>();
        Map<String, Map<String, Map<String, AreaInfo>>> allMap = Global.AllAreaMap;
        List<String> areaTypeSortList = Global.AreaTypeSortList;
        Map<String, List<String>> areaInfoSortMap = Global.AreaInfoSortMap;

        Iterator<String> it = areaTypeSortList.iterator();
        while (it.hasNext()) {
            String areaType = it.next();
            List<String> areaInfoList = areaInfoSortMap.get(areaType);
            Iterator<String> infoIt = areaInfoList.iterator();
            List<AreaInfo> resultList = new ArrayList<>();
            while (infoIt.hasNext()) {
                String areaInfo = infoIt.next();
                Map<String, AreaInfo> areaInfoMap = allMap.get(areaType).get(areaInfo);
                if (areaInfoMap.containsKey(DOU_YU)) {
                    resultList.add(areaInfoMap.get(DOU_YU));
                } else if (areaInfoMap.containsKey(BILIBILI)) {
                    resultList.add(areaInfoMap.get(BILIBILI));
                } else if (areaInfoMap.containsKey(HU_YA)) {
                    resultList.add(areaInfoMap.get(HU_YA));
                } else if (areaInfoMap.containsKey(WANGYI_CC)) {
                }
            }
            if (resultList.isEmpty()) {
                continue;
            }
            result.add(resultList);
        }
        return result;
    }

    /**
     * 获取分区中所有平台的推荐列表
     *
     * @param area
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommendByAreaAll(String areaType, String area, int page, int size) {
        List<LiveRoomInfo> list = new ArrayList<>();
        class MyThread implements Runnable {
            private String platform;
            private List<LiveRoomInfo> list;
            private String area;

            public MyThread(String platform, String area, List<LiveRoomInfo> list) {
                this.platform = platform;
                this.list = list;
                this.area = area;
            }

            @Override
            public void run() {
                list.addAll(getRecommendByPlatformArea(platform, area, page, size));
            }
        }
        String areaTypeKey = areaType.substring(0, 2);
        Map<String, AreaInfo> map = Global.AllAreaMap.get(areaTypeKey).get(area);
        for (Map.Entry<String, AreaInfo> entry : map.entrySet()) {
            if (DOU_YU.equals(entry.getKey())) {
                Thread t = new Thread(new MyThread(DOU_YU, area, list));
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (BILIBILI.equals(entry.getKey())) {
                Thread t = new Thread(new MyThread(BILIBILI, area, list));
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (HU_YA.equals(entry.getKey())) {
                Thread t = new Thread(new MyThread(HU_YA, area, list));
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (WANGYI_CC.equals(entry.getKey())) {
                Thread t = new Thread(new MyThread(WANGYI_CC, area, list));
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return list;
    }

    /**
     * 搜索
     *
     * @param platform 搜索的目标平台，"all"为搜索所有平台
     * @param keyWords 搜索关键字
     * @param isLive   是否搜索直播中的信息
     * @return
     */
    public List<Owner> search(String platform, String keyWords, String isLive) {
        List<Owner> list = new ArrayList<>();
        if (DOU_YU.equals(platform)) {
            List<Owner> douyuList = Douyu.search(keyWords, isLive);
            list.addAll(douyuList);
        }
        if (BILIBILI.equals(platform)) {
            List<Owner> bilibiliList = bilibili.search(keyWords, isLive);
            list.addAll(bilibiliList);
        }
        if (HU_YA.equals(platform)) {
            List<Owner> huyaList = Huya.search(keyWords, isLive);
            list.addAll(huyaList);
        }
        if (WANGYI_CC.equals(platform)) {
            List<Owner> ccList = CC.search(keyWords, isLive);
            list.addAll(ccList);
        }
        if ("all".equals(platform)) {
            List<Owner> douyuList = Douyu.search(keyWords, isLive);
            List<Owner> bilibiliList = bilibili.search(keyWords, isLive);
            List<Owner> huyaList = Huya.search(keyWords, isLive);
            List<Owner> ccList = CC.search(keyWords, isLive);
            list.addAll(ccList);
            list.addAll(huyaList);
            list.addAll(bilibiliList);
            list.addAll(douyuList);
        }
        return list;
    }
}
