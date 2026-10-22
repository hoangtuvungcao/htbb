package game.pokemon;

import game.model.AttackResult;
import game.model.Char;
import game.model.LiveActor;
import game.model.MainChar;
import game.model.Tilemap;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.GameScr;
import game.render.screen.RMS;
import java.io.DataInputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * NGẮM TAY (0.9.8, bản 908): trong trận giữa người chơi (máy chủ báo qua lệnh -113) KHÔNG tự động đánh, KHÔNG tự nhắm -
 * kéo chiêu để ngắm, thả tay để bắn, chiêu bay thẳng theo hướng kéo. PvE không đổi trừ khi bật "Ngắm thủ công" (Cài đặt).
 * <ul>
 *   <li><b>Cảm ứng</b> (APK Hud, J2ME HudRong / thanh ô nhanh): nhấn nút chiêu rồi kéo = vạch ngắm từ Pokémon theo hướng
 *       kéo (như cần điều khiển); kéo ra rồi kéo VỀ nút = huỷ; chạm nhanh không kéo = bắn theo hướng đang quay.</li>
 *   <li><b>PC</b>: giữ phím chiêu (hoặc nhấn giữ nút chiêu bằng chuột) và rê chuột: vạch ngắm theo hướng con trỏ; thả =
 *       bắn; chuột phải / thả trên nút = huỷ.</li>
 *   <li><b>Phím J2ME</b>: bấm phím chiêu = bắn theo hướng đang quay.</li>
 * </ul>
 * Máy chủ tự mô phỏng đường đạn (đạn / tia xuyên / nón / vùng; tầm, bề rộng, tốc độ theo bảng gói -113 loại 2) - client
 * chỉ gửi chiêu + hướng + điểm xuất phát (-113 loại 0), không bao giờ gửi "trúng ai".
 *
 * <p>Lệnh -113 máy chủ -> client: loại 0 đạn bay của người khác (vẽ hiệu ứng), 1 chế độ (byte PvP, UTF lý do), 2 bảng
 * thông số chiêu của lớp. Máy chủ chỉ gửi cho client &gt;= 908; máy chủ cũ không gửi = client giữ cách đánh cũ.
 */
public final class NgamTay {

    private NgamTay() {
    }

    public static final int LENH = -113;
    public static final int K_DAN = 0, K_XUYEN = 1, K_NON = 2, K_VUNG = 3, K_TU_THAN = 4;
    static final String RMS_THU_CONG = "htbbNgamTay";

    // ------------------------------------------------------------------ trạng thái máy chủ

    /** Máy chủ đã gửi -113 (hỗ trợ ngắm tay). */
    public static boolean mayChu;
    /** Máy chủ báo đang trong trận giữa người chơi. */
    public static boolean pvp;
    public static String lyDo = "";
    /** Người chơi bật "Ngắm thủ công" cho cả PvE. */
    static boolean thuCong;
    static boolean daNapThuCong;

    /** Bảng thông số chiêu (theo chiêu 0..15) - mặc định giống máy chủ cho tới khi có gói loại 2. */
    static final byte[] KIEU = new byte[16];
    static final short[] TAM = new short[16];
    static final short[] RONG = new short[16];
    static final short[] TOC = new short[16];
    static final short[] GOC = new short[16];
    static boolean coBang;
    static int lopBang = -1;

    public static boolean thuCong() {
        if (!daNapThuCong) {
            daNapThuCong = true;
            try {
                thuCong = RMS.loadRMSInt(RMS_THU_CONG) == 1;
            } catch (Throwable e) {
                thuCong = false;
            }
        }
        return thuCong;
    }

    public static void datThuCong(boolean b) {
        thuCong = b;
        daNapThuCong = true;
        try {
            RMS.saveRMSInt(RMS_THU_CONG, b ? 1 : 0);
        } catch (Throwable e) {
            // không lưu được: vẫn đổi trong phiên
        }
        if (mayChu) {
            try {
                Message m = new Message((byte) LENH);
                m.writer().writeByte(1);
                m.writer().writeByte(b ? 1 : 0);
                GameService.gI().session.sendMessage(m);
                m.cleanup();
            } catch (Throwable e) {
                // mất kết nối
            }
        }
    }

    /** Đang ở chế độ ngắm (mọi chiêu tấn công thành phát bắn theo hướng). */
    public static boolean dangNgam() {
        return mayChu && (pvp || thuCong());
    }

    // ------------------------------------------------------------------ mạng

    /** Gói -113 (gọi ở đầu MessageHandler.onMessage). true = đã xử lý. */
    public static boolean nhan(Message msg) {
        if (msg == null || msg.command != LENH) {
            return false;
        }
        mayChu = true;
        try {
            DataInputStream r = msg.reader();
            int loai = r.readByte();
            if (loai == 1) {
                boolean cu = pvp;
                pvp = r.readByte() == 1;
                String s = r.readUTF();
                lyDo = s == null ? "" : s;
                if (pvp) {
                    vaoPvp(!cu);
                } else if (cu) {
                    huy();
                    try {
                        ThongBao.hien("Rời trận giữa người chơi: đánh như thường.", ThongBao.TIN, ThongBao.HINH_KHONG, 0);
                    } catch (Throwable e) {
                        // chưa vào game
                    }
                }
            } else if (loai == 2) {
                int n = r.readByte();
                for (int i = 0; i < n; i++) {
                    int s = r.readByte();
                    int k = r.readByte();
                    int tam = r.readShort(), rong = r.readShort(), toc = r.readShort();
                    int goc = (r.readByte() & 0xFF) * 3;
                    r.readByte(); // tối đa mục tiêu (máy chủ tự đếm)
                    if (s >= 0 && s < KIEU.length) {
                        KIEU[s] = (byte) k;
                        TAM[s] = (short) tam;
                        RONG[s] = (short) rong;
                        TOC[s] = (short) toc;
                        GOC[s] = (short) goc;
                    }
                }
                coBang = true;
                lopBang = lopNay();
            } else if (loai == 0) {
                nhanDan(r);
            }
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    static int lopNay() {
        try {
            return Canvas.gameScr.mainChar.clazz;
        } catch (Throwable e) {
            return 0;
        }
    }

    /** Vào trận PvP: tắt tự động (cả bản cũ phía client), bỏ đuổi mục tiêu, báo một lần. */
    static void vaoPvp(boolean moi) {
        try {
            if (game.render.screen.TuDong.dang) {
                game.render.screen.TuDong.tat(false);
            }
        } catch (Throwable e) {
            // chưa có tự động
        }
        GameScr.autoFight = false;
        GameScr.saveAutoFight = false;
        try {
            GameScr gs = Canvas.gameScr;
            if (gs != null) {
                gs.duoiMucTieu = null;
            }
        } catch (Throwable e) {
            // bỏ qua
        }
        if (moi) {
            try {
                ThongBao.hien(lyDo.length() > 0 ? lyDo : "Trận giữa người chơi: kéo chiêu để ngắm, thả tay để bắn.",
                        ThongBao.TIN, ThongBao.HINH_KHONG, 0);
            } catch (Throwable e) {
                // chưa vào game
            }
        }
    }

    /** Gửi phát bắn: -113 loại 0 (chiêu, gốc, hướng x1000). */
    static void gui(int chieu, int ox, int oy, int hx, int hy) {
        try {
            Message m = new Message((byte) LENH);
            m.writer().writeByte(0);
            m.writer().writeByte(chieu);
            m.writer().writeShort(ox);
            m.writer().writeShort(oy);
            m.writer().writeShort(hx);
            m.writer().writeShort(hy);
            GameService.gI().session.sendMessage(m);
            m.cleanup();
            GUI_LAN++;
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    /** Kiểm thử: số lần gửi. */
    public static int GUI_LAN;

    // ------------------------------------------------------------------ thông số chiêu

    static final int[][] BUFF = {{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};

    static boolean laBuff(int lop, int s) {
        if (lop < 0 || lop >= BUFF.length) {
            return false;
        }
        for (int i = 0; i < BUFF[lop].length; i++) {
            if (BUFF[lop][i] == s) {
                return true;
            }
        }
        return false;
    }

    /** Thông số chiêu {kieu, tam, rong, toc, goc} - bảng máy chủ hoặc mặc định (khớp com.htbb.pokemon.BanHuong.macDinh). */
    public static int[] thongSo(int lop, int s) {
        if (coBang && lopBang == lop && s >= 0 && s < KIEU.length && TAM[s] > 0) {
            return new int[]{KIEU[s], TAM[s], RONG[s], TOC[s], GOC[s]};
        }
        int tam = 64;
        try {
            tam = SkillManager.getSkillRange((byte) s, (byte) lop);
        } catch (Throwable e) {
            // ngoài bảng
        }
        if (tam < 24) {
            tam = 24;
        }
        if (laBuff(lop, s)) {
            return new int[]{K_TU_THAN, tam, 32, 0, 360};
        }
        switch (s) {
            case 0:
                return lop == 2 || lop == 4 ? new int[]{K_DAN, tam, 16, 420, 30} : new int[]{K_NON, tam, 24, 0, 100};
            case 1:
                return new int[]{K_DAN, tam, 20, 480, 30};
            case 2:
                return new int[]{K_XUYEN, tam, 24, 0, 30};
            case 3:
                return new int[]{K_NON, tam, 32, 0, 70};
            default:
                return new int[]{K_VUNG, tam, 64, 0, 360};
        }
    }

    // ------------------------------------------------------------------ cử chỉ ngắm

    /** 0 = không ngắm; 1 = kéo cảm ứng từ nút; 2 = giữ phím + chuột; 3 = nhấn giữ nút bằng chuột. */
    static int nguon;
    /** Phím chiêu đang ngắm (1 3 5 7 9). */
    static int phim = -1;
    static int xNut, yNut, rNut;
    static int xKeo, yKeo;
    static boolean daRa;
    /** Hướng ngắm hiện tại (x1000), 0,0 = chưa có (dùng hướng đang quay). */
    static int hx, hy;
    /** Phát chờ doFire bắn (sau khi thả). */
    static boolean choBan;
    static int choHx, choHy;
    /** PC: có chuột (bản PC gọi {@link #chuotDi}) + vị trí con trỏ (toạ độ logic màn hình). */
    public static boolean chuot;
    static int xChuot = -1, yChuot = -1;

    public static boolean dangGiu() {
        return nguon != 0;
    }

    public static int phimDangGiu() {
        return nguon != 0 ? phim : -1;
    }

    /** APK: HUD HD (com.htbb.pokemon.hd.Hud) tự xử lý chạm nút chiêu - GameScr.updatePoint không bắt kéo. */
    public static boolean HUD_RIENG;

    /**
     * GameScr.updatePoint (đầu tiên): nhấn nút chiêu (HudRong / thanh ô nhanh jar) rồi kéo = ngắm, thả = bắn / huỷ.
     * @return true = cú chạm thuộc ngắm tay (không đi / chọn trong thế giới).
     */
    public static boolean chamNgam(GameScr gs) {
        if (HUD_RIENG || gs == null) {
            return false;
        }
        if (!dangNgam()) {
            if (nguon == 1 || nguon == 3) {
                huy();
            }
            return false;
        }
        if (nguon == 1 || nguon == 3) {
            if (Canvas.isPointerClick || !Canvas.isPointerDown) {
                tha(Canvas.px, Canvas.py);
                Canvas.isPointerClick = false;
                Canvas.isPointerDown = false;
                try {
                    game.render.screen.HudRong.boGiu();
                } catch (Throwable e) {
                    // bỏ qua
                }
            } else {
                keo(Canvas.px, Canvas.py);
            }
            return true;
        }
        if (nguon == 0 && Canvas.isPointerDown && !Canvas.isPointerClick) {
            int[] o = gs.oChieuTai(Canvas.pxLast, Canvas.pyLast);
            if (o != null) {
                int sk = skillCuaPhim(gs, o[0]);
                if (sk >= 0 && thongSo(gs.mainChar.clazz, sk)[0] != K_TU_THAN && !laBuff(gs.mainChar.clazz, sk)) {
                    batDauKeo(o[0], o[1], o[2], o[3]);
                    keo(Canvas.px, Canvas.py);
                    return true;
                }
            }
        }
        return false;
    }

    /** PC (ManHinh.mouseMoved / mouseDragged): con trỏ ở (x, y) logic. */
    public static void chuotDi(int x, int y) {
        chuot = true;
        xChuot = x;
        yChuot = y;
        if (nguon == 2 || nguon == 3) {
            keo(x, y);
        }
    }

    /** Bắt đầu kéo từ nút chiêu (tâm x, y bán kính r, toạ độ màn hình logic). */
    public static void batDauKeo(int phimChieu, int x, int y, int r) {
        phim = phimChieu;
        nguon = chuot ? 3 : 1;
        xNut = x;
        yNut = y;
        rNut = Math.max(8, r);
        xKeo = x;
        yKeo = y;
        daRa = false;
        hx = 0;
        hy = 0;
    }

    /** Cập nhật điểm kéo (màn hình logic). */
    public static void keo(int x, int y) {
        if (nguon == 0) {
            return;
        }
        xKeo = x;
        yKeo = y;
        int dx = x - xNut, dy = y - yNut;
        if (nguon == 1 || nguon == 3) {
            if (dx * dx + dy * dy > rNut * rNut) {
                daRa = true;
            }
        }
        int[] h;
        if (nguon == 1) {
            h = chuan(dx, dy);
        } else {
            h = huongChuot(x, y);
        }
        if (h != null) {
            hx = h[0];
            hy = h[1];
        }
    }

    /** Hướng từ Pokémon tới điểm màn hình (x, y) - PC (HanhDong.wx / wy đã vá theo zoom). */
    static int[] huongChuot(int x, int y) {
        try {
            MainChar mc = Canvas.gameScr.mainChar;
            return chuan(HanhDong.wx(x) - mc.x, HanhDong.wy(y) - (mc.y - 12));
        } catch (Throwable e) {
            return null;
        }
    }

    /** Vector (dx, dy) chuẩn hoá x1000; null = quá ngắn. */
    public static int[] chuan(int dx, int dy) {
        long l2 = (long) dx * dx + (long) dy * dy;
        if (l2 < 9) {
            return null;
        }
        int l = isqrt(l2);
        return new int[]{dx * 1000 / l, dy * 1000 / l};
    }

    static int isqrt(long v) {
        if (v <= 0) {
            return 0;
        }
        long r = (long) Math.sqrt((double) v);
        while (r * r > v) {
            r--;
        }
        while ((r + 1) * (r + 1) <= v) {
            r++;
        }
        return (int) Math.max(1, r);
    }

    /** Đang ở vùng huỷ (đã kéo ra rồi kéo về nút). */
    public static boolean vungHuy() {
        if (nguon != 1 && nguon != 3) {
            return false;
        }
        int dx = xKeo - xNut, dy = yKeo - yNut;
        return daRa && dx * dx + dy * dy <= rNut * rNut;
    }

    /** Kết quả thả: 0 = huỷ, 1 = bắn theo hướng kéo, 2 = bắn theo hướng đang quay (chạm nhanh). */
    public static int KQ_THA;

    /** Thả tay. @return true = sẽ bắn (đặt phím cho doFire). */
    public static boolean tha(int x, int y) {
        if (nguon == 0) {
            return false;
        }
        keo(x, y);
        int p = phim;
        boolean huy = vungHuy();
        boolean nhanh = (nguon == 1 || nguon == 3) && !daRa;
        int bx = hx, by = hy;
        nguon = 0;
        phim = -1;
        if (huy) {
            KQ_THA = 0;
            return false;
        }
        choBan = true;
        if (nhanh || (bx == 0 && by == 0)) {
            KQ_THA = 2;
            choHx = 0;
            choHy = 0;
        } else {
            KQ_THA = 1;
            choHx = bx;
            choHy = by;
        }
        if (p >= 0 && p < Canvas.keyPressed.length) {
            Canvas.keyPressed[p] = true;
        }
        return true;
    }

    /** Huỷ ngắm đang giữ (chuột phải, rời PvP, đổi màn). @return true = có đang ngắm. */
    public static boolean huy() {
        boolean co = nguon != 0;
        nguon = 0;
        phim = -1;
        choBan = false;
        if (co) {
            KQ_THA = 0;
        }
        return co;
    }

    /** Mỗi khung (GameScr.update): PC giữ phím - thả phím = bắn theo con trỏ. */
    public static void capNhat() {
        if (pvp && mayChu && GameScr.autoFight) {
            vaoPvp(false);   // trong trận giữa người chơi: không tự đánh (kể cả bật lại tự đánh cũ phía client)
        }
        if (nguon == 2) {
            if (!dangNgam()) {
                huy();
                return;
            }
            if (phim >= 0 && phim < Canvas.keyHold.length && !Canvas.keyHold[phim]) {
                if (xChuot >= 0) {
                    keo(xChuot, yChuot);
                }
                tha(xKeo, yKeo);
            }
        } else if (nguon != 0 && !dangNgam()) {
            huy();
        }
    }

    // ------------------------------------------------------------------ chặn doFire

    /**
     * GameScr.doFire, sau khi lấy ô nhanh: đang ngắm + ô là chiêu tấn công -> phát bắn theo hướng. @return true = đã xử
     * lý (doFire dừng).
     */
    public static boolean chanDoFire(GameScr gs, int key, int skillType, boolean laChieu, boolean laBuffO) {
        if (!dangNgam() || !laChieu || laBuffO || gs == null || gs.mainChar == null) {
            return false;
        }
        int lop = gs.mainChar.clazz;
        if (laBuff(lop, skillType) || thongSo(lop, skillType)[0] == K_TU_THAN) {
            return false;   // buff: tức thì như cũ
        }
        if (choBan) {
            choBan = false;
            ban(gs, skillType, choHx, choHy);
            return true;
        }
        if (chuot && nguon == 0 && Canvas.keyHold != null && key >= 0 && key < Canvas.keyHold.length && Canvas.keyHold[key]) {
            // PC: giữ phím chiêu -> ngắm theo con trỏ, thả phím mới bắn
            phim = key;
            nguon = 2;
            daRa = true;
            hx = 0;
            hy = 0;
            if (xChuot >= 0) {
                keo(xChuot, yChuot);
            }
            return true;
        }
        ban(gs, skillType, 0, 0);   // phím J2ME / chạm nhanh: hướng đang quay
        return true;
    }

    /** Hướng 4 phía của mainChar.dir (0 xuống, 1 lên, 2 trái, 3 phải) -> x1000. */
    public static int[] huongQuay(int dir) {
        switch (dir) {
            case 1:
                return new int[]{0, -1000};
            case 2:
                return new int[]{-1000, 0};
            case 3:
                return new int[]{1000, 0};
            default:
                return new int[]{0, 1000};
        }
    }

    /** Hướng vector -> dir 4 phía (khớp Util.findDirection). */
    public static short dirTu(int x, int y) {
        if (Math.abs(x) < Math.abs(y)) {
            return (short) (y > 0 ? 0 : 1);
        }
        return (short) (x < 0 ? 2 : 3);
    }

    /** Tầm thật: dừng ở ô chặn đầu tiên (bước 4 px) - khớp máy chủ. */
    static int tamThat(int ox, int oy, int hx1000, int hy1000, int tam) {
        for (int d = 4; d <= tam; d += 4) {
            int x = ox + hx1000 * d / 1000, y = oy + hy1000 * d / 1000;
            boolean chan;
            try {
                chan = Tilemap.tileTypeAtPixel(x, y, 2);
            } catch (Throwable e) {
                chan = false;
            }
            if (chan) {
                return Math.max(0, d - 4);
            }
        }
        return tam;
    }

    /** Lần bắn cuối (kiểm thử): {chieu, hx, hy}. */
    public static int[] BAN_CUOI;

    /** Bắn: kiểm hồi chiêu / PP phía client (máy chủ kiểm lại), hoạt ảnh tới điểm cuối đường, gửi -113. */
    public static boolean ban(GameScr gs, int skillType, int x1000, int y1000) {
        MainChar mc = gs.mainChar;
        if (mc.state == 3 || mc.beStune) {
            return false;
        }
        int[] h = x1000 == 0 && y1000 == 0 ? huongQuay(mc.dir) : new int[]{x1000, y1000};
        byte cap = 1;
        try {
            cap = Char.skillLevelLearnt[skillType];
        } catch (Throwable e) {
            cap = 1;
        }
        if (cap <= 0 && skillType != 0) {
            return false;
        }
        int pp = 0;
        try {
            pp = SkillManager.getSkillMP(skillType, cap, mc.clazz);
        } catch (Throwable e) {
            pp = 0;
        }
        if (pp > mc.mp) {
            try {
                ThongBao.hien("Không đủ PP", ThongBao.LOI, ThongBao.HINH_KHONG, 0);
            } catch (Throwable e) {
                // bỏ qua
            }
            return false;
        }
        long now = System.currentTimeMillis();
        if (skillType < mc.timeLastUseSkills.length && now - mc.timeLastUseSkills[skillType] <= mc.coolDownSkill[skillType]) {
            return false;   // còn hồi chiêu
        }
        if (mc.state == 1) {
            mc.moveTo(mc.x, mc.y);
            mc.justStopFromRun = true;
        }
        mc.dir = dirTu(h[0], h[1]);
        int[] ts = thongSo(mc.clazz, skillType);
        int tam = tamThat(mc.x, mc.y, h[0], h[1], ts[1]);
        int dDiem = ts[0] == K_NON ? tam * 2 / 3 : tam;
        LiveActor dich = diem(mc.x + h[0] * dDiem / 1000, mc.y + h[1] * dDiem / 1000);
        AttackResult atr = new AttackResult();
        atr.dam = 2000000;
        mc.mp -= pp;
        try {
            mc.startAttack(dich, atr, (byte) skillType, cap <= 0 ? 1 : cap);
            mc.coolDownSkill[skillType] = SkillManager.getSkillCooldown((byte) skillType, mc.clazz, cap <= 0 ? 1 : cap);
        } catch (Throwable e) {
            // hoạt ảnh lỗi: vẫn bắn
        }
        gs.duoiMucTieu = null;
        gui(skillType, mc.x, mc.y, h[0], h[1]);
        try {
            mc.downDurableWeapone();
        } catch (Throwable e) {
            // bỏ qua
        }
        themVet(mc.x, mc.y, h[0], h[1], tam, ts);
        BAN_CUOI = new int[]{skillType, h[0], h[1]};
        return true;
    }

    /** Điểm đích ảo cho hoạt ảnh chiêu (không vào danh sách actor). */
    static LiveActor diem(final int x, final int y) {
        LiveActor a = new LiveActor() {
            public void paint(Graphics g) {
            }

            public void setPosTo(short a1, short a2) {
            }
        };
        a.x = (short) x;
        a.y = (short) y;
        a.catagory = (byte) 16;
        a.hp = 1;
        a.realHP = 1;
        a.ID = (short) -32000;
        return a;
    }

    /** Gói -113 loại 0: đạn của người khác -> quay hướng + hoạt ảnh tới điểm cuối, vệt bay. */
    static void nhanDan(DataInputStream r) throws java.io.IOException {
        short id = r.readShort();
        int chieu = r.readByte();
        int ox = r.readShort(), oy = r.readShort();
        int dx = r.readShort(), dy = r.readShort();
        int tam = r.readShort();
        int kieu = r.readByte();
        int rong = r.readShort(), toc = r.readShort();
        int goc = (r.readByte() & 0xFF) * 3;
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null || id == gs.mainChar.ID) {
            return;
        }
        Object o = gs.findCharByID(id);
        if (o instanceof Char) {
            Char c = (Char) o;
            c.dir = dirTu(dx, dy);
            int dDiem = kieu == K_NON ? tam * 2 / 3 : tam;
            AttackResult atr = new AttackResult();
            atr.dam = 2000000;
            try {
                c.startAttack(diem(ox + dx * dDiem / 1000, oy + dy * dDiem / 1000), atr, (byte) chieu, (byte) 1);
            } catch (Throwable e) {
                // hoạt ảnh lỗi
            }
        }
        themVet(ox, oy, dx, dy, tam, new int[]{kieu, tam, rong, toc, goc});
    }

    // ------------------------------------------------------------------ vẽ (toạ độ thế giới)

    /** Vệt đường đạn mờ dần (đường bắn thấy được kể cả khi hoạt ảnh chiêu ngắn). */
    static final Vector VET = new Vector();

    static void themVet(int ox, int oy, int dx, int dy, int tam, int[] ts) {
        if (VET.size() > 12) {
            VET.removeElementAt(0);
        }
        VET.addElement(new int[]{ox, oy - 12, dx, dy, tam, ts[0], ts[2], ts[3], ts[4], (int) (System.currentTimeMillis() & 0x7FFFFFFF)});
    }

    /** GameScr.paint trong lớp thế giới (trước NhipVe.hetTheGioi). */
    /**
     * Vẽ vạch ngắm / nón / vòng vùng / vệt đạn. MIỄN TRỪ quy tắc "icon chỉ là PNG" (0.9.7, VeTay): đây là HÌNH CHỈ HƯỚNG
     * động theo góc / tầm thật của chiêu (không phải icon), nên vẽ bằng lệnh là chủ ý và không ghi VeTay. Không dùng
     * khối trắng / teal (kiem_kieu_cu). Icon nào thêm vào màn ngắm phải lấy từ /nro/icon.png.
     */
    public static void ve(Graphics g, GameScr gs) {
        try {
            veVet(g);
            if (nguon != 0 && gs != null && gs.mainChar != null) {
                veNgam(g, gs);
            }
        } catch (Throwable e) {
            // không làm hỏng khung vẽ
        }
    }

    static void veVet(Graphics g) {
        int bay = (int) (System.currentTimeMillis() & 0x7FFFFFFF);
        for (int i = VET.size() - 1; i >= 0; i--) {
            int[] v = (int[]) VET.elementAt(i);
            int toc = v[7];
            int msBay = toc > 0 && (v[5] == K_DAN || v[5] == K_XUYEN) ? v[4] * 1000 / toc : 0;
            int tuoi = bay - v[9];
            if (tuoi < 0 || tuoi > msBay + 250) {
                VET.removeElementAt(i);
                continue;
            }
            int s = msBay <= 0 ? v[4] : Math.min(v[4], toc * tuoi / 1000);
            int ex = v[0] + v[2] * s / 1000, ey = v[1] + v[3] * s / 1000;
            g.setColor(ChuDe.VANG);
            if (v[5] == K_VUNG) {
                int r = v[6] / 2;
                g.drawArc(ex - r, ey - r, 2 * r, 2 * r, 0, 360);
            } else {
                int t = Math.max(1, Math.min(3, v[6] / 10));
                int sx = v[0] + v[2] * Math.max(0, s - 24) / 1000, sy = v[1] + v[3] * Math.max(0, s - 24) / 1000;
                for (int k = -t / 2; k <= t / 2; k++) {
                    g.drawLine(sx + k, sy, ex + k, ey);
                    g.drawLine(sx, sy + k, ex, ey + k);
                }
                g.fillArc(ex - 3, ey - 3, 6, 6, 0, 360);
            }
        }
    }

    /** Vạch ngắm: đường (đạn / tia) hai mép theo bề rộng, nón hai cạnh + cung, vùng = vòng tròn cuối tầm. */
    static void veNgam(Graphics g, GameScr gs) {
        MainChar mc = gs.mainChar;
        int sk = skillCuaPhim(gs, phim);
        if (sk < 0) {
            return;
        }
        int[] ts = thongSo(mc.clazz, sk);
        int[] h = hx == 0 && hy == 0 ? huongQuay(mc.dir) : new int[]{hx, hy};
        int ox = mc.x, oy = mc.y - 12;
        int tam = tamThat(mc.x, mc.y, h[0], h[1], ts[1]);
        boolean huyNay = vungHuy();
        int mau = huyNay ? ChuDe.DO : ChuDe.CHINH_SANG;
        int ex = ox + h[0] * tam / 1000, ey = oy + h[1] * tam / 1000;
        // vuông góc
        int px = -h[1], py = h[0];
        g.setColor(ChuDe.BONG);
        g.drawLine(ox + 1, oy + 1, ex + 1, ey + 1);
        g.setColor(mau);
        switch (ts[0]) {
            case K_NON: {
                double a = Math.toRadians(ts[4] / 2.0);
                for (int k = -1; k <= 1; k += 2) {
                    double c = Math.cos(a * k), s = Math.sin(a * k);
                    int mx = (int) (h[0] * c - h[1] * s), my = (int) (h[0] * s + h[1] * c);
                    g.drawLine(ox, oy, ox + mx * tam / 1000, oy + my * tam / 1000);
                }
                int gocDau = gocDo(h[0], -h[1]) - ts[4] / 2;
                g.drawArc(ox - tam, oy - tam, 2 * tam, 2 * tam, gocDau, ts[4]);
                break;
            }
            case K_VUNG: {
                int r = ts[2] / 2;
                veDut(g, ox, oy, ex, ey);
                g.drawArc(ex - r, ey - r, 2 * r, 2 * r, 0, 360);
                g.drawArc(ex - r + 1, ey - r + 1, 2 * r - 2, 2 * r - 2, 0, 360);
                break;
            }
            default: {
                int nr = ts[2] / 2;
                int ax = px * nr / 1000, ay = py * nr / 1000;
                g.drawLine(ox + ax, oy + ay, ex + ax, ey + ay);
                g.drawLine(ox - ax, oy - ay, ex - ax, ey - ay);
                g.drawLine(ex + ax, ey + ay, ex - ax, ey - ay);
                veDut(g, ox, oy, ex, ey);
                // mũi tên ở đầu
                int bx = ex - h[0] * 10 / 1000, by = ey - h[1] * 10 / 1000;
                g.fillTriangle(ex, ey, bx + px * 6 / 1000, by + py * 6 / 1000, bx - px * 6 / 1000, by - py * 6 / 1000);
                break;
            }
        }
    }

    /** Góc (độ, 0..359, ngược chiều kim đồng hồ, trục y hướng lên) của vector (x, y) - CLDC 1.1 không có Math.atan2. */
    public static int gocDo(int x, int y) {
        if (x == 0 && y == 0) {
            return 0;
        }
        int ax = Math.abs(x), ay = Math.abs(y);
        double z = ax >= ay ? (double) ay / ax : (double) ax / ay;
        // atan(z) (độ) cho 0 <= z <= 1, sai số < 0,3 độ
        double a = z * (45.0 - (z - 1.0) * (14.0 + 3.83 * z));
        if (ay > ax) {
            a = 90.0 - a;
        }
        if (x < 0) {
            a = 180.0 - a;
        }
        if (y < 0) {
            a = 360.0 - a;
        }
        int d = (int) (a + 0.5);
        return d >= 360 ? d - 360 : d;
    }

    /** Đường giữa nét đứt. */
    static void veDut(Graphics g, int x0, int y0, int x1, int y1) {
        int dx = x1 - x0, dy = y1 - y0;
        int l = isqrt((long) dx * dx + (long) dy * dy);
        if (l <= 0) {
            return;
        }
        for (int d = 0; d < l; d += 10) {
            int e = Math.min(l, d + 5);
            g.drawLine(x0 + dx * d / l, y0 + dy * d / l, x0 + dx * e / l, y0 + dy * e / l);
        }
    }

    /** Chiêu (skillType) của phím 1 3 5 7 9 ở bộ ô đang dùng; -1 = không phải chiêu. */
    public static int skillCuaPhim(GameScr gs, int phimChieu) {
        try {
            game.model.QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][gs.SLOTINDEXOFKEY[phimChieu]];
            if (qs == null || qs.quickslotType != game.model.QuickSlot.TYPE_SKILL) {
                return -1;
            }
            return qs.getSkillType();
        } catch (Throwable e) {
            return -1;
        }
    }

    /** Kiểm thử: đặt lại mọi trạng thái. */
    public static void datLai() {
        mayChu = false;
        pvp = false;
        lyDo = "";
        thuCong = false;
        daNapThuCong = true;
        coBang = false;
        lopBang = -1;
        nguon = 0;
        phim = -1;
        choBan = false;
        chuot = false;
        xChuot = -1;
        yChuot = -1;
        hx = 0;
        hy = 0;
        VET.removeAllElements();
        GUI_LAN = 0;
        BAN_CUOI = null;
        KQ_THA = -1;
    }
}
