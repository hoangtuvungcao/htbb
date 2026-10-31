package com.htbb.pokemon;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * San doi luong THUONG <-> xu giua nguoi choi ({@link DoiLuong} muc 3). Luong khoa KHONG BAO GIO len san: lenh ban chi ky
 * quy Char.luong (luong thuong, truLuongMoChuyen), lenh mua chi nhan luong thuong (addLuong).
 *
 * Phan THUAN ({@link #khop}) tinh cac lan khop tu so lenh da loc; phan CSDL ({@link #datLenh}, {@link #huyLenh},
 * {@link #hetHan}) ap dung trong MOT giao dich duoi khoa {@link #KHOA} (cac lenh doi dien con khoa dong FOR UPDATE).
 * Ky quy da tru khoi nhan vat TRUOC (va da luu) - moi khoan cong lai (xu ban duoc, luong mua duoc, hoan ky quy) di qua
 * hang doi tob_san_luong_tra. Khong sinh xu: xu nguoi mua tra = xu nguoi ban nhan + phi (dot).
 */
public final class SanLuong {

    /** Moi thao tac so lenh chay tuan tu trong tien trinh game. */
    public static final Object KHOA = new Object();

    private SanLuong() {
    }

    /** Mot lenh dang cho (doi dien). */
    public static final class Lenh {
        public final long id;
        public final int loai;
        public final int charId;
        public final int userId;
        public final String ten;
        public final long gia;
        public int conLai;
        public long xuKyQuy;

        public Lenh(final long id, final int loai, final int charId, final int userId, final String ten, final long gia,
                final int conLai, final long xuKyQuy) {
            this.id = id;
            this.loai = loai;
            this.charId = charId;
            this.userId = userId;
            this.ten = ten;
            this.gia = gia;
            this.conLai = conLai;
            this.xuKyQuy = xuKyQuy;
        }
    }

    /** Mot lan khop voi lenh dang cho {@code cho}. */
    public static final class Khop {
        public final Lenh cho;
        public final int so;
        public final long gia;
        /** Xu nguoi ban nhan. */
        public final long xuBan;
        /** Phi xu (dot). */
        public final long phi;
        /** Xu tru khoi ky quy nguoi mua = xuBan + phi. */
        public final long xuMua;

        Khop(final Lenh cho, final int so, final long gia, final long phiPhanTram) {
            this.cho = cho;
            this.so = so;
            this.gia = gia;
            final long[] t = DoiLuong.tienKhop(so, gia, phiPhanTram);
            this.xuBan = t[0];
            this.phi = t[1];
            this.xuMua = t[2];
        }
    }

    /** Chan mot doi tac (cong chong clone giua hai nguoi). */
    public interface ChanDoiTac {
        boolean chan(Lenh doiTac, long xu);
    }

    /**
     * THUAN: lenh moi ({@code moiLaBan}, gia, so luong) khop voi {@code cho} (DA sap theo uu tien: gia tot nhat roi id nho
     * nhat, DA bo lenh cung tai khoan). Gia khop = gia lenh dang cho. Dung o lenh dau tien khong khop gia.
     */
    public static List<Khop> khop(final boolean moiLaBan, final long gia, final int soLuong, final List<Lenh> cho,
            final long phiPhanTram, final ChanDoiTac chan) {
        final List<Khop> ra = new ArrayList<Khop>();
        int con = soLuong;
        for (final Lenh l : cho) {
            if (con <= 0) {
                break;
            }
            if (l.conLai <= 0) {
                continue;
            }
            if (!DoiLuong.khopDuoc(moiLaBan, gia, l.gia)) {
                break;
            }
            final int k = Math.min(con, l.conLai);
            if (chan != null && chan.chan(l, (long) k * l.gia)) {
                continue;
            }
            ra.add(new Khop(l, k, l.gia, phiPhanTram));
            con -= k;
        }
        return ra;
    }

    /** Ket qua dat lenh. */
    public static final class KetQua {
        public long idLenh;
        public int daKhop;
        public long xuNhan;
        public long luongNhan;
        public long phi;
        public int conCho;
        public final List<Integer> doiTac = new ArrayList<Integer>();
    }

    /**
     * Dat lenh (ky quy DA tru khoi nhan vat va da luu): ghi lenh, khop, ghi khoan tra cho moi ben - mot giao dich.
     * @param xuKyQuy lenh mua: xu da ky quy (DoiLuong.kyQuyMua); lenh ban: 0
     */
    public static KetQua datLenh(final int loai, final int charId, final int userId, final String ten, final long gia,
            final int soLuong, final long xuKyQuy, final long phiPhanTram, final LocalDate ngay, final ChanDoiTac chan)
            throws Exception {
        synchronized (KHOA) {
            return DoiLuongDb.giaoDich((Connection conn) -> {
                final boolean laBan = loai == DoiLuongDb.LOAI_BAN;
                final long bay = System.currentTimeMillis();
                final Timestamp luc = new Timestamp(bay);
                final KetQua kq = new KetQua();
                kq.idLenh = DoiLuongDb.chen(conn, "INSERT INTO tob_san_luong_lenh (loai, char_id, user_id, charname, gia, so_luong,"
                        + " con_lai, xu_ky_quy, trang_thai, ngay_tao, tao_luc, het_han) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)",
                        loai, charId, userId, ten, gia, soLuong, soLuong, xuKyQuy, Date.valueOf(ngay), luc,
                        new Timestamp(bay + DoiLuong.SAN_HAN_GIO * 3600000L));
                final List<Object[]> dong = DoiLuongDb.doc(conn, "SELECT id, loai, char_id, user_id, charname, gia, con_lai, xu_ky_quy"
                        + " FROM tob_san_luong_lenh WHERE trang_thai = 0 AND loai = ? AND user_id <> ? AND gia " + (laBan ? ">=" : "<=")
                        + " ? ORDER BY gia " + (laBan ? "DESC" : "ASC") + ", id ASC LIMIT 50 FOR UPDATE",
                        laBan ? DoiLuongDb.LOAI_MUA : DoiLuongDb.LOAI_BAN, userId, gia);
                final List<Lenh> cho = new ArrayList<Lenh>();
                for (final Object[] r : dong) {
                    cho.add(new Lenh(DoiLuongDb.so(r[0]), (int) DoiLuongDb.so(r[1]), (int) DoiLuongDb.so(r[2]),
                            (int) DoiLuongDb.so(r[3]), String.valueOf(r[4]), DoiLuongDb.so(r[5]), (int) DoiLuongDb.so(r[6]),
                            DoiLuongDb.so(r[7])));
                }
                int con = soLuong;
                long kyQuyCon = xuKyQuy;
                for (final Khop k : khop(laBan, gia, soLuong, cho, phiPhanTram, chan)) {
                    final Lenh l = k.cho;
                    l.conLai -= k.so;
                    con -= k.so;
                    final int charBan = laBan ? charId : l.charId;
                    final int charMua = laBan ? l.charId : charId;
                    long hoanMua = 0L;
                    if (laBan) {
                        l.xuKyQuy -= k.xuMua; // nguoi mua la lenh dang cho
                        if (l.conLai <= 0) {
                            hoanMua = Math.max(0L, l.xuKyQuy);
                            l.xuKyQuy = 0L;
                        }
                    } else {
                        kyQuyCon -= k.xuMua;
                    }
                    DoiLuongDb.capNhat(conn, "UPDATE tob_san_luong_lenh SET con_lai = ?, xu_ky_quy = ?, trang_thai = ?, xong_luc = ?"
                            + " WHERE id = ?", l.conLai, l.xuKyQuy, l.conLai <= 0 ? DoiLuongDb.XONG : DoiLuongDb.MO,
                            l.conLai <= 0 ? luc : null, l.id);
                    DoiLuongDb.capNhat(conn, "INSERT INTO tob_san_luong_khop (lenh_ban, lenh_mua, char_ban, char_mua, so_luong, gia,"
                            + " xu_ban_nhan, phi_xu, luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", laBan ? kq.idLenh : l.id,
                            laBan ? l.id : kq.idLenh, charBan, charMua, k.so, k.gia, k.xuBan, k.phi, luc);
                    DoiLuongDb.themTra(conn, charBan, k.xuBan, 0L, "san: ban " + k.so + " luong gia " + k.gia);
                    DoiLuongDb.themTra(conn, charMua, hoanMua, k.so, "san: mua " + k.so + " luong gia " + k.gia);
                    kq.daKhop += k.so;
                    kq.phi += k.phi;
                    if (laBan) {
                        kq.xuNhan += k.xuBan;
                    } else {
                        kq.luongNhan += k.so;
                    }
                    if (!kq.doiTac.contains(l.charId)) {
                        kq.doiTac.add(l.charId);
                    }
                }
                long hoan = 0L;
                if (!laBan && con <= 0) {
                    hoan = Math.max(0L, kyQuyCon);
                    kyQuyCon = 0L;
                }
                DoiLuongDb.capNhat(conn, "UPDATE tob_san_luong_lenh SET con_lai = ?, xu_ky_quy = ?, trang_thai = ?, xong_luc = ?"
                        + " WHERE id = ?", con, kyQuyCon, con <= 0 ? DoiLuongDb.XONG : DoiLuongDb.MO, con <= 0 ? luc : null,
                        kq.idLenh);
                if (hoan > 0L) {
                    DoiLuongDb.themTra(conn, charId, hoan, 0L, "san: hoan ky quy du lenh mua #" + kq.idLenh);
                }
                kq.conCho = con;
                return kq;
            });
        }
    }

    /**
     * Dong mot lenh dang mo (huy / het han) va ghi khoan hoan: lenh ban hoan con_lai luong, lenh mua hoan xu_ky_quy.
     * @return {loai, con_lai, xu hoan, ngay_tao epochDay, user_id} hoac null neu lenh khong con mo / khong phai cua charId (charId <= 0 = bo qua)
     */
    static long[] dong(final Connection conn, final long id, final int charId, final int trangThai) throws Exception {
        final List<Object[]> r = DoiLuongDb.doc(conn, "SELECT loai, char_id, con_lai, xu_ky_quy, ngay_tao, user_id FROM tob_san_luong_lenh"
                + " WHERE id = ? AND trang_thai = 0 FOR UPDATE", id);
        if (r.isEmpty()) {
            return null;
        }
        final Object[] d = r.get(0);
        final int chu = (int) DoiLuongDb.so(d[1]);
        if (charId > 0 && chu != charId) {
            return null;
        }
        final int loai = (int) DoiLuongDb.so(d[0]);
        final long conLai = DoiLuongDb.so(d[2]);
        final long xu = DoiLuongDb.so(d[3]);
        DoiLuongDb.capNhat(conn, "UPDATE tob_san_luong_lenh SET trang_thai = ?, xu_ky_quy = 0, xong_luc = ? WHERE id = ?",
                trangThai, new Timestamp(System.currentTimeMillis()), id);
        final String lyDo = (trangThai == DoiLuongDb.HUY ? "san: huy lenh #" : "san: het han lenh #") + id;
        if (loai == DoiLuongDb.LOAI_BAN) {
            DoiLuongDb.themTra(conn, chu, 0L, conLai, lyDo);
        } else {
            DoiLuongDb.themTra(conn, chu, xu, 0L, lyDo);
        }
        final LocalDate ngay = ((java.sql.Date) d[4]).toLocalDate();
        return new long[]{loai, conLai, loai == DoiLuongDb.LOAI_BAN ? 0L : xu, ngay.toEpochDay(), DoiLuongDb.so(d[5]), chu};
    }

    /** Nguoi choi huy lenh cua minh. null = khong con mo. */
    public static long[] huyLenh(final long id, final int charId) throws Exception {
        synchronized (KHOA) {
            return DoiLuongDb.giaoDich(conn -> dong(conn, id, charId, DoiLuongDb.HUY));
        }
    }

    /** Dong cac lenh qua han (toi da 100 / lan). @return so lenh da dong; moi phan tu {.., char_id} cho nguoi goi bao. */
    public static List<long[]> hetHan() throws Exception {
        synchronized (KHOA) {
            return DoiLuongDb.giaoDich(conn -> {
                final List<long[]> ra = new ArrayList<long[]>();
                for (final Object[] r : DoiLuongDb.doc(conn, "SELECT id FROM tob_san_luong_lenh WHERE trang_thai = 0 AND het_han < ?"
                        + " ORDER BY id LIMIT 100", new Timestamp(System.currentTimeMillis()))) {
                    final long[] d = dong(conn, DoiLuongDb.so(r[0]), 0, DoiLuongDb.HET_HAN);
                    if (d != null) {
                        ra.add(d);
                    }
                }
                return ra;
            });
        }
    }
}
