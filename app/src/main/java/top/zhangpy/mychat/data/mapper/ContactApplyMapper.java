package top.zhangpy.mychat.data.mapper;

import android.os.Build;

import java.util.List;
import java.util.stream.Collectors;

import top.zhangpy.mychat.data.local.entity.ContactApply;
import top.zhangpy.mychat.data.remote.model.ContactApplyModel;
import top.zhangpy.mychat.util.Converter;

public class ContactApplyMapper {

    public static ContactApply mapToContactApply(ContactApplyModel contactApplyModel) {
        ContactApply contactApply = new ContactApply();
        contactApply.setApplicantId(Integer.parseInt(contactApplyModel.getApplicantId()));
        contactApply.setReceiverId(Integer.parseInt(contactApplyModel.getReceiverId()));
        contactApply.setGroupId(Integer.parseInt(contactApplyModel.getGroupId()));
        contactApply.setContactType(contactApplyModel.getContactType());
        contactApply.setMessage(contactApplyModel.getMessage());
        contactApply.setStatus(contactApplyModel.getStatus());
        contactApply.setApplyTime(Converter.fromUnixTimestamp(contactApplyModel.getApplyTime()));
        return contactApply;
    }

    public static List<ContactApply> mapToContactApplyList(List<ContactApplyModel> contactApplyModels) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return contactApplyModels.stream().map(ContactApplyMapper::mapToContactApply).toList();
        } else {
            return contactApplyModels.stream().map(ContactApplyMapper::mapToContactApply).collect(Collectors.toList());
        }
    }
}
