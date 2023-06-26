package dto;

import java.util.List;

public class Group {

    private static final int LEVEL1_LIMIT = 10;
    private static final int LEVEL2_LIMIT = 50;
    private static final int LEVEL3_LIMIT = 100;

    int groupID;
    private int limit;
    List<User> groupMember;
    String name ;

    public Group(String name,int level,List<User> users){
        this.name = name;
        groupMember = users;
        switch (level) {
            case 1 : this.limit = LEVEL1_LIMIT; break;
            case 2 : this.limit = LEVEL2_LIMIT; break;
            case 3 : this.limit = LEVEL3_LIMIT; break;
        }
    }



    public boolean addMember (User user) {
        if (groupMember.size() >= limit) {
            return false;
        }
        groupMember.add(user);
        return true;
    }

    public void removeUser (User user) {
        groupMember.remove(user);
    }

    public int getGroupID() {
        return groupID;
    }

    @Override
    public String toString() {
        return "Group{" +
            "groupID=" + groupID +
            ", limit=" + limit +
            ", groupMember=" + groupMember +
            ", name='" + name + '\'' +
            '}';
    }
}
