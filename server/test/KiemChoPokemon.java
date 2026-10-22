import com.htbb.pokemon.BatPokemon;
import com.htbb.pokemon.CaThePokemon;
import com.htbb.pokemon.ChoPokemon;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.TraoDoiPokemon;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KIEM CHO POKEMON (docs/thiet-ke-game.md 7.9; lop thuan com.htbb.pokemon.ChoPokemon, CSDL gia trong bo nho co CAS nhu
 * UPDATE ... WHERE trang_thai = 0 AND het_han &gt; ?): khung gia, thue bao toan (nguoi mua tra = nguoi ban nhan + thue),
 * cong (cap, huyen thoai / su kien, dang theo, khoa 72 gio, toi da 3 tin, toi da 5 mua / ngay, cung tai khoan / may / IP),
 * ky quy (dang bo con khoi hop, huy / het han tra lai, ghi hong khong doi gi), hai nguoi mua cung luc mot tin (200 vong),
 * con mua chi chuyen doi ra Keo, nghi van, SQL + khoa cau hinh.
 */
public final class KiemChoPokemon {

    private KiemChoPokemon() {
    }

    static final long BAY = 1_800_000_000L;
    static final int NGAY = 20261022;
    static final long XMG = 10000L;

    public static void chay() {
        Kiem.nhom("Cho Pokemon: khung gia theo bac hiem / sac mau / 3 sao, thue + phi dang");
        kiemKhung();
        Kiem.nhom("Cho Pokemon: dang ban (ky quy), cong nguoi ban, huy / het han tra lai");
        kiemDang();
        Kiem.nhom("Cho Pokemon: mua nguyen tu, cong nguoi mua, bao toan xu, hai nguoi mua cung luc");
        kiemMua();
        kiemDongThoi();
        Kiem.nhom("Cho Pokemon: con mua chi chuyen doi ra Keo, nghi van, SQL + cau hinh");
        kiemKhac();
    }

    /** CSDL gia: CAS dung nhu SQL that, ghi chuoi Hop PC. */
    static final class KhoGia implements ChoPokemon.Kho {
        final Map<Long, ChoPokemon.Tin> tin = new HashMap<Long, ChoPokemon.Tin>();
        final Map<Integer, String> hop = new HashMap<Integer, String>();
        long idKe = 1;
        int log;
        boolean hong;

        public synchronized long dang(final ChoPokemon.Tin t, final String hopBan) {
            if (this.hong) {
                return 0L;
            }
            t.id = this.idKe++;
            this.tin.put(Long.valueOf(t.id), sao(t));
            this.hop.put(Integer.valueOf(t.idBan), hopBan);
            return t.id;
        }

        public synchronized boolean mua(final ChoPokemon.Tin t, final long bay, final int idMua, final String ten, final String hopMua, final long thue) {
            final ChoPokemon.Tin x = this.tin.get(Long.valueOf(t.id));
            if (this.hong || x == null || x.trangThai != ChoPokemon.DANG_BAN || bay >= x.hetHan) {
                return false;
            }
            x.trangThai = ChoPokemon.DA_BAN;
            x.idMua = idMua;
            x.thue = thue;
            this.hop.put(Integer.valueOf(idMua), hopMua);
            this.log++;
            return true;
        }

        public synchronized boolean thuHoi(final ChoPokemon.Tin t, final String hopBan) {
            final ChoPokemon.Tin x = this.tin.get(Long.valueOf(t.id));
            if (this.hong || x == null || x.trangThai != ChoPokemon.DANG_BAN) {
                return false;
            }
            x.trangThai = ChoPokemon.DA_THU_HOI;
            this.hop.put(Integer.valueOf(t.idBan), hopBan);
            return true;
        }

        /** Ban doc tu "CSDL" (moi nguoi mua mot ban rieng nhu doc that). */
        synchronized ChoPokemon.Tin doc(final long id) {
            final ChoPokemon.Tin x = this.tin.get(Long.valueOf(id));
            return x == null ? null : sao(x);
        }
    }

    static ChoPokemon.Tin sao(final ChoPokemon.Tin t) {
        final ChoPokemon.Tin c = new ChoPokemon.Tin();
        c.id = t.id;
        c.idBan = t.idBan;
        c.tenBan = t.tenBan;
        c.userBan = t.userBan;
        c.mayBan = t.mayBan;
        c.ipBan = t.ipBan;
        c.con = t.con.saoVoiId(0);
        c.doHiem = t.doHiem;
        c.gia = t.gia;
        c.phiDang = t.phiDang;
        c.dangLuc = t.dangLuc;
        c.hetHan = t.hetHan;
        c.trangThai = t.trangThai;
        c.idMua = t.idMua;
        c.thue = t.thue;
        return c;
    }

    static final BatPokemon.Loai THUONG = new BatPokemon.Loai(19, "Rattata", 0, 0, 0, 0, 0, true, false);
    static final BatPokemon.Loai HIEM = new BatPokemon.Loai(123, "Scyther", 2, 0, 0, 0, 0, true, false);
    static final BatPokemon.Loai SU_KIEN = new BatPokemon.Loai(25, "Pikachu", 0, 0, 0, 0, 0, true, true);

    static ChoPokemon.Ben ban(final int id) {
        return new ChoPokemon.Ben(id, "Ban" + id, 100 + id, "may" + id, "10.0.0." + id, 30);
    }

    static HopPc hop(final int chu, final int dex, final int n) {
        final HopPc h = new HopPc();
        for (int i = 0; i < n; i++) {
            h.them(dex, 20, false, NGAY, "poke", CaThePokemon.dongGoi(new int[]{10, 10, 10, 10, 10, 10}), 0, chu);
        }
        h.ban = false;
        return h;
    }

    static void kiemKhung() {
        final ChoPokemon.Luat l = ChoPokemon.Luat.macDinh();
        Kiem.bang("mac dinh: thue 8%, phi dang 1%, cap 20, 3 tin, 5 mua / ngay, khoa 72 gio, han 48 gio, sac mau x3, 3 sao x2",
                "8,1,20,3,5,259200,172800,3,2", l.thuePhanTram + "," + l.phiDangPhanTram + "," + l.capToiThieu + "," + l.toiDaDang + ","
                        + l.muaNgay + "," + l.khoaGiay + "," + l.hanGiay + "," + l.heSoSacMau + "," + l.heSo3Sao);
        final StringBuilder sb = new StringBuilder();
        for (int h = 0; h < 4; h++) {
            final long[] k = ChoPokemon.khung(l, h, false, 0, XMG);
            sb.append(k[0]).append('-').append(k[1]).append(' ');
        }
        Kiem.bang("khung (10.000 xu / gio): Thuong 0,1-1 gio, It gap 0,3-3, Hiem 1-10, Rat hiem 3-30", "1000-10000 3000-30000 10000-100000 30000-300000",
                sb.toString().trim());
        final long[] s = ChoPokemon.khung(l, 2, true, 0, XMG);
        final long[] s3 = ChoPokemon.khung(l, 2, false, 3, XMG);
        final long[] ca = ChoPokemon.khung(l, 3, true, 3, XMG);
        Kiem.bang("sac mau x3 / 3 sao x2 / ca hai x6 tren CA san lan tran", "30000-300000 20000-200000 180000-1800000",
                s[0] + "-" + s[1] + " " + s3[0] + "-" + s3[1] + " " + ca[0] + "-" + ca[1]);
        Kiem.dung("2 sao khong nhan", ChoPokemon.khung(l, 0, false, 2, XMG)[1] == 10000);
        final HopPc h = hop(1, 19, 1);
        final HopPc.Con c = h.lay(1);
        final long[] k = ChoPokemon.khung(l, 0, false, 0, XMG);
        Kiem.dung("gia duoi san -> tu choi", ChoPokemon.lyDoDang(l, h, c, THUONG, 30, 0, 999, k, BAY).contains("trong khoảng"));
        Kiem.dung("gia tren tran -> tu choi", ChoPokemon.lyDoDang(l, h, c, THUONG, 30, 0, 10001, k, BAY).contains("trong khoảng"));
        Kiem.dung("gia dung san / tran -> duoc", ChoPokemon.lyDoDang(l, h, c, THUONG, 30, 0, 1000, k, BAY) == null
                && ChoPokemon.lyDoDang(l, h, c, THUONG, 30, 0, 10000, k, BAY) == null);
        int sai = 0;
        for (long g = 1; g < 5_000_000L; g = g * 3 + 7) {
            if (ChoPokemon.nguoiBanNhan(l, g) + ChoPokemon.thue(l, g) != g || ChoPokemon.thue(l, g) < g * 8 / 100) {
                sai++;
            }
        }
        Kiem.bang("bao toan: gia = nguoi ban nhan + thue, thue >= 8% (moi muc gia)", Integer.valueOf(0), Integer.valueOf(sai));
        Kiem.bang("phi dang 1%, toi thieu 1 xu", "100,1", ChoPokemon.phiDang(l, 10000) + "," + ChoPokemon.phiDang(l, 50));
    }

    static void kiemDang() {
        final ChoPokemon.Luat l = ChoPokemon.Luat.macDinh();
        final KhoGia kho = new KhoGia();
        final HopPc h = hop(1, 19, 5);
        final ChoPokemon.KetQua k1 = ChoPokemon.dang(l, ban(1), h, 2, 5000, THUONG, XMG, 0, BAY, kho);
        Kiem.dung("dang ban: thanh cong", k1.ok && k1.id > 0);
        Kiem.dung("ky quy: con roi khoi Hop PC, CSDL giu chuoi hop moi", h.lay(2) == null && h.soCon() == 4
                && HopPc.giaiMa(kho.hop.get(Integer.valueOf(1))).maHoa().equals(h.maHoa()));
        final ChoPokemon.Tin t = kho.doc(k1.id);
        Kiem.dung("tin giu dung con (IV, cap) + het han sau 48 gio", t.con.iv == k1.con.iv && t.con.cap == 20 && t.hetHan == BAY + 48L * 3600L);
        Kiem.dung("cap nguoi ban < 20 -> tu choi", ChoPokemon.dang(l, new ChoPokemon.Ben(1, "x", 1, "", "", 19), h, 3, 5000, THUONG, XMG, 0, BAY, kho)
                .loi.contains("cấp 20"));
        Kiem.dung("huyen thoai (khong co dong loai) -> khong bao gio ban", ChoPokemon.dang(l, ban(1), h, 3, 5000, null, XMG, 0, BAY, kho)
                .loi.contains("huyền thoại"));
        Kiem.dung("loai chi su kien -> khong ban", ChoPokemon.dang(l, ban(1), h, 3, 5000, SU_KIEN, XMG, 0, BAY, kho).loi.contains("sự kiện"));
        h.datTheo(3);
        Kiem.dung("con dang di theo -> phai thoi theo", ChoPokemon.dang(l, ban(1), h, 3, 5000, THUONG, XMG, 0, BAY, kho).loi.contains("Thôi đi theo"));
        h.datTheo(0);
        h.lay(3).doiLuc = BAY - 71L * 3600L;
        Kiem.dung("nhan qua trao doi / cho 71 gio truoc -> khoa (con 1 gio)", ChoPokemon.dang(l, ban(1), h, 3, 5000, THUONG, XMG, 0, BAY, kho)
                .loi.contains("1 giờ"));
        h.lay(3).doiLuc = BAY - 72L * 3600L;
        Kiem.dung("du 72 gio -> ban duoc", ChoPokemon.lyDoDang(l, h, h.lay(3), THUONG, 30, 0, 5000, ChoPokemon.khung(l, 0, false, 0, XMG), BAY) == null);
        Kiem.dung("da co 3 tin -> tu choi tin thu 4", ChoPokemon.dang(l, ban(1), h, 3, 5000, THUONG, XMG, 3, BAY, kho).loi.contains("tối đa 3"));
        // CSDL hong: hop khong doi
        final String truoc = h.maHoa();
        kho.hong = true;
        final ChoPokemon.KetQua kh = ChoPokemon.dang(l, ban(1), h, 4, 5000, THUONG, XMG, 0, BAY, kho);
        kho.hong = false;
        Kiem.dung("ghi tin hong: Pokemon van trong Hop PC, khong mat", !kh.ok && h.maHoa().equals(truoc) && h.lay(4) != null);
        // huy: tra con (giu IV), vuot suc chua van tra
        for (int i = 0; i < 30; i++) {
            h.them(43, 5, false, NGAY, "poke");
        }
        Kiem.dung("hop day (30/30)", h.day());
        final ChoPokemon.KetQua kv = ChoPokemon.thuHoi(kho.doc(k1.id), 1, h, kho);
        Kiem.dung("huy tin: con ve Hop PC (ke ca khi day), giu IV", kv.ok && kv.con.iv == k1.con.iv && h.soCon() == 31);
        Kiem.dung("huy lan 2 -> tu choi (khong nhan ban)", !ChoPokemon.thuHoi(kho.doc(k1.id), 1, h, kho).ok && h.soCon() == 31);
        Kiem.dung("nguoi khac khong huy duoc tin", !ChoPokemon.thuHoi(kho.doc(k1.id), 2, hop(2, 19, 1), kho).ok);
    }

    static void kiemMua() {
        final ChoPokemon.Luat l = ChoPokemon.Luat.macDinh();
        final KhoGia kho = new KhoGia();
        final HopPc hBan = hop(1, 123, 3);
        final ChoPokemon.KetQua d = ChoPokemon.dang(l, ban(1), hBan, 1, 50000, HIEM, XMG, 0, BAY, kho);
        final HopPc hMua = hop(2, 19, 1);
        final ChoPokemon.Ben mua = ban(2);
        Kiem.dung("tu mua tin cua minh -> tu choi", ChoPokemon.lyDoMua(l, kho.doc(d.id), ban(1), hBan, BAY, NGAY).contains("chính mình"));
        Kiem.dung("cung tai khoan -> tu choi", ChoPokemon.lyDoMua(l, kho.doc(d.id), new ChoPokemon.Ben(9, "x", 101, "", "", 30), hMua, BAY, NGAY)
                .contains("tài khoản"));
        Kiem.dung("cung thiet bi -> tu choi", ChoPokemon.lyDoMua(l, kho.doc(d.id), new ChoPokemon.Ben(9, "x", 9, "may1", "", 30), hMua, BAY, NGAY)
                .contains("thiết bị"));
        Kiem.dung("cung IP -> tu choi", ChoPokemon.lyDoMua(l, kho.doc(d.id), new ChoPokemon.Ben(9, "x", 9, "", "10.0.0.1", 30), hMua, BAY, NGAY)
                .contains("IP"));
        Kiem.dung("nguoi mua cap < 20 -> tu choi", ChoPokemon.lyDoMua(l, kho.doc(d.id), new ChoPokemon.Ben(9, "x", 9, "", "", 19), hMua, BAY, NGAY)
                .contains("cấp 20"));
        Kiem.dung("tin het han (48 gio) -> khong mua duoc", ChoPokemon.lyDoMua(l, kho.doc(d.id), mua, hMua, BAY + 48L * 3600L, NGAY).contains("hết hạn"));
        // mua + bao toan xu
        long xuMua = 100000L;
        final long gia = kho.doc(d.id).gia;
        xuMua -= gia;
        final ChoPokemon.KetQua k = ChoPokemon.mua(l, kho.doc(d.id), mua, hMua, BAY, NGAY, kho);
        Kiem.dung("mua thanh cong, Scyther vao Hop PC nguoi mua", k.ok && hMua.soCungLoai(123, false) == 1 && hMua.muaNgay == 1);
        final ChoPokemon.Tin sau = kho.doc(d.id);
        final long nhan = sau.gia - sau.thue;
        Kiem.bang("bao toan: nguoi mua tra 50.000 = nguoi ban nhan 46.000 + thue 4.000 (dot)", "50000=46000+4000",
                (100000L - xuMua) + "=" + nhan + "+" + sau.thue);
        Kiem.dung("con mua: doiLuc = luc mua, giu chu goc (nguoi bat)", k.con.doiLuc == BAY && k.con.chuGoc == 1);
        Kiem.dung("mua lan 2 cung tin -> that bai (da ban)", !ChoPokemon.mua(l, kho.doc(d.id), ban(3), hop(3, 19, 1), BAY, NGAY, kho).ok);
        Kiem.dung("nhat ky ghi 1 lan", kho.log == 1);
        // tin het han: CSDL CAS tu choi ke ca khi ban doc cu con trang thai 0
        final ChoPokemon.KetQua d2 = ChoPokemon.dang(l, ban(1), hBan, 2, 20000, HIEM, XMG, 1, BAY, kho);
        final ChoPokemon.Tin cu = kho.doc(d2.id);
        cu.hetHan = BAY + 10L * 3600L * 24L; // ban doc "sai" van bi CSDL chan
        final HopPc h4 = hop(4, 19, 1);
        Kiem.dung("het han o CSDL: CAS tu choi, hop nguoi mua khong doi", !ChoPokemon.mua(l, cu, ban(4), h4, BAY + 48L * 3600L, NGAY, kho).ok
                && h4.soCon() == 1);
        final ChoPokemon.KetQua hh = ChoPokemon.thuHoi(kho.doc(d2.id), 1, hBan, kho);
        Kiem.dung("het han: thu hoi tra con ve nguoi ban", hh.ok && hBan.soCungLoai(123, false) == 2);
        Kiem.dung("da ban thi khong thu hoi duoc (khong nhan ban)", !ChoPokemon.thuHoi(kho.doc(d.id), 1, hBan, kho).ok);
        // tran 5 mua / ngay
        final HopPc hm = hop(5, 19, 1);
        hm.doiNgay(NGAY);
        hm.muaNgay = 5;
        final ChoPokemon.KetQua d3 = ChoPokemon.dang(l, ban(1), hBan, 3, 20000, HIEM, XMG, 0, BAY, kho);
        Kiem.dung("da mua 5 hom nay -> tu choi", ChoPokemon.lyDoMua(l, kho.doc(d3.id), ban(5), hm, BAY, NGAY).contains("đủ 5"));
        Kiem.dung("sang ngay moi -> mua duoc", ChoPokemon.lyDoMua(l, kho.doc(d3.id), ban(5), hm, BAY, NGAY + 1) == null);
        final HopPc day = hop(6, 19, 30);
        Kiem.dung("hop nguoi mua day -> tu choi", ChoPokemon.lyDoMua(l, kho.doc(d3.id), ban(6), day, BAY, NGAY).contains("đầy"));
    }

    /** Hai nguoi mua cung luc MOT tin: dung mot nguoi duoc, khong nhan ban; nguoi thua khong doi hop. */
    static void kiemDongThoi() {
        final ChoPokemon.Luat l = ChoPokemon.Luat.macDinh();
        int sai = 0;
        for (int v = 0; v < 200; v++) {
            final KhoGia kho = new KhoGia();
            final HopPc hBan = hop(1, 371, 1);
            final ChoPokemon.KetQua d = ChoPokemon.dang(l, ban(1), hBan, 1, 300000, new BatPokemon.Loai(371, "Bagon", 3, 0, 0, 0, 0, true, false), XMG, 0,
                    BAY, kho);
            final HopPc a = hop(2, 19, 1);
            final HopPc b = hop(3, 19, 1);
            final ChoPokemon.Tin ta = kho.doc(d.id);
            final ChoPokemon.Tin tb = kho.doc(d.id);
            final CountDownLatch bat = new CountDownLatch(1);
            final AtomicInteger duoc = new AtomicInteger();
            final Thread t1 = new Thread(() -> {
                try {
                    bat.await();
                } catch (final InterruptedException ignored) {
                }
                if (ChoPokemon.mua(l, ta, ban(2), a, BAY, NGAY, kho).ok) {
                    duoc.incrementAndGet();
                }
            });
            final Thread t2 = new Thread(() -> {
                try {
                    bat.await();
                } catch (final InterruptedException ignored) {
                }
                if (ChoPokemon.mua(l, tb, ban(3), b, BAY, NGAY, kho).ok) {
                    duoc.incrementAndGet();
                }
            });
            t1.start();
            t2.start();
            bat.countDown();
            try {
                t1.join(5000);
                t2.join(5000);
            } catch (final InterruptedException ignored) {
            }
            final int bagon = a.soCungLoai(371, false) + b.soCungLoai(371, false) + hBan.soCungLoai(371, false);
            if (duoc.get() != 1 || bagon != 1 || kho.log != 1) {
                sai++;
            }
        }
        Kiem.bang("200 vong x 2 nguoi mua cung mot tin: dung 1 nguoi duoc, Bagon van la 1 con, 1 dong nhat ky", Integer.valueOf(0), Integer.valueOf(sai));
    }

    static void kiemKhac() {
        final ChoPokemon.Luat l = ChoPokemon.Luat.macDinh();
        final KhoGia kho = new KhoGia();
        final HopPc hBan = hop(1, 123, 1);
        final ChoPokemon.KetQua d = ChoPokemon.dang(l, ban(1), hBan, 1, 50000, HIEM, XMG, 0, BAY, kho);
        final HopPc hMua = hop(2, 123, 1);
        final ChoPokemon.KetQua k = ChoPokemon.mua(l, kho.doc(d.id), ban(2), hMua, BAY, NGAY, kho);
        final TraoDoiPokemon.DaChuyen ch = TraoDoiPokemon.tinhChuyen(hMua, hMua.lay(k.con.id), 2, TraoDoiPokemon.chuyenMacDinh()[2], XMG, 300, 1, NGAY);
        Kiem.dung("con mua o cho: chuyen doi chi ra Keo (0 xu, 0 luong)", ch.keo == 5 && ch.xu == 0 && ch.luongKhoa == 0);
        Kiem.dung("con mua: chua ban lai duoc (khoa 72 gio)", ChoPokemon.lyDoDang(l, hMua, hMua.lay(k.con.id), HIEM, 30, 0, 50000,
                ChoPokemon.khung(l, 2, false, 0, XMG), BAY + 3600L).contains("giờ nữa"));
        Kiem.dung("con mua: trao doi tiep cung bi khoa 24 gio", TraoDoiPokemon.lyDoKhongDua(hMua, hMua.lay(k.con.id), BAY + 3600L,
                TraoDoiPokemon.Luat.macDinh()) != null);
        Kiem.bang("nghi van: cung cap 3 lan / 7 ngay", "cung cap mua-ban 3 lan/7 ngay", ChoPokemon.nghiVan(3, 0, 100, 1000));
        Kiem.bang("nghi van: gia sat tran lan thu 3", "gia sat tran 3 lan/7 ngay", ChoPokemon.nghiVan(1, 2, 950, 1000));
        Kiem.bang("khong nghi: gia thuong, it lan", "", ChoPokemon.nghiVan(1, 5, 500, 1000));
        Kiem.dung("ma hoa / giai ma mot con giu moi thu", HopPc.giaiMaCon(HopPc.maHoaCon(k.con)).iv == k.con.iv
                && HopPc.giaiMaCon(HopPc.maHoaCon(k.con)).doiLuc == BAY && HopPc.giaiMaCon("x.y") == null);
        final String sql = KiemBoNgocCheTao.doc("../sql/them_trade_pokemon.sql");
        Kiem.dung("SQL: tob_cho_pokemon, tob_log_cho_pokemon, data_cho_pokemon_gia (idempotent)", sql.contains("CREATE TABLE IF NOT EXISTS `tob_cho_pokemon`")
                && sql.contains("CREATE TABLE IF NOT EXISTS `tob_log_cho_pokemon`") && sql.contains("(3, 'Rất hiếm', 3000, 30000)"));
        final String dm = KiemBoNgocCheTao.doc("../tools/cauhinh/danh_muc.json");
        final String[][] khoa = {
            {"cho_pokemon.bat", "1"}, {"cho_pokemon.thue_phan_tram", "8"}, {"cho_pokemon.phi_dang_phan_tram", "1"},
            {"cho_pokemon.cap_toi_thieu", "20"}, {"cho_pokemon.toi_da_dang", "3"}, {"cho_pokemon.mua_ngay", "5"},
            {"cho_pokemon.khoa_gio", "72"}, {"cho_pokemon.han_gio", "48"}, {"cho_pokemon.he_so_sac_mau", "3"}, {"cho_pokemon.he_so_3_sao", "2"},
        };
        for (final String[] x : khoa) {
            Kiem.dung("danh muc cau hinh: " + x[0] + " mac dinh " + x[1],
                    dm.matches("(?s).*\"khoa\": \"" + x[0].replace(".", "\\.") + "\"[^}]*\"macDinh\": \"" + x[1] + "\".*"));
        }
        Kiem.dung("thue cho Pokemon (8%) >= thue cho ky gui (thue.cho)", dm.matches("(?s).*\"khoa\": \"thue\\.cho\"[^}]*\"macDinh\": \"[0-8]\".*"));
    }
}
