package work.yj1211.live.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import work.yj1211.live.mapper.UserMapper;
import work.yj1211.live.vo.BanInfo;
import work.yj1211.live.vo.UserInfo;

import java.util.List;

@Service
public class UserService {

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
}
