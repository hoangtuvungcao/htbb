package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Che mat khau CSDL trong moi thu di ra System.out / System.err.
 *
 * VI SAO PHAI CO. data.ConnPool (nam trong lib/htbb-core.jar, KHONG co ma nguon nen khong
 * sua duoc) in ra dong nay ngay trong ham dung, moi lan tao pool:
 *
 *     URL: jdbc:mysql://localhost:3306/htbb_db | USER: htbb | PASS: &lt;mat khau that&gt;
 *
 * Bon pool -> bon dong moi lan khoi dong, di thang vao /opt/htbb/log/game.out, va cac tep
 * log lai de quyen -rw-r--r--. Khong sua duoc ben trong jar thi chan o dau ra.
 *
 * Loc theo TUNG DONG chu khong ghi de println(String): nhu vay bat duoc ca printf, ca
 * printStackTrace, ca nhung cho goi write() thang - khong phu thuoc vao viec ai in kieu gi.
 *
 * Chi che chuoi tu 6 ky tu tro len. Mat khau ngan hon the thi bo qua, vi che mot chuoi
 * qua ngan se an nham ca chu binh thuong trong log va lam log thanh vo dung.
 */
public final class LocBiMat extends OutputStream {

    private static final String CHE = "***";
    /** Do dai toi thieu de mot chuoi duoc coi la bi mat dang che. */
    public static final int DAI_TOI_THIEU = 6;
    /** Dong dai hon nguong nay thi xa bot, de mot dong khong xuong dong khong an het bo nho. */
    private static final int TOI_DA = 65536;

    private static final List<String> BI_MAT = new ArrayList<String>();

    private final OutputStream goc;
    private final ByteArrayOutputStream dem = new ByteArrayOutputStream(256);

    private LocBiMat(final OutputStream goc) {
        this.goc = goc;
    }

    /**
     * Dang ky mat khau tu server.ini roi boc System.out/System.err lai.
     *
     * PHAI goi TRUOC Database.setLink(...) - ConnPool in mat khau ngay trong ham dung,
     * nen goi sau thi dong do da ra log mat roi.
     */
    public static void batDau(final Properties p) {
        themBiMat(p.getProperty("db.password"));
        themBiMat(p.getProperty("db.pass"));
        themBiMat(p.getProperty("db.password1"));
        if (BI_MAT.isEmpty()) {
            return;
        }
        System.setOut(boc(System.out));
        System.setErr(boc(System.err));
    }

    public static void themBiMat(final String s) {
        if (s != null && s.length() >= DAI_TOI_THIEU && !BI_MAT.contains(s)) {
            BI_MAT.add(s);
        }
    }

    /** Chi dung cho kiem thu - xoa danh sach bi mat da dang ky. */
    public static void xoaHet() {
        BI_MAT.clear();
    }

    /** Thay moi bi mat da dang ky bang ***. */
    public static String che(final String dong) {
        String ra = dong;
        for (int i = 0; i < BI_MAT.size(); i++) {
            ra = ra.replace(BI_MAT.get(i), CHE);
        }
        return ra;
    }

    private static PrintStream boc(final PrintStream cu) {
        try {
            return new PrintStream(new LocBiMat(cu), true, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return new PrintStream(new LocBiMat(cu), true);
        }
    }

    @Override
    public synchronized void write(final int b) throws IOException {
        this.dem.write(b);
        if (b == '\n' || this.dem.size() >= TOI_DA) {
            xa();
        }
    }

    @Override
    public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
        int batDau = off;
        final int het = off + len;
        for (int i = off; i < het; i++) {
            if (b[i] == '\n') {
                this.dem.write(b, batDau, i - batDau + 1);
                xa();
                batDau = i + 1;
            }
        }
        if (batDau < het) {
            this.dem.write(b, batDau, het - batDau);
            if (this.dem.size() >= TOI_DA) {
                xa();
            }
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        xa();
        this.goc.flush();
    }

    private void xa() throws IOException {
        if (this.dem.size() == 0) {
            return;
        }
        final String dong = new String(this.dem.toByteArray(), "UTF-8");
        this.dem.reset();
        this.goc.write(che(dong).getBytes("UTF-8"));
    }
}
