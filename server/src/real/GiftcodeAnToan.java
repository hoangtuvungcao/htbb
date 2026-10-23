package real;

import com.htbb.pokemon.GiftcodeAnToanLogic;
import com.htbb.pokemon.LauDai;
import data.Database;
import data.DatabaseGiftCode;
import data.DatabaseLauDai;
import java.util.concurrent.ConcurrentHashMap;

/**
 * He giftcode AN TOAN (yeu cau 17/10/2026; bat khi sv.lau_dai=1). Bang tob_giftcode (admin/web tao
 * ma) + tob_giftcode_log (moi tai khoan 1 lan / ma, PRIMARY KEY (ma, user_id)). Schema va cach web
 * dung: docs/thiet-ke-game.md muc "Giftcode an toan".
 *
 * Kiem o CHINH noi phat (muc 11), thu tu:
 *  1. chong do ma: tai khoan nhap sai 5 lan -> khoa 10 phut (cung voi gian cach Char.GIFTCODE_COOLDOWN_MS);
 *  2. toi da giftcode.toi_da_thang ma / thang / nhan vat (GiftcodeLauDai);
 *  3. ma ton tai, dang bat, chua het han, du cap;
 *  4. phan thuong chi gom vat pham khoa / luong khoa, gia tri <= tran ma (giftcode.gia_tri_toi_da);
 *  5. ngan sach toan may chu / thang (giftcode.ngan_sach_thang);
 *  6. DANH DAU TRUOC (INSERT log - trung khoa = da nhap) roi tru luot CO DIEU KIEN; het luot thi xoa
 *     dong log vua ghi; chi phat khi ca hai buoc thanh cong (muc 5).
 */
public final class GiftcodeAnToan {

    /** userID -> {so lan sai, moc khoa den}. */
    private static final ConcurrentHashMap<Integer, long[]> SAI = new ConcurrentHashMap<Integer, long[]>();

    private GiftcodeAnToan() {
    }

    static void bao(final Char p, final String s) {
        p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
    }

    static boolean dangKhoa(final int userId) {
        final long[] s = SAI.get(Integer.valueOf(userId));
        return s != null && s[1] > com.htbb.pokemon.DongHo.bay();
    }

    static void ghiSai(final int userId) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        final long[] s = SAI.computeIfAbsent(Integer.valueOf(userId), k -> new long[2]);
        synchronized (s) {
            s[0]++;
            if (s[0] >= GiftcodeAnToanLogic.SAI_TOI_DA) {
                s[0] = 0;
                s[1] = bay + GiftcodeAnToanLogic.KHOA_MS;
            }
        }
    }

    public static void nhap(final Char p, final String goc) {
        if (!LauDai.bat() || p == null) {
            return;
        }
        if (dangKhoa(p.userID)) {
            bao(p, "Nhập sai quá nhiều lần, thử lại sau 10 phút.");
            return;
        }
        final String ma = GiftcodeAnToanLogic.chuanHoaMa(goc);
        if (ma == null) {
            ghiSai(p.userID);
            bao(p, "Mã quà tặng không hợp lệ.");
            return;
        }
        final String gioiHan = GiftcodeLauDai.choPhep(p);
        if (gioiHan != null) {
            bao(p, gioiHan);
            return;
        }
        final Object[] dong = DatabaseLauDai.napGiftcode(ma);
        if (dong == null) {
            ghiSai(p.userID);
            bao(p, "Mã quà tặng không tồn tại hoặc đã hết hạn.");
            return;
        }
        // {phan_thuong, han(ms,0=khong), tong_luot, da_dung, cap_toi_thieu, bat}
        final long han = ((Long) dong[1]).longValue();
        if (((Integer) dong[5]).intValue() != 1 || (han > 0L && han < com.htbb.pokemon.DongHo.bay())) {
            ghiSai(p.userID);
            bao(p, "Mã quà tặng không tồn tại hoặc đã hết hạn.");
            return;
        }
        if (p.lvDetail.lv < ((Integer) dong[4]).intValue()) {
            bao(p, "Cần đạt cấp " + dong[4] + " để nhập mã này.");
            return;
        }
        final int tran = LauDai.so("giftcode.gia_tri_toi_da", GiftcodeAnToanLogic.GIA_TRI_TOI_DA);
        final String loi = GiftcodeAnToanLogic.kiem((String) dong[0], tran);
        if (loi != null) {
            bao(p, "Mã quà tặng đang bị khoá (phần thưởng không hợp lệ).");
            System.out.println("[GIFTCODE] ma " + ma + " bi tu choi: " + loi);
            return;
        }
        final String thuong = GiftcodeAnToanLogic.chuanHoaThuong((String) dong[0]);
        // danh sach trang vat pham (GiftcodeAnToanDuLieu): trang bi khong vuot cap nhan vat, khong do ban luong
        final String loiVp = GiftcodeAnToanLogic.kiemVatPham(thuong, p.lvDetail.lv, id -> {
            final ItemTemplates t = Map.itemTemplates.get(5).get(Integer.valueOf(id));
            return t == null ? -1 : t.level;
        });
        if (loiVp != null) {
            bao(p, loiVp.contains("cao hơn cấp") ? "Mã này có trang bị cao hơn cấp của bạn, hãy lên cấp rồi nhập lại."
                    : "Mã quà tặng đang bị khoá (phần thưởng không hợp lệ).");
            System.out.println("[GIFTCODE] ma " + ma + " cho " + p.charname + ": " + loiVp);
            return;
        }
        final int giaTri = GiftcodeAnToanLogic.giaTri(thuong);
        final int thang = GiftcodeLauDai.thang(com.htbb.pokemon.DongHo.bay());
        final long daDungThang = DatabaseLauDai.tongGiftcodeThang(thang);
        if (daDungThang < 0L || daDungThang + giaTri > LauDai.so("giftcode.ngan_sach_thang", GiftcodeAnToanLogic.NGAN_SACH_THANG)) {
            bao(p, "Quà tặng tháng này đã hết, hẹn tháng sau.");
            return;
        }
        // Tui khong du cho: bao ro, KHONG danh dau / tru luot (truoc day ma bi dung het luot ma vat pham roi mat).
        final int canO = GiftcodeAnToanLogic.soOCan(thuong, id -> id >= 0 && id < p.listGemitemLock.length && p.listGemitemLock[id] > 0);
        if (canO > 0 && p.countSlotInventory() < canO) {
            bao(p, "Hành trang không đủ chỗ: cần " + canO + " ô trống để nhận quà của mã này. Dọn bớt rồi nhập lại (mã chưa bị dùng).");
            return;
        }
        // 6. danh dau truoc
        if (!DatabaseLauDai.ghiLogGiftcode(ma, p.userID, p.charDBID, p.charname, thuong, giaTri, thang)) {
            bao(p, "Tài khoản của bạn đã nhập mã quà tặng này rồi.");
            return;
        }
        if (!DatabaseLauDai.truLuotGiftcode(ma)) {
            DatabaseLauDai.xoaLogGiftcode(ma, p.userID);
            bao(p, "Mã quà tặng đã hết lượt sử dụng.");
            return;
        }
        GiftcodeLauDai.ghiNhan(p);
        try {
            DatabaseGiftCode.applyItemString(p, thuong);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharGemItem(p));
        } catch (final Exception e) {
            System.out.println("[GIFTCODE] loi phat " + ma + " cho " + p.charname + ": " + e);
        }
        Database.instance.saveOrtherLog("", p.charname, "giftcode an toan " + ma + ": " + thuong, "putGiftCode");
        // 0.9.9: cau "Đổi giftcode thành công: ..." -> client hien THE phan thuong (HopThongBao / PhanThuong tach tung muc)
        bao(p, "Đổi giftcode thành công: " + moTa(thuong) + ".");
    }

    /** "+5 lượng khoá, +2 Poké Ball" tu chuoi phan thuong da chuan hoa (LUONGKHOA / GEM / ITEM). */
    static String moTa(final String thuong) {
        final StringBuilder b = new StringBuilder();
        for (final String phan : thuong.split(",")) {
            final String[] t = phan.trim().split(":");
            String muc = null;
            try {
                final String k = t[0].trim().toUpperCase();
                if (k.equals("LUONGKHOA")) {
                    muc = "+" + Integer.parseInt(t[1].trim()) + " lượng khoá";
                } else if (k.equals("GEM") || k.equals("ITEM")) {
                    final int id = Integer.parseInt(t[1].trim());
                    final int n = Integer.parseInt(t[2].trim());
                    String ten = null;
                    if (k.equals("GEM")) {
                        if (Map.gemTemplate != null && id >= 0 && id < Map.gemTemplate.length && Map.gemTemplate[id] != null) {
                            ten = Map.gemTemplate[id].name;
                        }
                    } else if (Map.itemTemplates != null && Map.itemTemplates.size() > 5) {
                        final ItemTemplates it = Map.itemTemplates.get(5).get(Integer.valueOf(id));
                        ten = it == null ? null : it.name;
                    }
                    muc = "+" + n + " " + (ten == null || ten.trim().isEmpty() ? "vật phẩm" : ten.trim());
                }
            } catch (final Exception ignored) {
                muc = null;
            }
            if (muc != null) {
                b.append(b.length() == 0 ? "" : ", ").append(muc);
            }
        }
        return b.length() == 0 ? "quà tặng" : b.toString();
    }
}
