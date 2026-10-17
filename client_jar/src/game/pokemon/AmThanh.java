package game.pokemon;

import game.model.Actor;
import game.model.Monster;
import game.model.Sound;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.util.Vector;

/**
 * Su kien am thanh cua ban jar: ten tieng (/am/s_&lt;ten&gt;.wav), uu tien, va chon nhac nen
 * (/am/n_&lt;ten&gt;.mid) theo loai ban do (HePoke.MAP) / boss gan / man tieu de. Tep sinh boi
 * j2me/tools/lam_am_thanh.py (Kenney CC0 + tieng/nhac tu tong hop, xem res/LICENSE-audio.txt).
 * Ban nhe (GOI=nhe) chi co mot phan tieng, khong co nhac: tep thieu thi im lang.
 * Moi ham deu an loi (am thanh khong bao gio lam hong game).
 */
public final class AmThanh {
    public static final int CLICK = 0, XAC_NHAN = 1, HUY = 2, MO = 3, DANH = 4, PHEP = 5, CHI_MANG = 6,
            HIEU_QUA = 7, IT_HIEU_QUA = 8, TRUOT = 9, BI_DANH = 10, CHET = 11, NHAT = 12, VANG = 13,
            DICH_CHUYEN = 14, HOI_MAU = 15, CANH_BAO = 16, LEN_CAP = 17, TIEN_HOA = 18, NHIEM_VU = 19, BOSS = 20;

    public static final String[] TEN = {
        "click", "xacnhan", "huy", "mo", "danh", "phep", "chimang", "hieuqua", "ithieuqua", "truot", "bidanh",
        "chet", "nhat", "vang", "dichchuyen", "hoimau", "canhbao", "lencap", "tienhoa", "nhiemvu", "boss"
    };
    /** Uu tien (cao thay duoc thap khi het cho phat): 0 giao dien/danh thuong .. 3 len cap/tien hoa. */
    public static final int[] UU_TIEN = {0, 1, 0, 0, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3};

    public static final int N_TIEU_DE = 0, N_THI_TRAN = 1, N_DUONG = 2, N_RUNG = 3, N_HANG = 4, N_TUYET = 5,
            N_SA_MAC = 6, N_DAU_CHIEN = 7, N_BOSS = 8;
    public static final String[] TEN_NHAC = {"tieude", "thitran", "duong", "rung", "hang", "tuyet", "samac", "dauchien", "boss"};

    /** Tick game lan cuoi phat tung tieng (chong don dap: chieu dien rong trung 5 quai). */
    private static final int[] lanCuoi = new int[TEN.length];
    private static int nhacMap = N_DUONG;
    private static boolean coBoss;

    private AmThanh() {
    }

    public static void phat(int id) {
        try {
            if (id < 0 || id >= TEN.length) {
                return;
            }
            int t = Canvas.gameTick;
            int d = t - lanCuoi[id];
            if (d < 0) {
                d += 10001;
            }
            if (d < 3 && lanCuoi[id] != 0) {
                return;
            }
            lanCuoi[id] = t == 0 ? 1 : t;
            Sound.me.phat(id);
        } catch (Throwable e) {
            // im lang
        }
    }

    /** Man tieu de / dang nhap / chon nhan vat. */
    public static void manTieuDe() {
        try {
            Sound.me.phatNhac(N_TIEU_DE);
        } catch (Throwable e) {
            // im lang
        }
    }

    /** Nhac theo ban do (GameScr.onMap): id cMap + ten (tim "tuyết", "sa mạc"...). */
    public static int nhacChoMap(int idMap, String ten) {
        String t = ten == null ? "" : ChuPokemon.thuong(ten);
        if (t.indexOf("tuyết") >= 0 || t.indexOf("băng giá") >= 0 || t.indexOf("núi băng") >= 0 || t.indexOf("hang băng") >= 0) {
            return N_TUYET;
        }
        if (t.indexOf("sa mạc") >= 0 || t.indexOf("hoang mạc") >= 0) {
            return N_SA_MAC;
        }
        switch (HePoke.loaiMap(idMap)) {
            case 't':
                return N_THI_TRAN;
            case 'r':
                return N_RUNG;
            case 'h':
                return N_HANG;
            case 's':
                return N_SA_MAC;
            case 'c':
                return N_DAU_CHIEN;
            default:
                if (t.indexOf("rừng") >= 0) {
                    return N_RUNG;
                }
                if (t.indexOf("hang") >= 0 || t.indexOf("động") >= 0) {
                    return N_HANG;
                }
                return N_DUONG;
        }
    }

    public static void vaoMap(int idMap, String ten) {
        try {
            nhacMap = nhacChoMap(idMap, ten);
            coBoss = false;
            Sound.me.phatNhac(nhacMap);
            phat(DICH_CHUYEN);
        } catch (Throwable e) {
            // im lang
        }
    }

    /**
     * Moi tick game (GameScr.update): moi giay xem co boss Pokemon con song gan nhan vat (trong
     * ~1,5 man hinh) -> nhac boss, het boss -> tra nhac map. Lan dau gap boss: tieng gam.
     */
    public static void capNhat() {
        try {
            if (Canvas.gameTick % 25 != 0) {
                return;
            }
            GameScr gs = Canvas.gameScr;
            if (gs == null || gs.mainChar == null) {
                return;
            }
            boolean thay = false;
            Vector ds = gs.actors;
            int n = ds.size();
            for (int i = 0; i < n && !thay; i++) {
                Object o = ds.elementAt(i);
                if (o instanceof Monster) {
                    Monster m = (Monster) o;
                    if (m.hp > 0 && HieuUngDanh.laBoss(m) && gan(m, gs.mainChar, Canvas.w * 3 / 2)) {
                        thay = true;
                    }
                }
            }
            if (thay != coBoss) {
                coBoss = thay;
                if (thay) {
                    phat(BOSS);
                }
                Sound.me.phatNhac(thay ? N_BOSS : nhacMap);
            }
        } catch (Throwable e) {
            // im lang
        }
    }

    private static boolean gan(Actor a, Actor b, int r) {
        int dx = a.x - b.x, dy = a.y - b.y;
        return dx > -r && dx < r && dy > -r && dy < r;
    }
}
