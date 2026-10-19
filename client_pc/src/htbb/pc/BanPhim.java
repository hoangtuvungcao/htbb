package htbb.pc;

import game.render.Canvas;
import game.render.screen.GameScr;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Properties;

/**
 * Bàn phím PC -> phím J2ME của code game (chạy trên luồng sự kiện NenPc).
 *
 * Trong màn chơi (không hộp thoại / menu): WASD + mũi tên đi, Space đánh (phím 5), 1..5 = ô chiêu 1,3,5,7,9,
 * Q / E = bình HP / MP (ô 7 / 9), Tab đổi mục tiêu, ENTER = hành động ngữ cảnh (tiếp lời thoại, nói chuyện NPC,
 * đánh, nhặt, câu cá - HanhDong.nguCanh), T hoặc "/" mở chat (Enter gửi khi đang gõ), Esc menu (phím mềm trái),
 * R đổi bộ chiêu (phím 0), M menu nhanh (phím #), P tổ đội (phím *). F1 trang Phím tắt, F2/F3 phím mềm phải/trái,
 * F10 cài đặt, F11 toàn màn, F12 chụp màn. KHÔNG thu phóng (vùng nhìn cố định). Đổi phím trong Cài đặt (F10)
 * -> lưu cai_dat.properties (phim.<ma>=mã phím Java).
 * Đang gõ chữ (ô nhập của game đang chọn): mọi chữ (cả tiếng Việt từ bộ gõ) vào ô nhập, Ctrl+V dán.
 */
public final class BanPhim {
    // ---- hành động đổi được phím ----
    public static final String[] MA = {"len", "xuong", "trai", "phai", "danh", "chieu1", "chieu2", "chieu3", "chieu4",
        "chieu5", "binh_hp", "binh_mp", "muc_tieu", "chat", "menu", "doi_bo_chieu", "menu_nhanh", "to_doi",
        "cai_dat", "toan_man", "chup_man"};
    public static final String[] TEN = {"Đi lên", "Đi xuống", "Sang trái", "Sang phải", "Đánh (ô giữa)", "Chiêu 1",
        "Chiêu 2", "Chiêu 3", "Chiêu 4", "Chiêu 5", "Bình HP", "Bình MP", "Đổi mục tiêu", "Chat", "Menu / Quay lại",
        "Đổi bộ chiêu", "Menu nhanh", "Tổ đội", "Cài đặt", "Toàn màn hình", "Chụp màn hình"};
    public static final int[] MAC_DINH = {KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE,
        KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_Q, KeyEvent.VK_E,
        KeyEvent.VK_TAB, KeyEvent.VK_T, KeyEvent.VK_ESCAPE, KeyEvent.VK_R, KeyEvent.VK_M, KeyEvent.VK_P,
        KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
    public static final int LEN = 0, XUONG = 1, TRAI = 2, PHAI = 3, DANH = 4, CHIEU1 = 5, BINH_HP = 10, BINH_MP = 11,
        MUC_TIEU = 12, CHAT = 13, MENU = 14, DOI_BO = 15, MENU_NHANH = 16, TO_DOI = 17, CAI_DAT = 18, TOAN_MAN = 19,
        CHUP = 20;
    public static final int[] PHIM = MAC_DINH.clone();
    /** ô chiêu 1..5 -> phím J2ME (nhãn trên ô = phím PC) */
    private static final int[] O_CHIEU = {49, 51, 53, 55, 57};

    /** phím Java đang giữ -> mã J2ME đã gửi lúc nhấn (thả gửi đúng mã đó) */
    private static final HashMap<Integer, Integer> DANG_GIU = new HashMap<Integer, Integer>();
    /** khung cuối cùng có ô nhập đang chọn được vẽ */
    private static volatile long khungONhap = -100;
    private static volatile boolean oNhapSo;

    private BanPhim() {
    }

    // ---------------------------------------------------------------- lưu / nạp phím
    static void nap(Properties p) {
        for (int i = 0; i < MA.length; i++) {
            try {
                String v = p.getProperty("phim." + MA[i]);
                PHIM[i] = v == null ? MAC_DINH[i] : Integer.parseInt(v.trim());
            } catch (Exception e) {
                PHIM[i] = MAC_DINH[i];
            }
        }
    }

    static void luu(Properties p) {
        for (int i = 0; i < MA.length; i++) {
            if (PHIM[i] != MAC_DINH[i]) p.setProperty("phim." + MA[i], String.valueOf(PHIM[i]));
        }
    }

    public static String tenPhim(int vk) {
        if (vk == 0) return "—";
        if (vk == KeyEvent.VK_SPACE) return "Space";
        if (vk == KeyEvent.VK_ESCAPE) return "Esc";
        if (vk == KeyEvent.VK_ENTER) return "Enter";
        if (vk == KeyEvent.VK_TAB) return "Tab";
        String s = KeyEvent.getKeyText(vk);
        return s == null || s.length() == 0 ? "#" + vk : s;
    }

    /** Nhãn phím PC trên ô chiêu thứ k (0..4) - port-j2me.py thay nhãn "1,3,5,7,9" của bản jar. */
    public static String nhanO(int k) {
        if (k < 0 || k > 4) return "";
        String s = tenPhim(PHIM[CHIEU1 + k]);
        return s.length() > 3 ? s.substring(0, 3) : s;
    }

    // ---------------------------------------------------------------- trạng thái gõ chữ
    /** TField (port-j2me.py) gọi khi vẽ ô nhập đang chọn. */
    public static void coONhap(boolean chiSo) {
        khungONhap = javax.microedition.lcdui.NenPc.soKhung;
        oNhapSo = chiSo;
    }

    /** Đang gõ chữ vào ô nhập của game (ô đang chọn vừa được vẽ, hoặc chat). */
    public static boolean dangGo() {
        if (Canvas.gameScr != null && Canvas.currentScreen == Canvas.gameScr && Canvas.gameScr.chatMode) return true;
        if (Canvas.currentDialog != null && !(Canvas.currentDialog instanceof game.model.InputDlg)) return false; // hộp thoại đè lên ô nhập
        // màn có ô nhập + dòng chọn (Tạo nhân vật: Tên / Pokémon / Vùng; Đăng nhập: ô Nhớ mật khẩu): chữ luôn là chữ
        // (màn tự đưa chữ về ô nhập), mũi tên / Tab / Enter / Esc vẫn điều khiển
        if (Canvas.currentDialog == null && !Canvas.menu.showMenu && (Canvas.currentScreen instanceof game.render.screen.CreateCharScr
                || Canvas.currentScreen instanceof game.render.screen.LoginScr)) return true;
        return javax.microedition.lcdui.NenPc.soKhung - khungONhap <= 2;
    }

    /** Màn chơi đang điều khiển nhân vật (không hộp thoại / menu / chat). */
    public static boolean trongTheGioi() {
        GameScr gs = Canvas.gameScr;
        return gs != null && Canvas.currentScreen == gs && Canvas.currentDialog == null && !Canvas.menu.showMenu
                && !gs.chatMode && !Canvas.isLoad && !game.render.screen.TuDong.bangDangMo();
    }

    static int hanhDong(int vk) {
        for (int i = 0; i < PHIM.length; i++) {
            if (PHIM[i] == vk && vk != 0) return i;
        }
        return -1;
    }

    // ---------------------------------------------------------------- xử lý (luồng sự kiện)
    private static void nhan(int vk, int ma) {
        if (Canvas.instance == null) return;
        DANG_GIU.put(vk, ma);
        Canvas.instance.keyPressed(ma);
    }

    private static void go(int ma) {
        if (Canvas.instance == null) return;
        Canvas.instance.keyPressed(ma);
        Canvas.instance.keyReleased(ma);
    }

    /** Nhấn phím (lap = phím tự lặp khi giữ). */
    static void nhanPhim(int vk, int mod, boolean lap) {
        if (Canvas.instance == null || Canvas.isLoad) return;
        boolean diChuyen = vk == KeyEvent.VK_UP || vk == KeyEvent.VK_DOWN || vk == KeyEvent.VK_LEFT || vk == KeyEvent.VK_RIGHT;
        if (vk == KeyEvent.VK_F1) {
            if (!lap) {
                CaiDat.goiYMo = !CaiDat.goiYMo;   // F1: bật / tắt dải phím tắt
                LopPhu.moGoiY();
            }
            return;
        }
        if (vk == KeyEvent.VK_F3) {
            if (!lap) go(-6);   // phím mềm trái (hiếm dùng: Esc trong màn chơi cũng là phím này)
            return;
        }
        if (vk == KeyEvent.VK_F9 && ManHinh.GHI_NHAP) {
            NhatKy.ghi("trang thai: " + TrangThai.mo());
            return;
        }
        if (vk == KeyEvent.VK_F2) {
            if (!lap) go(-7);
            return;
        }
        if (dangGo()) {
            switch (vk) {
                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_DELETE:
                    go(-8);
                    return;
                case KeyEvent.VK_ENTER:
                    if (!lap) go(-5);
                    return;
                case KeyEvent.VK_ESCAPE:
                    if (lap) return;
                    if (Canvas.gameScr != null && Canvas.currentScreen == Canvas.gameScr && Canvas.gameScr.chatMode) go(-6);
                    else go(-7);
                    return;
                case KeyEvent.VK_LEFT:
                    go(-3);
                    return;
                case KeyEvent.VK_RIGHT:
                    go(-4);
                    return;
                case KeyEvent.VK_UP:
                    go(-1);
                    return;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_TAB:
                    go(-2);
                    return;
                case KeyEvent.VK_V:
                case KeyEvent.VK_INSERT:
                    if ((mod & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0
                            || vk == KeyEvent.VK_INSERT && (mod & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                        dan();   // Ctrl+V / Shift+Insert: dán vào ô nhập
                    }
                    return;
                default:
                    return; // chữ đi qua KEY_TYPED / bộ gõ
            }
        }
        boolean theGioi = trongTheGioi();
        int hd = hanhDong(vk);
        if (theGioi && !lap && vk == KeyEvent.VK_ENTER && hd != CHAT) {
            // Enter = PHÍM HÀNH ĐỘNG NGỮ CẢNH (kiểu NRO): tiếp lời thoại / nói chuyện NPC / đánh / nhặt / câu cá
            hanhDongNguCanh(vk);
            return;
        }
        if (theGioi && !lap && vk == KeyEvent.VK_SLASH && hd < 0) {
            moChat();   // "/" mở chat (gõ lệnh)
            return;
        }
        if (theGioi) {
            if (lap && (diChuyen || hd == LEN || hd == XUONG || hd == TRAI || hd == PHAI)) return; // đang giữ
            if (diChuyen) {
                nhan(vk, vk == KeyEvent.VK_UP ? -1 : vk == KeyEvent.VK_DOWN ? -2 : vk == KeyEvent.VK_LEFT ? -3 : -4);
                return;
            }
            if (lap && hd != DANH) return;
            switch (hd) {
                case LEN: nhan(vk, -1); return;
                case XUONG: nhan(vk, -2); return;
                case TRAI: nhan(vk, -3); return;
                case PHAI: nhan(vk, -4); return;
                case DANH: nhan(vk, 53); return;
                case BINH_HP: nhan(vk, 55); return;
                case BINH_MP: nhan(vk, 57); return;
                case DOI_BO: nhan(vk, 48); return;
                case MENU_NHANH: nhan(vk, 35); return;
                case TO_DOI: nhan(vk, 42); return;
                case MENU: nhan(vk, -6); return;
                case MUC_TIEU:
                    try {
                        Canvas.gameScr.changeToNextFocusActor((mod & KeyEvent.SHIFT_DOWN_MASK) != 0);
                    } catch (Throwable e) {
                        NhatKy.loi("doi muc tieu", e);
                    }
                    return;
                case CHAT:
                    moChat();
                    return;
                default:
                    break;
            }
            if (hd >= CHIEU1 && hd < CHIEU1 + 5) {
                nhan(vk, O_CHIEU[hd - CHIEU1]);
                return;
            }
            if (vk >= KeyEvent.VK_0 && vk <= KeyEvent.VK_9) {
                nhan(vk, 48 + vk - KeyEvent.VK_0);
            }
            return;
        }
        // màn khác / menu / hộp thoại: điều hướng
        int ma = 0;
        if (vk == KeyEvent.VK_UP || hd == LEN) ma = -1;
        else if (vk == KeyEvent.VK_DOWN || hd == XUONG) ma = -2;
        else if (vk == KeyEvent.VK_LEFT || hd == TRAI) ma = -3;
        else if (vk == KeyEvent.VK_RIGHT || hd == PHAI) ma = -4;
        else if (vk == KeyEvent.VK_ENTER || vk == KeyEvent.VK_SPACE || hd == DANH) ma = -5;
        else if (vk == KeyEvent.VK_ESCAPE || hd == MENU || vk == KeyEvent.VK_BACK_SPACE) ma = -7;
        else if (vk == KeyEvent.VK_TAB) ma = -2;
        else if (vk >= KeyEvent.VK_0 && vk <= KeyEvent.VK_9) ma = 48 + vk - KeyEvent.VK_0;
        else if (vk >= KeyEvent.VK_NUMPAD0 && vk <= KeyEvent.VK_NUMPAD9) ma = 48 + vk - KeyEvent.VK_NUMPAD0;
        if (ma == -5 && vk == KeyEvent.VK_ENTER) {
            // hộp thoại chỉ có lệnh trái ("OK"): Enter = lệnh đó
            game.model.Main m = Canvas.menu.showMenu ? Canvas.menu : Canvas.currentDialog != null ? (game.model.Main) Canvas.currentDialog : Canvas.currentScreen;
            if (m != null && m.center == null && m.left != null) ma = -6;
        }
        if (ma == 0) return;
        if (lap) {
            if (ma == -1 || ma == -2 || ma == -3 || ma == -4) go(ma);
            return;
        }
        nhan(vk, ma);
    }

    static void thaPhim(int vk) {
        Integer ma = DANG_GIU.remove(vk);
        if (ma != null && Canvas.instance != null) Canvas.instance.keyReleased(ma.intValue());
    }

    /** Mở ô chat (phím Chat - mặc định T - hoặc "/"); Enter gửi khi đang gõ chat. */
    static void moChat() {
        GameScr.typechat = GameScr.CHAT_NORMAL;
        Canvas.gameScr.chatMode = true;
        Canvas.gameScr.tfChat.setIputType(0);
        Canvas.gameScr.tfChat.isFocus = true;
        Canvas.clearKeyHold();
        BO_KY_TU_MO_CHAT[0] = true;   // ký tự của chính phím mở chat (t / /) không vào ô chat
    }

    /** bỏ đúng một KEY_TYPED ngay sau khi mở chat bằng phím chữ */
    private static final boolean[] BO_KY_TU_MO_CHAT = new boolean[1];

    /** Enter trong màn chơi: hành động ngữ cảnh (HanhDong) hoặc phím chọn (5) của game. */
    static void hanhDongNguCanh(int vk) {
        try {
            if (game.pokemon.HanhDong.nguCanh()) return;
        } catch (Throwable e) {
            NhatKy.loi("hanh dong ngu canh", e);
        }
        nhan(vk, -5);
    }

    /** Ctrl+V: dán chữ trong khay nhớ vào ô nhập đang gõ (lọc xuống dòng / ký tự điều khiển). */
    static void dan() {
        try {
            Object o = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
            String d = NhapChu.locDan(o == null ? null : String.valueOf(o), 200);
            for (int i = 0; i < d.length(); i++) goChu(d.charAt(i));
        } catch (Throwable e) {
            // khay nhớ trống / không phải chữ
        }
    }

    /** Ký tự gõ (KEY_TYPED, cả chữ tiếng Việt từ bộ gõ). */
    static void goChu(char c) {
        if (BO_KY_TU_MO_CHAT[0]) {
            BO_KY_TU_MO_CHAT[0] = false;
            int ma = PHIM[CHAT];
            if (c == '/' || ma >= KeyEvent.VK_A && ma <= KeyEvent.VK_Z && Character.toUpperCase(c) == (char) ma) return;
        }
        if (c < 32 || c == 127 || !dangGo()) return;
        if (oNhapSo && (c < '0' || c > '9')) return;
        game.render.screen.TField.isQwerty = true;
        go(c);
    }

    /** Mất focus cửa sổ: thả hết phím đang giữ (không đi mãi). */
    static void thaHet() {
        Object[] a = DANG_GIU.keySet().toArray();
        for (int i = 0; i < a.length; i++) thaPhim(((Integer) a[i]).intValue());
        Canvas.clearKeyHold();
    }
}
