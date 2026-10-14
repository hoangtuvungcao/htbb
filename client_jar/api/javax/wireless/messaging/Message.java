package javax.wireless.messaging;

/** Khai báo API WMA (JSR-120) chỉ để biên dịch - máy J2ME có sẵn bản thật, không đóng vào jar. */
public interface Message {
    String getAddress();

    void setAddress(String addr);

    java.util.Date getTimestamp();
}
