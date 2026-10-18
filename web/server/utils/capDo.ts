/**
 * BẢNG QUY ĐỔI CẤP ĐỘ -> ĐIỂM KINH NGHIỆM.
 *
 * DỮ LIỆU SINH TỰ ĐỘNG — ĐỪNG SỬA TAY.
 *   Nguồn : real.LevelDetail.expMain / expRequire (src/real/LevelDetail.java, repo game HTBB),
 *           xuất ra bằng chương trình Java SinhBangCapDo trong cùng repo đó.
 *   Server game đổi bảng exp thì chạy lại SinhBangCapDo rồi thay hai mảng dưới đây bằng
 *   kết quả mới; không chỉnh từng số bằng tay (lệch một số là lệch cấp của người chơi).
 *
 * Web KHÔNG tự tính exp để ghi tob_char (xem server/utils/lenhNhanVat.ts) — server game
 * tính bằng chính real.LevelDetail. Bảng này chỉ còn dùng để giới hạn cấp (CAPDO_TOI_DA)
 * và để đối chiếu khi cần. MỌI chỉ số khác của nhân vật (sức mạnh/nhanh nhẹn/HP/điểm kỹ
 * năng...) do máy chủ game TỰ TÍNH LẠI từ cấp lúc chọn nhân vật
 * (src/real/cmd/SelectCharHandler.java:206-215, rồi p.calculateAttrib()) — web KHÔNG được
 * và KHÔNG cần đụng vào cột nào khác.
 *
 * Mọi giá trị đều < Number.MAX_SAFE_INTEGER nên dùng number.
 */

/** Cấp độ cao nhất trang quản trị cho phép đặt. */
export const CAPDO_TOI_DA = 90

/**
 * Điểm kinh nghiệm đúng tại LÚC VỪA LÊN cấp độ này (0% vào cấp tiếp theo).
 * Chỉ số mảng = cấp độ - 1, đúng như real.LevelDetail.expMain bên Java.
 */
const EXP_MAIN: readonly number[] = Object.freeze([
  0, // cap 1
  200, // cap 2
  800, // cap 3
  2000, // cap 4
  4000, // cap 5
  8000, // cap 6
  16000, // cap 7
  32000, // cap 8
  48000, // cap 9
  72000, // cap 10
  108000, // cap 11
  162000, // cap 12
  226800, // cap 13
  317520, // cap 14
  444528, // cap 15
  622339, // cap 16
  871274, // cap 17
  1132656, // cap 18
  1472452, // cap 19
  1914187, // cap 20
  2488443, // cap 21
  2986131, // cap 22
  3583357, // cap 23
  4300028, // cap 24
  5160033, // cap 25
  6192039, // cap 26
  7430446, // cap 27
  8916535, // cap 28
  10699842, // cap 29
  12839810, // cap 30
  15407772, // cap 31
  18489326, // cap 32
  22187191, // cap 33
  26624629, // cap 34
  31949554, // cap 35
  38339464, // cap 36
  46007356, // cap 37
  55208827, // cap 38
  66250592, // cap 39
  79500710, // cap 40
  95400852, // cap 41
  114481022, // cap 42
  137377226, // cap 43
  164852671, // cap 44
  197823205, // cap 45
  237387846, // cap 46
  284865415, // cap 47
  341838498, // cap 48
  410206197, // cap 49
  492247436, // cap 50
  590696923, // cap 51
  708836307, // cap 52
  850603568, // cap 53
  1020724281, // cap 54
  1224869137, // cap 55
  1469842964, // cap 56
  1763811556, // cap 57
  2116573867, // cap 58
  2539888640, // cap 59
  3047866368, // cap 60
  3657439641, // cap 61
  4388927569, // cap 62
  5266713082, // cap 63
  6320055698, // cap 64
  7584066837, // cap 65
  9100880204, // cap 66
  10921056244, // cap 67
  13105267492, // cap 68
  15726320990, // cap 69
  18871585188, // cap 70
  22645902225, // cap 71
  33968853337, // cap 72
  50953280005, // cap 73
  76429920007, // cap 74
  114644880010, // cap 75
  171967320015, // cap 76
  257950980022, // cap 77
  386926470033, // cap 78
  580389705049, // cap 79
  870584557573, // cap 80
  1305876836359, // cap 81
  1958815254538, // cap 82
  2938222881807, // cap 83
  4407334322710, // cap 84
  6611001484065, // cap 85
  9916502226098, // cap 86
  14874753339147, // cap 87
  22312130008720, // cap 88
  33468195013080, // cap 89
  50202292519620, // cap 90
  75303438779430, // cap 91
])

/**
 * Exp cần để đi hết một cấp (real.LevelDetail.expRequire).
 * Chỉ số mảng = cấp độ (KHÔNG phải cấp độ - 1) — trùng với cách Java đọc expRequire[lv].
 */
const EXP_REQUIRE: readonly number[] = Object.freeze([
  0, // cap 0
  200, // cap 1
  600, // cap 2
  1200, // cap 3
  2000, // cap 4
  4000, // cap 5
  8000, // cap 6
  16000, // cap 7
  16000, // cap 8
  24000, // cap 9
  36000, // cap 10
  54000, // cap 11
  64800, // cap 12
  90720, // cap 13
  127008, // cap 14
  177811, // cap 15
  248935, // cap 16
  261382, // cap 17
  339796, // cap 18
  441735, // cap 19
  574256, // cap 20
  497688, // cap 21
  597226, // cap 22
  716671, // cap 23
  860005, // cap 24
  1032006, // cap 25
  1238407, // cap 26
  1486089, // cap 27
  1783307, // cap 28
  2139968, // cap 29
  2567962, // cap 30
  3081554, // cap 31
  3697865, // cap 32
  4437438, // cap 33
  5324925, // cap 34
  6389910, // cap 35
  7667892, // cap 36
  9201471, // cap 37
  11041765, // cap 38
  13250118, // cap 39
  15900142, // cap 40
  19080170, // cap 41
  22896204, // cap 42
  27475445, // cap 43
  32970534, // cap 44
  39564641, // cap 45
  47477569, // cap 46
  56973083, // cap 47
  68367699, // cap 48
  82041239, // cap 49
  98449487, // cap 50
  118139384, // cap 51
  141767261, // cap 52
  170120713, // cap 53
  204144856, // cap 54
  244973827, // cap 55
  293968592, // cap 56
  352762311, // cap 57
  423314773, // cap 58
  507977728, // cap 59
  609573273, // cap 60
  731487928, // cap 61
  877785513, // cap 62
  1053342616, // cap 63
  1264011139, // cap 64
  1516813367, // cap 65
  1820176040, // cap 66
  2184211248, // cap 67
  2621053498, // cap 68
  3145264198, // cap 69
  3774317037, // cap 70
  11322951112, // cap 71
  16984426668, // cap 72
  25476640002, // cap 73
  38214960003, // cap 74
  57322440005, // cap 75
  85983660007, // cap 76
  128975490011, // cap 77
  193463235016, // cap 78
  290194852524, // cap 79
  435292278786, // cap 80
  652938418179, // cap 81
  979407627269, // cap 82
  1469111440903, // cap 83
  2203667161355, // cap 84
  3305500742033, // cap 85
  4958251113049, // cap 86
  7437376669573, // cap 87
  11156065004360, // cap 88
  16734097506540, // cap 89
  25101146259810, // cap 90
  0, // cap 91
])

export function capDoRaExp(): readonly number[] {
  return EXP_MAIN
}

export function capDoRaExpRequire(): readonly number[] {
  return EXP_REQUIRE
}
