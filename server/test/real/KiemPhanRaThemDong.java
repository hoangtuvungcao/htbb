package real;

import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * PHAN RA (nghien) do dai han ra nguyen lieu Thêm dòng (bao loi 08/11/2026 Đào Ngọc Thịnh / rongngoo: "Phân giải trang
 * bị ra xong không nhận được cái đồ luyện thêm dòng"): ti le theo do hiem (Hoan my chac chan 1 Bot xanh la + 1 Da them
 * dong, Do / Xanh co ti le, Trang khong), dung id ngoc ma Tho ren › Thêm dòng doi (TYPE_BOT 249 / TYPE_DA_TIEN_GIAI 250),
 * khoa theo do, thong bao liet ke DUNG phan nhan + ti le phan khong ra, icon ket qua nghien tay la Da nang cap (khong con
 * icon Bột trắng 72). Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemPhanRaThemDong {

    private KiemPhanRaThemDong() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void dung(final String m, final boolean v) {
        BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
    }

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static final int TPL = 31901;

    public static void chay() {
        final GemTemplate[] gemCu = Map.gemTemplate;
        ItemTemplates tplCu = null;
        try {
            MapNapTaiNguyen.loadGemTemplate();
            while (Map.itemTemplates.size() < 6) {
                Map.itemTemplates.add(new Hashtable<Integer, ItemTemplates>());
            }
            tplCu = Map.itemTemplates.get(5).get(Integer.valueOf(TPL));
            final ItemTemplates t = new ItemTemplates();
            t.id = TPL;
            t.type = 0;
            t.name = "Khăn quàng thử";
            t.level = 50;
            t.price = 0; // khong cong xu (addXu ngoai server), phan xu kiem o moTaPhanRa
            t.atb = new short[12];
            Map.itemTemplates.get(5).put(Integer.valueOf(TPL), t);
            bangTiLe();
            moTa();
            game();
            nguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            dung("khong nem loi: " + e, false);
        } finally {
            Map.gemTemplate = gemCu;
            if (tplCu != null) {
                Map.itemTemplates.get(5).put(Integer.valueOf(TPL), tplCu);
            } else {
                Map.itemTemplates.get(5).remove(Integer.valueOf(TPL));
            }
        }
    }

    static void bangTiLe() {
        bang("ngoc 249 la Bot (TYPE_BOT) ma Tho ren › Thêm dòng doi", Integer.valueOf(GemTemplate.TYPE_BOT),
                Integer.valueOf(Map.gemTemplate[RoiLauDai.ID_BOT_THEM_DONG].type));
        bang("ngoc 250 la Da them dong (TYPE_DA_TIEN_GIAI)", Integer.valueOf(GemTemplate.TYPE_DA_TIEN_GIAI),
                Integer.valueOf(Map.gemTemplate[RoiLauDai.ID_DA_THEM_DONG].type));
        bang("ngoc 250 ten 'Đá thêm dòng' (bo 'thăng bậc')", "Đá thêm dòng", Map.gemTemplate[RoiLauDai.ID_DA_THEM_DONG].name);
        bang("Hoan my: chac chan ca hai", "[1000, 1000]", java.util.Arrays.toString(RoiLauDai.tiLeThemDong(Item.COLOR_GREEN)));
        bang("Do: 30% bot / 15% da", "[300, 150]", java.util.Arrays.toString(RoiLauDai.tiLeThemDong(Item.COLOR_RED)));
        bang("Xanh: 15% bot / 5% da", "[150, 50]", java.util.Arrays.toString(RoiLauDai.tiLeThemDong(Item.COLOR_BLUE)));
        bang("Trang: khong", "[0, 0]", java.util.Arrays.toString(RoiLauDai.tiLeThemDong(Item.COLOR_WHITE)));
    }

    static void moTa() {
        String s = RoiLauDai.moTaPhanRa("Khăn", 2, 5088L, RoiLauDai.tiLeThemDong(Item.COLOR_GREEN), true, true, true);
        dung("mo ta Hoan my: liet ke da + xu + bot + da them dong + khoa (" + s + ")",
                s.contains("+2 Đá nâng cấp") && s.contains("+5088 xu") && s.contains("+1 Bột xanh lá") && s.contains("+1 Đá thêm dòng")
                && s.contains("(khoá)") && s.contains("Thợ rèn › Thêm dòng") && !s.contains("không ra"));
        s = RoiLauDai.moTaPhanRa("Khăn", 2, 100L, RoiLauDai.tiLeThemDong(Item.COLOR_RED), false, false, false);
        dung("mo ta Do khong trung: noi ro khong ra + ti le ca hai (" + s + ")",
                s.contains("không ra Bột xanh lá (tỉ lệ 30%), Đá thêm dòng (tỉ lệ 15%)") && !s.contains("+1 Bột"));
        s = RoiLauDai.moTaPhanRa("Khăn", 2, 100L, RoiLauDai.tiLeThemDong(Item.COLOR_BLUE), true, false, false);
        dung("mo ta Xanh trung bot: +bot, khong ra da (5%) (" + s + ")",
                s.contains("+1 Bột xanh lá") && s.contains("không ra Đá thêm dòng (tỉ lệ 5%)"));
        s = RoiLauDai.moTaPhanRa("Khăn", 2, 0L, RoiLauDai.tiLeThemDong(Item.COLOR_WHITE), false, false, false);
        dung("mo ta Trang: noi do Trang khong ra nguyen lieu (" + s + ")", s.contains("Đồ Trắng không ra") && !s.contains("xu"));
    }

    static Char nv() {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.getSession().banClient = DungVatPham.BAN_TOAST;
        p.charname = "kiemphanra";
        p.lvDetail.lv = 50;
        return p;
    }

    static Item mon(final int mau, final int khoa) {
        final Item it = new Item();
        it.tempateID = TPL;
        it.classTemplate = -1;
        it.colorName = (byte) mau;
        it.lock = (byte) khoa;
        it.level = 50;
        return it;
    }

    static String toastCuoi() throws Exception {
        String kq = null;
        for (final Message m : GOI) {
            if (m.command == DungVatPham.LENH) {
                final byte[] b = m.toByteArray();
                final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
                r.readByte();
                kq = r.readUTF();
            }
        }
        return kq;
    }

    static void game() throws Exception {
        Char p = nv();
        GOI.clear();
        int da = RoiLauDai.phanRa(p, mon(Item.COLOR_GREEN, 1), true);
        bang("Hoan my khoa: tra 2 da", 2, da);
        bang("Hoan my khoa: +2 Da nang cap khoa", 2, p.listGemitemLock[RoiLauDai.ID_DA]);
        bang("Hoan my khoa: +1 Bot xanh la khoa", 1, p.listGemitemLock[RoiLauDai.ID_BOT_THEM_DONG]);
        bang("Hoan my khoa: +1 Da them dong khoa", 1, p.listGemitemLock[RoiLauDai.ID_DA_THEM_DONG]);
        final String t = toastCuoi();
        dung("nghien tay: thong bao nhanh liet ke dung (" + t + ")", t != null && t.contains("+1 Bột xanh lá") && t.contains("+1 Đá thêm dòng"));

        p = nv();
        GOI.clear();
        RoiLauDai.phanRa(p, mon(Item.COLOR_GREEN, 0), false);
        bang("Hoan my thuong (tu dong): bot khong khoa", 1, p.listGemitem[RoiLauDai.ID_BOT_THEM_DONG]);
        bang("tu dong: khong bat hop thoai / thong bao nhanh", null, toastCuoi());

        p = nv();
        RoiLauDai.phanRa(p, mon(Item.COLOR_WHITE, 0), true);
        bang("Trang: khong bot", 0, p.listGemitem[RoiLauDai.ID_BOT_THEM_DONG]);
        bang("Trang: khong da them dong", 0, p.listGemitem[RoiLauDai.ID_DA_THEM_DONG]);
        bang("Trang: van 2 Da nang cap", 2, p.listGemitem[RoiLauDai.ID_DA]);

        p = nv();
        final int n = 4000;
        for (int i = 0; i < n; i++) {
            RoiLauDai.phanRa(p, mon(Item.COLOR_RED, 0), false);
        }
        final int bot = p.listGemitem[RoiLauDai.ID_BOT_THEM_DONG];
        final int dtd = p.listGemitem[RoiLauDai.ID_DA_THEM_DONG];
        dung("Do x" + n + ": bot ~30% (" + bot + ")", bot > n * 25 / 100 && bot < n * 35 / 100);
        dung("Do x" + n + ": da them dong ~15% (" + dtd + ")", dtd > n * 11 / 100 && dtd < n * 19 / 100);
    }

    static void nguon() {
        final String om = doc("src/real/OfflineMap.java");
        dung("nghien tay (Tho ren) goi phanRa(..., true): thong bao ro", om.contains("RoiLauDai.phanRa(player, it, true)"));
        dung("nghien tay: icon ket qua = Da nang cap khi dai han (khong co dinh 72 Bột trắng)",
                om.contains("gemTemplate[RoiLauDai.ID_DA].idImage") && om.contains("m.dos.writeShort(iconKetQua)"));
        final String ct = doc("src/real/MapCheTao.java");
        dung("Thêm dòng: bao ro dieu kien (Hoan my / cap 50 / da khoa)", ct.contains("Thêm dòng chỉ dùng cho trang bị Hoàn mỹ.")
                && ct.contains("Thêm dòng cần trang bị đã khoá"));
    }
}
