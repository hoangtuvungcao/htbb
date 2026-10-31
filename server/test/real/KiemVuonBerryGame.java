package real;

import com.htbb.pokemon.HeSinhThai;
import com.htbb.pokemon.HeSinhThaiMenu;
import com.htbb.pokemon.HoSoSinhThai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuMau;
import com.htbb.pokemon.VatLieuSinhThai;
import com.htbb.pokemon.VuonBerry;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vuon berry qua DUONG MENU THAT (bat lai vuon 29/10/2026): Hệ sinh thái → Vườn nhà và bạn bè → Ô đất của tôi → chon o
 * trong → chon hat (tru xu) → (tua o gieo luc ve truoc) → chon o chin = thu hoach (berry vao tui) → buoc So tay
 * "Trồng và thu hoạch 2 cây Berry Oran" (N-1.12) xong, buoc Berry Pecha (TV-08.1) KHONG tang khi thu Oran; bam thu
 * lai / dang nhap lai (doc lai ho so tu chuoi da luu) khong nhan doi; tham vuon ban thay o dat. Khong CSDL (ho so dat
 * thang vao bo nho dem HeSinhThaiGame.HO_SO, tien do vao NhiemVuEngine.TD).
 */
public final class KiemVuonBerryGame {

    private KiemVuonBerryGame() {
    }

    public static KiemKetTau.Bao BAO;

    static void dung(final String m, final boolean v) {
        BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
    }

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    static final class NguoiChoi extends Char {
        final List<String> chu = new ArrayList<String>();
        List<String> menu = new ArrayList<String>();
        int idMenu = -1;

        NguoiChoi(final int id, final int cap, final long xuDau) {
            super(null);
            charDBID = id;
            charname = "vuon" + id;
            isBot = -1;
            lvDetail = new LevelDetail();
            lvDetail.lv = cap;
            xu = xuDau;
        }

        @Override
        public void sendMessage(final Message m) {
            try {
                final DataInputStream in = new DataInputStream(new ByteArrayInputStream(m.toByteArray()));
                in.readByte();
                in.readShort();
                if (m.command == -30) {
                    in.readShort();
                    idMenu = in.readByte();
                    final int n = in.readByte();
                    final List<String> l = new ArrayList<String>();
                    for (int j = 0; j < n; j++) {
                        l.add(in.readUTF());
                    }
                    menu = l;
                } else if (m.command == -26 || m.command == 37) {
                    chu.add(in.readUTF());
                }
            } catch (final Exception ignored) {
            }
        }

        int muc(final String chua) {
            for (int i = 0; i < menu.size(); i++) {
                if (menu.get(i).contains(chua)) {
                    return i;
                }
            }
            return -1;
        }

        String chuCuoi() {
            return chu.isEmpty() ? "" : chu.get(chu.size() - 1);
        }
    }

    static int hang(final String ten) throws Exception {
        final java.lang.reflect.Field f = HeSinhThaiMenu.class.getDeclaredField(ten);
        f.setAccessible(true);
        return f.getInt(null);
    }

    @SuppressWarnings("unchecked")
    static <T> T truong(final Class<?> c, final String ten) throws Exception {
        final java.lang.reflect.Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        return (T) f.get(null);
    }

    static void datTruong(final Class<?> c, final String ten, final Object v) throws Exception {
        final java.lang.reflect.Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        f.set(null, v);
    }

    static NhiemVuMau buoc(final String ma, final String berry, final int so) {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = ma;
        m.loai = ma.startsWith("N-") ? "ngay" : "tuan";
        m.kieu = "lam_vuon";
        m.ten = "Vườn " + ma;
        m.soLuong = so;
        m.cap = 1;
        m.dieuKien = NhiemVuMau.docDieuKien("vat_sinh_thai=" + berry);
        m.mucTieuText = "Trồng và thu hoạch " + so + " cây " + berry + " ở vườn nhà";
        return m;
    }

    /** Bam mot muc menu he sinh thai (bo chan bam nhanh 1,5 giay nhu nguoi choi cho giua hai lan bam). */
    static void bam(final NguoiChoi p, final int idMenu, final int op) throws Exception {
        final java.util.Map<Integer, Long> lan = truong(HeSinhThaiMenu.class, "LAN_CUOI");
        lan.remove(Integer.valueOf(p.charDBID));
        HeSinhThaiMenu.doMenu(p, idMenu, op);
    }

    public static void chay() throws Exception {
        final Class<?> game = Class.forName("com.htbb.pokemon.HeSinhThaiGame");
        final boolean hstCu = HeSinhThai.bat();
        final boolean ldCu = LauDai.bat();
        final boolean kdCu = (Boolean) truong(game, "daKhoiDong");
        final ConcurrentHashMap<Integer, HoSoSinhThai> hoSo = truong(game, "HO_SO");
        final LinkedHashMap<String, NhiemVuMau> mauCu = NhiemVuEngine.MAU;
        final HashMap<String, List<NhiemVuMau>> kieuCu = NhiemVuEngine.THEO_KIEU;
        final ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>> td = truong(NhiemVuEngine.class, "TD");
        final GemTemplate[] gemCu = Map.gemTemplate;
        if (Map.gemTemplate == null) {
            Map.gemTemplate = new GemTemplate[0]; // Char(null) can mang ngoc (giong KiemSuaTuyen)
        }
        final NguoiChoi p = new NguoiChoi(990901, 12, 100000L);
        final NguoiChoi ban = new NguoiChoi(990902, 40, 0L);
        try {
            HeSinhThai.dat(true);
            LauDai.dat(true);
            datTruong(game, "daKhoiDong", Boolean.TRUE); // khong mo luong dinh ky / CSDL trong test
            final HoSoSinhThai h = new HoSoSinhThai();
            hoSo.put(Integer.valueOf(p.charDBID), h);
            final HoSoSinhThai hb = new HoSoSinhThai();
            hoSo.put(Integer.valueOf(ban.charDBID), hb);
            final LinkedHashMap<String, NhiemVuMau> mau = new LinkedHashMap<String, NhiemVuMau>();
            mau.put("N-1.12", buoc("N-1.12", "Berry Oran", 2));
            mau.put("TV-08.1", buoc("TV-08.1", "Berry Pecha", 3));
            final HashMap<String, List<NhiemVuMau>> theoKieu = new HashMap<String, List<NhiemVuMau>>();
            theoKieu.put("lam_vuon", new ArrayList<NhiemVuMau>(mau.values()));
            NhiemVuEngine.MAU = mau;
            NhiemVuEngine.THEO_KIEU = theoKieu;
            final ConcurrentHashMap<String, long[]> t = new ConcurrentHashMap<String, long[]>();
            t.put("N-1.12", new long[]{0L, 0L, 0L, 0L});
            t.put("TV-08.1", new long[]{0L, 0L, 0L, 0L});
            td.put(Integer.valueOf(p.charDBID), t);

            final int mVuon = hang("M_VUON"), mO = hang("M_VUON_O"), mGieo = hang("M_VUON_GIEO");
            // Hệ sinh thái → Vườn nhà và bạn bè (muc goc G_VUON)
            final int goc = java.util.Arrays.asList(HeSinhThaiMenu.GOC).indexOf("vuon");
            bam(p, 0, goc);
            dung("menu Vườn: muc dau 'Ô đất của tôi (0/2 ô có cây, 0 chín)'", p.idMenu == mVuon && p.menu.size() == 6
                    && p.menu.get(0).equals("Ô đất của tôi (0/2 ô có cây, 0 chín)"));
            dung("menu Vườn: giu cac muc cu", p.muc("Xem vườn của tôi") == 1 && p.muc("Tưới vườn bạn bè") == 3
                    && p.muc("Thăm vườn bạn bè") == 4 && p.muc("Hướng dẫn trồng berry") == 5);
            bam(p, mVuon, 5);
            dung("huong dan trong berry: khong con 'chưa mở trong bản thử nghiệm'", !p.chuCuoi().contains("chưa mở")
                    && p.chuCuoi().contains("Ô đất của tôi") && p.chuCuoi().contains("Berry Oran: cấp 1, 20 phút"));
            bam(p, mVuon, 0);
            bang("o dat cap 12: 2 o trong + mo them", "[Ô 1: trống - gieo hạt, Ô 2: trống - gieo hạt, Mở thêm ô đất ("
                    + VuonBerry.giaMoO(12, 0) + " xu)]", p.menu.toString());
            // gieo Berry Oran o 1
            bam(p, mO, 0);
            dung("chon o trong -> menu hat (8 berry, Chesto can cap 10 mo, Leppa khoa)", p.idMenu == mGieo && p.menu.size() == 8
                    && p.menu.get(0).startsWith("Berry Oran - hạt " + VuonBerry.giaHat(30, 12) + " xu") && p.menu.get(5).contains("cần cấp 20"));
            bam(p, mGieo, 5); // Leppa can cap 20
            dung("hat khoa cap: bao 'Cần đạt cấp 20', khong tru xu", p.chuCuoi().contains("Cần đạt cấp 20") && p.xu == 100000L);
            bam(p, mO, 0);
            final long gia = VuonBerry.giaHat(30, 12);
            bam(p, mGieo, 0);
            bang("gieo Oran: tru xu hat", Long.valueOf(100000L - gia), Long.valueOf(p.xu));
            dung("gieo Oran: bao + menu o dat cap nhat", p.chu.toString().contains("Đã gieo Berry Oran ở ô 1") && p.menu.get(0).startsWith("Ô 1: Berry Oran - còn 20 phút"));
            // bam lai o dang lon: khong gieo chong, khong thu
            final long xu1 = p.xu;
            bam(p, mO, 0);
            dung("o dang lon: bao con bao lau, khong tru xu", p.chuCuoi().contains("còn 20 phút") && p.xu == xu1 && h.vuonMa[0] == 30);
            // goi menu hat cu (menu da gui truoc) cho o 1 dang co cay: khong gieo chong
            final java.util.Map<Integer, Object> ngu = truong(HeSinhThaiMenu.class, "NGU_CANH");
            final Object c = ngu.get(Integer.valueOf(p.charDBID));
            final java.lang.reflect.Field chiSo = c.getClass().getDeclaredField("chiSo");
            chiSo.setAccessible(true);
            final java.lang.reflect.Field ds = c.getClass().getDeclaredField("ds");
            ds.setAccessible(true);
            chiSo.setInt(c, 0);
            @SuppressWarnings("unchecked")
            final List<Integer> l = (List<Integer>) ds.get(c);
            l.clear();
            l.add(31);
            bam(p, mGieo, 0);
            dung("goi menu hat cu cho o dang co cay: tu choi, khong tru xu", p.chuCuoi().contains("đang có cây") && p.xu == xu1 && h.vuonMa[0] == 30);
            // thu som: chua chin
            bam(p, mVuon, 0);
            bam(p, mO, 0);
            dung("chua chin: khong co berry", h.tongMa(30) == 0);
            // dong ho may chu tien 21 phut (tua moc gieo ve truoc)
            h.vuonGieo[0] -= 21 * 60000L;
            bam(p, 0, goc);
            dung("menu Vườn: 1 chín", p.menu.get(0).equals("Ô đất của tôi (1/2 ô có cây, 1 chín)"));
            bam(p, mVuon, 0);
            dung("o chin: 'Ô 1: Berry Oran đã chín - thu hoạch'", p.menu.get(0).equals("Ô 1: Berry Oran đã chín - thu hoạch"));
            bam(p, mO, 0);
            bang("thu hoach: +3 Berry Oran (bac 1) vao tui", Integer.valueOf(3), Integer.valueOf(h.so(VatLieuSinhThai.khoa(30, 1))));
            dung("thu hoach: o trong lai", h.vuonMa[0] == 0 && p.menu.get(0).equals("Ô 1: trống - gieo hạt"));
            bang("So tay N-1.12 (Berry Oran) 1/2", Long.valueOf(1L), Long.valueOf(t.get("N-1.12")[0]));
            bang("So tay TV-08.1 (Berry Pecha) KHONG tang khi thu Oran", Long.valueOf(0L), Long.valueOf(t.get("TV-08.1")[0]));
            bang("dem thu hoach hom nay", Integer.valueOf(1), Integer.valueOf(h.thuVuonHomNay));
            // bam lai dong cu (o vua thu): khong nhan doi
            final int truoc = h.tongMa(30);
            l.clear();
            l.add(0);
            bam(p, mO, 0);
            bang("bam lai o vua thu (menu cu): khong them berry", Integer.valueOf(truoc), Integer.valueOf(h.tongMa(30)));
            bang("bam lai: So tay khong tang", Long.valueOf(1L), Long.valueOf(t.get("N-1.12")[0]));
            // dang nhap lai giua chung: gieo, luu chuoi, nap lai (nhu CSDL) -> cay con, thu mot lan
            bam(p, mVuon, 0);
            bam(p, mO, 0);
            bam(p, mGieo, 0);
            final String luu = h.chuoi();
            final HoSoSinhThai h2 = HoSoSinhThai.doc(luu);
            hoSo.put(Integer.valueOf(p.charDBID), h2);
            dung("dang nhap lai: o 1 van dang lon Berry Oran (cung moc gieo)", h2.vuonMa[0] == 30 && h2.vuonGieo[0] == h.vuonGieo[0]
                    && h2.tongMa(30) == 3 && h2.thuVuonHomNay == 1);
            h2.vuonGieo[0] -= 21 * 60000L;
            bam(p, mVuon, 0);
            bam(p, mO, 0);
            bang("sau dang nhap lai: thu duoc 3 nua (tong 6)", Integer.valueOf(6), Integer.valueOf(h2.tongMa(30)));
            bang("So tay N-1.12 xong 2/2", Long.valueOf(2L), Long.valueOf(t.get("N-1.12")[0]));
            bang("So tay N-1.12 trang thai 1 (cho nhan thuong)", Long.valueOf(1L), Long.valueOf(t.get("N-1.12")[1]));
            dung("bao hoan thanh nhiem vu", p.chu.toString().contains("Hoàn thành nhiệm vụ: Vườn N-1.12"));
            // nap lai chuoi CU (truoc thu hoach) khong xay ra: chuoi moi khong con cay
            final HoSoSinhThai h3 = HoSoSinhThai.doc(h2.chuoi());
            dung("chuoi sau thu hoach: o trong, berry da vao tui", h3.vuonMa[0] == 0 && h3.tongMa(30) == 6);
            // thu Pecha: TV-08.1 tang
            bam(p, mVuon, 0);
            bam(p, mO, 1);
            bam(p, mGieo, 1);
            h2.vuonGieo[1] -= 21 * 60000L;
            bam(p, mVuon, 0);
            bam(p, mO, p.muc("Ô 2"));
            bang("thu Berry Pecha: TV-08.1 1/3", Long.valueOf(1L), Long.valueOf(t.get("TV-08.1")[0]));
            // che "Thuốc xịt từ vườn" (3 Berry Oran -> 10 binh HP nho): re hon NPC, tran gia tri / ngay
            final java.lang.reflect.Method moCt = HeSinhThaiMenu.class.getDeclaredMethod("moCongThuc", Char.class);
            moCt.setAccessible(true);
            moCt.invoke(null, p);
            dung("menu cong thuc co 'Thuốc xịt từ vườn' o vi tri id 30", p.menu.size() > 30 && p.menu.get(30).startsWith("Thuốc xịt từ vườn"));
            final long xuCt = p.xu;
            final int oranTruoc = h2.tongMa(30);
            bam(p, hang("M_CONG_THUC"), 30);
            bang("che thuoc vuon: -3 Berry Oran", Integer.valueOf(oranTruoc - 3), Integer.valueOf(h2.tongMa(30)));
            bang("che thuoc vuon: +1 Thuốc xịt (10 binh HP nho khi dung)", Integer.valueOf(1), Integer.valueOf(h2.tongMa(90)));
            com.htbb.pokemon.KyNangSong.CongThuc ct30 = null;
            for (final com.htbb.pokemon.KyNangSong.CongThuc ctx : com.htbb.pokemon.KyNangSong.CONG_THUC) {
                if (ctx.id == 30) {
                    ct30 = ctx;
                }
            }
            // 01/11/2026: phi bep thuoc vuon = 65% gia NPC 10 binh - tien hat (VuonBerry.phiBepThuocVuon), khong theo gio thu nhap
            bang("che thuoc vuon: chi tru phi bep thuoc vuon", Long.valueOf(xuCt - VuonBerry.phiBepThuocVuon(ct30, 12)), Long.valueOf(p.xu));
            bang("dem gia tri thuoc vuon hom nay", Long.valueOf(1000L), Long.valueOf(h2.thuocVuonHomNay));
            h2.thuocVuonHomNay = VuonBerry.tranThuocNgay(12) - 999L;
            bam(p, hang("M_CONG_THUC"), 30);
            dung("vuot tran gia tri thuoc vuon / ngay: tu choi, khong tru berry", p.chuCuoi().contains("Thuốc chế hôm nay đã đủ")
                    && h2.tongMa(30) == oranTruoc - 3);
            // tham vuon ban: thay o dat cua ban
            hb.vuonMa[0] = 37;
            hb.vuonBac[0] = 3;
            hb.vuonGieo[0] = com.htbb.pokemon.DongHo.bay();
            final java.lang.reflect.Method mt = HeSinhThaiMenu.class.getDeclaredMethod("moTaVuon", Char.class, HoSoSinhThai.class);
            mt.setAccessible(true);
            final String tham = (String) mt.invoke(null, ban, hb);
            dung("tham vuon ban: thay 'Ô 1: Berry Lum - còn 120 phút' + o trong", tham.contains("Ô 1: Berry Lum - còn 120 phút")
                    && tham.contains("Ô 4: trống") && !tham.contains("Ô 5"));
            // mo them o: tru xu, them o
            final long xu2 = p.xu;
            bam(p, mVuon, 0);
            bam(p, mO, p.muc("Mở thêm ô đất"));
            bang("mo them o: tru 1 gio thu nhap", Long.valueOf(xu2 - VuonBerry.giaMoO(12, 0)), Long.valueOf(p.xu));
            bang("mo them o: vuon 3 o", Integer.valueOf(1), Integer.valueOf(h2.vuonDaMua));
            dung("menu o dat co Ô 3", p.muc("Ô 3: trống") >= 0);
            // het xu: khong gieo, khong mat xu
            p.xu = 1L;
            bam(p, mO, p.muc("Ô 3"));
            bam(p, mGieo, 0);
            dung("thieu xu: khong gieo", h2.vuonMa[2] == 0 && p.xu == 1L && p.chuCuoi().contains("Không đủ"));
        } finally {
            hoSo.remove(Integer.valueOf(p.charDBID));
            hoSo.remove(Integer.valueOf(ban.charDBID));
            td.remove(Integer.valueOf(p.charDBID));
            NhiemVuEngine.MAU = mauCu;
            NhiemVuEngine.THEO_KIEU = kieuCu;
            datTruong(game, "daKhoiDong", Boolean.valueOf(kdCu));
            HeSinhThai.dat(hstCu);
            LauDai.dat(ldCu);
            Map.gemTemplate = gemCu;
        }
    }
}
