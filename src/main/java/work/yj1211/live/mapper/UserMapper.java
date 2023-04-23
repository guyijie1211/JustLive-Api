package work.yj1211.live.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import work.yj1211.live.model.ActiveUsers;
import work.yj1211.live.model.SimpleRoomInfo;
import work.yj1211.live.model.UserInfo;
import work.yj1211.live.model.platformArea.AreaSimple;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface UserMapper {
    UserInfo login(String userName, String password);
    UserInfo selectAllByUidAndPassword(@Param("uid") String uid, @Param("password")String password);
    UserInfo findUserByName(String userName);
    UserInfo findByUid(@Param("uid") String uid);
    List<AreaSimple> getAreasByUid(String uid);
    List<UserInfo> countUserActived(Date start);
    SimpleRoomInfo checkFollowed(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void register(UserInfo user);
    void followRoom(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void followArea(@Param("areaType") String areaType, @Param("area")String area, @Param("uid")String uid);
    void unFollowRoom(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void unFollowArea(@Param("areaType") String areaType, @Param("area")String area, @Param("uid")String uid);
    void changeUserInfo(UserInfo userInfo);
    void changePassword(@Param("userName")String userName, @Param("password") String password);
    void changeUserBan(UserInfo userInfo);
    void updateLastLogin(String uid);
    void insertActiveUserNum(ActiveUsers activeUsers);
}
