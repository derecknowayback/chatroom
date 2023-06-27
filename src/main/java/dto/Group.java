package dto;

import java.util.List;

public class Group {

    private static final int LEVEL1_LIMIT = 10;
    private static final int LEVEL2_LIMIT = 50;
    private static final int LEVEL3_LIMIT = 100;

    int groupID;
    private int limit;
    private int level;
    List<Integer> groupMember;
    String name ;

    public Group(String name,int level,List<Integer> users){
        this.name = name;
        groupMember = users;
        this.level = level;
        switch (level) {
            case 1 : this.limit = LEVEL1_LIMIT; break;
            case 2 : this.limit = LEVEL2_LIMIT; break;
            case 3 : this.limit = LEVEL3_LIMIT; break;
        }
    }

    public Group(int id,String name,int level,List<Integer> users){
        this.groupID = id;
        this.name = name;
        groupMember = users;
        switch (level) {
            case 1 : this.limit = LEVEL1_LIMIT; break;
            case 2 : this.limit = LEVEL2_LIMIT; break;
            case 3 : this.limit = LEVEL3_LIMIT; break;
        }
    }

    public static int getLimitByLevel(int level) {
        switch (level) {
            case 1 : return LEVEL1_LIMIT;
            case 2 : return LEVEL2_LIMIT;
            case 3 : return LEVEL3_LIMIT;
        }
        return -1;
    }



    public boolean addMember (int id) {
        if (groupMember.size() >= limit) {
            return false;
        }
        groupMember.add(id);
        return true;
    }

    public void removeUser (int userId) {
        groupMember.remove(userId);
    }

    public int getGroupID() {
        return groupID;
    }

    @Override
    public String toString() {
        return null;
    }


    public String askCreateStr(){
        StringBuilder builder = new StringBuilder(name + "|" + level + "|");
        for (int i = 0; i < groupMember.size(); i++) {
            builder.append(groupMember.get(i));
            if (i != groupMember.size() - 1)
                builder.append(",");
        }
        return builder.toString();
    }

    public boolean isMember(int userId) {
        return groupMember.contains(userId);
    }



}
