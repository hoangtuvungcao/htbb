package htbb.capnhat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * TẢI TỆP BẢN MỚI (0.9.7) cho bộ cập nhật trong game - Java SE thuần, dùng chung PC (htbb.pc.CapNhatPc) và Android
 * (com.htbb.pokemon.CapNhatApk); J2ME không dùng (client_jar/shared/se KHÔNG vào bản jar).
 * <ul>
 *   <li>chỉ http / https, theo chuyển hướng (tối đa 5, cả http -&gt; https), hẹn giờ kết nối / đọc;</li>
 *   <li>ghi vào {@code <đích>.part}, tính SHA-256 trong lúc tải, báo tiến độ;</li>
 *   <li>SHA-256 BẮT BUỘC và phải khớp, thiếu byte / quá cỡ / huỷ -&gt; xoá tệp dở, ném {@link IOException} (chữ cho người
 *       chơi); khớp -&gt; đổi tên .part thành đích (thay tệp cũ).</li>
 * </ul>
 */
public final class TaiVe {
    private TaiVe() {
    }

    /** Báo tiến độ (luồng tải gọi). */
    public interface TienDo {
        void tienDo(long da, long tong);

        /** Đã tải đủ, đang so SHA-256. */
        void dangKiem();
    }

    /** Huỷ giữa chừng. */
    public interface Huy {
        boolean daHuy();
    }

    /** Tệp lớn nhất nhận (APK / bộ cài / AppImage ~ 100 MB). */
    public static long TOI_DA = 1536L * 1024 * 1024;
    public static int HEN_KET_NOI_MS = 15000, HEN_DOC_MS = 30000;

    public static boolean urlHopLe(String url) {
        if (url == null) {
            return false;
        }
        String u = url.trim().toLowerCase();
        return (u.startsWith("https://") || u.startsWith("http://")) && u.length() > 10 && u.indexOf(' ') < 0;
    }

    public static boolean shaHopLe(String sha) {
        return sha != null && sha.trim().toLowerCase().matches("[0-9a-f]{64}");
    }

    static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte x : b) {
            s.append(Character.forDigit((x >> 4) & 15, 16)).append(Character.forDigit(x & 15, 16));
        }
        return s.toString();
    }

    /** SHA-256 hex của tệp. */
    public static String sha256(File f) throws IOException {
        try (InputStream in = new java.io.FileInputStream(f)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            return hex(md.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("Máy không hỗ trợ SHA-256");
        }
    }

    /** Mở kết nối, theo chuyển hướng. */
    static HttpURLConnection mo(String url) throws IOException {
        String u = url;
        for (int i = 0; i < 6; i++) {
            if (!urlHopLe(u)) {
                throw new IOException("Link tải không hợp lệ");
            }
            HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
            c.setInstanceFollowRedirects(false);
            c.setConnectTimeout(HEN_KET_NOI_MS);
            c.setReadTimeout(HEN_DOC_MS);
            c.setRequestProperty("User-Agent", "HTBB-CapNhat/1");
            c.setRequestProperty("Accept-Encoding", "identity");
            int ma = c.getResponseCode();
            if (ma == 301 || ma == 302 || ma == 303 || ma == 307 || ma == 308) {
                String toi = c.getHeaderField("Location");
                c.disconnect();
                if (toi == null) {
                    throw new IOException("Máy chủ tải chuyển hướng hỏng");
                }
                u = new URL(new URL(u), toi).toString();
                continue;
            }
            if (ma != 200) {
                c.disconnect();
                throw new IOException(ma == 404 ? "Không thấy tệp trên máy chủ (404)" : "Máy chủ tải trả mã " + ma);
            }
            return c;
        }
        throw new IOException("Chuyển hướng quá nhiều lần");
    }

    /**
     * Tải url -&gt; dich (qua dich.part), kiểm SHA-256. Trả dich. Lỗi: IOException với chữ cho người chơi (tệp dở đã xoá,
     * dich cũ - nếu có - giữ nguyên).
     */
    public static File tai(String url, String sha256, File dich, TienDo td, Huy huy) throws IOException {
        if (!shaHopLe(sha256)) {
            throw new IOException("Thiếu mã SHA-256 để kiểm tệp");
        }
        String muon = sha256.trim().toLowerCase();
        File thu = dich.getParentFile();
        if (thu != null && !thu.isDirectory() && !thu.mkdirs()) {
            throw new IOException("Không tạo được thư mục " + thu);
        }
        File part = new File(dich.getPath() + ".part");
        part.delete();
        HttpURLConnection c = null;
        boolean xong = false;
        try {
            c = mo(url);
            long tong = c.getContentLengthLong();
            if (tong > TOI_DA) {
                throw new IOException("Tệp quá lớn");
            }
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IOException("Máy không hỗ trợ SHA-256");
            }
            long da = 0;
            long lucBao = 0;
            try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(part)) {
                byte[] buf = new byte[65536];
                if (td != null) {
                    td.tienDo(0, tong);
                }
                int n;
                while ((n = in.read(buf)) > 0) {
                    if (huy != null && huy.daHuy()) {
                        throw new IOException("Đã huỷ");
                    }
                    out.write(buf, 0, n);
                    md.update(buf, 0, n);
                    da += n;
                    if (da > TOI_DA || (tong > 0 && da > tong)) {
                        throw new IOException("Tệp lớn hơn khai báo");
                    }
                    long bay = System.currentTimeMillis();
                    if (td != null && bay - lucBao >= 100) {
                        lucBao = bay;
                        td.tienDo(da, tong);
                    }
                }
            }
            if (huy != null && huy.daHuy()) {
                throw new IOException("Đã huỷ");
            }
            if (td != null) {
                td.tienDo(da, tong);
                td.dangKiem();
            }
            if (tong > 0 && da != tong) {
                throw new IOException("Tải thiếu (" + da + "/" + tong + " byte) - mạng bị ngắt");
            }
            String co = hex(md.digest());
            if (!co.equals(muon)) {
                throw new IOException("Tệp tải về không khớp SHA-256 (hỏng hoặc bị sửa) - đã xoá");
            }
            doiTen(part, dich);
            xong = true;
            return dich;
        } catch (java.net.UnknownHostException e) {
            throw new IOException("Không kết nối được máy chủ tải");
        } catch (java.net.SocketTimeoutException e) {
            throw new IOException("Mạng chậm, quá thời gian chờ");
        } catch (java.net.ConnectException e) {
            throw new IOException("Không kết nối được máy chủ tải");
        } finally {
            if (c != null) {
                c.disconnect();
            }
            if (!xong) {
                part.delete();
            }
        }
    }

    /** Đổi tên nguyên tử (cùng thư mục), thay tệp cũ. */
    public static void doiTen(File tu, File toi) throws IOException {
        try {
            java.nio.file.Files.move(tu.toPath(), toi.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException | UnsupportedOperationException e) {
            java.nio.file.Files.move(tu.toPath(), toi.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
