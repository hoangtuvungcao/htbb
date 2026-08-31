<?php
/**
 * BANG QUY DOI CAP DO -> DIEM KINH NGHIEM, cho chuc nang "Dat cap do" o
 * trang Nhan vat (nhanvat.php).
 *
 * TEP NAY DUOC SINH RA, DUNG SUA TAY.
 *   Nguon   : real.LevelDetail.expMain (src/real/LevelDetail.java)
 *   Sinh lai: java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhBangCapDo
 *
 * CHI dung de dat cot `xp` cho KHOP voi cap do dat vao `lastLv`, giong het
 * trang thai vua len cap tu nhien (0% vao cap tiep theo). MOI chi so khac
 * cua nhan vat (suc manh/nhanh nhen/HP/diem ky nang...) deu duoc may chu game
 * TU TINH LAI tu `lastLv` ngay luc chon nhan vat
 * (src/real/cmd/SelectCharHandler.java:206-215, roi p.calculateAttrib()) -
 * trang web KHONG duoc va KHONG can dong vao bat ky cot nao khac.
 *
 * test/KiemBangCapDo.java doi chieu tung phan tu voi mang that trong Java.
 */

/** Cap do cao nhat trang quan tri cho phep dat. */
const CAPDO_TOI_DA = 90;

/**
 * Diem kinh nghiem dung tai LUC VUA LEN cap do nay (0% vao cap tiep theo).
 * Chi so mang = cap do - 1, dung nhu real.LevelDetail.expMain ben Java.
 */
function capDoRaExp(): array
{
    return [
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
    ];
}
