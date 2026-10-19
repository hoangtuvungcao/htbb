import com.htbb.pokemon.MoRongHanhTrang;
import io.Message;
import io.Session;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import real.Char;
import real.DungVatPham;
import real.GemTemplate;
import real.HanhDongVatPham;
import real.Item;
import real.ItemTemplates;
import real.MoRongTuiGame;

/**
 * Mo rong hanh trang (ve +10 o / mua xu gia tang dan, tran 126) va kiem toan vut / ban vat pham (19/10/2026):
 * com.htbb.pokemon.MoRongHanhTrang, real.MoRongTuiGame, goi -105 noi them, lenh -107, so luong tuy chon -21 / -23 / 78,
 * cac duong them vao tui da va (khong tran suc chua). Khong CSDL, khong mang.
 */
public final class KiemMoRongTui {

    private KiemMoRongTui() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static short idKe = 21000;
    static GemTemplate[] gemCu;

    public static void chay() {
        Kiem.nhom("Mo rong hanh trang (ve / xu) + vut / ban vat pham");
        gemCu = real.Map.gemTemplate;
        try {
            final GemTemplate[] g = new GemTemplate[MoRongTuiGame.ID_VE + 1];
            for (int i = 0; i < g.length; i++) {
                g[i] = new GemTemplate();
                g[i].id = (short) i;
                g[i].name = "Nguyên liệu " + i;
            }
            g[MoRongTuiGame.ID_VE] = MoRongTuiGame.taoMauVe();
            real.Map.gemTemplate = g;
            while (real.Map.itemTemplates.size() < 6) {
                real.Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
            }
            thuan();
            dungVe();
            muaXu();
            goiTrangThai();
            lenhMoi();
            tranNap();
            vutThuoc();
            vutNguyenLieu();
            banNguyenLieu();
            vutVatPham();
            duongThemVaoTui();
            maNguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
            for (int i = 0; i < 6 && i < real.Map.itemTemplates.size(); i++) {
                real.Map.itemTemplates.get(i).remove(Integer.valueOf(MAU_DO));
            }
        }
    }

    static Char nhanVat() {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.getSession().banClient = HanhDongVatPham.BAN_TRANG_THAI;
        p.charname = "kiemmorong" + (idKe++);
        p.lvDetail.lv = 30;
        p.maxhp = 100;
        p.hp = 100;
        p.MAX_INVENTORY = 42;
        p.maxBag = 1;
        p.oMoRong = 0;
        return p;
    }

    static Message goi(final int lenh, final int... giaTri) throws Exception {
        // giaTri: cap (so byte, gia tri)
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(b);
        for (int i = 0; i + 1 < giaTri.length; i += 2) {
            if (giaTri[i] == 1) {
                d.writeByte(giaTri[i + 1]);
            } else {
                d.writeShort(giaTri[i + 1]);
            }
        }
        d.flush();
        return new Message(lenh, b.toByteArray());
    }

    /** Noi dung -104 cuoi cung (null neu khong co). */
    static String toastCuoi() {
        for (int i = GOI.size() - 1; i >= 0; i--) {
            final Message m = GOI.get(i);
            if (m.command == DungVatPham.LENH) {
                final String[] t = docToast(m);
                return t == null ? null : t[0] + "|" + t[1];
            }
        }
        return null;
    }

    static String[] docToast(final Message m) {
        try {
            final byte[] b = m.toByteArray();
            final int dau = (b.length > 0 && b[0] == (byte) -128) ? 6 : 3;
            final java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(b, dau, b.length - dau));
            final byte loai = r.readByte();
            return new String[]{String.valueOf(loai), r.readUTF()};
        } catch (final Exception e) {
            return null;
        }
    }

    static boolean coGoi(final int lenh) {
        for (final Message m : GOI) {
            if (m.command == lenh) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ ham thuan

    static void thuan() {
        Kiem.bang("suc chua 1 trang", 42, MoRongHanhTrang.sucChua(42, 1, 0));
        Kiem.bang("suc chua 1 trang + 10 o le", 52, MoRongHanhTrang.sucChua(42, 1, 10));
        Kiem.bang("tran ve 126 = 3 x 42", 126, MoRongHanhTrang.TRAN_VE);
        Kiem.bang("o le toi da 84", 84, MoRongHanhTrang.O_LE_TOI_DA);
        Kiem.bang("ve them 10 khi con xa tran", 10, MoRongHanhTrang.soOThem(42));
        Kiem.bang("ve them phan con thieu gan tran (122 -> +4)", 4, MoRongHanhTrang.soOThem(122));
        Kiem.bang("cham tran: 0", 0, MoRongHanhTrang.soOThem(126));
        Kiem.bang("vuot tran (5 trang luong): 0", 0, MoRongHanhTrang.soOThem(210));
        Kiem.bang("tran hien thi khi vuot = suc chua that", 210, MoRongHanhTrang.tranHienThi(210));
        Kiem.bang("gia buoc 0", 20000L, MoRongHanhTrang.giaXu(0));
        boolean tang = true;
        for (int b = 1; b < 12; b++) {
            tang &= MoRongHanhTrang.giaXu(b) > MoRongHanhTrang.giaXu(b - 1);
        }
        Kiem.dung("gia xu tang dan moi buoc", tang);
        long tong = 0;
        for (int b = 0; b < 9; b++) {
            tong += MoRongHanhTrang.giaXu(b);
        }
        Kiem.bang("tong 9 buoc (42 -> 126 o)", 1493000L, tong);
        Kiem.bang("gia ke tiep khi cham tran = -1", -1L, MoRongHanhTrang.giaXuKeTiep(126, 84));
        Kiem.bang("kep o le am", 0, MoRongHanhTrang.kep(-5));
        Kiem.bang("kep o le qua tran", 84, MoRongHanhTrang.kep(500));
        Kiem.bang("so trang client cu: 52 o -> 2 trang", 2, MoRongHanhTrang.soTrangHienThi(42, 52));
        Kiem.bang("so trang client cu: 126 o -> 3 trang", 3, MoRongHanhTrang.soTrangHienThi(42, 126));
        Kiem.dung("tran doc hanh trang >= 42 x 5 + 84 (khong cat mon khi dang nhap)",
                Char.MAX_ENTRY_INVEN_LOAD >= 42 * Char.MAX_BAG_LIMIT + MoRongHanhTrang.O_LE_TOI_DA);
        Kiem.dung("tran doc hanh trang > 210", Char.MAX_ENTRY_INVEN_LOAD > 210);
    }

    // ------------------------------------------------------------------ ve

    static void dungVe() {
        final Char p = nhanVat();
        Kiem.bang("ve: id mau 270", (int) MoRongTuiGame.ID_VE, (int) MoRongTuiGame.idVe());
        Kiem.bang("ve: mau kieu 4 (nut Dung o moi client)", (int) GemTemplate.TYPE_TU_BINH, (int) real.Map.gemTemplate[MoRongTuiGame.ID_VE].type);
        Kiem.dung("ve: khong co ve -> tu choi", MoRongTuiGame.dungVe(p, null) != null);
        p.doAddGemItem(MoRongTuiGame.ID_VE, 2, true);
        p.doAddGemItem(MoRongTuiGame.ID_VE, 1, false);
        Kiem.bang("ve: dem 3 ve (2 khoa + 1 thuong)", 3, MoRongTuiGame.soVe(p));
        Kiem.bang("ve: dung thanh cong", null, MoRongTuiGame.dungVe(p, null));
        Kiem.bang("ve: +10 o le", 10, (int) p.oMoRong);
        Kiem.bang("ve: suc chua 52", 52, p.sucChuaTui());
        Kiem.bang("ve: tru 1 ve khoa truoc", 1, p.listGemitemLock[MoRongTuiGame.ID_VE]);
        Kiem.bang("ve: ve thuong con nguyen", 1, p.listGemitem[MoRongTuiGame.ID_VE]);
        Kiem.bang("ve: dung dung loai thuong (goi -67 khoa=0)", null, MoRongTuiGame.dungVe(p, Boolean.FALSE));
        Kiem.bang("ve: ve thuong het", 0, p.listGemitem[MoRongTuiGame.ID_VE]);
        Kiem.dung("ve: het ve thuong -> tu choi loai thuong", MoRongTuiGame.dungVe(p, Boolean.FALSE) != null);
        // gan tran: 2 trang (84) + 40 = 124 -> ve chi them 2
        p.maxBag = 2;
        p.oMoRong = 40;
        Kiem.bang("ve: gan tran them phan con thieu", null, MoRongTuiGame.dungVe(p, null));
        Kiem.bang("ve: 124 -> 126", 126, p.sucChuaTui());
        p.doAddGemItem(MoRongTuiGame.ID_VE, 1, true);
        final int truoc = MoRongTuiGame.soVe(p);
        final String loi = MoRongTuiGame.dungVe(p, null);
        Kiem.dung("ve: cham tran -> tu choi co ly do", loi != null && loi.contains("tối đa"));
        Kiem.bang("ve: cham tran -> KHONG tru ve", truoc, MoRongTuiGame.soVe(p));
        // vuot tran bang trang luong: giu nguyen
        p.maxBag = 5;
        p.oMoRong = 0;
        Kiem.bang("ve: 5 trang luong giu 210 o", 210, p.sucChuaTui());
        Kiem.dung("ve: 5 trang luong -> khong dung ve duoc", MoRongTuiGame.dungVe(p, null) != null);
        // dang trao doi
        final Char q = nhanVat();
        q.doAddGemItem(MoRongTuiGame.ID_VE, 1, true);
        q.userTrade.add(nhanVat());
        Kiem.dung("ve: dang trao doi -> tu choi", MoRongTuiGame.dungVe(q, null) != null);
        Kiem.bang("ve: dang trao doi -> khong tru", 1, MoRongTuiGame.soVe(q));
        // suc chua tinh vao day tui
        final Char r = nhanVat();
        r.oMoRong = 10;
        for (int i = 0; i < 51; i++) {
            r.iItems.add(new Item());
        }
        Kiem.sai("tui 51/52 chua day", r.isFullInventory());
        Kiem.bang("tui 51/52 con 1 o", 1, r.countSlotInventory());
        r.iItems.add(new Item());
        Kiem.dung("tui 52/52 day", r.isFullInventory());
    }

    static void muaXu() {
        final Char p = nhanVat();
        p.setxu(1000000L);
        Kiem.bang("xu: mua lan 1", null, MoRongTuiGame.muaXu(p));
        Kiem.bang("xu: tru 20.000", 980000L, p.getxu());
        Kiem.bang("xu: +10 o", 52, p.sucChuaTui());
        Kiem.bang("xu: mua lan 2", null, MoRongTuiGame.muaXu(p));
        Kiem.bang("xu: lan 2 tru 30.000 (tang dan)", 950000L, p.getxu());
        // buoc da mua bang VE cung day gia len
        p.oMoRong = 50;
        Kiem.bang("xu: gia ke tiep sau 5 buoc", 150000L, MoRongHanhTrang.giaXuKeTiep(p.sucChuaTui(), p.oMoRong));
        p.setxu(1000L);
        final String loi = MoRongTuiGame.muaXu(p);
        Kiem.dung("xu: khong du xu -> tu choi", loi != null && loi.contains("Không đủ xu"));
        Kiem.bang("xu: khong du -> khong doi o", 50, (int) p.oMoRong);
        Kiem.bang("xu: khong du -> khong tru", 1000L, p.getxu());
        p.oMoRong = 84;
        p.setxu(100000000L);
        Kiem.dung("xu: cham tran -> tu choi", MoRongTuiGame.muaXu(p) != null);
        Kiem.bang("xu: cham tran -> khong tru", 100000000L, p.getxu());
    }

    // ------------------------------------------------------------------ giao thuc

    static void goiTrangThai() throws Exception {
        final Char p = nhanVat();
        p.oMoRong = 20;
        p.doAddGemItem(MoRongTuiGame.ID_VE, 3, true);
        final long[] v = HanhDongVatPham.docTrangThai(HanhDongVatPham.taoTrangThai(p, HanhDongVatPham.MO_RONG), new int[256]);
        Kiem.bang("-105: 19 truong", 19, v.length);
        Kiem.bang("-105: kieu MO_RONG", (long) HanhDongVatPham.MO_RONG, v[0]);
        Kiem.bang("-105: oDaMo", 62L, v[12]);
        Kiem.bang("-105: oToiDaVe", 126L, v[13]);
        Kiem.bang("-105: oMoiVe", 10L, v[14]);
        Kiem.bang("-105: giaXuBuocKe (buoc 2)", 45000L, v[15]);
        Kiem.bang("-105: soVe", 3L, v[16]);
        Kiem.bang("-105: idVe", 270L, v[17]);
        Kiem.bang("-105: loaiVe = nguyen lieu", (long) MoRongTuiGame.LOAI_NGUYEN_LIEU, v[18]);
        p.maxBag = 3;
        p.oMoRong = 0;
        final long[] w = HanhDongVatPham.docTrangThai(HanhDongVatPham.taoTrangThai(p, (byte) 0), null);
        Kiem.bang("-105: cham tran -> gia -1", -1L, w[15]);
        p.maxBag = 5;
        final long[] x = HanhDongVatPham.docTrangThai(HanhDongVatPham.taoTrangThai(p, (byte) 0), null);
        Kiem.bang("-105: vuot tran -> oToiDaVe = suc chua", 210L, x[13]);
    }

    static void lenhMoi() throws Exception {
        final Char p = nhanVat();
        p.doAddGemItem(MoRongTuiGame.ID_VE, 1, false);
        GOI.clear();
        MoRongTuiGame.xuLy(p, goi(MoRongTuiGame.LENH, 1, MoRongTuiGame.KIEU_DUNG_VE));
        Kiem.bang("-107 kieu 1: +10 o", 52, p.sucChuaTui());
        Kiem.dung("-107 kieu 1: tra -104 OK", String.valueOf(toastCuoi()).startsWith("0|"));
        Kiem.dung("-107 kieu 1: tra -105", coGoi(HanhDongVatPham.LENH_TRANG_THAI));
        GOI.clear();
        MoRongTuiGame.xuLy(p, goi(MoRongTuiGame.LENH, 1, MoRongTuiGame.KIEU_DUNG_VE));
        Kiem.dung("-107 kieu 1 het ve: -104 LOI", String.valueOf(toastCuoi()).startsWith("1|"));
        Kiem.dung("-107 kieu 1 het ve: van tra -105", coGoi(HanhDongVatPham.LENH_TRANG_THAI));
        GOI.clear();
        p.setxu(25000L);
        MoRongTuiGame.xuLy(p, goi(MoRongTuiGame.LENH, 1, MoRongTuiGame.KIEU_MUA_XU));
        Kiem.dung("-107 kieu 2: thieu xu (can 30.000) -> LOI", String.valueOf(toastCuoi()).startsWith("1|"));
        Kiem.bang("-107 kieu 2: thieu xu -> giu xu", 25000L, p.getxu());
        GOI.clear();
        MoRongTuiGame.xuLy(p, goi(MoRongTuiGame.LENH, 1, MoRongTuiGame.KIEU_TRANG_THAI));
        Kiem.dung("-107 kieu 0: chi tra -105", coGoi(HanhDongVatPham.LENH_TRANG_THAI) && !coGoi(DungVatPham.LENH));
        GOI.clear();
        MoRongTuiGame.xuLy(p, new Message(MoRongTuiGame.LENH, new byte[0]));
        Kiem.dung("-107 rong: coi nhu kieu 0", coGoi(HanhDongVatPham.LENH_TRANG_THAI));
        p.getSession().banClient = 900;
        GOI.clear();
        MoRongTuiGame.guiTrangThai(p);
        Kiem.sai("client cu (< 903): khong gui -105", coGoi(HanhDongVatPham.LENH_TRANG_THAI));
    }

    static void tranNap() {
        final Char p = nhanVat();
        Kiem.bang("cua hang: mua ve duoc", null, MoRongTuiGame.kiemMuaVe(p));
        p.doAddGemItem(MoRongTuiGame.ID_VE, 9, true);
        Kiem.dung("cua hang: giu 9 ve (du toi tran) -> khong ban them", MoRongTuiGame.kiemMuaVe(p) != null);
        final Char q = nhanVat();
        q.maxBag = 3;
        Kiem.dung("cua hang: 126 o -> khong ban ve", MoRongTuiGame.kiemMuaVe(q) != null);
        final Char r = nhanVat();
        for (int i = 0; i < 42; i++) {
            r.iItems.add(new Item());
        }
        Kiem.dung("cua hang: tui day, chua co ve -> can 1 o", String.valueOf(MoRongTuiGame.kiemMuaVe(r)).contains("ô trống"));
        Kiem.dung("NapTienLogic: ma ve_mo_rong_tui la ve", com.htbb.pokemon.NapTienLogic.laVeMoRong(
                new com.htbb.pokemon.NapTienLogic.Mon("ve_mo_rong_tui", "Vé", "tien_ich", "mo_rong_tui", 80, 0, 9, 0, false)));
        Kiem.sai("NapTienLogic: mo_rong_tui (trang) khong phai ve", com.htbb.pokemon.NapTienLogic.laVeMoRong(
                new com.htbb.pokemon.NapTienLogic.Mon("mo_rong_tui", "Mở rộng", "tien_ich", "mo_rong_tui", 300, 0, 4, 0, false)));
        boolean coMon = false;
        for (final com.htbb.pokemon.NapTienLogic.Mon m : com.htbb.pokemon.NapTienLogic.MON_MAC_DINH) {
            if (com.htbb.pokemon.NapTienLogic.laVeMoRong(m)) {
                coMon = com.htbb.pokemon.NapTienLogic.kiemMon(m) == null;
            }
        }
        Kiem.dung("cua hang luong: co mon ve hop le (kiemMon)", coMon);
    }

    // ------------------------------------------------------------------ vut / ban (kiem toan)

    static Object goiRieng(final Class<?> lop, final Object dt, final String ten, final Class<?>[] kieu, final Object... ts) throws Exception {
        final Method m = lop.getDeclaredMethod(ten, kieu);
        m.setAccessible(true);
        try {
            return m.invoke(dt, ts);
        } catch (final java.lang.reflect.InvocationTargetException e) {
            return e.getCause(); // vd NullPointerException o dong ghi log CSDL (khong co CSDL khi kiem)
        }
    }

    static void vutThuoc() throws Exception {
        final real.Map map = new real.Map();
        final Char p = nhanVat();
        p.passWord = "";
        p.potions[1] = 5;
        final Class<?>[] k = {Char.class, Message.class};
        goiRieng(real.Map.class, map, "doGivePotion2Ground", k, p, goi(-21, 2, 1, 2, 2));
        Kiem.bang("-21 so luong 2: con 3", 3, p.potions[1]);
        goiRieng(real.Map.class, map, "doGivePotion2Ground", k, p, goi(-21, 2, 1));
        Kiem.bang("-21 khong so luong: bo ca chong (nhu cu)", 0, p.potions[1]);
        GOI.clear();
        goiRieng(real.Map.class, map, "doGivePotion2Ground", k, p, goi(-21, 2, 1));
        Kiem.dung("-21 khong con: -104 LOI", String.valueOf(toastCuoi()).startsWith("1|"));
        GOI.clear();
        goiRieng(real.Map.class, map, "doGivePotion2Ground", k, p, goi(-21, 2, 9999));
        Kiem.dung("-21 id ngoai mang: -104 LOI (khong nem loi)", String.valueOf(toastCuoi()).startsWith("1|"));
        p.potions[88] = 1;
        GOI.clear();
        goiRieng(real.Map.class, map, "doGivePotion2Ground", k, p, goi(-21, 2, 88));
        Kiem.bang("-21 nhan Trai tim: khong bo", 1, p.potions[88]);
        Kiem.dung("-21 nhan Trai tim: -104 co ly do", String.valueOf(toastCuoi()).contains("không bỏ được"));
        p.potions[2] = 4;
        p.userTrade.add(nhanVat());
        goiRieng(real.Map.class, map, "doGivePotion2Ground", k, p, goi(-21, 2, 2));
        Kiem.bang("-21 dang trao doi: khong bo", 4, p.potions[2]);
        Kiem.bang("HanhDongVatPham.kepSoLuong(0, 7) = ca chong", 7, HanhDongVatPham.kepSoLuong(0, 7));
        Kiem.bang("HanhDongVatPham.kepSoLuong(99, 7) = 7", 7, HanhDongVatPham.kepSoLuong(99, 7));
        Kiem.bang("HanhDongVatPham.kepSoLuong(3, 7) = 3", 3, HanhDongVatPham.kepSoLuong(3, 7));
    }

    static void vutNguyenLieu() throws Exception {
        final real.Map map = new real.Map();
        final Char p = nhanVat();
        p.passWord = "";
        p.doAddGemItem(5, 10, false);
        p.doAddGemItem(6, 3, true);
        final Class<?>[] k = {real.Map.class, Char.class, Message.class};
        goiRieng(Class.forName("real.MapEpNgoc"), null, "doGiveSpecialGem2Ground", k, map, p, goi(-23, 1, 0, 2, 5, 1, 0, 2, 4));
        Kiem.bang("-23 so luong 4: con 6", 6, p.listGemitem[5]);
        goiRieng(Class.forName("real.MapEpNgoc"), null, "doGiveSpecialGem2Ground", k, map, p, goi(-23, 1, 0, 2, 5, 1, 0));
        Kiem.bang("-23 khong so luong: bo ca chong", 0, p.listGemitem[5]);
        GOI.clear();
        goiRieng(Class.forName("real.MapEpNgoc"), null, "doGiveSpecialGem2Ground", k, map, p, goi(-23, 1, 0, 2, 6, 1, 1));
        Kiem.bang("-23 nguyen lieu khoa: khong bo", 3, p.listGemitemLock[6]);
        Kiem.dung("-23 khoa: -104 'Vật phẩm khoá không bỏ được.'", String.valueOf(toastCuoi()).contains("Vật phẩm khoá không bỏ được."));
        p.doAddGemItem(7, 2, false);
        p.userTrade.add(nhanVat());
        goiRieng(Class.forName("real.MapEpNgoc"), null, "doGiveSpecialGem2Ground", k, map, p, goi(-23, 1, 0, 2, 7, 1, 0));
        Kiem.bang("-23 dang trao doi: khong bo (truoc day bo duoc)", 2, p.listGemitem[7]);
    }

    static void banNguyenLieu() throws Exception {
        final Object om = taoKhongKhoiTao(real.OfflineMap.class);
        final Char p = nhanVat();
        p.passWord = "";
        p.setxu(0L);
        p.doAddGemItem(5, 10, false);
        final short realId = p.gemItem.get(p.gemItem.size() - 1).realId;
        final Class<?>[] k = {Char.class, Message.class};
        goiRieng(real.OfflineMap.class, om, "doSellGemItem", k, p, goi(78, 2, realId, 1, 0, 2, 3));
        Kiem.bang("78 so luong 3: con 7", 7, p.listGemitem[5]);
        goiRieng(real.OfflineMap.class, om, "doSellGemItem", k, p, goi(78, 2, realId, 1, 0));
        Kiem.bang("78 khong so luong: ban ca chong (nhu cu)", 0, p.listGemitem[5]);
        p.doAddGemItem(MoRongTuiGame.ID_VE, 2, false);
        final short idVe = p.gemItem.get(p.gemItem.size() - 1).realId;
        GOI.clear();
        goiRieng(real.OfflineMap.class, om, "doSellGemItem", k, p, goi(78, 2, idVe, 1, 0));
        Kiem.bang("78 ve mo rong: khong ban", 2, p.listGemitem[MoRongTuiGame.ID_VE]);
        Kiem.dung("78 ve mo rong: -104 LOI", String.valueOf(toastCuoi()).startsWith("1|"));
    }

    static final int MAU_DO = 9101;

    static void vutVatPham() throws Exception {
        final ItemTemplates t = new ItemTemplates();
        t.id = MAU_DO;
        t.type = 9;
        t.name = "Vòng cổ kiểm";
        t.level = 1;
        t.atb = new short[12];
        for (int i = 0; i < 6; i++) {
            real.Map.itemTemplates.get(i).put(Integer.valueOf(MAU_DO), t);
        }
        final real.Map map = new real.Map();
        final Char p = nhanVat();
        p.passWord = "";
        final Item it = new Item();
        it.tempateID = MAU_DO;
        it.classTemplate = -1;
        it.id = idKe++;
        it.lock = 1;
        p.iItems.add(it);
        final Class<?>[] k = {Char.class, Message.class};
        GOI.clear();
        goiRieng(real.Map.class, map, "doGiveItemToGround", k, p, goi(61, 2, it.id));
        Kiem.dung("61 vat pham khoa: con trong tui", p.iItems.contains(it));
        Kiem.dung("61 vat pham khoa: -104 ly do", String.valueOf(toastCuoi()).contains("Vật phẩm khoá không bỏ được."));
        GOI.clear();
        goiRieng(real.Map.class, map, "doGiveItemToGround", k, p, goi(61, 2, 30000));
        Kiem.dung("61 vat pham khong co: -104 ly do (truoc day im lang)", String.valueOf(toastCuoi()).startsWith("1|"));
        it.lock = 0;
        goiRieng(real.Map.class, map, "doGiveItemToGround", k, p, goi(61, 2, it.id));
        Kiem.sai("61 vat pham thuong: da bo", p.iItems.contains(it));
    }

    static void duongThemVaoTui() {
        Kiem.bang("giao dich thuoc: 2 loai moi can 2 o", 2,
                real.GiaoDichThuoc.soOThemCan(new int[]{0, 0, 0, 5}, new int[4], new int[]{0, 3, 1, 0}));
        Kiem.bang("giao dich thuoc: dua het 1 loai, nhan 1 loai moi = 0", 0,
                real.GiaoDichThuoc.soOThemCan(new int[]{0, 0, 2}, new int[]{0, 0, 2}, new int[]{0, 4, 0}));
        Kiem.bang("giftcode: uoc luong o (DB x3 + 1 muc + danh hieu)", 4,
                data.DatabaseGiftCode.uocLuongO("DB:100:3:0,THUOC:1:5,DH:8767:1"));
        Kiem.bang("giftcode: chuoi rong 0 o", 0, data.DatabaseGiftCode.uocLuongO(""));
        final data.InfoQuestTemplate q = new data.InfoQuestTemplate();
        q.idItemgift = new short[]{100, 100, 100, 100, 100, 0};
        q.potiongift = new short[]{1, 2};
        q.npotiongift = new short[]{3, 3};
        final Char p = nhanVat();
        p.potions[1] = 1;
        Kiem.bang("nhiem vu: 1 vat pham + 1 loai binh moi = 2 o", 2, q.soOCanThuong(p));
    }

    static void maNguon() throws Exception {
        final String map = doc("src/real/Map.java");
        final String off = doc("src/real/OfflineMap.java");
        Kiem.dung("Tui do -> hanh trang kiem day tui", map.contains("lay ra hanh trang phai con o trong + chi so hop le"));
        Kiem.dung("Mua lai do da ban kiem day tui", map.contains("mua lai do da ban phai con o trong"));
        Kiem.dung("Kho (Map) -> hanh trang kiem day tui", map.contains("lay do tu kho ra hanh trang phai con o trong"));
        Kiem.dung("Kho (OfflineMap) -> hanh trang kiem day tui", off.contains("lay do tu kho ra hanh trang phai con o trong"));
        Kiem.dung("Cho nguoi choi: kiem tui nguoi mua", off.contains("kiem tui nguoi MUA truoc khi rut mon"));
        Kiem.dung("Shop dac biet: kiem tui truoc khi tru tien", map.contains("kiem tui TRUOC khi tru tien"));
        Kiem.dung("OfflineMap ban nguyen lieu khoa: chong clone", off.contains("Vật phẩm khoá không bán được."));
        Kiem.dung("Vong quay kiem o trong", doc("src/real/Char.java").contains("phan thuong vong quay vao hanh trang"));
        Kiem.dung("Da tien hoa roi kiem day tui", doc("src/real/RoiDaTienHoa.java").contains("p.isFullInventory()"));
        Kiem.dung("Giao dich thuoc kiem o moi", doc("src/real/MapMuaBan.java").contains("GiaoDichThuoc.soOThemCan"));
        Kiem.dung("Giftcode kiem o truoc khi tru luot", doc("src/data/DatabaseGiftCode.java").contains("uocLuongO(item)"));
        Kiem.dung("Nhat do tu nghien: khong lam mat mon khi day", doc("src/real/MapXuLyLenh.java").contains("chi nhat mon SE bi nghien ngay"));
        Kiem.dung("MapXuLyLenh: lenh -107", doc("src/real/MapXuLyLenh.java").contains("case MoRongTuiGame.LENH:"));
        Kiem.dung("Map.doTuBinh: ve mo rong", map.contains("MoRongTuiGame.laVe(idGem)"));
        Kiem.dung("Dang nhap gui -105", doc("src/real/cmd/SelectCharHandler.java").contains("MoRongTuiGame.guiTrangThai(p)"));
        Kiem.dung("TeamServer them cot o_mo_rong", doc("src/server/TeamServer.java").contains("themCotVeMoRong()"));
        final String nv = doc("src/data/DatabaseNhanVat.java");
        Kiem.dung("saveChar / saveCharAuto / saveCharLienDau luu o_mo_rong (3 cho)",
                nv.split("Database.luuCotMoRong\\(", -1).length - 1 == 3);
        Kiem.dung("hai duong doc nhan vat doc o_mo_rong",
                nv.contains("Database.docCotMoRong(rs, p)") && doc("src/data/Database.java").contains("Database.docCotMoRong(rs, p)"));
        final String sql = doc("../sql/them_ve_mo_rong.sql");
        Kiem.dung("sql/them_ve_mo_rong.sql: cot o_mo_rong idempotent", sql.contains("information_schema.COLUMNS") && sql.contains("o_mo_rong"));
        Kiem.dung("sql/them_ve_mo_rong.sql: mon ve_mo_rong_tui", sql.contains("INSERT IGNORE INTO `nap_cua_hang`") && sql.contains("'ve_mo_rong_tui'"));
    }

    static String doc(final String duong) throws Exception {
        return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
    }

    /** Tao doi tuong khong chay ham dung (OfflineMap(int) khoi dong luong ban do). */
    static Object taoKhongKhoiTao(final Class<?> lop) throws Exception {
        final Class<?> u = Class.forName("sun.misc.Unsafe");
        final java.lang.reflect.Field f = u.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        final Object unsafe = f.get(null);
        return u.getMethod("allocateInstance", Class.class).invoke(unsafe, lop);
    }
}
