package com.htbb.log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bo dem BAT THUONG theo nhan vat cho bao cao van hanh (vps/theo-doi/bao-cao-ngay.sh).
 *
 * Hai loi nguoi choi gap ma log cu khong thay:
 *   NAP_MAP_LAP  client nap lai ban do lien tuc (goi 12 / -500 = "nap map xong") - vd ket tren thuyen 24/10/2026:
 *                ban do bi chan nap lai moi nhip, nguoi choi thay man hinh nhap nhay, log chi co [KHOA_MAP] bi gop.
 *   KEO_VE       may chu tra vi tri cu cho goi di chuyen (lenh 4): o cam, xuyen vat can, vuot quy toc do. Vai lan
 *                la binh thuong (tre mang); hang chuc lan trong mot phut = nhan vat bi "giat lui" lien tuc.
 *
 * Moi su kien chi dem trong bo nho (khong ghi log). Khi mot nhan vat dat {@code nguong} lan trong {@code cuaSoMs}
 * thi tra ve MOT dong de ghi, roi im lang voi nhan vat do {@code imLangMs} (khong lam ngap log). So khoa giu toi
 * da {@code soKhoaToiDa} (bo khoa cu nhat) - ten nhan vat khong lam bang nay phinh mai.
 *
 * Thoi gian truyen tu ngoai vao de kiem thu dieu khien duoc dong ho. Thread-safe.
 */
public final class TheoDoiBatThuong {

    /** goi 12 -500 (nap map xong): 6 lan / 30 giay / nhan vat, bao lai sau 5 phut. */
    public static final TheoDoiBatThuong NAP_MAP = new TheoDoiBatThuong("NAP_MAP_LAP", 6, 30000L, 300000L, 4096);
    /** vi tri bi tra ve (lenh 4): 20 lan / 60 giay / nhan vat, bao lai sau 5 phut. */
    public static final TheoDoiBatThuong KEO_VE = new TheoDoiBatThuong("KEO_VE", 20, 60000L, 300000L, 4096);

    private static final class Dem {
        long moc;
        int lan;
        int banDo = Integer.MIN_VALUE;
        boolean cungBanDo = true;
        long baoLuc = Long.MIN_VALUE / 2;
        final StringBuilder lyDo = new StringBuilder();
    }

    private final String loai;
    private final int nguong;
    private final long cuaSoMs;
    private final long imLangMs;
    private final int soKhoaToiDa;
    private final LinkedHashMap<String, Dem> bang = new LinkedHashMap<String, Dem>(64, 0.75f, true);

    public TheoDoiBatThuong(final String loai, final int nguong, final long cuaSoMs, final long imLangMs,
            final int soKhoaToiDa) {
        this.loai = loai;
        this.nguong = Math.max(1, nguong);
        this.cuaSoMs = Math.max(1L, cuaSoMs);
        this.imLangMs = Math.max(0L, imLangMs);
        this.soKhoaToiDa = Math.max(16, soKhoaToiDa);
    }

    public String loai() {
        return this.loai;
    }

    /**
     * Ghi nhan mot lan. {@code lyDo} (co the null) duoc gom - toi da vai ly do khac nhau - vao dong bao.
     *
     * @return dong can ghi (chua co tien to loai) khi vua cham nguong va khong trong thoi gian im lang; null neu khong.
     */
    public synchronized String ghiNhan(final String ten, final int banDo, final String lyDo, final long bayGio) {
        if (ten == null || ten.isEmpty()) {
            return null;
        }
        Dem d = this.bang.get(ten);
        if (d == null) {
            if (this.bang.size() >= this.soKhoaToiDa) {
                final Iterator<Map.Entry<String, Dem>> it = this.bang.entrySet().iterator();
                it.next();
                it.remove();
            }
            d = new Dem();
            d.moc = bayGio;
            this.bang.put(ten, d);
        } else if (bayGio - d.moc >= this.cuaSoMs) {
            d.moc = bayGio;
            d.lan = 0;
            d.banDo = Integer.MIN_VALUE;
            d.cungBanDo = true;
            d.lyDo.setLength(0);
        }
        d.lan++;
        if (d.banDo == Integer.MIN_VALUE) {
            d.banDo = banDo;
        } else if (d.banDo != banDo) {
            d.cungBanDo = false;
        }
        if (lyDo != null && d.lyDo.length() < 80 && d.lyDo.indexOf(lyDo) < 0) {
            if (d.lyDo.length() > 0) {
                d.lyDo.append(',');
            }
            d.lyDo.append(lyDo);
        }
        if (d.lan < this.nguong || bayGio - d.baoLuc < this.imLangMs) {
            return null;
        }
        d.baoLuc = bayGio;
        final StringBuilder s = new StringBuilder(96);
        s.append(ten).append(" lan=").append(d.lan).append('/').append((bayGio - d.moc + 999L) / 1000L).append('s');
        if (d.cungBanDo) {
            s.append(" cung_map=").append(d.banDo);
        } else {
            s.append(" map_cuoi=").append(banDo).append(" (nhieu map)");
        }
        if (d.lyDo.length() > 0) {
            s.append(" ly_do=").append(d.lyDo);
        }
        return s.toString();
    }

    /** Ghi nhan va - neu cham nguong - ghi MOT dong CANH_BAO vao nhat ky may chu. Khong bao gio nem loi. */
    public void ghi(final String ten, final int banDo, final String lyDo) {
        try {
            final String dong = ghiNhan(ten, banDo, lyDo, System.currentTimeMillis());
            if (dong != null) {
                Nhat.canhBao(this.loai, dong);
            }
        } catch (final RuntimeException ignored) {
            // theo doi khong bao gio duoc lam hong luong game
        }
    }

    /** So khoa dang giu (kiem thu). */
    public synchronized int soKhoa() {
        return this.bang.size();
    }
}
