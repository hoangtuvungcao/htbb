/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 */
package game.render.screen;

import game.GameMidlet;
import game.model.Char;
import game.model.Tilemap;
import game.model.CMLocation;
import game.model.Command;
import game.model.XaphuTemplate;
import network.Session_ME;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.InfoAttributeItem;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.Layer;
import game.model.MainChar;
import game.model.Mineral;
import game.model.PageScr;
import game.model.ShopNew;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import game.render.screen.HelpScr;
import game.render.screen.ListScr;
import game.render.screen.MessageScr;
import game.render.screen.MsgWorld;
import game.render.screen.Screen;
import game.render.screen.Tree2014;
import game.render.screen.TuBinhScr;
import game.render.screen.WindowInfoScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class PauseMenu
extends Screen {
    public static PauseMenu me;
    private static final int TOP_COUNT = 9;
    private static final String[] DONG_AN_NAME;
    private static final int DONG_AN_TIER = 2;
    private boolean[] dongAnChon = new boolean[DONG_AN_NAME.length * 2];
    private boolean heMaPhap = true;
    private static final String[] AUTO_SLOT_NAME;
    private boolean autoRunning = false;
    private int autoMode;
    private int autoSlot;
    private int autoLevel;
    private int autoPhase;
    private int autoWaitTicks;
    private int autoWaitCount;
    private short[] autoSnapshot;
    private int autoTries;
    private int autoFound;
    private byte[][] autoFill;
    private int autoStageIdx;
    private int autoJob;
    private int autoGoalLevel = 50;
    private short[] autoTargets;
    private int autoTargetPos;
    private boolean autoOkSeen;
    private boolean autoLockTtc;
    private int autoLockGoal = 20;
    private int autoRepStat;
    private int autoRepHp;
    private ItemInInventory autoRefObj;
    private int autoSold;
    private int autoAfterHouse;
    private int autoPetLevel = 30;
    private int autoPetSlot = 5;
    private int autoPetFilter;
    private int autoPetCurSlot;
    private int autoPetEndSlot;
    private int autoPetStep;
    private int autoPetBuyW;
    private Vector autoPetOwned;
    private Vector autoPetSell;
    private Vector autoPetEp;
    private int autoPetMode;
    private int autoOkCount;
    private int autoPetGroups;
    private Vector autoInvRef;
    private int autoTkType;
    private int autoTkFilter;
    private int autoTkQty = 1;
    private int autoTkGrade;
    private boolean autoTkNoMat;
    private boolean autoTkNeedStone;
    private int autoTkStones;
    private Vector autoSoldIds = new Vector();
    private int autoTbLevel = 60;
    private int autoTbPct = -1;
    private int autoTbResult;
    private short autoTbItem;
    private Vector autoShopRef;
    private int autoLuyenTarget = 10;
    private boolean autoLuyenBuy;
    private short autoLkd = 10;
    private short autoBh = -1;
    private short autoMm = -1;
    private static final short[] LKD_IDS = new short[]{8, 9, 10, 157, 158, 11, 66, 245};
    private static final String[] LKD_NAME = new String[]{"20%", "30%", "40%", "cao cấp 30%", "cao cấp 40%", "đặc biệt 30", "đặc biệt 35", "đặc biệt 40"};
    private static final short[] BH_IDS = new short[]{-1, 0, 1, 2, 3, 4};
    private static final String[] BH_NAME = new String[]{"Không", "20%", "30%", "40%", "50%", "90%"};
    private static final short[] MM_IDS = new short[]{-1, 5, 6, 7, 155, 156};
    private static final String[] MM_NAME = new String[]{"Không", "cấp 1", "cấp 2", "cấp 3", "cấp 4", "cấp 5"};
    private boolean autoLkdLock;
    private boolean autoBhLock;
    private boolean autoMmLock;
    private int autoLuyenIdle;
    private int autoLuyenLastPlus;
    private int autoLuyenLastLkd;
    private String autoLastAlert;
    private int autoLuyenBuyCd;
    private short autoThemItem;
    private int autoThemBotLock;
    private int autoThemDaLock;
    private boolean autoThemWeapon;
    private boolean autoThemFull;
    private boolean autoThemBuy;
    private boolean autoThemOk;
    private boolean[] autoThemChon = new boolean[34];
    private int[] autoThemStart = new int[5];
    private static final String[] TD_NAME = new String[]{"Hút máu", "Tàn phế", "Câm lặng", "Hồi HP", "Hồi MP", "Tăng rớt vật phẩm", "Tăng rớt xu", "Kháng băng lan", "Kháng sét lan", "Kháng độc lan", "Tăng thủ trang bị", "Kháng lửa lan", "Dòng ẩn: Giảm st vật", "Dòng ẩn: Giảm st ma", "Dòng ẩn: Tăng tấn công", "Dòng ẩn: Xuyên giáp", "Dòng ẩn: Phản st"};
    private static final int[] TD_ID = new int[]{77, 80, 82, 78, 79, 83, 84, 85, 86, 87, 88, 92, 28, 29, 30, 31, 32};
    private static final int[][] TD_MAX = new int[][]{{30, 40, 55}, {10, 20, 30}, {10, 20, 30}, {3, 7, 10}, {3, 7, 10}, {30, 70, 100}, {30, 70, 100}, {10, 20, 40}, {10, 20, 40}, {10, 20, 40}, {10, 20, 40}, {10, 20, 40}, {3, 6, 9}, {3, 6, 9}, {3, 6, 9}, {2, 3, 5}, {3, 6, 9}};
    private static final String[] TK_TYPE_NAME = new String[]{"Quạt ba tiêu", "Gậy như ý", "Lưỡng nhận đao"};
    private static final int[] TK_QTY = new int[]{1, 2, 3, 5, 10, 20, 50};
    // Auto "Cộng thuộc tính" (job 9, ScreenDapDo type 0 ở nhà Hắc Ngưu): đập lại tới khi ra
    // đúng loại (hoặc loại bất kỳ) với mức >= autoCttMin (phần mười %, 5 = 0.5%).
    private short autoCttItem;
    private short autoCttDa;
    private boolean autoCttLock;
    private int autoCttLoai = -1;
    private int autoCttMin = 5;
    private boolean autoCttOk;
    private String autoCttVuaRa;
    private boolean autoCttFull;
    private boolean autoCttBoQua;
    /** id thuộc tính phía client của CTT_NAME (otherAtt 5/6/7/28 + 61). */
    private static final int[] CTT_ID = new int[]{66, 67, 68, 89};
    private static final String[] CTT_NAME = new String[]{"Sét lan", "Độc lan", "Băng lan", "Lửa lan"};
    private static final short CTT_DA_THUONG = 227;
    // Auto "Khóa đồ thú" (job 10, ScreenDapDo type 2): khóa lại tới khi chỉ số khóa (id 58/59/60,
    // lấy max) >= autoKtMuc. 20-21% chỉ ra với xương cấp 6 - Item.lockItemAnimal ở server.
    private short autoKtItem;
    private short autoKtDa;
    private boolean autoKtLock;
    private int autoKtMuc = 20;
    private boolean autoKtFull;
    private int autoKtDaTruoc;
    private static final short KT_XUONG_CAP6 = 234;
    // Auto "Nghiền bột" (job 14, trong nhà): chế đồ cấp 20/21 bằng nguyên liệu đã chọn rồi
    // nghiền (-68 type 4, OfflineMap.doNghienBot: 2000 xu, 1 bột, cách nhau >= 5s). Chế món sau
    // trong lúc chờ 5s. CHỈ nghiền món do chính auto vừa chế; món trắng không nghiền được -> bán.
    private int autoNbLoai = -1;
    private int autoNbCapSo = 0;
    private int autoNbCapCao = 5;
    private short autoNbDang = (short)-1;
    /** Đang có món chờ kết quả nghiền. KHÔNG dùng "autoNbDang < 0" làm dấu hiệu: itemID là
     *  short, id > 32767 bị âm (vd -31922) - auto tưởng chưa có món và không bao giờ gửi lệnh. */
    private boolean autoNbCo;
    private int autoNbThu;
    private long autoNbLuc;
    private boolean autoNbHet;
    private int autoNbChe;
    private int autoNbBan;
    /** Câu server trả về gần nhất khi từ chối nghiền (đã ẩn hộp thoại) - để báo khi bỏ cuộc. */
    private String autoNbLoi;
    /** Số bột (gem 246) lúc gửi lệnh nghiền - nghiền thành công server cộng 1 bột. */
    private int autoNbBotTruoc;
    /** Chẩn đoán: số món gom được lúc bắt đầu / bỏ vì không thấy trong túi / số lệnh nghiền đã gửi. */
    private int autoNbGom;
    private int autoNbBo;
    private int autoNbGui;
    private static final int NB_BOT = 246;
    /** Server cho nghiền cách nhau >= 200ms (OfflineMap.THOI_GIAN_CHO_NGHIEN_BOT_MS, trước là 5s). */
    private static final long NB_CACH_MS = 300L;
    /** Gửi lệnh nghiền lúc nào - chờ kết quả (bột tăng / câu từ chối) tối đa NB_CHO_KQ_MS rồi mới
     *  coi là thất bại: gửi lại khi kết quả chưa về là nghiền lại món đã mất -> "Không tìm thấy". */
    private long autoNbGuiLuc;
    private static final long NB_CHO_KQ_MS = 3000L;
    private static final int NB_HANG_DOI_TOI_DA = 3;
    // Auto "Nâng cấp Hỏa kỳ lân" (job 13): Hỏa kỳ lân 681 -> cấp 2 (682) -> cấp 3 (683, tối đa).
    // Mỗi lần: dùng món (server mở menu ảo -513) -> "Nâng cấp" -> chọn Hỏa thạch; 1 viên/lần,
    // cấp 1 (gem 258) 1%, cấp 2 (gem 259) 10% - Char.doNangCapHoaKyLan. Hỏa thạch cấp 1 bán
    // 10 lượng/viên nhưng CHỈ mua được trong nhà (OfflineMap.doBuyGemItem); cấp 2 không bán.
    private boolean autoHklMua = true;
    private int autoHklOk;
    private int autoHklHong;
    private int autoHklGui;
    private int autoHklKqTruoc;
    private int autoHklDaMua;
    private int autoHklMuaTruoc;
    private static final int HKL_CAP_TOI_DA = 683;
    private static final short HOA_THACH_C1 = 258;
    private static final short HOA_THACH_C2 = 259;
    private static final int HKL_LO = 10;
    private static final int HKL_MUA_LO = 100;
    // Auto "Học skill pet" (job 12): dùng Sách kỹ năng pet (potion 111) tới khi pet có skill
    // mong muốn; hết slot mà chưa ra (hoặc phẩm chất thấp hơn mức chọn) thì dùng Huyết Bồ Đề
    // (item 721) - xoá skill và random lại phẩm chất (Pet.doChangeThongThao ở server).
    private int autoSkMuon;
    /** Học slot 2: Tắt = chỉ cần slot 1 đúng skill; Bật = cần đủ cặp skill slot 1 + slot 2. */
    private boolean autoSkSlot2;
    private int autoSkMuon2 = 1;
    private int autoSkPhamChat = 2;
    private int autoSkKq;
    private boolean autoSkReset;
    private int autoSkSach;
    private int autoSkHbd;
    /** Server bỏ qua (không trả lời) nếu dùng cùng potion trong 250ms - Char.POTION_COOLDOWN_MS. */
    private long autoSkLuc;
    /** Tên skill y như Pet.ALL_SKILL ở server (dòng "- Kỹ năng: ..." trong thông tin pet). */
    private static final String[] SK_TEN = new String[]{"Chữa trị", "Phá triền", "Đoạt mệnh", "Thiên nhãn", "Tiên khí", "Kim cang", "Càn khôn", "Cường tráng", "Cường thân", "Kiên cố", "Hộ thể", "Liên kích", "Tăng kích", "Thuấn ảnh", "Phí huyết"};
    /** Pet.NAME_PHAM_CHAT; tỉ lệ học slot 1 = 0/1/10/25%, slot 2 = 0/0/5/10%. */
    private static final String[] SK_PHAM_CHAT = new String[]{"Ngu đần", "Bình thường", "Nhanh nhẹn", "Thông minh"};
    private static final int POTION_SACH_SKILL = 111;
    private static final int ITEM_HUYET_BO_DE = 721;
    /** Huyết Bồ Đề ở cửa hàng đặc biệt: mục 132 (data_shop), 10 lượng/viên - lệnh 85. */
    private static final int SHOP_HUYET_BO_DE = 132;
    private boolean autoSkMua = true;
    private int autoSkMuaTruoc;
    private int autoSkDaMua;
    // Auto "Nuôi pet" (job 11): đứng ở Thành gần NPC thợ săn (-16) -> chế 1 món cấp 20/21 bằng
    // nguyên liệu sơ cấp cấp autoPetCapSo + cao cấp cấp autoPetCapNL -> cho pet ăn đúng món đó -> pet chạm 9/19/29/39+99.9% thì
    // nâng cấp (LKD thấp nhất đang có) -> lặp lại. Sức ăn đầy thì dừng.
    /** Cấp (0..5 = cấp 1..6) nguyên liệu CAO CẤP - quyết định màu món, tức sức ăn tốn. */
    private int autoPetCapNL = 5;
    /** Cấp nguyên liệu SƠ CẤP (id < 103: Vải, Sắt, Ngọc, Gỗ thường, Da mềm). */
    private int autoPetCapSo = 0;
    /** Cặp nguyên liệu được dùng (-1 = tất cả) - mỗi món chế dùng đúng 1 cặp sơ cấp + cao cấp
     * (CreateItemTemplate.ID_MATERIAL ở server); id là cấp 1 của loại (startIDMaterial). */
    private int autoPetLoai = -1;
    /** true = chỉ cho ăn Tụ hồn đan (potions[83], dòng CUỐI danh sách "Cho pet ăn"), không chế đồ. */
    private boolean autoPetTuHon;
    private int autoPetTuTruoc;
    private static final int POTION_TU_HON_DAN = 83;
    private static final String[] PET_LOAI_TEN = new String[]{"Vải + Tơ lụa", "Sắt + Bạc", "Sắt + Gỗ sưa", "Ngọc + Thủy tinh", "Gỗ thường + Bạc", "Da mềm + Da cứng"};
    private static final int[] PET_LOAI_SO = new int[]{68, 75, 75, 82, 89, 96};
    private static final int[] PET_LOAI_CAO = new int[]{103, 110, 124, 117, 110, 131};
    /** Món auto đã chế mà pet chưa ăn (Integer itemID) - giữ qua cả đăng nhập lại. */
    private Vector autoPetLo = new Vector();
    /** "Công thức" chế lấy từ món đầu tiên: chuỗi menu NPC -8, id món, cách xếp nguyên liệu. */
    private boolean autoPetCoCongThuc;
    private int autoPetQNpc;
    private int[] autoPetQMenu;
    private int[] autoPetQOpt;
    private short autoPetIdTemp;
    private byte[][] autoPetFillMau;
    private int[] autoPetNLId;
    /** Số món đang chờ trong lô (chế hoặc cho ăn) và số món còn lại trước khi cho ăn. */
    private int autoPetLoCho;
    /** Mỗi lần chế 1 món rồi nối đuôi: gửi lệnh cho ăn món này kèm lệnh chế món sau. */
    private static final int PET_LO_TOI_DA = 1;
    /** Mỗi lượt Tụ hồn đan gửi liền tối đa ngần này lệnh ăn. */
    private static final int PET_LO_TU_HON = 10;
    /** Đếm danh sách "Cho pet ăn" đã xin / đã nhận - chỉ dùng danh sách của lần xin MỚI NHẤT. */
    private int autoPetDsCho;
    private int autoPetDsNhan;
    private boolean autoPetXinLai;
    /** Số món chế nối đuôi đang chờ, và id các món của lô lúc gửi lệnh cho ăn. */
    private int autoPetCheTheo;
    private short[] autoPetLoTruocIds;
    private String[] autoPetDsAn;
    private int autoPetDaAn;
    private int autoPetDaNang;
    private int autoPetUpKq;
    private boolean autoPetDay;
    private boolean autoPetCanNang;
    /** Server báo "... đã có thể nâng cấp lên N" / "tiến hoá" sau khi cho ăn. */
    private boolean autoPetBaoNang;
    // ---- giai đoạn 2: đăng nhập lại + tự lên Thành
    /** Vừa đăng nhập lại: sức ăn hiển thị còn cũ, server chỉ trừ về 0 ở lần cho ăn đầu tiên. */
    private boolean autoPetSauRelog;
    private int autoPetRelog;
    private volatile String autoRelogLoi;
    private int autoLenMap;
    /** Đường đi hiện tại: các điểm (chỉ số ô y*w+x), đi từ đầu tới cuối. */
    private int[] autoDuong;
    private int autoDuongPos;
    private boolean[] autoDich;
    private int autoDiDung;
    private int autoDiLastX;
    private int autoDiLastY;
    /** Lướt: tự gửi từng bước <= 48px đúng nhịp server cho phép (nhanh hơn giữ phím). */
    private long autoDiLuc;
    private int autoDiBiKeo;
    /** Server từ chối bước lướt (gửi lại lệnh 4 vị trí thật cho chính mình) - xem viTriServer(). */
    private volatile boolean autoCoKeo;
    private volatile int autoKeoX;
    private volatile int autoKeoY;
    /** Bị từ chối nhiều -> chặng này đi bằng giữ phím mũi tên. */
    private boolean autoDiPhim;
    private int autoDiTinhLai;
    /** Thành (201) và map 9 (có cổng lên Thành) - so theo nhóm map của client. */
    private static final int MAP_THANH = 201;
    private static final int MAP_CONG_THANH = 9;
    /** Xe ngựa: ưu tiên đích xa nhất - Làng -> 5 -> 7 -> 9. */
    private static final int[] XE_NGUA_DICH = new int[]{9, 7, 5};
    /** Sức ăn tốn nhiều nhất của món chế bằng nguyên liệu cấp 1..6 (trắng 20, đỏ 5, Hoàn mỹ 1). */
    private static final int[] PET_AN_TON = new int[]{20, 20, 20, 20, 5, 1};
    /** LKD 30 / 35 / 40 và mục nâng cấp tương ứng ở NPC -16 (2 = không dùng LKD). */
    private static final short[] PET_LKD_ID = new short[]{11, 66, 245};
    private static final int[] PET_LKD_MUC = new int[]{3, 4, 5};
    private static final short CTT_DA_CAO_CAP = 228;
    private int autoPetHe;
    private int autoPetBaseHe = -1;
    private static final String[] PET_MODE_NAME = new String[]{"Bất kỳ hệ", "Theo hệ", "Full 5 món kích ngũ hành"};
    private static final String[] PET_HE_NAME = new String[]{"Thủy", "Mộc", "Hỏa", "Thổ", "Kim"};
    private static final String[] PET_SLOT_NAME = new String[]{"Nón", "Giáp", "Yên cương", "Bàn đạp", "Hộ uyển", "Tất cả"};
    private static final int[] PET_BASE = new int[]{509, 521, 533, 545, 557};
    private static final int[] PET_MAT = new int[]{1, 2, 3, 4, 4, 5, 5, 5, 5, 7, 9, 11};
    private static final int[] PET_STONE = new int[]{137, 143, 154};
    private static final int[] PET_SRC_COLOR = new int[]{0, 3, 2};
    private static final String[] PET_FILTER_NAME = new String[]{"Bỏ qua tc ma 3%", "Bỏ qua tc vật 3%", "Ma hoặc vật 3%", "Tắt (dòng ẩn ngẫu nhiên)"};
    private static final short[] CARD_BLOCKED_MAP = new short[]{0, 301, 302, 303, 304, 70, 1701, 1702, 1703, 1704, 17, 25, 111, 118, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 225, 226};
    private static final String[] REP_STAT_NAME = new String[]{"Bất kỳ", "Sức mạnh", "Khéo léo", "Tinh thần", "Sức khỏe"};
    private static final String[] REP_HP_NAME = new String[]{"Bất kỳ", "HP", "MP"};
    private static final int[] AUTO_SLOT_OPT = new int[]{-1, 0, 1, 2, 3, 4, 5, 5, 6, 7};
    private static final String[] AUTO_JOB_NAME = new String[]{"Chế đồ", "Nâng cấp lv", "Khóa đồ", "Sửa thuộc tính", "Trang bị thú", "Thần khí", "Thần binh", "Luyện đồ", "Thêm dòng", "Cộng thuộc tính", "Khóa đồ thú", "Nuôi pet", "Học skill pet", "Nâng cấp Hỏa kỳ lân", "Nghiền bột"};
    private int autoQueueNpc;
    private int[] autoQueueMenu;
    private int[] autoQueueOption;
    private int autoQueuePos;
    private short autoCraftedItemID = (short)-1;
    private String[][] name = new String[][]{{"", "Xa phu", "Ch\u1ebf \u0111\u1ed3 nhanh", "D\u00f2ng \u1ea9n", "Auto up", "V\u1eadn ti\u00eau", "V\u1eadn may", "Ti\u1ec7n \u00edch"}, {"C\u00e0i \u0111\u1eb7t", "C\u1ea5u h\u00ecnh", "B\u1ea3n \u0111\u1ed3 l\u1edbn", "B\u1eadt/t\u1eaft giao di\u1ec7n", "H\u01b0\u1edbng d\u1eabn", "\u00e2m thanh", "B\u1eadt/t\u1eaft l\u1eddi m\u1eddi", "B\u1eadt/t\u1eaft auto \u0111\u00e1nh", "Ch\u1ebf \u0111\u1ed9 focus"}, {"B\u1ea3n th\u00e2n", "H\u00e0nh trang", "K\u1ef9 n\u0103ng", "Ti\u1ec1m n\u0103ng", "Trang b\u1ecb", "Th\u00f4ng tin", "Trang b\u1ecb th\u00fa", "C\u00e2y th\u1ea7n", "C\u1ed5 v\u1eadt", "Kh\u00e1c"}, {"Nap Xu"}, {"Nhi\u1ec7m v\u1ee5"}, {"\u0110\u1ed3 s\u00e1t"}, {"Kh\u00e1c", "Tin nh\u1eafn", "Nh\u00f3m", "B\u1ea1n b\u00e8", "Top cao th\u1ee7", "Top \u0111\u1ea1i gia", "B\u1ea3ng Top", "K\u00eanh th\u1ebf gi\u1edbi", "Di\u1ec5n \u0111\u00e0n", "R\u1eddi bang"}, {""}, {"C\u1eeda h\u00e0ng"}, {""}};
    int selected = 0;
    int index = -1;
    int Limit;
    int start = 0;
    int maxOption = 5;
    public int cmtoY;
    public int cmy;
    public int cmdy;
    public int cmvy;
    public int cmyLim;
    public int x;
    public int y;
    public int w;
    public int h;
    public int hText;
    public Command cmdTimban;
    boolean trans = false;
    int pa = 0;
    byte frame;

    public static PauseMenu gI() {
        return me == null ? (me = new PauseMenu()) : me;
    }

    public void initName() {
        if (Canvas.gameScr.mainChar.idClan == -1) {
            this.name[7] = new String[]{"Bang h\u1ed9i", "Top bang h\u1ed9i", "\u0110\u0103ng k\u00fd bang h\u1ed9i"};
        } else if (Canvas.gameScr.mainChar.isMaster == 0) {
            this.name[7] = new String[]{"Bang h\u1ed9i", "Top bang h\u1ed9i", "Nhi\u1ec7m v\u1ee5", "Th\u00e0nh vi\u00ean bang h\u1ed9i", "Th\u00f4ng tin bang h\u1ed9i", "Tin nh\u1eafn bang h\u1ed9i", "K\u1ef9 n\u0103ng", "Chat to\u00e0n bang", "Quy\u00ean g\u00f3p", "Gi\u1ea3i t\u00e1n bang h\u1ed9i"};
            if (Char.dissolved) {
                this.name[7][9] = "Ph\u1ee5c h\u1ed3i bang h\u1ed9i";
            }
        } else {
            this.name[7] = new String[]{"Bang h\u1ed9i", "Top bang h\u1ed9i", "Nhi\u1ec7m v\u1ee5", "Th\u00e0nh vi\u00ean bang h\u1ed9i", "Th\u00f4ng tin bang h\u1ed9i", "Tin nh\u1eafn bang h\u1ed9i", "K\u1ef9 n\u0103ng", "Chat to\u00e0n bang", "Quy\u00ean g\u00f3p", "R\u1eddi bang"};
        }
    }

    public void init() {
        this.w = 128;
        this.h = 83;
        this.hText = 18;
        if (Canvas.instance.hasPointerEvents()) {
            this.w = 170;
            this.h = 130;
            this.hText = 24;
        }
        this.x = (Canvas.w - this.w) / 2;
        this.y = (Canvas.h - this.h) / 2;
        this.cmy = 0;
        this.cmyLim = this.name.length * this.hText - this.h;
    }

    private void showCheDoNhanhMenu() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command("Ch\u1ebf \u0111\u1ed3 lv20", new IAction(){

            public void perform() {
                PauseMenu.this.autoStart(0);
            }
        }));
        vector.addElement(new Command("Ch\u1ebf \u0111\u1ed3 lv35 \u0111\u1ebfn 50", new IAction(){

            public void perform() {
                PauseMenu.this.autoStart(1);
            }
        }));
        vector.addElement(new Command(this.heMaPhap ? "H\u1ec7: Ma ph\u00e1p" : "H\u1ec7: V\u1eadt l\u00fd", new IAction(){

            public void perform() {
                PauseMenu.this.heMaPhap = !PauseMenu.this.heMaPhap;
                PauseMenu.this.showCheDoNhanhMenu();
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Nâng cấp tới lv").append(this.autoGoalLevel).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoGoalLevel = PauseMenu.this.autoGoalLevel >= 85 || PauseMenu.this.autoGoalLevel < 50 ? 50 : PauseMenu.this.autoGoalLevel + 5;
                PauseMenu.this.showCheDoNhanhMenu();
            }
        }));
        vector.addElement(new Command("Nâng cấp đồ có dòng ẩn", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartJob(1);
            }
        }));
        vector.addElement(new Command("Khóa đồ có dòng ẩn...", new IAction(){

            public void perform() {
                PauseMenu.this.showKhoaMenu();
            }
        }));
        vector.addElement(new Command("Sửa thuộc tính đồ có dòng ẩn...", new IAction(){

            public void perform() {
                PauseMenu.this.showSuaMenu();
            }
        }));
        vector.addElement(new Command("Trang bị thú...", new IAction(){

            public void perform() {
                PauseMenu.this.showThuMenu();
            }
        }));
        vector.addElement(new Command("Thần khí...", new IAction(){

            public void perform() {
                PauseMenu.this.showThanKhiMenu();
            }
        }));
        vector.addElement(new Command("Nâng cấp thần binh...", new IAction(){

            public void perform() {
                PauseMenu.this.showThanBinhMenu();
            }
        }));
        vector.addElement(new Command("Nuôi pet...", new IAction(){

            public void perform() {
                PauseMenu.this.showNuoiPetMenu(0);
            }
        }));
        vector.addElement(new Command("Học skill pet...", new IAction(){

            public void perform() {
                PauseMenu.this.showHocSkillMenu(0);
            }
        }));
        vector.addElement(new Command("Nâng cấp Hỏa kỳ lân...", new IAction(){

            public void perform() {
                PauseMenu.this.showHklMenu(0);
            }
        }));
        vector.addElement(new Command("Nghiền bột...", new IAction(){

            public void perform() {
                PauseMenu.this.showNghienMenu(0);
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    private void showKhoaMenu() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(this.autoLockGoal == 20 ? "Mức khóa: 20% trở lên" : "Mức khóa: chỉ 21%", new IAction(){

            public void perform() {
                PauseMenu.this.autoLockGoal = PauseMenu.this.autoLockGoal == 20 ? 21 : 20;
                PauseMenu.this.showKhoaMenu();
            }
        }));
        vector.addElement(new Command("Khóa tất cả đồ có dòng ẩn", new IAction(){

            public void perform() {
                PauseMenu.this.autoLockTtc = false;
                PauseMenu.this.autoStartJob(2);
            }
        }));
        vector.addElement(new Command("Khóa đồ TTC có dòng ẩn", new IAction(){

            public void perform() {
                PauseMenu.this.autoLockTtc = true;
                PauseMenu.this.autoStartJob(2);
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    public static void addLuyenCommands(Vector vector) {
        if (ScreenDapDo.gI().type != 1) {
            return;
        }
        final PauseMenu pauseMenu = PauseMenu.gI();
        if (ScreenDapDo.idLkd >= 0) {
            pauseMenu.autoLkd = ScreenDapDo.idLkd;
            pauseMenu.autoBh = ScreenDapDo.idBH;
            pauseMenu.autoMm = ScreenDapDo.idMM;
        }
        vector.insertElementAt(new Command("Đập full tất cả đồ...", new IAction(){

            public void perform() {
                pauseMenu.showLuyenMenu();
            }
        }), 0);
    }

    private void showLuyenMenu() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(new StringBuilder().append("Đập tới: Cộng ").append(this.autoLuyenTarget).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoLuyenTarget = PauseMenu.this.autoLuyenTarget >= 15 ? 1 : PauseMenu.this.autoLuyenTarget + 1;
                PauseMenu.this.showLuyenMenu();
            }
        }));
        vector.addElement(new Command(this.autoLuyenBuy ? "Tự mua nguyên liệu: Bật" : "Tự mua nguyên liệu: Tắt", new IAction(){

            public void perform() {
                PauseMenu.this.autoLuyenBuy = !PauseMenu.this.autoLuyenBuy;
                PauseMenu.this.showLuyenMenu();
            }
        }));
        vector.addElement(new Command("Bắt đầu đập full", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartLuyen();
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    private static String autoPickName(short[] sArray, String[] stringArray, short s) {
        for (int i = 0; i < sArray.length; ++i) {
            if (sArray[i] != s) continue;
            return stringArray[i];
        }
        return String.valueOf(s);
    }

    private static short autoPickNext(short[] sArray, short s) {
        for (int i = 0; i < sArray.length; ++i) {
            if (sArray[i] != s) continue;
            return sArray[(i + 1) % sArray.length];
        }
        return sArray[0];
    }

    private void autoStartLuyen() {
        this.autoLkdLock = PauseMenu.autoGemCountLock(this.autoLkd, false) < 1 && PauseMenu.autoGemCountLock(this.autoLkd, true) >= 1;
        this.autoBhLock = this.autoBh >= 0 && PauseMenu.autoGemCountLock(this.autoBh, false) < 1 && PauseMenu.autoGemCountLock(this.autoBh, true) >= 1;
        this.autoMmLock = this.autoMm >= 0 && PauseMenu.autoGemCountLock(this.autoMm, false) < 7 && PauseMenu.autoGemCountLock(this.autoMm, true) > PauseMenu.autoGemCountLock(this.autoMm, false);
        Vector vector = ItemInInventory.getAllItem();
        short[] sArray = new short[vector == null ? 0 : vector.size()];
        int n = 0;
        for (int i = 0; i < sArray.length; ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if (itemTemplate == null || itemTemplate.type == 13 || itemInInventory.isSelling || itemInInventory.plusTemplate >= this.autoLuyenTarget) continue;
            sArray[n++] = itemInInventory.itemID;
        }
        if (n == 0) {
            Canvas.startOKDlg(new StringBuilder().append("Không có món nào cần đập lên Cộng ").append(this.autoLuyenTarget).append(".").toString());
            return;
        }
        this.autoTargets = new short[n];
        System.arraycopy(sArray, 0, this.autoTargets, 0, n);
        this.autoTargetPos = 0;
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 7;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoLastAlert = null;
        this.autoPhase = 80;
        this.autoWaitTicks = 0;
    }

    private static int autoGemCountLock(int n, boolean bl) {
        Vector vector = bl ? MainChar.gemItemKhoa : MainChar.gemItem;
        int n2 = 0;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            GemTemplate gemTemplate = (GemTemplate)vector.elementAt(i);
            if (gemTemplate.id != n) continue;
            n2 += gemTemplate.number;
        }
        return n2;
    }

    private boolean autoLuyenBuyNow() {
        short[] sArray = new short[]{this.autoLkd, this.autoBh, this.autoMm};
        int[] nArray = new int[]{100, 100, 600};
        boolean bl = false;
        for (int i = 0; i < 3; ++i) {
            if (sArray[i] < 0 || sArray[i] > 10) continue;
            int n = nArray[i] - PauseMenu.autoGemCountLock(sArray[i], false);
            // the server drops (and desyncs on) any client packet of 1024+ bytes: 2 bytes per unit,
            // so buy in chunks of at most 300 units
            while (n > 0) {
                int n2 = Math.min(n, 300);
                Vector vector = new Vector();
                GemTemp gemTemp = new GemTemp();
                gemTemp.id = sArray[i];
                gemTemp.number = (short)n2;
                vector.addElement(gemTemp);
                GameService.gI().buyGemItem(vector);
                n -= n2;
                bl = true;
            }
        }
        return bl;
    }

    private static boolean autoBagFull() {
        return 42 * Canvas.gameScr.mainChar.limItemBag - Canvas.gameScr.mainChar.FullInventory() - (MainChar.gemItemKhoa == null ? 0 : MainChar.gemItemKhoa.size()) <= 0;
    }

    private boolean autoLuyenLow() {
        if (this.autoLkd >= 0 && this.autoLkd <= 10 && PauseMenu.autoGemCountLock(this.autoLkd, false) < 10) {
            return true;
        }
        if (this.autoBh >= 0 && this.autoBh <= 10 && PauseMenu.autoGemCountLock(this.autoBh, false) < 10) {
            return true;
        }
        return this.autoMm >= 0 && this.autoMm <= 10 && PauseMenu.autoGemCountLock(this.autoMm, false) < 60;
    }

    private boolean autoLuyenShort() {
        if (PauseMenu.autoGemCountLock(this.autoLkd, this.autoLkdLock) < 1) {
            return true;
        }
        if (this.autoBh >= 0 && PauseMenu.autoGemCountLock(this.autoBh, this.autoBhLock) < 1) {
            return true;
        }
        return this.autoMm >= 0 && PauseMenu.autoGemCountLock(this.autoMm, this.autoMmLock) < (this.autoBh >= 0 ? 6 : 7);
    }

    public static boolean showThemDongMenu(short s, int n, int n2, int n3, int n4, int n5) {
        PauseMenu pauseMenu = PauseMenu.gI();
        pauseMenu.autoThemItem = s;
        pauseMenu.autoThemBotLock = n3;
        pauseMenu.autoThemDaLock = n5;
        ItemInInventory itemInInventory = pauseMenu.autoFindItem(s);
        ItemTemplate itemTemplate = itemInInventory == null ? null : Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        pauseMenu.autoThemWeapon = itemTemplate != null && (itemTemplate.type >= 3 && itemTemplate.type <= 9 || itemTemplate.type == 12);
        pauseMenu.showThemMain();
        Canvas.menu.selected = -1; // như showCongTTMenu: tránh bấm "Đập" là chạy luôn mục đầu
        return true;
    }

    private void showThemMain() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command("Thêm dòng 1 lần", new IAction(){

            public void perform() {
                GameService.gI().doRequestThemThuocTinh(PauseMenu.this.autoThemItem, 5, 249, PauseMenu.this.autoThemBotLock, 250, PauseMenu.this.autoThemDaLock);
            }
        }));
        vector.addElement(new Command("Auto thêm dòng (giữ dòng đã chọn)", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartThem(false);
            }
        }));
        vector.addElement(new Command("Auto thêm dòng full (tất cả đồ)", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartThem(true);
            }
        }));
        vector.addElement(new Command("Chọn dòng muốn giữ...", new IAction(){

            public void perform() {
                PauseMenu.this.showThemPick(0);
            }
        }));
        vector.addElement(new Command(this.autoThemBuy ? "Tự mua đá tiến giai: Bật" : "Tự mua đá tiến giai: Tắt", new IAction(){

            public void perform() {
                PauseMenu.this.autoThemBuy = !PauseMenu.this.autoThemBuy;
                PauseMenu.this.showThemMain();
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    private void showThemPick(int n) {
        Vector<Command> vector = new Vector<Command>();
        int n2 = this.autoThemWeapon ? 0 : 3;
        int n3 = this.autoThemWeapon ? 3 : 12;
        int n4 = 0;
        for (int i = 12; i < 17; ++i) {
            for (int j = 0; j < 2; ++j) {
                final int n5 = i * 2 + j;
                final int n6 = n4++;
                vector.addElement(new Command(new StringBuilder().append(this.autoThemChon[n5] ? "(x) " : "( ) ").append(TD_NAME[i]).append(j == 1 ? " max" : "").toString(), new IAction(){

                    public void perform() {
                        PauseMenu.this.autoThemChon[n5] = !PauseMenu.this.autoThemChon[n5];
                        PauseMenu.this.showThemPick(n6);
                    }
                }));
            }
        }
        vector.addElement(new Command("Xong", new IAction(){

            public void perform() {
                PauseMenu.this.showThemMain();
            }
        }));
        int n7 = Menu.cmy;
        int n8 = Menu.cmtoY;
        Canvas.menu.startAt(vector, 3);
        if (n > 0) {
            Canvas.menu.selected = n;
            Menu.cmy = n7;
            Menu.cmtoY = n8;
        }
    }

    private boolean autoThemAnyChosen() {
        int n = this.autoThemWeapon ? 0 : 3;
        int n2 = this.autoThemWeapon ? 3 : 12;
        for (int i = 24; i < 34; ++i) {
            if (!this.autoThemChon[i]) continue;
            return true;
        }
        return false;
    }

    private int autoThemLines(ItemInInventory itemInInventory, boolean[] blArray) {
        int n = Math.max(0, Math.min(2, (itemInInventory.level - 50) / 10));
        int n2 = 0;
        blArray[0] = false;
        blArray[1] = true;
        for (int i = 0; i < TD_ID.length; ++i) {
            int n3 = PauseMenu.autoAttVal(itemInInventory, TD_ID[i]);
            if (n3 <= 0 || i >= 12 && this.autoThemStart[i - 12] > 0) continue;
            ++n2;
            boolean bl = i >= 12 && (this.autoThemChon[i * 2] || this.autoThemChon[i * 2 + 1] && n3 >= TD_MAX[i][n]);
            if (bl) {
                blArray[0] = true;
                continue;
            }
            blArray[1] = false;
        }
        return n2;
    }

    private boolean autoThemNext() {
        ++this.autoTargetPos;
        if (this.autoTargets == null || this.autoTargetPos >= this.autoTargets.length) {
            return false;
        }
        this.autoThemItem = this.autoTargets[this.autoTargetPos];
        this.autoThemSnap();
        this.autoPhase = 90;
        return true;
    }

    private int autoThemStock(int n, boolean bl) {
        return PauseMenu.autoGemCountLock(n, bl);
    }

    private void autoThemSnap() {
        ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
        for (int i = 0; i < 5; ++i) {
            this.autoThemStart[i] = itemInInventory == null ? 0 : PauseMenu.autoAttVal(itemInInventory, 28 + i);
        }
    }

    private void autoStartThem(boolean bl) {
        if (!this.autoThemAnyChosen()) {
            Canvas.startOKDlg("Hãy vào \"Chọn dòng muốn giữ...\" để tick dòng ẩn cần giữ trước.");
            return;
        }
        this.autoThemFull = bl;
        if (bl) {
            Vector vector = ItemInInventory.getAllItem();
            short[] sArray = new short[vector == null ? 0 : vector.size()];
            int n = 0;
            for (int i = 0; i < sArray.length; ++i) {
                ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
                ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                if (itemTemplate == null || itemTemplate.type >= 14 && itemTemplate.type <= 18 || itemInInventory.colorName != 1 || itemInInventory.hangItem != 1 || itemInInventory.lock == 0 || itemInInventory.level < 50 || itemInInventory.isSelling) continue;
                sArray[n++] = itemInInventory.itemID;
            }
            if (n == 0) {
                Canvas.startOKDlg("Không có món nào thêm dòng được (cần Hoàn mỹ, Nhất phẩm, đã khóa, cấp 50 trở lên).");
                return;
            }
            this.autoTargets = new short[n];
            System.arraycopy(sArray, 0, this.autoTargets, 0, n);
            this.autoTargetPos = 0;
            this.autoThemItem = this.autoTargets[0];
        }
        this.autoThemSnap();
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 8;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPhase = 90;
        this.autoWaitTicks = 0;
    }

    public static boolean showCongTTMenu(short s, int n, int n2) {
        PauseMenu pauseMenu = PauseMenu.gI();
        pauseMenu.autoCttItem = s;
        pauseMenu.autoCttDa = (short)n;
        pauseMenu.autoCttLock = n2 == 1;
        pauseMenu.showCongTTMain(0);
        // không chọn sẵn mục nào: cú bấm "Đập" vừa mở menu có thể tới thêm 1 sự kiện phím mềm
        // trái ("Chọn" cùng chỗ) - nếu mục 0 đang được chọn thì sẽ "Đập 1 lần" ngoài ý muốn
        Canvas.menu.selected = -1;
        return true;
    }

    private void showCongTTMain(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command("Đập 1 lần", new IAction(){

            public void perform() {
                AutoGuard.dapMotLan();
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Loại: ").append(this.autoCttLoai < 0 ? "Bất kỳ" : CTT_NAME[this.autoCttLoai]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoCttLoai = PauseMenu.this.autoCttLoai >= CTT_NAME.length - 1 ? -1 : PauseMenu.this.autoCttLoai + 1;
                PauseMenu.this.showCongTTMain(1);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Mức: ").append(PauseMenu.cttMucText(this.autoCttMin)).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoCttMin = PauseMenu.this.autoCttMin <= 3 ? 5 : PauseMenu.this.autoCttMin - 1; // chỉ 0.5 | 0.4-0.5 | 0.3-0.5
                PauseMenu.this.showCongTTMain(2);
            }
        }));
        vector.addElement(new Command("Auto món đang đặt", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartCongTT(false);
            }
        }));
        vector.addElement(new Command("Auto tất cả đồ", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartCongTT(true);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 3, chon);
    }

    public static boolean showKhoaThuMenu(short s, int n, int n2) {
        PauseMenu pauseMenu = PauseMenu.gI();
        pauseMenu.autoKtItem = s;
        pauseMenu.autoKtDa = (short)n;
        pauseMenu.autoKtLock = n2 == 1;
        pauseMenu.showKhoaThuMain(0);
        Canvas.menu.selected = -1; // như showCongTTMenu
        return true;
    }

    private void showKhoaThuMain(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command("Khóa 1 lần", new IAction(){

            public void perform() {
                AutoGuard.dapMotLan();
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Mức: ").append(this.autoKtMuc >= 21 ? "21%" : "20-21%").toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoKtMuc = PauseMenu.this.autoKtMuc >= 21 ? 20 : 21;
                PauseMenu.this.showKhoaThuMain(1);
            }
        }));
        vector.addElement(new Command("Auto khóa món đang đặt", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartKhoaThu(false);
            }
        }));
        vector.addElement(new Command("Auto khóa tất cả", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartKhoaThu(true);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 3, chon);
    }

    private static int ktGiaTri(ItemInInventory itemInInventory) {
        return Math.max(PauseMenu.autoAttVal(itemInInventory, 58), Math.max(PauseMenu.autoAttVal(itemInInventory, 59), PauseMenu.autoAttVal(itemInInventory, 60)));
    }

    private boolean ktDat(ItemInInventory itemInInventory) {
        return itemInInventory != null && PauseMenu.ktGiaTri(itemInInventory) >= this.autoKtMuc;
    }

    /** Đồ thú Hoàn mỹ Nhất phẩm (đúng danh sách màn Khóa đồ thú), không đang bán, chưa đạt. */
    private boolean ktCanKhoa(ItemInInventory itemInInventory) {
        return itemInInventory.colorName == 1 && itemInInventory.hangItem == 1 && ItemInInventory.isAnimalArmor(itemInInventory.getTemplate().type) && !itemInInventory.isSelling && !this.ktDat(itemInInventory);
    }

    private void autoStartKhoaThu(boolean bl) {
        if (this.autoKtDa != KT_XUONG_CAP6) {
            Canvas.startOKDlg("Chỉ xương cấp 6 mới khóa ra được 20-21%. Hãy đặt xương cấp 6.");
            return;
        }
        ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
        if (!bl && itemInInventory == null) {
            Canvas.startOKDlg("Không thấy món đồ trong hành trang.");
            return;
        }
        this.autoKtFull = bl;
        if (bl) {
            // món đang đặt (nếu còn cần khóa) làm trước, rồi lần lượt các món còn lại
            Vector vector = Char.inventory;
            short[] sArray = new short[(vector == null ? 0 : vector.size()) + 1];
            int n = 0;
            if (itemInInventory != null && this.ktCanKhoa(itemInInventory)) {
                sArray[n++] = itemInInventory.itemID;
            }
            for (int i = 0; vector != null && i < vector.size(); ++i) {
                ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
                if (itemInInventory2 == itemInInventory || !this.ktCanKhoa(itemInInventory2)) continue;
                sArray[n++] = itemInInventory2.itemID;
            }
            if (n == 0) {
                Canvas.startOKDlg(new StringBuilder().append("Không có đồ thú Hoàn mỹ Nhất phẩm nào cần khóa tới ").append(this.autoKtMuc >= 21 ? "21%" : "20-21%").append(".").toString());
                return;
            }
            this.autoTargets = new short[n];
            System.arraycopy(sArray, 0, this.autoTargets, 0, n);
            this.autoTargetPos = 0;
            this.autoKtItem = this.autoTargets[0];
        }
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 10;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPhase = 97;
        this.autoWaitTicks = 0;
    }

    /** Khóa tất cả: sang món kế tiếp chưa đạt; false nếu đã hết đồ. */
    private boolean autoKtNext() {
        while (this.autoTargets != null && ++this.autoTargetPos < this.autoTargets.length) {
            ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
            if (itemInInventory == null || this.ktDat(itemInInventory)) continue;
            this.autoKtItem = this.autoTargets[this.autoTargetPos];
            this.autoPhase = 97;
            return true;
        }
        return false;
    }

    private String ktTongKet() {
        return new StringBuilder().append("Auto khóa tất cả đồ thú: xong ").append(this.autoFound).append("/").append(this.autoTargets == null ? 0 : this.autoTargets.length).append(" món").toString();
    }

    private void showNuoiPetMenu(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(this.autoPetTuHon ? new StringBuilder().append("Thức ăn: Tụ hồn đan (").append(PauseMenu.tuHonDanCo()).append(")").toString() : "Thức ăn: Đồ chế", new IAction(){

            public void perform() {
                PauseMenu.this.autoPetTuHon = !PauseMenu.this.autoPetTuHon;
                PauseMenu.this.showNuoiPetMenu(0);
            }
        }));
        if (!this.autoPetTuHon) {
        vector.addElement(new Command(new StringBuilder().append("Loại nguyên liệu: ").append(this.autoPetLoai < 0 ? "Tất cả" : PET_LOAI_TEN[this.autoPetLoai]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoPetLoai = PauseMenu.this.autoPetLoai >= PET_LOAI_TEN.length - 1 ? -1 : PauseMenu.this.autoPetLoai + 1;
                PauseMenu.this.showNuoiPetMenu(1);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Nguyên liệu sơ cấp: cấp ").append(this.autoPetCapSo + 1).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoPetCapSo = (PauseMenu.this.autoPetCapSo + 1) % 6;
                PauseMenu.this.showNuoiPetMenu(2);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Nguyên liệu cao cấp: cấp ").append(this.autoPetCapNL + 1).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoPetCapNL = (PauseMenu.this.autoPetCapNL + 1) % 6;
                PauseMenu.this.showNuoiPetMenu(3);
            }
        }));
        }
        vector.addElement(new Command("Bắt đầu nuôi pet", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartNuoiPet();
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    private static int tuHonDanCo() {
        MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        return mainChar == null || mainChar.potions == null || mainChar.potions.length <= POTION_TU_HON_DAN ? 0 : mainChar.potions[POTION_TU_HON_DAN];
    }

    /** Sức ăn tốn nhiều nhất cho 1 miếng: Tụ hồn đan 1, đồ chế theo cấp nguyên liệu cao cấp. */
    private int petAnTon() {
        return this.autoPetTuHon ? 1 : PET_AN_TON[this.autoPetCapNL];
    }

    private static String petInfo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null || Canvas.gameScr.mainChar.myPet == null) {
            return null;
        }
        return Canvas.gameScr.mainChar.myPet.infoPet;
    }

    /** Đọc số nguyên ngay sau "nhan" trong chuỗi thông tin pet, -1 nếu không có. */
    private static int petDocSo(String string, String string2) {
        if (string == null) {
            return -1;
        }
        int n = string.indexOf(string2);
        if (n < 0) {
            return -1;
        }
        n += string2.length();
        while (n < string.length() && string.charAt(n) == ' ') {
            ++n;
        }
        int n2 = n;
        while (n2 < string.length() && string.charAt(n2) >= '0' && string.charAt(n2) <= '9') {
            ++n2;
        }
        if (n2 == n) {
            return -1;
        }
        return Integer.parseInt(string.substring(n, n2));
    }

    /** "- Sức ăn : 120/300" -> 120. */
    private static int petSucAn() {
        return PauseMenu.petDocSo(PauseMenu.petInfo(), "Sức ăn :");
    }

    /** "- Cấp độ: 9+99.9%" -> 9. */
    private static int petCap() {
        return PauseMenu.petDocSo(PauseMenu.petInfo(), "Cấp độ:");
    }

    /** "- Cấp độ: 9+99.9%" -> 999 (phần nghìn). */
    private static int petPhanTram() {
        String string = PauseMenu.petInfo();
        int n = PauseMenu.petDocSo(string, "Cấp độ:");
        if (n < 0) {
            return -1;
        }
        int n2 = string.indexOf("Cấp độ:");
        int n3 = string.indexOf('+', n2);
        int n4 = n3 < 0 ? -1 : string.indexOf('.', n3);
        if (n3 < 0 || n4 < 0) {
            return -1;
        }
        try {
            return Integer.parseInt(string.substring(n3 + 1, n4).trim()) * 10 + Integer.parseInt(string.substring(n4 + 1, n4 + 2));
        }
        catch (Exception exception) {
            return -1;
        }
    }

    /** Pet đứng ở 9/19/29/39 + 99.9%: server không cho ăn tới khi nâng cấp. */
    /** Mốc 49.99% (từ cấp 40 trở lên): không nâng cấp ở NPC được, phải tiến hoá. */
    private static boolean petCanTienHoa() {
        int n = PauseMenu.petCap();
        return n >= 40 && (n + 1) % 10 == 0 && PauseMenu.petPhanTram() >= 999;
    }

    private static boolean petCanNang() {
        int n = PauseMenu.petCap();
        return n >= 0 && n < 40 && (n + 1) % 10 == 0 && PauseMenu.petPhanTram() >= 999;
    }

    /** Tên món trong danh sách "Cho pet ăn" - y hệt Item.getNameItemPetEat ở server. */
    private static String petTenMonAn(ItemInInventory itemInInventory) {
        ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        if (itemTemplate == null) {
            return null;
        }
        String string = itemTemplate.name;
        String[] stringArray = new String[]{"ma pháp", "vậy lý"};
        if (itemInInventory.magic_physic >= 0 && itemInInventory.magic_physic < 2) {
            string = new StringBuilder().append(string).append(" ").append(stringArray[itemInInventory.magic_physic]).toString();
        }
        String[] stringArray2 = new String[]{"", "Hoàn mỹ", "Đỏ", "Xanh"};
        if (itemInInventory.colorName > 0 && itemInInventory.colorName < 4) {
            string = new StringBuilder().append(string).append(" ").append(stringArray2[itemInInventory.colorName]).toString();
        }
        String[] stringArray3 = new String[]{"", "Nhất phẩm", "Nhị phẩm", "Tam phẩm", "Tứ phẩm", "NGũ phẩm"};
        if (itemInInventory.hangItem > 1 && itemInInventory.hangItem < 6) {
            string = new StringBuilder().append(string).append(" ").append(stringArray3[itemInInventory.hangItem]).toString();
        }
        if (itemInInventory.plusTemplate > 0) {
            string = new StringBuilder().append(string).append(" +").append(itemInInventory.plusTemplate).toString();
        }
        return string;
    }

    /** Bỏ khỏi lô các món không còn trong hành trang (đã ăn); trả số món còn lại. */
    private int petConLai() {
        for (int i = this.autoPetLo.size() - 1; i >= 0; --i) {
            if (this.autoFindItem((short)((Integer)this.autoPetLo.elementAt(i)).intValue()) != null) continue;
            this.autoPetLo.removeElementAt(i);
        }
        return this.autoPetLo.size();
    }

    private boolean petTrongLo(short s) {
        for (int i = 0; i < this.autoPetLo.size(); ++i) {
            if (((Integer)this.autoPetLo.elementAt(i)).intValue() != s) continue;
            return true;
        }
        return false;
    }

    /** Thêm vào lô các món mới xuất hiện so với snapshot (phase 7 / 115); trả số món mới. */
    private int petMonMoi() {
        Vector vector = Char.inventory;
        int n = 0;
        block0: for (int i = 0; vector != null && i < vector.size(); ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            for (int j = 0; this.autoSnapshot != null && j < this.autoSnapshot.length; ++j) {
                if (this.autoSnapshot[j] == itemInInventory.itemID) continue block0;
            }
            ++n;
            if (this.petTrongLo(itemInInventory.itemID)) continue;
            this.autoPetLo.addElement(new Integer(itemInInventory.itemID));
        }
        return n;
    }

    /** Xin danh sách "Cho pet ăn" (NPC -16 mục 1) và ghi nhận đang chờ thêm 1 danh sách. */
    private void xinDsAn() {
        if (this.autoPetDsCho < this.autoPetDsNhan) {
            this.autoPetDsCho = this.autoPetDsNhan;
        }
        ++this.autoPetDsCho;
        GameService.gI().doMenuOption(-16, (byte)0, 1);
    }

    /**
     * Bắt đầu lượt mới: bỏ qua các lần xin cũ chưa được trả lời (vd NPC không nhận lần trước) -
     * nếu không bộ đếm cộng dồn và lượt sau chờ mãi một danh sách không bao giờ tới.
     */
    private void batDauDs() {
        this.autoPetDsCho = this.autoPetDsNhan;
        this.autoPetDsAn = null;
    }

    /** Đã nhận đủ các danh sách đã xin (đang cầm danh sách mới nhất). */
    private boolean dsSan() {
        return this.autoPetDsAn != null && this.autoPetDsNhan >= this.autoPetDsCho;
    }

    /** Chế được ngay bao nhiêu món theo công thức (sức ăn, ô trống, xu, nguyên liệu), tối đa n. */
    private int petCheDuoc(int n) {
        int n2 = PauseMenu.petSucAn();
        int n3 = Math.min(n, (this.autoPetSauRelog ? 300 : 300 - Math.max(0, n2)) / this.petAnTon());
        n3 = Math.min(n3, PauseMenu.petOTrong());
        n3 = Math.min(n3, (int)(Canvas.gameScr.mainChar.charXu / 5000L));
        for (int i = 0; i < this.autoPetFillMau.length; ++i) {
            for (int j = 0; j < this.autoPetFillMau[i].length; ++j) {
                if (this.autoPetFillMau[i][j] <= 0) continue;
                int n4 = PauseMenu.autoGemCountLock(this.autoPetNLId[i] + j, false) + PauseMenu.autoGemCountLock(this.autoPetNLId[i] + j, true);
                n3 = Math.min(n3, n4 / this.autoPetFillMau[i][j]);
            }
        }
        return n3;
    }

    /** Gửi liền n lần "menu NPC -8 + chế" theo công thức (snapshot trước để nhận ra món mới). */
    private void petGuiChe(int n) {
        this.autoSnapshot = this.autoInventoryIds();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < this.autoPetQMenu.length; ++j) {
                GameService.gI().doMenuOption(this.autoPetQNpc, (byte)this.autoPetQMenu[j], this.autoPetQOpt[j]);
            }
            GameService.gI().doCheDo((short)this.autoPetFillMau.length, this.autoPetIdTemp, this.autoPetFillMau);
        }
    }

    private static int petOTrong() {
        MainChar mainChar = Canvas.gameScr.mainChar;
        return 42 * mainChar.limItemBag - mainChar.FullInventory() - (MainChar.gemItemKhoa == null ? 0 : MainChar.gemItemKhoa.size());
    }

    /**
     * Cho ăn cả nhóm món cùng tên trong lô, gửi liền không chờ. Server chọn theo VỊ TRÍ trong
     * danh sách "Cho pet ăn" nên CHỈ làm khi chắc chắn: mọi đồ trong hành trang mang tên đó
     * đều là món của lô, và số dòng trùng tên trong danh sách khớp đúng số món đó.
     *
     * Chỉ gửi cho DÃY LIỀN NHAU đầu tiên và luôn chọn CÙNG vị trí p0 (đầu dãy): ăn xong 1 món
     * thì món sau dồn lên p0; bị từ chối (cần nâng cấp, đầy) thì món đó vẫn ở p0. Số lệnh =
     * số món trong dãy nên p0 lúc nào cũng là món của lô. KHÔNG dùng "vị trí gốc - j" cho dãy
     * rời: một lệnh bị từ chối là các vị trí sau lệch sang đồ thật của người chơi.
     * CHỈ TÍNH, chưa gửi: trả p0 * 256 + số món trong dãy, -1 = không chắc (gửi bằng petGuiAn).
     */
    private int petTinhLo() {
        ItemInInventory itemInInventory = this.autoFindItem((short)((Integer)this.autoPetLo.elementAt(0)).intValue());
        String string = itemInInventory == null ? null : PauseMenu.petTenMonAn(itemInInventory);
        if (string == null || this.autoPetDsAn == null) {
            return -1;
        }
        int n = 0;
        Vector vector = Char.inventory;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
            if (!string.equals(PauseMenu.petTenMonAn(itemInInventory2))) continue;
            if (!this.petTrongLo(itemInInventory2.itemID)) {
                // đồ cùng tên nhưng không do lô này chế (vd còn từ lần chạy trước): cấp 20/21
                // đúng là đồ chế để cho ăn -> gộp vào lô cho ăn luôn (người chơi yêu cầu);
                // cấp cao hơn là đồ thật -> không chắc, dừng
                if (itemInInventory2.level > 21) {
                    return -1;
                }
                this.autoPetLo.addElement(new Integer(itemInInventory2.itemID));
            }
            ++n;
        }
        int[] nArray = new int[this.autoPetDsAn.length];
        int n2 = 0;
        for (int i = 0; i < this.autoPetDsAn.length; ++i) {
            if (!string.equals(this.autoPetDsAn[i])) continue;
            nArray[n2++] = i;
        }
        if (n2 != n || n2 == 0) {
            return -1;
        }
        int n3 = nArray[0];
        if (n3 > 127) {
            return -1;
        }
        int n4 = 1;
        while (n4 < n2 && nArray[n4] == n3 + n4) {
            ++n4;
        }
        return n3 * 256 + n4;
    }

    /** Gửi m lệnh cho ăn, đều ở vị trí p0 (kết quả của petTinhLo). */
    private void petGuiAn(int n) {
        int n2 = n / 256;
        int n3 = n % 256;
        for (int i = 0; i < n3; ++i) {
            if (i > 0) {
                this.xinDsAn();
            }
            GameService.gI().doMenuOption(-16, (byte)1, n2);
        }
    }

    /** Cấp đã chọn cho 1 loại nguyên liệu: sơ cấp (id < 103) hay cao cấp. */
    private int petCapCua(int n) {
        if (this.autoJob == 14) {
            return n < 103 ? this.autoNbCapSo : this.autoNbCapCao;
        }
        return n < 103 ? this.autoPetCapSo : this.autoPetCapNL;
    }

    /** Cặp nguyên liệu đang chọn của job đang chạy (Nuôi pet / Nghiền bột). */
    private int nlLoai() {
        return this.autoJob == 14 ? this.autoNbLoai : this.autoPetLoai;
    }

    /** Lọc đúng 2 nguyên liệu của món ở cấp đã chọn; thiếu thì null (thử món khác). */
    private byte[][] petFillCap() {
        WindowInfoScr windowInfoScr = WindowInfoScr.gI();
        Vector vector = windowInfoScr.listCheDo;
        Vector vector2 = windowInfoScr.listTemp;
        if (vector == null || vector.size() == 0 || vector2 == null) {
            return null;
        }
        // món này không dùng đúng cặp nguyên liệu đã chọn -> null (thử món kế tiếp)
        for (int i = 0; this.nlLoai() >= 0 && i < vector.size(); ++i) {
            int n = ((Mineral)vector.elementAt(i)).idTemplate;
            if (n == PET_LOAI_SO[this.nlLoai()] || n == PET_LOAI_CAO[this.nlLoai()]) continue;
            return null;
        }
        byte[][] byArray = new byte[vector.size()][6];
        for (int i = 0; i < vector.size(); ++i) {
            Mineral mineral = (Mineral)vector.elementAt(i);
            int n = 0;
            for (int j = 0; j < vector2.size(); ++j) {
                GemTemplate gemTemplate = (GemTemplate)vector2.elementAt(j);
                if (gemTemplate.id != mineral.idTemplate + this.petCapCua(mineral.idTemplate)) continue;
                n += gemTemplate.number;
            }
            if (n < mineral.number) {
                return null;
            }
            byArray[i][this.petCapCua(mineral.idTemplate)] = (byte)mineral.number;
        }
        return byArray;
    }

    private void autoStartNuoiPet() {
        if (PauseMenu.petInfo() == null || PauseMenu.petCap() < 0) {
            Canvas.startOKDlg("Bạn phải mang theo pet.");
            return;
        }
        if (PauseMenu.petCanTienHoa()) {
            Canvas.startOKDlg("Pet đã đạt 49.99%, cần tiến hoá (dùng Huyết linh thảo) mới luyện tiếp được.");
            return;
        }
        if (this.autoPetTuHon && PauseMenu.tuHonDanCo() <= 0) {
            Canvas.startOKDlg("Bạn không có Tụ hồn đan.");
            return;
        }
        this.autoPetLo.removeAllElements();
        this.autoPetCoCongThuc = false;
        this.autoPetDaAn = 0;
        this.autoPetDaNang = 0;
        this.autoPetRelog = 0;
        this.autoPetSauRelog = false;
        this.autoDuong = null;
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 11;
        this.autoMode = 0;
        this.autoSlot = 0;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPhase = 140;
        this.autoWaitTicks = 0;
    }

    /** Đang ở map m (so theo nhóm map của client, vì một map lớn có nhiều map con). */
    static boolean oMap(int n) {
        GameScr gameScr = Canvas.gameScr;
        if (gameScr == null) {
            return false;
        }
        if (gameScr.map == n) {
            return true;
        }
        short s = gameScr.getIndexArrMap();
        return s >= 0 && s == gameScr.getIndexArrMap(n);
    }

    private static boolean oTrongGame() {
        return Canvas.currentScreen == Canvas.gameScr && Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Tilemap.type != null && Tilemap.w > 0;
    }

    /**
     * Ô không đi qua được - đúng luật va chạm của client ((type & 2) == 2). Ô cổng chuyển map
     * (type >= 2000000000, xem Tilemap.loadMap) cũng không đi lên: tới sát cổng là đủ.
     */
    static boolean laTuong(int n, int n2) {
        if (n < 0 || n2 < 0 || n >= Tilemap.w || n2 >= Tilemap.h) {
            return true;
        }
        int n3 = n2 * Tilemap.w + n;
        if (n3 >= Tilemap.type.length) {
            return true;
        }
        int n4 = Tilemap.type[n3];
        if (n4 >= 2000000000 || (n4 & 2) == 2) {
            return true;
        }
        // vùng chặn của NPC (vd tượng lân sự kiện giữa làng): không có trong dữ liệu ô nhưng
        // GameScr.doActorMove vẫn chặn bước đi -> trước đây BFS đi xuyên qua rồi đứng kẹt
        return Canvas.gameScr != null && Canvas.gameScr.checkMoveLimit(n * 16 + 8, n2 * 16 + 8);
    }

    /**
     * BFS 4 hướng trên toàn bản đồ từ chỗ đứng tới ô đích bất kỳ, rồi chỉ giữ các GÓC đổi hướng
     * và ô cuối - đi bằng phím mũi tên nên mỗi đoạn là một đường thẳng ngang/dọc. null = không có đường.
     */
    static int[] timDuong(boolean[] blArray) {
        int n = Tilemap.w;
        int n2 = Tilemap.h;
        int n3 = Canvas.gameScr.mainChar.x / 16;
        int n4 = Canvas.gameScr.mainChar.y / 16;
        if (n3 < 0 || n4 < 0 || n3 >= n || n4 >= n2) {
            return null;
        }
        int[] nArray = new int[n * n2];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = -2;
        }
        int[] nArray2 = new int[n * n2];
        int n5 = 0;
        int n6 = 0;
        int n7 = n4 * n + n3;
        nArray[n7] = -1;
        nArray2[n6++] = n7;
        int n8 = -1;
        int[] nArray3 = new int[]{1, -1, 0, 0};
        int[] nArray4 = new int[]{0, 0, 1, -1};
        while (n5 < n6) {
            int n9 = nArray2[n5++];
            if (blArray[n9]) {
                n8 = n9;
                break;
            }
            int n10 = n9 % n;
            int n11 = n9 / n;
            for (int i = 0; i < 4; ++i) {
                int n12 = n10 + nArray3[i];
                int n13 = n11 + nArray4[i];
                if (PauseMenu.laTuong(n12, n13)) continue;
                int n14 = n13 * n + n12;
                if (nArray[n14] != -2) continue;
                nArray[n14] = n9;
                nArray2[n6++] = n14;
            }
        }
        if (n8 < 0) {
            return null;
        }
        int n15 = 0;
        for (int i = n8; i != -1; i = nArray[i]) {
            ++n15;
        }
        int[] nArray5 = new int[n15];
        for (int i = n8; i != -1; i = nArray[i]) {
            nArray5[--n15] = i;
        }
        Vector vector = new Vector();
        for (int i = 1; i < nArray5.length; ++i) {
            if (i < nArray5.length - 1 && nArray5[i] - nArray5[i - 1] == nArray5[i + 1] - nArray5[i]) continue;
            vector.addElement(new Integer(nArray5[i]));
        }
        int[] nArray6 = new int[vector.size()];
        for (int i = 0; i < nArray6.length; ++i) {
            nArray6[i] = (Integer)vector.elementAt(i);
        }
        return nArray6;
    }

    /** Đích là hình chữ nhật ô (x1,y1)-(x2,y2). */
    private void datDich(int n, int n2, int n3, int n4) {
        boolean[] blArray = new boolean[Tilemap.w * Tilemap.h];
        for (int i = Math.max(0, n2); i <= n4 && i < Tilemap.h; ++i) {
            for (int j = Math.max(0, n); j <= n3 && j < Tilemap.w; ++j) {
                blArray[i * Tilemap.w + j] = !PauseMenu.laTuong(j, i);
            }
        }
        this.datDich(blArray);
    }

    private void datDich(boolean[] blArray) {
        this.autoDich = blArray;
        this.autoDiPhim = false;
        this.autoDiBiKeo = 0;
        this.autoCoKeo = false;
        this.autoDuong = null;
        this.autoDuongPos = 0;
        this.autoDiDung = 0;
        this.autoDiTinhLai = 0;
    }

    /**
     * Cổng (lối ra) này có đưa tới map m không: trả vị trí điểm đến m trong bảng Res.getLocation,
     * -1 nếu không. CHÚ Ý cMLocation.toM KHÔNG phải id map đích mà là SỐ THỨ TỰ LỐI RA của
     * bản đồ (0, 1, 2...); map đích thật nằm trong Res.getLocation(nhóm map, toM) - mỗi điểm
     * đến 3 số (map, x, y). Vd map 9: lối ra 1 (mép trên) -> {201, 39, 90, ...} = Thành.
     */
    static int congDen(CMLocation cMLocation, int n) {
        short[] sArray = Res.getLocation(Canvas.gameScr.getIndexArrMap(), cMLocation.toM);
        for (int i = 0; sArray != null && i * 3 < sArray.length; ++i) {
            if (sArray[i * 3] != n) continue;
            return i;
        }
        return -1;
    }

    /** Cổng dẫn tới map m trên bản đồ hiện tại (lấy từ dữ liệu bản đồ của client). */
    static CMLocation congToi(int n) {
        Vector vector = Tilemap.mapchangeLocations;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            CMLocation cMLocation = (CMLocation)vector.elementAt(i);
            if (PauseMenu.congDen(cMLocation, n) < 0) continue;
            return cMLocation;
        }
        return null;
    }

    /** Ô trống sát (8 hướng) các ô cổng dẫn tới map m; null nếu bản đồ không có cổng đó. */
    static boolean[] dichSatCong(int n) {
        Vector vector = Tilemap.mapchangeLocations;
        if (vector == null) {
            return null;
        }
        int n2 = Tilemap.w;
        boolean[] blArray = new boolean[n2 * Tilemap.h];
        boolean bl = false;
        for (int i = 0; i < Tilemap.type.length && i < blArray.length; ++i) {
            int n3 = Tilemap.type[i] - 2000000000;
            if (Tilemap.type[i] < 2000000000 || n3 >= vector.size()) continue;
            CMLocation cMLocation = (CMLocation)vector.elementAt(n3);
            if (PauseMenu.congDen(cMLocation, n) < 0) continue;
            int n4 = i % n2;
            int n5 = i / n2;
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (PauseMenu.laTuong(n4 + k, n5 + j)) continue;
                    blArray[(n5 + j) * n2 + n4 + k] = true;
                    bl = true;
                }
            }
        }
        return bl ? blArray : null;
    }

    /** Qua cổng tới map m như client tự làm (GameScr.checkCanChangeMap) - chọn đúng điểm đến m. */
    static void quaCong(CMLocation cMLocation, int n) {
        GameScr gameScr = Canvas.gameScr;
        // tới sát cổng game có thể tự bật menu chọn map - đóng đi, auto tự chọn đúng điểm đến
        Canvas.menu.showMenu = false;
        ChangScr.gI().setMap(gameScr.map, cMLocation.toX, cMLocation.toY);
        short[] sArray = Res.getLocation(gameScr.getIndexArrMap(), cMLocation.toM);
        int n2 = PauseMenu.congDen(cMLocation, n);
        GameScr.oldMap = gameScr.map;
        GameService.gI().changeMap(sArray[n2 * 3], sArray[n2 * 3 + 1], sArray[n2 * 3 + 2]);
        ChangScr.gI().switchToMe();
        Res.removeMonsterTemplet();
    }

    /** Nhả hết phím mũi tên ảo mà diBo() đang giữ. */
    private static void nhaPhim() {
        Canvas.keyHold[2] = false;
        Canvas.keyHold[4] = false;
        Canvas.keyHold[6] = false;
        Canvas.keyHold[8] = false;
    }

    /**
     * Đi theo đường BFS bằng cách GIỮ PHÍM MŨI TÊN ẢO (Canvas.keyHold) - y như người chơi tự
     * bấm: GameScr.doActorMove lo bước đi, trượt góc tường và gửi vị trí lên server. (Gọi thẳng
     * mainChar.moveTo không làm nhân vật chính đi.) 1 = đã tới đích, 0 = đang đi, -1 = không đi được.
     */
    private int diBo() {
        MainChar mainChar = Canvas.gameScr.mainChar;
        int n = mainChar.x / 16;
        int n2 = mainChar.y / 16;
        int n3 = n2 * Tilemap.w + n;
        if (this.autoDich == null) {
            PauseMenu.nhaPhim();
            return -1;
        }
        if (n >= 0 && n2 >= 0 && n < Tilemap.w && n3 < this.autoDich.length && this.autoDich[n3]) {
            PauseMenu.nhaPhim();
            return 1;
        }
        if (this.autoDuong == null) {
            PauseMenu.nhaPhim();
            if (++this.autoDiTinhLai > 5) {
                return -1;
            }
            this.autoDuong = PauseMenu.timDuong(this.autoDich);
            this.autoDuongPos = 0;
            this.autoDiDung = 0;
            if (this.autoDuong == null) {
                return -1;
            }
        }
        int n4 = this.autoDuong[this.autoDuongPos];
        int n5 = n4 % Tilemap.w * 16 + 8 - mainChar.x;
        int n6 = n4 / Tilemap.w * 16 + 8 - mainChar.y;
        int n7 = this.autoDiPhim ? Math.max(3, (int)mainChar.speed) : 2;
        if (Math.abs(n5) <= n7 && Math.abs(n6) <= n7) {
            if (++this.autoDuongPos >= this.autoDuong.length) {
                this.autoDuong = null;
            }
            return 0;
        }
        if (!this.autoDiPhim) {
            return this.luot(mainChar, n5, n6);
        }
        PauseMenu.nhaPhim();
        if (Math.abs(n5) > n7) {
            Canvas.keyHold[n5 < 0 ? 4 : 6] = true;
        } else {
            Canvas.keyHold[n6 < 0 ? 2 : 8] = true;
        }
        // kẹt (vướng người/vật, bị server kéo lại): đứng yên ~60 tick thì tìm đường lại
        if (mainChar.x == this.autoDiLastX && mainChar.y == this.autoDiLastY) {
            if (++this.autoDiDung >= 60) {
                this.autoDuong = null;
            }
        } else {
            this.autoDiDung = 0;
            this.autoDiLastX = mainChar.x;
            this.autoDiLastY = mainChar.y;
        }
        return 0;
    }

    /**
     * Tài khoản/mật khẩu để đăng nhập lại: lần gửi lệnh đăng nhập gần nhất (AutoGuard.loginGuard)
     * -> ô nhập màn hình đăng nhập -> mật khẩu game đã lưu (RMS "nqshlogin", xem LoginScr.loadPass).
     */
    private static String[] layTaiKhoan() {
        if (AutoGuard.tkDangNhap != null && AutoGuard.mkDangNhap != null) {
            return new String[]{AutoGuard.tkDangNhap, AutoGuard.mkDangNhap};
        }
        if (Canvas.loginScr != null && Canvas.loginScr.tfUser.getText().trim().length() > 0 && Canvas.loginScr.tfPass.getText().length() > 0) {
            return new String[]{Canvas.loginScr.tfUser.getText().toLowerCase().trim(), Canvas.loginScr.tfPass.getText()};
        }
        try {
            byte[] byArray = RMS.loadRMS("nqshlogin");
            if (byArray != null) {
                java.io.DataInputStream dataInputStream = new java.io.DataInputStream(new java.io.ByteArrayInputStream(byArray));
                if (dataInputStream.readBoolean()) {
                    String string = dataInputStream.readUTF();
                    String string2 = dataInputStream.readUTF();
                    dataInputStream.close();
                    return new String[]{string.toLowerCase().trim(), string2};
                }
                dataInputStream.close();
            }
        }
        catch (Throwable throwable) {
            // không đọc được mật khẩu đã lưu
        }
        return new String[]{"", ""};
    }

    /**
     * Chèn (qua AutoGuard) ở ĐẦU GameScr.onActorMove: server từ chối bước đi thì gửi lại lệnh 4
     * với vị trí THẬT của chính mình. Client gốc chỉ đổi điểm đến chứ không đặt lại tọa độ, nên
     * lướt mà bị từ chối là client tưởng đã tới còn server giữ chỗ cũ (NPC không trả lời).
     */
    public static void viTriServer(byte by, short s, short s2, short s3) {
        if (me == null || !me.autoRunning || me.autoJob != 11 || by != 0 || Canvas.gameScr == null || Canvas.gameScr.mainChar == null || s != Canvas.gameScr.mainChar.ID) {
            return;
        }
        me.autoKeoX = s2;
        me.autoKeoY = s3;
        me.autoCoKeo = true;
    }

    /**
     * Lướt 1 bước tới góc kế tiếp: tối đa 48px mỗi trục (server cho <= 50px/bước, đường thẳng
     * không xuyên tường), cách nhau 1000/tốc độ ms (server: 200ms*5/speed giữa 2 bước).
     * Bị server từ chối thì đặt ngay về vị trí server rồi tìm đường lại; 3 lần thì giữ phím.
     */
    private int luot(MainChar mainChar, int n, int n2) {
        PauseMenu.nhaPhim();
        if (this.autoCoKeo) {
            this.autoCoKeo = false;
            short s = (short)this.autoKeoX;
            short s2 = (short)this.autoKeoY;
            mainChar.x = s;
            mainChar.y = s2;
            mainChar.xTo = s;
            mainChar.yTo = s2;
            mainChar.xBegin = s;
            mainChar.yBegin = s2;
            this.autoDuong = null;
            if (++this.autoDiBiKeo >= 3) {
                this.autoDiPhim = true;
                this.autoDiTinhLai = 0;
            }
            this.autoDiLuc = System.currentTimeMillis() + 300L;
            return 0;
        }
        long l = System.currentTimeMillis();
        if (l < this.autoDiLuc) {
            return 0;
        }
        int n3 = Math.max(-48, Math.min(48, n));
        int n4 = Math.max(-48, Math.min(48, n2));
        short s = (short)(mainChar.x + n3);
        short s2 = (short)(mainChar.y + n4);
        mainChar.x = s;
        mainChar.y = s2;
        mainChar.xTo = s;
        mainChar.yTo = s2;
        mainChar.xBegin = s;
        mainChar.yBegin = s2;
        GameService.gI().moveChar(s, s2);
        this.autoDiLuc = l + 1000L / (long)Math.max(1, (int)mainChar.speed) + 40L;
        return 0;
    }

    /** Thoát game rồi đăng nhập lại đúng nhân vật này (chạy trên luồng riêng). */
    private void batDauRelog() {
        String[] stringArray = PauseMenu.layTaiKhoan();
        final String string = stringArray[0];
        final String string2 = stringArray[1];
        final String string3 = Canvas.gameScr.mainChar.name;
        if (string.length() == 0 || string2.length() == 0 || string3 == null) {
            this.autoStop("Auto nuôi pet: không lấy được tài khoản/mật khẩu để đăng nhập lại (hãy thoát game, đăng nhập lại rồi bật auto), đã dừng.");
            return;
        }
        ++this.autoPetRelog;
        this.autoRelogLoi = null;
        this.autoPhase = 151;
        CharSelectScr.me = null;
        // ngắt kết nối đưa về màn chọn server, ServerListScr.init() đặt index = 0 (server ĐẦU
        // danh sách) -> phải nhớ server đang chơi để kết nối lại đúng chỗ
        final int n = ServerListScr.index;
        Canvas.cmdNgatKetNoi.action.perform();
        ServerListScr.index = n;
        new Thread(new Runnable(){

            public void run() {
                String string4 = PauseMenu.relog(string, string2, string3, n);
                if (string4 != null) {
                    // không vào được game nên vòng auto không chạy: tự dừng và báo ở đây
                    PauseMenu.this.autoRelogLoi = string4;
                    PauseMenu.this.autoRunning = false;
                    AutoGuard.on = false;
                    Canvas.startOKDlg(new StringBuilder().append("Auto nuôi pet: ").append(string4).append(", đã dừng.").toString());
                }
            }
        }).start();
    }

    /** null = đã vào game; còn lại là lý do lỗi. */
    private static String relog(String string, String string2, String string3, int n0) {
        try {
            // chờ server gỡ phiên cũ, đăng nhập sớm quá server đá cả hai phiên
            Thread.sleep(10000L);
            for (int i = 0; i < 2; ++i) {
                ServerListScr.index = n0;
                Canvas.connect();
                GameService.gI().setClientType();
                GameService.gI().login(string, string2, GameMidlet.version);
                long l = System.currentTimeMillis() + 20000L;
                while (System.currentTimeMillis() < l && (CharSelectScr.me == null || CharSelectScr.me.charInfos == null || CharSelectScr.me.cmdSelect == null)) {
                    Thread.sleep(200L);
                }
                if (CharSelectScr.me != null && CharSelectScr.me.charInfos != null && CharSelectScr.me.cmdSelect != null) break;
                Session_ME.gI().close();
                Thread.sleep(8000L);
            }
            CharSelectScr charSelectScr = CharSelectScr.me;
            if (charSelectScr == null || charSelectScr.charInfos == null || charSelectScr.cmdSelect == null) {
                return "không đăng nhập lại được";
            }
            int n = -1;
            for (int i = 0; i < charSelectScr.charInfos.length; ++i) {
                if (charSelectScr.charInfos[i] == null || !string3.equals(charSelectScr.charInfos[i].name)) continue;
                n = i;
            }
            if (n < 0) {
                return "không thấy nhân vật " + string3;
            }
            charSelectScr.selected = n;
            charSelectScr.cmdSelect.action.perform();
            long l = System.currentTimeMillis() + 30000L;
            while (System.currentTimeMillis() < l && !PauseMenu.oTrongGame()) {
                Thread.sleep(300L);
            }
            return PauseMenu.oTrongGame() ? null : "vào game quá lâu";
        }
        catch (Throwable throwable) {
            return "lỗi đăng nhập lại";
        }
    }

    /**
     * Chèn ở ĐẦU GameScr.showWindow: lúc nuôi pet không mở cửa sổ chế đồ (tab 28) - dữ liệu
     * nguyên liệu onCheDo đã nạp xong trước khi gọi showWindow, auto chế thẳng, đỡ hoạt ảnh.
     */
    public static boolean anCuaSoCheDo(int n, boolean bl, byte[] byArray) {
        return me != null && me.autoRunning && (me.autoJob == 11 || me.autoJob == 14) && byArray != null && byArray.length == 1 && byArray[0] == 28;
    }

    /** NPC -16 trả danh sách "Cho pet ăn" (idMenu 1): giữ lại để chọn, không hiện menu. */
    public static boolean petMenuGuard(int n, byte by, String[] stringArray) {
        if (me != null && me.autoRunning && me.autoJob == 13 && n == -513) {
            return true;
        }
        if (me == null || !me.autoRunning || me.autoJob != 11 || n != -16 || by != 1) {
            return false;
        }
        me.autoPetDsAn = stringArray;
        ++me.autoPetDsNhan;
        return true;
    }

    /** Mở lại menu cài đặt mà vẫn đứng ở dòng vừa bấm (Menu.startAt luôn về dòng đầu). */
    /** Menu mod mở gần nhất - các auto không tự đóng menu này (vd AutoVanTieu đóng menu cổng). */
    static Vector menuMod;

    static void moMenuGiuChon(Vector vector, int n, int n2) {
        menuMod = vector;
        // mọi popup của mod mở ở vị trí 3 (góc dưới phải) như menu xa phu của game
        Canvas.menu.startAt(vector, 3);
        if (n2 <= 0 || n2 >= vector.size()) {
            return;
        }
        Menu menu = Canvas.menu;
        menu.selected = n2;
        int n3 = n2 * Screen.ITEM_HEIGHT - Screen.ITEM_HEIGHT * 2;
        if (n3 > menu.cylim) {
            n3 = menu.cylim;
        }
        if (n3 < 0) {
            n3 = 0;
        }
        menu.ctoyBag = n3;
        menu.cyBag = n3;
    }

    /**
     * Menu mod dạng popup - cùng mục và hành động với hàng mod (index 0) của doSelect. Bản jar mở hàng
     * đó trong màn PauseMenu; Android / PC (menu thật là MenuWindows) mở popup này từ tab "Khác".
     */
    public static void moMenuMod() {
        final PauseMenu pauseMenu = PauseMenu.gI();
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command("Xa phu", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                TienIch.moXaPhu();
            }
        }));
        vector.addElement(new Command("Chế đồ nhanh", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                pauseMenu.showCheDoNhanhMenu();
            }
        }));
        vector.addElement(new Command("Dòng ẩn", new IAction(){

            public void perform() {
                pauseMenu.moMenuDongAn(0);
            }
        }));
        vector.addElement(new Command("Auto up", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                AutoUp.moMenu(0);
            }
        }));
        vector.addElement(new Command("Vận tiêu", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                AutoVanTieu.moMenu(0);
            }
        }));
        vector.addElement(new Command("PK", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                AutoPk.moMenu(0);
            }
        }));
        vector.addElement(new Command("Vận may", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                AutoVanMay.moMenu(0);
            }
        }));
        vector.addElement(new Command("Hiệu ứng", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                HieuUng.moMenu(0);
            }
        }));
        vector.addElement(new Command("Tiện ích", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
                TienIch.moMenu(0);
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 3, 0);
    }

    /** "Dòng ẩn" dạng popup bật/tắt (bản jar: hàng index 9 của màn PauseMenu). */
    private void moMenuDongAn(int chon) {
        Vector<Command> vector = new Vector<Command>();
        for (int i = 0; i < this.dongAnChon.length; ++i) {
            final int n = i;
            vector.addElement(new Command(new StringBuilder().append(this.dongAnChon[i] ? "(x) " : "( ) ").append(this.dongAnLabel(i)).toString(), new IAction(){

                public void perform() {
                    PauseMenu.this.dongAnChon[n] = !PauseMenu.this.dongAnChon[n];
                    PauseMenu.this.name[9][n + 1] = new StringBuilder().append(PauseMenu.this.dongAnChon[n] ? "(x) " : "( ) ").append(PauseMenu.this.dongAnLabel(n)).toString();
                    PauseMenu.this.moMenuDongAn(n);
                }
            }));
        }
        PauseMenu.moMenuGiuChon(vector, 3, chon);
    }

    private void showHocSkillMenu(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(new StringBuilder().append("Skill slot 1: ").append(SK_TEN[this.autoSkMuon]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoSkMuon = (PauseMenu.this.autoSkMuon + 1) % SK_TEN.length;
                if (PauseMenu.this.autoSkMuon == PauseMenu.this.autoSkMuon2) {
                    PauseMenu.this.autoSkMuon2 = (PauseMenu.this.autoSkMuon2 + 1) % SK_TEN.length;
                }
                PauseMenu.this.showHocSkillMenu(0);
            }
        }));
        vector.addElement(new Command(this.autoSkSlot2 ? "Học slot 2: Bật" : "Học slot 2: Tắt", new IAction(){

            public void perform() {
                PauseMenu.this.autoSkSlot2 = !PauseMenu.this.autoSkSlot2;
                PauseMenu.this.showHocSkillMenu(1);
            }
        }));
        int n = 2;
        if (this.autoSkSlot2) {
            vector.addElement(new Command(new StringBuilder().append("Skill slot 2: ").append(SK_TEN[this.autoSkMuon2]).toString(), new IAction(){

                public void perform() {
                    // 2 slot không được trùng skill (server không cho)
                    do {
                        PauseMenu.this.autoSkMuon2 = (PauseMenu.this.autoSkMuon2 + 1) % SK_TEN.length;
                    } while (PauseMenu.this.autoSkMuon2 == PauseMenu.this.autoSkMuon);
                    PauseMenu.this.showHocSkillMenu(2);
                }
            }));
            n = 3;
        }
        final int n2 = n;
        vector.addElement(new Command(new StringBuilder().append("Học khi phẩm chất từ: ").append(SK_PHAM_CHAT[this.autoSkPhamChat]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoSkPhamChat = PauseMenu.this.autoSkPhamChat >= 3 ? 1 : PauseMenu.this.autoSkPhamChat + 1;
                PauseMenu.this.showHocSkillMenu(n2);
            }
        }));
        vector.addElement(new Command(this.autoSkMua ? "Tự mua Huyết Bồ Đề (10 lượng): Bật" : "Tự mua Huyết Bồ Đề: Tắt", new IAction(){

            public void perform() {
                PauseMenu.this.autoSkMua = !PauseMenu.this.autoSkMua;
                PauseMenu.this.showHocSkillMenu(n2 + 1);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Bắt đầu học skill (sách ").append(PauseMenu.skSach()).append(", Huyết Bồ Đề ").append(PauseMenu.skHbdCo()).append(")").toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoStartHocSkill();
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    /** Tên mục tiêu: "Tiên khí" hoặc "Tiên khí + Phá triền". */
    private String skMucTieu() {
        return this.autoSkSlot2 ? new StringBuilder().append(SK_TEN[this.autoSkMuon]).append(" + ").append(SK_TEN[this.autoSkMuon2]).toString() : SK_TEN[this.autoSkMuon];
    }

    /**
     * Đánh giá skill pet đang có (theo thứ tự slot 1, slot 2):
     * 1 = đã đạt mục tiêu, 0 = còn có thể học tiếp ở slot trống, -1 = có skill sai -> phải xoá.
     * Học slot 2: Tắt - chỉ xét slot 1. Bật - cần đủ cặp; slot 1 trúng skill nào trong cặp cũng
     * được giữ (học slot 2 tìm skill còn lại), kết quả cuối vẫn đúng cặp đã chọn.
     */
    private int skDanhGia(Vector vector) {
        if (!this.autoSkSlot2) {
            if (vector.size() == 0) {
                return 0;
            }
            return ((Integer)vector.elementAt(0)).intValue() == this.autoSkMuon ? 1 : -1;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < vector.size(); ++i) {
            int n = ((Integer)vector.elementAt(i)).intValue();
            if (n == this.autoSkMuon) {
                bl = true;
                continue;
            }
            if (n == this.autoSkMuon2) {
                bl2 = true;
                continue;
            }
            return -1;
        }
        if (bl && bl2) {
            return 1;
        }
        return 0;
    }

    private static int skSach() {
        MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        return mainChar == null || mainChar.potions == null || mainChar.potions.length <= POTION_SACH_SKILL ? 0 : mainChar.potions[POTION_SACH_SKILL];
    }

    private static ItemInInventory skHbd() {
        Vector vector = Char.inventory;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            if (itemInInventory.idTemplate != ITEM_HUYET_BO_DE || itemInInventory.isSelling) continue;
            return itemInInventory;
        }
        return null;
    }

    private static int skHbdCo() {
        int n = 0;
        Vector vector = Char.inventory;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            if (((ItemInInventory)vector.elementAt(i)).idTemplate != ITEM_HUYET_BO_DE) continue;
            ++n;
        }
        return n;
    }

    /** Phẩm chất pet (0..3) từ dòng "- Phẩm chất: ...", -1 = chưa có. */
    private static int skPhamChat() {
        String string = PauseMenu.petInfo();
        int n = string == null ? -1 : string.indexOf("Phẩm chất:");
        if (n < 0) {
            return -1;
        }
        String string2 = string.substring(n + "Phẩm chất:".length()).trim();
        for (int i = SK_PHAM_CHAT.length - 1; i >= 0; --i) {
            if (!string2.startsWith(SK_PHAM_CHAT[i])) continue;
            return i;
        }
        return -1;
    }

    /** Các dòng "- Kỹ năng: <tên>..." (bỏ dòng "Kỹ năng có thể học"). */
    private static Vector skDangCo() {
        Vector vector = new Vector();
        String string = PauseMenu.petInfo();
        int n = 0;
        while (string != null && (n = string.indexOf("- Kỹ năng: ", n)) >= 0) {
            n += "- Kỹ năng: ".length();
            for (int i = 0; i < SK_TEN.length; ++i) {
                if (!string.startsWith(SK_TEN[i], n)) continue;
                vector.addElement(new Integer(i));
                break;
            }
        }
        return vector;
    }

    /** Trạng thái tiến hoá "- Trạng thái: x/3" (1..3). */
    private static int skTrangThai() {
        return PauseMenu.petDocSo(PauseMenu.petInfo(), "Trạng thái:");
    }

    private void autoStartHocSkill() {
        if (PauseMenu.petInfo() == null || PauseMenu.petCap() < 0) {
            Canvas.startOKDlg("Bạn phải mang theo pet.");
            return;
        }
        if (PauseMenu.skTrangThai() < 2) {
            Canvas.startOKDlg("Pet phải tiến hoá lên trạng thái 2 mới học được kỹ năng.");
            return;
        }
        if (this.skDanhGia(PauseMenu.skDangCo()) == 1) {
            Canvas.startOKDlg(new StringBuilder().append("Pet đã có sẵn kỹ năng ").append(this.skMucTieu()).append(".").toString());
            return;
        }
        this.autoSkSach = 0;
        this.autoSkHbd = 0;
        this.autoSkDaMua = 0;
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 12;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPhase = 170;
        this.autoWaitTicks = 0;
    }

    private void showHklMenu(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(this.autoHklMua ? "Tự mua Hỏa thạch cấp 1 (10 lượng): Bật" : "Tự mua Hỏa thạch cấp 1: Tắt", new IAction(){

            public void perform() {
                PauseMenu.this.autoHklMua = !PauseMenu.this.autoHklMua;
                PauseMenu.this.showHklMenu(0);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Bắt đầu nâng cấp (Hỏa thạch cấp 1: ").append(PauseMenu.hklDa(HOA_THACH_C1)).append(", cấp 2: ").append(PauseMenu.hklDa(HOA_THACH_C2)).append(")").toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoStartHkl();
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    private static int hklDa(int n) {
        return PauseMenu.autoGemCountLock(n, false) + PauseMenu.autoGemCountLock(n, true);
    }

    /** Hỏa kỳ lân trong hành trang (681..683), ưu tiên món chưa tối đa. */
    private static ItemInInventory hklMon() {
        ItemInInventory itemInInventory = null;
        Vector vector = Char.inventory;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
            if (itemInInventory2.idTemplate < 681 || itemInInventory2.idTemplate > HKL_CAP_TOI_DA || itemInInventory2.isSelling) continue;
            if (itemInInventory2.idTemplate < HKL_CAP_TOI_DA) {
                return itemInInventory2;
            }
            itemInInventory = itemInInventory2;
        }
        return itemInInventory;
    }

    private void autoStartHkl() {
        ItemInInventory itemInInventory = PauseMenu.hklMon();
        if (itemInInventory == null) {
            Canvas.startOKDlg("Không thấy Hỏa kỳ lân trong hành trang (nếu đang mặc, hãy tháo ra hành trang).");
            return;
        }
        if (itemInInventory.idTemplate >= HKL_CAP_TOI_DA) {
            Canvas.startOKDlg("Hỏa kỳ lân đã nâng cấp tối đa (cấp 3).");
            return;
        }
        this.autoHklOk = 0;
        this.autoHklHong = 0;
        this.autoHklDaMua = 0;
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 13;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPhase = 180;
        this.autoWaitTicks = 0;
    }

    private void showNghienMenu(int chon) {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(new StringBuilder().append("Loại nguyên liệu: ").append(this.autoNbLoai < 0 ? "Tất cả" : PET_LOAI_TEN[this.autoNbLoai]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoNbLoai = PauseMenu.this.autoNbLoai >= PET_LOAI_TEN.length - 1 ? -1 : PauseMenu.this.autoNbLoai + 1;
                PauseMenu.this.showNghienMenu(0);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Nguyên liệu sơ cấp: cấp ").append(this.autoNbCapSo + 1).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoNbCapSo = (PauseMenu.this.autoNbCapSo + 1) % 6;
                PauseMenu.this.showNghienMenu(1);
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Nguyên liệu cao cấp: cấp ").append(this.autoNbCapCao + 1).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoNbCapCao = (PauseMenu.this.autoNbCapCao + 1) % 6;
                PauseMenu.this.showNghienMenu(2);
            }
        }));
        vector.addElement(new Command("Bắt đầu chế và nghiền bột", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartNghien();
            }
        }));
        PauseMenu.moMenuGiuChon(vector, 2, chon);
    }

    /** Món auto được phép nghiền: trang bị thường (type < 13) cấp 20/21, có màu, không đang bán. */
    private static boolean nbNghienDuoc(ItemInInventory itemInInventory) {
        ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        return itemTemplate != null && itemTemplate.type < 13 && itemInInventory.level >= 20 && itemInInventory.level <= 21 && itemInInventory.colorName != 0 && !itemInInventory.isSelling;
    }

    /** Món đầu tiên trong túi auto được nghiền, null nếu không còn. */
    private static ItemInInventory nbMonNghien() {
        Vector vector = Char.inventory;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            if (!PauseMenu.nbNghienDuoc(itemInInventory)) continue;
            return itemInInventory;
        }
        return null;
    }

    private static int nbDemNghien() {
        int n = 0;
        Vector vector = Char.inventory;
        for (int i = 0; vector != null && i < vector.size(); ++i) {
            if (!PauseMenu.nbNghienDuoc((ItemInInventory)vector.elementAt(i))) continue;
            ++n;
        }
        return n;
    }

    private void autoStartNghien() {
        this.autoNbDang = (short)-1;
        this.autoNbCo = false;
        this.autoNbThu = 0;
        this.autoNbLuc = 0L;
        this.autoNbHet = false;
        this.autoNbChe = 0;
        this.autoNbBan = 0;
        this.autoNbLoi = null;
        // không giữ hàng đợi: mỗi lượt nghiền lấy thẳng món đủ điều kiện trong túi (nbMonNghien) -
        // gồm cả đồ cấp 20/21 có màu còn dư từ trước và món vừa chế
        this.autoNbGom = PauseMenu.nbDemNghien();
        this.autoNbBo = 0;
        this.autoNbGui = 0;
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 14;
        this.autoMode = 0;
        this.autoSlot = 0;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoSoldIds.removeAllElements();
        // nghiền chỉ làm được trong nhà; vào nhà rồi qua 193 (chờ hàng đợi / hành trang) mới chế
        if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
            this.autoPhase = 193;
        } else {
            this.autoAfterHouse = 193;
            this.autoPhase = 20;
        }
        this.autoWaitTicks = 0;
    }

    /**
     * Gọi mỗi tick khi đang nghiền (trước switch): đủ 5s thì nghiền món đầu hàng đợi. Món vừa
     * gửi nghiền mà còn trong túi ở lượt sau (vd "Thao tác quá nhanh") thì gửi lại, 3 lần thì bỏ.
     */
    private void nghienTick() {
        long l = System.currentTimeMillis();
        if (l < this.autoNbLuc) {
            return;
        }
        if (this.autoNbCo) {
            // KHÔNG xét "món còn trong túi": server tái dùng id - nghiền xong id X được trả lại
            // và món chế ngay sau lấy đúng X, trông như món chưa bị nghiền. Đếm theo số bột.
            if (PauseMenu.hklDa(NB_BOT) > this.autoNbBotTruoc) {
                ++this.autoFound;
                this.autoNbCo = false;
            } else if (this.autoNbLoi == null && l - this.autoNbGuiLuc < NB_CHO_KQ_MS) {
                // kết quả chưa về: chờ tiếp
                return;
            } else if (this.autoNbLoi != null && this.autoNbLoi.startsWith("Thao tác quá nhanh")) {
                // server còn đang trong thời gian chờ (vd server chưa cập nhật bản 1 giây): gửi lại,
                // không tính là một lần thất bại
                this.autoNbLoi = null;
            } else if (++this.autoNbThu >= 3) {
                this.autoStop(new StringBuilder().append("Auto nghiền bột: server không cho nghiền").append(this.autoNbLoi == null ? " (không có phản hồi)" : new StringBuilder().append(": ").append(this.autoNbLoi).toString()).append(" - map ").append(Canvas.gameScr.map).append(", đã gửi ").append(this.autoNbGui).append(" lệnh, đã dừng.").toString());
                return;
            }
        }
        if (!this.autoNbCo) {
            ItemInInventory itemInInventory = PauseMenu.nbMonNghien();
            if (itemInInventory != null) {
                this.autoNbDang = itemInInventory.itemID;
                this.autoNbCo = true;
                this.autoNbThu = 0;
            }
        }
        if (!this.autoNbCo) {
            return;
        }
        this.autoNbBotTruoc = PauseMenu.hklDa(NB_BOT);
        // món này sắp biến mất và id của nó có thể được cấp lại cho món đang chế dở: bỏ id khỏi
        // snapshot để phase 190 vẫn nhận ra món mới mang lại id đó
        for (int i = 0; this.autoSnapshot != null && i < this.autoSnapshot.length; ++i) {
            if (this.autoSnapshot[i] != this.autoNbDang) continue;
            this.autoSnapshot[i] = (short)-32768;
        }
        this.autoNbLoi = null;
        this.autoNbGuiLuc = l;
        GameService.gI().doRequestTachNguyenLIeu(this.autoNbDang, 4, 0, 0);
        ++this.autoNbGui;
        this.autoNbLuc = l + NB_CACH_MS;
    }

    private static String cttMucText(int n) {
        return n >= 5 ? "0.5%" : new StringBuilder().append("0.").append(n).append("% - 0.5%").toString();
    }

    private static String cttGiaTri(int n) {
        return new StringBuilder().append(n / 10).append(".").append(n % 10).append("%").toString();
    }

    /** Mức cao nhất món này ra được với viên đá đang chọn - Item.doAddNewAttribute ở server. */
    private int cttMucToiDa(ItemInInventory itemInInventory) {
        if (this.autoCttDa != CTT_DA_CAO_CAP || itemInInventory == null) {
            return 3;
        }
        return itemInInventory.hangItem == 1 ? 5 : (itemInInventory.hangItem == 2 ? 4 : 3);
    }

    /** Món đã có sẵn thuộc tính đạt mục tiêu (server chỉ giữ 1 trong 4 loại lan mỗi lần cộng). */
    private boolean cttDatSan(ItemInInventory itemInInventory) {
        for (int i = 0; i < CTT_ID.length; ++i) {
            if (this.autoCttLoai >= 0 && this.autoCttLoai != i || PauseMenu.autoAttVal(itemInInventory, CTT_ID[i]) < this.autoCttMin) continue;
            return true;
        }
        return false;
    }

    /** Đồ xanh lá còn cộng được tới mục tiêu: không đang bán, không phải đồ thú, chưa đạt sẵn. */
    private boolean cttCanDap(ItemInInventory itemInInventory) {
        ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        return itemTemplate != null && (itemTemplate.type < 14 || itemTemplate.type > 18) && itemInInventory.colorName == 1 && !itemInInventory.isSelling && this.cttMucToiDa(itemInInventory) >= this.autoCttMin && !this.cttDatSan(itemInInventory);
    }

    private void autoStartCongTT(boolean bl) {
        ItemInInventory itemInInventory = this.autoFindItem(this.autoCttItem);
        if (!bl && itemInInventory == null) {
            Canvas.startOKDlg("Không thấy món đồ trong hành trang.");
            return;
        }
        int n = this.cttMucToiDa(itemInInventory);
        if (!bl && this.autoCttMin > n) {
            Canvas.startOKDlg(new StringBuilder().append("Với ").append(this.autoCttDa == CTT_DA_CAO_CAP ? "món đồ này" : "đá thuộc tính thường").append(" chỉ ra được tối đa ").append(PauseMenu.cttGiaTri(n)).append(this.autoCttDa == CTT_DA_CAO_CAP ? " (0.5% cần đồ Nhất phẩm, 0.4% cần Nhị phẩm)." : ", hãy dùng đá cao cấp.").toString());
            return;
        }
        this.autoCttFull = bl;
        if (bl) {
            // món đang đặt (nếu hợp lệ) đập trước, rồi lần lượt các món còn lại trong hành trang
            Vector vector = Char.inventory;
            short[] sArray = new short[(vector == null ? 0 : vector.size()) + 1];
            int n2 = 0;
            if (itemInInventory != null && this.cttCanDap(itemInInventory)) {
                sArray[n2++] = itemInInventory.itemID;
            }
            for (int i = 0; vector != null && i < vector.size(); ++i) {
                ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
                if (itemInInventory2 == itemInInventory || !this.cttCanDap(itemInInventory2)) continue;
                sArray[n2++] = itemInInventory2.itemID;
            }
            if (n2 == 0) {
                Canvas.startOKDlg(new StringBuilder().append("Không có món nào cần cộng tới ").append(this.autoCttLoai < 0 ? "" : new StringBuilder().append(CTT_NAME[this.autoCttLoai]).append(" ").toString()).append(PauseMenu.cttMucText(this.autoCttMin)).append(" (cần đồ xanh lá").append(this.autoCttDa == CTT_DA_CAO_CAP ? (this.autoCttMin >= 5 ? ", Nhất phẩm" : (this.autoCttMin == 4 ? ", Nhất/Nhị phẩm" : "")) : "").append(this.autoCttDa != CTT_DA_CAO_CAP && this.autoCttMin > 3 ? "; đá thường chỉ ra tối đa 0.3%" : "").append(").").toString());
                return;
            }
            this.autoTargets = new short[n2];
            System.arraycopy(sArray, 0, this.autoTargets, 0, n2);
            this.autoTargetPos = 0;
            this.autoCttItem = this.autoTargets[0];
        }
        this.autoCttVuaRa = null;
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 9;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPhase = 95;
        this.autoWaitTicks = 0;
    }

    private void showThanBinhMenu() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(new StringBuilder().append("Cấp thần binh đang mặc: ").append(this.autoTbLevel).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoTbLevel = PauseMenu.this.autoTbLevel >= 80 ? 60 : PauseMenu.this.autoTbLevel + 10;
                PauseMenu.this.showThanBinhMenu();
            }
        }));
        vector.addElement(new Command("Bắt đầu tinh luyện thần binh", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartThanBinh();
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    private void autoStartThanBinh() {
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 6;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoTbPct = -1;
        this.autoSlot = 0;
        this.autoPhase = 0;
        this.autoWaitTicks = 0;
    }

    private void showThanKhiMenu() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(new StringBuilder().append("Loại: ").append(TK_TYPE_NAME[this.autoTkType]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoTkType = (PauseMenu.this.autoTkType + 1) % TK_TYPE_NAME.length;
                PauseMenu.this.showThanKhiMenu();
            }
        }));
        vector.addElement(new Command(this.autoTkFilter == 0 ? "Lọc: chỉ giữ 9 dòng" : "Lọc: Tắt", new IAction(){

            public void perform() {
                PauseMenu.this.autoTkFilter = 1 - PauseMenu.this.autoTkFilter;
                PauseMenu.this.showThanKhiMenu();
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Số lượng: ").append(this.autoTkQty).toString(), new IAction(){

            public void perform() {
                int n = 0;
                for (int i = 0; i < TK_QTY.length; ++i) {
                    if (TK_QTY[i] != PauseMenu.this.autoTkQty) continue;
                    n = i;
                }
                PauseMenu.this.autoTkQty = TK_QTY[(n + 1) % TK_QTY.length];
                PauseMenu.this.showThanKhiMenu();
            }
        }));
        vector.addElement(new Command("Bắt đầu chế thần khí", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartThanKhi();
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    private void autoStartThanKhi() {
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 5;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoTkGrade = 5;
        this.autoTkNeedStone = false;
        this.autoTkStones = PauseMenu.autoGemCount(154);
        this.autoAfterHouse = 60;
        this.autoPhase = this.autoTkFilter == 0 ? 20 : 60;
        this.autoWaitTicks = 0;
    }

    private void showThuMenu() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(new StringBuilder().append("Cấp đồ thú: lv").append(this.autoPetLevel).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoPetLevel = PauseMenu.this.autoPetLevel >= 85 ? 30 : PauseMenu.this.autoPetLevel + 5;
                PauseMenu.this.showThuMenu();
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Kiểu: ").append(PET_MODE_NAME[this.autoPetMode]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoPetMode = (PauseMenu.this.autoPetMode + 1) % PET_MODE_NAME.length;
                PauseMenu.this.showThuMenu();
            }
        }));
        if (this.autoPetMode != 2) {
            vector.addElement(new Command(new StringBuilder().append("Món: ").append(PET_SLOT_NAME[this.autoPetSlot]).toString(), new IAction(){

                public void perform() {
                    PauseMenu.this.autoPetSlot = (PauseMenu.this.autoPetSlot + 1) % PET_SLOT_NAME.length;
                    PauseMenu.this.showThuMenu();
                }
            }));
        }
        if (this.autoPetMode == 1) {
            vector.addElement(new Command(new StringBuilder().append("Ngũ hành: ").append(PET_HE_NAME[this.autoPetHe]).toString(), new IAction(){

                public void perform() {
                    PauseMenu.this.autoPetHe = (PauseMenu.this.autoPetHe + 1) % PET_HE_NAME.length;
                    PauseMenu.this.showThuMenu();
                }
            }));
        }
        vector.addElement(new Command(this.heMaPhap ? "Hệ: Ma pháp" : "Hệ: Vật lý", new IAction(){

            public void perform() {
                PauseMenu.this.heMaPhap = !PauseMenu.this.heMaPhap;
                PauseMenu.this.showThuMenu();
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("Lọc: ").append(PET_FILTER_NAME[this.autoPetFilter]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoPetFilter = (PauseMenu.this.autoPetFilter + 1) % PET_FILTER_NAME.length;
                PauseMenu.this.showThuMenu();
            }
        }));
        vector.addElement(new Command("Bắt đầu auto đồ thú", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartPet();
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    private void autoStartPet() {
        this.autoRunning = true;
        AutoGuard.on = true;
        AutoGuard.pendingSell = 0;
        this.autoJob = 4;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPetCurSlot = this.autoPetSlot == 5 || this.autoPetMode == 2 ? 0 : this.autoPetSlot;
        this.autoPetEndSlot = this.autoPetSlot == 5 || this.autoPetMode == 2 ? 5 : this.autoPetSlot + 1;
        this.autoPetBaseHe = -1;
        this.autoPetOwned = new Vector();
        this.autoPetSell = new Vector();
        this.autoAfterHouse = 41;
        this.autoPhase = 30;
        this.autoWaitTicks = 0;
    }

    private int autoPetN() {
        return PET_MAT[(this.autoPetLevel - 30) / 5];
    }

    private int autoPetTemplate() {
        return PET_BASE[this.autoPetCurSlot] + (this.autoPetLevel - 30) / 5;
    }

    private static boolean autoHasGemType(int n) {
        if (MainChar.gemItem == null) {
            return false;
        }
        for (int i = 0; i < MainChar.gemItem.size(); ++i) {
            if (((GemTemplate)MainChar.gemItem.elementAt(i)).id != n) continue;
            return true;
        }
        return false;
    }

    private static int autoGemCount(int n) {
        int n2 = 0;
        Vector[] vectorArray = new Vector[]{MainChar.gemItem, MainChar.gemItemKhoa};
        for (int i = 0; i < 2; ++i) {
            if (vectorArray[i] == null) continue;
            for (int j = 0; j < vectorArray[i].size(); ++j) {
                GemTemplate gemTemplate = (GemTemplate)vectorArray[i].elementAt(j);
                if (gemTemplate.id != n) continue;
                n2 += gemTemplate.number;
            }
        }
        return n2;
    }

    private boolean autoPetIsOwned(short s) {
        for (int i = 0; i < this.autoPetOwned.size(); ++i) {
            if (((Integer)this.autoPetOwned.elementAt(i)).intValue() != s) continue;
            return true;
        }
        return false;
    }

    private boolean autoPetIsSell(short s) {
        for (int i = 0; i < this.autoPetSell.size(); ++i) {
            if (((Integer)this.autoPetSell.elementAt(i)).intValue() != s) continue;
            return true;
        }
        return false;
    }

    private Vector autoPetByColor(int n) {
        Vector vector = new Vector();
        Vector vector2 = Char.inventory;
        if (vector2 == null) {
            return vector;
        }
        for (int i = 0; i < vector2.size(); ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector2.elementAt(i);
            if (itemInInventory.colorName != n || !this.autoPetIsOwned(itemInInventory.itemID) || this.autoPetIsSell(itemInInventory.itemID)) continue;
            if (itemInInventory.idTemplate == this.autoPetTemplate()) {
                vector.insertElementAt(itemInInventory, 0);
                continue;
            }
            vector.addElement(itemInInventory);
        }
        return vector;
    }

    private int autoPetH() {
        if (this.autoPetMode == 1) {
            return this.autoPetHe;
        }
        if (this.autoPetMode == 2 && this.autoPetBaseHe >= 0) {
            return (this.autoPetBaseHe + this.autoPetCurSlot) % 5;
        }
        return -1;
    }

    private boolean autoPetCenterOk(ItemInInventory itemInInventory, int n) {
        return itemInInventory.idTemplate == this.autoPetTemplate() && (n == 0 || this.autoPetH() < 0 || itemInInventory.heItem == this.autoPetH());
    }

    private boolean autoPetHas(int n, int n2) {
        Vector vector = this.autoPetByColor(n);
        for (int i = 0; i < vector.size(); ++i) {
            if (!this.autoPetCenterOk((ItemInInventory)vector.elementAt(i), n2)) continue;
            return true;
        }
        return false;
    }

    private Vector autoPetPickEp(int n) {
        Vector vector = this.autoPetByColor(PET_SRC_COLOR[n]);
        if (vector.size() < 5) {
            return null;
        }
        ItemInInventory itemInInventory = null;
        for (int i = 0; i < vector.size(); ++i) {
            if (!this.autoPetCenterOk((ItemInInventory)vector.elementAt(i), n)) continue;
            itemInInventory = (ItemInInventory)vector.elementAt(i);
            break;
        }
        if (itemInInventory == null) {
            itemInInventory = (ItemInInventory)vector.elementAt(0);
        }
        Vector vector2 = new Vector();
        vector2.addElement(itemInInventory);
        for (int i = 0; i < 2 && vector2.size() < 5; ++i) {
            for (int j = 0; j < vector.size() && vector2.size() < 5; ++j) {
                ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(j);
                if (itemInInventory2 == itemInInventory || this.autoPetCenterOk(itemInInventory2, 1) != (i == 1)) continue;
                vector2.addElement(itemInInventory2);
            }
        }
        return vector2;
    }

    private Vector autoPetBuildGroups(int n, int n2) {
        Vector vector = new Vector();
        Vector vector2 = this.autoPetPickEp(n);
        if (vector2 == null || n2 <= 0) {
            return vector;
        }
        vector.addElement(vector2);
        Vector vector3 = this.autoPetByColor(PET_SRC_COLOR[n]);
        for (int i = 0; i < vector2.size(); ++i) {
            vector3.removeElement(vector2.elementAt(i));
        }
        while (vector.size() < n2 && vector3.size() >= 5) {
            Vector vector4 = new Vector();
            for (int i = 0; i < 5; ++i) {
                vector4.addElement(vector3.elementAt(0));
                vector3.removeElementAt(0);
            }
            vector.addElement(vector4);
        }
        return vector;
    }

    private int[] autoPetNeeds() {
        boolean bl = this.autoPetHas(2, 2);
        boolean bl2 = this.autoPetHas(3, 1);
        int n = Math.max(0, 5 - this.autoPetByColor(2).size());
        if (!bl) {
            n = Math.max(n, 1);
        }
        int n2 = Math.max(0, 5 * n - this.autoPetByColor(3).size());
        if (!bl && !bl2) {
            n2 = Math.max(n2, 1);
        }
        int n3 = Math.max(0, 5 * n2 - this.autoPetByColor(0).size());
        return new int[]{n, n2, n3};
    }

    private boolean autoPetOk(ItemInInventory itemInInventory) {
        if (this.autoPetFilter == 3) {
            return true;
        }
        int n = PauseMenu.autoAttVal(itemInInventory, 61);
        int n2 = PauseMenu.autoAttVal(itemInInventory, 62);
        if (this.autoPetFilter == 0) {
            return n >= 3;
        }
        if (this.autoPetFilter == 1) {
            return n2 >= 3;
        }
        return n >= 3 || n2 >= 3;
    }

    private void autoPetExitHouse() {
        Vector vector = Tilemap.mapchangeLocations;
        CMLocation cMLocation = vector == null || vector.size() == 0 ? null : (CMLocation)vector.elementAt(0);
        if (cMLocation == null) {
            this.autoStop("Auto đồ thú: không tìm thấy cửa ra khỏi nhà, đã dừng.");
            return;
        }
        GameService.gI().changeMap(GameScr.oldMap, cMLocation.toX, cMLocation.toY);
        ChangScr.gI().switchToMe();
        Res.removeMonsterTemplet();
        this.autoPhase = 51;
        this.autoWaitCount = 0;
    }

    private void showSuaMenu() {
        Vector<Command> vector = new Vector<Command>();
        vector.addElement(new Command(new StringBuilder().append("Thuộc tính: ").append(REP_STAT_NAME[this.autoRepStat]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoRepStat = (PauseMenu.this.autoRepStat + 1) % REP_STAT_NAME.length;
                PauseMenu.this.showSuaMenu();
            }
        }));
        vector.addElement(new Command(new StringBuilder().append("HP/MP: ").append(REP_HP_NAME[this.autoRepHp]).toString(), new IAction(){

            public void perform() {
                PauseMenu.this.autoRepHp = (PauseMenu.this.autoRepHp + 1) % REP_HP_NAME.length;
                PauseMenu.this.showSuaMenu();
            }
        }));
        vector.addElement(new Command("Bắt đầu sửa thuộc tính", new IAction(){

            public void perform() {
                PauseMenu.this.autoStartJob(3);
            }
        }));
        Canvas.menu.startAt(vector, 3);
    }

    private static int autoAttVal(ItemInInventory itemInInventory, int n) {
        if (itemInInventory.allAttribute == null) {
            return 0;
        }
        for (int i = 0; i < itemInInventory.allAttribute.size(); ++i) {
            InfoAttributeItem infoAttributeItem = (InfoAttributeItem)itemInInventory.allAttribute.elementAt(i);
            if (infoAttributeItem.getID() != n) continue;
            return infoAttributeItem.getValueAtt();
        }
        return 0;
    }

    private boolean autoLockOk(ItemInInventory itemInInventory) {
        int n = Math.max(PauseMenu.autoAttVal(itemInInventory, 58), Math.max(PauseMenu.autoAttVal(itemInInventory, 59), PauseMenu.autoAttVal(itemInInventory, 60)));
        return n >= this.autoLockGoal;
    }

    private boolean autoRepAny() {
        return this.autoRepStat == 0 && this.autoRepHp == 0;
    }

    private boolean autoRepOk(ItemInInventory itemInInventory) {
        return (this.autoRepStat == 0 || PauseMenu.autoAttVal(itemInInventory, 34 + this.autoRepStat) > 0) && (this.autoRepHp == 0 || PauseMenu.autoAttVal(itemInInventory, 32 + this.autoRepHp) > 0);
    }

    private boolean autoJobDone(ItemInInventory itemInInventory) {
        if (this.autoJob == 1) {
            return itemInInventory.level >= this.autoGoalFor(itemInInventory);
        }
        if (this.autoJob == 2) {
            return this.autoLockOk(itemInInventory);
        }
        return !this.autoRepAny() && this.autoRepOk(itemInInventory);
    }

    private String dongAnLabel(int n) {
        int n2 = n / 2;
        int n3 = n % 2;
        return n3 == 0 ? DONG_AN_NAME[n2] : new StringBuilder().append(DONG_AN_NAME[n2]).append(" ").append(PauseMenu.dongAnMax(n2)).append("%").toString();
    }

    private static int dongAnMax(int n) {
        return n == 3 ? 5 : 9;
    }

    private boolean dongAnAnyChosen() {
        for (int i = 0; i < this.dongAnChon.length; ++i) {
            if (!this.dongAnChon[i]) continue;
            return true;
        }
        return false;
    }

    private void autoStop(String string) {
        PauseMenu.nhaPhim();
        this.autoRunning = false;
        AutoGuard.on = false;
        Canvas.menu.showMenu = false;
        Canvas.startOKDlg(string);
    }

    public static boolean autoHideUi() {
        return me != null && me.autoRunning;
    }

    public static boolean autoEatDlg(String string) {
        if (me == null || !me.autoRunning) {
            return false;
        }
        if (string != null && me.autoJob == 14) {
            if (string.startsWith("Thao tác quá nhanh") || string.startsWith("Chỉ có thể nghiền")) {
                // nghienTick tự gửi lại ở lượt 5s sau; 3 lần không được thì dừng và báo câu này
                me.autoNbLoi = string;
                return true;
            }
        }
        if (string != null && me.autoJob == 13) {
            if (string.startsWith("Nâng cấp thành công")) {
                ++me.autoHklOk;
                return true;
            }
            if (string.startsWith("Nâng cấp thất bại")) {
                ++me.autoHklHong;
                return true;
            }
            if (string.startsWith("Không đủ hoả thạch")) {
                // phase 180 thấy hết đá sẽ tự mua; đếm như 1 lần không nâng
                ++me.autoHklHong;
                return true;
            }
        }
        if (string != null && me.autoJob == 12) {
            if (string.indexOf("học được kỹ năng") >= 0) {
                me.autoSkKq = 1;
                return true;
            }
            if (string.indexOf("học kỹ năng thất bại") >= 0) {
                me.autoSkKq = 2;
                return true;
            }
            if (string.indexOf("không thể học thêm kỹ năng") >= 0) {
                me.autoSkKq = 3;
                return true;
            }
            if (string.startsWith("Thay đổi phẩm chất")) {
                me.autoSkReset = true;
                return true;
            }
            if (string.startsWith("Đã mua")) {
                // mua Huyết Bồ Đề thành công ("Đã mua, món đồ đang ở trong hành trang.") - phase 173 tự thấy món mới
                return true;
            }
        }
        if (string != null && (string.startsWith("Chúc mừng") || string.startsWith("Đã bán"))) {
            if (me.autoJob == 8 && string.startsWith("Chúc mừng bạn đã tạo được thuộc tính")) {
                me.autoThemOk = true;
            }
            if (me.autoJob == 9 && string.startsWith("Chúc mừng bạn đã tạo được thuộc tính")) {
                me.autoCttOk = true;
                me.autoCttVuaRa = PauseMenu.cttCatKetQua(string);
            }
            return true;
        }
        if (string != null && me.autoJob == 11) {
            if (string.startsWith("Nâng cấp thành công")) {
                me.autoPetUpKq = 1;
                return true;
            }
            if (string.startsWith("Nâng cấp thất bại")) {
                me.autoPetUpKq = 2;
                return true;
            }
            if (string.startsWith("Vật phẩm cho ăn vượt quá sức ăn")) {
                me.autoPetDay = true;
                return true;
            }
            if (string.indexOf("đã có thể nâng cấp") >= 0 || string.indexOf("đã có thể tiến hoá") >= 0) {
                me.autoPetBaoNang = true;
                return true;
            }
            if (string.startsWith("Ngươi cần có") || string.startsWith("Không đủ Luyện kim dược")) {
                me.autoStop(new StringBuilder().append("Auto nuôi pet: không đủ nguyên liệu để nâng cấp pet (").append(string).append(" - nguyên liệu cấp 6 của cả 10 loại), đã dừng.").toString());
                return true;
            }
            if (string.startsWith("Không thể cho ăn khi chưa nâng cấp")) {
                me.autoPetCanNang = true;
                return true;
            }
        }
        if (string != null && me.autoJob == 10 && me.autoKtFull && (string.startsWith("Vật phẩm không hợp lệ") || string.startsWith("Chỉ có vật phẩm nhất phẩm"))) {
            me.autoCttBoQua = true;
            return true;
        }
        if (string != null && me.autoJob == 9 && me.autoCttFull && string.startsWith("Không thể cộng thuộc tính")) {
            me.autoCttBoQua = true;
            return true;
        }
        if (me.autoJob == 7) {
            me.autoLastAlert = string;
            return true;
        }
        if (string != null && me.autoJob == 6 && string.startsWith("Tinh luyện thành công")) {
            try {
                me.autoTbPct = Integer.parseInt(string.substring(string.lastIndexOf(':') + 1).trim());
            }
            catch (Exception exception) {
                // keep the previous value
            }
            me.autoTbResult = 1;
            return true;
        }
        if (string != null && me.autoJob == 6 && string.startsWith("Tinh luyện thất bại")) {
            me.autoTbResult = 2;
            return true;
        }
        if (string != null && me.autoJob == 5 && string.startsWith("Ngươi không đủ nguyên liệu")) {
            if (me.autoTkGrade == 5 && string.indexOf("ngũ hợp") >= 0) {
                me.autoTkNeedStone = true;
            } else {
                me.autoTkNoMat = true;
            }
            return true;
        }
        if (string != null && me.autoJob != 0 && string.indexOf("thành công") >= 0) {
            me.autoOkSeen = true;
            ++me.autoOkCount;
            return true;
        }
        me.autoStop(new StringBuilder().append("Auto ").append(AUTO_JOB_NAME[me.autoJob]).append(" đã dừng: ").append(string).toString());
        return true;
    }


    /** "Chúc mừng bạn đã tạo được thuộc tính Sét lan 0.5% cho vật phẩm ..." -> "Sét lan 0.5%". */
    private static String cttCatKetQua(String string) {
        int n = "Chúc mừng bạn đã tạo được thuộc tính ".length();
        int n2 = string.indexOf(" cho vật phẩm");
        if (n2 <= n) {
            return null;
        }
        return string.substring(n, n2).trim();
    }

    /** "Sét lan 0.5%" -> 5 (phần mười %), -1 nếu không đọc được. */
    private static int cttDocGiaTri(String string) {
        int n = string.lastIndexOf(' ');
        int n2 = string.indexOf('.', n + 1);
        int n3 = string.indexOf('%', n + 1);
        if (n < 0 || n2 < 0 || n3 < n2) {
            return -1;
        }
        try {
            return Integer.parseInt(string.substring(n + 1, n2)) * 10 + Integer.parseInt(string.substring(n2 + 1, n3));
        }
        catch (Exception exception) {
            return -1;
        }
    }

    private boolean cttDat(String string) {
        if (string == null) {
            return false;
        }
        int n = string.lastIndexOf(' ');
        if (n < 0) {
            return false;
        }
        if (this.autoCttLoai >= 0 && !string.substring(0, n).trim().equals(CTT_NAME[this.autoCttLoai])) {
            return false;
        }
        return PauseMenu.cttDocGiaTri(string) >= this.autoCttMin;
    }

    /** Full: sang món kế tiếp chưa đạt; false nếu đã hết đồ. */
    private boolean autoCttNext() {
        while (this.autoTargets != null && ++this.autoTargetPos < this.autoTargets.length) {
            ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
            if (itemInInventory == null || this.cttDatSan(itemInInventory)) continue;
            this.autoCttItem = this.autoTargets[this.autoTargetPos];
            this.autoCttVuaRa = null;
            this.autoPhase = 95;
            return true;
        }
        return false;
    }

    private String cttTongKet() {
        return new StringBuilder().append("Auto cộng thuộc tính tất cả đồ: xong ").append(this.autoFound).append("/").append(this.autoTargets == null ? 0 : this.autoTargets.length).append(" món, ").append(this.autoTries).append(" lần đập").toString();
    }

    private void autoStart(int n) {
        this.autoRunning = true;
        AutoGuard.on = true;
        this.autoJob = 0;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoMode = n;
        this.autoSlot = 0;
        this.autoPhase = 20;
        this.autoWaitTicks = 0;
        this.autoSold = 0;
        this.autoAfterHouse = 0;
        AutoGuard.pendingSell = 0;
        this.autoCraftedItemID = (short)-1;
    }

    static CMLocation autoFindHouseDoor() {
        Vector vector = Tilemap.mapchangeLocations;
        if (vector == null) {
            return null;
        }
        for (int i = 0; i < vector.size(); ++i) {
            CMLocation cMLocation = (CMLocation)vector.elementAt(i);
            if (cMLocation.toM != 103 && cMLocation.toM != 73 && cMLocation.toM != 83) continue;
            return cMLocation;
        }
        return null;
    }

    private short autoFindCard() {
        Vector vector = Char.inventory;
        if (vector == null) {
            return -1;
        }
        for (int i = 0; i < vector.size(); ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            if (itemInInventory.idTemplate != 675) continue;
            return itemInInventory.itemID;
        }
        return -1;
    }

    private void autoStartJob(int n) {
        if (!this.dongAnAnyChosen()) {
            Canvas.startOKDlg("Chưa tick dòng ẩn nào trong mục Dòng ẩn.");
            return;
        }
        Vector vector = Char.inventory;
        int n2 = 0;
        short[] sArray = new short[vector == null ? 0 : vector.size()];
        for (int i = 0; i < sArray.length; ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            if (itemInInventory.colorName == 0 || itemInInventory.colorName == 4 || itemInInventory.isSelling || itemInInventory.allAttribute == null || !this.autoItemMatches(itemInInventory)) continue;
            if (n == 1 && itemInInventory.level >= this.autoGoalFor(itemInInventory)) continue;
            if (n == 2 && this.autoLockOk(itemInInventory)) continue;
            if (n == 3 && !this.autoRepAny() && this.autoRepOk(itemInInventory)) continue;
            if ((n == 1 || n == 3) && itemInInventory.lock == 0) continue;
            if (n == 2 && this.autoLockTtc && !PauseMenu.autoIsTtc(itemInInventory)) continue;
            if (n >= 2 && (itemInInventory.nameCharSeal == null || !itemInInventory.nameCharSeal.toLowerCase().equals(Canvas.gameScr.mainChar.name.toLowerCase()))) continue;
            sArray[n2++] = itemInInventory.itemID;
        }
        if (n2 == 0) {
            Canvas.startOKDlg(n != 1 ? "Không có món nào có dòng ẩn đã tick cần làm (hoặc tất cả đã đạt yêu cầu)." : n == 1 ? new StringBuilder().append("Không có món nào có dòng ẩn đã tick cần nâng tới lv").append(this.autoGoalLevel).append(".").toString() : "Không có món nào có dòng ẩn đã tick trong hành trang.");
            return;
        }
        this.autoTargets = new short[n2];
        System.arraycopy(sArray, 0, this.autoTargets, 0, n2);
        this.autoTargetPos = 0;
        this.autoRunning = true;
        AutoGuard.on = true;
        this.autoJob = n;
        this.autoTries = 0;
        this.autoFound = 0;
        this.autoPhase = 10;
        this.autoWaitTicks = 0;
    }

    private static boolean autoIsTtc(ItemInInventory itemInInventory) {
        ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        return itemTemplate != null && itemTemplate.type >= 3 && itemTemplate.type <= 9;
    }

    private int autoGoalFor(ItemInInventory itemInInventory) {
        return itemInInventory.level % 5 == 1 ? this.autoGoalLevel + 1 : this.autoGoalLevel;
    }

    private ItemInInventory autoFindItem(short s) {
        Vector vector = Char.inventory;
        if (vector == null) {
            return null;
        }
        for (int i = 0; i < vector.size(); ++i) {
            ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
            if (itemInInventory.itemID != s) continue;
            return itemInInventory;
        }
        return null;
    }

    private boolean autoAnyKeyPressed() {
        for (int i = 0; i < Canvas.keyPressed.length; ++i) {
            if (!Canvas.keyPressed[i]) continue;
            return true;
        }
        return Canvas.isPointerDown || Canvas.isPointerClick;
    }

    private void autoBuildCraftQueue() {
        this.autoQueueNpc = -8;
        if (this.autoSlot == 0) {
            this.autoQueueMenu = new int[]{0, 1, 2, 11};
            this.autoQueueOption = new int[]{0, Canvas.gameScr.mainChar.clazz, (this.autoLevel - 21) / 5, 2};
        } else {
            int n2 = AUTO_SLOT_OPT[this.autoSlot];
            int n = n2 <= 2 ? 2 * ((this.autoLevel - 20) / 5) + (Canvas.gameScr.mainChar.gender == 1 ? 1 : 0) : (this.autoLevel - 20) / 5;
            this.autoQueueMenu = new int[]{0, 3, 4, 5, 6, 11};
            this.autoQueueOption = new int[]{1, this.heMaPhap ? 0 : 1, Canvas.gameScr.mainChar.clazz, n2, n, 2};
        }
        this.autoQueuePos = 0;
        WindowInfoScr.gI().listCheDo = null;
    }

    private byte[][] autoFillMaterial() {
        WindowInfoScr windowInfoScr = WindowInfoScr.gI();
        Vector vector = windowInfoScr.listCheDo;
        Vector vector2 = windowInfoScr.listTemp;
        if (vector == null || vector.size() == 0 || vector2 == null) {
            return null;
        }
        int n = WindowInfoScr.optionCreateItem;
        int[] nArray = new int[vector2.size()];
        byte[][] byArray = new byte[vector.size()][6];
        for (int i = 0; i < vector.size(); ++i) {
            Mineral mineral = (Mineral)vector.elementAt(i);
            int n2 = mineral.number;
            int n3 = n <= 2 && (n != 2 || i == 0) || n > 2 ? (n == 0 ? 0 : 3) : 5;
            for (int j = 5; j >= n3 && n2 > 0; --j) {
                for (int k = 0; k < vector2.size() && n2 > 0; ++k) {
                    GemTemplate gemTemplate = (GemTemplate)vector2.elementAt(k);
                    if (gemTemplate.id != mineral.idTemplate + j) continue;
                    int n4 = gemTemplate.number - nArray[k];
                    if (n4 <= 0) continue;
                    if (n4 > n2) {
                        n4 = n2;
                    }
                    nArray[k] = nArray[k] + n4;
                    byArray[i][j] = (byte)(byArray[i][j] + n4);
                    n2 -= n4;
                }
            }
            if (n2 <= 0) continue;
            return null;
        }
        return byArray;
    }

    private boolean autoFillTopGrade() {
        byte[] byArray = this.autoFill[this.autoFill.length - 1];
        for (int i = 0; i < 5; ++i) {
            if (byArray[i] < byArray[5]) continue;
            return false;
        }
        return true;
    }

    private boolean autoStageNext() {
        WindowInfoScr windowInfoScr = WindowInfoScr.gI();
        Vector vector = windowInfoScr.listCheDo;
        if (vector == null || windowInfoScr.listEp == null || this.autoFill == null) {
            return false;
        }
        while (this.autoStageIdx < this.autoFill.length * 6) {
            int n = this.autoStageIdx / 6;
            int n2 = 5 - this.autoStageIdx % 6;
            ++this.autoStageIdx;
            int n3 = this.autoFill[n][n2];
            if (n3 <= 0) continue;
            Mineral mineral = (Mineral)vector.elementAt(n);
            int n4 = mineral.idTemplate + n2;
            GemTemplate gemTemplate = null;
            for (int i = 0; i < windowInfoScr.listTemp.size(); ++i) {
                GemTemplate gemTemplate2 = (GemTemplate)windowInfoScr.listTemp.elementAt(i);
                if (gemTemplate2.id != n4) continue;
                gemTemplate = gemTemplate2;
                break;
            }
            GemTemplate gemTemplate3 = new GemTemplate(n4);
            if (gemTemplate != null) {
                GemTemplate.copyData(gemTemplate, gemTemplate3);
                if (WindowInfoScr.khoaCheDo == 2) {
                    gemTemplate.number -= n3;
                    if (gemTemplate.number <= 0) {
                        windowInfoScr.listTemp.removeElement(gemTemplate);
                    }
                }
            }
            gemTemplate3.number = n3;
            windowInfoScr.listEp.addElement(gemTemplate3);
            return true;
        }
        return false;
    }

    private void autoSellQuiet(short s) {
        ++AutoGuard.pendingSell;
        this.autoSoldIds.addElement(new Integer(s));
        GameService.gI().sellItem(s);
    }

    private short[] autoInventoryIds() {
        Vector vector = Char.inventory;
        int n = vector == null ? 0 : vector.size();
        short[] sArray = new short[n];
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            short s = ((ItemInInventory)vector.elementAt(i)).itemID;
            if (this.autoSoldIds.contains(new Integer(s))) continue;
            sArray[n2++] = s;
        }
        short[] sArray2 = new short[n2];
        System.arraycopy(sArray, 0, sArray2, 0, n2);
        return sArray2;
    }

    public static void autoPaintStatus(Graphics graphics) {
        if (me == null || !me.autoRunning || Font.normalFont == null || Font.normalFont[0] == null) {
            return;
        }
        String string = me.autoStatusText();
        Font font = Font.normalFont[0];
        if (font.getWidth(string) > Canvas.w - 12 && string.startsWith("Auto ")) {
            string = string.substring(5);
        }
        while (string.length() > 6 && font.getWidth(string) > Canvas.w - 12) {
            string = string.substring(0, string.length() - 1);
        }
        int n = Math.min(Canvas.w, font.getWidth(string) + 12);
        int n2 = font.getHeight() + 6;
        int n3 = (Canvas.w - n) / 2;
        graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
        graphics.setClip(0, 0, Canvas.w, Canvas.h);
        graphics.setColor(0xFF0B2E2E);
        graphics.fillRect(n3, 2, n, n2);
        graphics.setColor(0xFFE0B000);
        graphics.drawRect(n3, 2, n - 1, n2 - 1);
        font.drawString(graphics, string, Canvas.w / 2, 5, 2);
    }

    private String autoStatusText() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.autoJob == 0) {
            stringBuilder.append("Auto chế đồ: ").append(AUTO_SLOT_NAME[Math.min(this.autoSlot, AUTO_SLOT_NAME.length - 1)]).append(" lv").append(this.autoLevel).append(" - lần ").append(this.autoTries + 1).append(" - đạt ").append(this.autoFound);
        } else if (this.autoJob == 4) {
            stringBuilder.append("Auto đồ thú: ").append(PET_SLOT_NAME[Math.min(this.autoPetCurSlot, 4)]).append(" lv").append(this.autoPetLevel).append(" - hợp ").append(this.autoTries).append(" - đạt ").append(this.autoFound);
        } else if (this.autoJob == 8) {
            ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
            int n2 = itemInInventory == null ? 0 : this.autoThemLines(itemInInventory, new boolean[2]);
            if (this.autoThemFull) {
                stringBuilder.append("Auto thêm dòng full: món ").append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.length)).append("/").append(this.autoTargets == null ? 0 : this.autoTargets.length).append(" - lần ").append(this.autoTries).append(" - xong ").append(this.autoFound);
            } else {
                stringBuilder.append("Auto thêm dòng: lần ").append(this.autoTries).append(" - đang có ").append(n2).append(" dòng");
            }
        } else if (this.autoJob == 14 && Canvas.currentScreen != Canvas.gameScr) {
            stringBuilder.append("Nghiền bột: tạm dừng - đang mở màn khác, hãy đóng lại");
        } else if (this.autoJob == 14) {
            stringBuilder.append("Nghiền bột: bột ").append(PauseMenu.hklDa(NB_BOT)).append(" | nghiền ").append(this.autoFound).append(" | chờ ").append(PauseMenu.nbDemNghien()).append(this.autoNbChe > 0 ? new StringBuilder().append(" | chế ").append(this.autoNbChe).toString() : "").append(this.autoNbBan > 0 ? new StringBuilder().append(" | bán trắng ").append(this.autoNbBan).toString() : "").append(this.autoNbHet ? " | nghiền nốt" : "");
        } else if (this.autoJob == 13) {
            ItemInInventory itemInInventory = PauseMenu.hklMon();
            stringBuilder.append("Nâng cấp Hỏa kỳ lân: cấp ").append(itemInInventory == null ? 0 : itemInInventory.idTemplate - 680).append(" - đã đập ").append(this.autoFound).append(" - Hỏa thạch ").append(PauseMenu.hklDa(HOA_THACH_C1)).append("/").append(PauseMenu.hklDa(HOA_THACH_C2)).append(this.autoHklDaMua > 0 ? new StringBuilder().append(" - mua ").append(this.autoHklDaMua).toString() : "");
        } else if (this.autoJob == 12) {
            int n3 = PauseMenu.skPhamChat();
            stringBuilder.append("Học skill pet: ").append(this.skMucTieu()).append(" - phẩm chất ").append(n3 < 0 ? "chưa có" : SK_PHAM_CHAT[n3]).append(" - sách ").append(this.autoSkSach).append(" - Huyết Bồ Đề ").append(this.autoSkHbd).append(this.autoSkDaMua > 0 ? new StringBuilder().append(" (mua ").append(this.autoSkDaMua).append(")").toString() : "");
        } else if (this.autoJob == 11) {
            stringBuilder.append("Nuôi pet: cấp ").append(PauseMenu.petCap()).append("+").append(PauseMenu.petPhanTram() / 10).append(".").append(Math.max(0, PauseMenu.petPhanTram()) % 10).append("% - sức ăn ").append(PauseMenu.petSucAn()).append("/300 - đã ăn ").append(this.autoPetDaAn).append(" - nâng cấp ").append(this.autoPetDaNang).append(" - relog ").append(this.autoPetRelog).append(this.autoPhase == 130 || this.autoPhase == 131 ? " - đang nâng cấp" : "").append(this.autoPhase >= 140 && this.autoPhase <= 145 ? " - đang lên Thành" : (this.autoPhase >= 150 ? " - đăng nhập lại" : (this.autoPhase == 116 || this.autoPhase == 127 || this.autoPhase == 162 ? new StringBuilder().append(" - lô ").append(this.autoPetLoCho).append(" món").toString() : "")));
        } else if (this.autoJob == 10) {
            ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
            if (this.autoKtFull) {
                stringBuilder.append("Auto khóa đồ thú: món ").append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.length)).append("/").append(this.autoTargets == null ? 0 : this.autoTargets.length).append(" - xong ").append(this.autoFound).append(" - ");
            } else {
                stringBuilder.append("Auto khóa đồ thú: ");
            }
            stringBuilder.append(this.autoKtMuc >= 21 ? "21%" : "20-21%").append(" - lần ").append(this.autoTries).append(" - đang ").append(itemInInventory == null ? 0 : PauseMenu.ktGiaTri(itemInInventory)).append("%");
        } else if (this.autoJob == 9) {
            if (this.autoCttFull) {
                stringBuilder.append("Auto cộng thuộc tính: món ").append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.length)).append("/").append(this.autoTargets == null ? 0 : this.autoTargets.length).append(" - xong ").append(this.autoFound).append(" - ");
            } else {
                stringBuilder.append("Auto cộng thuộc tính: ");
            }
            stringBuilder.append(this.autoCttLoai < 0 ? "Bất kỳ" : CTT_NAME[this.autoCttLoai]).append(" ").append(PauseMenu.cttMucText(this.autoCttMin)).append(" - lần ").append(this.autoTries).append(this.autoCttVuaRa == null ? "" : new StringBuilder().append(" - vừa ra ").append(this.autoCttVuaRa).toString());
        } else if (this.autoJob == 7) {
            ItemInInventory itemInInventory = this.autoTargets == null || this.autoTargetPos >= this.autoTargets.length ? null : this.autoFindItem(this.autoTargets[this.autoTargetPos]);
            stringBuilder.append("Auto luyện đồ: món ").append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.length)).append("/").append(this.autoTargets == null ? 0 : this.autoTargets.length).append(" - Cộng ").append(itemInInventory == null ? 0 : itemInInventory.plusTemplate).append("/").append(this.autoLuyenTarget).append(" - xong ").append(this.autoFound);
        } else if (this.autoJob == 6) {
            stringBuilder.append("Auto thần binh: tinh luyện ").append(this.autoTbPct < 0 ? "--" : String.valueOf(this.autoTbPct)).append("% - lần ").append(this.autoTries).append(" - thành công ").append(this.autoFound);
        } else if (this.autoJob == 5) {
            stringBuilder.append("Auto thần khí: ").append(this.autoFound).append("/").append(this.autoTkQty).append(this.autoTkFilter == 0 ? " món 9 dòng" : " món").append(" - đã chế ").append(this.autoTries);
        } else {
            stringBuilder.append("Auto ").append(AUTO_JOB_NAME[this.autoJob]).append(": món ").append(Math.min(this.autoTargetPos + 1, this.autoTargets == null ? 0 : this.autoTargets.length)).append("/").append(this.autoTargets == null ? 0 : this.autoTargets.length).append(" - ").append(this.autoTries).append(" lần");
        }
        int n = this.autoPhase;
        if (this.autoJob == 14) {
            // nghiền bột: dòng gọn, chỉ báo thêm khi đang vào nhà
            return n == 20 || n == 21 ? new StringBuilder().append(stringBuilder.toString()).append(" | vào nhà").toString() : stringBuilder.toString();
        }
        stringBuilder.append(n == 20 || n == 21 ? " - vào nhà" : (n >= 41 && n <= 43 || n == 62 || n == 63 ? " - mua" : (n == 50 || n == 51 ? " - ra làng" : " - đang chế")));
        long l = System.currentTimeMillis() / 400L % 4L;
        for (int i = 0; (long)i < l; ++i) {
            stringBuilder.append('.');
        }
        return stringBuilder.toString();
    }

    private ItemInInventory autoNewItem() {
        ItemInInventory itemInInventory = this.autoNewItem0();
        if (itemInInventory != null) {
            this.autoSoldIds.removeAllElements();
        }
        return itemInInventory;
    }

    private ItemInInventory autoNewItem0() {
        Vector vector = Char.inventory;
        if (vector == null || this.autoSnapshot == null) {
            return null;
        }
        ItemInInventory itemInInventory = null;
        block0: for (int i = 0; i < vector.size(); ++i) {
            ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
            for (int j = 0; j < this.autoSnapshot.length; ++j) {
                if (this.autoSnapshot[j] == itemInInventory2.itemID) continue block0;
            }
            if (itemInInventory != null && itemInInventory2.itemID <= itemInInventory.itemID) continue;
            itemInInventory = itemInInventory2;
        }
        return itemInInventory;
    }

    private ItemInInventory autoLatestItem() {
        Vector vector = Char.inventory;
        if (vector == null || vector.size() == 0) {
            return null;
        }
        ItemInInventory itemInInventory = null;
        for (int i = 0; i < vector.size(); ++i) {
            ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
            if (itemInInventory != null && itemInInventory2.itemID <= itemInInventory.itemID) continue;
            itemInInventory = itemInInventory2;
        }
        return itemInInventory;
    }

    private boolean autoItemMatches(ItemInInventory itemInInventory) {
        for (int i = 0; i < itemInInventory.allAttribute.size(); ++i) {
            InfoAttributeItem infoAttributeItem = (InfoAttributeItem)itemInInventory.allAttribute.elementAt(i);
            short s = infoAttributeItem.getID();
            if (s < 28 || s > 32) continue;
            int n = s - 28;
            int n2 = infoAttributeItem.getValueAtt();
            if (!this.dongAnChon[n * 2] && (n2 < PauseMenu.dongAnMax(n) || !this.dongAnChon[n * 2 + 1])) continue;
            return true;
        }
        return false;
    }

    private void autoTick() {
        if (!this.autoRunning) {
            return;
        }
        if (this.autoAnyKeyPressed()) {
            this.autoStop("Đã dừng auto chế đồ.");
            return;
        }
        if (this.autoJob == 0 && this.autoSlot >= AUTO_SLOT_NAME.length) {
            this.autoStop("Auto chế đồ đã hoàn tất.");
            return;
        }
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        if (this.autoJob == 14) {
            this.nghienTick();
        }
        if (this.autoWaitTicks > 0) {
            --this.autoWaitTicks;
            return;
        }
        switch (this.autoPhase) {
            case 0: {
                this.autoLevel = this.autoJob == 6 ? (this.autoTbLevel == 60 ? 61 : this.autoTbLevel - 9) : (this.autoSlot == 0 ? (this.autoMode == 0 ? 21 : 36) : (this.autoMode == 0 ? 20 : 35));
                this.autoBuildCraftQueue();
                this.autoPhase = 1;
                break;
            }
            case 1: {
                while (this.autoQueuePos < this.autoQueueMenu.length) {
                    GameService.gI().doMenuOption(this.autoQueueNpc, (byte)this.autoQueueMenu[this.autoQueuePos], this.autoQueueOption[this.autoQueuePos]);
                    ++this.autoQueuePos;
                }
                this.autoPhase = 2;
                this.autoWaitCount = 0;
                break;
            }
            case 2: {
                if (WindowInfoScr.gI().listCheDo != null) {
                    this.autoPhase = 3;
                    break;
                }
                if (++this.autoWaitCount > 200) {
                    this.autoStop("Auto chế đồ: không nhận được danh sách nguyên liệu, đã dừng.");
                }
                break;
            }
            case 3: {
                this.autoFill = this.autoJob == 11 || this.autoJob == 14 ? this.petFillCap() : this.autoFillMaterial();
                Canvas.menu.showMenu = false;
                if (this.autoFill == null && this.autoJob == 14) {
                    // món này hết nguyên liệu đã chọn -> thử món kế; hết hẳn thì nghiền nốt rồi dừng
                    if (Canvas.currentScreen == WindowInfoScr.gI()) {
                        WindowInfoScr.gI().right.action.perform();
                    }
                    if (++this.autoSlot >= AUTO_SLOT_NAME.length) {
                        this.autoNbHet = true;
                        this.autoPhase = 194;
                        break;
                    }
                    this.autoPhase = 0;
                    this.autoWaitTicks = 1;
                    break;
                }
                if (this.autoFill == null && this.autoJob == 11) {
                    // món này thiếu nguyên liệu cấp đã chọn -> thử món kế tiếp
                    if (Canvas.currentScreen == WindowInfoScr.gI()) {
                        WindowInfoScr.gI().right.action.perform();
                    }
                    if (++this.autoSlot >= AUTO_SLOT_NAME.length) {
                        this.autoStop(new StringBuilder().append("Auto nuôi pet: không có món nào chế được bằng ").append(this.autoPetLoai < 0 ? "nguyên liệu" : PET_LOAI_TEN[this.autoPetLoai]).append(" (sơ cấp cấp ").append(this.autoPetCapSo + 1).append(", cao cấp cấp ").append(this.autoPetCapNL + 1).append(") để chế đồ, đã dừng.").toString());
                        return;
                    }
                    this.autoPhase = 0;
                    this.autoWaitTicks = 1;
                    break;
                }
                if (this.autoFill == null) {
                    if (Canvas.currentScreen == WindowInfoScr.gI()) {
                        WindowInfoScr.gI().right.action.perform();
                    }
                    this.autoStop("Auto chế đồ: không đủ nguyên liệu tổng hợp (thường + khóa), đã dừng.");
                    return;
                }
                if (this.autoJob == 6 && (this.autoFill.length < 2 || this.autoFill[0][5] < ((Mineral)WindowInfoScr.gI().listCheDo.elementAt(0)).number || this.autoFill[1][5] < ((Mineral)WindowInfoScr.gI().listCheDo.elementAt(1)).number)) {
                    if (Canvas.currentScreen == WindowInfoScr.gI()) {
                        WindowInfoScr.gI().right.action.perform();
                    }
                    this.autoStop("Auto thần binh: cần đủ nguyên liệu cấp 6 cho cả 2 loại (mới chắc ra vũ khí Hoàn mỹ Nhất phẩm), đã dừng.");
                    return;
                }
                if (this.autoJob == 2 && !this.autoFillTopGrade()) {
                    if (Canvas.currentScreen == WindowInfoScr.gI()) {
                        WindowInfoScr.gI().right.action.perform();
                    }
                    this.autoStop("Auto khóa: không đủ nguyên liệu cấp 6 (phải chiếm nhiều nhất mới ra được 20-21%), đã dừng.");
                    return;
                }
                WindowInfoScr.gI().name = this.autoJob == 11 || this.autoJob == 14 ? "[Auto]" : this.autoJob == 0 || this.autoJob == 6 ? new StringBuilder().append("[Auto] ").append(AUTO_SLOT_NAME[this.autoSlot]).append(" lv").append(this.autoLevel).append(" - lần ").append(this.autoTries + 1).toString() : new StringBuilder().append("[Auto] ").append(AUTO_JOB_NAME[this.autoJob]).append(" - món ").append(this.autoTargetPos + 1).append("/").append(this.autoTargets.length).toString();
                this.autoStageIdx = 0;
                while (this.autoStageNext()) {
                }
                this.autoPhase = 7;
                this.autoWaitTicks = this.autoJob == 11 ? 0 : 1;
                break;
            }
            case 7: {
                int n = WindowInfoScr.gI().listCheDo.size();
                if (Canvas.currentScreen == WindowInfoScr.gI()) {
                    WindowInfoScr.gI().right.action.perform();
                }
                this.autoSnapshot = this.autoInventoryIds();
                this.autoOkSeen = false;
                this.autoRefObj = (this.autoJob == 2 || this.autoJob == 3) && this.autoTargets != null ? this.autoFindItem(this.autoTargets[this.autoTargetPos]) : null;
                GameService.gI().doCheDo((short)n, WindowInfoScr.idTemp, this.autoFill);
                if (this.autoJob == 11) {
                    // ghi "công thức" để các lần sau chế cả lô không cần chờ server mở cửa sổ
                    this.autoPetQNpc = this.autoQueueNpc;
                    this.autoPetQMenu = this.autoQueueMenu;
                    this.autoPetQOpt = this.autoQueueOption;
                    this.autoPetIdTemp = WindowInfoScr.idTemp;
                    this.autoPetFillMau = new byte[this.autoFill.length][];
                    this.autoPetNLId = new int[this.autoFill.length];
                    for (int i = 0; i < this.autoFill.length; ++i) {
                        this.autoPetFillMau[i] = new byte[this.autoFill[i].length];
                        System.arraycopy(this.autoFill[i], 0, this.autoPetFillMau[i], 0, this.autoFill[i].length);
                        this.autoPetNLId[i] = ((Mineral)WindowInfoScr.gI().listCheDo.elementAt(i)).idTemplate;
                    }
                    this.autoPetCoCongThuc = true;
                    this.autoPetLoCho = 1;
                    this.batDauDs();
                    // server xử lý tuần tự: xin luôn danh sách "Cho pet ăn" - đã có món vừa chế
                    this.autoPetDsAn = null;
                    this.autoPetDay = false;
                    this.autoPetCanNang = false;
                    this.xinDsAn();
                }
                this.autoPhase = this.autoJob == 0 ? 4 : (this.autoJob == 6 ? 70 : (this.autoJob == 11 ? 110 : (this.autoJob == 14 ? 190 : 11)));
                this.autoWaitCount = 0;
                break;
            }
            case 10: {
                if (this.autoTargetPos >= this.autoTargets.length) {
                    this.autoStop(new StringBuilder().append("Auto ").append(AUTO_JOB_NAME[this.autoJob]).append(" đã hoàn tất.").toString());
                    return;
                }
                ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
                if (itemInInventory == null || this.autoJobDone(itemInInventory)) {
                    if (itemInInventory != null) {
                        ++this.autoFound;
                    }
                    ++this.autoTargetPos;
                    break;
                }
                GameService.gI().doRequestActionCheDo(this.autoJob == 1 ? 11 : (this.autoJob == 2 ? 12 : 13), itemInInventory.itemID);
                this.autoQueueNpc = -502;
                this.autoQueueMenu = new int[]{0};
                this.autoQueueOption = new int[]{2};
                this.autoQueuePos = 0;
                WindowInfoScr.gI().listCheDo = null;
                this.autoPhase = 1;
                break;
            }
            case 11: {
                if (this.autoJob == 1) {
                    ItemInInventory itemInInventory = this.autoNewItem();
                    if (itemInInventory != null) {
                        ++this.autoTries;
                        this.autoTargets[this.autoTargetPos] = itemInInventory.itemID;
                        this.autoPhase = 10;
                        break;
                    }
                } else if (this.autoOkSeen) {
                    ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
                    if (itemInInventory != null && itemInInventory == this.autoRefObj && this.autoWaitCount < 60) {
                        ++this.autoWaitCount;
                        break;
                    }
                    ++this.autoTries;
                    if (this.autoJob == 3 && this.autoRepAny()) {
                        ++this.autoFound;
                        ++this.autoTargetPos;
                    }
                    this.autoPhase = 10;
                    break;
                }
                if (++this.autoWaitCount <= 250) break;
                this.autoStop(new StringBuilder().append("Auto ").append(AUTO_JOB_NAME[this.autoJob]).append(": không nhận được kết quả từ server, đã dừng.").toString());
                return;
            }
            case 4: {
                boolean bl;
                ItemInInventory itemInInventory = this.autoNewItem();
                if (itemInInventory == null) {
                    if (++this.autoWaitCount <= 250) break;
                    this.autoStop("Auto ch\u1ebf \u0111\u1ed3: kh\u00f4ng nh\u1eadn \u0111\u01b0\u1ee3c \u0111\u1ed3, \u0111\u00e3 d\u1eebng (c\u00f3 th\u1ec3 kh\u00f4ng \u0111\u1ee7 nguy\u00ean li\u1ec7u ho\u1eb7c kh\u00f4ng \u1edf trong Th\u00e0nh/L\u00e0ng).");
                    return;
                }
                this.autoCraftedItemID = itemInInventory.itemID;
                ++this.autoTries;
                bl = itemInInventory.colorName != 0 && itemInInventory.colorName != 4;
                if (!this.dongAnAnyChosen() || this.autoItemMatches(itemInInventory)) {
                    if (this.dongAnAnyChosen()) {
                        ++this.autoFound;
                    }
                    ++this.autoSlot;
                    this.autoPhase = 0;
                    break;
                }
                if (this.autoMode == 1 && bl && this.autoLevel < (this.autoSlot == 0 ? 51 : 50)) {
                    this.autoLevel += 5;
                    this.autoPhase = 5;
                    break;
                }
                if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                    this.autoSellQuiet(this.autoCraftedItemID);
                    ++this.autoSold;
                }
                this.autoPhase = 0;
                this.autoWaitTicks = 2;
                break;
            }
            case 109: {
                // vòng mới: kiểm tra pet trước khi chế
                // NPC thợ săn chỉ nâng cấp khi cấp < 40. Từ cấp 40 pet (đã lên cấp 5) vẫn luyện tiếp
                // tới 49.99% - lúc đó server không cho ăn nữa, phải tiến hoá bằng Huyết linh thảo
                if (PauseMenu.petCanTienHoa() || PauseMenu.petCap() >= 40 && (this.autoPetBaoNang || this.autoPetCanNang)) {
                    this.autoStop(new StringBuilder().append("Auto nuôi pet: pet đã đạt ").append(PauseMenu.petCap()).append("+99.9%, cần tiến hoá.").toString());
                    return;
                }
                // cần nâng cấp: đọc từ thông tin pet, từ câu báo "đã có thể nâng cấp", hoặc từ việc
                // server từ chối cho ăn "Không thể cho ăn khi chưa nâng cấp" (lỡ hai cách kia trượt)
                if (PauseMenu.petCanNang() || this.autoPetBaoNang || this.autoPetCanNang) {
                    this.autoPhase = 130;
                    break;
                }
                // món chế từ trước chưa ăn (sức ăn đầy / cần nâng cấp giữa lô) -> ăn trước
                if (this.petConLai() > 0) {
                    this.autoPhase = 125;
                    break;
                }
                int n2 = PauseMenu.petSucAn();
                // sau khi đăng nhập lại sức ăn hiển thị vẫn là số cũ - server chỉ trừ ở lần ăn đầu
                if (!this.autoPetSauRelog && n2 >= 0 && n2 + this.petAnTon() > 300) {
                    this.autoPhase = 150;
                    break;
                }
                if (PauseMenu.autoBagFull()) {
                    this.autoStop("Auto nuôi pet: hành trang đầy, đã dừng.");
                    return;
                }
                if (this.autoPetTuHon) {
                    this.autoPhase = 160;
                    break;
                }
                if (this.autoPetCoCongThuc) {
                    this.autoPhase = 115;
                    break;
                }
                this.autoSlot = this.autoSlot >= AUTO_SLOT_NAME.length ? 0 : this.autoSlot;
                this.autoPhase = 0;
                break;
            }
            case 110: {
                // món đầu tiên (chế thường) đã về chưa - snapshot ở phase 7
                if (this.petMonMoi() == 0) {
                    if (++this.autoWaitCount <= 250) break;
                    this.autoStop("Auto nuôi pet: chế đồ không ra món nào (thiếu xu? cần 5000 xu/món), đã dừng.");
                    return;
                }
                ++this.autoTries;
                // danh sách đồ ăn đã xin ngay sau lệnh chế (phase 7) -> cho ăn luôn
                this.autoPhase = 126;
                this.autoWaitCount = 0;
                break;
            }
            case 115: {
                // chế theo công thức (không chờ server mở cửa sổ), xin luôn danh sách đồ ăn
                int n = this.petCheDuoc(PET_LO_TOI_DA);
                if (n < 1) {
                    // không đủ cho công thức này -> quay lại chế thường (tự thử món khác / báo lỗi)
                    this.autoPetCoCongThuc = false;
                    this.autoPhase = 109;
                    break;
                }
                this.petGuiChe(n);
                this.autoPetLoCho = n;
                this.batDauDs();
                this.autoPetDay = false;
                this.autoPetCanNang = false;
                this.xinDsAn();
                this.autoPhase = 116;
                this.autoWaitCount = 0;
                break;
            }
            case 116: {
                // chờ cả lô về (hoặc hết giờ mà đã có ít nhất 1 món)
                int n = this.petMonMoi();
                if (n < this.autoPetLoCho && ++this.autoWaitCount <= 300) break;
                if (n == 0) {
                    this.autoPetCoCongThuc = false;
                    this.autoPhase = 109;
                    break;
                }
                this.autoTries += n;
                this.autoPhase = 126;
                this.autoWaitCount = 0;
                break;
            }
            case 125: {
                this.batDauDs();
                this.autoPetDsAn = null;
                this.autoPetDay = false;
                this.autoPetCanNang = false;
                this.xinDsAn();
                this.autoPhase = 126;
                this.autoWaitCount = 0;
                break;
            }
            case 126: {
                if (!this.dsSan()) {
                    if (++this.autoWaitCount <= 400) break;
                    if (!this.autoPetXinLai) {
                        // server bận (chế đồ ghi log lâu) - xin lại danh sách 1 lần rồi mới bỏ cuộc
                        this.autoPetXinLai = true;
                        this.autoPhase = 125;
                        break;
                    }
                    this.autoStop("Auto nuôi pet: NPC thợ săn không trả lời (phải đứng gần NPC thợ săn trong Thành), đã dừng.");
                    return;
                }
                this.autoPetXinLai = false;
                if (this.petConLai() == 0) {
                    this.autoPhase = 109;
                    break;
                }
                int n = this.petTinhLo();
                if (n < 0) {
                    this.autoStop("Auto nuôi pet: trong hành trang có đồ trùng tên món chế nhưng cấp cao hơn 21 (hoặc danh sách đồ ăn lệch), đã dừng để tránh pet ăn nhầm đồ.");
                    return;
                }
                // chụp id lô SAU petTinhLo (có thể vừa gộp thêm món cùng tên cấp 20/21)
                this.autoPetLoTruocIds = new short[this.autoPetLo.size()];
                for (int i = 0; i < this.autoPetLoTruocIds.length; ++i) {
                    this.autoPetLoTruocIds[i] = (short)((Integer)this.autoPetLo.elementAt(i)).intValue();
                }
                this.autoPetLoCho = n % 256;
                // lượt cho ăn mới: chỉ chờ các danh sách xin từ đây
                this.autoPetDsCho = this.autoPetDsNhan;
                // vừa ăn vừa chế. THỨ TỰ QUAN TRỌNG: CHẾ món sau TRƯỚC rồi mới CHO ĂN món này.
                // Server tái dùng id món đồ: ăn xong id X được trả lại, lệnh chế đi SAU sẽ lấy
                // đúng X -> client tưởng món chưa bị ăn và món mới chưa về. Chế trước thì lúc đó
                // X còn đang dùng nên món mới mang id khác. Vị trí cho ăn vẫn đúng: danh sách đồ ăn
                // trên server tạo từ lần xin trước, món mới chỉ thêm vào cuối hành trang.
                this.autoPetCheTheo = 0;
                if (this.autoPetCoCongThuc && !this.autoPetDay && !this.autoPetCanNang && !this.autoPetBaoNang && !PauseMenu.petCanNang()) {
                    int n2 = this.petCheDuoc(PET_LO_TOI_DA);
                    if (n2 > 0) {
                        this.petGuiChe(n2);
                        this.autoPetCheTheo = n2;
                    }
                }
                this.petGuiAn(n);
                if (this.autoPetCheTheo > 0) {
                    // danh sách mới: đã bỏ món vừa ăn, đã có món vừa chế
                    this.xinDsAn();
                }
                this.autoPhase = 127;
                this.autoWaitCount = 0;
                break;
            }
            case 127: {
                // chờ pet ăn xong nhóm vừa gửi và món chế nối đuôi về; đầy / cần nâng cấp thì server từ chối phần còn lại
                int n = 0;
                for (int i = 0; i < this.autoPetLoTruocIds.length; ++i) {
                    if (this.autoFindItem(this.autoPetLoTruocIds[i]) != null) continue;
                    ++n;
                }
                int n2 = this.autoPetCheTheo > 0 ? this.petMonMoi() : 0;
                boolean bl = this.autoPetDay || this.autoPetCanNang || this.autoPetBaoNang;
                boolean bl2 = n < this.autoPetLoCho || this.autoPetCheTheo > 0 && n2 < this.autoPetCheTheo;
                if (bl2 && ++this.autoWaitCount <= (bl ? 40 : 400)) break;
                this.petConLai();
                this.autoPetDaAn += n;
                this.autoTries += n2;
                if (n > 0) {
                    this.autoPetSauRelog = false;
                }
                if (this.autoPetCheTheo > 0 && n2 == 0) {
                    // món nối đuôi không ra (hết xu/nguyên liệu...) -> 109 chế thường để báo đúng lỗi
                    this.autoPetCoCongThuc = false;
                }
                if (this.autoPetDay && n < this.autoPetLoCho) {
                    if (this.autoPetSauRelog) {
                        this.autoStop(new StringBuilder().append("Auto nuôi pet: vừa đăng nhập lại mà sức ăn vẫn đầy (").append(PauseMenu.petSucAn()).append("/300), đã dừng.").toString());
                        return;
                    }
                    this.autoPhase = 150;
                    break;
                }
                if (n == 0 && !bl) {
                    this.autoStop("Auto nuôi pet: cho ăn không có phản hồi, đã dừng.");
                    return;
                }
                if (bl || this.autoPetLo.size() == 0) {
                    // cần nâng cấp / hết món: 109 lo nâng cấp rồi cho ăn nốt món còn lại
                    this.autoPhase = 109;
                    break;
                }
                // còn món (món nối đuôi vừa về): danh sách mới đã xin sau lệnh chế -> cho ăn luôn
                this.autoPhase = this.autoPetCheTheo > 0 ? 126 : 125;
                this.autoWaitCount = 0;
                break;
            }
            case 190: {
                // món vừa chế đã về chưa (snapshot ở phase 7)
                ItemInInventory itemInInventory = this.autoNewItem();
                if (itemInInventory == null) {
                    if (++this.autoWaitCount <= 250) break;
                    this.autoStop("Auto nghiền bột: chế đồ không ra món nào (cần 5000 xu/món chế + 2000 xu/lần nghiền), đã dừng.");
                    return;
                }
                ++this.autoNbChe;
                if (itemInInventory.colorName == 0) {
                    // đồ trắng không nghiền được -> bán
                    this.autoSellQuiet(itemInInventory.itemID);
                    ++this.autoNbBan;
                }
                this.autoPhase = 193;
                this.autoWaitCount = 0;
                break;
            }
            case 193: {
                // hàng đợi đủ thì chờ nghiền bớt rồi mới chế tiếp (khỏi đầy hành trang)
                int n0 = PauseMenu.nbDemNghien();
                if (n0 >= NB_HANG_DOI_TOI_DA) break;
                if (PauseMenu.autoBagFull()) {
                    // đầy: chờ nghiền bớt; không còn gì để nghiền mà vẫn đầy thì dừng
                    if (n0 > 0 || this.autoNbCo) break;
                    int n = 0;
                    Vector vector = Char.inventory;
                    for (int i = 0; vector != null && i < vector.size(); ++i) {
                        if (!PauseMenu.nbNghienDuoc((ItemInInventory)vector.elementAt(i))) continue;
                        ++n;
                    }
                    this.autoStop(new StringBuilder().append("Auto nghiền bột: hành trang đầy, nghiền được ").append(this.autoFound).append(" món (trong túi ").append(vector == null ? 0 : vector.size()).append(" món, ").append(n).append(" món cấp 20/21 có màu; map ").append(Canvas.gameScr.map).append(", gửi ").append(this.autoNbGui).append(").").toString());
                    return;
                }
                this.autoPhase = 0;
                break;
            }
            case 194: {
                // hết nguyên liệu: nghiền nốt đồ còn trong túi rồi dừng
                if (PauseMenu.nbDemNghien() > 0 || this.autoNbCo) break;
                this.autoStop(new StringBuilder().append("Auto nghiền bột: ").append(PauseMenu.autoBagFull() ? "hành trang đầy" : "đã hết nguyên liệu").append(", nghiền được ").append(this.autoFound).append(" món.").toString());
                return;
            }
            case 180: {
                ItemInInventory itemInInventory = PauseMenu.hklMon();
                if (itemInInventory == null) {
                    // vừa lên cấp: món cũ mất, món mới đang về
                    if (++this.autoWaitCount <= 100) break;
                    this.autoStop("Auto nâng cấp Hỏa kỳ lân: không thấy Hỏa kỳ lân trong hành trang, đã dừng.");
                    return;
                }
                if (itemInInventory.idTemplate >= HKL_CAP_TOI_DA) {
                    this.autoStop("Auto nâng cấp Hỏa kỳ lân: đã nâng cấp tối đa (cấp 3).");
                    return;
                }
                int n = PauseMenu.hklDa(HOA_THACH_C2);
                int n2 = 1;
                if (n < 1) {
                    n = PauseMenu.hklDa(HOA_THACH_C1);
                    n2 = 0;
                }
                if (n < 1) {
                    if (!this.autoHklMua) {
                        this.autoStop("Auto nâng cấp Hỏa kỳ lân: hết Hỏa thạch, đã dừng.");
                        return;
                    }
                    // Hỏa thạch chỉ mua được trong nhà
                    if (!Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                        this.autoAfterHouse = 183;
                        this.autoPhase = 20;
                        this.autoWaitCount = 0;
                        break;
                    }
                    this.autoPhase = 183;
                    break;
                }
                // gửi liền tối đa HKL_LO lần nâng; lên cấp giữa chừng thì món cũ biến mất nên các
                // lần sau server tự bỏ qua (Char.doNangCapHoaKyLan kiểm món còn trong túi), không tốn đá
                int n3 = Math.min(n, HKL_LO);
                this.autoHklKqTruoc = this.autoHklOk + this.autoHklHong;
                this.autoHklGui = n3;
                for (int i = 0; i < n3; ++i) {
                    GameService.gI().useItem(itemInInventory.itemID);
                    GameService.gI().doMenuOption(-513, (byte)0, 1);
                    GameService.gI().doMenuOption(-513, (byte)1, n2);
                }
                this.autoPhase = 181;
                this.autoWaitCount = 0;
                break;
            }
            case 181: {
                int n = this.autoHklOk + this.autoHklHong - this.autoHklKqTruoc;
                boolean bl = this.autoHklOk > 0 && this.autoTries < this.autoHklOk;
                if (n < this.autoHklGui && !bl && ++this.autoWaitCount <= 300) break;
                if (n == 0 && !bl) {
                    this.autoStop("Auto nâng cấp Hỏa kỳ lân: không có phản hồi (Hỏa kỳ lân có thời hạn không nâng được), đã dừng.");
                    return;
                }
                this.autoTries = this.autoHklOk;
                this.autoFound += n;
                this.autoPhase = 180;
                this.autoWaitCount = 0;
                this.autoWaitTicks = bl ? 8 : 1;
                break;
            }
            case 183: {
                // mua Hỏa thạch cấp 1 (đang ở trong nhà), tối đa 300 viên/gói
                if (!Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                    this.autoAfterHouse = 183;
                    this.autoPhase = 20;
                    break;
                }
                this.autoHklMuaTruoc = PauseMenu.hklDa(HOA_THACH_C1);
                Vector vector = new Vector();
                GemTemp gemTemp = new GemTemp();
                gemTemp.id = HOA_THACH_C1;
                gemTemp.number = (short)HKL_MUA_LO;
                vector.addElement(gemTemp);
                GameService.gI().buyGemItem(vector);
                this.autoPhase = 184;
                this.autoWaitCount = 0;
                break;
            }
            case 184: {
                int n = PauseMenu.hklDa(HOA_THACH_C1) - this.autoHklMuaTruoc;
                if (n <= 0) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto nâng cấp Hỏa kỳ lân: không mua được Hỏa thạch cấp 1 (cần 10 lượng/viên), đã dừng.");
                    return;
                }
                this.autoHklDaMua += n;
                this.autoPhase = 180;
                this.autoWaitCount = 0;
                break;
            }
            case 170: {
                // học skill pet: quyết định học tiếp hay xoá skill (Huyết Bồ Đề)
                Vector vector = PauseMenu.skDangCo();
                int n4 = this.skDanhGia(vector);
                if (n4 == 1) {
                    this.autoStop(new StringBuilder().append("Auto học skill pet: đã học được ").append(this.skMucTieu()).toString());
                    return;
                }
                int n = PauseMenu.skPhamChat();
                int n2 = Math.max(0, PauseMenu.petDocSo(PauseMenu.petInfo(), "Kỹ năng có thể học:"));
                int n3 = vector.size();
                // học tiếp khi: chưa có skill sai, phẩm chất >= mức chọn, còn slot trống; slot 1 cần
                // phẩm chất >= 1, slot 2 cần >= 2 (tỉ lệ 0% ở dưới). Học slot 2 mà pet chỉ có 1 slot
                // thì cũng phải xoá (random lại phẩm chất để có 2 slot).
                boolean bl = n4 == 0 && n >= this.autoSkPhamChat && n3 < n2 && (n3 == 0 ? n >= 1 : n >= 2) && (!this.autoSkSlot2 || n2 >= 2 && n >= 2);
                if (bl) {
                    if (PauseMenu.skSach() <= 0) {
                        this.autoStop(new StringBuilder().append("Auto học skill pet: hết Sách kỹ năng pet (đã dùng ").append(this.autoSkSach).append(").").toString());
                        return;
                    }
                    if (System.currentTimeMillis() < this.autoSkLuc) break;
                    this.autoSkKq = 0;
                    this.autoSkLuc = System.currentTimeMillis() + 300L;
                    GameService.gI().usePotion(POTION_SACH_SKILL);
                    ++this.autoSkSach;
                    this.autoPhase = 171;
                    this.autoWaitCount = 0;
                    break;
                }
                ItemInInventory itemInInventory = PauseMenu.skHbd();
                if (itemInInventory == null && this.autoSkMua) {
                    // hết Huyết Bồ Đề: mua 1 viên ở cửa hàng đặc biệt rồi dùng luôn
                    this.autoSkMuaTruoc = PauseMenu.skHbdCo();
                    GameService.gI().buyItemShopSpecial(SHOP_HUYET_BO_DE);
                    this.autoPhase = 173;
                    this.autoWaitCount = 0;
                    break;
                }
                if (itemInInventory == null) {
                    this.autoStop(new StringBuilder().append("Auto học skill pet: hết Huyết Bồ Đề để xoá skill / đổi phẩm chất (đã dùng ").append(this.autoSkHbd).append(").").toString());
                    return;
                }
                this.autoSkReset = false;
                GameService.gI().useItem(itemInInventory.itemID);
                ++this.autoSkHbd;
                this.autoPhase = 172;
                this.autoWaitCount = 0;
                break;
            }
            case 171: {
                if (this.autoSkKq == 0) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto học skill pet: dùng sách không có phản hồi, đã dừng.");
                    return;
                }
                ++this.autoTries;
                // chờ thông tin pet mới rồi mới quyết định tiếp
                this.autoPhase = 170;
                this.autoWaitTicks = 4;
                break;
            }
            case 173: {
                // chờ Huyết Bồ Đề vừa mua vào hành trang (không mua được thì server báo lý do và auto dừng)
                if (PauseMenu.skHbdCo() <= this.autoSkMuaTruoc) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto học skill pet: không mua được Huyết Bồ Đề (cần 10 lượng/viên và hành trang còn chỗ), đã dừng.");
                    return;
                }
                ++this.autoSkDaMua;
                this.autoPhase = 170;
                break;
            }
            case 172: {
                if (!this.autoSkReset) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto học skill pet: dùng Huyết Bồ Đề không có phản hồi (pet phải ở trạng thái 2 trở lên), đã dừng.");
                    return;
                }
                this.autoPhase = 170;
                this.autoWaitTicks = 4;
                break;
            }
            case 160: {
                // ăn bằng Tụ hồn đan: xin danh sách, Tụ hồn đan luôn là dòng cuối
                int n = PauseMenu.tuHonDanCo();
                if (n <= 0) {
                    this.autoStop(new StringBuilder().append("Auto nuôi pet: đã hết Tụ hồn đan (đã ăn ").append(this.autoPetDaAn).append(").").toString());
                    return;
                }
                int n2 = PauseMenu.petSucAn();
                int n3 = Math.min(Math.min(n, PET_LO_TU_HON), this.autoPetSauRelog ? 300 : 300 - Math.max(0, n2));
                if (n3 < 1) {
                    this.autoPhase = 150;
                    break;
                }
                this.autoPetLoCho = n3;
                this.autoPetTuTruoc = n;
                this.batDauDs();
                this.autoPetDay = false;
                this.autoPetCanNang = false;
                this.xinDsAn();
                this.autoPhase = 161;
                this.autoWaitCount = 0;
                break;
            }
            case 161: {
                if (!this.dsSan()) {
                    if (++this.autoWaitCount <= 400) break;
                    this.autoStop("Auto nuôi pet: NPC thợ săn không trả lời (phải đứng gần NPC thợ săn trong Thành), đã dừng.");
                    return;
                }
                int n = this.autoPetDsAn.length - 1;
                // dòng cuối phải đúng là Tụ hồn đan; số lệnh <= số viên đang có nên dòng cuối
                // không đổi suốt lượt (không chế đồ, hành trang không thay đổi)
                if (n < 0 || n > 127 || !this.autoPetDsAn[n].startsWith("Tụ hồn đan")) {
                    this.autoStop("Auto nuôi pet: không thấy Tụ hồn đan trong danh sách cho ăn, đã dừng.");
                    return;
                }
                this.petGuiAn(n * 256 + this.autoPetLoCho);
                this.autoPhase = 162;
                this.autoWaitCount = 0;
                break;
            }
            case 162: {
                int n = this.autoPetTuTruoc - PauseMenu.tuHonDanCo();
                boolean bl = this.autoPetDay || this.autoPetCanNang || this.autoPetBaoNang;
                if (n < this.autoPetLoCho && ++this.autoWaitCount <= (bl ? 40 : 400)) break;
                this.autoPetDaAn += Math.max(0, n);
                if (n > 0) {
                    this.autoPetSauRelog = false;
                }
                if (this.autoPetDay && n < this.autoPetLoCho) {
                    if (this.autoPetSauRelog) {
                        this.autoStop(new StringBuilder().append("Auto nuôi pet: vừa đăng nhập lại mà sức ăn vẫn đầy (").append(PauseMenu.petSucAn()).append("/300), đã dừng.").toString());
                        return;
                    }
                    this.autoPhase = 150;
                    break;
                }
                if (n <= 0 && !bl) {
                    this.autoStop("Auto nuôi pet: cho ăn Tụ hồn đan không có phản hồi, đã dừng.");
                    return;
                }
                this.autoPhase = 109;
                break;
            }
            case 130: {
                // nâng cấp: ưu tiên LKD thấp nhất đang có, hết LKD thì nâng không dùng LKD
                int n = 2;
                for (int i = 0; i < PET_LKD_ID.length; ++i) {
                    if (PauseMenu.autoGemCountLock(PET_LKD_ID[i], false) + PauseMenu.autoGemCountLock(PET_LKD_ID[i], true) < 1) continue;
                    n = PET_LKD_MUC[i];
                    break;
                }
                this.autoPetUpKq = 0;
                GameService.gI().doMenuOption(-16, (byte)0, n);
                this.autoPhase = 131;
                this.autoWaitCount = 0;
                break;
            }
            case 131: {
                if (this.autoPetUpKq == 0) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto nuôi pet: nâng cấp pet không có phản hồi (phải đứng gần NPC thợ săn trong Thành), đã dừng.");
                    return;
                }
                if (this.autoPetUpKq == 2) {
                    this.autoPhase = 130;
                    this.autoWaitTicks = 2;
                    break;
                }
                if (this.autoPetUpKq == 1) {
                    this.autoPetBaoNang = false;
                    this.autoPetCanNang = false;
                    ++this.autoPetDaNang;
                    this.autoPetUpKq = 3;
                    this.autoWaitCount = 0;
                }
                // chờ thông tin pet mới (đã qua 99.9%) rồi mới chế/cho ăn tiếp
                if (PauseMenu.petCanNang() && ++this.autoWaitCount <= 260) break;
                this.autoPhase = 109;
                this.autoWaitTicks = 2;
                break;
            }
            case 140: {
                // điều phối đường lên Thành
                if (!PauseMenu.oTrongGame()) {
                    break;
                }
                if (PauseMenu.oMap(MAP_THANH)) {
                    // NPC thợ săn ở ô (25,10), server nhận trong bán kính 5 ô
                    this.datDich(22, 7, 28, 13);
                    this.autoPhase = 145;
                    break;
                }
                if (PauseMenu.oMap(MAP_CONG_THANH)) {
                    // đích = ô trống sát cổng lên Thành, lấy từ dữ liệu bản đồ của client
                    boolean[] blArray = PauseMenu.dichSatCong(MAP_THANH);
                    if (blArray == null) {
                        this.autoStop("Auto nuôi pet: map này không thấy cổng lên Thành, đã dừng.");
                        return;
                    }
                    this.datDich(blArray);
                    this.autoPhase = 143;
                    break;
                }
                XaphuTemplate xaphuTemplate = Res.getXaphu();
                int n = -1;
                int n2 = -1;
                for (int i = 0; xaphuTemplate != null && i < XE_NGUA_DICH.length && n < 0; ++i) {
                    for (int j = 0; j < xaphuTemplate.mapID.length; ++j) {
                        if (xaphuTemplate.mapID[j] != XE_NGUA_DICH[i]) continue;
                        n = j;
                        n2 = XE_NGUA_DICH[i];
                        break;
                    }
                }
                if (n < 0) {
                    this.autoStop("Auto nuôi pet: map này không có xe ngựa đi lên Thành (hãy về Làng), đã dừng.");
                    return;
                }
                if ((long)xaphuTemplate.price[n] > Canvas.gameScr.mainChar.charXu) {
                    this.autoStop("Auto nuôi pet: không đủ xu đi xe ngựa, đã dừng.");
                    return;
                }
                short s = Canvas.gameScr.getIndexArrMap(n2);
                if (s < 0 || Res.localXaphu == null || s >= Res.localXaphu.length || Res.localXaphu[s] == null) {
                    this.autoStop("Auto nuôi pet: không đọc được điểm đến của xe ngựa, đã dừng.");
                    return;
                }
                this.autoLenMap = Canvas.gameScr.map;
                GameService.gI().doMove2Map((byte)n, Res.localXaphu[s][0]);
                ChangScr.gI().switchToMe();
                ChangScr.gI().setMap(Canvas.gameScr.map, 0, 0);
                Res.removeMonsterTemplet();
                this.autoPhase = 141;
                this.autoWaitCount = 0;
                break;
            }
            case 141: {
                if (PauseMenu.oTrongGame() && Canvas.gameScr.map != this.autoLenMap) {
                    this.autoPhase = 140;
                    this.autoWaitTicks = 15;
                    break;
                }
                if (++this.autoWaitCount <= 400) break;
                this.autoStop("Auto nuôi pet: đi xe ngựa không được, đã dừng.");
                return;
            }
            case 143: {
                int n = this.diBo();
                if (n < 0) {
                    this.autoStop(new StringBuilder().append("Auto nuôi pet: không tìm được đường tới cổng lên Thành (đang ở ô ").append(Canvas.gameScr.mainChar.x / 16).append(",").append(Canvas.gameScr.mainChar.y / 16).append(" map ").append(Canvas.gameScr.map).append("), đã dừng.").toString());
                    return;
                }
                if (n == 0) break;
                CMLocation cMLocation = PauseMenu.congToi(MAP_THANH);
                if (cMLocation == null) {
                    this.autoStop("Auto nuôi pet: map này không thấy cổng lên Thành, đã dừng.");
                    return;
                }
                this.autoLenMap = Canvas.gameScr.map;
                PauseMenu.quaCong(cMLocation, MAP_THANH);
                this.autoPhase = 144;
                this.autoWaitCount = 0;
                break;
            }
            case 144: {
                if (PauseMenu.oTrongGame() && PauseMenu.oMap(MAP_THANH)) {
                    this.autoPhase = 140;
                    this.autoWaitTicks = 15;
                    break;
                }
                if (++this.autoWaitCount <= 400) break;
                this.autoStop("Auto nuôi pet: không vào được Thành (đang công thành?), đã dừng.");
                return;
            }
            case 145: {
                int n = this.diBo();
                if (n < 0) {
                    this.autoStop(new StringBuilder().append("Auto nuôi pet: không tìm được đường tới NPC thợ săn (đang ở ô ").append(Canvas.gameScr.mainChar.x / 16).append(",").append(Canvas.gameScr.mainChar.y / 16).append("), đã dừng.").toString());
                    return;
                }
                if (n == 0) break;
                this.autoPhase = 109;
                this.autoWaitTicks = 4;
                break;
            }
            case 150: {
                this.batDauRelog();
                break;
            }
            case 151: {
                // chạy lại khi đã vào game (luồng relog xong); lỗi thì dừng
                if (this.autoRelogLoi != null) {
                    this.autoStop(new StringBuilder().append("Auto nuôi pet: ").append(this.autoRelogLoi).append(", đã dừng.").toString());
                    return;
                }
                if (!PauseMenu.oTrongGame()) break;
                this.autoPetSauRelog = true;
                this.autoPhase = 140;
                this.autoWaitTicks = 30;
                break;
            }
            case 97: {
                ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
                if (itemInInventory == null || this.ktDat(itemInInventory)) {
                    if (this.autoKtFull) {
                        if (this.autoKtNext()) break;
                        this.autoStop(this.ktTongKet());
                        return;
                    }
                    if (itemInInventory == null) {
                        this.autoStop("Auto khóa đồ thú: không thấy món đồ trong hành trang, đã dừng.");
                    } else {
                        this.autoStop(new StringBuilder().append("Auto khóa đồ thú: đã khóa được ").append(PauseMenu.ktGiaTri(itemInInventory)).append("%").toString());
                    }
                    return;
                }
                // hết loại xương đang dùng (thường/khóa) thì chuyển sang loại còn lại cùng id
                if (PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock) < 1) {
                    this.autoKtLock = !this.autoKtLock;
                    if (PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock) < 1) {
                        this.autoStop(this.autoKtFull ? new StringBuilder().append(this.ktTongKet()).append(" - hết xương cấp 6").toString() : new StringBuilder().append("Auto khóa đồ thú: hết xương cấp 6 sau ").append(this.autoTries).append(" lần, đã dừng.").toString());
                        return;
                    }
                }
                this.autoOkSeen = false;
                this.autoCttBoQua = false;
                this.autoKtDaTruoc = PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock);
                GameService.gI().doRequestTachNguyenLIeu(this.autoKtItem, 2, this.autoKtDa, this.autoKtLock ? 1 : 0);
                this.autoPhase = 98;
                this.autoWaitCount = 0;
                break;
            }
            case 98: {
                if (this.autoCttBoQua) {
                    if (this.autoKtNext()) break;
                    this.autoStop(this.ktTongKet());
                    return;
                }
                if (!this.autoOkSeen) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto khóa đồ thú: server không phản hồi (đồ thú phải Hoàn mỹ Nhất phẩm và đứng trong nhà Hắc Ngưu), đã dừng.");
                    return;
                }
                // "Đã khóa thành công" tới TRƯỚC chỉ số mới. Server gửi tiếp chỉ số món đồ rồi mới
                // gửi lại túi nguyên liệu, nên thấy xương giảm là chỉ số đã về (client sửa chỉ số
                // ngay trên object cũ nên không dùng so sánh object được - chờ hết 60 tick mỗi lần)
                if (PauseMenu.autoGemCountLock(this.autoKtDa, this.autoKtLock) >= this.autoKtDaTruoc && ++this.autoWaitCount < 60) break;
                ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
                ++this.autoTries;
                if (this.ktDat(itemInInventory)) {
                    ++this.autoFound;
                    if (this.autoKtFull) {
                        if (this.autoKtNext()) {
                            this.autoWaitTicks = 1;
                            break;
                        }
                        this.autoStop(this.ktTongKet());
                        return;
                    }
                    this.autoStop(new StringBuilder().append("Auto khóa đồ thú: đã khóa được ").append(PauseMenu.ktGiaTri(itemInInventory)).append("%").toString());
                    return;
                }
                this.autoPhase = 97;
                this.autoWaitTicks = 1;
                break;
            }
            case 95: {
                ItemInInventory itemInInventory = this.autoFindItem(this.autoCttItem);
                if (itemInInventory == null || this.autoCttFull && this.cttDatSan(itemInInventory)) {
                    if (this.autoCttFull) {
                        if (this.autoCttNext()) break;
                        this.autoStop(new StringBuilder().append(this.cttTongKet()).append(". Đã hết đồ.").toString());
                        return;
                    }
                    this.autoStop("Auto cộng thuộc tính: không thấy món đồ trong hành trang, đã dừng.");
                    return;
                }
                // hết loại đá đang dùng (thường/khóa) thì chuyển sang loại còn lại cùng id
                if (PauseMenu.autoGemCountLock(this.autoCttDa, this.autoCttLock) < 1) {
                    this.autoCttLock = !this.autoCttLock;
                    if (PauseMenu.autoGemCountLock(this.autoCttDa, this.autoCttLock) < 1) {
                        this.autoStop(this.autoCttFull ? new StringBuilder().append(this.cttTongKet()).append(". Hết đá thuộc tính, đã dừng.").toString() : new StringBuilder().append("Auto cộng thuộc tính: hết đá thuộc tính sau ").append(this.autoTries).append(" lần").append(this.autoCttVuaRa == null ? "" : new StringBuilder().append(" (lần cuối ra ").append(this.autoCttVuaRa).append(")").toString()).append(", đã dừng.").toString());
                        return;
                    }
                }
                this.autoCttOk = false;
                this.autoCttBoQua = false;
                this.autoCttVuaRa = null;
                GameService.gI().doRequestTachNguyenLIeu(this.autoCttItem, 1, this.autoCttDa, this.autoCttLock ? 1 : 0);
                this.autoPhase = 96;
                this.autoWaitCount = 0;
                break;
            }
            case 96: {
                if (this.autoCttBoQua) {
                    // server không cho cộng món này (đồ thời trang...) -> bỏ qua, sang món khác
                    if (this.autoCttNext()) break;
                    this.autoStop(new StringBuilder().append(this.cttTongKet()).append(". Đã hết đồ.").toString());
                    return;
                }
                if (!this.autoCttOk) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto cộng thuộc tính: server không phản hồi (đồ phải màu xanh lá và đứng trong nhà Hắc Ngưu), đã dừng.");
                    return;
                }
                ++this.autoTries;
                if (this.cttDat(this.autoCttVuaRa)) {
                    ++this.autoFound;
                    if (this.autoCttFull) {
                        if (this.autoCttNext()) {
                            this.autoWaitTicks = 1;
                            break;
                        }
                        this.autoStop(new StringBuilder().append(this.cttTongKet()).append(". Tất cả đồ đã đạt!").toString());
                        return;
                    }
                    this.autoStop(new StringBuilder().append("Auto cộng thuộc tính: đã ra ").append(this.autoCttVuaRa).toString());
                    return;
                }
                this.autoPhase = 95;
                this.autoWaitTicks = 1;
                break;
            }
            case 90: {
                ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
                if (itemInInventory == null) {
                    if (this.autoThemFull && this.autoThemNext()) {
                        break;
                    }
                    this.autoStop(this.autoThemFull ? "Auto thêm dòng full: đã xong tất cả đồ." : "Auto thêm dòng: không thấy món đồ trong hành trang, đã dừng.");
                    return;
                }
                this.autoThemBotLock = this.autoThemStock(249, true) >= 1 ? 1 : 0;
                if (this.autoThemStock(249, this.autoThemBotLock == 1) < 1) {
                    this.autoStop("Auto thêm dòng: hết bột xanh lá (bột không bán ở shop), đã dừng.");
                    return;
                }
                this.autoThemDaLock = this.autoThemStock(250, true) >= 1 ? 1 : 0;
                if (this.autoThemStock(250, this.autoThemDaLock == 1) < 1) {
                    if (this.autoThemBuy && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                        Vector vector = new Vector();
                        GemTemp gemTemp = new GemTemp();
                        gemTemp.id = (short)250;
                        gemTemp.number = (short)20;
                        vector.addElement(gemTemp);
                        GameService.gI().buyGemItem(vector);
                        this.autoPhase = 92;
                        this.autoWaitCount = 0;
                        break;
                    }
                    this.autoStop("Auto thêm dòng: hết đá tiến giai, đã dừng.");
                    return;
                }
                this.autoThemOk = false;
                GameService.gI().doRequestThemThuocTinh(this.autoThemItem, 5, 249, this.autoThemBotLock, 250, this.autoThemDaLock);
                this.autoPhase = 91;
                this.autoWaitCount = 0;
                break;
            }
            case 91: {
                if (!this.autoThemOk) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto thêm dòng: server không phản hồi (đồ phải Hoàn mỹ Nhất phẩm, đã khóa, lv50 trở lên, và đứng trong nhà Hắc Ngưu), đã dừng.");
                    return;
                }
                ++this.autoTries;
                ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
                if (itemInInventory != null) {
                    boolean[] blArray = new boolean[2];
                    int n = this.autoThemLines(itemInInventory, blArray);
                    if (blArray[0]) {
                        ++this.autoFound;
                        if (this.autoThemFull) {
                            if (this.autoThemNext()) {
                                this.autoWaitTicks = 1;
                                break;
                            }
                            this.autoStop("Auto thêm dòng full: tất cả đồ đã ra dòng như ý!");
                            return;
                        }
                        this.autoStop(new StringBuilder().append("Auto thêm dòng: đã ra dòng như ý sau ").append(this.autoTries).append(" lần!").toString());
                        return;
                    }
                }
                this.autoPhase = 90;
                this.autoWaitTicks = 1;
                break;
            }
            case 92: {
                int n = this.autoThemStock(250, false) + this.autoThemStock(250, true);
                if (n < 1 && ++this.autoWaitCount <= 100) break;
                if (n < 1) {
                    this.autoStop("Auto thêm dòng: không mua được đá tiến giai (10 lượng/viên, có thể hết lượng), đã dừng.");
                    return;
                }
                this.autoPhase = 90;
                break;
            }
            case 80: {
                if (this.autoTargetPos >= this.autoTargets.length) {
                    this.autoStop("Auto luyện đồ: đã đập xong tất cả đồ.");
                    return;
                }
                ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
                if (itemInInventory == null || itemInInventory.plusTemplate >= this.autoLuyenTarget) {
                    if (itemInInventory != null) {
                        ++this.autoFound;
                    }
                    ++this.autoTargetPos;
                    break;
                }
                if (Canvas.gameScr.mainChar.charXu < 5000L) {
                    this.autoStop("Auto luyện đồ: hết xu (cần 5000 xu mỗi lần đập), đã dừng.");
                    return;
                }
                if (this.autoLuyenBuy && this.autoLuyenBuyCd <= 0 && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map) && this.autoLuyenLow() && !this.autoLuyenShort()) {
                    this.autoLuyenBuyNow();
                    this.autoLuyenBuyCd = 75;
                }
                if (this.autoLuyenShort()) {
                    if (this.autoLuyenBuy && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                        this.autoPhase = 82;
                        break;
                    }
                    if (PauseMenu.autoGemCountLock(this.autoLkd, this.autoLkdLock) < 1) {
                        this.autoStop(this.autoLuyenBuy ? "Auto luyện đồ: hết luyện kim dược (tự mua chỉ làm được trong nhà Hắc Ngưu), đã dừng." : "Auto luyện đồ: hết luyện kim dược, đã dừng.");
                        return;
                    }
                }
                Vector vector = new Vector();
                Vector vector2 = new Vector();
                vector.addElement(String.valueOf(this.autoLkd));
                vector2.addElement(this.autoLkdLock ? "1" : "0");
                vector.addElement(String.valueOf(this.autoBh));
                vector2.addElement(this.autoBh >= 0 && this.autoBhLock ? "1" : "0");
                vector.addElement(String.valueOf(this.autoMm));
                vector2.addElement(this.autoMm >= 0 && this.autoMmLock ? "1" : "0");
                ScreenDapDo.itemSelect = itemInInventory;
                ScreenDapDo.itemID = itemInInventory.itemID;
                ScreenDapDo.isFinishImbue = false;
                this.autoLuyenIdle = 0;
                this.autoLuyenLastPlus = itemInInventory.plusTemplate;
                GameService.gI().doAutoImbue(itemInInventory.itemID, vector, this.autoLuyenTarget, vector2, -1);
                ++this.autoTries;
                this.autoPhase = 81;
                break;
            }
            case 81: {
                if (!ScreenDapDo.isFinishImbue && this.autoLuyenBuy && --this.autoLuyenBuyCd <= 0 && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map) && this.autoLuyenLow()) {
                    this.autoLuyenBuyNow();
                    this.autoLuyenBuyCd = 75;
                }
                if (!ScreenDapDo.isFinishImbue) {
                    ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
                    int n = itemInInventory == null ? -1 : itemInInventory.plusTemplate;
                    int n2 = PauseMenu.autoGemCountLock(this.autoLkd, false) + PauseMenu.autoGemCountLock(this.autoLkd, true);
                    if (n != this.autoLuyenLastPlus || n2 != this.autoLuyenLastLkd) {
                        this.autoLuyenLastPlus = n;
                        this.autoLuyenLastLkd = n2;
                        this.autoLuyenIdle = 0;
                    }
                    if (++this.autoLuyenIdle <= 3000) break;
                    this.autoStop(new StringBuilder().append("Auto luyện đồ: server không phản hồi").append(this.autoLastAlert == null ? "" : new StringBuilder().append(" (").append(this.autoLastAlert).append(")").toString()).append(", đã dừng.").toString());
                    return;
                }
                this.autoPhase = 83;
                this.autoWaitTicks = 5;
                break;
            }
            case 83: {
                ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
                String string = ScreenDapDo.gI().infoPoup;
                if (itemInInventory != null && itemInInventory.plusTemplate >= this.autoLuyenTarget) {
                    ++this.autoFound;
                    ++this.autoTargetPos;
                    this.autoPhase = 80;
                    break;
                }
                if (string != null && string.indexOf("hết nguyên liệu") >= 0) {
                    if (Canvas.gameScr.mainChar.charXu < 5000L) {
                        this.autoStop("Auto luyện đồ: hết xu (cần 5000 xu mỗi lần đập), đã dừng.");
                        return;
                    }
                    if (this.autoLuyenBuy && Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                        this.autoPhase = 82;
                        break;
                    }
                    this.autoStop("Auto luyện đồ: hết nguyên liệu, đã dừng.");
                    return;
                }
                ++this.autoTargetPos;
                this.autoPhase = 80;
                break;
            }
            case 82: {
                boolean bl5 = this.autoLuyenBuyNow();
                if (!bl5 && PauseMenu.autoGemCountLock(this.autoLkd, false) < 1 && PauseMenu.autoGemCountLock(this.autoLkd, true) < 1) {
                    this.autoStop("Auto luyện đồ: loại luyện kim dược đang dùng không bán ở shop, đã dừng.");
                    return;
                }
                this.autoPhase = 84;
                this.autoWaitCount = 0;
                break;
            }
            case 84: {
                boolean bl6 = PauseMenu.autoGemCountLock(this.autoLkd, false) >= 1;
                if (!bl6 && ++this.autoWaitCount <= 100) break;
                if (!bl6 && PauseMenu.autoGemCountLock(this.autoLkd, true) < 1) {
                    if (this.autoLkd > 10) {
                        this.autoStop("Auto luyện đồ: loại luyện kim dược đang dùng không bán ở shop và đã hết, đã dừng.");
                    } else if (PauseMenu.autoBagFull()) {
                        this.autoStop("Auto luyện đồ: hành trang đầy nên không mua lại được luyện kim dược (cần 1 ô trống), đã dừng.");
                    } else if (Canvas.gameScr.mainChar.charXu < 100000L) {
                        this.autoStop("Auto luyện đồ: không đủ xu để mua luyện kim dược, đã dừng.");
                    } else {
                        this.autoStop(new StringBuilder().append("Auto luyện đồ: không mua được luyện kim dược").append(this.autoLastAlert == null ? "" : new StringBuilder().append(" (").append(this.autoLastAlert).append(")").toString()).append(", đã dừng.").toString());
                    }
                    return;
                }
                if (this.autoLkdLock && PauseMenu.autoGemCountLock(this.autoLkd, true) < 1) {
                    this.autoLkdLock = false;
                }
                if (this.autoBh >= 0 && this.autoBhLock && PauseMenu.autoGemCountLock(this.autoBh, true) < 1) {
                    this.autoBhLock = false;
                }
                if (this.autoMm >= 0 && this.autoMmLock && PauseMenu.autoGemCountLock(this.autoMm, true) < (this.autoBh >= 0 ? 6 : 7)) {
                    this.autoMmLock = false;
                }
                this.autoPhase = 80;
                this.autoWaitTicks = 2;
                break;
            }
            case 70: {
                ItemInInventory itemInInventory = this.autoNewItem();
                if (itemInInventory == null) {
                    if (++this.autoWaitCount <= 250) break;
                    this.autoStop("Auto thần binh: không nhận được vũ khí vừa chế, đã dừng.");
                    return;
                }
                if (itemInInventory.colorName != 1 || itemInInventory.hangItem < 1 || itemInInventory.hangItem > 3) {
                    this.autoStop("Auto thần binh: vũ khí chế ra không phải Hoàn mỹ Nhất-Tam phẩm, đã dừng.");
                    return;
                }
                this.autoTbItem = itemInInventory.itemID;
                this.autoShopRef = ShopNew.gI().ItemShopNew;
                GameService.gI().buyItemShopnew((byte)0, (short)7);
                this.autoPhase = 71;
                this.autoWaitCount = 0;
                break;
            }
            case 71: {
                Vector vector = ShopNew.gI().ItemShopNew;
                if (vector == null || vector == this.autoShopRef || ShopNew.gI().typeshop != 6) {
                    if (++this.autoWaitCount <= 200) break;
                    this.autoStop("Auto thần binh: không mở được danh sách nguyên liệu tinh luyện, đã dừng.");
                    return;
                }
                int n = -1;
                for (int i = 0; i < vector.size(); ++i) {
                    if (((ItemInInventory)vector.elementAt(i)).itemID != this.autoTbItem) continue;
                    n = i;
                    break;
                }
                if (n < 0) {
                    this.autoStop("Auto thần binh: vũ khí vừa chế không có trong danh sách nguyên liệu (thần binh có thể đã 99%, hoặc chọn sai cấp thần binh), đã dừng.");
                    return;
                }
                this.autoTbResult = 0;
                GameService.gI().doCustomPopup(n, (byte)124, "", 1);
                this.autoPhase = 72;
                this.autoWaitCount = 0;
                break;
            }
            case 72: {
                if (this.autoTbResult == 0) {
                    if (++this.autoWaitCount <= 250) break;
                    this.autoStop("Auto thần binh: không nhận được kết quả tinh luyện, đã dừng.");
                    return;
                }
                if (Canvas.currentScreen == ShopNew.gI() && ShopNew.gI().right != null) {
                    ShopNew.gI().right.action.perform();
                }
                ++this.autoTries;
                if (this.autoTbResult == 1) {
                    ++this.autoFound;
                    if (this.autoTbPct >= 99) {
                        this.autoStop("Auto thần binh: đã tinh luyện lên 99%, dừng lại (bước cuối lên 100% hãy tự làm bằng vũ khí cùng cấp).");
                        return;
                    }
                }
                this.autoPhase = 0;
                this.autoWaitTicks = 1;
                break;
            }
            case 60: {
                if (this.autoFound >= this.autoTkQty) {
                    this.autoStop("Auto chế thần khí đã hoàn tất.");
                    return;
                }
                if (this.autoTkGrade == 5 && (this.autoTkStones <= 0 || this.autoTkNeedStone)) {
                    this.autoAfterHouse = 62;
                    this.autoPhase = 20;
                    break;
                }
                this.autoSnapshot = this.autoInventoryIds();
                this.autoTkNoMat = false;
                this.autoTkNeedStone = false;
                GameService.gI().doMenuOption(-517, (byte)0, 0);
                GameService.gI().doMenuOption(-517, (byte)50, this.autoTkType);
                GameService.gI().doMenuOption(-517, (byte)55, 2);
                GameService.gI().doMenuOption(-517, (byte)51, this.autoTkGrade);
                GameService.gI().doCustomPopup(Canvas.gameScr.mainChar.ID, (byte)121, "", 1);
                this.autoPhase = 61;
                this.autoWaitCount = 0;
                break;
            }
            case 62: {
                int n = Math.max(1, Math.min(10, (this.autoTkQty - this.autoFound) * (this.autoTkFilter == 0 ? 5 : 1)));
                Vector vector = new Vector();
                GemTemp gemTemp = new GemTemp();
                gemTemp.id = (short)154;
                gemTemp.number = (short)n;
                vector.addElement(gemTemp);
                this.autoTkNeedStone = false;
                this.autoTkStones = 0;
                Vector[] vectorArray = new Vector[]{MainChar.gemItem, MainChar.gemItemKhoa};
                for (int i = 0; i < 2; ++i) {
                    for (int j = 0; vectorArray[i] != null && j < vectorArray[i].size(); ++j) {
                        GemTemplate gemTemplate = (GemTemplate)vectorArray[i].elementAt(j);
                        if (gemTemplate.id != 154) continue;
                        gemTemplate.number = 0;
                    }
                }
                GameService.gI().buyGemItem(vector);
                this.autoPhase = 63;
                this.autoWaitCount = 0;
                break;
            }
            case 63: {
                int n = PauseMenu.autoGemCount(154);
                if (n <= 0 && ++this.autoWaitCount <= 100) break;
                if (n <= 0) {
                    this.autoStop("Auto chế thần khí: không mua được Đá ngũ hợp tinh khiết cấp 6 (có thể hết lượng hoặc hành trang đầy), đã dừng.");
                    return;
                }
                this.autoTkStones = n;
                this.autoAfterHouse = 60;
                this.autoPhase = 60;
                break;
            }
            case 61: {
                if (this.autoTkNeedStone) {
                    Canvas.endDlg();
                    this.autoTkStones = 0;
                    this.autoPhase = 60;
                    this.autoWaitTicks = 1;
                    break;
                }
                if (this.autoTkNoMat) {
                    Canvas.endDlg();
                    if (this.autoTkGrade > (this.autoTkFilter == 0 ? 4 : 0)) {
                        --this.autoTkGrade;
                        this.autoPhase = 60;
                        this.autoWaitTicks = 2;
                        break;
                    }
                    this.autoStop(this.autoTkFilter == 0 ? "Auto chế thần khí: hết nguyên liệu cấp 5-6 (cấp thấp hơn không ra được 9 dòng), đã dừng." : "Auto chế thần khí: hết nguyên liệu, đã dừng.");
                    return;
                }
                ItemInInventory itemInInventory = this.autoNewItem();
                if (itemInInventory != null && itemInInventory.idTemplate >= 890 && itemInInventory.idTemplate <= 892) {
                    Canvas.endDlg();
                    ++this.autoTries;
                    if (this.autoTkGrade == 5) {
                        --this.autoTkStones;
                    }
                    if (this.autoTkFilter != 0 || PauseMenu.autoAttVal(itemInInventory, 168) >= 9) {
                        ++this.autoFound;
                    } else if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                        this.autoSellQuiet(itemInInventory.itemID);
                    }
                    this.autoPhase = 60;
                    this.autoWaitTicks = 1;
                    break;
                }
                if (++this.autoWaitCount <= 250) break;
                Canvas.endDlg();
                this.autoStop("Auto chế thần khí: không nhận được kết quả từ server, đã dừng.");
                return;
            }
            case 30: {
                if (this.autoPetCurSlot >= this.autoPetEndSlot) {
                    this.autoStop("Auto đồ thú đã hoàn tất.");
                    return;
                }
                Vector vector = this.autoPetByColor(1);
                for (int i = 0; i < vector.size(); ++i) {
                    ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
                    this.autoPetOwned.removeElement(new Integer(itemInInventory.itemID));
                    for (int j = this.autoPetOwned.size() - 1; j >= 0; --j) {
                        if (((Integer)this.autoPetOwned.elementAt(j)).intValue() != itemInInventory.itemID) continue;
                        this.autoPetOwned.removeElementAt(j);
                    }
                    if (this.autoPetOk(itemInInventory) && this.autoPetCenterOk(itemInInventory, 2)) {
                        ++this.autoFound;
                        if (this.autoPetMode == 2 && this.autoPetBaseHe < 0 && itemInInventory.heItem >= 0) {
                            this.autoPetBaseHe = (itemInInventory.heItem - this.autoPetCurSlot + 5) % 5;
                        }
                        ++this.autoPetCurSlot;
                        continue;
                    }
                    if (this.autoPetOk(itemInInventory)) continue;
                    this.autoPetSell.addElement(new Integer(itemInInventory.itemID));
                }
                if (this.autoPetCurSlot >= this.autoPetEndSlot) {
                    break;
                }
                int n5 = this.autoPetByColor(2).size();
                boolean bl4 = this.autoPetHas(2, 2);
                int n = n5 >= 5 && bl4 ? 2 : (this.autoPetByColor(3).size() >= 5 && (this.autoPetHas(3, 1) || bl4 || n5 < 4) ? 1 : (this.autoPetByColor(0).size() >= 5 ? 0 : -1));
                if (n < 0 || PauseMenu.autoGemCount(PET_STONE[n]) < this.autoPetN() || this.autoPetSell.size() > 0) {
                    this.autoPhase = Canvas.gameScr.isOffLineMap(Canvas.gameScr.map) ? 41 : 20;
                    break;
                }
                if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                    this.autoPetExitHouse();
                    return;
                }
                this.autoPetStep = n;
                int n6 = n == 2 ? 1 : (n == 1 ? Math.max(1, 5 - n5) : 8);
                n6 = Math.min(Math.min(n6, 8), PauseMenu.autoGemCount(PET_STONE[n]) / this.autoPetN());
                Vector vector2 = this.autoPetBuildGroups(n, n6);
                if (vector2.size() == 0) {
                    this.autoPhase = 20;
                    break;
                }
                byte[] byArray = new byte[6];
                byArray[n == 2 ? 5 : 0] = (byte)this.autoPetN();
                for (int i = 0; i < vector2.size(); ++i) {
                    GameService.gI().doMenuOption(-8, (byte)0, 2);
                    GameService.gI().doMenuOption(-8, (byte)7, (this.autoPetLevel - 30) / 5);
                    GameService.gI().doMenuOption(-8, (byte)8, this.heMaPhap ? 0 : 1);
                    GameService.gI().doMenuOption(-8, (byte)9, n);
                    GameService.gI().doMenuOption(-8, (byte)10, 2);
                    GameService.gI().epDothu((Vector)vector2.elementAt(i), byArray);
                }
                this.autoPetGroups = vector2.size();
                this.autoTries += this.autoPetGroups;
                this.autoOkCount = 0;
                this.autoOkSeen = false;
                this.autoPhase = 34;
                this.autoWaitCount = 0;
                break;
            }
            case 32: {
                if (WindowInfoScr.gI().name != null) {
                    WindowInfoScr.gI().name = new StringBuilder().append("[Auto] Hợp đồ thú ").append(new String[]{"xanh", "đỏ", "hoàn mỹ"}[this.autoPetStep]).append(" lv").append(this.autoPetLevel).toString();
                    this.autoPhase = 33;
                    this.autoWaitTicks = 1;
                    break;
                }
                if (++this.autoWaitCount <= 200) break;
                this.autoStop("Auto đồ thú: không mở được hợp đồ thú (phải đứng ở làng và hành trang còn chỗ trống), đã dừng.");
                return;
            }
            case 33: {
                if (Canvas.currentScreen == WindowInfoScr.gI()) {
                    WindowInfoScr.gI().right.action.perform();
                }
                this.autoPetEp = this.autoPetPickEp(this.autoPetStep);
                if (this.autoPetEp == null) {
                    this.autoPhase = 30;
                    break;
                }
                byte[] byArray = new byte[6];
                byArray[this.autoPetStep == 2 ? 5 : 0] = (byte)this.autoPetN();
                this.autoRefObj = (ItemInInventory)this.autoPetEp.elementAt(0);
                this.autoOkSeen = false;
                GameService.gI().epDothu(this.autoPetEp, byArray);
                ++this.autoTries;
                this.autoPhase = 34;
                this.autoWaitCount = 0;
                break;
            }
            case 34: {
                if (this.autoOkCount < this.autoPetGroups && ++this.autoWaitCount <= 250) break;
                if (Canvas.currentScreen == WindowInfoScr.gI()) {
                    WindowInfoScr.gI().right.action.perform();
                }
                if (this.autoOkCount == 0) {
                    this.autoStop("Auto đồ thú: không hợp được đồ (phải đứng ở làng và hành trang còn chỗ trống), đã dừng.");
                    return;
                }
                this.autoPhase = 30;
                this.autoWaitTicks = 1;
                break;
            }
            case 41: {
                for (int i = 0; i < this.autoPetSell.size(); ++i) {
                    short s = (short)((Integer)this.autoPetSell.elementAt(i)).intValue();
                    if (this.autoFindItem(s) == null) continue;
                    this.autoSellQuiet(s);
                }
                this.autoPetSell.removeAllElements();
                int n = this.autoPetN();
                int[] nArray2 = this.autoPetNeeds();
                int n2 = nArray2[0];
                int n3 = nArray2[1];
                int n4 = nArray2[2];
                int[] nArray = new int[]{n * n3, n * n2, n};
                Vector vector = new Vector();
                for (int i = 0; i < 3; ++i) {
                    int n5 = nArray[i] - PauseMenu.autoGemCount(PET_STONE[i]);
                    if (n5 <= 0) continue;
                    GemTemp gemTemp = new GemTemp();
                    gemTemp.id = (short)PET_STONE[i];
                    gemTemp.number = (short)Math.min(n5, 300);
                    vector.addElement(gemTemp);
                }
                if (vector.size() > 0) {
                    GameService.gI().buyGemItem(vector);
                }
                int n7 = 0;
                for (int i = 0; i < vector.size(); ++i) {
                    if (PauseMenu.autoHasGemType(((GemTemp)vector.elementAt(i)).id)) continue;
                    ++n7;
                }
                int n6 = 42 * Canvas.gameScr.mainChar.limItemBag - Canvas.gameScr.mainChar.FullInventory() - (MainChar.gemItemKhoa == null ? 0 : MainChar.gemItemKhoa.size()) - n7 - 1;
                this.autoPetBuyW = Math.min(n4, Math.min(n6, 120));
                if (n4 > 0 && this.autoPetBuyW <= 0) {
                    this.autoStop("Auto đồ thú: hành trang đầy, không mua thêm được đồ thú, đã dừng.");
                    return;
                }
                this.autoSnapshot = this.autoInventoryIds();
                if (this.autoPetBuyW > 0) {
                    GameService.gI().doMenuOption(2, (byte)0, 1);
                    GameService.gI().doMenuOption(2, (byte)1, this.heMaPhap ? 0 : 1);
                }
                this.autoPhase = 42;
                break;
            }
            case 42: {
                if (this.autoPetBuyW > 0) {
                    Vector vector = new Vector();
                    for (int i = 0; i < this.autoPetBuyW; ++i) {
                        ItemInInventory itemInInventory = new ItemInInventory();
                        itemInInventory.catagory = (byte)3;
                        itemInInventory.charClazz = Canvas.gameScr.mainChar.clazz;
                        itemInInventory.idTemplate = (short)this.autoPetTemplate();
                        vector.addElement(itemInInventory);
                    }
                    this.autoInvRef = Char.inventory;
                    GameService.gI().buyItem(vector);
                }
                this.autoPhase = 43;
                this.autoWaitCount = 0;
                break;
            }
            case 43: {
                int n = 0;
                Vector vector = Char.inventory;
                for (int i = 0; vector != null && i < vector.size(); ++i) {
                    ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
                    boolean bl2 = false;
                    for (int j = 0; j < this.autoSnapshot.length; ++j) {
                        if (this.autoSnapshot[j] != itemInInventory.itemID) continue;
                        bl2 = true;
                        break;
                    }
                    if (bl2 || itemInInventory.idTemplate != this.autoPetTemplate()) continue;
                    ++n;
                }
                if (n < this.autoPetBuyW && Char.inventory == this.autoInvRef && ++this.autoWaitCount <= 150) break;
                for (int i = 0; vector != null && i < vector.size(); ++i) {
                    ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
                    boolean bl3 = false;
                    for (int j = 0; j < this.autoSnapshot.length; ++j) {
                        if (this.autoSnapshot[j] != itemInInventory.itemID) continue;
                        bl3 = true;
                        break;
                    }
                    if (bl3 || itemInInventory.idTemplate != this.autoPetTemplate() || this.autoPetIsOwned(itemInInventory.itemID)) continue;
                    this.autoPetOwned.addElement(new Integer(itemInInventory.itemID));
                }
                this.autoSoldIds.removeAllElements();
                if (Canvas.currentScreen == WindowInfoScr.gI()) {
                    WindowInfoScr.gI().right.action.perform();
                }
                if (this.autoPetBuyW > 0 && n == 0) {
                    this.autoStop("Auto đồ thú: không mua được đồ thú (có thể hết xu), đã dừng.");
                    return;
                }
                int n2 = this.autoPetN();
                int[] nArray3 = this.autoPetNeeds();
                int n3 = nArray3[0];
                int n4 = nArray3[1];
                if (PauseMenu.autoGemCount(PET_STONE[0]) < Math.min(n2 * n4, n2) || PauseMenu.autoGemCount(PET_STONE[1]) < Math.min(n2 * n3, n2) || PauseMenu.autoGemCount(PET_STONE[2]) < n2) {
                    this.autoStop("Auto đồ thú: không mua đủ đá ngũ hợp (thiếu xu hoặc lượng), đã dừng.");
                    return;
                }
                this.autoPetExitHouse();
                return;
            }
            case 51: {
                if (!Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                    this.autoPhase = 30;
                    this.autoWaitTicks = 8;
                    break;
                }
                if (++this.autoWaitCount <= 300) break;
                this.autoStop("Auto đồ thú: không ra được khỏi nhà, đã dừng.");
                return;
            }
            case 20: {
                if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                    this.autoPhase = this.autoAfterHouse;
                    break;
                }
                CMLocation cMLocation = PauseMenu.autoFindHouseDoor();
                if (cMLocation != null) {
                    GameScr.oldMap = Canvas.gameScr.map;
                    GameService.gI().changeMap(cMLocation.toM, cMLocation.toX, cMLocation.toY);
                    ChangScr.gI().switchToMe();
                    Res.removeMonsterTemplet();
                    this.autoPhase = 21;
                    this.autoWaitCount = 0;
                    break;
                }
                short s = this.autoFindCard();
                if (s < 0) {
                    this.autoStop("Auto chế đồ: không có Thẻ mua bán trong hành trang. Hãy tự vào nhà Hắc Ngưu rồi bật lại auto.");
                    return;
                }
                for (int i = 0; i < CARD_BLOCKED_MAP.length; ++i) {
                    if (CARD_BLOCKED_MAP[i] != Canvas.gameScr.map) continue;
                    this.autoStop("Auto chế đồ: ở map này không có cửa nhà Hắc Ngưu và cũng không dùng được Thẻ mua bán. Hãy về làng rồi bật lại auto.");
                    return;
                }
                GameService.gI().useItem(s);
                this.autoPhase = 21;
                this.autoWaitCount = 0;
                break;
            }
            case 21: {
                if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map)) {
                    this.autoPhase = this.autoAfterHouse;
                    this.autoWaitTicks = 8;
                    break;
                }
                if (++this.autoWaitCount <= 300) break;
                this.autoStop("Auto chế đồ: không vào được nhà Hắc Ngưu, đã dừng.");
                return;
            }
            case 5: {
                GameService.gI().doRequestActionCheDo(10, this.autoCraftedItemID);
                this.autoQueueNpc = -502;
                this.autoQueueMenu = new int[]{0};
                this.autoQueueOption = new int[]{2};
                this.autoQueuePos = 0;
                WindowInfoScr.gI().listCheDo = null;
                this.autoPhase = 1;
                this.autoWaitTicks = 1;
            }
        }
    }

    /**
     * Gọi ở ĐẦU GameScr.update(): lỗi ném ra ở đây làm bỏ qua cả phần update phía sau (nhân
     * vật không đi, màn hình như đơ) - nên bắt hết, dừng auto và báo lỗi ở bước nào.
     */
    public static void autoCheDoTick() {
        try {
            AutoGuard.nutUpTick();
            AutoUp.tick();
            AutoVanTieu.tick();
            AutoPk.tick();
            AutoVanMay.tick();
            LenThanh.tick();
            TienIch.batLuong();
        }
        catch (Throwable throwable) {
            // auto up lỗi không được làm hỏng khung hình của game
        }
        if (KiemServer.canNgat()) {
            KiemServer.daQua = true;
            Canvas.cmdNgatKetNoi.action.perform();
            return;
        }
        if (me != null) {
            try {
                me.autoTick();
            }
            catch (Throwable throwable) {
                int n = me.autoPhase;
                try {
                    me.autoStop(new StringBuilder().append("Auto ").append(me.autoJob >= 0 && me.autoJob < AUTO_JOB_NAME.length ? AUTO_JOB_NAME[me.autoJob] : "").append(" lỗi ở bước ").append(n).append(": ").append(throwable.toString()).toString());
                }
                catch (Throwable throwable2) {
                    me.autoRunning = false;
                    AutoGuard.on = false;
                }
            }
        }
    }

    private PauseMenu() {
        this.init();
        this.name[9] = new String[DONG_AN_NAME.length * 2 + 1];
        this.name[9][0] = "D\u00f2ng \u1ea9n";
        for (int i = 0; i < DONG_AN_NAME.length * 2; ++i) {
            this.name[9][i + 1] = new StringBuilder().append("( ) ").append(this.dongAnLabel(i)).toString();
        }
        this.center = new Command("Ch\u1ecdn", new IAction(){

            public void perform() {
                PauseMenu.this.doSelect();
            }
        });
        this.right = new Command("\u0110\u00f3ng", new IAction(){

            public void perform() {
                if (PauseMenu.this.index == 9) {
                    PauseMenu.this.index = 0;
                    PauseMenu.this.selected = 1;
                    PauseMenu.this.Limit = PauseMenu.this.name[0].length - 1;
                    PauseMenu.this.cmyLim = PauseMenu.this.name[0].length * PauseMenu.this.hText - PauseMenu.this.h;
                    if (PauseMenu.this.cmyLim < 0) {
                        PauseMenu.this.cmyLim = 0;
                    }
                    PauseMenu.this.cmtoY = PauseMenu.this.selected * PauseMenu.this.hText - PauseMenu.this.h / 2;
                } else if (PauseMenu.this.index != -1) {
                    PauseMenu.this.selected = PauseMenu.this.index;
                    PauseMenu.this.index = -1;
                    PauseMenu.this.Limit = 9;
                    PauseMenu.this.cmyLim = PauseMenu.this.Limit * PauseMenu.this.hText - PauseMenu.this.h;
                    PauseMenu.this.cmtoY = PauseMenu.this.selected * PauseMenu.this.hText - PauseMenu.this.h / 2;
                } else {
                    Canvas.gameScr.switchToMe();
                }
            }
        });
    }

    public void paint(Graphics graphics) {
        Canvas.gameScr.paint(graphics);
        Canvas.gameScr.paintServerInfo(graphics);
        Res.paintDlgDragonFull(graphics, this.x - 5, this.y - 10, this.w + 10, this.h + 20);
        graphics.setClip(this.x, this.y - 2, this.w + 2, this.h + 4);
        int n = this.y;
        graphics.translate(0, -this.cmy);
        for (int i = 0; i < this.Limit; ++i) {
            if (i == this.selected) {
                Res.paintSelected(graphics, this.x, n, this.w, this.hText);
                Font.normalFont[0].drawString(graphics, this.index == -1 ? this.name[i][0] : this.name[this.index][i + 1], Canvas.hw, n + this.hText / 2 - Font.normalFont[0].getHeight() / 2, 2);
            } else {
                Font.arialFontW.drawString(graphics, this.index == -1 ? this.name[i][0] : this.name[this.index][i + 1], Canvas.hw, n + this.hText / 2 - Font.normalFont[0].getHeight() / 2, 2);
            }
            n += this.hText;
        }
        super.paint(graphics);
    }

    public void moveCamera() {
        if (this.cmy != this.cmtoY) {
            this.cmvy = this.cmtoY - this.cmy << 2;
            this.cmdy += this.cmvy;
            this.cmy += this.cmdy >> 4;
            this.cmdy &= 0xF;
        }
    }

    public void update() {
        Canvas.gameScr.update();
        this.moveCamera();
        if (Math.abs(this.cmtoY - this.cmy) < 15 && this.cmy < 0) {
            this.cmtoY = 0;
        }
        if (Math.abs(this.cmtoY - this.cmy) < 10 && this.cmy > this.cmyLim) {
            this.cmtoY = this.cmyLim;
        }
    }

    public void updateKey() {
        if (Canvas.isPointer(this.x, this.y, this.w, this.h)) {
            int n;
            int n2 = Canvas.pyLast - Canvas.py;
            if (Canvas.isPointerDown) {
                if (!this.trans) {
                    this.pa = this.cmy;
                    this.trans = true;
                    n = (this.cmtoY + Canvas.py - this.y) / this.hText;
                    if (n == this.selected) {
                        this.doSelect();
                    }
                    this.selected = n;
                    if (this.selected < 0) {
                        this.selected = 0;
                    }
                    if (this.selected >= 9) {
                        this.selected = 8;
                    }
                    this.cmtoY = this.selected * this.hText - this.h / 2;
                    if (this.cmtoY < 0) {
                        this.cmtoY = 0;
                    }
                }
                this.cmtoY = this.pa + (Canvas.pyLast - Canvas.py);
                if (this.cmtoY < -10) {
                    this.cmtoY = -10;
                }
                if (this.cmtoY > this.cmyLim + 10) {
                    this.cmtoY = this.cmyLim + 10;
                }
            }
            if (Canvas.isPointerClick) {
                Canvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(n2) < 5 && (n = (this.cmtoY + this.y) / this.hText) == this.selected) {
                    this.doSelect();
                }
            }
        }
        if (Canvas.isKeyPressed(8)) {
            ++this.selected;
            if (this.selected >= this.Limit) {
                this.selected = 0;
            }
            this.cmtoY = this.selected * this.hText - this.h / 2;
            if (this.cmtoY < 0) {
                this.cmtoY = 0;
            }
            if (this.cmtoY > this.cmyLim) {
                this.cmtoY = this.cmyLim;
            }
        }
        if (Canvas.isKeyPressed(2)) {
            --this.selected;
            if (this.selected < 0) {
                this.selected = this.Limit - 1;
            }
            this.cmtoY = this.selected * this.hText - this.h / 2;
            if (this.cmtoY < 0) {
                this.cmtoY = 0;
            }
            if (this.cmtoY > this.cmyLim) {
                this.cmtoY = this.cmyLim;
            }
        }
        super.updateKey();
    }

    private void doSelect() {
        if (this.index == -1) {
            if (this.name[this.selected].length == 1) {
                switch (this.selected) {
                    case 8: {
                        int n = 0;
                        for (int i = 0; i < Res.shopTemplate.size(); ++i) {
                            GemTemp gemTemp = (GemTemp)Res.shopTemplate.elementAt(i);
                            if (gemTemp.shopType + 1 <= n) continue;
                            n = gemTemp.shopType + 1;
                        }
                        byte[] object = new byte[n];
                        for (int i = 0; i < object.length; ++i) {
                            object[i] = 20;
                        }
                        WindowInfoScr.gI().setInfo(0, true, object);
                        WindowInfoScr.gI().switchToMe();
                        return;
                    }
                    case 3: {
                        Canvas.gameScr.doNapXu();
                        this.right.action.perform();
                        break;
                    }
                    case 4: {
                        this.showMe(6);
                        return;
                    }
                    case 6: {
                        break;
                    }
                    case 5: {
                        Canvas.gameScr.switchToMe();
                        Canvas.gameScr.gameService.requestKiller();
                        return;
                    }
                }
            }
            this.index = this.selected;
            this.Limit = this.name[this.selected].length - 1;
            this.cmyLim = this.name[this.selected].length * this.hText - this.h;
            if (this.cmyLim < 0) {
                this.cmyLim = 0;
            }
            this.selected = 0;
            this.cmtoY = 0;
            this.cmy = 0;
        } else if (this.index == 9) {
            this.dongAnChon[this.selected] = !this.dongAnChon[this.selected];
            this.name[9][this.selected + 1] = new StringBuilder().append(this.dongAnChon[this.selected] ? "(x) " : "( ) ").append(this.dongAnLabel(this.selected)).toString();
        } else {
            // hàng mod (index 0): mục đầu là "Xa phu", các mục cũ lùi xuống một
            if (this.index == 0 && this.selected == 0) {
                Canvas.gameScr.switchToMe();
                TienIch.moXaPhu();
                return;
            }
            switch (this.index == 0 ? this.selected - 1 : this.selected) {
                case 0: {
                    if (this.index == 0) {
                        Canvas.gameScr.switchToMe();
                        this.showCheDoNhanhMenu();
                        break;
                    }
                    if (this.index == 1) {
                        Canvas.gameScr.showConfig();
                        Canvas.gameScr.switchToMe();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(0);
                        break;
                    }
                    if (this.index == 6) {
                        Canvas.gameScr.switchToMe();
                        MessageScr.gI().switchToMe();
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.gameScr.switchToMe();
                    GameService.gI().requestTopStronger_Righer(5, 0);
                    break;
                }
                case 1: {
                    if (this.index == 0) {
                        this.index = 9;
                        this.Limit = this.name[9].length - 1;
                        this.cmyLim = this.name[9].length * this.hText - this.h;
                        if (this.cmyLim < 0) {
                            this.cmyLim = 0;
                        }
                        this.selected = 0;
                        this.cmtoY = 0;
                        this.cmy = 0;
                        break;
                    }
                    if (this.index == 1) {
                        Canvas.gameScr.switchToMe();
                        this.doBigMap();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(3);
                        break;
                    }
                    if (this.index == 6) {
                        this.showMe(5);
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.gameScr.switchToMe();
                    Canvas.startWaitDlg();
                    if (Canvas.gameScr.mainChar.idClan == -1) {
                        GameService.gI().registerClan();
                        break;
                    }
                    GameService.gI().questClan(0);
                    Canvas.gameScr.showWindow(0, false, new byte[]{27});
                    Canvas.startWaitDlg();
                    break;
                }
                case 2: {
                    if (this.index == 0) {
                        Canvas.gameScr.switchToMe();
                        AutoUp.moMenu(0);
                        break;
                    }
                    if (this.index == 1) {
                        Canvas.gameScr.switchToMe();
                        ++Canvas.gameScr.hideGUI;
                        if (Canvas.gameScr.hideGUI <= 2) break;
                        Canvas.gameScr.hideGUI = 0;
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(2);
                        break;
                    }
                    if (this.index == 6) {
                        Canvas.gameScr.switchToMe();
                        if (ListScr.gI().freindList == null || ListScr.gI().freindList.size() == 0) {
                            Canvas.startOKDlg("Ch\u01b0a c\u00f3 b\u1ea1n");
                            break;
                        }
                        Vector<Char> vector = new Vector<Char>();
                        for (int i = 0; i < ListScr.gI().freindList.size(); ++i) {
                            Char char_ = (Char)ListScr.gI().freindList.elementAt(i);
                            vector.addElement(char_);
                        }
                        ListScr.gI().setInfo(vector, 0, "B\u1ea0N B\u00c8");
                        ListScr.gI().switchToMe();
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.startWaitDlg();
                    GameService.gI().requestClanList(Canvas.gameScr.mainChar.idClan, (byte)0);
                    break;
                }
                case 3: {
                    if (this.index == 0) {
                        Canvas.gameScr.switchToMe();
                        AutoVanTieu.moMenu(0);
                        break;
                    }
                    if (this.index == 1) {
                        Canvas.gameScr.switchToMe();
                        HelpScr.gI().switchToMe();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(1);
                        break;
                    }
                    if (this.index == 6) {
                        GameService.gI().requestTopStronger_Righer(4, 0);
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.startWaitDlg();
                    GameService.gI().requestClanInfo(Canvas.gameScr.mainChar.idClan);
                    break;
                }
                case 4: {
                    if (this.index == 0) {
                        Canvas.gameScr.switchToMe();
                        AutoVanMay.moMenu(0);
                        break;
                    }
                    if (this.index == 1) {
                        if (!Nen.coAmThanh()) break;
                        Nen.datAmThanh(!Nen.amThanhBat());
                        Canvas.gameScr.switchToMe();
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(4);
                        break;
                    }
                    if (this.index == 6) {
                        Canvas.gameScr.switchToMe();
                        GameService.gI().requestTopStronger_Righer(3, 0);
                        break;
                    }
                    if (this.index != 7) break;
                    Canvas.gameScr.switchToMe();
                    GameService.gI().msgClanAll(null, (byte)1, 0);
                    break;
                }
                case 5: {
                    if (this.index == 0) {
                        Canvas.gameScr.switchToMe();
                        TienIch.moMenu(0);
                        break;
                    }
                    if (this.index == 7) {
                        Canvas.gameScr.showWindow(0, true, new byte[]{29, 30});
                        break;
                    }
                    if (this.index == 2) {
                        this.showMe(7);
                        break;
                    }
                    if (this.index == 6) {
                        GameService.gI().requestTopStronger_Righer(7, 0);
                        break;
                    }
                    if (this.index != 1) break;
                    Canvas.gameScr.onoffInvite = !Canvas.gameScr.onoffInvite;
                    GameService.gI().setConfig(!Canvas.gameScr.onoffInvite ? 4 : 5);
                    Canvas.gameScr.switchToMe();
                    Canvas.startOKDlg(!Canvas.gameScr.onoffInvite ? "\u0110\u00e3 b\u1eadt ch\u1ee9c n\u0103ng nh\u1eadn l\u1eddi m\u1eddi." : "\u0110\u00e3 t\u1eaft ch\u1ee9c n\u0103ng nh\u1eadn l\u1eddi m\u1eddi.");
                    break;
                }
                case 6: {
                    if (this.index == 7) {
                        Canvas.gameScr.switchToMe();
                        MessageScr.gI().addTab("", "Bang h\u1ed9i", 1);
                        MessageScr.gI().setFocusTab("Bang h\u1ed9i");
                        MessageScr.gI().switchToMe();
                        break;
                    }
                    if (this.index == 1) {
                        Canvas.autoTab.switchToMe();
                        break;
                    }
                    if (this.index == 6) {
                        MsgWorld.gI().switchToMe();
                        break;
                    }
                    if (this.index != 2) break;
                    Tree2014.gI().switchToMe(Canvas.gameScr);
                    break;
                }
                case 7: {
                    if (this.index == 7) {
                        Canvas.inputDlg.setInfo("S\u1ed1 l\u01b0\u1ee3ng", new IAction(){

                            public void perform() {
                                String string = Canvas.inputDlg.tfInput.getText();
                                if (!string.equals("")) {
                                    try {
                                        GameService.gI().transMoney(Integer.parseInt(string));
                                        Canvas.startOKDlg("\u0110\u00e3 chuy\u1ec3n ti\u1ec1n th\u00e0nh c\u00f4ng");
                                        Canvas.gameScr.switchToMe();
                                    }
                                    catch (Exception exception) {
                                        Canvas.startOKDlg("B\u1ea1n ph\u1ea3i nh\u1eadp s\u1ed1");
                                    }
                                }
                            }
                        }, 1, 100, false);
                        Canvas.inputDlg.show();
                        break;
                    }
                    if (this.index == 6) {
                        try {
                            if (GameMidlet.url == "") break;
                            GameMidlet.instance.platformRequest(GameMidlet.url);
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        break;
                    }
                    if (this.index == 2) {
                        TuBinhScr.gI().switchToMe(Canvas.gameScr);
                        break;
                    }
                    if (this.index != 1) break;
                    Canvas.autoTab.switchToMe();
                    Canvas.autoTab.isOptionFocus = true;
                    Canvas.autoTab.currentIndex = GameScr.typeOptionFocus;
                    Canvas.autoTab.setInfo();
                    break;
                }
                case 8: {
                    if (this.index == 2) {
                        GameService.gI().dosendKhacMenu();
                        return;
                    }
                    if (Canvas.gameScr.mainChar.idClan == -1) {
                        Canvas.startOKDlg("B\u1ea1n ch\u01b0a c\u00f3 bang");
                        break;
                    }
                    if (Canvas.gameScr.mainChar.isMaster == 0) {
                        Canvas.startYesNoDlg(Char.dissolved ? "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n ph\u1ee5c h\u1ed3i l\u1ea1i bang h\u1ed9i kh\u00f4ng ?" : "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n gi\u1ea3i t\u00e1n bang h\u1ed9i kh\u00f4ng ?", new IAction(){

                            public void perform() {
                                Canvas.gameScr.switchToMe();
                                GameService.gI().dissolveCLan();
                                Canvas.startOKDlg(Char.dissolved ? "\u0110\u00e3 kh\u00f4i ph\u1ee5c l\u1ea1i bang h\u1ed9i" : "\u0110\u00e3 gi\u1ea3i t\u00e1n bang h\u1ed9i");
                                Char.dissolved = !Char.dissolved;
                                PauseMenu.this.initName();
                            }
                        });
                        break;
                    }
                    Canvas.startYesNoDlg("B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n r\u1eddi bang kh\u00f4ng ?", new IAction(){

                        public void perform() {
                            GameService.gI().outClan();
                            Canvas.gameScr.mainChar.idClan = (short)-1;
                            Canvas.gameScr.switchToMe();
                            PauseMenu.this.initName();
                            Canvas.startOKDlg("R\u1eddi bang th\u00e0nh c\u00f4ng");
                        }
                    });
                    break;
                }
            }
        }
    }

    public void showMe(int n) {
        byte[] byArray;
        WindowInfoScr.gI().switchToMe();
        WindowInfoScr windowInfoScr = WindowInfoScr.gI();
        boolean bl = WindowInfoScr.gI().isSell;
        if (WindowInfoScr.gI().isSell) {
            byArray = WindowInfoScr.gI().arrSell;
        } else {
            byte[] byArray2 = new byte[8];
            byArray2[1] = 1;
            byArray2[2] = 2;
            byArray2[3] = 3;
            byArray2[4] = 4;
            byArray2[5] = 5;
            byArray2[6] = 6;
            byArray = byArray2;
            byArray2[7] = 31;
        }
        windowInfoScr.setInfo(n, bl, byArray);
        WindowInfoScr.charWearing = Canvas.gameScr.mainChar;
        WindowInfoScr.gI().setImageCharWear();
    }

    public void showShopNew(int n) {
        byte[] byArray;
        ShopNew.gI().switchToMe();
        ShopNew shopNew = ShopNew.gI();
        boolean bl = ShopNew.gI().isSell;
        if (ShopNew.gI().isSell) {
            byArray = ShopNew.gI().arrSell;
        } else {
            byte[] byArray2 = new byte[8];
            byArray2[1] = 1;
            byArray2[2] = 2;
            byArray2[3] = 3;
            byArray2[4] = 4;
            byArray2[5] = 5;
            byArray2[6] = 6;
            byArray = byArray2;
            byArray2[7] = 31;
        }
        shopNew.setInfo(n, bl, byArray);
        ShopNew.charWearing = Canvas.gameScr.mainChar;
        ShopNew.gI().setImageCharWear();
    }

    private void doBigMap() {
        final short[][] sArrayArray = new short[][]{{28, 105}, {55, 100}, {24, 138}, {61, 148}, {105, 76}, {80, 128}, {112, 132}, {136, 111}, {76, 60}, {129, 158}, {173, 155}, {153, 160}, {145, 60}, {127, 87}, {126, 62}, {35, 60}, {35, 60}};
        short[] sArray = new short[17];
        sArray[1] = 1;
        sArray[2] = 2;
        sArray[3] = 3;
        sArray[4] = 4;
        sArray[5] = 5;
        sArray[6] = 6;
        sArray[7] = 7;
        sArray[8] = 8;
        sArray[9] = 9;
        sArray[10] = 10;
        sArray[11] = 11;
        sArray[12] = 110;
        sArray[13] = 111;
        sArray[14] = 112;
        sArray[15] = 202;
        sArray[16] = 104;
        short[] sArray2 = sArray;
        int n = 0;
        for (int i = 0; i < sArray2.length; ++i) {
            if (Canvas.gameScr.map != sArray2[i]) continue;
            n = i;
            break;
        }
        final int n2 = n;
        PageScr.gI().setInfo(Canvas.hw - 94, Canvas.hh - 84 - Screen.deltaY, 197, 168, Res.nameBigMap[n2], null);
        PageScr.gI().indexFont = 1;
        PageScr.gI().left = new Command("Tr\u1edf l\u1ea1i", new IAction(){

            public void perform() {
                Canvas.gameScr.switchToMe();
            }
        });
        PageScr.gI().layer = new Layer(){
            public void paint(Graphics graphics, int n, int n22) {
                GameData.paintIconCayThan(graphics, (short)800, 98, 95);
                Res.getCharPartInfo(2, Canvas.gameScr.mainChar.currentHead).paint(graphics, sArrayArray[n2][0], sArrayArray[n2][1], 0, 1);
            }
        };
        PageScr.gI().switchToMe();
    }

    public void switchToMe() {
        this.Limit = 9;
        this.selected = 0;
        this.index = -1;
        this.cmyLim = this.Limit * 18 - this.h;
        this.cmy = 0;
        this.cmtoY = 0;
        this.name[0][0] = Canvas.gameScr.mainChar.name;
        if (Nen.coAmThanh()) {
            this.name[1][5] = Nen.amThanhBat() ? "T\u1ea5t \u00e2m thanh" : "B\u1eadt \u00e2m thanh";
        }
        super.switchToMe();
        this.init();
    }

    static {
        DONG_AN_NAME = new String[]{"Gi\u1ea3m st v\u1eadt", "Gi\u1ea3m st ma", "T\u0103ng t\u1ea5n c\u00f4ng", "Xuy\u00ean gi\u00e1p", "Ph\u1ea3n st"};
        AUTO_SLOT_NAME = new String[]{"V\u0169 kh\u00ed", "\u00c1o", "Qu\u1ea7n", "N\u00f3n", "Gi\u00e0y", "G\u0103ng", "Nh\u1eabn", "Nh\u1eabn 2", "D\u00e2y chuy\u1ec1n", "Ng\u1ecdc"};
    }
}
