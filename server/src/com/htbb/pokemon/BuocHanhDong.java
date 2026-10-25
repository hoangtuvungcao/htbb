package com.htbb.pokemon;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BUOC HANH DONG CUA NHIEM VU NOI CHUYEN (-64 type 3) - luat thuan (nhanh sua-buoc-hanh-dong, 25/10/2026). Bot choi that
 * (bao_cao_cap1_40) thay ~80 buoc noi chuyen ghi "Chế tạo… / Nâng +2 / Hộ tống… / Giải câu đố… / Quan sát ban đêm… / Hạ 26
 * Gloom trong 8 phút…" ma nghe NPC xong la tra luon: may chu khong kiem viec ghi trong chu. Chu game: "tự động fix".
 *
 * <ul>
 *   <li>Buoc co he thong that + kieu dem duoc (o day: {@link #KIEU}) VA kha thi o cap buoc -> danh dau
 *       {@code kiem_hanh_dong=1} trong data_nhiem_vu_noi_dung.dieu_kien (khoi sua_buoc_hanh_dong.sql, cung khoi viet lai
 *       chu muc tieu): nghe NPC = NHAN buoc, chi xong khi lam viec do SAU khi nhan (real.HanhDongNhiemVuGame).</li>
 *   <li>Buoc khong co co che that (ho tong, cau do qua NPC, chon duong, che tao thieu nguyen lieu o cap buoc…) -> chi
 *       viet lai chu (cung khoi SQL): "Nói chuyện với X ở Y", loi thoai noi ro viec dien ra trong hoi thoai.</li>
 * </ul>
 * Tien do luu o NewCharQuest.monsterKilled[0] (buoc type 3 co mang 1 phan tu, luu / nap cung chuoi nhiem vu cu).
 */
public final class BuocHanhDong {

    /** Khoa danh dau trong dieu_kien. */
    public static final String DAU = "kiem_hanh_dong";
    /** Kieu may chu dem duoc cho buoc -64 (diem moc san co: DiemMocNhiemVu.khiMacDo, NangCapLauDaiGame, RoiLauDai). */
    public static final Set<String> KIEU = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "nang_cap", "nang_cap_cong", "quan_sat", "thu_thach_gio")));

    public final String ma;
    public final String kieu;
    public final String mucTieuId;
    public final int can;
    public final Map<String, String> dieuKien;

    public BuocHanhDong(final String ma, final String kieu, final String mucTieuId, final int can, final Map<String, String> dieuKien) {
        this.ma = ma;
        this.kieu = kieu;
        this.mucTieuId = mucTieuId == null ? "" : mucTieuId.trim();
        this.can = Math.max(1, can);
        this.dieuKien = dieuKien == null ? Collections.<String, String>emptyMap() : dieuKien;
    }

    /** Dong du lieu co danh dau va kieu dem duoc -> buoc; khac -> null. */
    public static BuocHanhDong tao(final String ma, final String kieu, final String mucTieuId, final int soLuong, final String dieuKien) {
        final Map<String, String> dk = NhiemVuMau.docDieuKien(dieuKien);
        if (ma == null || ma.isEmpty() || !"1".equals(dk.get(DAU)) || !KIEU.contains(kieu)) {
            return null;
        }
        return new BuocHanhDong(ma, kieu, mucTieuId, soLuong, dk);
    }

    /** Tham so su kien (id mau quai / so dex) khop muc_tieu_id (rong = bat ky). */
    public boolean khopId(final String id) {
        if (mucTieuId.isEmpty()) {
            return true;
        }
        if (id == null) {
            return false;
        }
        for (final String x : mucTieuId.split("[,|]")) {
            if (x.trim().equals(id.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gio {@code gio} (0..23, dong ho may chu) co dung khung cua buoc quan sat khong: "dem" = 19:00-5:59, "gio=a-b" = a:00
     * toi b:59. Thoi tiet KHONG kiem (chu cua cac buoc nay da bo "khi trời mưa…").
     */
    public boolean dungGio(final int gio) {
        return dungGio(dieuKien.get("dieu_kien_quan_sat"), gio);
    }

    public static boolean dungGio(final String qs, final int gio) {
        if (qs == null || qs.isEmpty() || qs.startsWith("thoi_tiet=")) {
            return true;
        }
        if ("dem".equals(qs)) {
            return gio >= 19 || gio < 6;
        }
        if (qs.startsWith("gio=")) {
            try {
                final String[] ab = qs.substring(4).split("-");
                return gio >= Integer.parseInt(ab[0].trim()) && gio <= Integer.parseInt(ab[1].trim());
            } catch (final RuntimeException e) {
                return true;
            }
        }
        return true;
    }

    /** Gioi han phut cua buoc ha quai tinh gio (dieu_kien gioi_han_phut), 0 = khong co. */
    public int phut() {
        try {
            final String s = dieuKien.get("gioi_han_phut");
            return s == null ? 0 : Math.max(0, Integer.parseInt(s.trim()));
        } catch (final RuntimeException e) {
            return 0;
        }
    }

    /** Tien do sau mot lan "nang_cap_cong" thanh cong len +{@code k}: muc cao nhat dat duoc sau khi nhan. */
    public static int sauNangCap(final int da, final String k) {
        try {
            return Math.max(da, Integer.parseInt(k.trim()));
        } catch (final RuntimeException e) {
            return da;
        }
    }

    /**
     * Ha quai tinh gio: {tien do moi, luc bat dau moi}. Dong ho chay tu con DAU TIEN; qua {@code phut} (hoac chua co luc bat
     * dau - vua vao lai game) thi dem lai tu con nay.
     */
    public static long[] thuThachGio(final int da, final long batDau, final long bay, final int phut) {
        if (batDau <= 0L || da <= 0 || phut > 0 && bay - batDau > phut * 60000L) {
            return new long[]{1L, bay};
        }
        return new long[]{da + 1L, batDau};
    }

    /** "(3/26)" / nang cap "(+1/+2)". */
    public String chuTienDo(final int da) {
        final int x = Math.min(Math.max(0, da), can);
        return "nang_cap_cong".equals(kieu) ? "(+" + x + "/+" + can + ")" : "(" + x + "/" + can + ")";
    }

    /** Loi nhac khi nghe NPC ma chua xong (kem cach tinh). */
    public String cachTinh() {
        if ("thu_thach_gio".equals(kieu)) {
            return "Đồng hồ " + phut() + " phút tính từ con đầu tiên, quá giờ thì đếm lại.";
        }
        if ("quan_sat".equals(kieu)) {
            final String qs = dieuKien.get("dieu_kien_quan_sat");
            return qs != null && (qs.equals("dem") || qs.startsWith("gio=")) ? "Chỉ tính khi đúng giờ trong game." : "";
        }
        if ("nang_cap_cong".equals(kieu)) {
            return "Nâng ở chỗ Machamp; trượt dưới +7 không mất cấp.";
        }
        if ("nang_cap".equals(kieu)) {
            return "Tháo ra mặc lại một món cũng được tính.";
        }
        return "";
    }
}
