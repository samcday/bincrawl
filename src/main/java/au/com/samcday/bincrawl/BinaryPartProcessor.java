package au.com.samcday.bincrawl;

import java.util.Date;

public interface BinaryPartProcessor {
    public void processPart(String group, String name, Date date, int size, String messageId, int part, int totalParts);
}
