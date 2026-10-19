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
            thuoc();
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
        final String dm = KiemTuDong.doc("../tools/cauhinh/danh_muc.json");
        Kiem.dung("danh_muc.json co tu_dong.ke_hoach + tu_dong.hoi_sinh_tra_phi", dm.contains("\"tu_dong.ke_hoach\"") && dm.contains("\"tu_dong.hoi_sinh_tra_phi\""));
        Kiem.dung("SQL cau hinh trung tam sinh lai (co tu_dong.chenh_cap_tren)", KiemTuDong.doc("../du_lieu/sql_nguon/pokemon_cau_hinh_trung_tam.sql").contains("tu_dong.chenh_cap_tren"));
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
        Kiem.dung("het 6 giay khong phan hoi: hien hop thoai (khong im lang)", c.contains("Canvas.startOKDlg(thongBao);\n        }"));
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
