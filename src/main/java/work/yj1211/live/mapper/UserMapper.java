package work.yj1211.live.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import work.yj1211.live.vo.BanInfo;
import work.yj1211.live.vo.UserInfo;

import java.util.List;

@Mapper
@Repository
public interface UserMapper {
    UserInfo login(String userName, String password);
    UserInfo findUserByName(String userName);
    void register(UserInfo user);
    void followRoom(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void unFollowRoom(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void changeUserInfo(UserInfo userInfo);
    void changePassword(@Param("userName")String userName, @Param("password") String password);
    void changeUserBan(UserInfo userInfo);
}
