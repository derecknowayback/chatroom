package dto;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//JDBC工具类
public class DBManager {
    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    private static Connection connection;

    private static final String loginFailedPrefix = "false" + ",";

    private static final String loginSuccessPrefix = "success" + ",";


    //静态代码块
    static {
        //读取资源文件，获取值
        try {
            //1.创建Properties集合类
            Properties pro =new Properties();
            //2.加载文件
            //这里假如找不到配置文件，就用绝对路径
            pro.load(new FileReader("database.properties"));

            //3.获取数据，赋值
            url = pro.getProperty("url");
            user = pro.getProperty("user");
            password = pro.getProperty("password");
            driver = pro.getProperty("driver");

            //4.注册驱动
            Class.forName(driver);

            connection = DriverManager.getConnection(url, user, password);
        } catch (IOException | ClassNotFoundException  | SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void closeRsAndStatement (ResultSet rs, Statement stemt) {
        if(rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(stemt!=null){
            try {
                stemt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getRows (ResultSet rs) throws SQLException {
        rs.last();
        int rowCount = rs.getRow();
        rs.beforeFirst();
        return rowCount;
    }


    private static final String NameOrPasswordNull = "name and password are required .";
    private static final String NoUserPrefix = "No user: ";
    private static final String WrongPassword = "Wrong Password";


    /**
     * 用于用于判断用户名和密码是否正确
     * @param name 用户名
     * @param password 密码
     * @return
     */
    public static String checkUser(String name,String password) {
        if (name==null || password==null) {
            return loginFailedPrefix +NameOrPasswordNull;
        }
        List<User> users = getByName(name);
        if (users.size() == 0) {
            return loginFailedPrefix +NoUserPrefix + name;
        }
        User user = users.get(0);
        if (! user.getPassword().equals(password)) {
            return loginFailedPrefix + WrongPassword;
        }
        return loginSuccessPrefix +user.getId();
    }


    private static List<User> getByName(String name) {
        Statement stmt=null;
        ResultSet rs=null;
        List<User> users = new ArrayList<>();
        try {
            stmt = connection.createStatement();
            String sql ;
            if (name != null) {
                sql = "select * from user  where username ='"+ name +"' ";
            } else {
                sql = "select * from user ";
            }
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String pwd = rs.getString("password");
                //封装user对象
                User user = new User(id,username,pwd);
                //装载集合
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            closeRsAndStatement(rs,stmt);
        }
        return users;
    }


    /**
     * 用于查询单个用户 输入id 返回对象
     * @param userId
     * @return user 返回对象
     */
    public static User findById(int userId) {
        Statement stmt=null;
        ResultSet rs=null;
        try {
            stmt = connection.createStatement();
            String sql = "select * from user where id = '"+ userId + "'";
            rs = stmt.executeQuery(sql);
            if (! rs.first()) {
                return null;
            }
            int id = rs.getInt("id");
            String username = rs.getString("username");
            String pwd = rs.getString("password");
            User user = new User(id,username,pwd);
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            closeRsAndStatement(rs,stmt);
        }
        return null;
    }

    /**
     * 用于插入新用户 返回true插入成功
     * @param name
     * @param password
     * @return
     */
    public static String insertUser(String name,String password) {
        if (name == null || password == null){
            return loginFailedPrefix + NameOrPasswordNull;
        }
        List<User> sameName = getByName(name);
        if (sameName.size() > 0) {
            return loginFailedPrefix + "The username is occupied .";
        }
        PreparedStatement stmt = null;
        try {
            String sql = "insert into user (username, password) values (?, ?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, password);
            int count = stmt.executeUpdate();
            if(count < 0)
                return null;
            List<User> users = getByName(name);
            return loginSuccessPrefix + users.get(0).getId();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(null,stmt);
        }
        return null;
    }

    /**
     * 查询所有用户
     * @return 返回users集合
     */
    public static List<User> findAllUsers() {
        return getByName(null);
    }



    // 创建群组并添加成员
    /**
     * 创建群聊
     * @param limit
     * @param name
     * @param users
     * @return
     * @throws SQLException
     */
    public static int createGroup(int limit, String name, List<Integer> memberIDs)  {
        String memberIDsStr = String.join(",", memberIDs.stream().map(Object::toString).toArray(String[]::new));
        String sql = "INSERT INTO `group` (`limit`, `groupMember`, `name`) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        try{
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, limit);
            stmt.setString(2, memberIDsStr);
            stmt.setString(3, name);
            stmt.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(null,stmt);
        }
        return false;
    }

    /**
     * 添加对应用户
     * 添加成功返回true
     * @param groupid
     * @param user 添加的用户
     * @return
     * @throws SQLException
     */
    public static boolean addUserToGroup(int groupid, int userId)  {
        // 查询原来的群组成员列表
        String selectSql = "SELECT * FROM group where groupID = '" + groupid + "'";
        PreparedStatement selectStmt = null;
        ResultSet rs = null;
        PreparedStatement updateStmt = null;
        try {
            selectStmt = connection.prepareStatement(selectSql);
            rs = selectStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Group not found");
            }
            String oldMemberStr = rs.getString("groupMember");
            String[] oldMemberIDs = oldMemberStr.split(",");
            // 检查用户是否已经在群组中
            for (String memberID : oldMemberIDs) {
                if (memberID.equals(String.valueOf(userId))) {
                    return true; // 用户已经在群组中，无需添加
                }
            }
            // 添加用户到群组
            String newMemberStr = oldMemberStr + "," + userId;
            String updateSql = "UPDATE group SET groupMember = ? WHERE groupID = '" + groupid + "'";
            updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setString(1, newMemberStr);
            return updateStmt.executeUpdate() > 0;
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeRsAndStatement(rs,selectStmt);
            closeRsAndStatement(null,updateStmt);
        }
        return false;
    }

    /**
     * 删除用户
     * 删除成员成功返回true
     * @param groupID
     * @param user
     * @return
     * @throws SQLException
     */
    public static boolean removeUserFromGroup(int groupID, User user) throws SQLException {
        // 查询原来的群组成员列表
        String selectSql = "SELECT * FROM `group` WHERE `groupID` = '"+groupID+"'";
        PreparedStatement selectStmt = null;
        ResultSet rs = null;
        PreparedStatement updateStmt = null;
        try  {
            selectStmt = connection.prepareStatement(selectSql);
            rs = selectStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Group not found");
            }
            String oldMemberStr = rs.getString("groupMember");
            String[] oldMemberIDs = oldMemberStr.split(",");
            // 检查用户是否在群组中
            boolean found = false;
            StringBuilder newMemberStrBuilder = new StringBuilder();
            for (String memberID : oldMemberIDs) {
                if (memberID.equals(String.valueOf(user.getId()))) {
                    found = true;
                } else {
                    newMemberStrBuilder.append(memberID).append(",");
                }
            }
            if (!found) {
                return true; // 用户不在群组中，无需删除
            }
            String newMemberStr = newMemberStrBuilder.toString();
            if (newMemberStr.endsWith(",")) {
                newMemberStr = newMemberStr.substring(0, newMemberStr.length() - 1);
            }
            // 更新群组成员列表
            String updateSql = "UPDATE group SET groupMember = ? WHERE groupID = '" + groupID + "'";
            updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setString(1, newMemberStr);
            return updateStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(rs,selectStmt);
            closeRsAndStatement(null,updateStmt);
        }
        return false;
    }

    // 查询指定群组的成员列表
    /**
     * 找对应群组的用户id
     * 返回对应用户id的数组
     * @param groupID
     * @return
     * @throws SQLException
     */
    public static int[] getGroupMembers(int groupID) {
        String selectSql = "SELECT * FROM `group` WHERE `groupID` = '"+groupID+"'";
        PreparedStatement selectStmt = null;
        ResultSet rs = null;
        try {
            selectStmt = connection.prepareStatement(selectSql);
            // selectStmt.setInt(1, groupID);
            rs = selectStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Group not found");
            }
            String memberStr = rs.getString("groupMember");
            String[] memberIDs = memberStr.split(",");
            List<Integer> memberList = new ArrayList<>();
            for (String memberID : memberIDs) {
                memberList.add(Integer.parseInt(memberID));
            }
            int[] members = new int[memberList.size()];
            for (int i = 0; i < memberList.size(); i++) {
                members[i] = memberList.get(i);
            }
            return members;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(rs,selectStmt);
        }
        return null;
    }

    public static Group getGroupIdByName() {

    }



}
