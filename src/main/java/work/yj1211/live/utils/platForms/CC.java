package work.yj1211.live.utils.platForms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.Owner;
import work.yj1211.live.vo.platformArea.AreaInfo;

import java.util.*;

@Slf4j
public class CC {
    /**
     * 搜索
     * @param keyWords  搜索关键字
     * @param isLive 是否搜索直播中的信息
     * @return
     */
    public static List<Owner> search(String keyWords, String isLive){
        List<Owner> list = new ArrayList<>();
        try {
            String url = "https://cc.163.com/search/anchor/?page=1&size=10&query="+keyWords;
            String result = HttpUtil.doGet(url);
            JSONObject resultJsonObj = JSON.parseObject(result);
            if (result != null) {
                JSONArray ownerList = resultJsonObj.getJSONObject("webcc_anchor").getJSONArray("result");
                Iterator<Object> it = ownerList.iterator();
                while(it.hasNext()){
                    JSONObject responseOwner = (JSONObject) it.next();
                    Owner owner = new Owner();
                    owner.setNickName(responseOwner.getString("nickname"));
                    owner.setCateName(responseOwner.getString("game_name"));
                    owner.setHeadPic(responseOwner.getString("portrait"));
                    owner.setPlatform("cc");
                    owner.setRoomId(responseOwner.getString("cuteid"));
                    owner.setIsLive((responseOwner.getInteger("status") !=null && responseOwner.getInteger("status") == 1) ? "1" : "0");
                    owner.setFollowers(responseOwner.getInteger("follower_num"));
                    if ("1".equals(isLive) && !"1".equals(owner.getIsLive())){
                        continue;
                    }
                    list.add(owner);
                }
            }
            if (list.size()>5){
                return list.subList(0,5);
            }
        } catch (Exception e) {
            log.error("CC---搜索异常---keyword：" + keyWords);
        }

        return list;
    }

    /**
     * 获取真实地址
     * @param urls
     * @param roomId
     */
    public static void getRealUrl(Map<String, String> urls, String roomId) {
        String url = "https://api.cc.163.com/v1/activitylives/anchor/lives?anchor_ccid="+roomId;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if ("OK".equals(resultJsonObj.getString("code"))){
            String channelId = resultJsonObj.getJSONObject("data").getJSONObject(roomId).getInteger("channel_id").toString();
            String urlToGetReal = "https://cc.163.com/live/channel/?channelids="+channelId;
            String resultReal = HttpUtil.doGet(urlToGetReal);
            JSONObject resultRealJsonObj = JSON.parseObject(resultReal);
            if (null != resultRealJsonObj){
                String real_url = resultRealJsonObj.getJSONArray("data").getJSONObject(0).getString("sharefile");
                urls.put("OD", real_url);
            }
        }
    }

    /**
     * 获取CC房间信息
     * @param roomId
     * @return
     */
    public static LiveRoomInfo getRoomInfo(String roomId) {
        String url = "https://api.cc.163.com/v1/activitylives/anchor/lives?anchor_ccid="+roomId;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
        if ("OK".equals(resultJsonObj.getString("code"))){
            String channelId = resultJsonObj.getJSONObject("data").getJSONObject(roomId).getInteger("channel_id").toString();
            String urlToGetReal = "https://cc.163.com/live/channel/?channelids="+channelId;
            String resultReal = HttpUtil.doGet(urlToGetReal);
            JSONObject resultRealJsonObj = JSON.parseObject(resultReal);
            if (null != resultRealJsonObj){
                JSONObject roomInfo = resultRealJsonObj.getJSONArray("data").getJSONObject(0);
                liveRoomInfo.setPlatForm("cc");
                liveRoomInfo.setRoomId(roomInfo.getString("cuteid"));
                liveRoomInfo.setCategoryId(roomInfo.getString("cate_id"));//分类id不对
                liveRoomInfo.setCategoryName(roomInfo.getString("gamename"));
                liveRoomInfo.setRoomName(roomInfo.getString("title"));
                liveRoomInfo.setOwnerName(roomInfo.getString("nickname"));
                liveRoomInfo.setRoomPic(roomInfo.getString("poster"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getString("purl"));
                liveRoomInfo.setOnline(roomInfo.getInteger("visitor"));
                liveRoomInfo.setIsLive((roomInfo.getInteger("status") !=null && roomInfo.getInteger("status") == 1) ? 1 : 0);
            }
        }
        return liveRoomInfo;
    }

    /**
     * 刷新分类缓存
     * @return
     */
    public static void refreshArea(){
        List<List<AreaInfo>> areaMapTemp = new ArrayList<>();
        areaMapTemp.add(refreshSingleArea("1", "网游"));
        areaMapTemp.add(refreshSingleArea("2", "手游"));
        areaMapTemp.add(refreshSingleArea("4", "网游竞技"));
        areaMapTemp.add(refreshSingleArea("5", "娱乐"));
        Global.platformAreaMap.put("cc",areaMapTemp);
    }

    /**
     * 获取cc单个类型下的所有分区
     * @param areaCode
     * @return
     */
    private static List<AreaInfo> refreshSingleArea(String areaCode, String typeName){
        String url = "https://api.cc.163.com/v1/wapcc/gamecategory?catetype=" + areaCode;
        List<AreaInfo> areaListTemp = new ArrayList<>();
        String result = HttpRequest.create(url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (resultJsonObj.getInteger("code") == 0) {
            JSONArray data = resultJsonObj.getJSONObject("data").getJSONObject("category_info").getJSONArray("game_list");
            Iterator<Object> it = data.iterator();
            while (it.hasNext()) {
                JSONObject areaInfo = (JSONObject) it.next();
                AreaInfo ccArea = new AreaInfo();
                ccArea.setAreaType(areaCode);
                ccArea.setTypeName(typeName);
                ccArea.setAreaId(areaInfo.getString("gametype"));
                ccArea.setAreaName(areaInfo.getString("name"));
                ccArea.setAreaPic(areaInfo.getString("cover"));
                ccArea.setPlatform("cc");
                areaListTemp.add(ccArea);
                Global.CCCateMap.put(ccArea.getAreaId(), ccArea.getAreaName());
                if (!Global.AllAreaMap.containsKey(typeName)){
                    List<String> list = new ArrayList<>();
                    list.add(ccArea.getAreaName());
                    Global.AreaTypeSortList.add(typeName);
                    Global.AreaInfoSortMap.put(typeName, list);
                }else {
                    if(!Global.AllAreaMap.get(typeName).containsKey(ccArea.getAreaName())){
                        Global.AreaInfoSortMap.get(typeName).add(ccArea.getAreaName());
                    }
                }
                Global.AllAreaMap.computeIfAbsent(typeName, k -> new HashMap<>())
                        .computeIfAbsent(ccArea.getAreaName(), k -> new HashMap<>()).put("cc", ccArea);
            }
        }
        return areaListTemp;
    }

    /**
     * 根据分页获取推荐直播间
     * @param page 页数
     * @param size 每页大小
     * @return
     */
    public static List<LiveRoomInfo> getRecommend(int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        int start = (page-1)*size;
        String url = "https://cc.163.com/api/category/live/?format=json&start=" + start + "&size=" + size;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (null != resultJsonObj) {
            JSONArray data = resultJsonObj.getJSONArray("lives");
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("cc");
                liveRoomInfo.setRoomId(roomInfo.getString("cuteid"));
                liveRoomInfo.setCategoryId(roomInfo.getString("gametype"));
                liveRoomInfo.setCategoryName(roomInfo.getString("gamename"));
                liveRoomInfo.setRoomName(roomInfo.getString("title"));
                liveRoomInfo.setOwnerName(roomInfo.getString("nickname"));
                liveRoomInfo.setRoomPic(roomInfo.getString("poster"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getString("purl"));
                liveRoomInfo.setOnline(roomInfo.getInteger("total_visitor"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }

    /**
     * 获取CC分区房间
     * @param area
     * @param page
     * @param size
     * @return
     */
    public static List<LiveRoomInfo> getAreaRoom(String area, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        int start = (page-1)*size;
        AreaInfo areaInfo = Global.getAreaInfo("cc", area);
        String url = "https://cc.163.com/api/category/" + areaInfo.getAreaId() + "/?format=json&tag_id=0&start=" + start + "&size=" +size;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSON.parseObject(result);
        if (null != resultJsonObj) {
            JSONArray data = resultJsonObj.getJSONArray("lives");
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm("cc");
                liveRoomInfo.setRoomId(roomInfo.getString("cuteid"));
                liveRoomInfo.setCategoryId(roomInfo.getString("gametype"));
                liveRoomInfo.setCategoryName(roomInfo.getString("gamename"));
                liveRoomInfo.setRoomName(roomInfo.getString("title"));
                liveRoomInfo.setOwnerName(roomInfo.getString("nickname"));
                liveRoomInfo.setRoomPic(roomInfo.getString("poster"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getString("purl"));
                liveRoomInfo.setOnline(roomInfo.getInteger("total_visitor"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }
}
