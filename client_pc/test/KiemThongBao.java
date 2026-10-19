import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM THÔNG BÁO (0.9.6.1, game.pokemon.ThongBao + DungDo): phân loại, thời gian hiện, hàng đợi, gói -104 mới, câu cá,
 * và VẼ từng kiểu (kết quả / lỗi / tin / hiếm / biểu ngữ boss / biểu ngữ toàn máy chủ / hai dòng / icon Pokémon) ở mọi cỡ
 * chuẩn của ChupTrongGame: thẻ nằm trong vùng an toàn, không chồng nhau, không chồng vùng chạm HUD, vùng chạm >= cỡ tối
 * thiểu, chữ chính đã vẽ ra.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemThongBao.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemThongBao <thư mục ảnh>
 * Mã thoát 1 = có phép kiểm hỏng.
 */
public class KiemThongBao {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static final int KQ = game.pokemon.ThongBao.KET_QUA, LOI = game.pokemon.ThongBao.LOI, TIN = game.pokemon.ThongBao.TIN,
            HIEM = game.pokemon.ThongBao.HIEM, SK = game.pokemon.ThongBao.SU_KIEN, HOP = game.pokemon.ThongBao.HOP;
    static final String[] TEN_LOAI = {"KET_QUA", "LOI", "TIN", "HIEM", "SU_KIEN", "HOP"};

    /** {câu, loại mong đợi qua hộp 37 / startOKDlg} - câu thật lấy từ kiểm kê (sql + java máy chủ). */
    static final Object[][] MAU_37 = {
        {"Câu được Vảy Magikarp ×1\nThể lực còn 176", KQ},
        {"Đào được Đá lửa ×2\nThể lực còn 150", KQ},
        {"Không đủ xu", LOI},
        {"Không đủ tiền.", LOI},
        {"Hết thể lực sinh hoạt hôm nay.", LOI},
        {"Bạn phải đạt cấp độ 50 trở lên mới có thể giao thẻ.", LOI},
        {"Chỉ có thể câu cá ở hồ.", LOI},
        {"Hành trang đã đầy.", LOI},
        {"Vé không trúng thưởng. Số trúng là số 7", LOI},
        {"Chúc mừng bạn đã nhận được hộp quà trung thu", HIEM},
        {"Kỹ năng Câu cá lên cấp 5!", HIEM},
        {"Chúc mừng! Charmander99 đã lên cấp 25 và nhận 1 điểm kỹ năng.", HIEM},
        {"Charmander đang tiến hoá... Chúc mừng! Charmander đã tiến hoá thành Charmeleon.", HIEM},
        {"Bạn nhận được 500 xu", KQ},
        {"Đã mua", KQ},
        {"Đã khóa thành công", KQ},
        {"Hãy đến gần Kangaskhan thủ kho để mở kho đồ.", TIN},
        {"Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!", SK},
        {"Máy chủ sẽ bảo trì lúc 04:00 (còn 10 phút). Vui lòng thoát game để không mất dữ liệu.", HOP},
        {"Tài khoản của bạn đã bị khoá.", HOP},
        {"Tài khoản chưa kích hoạt: vẫn chơi bình thường.", TIN},
        {"Thang PvP mùa 2\nElo: 1200 - bậc 3\nThắng/thua: 10/4\nChỉ tính trận cùng dải 10 cấp.", HOP},
    };

    /** {câu chữ chạy -26, loại mong đợi}. */
    static final Object[][] MAU_26 = {
        {"Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!", SK},
        {"Boss Gyarados đã rời Hồ Phẫn Nộ. Hẹn lần sau!", SK},
        {"Trainer9 đã hạ gục Boss Người tuyết ở Núi Tuyết!", SK},
        {"Bùng phát! Đàn Magikarp tràn ra Hồ Lớn trong 1 giờ tới - cơ hội nghiên cứu và chụp album!", SK},
        {"Giờ vàng đã bắt đầu.", SK},
        {"Chúc mừng Ash mở rương may mắn trúng Mặt nạ", SK},
        {"Thời gian sử dụng hạt mầm của bạn đã hết", TIN},
        {"Bạn vừa thấy một Pikachu shiny! Đã ghi vào Pokédex.", HIEM},
        {"Bạn không thể vào Gym trong thơi gian này do chưa tham gia đội.", LOI},
        {"Pokémon hỗ trợ của bạn đã hết hạn", TIN},
    };

    static void kiemPhanLoai() {
        for (Object[] m : MAU_37) {
            int k = game.pokemon.ThongBao.phanLoai((String) m[0]);
            kiem(k == (Integer) m[1], "phân loại 37 '" + m[0] + "' = " + TEN_LOAI[k] + " (mong " + TEN_LOAI[(Integer) m[1]] + ")");
        }
        for (Object[] m : MAU_26) {
            int k = game.pokemon.ThongBao.phanLoaiChay((String) m[0]);
            kiem(k == (Integer) m[1], "phân loại -26 '" + m[0] + "' = " + TEN_LOAI[k] + " (mong " + TEN_LOAI[(Integer) m[1]] + ")");
        }
    }

    static void kiemThoiGian() {
        int[] toiThieu = {2500, 4000, 3000, 5000, 6000};
        for (int k = 0; k <= SK; k++) {
            int truoc = 0;
            for (int n = 0; n <= 400; n += 7) {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < n; i++) b.append('a');
                int t = game.pokemon.ThongBao.thoiGian(k, b.toString());
                kiem(t >= game.pokemon.ThongBao.TOI_THIEU && t >= toiThieu[k], TEN_LOAI[k] + " " + n + " chữ: " + t + " ms >= " + toiThieu[k]);
                kiem(t >= truoc, TEN_LOAI[k] + " thời gian không giảm theo độ dài (" + n + " chữ)");
                kiem(t <= 9000, TEN_LOAI[k] + " " + n + " chữ: " + t + " ms <= 9 s");
                int doc = game.pokemon.ThongBao.docToiThieu(k, b.toString());
                kiem(doc >= game.pokemon.ThongBao.TOI_THIEU && doc <= t, TEN_LOAI[k] + " đọc tối thiểu " + doc + " trong [2500, " + t + "]");
                truoc = t;
            }
        }
        // 60 ms mỗi chữ cho kết quả: 40 chữ -> 2,5 + 2,4 = 4,9 s
        kiem(game.pokemon.ThongBao.thoiGian(KQ, "1234567890123456789012345678901234567890") == 4900, "kết quả 40 chữ = 4,9 s");
        kiem(game.pokemon.ThongBao.thoiGian(SK, "Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!") >= 6000, "biểu ngữ boss >= 6 s");
        kiem(game.pokemon.ThongBao.thoiGian(LOI, "Không đủ xu") >= 4000, "lỗi ngắn >= 4 s");
    }

    /** Mô phỏng thời gian: bước 100 ms, ghi lúc thấy đầu / cuối mỗi thẻ. */
    static void kiemHangDoi() throws Exception {
        ChupTrongGame.chay(() -> {
            game.pokemon.DungDo.xoaHet();
            for (int i = 0; i < 8; i++) {
                game.pokemon.DungDo.them("Nhận được Thuốc hồi HP ×" + (i + 1), KQ, 0, 0, false);
            }
            game.pokemon.DungDo.them("Boss A đã xuất hiện ở X!", SK, 0, 0, false);
            game.pokemon.DungDo.them("Boss B đã xuất hiện ở Y!", SK, 0, 0, false);
        });
        final int[] dau = new int[8], cuoi = new int[8];
        java.util.Arrays.fill(dau, -1);
        final int[] bieuThay = {0};
        final String[] bieuTruoc = {null};
        final int[] maxHien = {0};
        for (int t = 0; t <= 60000; t += 100) {
            final int tt = t;
            ChupTrongGame.chay(() -> {
                if (tt > 0) game.pokemon.DungDo.troiGio(100);
                maxHien[0] = Math.max(maxHien[0], game.pokemon.DungDo.soHien());
                for (int i = 0; i < 3; i++) {
                    String s = game.pokemon.DungDo.chuHien(i);
                    if (s == null) continue;
                    int k = Integer.parseInt(s.substring(s.lastIndexOf('×') + 1)) - 1;
                    if (dau[k] < 0) dau[k] = tt;
                    cuoi[k] = tt;
                }
                String b = game.pokemon.DungDo.chuHien(-1);
                if (b != null && !b.equals(bieuTruoc[0])) bieuThay[0]++;
                bieuTruoc[0] = b;
            });
        }
        kiem(maxHien[0] == 3, "hàng đợi: tối đa 3 thẻ cùng lúc (thấy " + maxHien[0] + ")");
        int doc = game.pokemon.ThongBao.docToiThieu(KQ, "Nhận được Thuốc hồi HP ×1");
        for (int k = 0; k < 8; k++) {
            kiem(dau[k] >= 0, "hàng đợi: thẻ " + (k + 1) + " có hiện (không mất)");
            kiem(cuoi[k] - dau[k] + 100 >= doc, "hàng đợi: thẻ " + (k + 1) + " hiện " + (cuoi[k] - dau[k] + 100) + " ms >= đọc tối thiểu " + doc);
            if (k > 0) kiem(dau[k] >= dau[k - 1], "hàng đợi: thẻ " + (k + 1) + " vào sau thẻ " + k + " (đúng thứ tự)");
        }
        kiem(bieuThay[0] == 2, "biểu ngữ: hai boss lần lượt (đổi " + bieuThay[0] + " lần)");
        ChupTrongGame.chay(() -> {
            game.pokemon.DungDo.xoaHet();
            game.pokemon.DungDo.them("Không đủ xu", LOI, 0, 0, false);
            game.pokemon.DungDo.them("Không đủ xu", LOI, 0, 0, false);
            game.pokemon.DungDo.them("Không đủ xu", LOI, 0, 0, false);
        });
        ChupTrongGame.chay(() -> {
            kiem(game.pokemon.DungDo.soHien() == 1 && game.pokemon.DungDo.soCho() == 0, "trùng lặp: 3 lần cùng câu = 1 thẻ (gộp ×3)");
            game.pokemon.DungDo.xoaHet();
        });
    }

    static network.Message goi(byte[] du) throws Exception {
        java.lang.reflect.Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        return c.newInstance((byte) -104, du);
    }

    static byte[] goi104(int loai, String s, int hinh, int id) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream o = new java.io.DataOutputStream(b);
        o.writeByte(loai);
        o.writeUTF(s);
        o.writeByte(0);
        if (hinh >= 0) {
            o.writeByte(hinh);
            if (hinh == 1 || hinh == 2) o.writeShort(id);
            else if (hinh == 3) o.writeByte(id);
        }
        return b.toByteArray();
    }

    /** Gói -104 máy chủ mới (có hình) và cũ (không có phần thêm) đều đọc đúng; câu cá giữ đến lúc kéo cần. */
    static void kiemGoi() throws Exception {
        final network.Message[] m = {goi(goi104(HIEM, "Câu được đồ hiếm: Vảy Gyarados ×1\nThể lực còn 120", 2, 130)),
            goi(goi104(TIN, "Không đủ xu", -1, 0)), goi(goi104(SK, "Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!", 3, 6))};
        ChupTrongGame.chay(() -> {
            game.pokemon.DungDo.xoaHet();
            game.render.Canvas.gameScr.switchToMe();
            kiem(game.pokemon.DungDo.nhan(m[1]), "gói -104 cũ (không phần thêm) đọc được");
            kiem(game.pokemon.DungDo.loaiHien(0) == LOI, "gói -104 TIN 'Không đủ xu' -> thẻ LỖI (" + game.pokemon.DungDo.loaiHien(0) + ")");
            kiem(game.pokemon.DungDo.nhan(m[2]), "gói -104 biểu ngữ đọc được");
            kiem("Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!".equals(game.pokemon.DungDo.chuHien(-1)), "gói -104 SU_KIEN -> biểu ngữ trên");
            game.pokemon.DungDo.xoaHet();
            // câu cá: kết quả về TRƯỚC lúc cá cắn -> giữ; lần gặp Pokémon (tin) chờ; kéo cần xong mới hiện, kết quả trước
            game.pokemon.SinhThai.batDauCau();
            kiem(game.pokemon.DungDo.nhan(m[0]), "gói -104 câu cá (có hình Pokémon) đọc được");
            game.pokemon.DungDo.them("Gặp Pokémon hiếm: Gyarados!\nĐã ghi vào Pokédex", HIEM, 2, 130, false);
            kiem(game.pokemon.DungDo.soHien() == 0, "câu cá: chưa kéo cần -> chưa hiện thẻ nào (" + game.pokemon.DungDo.soHien() + ")");
        });
        // chờ hoạt cảnh (gameTick tăng theo khung): tối đa 20 giây
        for (int i = 0; i < 200; i++) {
            final boolean[] xong = {false};
            ChupTrongGame.chay(() -> xong[0] = !game.pokemon.SinhThai.dangCau());
            if (xong[0]) break;
            ChupTrongGame.ve(1280, 720);
            Thread.sleep(100);
        }
        ChupTrongGame.chay(() -> {
            kiem(!game.pokemon.SinhThai.dangCau(), "câu cá: hoạt cảnh kết thúc");
            game.pokemon.DungDo.troiGio(400);
            kiem(game.pokemon.DungDo.soHien() == 2, "câu cá: sau khi kéo cần hiện 2 thẻ (kết quả + gặp) - thấy " + game.pokemon.DungDo.soHien());
            kiem(game.render.Canvas.currentDialog == null, "câu cá: KHÔNG có hộp thoại chặn");
            String s0 = game.pokemon.DungDo.chuHien(0), s1 = game.pokemon.DungDo.chuHien(1);
            kiem(s1 != null && s1.startsWith("Câu được"), "câu cá: kết quả vào trước (dưới) - " + s1);
            kiem(s0 != null && s0.startsWith("Gặp"), "câu cá: lần gặp là thẻ riêng - " + s0);
            game.pokemon.DungDo.xoaHet();
        });
        // máy chủ cũ: câu dính liền -> tách
        String[] r = game.pokemon.ThongBao.tachCauCu("Câu được: 1 Vảy Magikarp (bậc 1). Gặp Magikarp! Thể lực còn 176.");
        kiem("Câu được Vảy Magikarp ×1\nThể lực còn 176".equals(r[0]), "câu cũ tách kết quả: " + r[0]);
        kiem("Bạn vừa gặp Magikarp!".equals(r[1]), "câu cũ tách lần gặp: " + r[1]);
        // hộp OK của client trong game -> thẻ nhanh; bảo trì vẫn là hộp
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.startOKDlg("Chỉ có thể câu cá ở hồ.");
            kiem(game.render.Canvas.currentDialog == null && game.pokemon.DungDo.loaiHien(0) == LOI, "startOKDlg lỗi trong game -> thẻ lỗi, không hộp");
            game.render.Canvas.startOKDlg("Máy chủ sẽ bảo trì sau 5 phút. Vui lòng thoát game.");
            kiem(game.render.Canvas.currentDialog != null, "startOKDlg bảo trì -> vẫn là hộp thoại");
            game.render.Canvas.endDlg();
            game.pokemon.DungDo.xoaHet();
        });
    }

    /** Một bộ mẫu để vẽ: mọi loại thẻ + biểu ngữ. */
    static final Object[][] BO_VE = {
        {"ket_qua", new Object[][]{{"Câu được Vảy Magikarp ×1\nThể lực còn 176", KQ, 2, 129}}},
        {"loi", new Object[][]{{"Không đủ xu để mua mồi câu.", LOI, 0, 0}}},
        {"tin", new Object[][]{{"Bạn vừa gặp Magikarp!\nĐã ghi vào Pokédex", TIN, 2, 129}}},
        {"hiem", new Object[][]{{"Câu được đồ hiếm: Vảy Gyarados ×1\nThể lực còn 120", HIEM, 2, 130}}},
        {"len_cap", new Object[][]{{"Kỹ năng Câu cá lên cấp 5!", HIEM, 3, 7}}},
        {"bieu_ngu_boss", new Object[][]{{"Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!", SK, 0, 0}}},
        {"bieu_ngu_the_gioi", new Object[][]{{"Bùng phát! Đàn Magikarp tràn ra Hồ Lớn trong 1 giờ tới - cơ hội nghiên cứu và chụp album!", SK, 0, 0}}},
        {"chong", new Object[][]{{"Trainer9 đã hạ gục Boss Người tuyết ở Núi Tuyết!", SK, 0, 0},
            {"Nhận được 2 Ether lớn.", KQ, 0, 0}, {"Không dùng được: HP đã đầy.", LOI, 0, 0},
            {"Kỹ năng Câu cá lên cấp 5!", HIEM, 3, 7}, {"Đào được Đá lửa ×2\nThể lực còn 150", KQ, 3, 28}}},
    };

    static boolean giao(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }

    static void kiemVe(File ra) throws Exception {
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            final String co = ChupTrongGame.TEN_CO[c];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            float s = NenPc.hienW / (float) NenPc.rong;
            int min = dpi > 0 ? (int) Math.ceil(44 * ((dpi / 160f) / s)) : (int) Math.ceil(32 / s);
            game.pokemon.VungCham.MIN = min;
            game.pokemon.Bang.anTrai = dpi > 0 ? (int) Math.ceil(100 / s) : 0;
            for (Object[] bo : BO_VE) {
                final String ten = (String) bo[0];
                final Object[][] ds = (Object[][]) bo[1];
                ChupTrongGame.chay(() -> {
                    ChupTrongGame.moMan("the_gioi");
                    game.render.Canvas.endDlg();
                    game.pokemon.DungDo.xoaHet();
                    htbb.pc.ChuHD.ghi.clear();
                    for (Object[] d : ds) {
                        game.pokemon.DungDo.them((String) d[0], (Integer) d[1], (Integer) d[2], (Integer) d[3], false);
                        game.pokemon.DungDo.troiGio(400);   // lần lượt vào (giãn cách 350 ms)
                    }
                });
                ChupTrongGame.ve(W, H);
                Thread.sleep(250);   // qua hoạt cảnh trượt vào
                BufferedImage anh = ChupTrongGame.ve(W, H);
                ImageIO.write(anh, "png", new File(ra, "thong_bao_" + ten + "_" + co + ".png"));
                java.util.List<int[]> the = new java.util.ArrayList<>(), hud = new java.util.ArrayList<>();
                int n = game.pokemon.VungCham.dem();
                for (int i = 0; i < n; i++) {
                    String t = game.pokemon.VungCham.ten(i);
                    if (t == null) continue;
                    if (t.startsWith("thong-bao")) the.add(game.pokemon.VungCham.vungVe(i));
                    else if (t.startsWith("hud-")) hud.add(game.pokemon.VungCham.vungVe(i));
                }
                String nhan = ten + " " + co;
                int soThe = 0, soBieu = 0;
                for (Object[] d : ds) {
                    if ((Integer) d[1] == SK) soBieu = 1;
                    else soThe++;
                }
                int mong = Math.min(3, soThe) + soBieu;
                kiem(the.size() == mong, nhan + ": " + mong + " thẻ / biểu ngữ có vùng chạm (thấy " + the.size() + ")");
                int L = game.pokemon.Bang.trai(), T = game.pokemon.Bang.tren(), R = game.pokemon.Bang.phai(), B = game.pokemon.Bang.duoi();
                for (int i = 0; i < the.size(); i++) {
                    int[] v = the.get(i);
                    kiem(v[0] >= L && v[1] >= T && v[0] + v[2] <= R && v[1] + v[3] <= B, nhan + " thẻ " + i + " trong vùng an toàn " + java.util.Arrays.toString(v));
                    kiem(v[3] >= min, nhan + " thẻ " + i + " cao " + v[3] + " >= cỡ chạm " + min);
                    kiem(Math.abs((v[0] + v[2] / 2) - NenPc.rong / 2) <= 2, nhan + " thẻ " + i + " canh giữa");
                    for (int j = i + 1; j < the.size(); j++) {
                        kiem(!giao(v, the.get(j)), nhan + " thẻ " + i + " không chồng thẻ " + j);
                    }
                    for (int[] h : hud) {
                        kiem(!giao(v, h), nhan + " thẻ " + i + " không chồng nút HUD " + java.util.Arrays.toString(h));
                    }
                }
                // chữ chính đã vẽ (ít nhất phần đầu dòng 1)
                int daThe = 0;
                for (Object[] d : ds) {
                    if ((Integer) d[1] != SK && ++daThe > 3) continue;   // thẻ thứ 4 trở đi đang chờ (hàng đợi)
                    String chinh = ((String) d[0]).split("\n")[0];
                    String dau = chinh.substring(0, Math.min(12, chinh.length()));
                    boolean thay = false;
                    synchronized (htbb.pc.ChuHD.ghi) {
                        for (String x : htbb.pc.ChuHD.ghi) if (x.startsWith(dau)) thay = true;
                    }
                    kiem(thay, nhan + " đã vẽ chữ '" + dau + "…'");
                }
            }
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_thong_bao");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                game.render.screen.TField.isQwerty = true;
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        Thread.sleep(800);
        htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
        game.pokemon.VungCham.MIN = 16;
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.Canvas.endDlg();
        });
        kiemPhanLoai();
        kiemThoiGian();
        kiemHangDoi();
        kiemGoi();
        kiemVe(ra);
        System.out.println("KiemThongBao: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
