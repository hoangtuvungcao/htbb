package com.htbb.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gioi han + GOP cac dong lap lai theo cua so thoi gian co dinh.
 *
 * Moi "khoa" (mau dong log, loai, ma loi...) duoc phep toi da {@code toiDa} lan trong mot
 * cua so {@code cuaSoMs}. Qua muc thi bo, chi dem; het cua so thi {@link #tomTat(long)} tra
 * ve mot dong tom tat "x N lan trong 60s" de khong mat dau vet nao - chi mat ban sao.
 *
 * Thoi gian truyen tu ngoai vao (khong goi System.currentTimeMillis() ben trong) de kiem thu
 * dieu khien duoc dong ho. Thread-safe (synchronized) - nhieu luong game cung ghi log.
 *
 * So khoa bi chan tren {@code soKhoaToiDa}: ten nhan vat / so ngau nhien trong log co the sinh
 * ra vo so mau khac nhau, khong chan thi bang nay tu no thanh ro ri bo nho.
 */
public final class GioiHanLap {

    /** Mot dong tom tat: khoa, so lan bi bo, do dai cua so (ms). */
    public static final class TomTat {
        public final String khoa;
        public final int soBo;
        public final int soChoPhep;
        public final long cuaSoMs;

        TomTat(final String khoa, final int soChoPhep, final int soBo, final long cuaSoMs) {
            this.khoa = khoa;
            this.soChoPhep = soChoPhep;
            this.soBo = soBo;
            this.cuaSoMs = cuaSoMs;
        }

        @Override
        public String toString() {
            return "x" + (this.soChoPhep + this.soBo) + " lan trong " + (this.cuaSoMs / 1000L) + "s (bo bot "
                    + this.soBo + "): " + this.khoa;
        }
    }

    private static final class Dem {
        long moc;
        int choPhep;
        int bo;
    }

    private volatile int toiDa;
    private volatile long cuaSoMs;
    private final int soKhoaToiDa;
    private final LinkedHashMap<String, Dem> bang = new LinkedHashMap<String, Dem>();
    /** Tom tat cua cac cua so da dong ma chua ai lay (khi khoa duoc dung lai sau khi het cua so). */
    private final List<TomTat> choLay = new ArrayList<TomTat>();
    /** So lan bi bo vi tran bang khoa (khong co cho de dem rieng). */
    private int boViTranBang;

    public GioiHanLap(final int toiDa, final long cuaSoMs, final int soKhoaToiDa) {
        this.toiDa = toiDa;
        this.cuaSoMs = cuaSoMs;
        this.soKhoaToiDa = Math.max(16, soKhoaToiDa);
    }

    public void dat(final int toiDaMoi, final long cuaSoMoi) {
        this.toiDa = toiDaMoi;
        this.cuaSoMs = Math.max(1L, cuaSoMoi);
    }

    public int toiDa() {
        return this.toiDa;
    }

    public long cuaSoMs() {
        return this.cuaSoMs;
    }

    /**
     * Ghi nhan mot lan xuat hien cua {@code khoa}.
     *
     * @return true = duoc phep ghi/gui; false = da qua muc trong cua so nay (chi dem lai).
     *         toiDa <= 0 nghia la KHONG gioi han (luon true).
     */
    public synchronized boolean choPhep(final String khoa, final long bayGio) {
        if (this.toiDa <= 0) {
            return true;
        }
        Dem d = this.bang.get(khoa);
        if (d == null) {
            if (this.bang.size() >= this.soKhoaToiDa) {
                donCu(bayGio);
                if (this.bang.size() >= this.soKhoaToiDa) {
                    this.boViTranBang++;
                    return false;
                }
            }
            d = new Dem();
            d.moc = bayGio;
            this.bang.put(khoa, d);
        } else if (bayGio - d.moc >= this.cuaSoMs) {
            if (d.bo > 0) {
                this.choLay.add(new TomTat(khoa, d.choPhep, d.bo, this.cuaSoMs));
            }
            d.moc = bayGio;
            d.choPhep = 0;
            d.bo = 0;
        }
        if (d.choPhep < this.toiDa) {
            d.choPhep++;
            return true;
        }
        d.bo++;
        return false;
    }

    /** So lan da bi bo cua khoa trong cua so hien tai (cho kiem thu / chi so). */
    public synchronized int dangBo(final String khoa) {
        final Dem d = this.bang.get(khoa);
        return d == null ? 0 : d.bo;
    }

    /**
     * Lay tom tat cua moi cua so DA HET HAN co dong bi bo, va don cac khoa nhan roi.
     * Goi dinh ky (luong ghi log goi moi giay).
     */
    public synchronized List<TomTat> tomTat(final long bayGio) {
        final List<TomTat> ra = new ArrayList<TomTat>(this.choLay);
        this.choLay.clear();
        final Iterator<Map.Entry<String, Dem>> it = this.bang.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, Dem> e = it.next();
            final Dem d = e.getValue();
            if (bayGio - d.moc >= this.cuaSoMs) {
                if (d.bo > 0) {
                    ra.add(new TomTat(e.getKey(), d.choPhep, d.bo, this.cuaSoMs));
                }
                it.remove();
            }
        }
        if (this.boViTranBang > 0) {
            ra.add(new TomTat("(qua nhieu mau khac nhau - tran bang " + this.soKhoaToiDa + " khoa)", 0,
                    this.boViTranBang, this.cuaSoMs));
            this.boViTranBang = 0;
        }
        return ra;
    }

    /** Bo cac khoa da het cua so ma khong co gi de tom tat (giai phong cho khi bang day). */
    private void donCu(final long bayGio) {
        final Iterator<Map.Entry<String, Dem>> it = this.bang.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, Dem> e = it.next();
            final Dem d = e.getValue();
            if (bayGio - d.moc >= this.cuaSoMs) {
                if (d.bo > 0) {
                    this.choLay.add(new TomTat(e.getKey(), d.choPhep, d.bo, this.cuaSoMs));
                }
                it.remove();
            }
        }
    }

    public synchronized int soKhoa() {
        return this.bang.size();
    }
}
