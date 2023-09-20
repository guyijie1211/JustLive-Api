package work.yj1211.live.factory;


import work.yj1211.live.enums.ResultCode;
import work.yj1211.live.model.response.Result;

public class ResultFactory {
    public static Result buildSuccessResult(Object data){
        return buildResult(ResultCode.SUCCESS,"成功",data);
    }
    public static Result buildFailResult(String message){
        return buildResult(ResultCode.FAIL,message,null);
    }
    public static Result buildResult(ResultCode resultCode, String message, Object data){
        return buildResult(resultCode.code, message, data);
    }
    public static Result buildResult(int resultCode, String message, Object data){
        return new Result(resultCode, message, data);
    }
}
