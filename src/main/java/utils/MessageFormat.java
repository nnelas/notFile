package utils;

import java.io.Serializable;

public class MessageFormat implements Serializable {

    private String query;
    private String msgId;
    private int fromPeerId;
    private int TTL_value;

    public MessageFormat(String query, String msgId, int fromPeerId, int TTL_value) {
        this.query = query;
        this.msgId = msgId;
        this.fromPeerId = fromPeerId;
        this.TTL_value = TTL_value;
    }

    public String getQuery() {
        return query;
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