import com.htbb.pokemon.GiftcodeAnToanLogic;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.TuDong;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import real.Char;
import real.DungVatPham;
import real.Item;

/**
 * VE TREO 10 GIO + giftcode TREO400 (yeu cau chu game 31/10/2026: "400 giờ treo game" + 1.000 xu, 7 ngay, moi tai
 * khoan 1 lan). Kiem:
 *   - kho gio treo (TuDong.Phien.msKho): tru SAU gio mien phi ngay, het ca hai thi tat; doi ngay giu kho; tran 500 gio;
 *     goi -100 "con lai" <= "toi da" (client cu); luu HoSoLauDai a3 (dang nhap lai van con);
 *   - dung ve tu hanh trang (lenh 29, DungVatPham -> VatPhamDungLauDai gio_treo -> TuDongGame.dungVeTreo): chong giam 1,
 *     ve le bo khoi tui, kho day thi tu choi va KHONG mat ve, client cu (hop thoai 37) / moi (-104) deu thay chu;
 *   - giftcode: XU:n + ITEM:1260:n (xep chong 1 o), gia tri, tran, tach phan phat, giu luat moi tai khoan 1 lan
 *     (ghi log truoc tru luot truoc phat) + tep SQL van hanh treo400 + khoi SQL vat pham 1260.
 */
public final class KiemVeTreo {

    private KiemVeTreo() {
    }

    static final int NGAY = 20261031;
    static final long T0 = 1_800_000_000_000L;
    static final long GIO = 3600000L;

    public static void chay() {
        Kiem.nhom("Ve treo 10 gio: kho gio Tu dong danh + giftcode TREO400 (XU + ve xep chong)");
        try {
            khoGio();
            hoSo();
            giftcodeLogic();
            tepSql();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi (thuan): " + e, false);
        }
        Object bangCu = null;
        Map<Integer, HoSoLauDai> cache = null;
        final boolean ldCu = com.htbb.pokemon.LauDai.bat();
        try {
            bangCu = chuanBi();
            cache = cache();
            dungVe(cache);
            traoVe();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi (game): " + e, false);
        } finally {
            try {
                if (bangCu != null) {
                    final Field fb = real.VatPhamDungLauDai.class.getDeclaredField("BANG");
                    fb.setAccessible(true);
                    fb.set(null, bangCu);
                }
                if (cache != null) {
                    cache.remove(Integer.valueOf(DBID));
                    cache.remove(Integer.valueOf(DBID + 1));
                }
            } catch (final Exception ignored) {
            }
            com.htbb.pokemon.LauDai.dat(ldCu);
            real.Map.gemTemplate = KiemDungVatPham.gemCu;
            KiemDungVatPham.traLai();
        }
    }

    // ------------------------------------------------------------------ thuan: TuDong

    static void khoGio() {
        final TuDong.CauHinh c = new TuDong.CauHinh(); // 5 gio / ngay
        final TuDong.Phien p = new TuDong.Phien();
        final TuDong.NguCanh n = new TuDong.NguCanh();
        n.map = 12;
        n.x = 400;
        n.y = 300;
        p.ngay = NGAY;
        p.msDung = 5 * GIO;
        Kiem.bang("het 5 gio mien phi, kho rong: con 0", 0, TuDong.giayConLai(c, p, false, NGAY));
        Kiem.bang("het gio + kho rong: khong bat duoc", TuDong.lyDo(TuDong.TAT_HET_GIO), TuDong.kiemBat(c, p, n, NGAY));
        Kiem.bang("them 10 gio vao kho: duoc", null, TuDong.kiemThemKho(p, 10 * GIO));
        Kiem.bang("kho sau khi them = 10 gio", Long.valueOf(10 * GIO), Long.valueOf(TuDong.themKho(p, 10 * GIO)));
        Kiem.bang("con lai = kho 10 gio", 10 * 3600, TuDong.giayConLai(c, p, false, NGAY));
        Kiem.bang("co kho: bat duoc du het gio mien phi", null, TuDong.kiemBat(c, p, n, NGAY));
        Kiem.bang("goi -100: toi da = 5 + 10 gio (con lai <= toi da cho client cu)", 15 * 3600, TuDong.giayToiDaHien(c, p, false));
        // thu tu tru: ngay moi -> mien phi truoc, kho giu nguyen
        final TuDong.Phien q = new TuDong.Phien();
        q.ngay = NGAY;
        TuDong.themKho(q, 10 * GIO);
        TuDong.bat(c, q, n, 0, T0);
        Kiem.sai("60 giay dau: chua het gio", TuDong.tich(c, q, false, T0 + 60000L, NGAY));
        Kiem.bang("60 giay dau tru gio mien phi", Long.valueOf(60000L), Long.valueOf(q.msDung));
        Kiem.bang("60 giay dau: kho chua bi tru", Long.valueOf(10 * GIO), Long.valueOf(q.msKho));
        Kiem.bang("con lai = 5 gio - 1 phut + 10 gio", 15 * 3600 - 60, TuDong.giayConLai(c, q, false, NGAY));
        Kiem.bang("mien phi con lai = 5 gio - 1 phut", 5 * 3600 - 60, TuDong.giayMienPhiConLai(c, q, false, NGAY));
        // vat qua moc mien phi: phan vuot tru kho
        q.msDung = 5 * GIO - 30000L;
        Kiem.sai("vuot moc mien phi 30 giay: van chay (con kho)", TuDong.tich(c, q, false, T0 + 120000L, NGAY));
        Kiem.bang("mien phi chot o 5 gio", Long.valueOf(5 * GIO), Long.valueOf(q.msDung));
        Kiem.bang("kho bi tru dung 30 giay", Long.valueOf(10 * GIO - 30000L), Long.valueOf(q.msKho));
        Kiem.dung("van dang chay", q.bat);
        // the thang: tran 7 gio -> chua tru kho
        final TuDong.Phien r = new TuDong.Phien();
        r.ngay = NGAY;
        r.msDung = 5 * GIO;
        TuDong.themKho(r, GIO);
        TuDong.bat(c, r, n, 0, T0);
        TuDong.tich(c, r, true, T0 + 60000L, NGAY);
        Kiem.bang("the thang (7 gio): 5 gio + 1 phut van la mien phi, kho nguyen", Long.valueOf(GIO), Long.valueOf(r.msKho));
        // het kho -> tat het gio
        q.msKho = 20000L;
        Kiem.dung("het kho: tich bao het gio", TuDong.tich(c, q, false, T0 + 180000L, NGAY));
        Kiem.bang("het kho: ly do het gio", Integer.valueOf(TuDong.TAT_HET_GIO), Integer.valueOf(q.lyDoTat));
        Kiem.bang("het kho: kho = 0", Long.valueOf(0L), Long.valueOf(q.msKho));
        Kiem.sai("het kho: da tat", q.bat);
        // doi ngay: mien phi lam moi, kho giu
        final TuDong.Phien d = new TuDong.Phien();
        d.ngay = NGAY;
        d.msDung = 5 * GIO;
        TuDong.themKho(d, 3 * GIO);
        Kiem.bang("doi ngay: con lai = 5 gio moi + 3 gio kho", 8 * 3600, TuDong.giayConLai(c, d, false, NGAY + 1));
        Kiem.bang("doi ngay: kho khong mat", Long.valueOf(3 * GIO), Long.valueOf(d.msKho));
        // tran kho 500 gio
        final TuDong.Phien t = new TuDong.Phien();
        t.msKho = 490 * GIO;
        Kiem.bang("490 + 10 = 500 gio: duoc", null, TuDong.kiemThemKho(t, 10 * GIO));
        t.msKho = 495 * GIO;
        Kiem.dung("495 + 10 gio: vuot tran 500, tu choi", TuDong.kiemThemKho(t, 10 * GIO) != null
                && TuDong.kiemThemKho(t, 10 * GIO).contains("500 giờ"));
        Kiem.bang("themKho kep tran 500 gio", Long.valueOf(TuDong.KHO_MS_TOI_DA), Long.valueOf(TuDong.themKho(t, 10 * GIO)));
        Kiem.dung("ve 0 gio: tu choi", TuDong.kiemThemKho(new TuDong.Phien(), 0L) != null);
        Kiem.bang("gioPhut", "12 giờ 05 phút", TuDong.gioPhut(12 * GIO + 5 * 60000L));
        Kiem.bang("gioPhut duoi 1 gio", "40 phút", TuDong.gioPhut(40 * 60000L));
        Kiem.bang("tran kho 500 gio, 10 gio / ve, id 1260", "500/10/1260",
                TuDong.KHO_GIO_TOI_DA + "/" + TuDong.GIO_MOI_VE_TREO + "/" + TuDong.ID_VE_TREO);
    }

    static void hoSo() {
        final HoSoLauDai h = new HoSoLauDai();
        final TuDong.Phien p = new TuDong.Phien();
        p.ngay = NGAY;
        p.msDung = 1234L;
        p.msKho = 400 * GIO;
        TuDong.ghiHoSo(p, h);
        Kiem.bang("ghiHoSo: kho vao a3", Long.valueOf(400 * GIO), Long.valueOf(h.tdMsKho));
        Kiem.dung("ghiHoSo: danh dau can luu", h.ban);
        final String s = h.maHoa();
        Kiem.dung("maHoa co a3", s.contains("a3=" + 400 * GIO));
        // dang nhap lai: ho so moi tu chuoi luu, phien moi
        final TuDong.Phien moi = new TuDong.Phien();
        TuDong.napTuHoSo(moi, HoSoLauDai.giaiMa(s));
        Kiem.bang("dang nhap lai: kho con nguyen", Long.valueOf(400 * GIO), Long.valueOf(moi.msKho));
        Kiem.bang("dang nhap lai: bo dem ngay giu", Long.valueOf(1234L), Long.valueOf(moi.msDung));
        Kiem.bang("ho so cu khong co a3: kho 0", Long.valueOf(0L), Long.valueOf(HoSoLauDai.giaiMa("a1=" + NGAY + ";a2=5").tdMsKho));
        final HoSoLauDai h2 = new HoSoLauDai();
        h2.ban = false;
        p.msKho = 400 * GIO - 1;
        TuDong.ghiHoSo(p, h2);
        TuDong.ghiHoSo(p, h);
        Kiem.dung("chi doi kho cung danh dau luu", h.ban && h.tdMsKho == 400 * GIO - 1);
    }

    // ------------------------------------------------------------------ thuan: giftcode

    static void giftcodeLogic() {
        final String chuan = GiftcodeAnToanLogic.chuanHoaThuong(" xu:1000 , item:1260:40 ");
        Kiem.bang("TREO400 chuan hoa", "XU:1000,ITEM:1260:40", chuan);
        Kiem.bang("TREO400 gia tri = ceil(1000/1000) + 40 x 1", Integer.valueOf(41), Integer.valueOf(GiftcodeAnToanLogic.giaTri(chuan)));
        Kiem.bang("TREO400 trong tran may chu that (150)", null, GiftcodeAnToanLogic.kiem("XU:1000,ITEM:1260:40", 150));
        Kiem.dung("TREO400 vuot tran mac dinh 30 (may chu moi phai nang tran)", GiftcodeAnToanLogic.kiem("XU:1000,ITEM:1260:40", 30) != null);
        Kiem.bang("ve treo trong danh sach trang", null, GiftcodeAnToanLogic.kiemVatPham(chuan, 1, id -> 1));
        Kiem.bang("XU:1 = 1 diem", Integer.valueOf(1), Integer.valueOf(GiftcodeAnToanLogic.giaTri("XU:1")));
        Kiem.bang("XU:2500 = 3 diem", Integer.valueOf(3), Integer.valueOf(GiftcodeAnToanLogic.giaTri("XU:2500")));
        Kiem.bang("XU tran 50.000", "XU:50000", GiftcodeAnToanLogic.chuanHoaThuong("XU:50000"));
        Kiem.bang("XU 50.001 bi tu choi", null, GiftcodeAnToanLogic.chuanHoaThuong("XU:50001"));
        Kiem.bang("XU 0 bi tu choi", null, GiftcodeAnToanLogic.chuanHoaThuong("XU:0"));
        Kiem.bang("XU am bi tu choi", null, GiftcodeAnToanLogic.chuanHoaThuong("XU:-5"));
        Kiem.bang("LUONG mo van cam", null, GiftcodeAnToanLogic.chuanHoaThuong("XU:1000,LUONG:1"));
        Kiem.bang("ve treo toi da 50 / ma", "ITEM:1260:50", GiftcodeAnToanLogic.chuanHoaThuong("ITEM:1260:50"));
        Kiem.bang("ve treo 51 bi tu choi", null, GiftcodeAnToanLogic.chuanHoaThuong("ITEM:1260:51"));
        Kiem.bang("vat pham thuong van <= 10", null, GiftcodeAnToanLogic.chuanHoaThuong("ITEM:1201:11"));
        Kiem.bang("ITEM thuong van 5 / mon", Integer.valueOf(50), Integer.valueOf(GiftcodeAnToanLogic.giaTri("ITEM:1201:10")));
        Kiem.bang("tach: phan applyItemString bo XU + ve", "LUONGKHOA:5,ITEM:1201:2",
                GiftcodeAnToanLogic.phanThuongThuong("XU:1000,LUONGKHOA:5,ITEM:1260:40,ITEM:1201:2"));
        Kiem.bang("tach: chi XU + ve -> rong", "", GiftcodeAnToanLogic.phanThuongThuong(chuan));
        Kiem.bang("tong xu", Long.valueOf(1000L), Long.valueOf(GiftcodeAnToanLogic.tongXu(chuan)));
        Kiem.bang("so ve", Integer.valueOf(40), Integer.valueOf(GiftcodeAnToanLogic.soVeTreo(chuan)));
        Kiem.bang("o can: 40 ve = 1 o (chua co chong)", Integer.valueOf(1), Integer.valueOf(GiftcodeAnToanLogic.soOCan(chuan, null, x -> 1)));
        Kiem.bang("o can: da co chong ve = 0 o", Integer.valueOf(0), Integer.valueOf(GiftcodeAnToanLogic.soOCan(chuan, null, x -> 0)));
        Kiem.bang("o can: khong biet tui = 1 o / muc ve", Integer.valueOf(1), Integer.valueOf(GiftcodeAnToanLogic.soOCan(chuan, null)));
        Kiem.bang("o can: XU khong can o", Integer.valueOf(0), Integer.valueOf(GiftcodeAnToanLogic.soOCan("XU:1000", null)));
        Kiem.bang("ma TREO400 hop le (chu thuong)", "treo400", GiftcodeAnToanLogic.chuanHoaMa("TREO400"));
        // luat chong lam dung giu nguyen o noi phat (doc nguon)
        final String at = doc("src/real/GiftcodeAnToan.java");
        final int log = at.indexOf("ghiLogGiftcode("), tru = at.indexOf("truLuotGiftcode("), xu = at.indexOf("p.addXu(xu"),
                ve = at.indexOf("TuDongGame.traoVeTreo("), o = at.indexOf("TuDongGame.soOCanVe(");
        Kiem.dung("moi tai khoan 1 lan: ghi log (ma, user_id) TRUOC tru luot TRUOC phat xu / ve", log > 0 && tru > log && xu > tru && ve > tru);
        Kiem.dung("kiem o trong (ve xep chong) TRUOC khi ghi log", o > 0 && o < log);
        Kiem.dung("xu giftcode dem xu_vao (XU_DEM)", at.contains("p.addXu(xu, \"giftcode \" + ma, Char.XU_DEM)"));
        Kiem.dung("van kiem tran gia tri + ngan sach + khoa do ma", at.contains("giftcode.gia_tri_toi_da") && at.contains("tongGiftcodeThang(")
                && at.contains("ghiSai(p.userID)") && at.contains("GiftcodeLauDai.choPhep(p)"));
        // ban TS (web) cung luat
        final String ts = doc("../web/server/utils/giftcodeAnToan.ts");
        Kiem.dung("web TS: XU + ve treo cung tran / gia tri", ts.contains("GC_ID_VE_TREO = 1260") && ts.contains("GC_VE_TREO_TOI_DA = 50")
                && ts.contains("GC_XU_TOI_DA = 50000") && ts.contains("Math.ceil(Number(t[1]) / GC_XU_MOI_GIA_TRI)"));
    }

    static void tepSql() {
        final String sql = doc("../vps/van_hanh/giftcode_treo400.sql");
        Kiem.dung("treo400: INSERT IGNORE (chay lai duoc)", sql.contains("INSERT IGNORE INTO `tob_giftcode`"));
        Kiem.dung("treo400: phan thuong XU:1000,ITEM:1260:40", sql.contains("'treo400', 'XU:1000,ITEM:1260:40'"));
        Kiem.dung("treo400: han 7 ngay tu luc chay", sql.contains("INTERVAL 7 DAY"));
        Kiem.dung("treo400: 1000 luot, cap 1, bat", sql.contains(", 1000, 0, 1, 1,"));
        Kiem.dung("treo400: chi chen khi da co vat pham 1260", sql.contains("WHERE `id` = 1260") && sql.contains("gio_treo:%"));
        final String khoi = DayDu.phanNangCap("them_ve_treo.sql");
        Kiem.dung("khoi SQL them_ve_treo.sql co trong phan B", khoi.length() > 100);
        Kiem.dung("khoi SQL: data_item 1260 type 28, gia 0, INSERT IGNORE", khoi.contains("INSERT IGNORE INTO `data_item`")
                && khoi.contains("(1260,'Vé treo 10 giờ',28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14293,"));
        Kiem.dung("khoi SQL: data_vat_pham_lau_dai 1260 gio_treo:10", khoi.contains("INSERT IGNORE INTO `data_vat_pham_lau_dai`")
                && khoi.contains("(1260,'ve_treo_10h','Vé treo 10 giờ','tien_ich','gio_treo:10',"));
        Kiem.sai("khoi SQL: khong chen giftcode vao CSDL moi", khoi.contains("tob_giftcode"));
    }

    // ------------------------------------------------------------------ game: dung ve tu hanh trang

    static final int DBID = 987650;

    /** Dung lai dung cu cua KiemDungVatPham (ban do, mau vat pham, bang data_vat_pham_lau_dai) + dong 1260. */
    @SuppressWarnings("unchecked")
    static Object chuanBi() throws Exception {
        KiemDungVatPham.chuanBi(); // LauDai bat, gemTemplate, map, BANG rieng (traLai tra ve ban cu)
        final Field fb = real.VatPhamDungLauDai.class.getDeclaredField("BANG");
        fb.setAccessible(true);
        ((java.util.Map<Integer, Object[]>) fb.get(null)).put(1260, new Object[]{"ve_treo_10h", "tien_ich", "gio_treo:10", "", 1, 100});
        KiemDungVatPham.mau(1260, 28, "Vé treo 10 giờ", 1);
        return null;
    }

    @SuppressWarnings("unchecked")
    static Map<Integer, HoSoLauDai> cache() throws Exception {
        final Field f = real.LauDaiNhanVat.class.getDeclaredField("CACHE");
        f.setAccessible(true);
        return (Map<Integer, HoSoLauDai>) f.get(null);
    }

    static Char nv(final Map<Integer, HoSoLauDai> cache, final int dbid, final HoSoLauDai h) {
        final Char p = KiemDungVatPham.nhanVat();
        p.charDBID = dbid;
        p.isBot = -1;
        cache.put(Integer.valueOf(dbid), h);
        return p;
    }

    static Item ve(final Char p, final int soLuong) {
        final Item it = KiemDungVatPham.cho(p, 1260);
        it.lock = 1;
        it.quanlity = soLuong;
        return it;
    }

    /** Goi -100 trang thai cuoi: {con lai, toi da, thong bao}. */
    static Object[] trangThai() throws Exception {
        Object[] kq = null;
        for (final Message m : KiemDungVatPham.GOI) {
            if (m.command == TuDong.LENH) {
                final byte[] b = m.toByteArray();
                final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                if (r.readByte() != 0) {
                    continue;
                }
                r.readByte();
                final int con = r.readInt(), toiDa = r.readInt();
                r.readShort();
                r.readByte();
                kq = new Object[]{Integer.valueOf(con), Integer.valueOf(toiDa), r.readUTF()};
            }
        }
        return kq;
    }

    static void dungVe(final Map<Integer, HoSoLauDai> cache) throws Exception {
        final HoSoLauDai h = new HoSoLauDai();
        final Char p = nv(cache, DBID, h);
        final Item chong = ve(p, 3);
        final String[] b = KiemDungVatPham.dung(p, chong.id);
        Kiem.dung("dung ve (client moi): thong bao +10 gio + kho", b != null && b[1].contains("+10 giờ") && b[1].contains("Kho giờ treo: 10 giờ"));
        Kiem.dung("dung ve: bao con 2 ve", b != null && b[1].contains("Còn 2 vé"));
        // ChongVatPham: DungVatPham tach 1 don vi de dung, phan con lai giu id cu (o phim tat / client tro dung chong)
        final short idChong = chong.id;
        Kiem.bang("chong 3 -> 2 (MOT lan tru, khong tru doi)", Integer.valueOf(2),
                Integer.valueOf(com.htbb.pokemon.ChongVatPham.dem(p.iItems, 1260)));
        Kiem.dung("chong con lai van trong tui, cung id, 1 o", theoId(p, idChong) != null && theoId(p, idChong).quanlity == 2
                && demDong(p, 1260) == 1);
        Kiem.bang("ho so: kho 10 gio (a3)", Long.valueOf(10 * GIO), Long.valueOf(h.tdMsKho));
        final Object[] tt = trangThai();
        Kiem.dung("goi -100 sau khi dung: con lai >= 10 gio, <= toi da", tt != null && ((Integer) tt[0]).intValue() >= 10 * 3600
                && ((Integer) tt[0]).intValue() <= ((Integer) tt[1]).intValue());
        Kiem.dung("goi -100: dong kho gio treo cho bang Tu dong", tt != null && ((String) tt[2]).contains("Kho giờ treo"));
        final String[] b2 = KiemDungVatPham.dung(p, idChong);
        Kiem.bang("dung lan 2: kho 20 gio", Long.valueOf(20 * GIO), Long.valueOf(h.tdMsKho));
        Kiem.dung("dung lan 2: bao con 1 ve", b2 != null && b2[1].contains("Còn 1 vé"));
        final String[] b3 = KiemDungVatPham.dung(p, idChong);
        Kiem.bang("dung ve cuoi: kho 30 gio", Long.valueOf(30 * GIO), Long.valueOf(h.tdMsKho));
        Kiem.bang("ve cuoi bo khoi tui", Integer.valueOf(0), Integer.valueOf(com.htbb.pokemon.ChongVatPham.dem(p.iItems, 1260)));
        Kiem.dung("ve cuoi: khong bao 'Còn'", b3 != null && !b3[1].contains("Còn "));
        // dang nhap lai: Char moi, phien moi nap tu chuoi luu
        final HoSoLauDai luu = HoSoLauDai.giaiMa(h.maHoa());
        final Char p2 = nv(cache, DBID, luu);
        final Item v2 = ve(p2, 1);
        KiemDungVatPham.dung(p2, v2.id);
        Kiem.bang("dang nhap lai: kho cu 30 gio + ve moi 10 gio", Long.valueOf(40 * GIO), Long.valueOf(luu.tdMsKho));
        // tran kho: khong mat ve
        luu.tdMsKho = 495 * GIO;
        final Char p3 = nv(cache, DBID, luu); // Char moi = phien moi nap 495 gio
        final Item v3 = ve(p3, 5);
        final String[] b4 = KiemDungVatPham.dung(p3, v3.id);
        Kiem.dung("kho 495 gio: tu choi (tran 500)", b4 != null && "LOI".equals(b4[0]) && b4[1].contains("500 giờ"));
        Kiem.bang("tu choi: khong tru ve", Integer.valueOf(5), Integer.valueOf(com.htbb.pokemon.ChongVatPham.dem(p3.iItems, 1260)));
        Kiem.bang("tu choi: kho khong doi", Long.valueOf(495 * GIO), Long.valueOf(luu.tdMsKho));
        // client cu (0.9.21 / 0.9.22 / J2ME): hop thoai 37
        final Char cu = nv(cache, DBID + 1, new HoSoLauDai());
        cu.getSession().banClient = 0;
        final Item v4 = ve(cu, 1);
        KiemDungVatPham.GOI.clear();
        DungVatPham.dung(KiemDungVatPham.map, cu, KiemDungVatPham.goi(29, true, v4.id));
        boolean thay = false;
        for (final Message m : KiemDungVatPham.GOI) {
            if (m.command == 37 && new String(m.toByteArray(), StandardCharsets.UTF_8).contains("Kho giờ treo")) {
                thay = true;
            }
        }
        Kiem.dung("client cu: dung ve tu tui, hop thoai 37 bao kho gio treo", thay);
        Kiem.sai("client cu: ve da tru", cu.iItems.contains(v4));
        // cloneItem giu so luong chong (kho Kangaskhan / cho)
        final Item c5 = ve(cu, 7);
        Kiem.bang("cloneItem giu so luong chong", Integer.valueOf(7), Integer.valueOf(c5.cloneItem().quanlity));
    }

    static Item theoId(final Char p, final short id) {
        for (final Item it : p.iItems) {
            if (it.id == id) {
                return it;
            }
        }
        return null;
    }

    static int demDong(final Char p, final int tpl) {
        int n = 0;
        for (final Item it : p.iItems) {
            if (it.tempateID == tpl) {
                n++;
            }
        }
        return n;
    }

    static void traoVe() throws Exception {
        final Char p = KiemDungVatPham.nhanVat();
        p.charDBID = DBID + 5;
        Kiem.bang("tui rong: 40 ve can 1 o", Integer.valueOf(1), Integer.valueOf(real.TuDongGame.soOCanVe(p, 1260, 40)));
        final int truoc = p.iItems.size();
        Kiem.bang("trao 40 ve", Integer.valueOf(40), Integer.valueOf(real.TuDongGame.traoVeTreo(p, 1260, 40)));
        Kiem.bang("40 ve = 1 o", Integer.valueOf(truoc + 1), Integer.valueOf(p.iItems.size()));
        Item chong = null;
        for (final Item it : p.iItems) {
            if (it.getTemplate() != null && it.getTemplate().id == 1260) {
                chong = it;
            }
        }
        Kiem.dung("chong ve KHOA (khong giao dich), so luong 40", chong != null && chong.lock == 1 && chong.quanlity == 40);
        Kiem.bang("da co chong: them 40 ve can 0 o", Integer.valueOf(0), Integer.valueOf(real.TuDongGame.soOCanVe(p, 1260, 40)));
        real.TuDongGame.traoVeTreo(p, 1260, 40);
        Kiem.bang("nhan lan 2 don vao chong (80, van 1 o)", Integer.valueOf(truoc + 1) + "/80", Integer.valueOf(p.iItems.size()) + "/" + chong.quanlity);
        final String[] cot = chong.getDBInfo().split(",");
        Kiem.dung("chuoi luu (getDBInfo) giu so luong 80 o cot 29", cot.length > 29 && "80".equals(cot[29]));
        final java.lang.reflect.Method moTa = real.GiftcodeAnToan.class.getDeclaredMethod("moTa", String.class);
        moTa.setAccessible(true);
        final String mt = (String) moTa.invoke(null, "XU:1000,ITEM:1260:40");
        Kiem.bang("thong bao doi giftcode", "+1.000 xu, +40 Vé treo 10 giờ", mt);
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
