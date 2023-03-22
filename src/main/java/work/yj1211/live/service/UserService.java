package work.yj1211.live.service;

import cn.hutool.core.date.DateUtil;
import com.aliyun.dm20151123.models.SingleSendMailRequest;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.UserMailMapper;
import work.yj1211.live.mapper.UserMapper;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.utils.platForms.Douyu;
import work.yj1211.live.vo.*;
import work.yj1211.live.vo.platformArea.AreaSimple;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Pattern PATTERN = Pattern.compile("domianload = '([\\s\\S]*?)';");
    private static final Pattern PATTERN2 = Pattern.compile("downloads = '([\\s\\S]*?)';");

    @Value("${ali.mail.accessKeyId}")
    private String accessKeyId;

    @Value("${ali.mail.accessKeySecret}")
    private String accessKeySecret;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMailMapper mailMapper;

    public UserInfo login(String userName, String password){
        UserInfo userInfo = userMapper.login(userName, password);
        if (userInfo == null) {
            String uid = mailMapper.selectUidByMail(userName);
            userInfo = userMapper.selectAllByUidAndPassword(uid, password);
        }
        return userInfo;
    }

    public void updateLastLogin(String uid) {
        userMapper.updateLastLogin(uid);
    }

    public void insertActiveUserNum() {
        Date date = DateUtil.yesterday();
        List<UserInfo> userInfoList = userMapper.countUserActived(date);
        ActiveUsers activeUsers = new ActiveUsers();
        activeUsers.setLoginUserNum(userInfoList.size());
        userMapper.insertActiveUserNum(activeUsers);
    }

    public UserInfo findUserByName(String userName){
        return userMapper.findUserByName(userName);
    }

    public void register(UserInfo userInfo){
        userMapper.register(userInfo);
    }

    public void followRoom(String platform, String roomId, String uid){
        if (platform.equalsIgnoreCase("douyu")) {
            roomId = Douyu.getRealRoomId(roomId);
        }
        if (userMapper.checkFollowed(platform, roomId, uid) == null) {
            userMapper.followRoom(platform, roomId, uid);
        }
    }

    public void followArea(String areaType, String area, String uid){
        userMapper.followArea(areaType, area, uid);
    }

    /**
     * 获取用户关注的所有分类
     * @param uid     用户uid
     * @return
     */
    public List<AreaSimple> getAreasByUid(String uid){
        List<AreaSimple> areaList = userMapper.getAreasByUid(uid);
        return areaList;
    }

    public void unFollowRoom(String platform, String roomId, String uid){
        userMapper.unFollowRoom(platform, roomId, uid);
    }

    public void unFollowArea(String areaType, String area, String uid){
        userMapper.unFollowArea(areaType, area, uid);
    }

    public void changeUserInfo(UserInfo userInfo){
        userMapper.changeUserInfo(userInfo);
    }

    public void changeUserBan(UserInfo userInfo){
        userMapper.changeUserBan(userInfo);
    }

    public void changePassword(String userName, String password){
        userMapper.changePassword(userName, password);
    }

    public UpdateInfo checkUpdate() {
//        UpdateInfo updateInfo = new UpdateInfo(Global.updateInfo);
//        String real_updateUrl = getDownloadUrl(updateInfo.getUpdateUrl());
//        updateInfo.setUpdateUrl(real_updateUrl);
        return Global.updateInfo;
    }

    //蓝奏根据端id获取直链
    public String getDownloadUrl(String shortUrl) {
        String share_url = "https://wwe.lanzoui.com/tp/" + shortUrl;
        String response = HttpRequest.create(share_url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        Matcher matcher = PATTERN.matcher(response);
        Matcher matcher2 = PATTERN2.matcher(response);
        if (!matcher2.find()){
            return "";
        }
        if (!matcher.find()) {
            return "";
        }
        String result = matcher.group();
        String result2 = matcher2.group();
        result = result.substring(result.indexOf("'") + 1, result.lastIndexOf("'"));
        result2 = result2.substring(result2.indexOf("'") + 1, result2.lastIndexOf("'"));
        return result + result2;
    }

    /**
     * 绑定邮箱
     * @param uid
     * @param mail
     */
    public void bindMail(String uid, String mail) {
        List<UserMail> userMail = mailMapper.selectAllByUid(uid);
        if (CollectionUtils.isEmpty(userMail)) {
            UserMail insertMail = new UserMail();
            insertMail.setUid(uid);
            insertMail.setMail(mail);
            mailMapper.insert(insertMail);
        } else {
            mailMapper.updateMailByUid(mail, uid);
        }
    }

    /**
     * 找回密码
     * @param mail
     * @param password
     * @return
     */
    public String changePassByMail(String mail, String password) {
        List<UserMail> userMailList = mailMapper.selectAllByMail(mail);
        if (CollectionUtils.isEmpty(userMailList)) {
            return "此邮箱未绑定账号";
        } else {
            String userName = userMapper.findByUid(userMailList.get(0).getUid()).getUserName();
            userMapper.changePassword(userName, password);
            return "密码修改成功";
        }
    }

    /**
     * 发送邮箱验证码
     * @param mail
     * @param code
     */
    public String sendMail(String mail, String code, String action) {
        List<UserMail> userMailList = mailMapper.selectAllByMail(mail);
        if ("forget".equalsIgnoreCase(action)) {
            if (CollectionUtils.isEmpty(userMailList)) {
                return "此邮箱未绑定账号";
            } else {
                String nickName = userMapper.findByUid(userMailList.get(0).getUid()).getNickName();
                try {
                    singleSendMail(mail, code, nickName, "找回密码");
                    return  "邮件已发送";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "邮件发送失败";
                }
            }
        } else if ("bind".equalsIgnoreCase(action)) {
            if (!CollectionUtils.isEmpty(userMailList)) {
                return "邮箱已被绑定";
            }
            try {
                singleSendMail(mail, code, "", "绑定邮箱");
                return  "邮件已发送";
            } catch (Exception e) {
                e.printStackTrace();
                return "邮件发送失败";
            }
        }
        return "邮件发送失败";
    }

    private void singleSendMail(String mailAddress, String code, String nickName, String action) throws Exception {
        String mailBody = "<!DOCTYPE html><html lang=\"en\" xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"UTF-8\"><title>邮箱验证码</title><style>table { width: 700px; margin: 0 auto; } #top { width: 700px; border-bottom: 1px solid #ccc; margin: 0 auto 30px; } #top table { font: 12px Tahoma, Arial, 宋体; height: 40px; } #content { width: 680px; padding: 0 10px; margin: 0 auto; } #content_top { line-height: 1.5; font-size: 14px; margin-bottom: 25px; color: #4d4d4d; } #content_top strong { display: block; margin-bottom: 15px; } #content_top strong span { color: #f60; font-size: 16px; } #verificationCode { color: #f60; font-size: 24px; } #content_bottom { margin-bottom: 30px; } #content_bottom small { display: block; margin-bottom: 20px; font-size: 12px; color: #747474; } #bottom { width: 700px; margin: 0 auto; } #bottom div { padding: 10px 10px 0; border-top: 1px solid #ccc; color: #747474; margin-bottom: 20px; line-height: 1.3em; font-size: 12px; } #content_top strong span { font-size: 18px; color: #3a9aed; } #sign { text-align: right; font-size: 18px; color: #3a9aed; font-weight: bold; } #verificationCode { height: 100px; width: 680px; text-align: center; margin: 30px 0; } #verificationCode div { height: 100px; width: 680px; } .button { color: #3a9aed; margin-left: 10px; height: 80px; width: 80px; resize: none; font-size: 42px; border: none; outline: none; padding: 10px 15px; background: #ededed; text-align: center; border-radius: 17px; box-shadow: 6px 6px 12px #cccccc, -6px -6px 12px #ffffff; }</style></head><body><table><tbody><tr><td><div id=\"top\"><table><tbody><tr><td></td></tr></tbody></table></div><div id=\"content\"><div id=\"content_top\"><strong>Hi,userName</strong></br><strong>您正在进行\n" +
                "<span>" + action + "</span>操作，验证码为：</strong><div id=\"verificationCode\"><button class=\"button\">code1</button><button class=\"button\">code2</button><button class=\"button\">code3</button><button class=\"button\">code4</button><button class=\"button\">code5</button></div></div></div><div id=\"bottom\"><div><p>此为系统邮件，请勿回复\n" +
                "<br>请保管好您的邮箱，避免账号被他人盗用</p></div></div></td></tr></tbody></table></body>";
        mailBody = mailBody.replaceAll("userName", nickName)
                .replaceAll("code1", code.substring(0,1))
                .replaceAll("code2", code.substring(1,2))
                .replaceAll("code3", code.substring(2,3))
                .replaceAll("code4", code.substring(3,4))
                .replaceAll("code5", code.substring(4,5));
        Config config = new Config()
                // 您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dm.aliyuncs.com";
        com.aliyun.dm20151123.Client client = new com.aliyun.dm20151123.Client(config);
        SingleSendMailRequest singleSendMailRequest = new SingleSendMailRequest()
                .setAccountName("justlive@mail.yj1211.work")
                .setAddressType(1)
                .setReplyToAddress(true)
                .setTagName("Verification")
                .setToAddress(mailAddress)
                .setSubject("JustLive验证码")
                .setHtmlBody(mailBody)
                .setFromAlias("YJ1211");
        RuntimeOptions runtime = new RuntimeOptions();
        client.singleSendMailWithOptions(singleSendMailRequest, runtime);
    }

    public String getUidByUserName(String userName) {
        UserInfo userInfo = userMapper.findUserByName(userName);
        if (userInfo != null) {
            return userInfo.getUid();
        }
        return null;
    }
}
