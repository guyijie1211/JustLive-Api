package work.yj1211.live.service.platforms.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import work.yj1211.live.enums.Platform;
import work.yj1211.live.service.platforms.BasePlatform;
import work.yj1211.live.utils.HttpUtil;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.model.LiveRoomInfo;
import work.yj1211.live.model.Owner;
import work.yj1211.live.model.platformArea.AreaInfo;

import java.util.*;

@Slf4j
@Component
public class CC implements BasePlatform {

    /**
     * 搜索
     *
     * @param keyWords 搜索关键字
     * @return
     */
    @Override
    public List<Owner> search(String keyWords){
        List<Owner> list = new ArrayList<>();
        String url = "https://cc.163.com/search/anchor/?page=1&size=10&query="+keyWords;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj != null) {
            JSONArray ownerList = resultJsonObj.getJSONObject("webcc_anchor").getJSONArray("result");
            ownerList.forEach(item ->{
                JSONObject responseOwner = (JSONObject) item;
                Owner owner = new Owner();
                owner.setNickName(responseOwner.getStr("nickname"));
                owner.setCateName(responseOwner.getStr("game_name"));
                owner.setHeadPic(responseOwner.getStr("portrait"));
                owner.setPlatform(getPlatformName());
                owner.setRoomId(responseOwner.getStr("cuteid"));
                owner.setIsLive((responseOwner.getStr("status") !=null && responseOwner.getInt("status") == 1) ? "1" : "0");
                owner.setFollowers(responseOwner.getInt("follower_num"));
                list.add(owner);
            });
        }
        if (list.size()>5){
            return list.subList(0,5);
        }
        return list;
    }

    @Override
    public String getPlatformName() {
        return Platform.CC.getName();
    }

    /**
     * 获取真实地址
     * @param urls
     * @param roomId
     */
    @Override
    public void getRealUrl(Map<String, String> urls, String roomId) {
        String url = "https://api.cc.163.com/v1/activitylives/anchor/lives?anchor_ccid="+roomId;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if ("OK".equals(resultJsonObj.getStr("code"))){
            String channelId = resultJsonObj.getJSONObject("data").getJSONObject(roomId).getInt("channel_id").toString();
            String urlToGetReal = "https://cc.163.com/live/channel/?channelids="+channelId;
            String resultReal = HttpUtil.doGet(urlToGetReal);
            JSONObject resultRealJsonObj = JSONUtil.parseObj(resultReal);
            if (null != resultRealJsonObj){
                String real_url = resultRealJsonObj.getJSONArray("data").getJSONObject(0).getStr("sharefile");
                urls.put("OD", real_url);
            }
        }
    }

    /**
     * 获取CC房间信息
     * @param roomId
     * @return
     */
    @Override
    public LiveRoomInfo getRoomInfo(String roomId) {
        LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
        try {
            String url = "https://api.cc.163.com/v1/activitylives/anchor/lives?anchor_ccid="+roomId;
            String result = HttpUtil.doGet(url);
            JSONObject resultJsonObj = JSONUtil.parseObj(result);

            if ("OK".equals(resultJsonObj.getStr("code"))){
                String channelId = resultJsonObj.getJSONObject("data").getJSONObject(roomId).getInt("channel_id").toString();
                String urlToGetReal = "https://cc.163.com/live/channel/?channelids="+channelId;
                String resultReal = HttpUtil.doGet(urlToGetReal);
                JSONObject resultRealJsonObj = JSONUtil.parseObj(resultReal);
                if (null != resultRealJsonObj){
                    JSONObject roomInfo = resultRealJsonObj.getJSONArray("data").getJSONObject(0);
                    liveRoomInfo.setPlatForm(getPlatformName());
                    liveRoomInfo.setRoomId(roomInfo.getStr("cuteid"));
                    liveRoomInfo.setCategoryId(roomInfo.getStr("cate_id"));//分类id不对
                    liveRoomInfo.setCategoryName(roomInfo.getStr("gamename"));
                    liveRoomInfo.setRoomName(roomInfo.getStr("title"));
                    liveRoomInfo.setOwnerName(roomInfo.getStr("nickname"));
                    liveRoomInfo.setRoomPic(roomInfo.getStr("poster"));
                    liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("purl"));
                    liveRoomInfo.setOnline(roomInfo.getInt("visitor"));
                    liveRoomInfo.setIsLive((roomInfo.getInt("status") !=null && roomInfo.getInt("status") == 1) ? 1 : 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return liveRoomInfo;
    }

    /**
     * 获取cc单个类型下的所有分区
     * @param areaCode
     * @return
     */
    private List<AreaInfo> refreshSingleArea(String areaCode, String typeName){
        List<AreaInfo> areaInfoList = new ArrayList<>();
        String url = "https://api.cc.163.com/v1/wapcc/gamecategory?catetype=" + areaCode;
        String result = HttpRequest.create(url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (resultJsonObj.getInt("code") == 0) {
            JSONArray gameList = resultJsonObj.getJSONObject("data").getJSONObject("category_info").getJSONArray("game_list");
            gameList.forEach(item->{
                JSONObject areaInfo = (JSONObject) item;
                AreaInfo ccArea = new AreaInfo();
                ccArea.setAreaType(areaCode);
                ccArea.setTypeName(typeName);
                ccArea.setAreaId(areaInfo.getStr("gametype"));
                ccArea.setAreaName(areaInfo.getStr("name"));
                ccArea.setAreaPic(areaInfo.getStr("cover"));
                ccArea.setPlatform(getPlatformName());
                areaInfoList.add(ccArea);
            });
        }
        return areaInfoList;
    }

    /**
     * 根据分页获取推荐直播间
     * @param page 页数
     * @param size 每页大小
     * @return
     */
    @Override
    public List<LiveRoomInfo> getRecommend(int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        int start = (page-1)*size;
        String url = "https://cc.163.com/api/category/live/?format=json&start=" + start + "&size=" + size;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (null != resultJsonObj) {
            JSONArray data = resultJsonObj.getJSONArray("lives");
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm(getPlatformName());
                liveRoomInfo.setRoomId(roomInfo.getStr("cuteid"));
                liveRoomInfo.setCategoryId(roomInfo.getStr("gametype"));
                liveRoomInfo.setCategoryName(roomInfo.getStr("gamename"));
                liveRoomInfo.setRoomName(roomInfo.getStr("title"));
                liveRoomInfo.setOwnerName(roomInfo.getStr("nickname"));
                liveRoomInfo.setRoomPic(roomInfo.getStr("poster"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("purl"));
                liveRoomInfo.setOnline(roomInfo.getInt("total_visitor"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }

    @Override
    public List<AreaInfo> getAreaList() {
        List<AreaInfo> areaInfoList = new ArrayList<>();
        areaInfoList.addAll(refreshSingleArea("1", "网游"));
        areaInfoList.addAll(refreshSingleArea("2", "手游"));
        areaInfoList.addAll(refreshSingleArea("4", "网游竞技"));
        areaInfoList.addAll(refreshSingleArea("5", "娱乐"));
        return areaInfoList;
    }

    /**
     * 获取CC分区房间
     *
     * @param areaInfo
     * @param page
     * @param size
     * @return
     */
    @Override
    public List<LiveRoomInfo> getAreaRoom(AreaInfo areaInfo, int page, int size){
        List<LiveRoomInfo> list = new ArrayList<>();
        int start = (page-1)*size;
        String url = "https://cc.163.com/api/category/" + areaInfo.getAreaId() + "/?format=json&tag_id=0&start=" + start + "&size=" +size;
        String result = HttpUtil.doGet(url);
        JSONObject resultJsonObj = JSONUtil.parseObj(result);
        if (null != resultJsonObj) {
            JSONArray data = resultJsonObj.getJSONArray("lives");
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject roomInfo = (JSONObject) it.next();
                LiveRoomInfo liveRoomInfo = new LiveRoomInfo();
                liveRoomInfo.setPlatForm(getPlatformName());
                liveRoomInfo.setRoomId(roomInfo.getStr("cuteid"));
                liveRoomInfo.setCategoryId(roomInfo.getStr("gametype"));
                liveRoomInfo.setCategoryName(roomInfo.getStr("gamename"));
                liveRoomInfo.setRoomName(roomInfo.getStr("title"));
                liveRoomInfo.setOwnerName(roomInfo.getStr("nickname"));
                liveRoomInfo.setRoomPic(roomInfo.getStr("poster"));
                liveRoomInfo.setOwnerHeadPic(roomInfo.getStr("purl"));
                liveRoomInfo.setOnline(roomInfo.getInt("total_visitor"));
                liveRoomInfo.setIsLive(1);
                list.add(liveRoomInfo);
            }
        }
        return list;
    }
}
