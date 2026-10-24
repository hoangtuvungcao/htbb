package com.htbb.pokemon;

/**
 * MO TA VAT PHAM DANG "THUOC" (potions[] / data_potion, 0.9.14): loai hien tren the vat pham, mo ta hieu ung THAT (doc tu
 * ma xu ly: real/doUsePotion.java, Char.setX2 / updateGoldTime, ExpLauDai, ThuCuoiTuTui...), dung duoc tu Hanh trang
 * hay khong (va ly do neu khong).
 *
 * <p>Truoc day client ve MOI thuoc la "Binh thuoc / Hoi HP cho Pokemon. Gan vao nut thuoc HP..." (TheVatPham.loai /
 * moTa cung trong ma) - Sao goi Tauros, ve gio vang, hop qua, nguyen lieu su kien deu ra chu do. Bang nay la nguon duy
 * nhat: may chu gui xuong client &gt;= {@link #BAN_CLIENT} (goi -104 kieu {@link #KIEU_GOI}, real.MoTaVatPhamGame) va
 * DungVatPham.dungThuoc dung {@link #dungDuoc} / {@link #lyDo} de tu choi (KHONG tru) vat pham khong dung tu tui duoc -
 * truoc: vai id roi vao nhanh default cua doUsePotion, bi tru mat ma khong co tac dung (Ve tau, La vang, Ve quay so dac
 * biet, hoa Vu Lan...).
 *
 * <p>Lop THUAN (khong CSDL). test/KiemMoTaVatPham kiem: moi id data_potion co dong, chu HP/PP chi o binh HP/PP, vat
 * pham co thoi han ghi ro thoi gian, khong co id dung duoc ma ma xu ly tra lai "Khong the su dung".
 */
public final class MoTaVatPham {

    /** Ban client dau tien doc bang mo ta (goi -104 kieu 9). */
    public static final int BAN_CLIENT = 914;
    /** Kieu goi -104 mang bang mo ta (client &lt; 914 khong duoc gui). */
    public static final int KIEU_GOI = 9;

    public static final String BINH = "Bình thuốc";
    public static final String THU_CUOI = "Vật phẩm gọi thú cưỡi";
    public static final String TRUNG = "Trứng thú cưỡi";
    public static final String VE = "Vé / thẻ";
    public static final String TANG_EXP = "Vật phẩm tăng kinh nghiệm";
    public static final String TANG_CUONG = "Vật phẩm tăng cường";
    public static final String HOP = "Hộp quà";
    public static final String NGUYEN_LIEU = "Nguyên liệu sự kiện";
    public static final String TANG = "Quà tặng";
    public static final String NGOAI_HINH = "Đổi kiểu lông";
    public static final String THU = "Đồ cho thú";
    public static final String CHIEN_TRUONG = "Vật phẩm Chiến trường";
    public static final String GIU = "Vật phẩm giữ trong túi";
    public static final String NGUNG = "Vật phẩm cũ";

    /** Binh hoi HP (nut thuoc HP o HUD). */
    public static final int[] HP = {1, 2, 3, 21, 22, 93, 94};
    /** Binh hoi PP (nut thuoc PP o HUD). */
    public static final int[] PP = {4, 5, 6, 23, 24, 95, 96};

    /**
     * Ly do "khong dung tu tui": BUOC TIEP truoc (de doc ca khi the vat pham hep / phong J2ME cat chu), roi "(không dùng
     * trực tiếp từ túi)". VD "dùng ở NPC quay số." -&gt; "Dùng ở NPC quay số (không dùng trực tiếp từ túi)."
     */
    static String buoc(final String b) {
        String t = b.trim();
        while (t.endsWith(".")) {
            t = t.substring(0, t.length() - 1).trim();
        }
        return t.isEmpty() ? "Không dùng trực tiếp từ túi." : Character.toUpperCase(t.charAt(0)) + t.substring(1) + " (không dùng trực tiếp từ túi).";
    }
    private static final String CT = " Chỉ dùng trong Chiến trường.";

    /** {id, loai, mo ta, "1" dung tu tui / "0" khong, ly do khi "0"}. */
    static final String[][] BANG = {
        {"1", BINH, "Hồi 80 HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.", "1", ""},
        {"2", BINH, "Hồi 300 HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.", "1", ""},
        {"3", BINH, "Hồi 1.000 HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.", "1", ""},
        {"4", BINH, "Hồi 160 PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.", "1", ""},
        {"5", BINH, "Hồi 600 PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.", "1", ""},
        {"6", BINH, "Hồi 2.000 PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.", "1", ""},
        {"7", TANG_CUONG, "Rút ngắn thời gian chín của cây trồng (trái thường).", "0", buoc("dùng khi chăm cây ở vườn.")},
        {"8", TANG_CUONG, "Giảm 20% thời gian chín của trái vàng.", "0", buoc("dùng khi chăm cây trái vàng ở vườn.")},
        {"9", TANG_EXP, "Cộng ngay 1% kinh nghiệm của cấp hiện tại. Chỉ dùng khi dưới cấp 90.", "1", ""},
        {"10", NGUNG, "Kẹo kinh nghiệm cũ (cộng 100.000 EXP) - đã ngừng dùng.", "0", "Đã ngừng dùng: đổi lấy Kẹo hiếm ở Sổ tay huấn luyện (Cửa hàng và đổi vật phẩm)."},
        {"11", NGUNG, "Kẹo kinh nghiệm cũ (cộng 500.000 EXP) - đã ngừng dùng.", "0", "Đã ngừng dùng: đổi lấy Kẹo hiếm ở Sổ tay huấn luyện (Cửa hàng và đổi vật phẩm)."},
        {"12", NGUNG, "Kẹo kinh nghiệm cũ (cộng 1.000.000 EXP) - đã ngừng dùng.", "0", "Đã ngừng dùng: đổi lấy Kẹo hiếm ở Sổ tay huấn luyện (Cửa hàng và đổi vật phẩm)."},
        {"13", TANG_CUONG, "Giảm 20% thời gian chín của trái bạc.", "0", buoc("dùng khi chăm cây trái bạc ở vườn.")},
        {"14", VE, "Khăn PK đỏ: bật / tắt chế độ PK.", "1", ""},
        {"15", VE, "Khăn PK xanh: bật / tắt chế độ PK.", "1", ""},
        {"16", VE, "Khăn PK vàng: bật / tắt chế độ PK.", "1", ""},
        {"17", VE, "Khăn PK xanh lá: bật / tắt chế độ PK.", "1", ""},
        {"18", VE, "Khăn PK tím: bật / tắt chế độ PK.", "1", ""},
        {"19", VE, "Thoát về làng của vùng mình. Không dùng được trong làng, khi đang vận chuyển hàng hoặc ở Núi châu báu.", "1", ""},
        {"20", VE, "Vé đi tàu: tự trừ 1 vé khi lên tàu ở bến.", "0", buoc("tự trừ khi lên tàu ở bến.")},
        {"21", BINH, "Hồi 1.500 HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.", "1", ""},
        {"22", BINH, "Hồi 3.000 HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.", "1", ""},
        {"23", BINH, "Hồi 2.500 PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.", "1", ""},
        {"24", BINH, "Hồi 3.500 PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.", "1", ""},
        {"25", TANG_CUONG, "Trả lại toàn bộ điểm tiềm năng đã cộng để cộng lại từ đầu.", "1", ""},
        {"26", TANG_CUONG, "Trả lại toàn bộ điểm kỹ năng đã cộng để cộng lại từ đầu.", "1", ""},
        {"27", VE, "Bay tức thời tới chỗ một Pokémon đang bị truy nã trong vùng của mình (chọn trong danh sách).", "1", ""},
        {"28", VE, "Cấm chat một người chơi. Chỉ quan chức vùng dùng được.", "1", ""},
        {"29", VE, "Giảm 100 điểm PK.", "1", ""},
        {"30", THU_CUOI, "Cưỡi Ponyta ngay: chạy nhanh hơn, +700 HP và +700 PP tối đa khi đang cưỡi (giữ cả khi thoát game, đến lúc xuống thú). Mỗi lần cưỡi dùng 1 sáo.", "1", ""},
        {"31", VE, "Huy hiệu dùng để tạo đội.", "0", buoc("dùng khi tạo đội ở NPC.")},
        {"32", VE, "Thẻ vào đấu trường cũ.", "0", "Đấu trường cũ đang bảo trì."},
        {"33", VE, "Thẻ xác nhận chiếm Gym.", "0", buoc("dùng khi đăng ký chiếm Gym.")},
        {"34", THU_CUOI, "Cưỡi Ponyta lửa ngay: chạy nhanh hơn, +1.000 HP, +1.000 PP tối đa và +3 điểm mọi tiềm năng khi đang cưỡi (giữ cả khi thoát game, đến lúc xuống thú). Mỗi lần cưỡi dùng 1 sáo.", "1", ""},
        {"35", TANG_EXP, "Tăng 100% kinh nghiệm đánh Pokémon hoang dã trong 60 phút chơi (chỉ đếm lúc online, không áp dụng trong phó bản). Dùng vé giờ vàng khác sẽ thay vé đang chạy, không cộng dồn.", "1", ""},
        {"36", NGUNG, "Sáo gọi Stantler cũ - đã ngừng.", "0", "Vật phẩm cũ đã ngừng dùng."},
        {"37", NGOAI_HINH, "Đổi kiểu lông nam: Lông đinh. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"38", NGOAI_HINH, "Đổi kiểu lông nữ: Lông búi. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"39", NGOAI_HINH, "Đổi kiểu lông nam: Lông cột cao. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"40", NGOAI_HINH, "Đổi kiểu lông nữ: Lông chéo. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"41", NGOAI_HINH, "Đổi kiểu lông nam: Lông xù. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"42", NGOAI_HINH, "Đổi kiểu lông nữ: Lông ngang vai. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"43", NGOAI_HINH, "Đổi kiểu lông nam: Lông đinh so le. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"44", NGOAI_HINH, "Đổi kiểu lông nữ: Lông so le. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"45", NGOAI_HINH, "Đổi kiểu lông nam: Lông gọn gàng. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"46", NGOAI_HINH, "Đổi kiểu lông nữ: Lông cài hoa. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"47", NGOAI_HINH, "Đổi kiểu lông nam: Bờm Ponyta. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"48", NGOAI_HINH, "Đổi kiểu lông nữ: Lông Clefairy. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"49", NGOAI_HINH, "Đổi kiểu lông nam: Lông dựng đứng. Bạn đang mang hình Pokémon nên kiểu lông chỉ thấy ở ảnh đại diện cũ.", "1", ""},
        {"50", TANG, "Tặng 2 bông hoa mai cho người chơi khác (nhập tên người nhận); cả hai thấy mưa hoa.", "1", ""},
        {"51", TANG, "Tặng bó 9 hoa mai cho người chơi khác (nhập tên người nhận); cả hai thấy mưa hoa.", "1", ""},
        {"52", TANG, "Tặng bó 39 hoa mai cho người chơi khác (nhập tên người nhận); cả máy chủ thấy lời chúc.", "1", ""},
        {"53", TANG, "Tặng bó 99 hoa mai cho người chơi khác (nhập tên người nhận); cả máy chủ thấy lời chúc.", "1", ""},
        {"54", TANG, "Tặng 1 bông hoa đào cho người chơi khác (nhập tên người nhận); cả hai thấy mưa hoa.", "1", ""},
        {"55", TANG, "Tặng bó 9 hoa đào cho người chơi khác (nhập tên người nhận); cả hai thấy mưa hoa.", "1", ""},
        {"56", TANG, "Tặng bó 39 hoa đào cho người chơi khác (nhập tên người nhận); cả máy chủ thấy lời chúc.", "1", ""},
        {"57", TANG, "Tặng bó 99 hoa đào cho người chơi khác (nhập tên người nhận); cả máy chủ thấy lời chúc.", "1", ""},
        {"58", TANG, "Tặng 1 bông hoa hồng cho người chơi khác (nhập tên người nhận); cả hai thấy mưa hoa.", "1", ""},
        {"59", TANG, "Tặng bó 9 hoa hồng cho người chơi khác (nhập tên người nhận); cả hai thấy mưa hoa.", "1", ""},
        {"60", TANG, "Tặng bó 39 hoa hồng cho người chơi khác (nhập tên người nhận); cả máy chủ thấy lời chúc.", "1", ""},
        {"61", TANG, "Tặng bó 99 hoa hồng cho người chơi khác (nhập tên người nhận); cả máy chủ thấy lời chúc.", "1", ""},
        {"62", NGUNG, "Vật phẩm Halloween cũ - đã ngừng.", "0", "Vật phẩm cũ đã ngừng dùng."},
        {"63", HOP, "Mở ra: cộng ngay 7.463.228 EXP.", "1", ""},
        {"64", THU_CUOI, "Dùng để nhận thú cưỡi Tauros vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất sáo.", "1", ""},
        {"65", THU_CUOI, "Dùng để nhận thú cưỡi Arcanine vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất sáo.", "1", ""},
        {"66", THU_CUOI, "Dùng để nhận thú cưỡi Mightyena vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất sáo.", "1", ""},
        {"67", THU_CUOI, "Dùng để nhận thú cưỡi Pidgeot vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất sáo.", "1", ""},
        {"68", THU_CUOI, "Dùng để nhận thú cưỡi Rapidash vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất sáo.", "1", ""},
        {"69", VE, "Vé quay số thường.", "0", buoc("dùng ở NPC quay số.")},
        {"70", TRUNG, "Dùng để nở thú cưỡi Articuno vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất trứng.", "1", ""},
        {"71", TRUNG, "Mở ra: nhận 1 thú cưỡi dạng vật phẩm vào hành trang. Cần 1 ô trống.", "1", ""},
        {"72", TRUNG, "Dùng để nở thú cưỡi Piloswine vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất trứng.", "1", ""},
        {"73", GIU, "Có túi trong hành trang thì đánh vỡ được ngọc rơi trên bản đồ để nhặt.", "0", "Không cần bấm dùng: chỉ cần có trong túi."},
        {"74", TRUNG, "Dùng để nở thú cưỡi Spectrier vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất trứng.", "1", ""},
        {"75", TANG_EXP, "Tăng 100% kinh nghiệm đánh Pokémon hoang dã trong 180 phút chơi (chỉ đếm lúc online, không áp dụng trong phó bản). Dùng vé giờ vàng khác sẽ thay vé đang chạy, không cộng dồn.", "1", ""},
        {"76", VE, "Vé quay số đặc biệt.", "0", buoc("dùng ở NPC quay số.")},
        {"77", NGUYEN_LIEU, "Mảnh bản đồ 1: ghép đủ 3 mảnh để đổi bản đồ kho báu.", "0", buoc("ghép mảnh ở NPC sự kiện.")},
        {"78", VE, "Chọn vị trí để dựng Liên hoa cho cả đội.", "1", ""},
        {"79", HOP, "Mở ra: cộng ngay 100.000 EXP.", "1", ""},
        {"80", TANG_CUONG, "Tăng 5% các chỉ số trong 3 ngày (tính theo giờ thực, kể cả lúc offline). Dùng lại sẽ đặt lại 3 ngày, không cộng dồn.", "1", ""},
        {"81", TANG_EXP, "Tăng 150% kinh nghiệm đánh Pokémon hoang dã trong 60 phút chơi (chỉ đếm lúc online, không áp dụng trong phó bản). Dùng vé giờ vàng khác sẽ thay vé đang chạy, không cộng dồn.", "1", ""},
        {"82", VE, "Thêm 1 lượt giao hàng trong ngày (tối đa 5 lượt / ngày).", "1", ""},
        {"83", THU, "Kẹo cho thú nuôi ăn: tăng 1% kinh nghiệm thú nuôi.", "0", buoc("cho ăn ở mục Thú nuôi.")},
        {"84", TANG_CUONG, "Nhân đôi tấn công, phòng thủ, chính xác, chí mạng, né trong 60 giây. Không dùng khi đang có hiệu ứng Thuốc X.", "1", ""},
        {"85", VE, "Triệu hồi thành viên đội tới chỗ mình (tối đa 2 lần / ngày).", "1", ""},
        {"86", THU_CUOI, "Dùng để nhận thú cưỡi Moltres vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất sáo.", "1", ""},
        {"87", VE, "Phong chức cho người chơi khác. Chỉ nhà vô địch vùng hoặc cố vấn vùng dùng được.", "1", ""},
        {"88", GIU, "Nhẫn kết đôi đồng: có trong túi khi đã kết đôi thì tăng 2% phòng thủ (chỉ tính 1 nhẫn, không cộng dồn).", "0", "Không cần bấm dùng: chỉ cần có trong túi khi đã kết đôi."},
        {"89", GIU, "Nhẫn kết đôi bạc: có trong túi khi đã kết đôi thì tăng 4% phòng thủ (chỉ tính 1 nhẫn, không cộng dồn).", "0", "Không cần bấm dùng: chỉ cần có trong túi khi đã kết đôi."},
        {"90", GIU, "Nhẫn kết đôi vàng: có trong túi khi đã kết đôi thì tăng 7% phòng thủ (chỉ tính 1 nhẫn, không cộng dồn).", "0", "Không cần bấm dùng: chỉ cần có trong túi khi đã kết đôi."},
        {"91", VE, "Vào khu mỏ đào nguyên liệu. Cần cấp 20, dùng ở làng hoặc bản đồ luyện quái của vùng mình.", "1", ""},
        {"92", VE, "Thiệp mời bạn bè dự Lễ kết đôi.", "0", buoc("dùng khi tổ chức Lễ kết đôi.")},
        {"93", BINH, "Hồi 7.000 HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.", "1", ""},
        {"94", BINH, "Hồi 15.000 HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.", "1", ""},
        {"95", BINH, "Hồi 7.000 PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.", "1", ""},
        {"96", BINH, "Hồi 15.000 PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.", "1", ""},
        {"97", VE, "Đổi màu hàng đang vận chuyển (cần đang nhận hàng).", "1", ""},
        {"98", VE, "Dịch chuyển tới chỗ người kết đôi với bạn (đang online).", "1", ""},
        {"99", VE, "Bắn pháo hoa tại chỗ (chỉ ở bản đồ sự kiện).", "1", ""},
        {"100", VE, "Gửi một dòng thông báo (50 ký tự) tới cả máy chủ.", "1", ""},
        {"101", HOP, "Mở ra: nhận ngẫu nhiên thuốc HP / PP 15k, EXP hoặc vật phẩm quý.", "1", ""},
        {"102", HOP, "Mở ra: nhận ngẫu nhiên thuốc HP / PP 15k, EXP hoặc vật phẩm.", "1", ""},
        {"103", NGUYEN_LIEU, "Lá vàng: nguyên liệu sự kiện.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"104", THU, "Poké Ball: dùng để bắt thú nuôi.", "0", buoc("dùng khi bắt thú nuôi.")},
        {"105", NGUYEN_LIEU, "Mảnh bản đồ 2: ghép đủ 3 mảnh để đổi bản đồ kho báu.", "0", buoc("ghép mảnh ở NPC sự kiện.")},
        {"106", NGUYEN_LIEU, "Nhánh hoa hồng vàng: nguyên liệu sự kiện Vu Lan.", "0", buoc("đổi ở NPC sự kiện Vu Lan.")},
        {"107", NGUYEN_LIEU, "Hoa hồng đỏ: nguyên liệu sự kiện Vu Lan.", "0", buoc("đổi ở NPC sự kiện Vu Lan.")},
        {"108", HOP, "Quà bình chọn World Cup: mở ra nhận ngẫu nhiên ruy băng, nguyên liệu, xu / lượng hoặc pet. Chỉ mở trong thời gian sự kiện.", "1", ""},
        {"109", HOP, "Bó hoa vàng (sự kiện Vu Lan): mở ra nhận quà ngẫu nhiên.", "1", ""},
        {"110", HOP, "Bó hoa đỏ (sự kiện Vu Lan): mở ra nhận quà ngẫu nhiên.", "1", ""},
        {"111", THU, "Dạy pet một chiêu ngẫu nhiên.", "1", ""},
        {"112", THU, "Cho thú đang cưỡi ăn: tăng phòng thủ của thú trong 4 giờ. Ăn món khác sẽ thay món đang có.", "1", ""},
        {"113", THU, "Cho thú đang cưỡi ăn: tăng tấn công của thú trong 4 giờ. Ăn món khác sẽ thay món đang có.", "1", ""},
        {"114", THU, "Cho thú đang cưỡi ăn: tăng tấn công, phòng thủ và thuộc tính đặc biệt của thú trong 4 giờ. Ăn món khác sẽ thay món đang có.", "1", ""},
        {"115", TRUNG, "Dùng để nở thú cưỡi Suicune vĩnh viễn (vào Ô thú, cấp 1). Ô thú đầy thì không dùng được và không mất trứng.", "1", ""},
        {"116", VE, "Đổi màu hàng đang vận chuyển, bản cao cấp (cần đang nhận hàng).", "1", ""},
        {"117", NGUYEN_LIEU, "Mảnh bản đồ 3: ghép đủ 3 mảnh để đổi bản đồ kho báu.", "0", buoc("ghép mảnh ở NPC sự kiện.")},
        {"118", VE, "Vé quay số Moltres.", "0", buoc("dùng ở NPC quay số Moltres.")},
        {"119", BINH, "Hồi sinh ngay tại chỗ. Chỉ dùng được khi đã gục.", "1", ""},
        {"120", NGUYEN_LIEU, "Thịt: nguyên liệu sự kiện.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"121", NGUYEN_LIEU, "Trứng: nguyên liệu sự kiện.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"122", NGUYEN_LIEU, "Đậu xanh: nguyên liệu sự kiện Trung Thu.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"123", TANG_CUONG, "Tăng 150% tấn công, phòng thủ, chính xác, chí mạng, né trong 30 giây. Không dùng khi đang có hiệu ứng Thuốc X.", "1", ""},
        {"124", NGUYEN_LIEU, "Bánh dẻo: nguyên liệu sự kiện Trung Thu.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"125", NGUYEN_LIEU, "Bánh nướng: nguyên liệu sự kiện Trung Thu.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"126", CHIEN_TRUONG, "Hồi 20.000 HP." + CT, "1", ""},
        {"127", CHIEN_TRUONG, "Hồi 20.000 PP." + CT, "1", ""},
        {"128", CHIEN_TRUONG, "Nhân đôi các chỉ số chiến đấu trong 60 giây." + CT, "1", ""},
        {"129", CHIEN_TRUONG, "Tăng 20% giáp trong 90 giây." + CT, "1", ""},
        {"130", CHIEN_TRUONG, "Tăng 20% sát thương trong 90 giây." + CT, "1", ""},
        {"131", CHIEN_TRUONG, "Tăng 50% sát thương lên trùm trong 90 giây." + CT, "1", ""},
        {"132", CHIEN_TRUONG, "Gây sát thương bằng 10% năng lượng bản thân lên mọi mục tiêu trong bán kính 5m." + CT, "1", ""},
        {"133", HOP, "Mở ra: cộng ngay 300.000 EXP (giới hạn số hộp mỗi ngày).", "1", ""},
        {"134", HOP, "Mở ra: nhận quà Trung Thu ngẫu nhiên (giới hạn số hộp mỗi ngày, cần 1 ô trống).", "1", ""},
        {"135", TANG_CUONG, "Thêm 180 phút được rơi nguyên liệu sự kiện khi đánh quái gần cấp (cộng dồn, tối đa 1.080 phút).", "1", ""},
        {"136", NGUYEN_LIEU, "Nến: nguyên liệu sự kiện.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"137", NGUYEN_LIEU, "Kẹo: nguyên liệu sự kiện Halloween.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"138", NGUYEN_LIEU, "Bí ngô: nguyên liệu sự kiện Halloween.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"139", NGUYEN_LIEU, "Hộp quà: nguyên liệu sự kiện.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"140", HOP, "Hộp quà Halloween: mở ra nhận quà ngẫu nhiên.", "1", ""},
        {"141", HOP, "Hộp quà tặng Halloween: mở ra nhận quà ngẫu nhiên.", "1", ""},
        {"142", NGUYEN_LIEU, "Bột mỳ: nguyên liệu sự kiện.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"143", HOP, "Hộp quà bí mật: mở ra nhận quà ngẫu nhiên (cần 1 ô trống, chỉ mở khi sự kiện cho phép).", "1", ""},
        {"144", VE, "Sao vàng: mở rương ở bản đồ đặc biệt.", "1", ""},
        {"145", NGUYEN_LIEU, "Dưa hấu: nguyên liệu sự kiện.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"146", HOP, "Ăn kem: cộng ngay 300.000 EXP (giới hạn số lần mỗi ngày).", "1", ""},
        {"147", HOP, "Túi quà may mắn: mở ra nhận quà ngẫu nhiên.", "1", ""},
        {"148", NGUYEN_LIEU, "Gạo nếp: nguyên liệu sự kiện Tết.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"149", NGUYEN_LIEU, "Thịt mỡ: nguyên liệu sự kiện Tết.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"150", NGUYEN_LIEU, "Lá dong: nguyên liệu sự kiện Tết.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"151", NGUYEN_LIEU, "Bánh chưng: vật phẩm sự kiện Tết.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"152", NGUYEN_LIEU, "Bánh tét: vật phẩm sự kiện Tết.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"153", NGUYEN_LIEU, "Giấy: nguyên liệu làm pháo sự kiện Tết.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"154", NGUYEN_LIEU, "Thuốc pháo: nguyên liệu làm pháo sự kiện Tết.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"155", VE, "Bắn pháo hoa Tết tại chỗ.", "1", ""},
        {"156", TANG_CUONG, "Đốt lửa nhỏ tại chỗ: người đứng gần được tăng kinh nghiệm.", "1", ""},
        {"157", TANG_CUONG, "Đốt lửa vừa tại chỗ: người đứng gần được tăng kinh nghiệm.", "1", ""},
        {"158", TANG_CUONG, "Đốt lửa to tại chỗ: người đứng gần được tăng kinh nghiệm.", "1", ""},
        {"159", HOP, "Mở bao lì xì: nhận quà ngẫu nhiên.", "1", ""},
        {"160", NGUYEN_LIEU, "Quả bóng bạc: vật phẩm sự kiện World Cup.", "0", buoc("đổi ở NPC sự kiện khi sự kiện mở.")},
        {"161", HOP, "Quả bóng vàng (sự kiện World Cup): dùng để nhận quà.", "1", ""},
        {"162", VE, "Dùng trong Hành trang: dịch chuyển về làng hoặc thành phố đã mở. Dùng 1 lần là hết. Không dùng khi đang đánh Gym, trong hầm ngục, PvP, giao hàng hay khi bị PK.", "1", ""},
    };

    private static final String[][] THEO_ID = new String[256][];

    static {
        for (final String[] d : BANG) {
            THEO_ID[Integer.parseInt(d[0])] = d;
        }
    }

    private MoTaVatPham() {
    }

    public static String[][] bang() {
        return BANG;
    }

    static String[] dong(final int id) {
        return id >= 0 && id < THEO_ID.length ? THEO_ID[id] : null;
    }

    public static boolean co(final int id) {
        return dong(id) != null;
    }

    public static boolean laHp(final int id) {
        return trong(HP, id);
    }

    public static boolean laPp(final int id) {
        return trong(PP, id);
    }

    /** Chi binh HP / PP duoc gan vao nut thuoc HUD / o nhanh. */
    public static boolean ganONhanhDuoc(final int id) {
        return laHp(id) || laPp(id);
    }

    public static String loai(final int id) {
        final String[] d = dong(id);
        return d == null ? "Vật phẩm" : d[1];
    }

    public static String moTa(final int id) {
        final String[] d = dong(id);
        return d == null ? "" : d[2];
    }

    /** Dung tu Hanh trang duoc (id khong co trong bang: true - giu hanh vi cu). */
    public static boolean dungDuoc(final int id) {
        final String[] d = dong(id);
        return d == null || "1".equals(d[3]);
    }

    public static String lyDo(final int id) {
        final String[] d = dong(id);
        return d == null ? "" : d[4];
    }

    static boolean trong(final int[] a, final int v) {
        for (final int x : a) {
            if (x == v) {
                return true;
            }
        }
        return false;
    }
}
