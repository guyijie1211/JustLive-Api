package work.yj1211.live.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import work.yj1211.live.vo.BanInfo;
import work.yj1211.live.vo.SimpleRoomInfo;
import work.yj1211.live.vo.UserInfo;
import work.yj1211.live.vo.platformArea.AreaSimple;

import java.util.List;

@Mapper
@Repository
public interface UserMapper {
    UserInfo login(String userName, String password);
    UserInfo selectAllByUidAndPassword(@Param("uid") String uid, @Param("password")String password);
    UserInfo findUserByName(String userName);
    UserInfo findByUid(@Param("uid") String uid);
    List<AreaSimple> getAreasByUid(String uid);
    SimpleRoomInfo checkFollowed(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void register(UserInfo user);
    void followRoom(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void followArea(@Param("areaType") String areaType, @Param("area")String area, @Param("uid")String uid);
    void unFollowRoom(@Param("platform") String platform, @Param("roomId")String roomId, @Param("uid")String uid);
    void unFollowArea(@Param("areaType") String areaType, @Param("area")String area, @Param("uid")String uid);
    void changeUserInfo(UserInfo userInfo);
    void changePassword(@Param("userName")String userName, @Param("password") String password);
    void changeUserBan(UserInfo userInfo);
}
