package network;

/**
 * Giải nén gói -127 (server io.GoiNen). Cài đặt theo nền: PC / Android = network.GiaiNenZip (java.util.zip.Inflater);
 * J2ME không có java.util.zip nên không có cài đặt - Session_ME.khaNang() = 0, server không bao giờ gửi gói nén.
 */
public interface GiaiNen {
    byte[] giai(byte[] nen) throws Exception;
}
