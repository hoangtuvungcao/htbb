package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CHE TAO o Machamp (Xuong Pewter) - thiet ke can bang 4.4 "Che tao". Lop THUAN (test/KiemBoNgocCheTao); phan game o
 * real.CheTaoGame.
 *
 * <ul>
 * <li>Cong thuc: bang data_noi_dung_che_tao (Vong co / Phien da he moi bac): (2 + bac) Da nang cap, (1 + bac/3) nguyen
 *     lieu phu (Bui sao / Manh sao / Manh thien thach / Vay rong), 50% gia xu cua bac, cot loi_che_tao (Loi che tao,
 *     mac dinh 0 cho toi khi phan ra ra Loi). Cap nguoi choi &gt;= cap cua cong thuc.</li>
 * <li>Bac hiem thanh pham: bang data_che_tao_bac (Hiem 60% / Quy 30% / Su thi 9% / Huyen thoai 1%). <b>Pity</b>: 20 lan
 *     lien tiep khong ra Su thi tro len thi lan thu 20 chac Su thi (spec "Bao hiem xui: Che tao").</li>
 * <li>Mon che ra CHUA MAC nen ban duoc (nghe "tho che"); Phien da he chon 1 trong 18 he: +5% ST he hoac -5% ST nhan.</li>
 * </ul>
 */
public final class CheTaoLogic {

    /** Nguong pity mac dinh (lan thu 20 chac Su thi). */
    public static final int PITY_MAC_DINH = 20;
    /** % chi so phien da he (+5% ST he X hoac -5% ST nhan tu he X), phan nghin. */
    public static final int PHIEN_DA_PHAN_NGHIN = 50;

    private CheTaoLogic() {
    }

    /** Mot cong thuc (dong data_noi_dung_che_tao). */
    public static final class CongThuc {
        public final int idTemplate;
        public final String ten;
        public final int bac;
        public final int cap;
        public final int da;
        public final String nlPhu;
        public final int idNlPhu;
        public final int soNlPhu;
        public final long xu;
        public final int loi;
        public final boolean phienDa;
        public final boolean bat;

        public CongThuc(final int idTemplate, final String ten, final int bac, final int cap, final int da,
                final String nlPhu, final int idNlPhu, final int soNlPhu, final long xu, final int loi,
                final boolean phienDa, final boolean bat) {
            this.idTemplate = idTemplate;
            this.ten = ten == null ? "" : ten;
            this.bac = bac;
            this.cap = cap;
            this.da = Math.max(0, da);
            this.nlPhu = nlPhu == null ? "" : nlPhu;
            this.idNlPhu = idNlPhu;
            this.soNlPhu = Math.max(0, soNlPhu);
            this.xu = Math.max(0L, xu);
            this.loi = Math.max(0, loi);
            this.phienDa = phienDa;
            this.bat = bat;
        }
    }

    /** Mot bac hiem thanh pham (dong data_che_tao_bac). ma tang dan theo do hiem. */
    public static final class BacHiem {
        public final int ma;
        public final String ten;
        public final int colorName;
        public final int phanNghin;
        public final int heSoPhanTram;
        public final int soLo;

        public BacHiem(final int ma, final String ten, final int colorName, final int phanNghin, final int heSoPhanTram,
                final int soLo) {
            this.ma = ma;
            this.ten = ten == null ? "" : ten;
            this.colorName = colorName;
            this.phanNghin = Math.max(0, phanNghin);
            this.heSoPhanTram = Math.max(100, Math.min(NganSachTrangBi.HE_SO_MON, heSoPhanTram));
            this.soLo = Math.max(0, Math.min(NgocHe.LO_TOI_DA, soLo));
        }
    }

    /** Bang mac dinh (spec 4.4) khi CSDL chua co data_che_tao_bac. Ma 3 = Su thi. */
    public static List<BacHiem> bacMacDinh() {
        final List<BacHiem> v = new ArrayList<BacHiem>();
        v.add(new BacHiem(1, "Hiếm", 3, 600, 110, 1));
        v.add(new BacHiem(2, "Quý", 2, 300, 120, 2));
        v.add(new BacHiem(3, "Sử thi", 1, 90, 130, 2));
        v.add(new BacHiem(4, "Huyền thoại", 1, 10, 130, 3));
        return Collections.unmodifiableList(v);
    }

    /** Ma bac "Su thi" (moc pity). */
    public static final int MA_SU_THI = 3;

    /** Nguyen lieu phu theo bac do (Bui sao 0-2, Manh sao 3-5, Manh thien thach 6-8, Vay rong 9-10). */
    public static String nlPhuTheoBac(final int bac) {
        if (bac <= 2) {
            return "Bụi sao";
        }
        if (bac <= 5) {
            return "Mảnh sao";
        }
        if (bac <= 8) {
            return "Mảnh thiên thạch";
        }
        return "Vảy rồng";
    }

    /** Luat dau vao spec: (2 + bac) da. */
    public static int daTheoBac(final int bac) {
        return 2 + Math.max(0, bac);
    }

    /** Luat dau vao spec: 1 + bac/3 nguyen lieu phu. */
    public static int nlPhuSoTheoBac(final int bac) {
        return 1 + Math.max(0, bac) / 3;
    }

    /**
     * Quay bac hiem: {@code roll} 0..(tong phan nghin - 1); {@code demTruoc} = so lan lien tiep chua ra Su thi TRUOC lan
     * nay. Lan thu {@code nguong} (demTruoc + 1 &gt;= nguong) ma quay ra duoi Su thi thi nang len Su thi.
     *
     * @return phan tu cua {@code ds} (null neu ds rong)
     */
    public static BacHiem quay(final List<BacHiem> ds, final int roll, final int demTruoc, final int nguong) {
        if (ds == null || ds.isEmpty()) {
            return null;
        }
        int tong = 0;
        for (final BacHiem b : ds) {
            tong += b.phanNghin;
        }
        BacHiem ra = ds.get(0);
        if (tong > 0) {
            int r = Math.floorMod(roll, tong);
            for (final BacHiem b : ds) {
                if (r < b.phanNghin) {
                    ra = b;
                    break;
                }
                r -= b.phanNghin;
            }
        }
        if (nguong > 0 && demTruoc + 1 >= nguong && ra.ma < MA_SU_THI) {
            for (final BacHiem b : ds) {
                if (b.ma == MA_SU_THI) {
                    return b;
                }
            }
        }
        return ra;
    }

    /** Bo dem pity sau khi ra {@code ra}: ve 0 khi &gt;= Su thi, khong thi +1. */
    public static int pitySau(final int demTruoc, final BacHiem ra) {
        return ra != null && ra.ma >= MA_SU_THI ? 0 : Math.max(0, demTruoc) + 1;
    }

    /** Ti le (phan nghin) cua bac {@code ma} trong bang (hien tren man che tao). */
    public static int phanNghin(final List<BacHiem> ds, final int ma) {
        for (final BacHiem b : ds) {
            if (b.ma == ma) {
                return b.phanNghin;
            }
        }
        return 0;
    }

    /** Chi so mon sau he so bac hiem (%), luon &gt;= goc, &lt;= 130%. */
    public static int nhanChiSo(final int goc, final int heSoPhanTram) {
        if (goc <= 0) {
            return goc;
        }
        final int hs = Math.max(100, Math.min(NganSachTrangBi.HE_SO_MON, heSoPhanTram));
        return (int) Math.min(Short.MAX_VALUE, Math.round(goc * hs / 100.0));
    }

    /** Thieu gi de che (null = du): theo thu tu cap, xu, da, nguyen lieu phu, loi. */
    public static String thieu(final CongThuc c, final int capNv, final long xu, final int da, final int nlPhu,
            final int loi) {
        if (c == null || !c.bat) {
            return "Công thức đang tạm khoá.";
        }
        if (capNv < c.cap) {
            return "Cần cấp " + c.cap + ".";
        }
        if (xu < c.xu) {
            return "Cần " + c.xu + " xu.";
        }
        if (da < c.da) {
            return "Cần " + c.da + " Đá nâng cấp (có " + da + ").";
        }
        if (c.soNlPhu > 0 && nlPhu < c.soNlPhu) {
            return "Cần " + c.soNlPhu + " " + c.nlPhu + " (có " + nlPhu + ").";
        }
        if (c.loi > 0 && loi < c.loi) {
            return "Cần " + c.loi + " Lõi chế tạo (có " + loi + ").";
        }
        return null;
    }
}
