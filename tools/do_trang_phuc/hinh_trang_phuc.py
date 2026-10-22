"""Hình TRANG PHỤC (lớp phủ trên thân Pokémon) + hào quang + skin thú cưỡi + phụ kiện bạn đồng hành.

Tự vẽ cho dự án (CC0), lưới ký tự -> PNG; viền tối 1 px tự sinh theo màu chính của từng mảnh.
Quy ước đặt (khớp client game.pokemon.NgoaiHinhVe / tools/pokemon/SinhPhuKien.java):
  - 3 hướng: XUONG (nhìn xuống / mặt trước), LEN (quay lưng), NGANG (nhìn sang TRÁI; phải = lật gương).
  - Mũ đặt TRÊN đỉnh đầu (neo N_DINH, chồng 1 px), không bao giờ che mặt; đồ cổ ở cổ (N_CO), áo choàng / cánh sau thân
    (lớp SAU khi nhìn xuống / ngang, lớp TRƯỚC khi quay lưng), đồ cầm ở tay (N_TAY).
  - (ax, ay) = điểm của hình đặt trùng điểm neo, tính TRÊN ẢNH ĐÃ CÓ VIỀN.
"""
from pixel import mau_vien, ve_luoi, lat_ngang

# ---------------------------------------------------------------- bảng màu chung của mảnh trang phục
MAU = {
    "R": 0xE03C30, "r": 0xFF7A60, "d": 0x9A1C18,
    "w": 0xF8F8F8, "q": 0xC8CCD8, "g": 0x8A8E9A, "k": 0x2A2A34,
    "Y": 0xF8C820, "y": 0xFFF080, "o": 0xF08A20,
    "B": 0x3870E0, "b": 0x88B8FF, "v": 0x1E3E8A,
    "G": 0x48A838, "l": 0x98E070, "D": 0x1E6A1E,
    "N": 0x9A5A26, "n": 0x5C3214, "T": 0xD8A868, "t": 0xF0D8A0,
    "P": 0x8A4AD0, "p": 0xC8A0FF, "m": 0x4A2080,
    "i": 0xF890B8, "I": 0xFFD0E0, "c": 0x58D0E8, "C": 0x2090B0,
}

MU, KHAN, VONG, TAY, LUNG, DA, KINH = 0, 1, 2, 3, 4, 5, 6
XUONG, LEN, NGANG = 0, 1, 2
SAU, TRUOC = 0, 1
N_DINH, N_DAU, N_CO, N_TAY, N_TAM = 0, 1, 2, 3, 4

# id mảnh -> định nghĩa. Mỗi mảnh: o, ten, chinh (ký tự màu chính -> viền), truoc (lưới), ngang, sau (lưới hoặc
# "thay:ab,cd" = lấy mặt trước rồi đổi ký tự a->b...), dx, dy.
MANH = {}


def manh(i, o, ten, chinh, truoc, ngang=None, sau=None, dx=0, dy=None):
    MANH[i] = dict(id=i, o=o, ten=ten, chinh=chinh, truoc=truoc.strip("\n"), ngang=ngang.strip("\n") if ngang else None,
                   sau=sau.strip("\n") if sau and not sau.startswith("thay:") else sau, dx=dx,
                   dy=(1 if o == MU else 0) if dy is None else dy)


# ================================================================ MŨ (đỉnh đầu)
manh(1, MU, "Mũ lưỡi trai đỏ", "R", """
..RRRR..
.RRwwRR.
RRRwwRRR
kkkkkkkk
""", ngang="""
..RRRR...
.RRRRwR..
RRRRRRR..
kkkkkkkkk
""", sau="thay:wR")
manh(2, MU, "Nón lá", "T", """
....t....
...tTt...
..tTTTt..
.tTTTTTt.
TTTTRTTTT
""", ngang="""
....t....
...tTt...
..tTTTt..
.tTTTTTt.
TTTTTTTTT
""", sau="thay:RT")
manh(3, MU, "Mũ Noel", "R", """
......ww
.....RRw
....RRR.
...RrRR.
..RRRRR.
.wwwwwww
""", ngang="""
ww......
wRR.....
.RRR....
.RRrR...
.RRRRR..
wwwwwww.
""")
manh(4, MU, "Mũ phù thuỷ", "P", """
.....P...
....PP...
....PPp..
...PPPP..
...PPPPP.
..YkkkkY.
PPPPPPPPP
""", ngang="""
...P.....
...PP....
..pPP....
..PPPP...
.PPPPP...
.kkkkY...
PPPPPPPP.
""")
manh(5, MU, "Mũ len", "B", """
...ww...
..wqw...
.BBBBBB.
BwwwwwwB
BBBBBBBB
wwwwwwww
""", ngang="""
..ww....
.wqw....
.BBBBB..
BwwwwwB.
BBBBBBB.
wwwwwww.
""")
manh(6, MU, "Vương miện", "Y", """
Y..Y..Y
YY.Y.YY
YYYYYYY
YRYbYRY
""", ngang="""
Y..Y.
YY.YY
YYYYY
YbYRY
""")
manh(7, MU, "Mũ thám tử", "N", """
...NN...
.NNTNNN.
NNTNNNNN
NnnnnnnN
.NN..NN.
""", ngang="""
...NN....
.NNNTNN..
NNNNNTNN.
nnnnnnnnN
.N.....N.
""")
manh(8, MU, "Mũ rơm", "T", """
...tttt...
..tTTTTt..
..RRRRRR..
tTTTTTTTTt
""", ngang="""
..tttt...
.tTTTTt..
.RRRRRR..
TTTTTTTTt
""")
manh(9, MU, "Mũ hải tặc", "k", """
k.......k
kk.kkk.kk
kkkkwkkkk
.kkkkkkk.
..YYYYY..
""", ngang="""
kk.....
kkkkk..
kkkwkk.
.kkkkkk
..YYYY.
""", sau="thay:wk")
manh(10, MU, "Mũ đầu bếp", "w", """
.wwqww.
wwwwwww
wqwwwqw
.wwwww.
.qqqqq.
""", ngang="""
.wwqw.
wwwwww
wwwwqw
.wwww.
.qqqq.
""")
manh(11, MU, "Mũ tốt nghiệp", "k", """
....k....
kkkkkkkkk
.kkkkkkY.
..kkkk.Y.
..kkkk.Y.
""", ngang="""
...kk...
kkkkkkkk
Y.kkkk..
Y.kkkk..
""")
manh(12, MU, "Vòng hoa", "G", """
.i.Y.i.
iIiYiIi
GlGGGlG
""", ngang="""
.i.Y.
iIiYi
GlGGG
""")
manh(13, MU, "Mũ tiệc", "B", """
..y..
..B..
.BYB.
.YBY.
BYBYB
""")
manh(14, MU, "Mũ thợ mỏ", "Y", """
...yy...
..YwwY..
.YYYYYY.
YYYYYYYY
oooooooo
""", ngang="""
.yy.....
wwYY....
YYYYYY..
YYYYYYY.
ooooooo.
""", sau="thay:wY,yY")
manh(15, MU, "Tai thỏ", "w", """
.w...w.
wiw.wiw
wiw.wiw
wiw.wiw
.ww.ww.
""", ngang="""
.w.
wiw
wiw
wiw
.ww
""", sau="thay:iw")
manh(16, MU, "Sừng tuần lộc", "N", """
N.N...N.N
NNN...NNN
.N.....N.
.NN...NN.
""", ngang="""
N.N.
NNN.
.N..
.NN.
""")
manh(17, MU, "Mũ cao bồi", "N", """
...NNN...
..NNnNN..
..NNNNN..
.TnnnnnT.
NNNNNNNNN
""", ngang="""
..NNN...
.NNnNN..
.NNNNN..
.nnnnnT.
NNNNNNNN
""")
manh(18, MU, "Mũ hiệp sĩ", "q", """
...RR..
..RRR..
.qqqqq.
qwqqqqq
qkkkkkq
qqqqqqq
""", ngang="""
.RR....
RRR....
.qqqq..
qwqqqq.
qqqkkk.
qqqqqq.
""", sau="thay:kq")
manh(19, MU, "Kính bơi trên trán", "B", """
.kkkkkkk.
kbbkkkbbk
kcbBkBcbk
.kk...kk.
""", ngang="""
.kkkk.
kbbkkk
kcbBkk
.kk...
""", sau="""
.kkkkkkk.
kkkkkkkkk
""")
manh(20, MU, "Mũ phi công", "N", """
..NNNNN..
.NNNNNNN.
NkkNNNkkN
NcbkNkcbN
N.kk.kk.N
""", ngang="""
..NNNN.
.NNNNNN
NkkNNNN
NcbkNNN
N.kk..N
""", sau="thay:kN,cN,bN")
manh(21, MU, "Hoa cài", "i", """
.iii.
iIYIi
.iii.
..G..
""", dx=-2)
manh(22, MU, "Nơ tim", "R", """
RR.RR
RrRRR
.RRR.
..R..
""", dx=-2)
manh(23, MU, "Mũ ngư dân", "G", """
..GGGG..
.GGGGGG.
.GlGGGG.
GDDDDDDG
""", ngang="""
.GGGG..
GGGGGG.
GlGGGG.
DDDDDDG
""")
manh(24, MU, "Mũ thám hiểm", "T", """
..tTTTt..
.tTTTTTT.
.TTTTTTT.
.nnnnnnn.
TTTTTTTTT
""", ngang="""
.tTTTt..
tTTTTTT.
TTTTTTT.
nnnnnnn.
TTTTTTTT
""")
manh(25, MU, "Băng đô ninja", "v", """
qqqqqqq..
vvqgqvvvv
.......vv
""", ngang="""
qqqqq..
vvqgvvv
.....vv
""", sau="""
vvvvvvv
vvvvvvv
""")

# ================================================================ CỔ (khăn / nơ / vòng)
manh(30, KHAN, "Khăn trắng", "w", """
wwwwwww
...qwww
.....ww
""", ngang="""
www.
..ww
...w
""")
manh(31, KHAN, "Khăn len sọc", "R", """
RwRwRwR
....wRw
.....Rw
.....wR
""", ngang="""
RwR.
..wR
...R
""")
manh(32, KHAN, "Khăn đỏ viền vàng", "R", """
RRRRRRR
YRRRRRY
...YRR.
....RY.
""", ngang="""
RRR.
.YRR
..RY
""")
manh(33, KHAN, "Nơ cổ", "R", """
RR.RR
RrYRR
RR.RR
""", ngang="""
RR
RY
""")
manh(34, KHAN, "Lục lạc vàng", "R", """
RRRRR
.yYY.
.YkY.
""", ngang="""
RR
YY
""")
manh(35, KHAN, "Vòng hoa Hawaii", "i", """
iYiYiYi
.IiYiI.
""", ngang="""
iYi.
.Ii.
""")
manh(36, KHAN, "Khăn rằn", "k", """
kwkwkwk
.wkwkw.
..kwk..
...w...
""", ngang="""
kwk.
.wkw
..k.
""")
manh(37, KHAN, "Dây chuyền ngọc trai", "q", """
w.....w
.w...w.
..wbw..
...c...
""", ngang="""
w..
.w.
..b
""")

# ================================================================ LƯNG (áo choàng / cánh / ba lô)
manh(40, LUNG, "Áo choàng đỏ", "R", """
.RRRRR.
RRRRRRR
RRRRRRR
RRRRRRR
RRRRRRR
dRdRdRd
""", ngang="""
RR...
RRR..
RRRR.
RRRRR
RRRRR
dRdRd
""", sau="""
.RYYYR.
RRRRRRR
RRrRRRR
RRRRRRR
RRRRRRR
dRdRdRd
""")
manh(41, LUNG, "Áo choàng tím", "m", """
R.mmmmm.R
RmmRRRmmR
.mmRRRmm.
.mmRRRmm.
.mmmmmmm.
.m.m.m.m.
""", ngang="""
mm...
mRm..
mRRm.
mRRmm
mmmmm
m.m.m
""", sau="""
R.mmmmm.R
Rmmmmmmm.
.mmmmmmm.
.mmpmmmm.
.mmmmmmm.
.m.m.m.m.
""")
manh(42, LUNG, "Cánh thiên thần", "w", """
ww.......ww
wqw.....wqw
.wqw...wqw.
.wwww.wwww.
..www.www..
""", ngang="""
.ww
wqw
wqw
www
.ww
""")
manh(43, LUNG, "Cánh dơi", "m", """
m.........m
mm.......mm
mpm.....mpm
mmmm...mmmm
.m.m...m.m.
""", ngang="""
.m.
mm.
mpm
mmm
.m.
""")
manh(44, LUNG, "Cánh bướm", "i", """
ii.....ii
iIi...iIi
iYii.iiYi
.iii.iii.
..ii.ii..
""", ngang="""
ii.
iIi
iYi
.ii
""")
manh(45, LUNG, "Ba lô", "N", """
.NNNNN.
NnnnnnN
NNNYNNN
NNNNNNN
.nnnnn.
""", ngang="""
NN.
NnN
NYN
NNN
.n.
""", sau="""
.NNNNN.
NNNNNNN
NNTTTNN
NNTYTNN
NNNNNNN
.nnnnn.
""")
manh(46, LUNG, "Áo choàng Gym", "B", """
.BBBBB.
BBBBBBB
BBBBBBB
BBBBBBB
vBvBvBv
""", ngang="""
BB...
BBB..
BBBB.
BBBBB
vBvBv
""", sau="""
.BBBBB.
BBBYBBB
BBYyYBB
BBBYBBB
vBvBvBv
""")

# ================================================================ CẦM TAY
manh(50, TAY, "Đèn ông sao", "R", """
...Y...
..YRY..
YYRrRYY
.YRRRY.
.YR.RY.
Y.....Y
""")
manh(51, TAY, "Bóng bay", "i", """
.iii.
iIiii
iiiii
.iii.
..i..
..k..
..k..
""")
manh(52, TAY, "Kính lúp", "N", """
.kkk..
kbwbk.
kbbbk.
.kkkN.
....NN
""")
manh(53, TAY, "Đũa phép", "N", """
...y
..Y.
.N..
N...
""")
manh(54, TAY, "Hoa hồng", "R", """
.RR.
RrRR
.RR.
..G.
.lG.
..G.
""")
manh(55, TAY, "Cần câu", "N", """
....N
...N.
..N.k
.N..k
N...k
....R
""")
manh(56, TAY, "Bao lì xì", "R", """
RRRR
RYYR
RRRR
RRRR
""")
manh(57, TAY, "Que kem", "i", """
.ii.
iIii
iiii
.Ty.
.T..
""")
manh(58, TAY, "Kẹo gậy", "R", """
.RwR
R..w
w..R
...w
...R
...w
""")

# ================================================================ PHỤ KIỆN BẠN ĐỒNG HÀNH (nhỏ, đặt trên đầu / cổ pet)
manh(70, MU, "Nơ đỏ nhỏ", "R", """
R.R
RYR
""")
manh(71, KHAN, "Chuông nhỏ", "Y", """
RRR
.Y.
""")
manh(72, KHAN, "Khăn xanh nhỏ", "B", """
BBBB
..bB
""")
manh(73, MU, "Vương miện nhỏ", "Y", """
Y.Y.Y
YYYYY
""")
manh(74, MU, "Hoa cài nhỏ", "i", """
.i.
iYi
.i.
""")
manh(75, MU, "Ngôi sao nhỏ", "Y", """
.Y.
YyY
.Y.
""")
manh(76, MU, "Tim hồng nhỏ", "i", """
i.i
iii
.i.
""")
manh(77, KHAN, "Thẻ tên", "T", """
TTT
TwT
""")


def _thay(luoi, quy):
    for cap in quy[5:].split(","):
        a, b = cap[0], cap[1]
        luoi = luoi.replace(a, b)
    return luoi


def luoi_huong(m, huong):
    if huong == XUONG:
        return m["truoc"]
    if huong == NGANG:
        return m["ngang"] or m["truoc"]
    s = m["sau"]
    if s is None:
        return m["truoc"]
    if s.startswith("thay:"):
        return _thay(m["truoc"], s)
    return s


def lop_va_neo(m, huong):
    o = m["o"]
    if o == MU:
        return TRUOC, N_DINH
    if o == KHAN:
        return (TRUOC if huong == XUONG else SAU), N_CO
    if o == LUNG:
        return (TRUOC if huong == LEN else SAU), N_CO
    if o == TAY:
        return TRUOC, N_TAY
    return TRUOC, N_DAU


def anh_manh(m, huong):
    """(ảnh có viền, ax, ay, lop, neo, dx, dy) cho mảnh m ở hướng huong (NGANG = nhìn sang trái)."""
    luoi = luoi_huong(m, huong)
    anh = ve_luoi(luoi, MAU, vien=mau_vien(MAU[m["chinh"]]))
    dong = luoi.split("\n")
    w0 = max(len(s) for s in dong)
    h0 = len(dong)
    o = m["o"]
    lop, neo = lop_va_neo(m, huong)
    if o == MU:
        # điểm neo = giữa đáy mũ (đặt lên đỉnh đầu); NGANG: lệch nhẹ về phía trước (trái)
        ax, ay = (w0 - 1) // 2 + 1, h0
    elif o in (KHAN, LUNG):
        ax, ay = (w0 - 1) // 2 + 1, 1
        if huong == NGANG and o == LUNG:
            ax = 1    # áo choàng nhìn ngang: mép trước bám cổ, phần còn lại bay về sau lưng (bên phải)
        if huong == NGANG and o == KHAN:
            ax = 1
    else:
        ax, ay = (w0 - 1) // 2 + 1, (h0 - 1) // 2 + 1
    dx = m["dx"]
    dy = m["dy"]
    if huong == NGANG and o == LUNG:
        dx = 1
    return anh, ax, ay, lop, neo, dx, dy


# ================================================================ HÀO QUANG (vòng dưới chân, 4 khung)
# Vòng elip 15x5 vẽ tay; 'a' màu chính, 'b' màu sáng, 'c' màu tối. 4 khung = điểm sáng chạy quanh vòng.
VONG_HQ = """
....ccccccc....
..cc.......cc..
.c...........c.
..cc.......cc..
....ccccccc....
"""
# vị trí điểm sáng chạy (theo thứ tự quanh vòng) - mỗi khung lấy 3 điểm cách đều
QUY_DAO = [(7, 0), (10, 0), (12, 1), (13, 2), (12, 3), (10, 4), (7, 4), (4, 4), (2, 3), (1, 2), (2, 1), (4, 0)]

HAO_QUANG = [
    # (id, ma, ten, mau chinh, mau sang, hat (luoi 3x3 hoac None))
    (1, "tap_su", "Hào quang tập sự", 0x9AA2B0, 0xE8ECF4),
    (2, "lua", "Hào quang Lửa", 0xF07028, 0xFFD060),
    (3, "nuoc", "Hào quang Nước", 0x3C88F0, 0xB8E0FF),
    (4, "co", "Hào quang Cỏ", 0x48B040, 0xC8F8A0),
    (5, "dien", "Hào quang Điện", 0xE8B818, 0xFFF8A0),
    (6, "bang", "Hào quang Băng", 0x78D0E8, 0xF0FFFF),
    (7, "tam_linh", "Hào quang Tâm linh", 0xE84890, 0xFFC0E0),
    (8, "rong", "Hào quang Rồng", 0x6838E8, 0xC8B0FF),
    (9, "tien", "Hào quang Tiên", 0xF090B8, 0xFFF0F8),
    (10, "bong_toi", "Hào quang Bóng tối", 0x4A3A58, 0xB090D0),
    (11, "cau_vong", "Hào quang Cầu vồng", 0xE84040, 0xFFFFFF),
    (12, "anh_dao", "Hào quang Hoa anh đào", 0xF8A0C0, 0xFFFFFF),
    (13, "sao_bang", "Hào quang Sao băng", 0x3050B0, 0xFFF080),
]
CAU_VONG = [0xFF5050, 0xFFA030, 0xFFE840, 0x58E070, 0x50B8FF, 0xC070FF]


def khung_hao_quang(hq, khung):
    """Ảnh 15x5 khung thứ `khung` (0..3) của hào quang hq."""
    _, ma, _, chinh, sang_ = hq
    from pixel import toi, sang as lam_sang
    dong = VONG_HQ.strip("\n").split("\n")
    w, h = len(dong[0]), len(dong)
    anh = [w, h, [[0] * w for _ in range(h)]]
    for y in range(h):
        for x in range(w):
            if dong[y][x] == "c":
                c = chinh
                if ma == "cau_vong":
                    c = CAU_VONG[(x * 6 // w + khung) % 6]
                anh[2][y][x] = 0xFF000000 | (toi(c, 0.15) if y >= 3 else c)
    n = len(QUY_DAO)
    for j in range(3):
        x, y = QUY_DAO[(khung * 1 + j * n // 3) % n]
        anh[2][y][x] = 0xFF000000 | sang_
        # đuôi sáng nhạt
        x2, y2 = QUY_DAO[(khung * 1 + j * n // 3 - 1) % n]
        anh[2][y2][x2] = 0xFF000000 | lam_sang(chinh, 0.45)
    return anh


# ================================================================ SKIN THÚ CƯỠI: yên cưỡi vẽ tay + bảng đổi màu thú
YEN = """
..1111111..
.122222223.
12W22222223
1222a2a2223
.333333333.
..4.....4..
..4.....4..
"""
SKIN_THU = [
    # (id, ma, ten, mau yen, mau day, (xoay mau do, bao hoa %, sang %, phu mau rgb, phu mau %))
    (1, "yen_co_ban", "Yên cương cơ bản", 0x9A5A26, 0x5C3214, (0, 100, 100, 0x000000, 0)),
    (2, "yen_da", "Yên da thợ săn", 0x7A4A2A, 0xC89040, (0, 95, 98, 0x8A5A30, 12)),
    (3, "yen_hoa", "Yên hoa xuân", 0xF890B8, 0x48A838, (0, 105, 106, 0xFFC0D8, 22)),
    (4, "yen_vang", "Yên vàng hoàng gia", 0xF0B828, 0xD83838, (0, 110, 106, 0xFFD838, 25)),
    (5, "yen_bang", "Yên băng Noel", 0x88D8F0, 0xF8F8F8, (0, 45, 114, 0xC8F0FF, 38)),
    (6, "yen_lua", "Yên lửa nhà vô địch", 0xE85020, 0xFFD040, (-20, 125, 104, 0xFF6020, 25)),
    (7, "yen_dem", "Yên bóng đêm", 0x3A2A58, 0xB090D0, (0, 70, 80, 0x7038B0, 40)),
    (8, "yen_cau_vong", "Yên cầu vồng", 0xE84040, 0x50B8FF, (40, 120, 106, 0xFFFFFF, 10)),
]

# ================================================================ THẺ BẠN ĐỒNG HÀNH: mảnh nhỏ trên pet + màu nhãn tên
THE_DONG_HANH = [
    # (id, ma, ten, id manh, mau nhan ten)
    (1, "the_co_ban", "Thẻ tên bạn đồng hành", 77, 0xF0D8A0),
    (2, "no_do", "Nơ đỏ bạn đồng hành", 70, 0xFF8070),
    (3, "chuong_vang", "Chuông vàng bạn đồng hành", 71, 0xFFE070),
    (4, "khan_xanh", "Khăn xanh bạn đồng hành", 72, 0x88B8FF),
    (5, "vuong_mien_nho", "Vương miện tí hon", 73, 0xFFD040),
    (6, "hoa_cai_nho", "Hoa cài bạn đồng hành", 74, 0xFFB0D0),
    (7, "ngoi_sao_nho", "Ngôi sao bạn đồng hành", 75, 0xFFF080),
    (8, "tim_hong_nho", "Tim hồng bạn đồng hành", 76, 0xFF90C0),
]
