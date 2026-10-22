import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Người chơi phụ cho video giới thiệu: bot giao thức thật (tools/pokemon/kiem_tai/BotChucNang.Bot) nhận lệnh từng dòng
 * qua stdin (KichBanVideo chạy tiến trình này và ghi lệnh). Chỉ dùng trên máy chủ THỬ cục bộ (sv.server=0 cho mto).
 *   java -Dban=908 -cp <kiem_tai + tools/video> BotVideo <host> <cong>
 * Lệnh:
 *   vao <tk> <lop 0..4>          đăng nhập (mật khẩu 123456), tạo nhân vật nếu chưa có, rời sân mở đầu
 *   mto <tk> <map> <x> <y>       tới bản đồ (lệnh chat mto)
 *   ra <tk>                      rời làng qua cổng bắc ra đồng (đi bộ)
 *   di <tk> <px> <py>            đi tới toạ độ điểm ảnh
 *   chat <tk> <chữ...>           chat bản đồ (gói 27)
 *   kenh <tk> <kênh> <đích|-> <chữ...>   chat kênh (gói -124: 1 Thế giới, 4 riêng)
 *   binh <tk> <id>               dùng bình (gói 22) - cờ PK 14 / 15
 *   ban <tk> <ô chiêu> <ox> <oy> <hx1000> <hy1000>   bắn ngắm tay (gói -113)
 *   ketban <tk> <id actor>       mời kết bạn (gói 101, 0) / nhanban <tk> <id> (101, 1)
 *   xephang <tk>                 NPC Đấu trường -> Đấu xếp hạng (máy chủ ghép, đưa vào Đấu trường 202)
 *   cup <tk>                     Sổ tay -> Cúp PvP tuần -> Đăng ký
 *   vitri <tk>                   in bản đồ + toạ độ
 *   ngu <giây>
 */
public class BotVideo {
    /** Bot nối từ một địa chỉ loopback riêng (127.x.0.1, khác dải /24): máy chủ coi mỗi người chơi phụ là một "mạng" khác (luật chống
     *  đánh hộ / ghép trận cùng IP của đấu xếp hạng / Cúp không chặn). */
    static final class BotNguon extends BotChucNang.Bot {
        final String nguon;

        BotNguon(String user, String nguon) {
            super(user);
            this.nguon = nguon;
        }

        @Override
        void ketNoi() throws java.io.IOException {
            s = new java.net.Socket();
            s.bind(new java.net.InetSocketAddress(nguon, 0));
            s.connect(new java.net.InetSocketAddress(BotChucNang.HOST, BotChucNang.PORT), 10000);
            s.setSoTimeout(0);
            in = new java.io.DataInputStream(new java.io.BufferedInputStream(s.getInputStream()));
            out = s.getOutputStream();
            gui(-40, new byte[0]);
            Object[] k = doc();
            xuLy((Integer) k[0], (byte[]) k[1]);
        }
    }

    static final Map<String, BotChucNang.Bot> BOT = new LinkedHashMap<String, BotChucNang.Bot>();

    static void bomHet() {
        for (BotChucNang.Bot b : BOT.values()) {
            try {
                b.bom(20);
            } catch (Throwable e) {
                // bỏ
            }
        }
    }

    static String noi(String[] p, int tu) {
        StringBuilder s = new StringBuilder();
        for (int i = tu; i < p.length; i++) s.append(i > tu ? " " : "").append(p[i]);
        return s.toString();
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.HOST = a[0];
        BotChucNang.PORT = Integer.parseInt(a[1]);
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        final java.util.concurrent.LinkedBlockingQueue<String> hang = new java.util.concurrent.LinkedBlockingQueue<String>();
        Thread doc = new Thread(() -> {
            try {
                String l;
                while ((l = in.readLine()) != null) hang.put(l);
            } catch (Exception e) {
                // hết
            }
            hang.add("thoat");
        });
        doc.setDaemon(true);
        doc.start();
        long nhipDi = 0;
        while (true) {
            String l = hang.poll(50, java.util.concurrent.TimeUnit.MILLISECONDS);
            bomHet();
            if (System.currentTimeMillis() - nhipDi > 20000) {
                // giữ kết nối (máy chủ cắt kết nối im lặng)
                nhipDi = System.currentTimeMillis();
                for (BotChucNang.Bot b : BOT.values()) {
                    try {
                        b.gui(4, (short) b.px, (short) b.py);
                    } catch (Throwable e) {
                        // bỏ
                    }
                }
            }
            if (l == null) continue;
            l = l.trim();
            if (l.isEmpty()) continue;
            String[] p = l.split(" ");
            try {
                BotChucNang.Bot b = p.length > 1 ? BOT.get(p[1]) : null;
                switch (p[0]) {
                    case "thoat":
                        for (BotChucNang.Bot x : BOT.values()) x.close();
                        System.exit(0);
                        break;
                    case "vao": {
                        b = new BotNguon(p[1], "127." + (11 + BOT.size()) + ".0.1");
                        String loi = b.dangNhap("123456");
                        if (loi == null) loi = b.vaoGame(Integer.parseInt(p[2]));
                        if (loi == null && b.mapId == 105) {
                            int m = b.moc();
                            b.gui(12, (short) 0, (short) 14, (short) 12);
                            b.cho(x -> x.coLenh(m, 12), 6000);
                            b.gui(12, (short) -500, (short) 0, (short) 0);
                            b.bom(2000);
                            b.px = b.tx * 16 + 8;
                            b.py = b.ty * 16 + 8;
                        }
                        BOT.put(p[1], b);
                        System.out.println("[bot] " + p[1] + " vao: " + (loi == null ? "ok map " + b.mapId + " actor " + b.actor : loi));
                        break;
                    }
                    case "mto":
                        System.out.println("[bot] " + p[1] + " mto " + b.den(Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4])));
                        break;
                    case "ra":
                        // rời làng qua cổng bắc ra đồng Pallet (đi bộ như người chơi)
                        System.out.println("[bot] " + p[1] + " ra " + KichBanHuongDan.quaCong(b, 0) + " map " + b.mapId + " o " + b.px / 16 + "," + b.py / 16);
                        break;
                    case "xephang":
                        // NPC Đấu trường (-31) -> "Đấu xếp hạng" (vào hàng chờ, máy chủ ghép trận)
                        System.out.println("[bot] " + p[1] + " xephang " + KichBanXepHang.chonDauTruong(b, "Đấu xếp hạng"));
                        break;
                    case "cup": {
                        // Sổ tay -> Cúp PvP tuần -> Đăng ký
                        int m0 = b.moc();
                        b.menu(-527, 0, 10);
                        b.bom(900);
                        b.menu(-527, 70, 0);
                        b.bom(1200);
                        System.out.println("[bot] " + p[1] + " cup map " + b.mapId + " | " + KichBanPhoBanCup.toast(b, m0) + " | " + KichBanPhoBanCup.menuChu(b, m0));
                        break;
                    }
                    case "vitri":
                        System.out.println("[bot] " + p[1] + " vitri " + b.mapId + " " + b.px + " " + b.py);
                        break;
                    case "di":
                        b.diToi(Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                        break;
                    case "chat":
                        b.chat(noi(p, 2));
                        break;
                    case "kenh":
                        b.gui(-124, (byte) 1, (byte) Integer.parseInt(p[2]), "-".equals(p[3]) ? "" : p[3], noi(p, 4));
                        break;
                    case "binh":
                        b.gui(22, (byte) Integer.parseInt(p[2]));
                        break;
                    case "ban":
                        b.gui(-113, (byte) 0, (byte) Integer.parseInt(p[2]), (short) Integer.parseInt(p[3]), (short) Integer.parseInt(p[4]),
                                (short) Integer.parseInt(p[5]), (short) Integer.parseInt(p[6]));
                        break;
                    case "ketban":
                        b.gui(101, (short) Integer.parseInt(p[2]), (byte) 0);
                        break;
                    case "nhanban":
                        b.gui(101, (short) Integer.parseInt(p[2]), (byte) 1);
                        break;
                    case "ngu":
                        Thread.sleep((long) (Double.parseDouble(p[1]) * 1000));
                        break;
                    default:
                        System.out.println("[bot] lenh la: " + l);
                }
            } catch (Throwable e) {
                System.out.println("[bot] loi '" + l + "': " + e);
            }
        }
    }
}
