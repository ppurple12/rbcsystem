package com.rbcsystem.demo.models;

public class Group {
    private int groupID;
    private Agent[] members;

     // Constructor 
     public Group(int groupID, Agent[] members) {
        this.groupID = groupID;
        this.members = members;
    }

    // setters and getter
    public int getGroupID() {
        return groupID;
    }

    public Agent[] getMembers() {
        return members;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public void setMembers(Agent[] members) {
        this.members = members;
    }
}
