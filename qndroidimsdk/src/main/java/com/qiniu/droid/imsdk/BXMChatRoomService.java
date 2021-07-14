package com.qiniu.droid.imsdk;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXGroupMemberResultPage;
import im.floo.floolib.BMXGroupService;

public class BXMChatRoomService {

    private BMXGroupService groupService;

    protected BXMChatRoomService(BMXGroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * 创建聊天室
     * @param name     聊天室名字
     * @param group  空的群对象容器
     */
    public BMXErrorCode create(String name,BMXGroup group) {
       return groupService.create(new BMXGroupService.CreateGroupOptions(name, "", true, true),group);
    }

    /**
     * 加入聊天室
     * @param groupId
     */
    public BMXErrorCode join(long groupId) {
        BMXGroup bmxGroup = new BMXGroup();
        BMXErrorCode bmxErrorCode = groupService.search(groupId,  bmxGroup,true);
        if (bmxGroup != null && bmxGroup.groupId() >= 0) {
            return groupService.join(bmxGroup,"");
        } else {
            return bmxErrorCode;
        }
    }

    /**
     * 退出聊天室
     *
     * @param groupId
     */
    public BMXErrorCode leave(long groupId) {
        BMXGroup bmxGroup = new BMXGroup();
        BMXErrorCode bmxErrorCode = groupService.search(groupId,  bmxGroup,true);
        if (bmxGroup != null && bmxGroup.groupId() >= 0) {
            return groupService.leave(bmxGroup);
        } else {
            return bmxErrorCode;
        }
    }

    /**
     * 销毁聊天室
     * @param groupId
     */
    public BMXErrorCode destroy(long groupId) {
        BMXGroup bmxGroup = new BMXGroup();
        BMXErrorCode bmxErrorCode = groupService.search(groupId,  bmxGroup,true);
        if (bmxGroup != null && bmxGroup.groupId() >= 0) {
            return groupService.destroy(bmxGroup);
        } else {
            return bmxErrorCode;
        }
    }

    /**
     * 获取群成员
     * @param groupId
     */
    public BMXErrorCode getMembers(long groupId,boolean forceUpdate, BMXGroupMemberResultPage memberResultPage) {
        BMXGroup bmxGroup = new BMXGroup();
        BMXErrorCode bmxErrorCode = groupService.search(groupId,  bmxGroup,forceUpdate);
        if (bmxGroup != null && bmxGroup.groupId() >= 0) {
            return groupService.getMembers(bmxGroup,memberResultPage);
        } else {
            return bmxErrorCode;
        }
    }
}
