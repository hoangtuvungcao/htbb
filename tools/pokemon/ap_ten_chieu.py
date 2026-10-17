#!/usr/bin/env python3
"""
Bang chieu Pokemon (K1) + ap ten vao code (K2).

Nguon su that: BANG ben duoi (sua O DAY). Script:
  1. ghi docs/pokemon/bang_chieu.csv (class, so hieu skill, ten cu, ten moi, he)
  2. thay 3 mang ten skill trong code, KIEM ten cu truoc khi thay (lech -> dung, khong ghi):
       src/real/MessageCreator.java            SKILL_NAME          (gui xuong client)
       ClientUnity/Assets/Scripts/Game/SkillManager.cs   SKILL_NAME (client tu hien)
       src/data/Text.java                      nameSkillLearn (ban tieng Viet, 3 skill hoc them)

Chay (o thu muc HTBB/):  python3 -I tools/pokemon/ap_ten_chieu.py [--chi-csv]
Chay lai nhieu lan van an toan: neu code da mang ten moi thi bo qua.
"""
import csv
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RA = os.path.join(GOC, "docs", "pokemon", "bang_chieu.csv")
F_SERVER = os.path.join(GOC, "src", "real", "MessageCreator.java")
F_CLIENT = os.path.join(GOC, "ClientUnity", "Assets", "Scripts", "Game", "SkillManager.cs")
F_TEXT = os.path.join(GOC, "src", "data", "Text.java")

HE = ["Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất",
      "Bay", "Siêu linh", "Bọ", "Đá", "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"]

# class -> danh sach (ten cu, ten moi, he). Thu tu = so hieu skill trong CharManager.
BANG = [
    ("Kiếm khách → Charmander", [
        ("Chém", "Cào", "Thường"),
        ("Kim tinh pháp", "Than hồng", "Lửa"),
        ("Lôi điện pháp", "Nanh lửa", "Lửa"),
        ("Kinh lôi bát thủ", "Xoáy lửa", "Lửa"),
        ("Hộ sát tiến", "Màn khói", "Thường"),
        ("Dĩ lực đáo công", "Múa kiếm", "Thường"),
        ("Thiên lôi điện trảm", "Súng phun lửa", "Lửa"),
        ("Sấm động dương gian", "Vuốt rồng", "Rồng"),
        ("Kiếm phi kinh thiên", "Đại hỏa", "Lửa"),
    ]),
    ("Chiến binh → Bulbasaur", [
        ("Chém", "Húc", "Thường"),
        ("Hỏa kinh thiên", "Roi dây leo", "Cỏ"),
        ("Nhất hỏa long", "Lá dao", "Cỏ"),
        ("Bát đại hỏa long", "Bom bùn", "Độc"),
        ("Cường thân giáp", "Tự vệ", "Thường"),
        ("Hộ công tiến", "Sinh trưởng", "Thường"),
        ("Thiên long bạo kích", "Bom hạt", "Cỏ"),
        ("Liệt hỏa bạo kích", "Bão lá", "Cỏ"),
        ("Sao băng giáng thế", "Tia mặt trời", "Cỏ"),
    ]),
    ("Pháp sư → Eevee", [
        ("Đánh", "Húc", "Thường"),
        ("Thủy giáng minh", "Tấn công nhanh", "Thường"),
        ("Thần long thủy", "Cắn", "Bóng tối"),
        ("Bát đại hải long", "Sao băng", "Thường"),
        ("Hồi công lực đan", "Mong ước", "Thường"),
        ("Hồi lực tiến", "Nghỉ ngơi", "Siêu linh"),
        ("Hồi sinh", "Phước lành hồi sinh", "Thường"),
        ("Song hộ công thủ", "Chia sẻ nỗi đau", "Ma"),
        ("Hải long xuất thế", "Đuôi sắt", "Thép"),
        ("Song long thị uy", "Bứt phá cuối cùng", "Thường"),
        ("Hàn băng vũ", "Tia phá hủy", "Thường"),
    ]),
    ("Đấu sĩ → Squirtle", [
        ("Đập", "Húc", "Thường"),
        ("Thổ Tú", "Súng nước", "Nước"),
        ("Kim sơn thủy", "Bong bóng", "Nước"),
        ("Khổng kình bát vĩ", "Xoáy nước", "Nước"),
        ("Bất di biến", "Phá mai", "Thường"),
        ("Hộ thủ tiến", "Rút vào mai", "Nước"),
        ("Kinh thiên động địa", "Đuôi nước", "Nước"),
        ("Sơn Tinh bộ thiên", "Bơm nước", "Nước"),
        ("Thạch nhũ công tâm", "Đại pháo nước", "Nước"),
    ]),
    ("Cung thủ → Pichu", [
        ("Bắn", "Tia lửa điện", "Điện"),
        ("Nhất hồn tiễn", "Sóng điện", "Điện"),
        ("Phi thiên tiễn", "Cầu điện", "Điện"),
        ("Bát kim tễn đáo", "Tia chớp", "Điện"),
        ("Độc lưu tiễn", "Nhanh nhẹn", "Siêu linh"),
        ("Hộ độc tiễn", "Nạp điện", "Điện"),
        ("Thập diện tâm tiễn", "Lưới điện", "Điện"),
        ("Thăng thiên loạn tiễn", "Sấm sét", "Điện"),
        ("Vạn tiễn quy tâm", "Đâm sầm điện", "Điện"),
    ]),
]


def ghi_csv():
    os.makedirs(os.path.dirname(RA), exist_ok=True)
    with open(RA, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["class", "dong_pokemon", "so_hieu_skill", "ten_cu", "ten_moi", "he", "so_he"])
        for c, (dong, ds) in enumerate(BANG):
            for i, (cu, moi, he) in enumerate(ds):
                w.writerow([c, dong, i, cu, moi, he, HE.index(he)])
    print("Da ghi", os.path.relpath(RA, GOC))


def mang_cu():
    return [[cu for cu, _, _ in ds] for _, ds in BANG]


def mang_moi():
    return [[moi for _, moi, _ in ds] for _, ds in BANG]


def thay(path, tim, thay_bang, mo_ta):
    s = open(path, encoding="utf8").read()
    if thay_bang in s:
        print("bo qua (da co ten moi):", mo_ta)
        return True
    if tim not in s:
        print("LOI: khong tim thay mang ten CU trong", mo_ta, "- code da bi doi, kiem tra tay")
        return False
    s = s.replace(tim, thay_bang, 1)
    open(path, "w", encoding="utf8").write(s)
    print("da thay:", mo_ta)
    return True


def java_mang(ds):
    return "{" + ", ".join("{" + ", ".join('"%s"' % x for x in d) + "}" for d in ds) + "}"


def main():
    ghi_csv()
    if "--chi-csv" in sys.argv:
        return 0
    cu, moi = mang_cu(), mang_moi()
    ok = True
    # Server: SKILL_NAME = new String[][]{{...}, ...};
    ok &= thay(F_SERVER, "SKILL_NAME = new String[][]" + java_mang(cu) + ";",
               "SKILL_NAME = new String[][]" + java_mang(moi) + ";", "MessageCreator.SKILL_NAME")
    # Client: KHONG thay theo chuoi (ten cu "Chém"/"Húc" lap lai giua cac class -> thay nham).
    # Doc cac chuoi trong khoi THEO THU TU, so voi bang cu/moi, roi sinh lai ca khoi.
    s = open(F_CLIENT, encoding="utf8").read()
    a = s.index("SKILL_NAME = new string[5][]")
    b = s.index("};", a)
    khoi = s[a:b]
    chuoi = re.findall(r'"((?:[^"\\]|\\.)*)"', khoi)
    phang_cu = [x for d in cu for x in d]
    phang_moi = [x for d in moi for x in d]
    khoi_moi = "SKILL_NAME = new string[5][]\n\t{\n" + ",\n".join(
        "\t\tnew string[%d] { %s }" % (len(d), ", ".join('"%s"' % x for x in d)) for d in moi) + "\n\t"
    if chuoi == phang_moi and khoi == khoi_moi:
        print("bo qua (da co ten moi): SkillManager.SKILL_NAME")
    elif chuoi == phang_cu or chuoi == phang_moi:
        open(F_CLIENT, "w", encoding="utf8").write(s[:a] + khoi_moi + s[b:])
        print("da thay: SkillManager.SKILL_NAME")
    else:
        print("LOI: SkillManager.SKILL_NAME khong khop bang cu lan bang moi - kiem tra tay")
        ok = False
    # Text.nameSkillLearn (ban tieng Viet - dong DAU TIEN): 3 skill cuoi moi class
    hoc_cu = [d[-3:] for d in cu]
    hoc_moi = [d[-3:] for d in moi]

    def text_mang(ds):
        return "new String[][] { " + ", ".join("{ " + ", ".join('"%s"' % x for x in d) + " }" for d in ds) + " }"
    ok &= thay(F_TEXT, "Text.nameSkillLearn = " + text_mang(hoc_cu) + ";",
               "Text.nameSkillLearn = " + text_mang(hoc_moi) + ";", "Text.nameSkillLearn (VI)")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
