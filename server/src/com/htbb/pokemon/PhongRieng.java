package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PHONG RIENG (0.9.7) - phan THUAN dung chung cho Pho ban Ham nguc (real.PhoBanGame) va Cup PvP tuan
 * (real.CupGame): ban do "phong rieng" chia khu (Map.nRegion), moi khu la MOT phong cua MOT to doi / MOT tran.
 *
 * <ul>
 *   <li>{@link Kho}: cap / tra khu trong (khu 0 de trong - nguoi vao nham ban do qua dang nhap cu roi ve khu 0 thi
 *       khong lot vao phong cua ai).</li>
 *   <li>Luat ban do (co rule cua ban do, cung tinh than co cho_phep_tu_dong cua nhanh khoa-map): ban do phong rieng
 *       KHONG cho Tu dong danh ({@link #khongTuDong}, TuDong.CauHinh.mapBiCam goi vao), KHONG cho doi khu bang NPC /
 *       lenh "khu", KHONG hoi sinh tai cho.</li>
 *   <li>Goi -112 (S-&gt;C, client BAN &gt;= {@link #BAN_CLIENT}): the trang thai phong (tieu de, 2 dong, dem nguoc,
 *       thanh tien do). Client cu khong nhan goi nay - chi nhan thong bao chu (com.htbb.thongbao.ThongBao).</li>
 * </ul>
 */
public final class PhongRieng {

    /** Lenh S-&gt;C the trang thai phong rieng. Da grep moi nhanh: -106 khoa-map/ten-pet, -107 giao-dien-nro, -110/-111 danh-hieu, -115 cap-nhat. */
    public static final int LENH = -112;
    /** Ban client dau tien biet lenh -112 (goi moi gate theo luat: BAN &gt;= 908). */
    public static final int BAN_CLIENT = 908;

    public static final byte AN = 0, PHO_BAN = 1, CUP = 2;

    /** Ban do phong rieng (map id da dang ky luc khoi dong). */
    private static final Set<Integer> MAP = ConcurrentHashMap.newKeySet();

    private PhongRieng() {
    }

    public static void dangKyMap(final int mapId) {
        MAP.add(Integer.valueOf(mapId));
    }

    public static boolean laMapRieng(final int mapId) {
        return MAP.contains(Integer.valueOf(mapId));
    }

    /** Luat ban do: phong rieng khong cho Tu dong danh (phai tu dieu khien - giong cho_phep_tu_dong = 0 cua khoa-map). */
    public static boolean khongTuDong(final int mapId) {
        return laMapRieng(mapId);
    }

    public static final String LY_DO_TU_DONG = "Phó bản và trận cúp không cho tự động đánh: hãy tự điều khiển.";
    public static final String LY_DO_DOI_KHU = "Đang ở phòng riêng (phó bản / trận cúp): không đổi khu được.";
    public static final String LY_DO_HOI_SINH = "Trong phó bản không hồi sinh tại chỗ được.";

    // ================================================================== kho khu

    /** Cap phat khu 1..n-1 cua mot ban do phong rieng (khu 0 khong dung). */
    public static final class Kho {
        private final boolean[] dung;

        public Kho(final int soKhu) {
            this.dung = new boolean[Math.max(1, soKhu)];
            this.dung[0] = true;
        }

        /** @return khu vua muon, -1 neu het phong. */
        public synchronized int muon() {
            for (int i = 1; i < dung.length; i++) {
                if (!dung[i]) {
                    dung[i] = true;
                    return i;
                }
            }
            return -1;
        }

        public synchronized void tra(final int khu) {
            if (khu > 0 && khu < dung.length) {
                dung[khu] = false;
            }
        }

        public synchronized boolean dangDung(final int khu) {
            return khu > 0 && khu < dung.length && dung[khu];
        }

        public synchronized int soTrong() {
            int n = 0;
            for (int i = 1; i < dung.length; i++) {
                if (!dung[i]) {
                    n++;
                }
            }
            return n;
        }

        public int soKhu() {
            return dung.length;
        }
    }

    // ================================================================== goi -112

    /**
     * Noi dung goi -112: byte loai ({@link #AN} = an the), UTF tieu de, UTF dong 1, UTF dong 2, short giay con lai
     * (-1 = khong dem), byte phan tram tien do (-1 = khong thanh). Chu da qua ChuPokemon o phia goi.
     */
    public static void viet(final DataOutputStream o, final byte loai, final String tieuDe, final String dong1,
            final String dong2, final int giayConLai, final int phanTram) throws IOException {
        o.writeByte(loai);
        o.writeUTF(cat(tieuDe, 60));
        o.writeUTF(cat(dong1, 90));
        o.writeUTF(cat(dong2, 90));
        o.writeShort(Math.max(-1, Math.min(Short.MAX_VALUE, giayConLai)));
        o.writeByte(phanTram < 0 ? -1 : Math.min(100, phanTram));
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() <= n ? s : s.substring(0, n);
    }

    /** "05:07" (phut:giay) cho dem nguoc trong chu thong bao. */
    public static String mmss(final int giay) {
        final int g = Math.max(0, giay);
        final int p = g / 60;
        final int s = g % 60;
        return (p < 10 ? "0" : "") + p + ":" + (s < 10 ? "0" : "") + s;
    }
}
