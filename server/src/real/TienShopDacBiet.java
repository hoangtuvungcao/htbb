package real;

import io.Session;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * LOAI TIEN CUA TUNG DONG CUA HANG DAC BIET (lenh 85, Res.shopTemplate o client).
 *
 * <p>Chu du an (PC 0.9.17): "Cửa hàng đặc biệt" hien moi gia bang bieu tuong LUONG (goi 25 chi co typeMoney 0 luong /
 * 1 xu) nhung may chu tru LUONG KHOA cho cac dong data_shop_gia_lau_dai.tien_moi = 2 (ve gio vang, thuoc, keo - qua
 * doBuySpecialItem.TRA_LUONG_KHOA) va cho moi dong Map.isItemLock (116..128, 150, 176: nguyen lieu / ve / thuoc khoa).
 * Nguoi choi khong biet phai tra bang luong khoa, cung khong thay so luong khoa dang co.
 *
 * <ul>
 * <li>Client cu (&lt; {@link #BAN_CLIENT}) va goi 25 phat lai cho ca may chu (createMsgLogin(null)): mo ta dong do mo
 * dau bang {@link #NHAN} - dong phu cua hang ghi ro "[Lượng khoá] ...".</li>
 * <li>Client &gt;= {@link #BAN_CLIENT}: mo ta giu nguyen, sau danh sach shop goi 25 noi them
 * {@code u8 so dong, u8 id_shop...} = cac dong tra bang luong khoa (client ve bieu tuong / chip loc / vi luong khoa).
 * Client moi van nhan ra {@link #NHAN} (goi phat lai khong co phan noi them) va bo nhan khi hien.</li>
 * <li>Mua: thieu luong khoa -&gt; tu choi "Không đủ lượng khoá: cần X, đang có Y." (KHONG bao gio tru luong thuong);
 * mua xong -&gt; "Đã mua ...: trừ X lượng khoá, còn Y."</li>
 * </ul>
 */
public final class TienShopDacBiet {

    /**
     * Ban client dau tien doc phan noi them cua goi 25 (MOT cho duy nhat). 918 neu ban client nay phat hanh la 0.9.18;
     * neu 0.9.18 ra truoc ma chua co phan doc nay thi doi thanh 919.
     */
    public static final int BAN_CLIENT = 918;

    /** Nhan dau mo ta (client cu): client moi cung nhan ra va bo di. */
    public static final String NHAN = "[Lượng khoá] ";

    private TienShopDacBiet() {
    }

    /** Dong luon tra bang luong khoa (nhanh Map.isItemLock cua doBuySpecialItem). */
    public static boolean laDongKhoa(final int idShop) {
        return idShop >= 116 && idShop <= 128 || idShop == 150 || idShop == 176;
    }

    /** Dong nay cua shop dac biet tru LUONG KHOA (khong phai luong / xu). */
    public static boolean traLuongKhoa(final int idShop) {
        return laDongKhoa(idShop)
                || (com.htbb.pokemon.LauDai.bat() && data.DatabaseLauDai.traBangLuongKhoa(idShop));
    }

    /** Client nay doc phan noi them cua goi 25. */
    public static boolean coClient(final Session s) {
        return s != null && s.banClient >= BAN_CLIENT;
    }

    /** Mo ta gui trong goi 25: client cu -&gt; them {@link #NHAN} cho dong tra bang luong khoa. */
    public static String moTa(final ShopTemplate t, final boolean clientMoi) {
        final String d = t.decript == null ? "" : t.decript;
        if (clientMoi || !traLuongKhoa(t.id)) {
            return d;
        }
        return d.trim().isEmpty() ? NHAN + "Giá tính bằng lượng khoá." : NHAN + d;
    }

    /**
     * Bang shop cuoi goi 25 (createMsgLogin): u8 so dong, moi dong {u8 id, u8 anh, int gia, UTF ten, UTF mo ta, u8 loai
     * tien 0 luong / 1 xu, u8 tab, u8 ban, short so luong}; client &gt;= {@link #BAN_CLIENT} them {@link #ghiNoiThem}.
     * s == null (goi phat lai cho ca may chu) -&gt; dang client cu (co nhan).
     */
    public static void ghiBangShop(final DataOutputStream dos, final ShopTemplate[] shop, final boolean sale, final Session s)
            throws IOException {
        final boolean clientMoi = coClient(s);
        dos.writeByte(shop.length);
        for (final ShopTemplate t : shop) {
            dos.writeByte(t.id);
            dos.writeByte(t.idImage);
            dos.writeInt(t.getPrice(sale));
            dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi(t.name));
            dos.writeUTF(com.htbb.pokemon.ChuPokemon.doi(moTa(t, clientMoi)));
            dos.writeByte(t.type);
            dos.writeByte(t.tab);
            dos.writeByte(t.sell);
            dos.writeShort(t.value);
        }
        if (clientMoi) {
            ghiNoiThem(dos, shop);
        }
    }

    /** Phan noi them goi 25 (client moi): u8 so dong, u8 id moi dong tra bang luong khoa. */
    public static void ghiNoiThem(final DataOutputStream dos, final ShopTemplate[] shop) throws IOException {
        int n = 0;
        for (final ShopTemplate t : shop) {
            if (t != null && traLuongKhoa(t.id)) {
                n++;
            }
        }
        dos.writeByte(n);
        for (final ShopTemplate t : shop) {
            if (t != null && traLuongKhoa(t.id)) {
                dos.writeByte(t.id);
            }
        }
    }

    /** "Không đủ lượng khoá: cần 30, đang có 12." */
    public static String khongDu(final long can, final long co) {
        return "Không đủ lượng khoá: cần " + so(can) + ", đang có " + so(Math.max(0L, co)) + ".";
    }

    /** "Đã mua Vé giờ vàng 100: trừ 30 lượng khoá, còn 12." */
    public static String daMua(final String ten, final long tru, final long con) {
        return "Đã mua " + tenNgan(ten) + ": trừ " + so(tru) + " lượng khoá, còn " + so(Math.max(0L, con)) + ".";
    }

    /** Dong dau cua ten (data_shop 126..128 co "ten \n - mo ta"). */
    static String tenNgan(final String ten) {
        if (ten == null) {
            return "vật phẩm";
        }
        final int i = ten.indexOf('\n');
        final String t = (i >= 0 ? ten.substring(0, i) : ten).trim();
        return t.isEmpty() ? "vật phẩm" : t;
    }

    /** 1250000 -&gt; "1.250.000". */
    static String so(final long v) {
        final String s = String.valueOf(Math.abs(v));
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (v < 0 ? "-" : "") + b;
    }
}
