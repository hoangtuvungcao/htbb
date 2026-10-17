package game.pokemon;

/**
 * Du lieu khung hinh cua 1 loai Pokemon - ban J2ME cua ClientUnity PokeDuLieu.cs (KPAH,
 * docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 7.1). Dinh dang (big-endian):
 * <pre>
 *   u8 phienBan = 1 hoac 2, u8 soDongTac = 6
 *   lap soDongTac lan: u16 rongKhung, u16 caoKhung, u8 soKhung, u8 thoiLuong[soKhung],
 *                      u16 yAtlas, [phien ban 2: u16 xAtlas], i8 dxChan[4], i8 dyChan[4]
 *   u8 coDongTacThat[6]
 * </pre>
 * Ban jar dung CHINH dinh dang nay cho tep res/pk/&lt;khoa&gt;.dl (sinh boi
 * j2me/tools/DongGoiPokeJ2me.java: khung da cat sat hinh, bot khung, moi dong tac mot anh rieng).
 * Sau coDongTacThat, tep .dl cua jar co them phan mo rong u8 anhDongTac[6] (dong tac i dung anh
 * &lt;khoa&gt;_&lt;anhDongTac[i]&gt;.png), roi u16 caoTrenGoc (= caoKhung/2 + dyChan[0] cua dong tac dung
 * TRUOC khi cat khung, de dat ten tren dau giong Unity). Tep _dl cua Unity khong co phan nay:
 * anhDongTac[i] = i, caoTrenGoc = 0 (tinh tu khung).
 *
 * Lop nay CHI dung byte[] (khong lcdui) de kiem thu duoc bang JDK thuong
 * (j2me/tools/KiemPokeDuLieu.java doc het Resources/poke/*_dl.bytes cua KPAH).
 */
public class PokeDuLieu {
    public static final int DUNG = 0;
    public static final int CHAY = 1;
    public static final int DANH_GAN = 2;
    public static final int DANH_XA = 3;
    public static final int TRUNG_DON = 4;
    public static final int GUC_NGA = 5;
    public static final int SO_DONG_TAC = 6;

    /** Canvas.run cua ban J2ME ngu 40 ms moi khung: gameTick tang 25 lan/giay (Unity: 50). */
    public static final int TICK_MOI_GIAY = 25;

    /** Don vi thoiLuong trong tep: 1/60 giay (AnimData.xml cua PMD). */
    public static final int DON_VI_THOI_LUONG = 60;

    public int phienBan;
    public PokeDongTac[] dongTac;
    public boolean[] coThat = new boolean[SO_DONG_TAC];
    /** Phan mo rong cua ban jar: so anh cua tung dong tac (dong tac giong het nhau dung chung anh). */
    public int[] anhDongTac = new int[SO_DONG_TAC];
    /** Phan mo rong cua ban jar: do cao dinh khung goc (chua cat) tren diem chan; 0 = khong co. */
    public int caoTrenGoc;

    private static int u8(byte[] b, int p) {
        return b[p] & 0xFF;
    }

    private static int u16(byte[] b, int p) {
        return ((b[p] & 0xFF) << 8) | (b[p + 1] & 0xFF);
    }

    /** Doc du lieu; tra ve null neu tep hong (khong nem ngoai le). */
    public static PokeDuLieu doc(byte[] b) {
        if (b == null || b.length < 2) {
            return null;
        }
        try {
            int p = 0;
            PokeDuLieu kq = new PokeDuLieu();
            kq.phienBan = u8(b, p++);
            if (kq.phienBan != 1 && kq.phienBan != 2) {
                return null;
            }
            int soDongTac = u8(b, p++);
            if (soDongTac <= 0) {
                return null;
            }
            kq.dongTac = new PokeDongTac[soDongTac];
            for (int i = 0; i < soDongTac; i++) {
                PokeDongTac d = new PokeDongTac();
                d.rongKhung = u16(b, p);
                p += 2;
                d.caoKhung = u16(b, p);
                p += 2;
                d.soKhung = u8(b, p++);
                if (d.soKhung <= 0 || d.rongKhung <= 0 || d.caoKhung <= 0) {
                    return null;
                }
                d.thoiLuong = new int[d.soKhung];
                d.tongThoiLuong = 0;
                for (int k = 0; k < d.soKhung; k++) {
                    int t = u8(b, p++);
                    if (t <= 0) {
                        t = 1;
                    }
                    d.thoiLuong[k] = t;
                    d.tongThoiLuong += t;
                }
                d.yAtlas = u16(b, p);
                p += 2;
                if (kq.phienBan >= 2) {
                    d.xAtlas = u16(b, p);
                    p += 2;
                }
                for (int h = 0; h < 4; h++) {
                    d.dxChan[h] = b[p++];
                }
                for (int h = 0; h < 4; h++) {
                    d.dyChan[h] = b[p++];
                }
                kq.dongTac[i] = d;
            }
            for (int i = 0; i < SO_DONG_TAC; i++) {
                // Phan coDongTacThat la phu: tep thieu phan nay van dung duoc.
                kq.coThat[i] = p < b.length && b[p++] != 0;
            }
            for (int i = 0; i < SO_DONG_TAC; i++) {
                int a = i;
                if (p < b.length) {
                    a = u8(b, p++);
                }
                if (a >= soDongTac) {
                    a = i < soDongTac ? i : 0;
                }
                kq.anhDongTac[i] = a;
            }
            if (p + 2 <= b.length) {
                kq.caoTrenGoc = u16(b, p);
                p += 2;
            }
            return kq;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /** Dong tac theo so; dong tac thieu trong tep thi dung dong tac 0 (dung). */
    public PokeDongTac layDongTac(int so) {
        if (so < 0 || so >= dongTac.length) {
            so = 0;
        }
        return dongTac[so];
    }

    /** So anh can nap cho dong tac so (xem anhDongTac). */
    public int anhCua(int so) {
        if (so < 0 || so >= dongTac.length) {
            so = 0;
        }
        return so < SO_DONG_TAC ? anhDongTac[so] : so;
    }

    /** Doi so tick game (25/giay o ban jar) sang don vi 1/60 giay. */
    public static int doiTick(int tickGame) {
        if (tickGame < 0) {
            tickGame = 0;
        }
        return tickGame * DON_VI_THOI_LUONG / TICK_MOI_GIAY;
    }

    /** Dong tac co lap lai khong. Trung don va guc nga dung lai o khung cuoi. */
    public static boolean laDongTacLap(int so) {
        return so != TRUNG_DON && so != GUC_NGA;
    }
}
