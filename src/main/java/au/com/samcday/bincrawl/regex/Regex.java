package au.com.samcday.bincrawl.regex;

import java.util.regex.Pattern;

public class Regex {
    private int id;
    private int ordinal;
    private Pattern pattern;
    private String group;
    private boolean groupPrefix = false;
    private boolean groupMatchAll = false;

    public Regex(Pattern pattern, String group, int ordinal, int id) {
        this.id = id;
        this.ordinal = ordinal;
        this.pattern = pattern;

        if(group.endsWith("*")) {
            group = group.substring(0, group.length() - 1);
            this.groupPrefix = true;
        }
        if(group.isEmpty()) {
            this.groupMatchAll = true;
        }
        this.group = group.toLowerCase();
    }

    public boolean matchesGroup(String groupName) {
        if(this.groupPrefix) {
            if(this.groupMatchAll) return true;
            return groupName.toLowerCase().startsWith(this.group);
        }
        return this.group.equalsIgnoreCase(groupName);
    }

    public int getId() {
        return id;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getGroup() {
        return group;
    }
}
