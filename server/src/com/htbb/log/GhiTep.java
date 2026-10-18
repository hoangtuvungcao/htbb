package com.htbb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Mot tep log xoay vong THEO NGAY + chan theo DUNG LUONG (khong thu vien ngoai).
 *
 *   &lt;thuMuc&gt;/&lt;tienTo&gt;-yyyyMMdd.log, khi day thi -yyyyMMdd.1.log, .2.log ... toi soPhan.
 *   Het ca soPhan trong ngay thi chi con ghi dong LOI (khong de mot con bao log lap day dia),
 *   so dong bo duoc dem lai va bao vao dau tep ngay hom sau.
 *   Tep cu hon giuNgay ngay bi xoa luc doi ngay.
 *
 * KHONG thread-safe: chi luong ghi log (Nhat) goi vao.
 *
 * Dat trong thu muc con rieng (mac dinh &lt;sv.pathLog&gt;/nhat_ky/) chu khong de canh game.out:
 * vps/htbb-logrotate.conf co glob /opt/htbb/log/*.log (weekly, doi ten) - de chung thu muc thi
 * logrotate va bo xoay vong nay gianh nhau cung mot tep.
 */
final class GhiTep {

    private final File thuMuc;
    private final String tienTo;
    private final long toiDaByte;
    private final int soPhan;
    private final int giuNgay;

    private String ngay = "";
    private int phan;
    private long daGhi;
    private BufferedWriter ra;
    /** Dong bi bo vi het han muc dung luong trong ngay. */
    private long boViDay;
    private boolean daBaoLoiMo;

    GhiTep(final File thuMuc, final String tienTo, final long toiDaByte, final int soPhan, final int giuNgay) {
        this.thuMuc = thuMuc;
        this.tienTo = tienTo;
        this.toiDaByte = Math.max(1024L * 1024L, toiDaByte);
        this.soPhan = Math.max(1, soPhan);
        this.giuNgay = Math.max(1, giuNgay);
    }

    static String ngayCua(final long ms) {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(ms));
    }

    File tepHienTai() {
        return new File(this.thuMuc, this.tienTo + "-" + this.ngay + (this.phan == 0 ? "" : "." + this.phan) + ".log");
    }

    /** @param loi dong muc LOI - van ghi khi da het han muc dung luong trong ngay. */
    void ghi(final String dong, final boolean loi, final long bayGio) {
        try {
            final String n = ngayCua(bayGio);
            if (!n.equals(this.ngay)) {
                doiNgay(n);
            }
            if (this.ra == null) {
                return;
            }
            final int dai = dong.length() + 1;
            if (this.daGhi + dai > this.toiDaByte) {
                if (this.phan + 1 < this.soPhan) {
                    dong();
                    this.phan++;
                    mo();
                } else if (!loi) {
                    this.boViDay++;
                    return;
                }
            }
            this.ra.write(dong);
            this.ra.write('\n');
            // UTF-8: tieng Viet co dau ~1.3 byte/ky tu; uoc luong theo ky tu la du cho han muc mem.
            this.daGhi += dai;
        } catch (final IOException e) {
            baoLoiMo(e);
        }
    }

    void xa() {
        try {
            if (this.ra != null) {
                this.ra.flush();
            }
        } catch (final IOException e) {
            baoLoiMo(e);
        }
    }

    void dong() {
        try {
            if (this.ra != null) {
                this.ra.close();
            }
        } catch (final IOException ignored) {
        }
        this.ra = null;
    }

    private void doiNgay(final String n) throws IOException {
        dong();
        final long boHomQua = this.boViDay;
        this.ngay = n;
        this.phan = 0;
        this.boViDay = 0;
        if (!this.thuMuc.isDirectory() && !this.thuMuc.mkdirs()) {
            baoLoiMo(new IOException("khong tao duoc thu muc " + this.thuMuc));
            return;
        }
        // Tiep tuc phan cuoi cung neu server khoi dong lai giua ngay.
        while (this.phan + 1 < this.soPhan && tepHienTai().length() >= this.toiDaByte) {
            this.phan++;
        }
        mo();
        if (boHomQua > 0) {
            ghi("[NHAT] ngay truoc da bo " + boHomQua + " dong vi het han muc dung luong (" + this.soPhan + " tep x "
                    + (this.toiDaByte / 1024 / 1024) + "MB)", true, System.currentTimeMillis());
        }
        donTepCu();
    }

    private void mo() throws IOException {
        final File f = tepHienTai();
        this.daGhi = f.length();
        this.ra = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8), 65536);
    }

    private void donTepCu() {
        final File[] ds = this.thuMuc.listFiles();
        if (ds == null) {
            return;
        }
        final String moc = ngayCua(System.currentTimeMillis() - this.giuNgay * 86400000L);
        final String dau = this.tienTo + "-";
        for (final File f : ds) {
            final String ten = f.getName();
            if (ten.startsWith(dau) && ten.endsWith(".log") && ten.length() >= dau.length() + 8) {
                final String n = ten.substring(dau.length(), dau.length() + 8);
                if (n.compareTo(moc) < 0) {
                    f.delete();
                }
            }
        }
    }

    private void baoLoiMo(final IOException e) {
        if (!this.daBaoLoiMo) {
            this.daBaoLoiMo = true;
            Nhat.manHinhGoc("[NHAT] khong ghi duoc tep log " + this.thuMuc + "/" + this.tienTo + ": " + e);
        }
    }
}
