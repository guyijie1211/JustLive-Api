package work.yj1211.live.controller;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import work.yj1211.live.factory.ResultFactory;
import work.yj1211.live.mapper.UserMailMapper;
import work.yj1211.live.service.UserService;
import work.yj1211.live.model.*;
import work.yj1211.live.model.platformArea.AreaSimple;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class UserInfoCrl {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMailMapper mailMapper;

    /**
     * 用户登录接口
     * @param username 用户名
     * @param password 加密后的用户密码
     * @return 登录结果
     */
    @CrossOrigin
    @RequestMapping(value = "/api/login", method = RequestMethod.POST, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result login(@PathParam("username")String username, @PathParam("password") String password){
        UserInfo user = userService.login(username, password);
        if (null == user){
            log.info(username+"---账户密码错误");
            return ResultFactory.buildFailResult("账户密码错误");
        }
        userService.updateLastLogin(user.getUid());
        return ResultFactory.buildSuccessResult(user);
    }

    /**
     * 用户注册接口
     * @param username 新用户名
     * @param nickname 新昵称
     * @param password 新密码
     * @return 用户名已存在或注册成功
     */
    @CrossOrigin
    @RequestMapping(value = "/api/register", method = RequestMethod.POST, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result register(@PathParam("username")String username, @PathParam("nickname")String nickname, @PathParam("password")String password, @PathParam("mail")String mail){
        if(null != userService.findUserByName(username)){
            log.info(username+"---注册失败---用户名已存在");
            return ResultFactory.buildFailResult("用户名已存在");
        }

        UserInfo user = new UserInfo();
        String uuid = UUID.randomUUID().toString().replace("-","");
        user.setUid(uuid);
        user.setUserName(username);
        user.setNickName(nickname);
        user.setPassword(password);
        userService.register(user);
        if (StrUtil.isNotEmpty(mail)) {
            try {
                userService.bindMail(uuid, mail);
            } catch (Exception e) {
                return ResultFactory.buildFailResult("邮箱已被绑定");
            }
        }
        return ResultFactory.buildSuccessResult(user);
    }

    /**
     * 绑定邮箱
     * @param userName
     * @param mail
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/bindMail", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result bindMail(@PathParam("userName")String userName, @PathParam("mail")String mail){
        try {
            userService.bindMail(userName, mail);
        } catch (Exception e) {
            return ResultFactory.buildFailResult("邮箱已被绑定");
        }
        return ResultFactory.buildSuccessResult("成功");
    }

    /**
     * 关注分区
     * @param uid
     * @param areaType
     * @param area
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/followArea", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result followArea(@PathParam("uid")String uid, @PathParam("areaType")String areaType, @PathParam("area")String area){
        userService.followArea(areaType, area, uid);
        return ResultFactory.buildSuccessResult("关注成功");
    }

    /**
     * 取消关注分区
     * @param uid
     * @param areaType
     * @param area
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/unFollowArea", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result unFollowArea(@PathParam("uid")String uid, @PathParam("areaType")String areaType, @PathParam("area")String area){
        userService.unFollowArea(areaType, area, uid);
        return ResultFactory.buildSuccessResult("已经取消关注");
    }

    /**
     * 获取用户关注的所有分区
     * @param uid 用户账户id
     * @return 所有关注的直播间的List
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/getFollowedAreas", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getFollowedAreas(@PathParam("uid")String uid){
        List<AreaSimple> areaList = userService.getAreasByUid(uid);
        return ResultFactory.buildSuccessResult(areaList);
    }

    /**
     * 关注直播间
     * @param platform 直播平台
     * @param roomId 直播房间号
     * @param uid 用户账号id
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/follow", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result follow(@PathParam("platform")String platform, @PathParam("roomId")String roomId, @PathParam("uid")String uid){
        userService.followRoom(platform, roomId, uid);
        log.info(uid+"---关注成功---直播间信息：" + platform + "-" + roomId);
        return ResultFactory.buildSuccessResult("关注成功");
    }

    /**
     * 取消关注
     * @param platform
     * @param roomId
     * @param uid
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/unFollow", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result unFollow(@PathParam("platform")String platform, @PathParam("roomId")String roomId, @PathParam("uid")String uid){
        userService.unFollowRoom(platform, roomId, uid);
        log.info(uid+"---取消关注成功---直播间信息：" + platform + "-" + roomId);
        return ResultFactory.buildSuccessResult("已经取消关注");
    }

    /**
     * 信息修改接口
     * @param userInfo 新的用户信息
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/changeUserInfo", method = RequestMethod.POST, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result changeUserInfo(@RequestBody UserInfo userInfo){
        userService.changeUserInfo(userInfo);
        userService.changeUserBan(userInfo);
        UserInfo userInfo1 = userService.findUserByName(userInfo.getUserName());
        return ResultFactory.buildSuccessResult(userInfo1);
    }

    /**
     * 用户密码修改接口
     * @param userName 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/changePassword", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result changePassword(@PathParam("userName")String userName, @PathParam("oldPassword")String oldPassword, @PathParam("newPassword")String newPassword){
        UserInfo user = userService.login(userName, oldPassword);
        if (null == user){
            log.info(userName + "---修改密码失败---旧密码错误");
            return ResultFactory.buildFailResult("旧密码错误");
        }else{
            userService.changePassword(userName, newPassword);
            log.info(userName + "---修改密码成功");
            return ResultFactory.buildSuccessResult("密码修改成功");
        }
    }

    /**
     * 找回密码
     * @param mail
     * @param newPassword
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/changePasswordByMail", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result changePassword(@PathParam("mail")String mail, @PathParam("newPassword")String newPassword){
        String changeResult = userService.changePassByMail(mail, newPassword);
        if ("此邮箱未绑定账号".equalsIgnoreCase(changeResult)) {
            return ResultFactory.buildFailResult(changeResult);
        } else {
            return ResultFactory.buildSuccessResult(changeResult);
        }
    }

    /**
     * 发送邮件
     * @param mail 邮件地址
     * @param code 验证码
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/sendMail", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result sendMail(@PathParam("mail")String mail, @PathParam("code")String code, @PathParam("action")String action) {
        String sendResult = userService.sendMail(mail, code, action);
        if ("邮件已发送".equalsIgnoreCase(sendResult)) {
            return ResultFactory.buildSuccessResult(sendResult);
        } else {
            return ResultFactory.buildFailResult(sendResult);
        }
    }

    /**
     * 获取绑定邮件
     * @param uid uid
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/getBindMail", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result getBindMail(@PathParam("uid")String uid) {
        List<UserMail> mails = mailMapper.selectAllByUid(uid);
        if (CollectionUtils.isEmpty(mails)) {
            return ResultFactory.buildSuccessResult("");
        } else {
            return ResultFactory.buildSuccessResult(mails.get(0).getMail());
        }
    }

    /**
     * AndroidApp版本更新接口
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/api/live/versionUpdate", method = RequestMethod.GET, produces = "application/json; charset = UTF-8")
    @ResponseBody
    public Result versionUpdate(){
        UpdateInfo updateInfo = userService.checkUpdate();
        return ResultFactory.buildSuccessResult(updateInfo);
    }
}
