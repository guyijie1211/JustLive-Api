package work.yj1211.live.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import work.yj1211.live.factory.ResultFactory;
import work.yj1211.live.service.LiveRoomService;
import work.yj1211.live.utils.annotation.AccessLimit;
import work.yj1211.live.vo.LiveRoomInfo;
import work.yj1211.live.vo.Owner;
import work.yj1211.live.vo.Result;
import work.yj1211.live.vo.platformArea.AreaInfo;

import javax.websocket.server.PathParam;
import java.util.*;

@Slf4j
@RestController
public class LiveRoomCrl {

    @Autowired
    private LiveRoomService liveRoomService;

    @CrossOrigin
    @RequestMapping(value = "/api/live/getRecommend", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getRecommend(@PathParam("page")int page, @PathParam("size")int size){
        List<LiveRoomInfo> list = liveRoomService.getRecommend(page, 10);
        Collections.sort(list);
        return ResultFactory.buildSuccessResult(list);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/getRecommendByPlatform", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getRecommendByPlatform(@PathParam("platform")String platform, @PathParam("page")int page, @PathParam("size")int size){
        List<LiveRoomInfo> list = liveRoomService.getRecommendByPlatform(platform, page, size);
        Collections.sort(list);
        return ResultFactory.buildSuccessResult(list);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/getRecommendByPlatformArea", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getRecommendByPlatformArea(@PathParam("platform")String platform, @PathParam("area")String area, @PathParam("page")int page, @PathParam("size")int size){
        List<LiveRoomInfo> list = liveRoomService.getRecommendByPlatformArea(platform, area, page, size);
        Collections.sort(list);
        return ResultFactory.buildSuccessResult(list);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/getRecommendByAreaAll", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getRecommendByAreaAll(@PathParam("areaType")String areaType, @PathParam("area")String area, @PathParam("page")int page){
        List<LiveRoomInfo> list = liveRoomService.getRecommendByAreaAll(areaType, area, page, 10);
        Collections.sort(list);
        return ResultFactory.buildSuccessResult(list);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/getRealUrl", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getRealUrl(@PathParam("platform")String platform, @PathParam("roomId")String roomId){
        Map<String, String> urls;
        urls = liveRoomService.getRealUrl(platform, roomId);
        return ResultFactory.buildSuccessResult(urls);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/getRoomInfo", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getRoomInfo(@PathParam("uid")String uid, @PathParam("platform")String platform, @PathParam("roomId")String roomId){
        LiveRoomInfo roomInfo = liveRoomService.getRoomInfo(uid, platform, roomId);
        if (null == roomInfo){
            return ResultFactory.buildFailResult("获取房间信息失败");
        }
        return ResultFactory.buildSuccessResult(roomInfo);
    }

    /**
     * 获取用户关注的所有直播间信息
     * @param uid 用户账户id
     * @return 所有关注的直播间的List
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/getRoomsOn", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getRoomsOn(@PathParam("uid")String uid){
        List<LiveRoomInfo> roomInfoList = liveRoomService.getRoomsByUid(uid);
        if (null == roomInfoList){
            return ResultFactory.buildFailResult("获取房间信息失败");
        }
        Collections.sort(roomInfoList);
        return ResultFactory.buildSuccessResult(roomInfoList);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/refreshArea", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result refreshArea(){
        try{
            liveRoomService.refreshArea();
        }catch (Exception e){
            return ResultFactory.buildFailResult(e.getMessage());
        }
        return ResultFactory.buildSuccessResult("刷新成功");
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/getAreas", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getAreas(@PathParam("platform")String platform){
        List<List<AreaInfo>> areaMap = liveRoomService.getAreaMap(platform);
        return ResultFactory.buildSuccessResult(areaMap);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/getAllAreas", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getAllAreas(){
        List<List<AreaInfo>> allAreaMap = liveRoomService.getAllAreaMap();
        return ResultFactory.buildSuccessResult(allAreaMap);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/search", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    @AccessLimit
    public Result search(@PathParam("platform")String platform, @PathParam("keyWords")String keyWords, @PathParam("uid")String uid){
//        List<Owner> roomInfoList = liveRoomService.search(platform, keyWords, uid);
//        if (null == roomInfoList){
//            return ResultFactory.buildFailResult("获取房间信息失败");
//        }
//        Collections.sort(roomInfoList);
//        return ResultFactory.buildSuccessResult(roomInfoList);
        return ResultFactory.buildSuccessResult(null);
    }

    /**
     * 刷新版本信息
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/versionRefresh", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result versionRefresh(){
        String result = liveRoomService.refreshUpdate();
        return ResultFactory.buildSuccessResult(result);
    }
}
