import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM HỌC CHIÊU THEO LOÀI + ĐIỂM HUẤN LUYỆN (0.9.8, game.pokemon.HocChieu / BangHuanLuyen, máy chủ real.HocChieuGame):
 * dựng gói -122 giả (bảng chiêu dòng Charmander + Điểm huấn luyện) qua đúng bộ đọc của client, rồi
 * <ul>
 *   <li>trạng thái từng ô theo loài (Charmeleon cấp 24): đã học / tự học cấp N / NPC khi tiến hoá, lý do NPC chưa dạy;</li>
 *   <li>nút tab Chỉ số thành "Huấn luyện", chấm đỏ khi còn điểm chưa phân bổ và tắt khi hết (ChamMoi DIEM_TN);</li>
 *   <li>tab Kỹ năng vẽ đủ hàng chiêu; bảng Huấn luyện mở từ nút, ở 6 cỡ màn (điện thoại / PC / J2ME): đủ nút
 *       hl-cong-i / hl-tru-i / hl-dong / hl-tay / hl-xac-nhan, thẻ canh giữa trong vùng an toàn, rộng &lt;= 80 %;</li>
 *   <li>+ / − tôn trọng trần dòng và điểm còn lại;</li>
 *   <li>ThanhNhanh: ô nhanh giữ chiêu đã bị gỡ -> chiêu học gần nhất chưa có trên thanh, hết chiêu -> trống.</li>
 * </ul>
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemHuanLuyen.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemHuanLuyen <thư mục>
 * Mã thoát 1 = hỏng.
 */
public class KiemHuanLuyen {
    static int dat, hong;
    static File ra;
    static int soAnh;

    /** 0.9.8 (giao diện NRO): đếm icon vẽ bằng code từ lần kiểm đầu (sau khi game đã lên) - phải 0. */
    static boolean daXoaVeTay;

    static void kiem(boolean ok, String ten) {
        if (!daXoaVeTay) {
            daXoaVeTay = true;
            game.pokemon.VeTay.xoa();
        }
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    // ------------------------------------------------------------------ gói giả

    interface Ghi {
        void ghi(DataOutputStream d) throws Exception;
    }

    static void goi(Ghi g) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(b);
        g.ghi(d);
        d.flush();
        game.pokemon.HocChieu.docGoi(new DataInputStream(new ByteArrayInputStream(b.toByteArray())));
    }

    /** {loài, tên loài, ô, cấp học, nguồn} - dòng Charmander như data_learnset. */
    static final Object[][] LS = {
        {4, "Charmander", 0, 1, 0}, {4, "Charmander", 1, 4, 0}, {4, "Charmander", 2, 7, 0}, {4, "Charmander", 5, 6, 0},
        {5, "Charmeleon", 0, 1, 0}, {5, "Charmeleon", 1, 4, 0}, {5, "Charmeleon", 2, 7, 0}, {5, "Charmeleon", 3, 16, 0},
        {5, "Charmeleon", 4, 16, 0}, {5, "Charmeleon", 5, 6, 0},
        {6, "Charizard", 0, 1, 0}, {6, "Charizard", 1, 4, 0}, {6, "Charizard", 2, 7, 0}, {6, "Charizard", 3, 16, 0},
        {6, "Charizard", 4, 16, 0}, {6, "Charizard", 5, 6, 0}, {6, "Charizard", 6, 36, 1}, {6, "Charizard", 7, 36, 1},
        {6, "Charizard", 8, 36, 1},
    };

    static void goiLearnset(final int dex) throws Exception {
        goi(d -> {
            d.writeByte(0);
            d.writeShort(dex);
            d.writeByte(0);
            d.writeByte(9);
            d.writeByte(LS.length);
            for (Object[] r : LS) {
                int o = (Integer) r[2];
                d.writeByte(o);
                d.writeShort((Integer) r[0]);
                d.writeUTF((String) r[1]);
                d.writeUTF(game.pokemon.PokeChieu.ten(0, o));
                d.writeByte(game.pokemon.PokeChieu.he(0, o));
                d.writeByte((Integer) r[4]);
                d.writeByte((Integer) r[3]);
                d.writeInt((Integer) r[4] == 1 ? 60000 * (o - 5) : 0);
            }
        });
    }

    static void goiHL(final int tong, final int[] diem, final long gia, final int cho, final boolean mien) throws Exception {
        final String[] ten = {"HP", "Tấn công", "Phòng thủ", "PP"};
        final String[] mt = {"Tăng % HP tối đa", "Tăng % tấn công (cả PvP)", "Tăng % phòng thủ và phòng thủ đặc biệt", "Tăng % PP tối đa"};
        goi(d -> {
            d.writeByte(1);
            d.writeByte(1);
            int dd = 0;
            for (int x : diem) dd += x;
            d.writeShort(tong);
            d.writeShort(dd);
            d.writeByte(4);
            for (int i = 0; i < 4; i++) {
                d.writeByte(i);
                d.writeUTF(ten[i]);
                d.writeUTF(mt[i]);
                d.writeShort(diem[i]);
                d.writeShort(80);
                d.writeShort(40);
                d.writeShort(3);
                d.writeShort(2);
            }
            d.writeLong(gia);
            d.writeInt(cho);
            d.writeByte(mien ? 1 : 0);
            d.writeByte(2);
            d.writeByte(1);
            d.writeByte(16);
        });
    }

    // ------------------------------------------------------------------ vẽ

    static boolean coVung(String ten) {
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return true;
        }
        return false;
    }

    static int[] vung(String ten) {
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return game.pokemon.VungCham.vungVe(i);
        }
        return null;
    }

    static int veW = 1280, veH = 720;

    static java.awt.image.BufferedImage ve(String nhan) throws Exception {
        ChupTrongGame.ve(veW, veH);
        java.awt.image.BufferedImage a = ChupTrongGame.ve(veW, veH);
        if (ra != null && soAnh < 40) {
            javax.imageio.ImageIO.write(a, "png", new File(ra, String.format("%02d_%s.png", soAnh++, nhan)));
        }
        return a;
    }

    /** Có chấm đỏ (ChuDe.cham) vẽ trong vùng chạm tên `vung` ở khung vừa vẽ. */
    static boolean coCham(String nhan, String vung) throws Exception {
        ChupTrongGame.ve(1280, 720);
        final int truoc = game.pokemon.ChuDe.demCham;
        ve(nhan);
        final int sau = game.pokemon.ChuDe.demCham;
        int[] r = vung(vung);
        if (r == null) return false;
        for (int c = truoc; c < sau && c - truoc < 64; c++) {
            int x = game.pokemon.ChuDe.CHAM_X[c & 63], y = game.pokemon.ChuDe.CHAM_Y[c & 63];
            if (x >= r[0] - 8 && x <= r[0] + r[2] + 8 && y >= r[1] - 8 && y <= r[1] + r[3] + 8) return true;
        }
        return false;
    }

    static void mo(String man) throws Exception {
        ChupTrongGame.chay(() -> ChupTrongGame.moMan(man));
        Thread.sleep(150);
        ChupTrongGame.chay(() -> {
            Object d = game.render.Canvas.currentDialog;
            if (d instanceof game.render.screen.MsgDlg || d instanceof game.pokemon.HopThongBao) game.render.Canvas.endDlg();
        });
    }

    static Object truong(Object o, String ten) throws Exception {
        java.lang.reflect.Field f = o.getClass().getDeclaredField(ten);
        f.setAccessible(true);
        return f.get(o);
    }

    static void bam(String mang, int i) throws Exception {
        final game.pokemon.BangHuanLuyen b = game.pokemon.BangHuanLuyen.gI();
        final Object[] a = (Object[]) truong(b, mang);
        ChupTrongGame.chay(() -> ((game.model.IAction) a[i]).perform());
    }

    static int them(int i) throws Exception {
        return ((int[]) truong(game.pokemon.BangHuanLuyen.gI(), "them"))[i];
    }

    /** {rộng, cao, dpi (0 = PC, -1 = J2ME màn logic)} */
    static final int[][] CO = {{2400, 1080, 420}, {1920, 1080, 420}, {1280, 720, 0}, {1920, 1080, 0}, {1024, 768, 0}, {320, 240, -1}};
    static final String[] TEN_CO = {"dt20x9", "dt16x9", "pc720", "pc1080", "pc4x3", "j2me320"};

    static void datCo(int W, int H, int dpi) throws Exception {
        veW = W;
        veH = H;
        if (dpi < 0) {
            NenPc.rong = W;
            NenPc.cao = H;
            NenPc.heSo = 1;
            NenPc.thatW = W;
            NenPc.thatH = H;
            NenPc.hienX = 0;
            NenPc.hienY = 0;
            NenPc.hienW = W;
            NenPc.hienH = H;
        } else {
            NenPc.tinhKichThuoc(W, H);
        }
        ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
        float s = NenPc.hienW / (float) NenPc.rong;
        if (dpi > 0) {
            float dpL = (dpi / 160f) / s;
            game.pokemon.VungCham.MIN = (int) Math.ceil(44 * dpL);
            game.pokemon.VungCham.CACH = (int) Math.floor(8 * dpL);
            game.pokemon.Bang.anTrai = (int) Math.ceil(100 / s);
        } else {
            game.pokemon.VungCham.MIN = dpi < 0 ? 0 : (int) Math.ceil(32 / s);
            game.pokemon.VungCham.CACH = dpi < 0 ? 0 : (int) Math.ceil(8 / s);
            game.pokemon.Bang.anTrai = 0;
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "kiem_huan_luyen");
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
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        Thread.sleep(500);
        game.pokemon.VungCham.MIN = 16;
        game.pokemon.VungCham.CACH = 4;

        // ---------------- máy chủ cũ (không gói -122): giữ "Tiềm năng"
        game.pokemon.HocChieu.xoa();
        kiem(!game.pokemon.HocChieu.batHL && !game.pokemon.HocChieu.dung(0), "máy chủ cũ: không bật học theo loài / Điểm huấn luyện");
        kiem("Cào".equals(game.pokemon.HocChieu.tenChieu(0, 0)), "máy chủ cũ: tên chiêu theo PokeChieu");

        // ---------------- gói giả: Charmeleon cấp 24 (giaLap: cấp 24, chiêu {1,2,1,1,0,0,-1,-1,-1})
        ChupTrongGame.chay(() -> game.model.Char.skillLevelLearnt = new byte[]{1, 2, 1, 1, 0, 0, -1, -1, -1});
        goiLearnset(5);
        goiHL(46, new int[]{4, 2, 0, 0}, 0L, 0, true);
        kiem(game.pokemon.HocChieu.coLearnset && game.pokemon.HocChieu.dung(0), "đọc gói bảng chiêu");
        kiem(game.pokemon.HocChieu.batHL && game.pokemon.HocChieu.conLai() == 40, "đọc gói Điểm huấn luyện: còn 40");
        kiem("12,0%".equals(game.pokemon.HocChieu.phanTram(game.pokemon.HocChieu.phanNghin(0, 40))), "hiệu quả 40 điểm = +12,0%");
        kiem(game.pokemon.HocChieu.phanNghin(0, 80) == 200 && game.pokemon.HocChieu.phanNghin(0, 99) == 200, "trần 80 điểm = +20,0%");
        kiem("Cấp 2".equals(game.pokemon.HocChieu.trangThai(1, 2, 24)), "ô 1 đã học: Cấp 2");
        kiem("Tự học cấp 16".equals(game.pokemon.HocChieu.trangThai(4, 0, 24)), "ô 4 chưa học: Tự học cấp 16 -> " + game.pokemon.HocChieu.trangThai(4, 0, 24));
        kiem("Tiến hoá: cấp 36".equals(game.pokemon.HocChieu.trangThai(6, -1, 24)), "ô 6 (Charizard): Tiến hoá: cấp 36");
        String ly = game.pokemon.HocChieu.lyDoNpc(6, 24);
        kiem(ly != null && ly.indexOf("Charizard") >= 0 && ly.indexOf("36") >= 0, "NPC chưa dạy Súng phun lửa: lý do tiến hoá Charizard cấp 36 (" + ly + ")");
        kiem(game.pokemon.HocChieu.demNpcNgay(24) == 0, "Charmeleon 24: không có chiêu NPC học ngay (không chấm 'Học chiêu mới')");
        goiLearnset(6);
        kiem(game.pokemon.HocChieu.lyDoNpc(6, 40) == null && game.pokemon.HocChieu.demNpcNgay(40) == 3, "Charizard 40: 3 chiêu NPC học ngay");
        kiem(game.pokemon.HocChieu.lyDoNpc(6, 30) != null, "Charizard 30: Súng phun lửa cần cấp 36");
        goiLearnset(5);

        // ---------------- nút Huấn luyện + chấm đỏ
        kiem(game.pokemon.ChamMoi.co(game.pokemon.ChamMoi.DIEM_TN), "ChamMoi DIEM_TN theo Điểm huấn luyện chưa dùng");
        mo("nv_chi_so");
        kiem(coCham("nut_huan_luyen_con_diem", "tiem-nang"), "nút Huấn luyện có chấm khi còn điểm");
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.mainChar.basePointLeft = 0);
        goiHL(46, new int[]{40, 6, 0, 0}, 50000L, 0, false);
        kiem(!game.pokemon.ChamMoi.co(game.pokemon.ChamMoi.DIEM_TN), "hết điểm: ChamMoi tắt");
        kiem(!coCham("nut_huan_luyen_het_diem", "tiem-nang"), "hết điểm: nút Huấn luyện không chấm");
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.mainChar.basePointLeft = 7);
        kiem(!game.pokemon.ChamMoi.co(game.pokemon.ChamMoi.DIEM_TN), "điểm tiềm năng cũ không còn gây chấm khi máy chủ bật Điểm huấn luyện");
        goiHL(46, new int[]{4, 2, 0, 0}, 0L, 0, true);

        // ---------------- tab Kỹ năng
        mo("nv_ky_nang");
        ve("ky_nang_theo_loai");
        int hang = 0;
        for (int i = 0; i < 9; i++) if (coVung("chieu-" + i)) hang++;
        kiem(hang >= 6, "tab Kỹ năng vẽ hàng chiêu theo loài (" + hang + " hàng thấy)");
        kiem(coVung("o-nhanh-0"), "tab Kỹ năng giữ hàng ô nhanh (+)");

        // ---------------- bảng Huấn luyện ở nhiều cỡ
        for (int c = 0; c < CO.length; c++) {
            final int W = CO[c][0], H = CO[c][1], dpi = CO[c][2];
            if (dpi < 0) {
                htbb.pc.GiaoDienHD.bat = false;
                htbb.pc.ChuHD.bat = false;
                game.pokemon.ChuUI.bo = null;
            }
            datCo(W, H, dpi);
            mo("nv_chi_so");
            ChupTrongGame.chay(game.pokemon.HanhDong::moTiemNang);
            kiem(game.render.Canvas.currentDialog instanceof game.pokemon.BangHuanLuyen, TEN_CO[c] + ": nút Huấn luyện mở BangHuanLuyen");
            ve("bang_" + TEN_CO[c]);
            String thieu = "";
            for (String t : new String[]{"hl-cong-0", "hl-cong-3", "hl-dong", "hl-tay", "hl-xac-nhan"}) {
                if (!coVung(t)) thieu += " " + t;
            }
            kiem(thieu.isEmpty(), TEN_CO[c] + ": đủ nút (thiếu:" + thieu + ")");
            int[] v = game.pokemon.BangHuanLuyen.VUA_VE;
            int tr = game.pokemon.Bang.trai(), ph = game.pokemon.Bang.phai(), tn = game.pokemon.Bang.tren(), du = game.pokemon.Bang.duoi();
            kiem(v[0] >= tr && v[0] + v[2] <= ph && v[1] >= tn && v[1] + v[3] <= du, TEN_CO[c] + ": thẻ trong vùng an toàn " + java.util.Arrays.toString(v));
            kiem(game.pokemon.BangHuanLuyen.DAY_HANG <= game.pokemon.BangHuanLuyen.DINH_NUT, TEN_CO[c] + ": hàng chỉ số không chồng hàng nút ("
                    + game.pokemon.BangHuanLuyen.DAY_HANG + " / " + game.pokemon.BangHuanLuyen.DINH_NUT + ")");
            kiem(v[2] <= (ph - tr) * 80 / 100 + 1, TEN_CO[c] + ": rộng <= 80% (" + v[2] + " / " + (ph - tr) + ")");
            kiem(Math.abs((v[0] - tr) - (ph - v[0] - v[2])) <= 2, TEN_CO[c] + ": canh giữa ngang");
            ChupTrongGame.chay(game.render.Canvas::endDlg);
        }
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.ChuHD.bat = true;
        datCo(1280, 720, 0);
        mo("nv_chi_so");
        ChupTrongGame.chay(game.pokemon.HanhDong::moTiemNang);
        ve("bang_mo");

        // ---------------- + / − theo trần và điểm còn
        goiHL(10, new int[]{79, 0, 0, 0}, 0L, 0, true);   // tổng 10 < đã dùng: còn 0
        bam("cong", 1);
        kiem(them(1) == 0, "hết điểm: + không cộng");
        goiHL(100, new int[]{79, 0, 0, 0}, 0L, 0, true);
        bam("cong", 0);
        bam("cong", 0);
        kiem(them(0) == 1, "trần 80: HP 79 chỉ cộng thêm 1");
        bam("cong", 1);
        bam("cong", 1);
        bam("tru", 1);
        kiem(them(1) == 1, "+ + − = 1");
        ve("bang_da_chon");
        kiem(coVung("hl-tru-1") && !coVung("hl-tru-2"), "nút − chỉ bấm được ở dòng đã chọn điểm");
        ChupTrongGame.chay(() -> ((game.model.IAction) truongKhong(game.pokemon.BangHuanLuyen.gI(), "xacNhan")).perform());
        kiem(them(0) == 0 && them(1) == 0 && !(game.render.Canvas.currentDialog instanceof game.pokemon.BangHuanLuyen), "Xác nhận: gửi và đóng bảng");
        goiHL(100, new int[]{20, 0, 0, 0}, 50000L, 3700, false);
        kiem(game.pokemon.HocChieu.choCon() > 3600 && !game.pokemon.HocChieu.mienPhi, "giây chờ tẩy + giá tẩy đọc đúng");

        // ---------------- ThanhNhanh: chiêu bị gỡ
        final boolean[] ok = new boolean[2];
        ChupTrongGame.chay(() -> {
            game.model.Char.skillLevelLearnt = new byte[]{1, 2, 1, 1, 0, 0, -1, -1, -1};
            game.model.QuickSlot[] bo = new game.model.QuickSlot[5];
            for (int i = 0; i < 5; i++) bo[i] = new game.model.QuickSlot();
            bo[0].setIsSkill(4, 0, false);   // Màn khói đã bị gỡ (cấp 0)
            bo[1].setIsSkill(1, 0, false);
            bo[2].setIsSkill(0, 0, false);
            int doi = game.pokemon.ThanhNhanh.thayChieuMat(bo, 0);
            ok[0] = doi == 1 && bo[0].quickslotType == game.model.QuickSlot.TYPE_SKILL && bo[0].skillType == 3;
            bo[3].setIsSkill(2, 0, false);
            bo[4].setIsSkill(7, 0, false);   // chiêu NPC chưa học (-1), mọi chiêu học đã có trên thanh
            doi = game.pokemon.ThanhNhanh.thayChieuMat(bo, 0);
            ok[1] = doi == 1 && bo[4].quickslotType != game.model.QuickSlot.TYPE_SKILL;
        });
        kiem(ok[0], "ThanhNhanh: ô giữ chiêu bị gỡ -> chiêu học gần nhất chưa có trên thanh");
        kiem(ok[1], "ThanhNhanh: không còn chiêu thay -> ô trống");

        kiem(game.pokemon.VeTay.dem == 0, "icon ve bang code (VeTay) = 0, co " + game.pokemon.VeTay.dem + " lan, cuoi: " + game.pokemon.VeTay.cuoi);
        System.out.println("KiemHuanLuyen: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }

    static Object truongKhong(Object o, String ten) {
        try {
            return truong(o, ten);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
