package top.zhangpy.mychat.data.remote.model;

import lombok.Data;

/*
List<ContactApply> contactApplies = contactService.getContactApplyFromOthers(Integer.valueOf(userId));
        List<Map<String, String>> res = new ArrayList<>(List.of());
        for (ContactApply contactApply : contactApplies) {
            contactApply.nullToEmpty();
            res.add(Map.of(
                    "applicantId", String.valueOf(contactApply.getApplicantId()),
                    "receiverId", String.valueOf(contactApply.getReceiverId()),
                    "groupId", String.valueOf(contactApply.getGroupId()),
                    "contactType", contactApply.getContactType(),
                    "message", contactApply.getMessage(),
                    "status", contactApply.getStatus()
            ));
        }
 */
@Data
public class ContactApplyModel {
    private String applicantId;
    private String receiverId;
    private String groupId;
    private String contactType;
    private String message;
    private String status;
    private Long applyTime;
}
