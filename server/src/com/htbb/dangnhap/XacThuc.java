package com.htbb.dangnhap;

import java.sql.SQLException;

/**
 * Logic kiem tai khoan - GIU NGUYEN ngu nghia login server cu (CheckLoginSocket.jar), xem docs/dang-nhap.md muc 3.
 * Khong cham CSDL truc tiep: moi truy van qua {@link Nguon} (CSDL that: {@link NguonCsdl}; bo kiem: nguon gia).
 *
 * <pre>
 *   ten / mat khau co dau '            -> -3 (khong truy van)
 *   ma game 2, 6, 15 (game cu khac)     -> -3 (ban cu tra CSDL hs2/nr2/am khong ton tai -> loi -> -3)
 *   mat khau bat dau '#' (token)        -> usertoken: co dong -> id (qua 3600 s tu dateCreate -> -1); khong dong -> -3
 *   con lai: team_user WHERE username=? AND password=PASSWORD(?)
 *       khong co dong -> -1;  ban=1 -> -2;  ban=3 -> -5;  ban khac -> id
 *       vinaphone = provider==0 va phone bat dau 8491/8494/84123/84124/84125/84127/84129;  fromgame = cot fromgame
 *   loi CSDL / het cho ket noi           -> -3
 * </pre>
 */
public final class XacThuc {

    public static final int SAI = -1;
    public static final int BI_KHOA = -2;
    public static final int LOI = -3;
    public static final int CHUA_KICH_HOAT = -5;

    /** Token cu hon so giay nay thi coi nhu sai (ban cu: 3600). */
    public static final long TOKEN_HAN_GIAY = 3600L;

    /** Mot dong team_user can cho tra loi. */
    public static final class TaiKhoan {
        public final int id;
        public final int ban;
        public final int provider;
        public final String phone;
        public final byte fromgame;

        public TaiKhoan(final int id, final int ban, final int provider, final String phone, final byte fromgame) {
            this.id = id;
            this.ban = ban;
            this.provider = provider;
            this.phone = phone;
            this.fromgame = fromgame;
        }
    }

    /** Mot dong usertoken. */
    public static final class Token {
        public final int id;
        public final long taoLucMs;

        public Token(final int id, final long taoLucMs) {
            this.id = id;
            this.taoLucMs = taoLucMs;
        }
    }

    /** Nguon du lieu tai khoan. Nem SQLException khi CSDL loi / het cho -> ket qua -3. */
    public interface Nguon {
        /**
         * Dong team_user khop ten va PASSWORD(matKhau); null neu khong co.
         *
         * @param hanChot moc System.currentTimeMillis() phai xong truoc (qua -> SQLException); Long.MAX_VALUE = khong han
         */
        TaiKhoan timTaiKhoan(String ten, String matKhau, long hanChot) throws SQLException;

        /** Dong usertoken khop ten va token; null neu khong co. */
        Token timToken(String ten, String token, long hanChot) throws SQLException;
    }

    /** Ket qua kiem + ly do loi (de ghi log, khong gui di). */
    public static final class KetQua {
        public final int ma;
        public final boolean vinaphone;
        public final byte fromgame;
        /** null khi khong loi; mo ta ngan khi ma = -3 vi loi. */
        public final String loi;

        KetQua(final int ma, final boolean vinaphone, final byte fromgame, final String loi) {
            this.ma = ma;
            this.vinaphone = vinaphone;
            this.fromgame = fromgame;
            this.loi = loi;
        }
    }

    private final Nguon nguon;
    private final DongHo dongHo;

    /** Dong ho (ms) - bo kiem thay duoc. */
    public interface DongHo {
        long bayGio();
    }

    public XacThuc(final Nguon nguon) {
        this(nguon, new DongHo() {
            @Override
            public long bayGio() {
                return System.currentTimeMillis();
            }
        });
    }

    public XacThuc(final Nguon nguon, final DongHo dongHo) {
        this.nguon = nguon;
        this.dongHo = dongHo;
    }

    public KetQua kiem(final String ten, final String matKhau, final byte maGame) {
        return kiem(ten, matKhau, maGame, Long.MAX_VALUE);
    }

    /** @param hanChot moc System.currentTimeMillis() cho truy van CSDL (xem {@link Nguon}). */
    public KetQua kiem(final String ten, final String matKhau, final byte maGame, final long hanChot) {
        if (ten == null || matKhau == null || ten.indexOf('\'') >= 0 || matKhau.indexOf('\'') >= 0) {
            return new KetQua(LOI, false, (byte) 0, null);
        }
        if (maGame == 2 || maGame == 6 || maGame == 15) {
            return new KetQua(LOI, false, (byte) 0, "ma game " + maGame + " khong ho tro");
        }
        try {
            if (matKhau.startsWith("#")) {
                final Token t = this.nguon.timToken(ten, matKhau, hanChot);
                if (t == null) {
                    return new KetQua(LOI, false, (byte) 0, null); // ban cu: khong co token -> giu -3
                }
                final long tuoiGiay = (this.dongHo.bayGio() - t.taoLucMs) / 1000L;
                return new KetQua(tuoiGiay > TOKEN_HAN_GIAY ? SAI : t.id, false, (byte) 0, null);
            }
            final TaiKhoan tk = this.nguon.timTaiKhoan(ten, matKhau, hanChot);
            if (tk == null) {
                return new KetQua(SAI, false, (byte) 0, null);
            }
            final int ma = tk.ban == 1 ? BI_KHOA : (tk.ban == 3 ? CHUA_KICH_HOAT : tk.id);
            return new KetQua(ma, tk.provider == 0 && laVinaphone(tk.phone), tk.fromgame, null);
        } catch (final SQLException e) {
            return new KetQua(LOI, false, (byte) 0, "CSDL: " + e.getClass().getSimpleName() + ": " + dongDau(e.getMessage()));
        } catch (final RuntimeException e) {
            return new KetQua(LOI, false, (byte) 0, "loi: " + e);
        }
    }

    /** Dong dau cua thong diep loi (Connector/J viet nhieu dong). */
    static String dongDau(final String s) {
        if (s == null) {
            return "";
        }
        final int i = s.indexOf('\n');
        return (i < 0 ? s : s.substring(0, i)).trim();
    }

    /** Dau so Vinaphone cu (giu cho tuong thich truong bool trong tra loi; game HTBB khong dung). */
    public static boolean laVinaphone(final String so) {
        if (so == null || so.length() < 5) {
            return false;
        }
        return so.startsWith("8491") || so.startsWith("8494") || so.startsWith("84123") || so.startsWith("84124")
                || so.startsWith("84125") || so.startsWith("84127") || so.startsWith("84129");
    }
}
