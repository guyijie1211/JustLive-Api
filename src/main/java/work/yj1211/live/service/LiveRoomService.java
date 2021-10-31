package work.yj1211.live.service;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.RoomMapper;
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

@Service
public class LiveRoomService{

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private AsyncService asyncService;

    /**
     * 获取总推荐
     * @param page
     * @param size
     * @return
     */
    public List<LiveRoomInfo> getRecommend(int page, int size){
        List<LiveRoomInfo> list = Collections.synchronizedList(new ArrayList<>());
        class MyThread implements Runnable {
            private String platform;
            private List<LiveRoomInfo> list;
            public MyThread(String platform, List<LiveRoomInfo> list){
                this.platform = platform;
                this.list = list;
            }
            @Override
            public void run() {
                list.addAll(getRecommendByPlatform(platform, page, size));
            }
        }

        Thread t1 = new Thread(new MyThread("bilibili", list));
        Thread t2 = new Thread(new MyThread("douyu", list));
        Thread t3 = new Thread(new MyThread("huya", list));
        Thread t4 = new Thread(new MyThread("cc", list));
        Thread t5 = new Thread(new MyThread("egame", list));
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        try{
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
        }catch (Exception e){
            e.printStackTrace();
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
        List<LiveRoomInfo> list = null;
        if ("bilibili".equals(platform)){
            list = Bilibili.getRecommend(page, size);
        }
        if("douyu".equals(platform)){
            list = Douyu.getRecommend(page, size);
        }
        if("huya".equals(platform)){
            list = Huya.getRecommend(page, size);//传的不是20的话，得改代码
        }
        if("cc".equals(platform)){
            list = CC.getRecommend(page, size);
        }
        if("egame".equals(platform)){
            list = Egame.getRecommend(page, size);
        }
        return list;
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
        List<LiveRoomInfo> list = null;
        if ("bilibili".equals(platform)){
            list = Bilibili.getAreaRoom(area, page, size);
        }
        if("douyu".equals(platform)){
            list = Douyu.getAreaRoom(area, page, size);
        }
        if("huya".equals(platform)){
            list = Huya.getAreaRoom(area, page, size);
        }
        if("cc".equals(platform)){
            list = CC.getAreaRoom(area, page, size);
        }
        if("egame".equals(platform)){
            list = Egame.getAreaRoom(area, page, size);
        }
        return list;
    }

    /**
     * 获取真实直播地址
     * @param platForm
     * @param roomId
     * @return
     */
    public Map<String, String> getRealUrl(String platForm, String roomId){
        Map<String, String> urls = new HashMap<>();
        if ("bilibili".equals(platForm)){
            Bilibili.get_real_url(urls, roomId);
        }
        if ("douyu".equals(platForm)){
            Douyu.get_real_url(urls, roomId);
        }
        if ("huya".equals(platForm)){
            Huya.getRealUrl(urls,roomId);
        }
        if ("cc".equals(platForm)){
            CC.getRealUrl(urls,roomId);
        }
        if ("egame".equals(platForm)){
            Egame.get_real_url(urls,roomId);
        }
        return urls;
    };

    /**
     * 获取用户关注的所有房间信息
     * @param uid     用户uid
     * @return
     */
    public List<LiveRoomInfo> getRoomsByUid(String uid){
        long start = System.currentTimeMillis();
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
        long end = System.currentTimeMillis();
        System.out.println(end-start);
        return roomList;
    }



    /**
     * 获取单个直播间信息
     * @param platForm
     * @param roomId
     * @return
     */
    public LiveRoomInfo getRoomInfo(String uid, String platForm, String roomId){
        LiveRoomInfo roomInfo = null;
        if ("bilibili".equals(platForm)){
            roomInfo = Bilibili.get_single_roomInfo(roomId);
        }
        if ("douyu".equals(platForm)){
            roomInfo = Douyu.getRoomInfo(roomId);
        }
        if ("huya".equals(platForm)){
            roomInfo = Huya.getRoomInfo(roomId);
        }
        if ("cc".equals(platForm)){
            roomInfo = CC.getRoomInfo(roomId);
        }
        if ("egame".equals(platForm)){
            roomInfo = Egame.getRoomInfo(roomId);
        }
        int isFollowed = roomMapper.ifIsFollowed(uid, platForm,roomId);
        roomInfo.setIsFollowed((isFollowed == 0) ? 0 : 1);
        return roomInfo;
    }

    /**
     * 刷新平台分类的缓存
     * @return
     */
    public void refreshArea(){
        long start = System.currentTimeMillis();
        Douyu.refreshArea();
        Bilibili.refreshArea();
        Huya.refreshArea();
        CC.refreshArea();
        Egame.refreshArea();
    }

    /**
     * 刷新平台分类的缓存
     * @return
     */
    public String refreshUpdate(){
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

    private String readTxtFile(String filePath){
        String readResult = "";
        try {
            String encoding="UTF-8";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    readResult = readResult + lineTxt;
                }
                read.close();
            }else{
                readResult = "找不到指定的文件";
            }
        } catch (Exception e) {
            readResult = "读取文件内容出错";
        }

        return readResult;
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
                    continue;
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
     * @param isLive 是否搜索直播中的信息
     * @return
     */
    public List<Owner> search(String platform, String keyWords, String isLive){
        List<Owner> list = new ArrayList<>();
        if ("douyu".equals(platform)){
            List<Owner> douyuList = Douyu.search(keyWords, isLive);
            list.addAll(douyuList);
        }
        if ("bilibili".equals(platform)){
            List<Owner> bilibiliList = Bilibili.search(keyWords, isLive);
            list.addAll(bilibiliList);
        }
        if ("huya".equals(platform)){
            List<Owner> huyaList = Huya.search(keyWords, isLive);
            list.addAll(huyaList);
        }
        if ("cc".equals(platform)){
            List<Owner> ccList = CC.search(keyWords, isLive);
            list.addAll(ccList);
        }
        if ("egame".equals(platform)){
            List<Owner> egameList = Egame.search(keyWords, isLive);
            list.addAll(egameList);
        }
        if ("all".equals(platform)){
            List<Owner> douyuList = Douyu.search(keyWords, isLive);
            List<Owner> bilibiliList = Bilibili.search(keyWords, isLive);
            List<Owner> huyaList = Huya.search(keyWords, isLive);
            List<Owner> ccList = CC.search(keyWords, isLive);
            List<Owner> egameList = Egame.search(keyWords, isLive);
            list.addAll(egameList);
            list.addAll(ccList);
            list.addAll(huyaList);
            list.addAll(bilibiliList);
            list.addAll(douyuList);
        }
        return list;
    }
}
