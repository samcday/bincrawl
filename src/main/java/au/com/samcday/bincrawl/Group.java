package au.com.samcday.bincrawl;

import au.com.samcday.jnntp.GroupInfo;

public class Group extends GroupInfo {
    public static Group of(String name, GroupInfo groupInfo) {
        Group group = new Group();
        group.high = groupInfo.high;
        group.low = groupInfo.low;
        group.count = groupInfo.count;
        group.name = name;
        return group;
    }

    public String name;
}
