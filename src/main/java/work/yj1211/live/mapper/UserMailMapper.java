package work.yj1211.live.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import work.yj1211.live.vo.UserMail;

import java.util.List;

@Mapper
public interface UserMailMapper {
    int deleteByPrimaryKey(@Param("uid") String uid, @Param("mail") String mail);

    int insert(UserMail row);

    int insertSelective(UserMail row);

    List<UserMail> selectAllByMail(@Param("mail") String mail);

    List<UserMail> selectAllByUid(@Param("uid") String uid);

    int updateMailByUid(@Param("mail") String mail, @Param("uid") String uid);

    String selectUidByMail(@Param("mail") String mail);
}