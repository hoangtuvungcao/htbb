package game.pokemon;

import game.model.Char;
import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * So/chu bay kieu moi (thay chu bay cu cua GameScr.startFlyText): so co vien toi, nay len theo
 * duong cong roi roi nhe, nhap nhay mo dan cuoi doi. Mau:
 *   sat thuong gay ra trang, nhan vao do, chi mang vang to hon kem "!", hieu qua cao cam + nhan
 *   "Hiệu quả cao!", it hieu qua xam + "Kém hiệu quả", truot = nhan "Trượt", hoi mau xanh la "+HP",
 *   EXP xanh lam "+N EXP", xu vang kim + dong xu, len cap = bang "Lên cấp!".
 * 0.9.5: ve o LOP GIAO DIEN (NhanTheGioi.ve doi toa do the gioi -> man): so chu that NHO (~8 px logic) vien toi,
 * trang thuong / vang chi mang / xanh la hoi mau / xam it hieu qua; nhan khac he la THE NHO canh so ("Siêu hiệu
 * quả!" cam, "Không hiệu quả lắm" / "Không tác dụng" xam) - khong con vien to giua man. "+N EXP" khong bay giua man
 * nua: VienTrangThai.themExp (canh thanh EXP). May J2ME (khong ChuUI): bo chu so 3x5 tu ve nhu cu.
 * Hook: GameScr.startFlyText / updateFlyText / paintFocusActorInfo (NhanTheGioi.ve).
 */
public final class SoBay {
    // ------------------------------------------------------------------ kieu
    static final int TRANG = 0, DO = 1, VANG = 2, CAM = 3, XAM = 4, LUC = 5, LAM = 6, KIM = 7;
    private static final int[] MAU = {0xFFFFFF, 0xFF6A58, 0xFFD84A, 0xFFC07A, 0xAEB6C4, 0x7CF07A, 0x8CC8FF, 0xFFD23A};
    private static final int[] MAU_SANG = {0xFFFFFF, 0xFFB0A0, 0xFFFFC0, 0xFFD8A0, 0xF0F2F6, 0xD0FFC8, 0xD0EAFF, 0xFFF4B0};
    private static final int VIEN = 0x101420;

    /** Loai muc: so (chuoi chu so), nhan (chu tren vien mau), bang len cap. */
    private static final int SO = 0, NHAN = 1, BANG_LEN_CAP = 2;

    private static final int TOI_DA = 24;
    private static final int TUOI_SO = 20;
    private static final int TUOI_NHAN = 30;
    private static final int TUOI_LEN_CAP = 45;

    private static final int[] loai = new int[TOI_DA];
    private static final int[] kieu = new int[TOI_DA];
    private static final int[] co = new int[TOI_DA];
    private static final String[] chu = new String[TOI_DA];
    private static final int[] mauNhan = new int[TOI_DA];
    /** toa do x8 (diem co dinh), van toc x8 */
    private static final int[] x8 = new int[TOI_DA];
    private static final int[] y8 = new int[TOI_DA];
    private static final int[] vx8 = new int[TOI_DA];
    private static final int[] vy8 = new int[TOI_DA];
    private static final int[] tuoi = new int[TOI_DA];
    private static final int[] tuoiMax = new int[TOI_DA];
    private static final boolean[] xu = new boolean[TOI_DA];
    /** the nho canh so (khac he) */
    private static final String[] the = new String[TOI_DA];
    private static final int[] mauThe = new int[TOI_DA];
    private static final int[] luc = new int[TOI_DA];
    private static int soMuc;
    private static int hat = 12345;

    // goi y cho so sat thuong ke tiep (dat boi cho goi biet he / chi mang)
    private static int goiYHeSo = 100;
    private static boolean goiYChiMang;
    private static int goiYLoai = -1;
    private static int goiYLuc = -100;
    // chi mang den truoc so (AttackResult.startEff truoc startFlyText)
    private static int chiMangX, chiMangY, chiMangLuc = -100;
    private static int lenCapLuc = -100;

    private SoBay() {
    }

    private static int ngauNhien(int n) {
        hat = hat * 1103515245 + 12345;
        int v = (hat >>> 8) % n;
        return v < 0 ? -v : v;
    }

    private static int tickTu(int luc0) {
        int d = Canvas.gameTick - luc0;
        if (d < 0) {
            d += 10001;
        }
        return d;
    }

    /**
     * Goi y cho so sat thuong ke tiep (cung tick): heSo khac he (HePoke.heSo, 100 = thuong),
     * chiMang, loaiDon 0 vat ly / 1 dac biet (chon tieng danh), -1 khong biet.
     */
    public static void goiY(int heSo, boolean chiMang, int loaiDon) {
        goiYHeSo = heSo;
        goiYChiMang = chiMang;
        goiYLoai = loaiDon;
        goiYLuc = Canvas.gameTick;
    }

    // ================================================================== nhan chu bay

    /** Thay GameScr.startFlyText. true = da ve kieu moi (bo kieu cu). */
    public static boolean bat(String s, int mauCu, int x, int y) {
        try {
            if (s == null || s.length() == 0) {
                return true;
            }
            String t = s.toLowerCase();
            char c0 = t.charAt(0);
            // ---- sat thuong "-123" / "- 123"
            if (c0 == '-' && t.length() > 1) {
                String so = t.charAt(1) == ' ' ? t.substring(2) : t.substring(1);
                if (laSo(so)) {
                    soSatThuong(so, mauCu, x, y);
                    return true;
                }
            }
            if (t.equals("miss")) {
                themNhan("Trượt", MAU_KEM, x, y - 4, TUOI_NHAN);
                AmThanh.phat(AmThanh.TRUOT);
                return true;
            }
            if (t.equals("chi mang") || t.equals("baokich")) {
                danhDauChiMang(x, y);
                return true;
            }
            if (t.equals("hieu qua cao")) {
                // AttackResult.EFF_HIEU_QUA_CAO (5) - server da tinh khac he
                nangSoGan(x, y, CAM, SIEU, AmThanh.HIEU_QUA);
                return true;
            }
            if (t.equals("xuyen giap")) {
                themNhan("Xuyên giáp", 0x8E5CC8, x, y, 20);
                return true;
            }
            if (t.equals("level-up")) {
                if (tickTu(lenCapLuc) > 5) {
                    lenCapLuc = Canvas.gameTick;
                    int i = cho();
                    if (i >= 0) {
                        dat(i, BANG_LEN_CAP, KIM, 0, "Lên cấp!", x, y - 10, 0, -10, TUOI_LEN_CAP);
                    }
                }
                return true;
            }
            if (t.equals("hp+")) {
                themSo("+HP", LUC, 2, x, y, false);
                AmThanh.phat(AmThanh.HOI_MAU);
                return true;
            }
            if (t.equals("mp+")) {
                themSo("+PP", LAM, 2, x, y, false);
                return true;
            }
            if (c0 == '+' && t.length() > 1) {
                int k = 1;
                while (k < t.length() && t.charAt(k) >= '0' && t.charAt(k) <= '9') {
                    k++;
                }
                String so = t.substring(1, k);
                String duoi = t.substring(k).trim();
                if (so.length() > 0) {
                    if (duoi.equals("xp")) {
                        VienTrangThai.themExp(Integer.parseInt(so));
                        return true;
                    }
                    if (duoi.length() == 0) {
                        themSo(t, LUC, 2, x, y, false);   // hoi mau "+123"
                        return true;
                    }
                    if (duoi.equals("xu") || duoi.equals("vàng") || duoi.equals("poké xu")) {
                        themSo("+" + so, KIM, 2, x, y, true);
                        AmThanh.phat(AmThanh.VANG);
                        return true;
                    }
                    // nhat vat pham: nhan xanh (giu ten goc)
                    themNhan(s, 0x3E9A40, x, y, TUOI_NHAN);
                    AmThanh.phat(AmThanh.NHAT);
                    return true;
                }
            }
            if (c0 == 'h' && t.startsWith("hp-")) {
                themSo("-" + t.substring(3) + " HP", DO, 2, x, y, false);
                return true;
            }
            // chu khac: nhan trang
            themNhan(s, 0x4A5878, x, y, TUOI_NHAN);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean laSo(String s) {
        if (s.length() == 0) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean ganNhanVatChinh(int x, int y) {
        if (Canvas.gameScr == null) {
            return false;
        }
        Char mc = Canvas.gameScr.mainChar;
        if (mc == null) {
            return false;
        }
        int dx = x - mc.x, dy = y - mc.y;
        return dx > -14 && dx < 14 && dy > -60 && dy < 0;
    }

    private static void soSatThuong(String so, int mauCu, int x, int y) {
        boolean nhan = mauCu == 4 || ganNhanVatChinh(x, y);
        boolean coGoiY = tickTu(goiYLuc) <= 1;
        int heSo = coGoiY && !mayChuCoDon ? goiYHeSo : 100;
        boolean chiMang = coGoiY && goiYChiMang;
        if (tickTu(chiMangLuc) <= 2 && gan(chiMangX, chiMangY, x, y)) {
            chiMang = true;
            chiMangLuc = -100;
        }
        int k = TRANG;
        int coChu = 2;
        String hien = so;
        if (nhan) {
            k = DO;
        }
        if (heSo >= 150) {
            k = CAM;
        } else if (heSo <= 70) {
            k = XAM;
        }
        if (chiMang) {
            k = VANG;
            coChu = 3;
            hien = so + "!";
        }
        int i = themSo(hien, k, coChu, x, y, false);
        if (i >= 0 && heSo >= 150) {
            datThe(i, SIEU, MAU_SIEU);
        } else if (i >= 0 && heSo <= 70) {
            datThe(i, KEM, MAU_KEM);
        }
        // tieng
        if (chiMang) {
            AmThanh.phat(AmThanh.CHI_MANG);
        } else if (heSo >= 150) {
            AmThanh.phat(AmThanh.HIEU_QUA);
        } else if (nhan) {
            AmThanh.phat(AmThanh.BI_DANH);
        } else if (heSo <= 70) {
            AmThanh.phat(AmThanh.IT_HIEU_QUA);
        } else {
            AmThanh.phat(coGoiY && goiYLoai == 1 ? AmThanh.PHEP : AmThanh.DANH);
        }
        if (coGoiY) {
            goiYLuc = -100;
        }
    }

    private static boolean gan(int x0, int y0, int x1, int y1) {
        int dx = x1 - x0, dy = y1 - y0;
        return dx > -40 && dx < 40 && dy > -60 && dy < 60;
    }

    /** Chi mang den sau so: nang so vua hien (cung tick, gan do) thanh so vang; chua co thi nho. */
    private static void danhDauChiMang(int x, int y) {
        for (int i = 0; i < soMuc; i++) {
            if (loai[i] == SO && tuoi[i] <= 1 && kieu[i] == VANG && gan(x8[i] >> 3, y8[i] >> 3, x, y)) {
                return;   // so da la chi mang (goi y truoc)
            }
        }
        for (int i = 0; i < soMuc; i++) {
            if (loai[i] == SO && tuoi[i] <= 1 && kieu[i] != VANG && kieu[i] != LUC && kieu[i] != LAM && kieu[i] != KIM
                    && gan(x8[i] >> 3, y8[i] >> 3, x, y)) {
                kieu[i] = VANG;
                co[i] = 3;
                if (chu[i] != null && !chu[i].endsWith("!")) {
                    chu[i] = chu[i] + "!";
                }
                AmThanh.phat(AmThanh.CHI_MANG);
                return;
            }
        }
        chiMangX = x;
        chiMangY = y;
        chiMangLuc = Canvas.gameTick;
    }

    /** Nhan chu tren vien mau (thong bao boss...). */
    public static void nhan(String s, int mau, int x, int y) {
        themNhan(s, mau, x, y, 40);
    }

    /**
     * Server da gui co don (-95 POKE_DON) - tu do KHONG tu doan khac he o client nua.
     */
    public static boolean mayChuCoDon;

    /** Nang so vua hien gan (x, y) sang kieu k (+ nhan); khong thay so thi chi hien nhan. */
    private static boolean nangSoGan(int x, int y, int k, String nhanChu, int tieng) {
        for (int i = soMuc - 1; i >= 0; i--) {
            if (loai[i] == SO && tuoi[i] <= 3 && (kieu[i] == TRANG || kieu[i] == DO || kieu[i] == CAM || kieu[i] == XAM
                    || (kieu[i] == VANG && nhanChu != null)) && gan(x8[i] >> 3, y8[i] >> 3, x, y)) {
                if (kieu[i] == k && k != VANG && (nhanChu == null || nhanChu == the[i])) {
                    return true;   // da to mau + nhan (goi y client) roi
                }
                if (k == VANG) {
                    co[i] = 3;
                    if (chu[i] != null && !chu[i].endsWith("!")) {
                        chu[i] = chu[i] + "!";
                    }
                }
                kieu[i] = k;
                if (nhanChu != null) {
                    datThe(i, nhanChu, nhanChu == SIEU ? MAU_SIEU : MAU_KEM);
                }
                AmThanh.phat(tieng);
                return true;
            }
        }
        if (nhanChu != null) {
            themNhan(nhanChu, nhanChu == SIEU ? MAU_SIEU : MAU_KEM, x, y, TUOI_NHAN);
        }
        return false;
    }

    /** Chu the khac he (server quyet dinh qua co -95; client khong tu tinh). */
    static final String SIEU = "Siêu hiệu quả!", KEM = "Không hiệu quả lắm", VO_HIEU = "Không tác dụng";
    static final int MAU_SIEU = 0xFF9A3C, MAU_KEM = 0xB4BCC8;

    private static void datThe(int i, String s, int mau) {
        the[i] = s;
        mauThe[i] = mau;
    }

    /**
     * -95 POKE_DON (goi kem SAU goi danh cu, so da hien): to mau lai theo co server.
     * co: bit0 chi mang, bit1 ne, bit2 khac &gt;= x1,5, bit3 kem &lt;= x0,7, bit4 gan mien x0,3.
     */
    public static void donMayChu(int x, int y, int co, int dam) {
        mayChuCoDon = true;
        if ((co & 2) != 0) {
            // ne: so da hien la "-0"/MISS - them nhan neu chua co
            themNhan("Né!", MAU_KEM, x, y - 4, TUOI_NHAN);
            AmThanh.phat(AmThanh.TRUOT);
            return;
        }
        if ((co & 1) != 0) {
            nangSoGan(x, y, VANG, null, AmThanh.CHI_MANG);
        }
        if ((co & 4) != 0) {
            nangSoGan(x, y, (co & 1) != 0 ? VANG : CAM, SIEU, AmThanh.HIEU_QUA);
        } else if ((co & 24) != 0) {
            nangSoGan(x, y, (co & 1) != 0 ? VANG : XAM, (co & 16) != 0 ? VO_HIEU : KEM, AmThanh.IT_HIEU_QUA);
        }
    }

    private static int cho() {
        if (soMuc < TOI_DA) {
            return soMuc++;
        }
        // day: thay muc gia nhat
        int gia = 0;
        for (int i = 1; i < TOI_DA; i++) {
            if (tuoi[i] * 100 / tuoiMax[i] > tuoi[gia] * 100 / tuoiMax[gia]) {
                gia = i;
            }
        }
        return gia;
    }

    private static void dat(int i, int l, int k, int c, String s, int x, int y, int vx, int vy, int tMax) {
        loai[i] = l;
        kieu[i] = k;
        co[i] = c;
        chu[i] = s;
        x8[i] = x << 3;
        y8[i] = y << 3;
        vx8[i] = vx;
        vy8[i] = vy;
        tuoi[i] = 0;
        tuoiMax[i] = tMax;
        xu[i] = false;
        luc[i] = 0;
        the[i] = null;
    }

    private static int themSo(String s, int k, int c, int x, int y, boolean laXu) {
        // nhieu don lien tiep cung cho: so moi dat cao hon so con moi (khong de len nhau)
        int chong = 0;
        for (int j = 0; j < soMuc; j++) {
            if (loai[j] == SO && tuoi[j] < 8) {
                int dx = (x8[j] >> 3) - x, dy = (y8[j] >> 3) - y;
                if (dx > -24 && dx < 24 && dy > -30 && dy < 12) {
                    chong++;
                }
            }
        }
        int i = cho();
        if (i < 0) {
            return -1;
        }
        int lech = ngauNhien(11) - 5;
        int vx = (ngauNhien(2) == 0 ? -1 : 1) * (3 + ngauNhien(5));
        dat(i, SO, k, c, s, x + lech, y - (chong > 3 ? 3 : chong) * 6, vx, -18 - ngauNhien(5), TUOI_SO);
        xu[i] = laXu;
        return i;
    }

    private static void themNhan(String s, int mau, int x, int y, int tMax) {
        int i = cho();
        if (i < 0) {
            return;
        }
        dat(i, NHAN, TRANG, 0, s, x, y, 0, -8, tMax);
        mauNhan[i] = mau;
    }

    // ================================================================== cap nhat / ve

    public static void capNhat() {
        int j = 0;
        for (int i = 0; i < soMuc; i++) {
            tuoi[i]++;
            if (tuoi[i] >= tuoiMax[i]) {
                continue;
            }
            if (loai[i] == SO) {
                x8[i] += vx8[i];
                y8[i] += vy8[i];
                vy8[i] += 2;          // bay len nhanh roi cham dan (van troi len nhe toi luc tat)
                if (vy8[i] > -3) {
                    vy8[i] = -3;
                }
                vx8[i] = vx8[i] * 7 / 8;
            } else {
                y8[i] += vy8[i];
                vy8[i] = vy8[i] * 7 / 8;   // nhan: troi len cham dan roi dung
            }
            if (j != i) {
                chep(i, j);
            }
            j++;
        }
        for (int i = j; i < soMuc; i++) {
            chu[i] = null;
            the[i] = null;
        }
        soMuc = j;
    }

    private static void chep(int a, int b) {
        loai[b] = loai[a];
        kieu[b] = kieu[a];
        co[b] = co[a];
        chu[b] = chu[a];
        mauNhan[b] = mauNhan[a];
        x8[b] = x8[a];
        y8[b] = y8[a];
        vx8[b] = vx8[a];
        vy8[b] = vy8[a];
        tuoi[b] = tuoi[a];
        tuoiMax[b] = tuoiMax[a];
        xu[b] = xu[a];
        luc[b] = luc[a];
        the[b] = the[a];
        mauThe[b] = mauThe[a];
    }

    /**
     * Ve o LOP GIAO DIEN (NhanTheGioi.ve, sau lop the gioi): x8/y8 la toa do the gioi, doi ra man o day.
     */
    public static void ve(Graphics g) {
        boolean that = ChuUI.bo != null;
        for (int i = 0; i < soMuc; i++) {
            try {
                int t = tuoi[i];
                int con = tuoiMax[i] - t;
                // may J2ME: mo dan = nhap nhay o 6 tick cuoi; chu that: toi dan ve mau vien (veSoChu)
                if (!that && con < 6 && (t & 1) == 1) {
                    continue;
                }
                int x = NhanTheGioi.sx(x8[i] >> 3), y = NhanTheGioi.sy(y8[i] >> 3);
                if (x < -60 || x > Canvas.w + 60 || y < -20 || y > Canvas.h + 40) {
                    continue;
                }
                if (loai[i] == SO) {
                    int c = co[i];
                    if (t < 2) {
                        c++;   // "bat" ra to roi thu ve
                    }
                    conVe = con;
                    veSo(g, chu[i], kieu[i], c, x, y, xu[i]);
                    if (the[i] != null) {
                        veThe(g, i, c, x, y, con);
                    }
                } else if (loai[i] == NHAN) {
                    veNhan(g, chu[i], mauNhan[i], x, y, con);
                } else {
                    veLenCap(g, x, y, t);
                }
            } catch (Throwable e) {
                tuoi[i] = tuoiMax[i];
            }
        }
    }

    /** so tick con lai cua so dang ve (veSoChu toi dan) */
    private static int conVe = TUOI_SO;

    /** Co chu so (px logic) theo bac c: 2 thuong, 3 chi mang, +1 luc "bat" ra. */
    private static float coSo(int c) {
        return (ChuDe.dt() ? 6.1f : 5.5f) + c * 1.2f;
    }

    /** 4 tick cuoi: mau toi dan ve vien (thay mo dan - chu that khong co alpha). */
    private static int mo(int mau, int con) {
        return con >= 4 ? mau : GiaoDien.pha(mau, NhanTheGioi.VIEN, 4 - con, 5);
    }

    /** The khac he canh phai so (cung dong). */
    private static void veThe(Graphics g, int i, int c, int x, int y, int con) {
        String s = chu[i];
        float cs = coSo(c);
        int w = s == null ? 0 : NhanTheGioi.rong(s, cs, 2) + (xu[i] ? 10 : 0);
        float ct = ChuDe.dt() ? 6.8f : 6.0f;
        int h = NhanTheGioi.cao(cs), ht = NhanTheGioi.cao(ct);
        NhanTheGioi.chuVien(g, the[i], x + w / 2 + 3, y - h + (h - ht) / 2 + 1, ct, 1, mo(mauThe[i], con), NhanTheGioi.VIEN, 0);
    }

    private static void veNhan(Graphics g, String s, int mau, int x, int y, int con) {
        if (ChuUI.bo == null) {
            Font f = Font.borderFont;
            if (f == null) {
                return;
            }
            int w = f.getWidth(s) + 10;
            int h = f.getHeight() + 2;
            GiaoDien.vien(g, x - w / 2, y - h, w, h, mau, s);
            return;
        }
        // nhan chu nho vien toi (mau nhat de doc tren nen ban do)
        float c = ChuDe.dt() ? 7.0f : 6.4f;
        int sang = GiaoDien.doSang(mau) < 150 ? GiaoDien.pha(mau, 0xFFFFFF, 2, 5) : mau;
        NhanTheGioi.chuVien(g, s, x, y - NhanTheGioi.cao(c), c, 1, mo(sang, con), NhanTheGioi.VIEN, 2);
    }

    private static void veLenCap(Graphics g, int x, int y, int t) {
        String s = "Lên cấp!";
        if (ChuUI.bo != null) {
            float c = ChuDe.dt() ? 9.0f : 8.2f;
            int h = NhanTheGioi.cao(c);
            int w = NhanTheGioi.rong(s, c, 2);
            int mau = (t / 3) % 2 == 0 ? 0xFFD23A : 0xFFF1A0;
            g.setColor(mau);
            int r = w / 2 + 3 + (t % 6) / 2;
            g.drawLine(x - r, y - h / 2, x - r - 5, y - h / 2);
            g.drawLine(x + r, y - h / 2, x + r + 5, y - h / 2);
            NhanTheGioi.chuVien(g, s, x, y - h, c, 2, mau, NhanTheGioi.VIEN, 2);
            return;
        }
        Font f = Font.borderFont;
        if (f == null) {
            return;
        }
        int w = f.getWidth(s) + 16;
        int h = f.getHeight() + 6;
        // tia sang hai ben nhap nhay
        int mau = (t / 3) % 2 == 0 ? GiaoDien.VANG : GiaoDien.VANG_NHAT;
        g.setColor(mau);
        int r = w / 2 + 4 + (t % 6);
        g.drawLine(x - r, y - h / 2, x - r - 6, y - h / 2);
        g.drawLine(x + r, y - h / 2, x + r + 6, y - h / 2);
        g.drawLine(x - r + 2, y - h, x - r - 3, y - h - 5);
        g.drawLine(x + r - 2, y - h, x + r + 3, y - h - 5);
        GiaoDien.vien(g, x - w / 2, y - h, w, h, (t / 4) % 2 == 0 ? 0xF0A800 : 0xFFC820, s);
    }

    // ------------------------------------------------------------------ bo chu so 3x5

    /** Thu tu ky tu trong anh. */
    private static final String KY_TU = "0123456789+-!EXPHM ";
    private static final int[] HINH = {
        0x7B6F, 0x2C97, 0x73E7, 0x73CF, 0x5BC9, 0x79CF, 0x79EF, 0x7292, 0x7BEF, 0x7BCF,
        0x05D0, 0x01C0, 0x2482, 0x79E7, 0x5AAD, 0x7BE4, 0x5BED, 0x5FED, 0x0000
    };
    /** anh[kieu * 3 + (co - 2)] */
    private static final Image[] anh = new Image[8 * 3];

    private static Image anhKieu(int k, int c) {
        if (c < 2) {
            c = 2;
        }
        if (c > 4) {
            c = 4;
        }
        int idx = k * 3 + (c - 2);
        if (anh[idx] != null) {
            return anh[idx];
        }
        int gw = 3 * c + 2, gh = 5 * c + 2;
        int n = KY_TU.length();
        int W = gw * n;
        int[] px = new int[W * gh];
        int mau = 0xFF000000 | MAU[k];
        int sang = 0xFF000000 | MAU_SANG[k];
        int vien = 0xFF000000 | VIEN;
        for (int g = 0; g < n; g++) {
            int bit = HINH[g];
            int x0 = g * gw;
            // vien: moi diem cua chu to ra 1 px
            for (int r = 0; r < 5; r++) {
                for (int q = 0; q < 3; q++) {
                    if ((bit >> (14 - (r * 3 + q)) & 1) == 0) {
                        continue;
                    }
                    for (int yy = r * c; yy < r * c + c + 2; yy++) {
                        for (int xx = q * c; xx < q * c + c + 2; xx++) {
                            px[yy * W + x0 + xx] = vien;
                        }
                    }
                }
            }
            for (int r = 0; r < 5; r++) {
                for (int q = 0; q < 3; q++) {
                    if ((bit >> (14 - (r * 3 + q)) & 1) == 0) {
                        continue;
                    }
                    for (int yy = 0; yy < c; yy++) {
                        // nua tren sang hon (bong) -> chu noi
                        int m = (r * c + yy) < (5 * c) / 2 ? sang : mau;
                        for (int xx = 0; xx < c; xx++) {
                            px[(r * c + yy + 1) * W + x0 + q * c + xx + 1] = m;
                        }
                    }
                }
            }
        }
        anh[idx] = Image.createRGBImage(px, W, gh, true);
        return anh[idx];
    }

    private static int rongChu(String s, int c) {
        int gw = 3 * c + 2;
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            w += s.charAt(i) == ' ' ? c + 1 : gw - 1 + (c > 2 ? 1 : 0);
        }
        return w;
    }

    private static void veSo(Graphics g, String s, int k, int c, int x, int y, boolean laXu) {
        if (ChuUI.bo != null) {
            veSoChu(g, s, k, c, x, y, laXu);
            return;
        }
        if (c > 4) {
            c = 4;
        }
        Image img = anhKieu(k, c);
        int gw = 3 * c + 2, gh = 5 * c + 2;
        int w = rongChu(s, c) + (laXu ? 9 : 0);
        int xx = x - w / 2;
        int yy = y - gh;
        if (laXu) {
            // dong xu nho
            g.setColor(VIEN);
            g.fillArc(xx, yy + gh / 2 - 4, 9, 9, 0, 360);
            g.setColor(0xF0B400);
            g.fillArc(xx + 1, yy + gh / 2 - 3, 7, 7, 0, 360);
            g.setColor(0xFFF0A0);
            g.fillRect(xx + 3, yy + gh / 2 - 2, 2, 3);
            xx += 10;
        }
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == ' ') {
                xx += c + 1;
                continue;
            }
            int gi = KY_TU.indexOf(ch >= 'a' && ch <= 'z' ? (char) (ch - 32) : ch);
            if (gi < 0) {
                gi = KY_TU.length() - 1;
            }
            g.drawRegion(img, gi * gw, 0, gw, gh, 0, xx, yy, Graphics.TOP | Graphics.LEFT);
            xx += gw - 1 + (c > 2 ? 1 : 0);
        }
    }

    /** So chu that nho vien toi; x = giua, y = day. */
    private static void veSoChu(Graphics g, String s, int k, int c, int x, int y, boolean laXu) {
        float cs = coSo(c);
        int w = NhanTheGioi.rong(s, cs, 2) + (laXu ? 10 : 0);
        int h = NhanTheGioi.cao(cs);
        int xx = x - w / 2, yy = y - h;
        int con = conVe;
        if (laXu) {
            int cy = yy + h / 2;
            g.setColor(NhanTheGioi.VIEN);
            g.fillArc(xx, cy - 4, 9, 9, 0, 360);
            g.setColor(0xF0B400);
            g.fillArc(xx + 1, cy - 3, 7, 7, 0, 360);
            g.setColor(0xFFF0A0);
            g.fillRect(xx + 3, cy - 2, 2, 3);
            xx += 10;
        }
        NhanTheGioi.chuVien(g, s, xx, yy, cs, 2, mo(MAU[k], con), NhanTheGioi.VIEN, 0);
    }
}
