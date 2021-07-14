package com.qiniu.droid.imsdk;

import im.floo.BMXCallBack;
import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupManager;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXGroupService;

/**
 * 聊天室
 */
public class BXMChatRoomManager {

    private BMXGroupManager groupManager;

    protected BXMChatRoomManager(BMXGroupManager groupManager) {
        this.groupManager = groupManager;
    }

    /**
     * 创建聊天室
     *
     * @param name     聊天室名字
     * @param callBack
     */
    public void create(String name, BMXDataCallBack<BMXGroup> callBack) {
        groupManager.create(new BMXGroupService.CreateGroupOptions(name, "", true, true), callBack);
    }


    /**
     * 加入聊天室
     *
     * @param groupId
     * @param callBack
     */
    public void join(long groupId, BMXCallBack callBack) {
        groupManager.getGroupList(groupId, true, new BMXDataCallBack<BMXGroup>() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode, BMXGroup bmxGroup) {
                if (bmxGroup != null && bmxGroup.groupId() >= 0) {
                    groupManager.join(bmxGroup, "", callBack);
                } else {
                    if (callBack != null) {
                        callBack.onResult((bmxErrorCode));
                    }
                }
            }
        });
    }

    /**
     * 退出聊天室
     *
     * @param groupId
     * @param callBack
     */
    public void leave(long groupId, BMXCallBack callBack) {
        groupManager.getGroupList(groupId, true, new BMXDataCallBack<BMXGroup>() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode, BMXGroup bmxGroup) {
                if (bmxGroup != null && bmxGroup.groupId() >= 0) {
                    groupManager.leave(bmxGroup, callBack);
                } else {
                    if (callBack != null) {
                        callBack.onResult(bmxErrorCode);
                    }
                }
            }
        });
    }

    /**
     * 销毁聊天室
     *
     * @param groupId
     * @param callBack
     */
    public void destroy(long groupId, BMXCallBack callBack) {
        groupManager.getGroupList(groupId, true, new BMXDataCallBack<BMXGroup>() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode, BMXGroup bmxGroup) {
                if (bmxGroup != null && bmxGroup.groupId() >= 0) {
                    groupManager.destroy(bmxGroup, callBack);
                } else {
                    if (callBack != null) {
                        callBack.onResult((bmxErrorCode));
                    }
                }
            }
        });
    }

    /**
     * 获取群成员
     *
     * @param groupId
     * @param callBack
     */
    public void getMembers(long groupId, boolean forceUpdate,BMXDataCallBack<BMXGroupMemberList> callBack) {
        groupManager.getGroupList(groupId, forceUpdate, new BMXDataCallBack<BMXGroup>() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode, BMXGroup bmxGroup) {
                if (bmxGroup != null && bmxGroup.groupId() >= 0) {
                    groupManager.getMembers(bmxGroup, true, callBack);
                } else {
                    if (callBack != null) {
                        callBack.onResult((bmxErrorCode), null);
                    }
                }
            }
        });
    }

}
