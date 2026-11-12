import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.SoSatThuong;
import com.htbb.pokemon.TuDong;
import com.htbb.pokemon.TuDongKeHoach;
import real.Char;
import real.GucGame;
import real.HoiSinhGame;

/**
 * Bao loi Zalo ~12/11/2026 (nguoi choi khong ro ten): "luc dang treo danh quai (Tu dong danh), ro rang con nua cay mau ma
 * khong hieu sao chet. Bi nhieu lan roi. Thu o bai quai thap cap hon van bi. Khong phai boss, khong phai khac he."
 * <p>
 * Production 08-12/11 (nhat_ky kiem_toan, nguoi that - bo 400 bot HLV AI): 38 lan guc cua kazuhas4 / ruby / rongngoo, chi
 * 11 lan guc KHI Tu dong dang chay (lyDo=7); phan con lai la guc SAU KHI Tu dong TU DUNG giua tran - nguoi treo dung im giua
 * bay quai dang danh, khong ai uong thuoc:
 * <ul>
 *   <li>lyDo=12 "HP thap va HET THUOC" x4 cua kazuhas4 khi con 900+ binh 94 - bo nao chan MOI binh 1,5 giay sau lenh thuoc
 *       (ca binh PP) nen HP &lt; 25% trong luc cho la roi xuong "het thuoc" -> DUNG; guc 13 / 144 giay sau;</li>
 *   <li>lyDo=15 vu khi hong (don van ra 30% tu 31/10) -> guc 40 / 120 / 250 giay sau; lyDo=5 ra vung 64 px (duoi theo con
 *       quai chay ra mep vung) -> guc 24 / 124 / 146 giay sau;</li>
 *   <li>binh HP bi binh PP gianh nhip (may chu + client tu uong PP chung mot moc 1,5 giay) -> binh HP tre 1,5-3 giay.</li>
 * </ul>
 * Kiem: bo nao moi (nhip binh theo binh THAT, HP truoc PP, khong "het thuoc" khi con binh, dung an toan, ra vung thi ve), so
 * sat thuong + dong [GUC] + cau "Guc vi ...", client (lenh binh may chu khong bi chan "HP day", goi quai danh khi chua thay quai
 * van dat HP), bot HLV AI uong thuoc khi treo.
 */
public final class KiemTreoGuc {

    private KiemTreoGuc() {
    }

    public static void chay() {
        Kiem.nhom("Treo con nua mau van guc (12/11/2026): nhip binh HP, het thuoc sai, dung an toan, ra vung, so sat thuong [GUC]");
        try {
            hetThuocSai();
            hpTruocPp();
            nhipBinh5Giay();
            dungAnToan();
            raVung();
            soSatThuong();
            gucThat();
            quaiDanhGhiSo();
            noiVaoGame();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static TuDongKeHoach.TinhHinh th() {
        final TuDongKeHoach.TinhHinh t = KiemTuDongKeHoach.th();
        t.cap = 84;
        t.maxhp = 20000;
        t.hp = 20000;
        t.maxmp = 4000;
        t.mp = 4000;
        t.nguongHpNguoi = 30;
        t.thuoc[94] = 903; // kazuhas4 11/2026
        t.quai.add(KiemTuDongKeHoach.q(1, 420, 400, 84));
        return t;
    }

    static TuDongKeHoach.KeHoach lap(final TuDongKeHoach.TinhHinh t) {
        return TuDongKeHoach.lap(new TuDongKeHoach.CauHinh(), t);
    }

    /** kazuhas4 lyDo=12 x4: HP < 25% ngay sau lenh thuoc -> "HP thap va het thuoc" -> DUNG khi con 903 binh 94. */
    static void hetThuocSai() {
        TuDongKeHoach.TinhHinh t = th();
        t.hp = 4000; // 20%
        t.tuLanThuocMs = 500L; // lenh binh vua ra, chua toi may chu
        t.tuLanBinhHpMs = 500L;
        t.tuLanBinhMs = 500L;
        t.veHoiPhucNguoi = false;
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.dung("HP 20% vua uong binh, con 903 binh 94, tat Ve hoi phuc: KHONG dung Tu dong (truoc: lyDo=12 'het thuoc') - " + k,
                k.hd != TuDongKeHoach.HD_DUNG);
        Kiem.bang("... danh tiep, buoc sau uong", TuDongKeHoach.HD_DANH, k.hd);
        t.veHoiPhucNguoi = true;
        k = lap(t);
        Kiem.bang("bat Ve hoi phuc: khong rut lui 'het thuoc' (truoc: HD_VE dung im cho hoi 80% giua bay quai)", TuDongKeHoach.HD_DANH, k.hd);
        Kiem.sai("... khong ghi dang hoi phuc", k.hoiPhuc);
        t.tuLanThuocMs = 800L;
        t.tuLanBinhHpMs = Long.MAX_VALUE;
        t.tuLanBinhMs = Long.MAX_VALUE;
        k = lap(t);
        Kiem.bang("lenh truoc khong toi (client bo qua) 0,8 giay sau: ra lenh binh HP lai (truoc: cho du 1,5 giay)", TuDongKeHoach.HD_THUOC, k.hd);
        Kiem.bang("... binh 94", 94, k.so);
        // het binh that van rut lui / dung nhu cu
        t = th();
        t.hp = 4000;
        t.thuoc[94] = 0;
        t.veHoiPhucNguoi = false;
        k = lap(t);
        Kiem.bang("HET binh HP that + tat Ve hoi phuc: van dung (ly do dung)", TuDong.TAT_HP_THAP, k.lyDoTat);
        t = th();
        t.hp = 4000;
        t.nguongHpNguoi = 0; // nguoi choi tat thuoc HP
        t.veHoiPhucNguoi = false;
        Kiem.bang("nguoi choi tat thuoc HP: HP 20% van dung nhu cu", TuDongKeHoach.HD_DUNG, lap(t).hd);
    }

    /** Binh PP vua uong / PP thap khong duoc gianh nhip cua binh HP khi HP duoi nguong. */
    static void hpTruocPp() {
        TuDongKeHoach.TinhHinh t = th();
        t.hp = 5000; // 25% < nguong 30%
        t.mp = 100;
        t.thuoc[24] = 500;
        t.tuLanThuocMs = 900L;
        t.tuLanBinhMs = 900L; // binh PP 0,9 giay truoc
        t.tuLanBinhHpMs = Long.MAX_VALUE;
        TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("binh PP 0,9 giay truoc, HP 25% < 30%: uong binh HP ngay (truoc: cho 1,5 giay)", TuDongKeHoach.HD_THUOC, k.hd);
        Kiem.bang("... binh HP 94", 94, k.so);
        t.tuLanBinhHpMs = 1000L;
        t.tuLanBinhMs = 1000L;
        t.tuLanThuocMs = 1000L;
        k = lap(t);
        Kiem.bang("binh HP 1 giay truoc: chua uong binh HP; PP thap cung KHONG uong PP (de nhip cho binh HP)", TuDongKeHoach.HD_DANH, k.hd);
        t.tuLanBinhHpMs = 1600L;
        t.tuLanBinhMs = 1600L;
        t.tuLanThuocMs = 1600L;
        Kiem.bang("binh HP 1,6 giay truoc: uong binh HP truoc binh PP", 94, lap(t).so);
        t.hp = 15000;
        Kiem.bang("HP 75%: binh PP", 24, lap(t).so);
        t.tuLanBinhMs = 900L;
        t.tuLanThuocMs = 900L;
        Kiem.bang("HP du, binh bat ky 0,9 giay truoc: binh PP cho du 1,5 giay", TuDongKeHoach.HD_DANH, lap(t).hd);
    }

    /**
     * Mo phong 30 giay HP + PP luon duoi nguong, bo nao hoi 0,1 giay / lan, client thuc hien ngay (+0..300 ms tre): binh
     * HP cach nhau >= 1,5 giay va KHONG bao gio vuot tu_dong.nhip_thuoc 4/5s (TuDong.choPhep: vuot = bo lenh + vi pham).
     */
    static void nhipBinh5Giay() {
        final TuDong.CauHinh c = new TuDong.CauHinh();
        final TuDong.Phien ph = new TuDong.Phien();
        ph.bat = true;
        long keThuoc = Long.MIN_VALUE / 4, binhHp = Long.MIN_VALUE / 4, binh = Long.MIN_VALUE / 4;
        int soHp = 0, soPp = 0, viPham = 0;
        long hpCuoi = -1L, khoangHpMin = Long.MAX_VALUE;
        final java.util.Random r = new java.util.Random(12);
        for (long bay = 0L; bay < 30000L; bay += 100L) {
            final TuDongKeHoach.TinhHinh t = th();
            t.hp = 3000;
            t.mp = 100;
            t.thuoc[24] = 500;
            t.tuLanThuocMs = bay - keThuoc;
            t.tuLanBinhHpMs = bay - binhHp;
            t.tuLanBinhMs = bay - binh;
            final TuDongKeHoach.KeHoach k = lap(t);
            if (k.hd != TuDongKeHoach.HD_THUOC) {
                continue;
            }
            keThuoc = bay;
            final long toi = bay + r.nextInt(300); // tre mang
            if (!TuDong.choPhep(c, ph, TuDong.HD_THUOC, toi)) {
                viPham++;
            }
            binh = toi;
            if (k.so == 94) {
                soHp++;
                if (hpCuoi >= 0) {
                    khoangHpMin = Math.min(khoangHpMin, toi - hpCuoi);
                }
                hpCuoi = toi;
                binhHp = toi;
            } else {
                soPp++;
            }
        }
        Kiem.bang("30 giay HP + PP duoi nguong: khong lan nao vuot nhip thuoc 4/5s", 0, viPham);
        Kiem.dung("binh HP cach nhau >= 1,5 giay (min " + khoangHpMin + " ms)", khoangHpMin >= TuDongKeHoach.KHOANG_THUOC_MS);
        Kiem.dung("binh HP gan toi da nhip (>= 17 lan / 30 giay, co " + soHp + ")", soHp >= 17);
        Kiem.dung("HP duoi nguong: binh PP khong gianh nhip (co " + soPp + ")", soPp <= 1);
    }

    /** Tui day / vu khi hong GIUA TRAN: danh not con dang danh minh roi moi dung (truoc: dung ngay, guc 40-250 giay sau). */
    static void dungAnToan() {
        for (final int ly : new int[]{TuDong.TAT_DAY_TUI, TuDong.TAT_VU_KHI_HONG}) {
            final String ten = ly == TuDong.TAT_DAY_TUI ? "tui day" : "vu khi hong";
            TuDongKeHoach.TinhHinh t = th();
            t.quai.clear();
            final TuDongKeHoach.Quai dangDanh = KiemTuDongKeHoach.q(7, 470, 400, 84);
            dangDanh.danhMinh = true;
            t.quai.add(KiemTuDongKeHoach.q(1, 420, 400, 84));
            t.quai.add(dangDanh);
            t.tuiDay = ly == TuDong.TAT_DAY_TUI;
            t.vuKhiHong = ly == TuDong.TAT_VU_KHI_HONG;
            t.tuLanBiDanhMs = 1200L;
            TuDongKeHoach.KeHoach k = lap(t);
            Kiem.bang(ten + " khi dang bi danh: chua dung, danh con DANG DANH MINH (khong danh con gan hon)", 7, k.id);
            Kiem.bang("... HD_DANH", TuDongKeHoach.HD_DANH, k.hd);
            Kiem.dung("... trang thai 'sap dung' (" + k.ly + ")", k.ly.startsWith("sắp dừng"));
            t.hp = 4000;
            Kiem.bang(ten + " giua tran, HP 20%: van uong thuoc", TuDongKeHoach.HD_THUOC, lap(t).hd);
            t.hp = 20000;
            dangDanh.danhMinh = false;
            k = lap(t);
            Kiem.bang(ten + ": vua bi danh (1,2 giay) nhung khong con nao nham minh: dung cho, chua dung", TuDongKeHoach.HD_CHO, k.hd);
            t.tuLanBiDanhMs = SoSatThuong.GIAO_CHIEN_MS;
            k = lap(t);
            Kiem.bang(ten + ": het bi danh 5 giay: DUNG", TuDongKeHoach.HD_DUNG, k.hd);
            Kiem.bang("... ly do nhu cu", ly, k.lyDoTat);
        }
    }

    /** Ra ngoai vung (ban kinh + 64) nhung chua xa: ve diem bat dau, khong dung (truoc lyDo=5). Xa han (x2): dung. */
    static void raVung() {
        final TuDongKeHoach.TinhHinh t = th();
        t.x = 400 + 240 + 100;
        final TuDongKeHoach.KeHoach k = lap(t);
        Kiem.bang("ra vung 100 px: quay ve diem bat dau", TuDongKeHoach.HD_VE, k.hd);
        Kiem.bang("... ve neo", 400, k.x);
        t.hp = 4000;
        Kiem.bang("ra vung + HP 20%: uong thuoc truoc", TuDongKeHoach.HD_THUOC, lap(t).hd);
        final TuDong.Phien p = new TuDong.Phien();
        final TuDong.NguCanh n = new TuDong.NguCanh();
        n.map = 12;
        n.x = 400;
        n.y = 300;
        final TuDong.CauHinh c = new TuDong.CauHinh();
        TuDong.bat(c, p, n, 240, 1000L);
        Kiem.dung("raVung (mem): 240 + 64 + 36", TuDong.raVung(p, 12, 400 + 340, 300));
        Kiem.sai("raVungXa: 340 px < 2 x 240 + 64 - chua dung", TuDong.raVungXa(p, 12, 400 + 340, 300));
        Kiem.dung("raVungXa: 560 px > 544 - dung", TuDong.raVungXa(p, 12, 400 + 560, 300));
        Kiem.dung("raVungXa: sang map khac - dung", TuDong.raVungXa(p, 13, 400, 300));
    }

    /** So sat thuong (lop thuan): 5 don cuoi, don ket lieu, nguon khong ghi nhan, chot mot lan / lan guc. */
    static void soSatThuong() {
        final SoSatThuong s = new SoSatThuong();
        Kiem.sai("chua bi danh: khong giao chien", s.dangGiaoChien(1000L));
        s.nhip(900L, 20000);
        for (int i = 0; i < 7; i++) {
            s.ghi(1000L + i * 100L, "Croconaw (cấp 24)", 500, 20000 - i * 500, 19500 - i * 500, 20000);
        }
        Kiem.bang("giu 5 don cuoi", 5, s.cuoi().length);
        Kiem.bang("cu -> moi: don cuoi HP sau 16500", 16500, s.cuoi()[4].hpSau);
        Kiem.dung("vua bi danh: dang giao chien", s.dangGiaoChien(1700L));
        Kiem.sai("5 giay sau: het giao chien", s.dangGiaoChien(1600L + SoSatThuong.GIAO_CHIEN_MS));
        s.ghi(2000L, "Galvantula (cấp 80, Alpha)", 16500, 16500, 0, 20000);
        SoSatThuong.Don[] ds = s.chotGuc(2000L, 20000);
        Kiem.bang("don ket lieu ghi nhan: giu nguyen", "Galvantula (cấp 80, Alpha)", ds[ds.length - 1].nguon);
        final String cau = SoSatThuong.cauNguoiChoi(ds, 2000L);
        Kiem.dung("cau nguoi choi noi quai + sat thuong + HP con truoc don: " + cau,
                cau.contains("Galvantula (cấp 80, Alpha) đánh 16500 HP") && cau.contains("16500/20000 HP (82%)"));
        final String log = SoSatThuong.dongLog(ds, 2000L);
        Kiem.dung("dong log co 5 don, don cuoi '-16500 Galvantula ... 16500->0/20000 (82%) @-0.0s': " + log,
                log.split(" \\| ").length == 5 && log.endsWith("-16500 Galvantula (cấp 80, Alpha) 16500->0/20000 (82%) @-0.0s"));
        Kiem.bang("guc goi lai (actorDie lap): khong chot lan 2", null, s.chotGuc(2500L, 20000));
        Kiem.sai("van guc (HP 0): chua mo so moi", s.nhip(2600L, 0));
        Kiem.dung("hoi sinh (HP > 0): so moi", s.nhip(5000L, 10000));
        Kiem.bang("... so cu da xoa", 0, s.cuoi().length);
        // HP mat o cho khong ghi nhan: don "nguon khac" = HP biet cuoi
        s.ghi(6000L, "Croconaw (cấp 24)", 300, 10000, 9700, 20000);
        s.nhip(6500L, 9100); // hoi / tru o dau do: nhip thay 9100
        ds = s.chotGuc(7000L, 20000);
        Kiem.bang("guc khong ro nguon: don cuoi 'nguon khac'", SoSatThuong.NGUON_KHAC, ds[ds.length - 1].nguon);
        Kiem.bang("... sat thuong = HP biet cuoi (nhip 9100)", 9100, ds[ds.length - 1].sat);
        Kiem.dung("... cau noi nguon chua ro: " + SoSatThuong.cauNguoiChoi(ds, 7000L),
                SoSatThuong.cauNguoiChoi(ds, 7000L).contains("nguồn chưa rõ"));
        Kiem.bang("tong 3 giay cuoi (300 + 9100)", Long.valueOf(9400L), Long.valueOf(SoSatThuong.tong(ds, 7000L, 3000L)));
        final SoSatThuong s2 = new SoSatThuong();
        s2.ghi(1000L, "A", 2000, 10000, 8000, 10000);
        s2.ghi(1500L, "B", 3000, 8000, 5000, 10000);
        s2.ghi(2000L, "B", 3000, 5000, 2000, 10000);
        s2.ghi(2400L, "A", 2000, 2000, 0, 10000);
        final String c2 = SoSatThuong.cauNguoiChoi(s2.chotGuc(2400L, 10000), 2400L);
        Kiem.dung("nhieu don 3 giay cuoi: tong + so don + nguon nhieu nhat: " + c2,
                c2.contains("mất 10000 HP qua 4 đòn") && c2.contains("nhiều nhất: B"));
    }

    static Char nv(final String ten, final int id) {
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0]; // Char(null) can mang ngoc (giong KiemSuaTuyen)
        }
        if (real.Map.idMapMONSTER.isEmpty()) {
            real.Map.idMapMONSTER.add(new java.util.Vector<Integer>());
        }
        final Char p = new Char(null);
        p.charDBID = id;
        p.id = (short) (id % 30000);
        p.isBot = -1;
        p.cat = 0;
        p.myCountry = 0;
        p.inCountry = 0;
        p.region = 0;
        p.map = new real.Map();
        p.mapID = 26;
        p.charname = ten;
        p.maxhp = 20000;
        p.hp = 20000;
        return p;
    }

    /** Guc that: HoiSinhGame.khiGuc chot so, cau "Guc vi ..." cho hop thoai guc, hoi sinh xoa cau cu. */
    static void gucThat() {
        final Char p = nv("kiemtreoguc", 987451);
        try {
            HoiSinhGame.xoaChoKiem(p);
            p.hp = 10000;
            final int truoc = p.hp;
            p.hp = 0;
            GucGame.ghi(p, "Galvantula (cấp 80)", truoc);
            HoiSinhGame.khiGuc(p);
            final String cau = GucGame.cau(p);
            Kiem.dung("guc: cau 'Guc vi Galvantula (cap 80) danh 10000 HP khi con 10000/20000 HP (50%)': " + cau,
                    cau.contains("Galvantula (cấp 80) đánh 10000 HP") && cau.contains("10000/20000 HP (50%)"));
            HoiSinhGame.khiGuc(p);
            Kiem.bang("actorDie goi lai: van mot cau", cau, GucGame.cau(p));
            p.hp = 20000;
            GucGame.nhip(p);
            Kiem.bang("hoi sinh: xoa cau guc cu", "", GucGame.cau(p));
            // guc khong ghi nhan (nguon chua noi so): van co cau, khong doan sai ten quai
            GucGame.nhip(p);
            p.hp = 0;
            HoiSinhGame.khiGuc(p);
            Kiem.dung("guc khong ro nguon: cau noi nguon chua ro (" + GucGame.cau(p) + ")", GucGame.cau(p).contains("nguồn chưa rõ"));
        } finally {
            HoiSinhGame.xoaChoKiem(p);
            GucGame.xoaCau(p);
        }
    }

    /** Monster.attack (duong quai danh nguoi chinh) ghi don vao so: ten quai + cap + sat thuong that. */
    static void quaiDanhGhiSo() {
        final Char p = nv("kiemquaidanh", 987452);
        final real.Map m = p.map;
        try {
            m.initKhoQuaiVaNguoi();
        } catch (final RuntimeException ignored) {
            // ban do rong
        }
        final real.MonsterTemplate mau = new real.MonsterTemplate();
        mau.id = 29;
        mau.name = "Croconaw";
        mau.level = (byte) 24;
        mau.maxhp = 3420;
        mau.attack = 5000;
        mau.defend = 142;
        mau.accurate = (short) 30000;
        mau.dodge = 0;
        mau.he = (byte) HePokemon.NUOC;
        final Object cu = real.Map.monsterTemplates.get(Integer.valueOf(29));
        real.Map.monsterTemplates.put(Integer.valueOf(29), mau);
        try {
            final real.Monster q = new real.Monster(m, mau, 400, 400, 0);
            q.level = mau.level; // nhu MapNapTaiNguyen khi nap ban do
            q.target = p;
            p.x = 400;
            p.y = 400;
            for (int i = 0; i < 30 && p.soSatThuong.cuoi().length == 0; i++) {
                q.lastTimeAttack = 0L;
                q.attack();
            }
            final SoSatThuong.Don[] ds = p.soSatThuong.cuoi();
            Kiem.dung("Monster.attack: ghi don vao so sat thuong (" + ds.length + ")", ds.length > 0);
            if (ds.length > 0) {
                final SoSatThuong.Don d = ds[ds.length - 1];
                Kiem.dung("... ten quai + cap: " + d.nguon, d.nguon.startsWith("Croconaw (cấp 24"));
                Kiem.bang("... sat thuong = HP that mat", Integer.valueOf(d.hpTruoc - d.hpSau), Integer.valueOf(d.sat));
                Kiem.bang("... HP sau = HP nhan vat", Integer.valueOf(p.hp), Integer.valueOf(d.hpSau));
            }
        } finally {
            if (cu == null) {
                real.Map.monsterTemplates.remove(Integer.valueOf(29));
            } else {
                real.Map.monsterTemplates.put(Integer.valueOf(29), (real.MonsterTemplate) cu);
            }
        }
    }

    static void noiVaoGame() {
        final String g = KiemTuDong.doc("src/real/TuDongGame.java");
        Kiem.dung("TuDongGame: dung khi ra XA (raVungXa), trong khoang bo nao dua ve", g.contains("TuDong.raVungXa(ph, p.map.mapId, p.x, p.y)"));
        Kiem.dung("TuDongGame: nhip binh theo binh THAT (Char.timeUsePotion)", g.contains("t.tuLanBinhHpMs = tuLanDung(")
                && g.contains("t.tuLanBinhMs = tuLanDung("));
        Kiem.dung("TuDongGame: dang giao chien (so sat thuong + quai nham minh)", g.contains("t.tuLanBiDanhMs =")
                && g.contains("q.danhMinh = mt.target == p;"));
        Kiem.dung("TuDongGame: vu khi hong giua tran khong dung ngay (bo nao dung an toan)", g.contains("vuKhiHong(p) && (!c.keHoach || !dangGiaoChien(p, bay))"));
        Kiem.dung("TuDongGame: % HP bo nao theo HP toi da THAT (buff HP)", g.contains("t.maxhp = GucGame.maxThat(p);"));
        final String mo = KiemTuDong.doc("src/real/Monster.java");
        Kiem.dung("Monster.attack ghi so sat thuong", mo.contains("GucGame.quaiDanh(target, this, hpTruocDon);"));
        final String hs = KiemTuDong.doc("src/real/HoiSinhGame.java");
        Kiem.dung("HoiSinhGame.khiGuc -> GucGame.khiGuc (moi ban do)", hs.contains("GucGame.khiGuc(p);"));
        Kiem.dung("hop thoai guc kem 'Guc vi' (tin rieng da gui o GucGame.khiGuc)", hs.contains("GucGame.cau(p)"));
        for (final String f : new String[]{"src/real/BossDusknoir.java", "src/real/BossDotKich.java", "src/real/BossSerperior.java",
            "src/real/BossServine.java", "src/data/BossMatQuy.java", "src/real/TrangThaiGame.java", "src/real/MapDanhNhau.java",
            "src/real/Region.java", "src/real/CharVongLap.java"}) {
            Kiem.dung(f + ": ghi so sat thuong", KiemTuDong.doc(f).contains("GucGame."));
        }
        final String c = KiemTuDong.doc("../client_jar/shared/mod/game/render/screen/TuDong.java");
        Kiem.dung("client TuDong: lenh binh may chu khong bi chan 'HP da day' theo HP client", c.contains("gs.doUsePotion(keSo, true);"));
        Kiem.dung("client TuDong: nhip binh HP rieng (khop may chu 1,5 s / 0,8 s)", c.contains("KHOANG_GIUA_BINH = 800L")
                && c.contains("static boolean duocUong(int n, long l)"));
        Kiem.dung("client TuDong: binh PP nhuong binh HP khi HP duoi nguong", c.contains("if (hpDuoi && coBinh(mc, BINH_HP) >= 0 || l - lucBinhCuoi < KHOANG_BINH)"));
        final String gs = KiemTuDong.doc("../client_jar/src/game/render/screen/GameScr.java");
        Kiem.dung("client GameScr: quai danh khi chua thay quai tren man van dat HP that",
                gs.contains("&& (!this.readyGetGameLogic || findMonsterByID(attacker) == null)) {") && gs.contains("game.pokemon.MauPP.datHp(this.mainChar, hpLeft);"));
        Kiem.dung("client GameScr: doUsePotion(.., lenhMayChu) bo chan HP day", gs.contains("if (!lenhMayChu && this.mainChar.hp >= this.mainChar.maxhp"));
        final String bot = KiemTuDong.doc("../tools/pokemon/hlv_ai/HlvAiNguoiChoi.java");
        Kiem.dung("bot HLV AI treo: uong thuoc theo lenh bo nao (truoc: bo qua, 2.012 lan guc thuoc=0)", bot.contains("} else if (k[0] == 2 && k[2] > 0 && b.soThuoc(k[2]) > 0) {"));
    }
}
