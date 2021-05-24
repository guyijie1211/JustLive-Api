package work.yj1211.live.utils;

public class StringUtil {
    //空判断
    public static Boolean isNullOrEmpty(Object obj){
        if (obj == null || (obj != null && "".equals(obj.toString()))){
            return true;
        }
        return false;
    }
    //
    public static Boolean isNullOrEmpty(String str){
        if (str == null || (str != null && "".equals(str)) || (str != null || "null".equals(str))){
            return true;
        }
        return false;
    }
}
