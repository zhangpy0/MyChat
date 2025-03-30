package top.zhangpy.mychat.data.exception;

import top.zhangpy.mychat.data.remote.model.ResultModel;

public class NetExceptionHandler {

    public static boolean responseCheck(ResultModel resultModel, int flag) {
        if (resultModel == null) {
            throw new RuntimeException("failed to get response");
        }
        if (resultModel.getCode() != 200) {
            throw new RuntimeException(resultModel.getCode() + ": " + resultModel.getMessage());
        }
        return true;
    }

    public static <T> T responseCheck(ResultModel<T> resultModel) {
        if (resultModel == null) {
            throw new RuntimeException("failed to get response");
        }
        if (resultModel.getCode() != 200) {
            throw new RuntimeException(resultModel.getCode() + ": " + resultModel.getMessage());
        }
        return resultModel.getData();
    }
}
