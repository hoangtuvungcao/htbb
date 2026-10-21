import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Sinh PHU KIEN TRANG BI pixel-art (ve tay = 0 byte anh ban quyen: moi hinh la mat na ASCII duoi day, to bong
 * + vien TU DONG) cho he ngoai hinh trang bi (docs/thiet-ke-game.md, server
 * com.htbb.pokemon.NgoaiHinh). Moi client (jar / Android / PC Java / Unity) doc CHUNG mot tep:
 *   &lt;Assets&gt;/Resources/poke/phukien.bytes   (DongGoiPokeJ2me chep sang j2me/res/pk/phukien.bin)
 * va ve theo cung mot quy tac (diem neo + lop truoc/sau than), nen bon ban giong nhau.
 *
 * Mat na: '#' = mau do hiem (tu to: hang tren cung sang, hang duoi toi), 'a' = mau diem (da quy), cac chu
 * w k r y g G u U m M = mau co dinh (trang, den, lua cam, lua vang, la, la toi, nuoc, nuoc nhat, do nam cham,
 * kim loai). Vien 1 px (mau vien cua do hiem) bao quanh moi pixel dac theo 4 huong.
 *
 * Dinh dang phukien.bytes (big-endian):
 * <pre>
 *   u8 'K', u8 phienBan = 2
 *   u8 soHiem (5): moi do hiem 5 mau rgb24: vien, toi, goc, sang, diem
 *   u8 soCoDinh (10): rgb24 (chi so bang mau 6..15)
 *   u8 soBac (11): rgb24 mau vang sang, UTF ten bac
 *   u8 soHiem lan UTF ten do hiem
 *   u8 soTrangPhuc: i16 xoayMau (do), u8 baoHoa %, u8 sang %, rgb24 phuMau, u8 phuMau %, UTF ten
 *   u16 soHinh: u8 o, u8 kieu, u8 huong (0 xuong, 1 len, 2 ngang - nhin sang TRAI; phai = lat guong),
 *               u8 lop (0 sau than, 1 truoc than), u8 neo (0 dinh dau, 1 dau, 2 co, 3 tay, 4 tam than),
 *               i8 dx, i8 dy (lech them, pixel hinh; huong ngang: dx duong = ve phia SAU lung),
 *               u8 rong, u8 cao, i8 ax, i8 ay (diem cua hinh dat trung diem neo),
 *               u8 nho (1 = ban cho loai nho), ceil(rong*cao/2) byte: chi so mau 4 bit (0 = trong, 1 vien, 2 toi,
 *               3 goc, 4 sang, 5 diem, 6.. co dinh)
 *   u16 soNgoaiLe: u16 dex, u8 co (1 khong do o co, 2 khong mu, 4 khong do cam, 8 khong sau lung)
 * </pre>
 *
 * Chay (o HTBB/): javac -d build/tools tools/pokemon/DongGoiSprite.java tools/pokemon/SinhPhuKien.java
 *                 java -cp build/tools SinhPhuKien [anh_xem_truoc.png]
 */
public final class SinhPhuKien {

    // ---- bang mau do hiem (vien, toi, goc, sang, diem) + bac trang bi: MOT NGUON tools/cauhinh/do_hiem.json
    // SINH TU tools/cauhinh/do_hiem.json: BAT DAU (khong sua tay, chay python3 tools/cauhinh/sinh_do_hiem.py)
    static final int[][] HIEM = {
        {0x3A4250, 0x9AA2B0, 0xD8DCE4, 0xFFFFFF, 0x8FA8C8},   // thuong
        {0x123A1C, 0x2A8A3E, 0x52C866, 0xC2F2C8, 0xFFFFFF},   // hiem
        {0x123060, 0x2A60B8, 0x48A0F8, 0xB8E0FF, 0xFFFFFF},   // quy
        {0x30104A, 0x6A2CA0, 0xA45CE8, 0xE0C0FF, 0xFFE070},   // su_thi
        {0x4A2006, 0xB45A10, 0xF08A28, 0xFFD8A0, 0xFFFFFF},   // huyen_thoai
        {0x4A3208, 0xC89018, 0xFFD040, 0xFFF8C8, 0xFFFFFF},   // trang_tri
    };
    static final String[] TEN_HIEM = {"Thường", "Hiếm", "Quý", "Sử thi", "Huyền thoại", "Trang trí"};
    static final int[] MAU_BAC = {0x9AA0A8, 0xC88048, 0xD0D8E0, 0xF8D030, 0xE4F4FF, 0x78D8F8, 0x52C866, 0xA45CE8, 0xF08A28, 0xF84868, 0xFFFFFF};
    static final String[] TEN_BAC = {"Sơ cấp", "Đồng", "Bạc", "Vàng", "Bạch kim", "Kim cương", "Chuyên gia", "Tinh anh", "Huyền thoại", "Thần thoại", "Tối thượng"};
    // SINH TU tools/cauhinh/do_hiem.json: KET THUC
    /** w k r y g G u U m M */
    static final int[] CO_DINH = {0xF8F8F8, 0x2A2A34, 0xF07828, 0xF8D048, 0x60B040, 0x2F6B2A, 0x4890F0, 0xB0E4FA,
        0xD83838, 0xB4B4C4};
    static final String CHU_CO_DINH = "wkrygGuUmM";
    /** Trang phuc doi mau: {xoay mau, bao hoa %, sang %, phu mau rgb, phu mau %}, ten. Khop NgoaiHinh.SO_TRANG_PHUC. */
    static final int[][] TRANG_PHUC = {
        {0, 110, 106, 0xFFD838, 30},
        {0, 55, 118, 0xFFF0F4, 30},
        {-25, 125, 104, 0xFF6020, 25},
        {0, 35, 112, 0xB8D0FF, 35},
        {-10, 100, 108, 0xFF4058, 25},
        {0, 90, 96, 0x30C060, 35},
        {0, 70, 80, 0x7038B0, 40},
        {0, 45, 116, 0xC8F0FF, 40},
    };
    static final String[] TEN_TRANG_PHUC = {"Sắc Pikachu", "Sắc Togepi", "Sắc Ho-Oh", "Sắc Lugia", "Sắc Latias",
        "Sắc Rayquaza", "Bóng đêm", "Băng tuyết"};

    static final int MU = 0, KHAN = 1, VONG = 2, TAY = 3, LUNG = 4, DA = 5, KINH = 6;
    static final int XUONG = 0, LEN = 1, NGANG = 2;
    static final int SAU = 0, TRUOC = 1;
    static final int N_DINH = 0, N_DAU = 1, N_CO = 2, N_TAY = 3, N_TAM = 4;

    static final class Hinh {
        int o, kieu, huong, lop, neo, dx, dy, ax, ay, rong, cao, nho;
        int[] px;
    }

    static final List<Hinh> DS = new ArrayList<Hinh>();

    /**
     * Them mot hinh. ax/ay tinh tren MAT NA (truoc khi them vien); vien lam hinh rong them 1 px moi phia.
     */
    static void them(final int o, final int kieu, final int huong, final int lop, final int neo, final int dx,
            final int dy, final int ax, final int ay, final int nho, final String... mat) {
        final int h0 = mat.length;
        int w0 = 0;
        for (final String s : mat) {
            w0 = Math.max(w0, s.length());
        }
        final int w = w0 + 2, h = h0 + 2;
        final int[] px = new int[w * h];
        final boolean[] dac = new boolean[w * h];
        for (int y = 0; y < h0; y++) {
            for (int x = 0; x < mat[y].length(); x++) {
                final char c = mat[y].charAt(x);
                if (c == '.' || c == ' ') {
                    continue;
                }
                final int i = (y + 1) * w + x + 1;
                dac[i] = true;
                if (c == '#') {
                    px[i] = 3;
                } else if (c == 'a') {
                    px[i] = 5;
                } else {
                    final int k = CHU_CO_DINH.indexOf(c);
                    if (k < 0) {
                        throw new IllegalArgumentException("chu la '" + c + "'");
                    }
                    px[i] = 6 + k;
                }
            }
        }
        // to bong mau do hiem: tren khong dac -> sang, duoi khong dac -> toi
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                final int i = y * w + x;
                if (px[i] != 3) {
                    continue;
                }
                if (!dac[i - w]) {
                    px[i] = 4;
                } else if (!dac[i + w]) {
                    px[i] = 2;
                }
            }
        }
        // vien 4 huong
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int i = y * w + x;
                if (dac[i]) {
                    continue;
                }
                if ((x > 0 && dac[i - 1]) || (x < w - 1 && dac[i + 1]) || (y > 0 && dac[i - w])
                        || (y < h - 1 && dac[i + w])) {
                    px[i] = 1;
                }
            }
        }
        final Hinh hi = new Hinh();
        hi.o = o;
        hi.kieu = kieu;
        hi.huong = huong;
        hi.lop = lop;
        hi.neo = neo;
        hi.dx = dx;
        hi.dy = dy;
        hi.ax = ax + 1;
        hi.ay = ay + 1;
        hi.rong = w;
        hi.cao = h;
        hi.px = px;
        hi.nho = nho;
        DS.add(hi);
    }

    /** Ca ba huong cung mot mat na. */
    static void ca3n(final int o, final int kieu, final int lop, final int neo, final int dx, final int dy,
            final int ax, final int ay, final int nho, final String... mat) {
        for (int hu = 0; hu < 3; hu++) {
            them(o, kieu, hu, lop, neo, dx, dy, ax, ay, nho, mat);
        }
    }

    static void khaiBao() {
        // Quy tac (ban 2, sau gop y "mieng bit mom"): phu kien NHO, de thuong nhu do Pokemon chinh thuc; KHONG
        // bao gio ve len mat: mu/no dat TREN dinh dau (chi chong 1 px), khan/vong o co (giua dau va tam than,
        // gan than), do cam o tay, ao choang / ruy bang sau than. Moi hinh co ban "nho" (n = 1) cho loai nho.
        // ================= MU (tren dinh dau) =================
        // 1 no nho
        ca3n(MU, 1, TRUOC, N_DINH, 0, 1, 2, 2, 0,
                "##.##",
                "##a##",
                "##.##");
        // 2 no to (no ruy bang Eevee)
        ca3n(MU, 2, TRUOC, N_DINH, 0, 1, 3, 3, 0,
                "##...##",
                "###.###",
                "###a###",
                "##...##");
        ca3n(MU, 2, TRUOC, N_DINH, 0, 1, 2, 2, 1,
                "##.##",
                "##a##",
                "##.##");
        // 3 hoa cai (lech ve mot ben)
        ca3n(MU, 3, TRUOC, N_DINH, -2, 1, 1, 2, 0,
                ".#.",
                "#a#",
                ".#.");
        // 4 mu luoi trai (kieu mu Pikachu)
        them(MU, 4, XUONG, TRUOC, N_DINH, 0, 1, 3, 2, 0,
                ".###.",
                "##a##",
                "#######");
        them(MU, 4, LEN, TRUOC, N_DINH, 0, 1, 2, 2, 0,
                ".###.",
                "#####",
                "#####");
        them(MU, 4, NGANG, TRUOC, N_DINH, 0, 1, 3, 2, 0,
                "..###.",
                ".##a##",
                "######");
        // 5 vuong mien
        them(MU, 5, XUONG, TRUOC, N_DINH, 0, 1, 2, 2, 0,
                "#.#.#",
                "#####",
                "#a#a#");
        them(MU, 5, LEN, TRUOC, N_DINH, 0, 1, 2, 2, 0,
                "#.#.#",
                "#####",
                "#####");
        them(MU, 5, NGANG, TRUOC, N_DINH, 0, 1, 1, 2, 0,
                "#.#",
                "###",
                "#a#");
        // 6 non tiec (thoi trang)
        ca3n(MU, 6, TRUOC, N_DINH, 0, 1, 1, 3, 0,
                ".a.",
                ".#.",
                "#a#",
                "###");

        // ================= KHAN (o co: chi huong XUONG ve truoc than; ngang / len ve SAU than) =================
        // 1 khan quang: dai ngang co, duoi lech mot ben (kieu khan Pokemon), khong nut o giua
        them(KHAN, 1, XUONG, TRUOC, N_CO, 0, 0, 3, 0, 0,
                "#######",
                "....a##",
                "......#");
        them(KHAN, 1, XUONG, TRUOC, N_CO, 0, 0, 2, 0, 1,
                "#####",
                "...a#");
        them(KHAN, 1, LEN, SAU, N_CO, 0, 0, 3, 0, 0,
                "#######",
                "##.....",
                "#......");
        them(KHAN, 1, NGANG, SAU, N_CO, 2, 0, 0, 0, 0,
                "###.",
                "..##",
                "...#");
        // 2 khan dai hai duoi bay
        them(KHAN, 2, XUONG, TRUOC, N_CO, 0, 0, 3, 0, 0,
                "#######",
                "....a##",
                ".....##",
                "......#");
        them(KHAN, 2, XUONG, TRUOC, N_CO, 0, 0, 2, 0, 1,
                "#####",
                "...a#",
                "....#");
        them(KHAN, 2, LEN, SAU, N_CO, 0, 0, 3, 0, 0,
                "#######",
                "##.....",
                "##.....",
                "#......");
        them(KHAN, 2, NGANG, SAU, N_CO, 2, 0, 0, 0, 0,
                "###...",
                "..####",
                "....##");

        // ================= VONG (o co) =================
        // 1 chuong nho
        them(VONG, 1, XUONG, TRUOC, N_CO, 0, 0, 2, 0, 0,
                "#####",
                ".yyy.",
                ".yky.");
        them(VONG, 1, XUONG, TRUOC, N_CO, 0, 0, 1, 0, 1,
                "###",
                "yyy",
                ".k.");
        them(VONG, 1, NGANG, SAU, N_CO, 1, 0, 0, 0, 0,
                "##",
                "yy");
        // 2 day chuyen mat da
        them(VONG, 2, XUONG, TRUOC, N_CO, 0, 0, 2, 0, 0,
                "#...#",
                ".#a#.",
                "..a..");
        them(VONG, 2, XUONG, TRUOC, N_CO, 0, 0, 1, 0, 1,
                "#.#",
                ".a.");

        // ================= TAY (do cam: mau co dinh, vien = mau do hiem) =================
        ca3n(TAY, 1, TRUOC, N_TAY, 0, 0, 2, 2, 0,
                ".kk.",
                "kkrk",
                "kryk",
                ".kk.");
        ca3n(TAY, 2, TRUOC, N_TAY, 0, 0, 2, 2, 0,
                "..G.",
                ".gg.",
                "gggg",
                ".gg.");
        ca3n(TAY, 3, TRUOC, N_TAY, 0, 0, 2, 1, 0,
                "www.",
                "wwww",
                ".www");
        ca3n(TAY, 4, TRUOC, N_TAY, 0, 0, 1, 2, 0,
                ".u.",
                "uUu",
                "uuu",
                ".u.");
        ca3n(TAY, 5, TRUOC, N_TAY, 0, 0, 2, 1, 0,
                "M..M",
                "m..m",
                "mmmm");

        // ================= LUNG (sau than; quay lung thi ve truoc) =================
        // 1 ruy bang sau lung
        them(LUNG, 1, XUONG, SAU, N_CO, 0, 1, 3, 1, 0,
                "##...##",
                "###a###",
                "##...##");
        them(LUNG, 1, LEN, TRUOC, N_CO, 0, 1, 3, 1, 0,
                "##...##",
                "###a###",
                "##.#.##",
                "...#...");
        them(LUNG, 1, NGANG, SAU, N_CO, 3, 1, 1, 1, 0,
                "##.",
                "#a#",
                "##.");
        // 2 ao choang
        them(LUNG, 2, XUONG, SAU, N_CO, 0, 0, 3, 0, 0,
                ".#####.",
                "#######",
                "#######",
                "#######",
                "#######");
        them(LUNG, 2, LEN, TRUOC, N_CO, 0, 0, 3, 0, 0,
                ".#a#a#.",
                "#######",
                "#######",
                "#######",
                "#.###.#");
        them(LUNG, 2, NGANG, SAU, N_CO, 1, 0, 0, 0, 0,
                "##...",
                "###..",
                "####.",
                "#####",
                ".####");
        // 3 canh thoi trang
        them(LUNG, 3, XUONG, SAU, N_CO, 0, 1, 4, 1, 0,
                "##.....##",
                "###...###",
                ".##...##.");
        them(LUNG, 3, LEN, TRUOC, N_CO, 0, 1, 4, 1, 0,
                "##.....##",
                "###.a.###",
                ".##...##.");
        them(LUNG, 3, NGANG, SAU, N_CO, 3, 0, 0, 1, 0,
                ".##",
                "###",
                ".##");
    }

    /**
     * Loai KHONG co co / khong hop ao choang (sau, giun, ca, cuc tron: CHI vong sang duoi chan; ran, chim bay: khong
     * ao choang...): bit 1 = khong ve do o
     * co, 2 = khong ve mu, 4 = khong ve do cam tay, 8 = khong ve sau lung. Con lai van co vong sang duoi chan.
     */
    static final int[][] NGOAI_LE = {
        // sau / ken / giun
        {10, 1 | 2 | 4 | 8}, {11, 1 | 2 | 4 | 8}, {13, 1 | 2 | 4 | 8}, {14, 1 | 2 | 4 | 8}, {265, 1 | 2 | 4 | 8}, {266, 1 | 2 | 4 | 8},
        {268, 1 | 2 | 4 | 8}, {290, 1 | 2 | 4 | 8}, {401, 1 | 2 | 4 | 8}, {412, 1 | 2 | 4 | 8}, {451, 1 | 8},
        // ca / sinh vat nuoc
        {118, 1 | 2 | 4 | 8}, {119, 1 | 2 | 4 | 8}, {129, 1 | 2 | 4 | 8}, {170, 1 | 2 | 4 | 8}, {171, 1 | 2 | 4 | 8}, {211, 1 | 2 | 4 | 8},
        {222, 1 | 2 | 4 | 8}, {223, 1 | 2 | 4 | 8}, {318, 1 | 2 | 4 | 8}, {319, 1 | 2 | 4 | 8}, {320, 1 | 2 | 4 | 8}, {321, 1 | 2 | 4 | 8},
        {339, 1 | 2 | 4 | 8}, {340, 1 | 2 | 4 | 8}, {349, 1 | 2 | 4 | 8}, {369, 1 | 2 | 4 | 8}, {370, 1 | 2 | 4 | 8}, {550, 1 | 2 | 4 | 8},
        {72, 1 | 8}, {73, 1 | 8}, {90, 1 | 2 | 4 | 8}, {91, 1 | 2 | 4 | 8}, {116, 1 | 2 | 4 | 8}, {117, 1 | 2 | 4 | 8}, {120, 1 | 2 | 4 | 8},
        {121, 1 | 2 | 4 | 8}, {138, 1 | 8}, {139, 1 | 8}, {140, 1 | 8}, {141, 1 | 8}, {367, 1 | 8}, {368, 1 | 8},
        // cuc tron / khong co (than = dau)
        {50, 1 | 2 | 4 | 8}, {51, 1 | 2 | 4 | 8}, {74, 1 | 4}, {81, 1 | 2 | 4 | 8}, {82, 1 | 2 | 4 | 8}, {88, 1 | 8}, {89, 1 | 8},
        {92, 1 | 4}, {93, 1}, {94, 1}, {100, 1 | 2 | 4 | 8}, {101, 1 | 2 | 4 | 8}, {109, 1 | 2 | 4 | 8}, {110, 1 | 2 | 4 | 8},
        {132, 1 | 2 | 4 | 8}, {137, 1 | 8}, {200, 1}, {201, 1 | 2 | 4 | 8}, {202, 1 | 4}, {204, 1 | 2 | 4 | 8}, {205, 1 | 2 | 4 | 8},
        {218, 1 | 2 | 4 | 8}, {219, 1 | 2 | 4 | 8}, {220, 1 | 4}, {299, 1 | 8}, {337, 1 | 2 | 4 | 8}, {338, 1 | 2 | 4 | 8},
        {343, 1 | 2 | 4 | 8}, {344, 1 | 4}, {351, 1}, {353, 1}, {354, 1}, {355, 1 | 4}, {358, 1 | 4}, {360, 1},
        {361, 1}, {362, 1 | 2 | 4 | 8}, {374, 1 | 2 | 4 | 8}, {375, 1 | 4}, {433, 1 | 4}, {436, 1 | 2 | 4 | 8}, {437, 1 | 4},
        {442, 1 | 4}, {479, 1 | 4}, {487, 8}, {599, 1 | 2 | 4 | 8}, {600, 1 | 2 | 4 | 8}, {601, 1 | 2 | 4 | 8}, {615, 1 | 2 | 4 | 8},
        {622, 4}, {703, 1 | 2 | 4 | 8}, {769, 1 | 2 | 4 | 8}, {781, 1 | 2 | 4 | 8},
        // ran / rong dai
        {23, 8}, {24, 8}, {95, 8}, {130, 8}, {147, 8}, {148, 8}, {208, 8}, {336, 8}, {350, 8}, {384, 8},
        // chim / con bay: khong ao choang
        {12, 8}, {15, 8}, {16, 8}, {17, 8}, {18, 8}, {21, 8}, {22, 8}, {41, 1 | 4 | 8}, {42, 1 | 4 | 8},
        {83, 8}, {84, 8}, {85, 8}, {142, 8}, {144, 8}, {145, 8}, {146, 8}, {149, 8}, {163, 8}, {164, 8},
        {169, 1 | 4 | 8}, {176, 8}, {177, 8}, {178, 8}, {198, 8}, {227, 8}, {249, 8}, {250, 8}, {276, 8}, {277, 8},
        {278, 8}, {279, 8}, {333, 8}, {334, 8}, {396, 8}, {397, 8}, {398, 8}, {425, 1 | 8}, {426, 1 | 8},
    };

    static void rgb(final DataOutputStream o, final int c) throws IOException {
        o.writeByte(c >> 16);
        o.writeByte(c >> 8);
        o.writeByte(c);
    }

    static byte[] ghi() throws IOException {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(bo);
        o.writeByte('K');
        o.writeByte(2);
        o.writeByte(HIEM.length);
        for (final int[] r : HIEM) {
            for (final int c : r) {
                rgb(o, c);
            }
        }
        o.writeByte(CO_DINH.length);
        for (final int c : CO_DINH) {
            rgb(o, c);
        }
        o.writeByte(MAU_BAC.length);
        for (int i = 0; i < MAU_BAC.length; i++) {
            rgb(o, MAU_BAC[i]);
            o.writeUTF(TEN_BAC[i]);
        }
        o.writeByte(TEN_HIEM.length);
        for (final String s : TEN_HIEM) {
            o.writeUTF(s);
        }
        o.writeByte(TRANG_PHUC.length);
        for (int i = 0; i < TRANG_PHUC.length; i++) {
            final int[] t = TRANG_PHUC[i];
            o.writeShort(t[0]);
            o.writeByte(t[1]);
            o.writeByte(t[2]);
            rgb(o, t[3]);
            o.writeByte(t[4]);
            o.writeUTF(TEN_TRANG_PHUC[i]);
        }
        o.writeShort(DS.size());
        for (final Hinh h : DS) {
            o.writeByte(h.o);
            o.writeByte(h.kieu);
            o.writeByte(h.huong);
            o.writeByte(h.lop);
            o.writeByte(h.neo);
            o.writeByte(h.dx);
            o.writeByte(h.dy);
            o.writeByte(h.rong);
            o.writeByte(h.cao);
            o.writeByte(h.ax);
            o.writeByte(h.ay);
            o.writeByte(h.nho);
            for (int i = 0; i < h.px.length; i += 2) {
                final int a = h.px[i];
                final int b = i + 1 < h.px.length ? h.px[i + 1] : 0;
                o.writeByte(a << 4 | b);
            }
        }
        o.writeShort(NGOAI_LE.length);
        for (final int[] e : NGOAI_LE) {
            o.writeShort(e[0]);
            o.writeByte(e[1]);
        }
        o.flush();
        return bo.toByteArray();
    }

    static int mau(final int chiSo, final int hiem) {
        if (chiSo == 0) {
            return 0;
        }
        if (chiSo <= 5) {
            return 0xFF000000 | HIEM[hiem][chiSo - 1];
        }
        return 0xFF000000 | CO_DINH[chiSo - 6];
    }

    /** Anh xem truoc: moi hang mot hinh (moi huong), moi cot mot do hiem, phong to 4 lan. */
    static void xemTruoc(final File f) throws IOException {
        final int o = 16, k = 4;
        final BufferedImage b = new BufferedImage(HIEM.length * o * k + 8, DS.size() * o * k / 2 + o * k, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < b.getWidth() * b.getHeight(); i++) {
            b.setRGB(i % b.getWidth(), i / b.getWidth(), ((i / b.getWidth() / (o * k / 2)) % 2 == 0) ? 0xFF2C3446 : 0xFF343E52);
        }
        for (int i = 0; i < DS.size(); i++) {
            final Hinh h = DS.get(i);
            for (int r = 0; r < HIEM.length; r++) {
                final int x0 = r * o * k + 4, y0 = i * o * k / 2;
                for (int y = 0; y < h.cao; y++) {
                    for (int x = 0; x < h.rong; x++) {
                        final int c = mau(h.px[y * h.rong + x], r);
                        if (c == 0) {
                            continue;
                        }
                        for (int yy = 0; yy < k / 2; yy++) {
                            for (int xx = 0; xx < k / 2; xx++) {
                                final int X = x0 + x * k / 2 + xx, Y = y0 + y * k / 2 + yy + 2;
                                if (X < b.getWidth() && Y < b.getHeight()) {
                                    b.setRGB(X, Y, c);
                                }
                            }
                        }
                    }
                }
            }
        }
        ImageIO.write(b, "png", f);
    }

    public static void main(final String[] a) throws Exception {
        khaiBao();
        final byte[] b = ghi();
        final File goc = new File(".").getCanonicalFile();
        for (final File ra : DongGoiSprite.thuMucRa(goc)) {
            ra.mkdirs();
            Files.write(new File(ra, "phukien.bytes").toPath(), b);
            System.out.println("-> " + new File(ra, "phukien.bytes") + " (" + b.length + " byte, " + DS.size() + " hinh)");
        }
        if (a.length > 0) {
            xemTruoc(new File(a[0]));
            System.out.println("-> xem truoc " + a[0]);
        }
    }
}
