import com.htbb.pokemon.DuongMenu;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.KhoaBanDo;
import com.htbb.pokemon.NhiemVuMau;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import real.Char;
import real.GymKiem;
import real.KhoaBanDoGame;
import real.LevelDetail;
import real.MenuDoiThuong;
import real.MucTieuNhiemVuGame;
import real.NhiemVuEngine;
import real.SoTayLauDai;

/**
 * DUONG MENU + DUONG TAT GYM (nhanh sua-duong-gym). Nguoi choi 0.9.14 (J2ME) bao: muc tieu cua ai "Hạ Trưởng Gym Pewter:
 * Golem (Sổ tay huấn luyện → Gym)", "Nhận thưởng ở NPC Đổi thưởng → …" - NPC that ten "Quầy đổi thưởng", muc that la
 * "Thách đấu Gym": "Làm theo hướng dẫn rồi mà tìm không thấy".
 * <ul>
 *   <li>Bo kiem duong (DuongMenu.kiem) tren chu mau dung / sai.</li>
 *   <li>Cay menu that dung TU MA menu (MenuDoiThuong.cay: -90, So tay goc, Nhiệm vụ của tôi, Thách đấu Gym) - moi chuoi
 *       "A → B → C" bat dau bang nhan menu may chu trong sql/htbb.sql, server/src, web, docs phai khop tung buoc (VINH VIEN:
 *       them chu moi sai duong la hong test).</li>
 *   <li>Thu tu menu Quầy đổi thưởng co / khong buoc ha Gym (trang dau 3 dong J2ME), menu Gym, Nhiệm vụ của tôi.</li>
 *   <li>Duong tat: muc dau quay / chon buoc o Đang làm / Huỷ o so -> hop 95 -> Đồng ý = cung GymLauDai.thachDauStt voi So
 *       tay -> Thách đấu Gym (cung kiem cong tac, cap).</li>
 * </ul>
 */
final class KiemDuongMenu {

    private KiemDuongMenu() {
    }

    static final Path GOC = Paths.get("..");

    public static void chay() {
        Kiem.nhom("Duong menu that (Quay doi thuong / So tay / Thach dau Gym) + duong tat thach dau Gym");
        try {
            boKiem();
            final DuongMenu.Nut cay = cayThat();
            cayTuMa(cay);
            quet(cay);
            thuTuMenu();
            duongTat(cay);
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.dung("KiemDuongMenu chay het: " + e, false);
        }
    }

    // ------------------------------------------------------------------ du lieu that

    static String doc(final Path f) throws Exception {
        return new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
    }

    /** {ten boss, cap} cua 16 Gym tu data_noi_dung_gym (phan A sql/htbb.sql). */
    static List<Object[]> gymSql() throws Exception {
        final String s = doc(GOC.resolve("sql/htbb.sql"));
        final int i = s.indexOf("INSERT INTO `data_noi_dung_gym` VALUES");
        final String dong = s.substring(i, s.indexOf('\n', i));
        final Matcher m = Pattern.compile("\\((\\d+),'([^']*)','([^']*)','([^']*)','([^']*)',(\\d+),(\\d+),'([^']*)'").matcher(dong);
        final List<Object[]> ds = new ArrayList<Object[]>();
        while (m.find()) {
            ds.add(new Object[]{m.group(8), Integer.valueOf(m.group(6)), Integer.valueOf(m.group(1)), m.group(2), Integer.valueOf(m.group(7))});
        }
        return ds;
    }

    /** Muc huong dan tan thu noi cuoi So tay (data_huong_dan_tan_thu so_tay.*). */
    static List<String> mucHuongDan() throws Exception {
        final String s = doc(GOC.resolve("sql/htbb.sql"));
        final List<String> ds = new ArrayList<String>();
        for (final String ma : new String[]{"so_tay.bo_qua", "so_tay.huong_dan"}) {
            final Matcher m = Pattern.compile("\\('" + Pattern.quote(ma) + "', ?\\d+, ?'([^']*)'").matcher(s);
            if (m.find()) {
                ds.add(m.group(1));
            }
        }
        return ds;
    }

    static DuongMenu.Nut cayThat() throws Exception {
        final List<Object[]> g = gymSql();
        Kiem.bang("data_noi_dung_gym: 16 Gym", Integer.valueOf(16), Integer.valueOf(g.size()));
        final List<String> hd = mucHuongDan();
        Kiem.bang("So tay: 2 muc huong dan tan thu", Integer.valueOf(2), Integer.valueOf(hd.size()));
        return MenuDoiThuong.cay(g, hd);
    }

    // ------------------------------------------------------------------ bo kiem

    static void boKiem() throws Exception {
        final DuongMenu.Nut c = cayThat();
        final String[][] dung = {
            {"duong cua ai", "Nhận ở: Quầy đổi thưởng → Nhiệm vụ của tôi → Thử thách cửa ải."},
            {"duong qua So tay", "Xem ở: Quầy đổi thưởng → Sổ tay huấn luyện → Nhiệm vụ của tôi → Đang làm."},
            {"duong Gym trong ngoac", "Hạ Trưởng Gym Pewter: Golem (Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Pewter)."},
            {"muc co ngoac (Sư phạm (nhận trò))", "Nhận trò ở Quầy đổi thưởng → Sổ tay huấn luyện → Sư phạm (nhận trò). Khi trò đạt cấp"},
            {"muc co dau phay", "Xem ở Quầy đổi thưởng → Sổ tay huấn luyện → Lịch đăng nhập, vé chương."},
            {"bat dau giua (Sổ tay huấn luyện → Cúp PvP tuần)", "Đăng ký: Sổ tay huấn luyện → Cúp PvP tuần (Cúp Đôi: đội trưởng)."},
            {"menu client bo qua", "Mở Menu → Pokédex → Thưởng mốc."},
            {"NPC noi tiep bo qua", "NPC:|Xatu → Blaziken|Phần thưởng"},
            {"muc tat Gym o quay", "Quầy đổi thưởng → Thách đấu Gym Pewter (Golem)"},
        };
        for (final String[] d : dung) {
            Kiem.bang("bo kiem dung: " + d[0], "[]", DuongMenu.kiem(d[1], c).toString());
        }
        final String[][] sai = {
            {"'Sổ tay huấn luyện → Gym' (bao cua nguoi choi)", "Hạ Trưởng Gym Pewter: Golem (Sổ tay huấn luyện → Gym)."},
            {"'NPC Đổi thưởng' khong phai ten NPC", "Nhận thưởng ở NPC Đổi thưởng → Sổ tay huấn luyện → Nhiệm vụ của tôi → Đang làm."},
            {"'Sổ tay' thieu chu", "Xem ở Sổ tay → Hầm ngục bí ẩn → Phó bản tổ đội."},
            {"ten muc cat ('Năng lượng')", "Trạng thái ở Quầy đổi thưởng → Sổ tay huấn luyện → Năng lượng."},
            {"muc khong o cap do ('Quầy → Thử thách cửa ải')", "Quầy đổi thưởng → Thử thách cửa ải."},
            {"Gym khong co", "Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Lavender."},
        };
        for (final String[] d : sai) {
            Kiem.bang("bo kiem bat loi: " + d[0], "1", String.valueOf(DuongMenu.kiem(d[1], c).size()));
        }
        Kiem.bang("tenGym", "Gym Pewter", DuongMenu.tenGym("Trưởng Gym Pewter: Golem"));
        Kiem.bang("nhanGym", "Gym Pewter (Golem) - cấp 12 (đã có)", DuongMenu.nhanGym("Trưởng Gym Pewter: Golem", 12, true));
        Kiem.bang("duongGym", "Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Pewter", DuongMenu.duongGym("Trưởng Gym Pewter: Golem"));
        Kiem.bang("nhanTatGym", "Thách đấu Gym Pewter (Golem)", DuongMenu.nhanTatGym("Trưởng Gym Pewter: Golem"));
    }

    // ------------------------------------------------------------------ cay dung tu ma

    static void cayTuMa(final DuongMenu.Nut c) {
        Kiem.bang("goc = ten NPC that o the gioi", "Quầy đổi thưởng", c.nhan);
        final DuongMenu.Nut soTay = c.tim(DuongMenu.SO_TAY);
        final DuongMenu.Nut nv = c.tim(DuongMenu.NHIEM_VU);
        Kiem.dung("quay co So tay + Nhiệm vụ của tôi", soTay != null && nv != null);
        final List<String> st = new ArrayList<String>(DuongMenu.nhanCon(soTay));
        Kiem.bang("So tay: muc = SoTayLauDai.MUC_GOC (+ huong dan)", Arrays.asList(SoTayLauDai.MUC_GOC).toString(),
                st.subList(0, SoTayLauDai.MUC_GOC.length).toString());
        Kiem.bang("So tay: SO_MUC_GOC = so muc co dinh", Integer.valueOf(SoTayLauDai.MUC_GOC.length), Integer.valueOf(GymKiem.soMucGoc()));
        Kiem.bang("So tay muc 8 = Nhiệm vụ của tôi (client 0.9.15 gui -527/0/8)", DuongMenu.NHIEM_VU, SoTayLauDai.MUC_GOC[8]);
        Kiem.bang("Nhiệm vụ của tôi: muc = NhiemVuEngine.MUC_GOC", Arrays.asList(NhiemVuEngine.MUC_GOC).toString(), DuongMenu.nhanCon(nv).toString());
        Kiem.bang("Thách đấu Gym: 16 dong", Integer.valueOf(16), Integer.valueOf(soTay.tim(DuongMenu.GYM).con.size()));
        Kiem.bang("Thách đấu Gym: dong 1", "Gym Pewter (Golem) - cấp 12", soTay.tim(DuongMenu.GYM).con.get(0).nhan);
    }

    // ------------------------------------------------------------------ quet moi chu cho nguoi choi

    static List<Path> tep(final String thuMuc, final String... duoi) throws Exception {
        final Path g = GOC.resolve(thuMuc);
        if (!Files.isDirectory(g)) {
            return new ArrayList<Path>();
        }
        try (Stream<Path> s = Files.walk(g)) {
            return s.filter(Files::isRegularFile).filter(p -> {
                final String t = p.toString();
                if (t.contains(File.separator + "node_modules" + File.separator) || t.contains(File.separator + ".nuxt" + File.separator)
                        || t.contains(File.separator + ".output" + File.separator)) {
                    return false;
                }
                for (final String d : duoi) {
                    if (t.endsWith(d)) {
                        return true;
                    }
                }
                return false;
            }).sorted().collect(Collectors.toList());
        }
    }

    static int soDuong(final String s, final DuongMenu.Nut c) {
        int n = 0;
        int i = s.indexOf(DuongMenu.MUI);
        while (i >= 0) {
            final String t = s.substring(Math.max(0, i - 40), i);
            if (t.contains(DuongMenu.QUAY) || t.contains(DuongMenu.SO_TAY) || t.contains(DuongMenu.NHIEM_VU)) {
                n++;
            }
            i = s.indexOf(DuongMenu.MUI, i + 1);
        }
        return n;
    }

    static void quet(final DuongMenu.Nut c) throws Exception {
        final List<Path> ds = new ArrayList<Path>();
        ds.add(GOC.resolve("sql/htbb.sql"));
        ds.addAll(tep("server/src", ".java"));
        ds.addAll(tep("web/app", ".vue", ".ts"));
        ds.addAll(tep("web/shared", ".ts", ".json"));
        ds.addAll(tep("docs", ".md"));
        final List<String> loi = new ArrayList<String>();
        int soTep = 0, duong = 0;
        for (final Path f : ds) {
            String s = doc(f);
            if (s.indexOf('→') < 0) {
                continue;
            }
            if (f.toString().endsWith(".vue")) {
                s = s.replaceAll("<[^<>]*>", ""); // <NuxtLink>, <strong>: chu hien thi
            }
            soTep++;
            duong += soDuong(s, c);
            for (final String l : DuongMenu.kiem(s, c)) {
                loi.add(GOC.relativize(f) + ": " + l);
            }
        }
        for (final String l : loi) {
            System.out.println("    " + l);
        }
        Kiem.dung("da quet chu that (" + soTep + " tep, " + duong + " duong menu)", soTep > 10 && duong >= 40);
        Kiem.bang("moi duong menu trong CSDL / ma nguon / web / docs khop nhan menu that", "0", String.valueOf(loi.size()));
        final String sql = doc(GOC.resolve("sql/htbb.sql"));
        Kiem.sai("CSDL: het 'Sổ tay huấn luyện → Gym'", sql.contains("Sổ tay huấn luyện → Gym"));
        Kiem.sai("CSDL: het 'NPC Đổi thưởng'", sql.contains("NPC Đổi thưởng"));
        Kiem.bang("CSDL: 14 buoc cua ai ha Gym x 2 cot x 2 phan ghi duong Gym day du", Integer.valueOf(56),
                Integer.valueOf(sql.split(Pattern.quote("(Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym "), -1).length - 1));
        Kiem.bang("CSDL: goi_y 14 x 2 noi Gym danh o dau (khong dich chuyen, danh tay)", Integer.valueOf(28),
                Integer.valueOf(sql.split(Pattern.quote("Trưởng Gym hiện ngay cạnh bạn ở bản đồ đang đứng (không dịch chuyển); đánh tay"), -1).length - 1));
        // chu sinh luc chay
        final String[] chay = {KhoaBanDo.DUONG_NHAN, KhoaBanDo.DUONG_XEM, DuongMenu.cachToiGym("Trưởng Gym Pewter: Golem"),
            DuongMenu.hoiGym("x", "Trưởng Gym Pewter: Golem"), KhoaBanDo.lyDo(new KhoaBanDo.Luat(10, 27, "CA-05.3", "Cửa ải: X", true, ""), 20, false, 1, "Y")};
        for (final String s : chay) {
            Kiem.bang("chu luc chay dung duong: " + s.substring(0, Math.min(50, s.length())), "[]", DuongMenu.kiem(s, c).toString());
        }
        Kiem.bang("DUONG_NHAN", "Nhận ở: Quầy đổi thưởng → Nhiệm vụ của tôi → Thử thách cửa ải.", KhoaBanDo.DUONG_NHAN);
    }

    // ------------------------------------------------------------------ thu tu menu

    static void thuTuMenu() {
        final String[][] khong = MenuDoiThuong.xep(true, true, null, 2);
        Kiem.bang("quay, khong buoc Gym: thu tu", "[Nhiệm vụ của tôi, Sổ tay huấn luyện, Điểm hiện tại: 2 điểm - Xem đổi thưởng, Pokédex, Hệ sinh thái]",
                Arrays.asList(khong[1]).toString());
        final String[][] co = MenuDoiThuong.xep(true, true, "Trưởng Gym Pewter: Golem", 2);
        Kiem.bang("quay, dang lam buoc ha Gym: trang dau (3 dong J2ME)", "[Thách đấu Gym Pewter (Golem), Nhiệm vụ của tôi, Sổ tay huấn luyện]",
                Arrays.asList(co[1]).subList(0, 3).toString());
        Kiem.bang("quay, dang lam buoc ha Gym: 6 muc", Integer.valueOf(6), Integer.valueOf(co[1].length));
        Kiem.bang("quay tat lau dai: nhu cu (Xem doi thuong, Pokédex)", "[Điểm hiện tại: 0 điểm - Xem đổi thưởng, Pokédex]",
                Arrays.asList(MenuDoiThuong.xep(false, false, "Trưởng Gym Pewter: Golem", 0)[1]).toString());
        Kiem.bang("Nhiệm vụ của tôi: Thử thách cửa ải ngay sau Đang làm (trang dau)", DuongMenu.CUA_AI, NhiemVuEngine.MUC_GOC[1]);
        final int[] nhom = GymKiem.nhomGoc();
        final int[] sx = nhom.clone();
        Arrays.sort(sx);
        Kiem.bang("Nhiệm vụ của tôi: nhom la hoan vi 0..7", "[0, 1, 2, 3, 4, 5, 6, 7]", Arrays.toString(sx));
        final String[] nhomTen = {"Đang làm", "Nhận nhiệm vụ phụ", "Nhiệm vụ hằng ngày", "Nhiệm vụ hằng tuần", "Thành tựu", "Sự kiện tuần này",
            "Cốt truyện năm 2", "Thử thách cửa ải"};
        boolean khop = true;
        for (int i = 0; i < nhom.length; i++) {
            khop &= nhomTen[nhom[i]].equals(NhiemVuEngine.MUC_GOC[i]);
        }
        Kiem.dung("Nhiệm vụ của tôi: moi muc dan dung nhom (dsCoTheNhan)", khop);
        final List<Object[]> g = new ArrayList<Object[]>();
        for (int i = 1; i <= 4; i++) {
            g.add(new Object[]{Integer.valueOf(i)});
        }
        Kiem.bang("menu Gym: Gym cua buoc dang lam len dau", "[3, 1, 2, 4]", Arrays.toString(GymKiem.thuTu(g, 3)));
        Kiem.bang("menu Gym: khong buoc -> theo stt", "[1, 2, 3, 4]", Arrays.toString(GymKiem.thuTu(g, 0)));
    }

    // ------------------------------------------------------------------ duong tat (trong game)

    static final class NguoiChoi extends Char {
        final List<Message> goi = new ArrayList<Message>();

        NguoiChoi(final int id, final int cap) {
            super(null);
            charDBID = id;
            charname = "kiemgym" + id;
            lvDetail = new LevelDetail();
            lvDetail.lv = cap;
            mainQuest = new real.NewCharQuest(true);
            subQuest = new real.NewCharQuest(true);
            clanQuestNew = new real.NewCharQuest(true);
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
        }

        /** Chu cuoi cung cua lenh (-30 menu: cac dong noi \u0001; -32: "id|chu"; 37: chu). */
        String chu(final int lenh, final int tu) throws Exception {
            String kq = null;
            for (int i = tu; i < goi.size(); i++) {
                final Message m = goi.get(i);
                if (m.command != lenh) {
                    continue;
                }
                final DataInputStream in = new DataInputStream(new ByteArrayInputStream(m.toByteArray()));
                in.readByte();
                in.readShort();
                if (lenh == -32) {
                    in.readShort();
                    kq = in.readByte() + "|" + in.readUTF();
                } else if (lenh == -30) {
                    in.readShort();
                    in.readByte();
                    final int n = in.readByte();
                    final StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < n; j++) {
                        sb.append(j > 0 ? "\u0001" : "").append(in.readUTF());
                    }
                    kq = sb.toString();
                } else {
                    kq = in.readUTF();
                }
            }
            return kq;
        }
    }

    static Object[] dongGym(final Object[] s) {
        // {stt, ma, huyHieu, cap, template, tenBoss, hp, tc, thu, noPhut, thuongDau, thuongTuan}
        return new Object[]{s[2], s[3], "Huy hiệu", s[1], s[4], s[0], Integer.valueOf(1000), Integer.valueOf(10), Integer.valueOf(10),
            Integer.valueOf(5), "", ""};
    }

    static NhiemVuMau buocGym(final String ma, final String stt, final String ten) {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = ma;
        m.loai = "cua_ai";
        m.kieu = "ha_gym";
        m.mucTieuId = stt;
        m.ten = ten;
        m.mucTieuText = "Hạ Trưởng Gym Pewter: Golem (Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Pewter).";
        m.soLuong = 1;
        m.cap = 1;
        m.truoc = "";
        return m;
    }

    static void duongTat(final DuongMenu.Nut cay) throws Exception {
        final List<Object[]> gymCu = GymKiem.gym();
        final boolean lauDaiCu = com.htbb.pokemon.LauDai.bat();
        final List<Object[]> g = new ArrayList<Object[]>();
        for (final Object[] s : gymSql()) {
            g.add(dongGym(s));
        }
        final LinkedHashMap<String, NhiemVuMau> mau = new LinkedHashMap<String, NhiemVuMau>();
        mau.put("CA-03.2", buocGym("CA-03.2", "1", "Cửa ải: hạ Gym Pewter"));
        final LinkedHashMap<String, NhiemVuMau> mauCu = GymKiem.datMau(mau);
        final NguoiChoi p = new NguoiChoi(990301, 5);
        final ConcurrentHashMap<String, long[]> t = new ConcurrentHashMap<String, long[]>();
        try {
            com.htbb.pokemon.LauDai.dat(true);
            GymKiem.datGym(g);
            GymKiem.datTienDo(p, t);
            GymKiem.datHoSo(p, new HoSoLauDai());
            KhoaBanDoGame.xoaHoi(p.charDBID);

            // ---- khong buoc Gym: menu thuong
            MenuDoiThuong.mo(p);
            String menu = p.chu(-30, 0);
            Kiem.dung("mo quay, chua nhan buoc Gym: muc dau Nhiệm vụ của tôi", menu != null && menu.startsWith(DuongMenu.NHIEM_VU + "\u0001"));

            // ---- dang lam CA-03.2 (0/1)
            t.put("CA-03.2", new long[]{0L, 0L, 0L, 0L});
            int tu = p.goi.size();
            MenuDoiThuong.mo(p);
            menu = p.chu(-30, tu);
            final List<String> dong = Arrays.asList(menu.split("\u0001"));
            Kiem.bang("mo quay, dang lam buoc ha Gym: trang dau", "[Thách đấu Gym Pewter (Golem), Nhiệm vụ của tôi, Sổ tay huấn luyện]",
                    dong.subList(0, 3).toString());
            Kiem.bang("muc tat co trong cay menu that", "[]", DuongMenu.kiem("Quầy đổi thưởng → " + dong.get(0) + ".", cay).toString());
            tu = p.goi.size();
            MenuDoiThuong.chon(p, 0, null);
            String hop = p.chu(-32, tu);
            Kiem.dung("chon muc dau: hop 95 'Thách đấu Trưởng Gym Pewter (Golem) ngay?' + noi danh",
                    hop != null && hop.startsWith(KhoaBanDoGame.POPUP_CUA_AI + "|") && hop.contains("Thách đấu Trưởng Gym Pewter (Golem) ngay?")
                            && hop.contains("không dịch chuyển") && hop.contains("Tự động đánh không dùng được"));

            // Đồng ý = cung cua So tay -> Thách đấu Gym: gym.bat=0 -> cung cau
            final HashMap<String, String> tat = new HashMap<String, String>();
            tat.put("gym.bat", "0");
            com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, tat, false);
            tu = p.goi.size();
            KhoaBanDoGame.khiTraLoi(p, true);
            final String dongY = p.chu(37, tu);
            Kiem.bang("Đồng ý (gym tat) = So tay -> Thách đấu Gym dong Pewter", GymKiem.chonSoTay(p, 0), dongY);
            if (!com.htbb.cauhinh.CongTac.gym()) { // gym.bat la khoa "luon_bat" khi danh muc da nap: ghi de CSDL khong tat duoc
                Kiem.bang("Đồng ý (gym tat): cau Gym tam dong", "Gym đang tạm đóng. Vui lòng quay lại sau.", dongY);
            }
            final HashMap<String, String> bat = new HashMap<String, String>();
            bat.put("gym.bat", "1");
            com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, bat, false);
            tu = p.goi.size();
            KhoaBanDoGame.khiTraLoi(p, true);
            Kiem.bang("hop dung mot lan: Đồng ý lan hai khong lam gi", tu, p.goi.size());

            // chon buoc o Nhiệm vụ của tôi -> Đang làm: hop, Đồng ý = cung kiem cap
            tu = p.goi.size();
            GymKiem.chonDangLam(p, "CA-03.2");
            hop = p.chu(-32, tu);
            Kiem.dung("Đang làm -> buoc ha Gym: hop kem tien do (0/1) + hoi thach dau",
                    hop != null && hop.contains("Cửa ải: hạ Gym Pewter (0/1)") && hop.contains("ngay?"));
            tu = p.goi.size();
            KhoaBanDoGame.khiTraLoi(p, true);
            Kiem.bang("Đồng ý (cap 5 < 12) = So tay -> Thách đấu Gym", GymKiem.chonSoTay(p, 0), p.chu(37, tu));
            if (com.htbb.cauhinh.CongTac.gym()) { // danh muc cau hinh da nap (KiemDuLieuSql chay truoc trong Kiem.main)
                Kiem.bang("Đồng ý (cap 5 < 12): Cần đạt cấp 12.", "Cần đạt cấp 12.", GymKiem.chonSoTay(p, 0));
            }
            Kiem.bang("thachDauStt(1) = So tay dong 0", GymKiem.thachDauStt(p, 1), GymKiem.chonSoTay(p, 0));

            // menu So tay -> Thách đấu Gym: Gym cua buoc len dau, dong 0 = Pewter
            tu = p.goi.size();
            GymKiem.moMenuGym(p);
            final String mg = p.chu(-30, tu);
            Kiem.dung("So tay -> Thách đấu Gym: dong dau '» Gym Pewter (Golem) - cấp 12'", mg != null && mg.startsWith("» Gym Pewter (Golem) - cấp 12\u0001"));

            // Huỷ o so Nhiem vu (client 0.9.14): khong huy, hoi thach dau
            tu = p.goi.size();
            MucTieuNhiemVuGame.khiGoiCuaAi(p, 2, MucTieuNhiemVuGame.idCuaAi("CA-03.2"));
            hop = p.chu(-32, tu);
            Kiem.dung("Huỷ the cua ai ha Gym: hop 'không huỷ được' + hoi thach dau", hop != null && hop.contains("Cửa ải không huỷ được")
                    && hop.contains("Thách đấu Trưởng Gym Pewter (Golem) ngay?") && t.containsKey("CA-03.2"));
            KhoaBanDoGame.khiTraLoi(p, false);

            // chi tiet nhiem vu (so -64, server dien) - cach toi Gym + noi danh, khong 'Chỉ tính quái'
            final String mt = GymKiem.moTaCuaAi(p);
            Kiem.dung("chi tiet cua ai ha Gym: muc tat + duong day du + Gym danh o dau", mt != null
                    && mt.startsWith("Mục tiêu: Hạ Trưởng Gym Pewter: Golem (0/1).") && mt.contains("«Thách đấu Gym Pewter (Golem)»")
                    && mt.contains("Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Pewter") && mt.contains("không dịch chuyển")
                    && mt.endsWith("Nhận thưởng ở: Quầy đổi thưởng → Nhiệm vụ của tôi → Đang làm."));
            Kiem.sai("chi tiet cua ai ha Gym: khong ghi 'Chỉ tính quái hạ ở đúng bản đồ'", mt != null && mt.contains("Chỉ tính quái"));
            Kiem.bang("chi tiet cua ai ha Gym: duong dung cay menu", "[]", DuongMenu.kiem(mt, cay).toString());

            // xong buoc: muc tat bien mat, chon lai (menu cu) -> bao, khong hop
            t.get("CA-03.2")[1] = 1;
            tu = p.goi.size();
            MenuDoiThuong.chon(p, 0, null);
            Kiem.dung("buoc xong, bam muc tat cu: bao, khong hop", p.chu(-32, tu) == null && p.chu(37, tu) != null);
            MenuDoiThuong.mo(p);
            Kiem.dung("buoc xong: muc dau lai la Nhiệm vụ của tôi", p.chu(-30, tu).startsWith(DuongMenu.NHIEM_VU + "\u0001"));

            // ma nguon: moi duong deu qua thachDauStt
            final String gl = doc(Paths.get("src/real/GymLauDai.java"));
            Kiem.bang("GymLauDai: mot cho trieu hoi boss Gym", Integer.valueOf(1),
                    Integer.valueOf(gl.split(Pattern.quote("BossLauDai.trieuHoi("), -1).length - 1));
            Kiem.dung("So tay MENU_GYM -> GymLauDai.chon", doc(Paths.get("src/real/SoTayLauDai.java")).contains("bao(p, GymLauDai.chon(p, idOption));"));
            Kiem.dung("hop 95 Gym -> GymLauDai.thachDauStt", doc(Paths.get("src/real/KhoaBanDoGame.java")).contains("SoTayLauDai.bao(p, GymLauDai.thachDauStt(p, h.gym));"));
            Kiem.dung("-90 -> MenuDoiThuong (mo + chon)", doc(Paths.get("src/real/cmd/RequestNPCInfoHandler.java")).contains("real.MenuDoiThuong.mo(p);")
                    && doc(Paths.get("src/real/MapDoMenu.java")).contains("MenuDoiThuong.chon(player, idOptionMenu, map);"));
            Kiem.dung("roi game: bo thu tu menu", doc(Paths.get("src/real/CharManager.java")).contains("MenuDoiThuong.khiThoat(p);"));
        } finally {
            GymKiem.datGym(gymCu);
            GymKiem.datMau(mauCu);
            GymKiem.datTienDo(p, null);
            GymKiem.datHoSo(p, null);
            KhoaBanDoGame.xoaHoi(p.charDBID);
            MenuDoiThuong.khiThoat(p);
            com.htbb.pokemon.LauDai.dat(lauDaiCu);
            com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, null, false);
        }
    }
}
