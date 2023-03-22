package work.yj1211.live.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author guyijie
 * @date 2023/3/22 16:42
 **/
@Data
public class ActiveUsers {
    private int uid;
    private Date date;
    private int loginUserNum;
}
