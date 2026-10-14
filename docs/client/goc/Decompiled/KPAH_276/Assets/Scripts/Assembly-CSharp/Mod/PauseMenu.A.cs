// Port 1:1 từ Jar276/src/game/render/screen/PauseMenu.java (dòng 1-1363): khai báo field/hằng số
// + các hàm từ showCheDoNhanhMenu tới autoStartNuoiPet. Các phần khác ở PauseMenu.B/C/D.cs.
public partial class PauseMenu : MyScreen
{
	private const int TOP_COUNT = 9;

	// Java khởi tạo DONG_AN_NAME / AUTO_SLOT_NAME trong khối static {} cuối file - ở C# khởi tạo luôn tại đây.
	private static readonly string[] DONG_AN_NAME = new string[] { "Giảm st vật", "Giảm st ma", "Tăng tấn công", "Xuyên giáp", "Phản st" };

	private const int DONG_AN_TIER = 2;

	private bool[] dongAnChon = new bool[DONG_AN_NAME.Length * 2];

	private bool heMaPhap = true;

	private static readonly string[] AUTO_SLOT_NAME = new string[] { "Vũ khí", "Áo", "Quần", "Nón", "Giày", "Găng", "Nhẫn", "Nhẫn 2", "Dây chuyền", "Ngọc" };

	private bool autoRunning = false;
	private int autoMode;
	private int autoSlot;
	private int autoLevel;
	private int autoPhase;
	private int autoWaitTicks;
	private int autoWaitCount;
	private short[] autoSnapshot;
	private int autoTries;
	private int autoFound;
	private sbyte[][] autoFill;
	private int autoStageIdx;
	private int autoJob;
	private int autoGoalLevel = 50;
	private short[] autoTargets;
	private int autoTargetPos;
	private bool autoOkSeen;
	private bool autoLockTtc;
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
	private mVector autoPetOwned;
	private mVector autoPetSell;
	private mVector autoPetEp;
	private int autoPetMode;
	private int autoOkCount;
	private int autoPetGroups;
	private mVector autoInvRef;
	private int autoTkType;
	private int autoTkFilter;
	private int autoTkQty = 1;
	private int autoTkGrade;
	private bool autoTkNoMat;
	private bool autoTkNeedStone;
	private int autoTkStones;
	private mVector autoSoldIds = new mVector();
	private int autoTbLevel = 60;
	private int autoTbPct = -1;
	private int autoTbResult;
	private short autoTbItem;
	private mVector autoShopRef;
	private int autoLuyenTarget = 10;
	private bool autoLuyenBuy;
	private short autoLkd = 10;
	private short autoBh = -1;
	private short autoMm = -1;
	private static readonly short[] LKD_IDS = new short[] { 8, 9, 10, 157, 158, 11, 66, 245 };
	private static readonly string[] LKD_NAME = new string[] { "20%", "30%", "40%", "cao cấp 30%", "cao cấp 40%", "đặc biệt 30", "đặc biệt 35", "đặc biệt 40" };
	private static readonly short[] BH_IDS = new short[] { -1, 0, 1, 2, 3, 4 };
	private static readonly string[] BH_NAME = new string[] { "Không", "20%", "30%", "40%", "50%", "90%" };
	private static readonly short[] MM_IDS = new short[] { -1, 5, 6, 7, 155, 156 };
	private static readonly string[] MM_NAME = new string[] { "Không", "cấp 1", "cấp 2", "cấp 3", "cấp 4", "cấp 5" };
	private bool autoLkdLock;
	private bool autoBhLock;
	private bool autoMmLock;
	private int autoLuyenIdle;
	private int autoLuyenLastPlus;
	private int autoLuyenLastLkd;
	private string autoLastAlert;
	private int autoLuyenBuyCd;
	private short autoThemItem;
	private int autoThemBotLock;
	private int autoThemDaLock;
	private bool autoThemWeapon;
	private bool autoThemFull;
	private bool autoThemBuy;
	private bool autoThemOk;
	private bool[] autoThemChon = new bool[34];
	private int[] autoThemStart = new int[5];
	private static readonly string[] TD_NAME = new string[] { "Hút máu", "Tàn phế", "Câm lặng", "Hồi HP", "Hồi MP", "Tăng rớt vật phẩm", "Tăng rớt xu", "Kháng băng lan", "Kháng sét lan", "Kháng độc lan", "Tăng thủ trang bị", "Kháng lửa lan", "Dòng ẩn: Giảm st vật", "Dòng ẩn: Giảm st ma", "Dòng ẩn: Tăng tấn công", "Dòng ẩn: Xuyên giáp", "Dòng ẩn: Phản st" };
	private static readonly int[] TD_ID = new int[] { 77, 80, 82, 78, 79, 83, 84, 85, 86, 87, 88, 92, 28, 29, 30, 31, 32 };
	private static readonly int[][] TD_MAX = new int[][] { new int[] { 30, 40, 55 }, new int[] { 10, 20, 30 }, new int[] { 10, 20, 30 }, new int[] { 3, 7, 10 }, new int[] { 3, 7, 10 }, new int[] { 30, 70, 100 }, new int[] { 30, 70, 100 }, new int[] { 10, 20, 40 }, new int[] { 10, 20, 40 }, new int[] { 10, 20, 40 }, new int[] { 10, 20, 40 }, new int[] { 10, 20, 40 }, new int[] { 3, 6, 9 }, new int[] { 3, 6, 9 }, new int[] { 3, 6, 9 }, new int[] { 2, 3, 5 }, new int[] { 3, 6, 9 } };
	private static readonly string[] TK_TYPE_NAME = new string[] { "Quạt ba tiêu", "Gậy như ý", "Lưỡng nhận đao" };
	private static readonly int[] TK_QTY = new int[] { 1, 2, 3, 5, 10, 20, 50 };
	// Auto "Cộng thuộc tính" (job 9, ScreenDapDo type 0 ở nhà Hắc Ngưu): đập lại tới khi ra
	// đúng loại (hoặc loại bất kỳ) với mức >= autoCttMin (phần mười %, 5 = 0.5%).
	private short autoCttItem;
	private short autoCttDa;
	private bool autoCttLock;
	private int autoCttLoai = -1;
	private int autoCttMin = 5;
	private bool autoCttOk;
	private string autoCttVuaRa;
	private bool autoCttFull;
	private bool autoCttBoQua;
	/** id thuộc tính phía client của CTT_NAME (otherAtt 5/6/7/28 + 61). */
	private static readonly int[] CTT_ID = new int[] { 66, 67, 68, 89 };
	private static readonly string[] CTT_NAME = new string[] { "Sét lan", "Độc lan", "Băng lan", "Lửa lan" };
	private const short CTT_DA_THUONG = 227;
	// Auto "Khóa đồ thú" (job 10, ScreenDapDo type 2): khóa lại tới khi chỉ số khóa (id 58/59/60,
	// lấy max) >= autoKtMuc. 20-21% chỉ ra với xương cấp 6 - Item.lockItemAnimal ở server.
	private short autoKtItem;
	private short autoKtDa;
	private bool autoKtLock;
	private int autoKtMuc = 20;
	private bool autoKtFull;
	private int autoKtDaTruoc;
	private const short KT_XUONG_CAP6 = 234;
	// Auto "Nghiền bột" (job 14, trong nhà): chế đồ cấp 20/21 bằng nguyên liệu đã chọn rồi
	// nghiền (-68 type 4, OfflineMap.doNghienBot: 2000 xu, 1 bột, cách nhau >= 5s). Chế món sau
	// trong lúc chờ 5s. CHỈ nghiền món do chính auto vừa chế; món trắng không nghiền được -> bán.
	private int autoNbLoai = -1;
	private int autoNbCapSo = 0;
	private int autoNbCapCao = 5;
	private short autoNbDang = (short)-1;
	/** Đang có món chờ kết quả nghiền. KHÔNG dùng "autoNbDang < 0" làm dấu hiệu: itemID là
	 *  short, id > 32767 bị âm (vd -31922) - auto tưởng chưa có món và không bao giờ gửi lệnh. */
	private bool autoNbCo;
	private int autoNbThu;
	private long autoNbLuc;
	private bool autoNbHet;
	private int autoNbChe;
	private int autoNbBan;
	/** Câu server trả về gần nhất khi từ chối nghiền (đã ẩn hộp thoại) - để báo khi bỏ cuộc. */
	private string autoNbLoi;
	/** Số bột (gem 246) lúc gửi lệnh nghiền - nghiền thành công server cộng 1 bột. */
	private int autoNbBotTruoc;
	/** Chẩn đoán: số món gom được lúc bắt đầu / bỏ vì không thấy trong túi / số lệnh nghiền đã gửi. */
	private int autoNbGom;
	private int autoNbBo;
	private int autoNbGui;
	private const int NB_BOT = 246;
	/** Server cho nghiền cách nhau >= 200ms (OfflineMap.THOI_GIAN_CHO_NGHIEN_BOT_MS, trước là 5s). */
	private const long NB_CACH_MS = 300L;
	/** Gửi lệnh nghiền lúc nào - chờ kết quả (bột tăng / câu từ chối) tối đa NB_CHO_KQ_MS rồi mới
	 *  coi là thất bại: gửi lại khi kết quả chưa về là nghiền lại món đã mất -> "Không tìm thấy". */
	private long autoNbGuiLuc;
	private const long NB_CHO_KQ_MS = 3000L;
	private const int NB_HANG_DOI_TOI_DA = 3;
	// Auto "Nâng cấp Hỏa kỳ lân" (job 13): Hỏa kỳ lân 681 -> cấp 2 (682) -> cấp 3 (683, tối đa).
	// Mỗi lần: dùng món (server mở menu ảo -513) -> "Nâng cấp" -> chọn Hỏa thạch; 1 viên/lần,
	// cấp 1 (gem 258) 1%, cấp 2 (gem 259) 10% - Char.doNangCapHoaKyLan. Hỏa thạch cấp 1 bán
	// 10 lượng/viên nhưng CHỈ mua được trong nhà (OfflineMap.doBuyGemItem); cấp 2 không bán.
	private bool autoHklMua = true;
	private int autoHklOk;
	private int autoHklHong;
	private int autoHklGui;
	private int autoHklKqTruoc;
	private int autoHklDaMua;
	private int autoHklMuaTruoc;
	private const int HKL_CAP_TOI_DA = 683;
	private const short HOA_THACH_C1 = 258;
	private const short HOA_THACH_C2 = 259;
	private const int HKL_LO = 10;
	private const int HKL_MUA_LO = 100;
	// Auto "Học skill pet" (job 12): dùng Sách kỹ năng pet (potion 111) tới khi pet có skill
	// mong muốn; hết slot mà chưa ra (hoặc phẩm chất thấp hơn mức chọn) thì dùng Huyết Bồ Đề
	// (item 721) - xoá skill và random lại phẩm chất (Pet.doChangeThongThao ở server).
	private int autoSkMuon;
	/** Học slot 2: Tắt = chỉ cần slot 1 đúng skill; Bật = cần đủ cặp skill slot 1 + slot 2. */
	private bool autoSkSlot2;
	private int autoSkMuon2 = 1;
	private int autoSkPhamChat = 2;
	private int autoSkKq;
	private bool autoSkReset;
	private int autoSkSach;
	private int autoSkHbd;
	/** Server bỏ qua (không trả lời) nếu dùng cùng potion trong 250ms - Char.POTION_COOLDOWN_MS. */
	private long autoSkLuc;
	/** Tên skill y như Pet.ALL_SKILL ở server (dòng "- Kỹ năng: ..." trong thông tin pet). */
	private static readonly string[] SK_TEN = new string[] { "Chữa trị", "Phá triền", "Đoạt mệnh", "Thiên nhãn", "Tiên khí", "Kim cang", "Càn khôn", "Cường tráng", "Cường thân", "Kiên cố", "Hộ thể", "Liên kích", "Tăng kích", "Thuấn ảnh", "Phí huyết" };
	/** Pet.NAME_PHAM_CHAT; tỉ lệ học slot 1 = 0/1/10/25%, slot 2 = 0/0/5/10%. */
	private static readonly string[] SK_PHAM_CHAT = new string[] { "Ngu đần", "Bình thường", "Nhanh nhẹn", "Thông minh" };
	private const int POTION_SACH_SKILL = 111;
	private const int ITEM_HUYET_BO_DE = 721;
	/** Huyết Bồ Đề ở cửa hàng đặc biệt: mục 132 (data_shop), 10 lượng/viên - lệnh 85. */
	private const int SHOP_HUYET_BO_DE = 132;
	private bool autoSkMua = true;
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
	private bool autoPetTuHon;
	private int autoPetTuTruoc;
	private const int POTION_TU_HON_DAN = 83;
	private static readonly string[] PET_LOAI_TEN = new string[] { "Vải + Tơ lụa", "Sắt + Bạc", "Sắt + Gỗ sưa", "Ngọc + Thủy tinh", "Gỗ thường + Bạc", "Da mềm + Da cứng" };
	private static readonly int[] PET_LOAI_SO = new int[] { 68, 75, 75, 82, 89, 96 };
	private static readonly int[] PET_LOAI_CAO = new int[] { 103, 110, 124, 117, 110, 131 };
	/** Món auto đã chế mà pet chưa ăn (Integer itemID) - giữ qua cả đăng nhập lại. */
	private mVector autoPetLo = new mVector();
	/** "Công thức" chế lấy từ món đầu tiên: chuỗi menu NPC -8, id món, cách xếp nguyên liệu. */
	private bool autoPetCoCongThuc;
	private int autoPetQNpc;
	private int[] autoPetQMenu;
	private int[] autoPetQOpt;
	private short autoPetIdTemp;
	private sbyte[][] autoPetFillMau;
	private int[] autoPetNLId;
	/** Số món đang chờ trong lô (chế hoặc cho ăn) và số món còn lại trước khi cho ăn. */
	private int autoPetLoCho;
	/** Mỗi lần chế 1 món rồi nối đuôi: gửi lệnh cho ăn món này kèm lệnh chế món sau. */
	private const int PET_LO_TOI_DA = 1;
	/** Mỗi lượt Tụ hồn đan gửi liền tối đa ngần này lệnh ăn. */
	private const int PET_LO_TU_HON = 10;
	/** Đếm danh sách "Cho pet ăn" đã xin / đã nhận - chỉ dùng danh sách của lần xin MỚI NHẤT. */
	private int autoPetDsCho;
	private int autoPetDsNhan;
	private bool autoPetXinLai;
	/** Số món chế nối đuôi đang chờ, và id các món của lô lúc gửi lệnh cho ăn. */
	private int autoPetCheTheo;
	private short[] autoPetLoTruocIds;
	private string[] autoPetDsAn;
	private int autoPetDaAn;
	private int autoPetDaNang;
	private int autoPetUpKq;
	private bool autoPetDay;
	private bool autoPetCanNang;
	/** Server báo "... đã có thể nâng cấp lên N" / "tiến hoá" sau khi cho ăn. */
	private bool autoPetBaoNang;
	// ---- giai đoạn 2: đăng nhập lại + tự lên Thành
	/** Vừa đăng nhập lại: sức ăn hiển thị còn cũ, server chỉ trừ về 0 ở lần cho ăn đầu tiên. */
	private bool autoPetSauRelog;
	private int autoPetRelog;
	private volatile string autoRelogLoi;
	private int autoLenMap;
	/** Đường đi hiện tại: các điểm (chỉ số ô y*w+x), đi từ đầu tới cuối. */
	private int[] autoDuong;
	private int autoDuongPos;
	private bool[] autoDich;
	private int autoDiDung;
	private int autoDiLastX;
	private int autoDiLastY;
	/** Lướt: tự gửi từng bước <= 48px đúng nhịp server cho phép (nhanh hơn giữ phím). */
	private long autoDiLuc;
	private int autoDiBiKeo;
	/** Server từ chối bước lướt (gửi lại lệnh 4 vị trí thật cho chính mình) - xem viTriServer(). */
	private volatile bool autoCoKeo;
	private volatile int autoKeoX;
	private volatile int autoKeoY;
	/** Bị từ chối nhiều -> chặng này đi bằng giữ phím mũi tên. */
	private bool autoDiPhim;
	private int autoDiTinhLai;
	/** Thành (201) và map 9 (có cổng lên Thành) - so theo nhóm map của client. */
	private const int MAP_THANH = 201;
	private const int MAP_CONG_THANH = 9;
	/** Xe ngựa: ưu tiên đích xa nhất - Làng -> 5 -> 7 -> 9. */
	private static readonly int[] XE_NGUA_DICH = new int[] { 9, 7, 5 };
	/** Sức ăn tốn nhiều nhất của món chế bằng nguyên liệu cấp 1..6 (trắng 20, đỏ 5, Hoàn mỹ 1). */
	private static readonly int[] PET_AN_TON = new int[] { 20, 20, 20, 20, 5, 1 };
	/** LKD 30 / 35 / 40 và mục nâng cấp tương ứng ở NPC -16 (2 = không dùng LKD). */
	private static readonly short[] PET_LKD_ID = new short[] { 11, 66, 245 };
	private static readonly int[] PET_LKD_MUC = new int[] { 3, 4, 5 };
	private const short CTT_DA_CAO_CAP = 228;
	private int autoPetHe;
	private int autoPetBaseHe = -1;
	private static readonly string[] PET_MODE_NAME = new string[] { "Bất kỳ hệ", "Theo hệ", "Full 5 món kích ngũ hành" };
	private static readonly string[] PET_HE_NAME = new string[] { "Thủy", "Mộc", "Hỏa", "Thổ", "Kim" };
	private static readonly string[] PET_SLOT_NAME = new string[] { "Nón", "Giáp", "Yên cương", "Bàn đạp", "Hộ uyển", "Tất cả" };
	private static readonly int[] PET_BASE = new int[] { 509, 521, 533, 545, 557 };
	private static readonly int[] PET_MAT = new int[] { 1, 2, 3, 4, 4, 5, 5, 5, 5, 7, 9, 11 };
	private static readonly int[] PET_STONE = new int[] { 137, 143, 154 };
	private static readonly int[] PET_SRC_COLOR = new int[] { 0, 3, 2 };
	private static readonly string[] PET_FILTER_NAME = new string[] { "Bỏ qua tc ma 3%", "Bỏ qua tc vật 3%", "Ma hoặc vật 3%", "Tắt (dòng ẩn ngẫu nhiên)" };
	private static readonly short[] CARD_BLOCKED_MAP = new short[] { 0, 301, 302, 303, 304, 70, 1701, 1702, 1703, 1704, 17, 25, 111, 118, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 225, 226 };
	private static readonly string[] REP_STAT_NAME = new string[] { "Bất kỳ", "Sức mạnh", "Khéo léo", "Tinh thần", "Sức khỏe" };
	private static readonly string[] REP_HP_NAME = new string[] { "Bất kỳ", "HP", "MP" };
	private static readonly int[] AUTO_SLOT_OPT = new int[] { -1, 0, 1, 2, 3, 4, 5, 5, 6, 7 };
	private static readonly string[] AUTO_JOB_NAME = new string[] { "Chế đồ", "Nâng cấp lv", "Khóa đồ", "Sửa thuộc tính", "Trang bị thú", "Thần khí", "Thần binh", "Luyện đồ", "Thêm dòng", "Cộng thuộc tính", "Khóa đồ thú", "Nuôi pet", "Học skill pet", "Nâng cấp Hỏa kỳ lân", "Nghiền bột" };
	private int autoQueueNpc;
	private int[] autoQueueMenu;
	private int[] autoQueueOption;
	private int autoQueuePos;
	private short autoCraftedItemID = (short)-1;
	// (Bỏ: name/selected/index/Limit/start/maxOption/cm*/x/y/w/h/hText/cmdTimban/trans/pa/frame - chỉ phục vụ màn PauseMenu J2ME.)

	private void showCheDoNhanhMenu()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Chế đồ lv20")
		{
			action = () =>
			{
				this.autoStart(0);
			}
		});
		vector.addElement(new Command("Chế đồ lv35 đến 50")
		{
			action = () =>
			{
				this.autoStart(1);
			}
		});
		vector.addElement(new Command(this.heMaPhap ? "Hệ: Ma pháp" : "Hệ: Vật lý")
		{
			action = () =>
			{
				this.heMaPhap = !this.heMaPhap;
				this.showCheDoNhanhMenu();
			}
		});
		vector.addElement(new Command("Nâng cấp tới lv" + this.autoGoalLevel)
		{
			action = () =>
			{
				this.autoGoalLevel = this.autoGoalLevel >= 85 || this.autoGoalLevel < 50 ? 50 : this.autoGoalLevel + 5;
				this.showCheDoNhanhMenu();
			}
		});
		vector.addElement(new Command("Nâng cấp đồ có dòng ẩn")
		{
			action = () =>
			{
				this.autoStartJob(1);
			}
		});
		vector.addElement(new Command("Khóa đồ có dòng ẩn...")
		{
			action = () =>
			{
				this.showKhoaMenu();
			}
		});
		vector.addElement(new Command("Sửa thuộc tính đồ có dòng ẩn...")
		{
			action = () =>
			{
				this.showSuaMenu();
			}
		});
		vector.addElement(new Command("Trang bị thú...")
		{
			action = () =>
			{
				this.showThuMenu();
			}
		});
		vector.addElement(new Command("Thần khí...")
		{
			action = () =>
			{
				this.showThanKhiMenu();
			}
		});
		vector.addElement(new Command("Nâng cấp thần binh...")
		{
			action = () =>
			{
				this.showThanBinhMenu();
			}
		});
		vector.addElement(new Command("Nuôi pet...")
		{
			action = () =>
			{
				this.showNuoiPetMenu(0);
			}
		});
		vector.addElement(new Command("Học skill pet...")
		{
			action = () =>
			{
				this.showHocSkillMenu(0);
			}
		});
		vector.addElement(new Command("Nâng cấp Hỏa kỳ lân...")
		{
			action = () =>
			{
				this.showHklMenu(0);
			}
		});
		vector.addElement(new Command("Nghiền bột...")
		{
			action = () =>
			{
				this.showNghienMenu(0);
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	private void showKhoaMenu()
	{
		mVector vector = new mVector();
		vector.addElement(new Command(this.autoLockGoal == 20 ? "Mức khóa: 20% trở lên" : "Mức khóa: chỉ 21%")
		{
			action = () =>
			{
				this.autoLockGoal = this.autoLockGoal == 20 ? 21 : 20;
				this.showKhoaMenu();
			}
		});
		vector.addElement(new Command("Khóa tất cả đồ có dòng ẩn")
		{
			action = () =>
			{
				this.autoLockTtc = false;
				this.autoStartJob(2);
			}
		});
		vector.addElement(new Command("Khóa đồ TTC có dòng ẩn")
		{
			action = () =>
			{
				this.autoLockTtc = true;
				this.autoStartJob(2);
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	public static void addLuyenCommands(mVector vector)
	{
		if (ScreenDapDo.gI().type != 1)
		{
			return;
		}
		PauseMenu pauseMenu = PauseMenu.gI();
		if (ScreenDapDo.idLkd >= 0)
		{
			pauseMenu.autoLkd = ScreenDapDo.idLkd;
			pauseMenu.autoBh = ScreenDapDo.idBH;
			pauseMenu.autoMm = ScreenDapDo.idMM;
		}
		vector.insertElementAt(new Command("Đập full tất cả đồ...")
		{
			action = () =>
			{
				pauseMenu.showLuyenMenu();
			}
		}, 0);
	}

	private void showLuyenMenu()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Đập tới: Cộng " + this.autoLuyenTarget)
		{
			action = () =>
			{
				this.autoLuyenTarget = this.autoLuyenTarget >= 15 ? 1 : this.autoLuyenTarget + 1;
				this.showLuyenMenu();
			}
		});
		vector.addElement(new Command(this.autoLuyenBuy ? "Tự mua nguyên liệu: Bật" : "Tự mua nguyên liệu: Tắt")
		{
			action = () =>
			{
				this.autoLuyenBuy = !this.autoLuyenBuy;
				this.showLuyenMenu();
			}
		});
		vector.addElement(new Command("Bắt đầu đập full")
		{
			action = () =>
			{
				this.autoStartLuyen();
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	private static string autoPickName(short[] sArray, string[] stringArray, short s)
	{
		for (int i = 0; i < sArray.Length; ++i)
		{
			if (sArray[i] != s) continue;
			return stringArray[i];
		}
		return s.ToString();
	}

	private static short autoPickNext(short[] sArray, short s)
	{
		for (int i = 0; i < sArray.Length; ++i)
		{
			if (sArray[i] != s) continue;
			return sArray[(i + 1) % sArray.Length];
		}
		return sArray[0];
	}

	private void autoStartLuyen()
	{
		this.autoLkdLock = PauseMenu.autoGemCountLock(this.autoLkd, false) < 1 && PauseMenu.autoGemCountLock(this.autoLkd, true) >= 1;
		this.autoBhLock = this.autoBh >= 0 && PauseMenu.autoGemCountLock(this.autoBh, false) < 1 && PauseMenu.autoGemCountLock(this.autoBh, true) >= 1;
		this.autoMmLock = this.autoMm >= 0 && PauseMenu.autoGemCountLock(this.autoMm, false) < 7 && PauseMenu.autoGemCountLock(this.autoMm, true) > PauseMenu.autoGemCountLock(this.autoMm, false);
		mVector vector = ItemInInventory.getAllItem();
		short[] sArray = new short[vector == null ? 0 : vector.size()];
		int n = 0;
		for (int i = 0; i < sArray.Length; ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
			ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
			if (itemTemplate == null || itemTemplate.type == 13 || itemInInventory.isSelling || itemInInventory.plusTemplate >= this.autoLuyenTarget) continue;
			sArray[n++] = itemInInventory.itemID;
		}
		if (n == 0)
		{
			Canvas.startOKDlg("Không có món nào cần đập lên Cộng " + this.autoLuyenTarget + ".");
			return;
		}
		this.autoTargets = new short[n];
		System.Array.Copy(sArray, 0, this.autoTargets, 0, n);
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

	private static int autoGemCountLock(int n, bool bl)
	{
		mVector vector = bl ? MainChar.gemItemKhoa : MainChar.gemItem;
		int n2 = 0;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			GemTemplate gemTemplate = (GemTemplate)vector.elementAt(i);
			if (gemTemplate.id != n) continue;
			n2 += gemTemplate.number;
		}
		return n2;
	}

	private bool autoLuyenBuyNow()
	{
		short[] sArray = new short[] { this.autoLkd, this.autoBh, this.autoMm };
		int[] nArray = new int[] { 100, 100, 600 };
		bool bl = false;
		for (int i = 0; i < 3; ++i)
		{
			if (sArray[i] < 0 || sArray[i] > 10) continue;
			int n = nArray[i] - PauseMenu.autoGemCountLock(sArray[i], false);
			// the server drops (and desyncs on) any client packet of 1024+ bytes: 2 bytes per unit,
			// so buy in chunks of at most 300 units
			while (n > 0)
			{
				int n2 = Math.min(n, 300);
				mVector vector = new mVector();
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

	private static bool autoBagFull()
	{
		return 42 * Canvas.gameScr.mainChar.limItemBag - Canvas.gameScr.mainChar.FullInventory() - (MainChar.gemItemKhoa == null ? 0 : MainChar.gemItemKhoa.size()) <= 0;
	}

	private bool autoLuyenLow()
	{
		if (this.autoLkd >= 0 && this.autoLkd <= 10 && PauseMenu.autoGemCountLock(this.autoLkd, false) < 10)
		{
			return true;
		}
		if (this.autoBh >= 0 && this.autoBh <= 10 && PauseMenu.autoGemCountLock(this.autoBh, false) < 10)
		{
			return true;
		}
		return this.autoMm >= 0 && this.autoMm <= 10 && PauseMenu.autoGemCountLock(this.autoMm, false) < 60;
	}

	private bool autoLuyenShort()
	{
		if (PauseMenu.autoGemCountLock(this.autoLkd, this.autoLkdLock) < 1)
		{
			return true;
		}
		if (this.autoBh >= 0 && PauseMenu.autoGemCountLock(this.autoBh, this.autoBhLock) < 1)
		{
			return true;
		}
		return this.autoMm >= 0 && PauseMenu.autoGemCountLock(this.autoMm, this.autoMmLock) < (this.autoBh >= 0 ? 6 : 7);
	}

	public static bool showThemDongMenu(short s, int n, int n2, int n3, int n4, int n5)
	{
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

	private void showThemMain()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Thêm dòng 1 lần")
		{
			action = () =>
			{
				GameService.gI().doRequestThemThuocTinh(this.autoThemItem, 5, 249, this.autoThemBotLock, 250, this.autoThemDaLock);
			}
		});
		vector.addElement(new Command("Auto thêm dòng (giữ dòng đã chọn)")
		{
			action = () =>
			{
				this.autoStartThem(false);
			}
		});
		vector.addElement(new Command("Auto thêm dòng full (tất cả đồ)")
		{
			action = () =>
			{
				this.autoStartThem(true);
			}
		});
		vector.addElement(new Command("Chọn dòng muốn giữ...")
		{
			action = () =>
			{
				this.showThemPick(0);
			}
		});
		vector.addElement(new Command(this.autoThemBuy ? "Tự mua đá tiến giai: Bật" : "Tự mua đá tiến giai: Tắt")
		{
			action = () =>
			{
				this.autoThemBuy = !this.autoThemBuy;
				this.showThemMain();
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	private void showThemPick(int n)
	{
		mVector vector = new mVector();
		int n2 = this.autoThemWeapon ? 0 : 3;
		int n3 = this.autoThemWeapon ? 3 : 12;
		int n4 = 0;
		for (int i = 12; i < 17; ++i)
		{
			for (int j = 0; j < 2; ++j)
			{
				int n5 = i * 2 + j;
				int n6 = n4++;
				vector.addElement(new Command((this.autoThemChon[n5] ? "(x) " : "( ) ") + TD_NAME[i] + (j == 1 ? " max" : ""))
				{
					action = () =>
					{
						this.autoThemChon[n5] = !this.autoThemChon[n5];
						this.showThemPick(n6);
					}
				});
			}
		}
		vector.addElement(new Command("Xong")
		{
			action = () =>
			{
				this.showThemMain();
			}
		});
		int n7 = Menu.cmy;
		int n8 = Menu.cmtoY;
		Canvas.menu.startAt(vector, 3);
		if (n > 0)
		{
			Canvas.menu.selected = n;
			Menu.cmy = n7;
			Menu.cmtoY = n8;
		}
	}

	private bool autoThemAnyChosen()
	{
		int n = this.autoThemWeapon ? 0 : 3;
		int n2 = this.autoThemWeapon ? 3 : 12;
		for (int i = 24; i < 34; ++i)
		{
			if (!this.autoThemChon[i]) continue;
			return true;
		}
		return false;
	}

	private int autoThemLines(ItemInInventory itemInInventory, bool[] blArray)
	{
		int n = Math.max(0, Math.min(2, (itemInInventory.level - 50) / 10));
		int n2 = 0;
		blArray[0] = false;
		blArray[1] = true;
		for (int i = 0; i < TD_ID.Length; ++i)
		{
			int n3 = PauseMenu.autoAttVal(itemInInventory, TD_ID[i]);
			if (n3 <= 0 || i >= 12 && this.autoThemStart[i - 12] > 0) continue;
			++n2;
			bool bl = i >= 12 && (this.autoThemChon[i * 2] || this.autoThemChon[i * 2 + 1] && n3 >= TD_MAX[i][n]);
			if (bl)
			{
				blArray[0] = true;
				continue;
			}
			blArray[1] = false;
		}
		return n2;
	}

	private bool autoThemNext()
	{
		++this.autoTargetPos;
		if (this.autoTargets == null || this.autoTargetPos >= this.autoTargets.Length)
		{
			return false;
		}
		this.autoThemItem = this.autoTargets[this.autoTargetPos];
		this.autoThemSnap();
		this.autoPhase = 90;
		return true;
	}

	private int autoThemStock(int n, bool bl)
	{
		return PauseMenu.autoGemCountLock(n, bl);
	}

	private void autoThemSnap()
	{
		ItemInInventory itemInInventory = this.autoFindItem(this.autoThemItem);
		for (int i = 0; i < 5; ++i)
		{
			this.autoThemStart[i] = itemInInventory == null ? 0 : PauseMenu.autoAttVal(itemInInventory, 28 + i);
		}
	}

	private void autoStartThem(bool bl)
	{
		if (!this.autoThemAnyChosen())
		{
			Canvas.startOKDlg("Hãy vào \"Chọn dòng muốn giữ...\" để tick dòng ẩn cần giữ trước.");
			return;
		}
		this.autoThemFull = bl;
		if (bl)
		{
			mVector vector = ItemInInventory.getAllItem();
			short[] sArray = new short[vector == null ? 0 : vector.size()];
			int n = 0;
			for (int i = 0; i < sArray.Length; ++i)
			{
				ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
				ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
				if (itemTemplate == null || itemTemplate.type >= 14 && itemTemplate.type <= 18 || itemInInventory.colorName != 1 || itemInInventory.hangItem != 1 || itemInInventory.islock == 0 || itemInInventory.level < 50 || itemInInventory.isSelling) continue;
				sArray[n++] = itemInInventory.itemID;
			}
			if (n == 0)
			{
				Canvas.startOKDlg("Không có món nào thêm dòng được (cần Hoàn mỹ, Nhất phẩm, đã khóa, cấp 50 trở lên).");
				return;
			}
			this.autoTargets = new short[n];
			System.Array.Copy(sArray, 0, this.autoTargets, 0, n);
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

	public static bool showCongTTMenu(short s, int n, int n2)
	{
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

	private void showCongTTMain(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Đập 1 lần")
		{
			action = () =>
			{
				AutoGuard.dapMotLan();
			}
		});
		vector.addElement(new Command("Loại: " + (this.autoCttLoai < 0 ? "Bất kỳ" : CTT_NAME[this.autoCttLoai]))
		{
			action = () =>
			{
				this.autoCttLoai = this.autoCttLoai >= CTT_NAME.Length - 1 ? -1 : this.autoCttLoai + 1;
				this.showCongTTMain(1);
			}
		});
		vector.addElement(new Command("Mức: " + PauseMenu.cttMucText(this.autoCttMin))
		{
			action = () =>
			{
				this.autoCttMin = this.autoCttMin <= 3 ? 5 : this.autoCttMin - 1; // chỉ 0.5 | 0.4-0.5 | 0.3-0.5
				this.showCongTTMain(2);
			}
		});
		vector.addElement(new Command("Auto món đang đặt")
		{
			action = () =>
			{
				this.autoStartCongTT(false);
			}
		});
		vector.addElement(new Command("Auto tất cả đồ")
		{
			action = () =>
			{
				this.autoStartCongTT(true);
			}
		});
		PauseMenu.moMenuGiuChon(vector, 3, chon);
	}

	public static bool showKhoaThuMenu(short s, int n, int n2)
	{
		PauseMenu pauseMenu = PauseMenu.gI();
		pauseMenu.autoKtItem = s;
		pauseMenu.autoKtDa = (short)n;
		pauseMenu.autoKtLock = n2 == 1;
		pauseMenu.showKhoaThuMain(0);
		Canvas.menu.selected = -1; // như showCongTTMenu
		return true;
	}

	private void showKhoaThuMain(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Khóa 1 lần")
		{
			action = () =>
			{
				AutoGuard.dapMotLan();
			}
		});
		vector.addElement(new Command("Mức: " + (this.autoKtMuc >= 21 ? "21%" : "20-21%"))
		{
			action = () =>
			{
				this.autoKtMuc = this.autoKtMuc >= 21 ? 20 : 21;
				this.showKhoaThuMain(1);
			}
		});
		vector.addElement(new Command("Auto khóa món đang đặt")
		{
			action = () =>
			{
				this.autoStartKhoaThu(false);
			}
		});
		vector.addElement(new Command("Auto khóa tất cả")
		{
			action = () =>
			{
				this.autoStartKhoaThu(true);
			}
		});
		PauseMenu.moMenuGiuChon(vector, 3, chon);
	}

	private static int ktGiaTri(ItemInInventory itemInInventory)
	{
		return Math.max(PauseMenu.autoAttVal(itemInInventory, 58), Math.max(PauseMenu.autoAttVal(itemInInventory, 59), PauseMenu.autoAttVal(itemInInventory, 60)));
	}

	private bool ktDat(ItemInInventory itemInInventory)
	{
		return itemInInventory != null && PauseMenu.ktGiaTri(itemInInventory) >= this.autoKtMuc;
	}

	/** Đồ thú Hoàn mỹ Nhất phẩm (đúng danh sách màn Khóa đồ thú), không đang bán, chưa đạt. */
	private bool ktCanKhoa(ItemInInventory itemInInventory)
	{
		return itemInInventory.colorName == 1 && itemInInventory.hangItem == 1 && ItemInInventory.isAnimalArmor(itemInInventory.getTemplate().type) && !itemInInventory.isSelling && !this.ktDat(itemInInventory);
	}

	private void autoStartKhoaThu(bool bl)
	{
		if (this.autoKtDa != KT_XUONG_CAP6)
		{
			Canvas.startOKDlg("Chỉ xương cấp 6 mới khóa ra được 20-21%. Hãy đặt xương cấp 6.");
			return;
		}
		ItemInInventory itemInInventory = this.autoFindItem(this.autoKtItem);
		if (!bl && itemInInventory == null)
		{
			Canvas.startOKDlg("Không thấy món đồ trong hành trang.");
			return;
		}
		this.autoKtFull = bl;
		if (bl)
		{
			// món đang đặt (nếu còn cần khóa) làm trước, rồi lần lượt các món còn lại
			mVector vector = Char.inventory;
			short[] sArray = new short[(vector == null ? 0 : vector.size()) + 1];
			int n = 0;
			if (itemInInventory != null && this.ktCanKhoa(itemInInventory))
			{
				sArray[n++] = itemInInventory.itemID;
			}
			for (int i = 0; vector != null && i < vector.size(); ++i)
			{
				ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
				if (itemInInventory2 == itemInInventory || !this.ktCanKhoa(itemInInventory2)) continue;
				sArray[n++] = itemInInventory2.itemID;
			}
			if (n == 0)
			{
				Canvas.startOKDlg("Không có đồ thú Hoàn mỹ Nhất phẩm nào cần khóa tới " + (this.autoKtMuc >= 21 ? "21%" : "20-21%") + ".");
				return;
			}
			this.autoTargets = new short[n];
			System.Array.Copy(sArray, 0, this.autoTargets, 0, n);
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
	private bool autoKtNext()
	{
		while (this.autoTargets != null && ++this.autoTargetPos < this.autoTargets.Length)
		{
			ItemInInventory itemInInventory = this.autoFindItem(this.autoTargets[this.autoTargetPos]);
			if (itemInInventory == null || this.ktDat(itemInInventory)) continue;
			this.autoKtItem = this.autoTargets[this.autoTargetPos];
			this.autoPhase = 97;
			return true;
		}
		return false;
	}

	private string ktTongKet()
	{
		return "Auto khóa tất cả đồ thú: xong " + this.autoFound + "/" + (this.autoTargets == null ? 0 : this.autoTargets.Length) + " món";
	}

	private void showNuoiPetMenu(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command(this.autoPetTuHon ? "Thức ăn: Tụ hồn đan (" + PauseMenu.tuHonDanCo() + ")" : "Thức ăn: Đồ chế")
		{
			action = () =>
			{
				this.autoPetTuHon = !this.autoPetTuHon;
				this.showNuoiPetMenu(0);
			}
		});
		if (!this.autoPetTuHon)
		{
			vector.addElement(new Command("Loại nguyên liệu: " + (this.autoPetLoai < 0 ? "Tất cả" : PET_LOAI_TEN[this.autoPetLoai]))
			{
				action = () =>
				{
					this.autoPetLoai = this.autoPetLoai >= PET_LOAI_TEN.Length - 1 ? -1 : this.autoPetLoai + 1;
					this.showNuoiPetMenu(1);
				}
			});
			vector.addElement(new Command("Nguyên liệu sơ cấp: cấp " + (this.autoPetCapSo + 1))
			{
				action = () =>
				{
					this.autoPetCapSo = (this.autoPetCapSo + 1) % 6;
					this.showNuoiPetMenu(2);
				}
			});
			vector.addElement(new Command("Nguyên liệu cao cấp: cấp " + (this.autoPetCapNL + 1))
			{
				action = () =>
				{
					this.autoPetCapNL = (this.autoPetCapNL + 1) % 6;
					this.showNuoiPetMenu(3);
				}
			});
		}
		vector.addElement(new Command("Bắt đầu nuôi pet")
		{
			action = () =>
			{
				this.autoStartNuoiPet();
			}
		});
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	private static int tuHonDanCo()
	{
		MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
		return mainChar == null || mainChar.potions == null || mainChar.potions.Length <= POTION_TU_HON_DAN ? 0 : mainChar.potions[POTION_TU_HON_DAN];
	}

	/** Sức ăn tốn nhiều nhất cho 1 miếng: Tụ hồn đan 1, đồ chế theo cấp nguyên liệu cao cấp. */
	private int petAnTon()
	{
		return this.autoPetTuHon ? 1 : PET_AN_TON[this.autoPetCapNL];
	}

	private static string petInfo()
	{
		if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null || Canvas.gameScr.mainChar.myPet == null)
		{
			return null;
		}
		return Canvas.gameScr.mainChar.myPet.infoPet;
	}

	/** Đọc số nguyên ngay sau "nhan" trong chuỗi thông tin pet, -1 nếu không có. */
	private static int petDocSo(string @string, string string2)
	{
		if (@string == null)
		{
			return -1;
		}
		int n = @string.IndexOf(string2, System.StringComparison.Ordinal);
		if (n < 0)
		{
			return -1;
		}
		n += string2.Length;
		while (n < @string.Length && @string[n] == ' ')
		{
			++n;
		}
		int n2 = n;
		while (n2 < @string.Length && @string[n2] >= '0' && @string[n2] <= '9')
		{
			++n2;
		}
		if (n2 == n)
		{
			return -1;
		}
		return int.Parse(@string.Substring(n, n2 - n));
	}

	/** "- Sức ăn : 120/300" -> 120. */
	private static int petSucAn()
	{
		return PauseMenu.petDocSo(PauseMenu.petInfo(), "Sức ăn :");
	}

	/** "- Cấp độ: 9+99.9%" -> 9. */
	private static int petCap()
	{
		return PauseMenu.petDocSo(PauseMenu.petInfo(), "Cấp độ:");
	}

	/** "- Cấp độ: 9+99.9%" -> 999 (phần nghìn). */
	private static int petPhanTram()
	{
		string @string = PauseMenu.petInfo();
		int n = PauseMenu.petDocSo(@string, "Cấp độ:");
		if (n < 0)
		{
			return -1;
		}
		int n2 = @string.IndexOf("Cấp độ:", System.StringComparison.Ordinal);
		int n3 = @string.IndexOf('+', n2);
		int n4 = n3 < 0 ? -1 : @string.IndexOf('.', n3);
		if (n3 < 0 || n4 < 0)
		{
			return -1;
		}
		try
		{
			return int.Parse(@string.Substring(n3 + 1, n4 - (n3 + 1)).Trim()) * 10 + int.Parse(@string.Substring(n4 + 1, 1));
		}
		catch (System.Exception)
		{
			return -1;
		}
	}

	/** Pet đứng ở 9/19/29/39 + 99.9%: server không cho ăn tới khi nâng cấp. */
	/** Mốc 49.99% (từ cấp 40 trở lên): không nâng cấp ở NPC được, phải tiến hoá. */
	private static bool petCanTienHoa()
	{
		int n = PauseMenu.petCap();
		return n >= 40 && (n + 1) % 10 == 0 && PauseMenu.petPhanTram() >= 999;
	}

	private static bool petCanNang()
	{
		int n = PauseMenu.petCap();
		return n >= 0 && n < 40 && (n + 1) % 10 == 0 && PauseMenu.petPhanTram() >= 999;
	}

	/** Tên món trong danh sách "Cho pet ăn" - y hệt Item.getNameItemPetEat ở server. */
	private static string petTenMonAn(ItemInInventory itemInInventory)
	{
		ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
		if (itemTemplate == null)
		{
			return null;
		}
		string @string = itemTemplate.name;
		string[] stringArray = new string[] { "ma pháp", "vậy lý" };
		if (itemInInventory.magic_physic >= 0 && itemInInventory.magic_physic < 2)
		{
			@string = @string + " " + stringArray[itemInInventory.magic_physic];
		}
		string[] stringArray2 = new string[] { "", "Hoàn mỹ", "Đỏ", "Xanh" };
		if (itemInInventory.colorName > 0 && itemInInventory.colorName < 4)
		{
			@string = @string + " " + stringArray2[itemInInventory.colorName];
		}
		string[] stringArray3 = new string[] { "", "Nhất phẩm", "Nhị phẩm", "Tam phẩm", "Tứ phẩm", "NGũ phẩm" };
		if (itemInInventory.hangItem > 1 && itemInInventory.hangItem < 6)
		{
			@string = @string + " " + stringArray3[itemInInventory.hangItem];
		}
		if (itemInInventory.plusTemplate > 0)
		{
			@string = @string + " +" + itemInInventory.plusTemplate;
		}
		return @string;
	}

	/** Bỏ khỏi lô các món không còn trong hành trang (đã ăn); trả số món còn lại. */
	private int petConLai()
	{
		for (int i = this.autoPetLo.size() - 1; i >= 0; --i)
		{
			if (this.autoFindItem((short)(int)this.autoPetLo.elementAt(i)) != null) continue;
			this.autoPetLo.removeElementAt(i);
		}
		return this.autoPetLo.size();
	}

	private bool petTrongLo(short s)
	{
		for (int i = 0; i < this.autoPetLo.size(); ++i)
		{
			if ((int)this.autoPetLo.elementAt(i) != s) continue;
			return true;
		}
		return false;
	}

	/** Thêm vào lô các món mới xuất hiện so với snapshot (phase 7 / 115); trả số món mới. */
	private int petMonMoi()
	{
		mVector vector = Char.inventory;
		int n = 0;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
			bool coTrongSnapshot = false; // thay cho "continue block0" của Java
			for (int j = 0; this.autoSnapshot != null && j < this.autoSnapshot.Length; ++j)
			{
				if (this.autoSnapshot[j] == itemInInventory.itemID)
				{
					coTrongSnapshot = true;
					break;
				}
			}
			if (coTrongSnapshot) continue;
			++n;
			if (this.petTrongLo(itemInInventory.itemID)) continue;
			this.autoPetLo.addElement((int)itemInInventory.itemID);
		}
		return n;
	}

	/** Xin danh sách "Cho pet ăn" (NPC -16 mục 1) và ghi nhận đang chờ thêm 1 danh sách. */
	private void xinDsAn()
	{
		if (this.autoPetDsCho < this.autoPetDsNhan)
		{
			this.autoPetDsCho = this.autoPetDsNhan;
		}
		++this.autoPetDsCho;
		GameService.gI().doMenuOption(-16, (sbyte)0, 1);
	}

	/**
	 * Bắt đầu lượt mới: bỏ qua các lần xin cũ chưa được trả lời (vd NPC không nhận lần trước) -
	 * nếu không bộ đếm cộng dồn và lượt sau chờ mãi một danh sách không bao giờ tới.
	 */
	private void batDauDs()
	{
		this.autoPetDsCho = this.autoPetDsNhan;
		this.autoPetDsAn = null;
	}

	/** Đã nhận đủ các danh sách đã xin (đang cầm danh sách mới nhất). */
	private bool dsSan()
	{
		return this.autoPetDsAn != null && this.autoPetDsNhan >= this.autoPetDsCho;
	}

	/** Chế được ngay bao nhiêu món theo công thức (sức ăn, ô trống, xu, nguyên liệu), tối đa n. */
	private int petCheDuoc(int n)
	{
		int n2 = PauseMenu.petSucAn();
		int n3 = Math.min(n, (this.autoPetSauRelog ? 300 : 300 - Math.max(0, n2)) / this.petAnTon());
		n3 = Math.min(n3, PauseMenu.petOTrong());
		n3 = Math.min(n3, (int)(Canvas.gameScr.mainChar.charXu / 5000L));
		for (int i = 0; i < this.autoPetFillMau.Length; ++i)
		{
			for (int j = 0; j < this.autoPetFillMau[i].Length; ++j)
			{
				if (this.autoPetFillMau[i][j] <= 0) continue;
				int n4 = PauseMenu.autoGemCountLock(this.autoPetNLId[i] + j, false) + PauseMenu.autoGemCountLock(this.autoPetNLId[i] + j, true);
				n3 = Math.min(n3, n4 / this.autoPetFillMau[i][j]);
			}
		}
		return n3;
	}

	/** Gửi liền n lần "menu NPC -8 + chế" theo công thức (snapshot trước để nhận ra món mới). */
	private void petGuiChe(int n)
	{
		this.autoSnapshot = this.autoInventoryIds();
		for (int i = 0; i < n; ++i)
		{
			for (int j = 0; j < this.autoPetQMenu.Length; ++j)
			{
				GameService.gI().doMenuOption(this.autoPetQNpc, (sbyte)this.autoPetQMenu[j], this.autoPetQOpt[j]);
			}
			GameService.gI().doCheDo((short)this.autoPetFillMau.Length, this.autoPetIdTemp, this.autoPetFillMau);
		}
	}

	private static int petOTrong()
	{
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
	private int petTinhLo()
	{
		ItemInInventory itemInInventory = this.autoFindItem((short)(int)this.autoPetLo.elementAt(0));
		string @string = itemInInventory == null ? null : PauseMenu.petTenMonAn(itemInInventory);
		if (@string == null || this.autoPetDsAn == null)
		{
			return -1;
		}
		int n = 0;
		mVector vector = Char.inventory;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
			if (!(@string == PauseMenu.petTenMonAn(itemInInventory2))) continue;
			if (!this.petTrongLo(itemInInventory2.itemID))
			{
				// đồ cùng tên nhưng không do lô này chế (vd còn từ lần chạy trước): cấp 20/21
				// đúng là đồ chế để cho ăn -> gộp vào lô cho ăn luôn (người chơi yêu cầu);
				// cấp cao hơn là đồ thật -> không chắc, dừng
				if (itemInInventory2.level > 21)
				{
					return -1;
				}
				this.autoPetLo.addElement((int)itemInInventory2.itemID);
			}
			++n;
		}
		int[] nArray = new int[this.autoPetDsAn.Length];
		int n2 = 0;
		for (int i = 0; i < this.autoPetDsAn.Length; ++i)
		{
			if (!(@string == this.autoPetDsAn[i])) continue;
			nArray[n2++] = i;
		}
		if (n2 != n || n2 == 0)
		{
			return -1;
		}
		int n3 = nArray[0];
		if (n3 > 127)
		{
			return -1;
		}
		int n4 = 1;
		while (n4 < n2 && nArray[n4] == n3 + n4)
		{
			++n4;
		}
		return n3 * 256 + n4;
	}

	/** Gửi m lệnh cho ăn, đều ở vị trí p0 (kết quả của petTinhLo). */
	private void petGuiAn(int n)
	{
		int n2 = n / 256;
		int n3 = n % 256;
		for (int i = 0; i < n3; ++i)
		{
			if (i > 0)
			{
				this.xinDsAn();
			}
			GameService.gI().doMenuOption(-16, (sbyte)1, n2);
		}
	}

	/** Cấp đã chọn cho 1 loại nguyên liệu: sơ cấp (id < 103) hay cao cấp. */
	private int petCapCua(int n)
	{
		if (this.autoJob == 14)
		{
			return n < 103 ? this.autoNbCapSo : this.autoNbCapCao;
		}
		return n < 103 ? this.autoPetCapSo : this.autoPetCapNL;
	}

	/** Cặp nguyên liệu đang chọn của job đang chạy (Nuôi pet / Nghiền bột). */
	private int nlLoai()
	{
		return this.autoJob == 14 ? this.autoNbLoai : this.autoPetLoai;
	}

	/** Lọc đúng 2 nguyên liệu của món ở cấp đã chọn; thiếu thì null (thử món khác). */
	private sbyte[][] petFillCap()
	{
		WindowInfoScr windowInfoScr = WindowInfoScr.gI();
		mVector vector = windowInfoScr.listCheDo;
		mVector vector2 = windowInfoScr.listTemp;
		if (vector == null || vector.size() == 0 || vector2 == null)
		{
			return null;
		}
		// món này không dùng đúng cặp nguyên liệu đã chọn -> null (thử món kế tiếp)
		for (int i = 0; this.nlLoai() >= 0 && i < vector.size(); ++i)
		{
			int n = ((Mineral)vector.elementAt(i)).idTemplate;
			if (n == PET_LOAI_SO[this.nlLoai()] || n == PET_LOAI_CAO[this.nlLoai()]) continue;
			return null;
		}
		sbyte[][] byArray = new sbyte[vector.size()][];
		for (int k = 0; k < byArray.Length; ++k)
		{
			byArray[k] = new sbyte[6]; // Java: new byte[size][6]
		}
		for (int i = 0; i < vector.size(); ++i)
		{
			Mineral mineral = (Mineral)vector.elementAt(i);
			int n = 0;
			for (int j = 0; j < vector2.size(); ++j)
			{
				GemTemplate gemTemplate = (GemTemplate)vector2.elementAt(j);
				if (gemTemplate.id != mineral.idTemplate + this.petCapCua(mineral.idTemplate)) continue;
				n += gemTemplate.number;
			}
			if (n < mineral.number)
			{
				return null;
			}
			byArray[i][this.petCapCua(mineral.idTemplate)] = (sbyte)mineral.number;
		}
		return byArray;
	}

	private void autoStartNuoiPet()
	{
		if (PauseMenu.petInfo() == null || PauseMenu.petCap() < 0)
		{
			Canvas.startOKDlg("Bạn phải mang theo pet.");
			return;
		}
		if (PauseMenu.petCanTienHoa())
		{
			Canvas.startOKDlg("Pet đã đạt 49.99%, cần tiến hoá (dùng Huyết linh thảo) mới luyện tiếp được.");
			return;
		}
		if (this.autoPetTuHon && PauseMenu.tuHonDanCo() <= 0)
		{
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
}
