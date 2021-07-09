package work.yj1211.live.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import work.yj1211.live.factory.ResultFactory;
import work.yj1211.live.service.TvLiveService;
import work.yj1211.live.utils.Global;
import work.yj1211.live.vo.Result;

@RestController
public class TvLiveCrl {

    @Autowired
    private TvLiveService tvLiveService;

    @CrossOrigin
    @RequestMapping(value = "/api/live/getTv", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getTv(){
        return ResultFactory.buildSuccessResult(Global.m3uResult);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/live/refreshM3U", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result refreshM3U(){
        tvLiveService.refreshM3U();
        return ResultFactory.buildSuccessResult("刷新成功");
    }
}
