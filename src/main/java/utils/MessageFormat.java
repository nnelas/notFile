package utils;

import java.io.Serializable;

public class MessageFormat implements Serializable {

    private String fname;
    private String msgId;
    private int fromPeerId;
    private int TTL_value;

    public MessageFormat(String fname, String msgId, int fromPeerId, int TTL_value) {
        this.fname = fname;
        this.msgId = msgId;
        this.fromPeerId = fromPeerId;
        this.TTL_value = TTL_value;
    }

    public String getFname() {
        return fname;
    }

    public String getMsgId() {
        return msgId;
    }

    public int getFromPeerId() {
        return fromPeerId;
    }

    public int getTTL_value() {
        return TTL_value;
    }
}