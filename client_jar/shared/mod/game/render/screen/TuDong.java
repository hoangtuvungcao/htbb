package game.render.screen;

import game.model.Actor;
import game.model.Char;
import game.model.Dropable;
import game.model.LiveActor;
import game.model.MainChar;
import game.model.Tilemap;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Util;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * TỰ ĐỘNG ĐÁNH chính thức (thay mod NQSH: Auto up / PK / Vận may / Lên thành / Giao hàng tự động đã bỏ).
 * MÁY CHỦ quyết định: client chỉ chạy khi server trả "đang bật" (lệnh -100, HTBB com.htbb.pokemon.TuDong /
 * real.TuDongGame); server đếm giờ mỗi ngày, chặn nhịp thao tác như người thật, dừng khi PvP / sàn đấu / Gym /
 * boss / đổi map / ra khỏi vùng / hết giờ. Server cũ không trả lời -100 thì không bao giờ chạy.
 *
 * Mọi thao tác đi qua ĐÚNG hàm của game như người chơi bấm tay: GameScr.doUseSkillToFocusActor (tự đi tới tầm,
 * kiểm MP / hồi chiêu, có hoạt ảnh), GameScr.doUsePotion, GameService.getDropableFromGround. Không lướt, không
 * bỏ hoạt ảnh, không đổi tầm chiêu.
 *
 * Lệnh -100 client -> server: byte 0 xin trạng thái / 1 bật (+ short bán kính px, byte tự hồi sinh) / 2 tắt (+ byte
 * lý do) / 3 nhịp (20 giây) / 4 xin kế hoạch (0,6 giây). Bản 0.9.2+ gửi thêm byte giao thức 2 (sau 0 / sau phần bật)
 * và cài đặt bảng: ngưỡng HP %, ngưỡng PP %, kiểu nhặt, về hồi phục, đồng ý hồi sinh bằng vật phẩm lượng.
 * Server -> client: byte 0 (trạng thái), byte bật, int giây còn lại hôm nay, int giây tối đa hôm nay, short bán kính
 * tối đa, byte cờ (1 tính năng bật, 2 cho tự uống thuốc, 4 dừng khi PvP, 8 chỉ bãi luyện tập, 16 máy chủ điều khiển
 * từng bước, 32 tự di chuyển thì dừng), UTF thông báo. Hoặc byte 1 (kế hoạch, khi cờ 16): byte hành động (0 chờ,
 * 1 đánh, 2 thuốc, 3 nhặt, 4 về điểm bắt đầu, 5 dừng), short id, short số (chiêu / loại thuốc / loại đồ), short x,
 * short y, UTF trạng thái. MÁY CHỦ chọn mục tiêu (gần nhất theo đường đi, đúng khoảng cấp, không boss), chiêu (khắc
 * hệ, PP, hồi chiêu), thuốc, đồ nhặt, rút lui; client chỉ làm đúng việc đó bằng hàm của game. Server cũ (không cờ 16)
 * thì client tự chọn như bản 0.9.1.
 *
 * Bảng cài đặt (giao diện) ở Nen.moBangTuDong: bản jar/APK = BangTuDong (kiểu Pokémon), bản khác = menu.
 */
public final class TuDong {
    public static final byte LENH = -100;
    static final int CO_TINH_NANG = 1, CO_THUOC = 2, CO_TAT_PVP = 4, CO_CHI_MAP = 8, CO_KE_HOACH = 16, CO_DUNG_TU_DI = 32;
    /** Phiên bản giao thức -100 của client này (server com.htbb.pokemon.TuDong.GIAO_THUC). */
    static final int GIAO_THUC = 2;
    static final int HD_CHO = 0, HD_DANH = 1, HD_THUOC = 2, HD_NHAT = 3, HD_VE = 4, HD_DUNG = 5;
    /** Lý do tắt gửi kèm lệnh 2 (TuDong.TAT_TU_DI của server). */
    static final int TAT_TU_DI = 13;
    private static final String RMS_TEN = "htbbtd";

    /** Bình HP / MP mạnh -> yếu (id loại thuốc của game, y GameScr.doUsePotion). */
    private static final int[] BINH_HP = new int[]{94, 93, 22, 21, 3, 2, 1};
    private static final int[] BINH_MP = new int[]{96, 95, 24, 23, 6, 5, 4};
    /** Loại skill buff theo dòng (không dùng để đánh). */
    private static final byte[][] SKILL_BUFF = new byte[][]{{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};

    // ---------------------------------------------------------------- trạng thái máy chủ
    /** Server đã trả lời ít nhất một lần (server có hỗ trợ -100). */
    static boolean coServer;
    static boolean svBat;
    static int svConLai;
    static int svToiDa;
    static int svBanKinh = 240;
    static int svCo = CO_TINH_NANG | CO_THUOC | CO_TAT_PVP | CO_CHI_MAP;
    static long svLuc;
    /** Câu server gửi kèm gần nhất (lý do dừng / từ chối). */
    static String thongBao = "";
    static long lucXin;
    // kế hoạch máy chủ gửi (cờ CO_KE_HOACH)
    private static int keHd = -1, keId, keSo, keX, keY;
    private static long keLuc, lucXinKe;

    // ---------------------------------------------------------------- đang chạy
    /** Client đang chạy (chỉ true khi server xác nhận). */
    public static boolean dang;
    /** Đã xin bật, chờ server. */
    static boolean choBat;
    static long lucChoBat;
    private static int neoX, neoY, neoMap = -1;
    private static Actor mucTieu, monNhat;
    private static long lucDanh, lucBinh, lucNhip, lucNhuong, lucKet, lucDoi;
    private static int ketX, ketY, lanKet;
    private static boolean giuPhim, hoiPhuc;
    static String trangThai = "";
    static int soHa;

    // ---------------------------------------------------------------- cài đặt (RMS)
    /** Thứ tự chiêu (loại skill, -1 = trống); trống hết = mọi chiêu đánh đã học (mạnh trước). */
    static final byte[] thuTu = new byte[]{-1, -1, -1, -1};
    /** Uống thuốc khi HP / MP dưới (%); 0 = tắt. */
    static int nguongHp = 50;
    static int nguongMp = 30;
    /** Nhặt đồ: 0 không, 1 thuốc + xu, 2 thêm đá / nguyên liệu, 3 tất cả. */
    static int nhat = 1;
    static final String[] TEN_NHAT = new String[]{"Không nhặt", "Thuốc + xu", "Thuốc, xu, đá", "Tất cả"};
    /** Hết thuốc mà HP thấp: về điểm bắt đầu đứng hồi phục tới 80% rồi đánh tiếp. */
    static boolean veHoiPhuc = true;
    /** Vùng đánh quanh điểm bắt đầu (ô, 1 ô = 16 px). Mặc định 15 ô = tu_dong.ban_kinh 240 px (10 ô của 0.9.1 hay trống quái). */
    static int vungO = 15;
    static final int[] VUNG_O = new int[]{4, 7, 10, 15, 20};
    /**
     * Gục khi đang tự động: bật = cho máy chủ (hệ thống hồi sinh) tự dùng vật phẩm hồi sinh tại chỗ, tối đa
     * tu_dong.hoi_sinh_moi_gio lần / giờ, rồi đánh tiếp; tắt (mặc định) = dừng, về Trung tâm Pokémon như thường.
     */
    static boolean tuHoiSinh;
    /**
     * Đồng ý RIÊNG cho tự dùng cả vật phẩm hồi sinh mua bằng lượng (Thuốc hồi sinh tối đa) - chỉ có tác dụng khi
     * tuHoiSinh bật và máy chủ cho (tu_dong.hoi_sinh_tra_phi). Mặc định tắt: chỉ Thuốc hồi sinh thường.
     */
    static boolean hoiSinhLuong;
    private static long lucChet;
    private static boolean daNap;

    private TuDong() {
    }

    // ================================================================ mạng

    private static void gui(int loai, int banKinh) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(loai);
            if (loai == 0) {
                m.writer().writeByte(GIAO_THUC);
            } else if (loai == 1) {
                m.writer().writeShort(banKinh);
                m.writer().writeByte(tuHoiSinh ? 1 : 0);
                m.writer().writeByte(GIAO_THUC);
                m.writer().writeByte(choThuoc() ? nguongHp : 0);
                m.writer().writeByte(choThuoc() ? nguongMp : 0);
                m.writer().writeByte(nhat);
                m.writer().writeByte(veHoiPhuc ? 1 : 0);
                m.writer().writeByte(tuHoiSinh && hoiSinhLuong ? 1 : 0);
            } else if (loai == 2 && banKinh > 0) {
                m.writer().writeByte(banKinh); // lý do tắt
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // mất kết nối: lần sau
        }
    }

    /**
     * Bảng Tự động đánh / Menu nhanh đang mở. Bản PC (htbb.pc.BanPhim) dùng để Enter / Esc / mũi tên đi vào bảng
     * thay vì mở chat / đi bộ (bản 0.9.1: Enter trên "Bắt đầu" mở ô chat, bảng không nhận phím).
     */
    public static boolean bangDangMo() {
        return BangTuDong.mo || MenuNhanh.mo;
    }

    /** Xin trạng thái (mở bảng / vào game). */
    public static void xinTrangThai() {
        lucXin = System.currentTimeMillis();
        gui(0, 0);
    }

    /** Gói -100 từ server (gọi ở đầu MessageHandler qua AutoGuard.goiGuard). true = đã xử lý. */
    public static boolean nhan(Message msg) {
        if (msg == null || msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            if (r.readByte() == 1) {
                // kế hoạch bước tiếp (máy chủ điều khiển)
                keHd = r.readByte();
                keId = r.readShort();
                keSo = r.readShort();
                keX = r.readShort();
                keY = r.readShort();
                String ly = r.readUTF();
                keLuc = System.currentTimeMillis();
                coServer = true;
                if (dang && ly != null && ly.length() > 0) {
                    trangThai = ly;
                }
                return true;
            }
            boolean bat = r.readByte() != 0;
            svConLai = r.readInt();
            svToiDa = r.readInt();
            svBanKinh = r.readShort();
            svCo = r.readByte();
            String tb = r.readUTF();
            coServer = true;
            svBat = bat;
            svLuc = System.currentTimeMillis();
            if (tb != null && tb.length() > 0) {
                thongBao = tb;
            }
            if (bat && choBat) {
                choBat = false;
                batThat();
            } else if (!bat) {
                boolean dangChay = dang || choBat;
                choBat = false;
                if (dang) {
                    dungTaiCho();
                }
                if (dangChay && tb != null && tb.length() > 0) {
                    game.pokemon.DungDo.bao(tb, game.pokemon.DungDo.TIN);   // 0.9.5.1: thông báo nhanh, không hộp
                }
            }
        } catch (Throwable e) {
            // gói hỏng: bỏ
        }
        return true;
    }

    /** Giây còn lại hôm nay (ước theo lần server báo gần nhất). */
    static int conLai() {
        if (!coServer) {
            return 0;
        }
        int g = svConLai;
        if (dang) {
            g -= (int) ((System.currentTimeMillis() - svLuc) / 1000L);
        }
        return g < 0 ? 0 : g;
    }

    static String gio(int giay) {
        int h = giay / 3600;
        int p = giay % 3600 / 60;
        if (h > 0) {
            return new StringBuffer().append(h).append(" giờ ").append(p < 10 ? "0" : "").append(p).append(" phút").toString();
        }
        return new StringBuffer().append(p).append(" phút").toString();
    }

    static boolean choThuoc() {
        return (svCo & CO_THUOC) != 0;
    }

    // ================================================================ bật / tắt

    /** Nút Bắt đầu / Dừng. */
    public static void batTat() {
        if (dang || choBat) {
            tat(true);
            return;
        }
        bat();
    }

    static void bat() {
        nap();
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        if (gs.mainChar.hp <= 0) {
            thongBao = "Pokémon đang gục, hồi sinh rồi bật lại.";
            game.pokemon.DungDo.bao(thongBao, game.pokemon.DungDo.TIN);   // 0.9.5.1: thông báo nhanh
            return;
        }
        if (game.pokemon.KhoaMap.chanTuDong(gs.map)) {
            thongBao = game.pokemon.KhoaMap.lyDoTuDong(gs.map);   // ban do khoa Tu dong (goi -106): khong xin server
            return;
        }
        if (coServer && (svCo & CO_TINH_NANG) == 0) {
            thongBao = "Tự động đánh đang tạm khoá trên máy chủ.";
            game.pokemon.DungDo.bao(thongBao, game.pokemon.DungDo.TIN);   // 0.9.5.1: thông báo nhanh
            return;
        }
        thongBao = "";
        choBat = true;
        lucChoBat = System.currentTimeMillis();
        gui(1, banKinh());
    }

    /** Bán kính (px) xin server: cài đặt của người chơi, không quá mức server cho. */
    static int banKinh() {
        int px = vungO * 16;
        return svBanKinh > 0 && px > svBanKinh ? svBanKinh : px;
    }

    /** Server đã cho: bắt đầu tại chỗ đứng hiện tại. */
    private static void batThat() {
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        dang = true;
        neoMap = gs.map;
        neoX = gs.mainChar.x;
        neoY = gs.mainChar.y;
        mucTieu = null;
        monNhat = null;
        hoiPhuc = false;
        soHa = 0;
        keHd = -1;
        keLuc = 0L;
        lucXinKe = 0L;
        lucNhip = System.currentTimeMillis() + 20000L;
        trangThai = "bắt đầu";
        // auto gốc của game (BangAuto) không chạy song song
        GameScr.autoFight = false;
        GameScr.saveAutoFight = false;
        GameScr.autoBomHMP = false;
    }

    /** Dừng (báo server nếu {@code baoServer}). */
    public static void tat(boolean baoServer) {
        boolean co = dang || choBat;
        choBat = false;
        dungTaiCho();
        if (co && baoServer) {
            gui(2, 0);
        }
    }

    private static void dungTaiCho() {
        dang = false;
        keHd = -1;
        nha();
        mucTieu = null;
        monNhat = null;
        hoiPhuc = false;
        trangThai = "";
    }

    // ================================================================ vòng chính (mỗi khung hình)

    public static void tick() {
        long l = System.currentTimeMillis();
        if (choBat && l - lucChoBat > 6000L) {
            choBat = false;
            thongBao = coServer ? "Máy chủ không phản hồi Tự động đánh, thử lại sau ít giây." : "Máy chủ chưa hỗ trợ Tự động đánh.";
            // bảng đã đóng khi bấm Bắt đầu: báo bằng thông báo nhanh, không im lặng
            game.pokemon.DungDo.bao(thongBao, game.pokemon.DungDo.TIN);   // 0.9.5.1: thông báo nhanh
        }
        if (!dang) {
            return;
        }
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        MainChar mc = gs.mainChar;
        if (l >= lucNhip) {
            lucNhip = l + 20000L;
            gui(3, 0);
        }
        if (mc.hp <= 0) {
            // tự hồi sinh: máy chủ quyết (luật hồi sinh + trần mỗi giờ); không thì nó tự dừng và báo
            if (tuHoiSinh) {
                if (lucChet == 0L) {
                    lucChet = l;
                }
                if (l - lucChet < 35000L) {
                    nha();
                    mucTieu = null;
                    trangThai = "chờ hồi sinh";
                    return;
                }
            }
            thongBao = "Pokémon đã gục - Tự động đánh dừng. Hồi sinh rồi bật lại.";
            tat(true);
            return;
        }
        lucChet = 0L;
        if (gs.map != neoMap) {
            thongBao = "Đã rời bãi luyện tập - Tự động đánh dừng.";
            tat(true);
            return;
        }
        if (Canvas.currentScreen != gs || Canvas.currentDialog != null || Canvas.menu.showMenu || gs.chatMode) {
            nha();
            trangThai = "tạm dừng";
            return;
        }
        if (nguoiBam()) {
            if ((svCo & CO_DUNG_TU_DI) != 0 && phimDiChuyen()) {
                // người chơi tự điều khiển: dừng hẳn (tu_dong.dung_khi_tu_di), máy chủ báo lý do
                thongBao = "Tự động đánh dừng vì bạn tự di chuyển.";
                choBat = false;
                dungTaiCho();
                gui(2, TAT_TU_DI);
                return;
            }
            lucNhuong = l + 3000L;
            nha();
        }
        if (l < lucNhuong) {
            trangThai = "chờ bạn";
            return;
        }
        if ((svCo & CO_KE_HOACH) != 0) {
            theoKeHoach(gs, mc, l);
            return;
        }
        binh(gs, mc, l);
        if (hoiPhuc(gs, mc, l)) {
            return;
        }
        if (nhatDo(gs, mc, l)) {
            return;
        }
        danh(gs, mc, l);
    }

    // ---------------------------------------------------------------- làm theo kế hoạch máy chủ

    /** Xin kế hoạch mới ~0,6 giây / lần (sớm hơn khi vừa làm xong một việc); làm đúng việc máy chủ chọn. */
    private static void theoKeHoach(GameScr gs, MainChar mc, long l) {
        if (l - lucXinKe >= 600L || keHd < 0 && l - lucXinKe >= 300L) {
            lucXinKe = l;
            gui(4, 0);
        }
        if (keHd < 0 || l - keLuc > 3000L) {
            nha();
            trangThai = "chờ máy chủ";
            return;
        }
        switch (keHd) {
            case HD_DANH: {
                Actor a = timActor(gs, 1, keId);
                if (a == null || !quaiSong(gs, a)) {
                    if (a != null) {
                        // vừa hạ: xin kế hoạch mới ngay
                        ++soHa;
                        keHd = -1;
                        lucXinKe = 0L;
                        nha();
                        return;
                    }
                    // chưa thấy trên màn (ngoài tầm nhìn): đi về phía đó
                    diToi(mc, keX, keY, l);
                    return;
                }
                mucTieu = a;
                gs.focusedActor = a;
                if (keSo < 0) {
                    // chưa có chiêu sẵn (hồi chiêu / PP): tiến lại gần, chờ
                    if (Util.distance(mc.x, mc.y, a.x, a.y) > 48) {
                        diToi(mc, a.x, a.y, l);
                    } else {
                        nha();
                    }
                    return;
                }
                nha();
                if (l < lucDanh || mc.isDoing || mc.state == Char.A_ATTACK) {
                    return;
                }
                gs.doUseSkillToFocusActor((byte) keSo);
                lucDanh = l + 450L;
                return;
            }
            case HD_THUOC: {
                nha();
                if (l >= lucBinh && keSo >= 0 && keSo < mc.potions.length && mc.potions[keSo] > 0) {
                    gs.doUsePotion(keSo);
                    lucBinh = l + 1500L;
                }
                keHd = -1;
                lucXinKe = 0L;
                return;
            }
            case HD_NHAT: {
                Actor d = timActor(gs, keSo, keId);
                if (d == null || !(d instanceof Dropable) || ((Dropable) d).isSendGet) {
                    keHd = -1;
                    lucXinKe = 0L;
                    nha();
                    return;
                }
                if (Util.distance(mc.x, mc.y, d.x, d.y) <= 30) {
                    nha();
                    GameService.gI().getDropableFromGround(d.catagory, d.ID);
                    ((Dropable) d).isSendGet = true;
                    keHd = -1;
                    lucXinKe = 0L;
                    return;
                }
                diToi(mc, d.x, d.y, l);
                return;
            }
            case HD_VE:
                gs.focusedActor = null;
                if (Util.distance(mc.x, mc.y, keX, keY) > 20) {
                    diToi(mc, keX, keY, l);
                } else {
                    nha();
                    gs.huyDiTheoDuong();
                }
                return;
            default:
                nha();
                gs.huyDiTheoDuong();
                gs.focusedActor = null;
                return;
        }
    }

    private static Actor timActor(GameScr gs, int cat, int id) {
        for (int i = 0; i < gs.actors.size(); ++i) {
            Actor a = (Actor) gs.actors.elementAt(i);
            if (a != null && a.catagory == cat && a.ID == id) {
                return a;
            }
        }
        return null;
    }

    private static boolean phimDiChuyen() {
        return Canvas.keyPressed[2] || Canvas.keyPressed[4] || Canvas.keyPressed[6] || Canvas.keyPressed[8];
    }

    /** Người chơi tự bấm phím di chuyển / chạm màn: nhường 3 giây (phím di chuyển: dừng nếu máy chủ bật cờ 32). */
    private static boolean nguoiBam() {
        if (Canvas.keyPressed[2] || Canvas.keyPressed[4] || Canvas.keyPressed[6] || Canvas.keyPressed[8]) {
            return true;
        }
        return Canvas.isPointerDown && !Nen.trongNutNhanh(Canvas.px, Canvas.py);
    }

    // ---------------------------------------------------------------- thuốc

    private static void binh(GameScr gs, MainChar mc, long l) {
        if (l < lucBinh || !choThuoc()) {
            return;
        }
        if (nguongHp > 0 && mc.maxhp > 0 && (long) mc.hp * 100L < (long) mc.maxhp * nguongHp && uong(gs, mc, BINH_HP, l)) {
            lucBinh = l + 1500L;
            return;
        }
        if (nguongMp > 0 && mc.maxmp > 0 && (long) mc.mp * 100L < (long) mc.maxmp * nguongMp && uong(gs, mc, BINH_MP, l)) {
            lucBinh = l + 1500L;
        }
    }

    private static int coBinh(MainChar mc, int[] loai) {
        for (int i = 0; i < loai.length; ++i) {
            int n = loai[i];
            if (n < mc.potions.length && mc.potions[n] > 0) {
                return n;
            }
        }
        return -1;
    }

    private static boolean uong(GameScr gs, MainChar mc, int[] loai, long l) {
        int n = coBinh(mc, loai);
        if (n < 0) {
            return false;
        }
        if (MainChar.listPotion != null && n < MainChar.listPotion.length && MainChar.listPotion[n] != null && l - mc.potionLastTimeUse[n] < MainChar.listPotion[n].delay) {
            return false;
        }
        gs.doUsePotion(n);
        return true;
    }

    /** Về hồi phục: HP thấp mà không có / không được dùng thuốc -> về điểm đầu đứng nghỉ tới 80%. */
    private static boolean hoiPhuc(GameScr gs, MainChar mc, long l) {
        if (!veHoiPhuc || mc.maxhp <= 0) {
            hoiPhuc = false;
            return false;
        }
        int pt = (int) ((long) mc.hp * 100L / mc.maxhp);
        int nguong = nguongHp > 0 ? nguongHp : 40;
        boolean hetThuoc = !choThuoc() || nguongHp <= 0 || coBinh(mc, BINH_HP) < 0;
        if (!hoiPhuc && pt < nguong && hetThuoc) {
            hoiPhuc = true;
            mucTieu = null;
            gs.focusedActor = null;
        }
        if (hoiPhuc && pt >= 80) {
            hoiPhuc = false;
        }
        if (!hoiPhuc) {
            return false;
        }
        if (Util.distance(mc.x, mc.y, neoX, neoY) > 20) {
            diToi(mc, neoX, neoY, l);
            trangThai = "về hồi phục";
        } else {
            nha();
            trangThai = "nghỉ hồi phục " + pt + "%";
        }
        return true;
    }

    // ---------------------------------------------------------------- chọn quái + đánh

    static int vungPx() {
        return banKinh();
    }

    private static boolean quaiSong(GameScr gs, Actor a) {
        // state 8 = đang xoá, 5 = bay chết; 3 là Pokémon hoang dã ĐANG ĐÁNH TRẢ (Monster.startAttack) - vẫn sống,
        // bản 0.9.1 coi 3 là chết nên bỏ đúng con đang cắn mình
        return a != null && a.catagory == 1 && !a.isMineral() && a.state != 8 && a.state != 5 && ((LiveActor) a).hp > 0 && gs.actors.contains(a);
    }

    private static boolean trongVung(Actor a) {
        return Util.distance(neoX, neoY, a.x, a.y) <= vungPx();
    }

    private static Actor chonQuai(GameScr gs, MainChar mc) {
        Actor best = null;
        int d = Integer.MAX_VALUE;
        for (int i = 0; i < gs.actors.size(); ++i) {
            Actor a = (Actor) gs.actors.elementAt(i);
            if (!quaiSong(gs, a) || !trongVung(a)) {
                continue;
            }
            int k = Util.distance(mc.x, mc.y, a.x, a.y);
            if (k < d) {
                d = k;
                best = a;
            }
        }
        return best;
    }

    private static void danh(GameScr gs, MainChar mc, long l) {
        if (mucTieu != null && !quaiSong(gs, mucTieu)) {
            if (((LiveActor) mucTieu).hp <= 0 || mucTieu.state == 8 || mucTieu.state == 5) {
                ++soHa;
            }
            mucTieu = null;
        }
        if (mucTieu != null && !trongVung(mucTieu)) {
            mucTieu = null;
        }
        // kẹt không tới được con này 4 giây: đổi con khác
        if (mucTieu != null && l - lucDoi > 4000L && Util.distance(mc.x, mc.y, mucTieu.x, mucTieu.y) > 60) {
            mucTieu = null;
        }
        if (mucTieu == null) {
            mucTieu = chonQuai(gs, mc);
            lucDoi = l;
        }
        if (mucTieu == null) {
            gs.focusedActor = null;
            if (Util.distance(mc.x, mc.y, neoX, neoY) > 24) {
                diToi(mc, neoX, neoY, l);
                trangThai = "về chỗ cũ";
            } else {
                nha();
                trangThai = "chờ Pokémon hoang dã";
            }
            return;
        }
        nha();
        gs.focusedActor = mucTieu;
        trangThai = "đang đánh";
        if (l < lucDanh || mc.isDoing || mc.state == Char.A_ATTACK) {
            return;
        }
        byte sk = chonChieu(mc, l);
        if (sk < 0) {
            return;
        }
        gs.doUseSkillToFocusActor(sk);
        lucDanh = l + 450L;
    }

    /** Chiêu theo thứ tự đã chọn (hồi xong, đủ MP); trống thì mọi chiêu đánh đã học (mạnh trước); không có thì 0. */
    static byte chonChieu(MainChar mc, long l) {
        byte[] ds = dsChieu(mc);
        for (int i = 0; i < ds.length; ++i) {
            byte s = ds[i];
            if (s < 0 || s >= Char.skillLevelLearnt.length || Char.skillLevelLearnt[s] <= 0 && s != 0) {
                continue;
            }
            if (l - mc.timeLastUseSkills[s] <= mc.coolDownSkill[s]) {
                continue;
            }
            if (SkillManager.getSkillMP(s, Char.skillLevelLearnt[s], mc.clazz) > mc.mp) {
                continue;
            }
            return s;
        }
        return -1;
    }

    /** Danh sách chiêu sẽ dùng theo thứ tự. */
    static byte[] dsChieu(MainChar mc) {
        int n = 0;
        for (int i = 0; i < thuTu.length; ++i) {
            if (thuTu[i] >= 0) {
                ++n;
            }
        }
        if (n > 0) {
            byte[] r = new byte[n];
            n = 0;
            for (int i = 0; i < thuTu.length; ++i) {
                if (thuTu[i] >= 0) {
                    r[n++] = thuTu[i];
                }
            }
            return r;
        }
        return chieuDanh(mc);
    }

    /** Mọi chiêu đánh đã học, mạnh trước (bỏ chiêu buff). */
    public static byte[] chieuDanh(MainChar mc) {
        int tong = mc == null ? 0 : mc.getnSkill();
        byte[] tam = new byte[tong];
        int n = 0;
        for (int i = tong - 1; i >= 0; --i) {
            if (laBuff(mc, i) || i >= Char.skillLevelLearnt.length || Char.skillLevelLearnt[i] <= 0 && i != 0) {
                continue;
            }
            tam[n++] = (byte) i;
        }
        byte[] r = new byte[n];
        System.arraycopy(tam, 0, r, 0, n);
        return r;
    }

    private static boolean laBuff(MainChar mc, int s) {
        byte[] b = SKILL_BUFF[Math.min(Math.max(0, mc.clazz), SKILL_BUFF.length - 1)];
        for (int i = 0; i < b.length; ++i) {
            if (b[i] == s) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- nhặt đồ

    private static boolean nhatDo(GameScr gs, MainChar mc, long l) {
        if (nhat <= 0) {
            return false;
        }
        if (monNhat != null && (!gs.actors.contains(monNhat) || ((Dropable) monNhat).isSendGet)) {
            monNhat = null;
        }
        if (monNhat == null) {
            monNhat = chonDo(gs, mc);
        }
        if (monNhat == null) {
            return false;
        }
        // đang đánh dở và đồ còn xa: đánh trước
        if (mucTieu != null && quaiSong(gs, mucTieu) && Util.distance(mc.x, mc.y, monNhat.x, monNhat.y) > 40) {
            return false;
        }
        if (Util.distance(mc.x, mc.y, monNhat.x, monNhat.y) <= 30) {
            nha();
            Dropable d = (Dropable) monNhat;
            GameService.gI().getDropableFromGround(d.catagory, d.ID);
            d.isSendGet = true;
            monNhat = null;
            trangThai = "nhặt đồ";
            return true;
        }
        diToi(mc, monNhat.x, monNhat.y, l);
        trangThai = "đi nhặt";
        return true;
    }

    private static Actor chonDo(GameScr gs, MainChar mc) {
        Actor best = null;
        int d = Integer.MAX_VALUE;
        for (int i = 0; i < gs.actors.size(); ++i) {
            Actor a = (Actor) gs.actors.elementAt(i);
            if (!(a instanceof Dropable) || !a.isItemCanGet() || ((Dropable) a).isSendGet) {
                continue;
            }
            int c = a.catagory;
            boolean muon = c == 4 ? nhat >= 1 : (c == 3 ? nhat >= 3 && !mc.isFullInventory() : nhat >= 2);
            if (!muon || !trongVung(a)) {
                continue;
            }
            int k = Util.distance(mc.x, mc.y, a.x, a.y);
            if (k < d) {
                d = k;
                best = a;
            }
        }
        return best;
    }

    // ---------------------------------------------------------------- đi bộ (giữ phím ảo, như người bấm)

    /** Phím đi lần trước (giữ trục, tránh đổi trục mỗi khung -> đi răng cưa). */
    private static int phimCu;

    private static void diToi(MainChar mc, int x, int y, long l) {
        GameScr gs = Canvas.gameScr;
        // đoạn thẳng bị tường chặn: đi theo đường (BFS game.model.DuongDi) thay vì giữ phím đâm vào tường rồi bỏ cuộc
        if (gs != null && Tilemap.type != null && !game.model.DuongDi.thongThoang(Tilemap.type, Tilemap.w, Tilemap.h, mc.x, mc.y, x, y)) {
            nha();
            gs.diTheoDuong(x, y);
            ketCho(mc, l);
            return;
        }
        if (gs != null) {
            gs.huyDiTheoDuong();
        }
        int dx = x - mc.x;
        int dy = y - mc.y;
        int ngang = dx < 0 ? 4 : 6;
        int doc = dy < 0 ? 2 : 8;
        int chinh = Math.abs(dx) >= Math.abs(dy) ? ngang : doc;
        if ((phimCu == ngang && Math.abs(dx) > 8 || phimCu == doc && Math.abs(dy) > 8) && !tuongTruoc(mc, phimCu)) {
            chinh = phimCu;
        }
        int phu = chinh == ngang ? doc : ngang;
        if (tuongTruoc(mc, chinh) && !tuongTruoc(mc, phu)) {
            chinh = phu;
        }
        phimCu = chinh;
        nha();
        Canvas.keyHold[chinh] = true;
        giuPhim = true;
        ketCho(mc, l);
    }

    /** Kẹt 1,5 giây (không nhúc nhích): bỏ mục tiêu (lần sau chọn con khác / đứng chờ). */
    private static void ketCho(MainChar mc, long l) {
        if (lucKet == 0L || mc.x != ketX || mc.y != ketY) {
            lucKet = l;
            ketX = mc.x;
            ketY = mc.y;
            lanKet = 0;
        } else if (l - lucKet > 1500L) {
            lucKet = 0L;
            nha();
            if (++lanKet >= 2) {
                mucTieu = null;
                monNhat = null;
            }
        }
    }

    private static boolean tuongTruoc(MainChar mc, int phim) {
        int tx = mc.x / 16 + (phim == 4 ? -1 : (phim == 6 ? 1 : 0));
        int ty = mc.y / 16 + (phim == 2 ? -1 : (phim == 8 ? 1 : 0));
        return laTuong(tx, ty);
    }

    /** Ô không đi qua được (luật va chạm của client) hoặc ô cổng chuyển map. */
    static boolean laTuong(int x, int y) {
        if (Tilemap.type == null || x < 0 || y < 0 || x >= Tilemap.w || y >= Tilemap.h) {
            return true;
        }
        int i = y * Tilemap.w + x;
        if (i >= Tilemap.type.length) {
            return true;
        }
        int t = Tilemap.type[i];
        return t >= 2000000000 || (t & 2) == 2;
    }

    private static void nha() {
        if (!giuPhim) {
            return;
        }
        giuPhim = false;
        Canvas.keyHold[2] = false;
        Canvas.keyHold[4] = false;
        Canvas.keyHold[6] = false;
        Canvas.keyHold[8] = false;
    }

    // ================================================================ thanh trạng thái

    /** Chữ thanh trạng thái (null = không vẽ). */
    static String chuThanh() {
        if (choBat) {
            return "Tự động: đang hỏi máy chủ...";
        }
        if (!dang) {
            return null;
        }
        return new StringBuffer("Tự động ").append(gio(conLai())).append(" - ").append(trangThai).toString();
    }

    /** 0.9.5: viên nhỏ dưới thẻ hồ sơ "Auto · 4:41 · đang đánh" (game.pokemon.VienTrangThai) + chữ "+N EXP". */
    static void ve(Graphics g, int y) {
        String s = null;
        if (!BangTuDong.mo) {
            if (choBat) {
                s = game.pokemon.VienTrangThai.chuAuto(-1, null);
            } else if (dang) {
                s = game.pokemon.VienTrangThai.chuAuto(conLai(), trangThai);
            }
        }
        game.pokemon.VienTrangThai.ve(g, s, dang, y);
    }

    // ================================================================ lưu cài đặt

    static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            String s = RMS.loadRMSString(RMS_TEN);
            if (s == null || s.length() < 9 || s.charAt(0) != '1') {
                return;
            }
            for (int i = 0; i < thuTu.length; ++i) {
                char c = s.charAt(1 + i);
                thuTu[i] = (byte) (c == '-' ? -1 : c - 'a');
            }
            nguongHp = (s.charAt(5) - 'a') * 10;
            nguongMp = (s.charAt(6) - 'a') * 10;
            nhat = s.charAt(7) - '0';
            veHoiPhuc = s.charAt(8) == '1';
            if (s.length() >= 10) {
                vungO = VUNG_O[Math.min(VUNG_O.length - 1, Math.max(0, s.charAt(9) - '0'))];
            }
            if (s.length() >= 11) {
                tuHoiSinh = s.charAt(10) == '1';
            }
            if (s.length() >= 12) {
                hoiSinhLuong = s.charAt(11) == '1';
            }
        } catch (Throwable e) {
            // mặc định
        }
    }

    static void luu() {
        try {
            StringBuffer b = new StringBuffer("1");
            for (int i = 0; i < thuTu.length; ++i) {
                b.append(thuTu[i] < 0 ? '-' : (char) ('a' + thuTu[i]));
            }
            b.append((char) ('a' + nguongHp / 10)).append((char) ('a' + nguongMp / 10)).append((char) ('0' + nhat)).append(veHoiPhuc ? '1' : '0');
            int k = 0;
            for (int i = 0; i < VUNG_O.length; ++i) {
                if (VUNG_O[i] == vungO) {
                    k = i;
                }
            }
            b.append((char) ('0' + k)).append(tuHoiSinh ? '1' : '0').append(hoiSinhLuong ? '1' : '0');
            RMS.saveRMSString(RMS_TEN, b.toString());
        } catch (Throwable e) {
            // không lưu được: giữ trong phiên
        }
    }
}
