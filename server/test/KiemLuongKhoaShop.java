import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import real.Char;
import real.ShopTemplate;
import real.TienShopDacBiet;

/**
 * CUA HANG DAC BIET TRA BANG LUONG KHOA (bao loi chu du an PC 0.9.17: "lượng khoá đâu có chỗ để mua đâu, đây là xu
 * với lượng thôi mà" - gia ve bieu tuong luong nhung may chu tru luong khoa).
 * <ul>
 * <li>Goi 25: client cu -&gt; mo ta dong luong khoa mo dau "[Lượng khoá] "; client &gt;= TienShopDacBiet.BAN_CLIENT -&gt;
 * mo ta nguyen + phan noi them {u8 n, u8 id...}; goi phat lai (session null) -&gt; nhan.</li>
 * <li>Mua (lenh 85 that): du luong khoa -&gt; tru luong khoa, "Đã mua ...: trừ X lượng khoá, còn Y.", do khoa; thieu luong
 * khoa nhung du luong (chu game 31/10/2026) -&gt; tru LUONG cung gia, do KHONG khoa, bao ro; thieu ca hai -&gt; tu choi
 * "Không đủ lượng khoá hoặc lượng: ..."; dong luong thuong van tru luong.</li>
 * </ul>
 */
public final class KiemLuongKhoaShop {

    private KiemLuongKhoaShop() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static real.Map map;
    static short idKe = 31000;

    public static void chay() {
        Kiem.nhom("Cua hang dac biet: dong tra bang luong khoa ghi ro, khong tru luong thay (TienShopDacBiet)");
        final boolean batCu = com.htbb.pokemon.LauDai.bat();
        final Map<Integer, int[]> giaCu = data.DatabaseLauDai.GIA_SHOP;
        final ShopTemplate[] shopCu = real.Map.shop;
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        try {
            com.htbb.pokemon.LauDai.dat(true);
            final Map<Integer, int[]> g = new HashMap<Integer, int[]>();
            g.put(0, new int[]{30, 0, 2, 1});
            g.put(2, new int[]{20, 0, 2, 0});
            g.put(10, new int[]{1, 0, 0, 0});
            data.DatabaseLauDai.GIA_SHOP = g;
            real.Map.shop = new ShopTemplate[]{
                new ShopTemplate(0, 11, "Vé giờ vàng 100", "Tăng 100% kinh nghiệm", 2, 30, 0, 0, 1, 30),
                new ShopTemplate(2, 9, "200 Siêu thuốc hồi máu", "Hồi phục 1500 hp", 200, 20, 0, 0, 1, 20),
                new ShopTemplate(10, 32, "Phiếu công ích", "Giảm trừ 100 điểm PK", 1, 1, 0, 1, 1, 1),
                new ShopTemplate(120, 102, "Ngọc cấp 4 khoá \n - Dùng chế tạo trang bị", "", 1, 15, 0, 2, 1, 15),
            };
            if (real.Map.gemTemplate == null) {
                real.Map.gemTemplate = new real.GemTemplate[0];
            }
            nhan();
            mua();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            com.htbb.pokemon.LauDai.dat(batCu);
            data.DatabaseLauDai.GIA_SHOP = giaCu;
            real.Map.shop = shopCu;
            real.Map.gemTemplate = gemCu;
        }
    }

    static void nhan() throws Exception {
        Kiem.dung("tien_moi=2 (id 0, 2) -> luong khoa", TienShopDacBiet.traLuongKhoa(0) && TienShopDacBiet.traLuongKhoa(2));
        Kiem.dung("do khoa 116..128/150/176 -> luong khoa", TienShopDacBiet.traLuongKhoa(120) && TienShopDacBiet.traLuongKhoa(176));
        Kiem.sai("dong luong thuong (id 10) khong phai luong khoa", TienShopDacBiet.traLuongKhoa(10));
        Kiem.bang("client cu: mo ta co nhan + ghi chu tra luong", "[Lượng khoá] Hồi phục 1500 hp" + TienShopDacBiet.GHI_CHU_TRA_LUONG,
                TienShopDacBiet.moTa(real.Map.shop[1], false));
        Kiem.bang("client cu: mo ta rong -> nhan + cau day du", "[Lượng khoá] Giá tính bằng lượng khoá." + TienShopDacBiet.GHI_CHU_TRA_LUONG,
                TienShopDacBiet.moTa(real.Map.shop[3], false));
        Kiem.bang("client cu: dong luong khong doi", "Giảm trừ 100 điểm PK", TienShopDacBiet.moTa(real.Map.shop[2], false));
        Kiem.bang("client moi: mo ta + ghi chu tra luong (khong nhan)", "Hồi phục 1500 hp" + TienShopDacBiet.GHI_CHU_TRA_LUONG,
                TienShopDacBiet.moTa(real.Map.shop[1], true));
        Kiem.bang("bao thieu ca hai", "Không đủ lượng khoá hoặc lượng: cần 30, đang có 12 lượng khoá, 5 lượng.",
                TienShopDacBiet.khongDu(30, 12, 5));
        Kiem.bang("bao da mua bang luong", "Đã mua Vé giờ vàng: trừ 30 lượng (thiếu lượng khoá), còn 70 lượng. Đồ không khoá.",
                TienShopDacBiet.daMuaBangLuong("Vé giờ vàng \n - x", 30, 70));
        Kiem.bang("bao thieu", "Không đủ lượng khoá: cần 30, đang có 12.", TienShopDacBiet.khongDu(30, 12));
        Kiem.bang("bao da mua (ten nhieu dong lay dong dau)", "Đã mua Ngọc cấp 4 khoá: trừ 1.500 lượng khoá, còn 0.",
                TienShopDacBiet.daMua("Ngọc cấp 4 khoá \n - x", 1500, 0));

        // Goi 25 that: client cu / moi / phat lai
        final byte[] cu = bangShop(phien(TienShopDacBiet.BAN_CLIENT - 1));
        final byte[] moi = bangShop(phien(TienShopDacBiet.BAN_CLIENT));
        final byte[] phatLai = bangShop(null);
        final String nguon = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/MessageCreator.java")), "UTF-8");
        Kiem.dung("createMsgLogin ghi bang shop qua TienShopDacBiet.ghiBangShop(..., s)", nguon.contains("TienShopDacBiet.ghiBangShop(m.dos, Map.shop, Map.isSale, s)"));
        Kiem.dung("goi 25 client cu: co '[Lượng khoá] Tăng 100% kinh nghiệm'", chua(cu, utf("[Lượng khoá] Tăng 100% kinh nghiệm")));
        Kiem.dung("goi 25 phat lai: co nhan", chua(phatLai, utf("[Lượng khoá] Hồi phục 1500 hp")));
        Kiem.sai("goi 25 client moi: khong nhan", chua(moi, utf("[Lượng khoá] Hồi phục 1500 hp")));
        Kiem.dung("goi 25 client moi: mo ta nguyen", chua(moi, utf("Hồi phục 1500 hp")));
        final byte[] duoi = {3, 0, 2, 120};
        Kiem.dung("goi 25 client moi: noi them {3, 0, 2, 120} o cuoi", duoiLa(moi, duoi));
        Kiem.sai("goi 25 client cu: khong noi them", duoiLa(cu, duoi));
        Kiem.bang("goi 25 client moi = cu - nhan (3 dong) + noi them (4 byte)",
                cu.length - 3 * utf("[Lượng khoá] ").length + 4, moi.length);
    }

    static void mua() throws Exception {
        map = new real.Map();
        // 1. thieu luong khoa, du luong: chu game 31/10/2026 - tru LUONG cung gia, bao ro, luong khoa giu
        Char p = nhanVat();
        p.setLuong(1000);
        p.lockLuong = 5;
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 2));
        Kiem.bang("thieu luong khoa, du luong: bao tru luong",
                "Đã mua 200 Siêu thuốc hồi máu: trừ 20 lượng (thiếu lượng khoá), còn 980 lượng. Đồ không khoá.", baoCuoi());
        Kiem.bang("thieu luong khoa, du luong: tru 20 luong", 980L, (long) p.getLuong());
        Kiem.bang("thieu luong khoa, du luong: luong khoa giu", 5L, (long) p.getLuongLock());
        Kiem.bang("thieu luong khoa, du luong: nhan thuoc", 200, p.potions[21]);
        // 1b. thieu ca hai: tu choi, khong tru gi
        p = nhanVat();
        p.setLuong(3);
        p.lockLuong = 5;
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 2));
        Kiem.bang("thieu ca hai: bao ro", "Không đủ lượng khoá hoặc lượng: cần 20, đang có 5 lượng khoá, 3 lượng.", baoCuoi());
        Kiem.bang("thieu ca hai: luong giu", 3L, (long) p.getLuong());
        Kiem.bang("thieu ca hai: luong khoa giu", 5L, (long) p.getLuongLock());
        Kiem.bang("thieu ca hai: khong nhan thuoc", 0, p.potions[21]);
        Kiem.bang("thieu ca hai: khong goi 85", 0, dem(85));
        p = nhanVat();
        p.setLuong(1000);
        p.lockLuong = 5;
        // 2. du luong khoa: tru luong khoa, bao ro
        p.lockLuong = 50;
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 2));
        Kiem.bang("du luong khoa: tru 20 luong khoa", 30L, (long) p.getLuongLock());
        Kiem.bang("du luong khoa: luong giu", 1000L, (long) p.getLuong());
        Kiem.bang("du luong khoa: nhan 200 thuoc", 200, p.potions[21]);
        Kiem.bang("du luong khoa: bao da mua", "Đã mua 200 Siêu thuốc hồi máu: trừ 20 lượng khoá, còn 30.", baoCuoi());
        // 3. nguyen lieu khoa (isItemLock) khong co luong khoa, du luong: tru luong, nguyen lieu KHONG khoa
        p = nhanVat();
        p.setLuong(1000);
        p.lockLuong = 0;
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 120));
        Kiem.bang("nguyen lieu tra bang luong: tru 15 luong", 985L, (long) p.getLuong());
        Kiem.bang("nguyen lieu tra bang luong: ngoc cap 4 KHONG khoa", 1, p.listGemitem[85]);
        Kiem.bang("nguyen lieu tra bang luong: khong vao o khoa", 0, p.listGemitemLock[85]);
        // 3b. du luong khoa: tru luong khoa, nguyen lieu khoa
        p = nhanVat();
        p.setLuong(1000);
        p.lockLuong = 100;
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 120));
        Kiem.bang("nguyen lieu tra bang luong khoa: tru 15 luong khoa", 85L, (long) p.getLuongLock());
        Kiem.bang("nguyen lieu tra bang luong khoa: luong giu", 1000L, (long) p.getLuong());
        Kiem.bang("nguyen lieu tra bang luong khoa: ngoc vao o khoa", 1, p.listGemitemLock[85]);
        // 3c. thieu ca hai: tu choi
        p = nhanVat();
        p.setLuong(2);
        p.lockLuong = 0;
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 120));
        Kiem.bang("nguyen lieu thieu ca hai: bao ro", "Không đủ lượng khoá hoặc lượng: cần 15, đang có 0 lượng khoá, 2 lượng.", baoCuoi());
        Kiem.bang("nguyen lieu thieu ca hai: luong giu", 2L, (long) p.getLuong());
        // 4. dong luong thuong: tru luong, khong bao luong khoa
        p = nhanVat();
        p.setLuong(10);
        p.lockLuong = 10;
        GOI.clear();
        real.doBuySpecialItem.doBuySpecialItem(p, goi(85, 10));
        Kiem.bang("dong luong: tru 1 luong", 9L, (long) p.getLuong());
        Kiem.bang("dong luong: luong khoa giu", 10L, (long) p.getLuongLock());
        final String b = baoCuoi();
        Kiem.dung("dong luong: khong bao luong khoa (" + b + ")", b == null || !b.contains("lượng khoá"));
    }

    // ------------------------------------------------------------------ tien ich

    static byte[] bangShop(final Session s) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        TienShopDacBiet.ghiBangShop(new DataOutputStream(bo), real.Map.shop, false, s);
        return bo.toByteArray();
    }

    static Session phien(final int ban) {
        final Session s = new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        };
        s.banClient = ban;
        return s;
    }

    static Char nhanVat() {
        final Char p = new Char(null);
        p.setSession(phien(TienShopDacBiet.BAN_CLIENT));
        p.charname = "kiemlk" + (idKe++);
        p.lvDetail.lv = 50;
        p.gender = 1;
        p.maxhp = 1000;
        p.hp = 1000;
        p.MAX_INVENTORY = 42;
        p.maxBag = 1;
        p.animal = new Vector<data.Animal>();
        p.pet = new Vector<data.Pet>();
        p.userTrade = new Vector<Char>();
        p.passWord = "";
        p.isCorrectPass = true;
        p.map = map;
        return p;
    }

    static Message goi(final int cmd, final int b) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        new DataOutputStream(bo).writeByte(b);
        return new Message(cmd, bo.toByteArray());
    }

    /** Chu cua thong bao 37 cuoi cung (null neu khong co). */
    static String baoCuoi() throws Exception {
        String kq = null;
        for (final Message m : GOI) {
            if (m.command == 37) {
                final byte[] b = m.toByteArray();
                kq = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3)).readUTF();
            }
        }
        return kq;
    }

    static int dem(final int cmd) {
        int n = 0;
        for (final Message m : GOI) {
            if (m.command == cmd) {
                n++;
            }
        }
        return n;
    }

    static byte[] utf(final String s) throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        new DataOutputStream(bo).writeUTF(com.htbb.pokemon.ChuPokemon.doi(s));
        final byte[] b = bo.toByteArray();
        final byte[] r = new byte[b.length - 2];
        System.arraycopy(b, 2, r, 0, r.length);
        return r;
    }

    static boolean chua(final byte[] a, final byte[] b) {
        outer:
        for (int i = 0; i + b.length <= a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (a[i + j] != b[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    static boolean duoiLa(final byte[] a, final byte[] b) {
        if (a.length < b.length) {
            return false;
        }
        for (int j = 0; j < b.length; j++) {
            if (a[a.length - b.length + j] != b[j]) {
                return false;
            }
        }
        return true;
    }
}
