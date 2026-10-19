package com.htbb.pokemon;

/**
 * MO HINH CHI SO POKEMON cho bang data_attribute (docs/thiet-ke-game.md). Lop THUAN, NGUON
 * DUY NHAT cua ten / don vi / mo ta / trang thai moi dong chi so: tools/luu_tru_sinh_du_lieu/pokemon/SinhSqlChiSo.java sinh ra
 * tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_chi_so.sql + tools/luu_tru_sinh_du_lieu/du_lieu/chi_so_trang_bi.csv tu day; server dung {@link #an} de an dong khi bat
 * sv.lau_dai.
 *
 * GIU NGUYEN ID (client tra ten theo VI TRI dong, xem test/KiemBangThuocTinh): khong them, khong xoa, chi doi
 * nghia / ten / don vi. Anh xa id -> mang luu tren Item:
 *   0..32 atb[i]  |  33..42 newAtb[i-33]  |  43..57 addMoreLevelSkill[i-43]  |  58..60 lockAtb[i-58]  |
 *   61..186 otherAtt[i-61]  |  200..210 chi so skin thu cuoi (khong luu tren Item).
 *
 * ispercent (cach client in so): 0 = so nguyen, 1 = "x%", 2 = "x/10 %" (gia tri luu PHAN NGHIN).
 */
public final class ChiSoTrangBi {

    private ChiSoTrangBi() {
    }

    /** Co tac dung (khi bat sv.lau_dai). */
    public static final String DUNG = "dung";
    /** Dong mo ta hieu ung CO DINH cua do dac biet (thoi trang, cai trang, mat na): giu hien. */
    public static final String HIEN = "hien";
    /** Noi bo (so hieu hinh, gia, so luong, danh dau): khong phai chi so, khong bao gio hien. */
    public static final String NOI_BO = "noi_bo";
    /** Ngung (di san kiem hiep / khong co code doc): khong tac dung khi bat sv.lau_dai va bi an. */
    public static final String NGUNG = "ngung";

    /** Mot dong: {id, ten, ispercent, idcolor, trang thai, nhom, mo ta (nguoi choi doc), ap dung o dau (noi bo)}. */
    public static final class Dong {
        public final int id;
        public final String ten;
        public final int phanTram;
        public final int mau;
        public final String trangThai;
        public final String nhom;
        public final String moTa;
        public final String apDung;

        Dong(final int id, final String ten, final int phanTram, final int mau, final String trangThai, final String nhom,
                final String moTa, final String apDung) {
            this.id = id;
            this.ten = ten;
            this.phanTram = phanTram;
            this.mau = mau;
            this.trangThai = trangThai;
            this.nhom = nhom;
            this.moTa = moTa;
            this.apDung = apDung;
        }
    }

    private static Dong d(final int id, final String ten, final int pt, final int mau, final String tt, final String nhom,
            final String moTa, final String apDung) {
        return new Dong(id, ten, pt, mau, tt, nhom, moTa, apDung);
    }

    // nhom
    private static final String CB = "co_ban";
    private static final String CM = "chi_mang";
    private static final String SS = "sinh_ton";
    private static final String HE = "he";
    private static final String TT = "trang_thai";
    private static final String DB = "dac_biet";
    private static final String KT = "kinh_te";
    private static final String NB = "noi_bo";

    /** Toan bo 198 dong (id 0..186 va 200..210), dung thu tu id. */
    public static final Dong[] BANG = {
        d(0, "Tấn công", 0, 0, DUNG, CB, "Cộng thẳng vào tấn công.", "CharChiSo.calculateAttack (đồ cầm tay 3-7, huy hiệu 8, vòng cổ 9, đồ thú cưỡi 15/17)"),
        d(1, "Phòng thủ", 0, 0, DUNG, CB, "Giảm sát thương vật lý nhận vào.", "CharChiSo.calculateDefend (giáp 0-2, 10, 11, ruy băng 19, đồ thú cưỡi 14/16/18)"),
        d(2, "Né tránh", 0, 0, DUNG, CB, "Điểm né tránh: tăng khả năng tránh đòn.", "Char.calculateDodge"),
        d(3, "Chính xác", 0, 0, DUNG, CB, "Điểm chính xác: tăng khả năng đánh trúng.", "Char.calculateAccurate"),
        d(4, "Chí mạng", 0, 0, DUNG, CM, "Điểm chí mạng: 3 điểm = 1% tỉ lệ chí mạng.", "Char.calculateCritical -> Char.havecrit"),
        d(5, "HP", 0, 0, DUNG, CB, "Tăng HP tối đa (nhân theo dòng Pokémon).", "CharChiSo.calculateMaxHPMP (phiến đá hệ 12: secondHealth)"),
        d(6, "Phòng thủ đặc biệt", 0, 0, DUNG, CB, "Giảm sát thương đặc biệt nhận vào.", "CharChiSo.calculateDefend"),
        d(7, "Mã hình 1", 0, 0, NOI_BO, NB, "Số hiệu hình của đồ thời trang (không phải chỉ số).", "Char/CharCopy id[4] (atb8 thời trang)"),
        d(8, "Mã hình 2", 0, 0, NOI_BO, NB, "Số hiệu hình của đồ thời trang (không phải chỉ số).", "Char/CharCopy id[4]"),
        d(9, "Đánh dấu thời trang", 0, 0, NOI_BO, NB, "1 = món là đồ thời trang (không phải chỉ số).", "Map.isModelClothes"),
        d(10, "Sức mạnh", 0, 3, DUNG, CB, "Cộng điểm sức mạnh (ngọc khảm).", "Char.calculateAttrib -> abilityKham[0]"),
        d(11, "Nhanh nhẹn", 0, 3, DUNG, CB, "Cộng điểm nhanh nhẹn (ngọc khảm).", "abilityKham[1]"),
        d(12, "Tinh thần", 0, 3, DUNG, CB, "Cộng điểm tinh thần (ngọc khảm).", "abilityKham[2]"),
        d(13, "Sức khoẻ", 0, 3, DUNG, CB, "Cộng điểm sức khoẻ (ngọc khảm).", "abilityKham[3]"),
        d(14, "Gây ngủ", 2, 3, DUNG, TT, "Tỉ lệ làm mục tiêu ngủ khi đánh trúng.", "Char.buffSkillKham -> TrangThaiGame (sv.lau_dai)"),
        d(15, "Gây đóng băng", 2, 3, DUNG, TT, "Tỉ lệ làm mục tiêu đóng băng khi đánh trúng.", "Char.buffSkillKham"),
        d(16, "Gây trúng độc", 2, 3, DUNG, TT, "Tỉ lệ làm mục tiêu trúng độc khi đánh trúng.", "Char.buffSkillKham"),
        d(17, "Gây tê liệt", 2, 3, DUNG, TT, "Tỉ lệ làm mục tiêu tê liệt khi đánh trúng.", "Char.buffSkillKham"),
        d(18, "Gây bỏng", 2, 3, DUNG, TT, "Tỉ lệ làm mục tiêu bị bỏng khi đánh trúng.", "Char.buffSkillKham"),
        d(19, "Gây giảm tốc", 2, 3, NGUNG, TT, "Không còn tác dụng.", "-"),
        d(20, "Kháng giảm tốc", 2, 3, NGUNG, TT, "Không còn tác dụng.", "-"),
        d(21, "Kháng trúng độc", 2, 3, DUNG, TT, "Giảm tỉ lệ bị trúng độc.", "Char.khamKhangDoc"),
        d(22, "Kháng ngủ", 2, 3, DUNG, TT, "Giảm tỉ lệ bị ngủ.", "Char.khamKhangMu"),
        d(23, "Kháng đóng băng", 2, 3, DUNG, TT, "Giảm tỉ lệ bị đóng băng.", "Char.khamKhangBang"),
        d(24, "Kháng tê liệt", 2, 3, DUNG, TT, "Giảm tỉ lệ bị tê liệt.", "Char.khamKhangChoang"),
        d(25, "Kháng bỏng", 2, 3, DUNG, TT, "Giảm tỉ lệ bị bỏng.", "Char.khamKhangHoathach"),
        d(26, "Đánh kép", 1, 3, NGUNG, DB, "Không còn tác dụng.", "Char.khamX2 (không nơi nào gọi)"),
        d(27, "Tăng rơi bảo vật", 1, 3, NGUNG, KT, "Không còn tác dụng.", "Char.khamDropItem (không nơi nào gọi)"),
        d(28, "Giảm ST vật lý nhận", 1, 1, DUNG, SS, "Giảm % sát thương vật lý nhận vào (khi kích bộ hệ).", "Char.checkKickAtt5 -> attNguHanh[0] -> Char.subDam"),
        d(29, "Giảm ST đặc biệt nhận", 1, 1, DUNG, SS, "Giảm % sát thương đặc biệt nhận vào (khi kích bộ hệ).", "attNguHanh[1] -> Char.subDam"),
        d(30, "Tấn công %", 1, 1, DUNG, CB, "Tăng % tấn công (khi kích bộ hệ).", "attNguHanh[2] -> CharChiSo.calculateAttack"),
        d(31, "Xuyên giáp", 1, 1, DUNG, DB, "Tỉ lệ đòn đánh bỏ qua phòng thủ (khi kích bộ hệ).", "attNguHanh[3] -> Char.getBuffEffAttack"),
        d(32, "Phản sát thương", 1, 1, DUNG, SS, "Trả lại % sát thương nhận vào cho kẻ đánh (khi kích bộ hệ).", "attNguHanh[4] -> CharCapNhat / Char"),
        d(33, "HP (mỗi điểm +1.000)", 0, 0, DUNG, CB, "Mỗi điểm cộng 1.000 HP tối đa.", "Char.newAtb[0] x1000"),
        d(34, "PP (mỗi điểm +1.000)", 0, 0, DUNG, CB, "Mỗi điểm cộng 1.000 PP tối đa.", "Char.newAtb[1] x1000"),
        d(35, "Sức mạnh", 0, 0, DUNG, CB, "Cộng điểm sức mạnh.", "Char.newAtb[2]"),
        d(36, "Nhanh nhẹn", 0, 0, DUNG, CB, "Cộng điểm nhanh nhẹn.", "Char.newAtb[3]"),
        d(37, "Tinh thần", 0, 0, DUNG, CB, "Cộng điểm tinh thần.", "Char.newAtb[4] (thú cưng: sức mạnh)"),
        d(38, "Sức khoẻ", 0, 0, DUNG, CB, "Cộng điểm sức khoẻ.", "Char.newAtb[5] (thú cưng: nhanh nhẹn)"),
        d(39, "Kỹ năng", 0, 0, DUNG, CB, "Cộng điểm kỹ năng của thú cưng.", "Pet.getspirit (newAtb[6])"),
        d(40, "Chí mạng", 0, 0, DUNG, CM, "Điểm chí mạng: 3 điểm = 1% tỉ lệ chí mạng.", "Char.calculateCritical (newAtb[7])"),
        d(41, "ST chí mạng %", 1, 0, DUNG, CM, "Tăng % sát thương của đòn chí mạng.", "Char.tkNhanChiMang (newAtb[8], sv.lau_dai)"),
        d(42, "Không dùng", 0, 0, NOI_BO, NB, "Ô dự phòng, không có chỉ số.", "-"),
        d(43, "Cấp chiêu 1", 0, 0, DUNG, DB, "Tăng cấp chiêu 1 (tên chiêu hiện theo dòng Pokémon).", "Char.addMoreLevelSkill"),
        d(44, "Cấp chiêu 2", 0, 0, DUNG, DB, "Tăng cấp chiêu 2.", "Char.addMoreLevelSkill"),
        d(45, "Cấp chiêu 3", 0, 0, DUNG, DB, "Tăng cấp chiêu 3.", "Char.addMoreLevelSkill"),
        d(46, "Cấp chiêu 4", 0, 0, DUNG, DB, "Tăng cấp chiêu 4.", "Char.addMoreLevelSkill"),
        d(47, "Cấp chiêu 5", 0, 0, DUNG, DB, "Tăng cấp chiêu 5.", "Char.addMoreLevelSkill"),
        d(48, "Cấp chiêu 6", 0, 0, DUNG, DB, "Tăng cấp chiêu 6.", "Char.addMoreLevelSkill"),
        d(49, "Cấp chiêu 7", 0, 0, DUNG, DB, "Tăng cấp chiêu 7.", "Char.addMoreLevelSkill"),
        d(50, "Cấp chiêu 8", 0, 0, DUNG, DB, "Tăng cấp chiêu 8.", "Char.addMoreLevelSkill"),
        d(51, "Cấp chiêu 9", 0, 0, DUNG, DB, "Tăng cấp chiêu 9.", "Char.addMoreLevelSkill"),
        d(52, "Cấp chiêu 10", 0, 0, DUNG, DB, "Tăng cấp chiêu 10.", "Char.addMoreLevelSkill"),
        d(53, "Cấp chiêu 11", 0, 0, DUNG, DB, "Tăng cấp chiêu 11.", "Char.addMoreLevelSkill"),
        d(54, "Cấp chiêu 12", 0, 0, DUNG, DB, "Tăng cấp chiêu 12.", "Char.addMoreLevelSkill"),
        d(55, "Cấp chiêu 13", 0, 0, DUNG, DB, "Tăng cấp chiêu 13.", "Char.addMoreLevelSkill"),
        d(56, "Cấp chiêu 14", 0, 0, DUNG, DB, "Tăng cấp chiêu 14.", "Char.addMoreLevelSkill"),
        d(57, "Cấp chiêu 15", 0, 0, DUNG, DB, "Tăng cấp chiêu 15.", "Char.addMoreLevelSkill"),
        d(58, "Tấn công %", 1, 0, DUNG, CB, "Tăng % tấn công.", "lockAtb[0] -> CharChiSo.calculateAttack"),
        d(59, "Phòng thủ đặc biệt %", 1, 0, DUNG, CB, "Tăng % phòng thủ đặc biệt.", "lockAtb[1] -> CharChiSo.calculateDefend"),
        d(60, "Phòng thủ %", 1, 0, DUNG, CB, "Tăng % phòng thủ.", "lockAtb[2] -> CharChiSo.calculateDefend"),
        d(61, "Bỏ qua phòng thủ đặc biệt", 1, 1, DUNG, DB, "Bỏ qua % phòng thủ đặc biệt của mục tiêu (mặt nạ).", "Char.getPcBoQuaThuMatNa"),
        d(62, "Bỏ qua phòng thủ", 1, 1, DUNG, DB, "Bỏ qua % phòng thủ của mục tiêu (mặt nạ).", "Char.getPcBoQuaThuMatNa"),
        d(63, "Phòng thủ đặc biệt từ trang bị %", 1, 0, DUNG, CB, "Tăng % phòng thủ đặc biệt lấy từ trang bị.", "CharChiSo.calculateDefend (ruy băng 19)"),
        d(64, "Phòng thủ từ trang bị %", 1, 0, DUNG, CB, "Tăng % phòng thủ lấy từ trang bị.", "CharChiSo.calculateDefend (ruy băng 19)"),
        d(65, "Chí mạng thêm", 2, 0, DUNG, CM, "Cộng thêm tỉ lệ chí mạng.", "Char.haveBaoKich -> gộp vào chí mạng (sv.lau_dai)"),
        d(66, "Lan sét", 2, 2, NGUNG, DB, "Không còn tác dụng.", "CharTrangBi.checkNewEffectItem (tắt khi sv.lau_dai)"),
        d(67, "Lan độc", 2, 2, NGUNG, DB, "Không còn tác dụng.", "CharTrangBi.checkNewEffectItem"),
        d(68, "Lan băng", 2, 2, NGUNG, DB, "Không còn tác dụng.", "CharTrangBi.checkNewEffectItem"),
        d(69, "Tăng lan sét", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(70, "Tăng lan độc", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(71, "Tăng lan băng", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(72, "ST lan băng", 1, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(73, "ST lan sét", 1, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(74, "ST lan độc", 1, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(75, "Biến người tuyết", 2, 0, NGUNG, DB, "Không còn tác dụng.", "CharChiSo.attackDam (tắt khi sv.lau_dai)"),
        d(76, "Kháng chí mạng", 2, 0, DUNG, CM, "Giảm tỉ lệ bị đòn chí mạng trong PvP.", "Char.getKhangBaoKichMatNa"),
        d(77, "Hút HP", 2, 0, DUNG, SS, "Hồi HP theo % sát thương gây ra.", "Char.hutmau -> CharChiSo.applyThanKhiOnHit (gộp, sv.lau_dai)"),
        d(78, "Hồi HP mỗi 5 giây", 0, 0, DUNG, SS, "Hồi HP mỗi 5 giây.", "Char.haveRegentHP"),
        d(79, "Hồi PP mỗi 5 giây", 0, 0, DUNG, SS, "Hồi PP mỗi 5 giây.", "Char.haveRegentMP"),
        d(80, "Gây chậm", 2, 0, DUNG, TT, "Tỉ lệ làm mục tiêu chậm lại khi đánh trúng.", "Char.haveTanPhe"),
        d(81, "Hấp thụ sát thương", 2, 0, NGUNG, SS, "Không còn tác dụng.", "-"),
        d(82, "Gây cấm chiêu", 2, 0, DUNG, TT, "Tỉ lệ làm mục tiêu không dùng được chiêu trong thời gian ngắn.", "Char.haveLamThinh"),
        d(83, "Tăng rơi vật phẩm", 2, 0, NGUNG, KT, "Không còn tác dụng.", "Char.tyleRotItem (không nơi nào đọc)"),
        d(84, "Tăng rơi xu", 2, 0, NGUNG, KT, "Không còn tác dụng.", "Char.tylerotXu (không nơi nào đọc)"),
        d(85, "Kháng lan băng", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(86, "Kháng lan sét", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(87, "Kháng lan độc", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(88, "Phòng thủ từ trang bị", 2, 0, DUNG, CB, "Tăng phòng thủ theo phòng thủ của trang bị.", "CharChiSo.calculateDefend (upThuTbChar)"),
        d(89, "Lan lửa", 2, 2, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(90, "Tăng lan lửa", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(91, "ST lan lửa", 1, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(92, "Kháng lan lửa", 2, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(93, "ST theo hệ phiến đá", 2, 0, DUNG, HE, "Tăng sát thương các chiêu cùng hệ đã chọn trên phiến đá hệ.", "ChiSoTrangBiGame.stHe (sv.lau_dai)"),
        d(94, "Kháng theo hệ phiến đá", 2, 0, DUNG, HE, "Giảm sát thương nhận từ hệ đã chọn trên phiến đá hệ.", "ChiSoTrangBiGame.khangHe (sv.lau_dai)"),
        d(95, "Không dùng", 2, 0, NGUNG, NB, "Không còn tác dụng.", "-"),
        d(96, "Không dùng", 2, 0, NGUNG, NB, "Không còn tác dụng.", "-"),
        d(97, "Gây hoảng sợ", 0, 0, NGUNG, TT, "Không còn tác dụng.", "-"),
        d(98, "Tỉ lệ hoảng sợ", 1, 0, NGUNG, TT, "Không còn tác dụng.", "-"),
        d(99, "ST theo chủ", 1, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(100, "Sức khoẻ", 0, 0, NGUNG, CB, "Không còn tác dụng.", "-"),
        d(101, "ST theo HP tối đa", 1, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(102, "Gây ngủ (thú cưng)", 2, 0, DUNG, TT, "Kỹ năng thú cưng: tỉ lệ làm mục tiêu ngủ.", "Pet (otherAtt[41])"),
        d(103, "Gây choáng (thú cưng)", 2, 0, DUNG, TT, "Kỹ năng thú cưng: tỉ lệ làm mục tiêu choáng.", "Pet (otherAtt[42])"),
        d(104, "Gây bỏng (thú cưng)", 2, 0, DUNG, TT, "Kỹ năng thú cưng: tỉ lệ gây bỏng.", "Pet (otherAtt[43])"),
        d(105, "Gây trúng độc (thú cưng)", 2, 0, DUNG, TT, "Kỹ năng thú cưng: tỉ lệ gây trúng độc.", "Pet.getpcTrungDoc"),
        d(106, "Gây suy yếu (thú cưng)", 2, 0, DUNG, TT, "Kỹ năng thú cưng: tỉ lệ làm mục tiêu suy yếu.", "Pet.getpcSuyYeu"),
        d(107, "Chuyển ST nhận thành PP (thú cưng)", 0, 0, DUNG, SS, "Kỹ năng thú cưng: chuyển một phần sát thương nhận vào thành PP.", "Pet (otherAtt[46])"),
        d(108, "Tăng tấn công (thú cưng)", 0, 0, DUNG, CB, "Kỹ năng thú cưng: tăng tấn công.", "Pet (otherAtt[47])"),
        d(109, "Hút HP (thú cưng)", 0, 0, DUNG, SS, "Kỹ năng thú cưng: lượng HP hút mỗi lần kích hoạt.", "Pet.getHpHutByHit -> gộp hút HP (sv.lau_dai)"),
        d(110, "Sát thương độc (thú cưng)", 0, 0, DUNG, TT, "Kỹ năng thú cưng: sát thương của độc.", "Pet (otherAtt[49])"),
        d(111, "Tăng EXP", 1, 0, DUNG, KT, "Tăng % EXP nhận được (gộp với thú cưng, thời trang, tối đa +30%).", "ExpLauDai.buffPhanTram (kẹp GioiHanChiSo.expTrangBi)"),
        d(112, "Thời gian suy yếu (thú cưng)", 1, 0, DUNG, TT, "Kỹ năng thú cưng: độ mạnh của suy yếu.", "Pet.getPcTacDongSuyYeu"),
        d(113, "Tỉ lệ hút HP (thú cưng)", 2, 0, DUNG, SS, "Kỹ năng thú cưng: tỉ lệ kích hoạt hút HP.", "Pet.getpcHutHpByHit"),
        d(114, "ST thêm khi mục tiêu bỏng (thú cưng)", 1, 0, DUNG, TT, "Kỹ năng thú cưng: tăng sát thương lên mục tiêu đang bỏng.", "Pet (otherAtt[53])"),
        d(115, "ST theo % HP mục tiêu", 2, 1, DUNG, DB, "Đòn đánh cộng thêm sát thương theo % HP tối đa của mục tiêu.", "CharChiSo.attackDam (bảo vật cầm tay, kẹp GioiHanChiSo.stTheoHp)"),
        d(116, "Tăng chỉ số đồ cầm tay", 2, 1, DUNG, CB, "Tăng % chỉ số của đồ cầm tay đang dùng.", "Item.getValueUpByVuKhiThoiTrang"),
        d(117, "Tỉ lệ ST theo % HP", 2, 1, DUNG, DB, "Tỉ lệ kích hoạt sát thương theo % HP mục tiêu.", "CharChiSo.attackDam"),
        d(118, "Giảm sát thương cuối", 2, 2, HIEN, SS, "Giảm sát thương nhận vào trong thời gian hiệu lực (đồ thời trang).", "ModelWearing.getPcGiamSatThuong"),
        d(119, "Ánh Trăng (tỉ lệ kích hoạt)", 2, 2, HIEN, DB, "Tỉ lệ kích hoạt Ánh Trăng của đồ thời trang.", "ModelWearing"),
        d(120, "Kháng hệ Lửa", 2, 1, DUNG, HE, "Giảm sát thương nhận từ chiêu hệ Lửa.", "ChiSoTrangBiGame.khangHe (sv.lau_dai)"),
        d(121, "Kháng hệ Nước", 2, 1, DUNG, HE, "Giảm sát thương nhận từ chiêu hệ Nước.", "ChiSoTrangBiGame.khangHe"),
        d(122, "Kháng hệ Cỏ", 2, 1, DUNG, HE, "Giảm sát thương nhận từ chiêu hệ Cỏ.", "ChiSoTrangBiGame.khangHe"),
        d(123, "Kháng hệ Thép", 2, 1, DUNG, HE, "Giảm sát thương nhận từ chiêu hệ Thép.", "ChiSoTrangBiGame.khangHe"),
        d(124, "Kháng hệ Đất", 2, 1, DUNG, HE, "Giảm sát thương nhận từ chiêu hệ Đất.", "ChiSoTrangBiGame.khangHe"),
        d(125, "Giá lượng", 0, 2, NOI_BO, NB, "Giá bán (không phải chỉ số).", "Map / MapMuaBan"),
        d(126, "Số lượng", 0, 2, NOI_BO, NB, "Số lượng gộp (không phải chỉ số).", "Char / Map"),
        d(127, "Kháng sát thương lan", 0, 2, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(128, "Hồi HP theo HP tối đa", 0, 2, NGUNG, SS, "Không còn tác dụng.", "-"),
        d(129, "Chuyển ST thành HP", 1, 0, DUNG, SS, "Hồi HP theo % sát thương gây ra (mặt nạ).", "Char.getPcChuyenStThanhHpMatNa -> gộp hút HP"),
        d(130, "Giảm phòng thủ đối thủ", 0, 0, NGUNG, DB, "Không còn tác dụng.", "-"),
        d(131, "Né tránh", 0, 2, NGUNG, CB, "Không còn tác dụng.", "-"),
        d(132, "Hoá giải kỹ năng thú cưng", 1, 0, DUNG, DB, "Tỉ lệ vô hiệu kỹ năng thú cưng của đối thủ (mặt nạ + bảo vật).", "Char.getPcHoaGiaiKyNangPet"),
        d(133, "Tăng hồi phục khi HP thấp", 0, 2, NGUNG, SS, "Không còn tác dụng.", "-"),
        d(134, "Sát thương theo HP", 1, 1, HIEN, DB, "Hiệu ứng cố định của cải trang.", "Item.setAttributeByBatGioi / SaTang"),
        d(135, "Mở khoá: chủ nhận đủ 500.000 sát thương", 1, 0, HIEN, DB, "Điều kiện mở hiệu ứng của cải trang.", "Item.setAttributeByBatGioi"),
        d(136, "Hấp thụ sát thương", 1, 1, HIEN, SS, "Hiệu ứng cố định của cải trang.", "Item.setAttributeByBatGioi"),
        d(137, "Tốc độ chạy", 1, 1, HIEN, CB, "Hiệu ứng cố định của cải trang.", "Item.setAttributeByBatGioi"),
        d(138, "Tiềm năng %", 1, 0, HIEN, CB, "Hiệu ứng cố định của cải trang.", "Item.setAttributeByBatGioi"),
        d(139, "HP", 0, 0, HIEN, CB, "Hiệu ứng cố định của cải trang.", "Item.setAttributeByBatGioi (CharChiSo cộng theo cấp cải trang)"),
        d(140, "Chí mạng %", 1, 0, HIEN, CM, "Hiệu ứng cố định của cải trang.", "Item.setAttributeBySaTang"),
        d(141, "ST chí mạng", 2, 0, DUNG, CM, "Tăng % sát thương của đòn chí mạng.", "Char.getTkStChiMang -> tkNhanChiMang"),
        d(142, "ST theo % HP đối thủ", 2, 0, DUNG, DB, "Cộng sát thương theo % HP tối đa của mục tiêu.", "CharChiSo.applyThanKhiOnHit (kẹp)"),
        d(143, "ST theo % PP đối thủ", 2, 0, DUNG, DB, "Cộng sát thương theo % PP tối đa của mục tiêu (PvP).", "CharChiSo.applyThanKhiOnHit (kẹp)"),
        d(144, "ST vật lý %", 1, 0, DUNG, CB, "Tăng % sát thương vật lý.", "Char.getThanKhiPhanTramSatThuong"),
        d(145, "ST đặc biệt %", 1, 0, DUNG, CB, "Tăng % sát thương đặc biệt.", "Char.getThanKhiPhanTramSatThuong"),
        d(146, "Xuyên phòng thủ đặc biệt", 2, 0, DUNG, DB, "Bỏ qua % phòng thủ đặc biệt của mục tiêu.", "CharChiSo.attackDam (getTkXuyenThu)"),
        d(147, "Xuyên phòng thủ", 2, 0, DUNG, DB, "Bỏ qua % phòng thủ của mục tiêu.", "CharChiSo.attackDam (getTkXuyenThu)"),
        d(148, "ST lên boss", 2, 0, DUNG, DB, "Tăng % sát thương lên boss.", "CharChiSo.applyThanKhiOnHit (kẹp 20%)"),
        d(149, "ST lên quái", 2, 0, DUNG, DB, "Tăng % sát thương lên quái thường.", "CharChiSo.applyThanKhiOnHit (kẹp 20%)"),
        d(150, "Giảm ST nhận từ boss", 2, 0, DUNG, SS, "Giảm % sát thương nhận từ boss.", "Char.subDamThanKhi"),
        d(151, "Phòng thủ cơ bản", 0, 0, DUNG, CB, "Cộng phòng thủ cơ bản.", "CharChiSo.calculateDefend"),
        d(152, "Kháng hệ Thép", 1, 0, DUNG, HE, "Giảm % sát thương nhận từ chiêu hệ Thép.", "ChiSoTrangBiGame.khangHe"),
        d(153, "Kháng hệ Cỏ", 1, 0, DUNG, HE, "Giảm % sát thương nhận từ chiêu hệ Cỏ.", "ChiSoTrangBiGame.khangHe"),
        d(154, "Kháng hệ Nước", 1, 0, DUNG, HE, "Giảm % sát thương nhận từ chiêu hệ Nước.", "ChiSoTrangBiGame.khangHe"),
        d(155, "Kháng hệ Lửa", 1, 0, DUNG, HE, "Giảm % sát thương nhận từ chiêu hệ Lửa.", "ChiSoTrangBiGame.khangHe"),
        d(156, "Kháng hệ Đất", 1, 0, DUNG, HE, "Giảm % sát thương nhận từ chiêu hệ Đất.", "ChiSoTrangBiGame.khangHe"),
        d(157, "Phòng thủ khi trong đội", 0, 0, DUNG, CB, "Cộng phòng thủ khi đang ở trong đội.", "CharChiSo.calculateDefend"),
        d(158, "ST vật lý cộng thêm", 0, 0, DUNG, CB, "Cộng thẳng sát thương vật lý (sau cùng).", "Char.getThanKhiDiemSatThuong"),
        d(159, "ST đặc biệt cộng thêm", 0, 0, DUNG, CB, "Cộng thẳng sát thương đặc biệt (sau cùng).", "Char.getThanKhiDiemSatThuong"),
        d(160, "Tốc độ đánh %", 1, 0, DUNG, CB, "Giảm % thời gian hồi chiêu.", "Char.calculateAttrib (getTkGiamHoiChieu, kẹp 33%)"),
        d(161, "Tốc độ chạy", 0, 0, DUNG, CB, "Tăng tốc độ chạy.", "Char.getThanKhiSpeedBonus"),
        d(162, "Gây cấm chiêu", 1, 0, DUNG, TT, "Tỉ lệ làm mục tiêu không dùng được chiêu.", "CharChiSo.applyThanKhiOnHit"),
        d(163, "ST hệ Thép", 1, 0, DUNG, HE, "Tăng % sát thương chiêu hệ Thép.", "ChiSoTrangBiGame.stHe (sv.lau_dai)"),
        d(164, "ST hệ Cỏ", 1, 0, DUNG, HE, "Tăng % sát thương chiêu hệ Cỏ.", "ChiSoTrangBiGame.stHe"),
        d(165, "ST hệ Nước", 1, 0, DUNG, HE, "Tăng % sát thương chiêu hệ Nước.", "ChiSoTrangBiGame.stHe"),
        d(166, "ST hệ Lửa", 1, 0, DUNG, HE, "Tăng % sát thương chiêu hệ Lửa.", "ChiSoTrangBiGame.stHe"),
        d(167, "ST hệ Đất", 1, 0, DUNG, HE, "Tăng % sát thương chiêu hệ Đất.", "ChiSoTrangBiGame.stHe"),
        d(168, "Số dòng ẩn", 0, 0, DUNG, DB, "Số dòng ẩn tối đa của bảo vật huyền thoại.", "ThoRenBiAn"),
        d(169, "Vải", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(170, "Sắt", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(171, "Ngọc", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(172, "Gỗ thường", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(173, "Da mềm", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(174, "Tơ lụa", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(175, "Bạc", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(176, "Thuỷ tinh", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(177, "Gỗ sưa", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(178, "Da cứng", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(179, "Đá cầu vồng", 0, 0, HIEN, NB, "Nguyên liệu đã góp vào bảo vật huyền thoại.", "ThoRenBiAn"),
        d(180, "Giảm ST nhận từ người chơi", 2, 0, DUNG, SS, "Giảm % sát thương nhận từ người chơi khác.", "Char.getTkGiamStPlayer"),
        d(181, "Hút PP", 1, 0, DUNG, SS, "Hồi PP theo % sát thương gây ra.", "Char.getTkHutMp"),
        d(182, "Hút HP %", 1, 0, DUNG, SS, "Hồi HP theo % sát thương gây ra.", "Char.getTkHutHp -> gộp hút HP"),
        d(183, "Né tránh %", 1, 0, DUNG, CB, "Tăng % tỉ lệ né đòn.", "Char.getTkNeTranh -> haveNeTranh"),
        d(184, "Làm yếu đối thủ", 2, 0, DUNG, TT, "Giảm tấn công và phòng thủ của mục tiêu trong vài giây.", "CharChiSo.applyThanKhiOnHit (SUY_YEU)"),
        d(185, "Phản đòn %", 1, 0, DUNG, SS, "Trả lại % sát thương nhận vào cho kẻ đánh.", "Char.getTkPhanDon (kẹp 15% / PvP 10%)"),
        d(186, "Kháng mọi hệ", 2, 0, DUNG, HE, "Giảm sát thương nhận từ mọi hệ.", "ChiSoTrangBiGame.khangHe"),
        d(200, "HP", 0, 0, HIEN, CB, "Cộng HP tối đa cho chủ (skin thú cưỡi).", "SkinThuCuoi.congHp"),
        d(201, "PP", 0, 0, HIEN, CB, "Cộng PP tối đa cho chủ (skin thú cưỡi).", "SkinThuCuoi.congMp"),
        d(202, "Giảm sát thương khi bị đánh", 1, 0, HIEN, SS, "Đặc tính skin thú cưỡi: giảm sát thương nhận vào.", "SkinThuCuoi.pcChanSatThuong"),
        d(203, "Chính xác", 0, 0, HIEN, CB, "Cộng điểm chính xác cho chủ (skin thú cưỡi).", "SkinThuCuoi.congChinhXac"),
        d(204, "Phản sát thương", 1, 0, HIEN, SS, "Đặc tính skin thú cưỡi: trả lại sát thương.", "SkinThuCuoi"),
        d(205, "Tiềm năng %", 1, 0, HIEN, CB, "Tăng % điểm tiềm năng cho chủ (skin thú cưỡi).", "SkinThuCuoi"),
        d(206, "Lá chắn chặn sát thương", 1, 0, HIEN, SS, "Đặc tính skin thú cưỡi: lá chắn chặn sát thương.", "SkinThuCuoi.pcChanSatThuong"),
        d(207, "Xuyên giáp", 1, 0, HIEN, DB, "Tăng tỉ lệ xuyên giáp cho chủ (skin thú cưỡi).", "SkinThuCuoi.pcXuyenGiap"),
        d(208, "Nộ: tăng tấn công khi HP thấp", 1, 0, HIEN, CB, "Đặc tính skin thú cưỡi: HP dưới 30% thì tăng tấn công.", "SkinThuCuoi.pcCongTanCong"),
        d(209, "Phòng thủ %", 1, 0, HIEN, CB, "Tăng % phòng thủ cho chủ (skin thú cưỡi).", "SkinThuCuoi.pcThu"),
        d(210, "Hấp thụ sát thương", 1, 0, HIEN, SS, "Đặc tính skin thú cưỡi: hấp thụ một phần sát thương.", "SkinThuCuoi.pcHapThu"),
    };

    private static final Dong[] THEO_ID = new Dong[211];

    static {
        for (final Dong x : BANG) {
            THEO_ID[x.id] = x;
        }
    }

    /** Dong theo id (null neu id khong co trong bang). */
    public static Dong lay(final int id) {
        return id >= 0 && id < THEO_ID.length ? THEO_ID[id] : null;
    }

    /** Dong nay bi AN o tooltip khi bat sv.lau_dai (ngung / noi bo). */
    public static boolean an(final int id) {
        final Dong x = lay(id);
        return x != null && (NGUNG.equals(x.trangThai) || NOI_BO.equals(x.trangThai));
    }

    /** Dong nay khong con tac dung khi bat sv.lau_dai. */
    public static boolean ngung(final int id) {
        final Dong x = lay(id);
        return x != null && NGUNG.equals(x.trangThai);
    }

    // ================================================================== he (18 he Pokemon)

    /** Dong "Khang he X" luu PHAN NGHIN tren trang bi/mat na (120..124) -> he. */
    public static final int[][] KHANG_HE_PHAN_NGHIN = {
        {120, HePokemon.LUA}, {121, HePokemon.NUOC}, {122, HePokemon.CO}, {123, HePokemon.THEP}, {124, HePokemon.DAT},
    };
    /** Dong "Khang he X" luu PHAN TRAM tren bao vat huyen thoai (152..156) -> he. */
    public static final int[][] KHANG_HE_PHAN_TRAM = {
        {152, HePokemon.THEP}, {153, HePokemon.CO}, {154, HePokemon.NUOC}, {155, HePokemon.LUA}, {156, HePokemon.DAT},
    };
    /** Dong "ST he X" (phan tram) tren bao vat huyen thoai (163..167) -> he. */
    public static final int[][] ST_HE_PHAN_TRAM = {
        {163, HePokemon.THEP}, {164, HePokemon.CO}, {165, HePokemon.NUOC}, {166, HePokemon.LUA}, {167, HePokemon.DAT},
    };
    /** Khang moi he (phan nghin). */
    public static final int ID_KHANG_MOI_HE = 186;
    /** Hai dong theo he phien da: gia tri = he * 1000 + phan nghin (he 0..17, phan nghin 0..999). */
    public static final int ID_ST_HE_PHIEN_DA = 93;
    public static final int ID_KHANG_HE_PHIEN_DA = 94;

    /** He trong gia tri dong phien da (-1 neu sai). */
    public static int heCuaDongPhienDa(final int giaTri) {
        final int he = giaTri / 1000;
        return giaTri > 0 && he >= 0 && he < HePokemon.SO_HE ? he : -1;
    }

    /** Phan nghin trong gia tri dong phien da. */
    public static int phanNghinDongPhienDa(final int giaTri) {
        return giaTri > 0 ? giaTri % 1000 : 0;
    }

    /** Gia tri luu cho dong phien da. */
    public static int maDongPhienDa(final int he, final int phanNghin) {
        return Math.max(0, Math.min(HePokemon.SO_HE - 1, he)) * 1000 + Math.max(0, Math.min(999, phanNghin));
    }

    /**
     * Khang mot he {@code he} (phan nghin, CHUA giam dan) tu bang gia tri thuoc tinh {@code giaTriTheoId}
     * (do dai &gt;= 187, chi so = id). Gom: 120..124 (phan nghin), 152..156 (phan tram x10), 186, dong phien da 94.
     */
    public static long khangHeTho(final int[] giaTriTheoId, final int he) {
        if (giaTriTheoId == null || he < 0 || he >= HePokemon.SO_HE) {
            return 0L;
        }
        long t = Math.max(0, g(giaTriTheoId, ID_KHANG_MOI_HE));
        for (final int[] x : KHANG_HE_PHAN_NGHIN) {
            if (x[1] == he) {
                t += Math.max(0, g(giaTriTheoId, x[0]));
            }
        }
        for (final int[] x : KHANG_HE_PHAN_TRAM) {
            if (x[1] == he) {
                t += Math.max(0, g(giaTriTheoId, x[0])) * 10L;
            }
        }
        final int pd = g(giaTriTheoId, ID_KHANG_HE_PHIEN_DA);
        if (heCuaDongPhienDa(pd) == he) {
            t += phanNghinDongPhienDa(pd);
        }
        return t;
    }

    /** ST he {@code he} (phan nghin, chua kep) tu 163..167 (phan tram x10) + dong phien da 93. */
    public static long stHeTho(final int[] giaTriTheoId, final int he) {
        if (giaTriTheoId == null || he < 0 || he >= HePokemon.SO_HE) {
            return 0L;
        }
        long t = 0L;
        for (final int[] x : ST_HE_PHAN_TRAM) {
            if (x[1] == he) {
                t += Math.max(0, g(giaTriTheoId, x[0])) * 10L;
            }
        }
        final int pd = g(giaTriTheoId, ID_ST_HE_PHIEN_DA);
        if (heCuaDongPhienDa(pd) == he) {
            t += phanNghinDongPhienDa(pd);
        }
        return t;
    }

    private static int g(final int[] a, final int i) {
        return i >= 0 && i < a.length ? a[i] : 0;
    }

    /** Tran "do he" (do cam tay cung he + ST he + phien da), phan tram: THIET_KE 3.4 = +20%. */
    public static final int TRAN_DO_HE_PHAN_TRAM = 20;

    /**
     * Trang thai Pokemon (TrangThaiPokemon) ma dong gay/khang trang thai cu (khảm) nay doi sang khi bat sv.lau_dai:
     * chi so abilityKham (0 mu->ngu, 3 bang, 1 doc, 2 choang->te, 4 hoa thach->bong).
     */
    public static final int[] TRANG_THAI_THEO_KHAM = {
        TrangThaiPokemon.NGU, TrangThaiPokemon.DOC, TrangThaiPokemon.TE, TrangThaiPokemon.BANG, TrangThaiPokemon.BONG,
    };
}
