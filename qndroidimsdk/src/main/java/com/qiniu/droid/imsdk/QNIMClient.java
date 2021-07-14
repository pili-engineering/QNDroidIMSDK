package com.qiniu.droid.imsdk;

import im.floo.floolib.BMXChatManager;
import im.floo.floolib.BMXChatService;
import im.floo.floolib.BMXClient;
import im.floo.floolib.BMXConnectStatus;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroupManager;
import im.floo.floolib.BMXGroupService;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXNetworkType;
import im.floo.floolib.BMXPushManager;
import im.floo.floolib.BMXPushService;
import im.floo.floolib.BMXRosterManager;
import im.floo.floolib.BMXRosterService;
import im.floo.floolib.BMXSDKConfig;
import im.floo.floolib.BMXSignInStatus;
import im.floo.floolib.BMXUserManager;
import im.floo.floolib.BMXUserProfile;
import im.floo.floolib.BMXUserService;

/**
 * 七牛im 客户端
 */
public class QNIMClient {

    private static BMXClient client;
    private static BXMChatRoomManager chatRoomManager;
    private static BXMChatRoomService chatRoomService;
    /**
     *
     */
    public static void delete() {
        client.delete();
    }

    /**
     * 创建 QNIMClient
     *
     * @param config
     * @return
     */
    public static void init(BMXSDKConfig config) {
        client = BMXClient.create(config);
    }

    /**
     * 获得聊天室管理
     *
     * @return
     */
    public static BXMChatRoomManager getChatRoomManager() {
        if (chatRoomManager == null) {
            chatRoomManager = new BXMChatRoomManager(client.getGroupManager());
        }
        return chatRoomManager;
    }

    /**
     * 获得聊天室服务
     * @return
     */
    public static BXMChatRoomService getChatRoomService() {
        return chatRoomService;
    }

    /**
     * @return
     */
    public static BMXSDKConfig getSDKConfig() {
        return client.getSDKConfig();
    }

    public static BMXUserService getUserService() {
        return client.getUserService();
    }

    /**
     * 获取聊天Service
     *
     * @return
     */
    public static BMXChatService getChatService() {
        return client.getChatService();
    }

    /**
     * @return
     */
    public static BMXGroupService getGroupService() {
        return client.getGroupService();
    }

    public static BMXRosterService getRosterService() {
        return client.getRosterService();
    }

    public static BMXPushService getPushService() {
        return client.getPushService();
    }

    public static BMXUserManager getUserManager() {
        return client.getUserManager();
    }

    /**
     * 获取聊天Manager
     *
     * @return
     */
    public static BMXChatManager getChatManager() {
        return client.getChatManager();
    }

    /**
     * 获取群组Manager
     *
     * @return
     */
    public static BMXGroupManager getGroupManager() {
        return client.getGroupManager();
    }

    /**
     * 获取好友Manager
     *
     * @return
     */
    public static BMXRosterManager getRosterManager() {
        return client.getRosterManager();
    }

    /**
     * 获取推送Manager
     *
     * @return
     */
    public static BMXPushManager getPushManager() {
        return client.getPushManager();
    }

    /**
     * 注册新用户，username和password是必填参数
     *
     * @param username          用户名
     * @param password          用户密码
     * @param bmxUserProfilePtr 注册成功后从该函数处获取新注册用户的Profile信息，初始传入指向为空的shared_ptr对象即可
     * @return BMXErrorCode
     */
    public static BMXErrorCode signUpNewUser(String username, String password, BMXUserProfile bmxUserProfilePtr) {
        return client.signUpNewUser(username, password, bmxUserProfilePtr);
    }

    /**
     * 通过用户名登录
     *
     * @param name     用户名
     * @param password 用户密码
     * @return
     */
    public static BMXErrorCode signInByName(String name, String password) {
        return client.signInByName(name, password);
    }

    /**
     * 通过用户ID登录
     *
     * @param uid      用户id
     * @param password 用户密码
     * @return
     */
    public static BMXErrorCode signInById(long uid, String password) {
        return client.signInById(uid, password);
    }

    /**
     * 通过用户名快速登录（要求之前成功登录过，登录速度较快）
     *
     * @param name     用户名
     * @param password 用户密码(用于sdk在内部token到期时自动更新用户token)
     * @return
     */
    public static BMXErrorCode fastSignInByName(String name, String password) {
        return client.fastSignInByName(name, password);
    }

    /**
     * 通过用户ID快速登录（要求之前成功登录过，登录速度较快）
     *
     * @param uid      用户id
     * @param password 用户密码(用于sdk在内部token到期时自动更新用户token)
     * @return
     */
    public static BMXErrorCode fastSignInById(long uid, String password) {
        return client.fastSignInById(uid, password);
    }

    /**
     * 退出登录
     *
     * @param uid                退出用户的uid（默认输入0则退出当前登陆用户）
     * @param ignoreUnbindDevice
     * @return
     */
    public static BMXErrorCode signOut(long uid, boolean ignoreUnbindDevice) {
        return client.signOut(uid, ignoreUnbindDevice);
    }

    /**
     * 退出登录
     *
     * @param uid 退出用户的uid（默认输入0则退出当前登陆用户）
     * @return
     */
    public static BMXErrorCode signOut(long uid) {
        return client.signOut(uid);
    }

    /**
     * 退出登录
     *
     * @return
     */
    public static BMXErrorCode signOut() {
        return client.signOut();
    }

    /**
     * 获取当前和服务器的连接状态
     *
     * @return
     */
    public static BMXConnectStatus connectStatus() {
        return client.connectStatus();
    }

    /**
     * 获取当前的登录状态
     *
     * @return
     */
    public static BMXSignInStatus signInStatus() {
        return client.signInStatus();
    }

    /**
     * 强制重新连接
     */
    public static void reconnect() {
        client.reconnect();
    }

    /**
     * 处理网络状态发送变化
     *
     * @param type      变化后的网络类型
     * @param reconnect 网络是否需要重连
     */
    public static void onNetworkChanged(BMXNetworkType type, boolean reconnect) {
        client.onNetworkChanged(type, reconnect);
    }

    /**
     * 断开网络连接
     */
    public static void disconnect() {
        client.disconnect();
    }

    /**
     * 更改SDK的appId，本操作会同时更新BMXConfig中的appId。
     *
     * @param appId     新变更的appId
     * @param appSecret 新变更的appSecret
     * @return
     */
    public static BMXErrorCode changeAppId(String appId, String appSecret) {
        return client.changeAppId(appId, appSecret);
    }

    /**
     * 更改SDK的appId，本操作会同时更新BMXConfig中的appId。
     *
     * @param appId 新变更的appId
     * @return
     */
    public static BMXErrorCode changeAppId(String appId) {
        return client.changeAppId(appId);
    }

    /**
     * 获取app的服务器网络配置，在初始化SDK之后登陆之前调用，可以提前获取服务器配置加快登陆速度。
     *
     * @param isLocal - 为true则使用本地缓存的dns配置，为false则从服务器获取最新的配置。
     * @return
     */
    public static BMXErrorCode initializeServerConfig(boolean isLocal) {
        return client.initializeServerConfig(isLocal);
    }

    /**
     * 发送消息，消息状态变化会通过listener通知，在发送群组消息且指定的群组为开启群组已读回执的情况下， 该接口会自动获取群成员列表id并且填充到message config中去，无需客户端自己进行群组成员列表的填充操作。
     *
     * @param msg
     */
    public static void sendMessage(BMXMessage msg) {
        client.sendMessage(msg);
    }

}
