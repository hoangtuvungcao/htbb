package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * PHO BAN HAM NGUC (0.9.7, de xuat 10 da duyet) - phan THUAN (khong CSDL, khong mang). Mo rong "Ham nguc bi an" cu
 * (real.HamNgucGame): cung muc So tay, cung MOT luot thuong ngay (HoSoLauDai.hnDaThuong - ngan sach "hang_bi_an"
 * 0,2 gio EXP + 5 luong khoa), nhung la phong rieng (instance) 5 tang cho to doi 1-4 nguoi.
 *
 * <ul>
 *   <li>Chu de theo thu trong tuan (data_pho_ban_ngay): he quai + boss tang 5 doi moi ngay (Hang Bang thu Hai, Nui
 *       Lua thu Ba...). Ngay doi luc 00:00 nhu luot ngay cua Ham nguc cu (NangLuong.ngay).</li>
 *   <li>Tang (data_pho_ban_tang): so quai + he so HP / tan cong / phong thu (phan nghin) so voi CHI SO GOC o cap do -
 *       noi suy tu bang Gym (data_noi_dung_gym: HP du ~105 giay danh mot minh dung cap, do +6). Tang 5 = boss.</li>
 *   <li>Luot vao: {@link #LUOT_NGAY} / ngay / nhan vat (vao lai = mat luot); thuong ngay chi MOT lan khi qua tang 5,
 *       dung chung voi Ham nguc cu.</li>
 *   <li>Thua: ca doi guc / roi phong (khong ai con song trong phong) qua {@link #GIAY_CHO_GUC} giay, hoac het
 *       {@link #PHUT_TOI_DA} phut. Mat ket noi: giu cho {@link #GIAY_MAT_KET_NOI} giay, dang nhap lai thi ve dung
 *       phong.</li>
 * </ul>
 */
public final class PhoBanLogic {

    public static final int SO_TANG = 5;
    public static final int NGUOI_TOI_DA = 4;
    /** Mac dinh cau hinh (pho_ban.*); test doi chieu tools/cauhinh/danh_muc.json. */
    public static final int CAP_TOI_THIEU = 20;
    public static final int LUOT_NGAY = 3;
    public static final int PHUT_TOI_DA = 20;
    /** Chenh cap toi da giua thanh vien (thiet_ke_can_bang 6.1: chi tinh khi chenh cap &lt;= 10). */
    public static final int CHENH_CAP_TOI_DA = 10;
    public static final int GIAY_CHO_GUC = 15;
    public static final int GIAY_MAT_KET_NOI = 180;
    /** Thuong ngay: 0,2 gio EXP o tang 5 (phan nghin gio), 5 luong khoa khi ha boss - giu nguyen ngan sach Ham nguc. */
    public static final int EXP_PHAN_NGHIN_GIO = 200;
    public static final int LUONG_KHOA_BOSS = 5;
    /** Nhom bang roi rương boss theo cap cao nhat cua doi (khop nguyen lieu nang cap: Bui sao / Manh sao / Manh thien thach). */
    public static final String NHOM_THAP = "boss:thap", NHOM_TRUNG = "boss:trung", NHOM_CAO = "boss:cao";
    /** Nguon bang roi trong data_noi_dung_roi. */
    public static final String NGUON_ROI = "pho_ban";

    /** Tran ky vong moi lan qua tang 5 / nguoi (kiem bang roi, KiemPhoBan): da, nguyen lieu, trang bi. */
    public static final double TRAN_DA_MOI_LAN = 4.0, TRAN_NGUYEN_LIEU_MOI_LAN = 1.0, TRAN_TRANG_BI_MOI_LAN = 0.35;

    private PhoBanLogic() {
    }

    // ================================================================== du lieu

    /** Mot chu de ngay (data_pho_ban_ngay). thu: 1 = thu Hai .. 7 = Chu nhat. */
    public static final class ChuDe {
        public final int thu;
        public final String ma, ten, moTa;
        public final int he;
        public final int[] mauQuai;
        public final int mauBoss;

        public ChuDe(final int thu, final String ma, final String ten, final int he, final int[] mauQuai, final int mauBoss,
                final String moTa) {
            this.thu = thu;
            this.ma = ma == null ? "" : ma;
            this.ten = ten == null ? "" : ten;
            this.he = he;
            this.mauQuai = mauQuai == null ? new int[0] : mauQuai;
            this.mauBoss = mauBoss;
            this.moTa = moTa == null ? "" : moTa;
        }
    }

    /** Mot tang (data_pho_ban_tang). He so tinh theo phan nghin chi so goc (Gym cung cap). */
    public static final class Tang {
        public final int tang, soQuai, hpPhanNghin, tcPhanNghin, thuPhanNghin;
        public final boolean laBoss;

        public Tang(final int tang, final int soQuai, final int hpPhanNghin, final int tcPhanNghin, final int thuPhanNghin,
                final boolean laBoss) {
            this.tang = tang;
            this.soQuai = soQuai;
            this.hpPhanNghin = hpPhanNghin;
            this.tcPhanNghin = tcPhanNghin;
            this.thuPhanNghin = thuPhanNghin;
            this.laBoss = laBoss;
        }
    }

    /** Ban sao mac dinh cua data_pho_ban_ngay (CSDL trong / test). Template theo du_lieu/bang_quai.csv. */
    public static List<ChuDe> chuDeMacDinh() {
        final List<ChuDe> ds = new ArrayList<ChuDe>();
        ds.add(new ChuDe(1, "hang_bang", "Hang Băng", HePokemon.BANG, new int[]{150, 155, 165}, 172,
                "Snorunt, Spheal, Glalie canh giữ; boss Walrein."));
        ds.add(new ChuDe(2, "nui_lua", "Núi Lửa", HePokemon.LUA, new int[]{17, 28, 141, 76}, 154,
                "Torkoal, Growlithe, Numel, Magcargo; boss Camerupt."));
        ds.add(new ChuDe(3, "ho_sau", "Hồ Sâu", HePokemon.NUOC, new int[]{26, 29, 35, 144}, 56,
                "Tentacool, Croconaw, Crawdaunt, Carvanha; boss Sharpedo."));
        ds.add(new ChuDe(4, "rung_ram", "Rừng Rậm", HePokemon.CO, new int[]{10, 19, 80, 20}, 58,
                "Oddish, Paras, Breloom, Gloom; boss Vileplume."));
        ds.add(new ChuDe(5, "nha_may_dien", "Nhà máy Điện", HePokemon.DIEN, new int[]{62, 140, 118}, 152,
                "Luxio, Electrike, Galvantula; boss Manectric."));
        ds.add(new ChuDe(6, "thap_ma", "Tháp Ma", HePokemon.MA, new int[]{25, 55, 52, 114}, 161,
                "Gastly, Duskull, Haunter, Shuppet; boss Banette."));
        ds.add(new ChuDe(7, "hang_rong", "Hang Rồng", HePokemon.RONG, new int[]{157, 167, 164}, 39,
                "Bagon, Shelgon, Altaria; boss Salamence."));
        return ds;
    }

    /** Ban sao mac dinh cua data_pho_ban_tang: tong ~11 phut danh mot minh dung cap (truoc he so to doi). */
    public static List<Tang> tangMacDinh() {
        final List<Tang> ds = new ArrayList<Tang>();
        ds.add(new Tang(1, 6, 120, 550, 900, false));
        ds.add(new Tang(2, 8, 120, 600, 900, false));
        ds.add(new Tang(3, 7, 150, 650, 950, false));
        ds.add(new Tang(4, 10, 120, 700, 950, false));
        ds.add(new Tang(5, 1, 1800, 1000, 1000, true));
        return ds;
    }

    // ================================================================== chu de ngay

    /** Thu trong tuan (1 = thu Hai .. 7 = Chu nhat) cua yyyyMMdd. */
    public static int thuCua(final int ngayYmd) {
        final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
        c.clear();
        c.set(ngayYmd / 10000, (ngayYmd / 100) % 100 - 1, ngayYmd % 100, 12, 0, 0);
        final int d = c.get(Calendar.DAY_OF_WEEK); // 1 = Chu nhat
        return d == Calendar.SUNDAY ? 7 : d - 1;
    }

    /** Chu de cua ngay (theo thu); danh sach thieu thu do thi lay theo vong tron. Danh sach rong = null. */
    public static ChuDe chuDe(final List<ChuDe> ds, final int ngayYmd) {
        if (ds == null || ds.isEmpty()) {
            return null;
        }
        final int thu = thuCua(ngayYmd);
        for (final ChuDe c : ds) {
            if (c.thu == thu) {
                return c;
            }
        }
        return ds.get((thu - 1) % ds.size());
    }

    // ================================================================== chi so quai

    /**
     * Chi so goc {hp, tan cong, phong thu} o cap {@code cap}: noi suy tuyen tinh (ngoai suy hai dau) bang Gym
     * {cap, hp, tc, thu}. Bang rong: cong thuc khop 16 Gym hien tai (sai lech &lt; 1%).
     */
    public static int[] chiSoGoc(final List<int[]> gym, final int cap) {
        final List<int[]> g = new ArrayList<int[]>();
        if (gym != null) {
            for (final int[] r : gym) {
                if (r != null && r.length >= 4 && r[0] > 0 && r[1] > 0) {
                    g.add(r);
                }
            }
        }
        if (g.size() < 2) {
            return new int[]{8550 + 3383 * cap, 321 + 28 * cap, 22 + (86 * cap) / 10};
        }
        Collections.sort(g, (a, b) -> Integer.compare(a[0], b[0]));
        int i = 0;
        while (i < g.size() - 2 && cap > g.get(i + 1)[0]) {
            i++;
        }
        final int[] a = g.get(i);
        final int[] b = g.get(i + 1);
        final int[] kq = new int[3];
        for (int k = 0; k < 3; k++) {
            final double v = a[k + 1] + (double) (b[k + 1] - a[k + 1]) * (cap - a[0]) / Math.max(1, b[0] - a[0]);
            kq[k] = (int) Math.max(1L, Math.round(v));
        }
        return kq;
    }

    /** He so to doi (phan tram) cho HP: quai thuong +60% / nguoi them, boss +70% / nguoi them. */
    public static int heSoNguoi(final int soNguoi, final boolean boss) {
        final int n = Math.max(1, Math.min(NGUOI_TOI_DA, soNguoi));
        return 100 + (boss ? 70 : 60) * (n - 1);
    }

    /** {hp, tan cong, phong thu} cua mot quai tang {@code t} cho doi {@code soNguoi} nguoi o cap {@code cap}. */
    public static int[] chiSoQuai(final int[] goc, final Tang t, final int soNguoi) {
        final long hp = (long) goc[0] * t.hpPhanNghin / 1000L * heSoNguoi(soNguoi, t.laBoss) / 100L;
        final long tc = (long) goc[1] * t.tcPhanNghin / 1000L;
        final long thu = (long) goc[2] * t.thuPhanNghin / 1000L;
        return new int[]{(int) Math.max(1L, Math.min(Integer.MAX_VALUE, hp)), (int) Math.max(1L, tc), (int) Math.max(1L, thu)};
    }

    /** Cap quai = cap CAO NHAT cua doi (nguoi cap thap khong ha quai yeu hon de cay). */
    public static int capQuai(final int[] capThanhVien) {
        int m = 1;
        for (final int c : capThanhVien) {
            m = Math.max(m, c);
        }
        return m;
    }

    /** Giay danh mot minh du kien de qua het 5 tang (HP / (HP Gym / 105 giay)), truoc he so to doi. */
    public static int giayMotMinh(final List<Tang> tang) {
        long phanNghin = 0;
        for (final Tang t : tang) {
            phanNghin += (long) t.soQuai * t.hpPhanNghin;
        }
        return (int) (phanNghin * 105L / 1000L);
    }

    // ================================================================== vao phong

    /**
     * Kiem doi vao phong. Moi mang cung chieu dai (thanh vien, truong doi o vi tri 0).
     * @return null = duoc, nguoc lai cau bao (tieng Viet co dau).
     */
    public static String kiemVao(final boolean bat, final int[] cap, final int[] luotDaDung, final boolean[] cungCho,
            final boolean[] dangBan, final String[] ten, final int capToiThieu, final int luotNgay) {
        if (!bat) {
            return "Phó bản đang tạm đóng. Vui lòng quay lại sau.";
        }
        final int n = cap.length;
        if (n < 1) {
            return "Không có thành viên nào.";
        }
        if (n > NGUOI_TOI_DA) {
            return "Tổ đội tối đa " + NGUOI_TOI_DA + " người mới vào được phó bản.";
        }
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if (cap[i] < capToiThieu) {
                return ten[i] + " chưa đạt cấp " + capToiThieu + ".";
            }
            if (luotDaDung[i] >= luotNgay) {
                return ten[i] + " đã hết lượt phó bản hôm nay (" + luotNgay + "/" + luotNgay + ").";
            }
            if (!cungCho[i]) {
                return ten[i] + " phải đứng cùng bản đồ với đội trưởng.";
            }
            if (dangBan[i]) {
                return ten[i] + " đang bận (phó bản / trận cúp / giao dịch).";
            }
            max = Math.max(max, cap[i]);
            min = Math.min(min, cap[i]);
        }
        if (max - min > CHENH_CAP_TOI_DA) {
            return "Thành viên chênh nhau quá " + CHENH_CAP_TOI_DA + " cấp, không vào chung phó bản được.";
        }
        return null;
    }

    // ================================================================== ket thuc

    public static final int DANG = 0, THANG = 1, HET_GIO = 2, THUA = 3, BO = 4;

    /**
     * Trang thai phong luc {@code bay}. {@code songTrongPhong} = so thanh vien con ket noi, dang o phong, con HP;
     * {@code matKetNoiConHan} = so thanh vien mat ket noi con trong {@link #GIAY_MAT_KET_NOI}; {@code khongAiTu} = moc
     * dau tien khong con ai song trong phong (0 = dang co).
     */
    public static int trangThai(final long bay, final long batDau, final int phutToiDa, final boolean daHaBoss,
            final int songTrongPhong, final int matKetNoiConHan, final long khongAiTu) {
        if (daHaBoss) {
            return THANG;
        }
        if (bay - batDau >= phutToiDa * 60000L) {
            return HET_GIO;
        }
        if (songTrongPhong <= 0 && matKetNoiConHan <= 0 && khongAiTu > 0 && bay - khongAiTu >= GIAY_CHO_GUC * 1000L) {
            return THUA;
        }
        return DANG;
    }

    // ================================================================== thuong

    /**
     * Thuong ngay theo tang cao nhat da qua: {exp phan nghin gio, luong khoa}. Chi phat khi qua tang 5 (ha boss) va
     * hom nay chua nhan thuong Ham nguc (chung voi Ham nguc cu). Tang &lt; 5: khong co thuong ngay (chi quai roi binh
     * thuong).
     */
    public static int[] thuongNgay(final int tangDaQua, final boolean daNhanHomNay) {
        if (daNhanHomNay || tangDaQua < SO_TANG) {
            return new int[]{0, 0};
        }
        return new int[]{EXP_PHAN_NGHIN_GIO, LUONG_KHOA_BOSS};
    }

    /** Nhom bang roi rương boss theo cap cao nhat (Bui sao duoi 30, Manh sao 30-59, Manh thien thach tu 60). */
    public static String nhomRoi(final int cap) {
        if (cap < 30) {
            return NHOM_THAP;
        }
        return cap < 60 ? NHOM_TRUNG : NHOM_CAO;
    }

    /** Ky vong so luong (ppm x trung binh sl / 1e6) cua mot dong roi. */
    public static double kyVong(final int ppm, final int slMin, final int slMax) {
        return ppm / 1000000.0 * (slMin + Math.max(slMin, slMax)) / 2.0;
    }

    /** Phan tram tien do (0..100) khi dang o tang {@code tang} da ha {@code daHa}/{@code tong} quai. */
    public static int phanTram(final int tang, final int daHa, final int tong) {
        final double t = (tang - 1) + (tong <= 0 ? 0 : Math.min(1.0, (double) daHa / tong));
        return (int) Math.max(0, Math.min(100, Math.round(t * 100.0 / SO_TANG)));
    }

    // ================================================================== vong doi phong (thuan)

    /** Mot thanh vien trong phong. */
    public static final class ThanhVien {
        public final int charId;
        public final String ten;
        /** Da roi phong (guc ve Trung tam, qua cong, dich chuyen, menu "Rời", mat ket noi qua han): khong quay lai. */
        public boolean roi;
        /** Moc mat ket noi (0 = dang online). */
        public long matKetNoiLuc;
        /** Vua dang nhap lai vao phong (nhip ke tiep gui lai the). */
        public boolean vuaVaoLai;

        public ThanhVien(final int charId, final String ten) {
            this.charId = charId;
            this.ten = ten == null ? "" : ten;
        }
    }

    /** Anh chup trang thai mot thanh vien luc nhip (do phan game doc tu Char). */
    public static final class TinhTrang {
        public final boolean online, trongPhong, conSong;

        public TinhTrang(final boolean online, final boolean trongPhong, final boolean conSong) {
            this.online = online;
            this.trongPhong = trongPhong;
            this.conSong = conSong;
        }
    }

    /** Su kien nhip tra ve cho phan game. */
    public static final int SK_ROI = 1, SK_VAO_LAI = 2, SK_SANG_TANG = 3, SK_KET_THUC = 4, SK_DONG = 5;

    /** Vong doi mot phong: tang, tien do, thanh vien, thang / thua / het gio, dong phong. */
    public static final class Phong {
        public final int khu, ngay, cap;
        public final long batDau;
        public final List<ThanhVien> tv = new ArrayList<ThanhVien>();
        public int tang;
        public int tong, daHa;
        public boolean daHaBoss;
        public long khongAiTu;
        public long sangTangLuc;
        public int ketQua = DANG;
        public long ketThucLuc;

        public Phong(final int khu, final int ngay, final int cap, final long batDau) {
            this.khu = khu;
            this.ngay = ngay;
            this.cap = cap;
            this.batDau = batDau;
        }

        public ThanhVien thanhVien(final int charId) {
            for (final ThanhVien t : tv) {
                if (t.charId == charId) {
                    return t;
                }
            }
            return null;
        }

        /** Con la thanh vien (chua roi) cua phong dang chay. */
        public boolean conTrong(final int charId) {
            final ThanhVien t = thanhVien(charId);
            return t != null && !t.roi && ketQua == DANG;
        }

        /** Bat dau tang {@code so} voi {@code soQuai} quai (0 quai - thieu template: coi nhu qua tang ngay). */
        public synchronized void batDauTang(final int so, final int soQuai, final long bay, final long treSangTangMs) {
            tang = so;
            tong = Math.max(0, soQuai);
            daHa = 0;
            sangTangLuc = 0L;
            if (tong == 0) {
                if (so >= SO_TANG) {
                    daHaBoss = true;
                } else {
                    sangTangLuc = bay + treSangTangMs;
                }
            }
        }

        /** Mot quai cua tang bi ha. @return true neu vua qua tang (tang 5: da ha boss). */
        public synchronized boolean haQuai(final long bay, final long treSangTangMs) {
            if (ketQua != DANG || daHa >= tong) {
                return false;
            }
            daHa++;
            if (daHa < tong) {
                return false;
            }
            if (tang >= SO_TANG) {
                daHaBoss = true;
            } else {
                sangTangLuc = bay + treSangTangMs;
            }
            return true;
        }

        /**
         * Dang nhap lai: thanh vien chua roi, mat ket noi chua qua han (hoac van "online" theo bo nho) thi ve phong.
         * @return true neu duoc ve phong.
         */
        public synchronized boolean dangNhapLai(final int charId, final long bay) {
            final ThanhVien t = thanhVien(charId);
            if (t == null || t.roi || ketQua != DANG) {
                return false;
            }
            if (t.matKetNoiLuc != 0L && bay - t.matKetNoiLuc >= GIAY_MAT_KET_NOI * 1000L) {
                t.roi = true;
                return false;
            }
            t.matKetNoiLuc = 0L;
            t.vuaVaoLai = true;
            return true;
        }

        /** Thanh vien roi tu nguyen (menu "Rời"). */
        public synchronized void roi(final int charId) {
            final ThanhVien t = thanhVien(charId);
            if (t != null) {
                t.roi = true;
            }
        }

        /**
         * Mot nhip (1 giay). {@code tt.get(i)} = tinh trang cua {@code tv.get(i)}. Tra danh sach su kien {ma, charId}:
         * SK_ROI (vua roi phong), SK_VAO_LAI (vua dang nhap lai), SK_SANG_TANG (den luc sang tang sau), SK_KET_THUC (ket
         * qua o {@link #ketQua}), SK_DONG (het gio cho sau ket thuc - dua moi nguoi ra, tra khu).
         */
        public synchronized List<int[]> nhip(final long bay, final List<TinhTrang> tt, final int phutToiDa) {
            final List<int[]> sk = new ArrayList<int[]>();
            if (ketQua != DANG) {
                if (bay - ketThucLuc >= (ketQua == THANG ? 8000L : 3000L)) {
                    sk.add(new int[]{SK_DONG, 0});
                }
                return sk;
            }
            int song = 0;
            int matKetNoi = 0;
            for (int i = 0; i < tv.size(); i++) {
                final ThanhVien t = tv.get(i);
                final TinhTrang x = i < tt.size() ? tt.get(i) : new TinhTrang(false, false, false);
                if (t.roi) {
                    continue;
                }
                if (!x.online) {
                    if (t.matKetNoiLuc == 0L) {
                        t.matKetNoiLuc = bay;
                    }
                    if (bay - t.matKetNoiLuc < GIAY_MAT_KET_NOI * 1000L) {
                        matKetNoi++;
                    } else {
                        t.roi = true;
                        sk.add(new int[]{SK_ROI, t.charId});
                    }
                    continue;
                }
                if (t.matKetNoiLuc != 0L || t.vuaVaoLai) {
                    t.matKetNoiLuc = 0L;
                    t.vuaVaoLai = false;
                    if (x.trongPhong) {
                        sk.add(new int[]{SK_VAO_LAI, t.charId});
                    }
                }
                if (!x.trongPhong) {
                    t.roi = true;
                    sk.add(new int[]{SK_ROI, t.charId});
                    continue;
                }
                if (x.conSong) {
                    song++;
                }
            }
            if (song > 0 || matKetNoi > 0) {
                khongAiTu = 0L;
            } else if (khongAiTu == 0L) {
                khongAiTu = bay;
            }
            if (sangTangLuc > 0L && bay >= sangTangLuc && !daHaBoss) {
                sangTangLuc = 0L;
                sk.add(new int[]{SK_SANG_TANG, tang + 1});
            }
            final int kq = trangThai(bay, batDau, phutToiDa, daHaBoss, song, matKetNoi, khongAiTu);
            if (kq != DANG) {
                ketQua = kq;
                ketThucLuc = bay;
                sk.add(new int[]{SK_KET_THUC, kq});
            }
            return sk;
        }

        /** Tang da qua khi ket thuc (thang = 5). */
        public int tangDaQua() {
            return ketQua == THANG ? SO_TANG : Math.max(0, tang - 1);
        }
    }
}
