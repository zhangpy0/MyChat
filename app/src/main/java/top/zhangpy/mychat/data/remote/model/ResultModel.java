package top.zhangpy.mychat.data.remote.model;


import lombok.Data;

@Data
public class ResultModel<T> {

    private Integer code;


    private String message;

    private T data;

    public ResultModel(){}

    public static <T> ResultModel<T> build(T body, Integer code, String message) {
        ResultModel<T> result = new ResultModel<T>();
        if (body != null) {
            result.setData(body);
        }
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static<T> ResultModel<T> ok(){
        return ResultModel.ok(null);
    }

    public static<T> ResultModel<T> ok(T data){
        return build(data,200,"成功");
    }

    public static<T> ResultModel<T> ok(T data, String message){
        return build(data,200,message);
    }

    public static<T> ResultModel<T> fail(){
        return ResultModel.fail(null);
    }

    public static<T> ResultModel<T> fail(T data){
        return build(data,400,"失败");
    }

    public static<T> ResultModel<T> fail(T data, String message){
        return build(data,400,message);
    }

    public static<T> ResultModel<T> fail(Integer code, String message, T data){
        return build(data,code,message);
    }

}
