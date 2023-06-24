package net;

public class Proto {

    private int type;

    private String message;

    // 4字节type + 数据


    // 客户端请求服务端
    public static final int AskForAllUsers = 1;
    public static final int AskForSaveMsg= 2;

    public static final int AskForRegister = 3;
    public static final int AskForLogin = 4;

    public static final int SynMsg = 100;

    // 服务端响应客户端
    public static final int RespForAllUsers = 5; //
    public static final int RespForSaveMsg = 6;

    public static final int RespForLogin = 7;
    // 数据 : 一个boolean + 一个string ，如果boolean是true，string null
    // 数据 : 一个boolean + 一个string ，如果boolean是false，string 不是null

    public static final int SendForMsg = 1012;

    // 客户端和客户端发消息
    public static final int AskForMakeFriend = 8;

    public static final int RespForMakeFriend = 9;

    public static final int NewMessage = 10;


    // 请求所有用户
    public static Proto getAskForAllUsers( ) {
        Proto proto = new Proto("");
        proto.type = AskForAllUsers;
        return proto;
    }

    public static Proto getAskForSaveMsg( String message) {
        Proto proto = new Proto(message);
        proto.type = AskForSaveMsg;
        return proto;
    }

    public static Proto getRespForAllUsers(String usersStr) {
        Proto proto = new Proto(usersStr);
        proto.type = RespForAllUsers;
        return proto;
    }

    public static Proto getRespForSaveMsg (String messageInfo) {
        Proto proto = new Proto(messageInfo);
        proto.type = RespForSaveMsg;
        return proto;
    }

    public static Proto getAskForMakeFriend (String hello) {
        Proto proto = new Proto(hello);
        proto.type = AskForMakeFriend;
        return proto;
    }

    public static Proto getRespForMakeFriend (boolean doAgree) {
        Proto proto = new Proto(doAgree ? "Y" : "N");
        proto.type = RespForMakeFriend;
        return proto;
    }

    public static Proto getNewMessage (String message) {
        Proto proto = new Proto(message);
        proto.type = NewMessage;
        return proto;
    }

    public static Proto getAskForRegister (String userStr) {
        Proto proto = new Proto(userStr);
        proto.type = AskForRegister;
        return proto;
    }

    public static Proto getAskForLogin (String userStr) {
        Proto proto = new Proto(userStr);
        proto.type = AskForLogin;
        return proto;
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
