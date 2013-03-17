package au.com.samcday.bincrawl.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BinaryPart {
    @JsonIgnore
    private int num;
    private String messageId;
    private int size;

    public BinaryPart(int num, String messageId, int size) {
        this.num = num;
        this.messageId = messageId;
        this.size = size;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
