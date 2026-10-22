package com.htbb.pokemon;

/**
 * CHO POKEMON (ky gui Pokemon giua nguoi choi, docs/thiet-ke-game.md 7.9). Lop THUAN (test/KiemChoPokemon): CSDL qua
 * {@link Kho}, thoi gian truyen vao.
 *
 * <h3>Khong lam phat, khong rua xu, loai hiem giu gia</h3>
 * <ul>
 * <li><b>Khung gia</b> do MAY CHU tinh (ngoai khung = tu choi): san / tran theo bac hiem tinh bang phan nghin gio thu nhap
 *     O CAP CUA CON POKEMON (khong theo cap nguoi ban: nick cap cao khong day gia len duoc), x sac mau, x 3 sao;</li>
 * <li><b>Thue</b> khi ban (dot) &gt;= thue cho ky gui; <b>phi dang</b> (dot, khong hoan);</li>
 * <li>Con dang ban nam trong <b>ky quy</b> (dong tin, khong con trong Hop PC): huy / het han tra lai; mua = CAS
 *     trang thai tin 0 -&gt; 1 trong cung giao dich ghi Hop PC nguoi mua (hai nguoi mua cung luc: dung mot nguoi duoc).</li>
 * </ul>
 */
public final class ChoPokemon {

    public static final int DANG_BAN = 0, DA_BAN = 1, DA_TRA_TIEN = 2, DA_THU_HOI = 3;

    private ChoPokemon() {
    }

    /** Luat (cau hinh cho_pokemon.* + bang data_cho_pokemon_gia). */
    public static final class Luat {
        public int thuePhanTram = 8;
        public int phiDangPhanTram = 1;
        public int capToiThieu = 20;
        public int toiDaDang = 3;
        public int muaNgay = 5;
        public long khoaGiay = 72L * 3600L;
        public long hanGiay = 48L * 3600L;
        public int heSoSacMau = 3;
        public int heSo3Sao = 2;
        /** San / tran theo bac hiem 0..3 (phan nghin gio thu nhap). */
        public long[] san = {100, 300, 1000, 3000};
        public long[] tran = {1000, 3000, 10000, 30000};

        public static Luat macDinh() {
            return new Luat();
        }
    }

    // ------------------------------------------------------------------ gia

    /** {san, tran} (xu) cho mot con: bac hiem, sac mau, sao; {@code xuMoiGio} = gio thu nhap O CAP CUA CON. */
    public static long[] khung(final Luat l, final int doHiem, final boolean shiny, final int sao, final long xuMoiGio) {
        final int h = Math.max(0, Math.min(l.san.length - 1, doHiem));
        long he = 1L;
        if (shiny) {
            he *= Math.max(1, l.heSoSacMau);
        }
        if (sao >= 3) {
            he *= Math.max(1, l.heSo3Sao);
        }
        final long g = Math.max(1L, xuMoiGio);
        final long a = Math.max(1L, g * l.san[h] / 1000L * he);
        final long b = Math.max(a, g * l.tran[h] / 1000L * he);
        return new long[]{a, Math.min(Integer.MAX_VALUE, b)};
    }

    public static long thue(final Luat l, final long gia) {
        return Math.max(0L, gia) * Math.max(0, l.thuePhanTram) / 100L;
    }

    public static long nguoiBanNhan(final Luat l, final long gia) {
        return Math.max(0L, gia - thue(l, gia));
    }

    public static long phiDang(final Luat l, final long gia) {
        return gia <= 0 ? 0L : Math.max(1L, gia * Math.max(0, l.phiDangPhanTram) / 100L);
    }

    // ------------------------------------------------------------------ tin + kho

    /** Mot tin ban. */
    public static final class Tin {
        public long id;
        public int idBan;
        public String tenBan = "";
        public int userBan;
        public String mayBan = "";
        public String ipBan = "";
        public HopPc.Con con;
        public int doHiem;
        public long gia;
        public long phiDang;
        public long dangLuc;
        public long hetHan;
        public int trangThai;
        public int idMua;
        public String tenMua = "";
        public long thue;
    }

    /** Mot ben (nguoi mua / nguoi ban) de kiem trung tai khoan / thiet bi / IP. */
    public static final class Ben {
        public final int charId;
        public final String ten;
        public final int userId;
        public final String may;
        public final String ip;
        public final int cap;

        public Ben(final int charId, final String ten, final int userId, final String may, final String ip, final int cap) {
            this.charId = charId;
            this.ten = ten == null ? "" : ten;
            this.userId = userId;
            this.may = may == null ? "" : may;
            this.ip = ip == null ? "" : ip;
            this.cap = cap;
        }
    }

    /** CSDL. Moi ham la MOT giao dich (loi / false = khong doi gi). */
    public interface Kho {
        /** Them tin + ghi Hop PC nguoi ban (da bo con). Tra id tin (&gt; 0) hoac &lt;= 0 neu hong. */
        long dang(Tin t, String hopBanMoi) throws Exception;

        /** CAS tin DANG_BAN -&gt; DA_BAN (con han tai {@code bayGiay}) + ghi Hop PC nguoi mua + nhat ky. false = da co nguoi mua / het han. */
        boolean mua(Tin t, long bayGiay, int idMua, String tenMua, String hopMuaMoi, long thue) throws Exception;

        /** CAS tin DANG_BAN -&gt; DA_THU_HOI + ghi Hop PC nguoi ban (da tra con). */
        boolean thuHoi(Tin t, String hopBanMoi) throws Exception;
    }

    /** Ket qua. */
    public static final class KetQua {
        public final boolean ok;
        public final String loi;
        public final long id;
        public final HopPc.Con con;

        KetQua(final boolean ok, final String loi, final long id, final HopPc.Con con) {
            this.ok = ok;
            this.loi = loi == null ? "" : loi;
            this.id = id;
            this.con = con;
        }

        static KetQua hong(final String loi) {
            return new KetQua(false, loi, 0L, null);
        }
    }

    // ------------------------------------------------------------------ dang ban

    /**
     * Ly do khong dang duoc (null = duoc). {@code loai} null = huyen thoai / boss (khong co dong data_bat_loai) =&gt; khong
     * bao gio ban duoc; chi su kien cung vay.
     */
    public static String lyDoDang(final Luat l, final HopPc h, final HopPc.Con c, final BatPokemon.Loai loai, final int capNguoiBan,
            final int soDangHienTai, final long gia, final long[] khung, final long bayGiay) {
        if (c == null) {
            return "Pokémon đó không còn trong Hộp PC.";
        }
        if (capNguoiBan < l.capToiThieu) {
            return "Cần đạt cấp " + l.capToiThieu + " mới bán Pokémon ở chợ.";
        }
        if (loai == null || loai.chiSuKien) {
            return "Pokémon huyền thoại / sự kiện không bán được ở chợ.";
        }
        if (h.dangTheo == c.id) {
            return "Pokémon này đang đi theo bạn: chọn Thôi đi theo trước khi bán.";
        }
        if (c.doiLuc > 0 && bayGiay - c.doiLuc < l.khoaGiay) {
            final long con = (l.khoaGiay - (bayGiay - c.doiLuc) + 3599L) / 3600L;
            return "Pokémon vừa nhận qua trao đổi / chợ, " + Math.max(1L, con) + " giờ nữa mới bán được.";
        }
        if (soDangHienTai >= l.toiDaDang) {
            return "Mỗi người bán tối đa " + l.toiDaDang + " Pokémon cùng lúc.";
        }
        if (gia < khung[0] || gia > khung[1]) {
            return "Giá phải trong khoảng " + TienTeChu.so(khung[0]) + " - " + TienTeChu.so(khung[1]) + " xu.";
        }
        return null;
    }

    /** Dang ban: bo con khoi hop (ky quy) + them tin trong mot giao dich. */
    public static KetQua dang(final Luat l, final Ben ban, final HopPc h, final int idCon, final long gia, final BatPokemon.Loai loai,
            final long xuMoiGioCapCon, final int soDangHienTai, final long bayGiay, final Kho kho) {
        synchronized (h) {
            final HopPc.Con c = h.lay(idCon);
            final int sao = c != null ? CaThePokemon.sao(c.iv) : 0;
            final long[] k = khung(l, loai != null ? loai.doHiem : 0, c != null && c.shiny, sao, xuMoiGioCapCon);
            final String m = lyDoDang(l, h, c, loai, ban.cap, soDangHienTai, gia, k, bayGiay);
            if (m != null) {
                return KetQua.hong(m);
            }
            final HopPc sao2 = h.banSao();
            final HopPc.Con ra = sao2.tha(idCon);
            final Tin t = new Tin();
            t.idBan = ban.charId;
            t.tenBan = ban.ten;
            t.userBan = ban.userId;
            t.mayBan = ban.may;
            t.ipBan = ban.ip;
            t.con = ra;
            t.doHiem = loai.doHiem;
            t.gia = gia;
            t.phiDang = phiDang(l, gia);
            t.dangLuc = bayGiay;
            t.hetHan = bayGiay + l.hanGiay;
            t.trangThai = DANG_BAN;
            long id;
            try {
                id = kho == null ? 0L : kho.dang(t, sao2.maHoa());
            } catch (final Exception e) {
                id = 0L;
            }
            if (id <= 0L) {
                return KetQua.hong("Máy chủ chưa ghi được tin bán, Pokémon vẫn trong Hộp PC.");
            }
            t.id = id;
            h.thayBang(sao2);
            h.ban = false;
            return new KetQua(true, "", id, ra);
        }
    }

    // ------------------------------------------------------------------ mua

    /** Ly do nguoi mua khong mua duoc tin (null = duoc). Chua xet xu (noi goi tru truoc). */
    public static String lyDoMua(final Luat l, final Tin t, final Ben mua, final HopPc hMua, final long bayGiay, final int homNay) {
        if (t == null || t.trangThai != DANG_BAN || t.con == null) {
            return "Tin bán này không còn.";
        }
        if (bayGiay >= t.hetHan) {
            return "Tin bán này đã hết hạn.";
        }
        if (mua.charId == t.idBan) {
            return "Không thể mua Pokémon của chính mình.";
        }
        if (mua.userId > 0 && mua.userId == t.userBan) {
            return "Không mua được từ người chơi cùng tài khoản.";
        }
        if (mua.may.length() > 0 && mua.may.equals(t.mayBan)) {
            return "Không mua được từ người bán cùng thiết bị.";
        }
        if (mua.ip.length() > 0 && mua.ip.equals(t.ipBan)) {
            return "Không mua được từ người bán cùng mạng (IP).";
        }
        if (mua.cap < l.capToiThieu) {
            return "Cần đạt cấp " + l.capToiThieu + " mới mua Pokémon ở chợ.";
        }
        synchronized (hMua) {
            final int daMua = hMua.ngay == homNay ? hMua.muaNgay : 0;
            if (daMua >= l.muaNgay) {
                return "Hôm nay bạn đã mua đủ " + l.muaNgay + " Pokémon ở chợ.";
            }
            if (hMua.day()) {
                return "Hộp PC đã đầy (" + hMua.sucChua() + " ô).";
            }
        }
        return null;
    }

    /** Mua: CAS tin + ghi Hop PC nguoi mua + nhat ky trong mot giao dich. Con mua danh dau doiLuc (khoa ban lai, chuyen doi chi ra Keo). */
    public static KetQua mua(final Luat l, final Tin t, final Ben mua, final HopPc hMua, final long bayGiay, final int homNay, final Kho kho) {
        synchronized (hMua) {
            final String m = lyDoMua(l, t, mua, hMua, bayGiay, homNay);
            if (m != null) {
                return KetQua.hong(m);
            }
            final HopPc sao = hMua.banSao();
            sao.doiNgay(homNay);
            final HopPc.Con moi = sao.nhan(t.con);
            if (moi == null) {
                return KetQua.hong("Hộp PC đã đầy.");
            }
            moi.doiLuc = bayGiay;
            sao.muaNgay++;
            final long th = thue(l, t.gia);
            boolean ok;
            try {
                ok = kho != null && kho.mua(t, bayGiay, mua.charId, mua.ten, sao.maHoa(), th);
            } catch (final Exception e) {
                ok = false;
            }
            if (!ok) {
                return KetQua.hong("Người khác vừa mua mất (hoặc tin đã hết hạn).");
            }
            hMua.thayBang(sao);
            hMua.ban = false;
            return new KetQua(true, "", t.id, moi);
        }
    }

    // ------------------------------------------------------------------ huy / het han

    /** Huy (nguoi ban) hoac het han: tra con ve hop nguoi ban (vuot suc chua van tra). */
    public static KetQua thuHoi(final Tin t, final int chuId, final HopPc h, final Kho kho) {
        if (t == null || t.idBan != chuId || t.trangThai != DANG_BAN || t.con == null) {
            return KetQua.hong("Tin bán này không còn.");
        }
        synchronized (h) {
            final HopPc sao = h.banSao();
            final HopPc.Con ve = sao.traVe(t.con);
            boolean ok;
            try {
                ok = kho != null && kho.thuHoi(t, sao.maHoa());
            } catch (final Exception e) {
                ok = false;
            }
            if (!ok) {
                return KetQua.hong("Tin vừa được bán hoặc đã thu hồi.");
            }
            h.thayBang(sao);
            h.ban = false;
            return new KetQua(true, "", t.id, ve);
        }
    }

    // ------------------------------------------------------------------ nghi van

    /**
     * Nghi rua xu (ghi nhat ky, khong chan): cung cap mua / ban &gt;= 3 lan trong 7 ngay, hoac nguoi ban dat gia &gt;= 90%
     * tran &gt;= 3 lan trong 7 ngay. "" = khong nghi.
     */
    public static String nghiVan(final int soLanCungCap, final int soLanSatTran, final long gia, final long tran) {
        final StringBuilder sb = new StringBuilder();
        if (soLanCungCap >= 3) {
            sb.append("cung cap mua-ban ").append(soLanCungCap).append(" lan/7 ngay");
        }
        final boolean satTran = tran > 0 && gia * 10L >= tran * 9L;
        if (satTran && soLanSatTran + 1 >= 3) {
            sb.append(sb.length() > 0 ? "; " : "").append("gia sat tran ").append(soLanSatTran + 1).append(" lan/7 ngay");
        }
        return sb.toString();
    }

    public static boolean satTran(final long gia, final long tran) {
        return tran > 0 && gia * 10L >= tran * 9L;
    }
}
