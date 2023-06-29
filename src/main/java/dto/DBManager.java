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

    private static final String loginSuccessPrefix = "true" + ",";

    //静态代码块
    static {
        //读取资源文件，获取值
        try {
            //1.创建Properties集合类
            Properties pro =new Properties();
            //2.加载文件
            pro.load(new FileReader("D:\\project\\swing\\withpom\\src\\main\\resources\\database.properties"));

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


    public static List<User> getByName(String name) {
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
    public static int createGroup(int limit, String name, List<Integer> memberIDs )  {
        String memberIDsStr = String.join(",", memberIDs.stream().map(Object::toString).toArray(String[]::new));
        String sql = "INSERT INTO `tb_group` (`limit`, `groupMember`, `name`) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, limit);
            stmt.setString(2, memberIDsStr);
            stmt.setString(3, name);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
//            Statement.RETURN_GENERATED_KEYS 是 Java 中与 java.sql.Statement 类一起使用的常量值。
//            当执行一个将数据插入带有自增列的表的 SQL 语句时，可以将这个常量作为标志传递给 executeUpdate() 方法，
//            以指示数据库返回插入行的自动生成键。
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(rs,stmt);
        }
        return -1;
    }



    public static boolean addUserToGroup(int userId, int groupId)  {
        // 查询group表，获取当前group的成员列表和限制人数
        String query = "SELECT * FROM tb_group where groupID = '" + groupId + "'";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        PreparedStatement updateStmt = null;
        try {
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int limit = rs.getInt("limit");
                String groupMember = rs.getString("groupMember");
                String[] memberIds = groupMember.split(",");
                int currentSize = memberIds.length;
                if (currentSize < limit) {
                    // 添加新用户的id到groupMember中
                    String newGroupMember = groupMember + "," + userId;
                    String updateQuery = "UPDATE tb_group SET groupMember=? WHERE groupID=?";
                    updateStmt = connection.prepareStatement(updateQuery);
                    updateStmt.setString(1, newGroupMember);
                    updateStmt.setInt(2, groupId);
                    updateStmt.executeUpdate();
                    return true;
                } else {
                    // group已满，无法添加新用户
                    return false;
                }
            } else {
                // 没有找到对应的group
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeRsAndStatement(rs,stmt);
            closeRsAndStatement(null,updateStmt);
        }
    }



    public static boolean removeUserFromGroup(int groupID, int userId) {
        // 查询原来的群组成员列表
        String selectSql = "SELECT * FROM `tb_group` WHERE `groupID` = '"+groupID+"'";
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
                if (memberID.equals(String.valueOf(userId))) {
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
            String updateSql = "UPDATE tb_group SET groupMember = ? WHERE groupID = '" + groupID + "'";
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
        String selectSql = "SELECT * FROM `tb_group` WHERE `groupID` = '"+groupID+"'";
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



    /**
     * 返回群聊对象通过群聊名
     * @param groupName 输入的群聊名
     */
    public static Group getGroupByName(String groupName)  {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // 构造 SQL 查询语句
            String sql = "SELECT * FROM tb_group WHERE name = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, groupName);

            // 执行 SQL 查询语句并获取结果集
            rs = stmt.executeQuery();

            // 如果结果集为空则返回 null
            if (!rs.next()) {
                return null;
            }

            // 从结果集中获取 groupID、limit、groupMember 和 name
            int groupID = rs.getInt("groupID");
            int limit = rs.getInt("limit");
            String groupMemberString = rs.getString("groupMember");
            String name = rs.getString("name");

            // 将 groupMember 字符串转换为 List<Integer>
            List<Integer> groupMember = new ArrayList<>();
            for (String id : groupMemberString.split(",")) {
                groupMember.add(Integer.parseInt(id));
            }
            // 构造 Group 对象并返回
            return new Group(groupID,name,Group.getLevelByLimit(limit),groupMember);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(rs,stmt);
        }
        return null;
    }

    /**
     * 返回所有群聊的集合
     */
    public static List<Group> getAllGroups() {
        // 构造 SQL 查询语句
        String sql = "SELECT * FROM tb_group";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(sql);
            // 执行 SQL 查询语句并获取结果集
            rs = stmt.executeQuery();

            // 构造 Group 对象集合
            List<Group> groups = new ArrayList<>();
            while (rs.next()) {
                // 从结果集中获取 groupID、limit、groupMember 和 name
                int groupID = rs.getInt("groupID");
                int limit = rs.getInt("limit");
                String groupMemberString = rs.getString("groupMember");
                String name = rs.getString("name");

                // 将 groupMember 字符串转换为 List<Integer>
                List<Integer> groupMember = new ArrayList<>();
                for (String id : groupMemberString.split(",")) {
                    groupMember.add(Integer.parseInt(id));
                }

                // 构造 Group 对象并添加到集合中
                Group group = new Group(groupID, name, Group.getLevelByLimit(limit), groupMember);
                groups.add(group);
            }
            // 返回 Group 对象集合
            return groups;
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(rs,stmt);
        }
        return null;
    }
}
