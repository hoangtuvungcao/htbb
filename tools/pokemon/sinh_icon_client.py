"""Sinh GÓI ICON VẬT PHẨM cho client (0.9.5) từ icon PMD của server - nguồn: server/map/icon_pokemon/<n>.png +
server/src/com/htbb/pokemon/IconPokemonBang.java (cả hai do tools/pokemon/sinh_icon_pokemon.py sinh).

    python3 tools/pokemon/sinh_icon_client.py

Ra:
  client_jar/res/pk_icon.png                       tấm ghép 16x16 / ô, 32 cột (ô n = icon id 14000+n)
  client_jar/src/game/pokemon/IconBang.java        bảng đổi chỉ số ảnh cũ (5500 / 6500 / 7500 / 8000 / 8200 + K) -> n
Client vẽ icon vật phẩm NGAY từ gói này (không chờ máy chủ, không ô trống); id ngoài gói vẫn hỏi máy chủ (-51).
Chạy lại mỗi khi sinh_icon_pokemon.py đổi icon (KiemPokeDuLieu so khớp bảng client với server).
"""
import os
import re
import sys

from PIL import Image

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
THU_MUC = os.path.join(GOC, "server", "map", "icon_pokemon")
BANG = os.path.join(GOC, "server", "src", "com", "htbb", "pokemon", "IconPokemonBang.java")
RA_ANH = os.path.join(GOC, "client_jar", "res", "pk_icon.png")
RA_JAVA = os.path.join(GOC, "client_jar", "src", "game", "pokemon", "IconBang.java")
O = 16
COT = 32


def main():
    n = 0
    while os.path.isfile(os.path.join(THU_MUC, "%d.png" % n)):
        n += 1
    if n == 0:
        sys.exit("khong thay %s/0.png" % THU_MUC)
    hang = (n + COT - 1) // COT
    tam = Image.new("RGBA", (COT * O, hang * O), (0, 0, 0, 0))
    for i in range(n):
        a = Image.open(os.path.join(THU_MUC, "%d.png" % i)).convert("RGBA")
        if a.size != (O, O):
            a.thumbnail((O, O), Image.NEAREST)
            b = Image.new("RGBA", (O, O), (0, 0, 0, 0))
            b.paste(a, ((O - a.size[0]) // 2, (O - a.size[1]) // 2))
            a = b
        tam.paste(a, ((i % COT) * O, (i // COT) * O))
    tam.save(RA_ANH, optimize=True)
    s = open(BANG, encoding="utf-8").read()
    bang = []
    for ten in ("THUOC", "NGOC", "THU", "NHIEM_VU", "TRAI"):
        dau = int(re.search(r"static final int %s_DAU = (\d+);" % ten, s).group(1))
        so = re.search(r"static final short\[\] %s = \{(.*?)\};" % ten, s, re.S).group(1)
        so = [int(x) for x in re.findall(r"-?\d+", so)]
        bang.append((ten, dau, so))
    j = ["package game.pokemon;", "", "/**",
         " * SINH TU tools/pokemon/sinh_icon_client.py - KHONG SUA TAY. Goi icon PMD trong client (res/pk_icon.png, %d o 16x16," % n,
         " * %d cot) + bang doi chi so anh cu -> o n (giong server IconPokemonBang / IconPokemon.doiAnh)." % COT, " */",
         "final class IconBang {", "    private IconBang() {", "    }", "",
         "    static final int SO = %d, COT = %d, O = %d;" % (n, COT, O), ""]
    for ten, dau, so in bang:
        j.append("    static final int %s_DAU = %d;" % (ten, dau))
        j.append("    static final short[] %s = {" % ten)
        for k in range(0, len(so), 16):
            j.append("        " + ", ".join(str(x) for x in so[k:k + 16]) + ",")
        j.append("    };")
        j.append("")
    j += ["    /** id anh -> o trong goi (-1 = khong co, hoi may chu). */",
          "    static int o(int id) {",
          "        if (id >= 14000 && id < 14000 + SO) {",
          "            return id - 14000;",
          "        }"]
    for ten, dau, so in bang:
        j.append("        if (id >= %s_DAU && id < %s_DAU + %s.length) {" % (ten, ten, ten))
        j.append("            return %s[id - %s_DAU];" % (ten, ten))
        j.append("        }")
    j += ["        return -1;", "    }", "}", ""]
    open(RA_JAVA, "w", encoding="utf-8").write("\n".join(j))
    print("pk_icon.png: %d icon, %d byte; IconBang.java" % (n, os.path.getsize(RA_ANH)))


main()
