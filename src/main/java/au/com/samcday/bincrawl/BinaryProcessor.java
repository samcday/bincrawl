package au.com.samcday.bincrawl;

import java.util.Date;

public interface BinaryProcessor {
    public void process(String group, String name, Date date, int size, String messageId, int part, int totalParts);
}
