package work.yj1211.live.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import work.yj1211.live.model.LiveRoomInfo;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface AllRoomsMapper {
    void updateRooms(@Param("roomList") List<LiveRoomInfo> roomList);
    void updateOneRoom(@Param("roomInfo") LiveRoomInfo roomInfo);
    List<LiveRoomInfo> getRecommendRooms(@Param("page") int page, @Param("size") int size);
    void setOffLine(@Param("platform") String platform, @Param("time") Date time);
}
