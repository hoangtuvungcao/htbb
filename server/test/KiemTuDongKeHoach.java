import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.HoiSinh;
import com.htbb.pokemon.TuDong;
import com.htbb.pokemon.TuDongKeHoach;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Bo nao Tu dong danh (com.htbb.pokemon.TuDongKeHoach, 19/10/2026): may chu chon muc tieu gan nhat THEO DUONG DI
 * (khong xuyen tuong), dung khoang cap, tranh boss / qua trau, chieu theo khac he + PP + hoi chieu, thuoc theo nguong
 * (binh vua du), rut lui / dung khi HP thap, nhat do trong ban kinh, dung khi tui day, tranh nguoi bat PK; cau hinh
 * tu_dong.* trong danh muc trung tam; hoi sinh khong tu dung vat pham tra phi khi chua dong y; cac cho noi vao game
 * (TuDongGame C_KE, goi ke hoach, bao ly do, vung chia region) va client (TuDong.java dung giao thuc 2).
 */
public final class KiemTuDongKeHoach {

    private KiemTuDongKeHoach() {
    }

    public static void chay() {
        Kiem.nhom("Tu dong danh - bo nao may chu (TuDongKeHoach): muc tieu, chieu, thuoc, rut lui, nhat, dung");
        try {
            mucTieu();
            duongDi();
            chieu();
            chieuGan();
            vuKhiHong();
            thuoc();
            hetPP();
            hpThap();
            nhatDo();
            dung();
            cauHinh();
            hoiSinhTraPhi();
            noiVaoGame();
            client();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static TuDongKeHoach.TinhHinh th() {
        final TuDongKeHoach.TinhHinh t = new TuDongKeHoach.TinhHinh();
        t.x = 400;
        t.y = 400;
        t.neoX = 400;
        t.neoY = 400;
        t.banKinh = 240;
        t.cap = 10;
        t.hp = 1000;
        t.maxhp = 1000;
        t.mp = 100;
        t.maxmp = 100;
        t.he1 = HePokemon.NUOC;
        t.thuoc = new int[200];
        t.chieu.add(new TuDongKeHoach.Chieu(0, HePokemon.THUONG, 100, 0, 0L));
        return t;
    }

    static TuDongKeHoach.Quai q(final int id, final int x, final int y, final int cap) {
        final TuDongKeHoach.Quai q = new TuDongKeHoach.Quai(id, x, y, cap, 100, 100);
        q.he1 = HePokemon.THUONG;
        return q;
    }

    static TuDongKeHoach.KeHoach lap(final TuDongKeHoach.TinhHinh t) {
        return TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
    }

    static void mucTieu() {
        TuDongKeHoach.TinhHinh t = th();
        t.quai.add(q(1, 560, 400, 10));
        t.quai.add(q(2, 450, 400, 10));
        t.quai.add(q(3, 300, 400, 10));
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("danh con GAN nhat", TuDongKeHoach.HD_DANH, k.hd);
        Kiem.bang("muc tieu id 2 (50px)", 2, k.id);
        Kiem.bang("toa do muc tieu gui client", 450, k.x);
        t.mucTieuCu = 1;
        Kiem.bang("dang danh con 1 (con hop le): giu muc tieu, khong nhay", 1, lap(t).id);
        t.mucTieuCu = -1;

        t = th();
        t.quai.add(q(1, 420, 400, 16));
        t.quai.add(q(2, 520, 400, 15));
        Kiem.bang("cao hon 6 cap (chenh_cap_tren=5) bi bo, lay con cao hon 5 cap", 2, lap(t).id);
        t = th();
        t.quai.add(q(1, 420, 400, 30));
        k = lap(t);
        Kiem.bang("chi co Pokemon qua manh: CHO", TuDongKeHoach.HD_CHO, k.hd);
        Kiem.dung("ly do noi ro manh hon qua nhieu", k.ly.contains("mạnh hơn"));
        final TuDongKeHoach.CauHinh c = new TuDongKeHoach.CauHinh();
        c.chenhCapTren = -1;
        Kiem.bang("chenh_cap_tren=-1: khong gioi han", 1, TuDongKeHoach.lap(c, t).id);
        c.chenhCapTren = 5;
        c.chenhCapDuoi = 3;
        t = th();
        t.quai.add(q(1, 420, 400, 2));
        t.quai.add(q(2, 500, 400, 8));
        Kiem.bang("chenh_cap_duoi=3: bo con thap hon 3 cap", 2, TuDongKeHoach.lap(c, t).id);

        t = th();
        TuDongKeHoach.Quai boss = q(1, 420, 400, 10);
        boss.boss = true;
        t.quai.add(boss);
        t.quai.add(q(2, 600, 400, 10));
        Kiem.bang("boss bi bo (danh_boss=0)", 2, lap(t).id);
        t.quai.remove(1);
        k = lap(t);
        Kiem.dung("chi co boss: cho + ly do boss", k.hd == TuDongKeHoach.HD_CHO && k.ly.contains("boss"));
        final TuDongKeHoach.CauHinh cb = new TuDongKeHoach.CauHinh();
        cb.danhBoss = true;
        Kiem.bang("danh_boss=1: danh boss", 1, TuDongKeHoach.lap(cb, t).id);

        t = th();
        TuDongKeHoach.Quai trau = q(1, 420, 400, 10);
        trau.maxhp = 6000;
        t.quai.add(trau);
        t.quai.add(q(2, 600, 400, 10));
        Kiem.bang("HP toi da > 500% minh: bo (hp_quai_toi_da_pt)", 2, lap(t).id);

        t = th();
        t.quai.add(q(1, 400 + 300, 400, 10));
        k = lap(t);
        Kiem.bang("ngoai ban kinh vung (240): khong danh", TuDongKeHoach.HD_CHO, k.hd);
        Kiem.dung("ly do cho noi so o vung", k.ly.contains("15 ô"));

        t = th();
        TuDongKeHoach.Quai xe = q(1, 420, 400, 10);
        xe.cuaNguoiKhac = true;
        t.quai.add(xe);
        TuDongKeHoach.Quai chet = q(2, 430, 400, 10);
        chet.hp = 0;
        t.quai.add(chet);
        Kiem.bang("bo quai cua nguoi khac (xe giao hang) + quai da guc", TuDongKeHoach.HD_CHO, lap(t).hd);

        t = th();
        t.x = 600;
        k = lap(t);
        Kiem.bang("khong co quai, dang xa diem bat dau: VE", TuDongKeHoach.HD_VE, k.hd);
        Kiem.bang("ve dung diem bat dau", 400, k.x);
    }

    static void duongDi() {
        // tuong doc o cot 27 (x 432..447) tu hang 0..39, chi chua lo o hang 40 tro xuong
        final TuDongKeHoach.TinhHinh t = th();
        t.diDuoc = (x, y) -> !(x / 16 == 27 && y / 16 < 40);
        t.quai.add(q(1, 460, 400, 10)); // sau tuong: chi cach 60px nhung phai vong xa
        t.quai.add(q(2, 400, 520, 10)); // 120px, duong thang
        final TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("gan nhat THEO DUONG DI: con 2 (con 1 cach tuong)", 2, k.id);
        final TuDongKeHoach.TinhHinh t2 = th();
        t2.diDuoc = (x, y) -> Math.abs(x - 400) < 40 && Math.abs(y - 400) < 40; // bi nhot trong o 5x5
        t2.quai.add(q(1, 520, 400, 10));
        final TuDongKeHoach.KeHoach k2 = lap(t2);
        Kiem.bang("khong toi duoc: cho", TuDongKeHoach.HD_CHO, k2.hd);
        Kiem.dung("ly do: bi chan", k2.ly.contains("chắn"));
    }

    static void chieu() {
        TuDongKeHoach.TinhHinh t = th();
        t.chieu.clear();
        t.chieu.add(new TuDongKeHoach.Chieu(0, HePokemon.THUONG, 100, 0, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(1, HePokemon.NUOC, 150, 5, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(2, HePokemon.CO, 200, 8, 0L));
        final TuDongKeHoach.Quai lua = q(1, 420, 400, 10);
        lua.he1 = HePokemon.LUA;
        t.quai.add(lua);
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("quai he Lua: chon chieu Nuoc (khac he + STAB) hon chieu Co manh hon", 1, k.so);
        Kiem.dung("ghi nhan rat hieu qua", k.heSo >= 150);
        final TuDongKeHoach.CauHinh c = new TuDongKeHoach.CauHinh();
        c.chonChieuTheoHe = false;
        Kiem.bang("chon_chieu_theo_he=0: chi theo sat thuong", 2, TuDongKeHoach.lap(c, t).so);
        t.chieu.get(1).conHoi = 500L;
        Kiem.bang("chieu Nuoc dang hoi: lay chieu khac (Co, sat thuong cao)", 2, lap(t).so);
        t.mp = 4;
        Kiem.bang("thieu PP cho chieu 1 & 2: chieu 0", 0, lap(t).so);
        t.chieu.get(0).conHoi = 300L;
        k = lap(t);
        Kiem.bang("khong chieu nao san: van DANH (tien lai) voi chieu -1", -1, k.so);
        Kiem.bang("... hanh dong DANH", TuDongKeHoach.HD_DANH, k.hd);
        t = th();
        t.chieu.clear();
        final TuDongKeHoach.Quai thuong = q(1, 420, 400, 10);
        t.quai.add(thuong);
        t.chieu.add(new TuDongKeHoach.Chieu(0, HePokemon.MA, 100, 0, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(1, HePokemon.THUONG, 90, 0, 0L));
        Kiem.bang("he Ma vao he Thuong (mien nhiem): khong chon", 1, lap(t).so);
    }

    /**
     * 0.9.24 (chu game: "jar bat auto khong dung cac chieu da gan a?"): bo nao truoc chon trong MOI chieu da hoc (chieu
     * 2 he Co manh nhat du nguoi choi chi gan chieu 1, 3 vao trang chieu). Nay chi chieu da gan.
     */
    static void chieuGan() {
        TuDongKeHoach.TinhHinh t = th();
        t.chieu.clear();
        t.chieu.add(new TuDongKeHoach.Chieu(0, HePokemon.THUONG, 100, 0, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(1, HePokemon.NUOC, 90, 5, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(2, HePokemon.CO, 300, 5, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(3, HePokemon.THUONG, 150, 5, 0L));
        final TuDongKeHoach.Quai qt = q(1, 420, 400, 10);
        t.quai.add(qt);
        Kiem.bang("khong biet chieu gan (client cu, ho so trong): nhu cu - manh nhat da hoc", 2, lap(t).so);
        t.chieuGan = new int[]{1, 3};
        Kiem.bang("trang chieu gan 1, 3: KHONG dung chieu 2 chua gan (manh hon)", 3, lap(t).so);
        qt.he1 = HePokemon.LUA;
        Kiem.bang("trang chieu: khac he chi la uu tien giua chieu DA GAN (Nuoc vao Lua)", 1, lap(t).so);
        qt.he1 = HePokemon.THUONG;
        t.chieu.get(3).conHoi = 400L;
        Kiem.bang("chieu 3 dang hoi: chieu gan con lai (1)", 1, lap(t).so);
        t.chieu.get(1).conHoi = 400L;
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("moi chieu gan dang hoi: danh thuong (chieu 0), khong chieu 2", 0, k.so);
        Kiem.bang("... van DANH", TuDongKeHoach.HD_DANH, k.hd);
        t.chieu.get(0).conHoi = 300L;
        Kiem.bang("ca danh thuong dang hoi: cho (-1), khong dung chieu 2 chua gan", -1, lap(t).so);
        t.chieu.get(0).conHoi = 0L;
        t.chieu.get(1).conHoi = 0L;
        t.chieu.get(3).conHoi = 0L;
        t.mp = 3;
        Kiem.bang("thieu PP cho moi chieu gan: danh thuong", 0, lap(t).so);
        t.mp = 100;
        // "Thu tu chieu" tu dat: dung dung thu tu
        t.thuTuNghiem = true;
        Kiem.bang("Thu tu chieu 1 -> 3: chieu 1 truoc du chieu 3 manh hon", 1, lap(t).so);
        t.chieu.get(1).conHoi = 400L;
        Kiem.bang("Thu tu: chieu 1 hoi -> chieu 3", 3, lap(t).so);
        t.chieu.get(1).conHoi = 0L;
        t.chieu.get(1).he = HePokemon.MA;
        Kiem.bang("Thu tu: chieu 1 he Ma mien nhiem vao Thuong -> bo qua, chieu 3", 3, lap(t).so);
        t.chieu.get(3).conHoi = 400L;
        Kiem.bang("Thu tu: chi con chieu mien nhiem -> van dung (khong dung im)", 1, lap(t).so);
        // chieu gan da quen / la buff (khong co trong t.chieu): coi nhu chua gan -> moi chieu da hoc
        t = th();
        t.chieu.add(new TuDongKeHoach.Chieu(2, HePokemon.CO, 300, 5, 0L));
        t.quai.add(q(1, 420, 400, 10));
        t.chieuGan = new int[]{7};
        Kiem.bang("chieu gan khong con hoc: moi chieu da hoc", 2, lap(t).so);
        t.chieuGan = new int[0];
        Kiem.bang("client gui danh sach rong (chua gan chieu danh nao): moi chieu da hoc", 2, lap(t).so);
        // PP thuoc: chi theo chieu se dung
        t = th();
        t.chieu.add(new TuDongKeHoach.Chieu(1, HePokemon.NUOC, 120, 5, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(2, HePokemon.CO, 300, 40, 0L));
        t.chieuGan = new int[]{2};
        Kiem.bang("chieu se dung = chi chieu da gan", 1, TuDongKeHoach.chieuDung(t).size());
        Kiem.bang("... la chieu 2 (40 PP), khong chieu 1 chua gan", 40, TuDongKeHoach.chieuDung(t).get(0).pp);
        // giai trang chieu trong ho so (NgoaiHinhGame, 5 o x 12 bit)
        final long trang = o(0, 2, 0, false) | o(1, 2, 5, true) | o(2, 2, 0, false) | o(3, 2, 3, false) | o(4, 3, 4, false);
        Kiem.dung("chieuTrongTrang: chieu 0, 3 (bo trung, bo o bo tro, bo thuoc)",
                java.util.Arrays.equals(new int[]{0, 3}, TuDongKeHoach.chieuTrongTrang(trang)));
        Kiem.bang("chieuTrongTrang: trang trong", 0, TuDongKeHoach.chieuTrongTrang(0L).length);
        // noi vao game + client
        final String g = KiemTuDong.doc("src/real/TuDongGame.java");
        Kiem.dung("C_KE doc danh sach chieu gan", g.contains("if (loai == TuDong.C_KE) {\n                docChieuGan(ph, message.dis);"));
        Kiem.dung("client cu: doc trang chieu dang dung trong ho so", g.contains("t.chieuGan = chieuTrangHoSo(p);"));
        Kiem.bang("DS_CHIEU = 1", 1, TuDong.DS_CHIEU);
        final String c = KiemTuDong.doc("../client_jar/shared/mod/game/render/screen/TuDong.java");
        if (!c.isEmpty()) {
            Kiem.dung("client gui chieu gan kem lenh 4", c.contains("} else if (loai == 4) {") && c.contains("byte[] ds = chieuGan(mc);"));
            Kiem.dung("client: may chu cu chon chieu khong gan -> chieu gan cua minh", c.contains("if (sk != 0 && !coTrong(chieuGan(mc), sk)) {"));
        }
    }

    /**
     * 31/10/2026 (chu game: "do vat cam tay bi hong, sau do thoat game vao lai thi lai treo binh thuong"): (1) dang nhap
     * lai KHONG duoc sua do mien phi - noi nap goi setTemplate lan hai sau initInfoFromDB tung dat lai do ben toi da;
     * (2) vu khi hong: bo nao dung + noi ro, bat Tu dong bi tu choi kem ly do, khong dung im.
     */
    static void vuKhiHong() {
        TuDongKeHoach.TinhHinh t = th();
        t.chieu.add(new TuDongKeHoach.Chieu(1, HePokemon.NUOC, 150, 5, 0L));
        t.quai.add(q(1, 420, 400, 10));
        Kiem.bang("vu khi tot: danh", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.vuKhiHong = true;
        final TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("vu khi hong: DUNG (khong ra lenh danh bi tu choi)", TuDongKeHoach.HD_DUNG, k.hd);
        Kiem.bang("... ma ly do TAT_VU_KHI_HONG", TuDong.TAT_VU_KHI_HONG, k.lyDoTat);
        Kiem.dung("... cau bao noi Tho ren: " + k.ly, k.ly.contains("hỏng") && k.ly.contains("Thợ rèn"));
        final TuDong.NguCanh n = new TuDong.NguCanh();
        n.vuKhiHong = true;
        n.map = 1;
        final TuDong.CauHinh ch = new TuDong.CauHinh();
        ch.bat = true;
        final String loi = TuDong.kiemBat(ch, new TuDong.Phien(), n, 20261031);
        Kiem.dung("bat Tu dong khi vu khi hong: tu choi + noi Tho ren: " + loi, loi != null && loi.contains("Thợ rèn"));
        // do ben sau dang nhap lai (real.Item.doBenSauNap): mau 10 (mDurable toi da 100)
        Kiem.dung("hong (0/0) van hong sau nap lai", java.util.Arrays.equals(new int[]{0, 0}, real.Item.doBenSauNap(0, 0, 10)));
        Kiem.dung("mDurable 0, durable le: van hong", real.Item.doBenSauNap(3, 0, 10)[1] == 0);
        Kiem.dung("con 37/100 -> giu 37 (durable 4)", java.util.Arrays.equals(new int[]{4, 37}, real.Item.doBenSauNap(4, 37, 10)));
        Kiem.dung("day giu day", java.util.Arrays.equals(new int[]{10, 100}, real.Item.doBenSauNap(10, 100, 10)));
        Kiem.dung("vuot toi da cua mau (mau doi) -> kep", java.util.Arrays.equals(new int[]{10, 100}, real.Item.doBenSauNap(30, 300, 10)));
        Kiem.dung("chua tung luu (-1) -> day", java.util.Arrays.equals(new int[]{10, 100}, real.Item.doBenSauNap(10, -1, 10)));
        Kiem.dung("mau khong co do ben -> nhu mau", java.util.Arrays.equals(new int[]{0, 0}, real.Item.doBenSauNap(5, 50, 0)));
        for (final String f : new String[]{"src/real/CharTrangBi.java", "src/real/CharVongLap.java", "src/data/Pet.java"}) {
            final String s = KiemTuDong.doc(f);
            Kiem.dung(f + ": nap do giu do ben (khong setTemplate lai sau initInfoFromDB)",
                    s.contains("napLaiTemplateGiuDoBen()") && !s.contains("item.setTemplate(item.tempateID, item.clazz, item.clazz, item.tempateID)"));
        }
        final String g = KiemTuDong.doc("src/real/TuDongGame.java");
        Kiem.dung("TuDongGame: vu khi hong -> tat ca che do client tu chon", g.contains("lyDo = TuDong.TAT_VU_KHI_HONG;"));
        Kiem.dung("TuDongGame: bo nao + bat biet vu khi hong", g.contains("t.vuKhiHong = vuKhiHong(p);") && g.contains("n.vuKhiHong = vuKhiHong(p);"));
    }

    /** Ma 12 bit mot o trang chieu (khop client TrangChieu.ma): kieu = quickslotType + 1 (2 = chieu, 3 = thuoc). */
    static long o(final int i, final int kieu, final int gt, final boolean buff) {
        return ((long) ((kieu & 7) << 9 | (gt & 255) << 1 | (buff ? 1 : 0))) << (12 * i);
    }

    static void thuoc() {
        TuDongKeHoach.TinhHinh t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.hp = 400;
        Kiem.bang("HP 40% < 50% nhung khong co thuoc: van danh", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.thuoc[1] = 5;
        t.thuoc[3] = 2;
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("co thuoc: uong", TuDongKeHoach.HD_THUOC, k.hd);
        Kiem.bang("thieu 600: binh 3 (1000) - binh 1 (80) khong du", 3, k.so);
        t.hp = 950;
        t.maxhp = 2000;
        t.thuoc[2] = 1;
        Kiem.bang("thieu 1050 HP: binh nho nhat du bu = 21? khong co -> binh manh nhat dang co (3)", 3, lap(t).so);
        t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.thuoc[1] = 3;
        t.thuoc[2] = 3;
        t.hp = 920;
        t.nguongHpNguoi = 95;
        Kiem.bang("nguoi chon nguong > 90 bi kep 90: 92% khong uong", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.hp = 750;
        k = lap(t);
        Kiem.bang("thieu 250: binh 2 (300) vua du, khong phi binh lon", 2, k.so);
        t.nguongHpNguoi = 0;
        Kiem.bang("nguoi choi tat thuoc HP (0): khong uong", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.nguongHpNguoi = -1;
        t.hp = 400;
        t.thuocConHoi = new long[200];
        t.thuocConHoi[1] = 200L;
        t.thuocConHoi[2] = 200L;
        Kiem.bang("binh dang hoi: khong uong, danh tiep", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.thuocConHoi = null;
        t.tuLanThuocMs = 500L;
        Kiem.bang("vua uong < 1,5 giay: cho HP cap nhat, khong uong lien (duoi nhip thuoc 4/5s)", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.tuLanThuocMs = Long.MAX_VALUE;
        Kiem.dung("nhip thuoc cua bo nao (1 lan / 1,5 giay) khong bao gio vuot tu_dong.nhip_thuoc 4/5s",
                TuDong.NHIP_THUOC_MAC_DINH.ms / TuDongKeHoach.KHOANG_THUOC_MS + 1 <= TuDong.NHIP_THUOC_MAC_DINH.so);
        final TuDongKeHoach.CauHinh c = new TuDongKeHoach.CauHinh();
        c.tuDungThuoc = false;
        Kiem.bang("tu_dong.tu_dung_thuoc=0: khong uong", TuDongKeHoach.HD_DANH, TuDongKeHoach.lap(c, t).hd);
        c.tuDungThuoc = true;
        c.nguongThuocHp = 0;
        Kiem.bang("tu_dong.nguong_thuoc_hp=0: may chu tat thuoc HP", TuDongKeHoach.HD_DANH, TuDongKeHoach.lap(c, t).hd);
        t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.mp = 10;
        t.thuoc[5] = 2;
        k = lap(t);
        Kiem.bang("PP 10% < 30%: uong thuoc PP", TuDongKeHoach.HD_THUOC, k.hd);
        Kiem.bang("binh PP 5 (600)", 5, k.so);
    }

    /** 0.9.22: "het PP khong tu su dung khi dang dung auto" (HP 5731/8752, PP 1/3769, 678 binh PP 24). */
    static void hetPP() {
        TuDongKeHoach.TinhHinh t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.hp = 5731;
        t.maxhp = 8752;
        t.mp = 1;
        t.maxmp = 3769;
        t.chieu.add(new TuDongKeHoach.Chieu(1, HePokemon.THUONG, 150, 3, 0L));
        t.chieu.add(new TuDongKeHoach.Chieu(2, HePokemon.THUONG, 200, 6, 0L));
        t.thuoc[24] = 678;
        t.thuoc[94] = 197;
        t.nguongHpNguoi = 50;
        t.nguongMpNguoi = 30;
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("anh chu game: PP 1/3769 < 30% -> uong binh PP", TuDongKeHoach.HD_THUOC, k.hd);
        Kiem.bang("anh chu game: binh PP 24 (Elixir toi da)", 24, k.so);
        t.nguongMpNguoi = 0;
        k = lap(t);
        Kiem.bang("nguong PP nguoi choi = Tat nhung PP < chieu re nhat (3): van uong", TuDongKeHoach.HD_THUOC, k.hd);
        Kiem.bang("nguong PP Tat: binh 24", 24, k.so);
        t.mp = 3;
        k = lap(t);
        Kiem.bang("nguong PP Tat, PP du chieu re nhat: khong uong, danh", TuDongKeHoach.HD_DANH, k.hd);
        final TuDongKeHoach.CauHinh c = new TuDongKeHoach.CauHinh();
        c.nguongThuocMp = 0;
        t.mp = 1;
        Kiem.bang("tu_dong.nguong_thuoc_mp=0 (may chu tat thuoc PP): khong uong ca khi het PP", TuDongKeHoach.HD_DANH, TuDongKeHoach.lap(c, t).hd);
        // HP duoi nguong nhung het binh HP: khong chan nhanh PP
        t.nguongMpNguoi = 30;
        t.hp = 1000;
        t.thuoc[94] = 0;
        k = lap(t);
        Kiem.bang("HP 11% het binh HP: van uong binh PP", TuDongKeHoach.HD_THUOC, k.hd);
        Kiem.bang("HP het binh: binh PP 24", 24, k.so);
        // het binh PP: bao ro, khong dung im
        t.hp = 5731;
        t.thuoc[24] = 0;
        k = lap(t);
        Kiem.bang("het binh PP: van danh bang chieu 0 PP", TuDongKeHoach.HD_DANH, k.hd);
        Kiem.bang("het binh PP: chieu 0", 0, k.so);
        Kiem.dung("het binh PP: trang thai bao 'het binh PP' (" + k.ly + ")", k.ly.startsWith("hết bình PP"));
        t.thuoc[24] = 678;
        t.thuocConHoi = new long[200];
        t.thuocConHoi[24] = 300L;
        k = lap(t);
        Kiem.bang("binh PP dang hoi (400 ms): khong uong", TuDongKeHoach.HD_DANH, k.hd);
        Kiem.dung("binh dang hoi: khong bao het binh (" + k.ly + ")", !k.ly.startsWith("hết bình PP"));
        t.thuocConHoi = null;
        t.tuLanThuocMs = 500L;
        Kiem.bang("vua uong < 1,5 giay: khong uong lien", TuDongKeHoach.HD_DANH, lap(t).hd);
    }

    static void hpThap() {
        TuDongKeHoach.TinhHinh t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.x = 520;
        t.hp = 200;
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("HP 20% < 25% het thuoc: RUT LUI", TuDongKeHoach.HD_VE, k.hd);
        Kiem.dung("ghi nho dang hoi phuc", k.hoiPhuc);
        Kiem.bang("rut ve diem bat dau", 400, k.x);
        t.dangHoiPhuc = true;
        t.x = 400;
        t.hp = 600;
        k = lap(t);
        Kiem.bang("dang hoi phuc 60% < 80%: dung cho nghi", TuDongKeHoach.HD_CHO, k.hd);
        Kiem.dung("van dang hoi phuc", k.hoiPhuc);
        t.hp = 820;
        k = lap(t);
        Kiem.bang("hoi phuc 82% >= 80%: danh tiep", TuDongKeHoach.HD_DANH, k.hd);
        Kiem.sai("het hoi phuc", k.hoiPhuc);
        t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.hp = 200;
        final TuDongKeHoach.CauHinh c = new TuDongKeHoach.CauHinh();
        c.hpThapDung = true;
        k = TuDongKeHoach.lap(c, t);
        Kiem.bang("hp_thap_dung=1: DUNG", TuDongKeHoach.HD_DUNG, k.hd);
        Kiem.bang("ly do HP thap", TuDong.TAT_HP_THAP, k.lyDoTat);
        t.veHoiPhucNguoi = false;
        Kiem.bang("nguoi choi tat Ve hoi phuc: DUNG", TuDongKeHoach.HD_DUNG, lap(t).hd);
        t.veHoiPhucNguoi = true;
        t.thuoc[1] = 1;
        Kiem.bang("HP thap nhung con thuoc: uong truoc", TuDongKeHoach.HD_THUOC, lap(t).hd);
    }

    static void nhatDo() {
        TuDongKeHoach.TinhHinh t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.doRoi.add(new TuDongKeHoach.DoRoi(TuDongKeHoach.DO_THUOC, 7, 460, 400, 0));
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("xu roi trong 96px: NHAT", TuDongKeHoach.HD_NHAT, k.hd);
        Kiem.bang("id do", 7, k.id);
        Kiem.bang("loai do (cat 4) gui client", TuDongKeHoach.DO_THUOC, k.so);
        t.doRoi.clear();
        t.doRoi.add(new TuDongKeHoach.DoRoi(TuDongKeHoach.DO_THUOC, 7, 560, 400, 0));
        Kiem.bang("xa hon ban kinh nhat: danh tiep", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.doRoi.clear();
        final TuDongKeHoach.DoRoi cuaNguoi = new TuDongKeHoach.DoRoi(TuDongKeHoach.DO_THUOC, 7, 420, 400, 0);
        cuaNguoi.nhatDuoc = false;
        t.doRoi.add(cuaNguoi);
        Kiem.bang("do cua nguoi khac: khong nhat", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.doRoi.clear();
        t.doRoi.add(new TuDongKeHoach.DoRoi(TuDongKeHoach.DO_VAT_PHAM, 8, 420, 400, 0));
        Kiem.bang("vat pham, kieu nhat 1 (thuoc + xu): khong nhat", TuDongKeHoach.HD_DANH, lap(t).hd);
        t.nhatNguoi = TuDongKeHoach.NHAT_TAT_CA;
        Kiem.bang("kieu nhat 3: nhat vat pham", TuDongKeHoach.HD_NHAT, lap(t).hd);
        t.nhatNguoi = TuDongKeHoach.NHAT_KHONG;
        Kiem.bang("kieu nhat 0: khong nhat", TuDongKeHoach.HD_DANH, lap(t).hd);
        final TuDongKeHoach.CauHinh c = new TuDongKeHoach.CauHinh();
        c.nhatBanKinh = 0;
        t.nhatNguoi = TuDongKeHoach.NHAT_TAT_CA;
        Kiem.bang("nhat_ban_kinh=0: khong nhat", TuDongKeHoach.HD_DANH, TuDongKeHoach.lap(c, t).hd);
    }

    static void dung() {
        TuDongKeHoach.TinhHinh t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.tuiDay = true;
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("tui day: DUNG", TuDongKeHoach.HD_DUNG, k.hd);
        Kiem.bang("ly do tui day", TuDong.TAT_DAY_TUI, k.lyDoTat);
        Kiem.dung("cau bao tui day", k.ly.contains("hành trang"));
        final TuDongKeHoach.CauHinh c = new TuDongKeHoach.CauHinh();
        c.dungKhiDayTui = false;
        Kiem.bang("dung_khi_day_tui=0: danh tiep", TuDongKeHoach.HD_DANH, TuDongKeHoach.lap(c, t).hd);
        t = th();
        t.quai.add(q(1, 420, 400, 10));
        t.nguoiPkGan = 1;
        t.x = 480;
        k = lap(t);
        Kiem.bang("nguoi bat PK gan: lui ve diem bat dau", TuDongKeHoach.HD_VE, k.hd);
        Kiem.dung("ly do PK", k.ly.contains("PK"));
        c.dungKhiDayTui = true;
        c.tranhPkPx = 0;
        Kiem.bang("tranh_pk_px=0: danh tiep", TuDongKeHoach.HD_DANH, TuDongKeHoach.lap(c, t).hd);
        Kiem.dung("ly do tat moi co cau rieng", !TuDong.lyDo(TuDong.TAT_TU_DI).equals(TuDong.lyDo(0))
                && !TuDong.lyDo(TuDong.TAT_KHU_CAM).equals(TuDong.lyDo(0)) && !TuDong.lyDo(TuDong.TAT_HP_THAP).equals(TuDong.lyDo(0)));
        // cap toi thieu / ban do cam: bao ro ly do
        final TuDong.CauHinh ch = new TuDong.CauHinh();
        final TuDong.NguCanh n = new TuDong.NguCanh();
        n.map = 12;
        n.cap = 3;
        ch.capToiThieu = 5;
        final String loi = TuDong.kiemBat(ch, new TuDong.Phien(), n, 20261019);
        Kiem.dung("cap_toi_thieu=5, cap 3: tu choi noi ro cap can", loi != null && loi.contains("cấp 5"));
        ch.capToiThieu = 0;
        ch.mapCam = new int[]{12};
        Kiem.bang("map_cam co map 12: tu choi", TuDong.lyDo(TuDong.TAT_KHU_CAM), TuDong.kiemBat(ch, new TuDong.Phien(), n, 20261019));
        ch.mapCam = new int[0];
        Kiem.bang("binh thuong: duoc bat", null, TuDong.kiemBat(ch, new TuDong.Phien(), n, 20261019));
        // bao ly do khong lap
        final TuDong.Phien p = new TuDong.Phien();
        Kiem.dung("bao ly do lan dau", TuDong.nenBao(p, "chờ", 1_000_000L));
        Kiem.sai("cung ly do 5 giay sau: khong bao lai", TuDong.nenBao(p, "chờ", 1_005_000L));
        Kiem.sai("ly do khac nhung chua qua 20 giay: khong", TuDong.nenBao(p, "khác", 1_010_000L));
        Kiem.dung("ly do khac sau 20 giay: bao", TuDong.nenBao(p, "khác", 1_021_000L));
    }

    static void cauHinh() {
        final String[] khoa = {TuDong.K_KE_HOACH, TuDong.K_CAP_TOI_THIEU, TuDong.K_CHENH_CAP_TREN, TuDong.K_CHENH_CAP_DUOI,
            TuDong.K_DANH_BOSS, TuDong.K_HP_QUAI_TOI_DA, TuDong.K_CHIEU_THEO_HE, TuDong.K_NGUONG_THUOC_HP,
            TuDong.K_NGUONG_THUOC_MP, TuDong.K_NGUONG_HP_THAP, TuDong.K_HP_THAP_DUNG, TuDong.K_HOI_PHUC_TOI,
            TuDong.K_NHAT_BAN_KINH, TuDong.K_DUNG_DAY_TUI, TuDong.K_DUNG_TU_DI, TuDong.K_TRANH_PK, TuDong.K_MAP_CAM,
            TuDong.K_HOI_SINH_TRA_PHI};
        boolean du = true;
        for (final String k : khoa) {
            du &= CauHinhTrungTam.dinhNghia(k) != null;
        }
        Kiem.dung(khoa.length + " khoa bo nao tu_dong.* co trong danh muc trung tam (cau_hinh_danh_muc)", du);
        CauHinhTrungTam.datChoTest(null, new HashMap<String, String>(), false);
        try {
            final TuDong.CauHinh c = TuDong.cauHinh();
            final TuDongKeHoach.CauHinh m = new TuDongKeHoach.CauHinh();
            Kiem.dung("mac dinh danh muc = mac dinh trong ma: ke_hoach=1, dung_khi_tu_di=1, khong tra phi",
                    c.keHoach && c.dungKhiTuDi && !c.hoiSinhTraPhi && c.capToiThieu == 0 && c.mapCam.length == 0);
            Kiem.dung("mac dinh bo nao khop TuDongKeHoach.CauHinh", c.kh.chenhCapTren == m.chenhCapTren
                    && c.kh.chenhCapDuoi == m.chenhCapDuoi && c.kh.danhBoss == m.danhBoss && c.kh.hpQuaiToiDaPt == m.hpQuaiToiDaPt
                    && c.kh.chonChieuTheoHe == m.chonChieuTheoHe && c.kh.nguongThuocHp == m.nguongThuocHp
                    && c.kh.nguongThuocMp == m.nguongThuocMp && c.kh.nguongHpThap == m.nguongHpThap
                    && c.kh.hpThapDung == m.hpThapDung && c.kh.hoiPhucToi == m.hoiPhucToi && c.kh.nhatBanKinh == m.nhatBanKinh
                    && c.kh.dungKhiDayTui == m.dungKhiDayTui && c.kh.tranhPkPx == m.tranhPkPx);
            Kiem.bang("co gui client mac dinh: tinh nang+thuoc+PvP+chi map+ke hoach+dung khi tu di", 63, c.co());
        } finally {
            CauHinhTrungTam.datChoTest(null, null, false);
        }
        final Map<String, String> db = new HashMap<String, String>();
        db.put(TuDong.K_KE_HOACH, "0");
        db.put(TuDong.K_CHENH_CAP_TREN, "8");
        db.put(TuDong.K_MAP_CAM, "12, 20,");
        db.put(TuDong.K_DUNG_TU_DI, "0");
        db.put(TuDong.K_NHAT_BAN_KINH, "9999");
        // khoa ke hoach / chenh cap la khoa CO DINH (docs/cau-hinh.md): may that bo qua CSDL, nen kiem doc
        // gia tri tren may thu (sv.cho_phep_thu_nghiem=1) - doc CSDL nhu cu.
        CauHinhTrungTam.datChoTest(null, db, true);
        try {
            final TuDong.CauHinh c = TuDong.cauHinh();
            Kiem.sai("doc ke_hoach=0", c.keHoach);
            Kiem.bang("doc chenh_cap_tren", 8, c.kh.chenhCapTren);
            Kiem.dung("doc map_cam (bo phan trong)", c.mapBiCam(12) && c.mapBiCam(20) && c.mapCam.length == 2);
            Kiem.bang("nhat_ban_kinh ngoai khoang danh muc (9999 > 480): bi tu choi, dung mac dinh", TuDongKeHoach.NHAT_BAN_KINH_MAC_DINH, c.kh.nhatBanKinh);
            Kiem.bang("co: khong ke hoach, khong dung khi tu di", 15, c.co());
        } finally {
            CauHinhTrungTam.datChoTest(null, null, false);
        }
        final String dm = DayDu.danhMucJson();
        Kiem.dung("danh muc cau hinh (CSDL) co tu_dong.ke_hoach + tu_dong.hoi_sinh_tra_phi", dm.contains("\"tu_dong.ke_hoach\"") && dm.contains("\"tu_dong.hoi_sinh_tra_phi\""));
        Kiem.dung("CSDL day du co khoa tu_dong.chenh_cap_tren", DayDu.tim("cau_hinh_danh_muc", "khoa", "tu_dong.chenh_cap_tren") != null);
    }

    static void hoiSinhTraPhi() {
        final HoiSinh.TinhHuong t = new HoiSinh.TinhHuong();
        t.conLuotThuoc = true;
        t.soThuoc = 0;
        t.soThuocToiDa = 2;
        Kiem.bang("chua dong y tra phi: KHONG tu dung Thuoc hoi sinh toi da (luong)", HoiSinh.LC_TRUNG_TAM, HoiSinh.chonTuDong(true, t, false));
        Kiem.bang("dong y + may chu cho: dung toi da", HoiSinh.LC_THUOC_TOI_DA, HoiSinh.chonTuDong(true, t, true));
        t.soThuoc = 1;
        Kiem.bang("co Thuoc hoi sinh thuong: dung thuong (khong can dong y tra phi)", HoiSinh.LC_THUOC, HoiSinh.chonTuDong(true, t, false));
        Kiem.dung("game goi chonTuDong co co tra phi", KiemTuDong.doc("src/real/HoiSinhGame.java").contains("HoiSinh.chonTuDong(true, th, TuDongGame.choHoiSinhTraPhi(p))"));
        final String g = KiemTuDong.doc("src/real/TuDongGame.java");
        Kiem.dung("tra phi = may chu cho VA nguoi choi dong y", g.contains("ph.xinHoiSinhTraPhi && TuDong.cauHinh().hoiSinhTraPhi"));
    }

    static void noiVaoGame() {
        final String g = KiemTuDong.doc("src/real/TuDongGame.java");
        Kiem.dung("TuDongGame: C_KE -> keHoach", g.contains("if (loai == TuDong.C_KE) {") && g.contains("keHoach(p, c, ph, bay);"));
        Kiem.dung("ke hoach kiem dieu kien dung truoc (het gio / doi map / ra vung / PvP)", g.contains("if (kiemDangChay(p, c, ph, bay)) {\n            return; // da tat + da bao"));
        Kiem.dung("goi ke hoach -100 byte 1", g.contains("m.dos.writeByte(TuDong.S_KE_HOACH);"));
        Kiem.dung("tu choi bat: bao ly do bang chu tren dau (khong im lang)", g.contains("guiTrangThai(p, c, ph, loi);\n                bao(p, loi);"));
        Kiem.dung("dung giua chung: bao ly do", g.contains("guiTrangThai(p, c, ph, TuDong.lyDo(lyDo));\n        bao(p, TuDong.lyDo(lyDo));"));
        Kiem.dung("ban do chia vung (Region): dem quai trong Region", g.contains("rg.monsters"));
        Kiem.dung("khu PvP them: cho loi dai, me cung, map boss", g.contains("m.isMapChoLoiDai() || m.isvanTienTran() || m.inMapBoss(p)"));
        Kiem.dung("duong di dung Map.canMove", g.contains("return m.canMove(x, y);"));
        Kiem.bang("C_KE = 4", 4, TuDong.C_KE);
        Kiem.bang("giao thuc client moi = 2", 2, TuDong.GIAO_THUC);
    }

    static void client() {
        final String c = KiemTuDong.doc("../client_jar/shared/mod/game/render/screen/TuDong.java");
        if (c.isEmpty()) {
            System.out.println("   (khong co client_jar - bo qua phan client)");
            return;
        }
        Kiem.dung("client gui giao thuc 2 khi xin / bat", c.contains("m.writer().writeByte(GIAO_THUC);"));
        Kiem.dung("client xin ke hoach (lenh 4) khi may chu bat co 16", c.contains("(svCo & CO_KE_HOACH) != 0") && c.contains("gui(4, 0);"));
        Kiem.dung("client doc goi ke hoach byte 1", c.contains("if (r.readByte() == 1) {"));
        // 0.9.5.1+: bao bang thong bao nhanh (toast DungDo) thay hop thoai - van khong im lang
        Kiem.dung("het 6 giay khong phan hoi: bao nguoi choi (khong im lang)", c.contains("game.pokemon.DungDo.bao(thongBao, game.pokemon.DungDo.TIN);"));
        Kiem.dung("tu di chuyen: dung + bao may chu ly do 13", c.contains("gui(2, TAT_TU_DI);") && c.contains("TAT_TU_DI = 13"));
        Kiem.sai("state 3 (Pokemon dang danh tra) khong con bi coi la chet", c.contains("a.state != 3"));
        Kiem.dung("dong y rieng hoi sinh bang do luong", c.contains("tuHoiSinh && hoiSinhLuong ? 1 : 0"));
        Kiem.bang("TAT_TU_DI khop server", TuDong.TAT_TU_DI, 13);
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
