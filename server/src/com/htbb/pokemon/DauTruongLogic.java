package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * DAU TRUONG BAO BOI (he_thong_day_du.md 3.6, D12) - phan THUAN ngoai tran: doi hinh, diem mua (kieu Elo, TACH khoi Elo
 * PvP xep hang), luot ngay, ghep doi thu bat dong bo, chong nick phu / chong cay, thuong co tran. Game:
 * real.DauTruongGame. Test: test/KiemDauTruong.
 *
 * <ul>
 * <li><b>Doi hinh:</b> 3 con KHAC NHAU trong Hop PC (Hop PC chi co Pokemon da thu phuc - khong bao gio co nhan vat nguoi
 *     choi), khong trung loai, khong loai T6 (Huyen thoai) - "Mở (cấm T6)".</li>
 * <li><b>Diem mua:</b> bat dau {@link #DIEM_BAN_DAU}, K = {@link #K} ben tan cong, ben phong thu doi {@link
 *     #PHAN_TRAM_THU}% muc do (bi danh luc offline nen doi it hon), san {@link #DIEM_SAN}. Mua = mua PvP 13 tuan
 *     (ThangPvp.mua), diem rieng tob_dau_truong_mua.</li>
 * <li><b>Luot:</b> {@link #LUOT_NGAY} tran tan cong / ngay (tinh luc bat dau - roi tran khong hoan luot), lam moi danh
 *     sach {@link #LAM_MOI_NGAY} lan / ngay.</li>
 * <li><b>Ghep:</b> doi thu co doi phong thu, |diem| &lt;= {@link #cuaSoDiem} (100 + 100 moi lan lam moi, toi da 400),
 *     suc manh doi (tong chi so cap 50) lech &lt;= {@link #daiSucManh} % (15 + 10 moi lan lam moi, toi da 50); khong
 *     cung tai khoan / thiet bi / IP (XepHangPvp.Cum); da gap {@link #TRAN_LAP_TOI_DA} lan trong 24 gio thi khong ghep.</li>
 * <li><b>Chong cay:</b> he so diem + thuong voi cung doi thu trong 24 gio x1, x1/2, x1/4, roi x0 (XepHangPvp.heSoLap).</li>
 * <li><b>Thuong:</b> thang = {@link #LUONG_THANG} luong khoa x he so lap, tran {@link #TRAN_LUONG_TUAN} / tuan (tuan cup,
 *     thu Hai 05:00); KHONG xu. Cuoi mua (&gt;= {@link #TRAN_TOI_THIEU_MUA} tran tan cong): danh hieu mua co han
 *     ({@link #danhHieuMua}), khong luong.</li>
 * </ul>
 */
public final class DauTruongLogic {

    private DauTruongLogic() {
    }

    public static final int DIEM_BAN_DAU = 1000;
    public static final int K = 32;
    public static final int PHAN_TRAM_THU = 50;
    public static final int DIEM_SAN = 100;
    public static final int LUOT_NGAY = 5;
    public static final int LAM_MOI_NGAY = 10;
    public static final int SO_DE_XUAT = 3;
    public static final int TRAN_LAP_TOI_DA = 3;
    public static final int LUONG_THANG = 3;
    public static final int TRAN_LUONG_TUAN = 25;
    public static final int TRAN_TOI_THIEU_MUA = 10;
    public static final int GIAY_LUOT = 20;
    public static final int CAP_TOI_THIEU = 20;
    /** Danh hieu mua (data_danh_hieu.id): quan quan (hang 1), tinh anh (2..10), dau thu (11..100 hoac diem &gt;= 1300). */
    public static final int DH_QUAN_QUAN = 9870, DH_TINH_ANH = 9871, DH_DAU_THU = 9872;
    public static final int DIEM_DAU_THU = 1300;
    public static final String[] MA_DANH_HIEU = {"dau_truong_quan_quan", "dau_truong_tinh_anh", "dau_truong_dau_thu"};
    public static final String[] TEN_BAC = {"Tập sự", "Đồng", "Bạc", "Vàng", "Bạch kim", "Kim cương", "Huyền thoại"};
    public static final int[] NGUONG_BAC = {900, 1100, 1250, 1400, 1550, 1700};
    /**
     * Sân đấu (ảnh client res/ui/dau_truong_san_&lt;n&gt;.png, tools/pokemon/sinh_anh_dau_truong.py): mở theo BẬC của người
     * tấn công (leo bậc = sân đẹp hơn); mùa thứ 4 mỗi năm ({@link #sanCho}) mọi trận đấu ở sân đêm Ma (biến thể mùa).
     */
    public static final String[] TEN_SAN = {"Sân cỏ Viridian", "Sân đá Pewter", "Hồ nước Cerulean", "Gym điện Vermilion",
        "Sân băng Mahogany", "Núi lửa Cinnabar", "Đỉnh rồng Blackthorn", "Sân đêm Lavender"};
    public static final int SAN_DEM = 7;
    /** Tốc độ diễn ×2 / ×3 mở theo cấp nhân vật (cấu hình dau_truong.cap_toc_2 / cap_toc_3 ghi đè). */
    public static final int CAP_TOC_2 = 20, CAP_TOC_3 = 40;

    /** Sân của trận: theo bậc người tấn công (0..6), mùa thứ 4 mỗi năm = sân đêm. */
    public static int sanCho(final int bac, final int mua) {
        if (Math.floorMod(mua, 4) == 3) {
            return SAN_DEM;
        }
        return Math.max(0, Math.min(SAN_DEM - 1, bac));
    }

    // ================================================================== doi hinh

    /**
     * Ly do doi hinh KHONG hop le (null = hop le).
     *
     * @param ids  3 id con trong Hop PC
     * @param hop  Hop PC cua chu
     * @param d    du lieu loai (bac T6)
     */
    public static String loiDoi(final int[] ids, final HopPc hop, final DauTruongDuLieu d) {
        if (ids == null || ids.length != DauTruongTran.SO_CON) {
            return "Đội hình cần đúng 3 Pokémon.";
        }
        if (hop == null) {
            return "Chưa mở được Hộp PC.";
        }
        final int[] dex = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            for (int j = 0; j < i; j++) {
                if (ids[i] == ids[j]) {
                    return "Mỗi Pokémon chỉ đứng một ô.";
                }
            }
            final HopPc.Con c = hop.lay(ids[i]);
            if (c == null) {
                return "Pokémon không còn trong Hộp PC.";
            }
            if (d != null && d.bac(c.dex) >= LoaiDuLieu.T6) {
                return d.ten(c.dex) + " là Huyền thoại, không vào Đấu trường được.";
            }
            dex[i] = c.dex;
            for (int j = 0; j < i; j++) {
                if (dex[j] == dex[i]) {
                    return "Đội không được có hai Pokémon cùng loài.";
                }
            }
        }
        return null;
    }

    /** "12,7,40" -&gt; {12,7,40}; hong / thieu = null. */
    public static int[] docIds(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        final String[] p = s.split(",");
        if (p.length != DauTruongTran.SO_CON) {
            return null;
        }
        final int[] kq = new int[p.length];
        try {
            for (int i = 0; i < p.length; i++) {
                kq[i] = Integer.parseInt(p[i].trim());
            }
        } catch (final NumberFormatException e) {
            return null;
        }
        return kq;
    }

    public static String ghiIds(final int[] ids) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; ids != null && i < ids.length; i++) {
            sb.append(i > 0 ? "," : "").append(ids[i]);
        }
        return sb.toString();
    }

    /** Suc manh doi = tong suc manh cap 50 cua 3 con. */
    public static int sucManhDoi(final int[] ids, final HopPc hop, final DauTruongDuLieu d) {
        int t = 0;
        for (int i = 0; ids != null && hop != null && i < ids.length; i++) {
            final HopPc.Con c = hop.lay(ids[i]);
            if (c != null) {
                t += d.sucManh50(c.dex, c.iv, c.tinhCach);
            }
        }
        return t;
    }

    // ================================================================== diem

    /** Diem moi cua ben tan cong. {@code thang}: 1 thang / 0 thua; heSo phan nghin (chong cay). */
    public static int diemCong(final int diemA, final int diemB, final int thang, final int heSo) {
        final int ky = ThangPvp.kyVongPhanNghin(diemA, diemB);
        final long d = Math.round(K * (thang * 1000 - ky) / 1000.0) * (long) Math.max(0, Math.min(1000, heSo)) / 1000L;
        return (int) Math.max(DIEM_SAN, diemA + d);
    }

    /** Diem moi cua ben phong thu (doi {@link #PHAN_TRAM_THU}% muc chuan, chieu nguoc). {@code congThang} 1 = thu thua. */
    public static int diemThu(final int diemThu, final int diemCong, final int congThang, final int heSo) {
        final int ky = ThangPvp.kyVongPhanNghin(diemThu, diemCong);
        final int ketQua = 1 - congThang;
        final long d = Math.round(K * (ketQua * 1000 - ky) / 1000.0) * PHAN_TRAM_THU / 100L
                * Math.max(0, Math.min(1000, heSo)) / 1000L;
        return (int) Math.max(DIEM_SAN, diemThu + d);
    }

    public static int bac(final int diem) {
        int b = 0;
        for (int i = 0; i < NGUONG_BAC.length; i++) {
            if (diem >= NGUONG_BAC[i]) {
                b = i + 1;
            }
        }
        return b;
    }

    /** He so (phan nghin) cho tran thu n+1 voi cung doi thu trong 24 gio. */
    public static int heSoLap(final int nTruoc) {
        return XepHangPvp.heSoLap(nTruoc);
    }

    // ================================================================== luot / thuong

    public static int luotConLai(final int daDanhHomNay) {
        return Math.max(0, LUOT_NGAY - Math.max(0, daDanhHomNay));
    }

    /** Luong khoa cua mot tran (da kep tran tuan). */
    public static int luongTran(final boolean thang, final int heSo, final int daNhanTuan) {
        return luongTran(thang, heSo, daNhanTuan, false);
    }

    /** Thang dau tien trong ngay: them {@link #THUONG_THANG_DAU} luong khoa (van trong tran tuan {@link #TRAN_LUONG_TUAN}). */
    public static final int THUONG_THANG_DAU = 2;

    public static int luongTran(final boolean thang, final int heSo, final int daNhanTuan, final boolean thangDauNgay) {
        if (!thang) {
            return 0;
        }
        final int goc = LUONG_THANG * Math.max(0, Math.min(1000, heSo)) / 1000 + (thangDauNgay && heSo > 0 ? THUONG_THANG_DAU : 0);
        return Math.max(0, Math.min(goc, TRAN_LUONG_TUAN - Math.max(0, daNhanTuan)));
    }

    /** Chuoi thang (chi hinh): so tran thang lien tiep gan nhat; ketQua moi nhat truoc (1 thang, 0 thua). */
    public static int chuoiThang(final int[] ketQuaMoiTruoc) {
        int n = 0;
        for (int i = 0; ketQuaMoiTruoc != null && i < ketQuaMoiTruoc.length && ketQuaMoiTruoc[i] == 1; i++) {
            n++;
        }
        return n;
    }

    /** Danh hieu bac cuoi mua (Dong .. Huyen thoai) = DH_BAC + bac - 1 (9873..9878). */
    public static final int DH_BAC = 9873;
    public static final String[] MA_DANH_HIEU_BAC = {"", "dau_truong_bac_dong", "dau_truong_bac_bac", "dau_truong_bac_vang",
        "dau_truong_bac_bach_kim", "dau_truong_bac_kim_cuong", "dau_truong_bac_huyen_thoai"};

    /** Danh hieu cuoi mua: 0 = khong; con lai DH_*. */
    public static int danhHieuMua(final int hang, final int diem, final int soTran) {
        if (soTran < TRAN_TOI_THIEU_MUA || hang <= 0) {
            return 0;
        }
        if (hang == 1) {
            return DH_QUAN_QUAN;
        }
        if (hang <= 10) {
            return DH_TINH_ANH;
        }
        final int bac = bac(diem);
        if (bac >= 4) {
            return DH_BAC + bac - 1;   // Bach kim / Kim cuong / Huyen thoai: danh hieu bac dep hon Dau thu
        }
        if (hang <= 100 || diem >= DIEM_DAU_THU) {
            return DH_DAU_THU;
        }
        return bac >= 1 ? DH_BAC + bac - 1 : 0;
    }

    public static String maDanhHieu(final int id) {
        if (id >= DH_BAC && id < DH_BAC + MA_DANH_HIEU_BAC.length - 1) {
            return MA_DANH_HIEU_BAC[id - DH_BAC + 1];
        }
        return id == DH_QUAN_QUAN ? MA_DANH_HIEU[0] : id == DH_TINH_ANH ? MA_DANH_HIEU[1] : id == DH_DAU_THU ? MA_DANH_HIEU[2] : "";
    }

    // ================================================================== ghep

    public static int cuaSoDiem(final int lanLamMoi) {
        return Math.min(400, 100 + 100 * Math.max(0, lanLamMoi));
    }

    public static int daiSucManh(final int lanLamMoi) {
        return Math.min(50, 15 + 10 * Math.max(0, lanLamMoi));
    }

    /** Mot nguoi co doi phong thu (ung vien doi thu). */
    public static final class UngVien {
        public final int id, diem, sucManh;
        public final XepHangPvp.Cum cum;
        /** So tran nguoi xin da danh voi nguoi nay trong 24 gio. */
        public final int tran24h;

        public UngVien(final int id, final int diem, final int sucManh, final XepHangPvp.Cum cum, final int tran24h) {
            this.id = id;
            this.diem = diem;
            this.sucManh = sucManh;
            this.cum = cum;
            this.tran24h = tran24h;
        }
    }

    public static boolean ghepDuoc(final int idToi, final int diemToi, final int sucToi, final XepHangPvp.Cum cumToi,
            final UngVien u, final int lanLamMoi) {
        if (u == null || u.id == idToi || u.sucManh <= 0) {
            return false;
        }
        if (cumToi != null && cumToi.trung(u.cum)) {
            return false;
        }
        if (u.tran24h >= TRAN_LAP_TOI_DA) {
            return false;
        }
        if (Math.abs(u.diem - diemToi) > cuaSoDiem(lanLamMoi)) {
            return false;
        }
        final int lech = Math.abs(u.sucManh - sucToi) * 100 / Math.max(1, sucToi);
        return lech <= daiSucManh(lanLamMoi);
    }

    /** Toi da {@link #SO_DE_XUAT} doi thu: gan diem nhat, it gap nhat; bang nhau xao bang hat. */
    public static List<UngVien> deXuat(final int idToi, final int diemToi, final int sucToi, final XepHangPvp.Cum cumToi,
            final List<UngVien> ds, final int lanLamMoi, final long hat) {
        final List<UngVien> ok = new ArrayList<UngVien>();
        for (final UngVien u : ds) {
            if (ghepDuoc(idToi, diemToi, sucToi, cumToi, u, lanLamMoi)) {
                ok.add(u);
            }
        }
        final DauTruongTran.Rng r = new DauTruongTran.Rng(hat);
        final long[] xao = new long[ok.size()];
        for (int i = 0; i < xao.length; i++) {
            xao[i] = r.tiep();
        }
        final List<Integer> chiSo = new ArrayList<Integer>();
        for (int i = 0; i < ok.size(); i++) {
            chiSo.add(Integer.valueOf(i));
        }
        chiSo.sort((x, y) -> {
            final UngVien a = ok.get(x.intValue()), b = ok.get(y.intValue());
            final int c = Integer.compare(a.tran24h, b.tran24h);
            if (c != 0) {
                return c;
            }
            final int e = Integer.compare(Math.abs(a.diem - diemToi), Math.abs(b.diem - diemToi));
            return e != 0 ? e : Long.compare(xao[x.intValue()], xao[y.intValue()]);
        });
        final List<UngVien> kq = new ArrayList<UngVien>();
        for (int i = 0; i < chiSo.size() && kq.size() < SO_DE_XUAT; i++) {
            kq.add(ok.get(chiSo.get(i).intValue()));
        }
        return kq;
    }
}
