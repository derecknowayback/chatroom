package net;

import dto.User;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *  全局类，只有一个实例，负责记录有哪些用户已经登录了，用户登录了会有ip记录
 */
public class LoginStatus {

    // 一个哈希表，记录每个用户对应的ip地址
    private static final HashMap<Integer, User> userIdMap = new HashMap<>();

    // 用户登录的时候，添加一条映射
    public static void addUser(User user){
        userIdMap.put(user.getId(),user);
    }

    // 用户下线后，移除映射
    public static void removeUser(int userId){
        userIdMap.remove(userId);
    }

    // 根据userId获取用户的ip地址，如果没有登录或者没有用户返回null
    public static InetAddress getIP(int userId){
        User user = userIdMap.get(userId);
        if (user == null) return null;
        return user.getIp();
    }

    // 返回某个用户是否登录
    public static boolean isOnline(int userId){
        return userIdMap.containsKey(userId);
    }

    public static List<User> getAllUser () {
        List<User> res = new ArrayList<>();
        Set<Integer> integers = userIdMap.keySet();

    }

}
