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
        limit = getLimitByLevel(level);
    }

    public Group(int id,String name,int level,List<Integer> users){
        this.groupID = id;
        this.name = name;
        groupMember = users;
        limit = getLimitByLevel(level);
        this.level = level;
    }

    public static int getLimitByLevel(int level) {
        switch (level) {
            case 1 : return LEVEL1_LIMIT;
            case 2 : return LEVEL2_LIMIT;
            case 3 : return LEVEL3_LIMIT;
        }
        return -1;
    }

    public static int getLevelByLimit(int limit) {
        switch (limit) {
            case LEVEL1_LIMIT : return 1;
            case LEVEL2_LIMIT : return 2;
            case LEVEL3_LIMIT : return 3;
        }
        return -1;
    }


    public boolean addMember (int id) {
        System.out.println("size " + groupMember.size() + "   limit" + limit);
        if (groupMember.size() >= limit) {
            return false;
        }
        if (!groupMember.contains(id))
            groupMember.add(id);
        return true;
    }

    public void removeUser (int userId) {
        groupMember.remove((Integer) userId);
    }

    public int getGroupID() {
        return groupID;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return groupID + "|" + askCreateStr();
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
