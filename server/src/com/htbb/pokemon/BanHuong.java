package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * NGAM TAY / BAN THEO HUONG (0.9.8, chu game 22/10/2026: "PK, solo, moi tran giua nguoi choi: KHONG tu dong danh, KHONG
 * tu nham - keo chieu de ngam, chieu bay thang theo huong keo"). Phan THUAN (khong CSDL, khong luong), kiem duoc bang
 * test: thong so chieu, hinh hoc duong dan (duong thang / xuyen / non / vung), tuong chan, bu tre, chong spam.
 *
 * <p>Phan game: real.BanHuongGame (lenh {@link #LENH}, ngu canh PvP, gay sat thuong qua duong danh cu).
 *
 * <p>Toa do: pixel the gioi (1 o = 16 px), y tang xuong duoi. Huong: vector bat ky khac 0 (client gui x1000), may chu
 * chuan hoa. Moi lan trung do MAY CHU tinh tu vi tri may chu biet (lich su vi tri + cua so bu tre), client chi gui
 * chieu + huong + diem xuat phat (diem xuat phat chi duoc dung khi lech vi tri may chu &lt;= {@link #LECH_GOC}).
 */
public final class BanHuong {

    private BanHuong() {
    }

    // ================================================================== giao thuc

    /** Lenh hai chieu (client -&gt; may chu: ban; may chu -&gt; client: dan bay / che do / bang chieu). */
    public static final int LENH = -113;
    /** Client tu ban nay moi ngam duoc (goi may chu -&gt; client chi gui cho client &gt;= ban nay). */
    public static final int BAN_CLIENT = 908;

    /** client -&gt; may chu: byte 0, byte chieu, short goc x, short goc y, short huong x, short huong y. */
    public static final int C_BAN = 0;
    /** client -&gt; may chu: byte 1, byte bat (Ngam thu cong o PvE - chi de may chu ghi nhan / thong ke). */
    public static final int C_NGAM_TAY = 1;

    /**
     * may chu -&gt; client: byte 0, short id nguoi ban, byte chieu, short goc x, short goc y, short huong x (x1000), short
     * huong y, short tam that (da cat o tuong), byte kieu, short be rong, short toc do (px/giay), byte goc non.
     */
    public static final int S_DAN = 0;
    /** may chu -&gt; client: byte 1, byte dang PvP (1 = bat buoc ngam tay, tat tu dong + tu nham), UTF ly do. */
    public static final int S_CHE_DO = 1;
    /**
     * may chu -&gt; client: byte 2, byte so chieu n, n x (byte chieu, byte kieu, short tam, short be rong, short toc do,
     * byte goc non, byte so muc tieu toi da).
     */
    public static final int S_BANG = 2;

    // ================================================================== kieu ban

    /** Dan bay thang: trung NGUOI DAU TIEN tren duong roi dung. */
    public static final int K_DAN = 0;
    /** Tia xuyen: trung moi nguoi tren duong (toi da {@link ThongSo#toiDa}). */
    public static final int K_XUYEN = 1;
    /** Hinh non tu nguoi ban (goc {@link ThongSo#goc} do). */
    public static final int K_NON = 2;
    /** Vung tron o cuoi tam (ban kinh = be rong / 2). */
    public static final int K_VUNG = 3;
    /** Buff / ban than: tuc thi, khong ngam (duong buff cu, lenh 51). */
    public static final int K_TU_THAN = 4;

    public static final String[] TEN_KIEU = {"dan", "xuyen", "non", "vung", "tu_than"};

    /** Ban kinh than nhan vat (px) - cong vao be rong khi xet trung. */
    public static final int BAN_KINH_THAN = 10;
    /** Diem xuat phat client gui lech vi tri may chu toi da ngan nay (px) thi moi dung; lech hon = dung vi tri may chu. */
    public static final int LECH_GOC = 32;
    /** Buoc mo phong dan bay (ms). */
    public static final long BUOC_MS = 20L;
    /** Buoc do tuong (px). */
    public static final int BUOC_TUONG = 4;
    /** Cua so bu tre mac dinh (ms): vi tri muc tieu trong [t - buTre, t] deu tinh (uu tien nguoi ban). */
    public static final long BU_TRE_MS = 150L;
    /** Tran cua so bu tre (ms) - cau hinh lon hon cung kep ve day. */
    public static final long BU_TRE_TOI_DA_MS = 300L;
    /** Thoi gian bay toi da (ms) - tam / toc do lon hon cung cat. */
    public static final long BAY_TOI_DA_MS = 1500L;

    /** Thong so ban cua MOT chieu (data_chieu_ban). */
    public static final class ThongSo {
        public final int kieu;
        /** Tam (px). */
        public final int tam;
        /** Be rong (px): duong dan / tia = be rong ca duong; vung = duong kinh. */
        public final int rong;
        /** Toc do (px / giay); 0 = tuc thi. */
        public final int tocDo;
        /** Goc mo cua non (do). */
        public final int goc;
        /** So muc tieu toi da (dan = 1). */
        public final int toiDa;

        public ThongSo(final int kieu, final int tam, final int rong, final int tocDo, final int goc, final int toiDa) {
            this.kieu = kieu < K_DAN || kieu > K_TU_THAN ? K_DAN : kieu;
            this.tam = kep(tam, 8, 640);
            this.rong = kep(rong, 2, 320);
            this.tocDo = kep(tocDo, 0, 4000);
            this.goc = kep(goc, 5, 360);
            this.toiDa = this.kieu == K_DAN ? 1 : kep(toiDa, 1, 20);
        }

        /** Thoi gian bay het tam (ms), 0 = tuc thi. */
        public long msBay(final int tamThat) {
            if (this.tocDo <= 0 || (this.kieu != K_DAN && this.kieu != K_XUYEN)) {
                return 0L;
            }
            return Math.min(BAY_TOI_DA_MS, (long) tamThat * 1000L / this.tocDo);
        }

        @Override
        public String toString() {
            return TEN_KIEU[this.kieu] + " tam=" + this.tam + " rong=" + this.rong + " toc=" + this.tocDo + " goc=" + this.goc
                    + " toiDa=" + this.toiDa;
        }
    }

    static int kep(final int v, final int lo, final int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    /** Chieu buff (ban than) theo lop - khop real.TuDongGame.SKILL_BUFF. */
    public static final int[][] CHIEU_BUFF = {{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};

    public static boolean laBuff(final int lop, final int chieu) {
        if (lop < 0 || lop >= CHIEU_BUFF.length) {
            return false;
        }
        for (final int b : CHIEU_BUFF[lop]) {
            if (b == chieu) {
                return true;
            }
        }
        return false;
    }

    /** Lop danh xa (tam goc lon): 2, 4. Con lai can chien. */
    public static boolean danhXa(final int lop) {
        return lop == 2 || lop == 4;
    }

    /**
     * Thong so mac dinh khi data_chieu_ban khong co dong. {@code tamGoc} = CharManager.SKILL_RANGE[lop][chieu] (duong danh
     * cu kiem tam theo hop vuong canh nay - tam ban KHONG vuot qua de don trung van qua duoc kiem tam cu).
     * <ul>
     * <li>chieu 0 (danh thuong): danh xa = dan rong 16, 420 px/s; can chien = non 100 do.</li>
     * <li>chieu 1: dan rong 20, 480 px/s.</li>
     * <li>chieu 2: tia xuyen tuc thi rong 24, toi da 3.</li>
     * <li>chieu 3: non 70 do, toi da 4.</li>
     * <li>chieu khac (6+): vung tron duong kinh 64 o cuoi tam, toi da 5.</li>
     * <li>buff: tu than.</li>
     * </ul>
     */
    public static ThongSo macDinh(final int lop, final int chieu, final int tamGoc) {
        final int tam = Math.max(24, tamGoc);
        if (laBuff(lop, chieu)) {
            return new ThongSo(K_TU_THAN, tam, 32, 0, 360, 1);
        }
        switch (chieu) {
            case 0:
                return danhXa(lop) ? new ThongSo(K_DAN, tam, 16, 420, 30, 1) : new ThongSo(K_NON, tam, 24, 0, 100, 2);
            case 1:
                return new ThongSo(K_DAN, tam, 20, 480, 30, 1);
            case 2:
                return new ThongSo(K_XUYEN, tam, 24, 0, 30, 3);
            case 3:
                return new ThongSo(K_NON, tam, 32, 0, 70, 4);
            default:
                return new ThongSo(K_VUNG, tam, 64, 0, 360, 5);
        }
    }

    // ================================================================== lich su vi tri (bu tre)

    /** Lich su vi tri cua mot nhan vat (vong, {@link #SO_MAU} mau). Khoa: chinh doi tuong. */
    public static final class LichSu {
        public static final int SO_MAU = 32;
        /** Hai goi di chuyen cach nhau hon ngan nay: coi nhu dung yen toi (t - ngan nay) roi moi di. */
        public static final long DUNG_YEN_MS = 250L;
        /** Nhay xa hon ngan nay giua hai mau = dich chuyen: xoa lich su, khong noi suy qua. */
        public static final int NHAY_PX = 96;

        final long[] t = new long[SO_MAU];
        final int[] x = new int[SO_MAU];
        final int[] y = new int[SO_MAU];
        int dau;
        int so;
        /** Huong di cuoi (dau dx, dy) - client cu: huong nhan vat dang quay. */
        public int huongX;
        public int huongY = 1;

        public synchronized void ghi(final long bay, final int nx, final int ny) {
            if (this.so > 0) {
                final int cuoi = (this.dau + this.so - 1) % SO_MAU;
                final int dx = nx - this.x[cuoi], dy = ny - this.y[cuoi];
                if (Math.abs(dx) > NHAY_PX || Math.abs(dy) > NHAY_PX) {
                    this.so = 0;
                } else {
                    if (dx != 0 || dy != 0) {
                        this.huongX = Integer.signum(dx);
                        this.huongY = Integer.signum(dy);
                        if (Math.abs(dx) >= Math.abs(dy)) {
                            this.huongY = 0;
                        } else {
                            this.huongX = 0;
                        }
                    }
                    if (bay - this.t[cuoi] > DUNG_YEN_MS && (dx != 0 || dy != 0)) {
                        them(bay - DUNG_YEN_MS, this.x[cuoi], this.y[cuoi]);
                    }
                }
            }
            them(bay, nx, ny);
        }

        private void them(final long bay, final int nx, final int ny) {
            if (this.so > 0) {
                final int cuoi = (this.dau + this.so - 1) % SO_MAU;
                if (bay < this.t[cuoi]) {
                    return; // khong lui thoi gian
                }
            }
            final int i;
            if (this.so < SO_MAU) {
                i = (this.dau + this.so) % SO_MAU;
                this.so++;
            } else {
                i = this.dau;
                this.dau = (this.dau + 1) % SO_MAU;
            }
            this.t[i] = bay;
            this.x[i] = nx;
            this.y[i] = ny;
        }

        public synchronized int soMau() {
            return this.so;
        }

        /**
         * Vi tri tai thoi diem {@code bay} (noi suy tuyen tinh giua hai mau). Truoc mau dau = mau dau; sau mau cuoi =
         * {@code (xNay, yNay)} (vi tri hien tai may chu giu, co the moi hon mau cuoi).
         */
        public synchronized int[] luc(final long bay, final int xNay, final int yNay) {
            if (this.so == 0) {
                return new int[]{xNay, yNay};
            }
            final int cuoi = (this.dau + this.so - 1) % SO_MAU;
            if (bay >= this.t[cuoi]) {
                return new int[]{xNay, yNay};
            }
            if (bay <= this.t[this.dau]) {
                return new int[]{this.x[this.dau], this.y[this.dau]};
            }
            for (int k = this.so - 1; k > 0; k--) {
                final int b = (this.dau + k) % SO_MAU, a = (this.dau + k - 1) % SO_MAU;
                if (bay >= this.t[a]) {
                    final long dt = this.t[b] - this.t[a];
                    if (dt <= 0L) {
                        return new int[]{this.x[b], this.y[b]};
                    }
                    final double f = (double) (bay - this.t[a]) / (double) dt;
                    return new int[]{(int) Math.round(this.x[a] + (this.x[b] - this.x[a]) * f),
                        (int) Math.round(this.y[a] + (this.y[b] - this.y[a]) * f)};
                }
            }
            return new int[]{this.x[this.dau], this.y[this.dau]};
        }

        /** Cac vi tri trong [tu, den]: hai dau + moi mau nam giua (de xet trung "uu tien nguoi ban"). */
        public synchronized List<int[]> trongKhoang(final long tu, final long den, final int xNay, final int yNay) {
            final List<int[]> ra = new ArrayList<int[]>();
            ra.add(luc(den, xNay, yNay));
            if (den > tu) {
                ra.add(luc(tu, xNay, yNay));
                for (int k = 0; k < this.so; k++) {
                    final int i = (this.dau + k) % SO_MAU;
                    if (this.t[i] > tu && this.t[i] < den) {
                        ra.add(new int[]{this.x[i], this.y[i]});
                    }
                }
            }
            return ra;
        }
    }

    // ================================================================== mo phong

    /** Ban do: o (pixel) chan dan khong. */
    public interface Tuong {
        boolean chan(int x, int y);
    }

    /** Muc tieu ung vien: id + loai (0 nguoi choi, 1 quai) + lich su vi tri (null = dung yen tai x, y). */
    public static final class MucTieu {
        public final int id;
        public final int loai;
        public final int x;
        public final int y;
        public final LichSu lichSu;

        public MucTieu(final int id, final int loai, final int x, final int y, final LichSu lichSu) {
            this.id = id;
            this.loai = loai;
            this.x = x;
            this.y = y;
            this.lichSu = lichSu;
        }

        List<int[]> viTri(final long tu, final long den) {
            if (this.lichSu == null) {
                final List<int[]> ra = new ArrayList<int[]>(1);
                ra.add(new int[]{this.x, this.y});
                return ra;
            }
            return this.lichSu.trongKhoang(tu, den, this.x, this.y);
        }
    }

    /** Mot lan trung: muc tieu, vi tri luc trung (da bu tre), quang duong doc duong dan, thoi diem (ms tu luc ban). */
    public static final class Trung {
        public final MucTieu mt;
        public final int x;
        public final int y;
        public final double s;
        public final long ms;

        Trung(final MucTieu mt, final int x, final int y, final double s, final long ms) {
            this.mt = mt;
            this.x = x;
            this.y = y;
            this.s = s;
            this.ms = ms;
        }
    }

    /** Ket qua mo phong: tam that (cat o tuong) + danh sach trung (theo thu tu trung). */
    public static final class KetQua {
        public int tamThat;
        public final List<Trung> trung = new ArrayList<Trung>();

        public boolean trungId(final int id) {
            for (final Trung t : this.trung) {
                if (t.mt.id == id) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Tam that doc huong (hx, hy da chuan hoa): dung o diem dau tien bi tuong chan (buoc {@link #BUOC_TUONG} px). */
    public static int tamThat(final Tuong tuong, final double ox, final double oy, final double hx, final double hy, final int tam) {
        if (tuong == null) {
            return tam;
        }
        for (int d = BUOC_TUONG; d <= tam; d += BUOC_TUONG) {
            if (tuong.chan((int) Math.floor(ox + hx * d), (int) Math.floor(oy + hy * d))) {
                return Math.max(0, d - BUOC_TUONG);
            }
        }
        return tam;
    }

    /** Doan thang (ax, ay) -&gt; (bx, by) khong bi tuong chan. */
    public static boolean thongDuong(final Tuong tuong, final double ax, final double ay, final double bx, final double by) {
        if (tuong == null) {
            return true;
        }
        final double dx = bx - ax, dy = by - ay;
        final double l = Math.sqrt(dx * dx + dy * dy);
        if (l < 1e-6) {
            return true;
        }
        for (double d = BUOC_TUONG; d < l; d += BUOC_TUONG) {
            if (tuong.chan((int) Math.floor(ax + dx * d / l), (int) Math.floor(ay + dy * d / l))) {
                return false;
            }
        }
        return true;
    }

    /** Huong chuan hoa, null = vector 0 / khong hop le. */
    public static double[] chuanHoa(final double dx, final double dy) {
        final double l = Math.sqrt(dx * dx + dy * dy);
        if (!(l >= 1e-6) || Double.isInfinite(l)) {
            return null;
        }
        return new double[]{dx / l, dy / l};
    }

    /**
     * Mo phong mot phat ban.
     *
     * @param ts    thong so chieu
     * @param ox    goc x (px)
     * @param oy    goc y
     * @param dx    huong x (bat ky, khac 0)
     * @param dy    huong y
     * @param t0    thoi diem ban (ms may chu)
     * @param buTre cua so bu tre (ms) - kep [0, {@link #BU_TRE_TOI_DA_MS}]
     * @param ds    ung vien (da loc: khong gom nguoi ban)
     * @param tuong tuong ban do (null = khong co)
     */
    public static KetQua moPhong(final ThongSo ts, final double ox, final double oy, final double dx, final double dy, final long t0,
            final long buTre, final List<MucTieu> ds, final Tuong tuong) {
        final KetQua kq = new KetQua();
        final double[] h = chuanHoa(dx, dy);
        if (ts == null || h == null || ts.kieu == K_TU_THAN) {
            return kq;
        }
        final long bu = Math.max(0L, Math.min(BU_TRE_TOI_DA_MS, buTre));
        final double hx = h[0], hy = h[1];
        kq.tamThat = tamThat(tuong, ox, oy, hx, hy, ts.tam);
        final double nuaRong = ts.rong / 2.0 + BAN_KINH_THAN;
        if (ts.kieu == K_DAN || ts.kieu == K_XUYEN) {
            final long msBay = ts.msBay(kq.tamThat);
            final List<Trung> thay = new ArrayList<Trung>();
            final boolean[] da = new boolean[ds.size()];
            double sTruoc = 0.0;
            long ms = 0L;
            while (true) {
                final double sNay = msBay <= 0L ? kq.tamThat : Math.min(kq.tamThat, (double) ts.tocDo * ms / 1000.0);
                final double sMin = (ms == 0L ? 0.0 : sTruoc) - BAN_KINH_THAN, sMax = sNay + BAN_KINH_THAN;
                final List<Trung> buocNay = new ArrayList<Trung>();
                for (int i = 0; i < ds.size(); i++) {
                    if (da[i]) {
                        continue;
                    }
                    final MucTieu mt = ds.get(i);
                    Trung tot = null;
                    for (final int[] v : mt.viTri(t0 + ms - bu, t0 + ms)) {
                        final double rx = v[0] - ox, ry = v[1] - oy;
                        final double s = rx * hx + ry * hy;
                        final double lech = Math.abs(rx * hy - ry * hx);
                        if (s >= sMin && s <= sMax && s >= -BAN_KINH_THAN && s <= kq.tamThat + BAN_KINH_THAN && lech <= nuaRong) {
                            if (tot == null || s < tot.s) {
                                tot = new Trung(mt, v[0], v[1], Math.max(0.0, s), ms);
                            }
                        }
                    }
                    if (tot != null) {
                        da[i] = true;
                        buocNay.add(tot);
                    }
                }
                Collections.sort(buocNay, THEO_S);
                thay.addAll(buocNay);
                if (ts.kieu == K_DAN && !thay.isEmpty()) {
                    break;
                }
                if (sNay >= kq.tamThat || msBay <= 0L || ms >= BAY_TOI_DA_MS) {
                    break;
                }
                sTruoc = sNay;
                ms += BUOC_MS;
            }
            for (int i = 0; i < thay.size() && kq.trung.size() < ts.toiDa; i++) {
                kq.trung.add(thay.get(i));
            }
            return kq;
        }
        // non / vung: tuc thi, vi tri trong [t0 - buTre, t0]
        final double cx = ox + hx * kq.tamThat, cy = oy + hy * kq.tamThat;
        final double cosNua = Math.cos(Math.toRadians(ts.goc / 2.0));
        final List<Trung> thay = new ArrayList<Trung>();
        for (final MucTieu mt : ds) {
            Trung tot = null;
            for (final int[] v : mt.viTri(t0 - bu, t0)) {
                final double rx = v[0] - ox, ry = v[1] - oy;
                final double kc = Math.sqrt(rx * rx + ry * ry);
                boolean ok;
                double s;
                if (ts.kieu == K_NON) {
                    s = kc;
                    if (kc > ts.tam + BAN_KINH_THAN) {
                        ok = false;
                    } else if (kc <= BAN_KINH_THAN) {
                        ok = true; // dinh sat nguoi ban
                    } else {
                        final double cos = (rx * hx + ry * hy) / kc;
                        // mep non: tinh ca than (lech vuong goc toi tia mep <= ban kinh than)
                        ok = cos >= cosNua || mepNon(rx, ry, hx, hy, ts.goc) <= BAN_KINH_THAN;
                    }
                    ok = ok && thongDuong(tuong, ox, oy, v[0], v[1]);
                } else {
                    final double ex = v[0] - cx, ey = v[1] - cy;
                    s = Math.sqrt(ex * ex + ey * ey);
                    ok = s <= ts.rong / 2.0 + BAN_KINH_THAN;
                }
                if (ok && (tot == null || s < tot.s)) {
                    tot = new Trung(mt, v[0], v[1], s, 0L);
                }
            }
            if (tot != null) {
                thay.add(tot);
            }
        }
        Collections.sort(thay, THEO_S);
        for (int i = 0; i < thay.size() && kq.trung.size() < ts.toiDa; i++) {
            kq.trung.add(thay.get(i));
        }
        return kq;
    }

    /** Khoang cach tu diem (rx, ry) toi hai tia mep cua non (chi phia truoc). */
    static double mepNon(final double rx, final double ry, final double hx, final double hy, final int goc) {
        final double a = Math.toRadians(goc / 2.0);
        double best = Double.MAX_VALUE;
        for (int k = -1; k <= 1; k += 2) {
            final double c = Math.cos(a * k), s = Math.sin(a * k);
            final double mx = hx * c - hy * s, my = hx * s + hy * c;
            final double chieu = rx * mx + ry * my;
            if (chieu < 0) {
                continue;
            }
            best = Math.min(best, Math.abs(rx * my - ry * mx));
        }
        return best;
    }

    static final Comparator<Trung> THEO_S = new Comparator<Trung>() {
        @Override
        public int compare(final Trung a, final Trung b) {
            return Double.compare(a.s, b.s);
        }
    };

    // ================================================================== kiem hop le

    /** Ly do tu choi phat ban (null = hop le). Thuan: moi gia tri do lop game dua vao. */
    public static String kiemBan(final int chieu, final int soChieu, final int capChieu, final boolean buff, final long msTuLanTruoc,
            final long hoiChieuMs, final int mp, final int mpCan, final boolean conSong, final boolean biKhong, final int dx, final int dy) {
        if (chieu < 0 || chieu >= soChieu) {
            return "chieu_sai";
        }
        if (capChieu <= 0) {
            return "chua_hoc";
        }
        if (buff) {
            return "buff";
        }
        if (!conSong) {
            return "da_guc";
        }
        if (biKhong) {
            return "bi_khong_che";
        }
        if (dx == 0 && dy == 0) {
            return "huong_0";
        }
        if (msTuLanTruoc < hoiChieuMs) {
            return "hoi_chieu";
        }
        if (mp < mpCan) {
            return "thieu_pp";
        }
        return null;
    }

    /** Goc client gui co dung duoc khong (lech vi tri may chu &lt;= {@link #LECH_GOC}). */
    public static boolean gocHopLe(final int ox, final int oy, final int sx, final int sy) {
        return Math.abs(ox - sx) <= LECH_GOC && Math.abs(oy - sy) <= LECH_GOC;
    }

    /**
     * Huong 4 phia client cu (Util.findDirection cua client J2ME: nhan vat quay ve muc tieu truoc khi danh). 0 xuong, 1
     * len, 2 trai, 3 phai -&gt; vector {dx, dy}.
     */
    public static int[] huongBonPhia(final int tuX, final int tuY, final int denX, final int denY) {
        if (Math.abs(tuX - denX) < Math.abs(tuY - denY)) {
            return denY > tuY ? new int[]{0, 1000} : new int[]{0, -1000};
        }
        return denX < tuX ? new int[]{-1000, 0} : new int[]{1000, 0};
    }

    // ================================================================== chong spam huong

    /**
     * Dem goi ban bi tu choi (hoi chieu / sai) trong cua so truot: qua {@link #SPAM_SO} lan / {@link #SPAM_MS} ms = nghi
     * van (spam doi huong / ban lien tuc mong trung). Moi phat hop le van chi qua duoc mot lan moi hoi chieu.
     */
    public static final int SPAM_SO = 25;
    public static final long SPAM_MS = 5000L;
    /** Khoang toi thieu giua hai phat (moi chieu) - chan goi lap doi huong trong cung khung. */
    public static final long KHOANG_TOI_THIEU_MS = 80L;

    public static final class ChongSpam {
        final long[] moc = new long[SPAM_SO];
        int dau;
        int so;
        long phatCuoi;

        /** Ghi mot goi bi tu choi; true = vua vuot nguong (bao nghi van). */
        public synchronized boolean tuChoi(final long bay) {
            while (this.so > 0 && bay - this.moc[this.dau] > SPAM_MS) {
                this.dau = (this.dau + 1) % SPAM_SO;
                this.so--;
            }
            if (this.so == SPAM_SO) {
                this.dau = (this.dau + 1) % SPAM_SO;
                this.so--;
            }
            this.moc[(this.dau + this.so) % SPAM_SO] = bay;
            this.so++;
            return this.so >= SPAM_SO;
        }

        public synchronized int soTuChoi() {
            return this.so;
        }

        /** Bo moc phat cuoi (test / doi ban do). */
        public synchronized void datLaiPhat() {
            this.phatCuoi = 0L;
        }

        /** Phat hop le: true = cho qua (cach phat truoc &gt;= {@link #KHOANG_TOI_THIEU_MS}). */
        public synchronized boolean choPhat(final long bay) {
            if (bay - this.phatCuoi < KHOANG_TOI_THIEU_MS) {
                return false;
            }
            this.phatCuoi = bay;
            return true;
        }
    }
}
