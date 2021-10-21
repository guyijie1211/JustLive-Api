package work.yj1211.live.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.UserMapper;
import work.yj1211.live.utils.Global;
import work.yj1211.live.utils.http.HttpContentType;
import work.yj1211.live.utils.http.HttpRequest;
import work.yj1211.live.vo.BanInfo;
import work.yj1211.live.vo.UpdateInfo;
import work.yj1211.live.vo.UserInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Pattern PATTERN = Pattern.compile("domianload = '([\\s\\S]*?)';");
    private static final Pattern PATTERN2 = Pattern.compile("downloads = '([\\s\\S]*?)';");

    @Autowired
    private UserMapper userMapper;

    public UserInfo login(String userName, String password){
        return userMapper.login(userName, password);
    }

    public UserInfo findUserByName(String userName){
        return userMapper.findUserByName(userName);
    }

    public void register(UserInfo userInfo){
        userMapper.register(userInfo);
    }

    public void followRoom(String platform, String roomId, String uid){
        userMapper.followRoom(platform, roomId, uid);
    }

    public void unFollowRoom(String platform, String roomId, String uid){
        userMapper.unFollowRoom(platform, roomId, uid);
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
}
