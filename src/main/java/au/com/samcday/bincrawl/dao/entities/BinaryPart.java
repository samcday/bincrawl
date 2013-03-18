package au.com.samcday.bincrawl.dao.entities;

public class BinaryPart {
    private int partNum;
    private String messageId;
    private long size;

    public BinaryPart(int partNum, String messageId, long size) {
        this.partNum = partNum;
        this.messageId = messageId;
        this.size = size;
    }

    public int getPartNum() {
        return partNum;
    }

    public void setPartNum(int partNum) {
        this.partNum = partNum;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
