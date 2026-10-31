package com.htbb.dangnhap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Giao thuc game server <-> login server (TCP, mac dinh cong 8023). Dac ta day du: docs/dang-nhap.md.
 *
 * <pre>
 * Khung:   u8 lenh | u16 do dai (big-endian, khong dau) | do dai byte du lieu      (KHONG ma hoa, KHONG nen)
 * Lenh 1 game -> login:  i64 sessionId | UTF ten | UTF mat khau | i8 ma game (HTBB luon gui 9)
 * Lenh 1 login -> game:  i64 sessionId | i32 ket qua | bool vinaphone | i8 fromgame
 * </pre>
 *
 * UTF = DataOutputStream.writeUTF (u16 do dai + modified UTF-8). Lenh khac 1: login server bo qua, khong tra loi
 * (giong ban cu). Tra loi co the KHONG theo thu tu gui: game ghep theo sessionId (io.LogInController).
 */
public final class GiaoThuc {

    /** Lenh duy nhat cua giao thuc: kiem tai khoan. */
    public static final byte LENH_KIEM = 1;
    /** Ma game HTBB gui (ke thua; login server cu in nhan theo ma nay). */
    public static final byte MA_GAME_HTBB = 9;

    private GiaoThuc() {
    }

    /** Mot khung da doc. */
    public static final class Khung {
        public final byte lenh;
        public final byte[] duLieu;

        public Khung(final byte lenh, final byte[] duLieu) {
            this.lenh = lenh;
            this.duLieu = duLieu;
        }
    }

    /** Khung qua lon so voi tran cau hinh - ket noi phai dong (khong the dong bo lai luong byte). */
    public static final class KhungQuaLon extends IOException {
        public final int doDai;

        KhungQuaLon(final int doDai, final int tran) {
            super("khung " + doDai + " byte > tran " + tran);
            this.doDai = doDai;
        }
    }

    /** Yeu cau kiem tai khoan (lenh 1 tu game). */
    public static final class YeuCau {
        public final long phien;
        public final String ten;
        public final String matKhau;
        public final byte maGame;

        public YeuCau(final long phien, final String ten, final String matKhau, final byte maGame) {
            this.phien = phien;
            this.ten = ten;
            this.matKhau = matKhau;
            this.maGame = maGame;
        }

        /** KHONG in mat khau. */
        @Override
        public String toString() {
            return "YeuCau{phien=" + phien + ", ten=" + ten + ", game=" + maGame + "}";
        }
    }

    /** Tra loi (lenh 1 tu login). */
    public static final class TraLoi {
        public final long phien;
        public final int ketQua;
        public final boolean vinaphone;
        public final byte fromgame;

        public TraLoi(final long phien, final int ketQua, final boolean vinaphone, final byte fromgame) {
            this.phien = phien;
            this.ketQua = ketQua;
            this.vinaphone = vinaphone;
            this.fromgame = fromgame;
        }
    }

    /**
     * Doc mot khung. Tra null neu luong ket thuc SACH ngay truoc byte dau khung; EOF giua khung -> EOFException.
     *
     * @param tran do dai du lieu toi da chap nhan (lon hon -> {@link KhungQuaLon})
     */
    public static Khung docKhung(final InputStream vao, final int tran) throws IOException {
        final int b0 = vao.read();
        if (b0 < 0) {
            return null;
        }
        final int h = vao.read();
        final int l = vao.read();
        if (h < 0 || l < 0) {
            throw new EOFException("EOF giua dau khung");
        }
        final int doDai = (h << 8) | l;
        if (doDai > tran) {
            throw new KhungQuaLon(doDai, tran);
        }
        final byte[] d = new byte[doDai];
        int da = 0;
        while (da < doDai) {
            final int n = vao.read(d, da, doDai - da);
            if (n < 0) {
                throw new EOFException("EOF giua khung (" + da + "/" + doDai + ")");
            }
            da += n;
        }
        return new Khung((byte) b0, d);
    }

    /** Dong goi mot khung. */
    public static byte[] dongKhung(final byte lenh, final byte[] duLieu) {
        if (duLieu.length > 0xFFFF) {
            throw new IllegalArgumentException("du lieu qua dai: " + duLieu.length);
        }
        final byte[] r = new byte[3 + duLieu.length];
        r[0] = lenh;
        r[1] = (byte) (duLieu.length >>> 8);
        r[2] = (byte) duLieu.length;
        System.arraycopy(duLieu, 0, r, 3, duLieu.length);
        return r;
    }

    /** Doc du lieu lenh 1 (game -> login). Thieu truong -> IOException (EOFException). */
    public static YeuCau docYeuCau(final byte[] duLieu) throws IOException {
        final DataInputStream d = new DataInputStream(new ByteArrayInputStream(duLieu));
        final long phien = d.readLong();
        final String ten = d.readUTF();
        final String mk = d.readUTF();
        final byte game = d.readByte();
        return new YeuCau(phien, ten, mk, game);
    }

    /** sessionId o dau du lieu (de van tra loi duoc khi phan sau hong); null neu khong du 8 byte. */
    public static Long docPhien(final byte[] duLieu) {
        if (duLieu.length < 8) {
            return null;
        }
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v = (v << 8) | (duLieu[i] & 0xFFL);
        }
        return v;
    }

    /** Khung lenh 1 game -> login (dung cho bo kiem / cong cu do tai). */
    public static byte[] khungYeuCau(final long phien, final String ten, final String matKhau, final byte maGame) {
        try {
            final ByteArrayOutputStream b = new ByteArrayOutputStream(32 + ten.length() + matKhau.length());
            final DataOutputStream o = new DataOutputStream(b);
            o.writeLong(phien);
            o.writeUTF(ten);
            o.writeUTF(matKhau);
            o.writeByte(maGame);
            o.flush();
            return dongKhung(LENH_KIEM, b.toByteArray());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Khung lenh 1 login -> game: 3 + 14 byte. */
    public static byte[] khungTraLoi(final TraLoi t) {
        final byte[] d = new byte[14];
        long p = t.phien;
        for (int i = 7; i >= 0; i--) {
            d[i] = (byte) p;
            p >>>= 8;
        }
        d[8] = (byte) (t.ketQua >>> 24);
        d[9] = (byte) (t.ketQua >>> 16);
        d[10] = (byte) (t.ketQua >>> 8);
        d[11] = (byte) t.ketQua;
        d[12] = (byte) (t.vinaphone ? 1 : 0);
        d[13] = t.fromgame;
        return dongKhung(LENH_KIEM, d);
    }

    /** Doc du lieu tra loi (dung cho bo kiem / cong cu do tai) - dung thu tu doc cua io.LogInController. */
    public static TraLoi docTraLoi(final byte[] duLieu) throws IOException {
        final DataInputStream d = new DataInputStream(new ByteArrayInputStream(duLieu));
        return new TraLoi(d.readLong(), d.readInt(), d.readBoolean(), d.readByte());
    }
}
