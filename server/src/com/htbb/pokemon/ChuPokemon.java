package com.htbb.pokemon;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * G7.C: doi chu nguoi choi nhin thay tu kiem hiep sang the gioi Pokemon (tu dien thuat ngu
 * o muc 15 cua docs/pokemon/KE_HOACH_CHUYEN_DOI.md).
 *
 * Cach lam: KHONG sua hang tram chuoi trong src/ (bo test characterization cu doc chu ma
 * nguon, sua chuoi la vo test). Thay vao do:
 *  - {@link #ap()} goi MOT lan luc bat may (TeamServer, sau khi nap template) doi cac bang
 *    chu dung chung: data.Text, ten vung, ten dong Pokemon (NAME_CLASS), ten thuoc...
 *  - {@link #doi(String)} doi chu o cac cua ngo gui chu xuong client (MessageCreator: thong
 *    bao, menu, hop thoai, NPC, nhiem vu...). Tat che do Pokemon thi tra nguyen chuoi.
 *
 * MOT NGUON cho ca 3 noi: tools/pokemon/sinh_sql_chu.py doc DOI/TEN_THU_CUOI/CHO_PHEP/TU_CAM
 * ngay trong file nay (regex tren ma nguon) de doi chu trong CSDL va sinh ban C# cho client
 * (ClientUnity/.../ChuPokemonDuLieu.cs). Sua bang o day roi chay lai tool, khong sua tay
 * hai file sinh ra. test/KiemChuPokemon doi chieu ban C# voi ban Java.
 *
 * Quy uoc bang: khoa viet thuong, so khop khong phan biet hoa thuong, nguyen tu (khong
 * khop giua mot tu khac). Khoa bat dau bang "=" la so khop PHAN BIET hoa thuong (vd "=Kiếm"
 * la ten vu khi, con "kiếm" viet thuong la dong tu "tìm/kiếm được" thi giu). Gia tri moi
 * KHONG duoc chua lai mot khoa nao (de doi hai lan ra cung ket qua - client cung doi lai
 * chu server da doi).
 */
public final class ChuPokemon {

    /**
     * Tu cam: con xuat hien trong chuoi nguoi choi thay la con sot. Dung boi
     * tools/pokemon/quet_tu_cam.py va test. "=" dau = phan biet hoa thuong.
     */
    public static final String[] TU_CAM = {
        "giang hồ", "võ lâm", "môn phái", "bang hội", "bang chủ", "phó bang", "bang",
        "lãnh thổ", "thanh long", "hắc hổ", "huyền vũ", "linh thú", "xa phu",
        "đại hiệp", "hiệp khách", "thiếu hiệp", "tiền bối", "nội công", "nội lực", "võ công",
        "thần binh", "bí kíp", "cao thủ", "kiếm khách", "kiếm hiệp", "kiếm sư", "kiếm sĩ",
        "kiếm sỹ", "thanh kiếm", "=Kiếm", "đao", "nhân vật", "ngũ hành", "huynh", "đệ tử",
        "sư phụ", "tại hạ", "nghĩa sỹ", "nghĩa sĩ", "tướng quân", "chiến binh", "pháp sư",
        "đấu sĩ", "cung thủ", "bôn tẩu", "hành tẩu",
        // Dot ra soat chu (docs/pokemon/DUYET_CHU.md): khong phai tu cam goc nhung van la
        // chat kiem hiep / co trang - con sot tuc la tu dien chua phu.
        "ngươi", "giết", "yêu ma", "cương thi", "nội thương", "nội kình", "kình lực", "anh hào",
        "ẩn sỹ", "ẩn sĩ", "kết nghĩa", "phu quân", "phu nhân", "nghĩa hiệp",
        "vận tiêu", "cướp tiêu", "bảo tiêu", "xa tiêu", "trấn yêu", "lệnh bài", "thẻ bài",
        "chiếm thành", "lôi đài", "đức vua", "đại thần", "bộ đầu", "phế chức", "quốc gia",
        "chính tà", "tống kim", "cừu nhân", "liên trảm", "đồ sát", "long trụ", "thống lĩnh",
        "thần khí", "hỏa thạch", "hoả thạch", "huyền minh", "ngũ hợp", "tiến giai", "tụ hồn đan",
        "tiên đan", "tiên dược", "huyết linh thảo", "huyết bồ đề", "huyết bổ đề", "nhân sâm",
        "bát bảo", "bách niên giai lão", "phu thê", "nguyệt linh", "ám khí", "kim cang", "kim cô",
        "bát giới", "sa tăng", "tề thiên", "tứ bình", "tứ nhạc", "tứ khoái", "tứ linh", "tóc",
        "ma pháp", "thủ ma", "thủ vật", "nhất phẩm", "ngũ phẩm", "nội tại", "thiên nhãn",
        "càn khôn", "phí huyết", "yểm bùa", "bạch cốt", "đương khang", "mãnh hổ", "tiên hạc",
        "bạch mã", "phượng hoàng", "kỳ lân", "kì lân", "lân sư", "xích thố", "thiên lý mã",
        "lạc long quân", "âu cơ", "thạch sanh", "hằng nga", "chú cuội", "hồng hài nhi",
        "voldemort", "voldermort", "harry potter", "dracula", "tiểu long nữ", "dương quá",
        "ngưu lang", "chức nữ", "sơn tinh", "mị nương", "ông địa", "ngạ quỷ", "dạ xoa",
        "phúc thú", "thần tài", "chí tôn", "tông sư", "hảo hán", "thiên hạ", "trừ gian",
        "trượng nghĩa", "cường thân",
        // Thuat ngu chuan (docs/pokemon/THUAT_NGU.md): moi tu cu con lai trong chu nguoi choi/admin thay.
        "phi phong", "liên đấu", "công thành", "thành chủ", "thần thú", "ngựa", "vũ khí", "giáp trụ",
        "trưởng lão", "thần hành phù", "tiềm năng đan", "kỹ năng đan", "linh đan", "nội đan",
        "công lực", "luyện công", "chân khí", "tâm pháp", "kinh mạch", "khinh công", "chưởng môn",
        "bang phái", "sư huynh", "sư đệ", "sư muội", "sư tỷ", "sư tỉ", "nguyên soái", "các hạ",
        "tiêu cục", "khách điếm", "tửu lâu", "ma đầu", "yêu quái", "thổ phỉ", "đạo tặc", "sơn tặc",
        "ngân lượng", "nguyên bảo", "võ đài", "tỷ võ", "tỉ võ", "luận kiếm",
        "đại đao", "lưỡng nhận đao", "kiếm phi kinh thiên", "xuống ngựa",
        // Khoa DOI kieu kiem hiep / co trang khac (ten NPC, vat pham, pet cu, danh hieu, ngu hanh)
        "hộ vệ", "tiểu đệ", "thần kiếm", "kim bài", "chuyến tiêu", "trả tiêu", "nhận tiêu", "đổi tiêu",
        "màu tiêu", "thế tiêu", "con tiêu", "trứng ph băng", "vạn tiên trận",
        "chiếm được thành", "chiến thành", "giữ thành", "sở hữu thành", "cổng thành",
        "thành trấn danh", "quốc chiến", "nhà vua", "thổ địa phù", "nhị phẩm", "tam phẩm", "tứ phẩm",
        "ngọc bội", "hộ uyển", "đấu sỹ", "hệ mộc", "hệ hỏa", "hệ hoả", "hệ thổ", "hệ kim", "hệ thủy",
        "hệ thuỷ", "kháng hỏa", "kháng hoả", "kháng thủy", "kháng thuỷ", "kháng mộc", "kháng kim",
        "kháng thổ", "duy ngã độc tôn", "tuyệt thế vô song", "nghịch thiên nhi hành", "độc bá nhất phương",
        "chân mệnh thiên tử", "chân mên thiên tử", "cửu huyền thất tổ", "hủy thiên diệt địa",
        "khai thiên thần kiếm", "chiến thần bất diệt", "long tiễn chấn thiên", "nhất tiễn phá thiên",
        "nhất dương tiễn", "dì út", "bà tám", "anh bảy", "hắc ngưu", "thiết bì", "phú ông", "phú ồng",
        "nhật thương nhân", "hỏa xích", "hoả xích", "hai minh", "địa chủ", "giáp sư",
        "bội châu", "lộc phát", "nhất giáp", "nhị giáp", "tam giáp", "tứ giáp", "ngũ giáp", "nhất ngưu",
        "nhị ngưu", "tam ngưu", "tứ ngưu", "ngũ ngưu", "quạt ba tiêu", "gậy như ý", "hỗn nguyên",
        "hoá công", "hóa công", "đoạn cốt", "hoả dược", "hỏa dược", "vĩnh kết đồng tâm",
        "loan phụng hòa minh", "loan phụng hoà minh", "nguyệt ảnh", "mảnh lan", "mảnh cúc", "mảnh trúc",
        "mảnh sáo", "mảnh quạt", "mảnh sênh", "mảnh đàn", "mảnh ngư", "mảnh tiều", "mảnh canh",
        "mảnh độc", "mảnh long", "mảnh lân", "mảnh quy", "mảnh phụng", "đại bạch thử", "bạch thử",
        "rồng băng", "sư tử băng", "hắc thỏ", "hắc thố", "hắc lang", "bạch mi", "thanh tước",
        "xích cước", "lôi công", "bá thiên hổ", "ngọc nữ", "kim đồng", "nghê thường", "thần nữ",
        "tiên nữ", "tinh quân", "thầy đồ", "yếm đào", "áo yếm", "tiên lộc", "xích dương", "chiến lang",
        "thuồng luồng", "miêu quý tộc", "miêu mắt xanh", "ngạ khuyển", "thi khiển", "anh hùng xuất thiếu niên",
    };

    /** Cum tu chua tu cam nhung la ten that trong Pokemon (giu nguyen). */
    public static final String[] CHO_PHEP = {
        "múa kiếm", // Swords Dance - ten chieu Pokemon tieng Viet
        "vào thành phố", // khoa DOI vao thanh (-> vao Gym) khong duoc an vao thanh pho
        "giữ thành phố",
        "hệ kim cương", // khoa DOI he Kim (-> he Thep) khong an vao kim cuong (Phien da he kim cuong)
    };

    /** {cu, moi}. Thu tu khong quan trong: khoa dai duoc thu truoc. */
    public static final String[][] DOI = {
        // Doi (bang hoi)
        {"bang hội", "đội"},
        {"bang chủ", "đội trưởng"},
        {"chủ bang", "đội trưởng"},
        {"phó bang", "đội phó"},
        {"bang chúng", "thành viên đội"},
        {"quý bang", "quý đội"},
        {"trưởng lão", "cố vấn"},
        {"bang", "đội"},
        // Vung (lanh tho)
        {"lãnh thổ", "vùng"},
        {"L.thổ", "Vùng"},
        // So tong dai cua game cu (TeaMobi) gui luc dang nhap, client ve "Hotline: <so>"
        {"Hotline", "Hỗ trợ"},
        {"19006610", "trang web game"},
        {"thanh long", "Kanto"},
        {"hắc hổ", "Johto"},
        {"huyền vũ", "Hoenn"},
        // Thu cuoi
        {"linh thú", "thú cưỡi"},
        {"cưỡi linh thú", "cưỡi thú"},
        // Nhan vat / dong Pokemon
        {"nhân vật", "Pokémon"},
        {"môn phái", "dòng Pokémon"},
        {"kiếm khách", "dòng Charmander"},
        {"kiếm sĩ", "dòng Charmander"},
        {"kiếm sỹ", "dòng Charmander"},
        {"chiến binh", "dòng Bulbasaur"},
        {"pháp sư", "dòng Eevee"},
        {"đấu sĩ", "dòng Squirtle"},
        {"cung thủ", "dòng Pichu"},
        {"hiệp khách", "nhà thám hiểm"},
        {"đại hiệp", "nhà huấn luyện"},
        {"thiếu hiệp", "nhà huấn luyện trẻ"},
        {"nghĩa sỹ", "nhà huấn luyện"},
        {"nghĩa sĩ", "nhà huấn luyện"},
        {"làng nghĩa sỹ", "làng huấn luyện"},
        {"làng nghĩa sĩ", "làng huấn luyện"},
        {"cao thủ", "nhà vô địch"},
        {"tiền bối", "bậc đàn anh"},
        {"đệ tử", "học trò"},
        {"sư phụ", "thầy"},
        {"tại hạ", "tôi"},
        {"huynh", "anh"},
        {"tiểu đệ", "em"},
        // The gioi
        {"võ lâm chí tôn", "nhà vô địch Pokémon"},
        {"giang hồ", "thế giới Pokémon"},
        {"võ lâm", "thế giới Pokémon"},
        {"kiếm hiệp", "Pokémon"},
        {"bôn ba giang hồ", "phiêu lưu khắp thế giới Pokémon"},
        {"bôn tẩu", "phiêu lưu"},
        {"hành tẩu", "phiêu lưu"},
        // Suc manh / chieu
        {"nội công", "năng lượng"},
        {"nội lực", "năng lượng"},
        {"võ công", "chiêu thức"},
        {"bí kíp", "đĩa chiêu"},
        {"thần binh", "bảo vật"},
        {"hệ ngũ hành", "hệ"},
        // Ngu hanh cu trong mo ta (skin thu cuoi...) -> he Pokemon gan nhat
        {"hệ Mộc", "hệ Cỏ"},
        {"hệ Hỏa", "hệ Lửa"},
        {"hệ Hoả", "hệ Lửa"},
        {"hệ Thổ", "hệ Đất"},
        {"hệ Kim", "hệ Thép"},
        {"hệ Thủy", "hệ Nước"},
        {"hệ Thuỷ", "hệ Nước"},
        {"ngũ hành", "hệ"},
        // Vu khi cu (bang 6.4: Kiem -> Than lua, Dao -> Hat giong than)
        {"=Kiếm", "Than lửa"},
        {"thanh kiếm", "lưỡi năng lượng"},
        {"đại đao", "hạt giống thần"},
        {"đao", "hạt giống thần"},
        {"thần kiếm", "thần hỏa"},
        // NPC (bang docs/pokemon/bang_npc.csv)
        {"xa phu", "Pidgeot (Bay)"},
        {"thần hành phù", "vé bay Pidgeot"},
        {"kim bài", "huy hiệu lập đội"},
        {"lâm tướng quân", "Blaziken"},
        {"tướng quân", "chỉ huy"},
        {"trần thống lĩnh", "Heracross"},
        {"kiếm sư", "Farfetch'd"},
        {"dì út hp", "Y tá Chansey"},
        {"dì út", "Y tá Chansey"},
        {"bà tám tạp hóa", "Kecleon tạp hoá"},
        {"bà tám tạp hoá", "Kecleon tạp hoá"},
        {"bà tám", "Kecleon"},
        {"hắc ngưu", "Machamp"},
        {"thiết bì", "Bronzong"},
        {"phú ông", "Persian"},
        {"phú ồng", "Persian"},
        {"anh bảy", "Kangaskhan"},
        {"nhật thương nhân", "Meowth thương nhân"},
        {"hỏa xích", "Torkoal"},
        {"hoả xích", "Torkoal"},
        // Ngoc (ep ngoc)
        {"ép ngọc", "ép ngọc hệ"},
        {"ép ngọc hệ", "ép ngọc hệ"}, // giu nguyen - de doi 2 lan khong thanh "ép ngọc hệ hệ"

        // ======== DOT RA SOAT CHU (docs/pokemon/DUYET_CHU.md) ========
        // Cach xung ho / loi thoai kieu kiem hiep
        {"ngươi", "cậu"},
        {"giết", "hạ"},
        {"yêu ma quỷ quái", "Pokémon hung dữ"},
        {"quái vật yêu ma", "Pokémon hung dữ"},
        {"yêu ma", "Pokémon hung dữ"},
        {"cương thi", "Pokémon ma"},
        {"nội thương", "vết thương"},
        {"vận nội kình", "dồn năng lượng"},
        {"vận kình lực", "dồn sức"},
        {"nội kình", "năng lượng"},
        {"kình lực", "sức mạnh"},
        {"đấng anh hào, ẩn sỹ", "nhà huấn luyện tài ba"},
        {"anh hào", "nhà huấn luyện tài ba"},
        {"ẩn sỹ", "bậc thầy"},
        {"ẩn sĩ", "bậc thầy"},
        {"anh em kết nghĩa", "bạn thân"},
        {"hai minh", "Xatu"},
        {"anh hùng xuất thiếu niên", "tuổi trẻ tài cao"},
        {"phu quân", "bạn đời"},
        {"phu nhân", "bạn đời"},
        // Nhiem vu (ten cu kieu "hanh hiep")
        {"cường thân kiện thể", "rèn luyện thể lực"},
        {"trừ gian diệt ác", "dẹp loạn khu hoang dã"},
        {"hành hiệp trượng nghĩa", "giúp đỡ mọi người"},
        {"trừ hại cho dân", "bảo vệ dân làng"},
        {"trừ khử yêu ma", "đẩy lùi Pokémon hung dữ"},
        {"việc nghĩa hiệp", "việc tốt"},
        // Van tieu -> giao hang
        {"ngựa vận tiêu", "Ponyta giao hàng"},
        {"vận tiêu", "giao hàng"},
        {"chuyến tiêu", "chuyến giao hàng"},
        {"trả tiêu", "trả hàng"},
        {"nhận tiêu", "nhận hàng"},
        {"đổi tiêu", "đổi hàng"},
        {"màu tiêu", "màu hàng"},
        {"nhận được tiêu", "nhận được hàng"},
        {"đổi được tiêu", "đổi được hàng"},
        {"=Tiêu của bạn", "Hàng của bạn"},
        {"cướp tiêu", "cướp hàng"},
        {"thế tiêu", "thế chỗ hàng"},
        {"con tiêu", "kiện hàng"},
        {"hộ vệ lệnh lân", "vé gọi Suicune hỗ trợ"},
        {"trứng ph băng", "trứng Articuno"},
        {"bảo tiêu", "giao hàng"},
        {"xa tiêu lệnh", "vé xe giao hàng"},
        {"đi tối đa 5 tiêu", "giao tối đa 5 chuyến hàng"},
        // Lenh bai / ho ve
        {"lệnh bài trấn yêu", "vé Mê cung"},
        {"vạn tiên trận", "Mê cung"},
        {"trấn yêu trận", "Mê cung"},
        {"trấn yêu", "Mê cung"},
        {"long lệnh bài", "thẻ Rayquaza"},
        {"lân lệnh bài", "thẻ Suicune"},
        {"quy lệnh bài", "thẻ Kyogre"},
        {"phụng lệnh bài", "thẻ Ho-Oh"},
        {"lệnh bài", "thẻ"},
        {"thẻ bài", "thẻ"},
        {"hộ vệ lệnh", "vé gọi Pokémon hỗ trợ"},
        {"lân sư tử hộ vệ", "Suicune hỗ trợ"},
        {"hộ vệ", "Pokémon hỗ trợ"},
        // Chiem thanh / chuc vu vung
        {"sở hữu thành trấn danh", "sở hữu Cao nguyên Indigo"},
        {"thành trấn danh", "Cao nguyên Indigo"},
        {"chiếm được thành", "chiếm được Gym"},
        {"chiếm thành", "chiếm Gym"},
        {"chiến thành", "chiến Gym"},
        {"vào thành", "vào Gym"},
        {"giữ thành", "giữ Gym"},
        {"sở hữu thành", "sở hữu Gym"},
        {"thành liên đấu", "sân Liên minh"},
        {"cổng thành", "cổng Gym"},
        {"lôi đài", "sàn đấu"},
        {"đức vua", "nhà vô địch vùng"},
        {"nhà vua", "Meowth"},
        {"vua", "nhà vô địch vùng"},
        {"đại thần", "cố vấn vùng"},
        {"bộ đầu", "cảnh sát vùng"},
        {"phế chức", "bãi nhiệm"},
        {"=Phế", "Bãi nhiệm"},
        {"quốc gia", "vùng"},
        {"quốc chiến", "chiến vùng"},
        {"chiến trường chính tà", "chiến trường liên vùng"},
        {"chính tà", "liên vùng"},
        {"chiến trường tống kim", "chiến trường liên vùng"},
        {"tống kim", "liên vùng"},
        {"cừu nhân", "đối thủ"},
        {"liên trảm", "chuỗi thắng"},
        {"đồ sát", "PK"},
        {"long trụ", "trụ năng lượng"},
        {"thống lĩnh", "chỉ huy"},
        // Bao vat / nguyen lieu
        {"thần khí", "bảo vật huyền thoại"},
        {"quạt ba tiêu", "Quạt lá Shiftry"},
        {"gậy như ý", "Gậy xương Marowak"},
        {"lưỡng nhận đao", "Lưỡi liềm Scyther"},
        {"hỏa thạch", "nham thạch"},
        {"hoả thạch", "nham thạch"},
        {"ngọc huyền minh", "ngọc hoàng hôn"},
        {"huyền minh", "hoàng hôn"},
        {"ngũ hợp", "cầu vồng"},
        {"tiến giai", "thăng bậc"},
        {"tụ hồn đan", "Kẹo Poké"},
        {"tiên đan", "cỏ hồi sinh"},
        {"tiên dược", "kẹo kinh nghiệm"},
        {"tiềm năng đan", "kẹo tiềm năng"},
        {"kỹ năng đan", "kẹo kỹ năng"},
        {"huyết linh thảo", "Kẹo thú cưng"}, // khong trung "Kẹo hiếm" (keo EXP 1214, kiem_item_mo_coi)
        {"huyết bồ đề", "Bạc hà tính cách"},
        {"huyết bổ đề", "Bạc hà tính cách"},
        {"nhân sâm", "Berry Razz vàng"},
        {"được sâm", "được Berry Razz vàng"},
        {"thổ địa phù", "vé bay Pidgeot"},
        {"thiên lý mã", "Ponyta"},
        {"xích thố", "Ponyta lửa"},
        {"hỗn độn", "X Phòng thủ"},
        {"hỗn nguyên", "X Tấn công"},
        {"hoá công", "Thuốc săn trùm"},
        {"hóa công", "Thuốc săn trùm"},
        {"đoạn cốt", "Ether chiến trường"},
        {"hoả dược", "Bom lửa"},
        {"hỏa dược", "Bom lửa"},
        {"rương bát bảo", "rương kho báu"},
        {"bách niên giai lão", "trái tim đồng"},
        {"vĩnh kết đồng tâm", "trái tim bạc"},
        {"loan phụng hòa minh", "trái tim vàng"},
        {"loan phụng hoà minh", "trái tim vàng"},
        {"phù phu thê", "Vảy trái tim"},
        {"nguyệt linh trượng", "Gậy Ánh Trăng"},
        {"nguyệt ảnh", "Ánh Trăng"},
        {"ám khí", "đòn đánh lén"},
        {"phi phong khổng tước", "ruy băng lông công"},
        {"kim cang long", "Dialga"},
        {"kim cang bát giới", "vòng tay Emboar"},
        {"kim cang sa tăng", "vòng tay Swampert"},
        {"kim cang", "Thu mình"}, // ky nang pet "Kim cang" (+% phong thu) = chieu Withdraw
        {"kim cô", "vòng đầu"},
        {"bát giới", "Emboar"},
        {"sa tăng", "Swampert"},
        {"tề thiên đại thánh", "Infernape"},
        {"tề thiên", "Infernape"},
        // Bo manh suu tap (tu binh / tu nhac / tu khoai / tu linh)
        {"tứ bình", "bộ Berry"},
        {"tứ nhạc", "bộ Flute"},
        {"tứ khoái", "bộ huy hiệu"},
        {"tứ linh", "bộ huyền thoại"},
        {"mảnh mai", "mảnh Berry Oran"},
        {"mảnh lan", "mảnh Berry Pecha"},
        {"mảnh cúc", "mảnh Berry Cheri"},
        {"mảnh trúc", "mảnh Berry Rawst"},
        {"mảnh sáo", "mảnh Blue Flute"},
        {"mảnh quạt", "mảnh Red Flute"},
        {"mảnh sênh", "mảnh Yellow Flute"},
        {"mảnh đàn", "mảnh White Flute"},
        {"mảnh ngư", "mảnh huy hiệu Cascade"},
        {"mảnh tiều", "mảnh huy hiệu Rainbow"},
        {"mảnh canh", "mảnh huy hiệu Boulder"},
        {"mảnh độc", "mảnh huy hiệu Marsh"},
        {"mảnh long", "mảnh Rayquaza"},
        {"mảnh lân", "mảnh Suicune"},
        {"mảnh quy", "mảnh Kyogre"},
        {"mảnh phụng", "mảnh Ho-Oh"},
        // Kieu toc -> kieu long (Pokemon khong co toc)
        {"tóc bờm ngựa", "bờm Ponyta"},
        {"tóc tiên đồng", "lông Clefairy"},
        {"tóc thư sinh", "lông gọn gàng"},
        {"đầu đinh", "lông đinh"},
        {"tóc", "lông"},
        {"xuống ngựa", "xuống thú cưỡi"},
        {"cưỡi ngựa", "cưỡi thú"},
        {"=Ngựa sẽ bị mất", "Thú cưỡi sẽ bị mất"},
        // Ten o trang bi / vu khi theo dong (bang 6.4)
        {"ngọc bội", "phiến đá hệ"},
        {"dây chuyền", "vòng cổ"},
        {"hộ uyển", "vòng chân"},
        {"=từ Bút", "từ Khăn lụa"},
        {"=từ Búa", "từ Nước thần"},
        {"=từ Cung", "từ Nam châm"},
        {"=Bút cấp độ", "Khăn lụa cấp độ"},
        {"=Búa cấp độ", "Nước thần cấp độ"},
        {"=Cung cấp độ", "Nam châm cấp độ"},
        {"đũa thần", "Khăn lụa"},
        {"đấu sỹ", "dòng Squirtle"},
        // Vat ly / ma phap -> vat ly / dac biet (cach goi chi so cua Pokemon)
        {"thủ ma pháp", "phòng thủ đặc biệt"},
        {"thủ vật lý", "phòng thủ"},
        {"thủ ma", "phòng thủ đặc biệt"},
        {"thủ vật", "phòng thủ"},
        {"ma pháp", "đặc biệt"},
        {"st ma", "st đặc biệt"},
        {"tc ma", "tc đặc biệt"},
        {"pt ma", "PT đặc biệt"},
        {"kháng hỏa", "kháng Lửa"},
        {"kháng hoả", "kháng Lửa"},
        {"kháng thủy", "kháng Nước"},
        {"kháng thuỷ", "kháng Nước"},
        {"kháng mộc", "kháng Cỏ"},
        {"kháng kim", "kháng Thép"},
        {"kháng thổ", "kháng Đất"},
        {"nhất phẩm", "hạng 1"},
        {"nhị phẩm", "hạng 2"},
        {"tam phẩm", "hạng 3"},
        {"tứ phẩm", "hạng 4"},
        {"ngũ phẩm", "hạng 5"},
        // Ky nang pet -> chieu / dac tinh Pokemon
        {"phá triền", "Bảo vệ"},
        {"đoạt mệnh", "Dồn sức"},
        {"thiên nhãn", "Phân thân"},
        {"tiên khí", "Màn bảo hộ"},
        {"càn khôn", "Màn ánh sáng"},
        {"cường thân", "Tĩnh tâm"},
        {"hộ thể", "Giáp sắt"},
        {"thuấn ảnh", "Hồi năng lượng"},
        {"phí huyết", "Rễ bám"},
        {"yểm bùa", "nguyền rủa"},
        {"nội tại", "đặc tính"},
        // Thu cuoi goi mot minh (ngoai "linh thú X" o TEN_THU_CUOI; "hắc ngưu" mot minh la NPC)
        {"bạch cốt mã", "Spectrier"},
        {"bạch cốt", "Spectrier"},
        {"đương khang băng giá", "Mamoswine"},
        {"đương khang", "Piloswine"},
        {"đường khang", "Piloswine"},
        {"đuong khang", "Piloswine"},
        {"đại bạch thử", "Wooloo"},
        {"bạch thử", "Sandshrew"},
        {"mãnh hổ", "Arcanine"},
        {"sói xám", "Mightyena"},
        {"tiên hạc", "Pidgeot"},
        {"bạch mã", "Rapidash"},
        {"tuần lộc", "Stantler"},
        {"rồng băng", "Dragonite"},
        {"sư tử băng", "Glastrier"},
        {"nhận được sư tử", "nhận được Entei"},
        {"phượng hoàng lửa", "Moltres"},
        {"phượng hoàng băng", "Articuno"},
        {"phượng hoàng đa sắc", "Ho-Oh"},
        {"phượng hoàng mộc", "Tropius"},
        {"phượng hoàng kim", "Skarmory"},
        {"phượng hoàng thổ", "Flygon"},
        {"phượng hoàng", "Moltres"},
        {"hoả kỳ lân", "Ponyta"},
        {"hỏa kỳ lân", "Ponyta"},
        {"hoả kì lân", "Ponyta"},
        {"hỏa kì lân", "Ponyta"},
        {"hoả kì nhông", "Ponyta"},
        {"hắc thỏ", "Lopunny"},
        {"hắc thố", "Lopunny"},
        {"lồng đèn lân sư tử", "Litleo"},
        {"lân sư tử", "Suicune"},
        {"lân sư", "Suicune"},
        {"pet lân", "pet Suicune"},
        {"hình lân", "hình Suicune"},
        {"lân cấp", "Suicune cấp"},
        // Pet kieu cu (docs/pokemon/bang_pet_cu.csv)
        {"lồng đèn xếp", "Litwick"},
        {"thanh tước", "Taillow"},
        {"cánh cam", "Ledyba"},
        {"dơi quỷ", "Zubat"},
        {"tim bay", "Luvdisc"},
        {"lồng đèn hoa sen", "Lotad"},
        {"=Lồng đèn ngôi sao", "Staryu"},
        {"=lồng đèn ngôi sao", "Staryu"},
        {"=Lồng đèn Ngôi sao", "Starmie"},
        {"pet hoa tuyết", "pet Cryogonal"},
        {"pet giáp ngọ", "pet Ponyta"},
        {"pet armadillo băng", "pet Sandshrew"},
        {"pet armadillo sét", "pet Pachirisu"},
        {"pet armadillo độc", "pet Nidoran"},
        {"lồng đèn cá chép", "Magikarp"},
        {"chim cánh cụt", "Piplup"},
        {"pet ất mùi", "pet Mareep"},
        {"ngôi sao băng", "Jirachi"},
        {"lồng đèn trăng khuyết", "Lunatone"},
        {"lồng đèn rồng xanh", "Articuno"},
        {"khỉ vàng", "Aipom"},
        {"thỏ bay", "Mareep"},
        {"trứng cú", "trứng Hoothoot"},
        {"trứng rồng", "trứng Dratini"},
        {"trứng yêu tinh", "trứng Cleffa"},
        {"trứng dơi", "trứng Zubat"},
        {"trứng đại bàng", "trứng Rufflet"},
        // Thoi trang / cai trang / skin -> Pokemon
        {"lạc long quân", "Rayquaza"},
        {"âu cơ", "Ho-Oh"},
        {"thầy đồ", "giáo sư"},
        {"tiên nữ", "Lunala"},
        {"tinh quân", "Solgaleo"},
        {"áo yếm đào", "áo Bellossom"},
        {"áo yếm", "áo Bellossom"},
        {"yếm đào", "Bellossom"},
        {"thạch sanh", "Lucario"},
        {"dracula", "Darkrai"},
        {"hằng nga", "Clefairy"},
        {"chú cuội", "Clefable"},
        {"đầu bí ngô", "đầu Pumpkaboo"},
        {"mặt nạ bí ngô", "mặt nạ Pumpkaboo"},
        {"nón phù thủy", "nón Mismagius"},
        {"nón phù thuỷ", "nón Mismagius"},
        {"phù thủy dễ thương", "Gardevoir"},
        {"phù thuỷ dễ thương", "Gardevoir"},
        {"hồng hài nhi", "Magby"},
        {"freddy", "Banette"},
        {"jason", "Dusknoir"},
        {"harry potter", "Alakazam"},
        {"chúa tể voldemort", "Giratina"},
        {"chúa tể voldermort", "Giratina"},
        {"voldemort", "Giratina"},
        {"voldermort", "Giratina"},
        {"hắc lang", "Zoroark"},
        {"bạch mi", "Absol"},
        {"thuý vũ hồng sam", "Oricorio"},
        {"thúy vũ hồng sam", "Oricorio"},
        {"thần nữ", "Gardevoir"},
        {"sơn tinh vương", "Groudon"},
        {"sơn tinh", "Groudon"},
        {"=Thuỷ Tinh", "Kyogre"},
        {"=Thủy Tinh", "Kyogre"},
        {"mị nương công chúa", "Diancie"},
        {"tiểu long nữ", "Latias"},
        {"dương quá", "Latios"},
        {"ngưu lang", "Volbeat"},
        {"chức nữ", "Illumise"},
        {"nghê thường", "Milotic"},
        {"ngọc nữ", "Minun"},
        {"kim đồng", "Plusle"},
        {"xích cước", "Blaziken"},
        {"lôi công", "Zapdos"},
        {"bá thiên hổ", "Raikou"},
        {"miêu mắt xanh", "Glameow"},
        {"miêu quý tộc", "Persian"},
        {"ông địa", "Snorlax"},
        {"ngạ quỷ", "Haunter"},
        {"ngạ khuyển", "Houndoom"},
        {"thi khiển", "Yamask"},
        {"dạ xoa", "Gengar"},
        {"phúc thú", "Copperajah"},
        {"gấu trúc", "Pangoro"},
        {"tiên lộc", "Xerneas"},
        {"xích dương", "Camerupt"},
        {"chiến lang", "Lycanroc"},
        // NPC / boss con ten cu
        {"thần tài", "Meowth"},
        {"mẹ hiền", "Gardevoir"},
        {"cây thông noel", "Abomasnow"},
        {"ông già noel", "Delibird"},
        {"cây lì xì", "Sudowoodo"},
        {"tả thống lĩnh", "Lucario"},
        {"hữu thống lĩnh", "Lucario"},
        {"tả phó thống", "Lucario"},
        {"nhất giáp", "Golem 1"},
        {"nhị giáp", "Golem 2"},
        {"tam giáp", "Golem 3"},
        {"tứ giáp", "Golem 4"},
        {"ngũ giáp", "Golem 5"},
        {"nhất ngưu", "Tauros 1"},
        {"nhị ngưu", "Tauros 2"},
        {"tam ngưu", "Tauros 3"},
        {"tứ ngưu", "Tauros 4"},
        {"ngũ ngưu", "Tauros 5"},
        {"bảo ngọc", "Sableye"},
        {"giáp sư", "Bronzong"},
        {"bội châu", "Persian"},
        {"lộc phát", "Meowth"},
        {"kim hoa", "Kecleon"},
        {"lính gác", "Arcanine gác cổng"},
        {"lão địa chủ", "Persian"},
        {"địa chủ", "Persian"},
        {"ông nội", "ông Snorlax"},
        {"thuồng luồng", "Gyarados"},
        {"đại bàng đã xuất hiện", "Zapdos đã xuất hiện"},
        {"boss người tuyết", "boss Abomasnow"},
        // Danh hieu
        {"duy ngã độc tôn", "Tứ Thiên Vương"},
        {"thiên hạ chí tôn", "Nhà Vô Địch Mọi Vùng"},
        {"chí tôn", "tối thượng"},
        {"hảo hán", "người dũng cảm"},
        {"tông sư", "bậc thầy Pokémon"},
        {"nhất dương tiễn", "Tia Mặt Trời"},
        {"tuyệt thế vô song", "có một không hai"},
        {"chiến thần bất diệt", "Huyền Thoại Bất Diệt"},
        {"long tiễn chấn thiên", "Rayquaza Chấn Thiên"},
        {"nghịch thiên nhi hành", "Kẻ Thách Thức Định Mệnh"},
        {"khai thiên thần kiếm", "Gươm Aegislash"},
        {"vô địch thiên hạ", "Vô Địch Mọi Vùng"},
        {"độc bá nhất phương", "Bá Chủ Một Vùng"},
        {"tung hoành thiên hạ", "Tung Hoành Mọi Vùng"},
        {"tuyệt đỉnh cao thủ", "Tinh Anh Tuyệt Đỉnh"},
        {"nhất tiễn phá thiên", "Tia Chớp Phá Trời"},
        {"chân mên thiên tử", "Người Được Chọn"},
        {"chân mệnh thiên tử", "Người Được Chọn"},
        {"cửu huyền thất tổ", "Truyền Nhân Huyền Thoại"},
        {"hủy thiên diệt địa", "Sức Mạnh Hủy Diệt"},
        {"quy ân giang hồ", "Lão Làng Pokémon"},
        {"thiên hạ đệ nhất", "Số Một Mọi Vùng"},
        {"thiên hạ vô song", "Vô Song Mọi Vùng"},
        {"quân lâm thiên hạ", "Chúa Tể Mọi Vùng"},
        {"thiên hạ", "mọi vùng"},
        {"đệ nhất", "số một"},
        // ======== THUAT NGU CHUAN (docs/pokemon/THUAT_NGU.md, 18/10/2026) ========
        // Phu kien o 19 (BangTrangBi "Ruy băng"), vu khi = do cam tay theo dong
        {"phi phong", "ruy băng"},
        // "ma"/"vật" sau ten phi phong = ma phap / vat ly (dac biet / vat ly)
        {"phi phong ma pháp", "ruy băng đặc biệt"},
        {"phi phong ma", "ruy băng đặc biệt"},
        {"phi phong vật lý", "ruy băng vật lý"},
        {"phi phong vật", "ruy băng vật lý"},
        {"phi phong vat", "ruy băng vật lý"},
        {"phi phong khổng tước ma pháp", "ruy băng lông công đặc biệt"},
        {"phi phong khổng tước ma", "ruy băng lông công đặc biệt"},
        {"phi phong khổng tước vật lý", "ruy băng lông công vật lý"},
        {"phi phong khổng tước vật", "ruy băng lông công vật lý"},
        {"phi phong huyền vũ ma pháp", "ruy băng Hoenn đặc biệt"},
        {"phi phong huyền vũ ma", "ruy băng Hoenn đặc biệt"},
        {"phi phong huyền vũ vật lý", "ruy băng Hoenn vật lý"},
        {"phi phong huyền vũ vật", "ruy băng Hoenn vật lý"},
        {"vũ khí thần binh", "bảo vật cầm tay"},
        {"vũ khí thời trang", "đồ cầm tay thời trang"},
        {"vũ khí", "đồ cầm tay"},
        {"giáp trụ", "lớp giáp"},
        // Lien dau (giai lien may chu) -> giai Lien minh (Pokemon League), thanh lien dau -> san Lien minh; cong thanh -> chiem Gym
        {"liên đấu", "giải Liên minh"},
        {"công thành", "chiếm Gym"},
        {"thành chủ", "trưởng Gym"},
        {"top cao thủ", "top sức mạnh"},
        {"võ đài", "sàn đấu"},
        {"tỷ võ", "đấu tập"},
        {"tỉ võ", "đấu tập"},
        {"luận kiếm", "đấu tập"},
        // Thu cuoi / di chuyen
        {"thần thú", "Pokémon huyền thoại"},
        {"đi xe ngựa", "bay Pidgeot"},
        {"xe ngựa", "chuyến bay Pidgeot"},
        {"ngựa", "thú cưỡi"},
        {"khinh công", "chạy nhanh"},
        // Luyen tap / suc manh
        {"luyện công", "luyện tập"},
        {"công lực", "sức mạnh"},
        {"chân khí", "năng lượng"},
        {"tâm pháp", "bí quyết"},
        {"kinh mạch", "thể trạng"},
        // Ten chieu cu con trong SKILL_NAME client (bang chieu ap_ten_chieu.py)
        {"hồi công lực đan", "Mong ước"},
        {"kiếm phi kinh thiên", "Đại hỏa"},
        // Dan duoc
        {"linh đan", "thuốc hồi phục"},
        {"nội đan", "lõi năng lượng"},
        // Nguoi / to chuc
        {"chưởng môn", "người đứng đầu"},
        {"bang phái", "đội"},
        {"sư huynh", "bạn học"},
        {"sư đệ", "bạn học"},
        {"sư muội", "bạn học"},
        {"sư tỷ", "bạn học"},
        {"sư tỉ", "bạn học"},
        {"nguyên soái", "chỉ huy"},
        {"các hạ", "cậu"},
        {"tiêu cục", "trạm giao hàng"},
        {"khách điếm", "nhà trọ"},
        {"tửu lâu", "quán ăn"},
        {"ma đầu", "kẻ xấu"},
        {"yêu quái", "Pokémon hung dữ"},
        {"thổ phỉ", "kẻ trộm"},
        {"đạo tặc", "kẻ trộm"},
        {"sơn tặc", "kẻ trộm"},
        // Tien te kieu cu
        {"ngân lượng", "xu"},
        {"nguyên bảo", "lượng"},
        // Dia danh viet thuong (TenMapPokemon.BANG so khop phan biet hoa thuong)
        {"đông sơn lâm", "Đông Rừng Viridian"},
        {"đông sơn nam", "Đông Pewter"},
        {"sơn lâm", "Rừng Viridian"},
        {"đông sơn", "Saffron"},
        {"sơn nam", "Pewter"},
        {"dao châu", "Pallet"},
        {"tiên du", "Viridian"},
        {"kỳ bố", "Cerulean"},
        {"phù liệt", "Vermilion"},
        {"hàm tử", "Lavender"},
        {"thạch giang", "Celadon"},
        {"tử quan", "Fuchsia"},
        {"trường giang", "Cinnabar"},
        {"phong châu", "Núi Mặt Trăng"},
        {"bình lục", "Vùng Safari"},
        {"lộc trĩ", "New Bark"},
        {"bảo thái", "Cherrygrove"},
        {"hồi hồ", "Hồ Phẫn Nộ"},
        {"siêu loại", "Violet"},
        {"cẩm khê", "Azalea"},
        {"bạch hạc", "Goldenrod"},
        {"tế giang", "Ecruteak"},
        {"đỗ đông", "Olivine"},
        {"bình kiều", "Mahogany"},
        {"dương đông", "Blackthorn"},
        {"lâm tây", "Cianwood"},
        {"chiến hạm", "Tàu S.S. Anne"},
        {"hang mãng xà", "Hang Serperior"},
        {"hang thằn lằn", "Hang Salamence"},
        {"hang đại bàng", "Tổ Zapdos"},
        {"cánh rừng đông", "Rừng Ilex"},
        {"rừng già", "Rừng Cổ"},
    };

    /** Ten thu cuoi cu -> Pokemon (docs/pokemon/bang_thu.csv). Dung sau "linh thú "/"trứng ". */
    public static final String[][] TEN_THU_CUOI = {
        {"bạch mã", "Rapidash"},
        {"mãnh hổ", "Arcanine"},
        {"hắc ngưu", "Tauros"},
        {"sói xám", "Mightyena"},
        {"tiên hạc", "Pidgeot"},
        {"phượng hoàng lửa", "Moltres"},
        {"phượng hoàng băng", "Articuno"},
        {"phượng hoàng đa sắc", "Ho-Oh"},
        {"phượng hoàng mộc", "Tropius"},
        {"phượng hoàng kim", "Skarmory"},
        {"phượng hoàng thổ", "Flygon"},
        {"phượng hoàng", "Moltres"},
        {"tuần lộc", "Stantler"},
        {"đại bạch thử", "Wooloo"},
        {"bạch thử", "Wooloo"},
        {"hắc thỏ", "Lopunny"},
        {"lân sư tử", "Suicune"},
        {"lân", "Suicune"},
        {"đương khang băng giá", "Mamoswine"},
        {"đương khang", "Piloswine"},
        {"bạch cốt mã", "Spectrier"},
        {"bạch cốt", "Spectrier"},
        {"rồng băng", "Dragonite"},
        {"sư tử băng", "Glastrier"},
        {"sư tử", "Entei"},
        {"hoả kỳ lân", "Ponyta"},
        {"hỏa kỳ lân", "Ponyta"},
    };

    /** Ten NPC tinh theo type 0..31 (thu tu NPC.cs / bang_npc.csv). */
    public static final String[] TEN_NPC = {
        "Y tá Chansey", "Kecleon tạp hoá", "Machamp thợ rèn", "Bronzong thợ giáp", "Arcanine gác cổng",
        "Trưởng làng Xatu", "Persian", "Pidgeot (Bay)", "Ông Snorlax", "Kangaskhan thủ kho",
        "Lapras hoa tiêu", "Golem 1", "Golem 2", "Golem 3", "Golem 4",
        "Golem 5", "Tauros 1", "Tauros 2", "Tauros 3", "Tauros 4",
        "Tauros 5", "Blaziken", "Meowth thương nhân", "Torkoal", "Sableye",
        "Heracross", "Kecleon", "Bronzong", "Farfetch'd", "Persian",
        "Kangaskhan", "Meowth",
    };

    /** CharManager.NAME_CLASS khi bat che do (class 0..4, bang 4.1). */
    public static final String[] TEN_DONG = {
        "Dòng Charmander", "Dòng Bulbasaur", "Dòng Eevee", "Dòng Squirtle", "Dòng Pichu",
    };

    /** Map.nameCountry / NpcReceiveCard.nameCountry khi bat che do. */
    public static final String[] TEN_VUNG = {"Kanto", "Johto", "Hoenn"};

    /** Text.help khi bat che do (giu 21 dong nhu ban cu). */
    public static final String[] HUONG_DAN = {
        "Phím mũi tên hoặc 2,4,6,8: Di chuyển Pokémon của bạn",
        "Phím 5 hoặc phím chọn giữa: Đánh thường. Phím 1,3: Dùng chiêu khác nếu đã học",
        "Phím 7: Dùng Thuốc hồi máu, Phím 9: Dùng Ether (nếu còn)",
        "Phím chọn trái: Mở Menu",
        "Phím chọn phải: đổi mục tiêu nếu có nhiều đối tượng xung quanh.",
        "Phím #: Chat.",
        "Phím *: mở menu mời nhóm, trao đổi, nhắn tin, kết bạn, xem thông tin, xuống thú cưỡi.",
        "Phím 0: chuyển đổi bảng chiêu.",
        "Bạn phải làm xong nhiệm vụ chào Trưởng làng (nhiệm vụ đầu tiên) mới có thể đấu với Pokémon hoang dã.",
        "Bạn có thể mua vật phẩm đặc biệt bằng cách vào Menu chọn Cửa Hàng.",
        "Bạn có thể mua vật phẩm tại các NPC trong làng.",
        "Khi lên cấp bạn được thưởng 5 điểm tiềm năng và 1 điểm kỹ năng.",
        "Hãy vào mục Bản thân để cộng điểm theo ý muốn.",
        "Pokémon của bạn dưới cấp 10 được bảo hộ và không dùng được chức năng PK.",
        "Hạ 1 người chơi khi PK sẽ bị cộng 100 điểm PK.",
        "Muốn giảm điểm PK, hãy dùng Phiếu công ích mua ở Cửa Hàng hoặc hạ Pokémon hoang dã.",
        "Bạn chỉ có thể nâng cấp vật phẩm khi Pokémon đạt cấp 10.",
        "Đeo khăn PK để bật chức năng chiến đấu.",
        "Khi vật phẩm bị hư hại, hãy đến Machamp thợ rèn để sửa.",
        "Để di chuyển nhanh giữa các vùng, hãy nhờ Pidgeot (Bay).",
        "Để ra đảo, hãy đến bến tàu mua vé và đi tàu.",
    };

    /** Mo ta 3 chieu hoc o NPC theo dong [class][k] (cung thu tu Text.nameSkillLearn). */
    public static final String[][] MO_TA_CHIEU_HOC = {
        {"Phun luồng lửa dữ dội thiêu đốt nhiều mục tiêu trong phạm vi.",
         "Vuốt móng sắc mang sức mạnh rồng, đánh liên tiếp các đối thủ đứng gần nhau.",
         "Phóng ngọn lửa hình chữ Đại thiêu rụi mọi mục tiêu trong vùng ảnh hưởng."},
        {"Bắn loạt hạt giống cứng tấn công nhiều mục tiêu trong phạm vi.",
         "Cuốn lá sắc thành cơn bão xoáy, đánh trúng mọi đối thủ trên đường đi.",
         "Hấp thụ ánh nắng rồi bắn tia năng lượng mặt trời gây sát thương lớn."},
        {"Quật đuôi cứng như thép vào nhiều mục tiêu xung quanh.",
         "Dồn toàn bộ sức lực lao vào đối thủ tạo cú va chạm cực mạnh.",
         "Bắn tia năng lượng hủy diệt quét qua các mục tiêu phía trước."},
        {"Quất đuôi tạo sóng nước tấn công nhiều mục tiêu trong phạm vi.",
         "Phun dòng nước áp lực cao xoáy tròn công phá đối thủ.",
         "Bắn pháo nước khổng lồ cuốn trôi mọi mục tiêu xung quanh."},
        {"Tung lưới điện bủa vây, gây sát thương nhiều mục tiêu.",
         "Gọi sấm sét giáng xuống, tia điện lan sang các đối thủ đứng gần nhau.",
         "Bọc mình trong điện rồi lao thẳng vào đối thủ với sức công phá khủng khiếp."},
    };

    /** Ten vat pham trong data.Text doi rieng (doi theo tu dien khong du hay). {ten truong, gia tri}. */
    public static final String[][] TEXT_RIENG = {
        {"HP_VUA", " Siêu thuốc hồi máu"},
        {"HP_TO", " Thuốc hồi máu cực mạnh"},
        {"HP_NHO", " Thuốc hồi máu cực mạnh"},
        {"MP_VUA", " Ether lớn"},
        {"MP_TO", " Ether tối đa"},
        {"THAN_HANH_PHU", "Vé bay Pidgeot"},
        {"THIEN_LY_MA", "Sáo gọi Ponyta"},
        {"KIM_BAI", "Huy hiệu lập đội"},
        {"KIM_BAI_DECRIPT", "Huy hiệu dùng để tạo đội"},
        {"XICH_THO", "Sáo gọi Ponyta lửa"},
        {"HAC_NGUU", "Sáo gọi Tauros"},
        {"MANH_HO", "Sáo gọi Arcanine"},
        {"SOI_XAM", "Sáo gọi Mightyena"},
        {"TIEN_HAC", "Sáo gọi Pidgeot"},
        {"BACH_MA", "Sáo gọi Rapidash"},
        {"THUOC_CUONG_HOA", "Thuốc X"},
        {"THUOC_CUONG_HOA_SUPER", "Thuốc X siêu cấp"},
        {"HP7000", "200 Thuốc hồi máu 7000"},
        {"HP15000", "200 Thuốc hồi máu 15000"},
        {"MP7000", "200 Ether 7000"},
        {"MP15000", "200 Ether 15000"},
    };

    private static final String PHAN_BIET = "=";

    /** Ket qua bien dich bang: mau regex + hai bang tra. */
    private static final class Bang {
        final Pattern mau;
        final HashMap<String, String> khongPhanBiet = new HashMap<>();
        final HashMap<String, String> phanBiet = new HashMap<>();

        Bang(final List<String[]> dong) {
            final List<String[]> sx = new ArrayList<>(dong);
            // Khoa dai thu truoc (alternation cua regex lay nhanh dau tien khop).
            Collections.sort(sx, new Comparator<String[]>() {
                @Override
                public int compare(final String[] a, final String[] b) {
                    return khoa(b[0]).length() - khoa(a[0]).length();
                }
            });
            // Truoc khoa: khong duoc la chu/gach duoi; CHU SO thi duoc - hoi thoai NPC dung "1Giang hồ"
            // ("1" = NPC noi), truoc day so dung ngay truoc lam bo sot ca tu dau cau.
            final StringBuilder sb = new StringBuilder("(?<![\\p{L}_])(?:");
            boolean dau = true;
            for (final String[] d : sx) {
                final String k = khoa(d[0]);
                if (d[0].startsWith(PHAN_BIET)) {
                    if (phanBiet.containsKey(k)) {
                        continue;
                    }
                    phanBiet.put(k, d[1]);
                    sb.append(dau ? "" : "|").append("(?-i:").append(Pattern.quote(k)).append(')');
                } else {
                    final String t = k.toLowerCase(Locale.ROOT);
                    if (khongPhanBiet.containsKey(t)) {
                        continue;
                    }
                    khongPhanBiet.put(t, d[1]);
                    sb.append(dau ? "" : "|").append(Pattern.quote(k));
                }
                dau = false;
            }
            sb.append(")(?![\\p{L}\\p{N}_])");
            mau = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }

    private static volatile Bang bangDoi;
    private static volatile Bang bangCam;

    private ChuPokemon() {
    }

    private static String khoa(final String s) {
        return s.startsWith(PHAN_BIET) ? s.substring(1) : s;
    }

    /**
     * Toan bo cap {cu, moi} theo dung thu tu ghep (tool Python va ban C# ghep y het):
     * CHO_PHEP (giu nguyen), DOI, "linh thú X"/"trứng X" cua TEN_THU_CUOI, roi ten dia danh
     * TenMapPokemon.BANG (phan biet hoa thuong: "Biên Giới" la dia danh, "biên giới" thi khong).
     */
    public static List<String[]> bangDayDu() {
        final List<String[]> d = new ArrayList<>();
        for (final String p : CHO_PHEP) {
            d.add(new String[]{p, p});
        }
        for (final String[] x : DOI) {
            d.add(new String[]{x[0], x[1]});
        }
        for (final String[] x : TEN_THU_CUOI) {
            d.add(new String[]{"linh thú " + x[0], "thú cưỡi " + x[1]});
            d.add(new String[]{"trứng " + x[0], "trứng " + x[1]});
        }
        for (final String[] x : TenMapPokemon.BANG) {
            d.add(new String[]{PHAN_BIET + x[0], x[1]});
        }
        return d;
    }

    private static Bang bangDoi() {
        Bang b = bangDoi;
        if (b == null) {
            b = new Bang(bangDayDu());
            bangDoi = b;
        }
        return b;
    }

    private static Bang bangCam() {
        Bang b = bangCam;
        if (b == null) {
            final List<String[]> d = new ArrayList<>();
            for (final String p : CHO_PHEP) {
                d.add(new String[]{p, ""});
            }
            for (final String t : TU_CAM) {
                d.add(new String[]{t, t});
            }
            b = new Bang(d);
            bangCam = b;
        }
        return b;
    }

    /**
     * Chu hoa theo kieu cua doan goc: VIET HOA het -> viet hoa het; Moi Tu Hoa (>= 2 tu) ->
     * moi tu cua gia tri moi hoa chu dau; chu dau hoa -> chu dau hoa. Ban Python/C# lam y het.
     */
    private static String theoKieu(final String goc, final String moi) {
        if (moi.isEmpty()) {
            return moi;
        }
        final String[] tuGoc = goc.split(" ");
        boolean moiTuHoa = tuGoc.length >= 2;
        for (final String t : tuGoc) {
            if (t.isEmpty() || !Character.isUpperCase(t.charAt(0))) {
                moiTuHoa = false;
            }
        }
        boolean coChu = false;
        boolean hetHoa = true;
        for (int i = 0; i < goc.length(); i++) {
            final char c = goc.charAt(i);
            if (Character.isLetter(c)) {
                coChu = true;
                if (!Character.isUpperCase(c)) {
                    hetHoa = false;
                }
            }
        }
        if (coChu && hetHoa && goc.length() > 1) {
            return moi.toUpperCase(Locale.ROOT);
        }
        if (moiTuHoa) {
            final StringBuilder sb = new StringBuilder(moi.length());
            boolean dauTu = true;
            for (int i = 0; i < moi.length(); i++) {
                final char c = moi.charAt(i);
                sb.append(dauTu ? Character.toUpperCase(c) : c);
                dauTu = c == ' ';
            }
            return sb.toString();
        }
        if (Character.isUpperCase(goc.charAt(0)) && Character.isLowerCase(moi.charAt(0))) {
            return Character.toUpperCase(moi.charAt(0)) + moi.substring(1);
        }
        return moi;
    }

    /** Doi chu bat ke co (cho tool/test). null/rong tra nguyen. */
    public static String doiLuon(final String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        final Bang b = bangDoi();
        final Matcher m = b.mau.matcher(s);
        if (!m.find()) {
            return s;
        }
        final StringBuffer sb = new StringBuffer(s.length() + 16);
        do {
            final String goc = m.group();
            String moi = b.phanBiet.get(goc);
            if (moi == null) {
                moi = b.khongPhanBiet.get(goc.toLowerCase(Locale.ROOT));
                moi = moi == null ? goc : theoKieu(goc, moi);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(moi));
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }

    /** Doi chu khi bat che do Pokemon; tat thi tra nguyen (hanh vi goc cu). */
    public static String doi(final String s) {
        return CheDoPokemon.bat() ? doiLuon(s) : s;
    }

    /** Cac tu cam con trong {@code s} (rong = sach). */
    public static List<String> tuCamTrong(final String s) {
        final List<String> kq = new ArrayList<>();
        if (s == null || s.isEmpty()) {
            return kq;
        }
        final Bang b = bangCam();
        final Matcher m = b.mau.matcher(s);
        while (m.find()) {
            final String g = m.group();
            if (b.phanBiet.containsKey(g)) {
                kq.add(g);
                continue;
            }
            final String v = b.khongPhanBiet.get(g.toLowerCase(Locale.ROOT));
            if (v != null && !v.isEmpty()) {
                kq.add(g);
            }
        }
        return kq;
    }

    /** Mo ta chieu hoc o NPC (goi dang nhap); tat che do hoac ngoai bang thi tra {@code cu}. */
    public static String moTaChieuHoc(final int dong, final int k, final String cu) {
        if (!CheDoPokemon.bat() || dong < 0 || dong >= MO_TA_CHIEU_HOC.length
                || k < 0 || k >= MO_TA_CHIEU_HOC[dong].length) {
            return doi(cu);
        }
        return MO_TA_CHIEU_HOC[dong][k];
    }

    /**
     * Ten thu cuoi dung MOT MINH (Animal.name: "Hắc ngưu", "Sư tử Băng"...) -> ten Pokemon.
     * Phai tra rieng vi trong tu dien "hắc ngưu" dung mot minh la NPC Machamp; chi khi co
     * "linh thú " dang truoc moi la thu cuoi. Tat che do thi tra nguyen.
     */
    public static String tenThuCuoi(final String ten) {
        if (!CheDoPokemon.bat() || ten == null) {
            return ten;
        }
        final String t = ten.trim().toLowerCase(Locale.ROOT);
        for (final String[] d : TEN_THU_CUOI) {
            if (d[0].equals(t)) {
                return d[1];
            }
        }
        return doiLuon(ten);
    }

    /** Ten NPC tinh moi theo type; ngoai bang thi doi theo tu dien. */
    public static String tenNpc(final int type, final String cu) {
        if (!CheDoPokemon.bat()) {
            return cu;
        }
        return type >= 0 && type < TEN_NPC.length ? TEN_NPC[type] : doiLuon(cu);
    }

    // ==================== AP MOT LAN LUC BAT MAY ====================

    private static final List<Runnable> HOAN_TAC = new ArrayList<>();
    private static boolean daAp = false;

    /**
     * Doi cac bang chu dung chung. Goi MOT lan trong TeamServer, SAU Database.startSaveChar()
     * (luc do Map.gemTemplate, Map.potionTemplates2 da nap) va truoc khi nhan nguoi choi.
     * Tat che do thi khong lam gi. Goi lai lan 2 khong doi them (co daAp).
     */
    public static synchronized void ap() {
        if (!CheDoPokemon.bat() || daAp) {
            return;
        }
        daAp = true;
        apText();
        datMang(real.CharManager.NAME_CLASS, TEN_DONG);
        datMang(real.Map.nameCountry, TEN_VUNG);
        datMang(real.NpcReceiveCard.nameCountry, TEN_VUNG);
        apTenThuoc();
        apNgoc();
    }

    /** Chi test dung: tra moi thu ap() da doi ve nhu cu. */
    public static synchronized void hoanTac() {
        for (int i = HOAN_TAC.size() - 1; i >= 0; i--) {
            HOAN_TAC.get(i).run();
        }
        HOAN_TAC.clear();
        daAp = false;
    }

    private static void datMang(final String[] dich, final String[] moi) {
        if (dich == null) {
            return;
        }
        final String[] cu = dich.clone();
        for (int i = 0; i < dich.length && i < moi.length; i++) {
            dich[i] = moi[i];
        }
        HOAN_TAC.add(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(cu, 0, dich, 0, cu.length);
            }
        });
    }

    private static void doiMang(final String[] a) {
        if (a == null) {
            return;
        }
        final String[] cu = a.clone();
        for (int i = 0; i < a.length; i++) {
            a[i] = doiLuon(a[i]);
        }
        HOAN_TAC.add(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(cu, 0, a, 0, cu.length);
            }
        });
    }

    private static void apText() {
        final HashMap<String, String> rieng = new HashMap<>();
        for (final String[] r : TEXT_RIENG) {
            rieng.put(r[0], r[1]);
        }
        for (final Field f : data.Text.class.getDeclaredFields()) {
            final int md = f.getModifiers();
            if (!Modifier.isStatic(md) || Modifier.isFinal(md)) {
                continue;
            }
            // Ten chieu da doi rieng (tools/pokemon/ap_ten_chieu.py) - khong dong vao.
            if ("nameSkillLearn".equals(f.getName())) {
                continue;
            }
            try {
                f.setAccessible(true);
                final Object cu = f.get(null);
                if (f.getType() == String.class) {
                    final String moi = rieng.containsKey(f.getName()) ? rieng.get(f.getName()) : doiLuon((String) cu);
                    if (moi != null && !moi.equals(cu)) {
                        f.set(null, moi);
                        HOAN_TAC.add(datLai(f, cu));
                    }
                } else if (f.getType() == String[].class) {
                    if ("help".equals(f.getName())) {
                        f.set(null, HUONG_DAN.clone());
                        HOAN_TAC.add(datLai(f, cu));
                    } else {
                        doiMang((String[]) cu);
                    }
                } else if (f.getType() == String[][].class && cu != null) {
                    for (final String[] a : (String[][]) cu) {
                        doiMang(a);
                    }
                }
            } catch (final Exception e) {
                System.out.println("[CHU POKEMON] khong doi duoc Text." + f.getName() + ": " + e);
            }
        }
    }

    private static Runnable datLai(final Field f, final Object cu) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    f.set(null, cu);
                } catch (final Exception e) {
                    // chi test goi
                }
            }
        };
    }

    /**
     * LoginHandler.PORTION_NAME (ten thuoc trong thong bao server) lay theo ten CSDL da doi
     * (sql/pokemon_doi_chu.sql), roi qua tu dien (phong khi chua chay SQL).
     */
    private static void apTenThuoc() {
        final String[] ten = real.cmd.LoginHandler.PORTION_NAME;
        if (ten == null) {
            return;
        }
        final String[] cu = ten.clone();
        final java.util.Vector<real.PotionTemplate2> csdl = real.Map.potionTemplates2;
        if (csdl != null) {
            for (final real.PotionTemplate2 p : csdl) {
                if (p != null && p.id >= 0 && p.id < ten.length && p.name != null) {
                    ten[p.id] = p.name;
                }
            }
        }
        for (int i = 0; i < ten.length; i++) {
            ten[i] = doiLuon(ten[i]);
        }
        HOAN_TAC.add(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(cu, 0, ten, 0, cu.length);
            }
        });
    }

    /** Map.gemTemplate (ngoc/da) da chep chu tu Text LUC NAP - doi lai ban chep. */
    private static void apNgoc() {
        final real.GemTemplate[] g = real.Map.gemTemplate;
        if (g == null) {
            return;
        }
        for (final real.GemTemplate t : g) {
            if (t == null) {
                continue;
            }
            final String ten = t.name;
            final String moTa = t.decript;
            t.name = doiLuon(ten);
            t.decript = doiLuon(moTa);
            HOAN_TAC.add(new Runnable() {
                @Override
                public void run() {
                    t.name = ten;
                    t.decript = moTa;
                }
            });
        }
    }
}
