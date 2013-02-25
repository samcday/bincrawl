package au.com.samcday.bincrawl.dto;

import java.util.List;

public class Release {
    String name;
    List<ReleaseBinary> binaries;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReleaseBinary> getBinaries() {
        return binaries;
    }

    public void setBinaries(List<ReleaseBinary> binaries) {
        this.binaries = binaries;
    }
}
