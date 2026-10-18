package javax.wireless.messaging;

/** Khai báo API WMA (JSR-120) chỉ để biên dịch. */
public interface TextMessage extends Message {
    String getPayloadText();

    void setPayloadText(String data);
}
