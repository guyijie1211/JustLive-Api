package work.yj1211.live.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import work.yj1211.live.model.platform.SimpleRoomInfo;

import java.util.List;

@Mapper
@Repository
public interface RoomMapper {
    List<SimpleRoomInfo> getRoomsByUid(String uid);
    int ifIsFollowed(@Param("uid")String uid, @Param("platform")String platForm, @Param("roomId") String roomId);
}
