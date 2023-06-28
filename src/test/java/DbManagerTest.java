
import dto.DBManager;
import dto.Group;
import dto.User;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class DbManagerTest {




    @Test
    public void testCheckUser() {
        String s = DBManager.checkUser("user1", "password1");
        String[] split = s.split(",");
        Assert.assertEquals(split[0],"true");
        Assert.assertEquals(split[1],"1");

        s = DBManager.checkUser("user1","password");
        split = s.split(",");
        Assert.assertEquals(split[0],"false");
    }

    @Test
    public void testGetByName() {
        List<User> res = DBManager.getByName("user1");
        User user = new User(1, "user1", null);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(user, res.get(0));

        res = DBManager.getByName("user3");
        Assert.assertEquals(0, res.size());
    }


    @Test
    public void testInsertUser () {
        String resMsg = DBManager.insertUser("user1", "1234");
        String[] split = resMsg.split(",");
        Assert.assertEquals("false",split[0]);
        Assert.assertEquals("The username is occupied .",split[1]);

        String resMsg1 = DBManager.insertUser("user1", null);
        String[] split1 = resMsg1.split(",");
        Assert.assertEquals("false",split1[0]);
        Assert.assertEquals("name and password are required .",split1[1]);


        String resMsg2 = DBManager.insertUser("user3", "1234");
        String[] split2 = resMsg2.split(",");
        Assert.assertEquals("true",split2[0]);
        Assert.assertEquals("3",split2[1]);
    }

    @Test
    public void testFindAllUsers () {
        List<User> users = DBManager.findAllUsers();
        Assert.assertEquals(3,users.size());
        for (int i = 0; i < users.size(); ++i) {
            User user = users.get(i);
            System.out.println(user);
        }
    }

    @Test
    public void testCreateGroup () {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ids.add(i);
        }
        int groupId = DBManager.createGroup(10,"group3",ids);
        Assert.assertEquals(groupId,3);
    }

    @Test
    public void testAddUserToGroup () {
        boolean b = DBManager.addUserToGroup(1, 7);
        Assert.assertTrue(b);

        b = DBManager.addUserToGroup(1,7);
        Assert.assertTrue(b);

        b = DBManager.addUserToGroup(3,10);
        Assert.assertFalse(b);
    }

    @Test
    public void testRemoveUserFromGroup () {
        boolean b = DBManager.addUserToGroup(1, 7);
        Assert.assertTrue(b);

        b = DBManager.removeUserFromGroup(1, 7);
        Assert.assertTrue(b);

        int[] members = DBManager.getGroupMembers(1);
        for (int i = 0; i < members.length; i++) {
            Assert.assertNotEquals(members[i],7);
        }
    }

    @Test
    public void testGetGroupMembers () {
        int[] members = DBManager.getGroupMembers(2);
        Assert.assertEquals(members.length,4);
    }

    @Test
    public void testGetAllGroups() {
        List<Group> groups = DBManager.getAllGroups();
        Assert.assertEquals(groups.size(),3);
        for (int i = 0; i < groups.size(); i++) {
            System.out.println(groups.get(i));
        }
    }


}


