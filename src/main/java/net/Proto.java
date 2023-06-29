package net;


public class Proto {

    private int type;

    private String message;

    // 4字节type + 数据


    // 客户端请求服务端
    public static final int AskForSaveMsg= 1;

    public static final int AskForRegister = 2;
    public static final int AskForLogin = 3;

    public static final int SynMsg = 4;

    public static final int PubSynMsg = 5;

    // 服务端响应客户端
    public static final int RespForAllUsers = 6; // user | user | ...

    public static final int RespForLogin = 7;
    // 数据 : 一个boolean + 一个string ，如果boolean是true，string null
    // 数据 : 一个boolean + 一个string ，如果boolean是false，string 不是null


    // 客户端和客户端发消息
    public static final int AskForMakeFriend = 8; //

    public static final int RespForMakeFriend = 9;

    public static final int NewMessage = 10;

    //群聊

    //创建群聊
    public static final int AskForNewGroup = 11;

    //加入群聊
    public static final int AskToJoinGroup = 12;


    //离开群聊
    public static final int AskToLeave = 13;

    //发送群消息
    public static final int NewGroupMessage = 14;


    public static final int BacklogMsg = 15;

    // 服务端发给客户端
    public static final int SynGroupMessages = 16;

    public static final int RespForAllGroups = 17;

    public static final int AskToLogOut = 18;

    public static Proto getAskForLogout (String userId) {
        // id
        Proto p = new Proto(userId);
        p.type = AskToLogOut;
        return p;
    }

    public static Proto getAskForSaveMsg( String message) {
        // senderId | receiverId | msg | time
        Proto proto = new Proto(message);
        proto.type = AskForSaveMsg;
        return proto;
    }

    public static Proto getRespForAllUsers(String usersStr) {
        // user1 | user2 | ...
        Proto proto = new Proto(usersStr);
        proto.type = RespForAllUsers;
        return proto;
    }

    public static Proto getAskForMakeFriend (String hello) {
        // sender | receiver | hello | time
        Proto proto = new Proto(hello);
        proto.type = AskForMakeFriend;
        return proto;
    }

    public static Proto getRespForMakeFriend (String msg) {
        // sender | receiver | Yes or No | time
        Proto proto = new Proto(msg);
        proto.type = RespForMakeFriend;
        return proto;
    }

    public static Proto getNewMessage (String message) {
        // senderId | receiverId | msg | time
        Proto proto = new Proto(message);
        proto.type = NewMessage;
        return proto;
    }

    public static Proto getAskForRegister (String userStr) {
        // name + , + password
        Proto proto = new Proto(userStr);
        proto.type = AskForRegister;
        return proto;
    }

    public static Proto getAskForLogin (String userStr) {
        // name + ,  + password
        Proto proto = new Proto(userStr);
        proto.type = AskForLogin;
        return proto;
    }

    public static Proto getRespForLogin (String msg) {
        Proto proto = new Proto(msg);
        proto.type = RespForLogin;
        return proto;
    }

//群聊proto(无resp部分):
    public static Proto getAskForNewGroup(String message) {
        // 群聊名称 | 等级 | 1,2,3,4,5,6(id们)
        Proto p = new Proto(message);
        p.type = AskForNewGroup;
        return p;
    }

    public static Proto getAskToJoin(String message){
    //message: group_name  |  id1,id2,id3
        Proto p = new Proto(message);
        p.type = AskToJoinGroup;
        return p;
    }

    public static Proto getNotifyToLeave(String message){
    //message: sender_id | group_name
        Proto p = new Proto(message);
        p.type = AskToLeave;
        return p;
    }

    public static Proto getNewGroupMessage(String message){
    //message: senderId | groupName | msg | time
        Proto p = new Proto(message);
        p.type = NewGroupMessage;
        return p;
    }

    public static Proto getBacklogMsg (String message) {
        // msg1 | msg2 | msg3 ...
        Proto p = new Proto(message);
        p.type = BacklogMsg;
        return p;
    }

    public static Proto getSynMsg (String dataStr) {
        // selfId(也就是senderId) + | + data
        // data = version | receiver | msg1 | msg2 ...
        Proto p = new Proto(dataStr);
        p.type = SynMsg;
        return p;
    }

    public static Proto getPubSynMsg (String dataStr) {
        // versionId | receiverId | msg1 | msg2 ...
        Proto p = new Proto(dataStr);
        p.type = PubSynMsg;
        return p;
    }

    public static Proto getSynGroupMessages (String dataStr) {
        // groupName | msg1 | msg2 | msg3
        Proto p = new Proto(dataStr);
        p.type = SynGroupMessages;
        return p;
    }

    public static Proto getRespForAllGroups (String allGroups) {
        //  group1 | group2 | group3 ...
        // groupId | name | level | 1,2,3,4,5...
        Proto p = new Proto(allGroups);
        p.type = RespForAllGroups;
        return p;
    }

    private Proto(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return  type + ";" + message;
    }
}
