package work.yj1211.live.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import work.yj1211.live.vo.LiveRoomInfo;

import java.util.List;

@Mapper
@Repository
public interface AllRoomsMapper {
    void updateRooms(@Param("roomList") List<LiveRoomInfo> roomList);
    void updateOneRoom(@Param("roomInfo") LiveRoomInfo roomInfo);
}
