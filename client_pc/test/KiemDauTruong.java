import com.htbb.pokemon.DauTruongDuLieu;
import com.htbb.pokemon.DauTruongTran;
import com.htbb.pokemon.GoiDauTruong;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM GIAO DIỆN ĐẤU TRƯỜNG BẢO BỐI 3v3 (headless, không máy chủ, đường vẽ bản PC) ở 11 cỡ màn của ChupTrongGame.CO.
 * Gói -109 tạo bằng CHÍNH lớp ghi của máy chủ (server/src/com/htbb/pokemon/GoiDauTruong + DauTruongTran mô phỏng thật)
 * rồi đi qua MessageHandler thật - kiểm luôn bố cục gói hai đầu khớp nhau. Màn:
 * <ul>
 * <li>thách đấu (thẻ mùa, 3 đối thủ, Tìm / Làm mới / Đấu), chưa có đối thủ, chưa có đội tấn công;</li>
 * <li>đội hình (2 hàng 3 ô, lưới Hộp PC có sao cá thể PNG, Lưu đội);</li>
 * <li>trận (hai bên, thanh HP, trạng thái, nhật ký, 4 nút chiêu màu hệ + PP, đổi Pokémon, Tự động, đồng hồ);</li>
 * <li>kết quả thắng / thua.</li>
 * </ul>
 * Mỗi màn: tấm trong vùng an toàn (kiemTam), vùng chạm đủ cỡ / không chồng (kiemCham), chữ trong khung (veKiem), icon PNG.
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -sourcepath server/src -d $O client_pc/test/ChupTrongGame.java \
 *       client_pc/test/KiemDauTruong.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemDauTruong [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemDauTruong {
    static int dat, hong;
    static File ra;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static boolean coVung(String ten) {
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) {
                return true;
            }
        }
        return false;
    }

    /** -Dchup.j2me: vẽ như máy J2ME (font ảnh, không chữ HD) - không ghi chữ đã vẽ, chỉ kiểm bố cục / chạm / khung. */
    static boolean j2me;

    static boolean daVe(String doan) {
        if (j2me) {
            return true;
        }
        java.util.Set s = htbb.pc.ChuHD.ghi;
        if (s == null) {
            return false;
        }
        synchronized (s) {
            for (java.util.Iterator it = s.iterator(); it.hasNext(); ) {
                if (((String) it.next()).indexOf(doan) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    interface Ghi {
        void ghi(DataOutputStream o) throws Exception;
    }

    static byte[] goi(Ghi g) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        g.ghi(o);
        o.flush();
        return b.toByteArray();
    }

    // ------------------------------------------------------------------ dữ liệu giả (lớp ghi thật của máy chủ)

    static final int[] DEX = {25, 6, 9, 3, 65, 94, 143, 130};
    static final String[] TEN = {"Pikachu", "Charizard", "Blastoise", "Venusaur", "Alakazam", "Gengar", "Snorlax", "Gyarados"};

    static DauTruongDuLieu duLieu() {
        Map<String, DauTruongDuLieu.Chieu> ch = new HashMap<String, DauTruongDuLieu.Chieu>();
        ch.put("e.xa", new DauTruongDuLieu.Chieu("e.xa", "Tia điện", 3, "dac_biet", "xa", 1, "te", 300, "hu_shock_wave"));
        ch.put("f.tc", new DauTruongDuLieu.Chieu("f.tc", "Nanh lửa", 1, "vat_ly", "tan_cong", 1, "bong", 300, "hu_fire_fang_fang"));
        ch.put("w.vung", new DauTruongDuLieu.Chieu("w.vung", "Sóng thần cuộn trào", 2, "dac_biet", "vung", 1, "", 0, "hu_hydro_pump_rse"));
        ch.put("g.kc", new DauTruongDuLieu.Chieu("g.kc", "Bột ngủ", 4, "ho_tro", "khong_che", 1, "ngu", 1000, "hu_sleep_z"));
        ch.put("p.buff", new DauTruongDuLieu.Chieu("p.buff", "Tĩnh tâm", 10, "ho_tro", "buff", 0, "", 0, "hu_stat_green_ring"));
        ch.put("n.khien", new DauTruongDuLieu.Chieu("n.khien", "Tự vệ", 0, "ho_tro", "khien", 0, "", 0, "hu_protect"));
        ch.put("gh.toi", new DauTruongDuLieu.Chieu("gh.toi", "Bóng ma hắc ám", 13, "dac_biet", "toi_thuong", 1, "", 0, "hu_shadow_claw"));
        ch.put("b.nd", new DauTruongDuLieu.Chieu("b.nd", "Đâm liên hoàn", 11, "vat_ly", "nhieu_don", 4, "", 0, "hu_scratch"));
        Map<Integer, String[]> cl = new HashMap<Integer, String[]>();
        String[] bo = {"e.xa", "f.tc", "w.vung", "g.kc", "p.buff", "n.khien", "gh.toi", "b.nd"};
        for (int i = 0; i < DEX.length; i++) {
            cl.put(Integer.valueOf(DEX[i]), new String[]{bo[i % bo.length], bo[(i + 1) % bo.length], bo[(i + 6) % bo.length], bo[(i + 3) % bo.length]});
        }
        return new DauTruongDuLieu(null, ch, cl);
    }

    static DauTruongTran.Con con(DauTruongDuLieu d, int i) {
        return new DauTruongTran.Con(DEX[i], TEN[i], game.pokemon.HePoke.he1(DEX[i]), game.pokemon.HePoke.he2(DEX[i]), i == 2, 0x3FFFFFFF - i * 77777,
                i * 3 % 25, new int[]{80, 90, 85, 95, 85, 90}, d.bonChieu(DEX[i]));
    }

    static DauTruongTran tran;
    static int sanKiem = 3;

    static DauTruongTran taoTran() {
        DauTruongDuLieu d = duLieu();
        DauTruongTran t = new DauTruongTran(2026L, new DauTruongTran.Con[]{con(d, 0), con(d, 1), con(d, 2)},
                new DauTruongTran.Con[]{con(d, 5), con(d, 6), con(d, 7)});
        return t;
    }

    static GoiDauTruong.TrangThai trangThai(boolean coDoi) {
        GoiDauTruong.TrangThai t = new GoiDauTruong.TrangThai();
        t.mo = true;
        t.mua = 2;
        t.ngayConLai = 37;
        t.diem = 1184;
        t.bac = 2;
        t.tenBac = "Bạc";
        t.hang = 12;
        t.thang = 14;
        t.thua = 6;
        t.luotCon = 3;
        t.luotNgay = 5;
        t.luongTuan = 12;
        t.tranLuongTuan = 25;
        t.lamMoiCon = 8;
        t.cong = coDoi ? new int[]{1, 2, 3} : new int[0];
        t.thu = coDoi ? new int[]{3, 4, 5} : new int[0];
        t.sucCong = 1980;
        t.sucThu = 2010;
        List<GoiDauTruong.KhoCon> kho = new ArrayList<GoiDauTruong.KhoCon>();
        for (int i = 0; i < DEX.length + 2; i++) {
            GoiDauTruong.KhoCon k = new GoiDauTruong.KhoCon();
            k.id = i + 1;
            k.dex = i < DEX.length ? DEX[i] : (i == DEX.length ? 150 : 19);
            k.ten = i < DEX.length ? TEN[i] : (i == DEX.length ? "Mewtwo" : "Rattata");
            k.cap = 20 + i * 7;
            k.shiny = i == 2;
            k.sao = i % 4;
            k.ivPt = 40 + i * 6;
            k.suc50 = 600 + i * 15;
            k.he1 = game.pokemon.HePoke.he1(k.dex);
            k.he2 = game.pokemon.HePoke.he2(k.dex);
            k.camLy = k.dex == 150 ? 1 : 0;
            kho.add(k);
        }
        t.kho = kho.toArray(new GoiDauTruong.KhoCon[0]);
        t.ghiChu = "";
        t.chuoiThang = 3;
        t.lichSu = new int[][]{{71, 1, 16}, {70, 0, -15}, {69, 1, 8}};
        t.lichSuTen = new String[]{"Mây Xanh", "Bạch Long Vương Đại Đế", "Lá"};
        return t;
    }

    static List<GoiDauTruong.DoiThu> doiThu() {
        List<GoiDauTruong.DoiThu> ds = new ArrayList<GoiDauTruong.DoiThu>();
        String[] ten = {"Mây Xanh", "Bạch Long Vương Đại Đế", "Lá"};
        for (int i = 0; i < 3; i++) {
            GoiDauTruong.DoiThu d = new GoiDauTruong.DoiThu();
            d.id = 100 + i;
            d.ten = ten[i];
            d.diem = 1150 + i * 30;
            d.bac = 2;
            d.suc = 1950 + i * 40;
            d.daGap = i == 2 ? 1 : 0;
            d.dex = new int[]{DEX[(i + 3) % 8], DEX[(i + 4) % 8], DEX[(i + 5) % 8]};
            d.shiny = new boolean[]{false, i == 1, false};
            d.sao = new int[]{i, 3, 1};
            ds.add(d);
        }
        return ds;
    }

    static void nap(final byte[] b) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.goi(-109, b);
            }
        });
    }

    // ------------------------------------------------------------------ mở màn

    static void mo(final String ten) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.Canvas.endDlg();
                    game.pokemon.DungDo.xoaHet();
                    htbb.pc.ChuHD.ghi.clear();
                    game.render.Canvas.gameScr.switchToMe();
                    game.pokemon.DauTruong.coDoiThu = false;
                    game.pokemon.DauTruong.DOI_THU = new java.util.Vector();
                    game.pokemon.DauTruong.ghiDoiThu = "";
                    if (ten.startsWith("dt_")) {
                        final boolean coDoi = !ten.equals("dt_chua_doi");
                        game.pokemon.DauTruong.choDoiThu = true; // không tự gửi tìm (không có máy chủ)
                        ChupTrongGame.goi(-109, goi(new Ghi() {
                            public void ghi(DataOutputStream o) throws Exception {
                                GoiDauTruong.trangThai(o, trangThai(coDoi));
                            }
                        }));
                        game.pokemon.ManDauTruong m = game.pokemon.ManDauTruong.gI();
                        if (ten.equals("dt_thach_dau")) {
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    GoiDauTruong.doiThu(o, doiThu(), 8, "");
                                }
                            }));
                        } else if (ten.equals("dt_thach_dau_trong")) {
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    GoiDauTruong.doiThu(o, new ArrayList<GoiDauTruong.DoiThu>(), 0,
                                            "Chưa có đối thủ hợp tầm (điểm ±100, sức mạnh ±15%). Làm mới để nới tầm.");
                                }
                            }));
                        }
                        game.pokemon.DauTruong.choDoiThu = false;
                        if (ten.equals("dt_tuan")) {
                            game.pokemon.DauTruong.choTuan = true;
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    List<GoiDauTruong.DongTuan> ds = new ArrayList<GoiDauTruong.DongTuan>();
                                    String[] tt = {"Mây Xanh", "Bạch Long Vương Đại Đế", "Lá", "Sóng Bạc", "Đá Cuội"};
                                    for (int i = 0; i < tt.length; i++) {
                                        GoiDauTruong.DongTuan d = new GoiDauTruong.DongTuan();
                                        d.hang = i + 1;
                                        d.ten = tt[i];
                                        d.diemDoi = 64 - i * 11;
                                        d.thang = 5 - i / 2;
                                        d.tran = 5;
                                        ds.add(d);
                                    }
                                    GoiDauTruong.DongTuan toi = new GoiDauTruong.DongTuan();
                                    toi.hang = 12;
                                    toi.diemDoi = 9;
                                    toi.thang = 2;
                                    toi.tran = 4;
                                    GoiDauTruong.bangTuan(o, ds, toi);
                                }
                            }));
                        }
                        m.moTab(ten.equals("dt_doi_hinh") ? 1 : ten.equals("dt_lich_su") ? 2 : ten.equals("dt_tuan") ? 3 : 0);
                    } else if (ten.startsWith("tran") || ten.startsWith("kq")) {
                        tran = taoTran();
                        final DauTruongTran t = tran;
                        ChupTrongGame.goi(-109, goi(new Ghi() {
                            public void ghi(DataOutputStream o) throws Exception {
                                GoiDauTruong.batDau(o, 9001, "Bạch Long Vương Đại Đế", 1210, 20, t, 20, !ten.startsWith("tran_tu_chon"), sanKiem, 20, 40,
                                        ten.equals("tran_xem_lai"));
                            }
                        }));
                        for (int l = 0; l < 3; l++) {
                            t.choi(DauTruongTran.HanhDong.chieu(l % 4));
                            final List<DauTruongTran.SuKien> dong = new ArrayList<DauTruongTran.SuKien>(t.suKienLuot());
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    GoiDauTruong.luot(o, 9001, t, dong, 14, !ten.startsWith("tran_tu_chon"));
                                }
                            }));
                        }
                        if (ten.equals("tran_trang_thai")) {
                            t.dangRa(1).trangThai = DauTruongTran.TT_TE;
                            t.dangRa(0).trangThai = DauTruongTran.TT_BONG;
                            final List<DauTruongTran.SuKien> dong = new ArrayList<DauTruongTran.SuKien>();
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    GoiDauTruong.luot(o, 9001, t, dong, 4, true);
                                }
                            }));
                        }
                        if (!ten.equals("tran_dien") && !ten.equals("tran_tu_chon_dien")) {
                            game.pokemon.ManTranDauTruong.xongHoatCanh();
                        }
                        if (ten.startsWith("tran_tu_chon")) {
                            // máy chủ báo: ô 3 đang hồi chiêu 2 lượt, ô 4 hết PP
                            game.pokemon.DauTruong.Chieu c2 = game.pokemon.DauTruong.chieu(2), c3 = game.pokemon.DauTruong.chieu(3);
                            c2.hoi = 2;
                            c2.lyDo = 2;
                            c3.pp = 0;
                            c3.lyDo = 1;
                        }

                        if (ten.startsWith("kq")) {
                            final boolean thang = ten.equals("kq_thang");
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    GoiDauTruong.ketQua(o, 9001, thang, 1184, thang ? 1201 : 1168, thang ? -8 : 8, thang ? 3 : 0, 1000, 2,
                                            thang ? "Thắng Bạch Long Vương Đại Đế · điểm đầy đủ · +3 lượng khoá" : "Thua Bạch Long Vương Đại Đế · điểm đầy đủ");
                                }
                            }));
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    kiem(false, ten + " mo man: " + e);
                }
            }
        });
    }

    /** Kịch bản clip: tìm hạt có trượt + trạng thái + ngã gục + ra sân thay + mình đổi Pokémon trong 4 lượt. */
    static final DauTruongTran.HanhDong[] KICH_BAN = {DauTruongTran.HanhDong.chieu(0), DauTruongTran.HanhDong.chieu(1),
        DauTruongTran.HanhDong.doi(1), DauTruongTran.HanhDong.chieu(2), DauTruongTran.HanhDong.chieu(0)};

    static DauTruongTran tranClip(long hat) {
        DauTruongTran t = taoTran();
        t.doi[1][0].hp = t.doi[1][0].hpMax / 4;
        return new DauTruongTran(hat, t.doi[0], t.doi[1]);
    }

    static void quayClip(File thu) throws Exception {
        thu.mkdirs();
        long chon = timHat();
        System.out.println("clip: hat " + chon);
        final DauTruongTran t = tranClip(chon);
        final long gocGio = 5000000L;
        game.pokemon.ManTranDauTruong.gioGia = gocGio;
        sanKiem = 3;
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.Canvas.endDlg();
                    game.render.Canvas.gameScr.switchToMe();
                    ChupTrongGame.goi(-109, goi(new Ghi() {
                        public void ghi(DataOutputStream o) throws Exception {
                            GoiDauTruong.batDau(o, 9002, "Bạch Long Vương Đại Đế", 1210, 20, t, 20, true, sanKiem, 20, 40, false);
                        }
                    }));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        int khung = 0;
        for (int l = 0; l <= KICH_BAN.length; l++) {
            if (l > 0) {
                t.choi(KICH_BAN[l - 1]);
                final List<DauTruongTran.SuKien> ds = new ArrayList<DauTruongTran.SuKien>(t.suKienLuot());
                ChupTrongGame.chay(new Runnable() {
                    public void run() {
                        try {
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    GoiDauTruong.luot(o, 9002, t, ds, 18, true);
                                }
                            }));
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            // diễn hết lượt + 1 giây đi lại tự do
            int them = 25;
            for (int f = 0; f < 2000; f++) {
                game.pokemon.ManTranDauTruong.gioGia += 40;
                BufferedImage anh = ChupTrongGame.ve(1280, 720);
                ImageIO.write(anh, "png", new File(thu, String.format("k%05d.png", khung++)));
                if (!game.pokemon.ManTranDauTruong.dangDien() && --them <= 0) {
                    break;
                }
            }
            if (t.xong()) {
                break;
            }
        }
        game.pokemon.ManTranDauTruong.gioGia = 0;
        System.out.println("clip: " + khung + " khung -> " + thu);
        kiem(khung > 100, "clip: du khung (" + khung + ")");
    }

    static void datCo(final int W, final int H, final int dpi) throws Exception {
        NenPc.tinhKichThuoc(W, H);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
            }
        });
        float s = NenPc.hienW / (float) NenPc.rong;
        if (dpi > 0) {
            float dpL = (dpi / 160f) / s;
            game.pokemon.VungCham.MIN = (int) Math.ceil(44 * dpL);
            game.pokemon.VungCham.CACH = (int) Math.floor(8 * dpL);
            game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);
        } else {
            game.pokemon.VungCham.MIN = (int) Math.ceil(32 / s);
            game.pokemon.VungCham.CACH = (int) Math.ceil(8 / s);
            game.pokemon.Bang.anTrai = 0;
        }
    }

    /** Bộ ảnh cho chủ game (-Danh=thư mục): máy đánh, tự chọn (tắt / đang diễn), trúng, trượt, siêu hiệu quả, chí mạng,
     *  ngã gục, ra sân + dịch chuyển, kết quả - ở 1920x1080 (PC) và 2400x1080 (điện thoại). */
    static void chupBo(File thu) throws Exception {
        thu.mkdirs();
        StringBuilder ds = new StringBuilder();
        int[][] co = {{1920, 1080, 0}, {2400, 1080, 420}};
        String[] tenCo = {"1920x1080", "2400x1080"};
        for (int c = 0; c < co.length; c++) {
            datCo(co[c][0], co[c][1], co[c][2]);
            final int W = co[c][0], H = co[c][1];
            String[][] tinh = {{"tran", "a_may_danh"}, {"tran_tu_chon", "b_tu_chon"}, {"tran_tu_chon_dien", "b_tu_chon_dang_dien"},
                {"kq_thang", "d_ket_qua_thang"}, {"kq_thua", "d_ket_qua_thua"}};
            for (int i = 0; i < tinh.length; i++) {
                if (tinh[i][0].equals("tran_tu_chon_dien")) {
                    game.pokemon.ManTranDauTruong.gioGia = 2000000L;
                }
                mo(tinh[i][0]);
                if (tinh[i][0].equals("tran_tu_chon_dien")) {
                    game.pokemon.ManTranDauTruong.gioGia += 1300;
                }
                Thread.sleep(120);
                String ten = tinh[i][1] + "_" + tenCo[c] + ".png";
                ImageIO.write(ChupTrongGame.ve(W, H), "png", new File(thu, ten));
                ds.append(ten).append('\n');
                game.pokemon.ManTranDauTruong.gioGia = 0;
            }
            // khoảnh khắc: diễn tran clip từng 40 ms, chụp lần đầu gặp mỗi loại sự kiện ở giữa hoạt cảnh
            String[] can = {"c_trung", "c_truot", "c_sieu_hieu_qua", "c_chi_mang", "c_nga_guc", "c_ra_san_dich_chuyen"};
            boolean[] da = new boolean[can.length];
            long chon = timHat();
            final DauTruongTran t = tranClip(chon);
            game.pokemon.ManTranDauTruong.gioGia = 7000000L;
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    try {
                        game.render.Canvas.endDlg();
                        game.render.Canvas.gameScr.switchToMe();
                        ChupTrongGame.goi(-109, goi(new Ghi() {
                            public void ghi(DataOutputStream o) throws Exception {
                                GoiDauTruong.batDau(o, 9003, "Bạch Long Vương Đại Đế", 1210, 20, t, 20, true, 3, 20, 40, false);
                            }
                        }));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            for (int l = 0; l < KICH_BAN.length && !t.xong(); l++) {
                t.choi(KICH_BAN[l]);
                final List<DauTruongTran.SuKien> sk = new ArrayList<DauTruongTran.SuKien>(t.suKienLuot());
                ChupTrongGame.chay(new Runnable() {
                    public void run() {
                        try {
                            ChupTrongGame.goi(-109, goi(new Ghi() {
                                public void ghi(DataOutputStream o) throws Exception {
                                    GoiDauTruong.luot(o, 9003, t, sk, 18, true);
                                }
                            }));
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
                for (int f = 0; f < 3000 && game.pokemon.ManTranDauTruong.dangDien(); f++) {
                    game.pokemon.ManTranDauTruong.gioGia += 40;
                    ChupTrongGame.ve(W, H);
                    game.pokemon.DauTruong.SuKien e = game.pokemon.ManTranDauTruong.suKienDang();
                    float p = game.pokemon.ManTranDauTruong.tienDoDang();
                    if (e == null) {
                        continue;
                    }
                    int k = -1;
                    if (e.kieu == DauTruongTran.SK_TRUNG && (e.co & DauTruongTran.CO_SIEU) != 0 && !da[2] && p > 0.25f) {
                        k = 2;
                    } else if (e.kieu == DauTruongTran.SK_TRUNG && (e.co & DauTruongTran.CO_CHI_MANG) != 0 && p > 0.25f) {
                        k = 3;
                    } else if (e.kieu == DauTruongTran.SK_TRUNG && e.so > 0 && p > 0.25f) {
                        k = 0;
                    } else if (e.kieu == DauTruongTran.SK_TRUOT && p > 0.4f) {
                        k = 1;
                    } else if (e.kieu == DauTruongTran.SK_GUC && p > 0.5f) {
                        k = 4;
                    } else if (e.kieu == DauTruongTran.SK_RA_SAN && (e.co & 2) == 0 && p > 0.47f) {
                        k = 5;
                    }
                    if (k >= 0 && !da[k]) {
                        da[k] = true;
                        String ten = can[k] + "_" + tenCo[c] + ".png";
                        ImageIO.write(ChupTrongGame.ve(W, H), "png", new File(thu, ten));
                        ds.append(ten).append('\n');
                    }
                }
            }
            game.pokemon.ManTranDauTruong.gioGia = 0;
            for (int k = 0; k < can.length; k++) {
                kiem(da[k], "anh chu game: co khoanh khac " + can[k] + " " + tenCo[c]);
            }
        }
        java.nio.file.Files.write(new File(thu, "danh_sach.txt").toPath(), ds.toString().getBytes("UTF-8"));
        System.out.println("anh chu game -> " + thu);
    }

    /** Hạt cho tran clip: có trượt + trạng thái + ngã gục + trúng siêu hiệu quả + chí mạng trong kịch bản. */
    static long timHat() {
        for (long h = 1; h < 20000; h++) {
            DauTruongTran t = tranClip(h);
            boolean truot = false, guc = false, sieu = false, chiMang = false, raSan = false;
            for (int i = 0; i < KICH_BAN.length && !t.xong(); i++) {
                t.choi(KICH_BAN[i]);
                for (DauTruongTran.SuKien e : t.suKienLuot()) {
                    truot |= e.kieu == DauTruongTran.SK_TRUOT;
                    guc |= e.kieu == DauTruongTran.SK_GUC;
                    raSan |= e.kieu == DauTruongTran.SK_RA_SAN;
                    sieu |= e.kieu == DauTruongTran.SK_TRUNG && (e.co & DauTruongTran.CO_SIEU) != 0;
                    chiMang |= e.kieu == DauTruongTran.SK_TRUNG && (e.co & DauTruongTran.CO_CHI_MANG) != 0;
                }
            }
            if (truot && guc && sieu && chiMang && raSan && !t.xong()) {
                return h;
            }
        }
        return 10;
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "anh_dau_truong");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(2500);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
            }
        });
        Thread.sleep(800);
        htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
        j2me = System.getProperty("chup.j2me") != null;
        if (j2me) {
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
        }

        String[] man = {"dt_thach_dau", "dt_thach_dau_trong", "dt_chua_doi", "dt_doi_hinh", "dt_lich_su", "dt_tuan", "tran", "tran_trang_thai",
            "tran_xem_lai", "tran_tu_chon", "tran_tu_chon_dien", "kq_thang", "kq_thua"};
        String loc = System.getProperty("chup.co");
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            String tenCo = ChupTrongGame.TEN_CO[c];
            if (loc != null && !("," + loc + ",").contains("," + tenCo + ",")) {
                continue;
            }
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                }
            });
            float s = NenPc.hienW / (float) NenPc.rong;
            int min, cach;
            if (dpi > 0) {
                float dpL = (dpi / 160f) / s;
                min = (int) Math.ceil(44 * dpL);
                cach = (int) Math.floor(8 * dpL);
                game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);
            } else {
                min = (int) Math.ceil(32 / s);
                cach = (int) Math.ceil(8 / s);
                game.pokemon.Bang.anTrai = 0;
            }
            game.pokemon.VungCham.MIN = min;
            game.pokemon.VungCham.CACH = cach;
            for (int m = 0; m < man.length; m++) {
                String t = man[m] + " " + tenCo;
                mo(man[m]);
                Thread.sleep(150);
                ChupTrongGame.ve(W, H);
                int dem0 = game.pokemon.Bang.demTam;
                int ic0 = game.pokemon.IconBat.demVe;
                BufferedImage anh = ChupTrongGame.veKiem(W, H, "KiemDauTruong:" + man[m]);
                ImageIO.write(anh, "png", new File(ra, man[m] + "_" + tenCo + ".png"));
                ChupTrongGame.kiemTam(t, dem0 - 1);
                ChupTrongGame.kiemCham(t, min, cach);
                if (man[m].equals("dt_doi_hinh") || man[m].startsWith("tran")) {
                    kiem(j2me || game.pokemon.IconBat.demVe > ic0, t + " ve icon PNG (IconBat)");
                }
                if (man[m].startsWith("dt_")) {
                    kiem(game.render.Canvas.currentScreen == game.pokemon.ManDauTruong.me, t + " goi trang thai mo man Dau truong");
                    kiem(daVe("Đấu trường Bảo Bối"), t + " tieu de");
                }
                if (man[m].equals("dt_thach_dau")) {
                    kiem(daVe("Mùa 2") && daVe("Hạng 12") && daVe("Điểm 1184"), t + " the mua: mua, hang, diem");
                    kiem(daVe("Chuỗi thắng 3"), t + " chuoi thang (chi hinh)");
                    kiem(daVe("Lượt hôm nay 3/5") && daVe("Lượng khoá tuần 12/25"), t + " luot ngay + luong khoa tuan");
                    kiem(coVung("dt-tim") && coVung("dt-lam-moi") && coVung("dt-tu-chon"), t + " nut Tim / Lam moi + cai dat Tu chon chieu (mac dinh Tat)");
                    kiem(coVung("dt-dau-0") && coVung("dt-dau-1"), t + " nut Dau tung doi thu");
                    kiem(daVe("Mây Xanh") && daVe("Sức mạnh 1950"), t + " ten + suc manh doi thu");
                } else if (man[m].equals("dt_thach_dau_trong")) {
                    kiem(daVe("Chưa có đối thủ") && daVe("Làm mới để nới tầm"), t + " khong co doi thu: ly do may chu");
                } else if (man[m].equals("dt_chua_doi")) {
                    kiem(daVe("Chưa có đội tấn công") && coVung("dt-xep-doi"), t + " chua co doi: nut Xep doi hinh");
                } else if (man[m].equals("dt_doi_hinh")) {
                    kiem(coVung("o-0-0") && coVung("o-1-2"), t + " 6 o doi tan cong / phong thu");
                    kiem(coVung("luu-cong") && coVung("luu-thu"), t + " nut Luu doi");
                    kiem(coVung("kho-0") && daVe("Pikachu"), t + " luoi Hop PC cham duoc");
                    kiem(daVe("Sức mạnh"), t + " suc manh doi");
                    kiem(daVe("Huyền thoại"), t + " Huyen thoai bi danh dau");
                } else if (man[m].equals("dt_lich_su")) {
                    kiem(coVung("dt-xem-0") && coVung("dt-xem-2") && daVe("Gặp Mây Xanh") && daVe("Thắng") && daVe("Điểm +16"), t + " lich su 5 tran + Xem lai");
                } else if (man[m].equals("dt_tuan")) {
                    kiem(daVe("Bạn: hạng 12") && daVe("Mây Xanh") && daVe("+64"), t + " bang tuan trong game + dong cua minh");
                } else if (man[m].startsWith("tran")) {
                    if (man[m].equals("tran_xem_lai")) {
                        kiem(daVe("Xem lại · Lượt"), t + " xem lai: bang luot ghi Xem lai");
                        kiem(!game.pokemon.DauTruong.dangDanh(), t + " xem lai: thoat khong tinh thua");
                    }
                    kiem(game.render.Canvas.currentScreen == game.pokemon.ManTranDauTruong.me, t + " goi bat dau mo man tran");
                    if (man[m].equals("tran_tu_chon")) {
                        kiem(coVung("chieu-0") && coVung("chieu-1") && !coVung("chieu-2") && !coVung("chieu-3"), t + " tu chon: 2 chieu dung duoc, hoi chieu + het PP bi tat");
                        kiem(daVe("0/10") && daVe("Tia điện"), t + " tu chon: ten + PP tren nut (het PP hien 0/10)");
                        kiem(coVung("doi-0") && coVung("doi-1") && coVung("tu-dong"), t + " tu chon: doi Pokemon + May danh + dong ho");
                        kiem(!coVung("toc-do"), t + " tu chon: khong nut toc do");
                    } else if (man[m].equals("tran_tu_chon_dien")) {
                        kiem(!coVung("chieu-0") && !coVung("doi-0"), t + " tu chon: dang dien -> moi nut tat");
                    } else {
                        kiem(!coVung("chieu-0") && coVung("toc-do"), t + " may tu danh: chi mot nut toc do");
                        kiem(daVe("×1"), t + " nut toc do x1");
                    }
                    kiem(daVe("Lv50") && daVe("/" + tran.doi[0][game.pokemon.DauTruong.dangRa[0]].hpMax), t + " bang HP kieu Pokemon (ten, Lv, so HP ben minh)");
                    kiem(daVe("Lượt 3/50"), t + " bang luot (so luot)");
                    kiem(daVe("Gym điện Vermilion"), t + " ten san tren bang luot");
                    if (man[m].equals("tran_trang_thai")) {
                        kiem(daVe("Tê liệt") && daVe("Bỏng"), t + " nhan trang thai");
                    }
                } else if (man[m].startsWith("kq")) {
                    boolean th = man[m].equals("kq_thang");
                    kiem(daVe(th ? "THẮNG" : "THUA"), t + " the ket qua");
                    kiem(daVe(th ? "Điểm 1184 thành 1201 (+17)" : "Điểm 1184 thành 1168 (-16)"), t + " diem truoc -> sau");
                    kiem(daVe(th ? "+3 lượng khoá" : "Không có lượng khoá"), t + " luong khoa");
                    kiem(coVung("kq-ve") && coVung("kq-dong"), t + " nut Ve Dau truong / Dong");
                }
            }
        }
        // 8 sân (theo bậc + sân đêm) ở pc720 + một khung GIỮA hoạt cảnh (lượt đang diễn) mỗi sân
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
            }
        });
        for (int sn = 0; sn < 8; sn++) {
            sanKiem = sn;
            mo("tran");
            Thread.sleep(100);
            ChupTrongGame.ve(1280, 720);
            ImageIO.write(ChupTrongGame.veKiem(1280, 720, "KiemDauTruong:san" + sn), "png", new File(ra, "san_" + sn + ".png"));
        }
        sanKiem = 3;
        game.pokemon.ManTranDauTruong.gioGia = 1000000L;
        mo("tran_dien");
        for (int f = 0; f < 6; f++) {
            game.pokemon.ManTranDauTruong.gioGia += 450;
            ChupTrongGame.ve(1280, 720);
            ImageIO.write(ChupTrongGame.veKiem(1280, 720, "KiemDauTruong:dien" + f), "png", new File(ra, "dien_" + f + ".png"));
        }
        kiem(game.pokemon.ManTranDauTruong.dangDien(), "hoat canh: con su kien dang dien sau 2,7 giay (khong nhay coc)");
        game.pokemon.ManTranDauTruong.gioGia = 0;
        String anhDir = System.getProperty("anh");
        if (anhDir != null) {
            chupBo(new File(anhDir));
        }
        String clip = System.getProperty("clip");
        if (clip != null) {
            quayClip(new File(clip));
        }
        // bố cục gói hai đầu: dữ liệu client đọc đúng như máy chủ ghi
        NenPc.tinhKichThuoc(1280, 720);
        mo("tran");
        Thread.sleep(100);
        kiem(game.pokemon.DauTruong.tranId == 9001 && game.pokemon.DauTruong.luot == 3, "goi luot: id tran + so luot");
        kiem(game.pokemon.DauTruong.DOI[1][0].dex == tran.doi[1][0].dex && game.pokemon.DauTruong.DOI[1][0].hpMax == tran.doi[1][0].hpMax,
                "goi bat dau: doi thu dung dex + HP toi da");
        boolean hpDung = true;
        for (int ben = 0; ben < 2; ben++) {
            for (int i = 0; i < 3; i++) {
                hpDung &= game.pokemon.DauTruong.DOI[ben][i].hp == tran.doi[ben][i].hp;
            }
        }
        kiem(hpDung, "goi luot: HP 6 con khop mo phong may chu");
        kiem(game.pokemon.DauTruong.dangRa[0] == tran.dangRa[0] && game.pokemon.DauTruong.dangRa[1] == tran.dangRa[1], "goi luot: con dang ra");
        kiem(game.pokemon.DauTruong.CHIEU[0][0].pp == tran.doi[0][0].pp[0] && game.pokemon.DauTruong.CHIEU[0][0].ten.equals(tran.doi[0][0].chieu[0].ten),
                "goi: PP + ten chieu");
        kiem(game.pokemon.DauTruong.NHAT_KY.size() > 3, "nhat ky luot");
        // tốc độ ×2 / ×3 khoá theo cấp (máy chủ gửi cấp cần trong gói bắt đầu)
        int capCu = game.pokemon.DauTruong.capToc2;
        game.pokemon.DauTruong.toc = 1;
        game.pokemon.DauTruong.capToc2 = 127;
        kiem(game.pokemon.DauTruong.tocKe() == -127, "toc x2 khoa: tra cap can");
        game.pokemon.DauTruong.capToc2 = 0;
        game.pokemon.DauTruong.capToc3 = 0;
        kiem(game.pokemon.DauTruong.tocKe() == 2, "toc x2 mo khi du cap");
        game.pokemon.DauTruong.toc = 3;
        kiem(game.pokemon.DauTruong.tocKe() == 1, "toc x3 -> x1 (vong)");
        game.pokemon.DauTruong.toc = 1;
        game.pokemon.DauTruong.capToc2 = capCu;
        game.pokemon.DauTruong.capToc3 = 40;
        dat += ChupTrongGame.dat;
        hong += ChupTrongGame.hong;
        System.out.println("KiemDauTruong: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
