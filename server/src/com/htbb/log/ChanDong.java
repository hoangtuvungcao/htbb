package com.htbb.log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Chan System.out / System.err CU (698 cho println + 782 cho printStackTrace trong src/, them
 * ca lib/htbb-core.jar khong co ma nguon) va dua TUNG DONG vao {@link Nhat} de phan loai, gioi
 * han, gop. Nho vay khong phai sua tay tung cho goi ma van khong mat dong loi nao.
 *
 * Gom STACK TRACE: printStackTrace() in dong dau ("java.lang.X: ...") roi tung dong "\tat ...".
 * Dong dau trong giong ngoai le thi giu lai cho toi khi het cac dong tiep noi (hoac qua 200ms),
 * roi giao CA KHOI cho Nhat - Nhat chi in day du lan dau moi loai loi trong mot chu ky.
 */
final class ChanDong extends OutputStream {

    private static final int TOI_DA = 65536;
    /** Dong dau cua mot ngoai le: "java.lang.NullPointerException", "x.y.LoiZ: ...", "Exception in thread". */
    static final Pattern DAU_NGOAI_LE = Pattern.compile(
            "^(Exception in thread .*|[\\w$]+(\\.[\\w$]+)+(Exception|Error|Throwable)(: .*)?)$");

    private final boolean err;
    private final ByteArrayOutputStream dem = new ByteArrayOutputStream(256);

    private StringBuilder khoi;
    private String dauKhoi;
    private String khungDau;
    private long mocKhoi;

    ChanDong(final boolean err) {
        this.err = err;
    }

    static boolean laDongTiepNoi(final String s) {
        return s.startsWith("\tat ") || s.startsWith("\t... ") || s.startsWith("Caused by: ")
                || s.startsWith("\tSuppressed: ") || s.startsWith("\t\t") || s.startsWith("    at ");
    }

    @Override
    public synchronized void write(final int b) {
        if (b == '\n') {
            xaDong();
            return;
        }
        this.dem.write(b);
        if (this.dem.size() >= TOI_DA) {
            xaDong();
        }
    }

    @Override
    public synchronized void write(final byte[] b, final int off, final int len) {
        int batDau = off;
        final int het = off + len;
        for (int i = off; i < het; i++) {
            if (b[i] == '\n') {
                this.dem.write(b, batDau, i - batDau);
                xaDong();
                batDau = i + 1;
            }
        }
        if (batDau < het) {
            this.dem.write(b, batDau, het - batDau);
            if (this.dem.size() >= TOI_DA) {
                xaDong();
            }
        }
    }

    /** flush() cua PrintStream (autoflush) KHONG xa nua dong: print(".") roi println(...) van la mot dong. */
    @Override
    public void flush() {
    }

    private void xaDong() {
        String s = new String(this.dem.toByteArray(), StandardCharsets.UTF_8);
        this.dem.reset();
        if (s.endsWith("\r")) {
            s = s.substring(0, s.length() - 1);
        }
        nhanDong(s);
    }

    /** Tach rieng de kiem thu goi thang. */
    synchronized void nhanDong(final String s) {
        if (this.khoi != null && laDongTiepNoi(s)) {
            this.khoi.append('\n').append(s);
            if (this.khungDau == null && (s.startsWith("\tat ") || s.startsWith("    at "))) {
                this.khungDau = s.trim();
            }
            return;
        }
        xaKhoi();
        if (DAU_NGOAI_LE.matcher(s).matches()) {
            this.khoi = new StringBuilder(s);
            this.dauKhoi = s;
            this.khungDau = null;
            this.mocKhoi = System.currentTimeMillis();
            return;
        }
        Nhat.tuDongCu(s, this.err);
    }

    /** Luong ghi log goi moi chu ky: khoi stack treo qua 200ms ma khong co dong moi thi xa. */
    synchronized void xaNeuCu(final long bayGio) {
        if (this.khoi != null && bayGio - this.mocKhoi >= 200L) {
            xaKhoi();
        }
    }

    private void xaKhoi() {
        if (this.khoi == null) {
            return;
        }
        final String k = this.khoi.toString();
        final String dau = this.dauKhoi;
        final String khung = this.khungDau;
        this.khoi = null;
        this.dauKhoi = null;
        this.khungDau = null;
        Nhat.tuStackCu(dau, khung, k);
    }
}
