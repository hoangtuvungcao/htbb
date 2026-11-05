package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PK 3 VS 3 BUOI TOI (chu game 05/11/2026: "hoat dong PK pet buoi toi, PK 3 vs 3, 500 luot/ngay") - phan THUAN (khong
 * CSDL, khong mang). Game: real.Pk3v3Game. Dung LAI san + duong danh nguoi cua Cup PvP tuan (real.CupGame /
 * real.PhongRiengGame: ban do phong rieng 4382, moi tran mot khu, Region.doAttackPlayer qua choDanh / satThuong / sauDon) -
 * KHONG co he chien dau moi, KHONG tinh Elo / co PK, HP khong ve 0 (het HP = bi loai), khong mat EXP, khong tu dong danh.
 *
 * <ul>
 *   <li><b>Gio mo</b>: {@link #GIO_MO}:00 - {@link #GIO_DONG}:00 gio Viet Nam (khoa pk3v3.gio_mo / gio_dong, sua tren web).
 *       Mo / dong bao toan may chu; het gio thi hang cho bi xoa, tran dang danh van danh xong.</li>
 *   <li><b>Hang cho</b>: vao mot minh hoac ca to doi 2-3 nguoi (doi truong bam). Ghep theo <b>bac cap</b> {@link #bac}
 *       (20-39, 40-59, ... 120+; to doi lay cap cao nhat) - cap thap khong gap cap cao. Hai ben cua mot tran khong
 *       chung thiet bi / IP / tai khoan; trong mot doi khong chung thiet bi / tai khoan. Cho qua {@link #GIAY_CHO_TOI_DA}
 *       giay thi tu roi hang.</li>
 *   <li><b>Tran</b>: {@link #GIAY_CHUAN_BI} giay chuan bi + toi da {@link #GIAY_TRAN} giay. Sat thuong chuan hoa nhu Cup
 *       ({@link CupLogic#satThuong}, {@link #PHAN_NGHIN_MIN}..{@link #PHAN_NGHIN_MAX} phan nghin HP muc tieu / don). Doi
 *       het nguoi thi thua; het gio: doi con nhieu tong % HP hon thang. Mat ket noi / roi san / bo cuoc = bi loai, doi
 *       danh tiep thieu nguoi.</li>
 *   <li><b>Luot</b>: {@link #TRAN_NGAY} tran / ngay / TAI KHOAN (gop moi Pokemon cua tai khoan - chan nick phu), tinh
 *       luc bat dau tran.</li>
 *   <li><b>Thuong</b> (nho, khong xu): thang hop le {@link #LUONG_THANG} luong khoa, chi {@link #THANG_THUONG_NGAY}
 *       tran thang dau moi ngay / tai khoan; top 3 ngay {@link #LK_TOP_NGAY}; moi khoan chung tran
 *       {@link #TRAN_LUONG_TUAN} luong khoa / tuan / tai khoan. Diem xep hang: thang {@link #DIEM_THANG}, hoa
 *       {@link #DIEM_HOA}. Tuan: hang 1 danh hieu {@link #DH_VUA}, hang 2-10 {@link #DH_DAU_SI} (7 ngay).</li>
 *   <li><b>Chong cay / treo</b> ({@link #xet}): tran duoi {@link #GIAY_TOI_THIEU} giay danh hoac ben thua gay duoi
 *       {@link #BEN_THUA_TOI_THIEU} phan nghin sat thuong = khong hop le (khong diem, khong thuong); nguoi gay duoi
 *       {@link #AFK_PHAN_NGHIN} phan nghin = treo may, khong diem / thuong; gap cung mot doi thu qua
 *       {@link #GAP_LAI_TOI_DA} lan trong ngay = tran do khong diem, khong thuong. Huan luyen vien AI (la_bot = 1): duoc danh (lap
 *       hang cho), khong nhan luong khoa, khong vao bang xep hang / danh hieu.</li>
 * </ul>
 * Kiem: test/KiemPk3v3.
 */
public final class Pk3v3 {

    private Pk3v3() {
    }

    /** Ten hoat dong = nhan menu (So tay, Cup, NPC Dau truong, the Su kien client). */
    public static final String TEN = "PK 3 vs 3 buổi tối";

    // ================================================================== khoa cau hinh + mac dinh

    public static final String K_BAT = "pk3v3.bat", K_GIO_MO = "pk3v3.gio_mo", K_GIO_DONG = "pk3v3.gio_dong",
            K_TRAN_NGAY = "pk3v3.tran_ngay", K_LUONG_THANG = "pk3v3.luong_thang", K_THANG_THUONG = "pk3v3.thang_thuong_ngay",
            K_TRAN_LUONG_TUAN = "pk3v3.tran_luong_tuan", K_CAP = "pk3v3.cap_toi_thieu", K_GAP_LAI = "pk3v3.gap_lai_toi_da",
            K_GIAY_TOI_THIEU = "pk3v3.giay_toi_thieu";
    /** Moi khoa cua hoat dong (danh muc cau_hinh_danh_muc nhom pvp; test doi chieu SQL / gen.ts / docs). */
    public static final String[] KHOA = {K_BAT, K_GIO_MO, K_GIO_DONG, K_TRAN_NGAY, K_LUONG_THANG, K_THANG_THUONG,
        K_TRAN_LUONG_TUAN, K_CAP, K_GAP_LAI, K_GIAY_TOI_THIEU};

    public static final int GIO_MO = 19, GIO_DONG = 23;
    public static final int TRAN_NGAY = 500;
    public static final int CAP_TOI_THIEU = 20;
    public static final int LUONG_THANG = 1;
    public static final int THANG_THUONG_NGAY = 3;
    public static final int TRAN_LUONG_TUAN = 25;
    public static final int GAP_LAI_TOI_DA = 3;
    public static final int GIAY_TOI_THIEU = 20;

    // ================================================================== hang so co dinh

    public static final int SO_NGUOI = 3;
    /** Do rong mot bac cap; bac cuoi gom moi cap tu (CAP_TOI_THIEU + (SO_BAC - 1) * BE_RONG_BAC). */
    public static final int BE_RONG_BAC = 20, SO_BAC = 6;
    public static final int GIAY_CHUAN_BI = 5, GIAY_TRAN = 180;
    public static final int GIAY_CHO_TOI_DA = 600;
    public static final int PHAN_NGHIN_MIN = 10, PHAN_NGHIN_MAX = 80;
    /** Nguoi gay it hon ngan nay phan nghin (5% mot thanh HP) ca tran = treo may. */
    public static final int AFK_PHAN_NGHIN = 50;
    /** Ben thua gay it hon ngan nay phan nghin ca tran = tran nhuong (khong hop le). */
    public static final int BEN_THUA_TOI_THIEU = 100;
    public static final int DIEM_THANG = 3, DIEM_HOA = 1, DIEM_THUA = 0;
    /** Top ngay (hang 1, 2, 3): luong khoa, can >= TRAN_TOP_NGAY tran hop le trong ngay. */
    public static final int[] LK_TOP_NGAY = {3, 2, 1};
    public static final int TRAN_TOP_NGAY = 5;
    /** Danh hieu tuan (data_danh_hieu): hang 1 / hang 2..TOP_DAU_SI; can >= TRAN_TOP_TUAN tran hop le trong tuan. */
    public static final int DH_VUA = 9880, DH_DAU_SI = 9881, TOP_DAU_SI = 10, TRAN_TOP_TUAN = 10;
    public static final int PHUT_DH = 7 * 1440;

    /** Ket qua (cung ma CupLogic). */
    public static final int A_THANG = CupLogic.A_THANG, B_THANG = CupLogic.B_THANG, HOA = CupLogic.HOA;

    /** Ma danh hieu (data_danh_hieu.ma) - test doi chieu SQL. */
    public static String maDanhHieu(final int id) {
        return id == DH_VUA ? "pk3v3_vua" : id == DH_DAU_SI ? "pk3v3_tinh_anh" : "";
    }

    // ================================================================== gio mo

    /** Gio (0..23) Viet Nam cua {@code ms}. */
    public static int gio(final long ms) {
        final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
        c.setTimeInMillis(ms);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    /** Gio mo / dong da kep: mo 0..23, dong 1..24; dong &lt;= mo = qua nua dem (vd 22 -&gt; 2). */
    public static boolean dangMo(final long ms, final int gioMo, final int gioDong) {
        final int mo = Math.max(0, Math.min(23, gioMo));
        final int dong = Math.max(1, Math.min(24, gioDong));
        final int h = gio(ms);
        if (dong > mo) {
            return h >= mo && h < dong;
        }
        return h >= mo || h < dong;
    }

    /** "19:00–23:00". */
    public static String khungGio(final int gioMo, final int gioDong) {
        return hai(Math.max(0, Math.min(23, gioMo))) + ":00–" + hai(Math.max(1, Math.min(24, gioDong)) % 24) + ":00";
    }

    static String hai(final int n) {
        return (n < 10 ? "0" : "") + n;
    }

    /** Ngay (yyyymmdd gio Viet Nam, cung NangLuong.ngay) - moc luot ngay / bang ngay. */
    public static int ngay(final long ms) {
        final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
        c.setTimeInMillis(ms);
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    /** Tuan (moc thu Hai 05:00 chung CupLogic / nhiem vu tuan) - tran luong tuan / bang tuan. */
    public static int tuan(final long ms) {
        return CupLogic.tuan(ms);
    }

    // ================================================================== bac cap

    /** Bac cap 0..SO_BAC-1; -1 = duoi cap toi thieu. */
    public static int bac(final int cap, final int capToiThieu) {
        if (cap < capToiThieu) {
            return -1;
        }
        return Math.min(SO_BAC - 1, (cap - capToiThieu) / BE_RONG_BAC);
    }

    /** "cấp 40–59" / "cấp 120+". */
    public static String tenBac(final int bac, final int capToiThieu) {
        if (bac < 0) {
            return "dưới cấp " + capToiThieu;
        }
        final int tu = capToiThieu + bac * BE_RONG_BAC;
        return bac >= SO_BAC - 1 ? "cấp " + tu + "+" : "cấp " + tu + "–" + (tu + BE_RONG_BAC - 1);
    }

    // ================================================================== hang cho + ghep

    /** Mot nhom trong hang cho: 1 nguoi (vao mot minh) hoac to doi 2-3 nguoi (vao cung nhau, luon chung doi). */
    public static final class Nhom {
        public final int[] ids;
        public final int bac;
        public final long vao;
        /** Khoa thiet bi + tai khoan (cam trung trong cung doi). */
        public final Set<String> khoaTrong;
        /** Khoa thiet bi + IP + tai khoan (cam trung giua hai ben). */
        public final Set<String> khoaNgoai;

        public Nhom(final int[] ids, final int bac, final long vao, final Set<String> khoaTrong, final Set<String> khoaNgoai) {
            this.ids = ids.clone();
            this.bac = bac;
            this.vao = vao;
            this.khoaTrong = khoaTrong == null ? new HashSet<String>() : khoaTrong;
            this.khoaNgoai = khoaNgoai == null ? new HashSet<String>() : khoaNgoai;
        }

        public int so() {
            return ids.length;
        }

        public boolean co(final int id) {
            for (final int x : ids) {
                if (x == id) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Mot doi dang gom (tu cac nhom). */
    public static final class Doi {
        public final List<Nhom> nhom = new ArrayList<Nhom>();
        public final int bac;

        Doi(final int bac) {
            this.bac = bac;
        }

        public int so() {
            int n = 0;
            for (final Nhom g : nhom) {
                n += g.so();
            }
            return n;
        }

        public List<Integer> ids() {
            final List<Integer> l = new ArrayList<Integer>();
            for (final Nhom g : nhom) {
                for (final int x : g.ids) {
                    l.add(Integer.valueOf(x));
                }
            }
            return l;
        }

        boolean nhanDuoc(final Nhom g) {
            if (g.bac != bac || so() + g.so() > SO_NGUOI) {
                return false;
            }
            for (final Nhom x : nhom) {
                if (CupLogic.trungClone(x.khoaTrong, g.khoaTrong)) {
                    return false;
                }
            }
            return true;
        }

        Set<String> khoaNgoai() {
            final Set<String> s = new HashSet<String>();
            for (final Nhom g : nhom) {
                s.addAll(g.khoaNgoai);
            }
            return s;
        }
    }

    /** So lan hai nhan vat da gap nhau (khac doi) hom nay. */
    public interface GapLai {
        int lan(int a, int b);
    }

    /** Lan gap nhieu nhat giua mot nguoi doi a va mot nguoi doi b. */
    public static int gapNhieuNhat(final List<Integer> a, final List<Integer> b, final GapLai g) {
        int m = 0;
        if (g == null) {
            return 0;
        }
        for (final Integer x : a) {
            for (final Integer y : b) {
                m = Math.max(m, g.lan(x.intValue(), y.intValue()));
            }
        }
        return m;
    }

    /**
     * Ghep: theo tung bac, xep nhom theo thu tu vao hang (som truoc) vao doi dau tien con cho (first-fit, khong trung
     * thiet bi / tai khoan trong doi); doi du {@link #SO_NGUOI} nguoi ghep cap voi doi du khac khong trung thiet bi / IP
     * / tai khoan, uu tien doi chua gap qua {@code gapLaiToiDa} lan. Nhom khong vao duoc tran nao thi o lai hang.
     *
     * @return moi phan tu {doi A, doi B}
     */
    public static List<Doi[]> ghep(final List<Nhom> hang, final GapLai g, final int gapLaiToiDa) {
        final List<Nhom> ds = new ArrayList<Nhom>(hang);
        Collections.sort(ds, new Comparator<Nhom>() {
            public int compare(final Nhom x, final Nhom y) {
                final int c = Long.compare(x.vao, y.vao);
                return c != 0 ? c : Integer.compare(x.ids.length == 0 ? 0 : x.ids[0], y.ids.length == 0 ? 0 : y.ids[0]);
            }
        });
        final List<Doi[]> kq = new ArrayList<Doi[]>();
        for (int b = 0; b < SO_BAC; b++) {
            final List<Doi> doi = new ArrayList<Doi>();
            for (final Nhom n : ds) {
                if (n.bac != b || n.so() < 1 || n.so() > SO_NGUOI) {
                    continue;
                }
                Doi vao = null;
                for (final Doi d : doi) {
                    if (d.nhanDuoc(n)) {
                        vao = d;
                        break;
                    }
                }
                if (vao == null) {
                    vao = new Doi(b);
                    doi.add(vao);
                }
                vao.nhom.add(n);
            }
            final List<Doi> du = new ArrayList<Doi>();
            for (final Doi d : doi) {
                if (d.so() == SO_NGUOI) {
                    du.add(d);
                }
            }
            final boolean[] dung = new boolean[du.size()];
            for (int i = 0; i < du.size(); i++) {
                if (dung[i]) {
                    continue;
                }
                int chon = -1;
                for (int lan = 0; lan < 2 && chon < 0; lan++) {
                    for (int j = i + 1; j < du.size(); j++) {
                        if (dung[j] || CupLogic.trungClone(du.get(i).khoaNgoai(), du.get(j).khoaNgoai())) {
                            continue;
                        }
                        if (lan == 0 && gapNhieuNhat(du.get(i).ids(), du.get(j).ids(), g) >= gapLaiToiDa) {
                            continue; // lan 1: tranh doi da gap qua nhieu; lan 2: van ghep (choi duoc, chi khong thuong)
                        }
                        chon = j;
                        break;
                    }
                }
                if (chon >= 0) {
                    dung[i] = true;
                    dung[chon] = true;
                    kq.add(new Doi[]{du.get(i), du.get(chon)});
                }
            }
        }
        return kq;
    }

    // ================================================================== ket qua + thuong

    /** Ly do ca tran khong hop le (khong diem, khong thuong), null = hop le. {@code giay} = giay danh (sau chuan bi). */
    public static String lyDoTran(final int ketQua, final int giay, final int stBenThua, final int giayToiThieu) {
        if (giay < giayToiThieu) {
            return "nhanh";
        }
        if (ketQua != HOA && stBenThua < BEN_THUA_TOI_THIEU) {
            return "nhuong";
        }
        return null;
    }

    /** Ket qua xet cho mot nguoi sau tran. */
    public static final class Xet {
        public final int diem;
        public final int luong;
        public final boolean thangTinhThuong;
        /** "" = hop le; nhanh / nhuong / treo / gap_lai / bot / het_thang / het_tuan. */
        public final String lyDo;

        Xet(final int diem, final int luong, final boolean thangTinhThuong, final String lyDo) {
            this.diem = diem;
            this.luong = luong;
            this.thangTinhThuong = thangTinhThuong;
            this.lyDo = lyDo;
        }

        /** Tran tinh vao bang xep hang cua nguoi nay (khong phai tran nhanh / nhuong / treo may / gap lai qua tran). */
        public boolean hopLe() {
            return !("nhanh".equals(lyDo) || "nhuong".equals(lyDo) || "treo".equals(lyDo) || "gap_lai".equals(lyDo));
        }
    }

    /**
     * Xet mot nguoi: {@code kq} 1 thang / 0 hoa / -1 thua (cua doi minh); {@code lyDoTran} tu {@link #lyDoTran};
     * {@code stMinh} phan nghin minh gay ca tran; {@code gapLai} so lan da gap doi thu nhieu nhat (truoc tran nay);
     * {@code thangDaThuong} tran thang da nhan luong hom nay (tai khoan); {@code luongTuan} luong khoa PK 3v3 da nhan
     * tuan nay (tai khoan).
     */
    public static Xet xet(final int kq, final String lyDoTran, final int stMinh, final boolean bot, final int gapLai,
            final int thangDaThuong, final int luongTuan, final int luongThang, final int thangThuongNgay,
            final int tranLuongTuan, final int gapLaiToiDa) {
        if (lyDoTran != null) {
            return new Xet(0, 0, false, lyDoTran);
        }
        if (stMinh < AFK_PHAN_NGHIN) {
            return new Xet(0, 0, false, "treo");
        }
        if (gapLai >= gapLaiToiDa) {
            return new Xet(0, 0, false, "gap_lai"); // cay diem / luong voi cung mot doi thu: khong diem, khong thuong
        }
        final int diem = kq > 0 ? DIEM_THANG : kq == 0 ? DIEM_HOA : DIEM_THUA;
        if (bot) {
            return new Xet(diem, 0, false, "bot");
        }
        if (kq <= 0) {
            return new Xet(diem, 0, false, "");
        }
        if (thangDaThuong >= thangThuongNgay) {
            return new Xet(diem, 0, false, "het_thang");
        }
        final int lk = Math.max(0, Math.min(luongThang, tranLuongTuan - Math.max(0, luongTuan)));
        return new Xet(diem, lk, true, lk <= 0 && luongThang > 0 ? "het_tuan" : "");
    }

    /** Luong khoa thuong top ngay (hang 1..3, da kep tran tuan), 0 neu khong du dieu kien. */
    public static int luongTopNgay(final int hang, final int tranHopLe, final int luongTuan, final int tranLuongTuan) {
        if (hang < 1 || hang > LK_TOP_NGAY.length || tranHopLe < TRAN_TOP_NGAY) {
            return 0;
        }
        return Math.max(0, Math.min(LK_TOP_NGAY[hang - 1], tranLuongTuan - Math.max(0, luongTuan)));
    }

    /** Danh hieu tuan theo hang (0 = khong). */
    public static int danhHieuTuan(final int hang, final int tranHopLe) {
        if (hang < 1 || tranHopLe < TRAN_TOP_TUAN) {
            return 0;
        }
        return hang == 1 ? DH_VUA : hang <= TOP_DAU_SI ? DH_DAU_SI : 0;
    }

    // ================================================================== bang xep hang

    /** Diem mot nhan vat trong mot ky (ngay / tuan). */
    public static final class Diem {
        public final int charId;
        public String ten = "";
        public int userId;
        public boolean bot;
        public int diem, thang, hoa, thua, hopLe;
        /** Moc (ms) lan cuoi doi diem - bang nhau thi ai dat truoc dung truoc. */
        public long luc;

        public Diem(final int charId) {
            this.charId = charId;
        }

        public void cong(final Diem d) {
            diem += d.diem;
            thang += d.thang;
            hoa += d.hoa;
            thua += d.thua;
            hopLe += d.hopLe;
            luc = Math.max(luc, d.luc);
            if (d.ten != null && !d.ten.isEmpty()) {
                ten = d.ten;
            }
            userId = d.userId != 0 ? d.userId : userId;
            bot |= d.bot;
        }
    }

    /** Bang xep hang (bo bot, bo nguoi chua co tran hop le): diem giam dan, it tran hon, dat som hon, id. */
    public static List<Diem> xepHang(final List<Diem> ds) {
        final List<Diem> l = new ArrayList<Diem>();
        for (final Diem d : ds) {
            if (!d.bot && d.hopLe > 0) {
                l.add(d);
            }
        }
        Collections.sort(l, new Comparator<Diem>() {
            public int compare(final Diem a, final Diem b) {
                int c = Integer.compare(b.diem, a.diem);
                if (c == 0) {
                    c = Integer.compare(a.hopLe, b.hopLe);
                }
                if (c == 0) {
                    c = Long.compare(a.luc, b.luc);
                }
                return c != 0 ? c : Integer.compare(a.charId, b.charId);
            }
        });
        return l;
    }

    // ================================================================== chu

    public static String luatChu(final int gioMo, final int gioDong, final int tranNgay, final int capToiThieu,
            final int luongThang, final int thangThuongNgay, final int tranLuongTuan, final int gapLaiToiDa, final int giayToiThieu) {
        return TEN + ": mở " + khungGio(gioMo, gioDong) + " (giờ Việt Nam) mỗi ngày, từ cấp " + capToiThieu + ".\n"
                + "Vào hàng chờ một mình hoặc cả tổ đội 2–3 người (đội trưởng bấm); hệ thống ghép đủ 3 đấu 3 cùng bậc cấp ("
                + BE_RONG_BAC + " cấp một bậc).\n"
                + "Trận " + (GIAY_TRAN / 60) + " phút trên Sàn Cúp PvP: hết HP là bị loại (không gục, không mất EXP), đội hết người thua; "
                + "hết giờ đội còn nhiều % HP hơn thắng. Rớt mạng / rời sân / bỏ cuộc = bị loại, đội đánh tiếp thiếu người.\n"
                + "Tối đa " + tranNgay + " trận / ngày / tài khoản.\n"
                + "Thưởng: thắng +" + luongThang + " lượng khoá (" + thangThuongNgay + " trận thắng đầu mỗi ngày), top 3 ngày +"
                + LK_TOP_NGAY[0] + "/" + LK_TOP_NGAY[1] + "/" + LK_TOP_NGAY[2] + ", tối đa " + tranLuongTuan
                + " lượng khoá / tuần; điểm: thắng " + DIEM_THANG + ", hoà " + DIEM_HOA + ". Top 1 tuần: danh hiệu Vua PK 3 vs 3, top 2–"
                + TOP_DAU_SI + ": Tinh anh PK 3 vs 3 (7 ngày).\n"
                + "Không tính: trận dưới " + giayToiThieu + " giây, bên thua gần như không đánh, người treo máy, gặp cùng đối thủ quá "
                + gapLaiToiDa + " lần / ngày (vẫn chơi được, chỉ không tính điểm / thưởng).";
    }

    public static String lyDoChu(final String ly) {
        if ("nhanh".equals(ly)) {
            return "trận kết thúc quá nhanh, không tính";
        }
        if ("nhuong".equals(ly)) {
            return "bên thua gần như không đánh, không tính";
        }
        if ("treo".equals(ly)) {
            return "bạn gần như không gây sát thương, không tính";
        }
        if ("gap_lai".equals(ly)) {
            return "đã gặp đối thủ này quá nhiều lần hôm nay, không tính điểm / thưởng";
        }
        if ("het_thang".equals(ly)) {
            return "đã nhận đủ thưởng thắng hôm nay";
        }
        if ("het_tuan".equals(ly)) {
            return "đã đủ trần lượng khoá tuần";
        }
        if ("bot".equals(ly)) {
            return "";
        }
        return "";
    }
}
