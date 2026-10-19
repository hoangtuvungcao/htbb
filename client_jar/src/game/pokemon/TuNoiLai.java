package game.pokemon;

import game.render.Canvas;

/**
 * TỰ KẾT NỐI LẠI (0.9.4): rớt mạng thoáng qua -> client tự nối lại tối đa 3 lần (chờ 2 / 5 / 10 giây), đăng nhập bằng
 * tài khoản vừa dùng và vào lại đúng Pokémon đang chơi; chỉ khi cả 3 lần hỏng mới hiện hộp "Mất kết nối..." (nút
 * "Kết nối lại" vẫn dùng được, không phải mở lại game).
 *
 * Không tự nối khi server vừa báo lý do (thông báo lệnh 37 trong 5 giây trước khi rớt: đăng nhập nơi khác, bảo trì,
 * khoá...) - tránh hai máy đá nhau qua lại.
 */
public final class TuNoiLai {
    public static final int TOI_DA = 3;
    static final long[] CHO = {2000L, 5000L, 10000L};

    /** số lần đã thử trong đợt rớt hiện tại */
    public static int lan;
    /** đang trong đợt tự nối lại (màn chọn Pokémon tự chọn lại) */
    public static volatile boolean dangNoi;
    /** IDDB Pokémon đang chơi (CharSelectScr chọn) */
    public static int charDb = -1;
    public static boolean tat;
    private static long thongBaoLuc;

    private TuNoiLai() {
    }

    private static volatile Runnable choChay;
    private static boolean daChonLai;

    /** Canvas.run mỗi nhịp (luồng game): chạy lần nối đã hẹn, tự chọn lại Pokémon, kết thúc đợt khi đã vào game. */
    public static void tick() {
        Runnable r = choChay;
        if (r != null) {
            choChay = null;
            daChonLai = false;
            try {
                r.run();
            } catch (Throwable e) {
                GiamSat.loi("tu ket noi lai", e);
            }
            return;
        }
        if (!dangNoi) {
            return;
        }
        Object man = Canvas.currentScreen;
        if (!daChonLai && man instanceof game.render.screen.CharSelectScr && game.render.screen.CharSelectScr.me == man && charDb >= 0) {
            daChonLai = ((game.render.screen.CharSelectScr) man).tuChonLai(charDb);
            if (daChonLai) {
                GiamSat.ghi("tu ket noi lai: vao lai Pokemon " + charDb);
            }
        } else if (man == Canvas.gameScr && man != null && network.Session_ME.gI().connected && !Canvas.isLoad
                && Canvas.gameScr.mainChar != null && Canvas.gameScr.mainChar.name != null && Canvas.gameScr.mainChar.name.length() > 0) {
            GiamSat.ghi("tu ket noi lai: da vao lai game");
            vaoGame();
        }
    }

    /** MessageHandler lệnh 37: server vừa gửi thông báo. */
    public static void coThongBao() {
        thongBaoLuc = System.currentTimeMillis();
    }

    /** tài khoản / mật khẩu của lần đăng nhập cuối (chỉ trong RAM) - ô nhập có thể đã bị xoá sau khi vào game */
    static String tkCuoi = "", mkCuoi = "";

    /** LoginScr.doLogin: nhớ tài khoản vừa đăng nhập. */
    public static void nho(String tk, String mk) {
        tkCuoi = tk == null ? "" : tk;
        mkCuoi = mk == null ? "" : mk;
    }

    public static String tk(String oNhap) {
        return oNhap != null && oNhap.length() > 0 ? oNhap : tkCuoi;
    }

    public static String mk(String oNhap) {
        return oNhap != null && oNhap.length() > 0 ? oNhap : mkCuoi;
    }

    public static void daChon(int idDb) {
        charDb = idDb;
    }

    /** Người chơi bấm "Kết nối lại": một lần nối (không tự thử thêm), vẫn tự chọn lại Pokémon. */
    public static void thuCong() {
        lan = TOI_DA;
        dangNoi = true;
        daChonLai = false;
    }

    /** Đã vào lại màn chơi (gói thông tin nhân vật): hết đợt. */
    public static void vaoGame() {
        lan = 0;
        dangNoi = false;
    }

    /**
     * Rớt kết nối / nối hỏng: thử nối lại. true = đã hẹn lần thử (không hiện hộp mất kết nối);
     * false = hết lượt / không tự nối được -> hiện hộp như cũ.
     */
    public static boolean thu(final String tk, final String mk, final Runnable noi) {
        if (tat || noi == null || tk == null || tk.length() == 0 || mk == null || mk.length() == 0
                || System.currentTimeMillis() - thongBaoLuc < 5000L || lan >= TOI_DA) {
            GiamSat.ghi("khong tu ket noi lai: lan=" + lan + " tk=" + (tk == null ? 0 : tk.length()) + " mk=" + (mk == null ? 0 : mk.length())
                    + " thong bao cach " + (System.currentTimeMillis() - thongBaoLuc) + " ms");
            lan = 0;
            dangNoi = false;
            return false;
        }
        final int n = lan++;
        dangNoi = true;
        GiamSat.ghi("tu ket noi lai lan " + (n + 1) + "/" + TOI_DA + " sau " + CHO[n] + " ms");
        ManTai.batDau(ManTai.KET_NOI, "Mất kết nối · đang thử lại " + (n + 1) + "/" + TOI_DA);
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(CHO[n]);
                } catch (InterruptedException e) {
                    return;
                }
                choChay = noi;   // chạy trên luồng game (tick) - không đua với vòng game / tải bản đồ
            }
        });
        t.start();
        return true;
    }
}
