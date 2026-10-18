#!/usr/bin/env python3
"""
Icon Pokemon cho MOI vat pham / thuoc / ngoc / nguyen lieu / trung / pet / do thu cuoi / trai cay
ma nguoi choi nhin thay. Nguon: PMDCollab RawAsset/Item (16x16) va chan dung SpriteCollab
(40x40, thu nho con 20x20) - cung phong cach PMD voi sprite trong game.

Ra (KHONG sua tay, chay lai tool):
  map/icon_pokemon/<n>.png               server phuc vu o id anh 14000+n (IconPokemon)
  sql/pokemon_icon_vat_pham.sql          UPDATE data_item.xstart -> 14000+n, va tach vai o anh
                                         thuoc dung chung (data_potion.idImage / data_shop.idImg)
  src/com/htbb/pokemon/IconPokemonBang.java
                                         bang thuan: chi so anh cu cua cac dai KHONG co cot DB
                                         (thuoc 5500+, ngoc 6500+, thu 7500+, vat pham nhiem vu
                                         8000+, trai cay 8200+) -> n. DataGame.sendByteImg dung
                                         bang nay khi bat sv.che_do_pokemon.
  docs/pokemon/bang_icon.csv             bang doi chieu n -> nguon -> dung cho
  docs/pokemon/xem_truoc_icon.png        anh xem truoc: cu | moi cho moi cho dung (--xem-truoc)

Vi sao dai rieng ma khong ghi de map/icon/<id>.png: icon cu dung chung giua nhieu vat pham,
nIconItem = 866 bi test cu (KiemSkinLinhThu) khoa cung, va khi tat che do Pokemon thi cac dai
cu van phai ra hinh goc.

Nguon su that DUY NHAT cua danh sach icon la bang ICON ben duoi. So thu tu n = vi tri trong
bang (n duoc ghi vao CSDL), nen CHI THEM VAO CUOI, khong chen/xoa giua chung.

Moi dong ICON = (nguon, [dung cho...]).
  nguon:  "<Ten>"            RawAsset/Item/<Ten>.None.png
          "dex:0163"         chan dung SpriteCollab (them 's' = shiny: "dex:0164s"), thu nho 20x20
          + bien doi noi bang '+':
            hue:<do>         xoay sac do
            mau:RRGGBB       to lai theo do sang (dung cho anh xam/trang)
            toi | sang | xam lam toi / sang / xam
            so:<n>           ve chu so n o goc duoi phai (phan bac)
            xanh             trai chua chin (pha xanh nhat)
            treo             trai treo tren cay: dem 16x26, ve cuong (anh trai cay 8200+)
  dung cho:
          "type:N"           moi trang bi type N trong docs/pokemon/bang_vat_pham.csv
          "item:A,B-C"       data_item id (chay TRUOC cac luat xs:)
          "xs:N"             moi data_item dang co xstart = N (icon goc cu N)
          "potion:K" "gem:K" "thu:K" "nv:K" "trai:K"
                             anh cu 5500+K / 6500+K / 7500+K / 8000+K / 8200+K
          "nd:KHOA"          noi dung moi (tools/pokemon/sinh_noi_dung.py doc bang_icon.csv, tu ghi
                             xstart/icon vao sql/pokemon_noi_dung.sql) - KHONG sinh dong SQL o day
          "dp:ID>K"          data_potion id ID chuyen sang o anh thuoc K (K phai la o TRONG -
          "ds:ID>K"          data_shop  ...                       test kiem tren htbb_day_du.sql)
                             (dung de tach hai vat pham khac nghia dang dung chung mot anh cu)

Chay (o thu muc HTBB/):
    python3 -I tools/pokemon/sinh_icon_pokemon.py               # tai (neu thieu) + sinh
    python3 -I tools/pokemon/sinh_icon_pokemon.py --xem-truoc   # them anh xem truoc
Tai ve: tools/pokemon/pmd_raw_icon/ (KHONG commit, CC BY-NC / tai san PMD, xem .gitignore).
"""
import colorsys
import csv
import os
import re
import struct
import sys
import urllib.request
import zlib

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RA_ANH = os.path.join(GOC, "map", "icon_pokemon")
RA_SQL = os.path.join(GOC, "sql", "pokemon_icon_vat_pham.sql")
RA_JAVA = os.path.join(GOC, "src", "com", "htbb", "pokemon", "IconPokemonBang.java")
RA_CSV = os.path.join(GOC, "docs", "pokemon", "bang_icon.csv")
RA_XEM = os.path.join(GOC, "docs", "pokemon", "xem_truoc_icon.png")
BANG_VP = os.path.join(GOC, "docs", "pokemon", "bang_vat_pham.csv")
TAI = os.path.join(GOC, "tools", "pokemon", "pmd_raw_icon")
NGUON_ITEM = "https://raw.githubusercontent.com/PMDCollab/RawAsset/master/Item/%s.None.png"
NGUON_CHAN_DUNG = "https://raw.githubusercontent.com/PMDCollab/SpriteCollab/master/portrait/%s/Normal.png"
ID_DAU = 14000
SO_TOI_DA = 1000  # khop IconPokemon.SO_TOI_DA (14000..14999)

# Dai anh khong co cot DB: ten -> (id dau, so o, thu muc anh cu, mau ten file)
DAI = {
    "potion": (5500, 256, "map/images_potion", "%d.png"),
    "gem": (6500, 96, "map/images_gem", "%d.png"),
    "thu": (7500, 58, "map/images_animal", "%d.png"),
    "nv": (8000, 10, "map/iconquest", "%d.png"),
    "trai": (8200, 15, "map/fruit", "b%d.png"),
}
TEN_JAVA = {"potion": "THUOC", "gem": "NGOC", "thu": "THU", "nv": "NHIEM_VU", "trai": "TRAI"}

EMBOAR = "mau:F06030"
SWAMPERT = "mau:3C8CFF"


def dai(a, b):
    return ",".join(str(i) for i in range(a, b + 1))


ICON = [
    # ---- 0..21: trang bi + da Eevee (giu nguyen thu tu, da co trong CSDL) ----
    ("Scarf_Red", ["type:0", "potion:15"]),  # Khan quang, Khan do
    ("Belt_Black", ["type:1"]),         # Dai
    ("Band_Brown", ["type:2"]),         # Bang do
    ("Stone_DarkBrown", ["type:3"]),    # Than lua
    ("Seed_Green", ["type:4"]),         # Hat giong than
    ("Silk_White", ["type:5"]),         # Khan lua
    ("Bottle_LightBlue", ["type:6"]),   # Nuoc than
    ("Iron_Ball", ["type:7"]),          # Nam cham
    ("Ring_Target", ["type:8"]),        # Huy hieu
    ("Pearl", ["type:9"]),              # Vong co
    ("Scale_LightBlue", ["type:10"]),   # Long vu toc do
    ("Claw_Gray", ["type:11"]),         # Gang dam
    ("Slab_Gray", ["type:12"]),         # Phien da he
    ("Bow_Pink", ["type:19"]),          # Ruy bang
    ("Stone_Blue", ["item:960"]),       # Da Nuoc
    ("Stone_Yellow", ["item:961"]),     # Da Sam
    ("Stone_Red", ["item:962"]),        # Da Lua
    ("Stone_Orange", ["item:963"]),     # Da Mat Troi
    ("Stone_Black", ["item:964"]),      # Da Mat Trang
    ("Stone_Green", ["item:965"]),      # Da La
    ("Stone_LightBlue", ["item:966"]),  # Da Bang
    ("Bow_Fuschia", ["item:967"]),      # Ruy Bang Tien

    # ---- data_item type 13: cuoc, choi ----
    ("Stick_Brown", ["item:465", "nv:2", "gem:21", "potion:103"]),  # Cuoc thuong, Khuc go, Go thuong tho
    ("Stick_Yellow", ["item:466", "gem:31"]),                        # Cuoc xin, Go thuong cap
    ("Wand_Orange+sang", ["xs:521"]),    # Choi dien anh
    ("Wand_Blue+sang", ["xs:522"]),      # Choi bang anh
    ("Wand_Purple+sang", ["xs:523"]),    # Choi doc anh
    ("Wand_Blue", ["xs:526"]),           # Choi bang
    ("Wand_Orange", ["xs:527"]),         # Choi set
    ("Wand_Purple", ["xs:528"]),         # Choi doc
    # ---- type 14..18: do thu cuoi, 4 bac (Poke/Great/Ultra, Quick/Dusk/Timer, Luxury/Heavy/Level, Moon/Dream/Master)
    ("Cape_Brown+so:1", ["xs:477"]), ("Cape_Gray+so:2", ["xs:478"]),
    ("Cape_Orange+so:3", ["xs:479"]), ("Cape_Red+so:4", ["xs:480"]),            # Giap cuoi
    ("Band_Tan+so:1", ["xs:481"]), ("Band_Gray+so:2", ["xs:482"]),
    ("Band_Green+so:3", ["xs:483"]), ("Band_Pink+so:4", ["xs:484"]),            # Vong chan
    ("Crown_Gray+so:1", ["xs:485"]), ("Crown_Gray+mau:5AA0FF+so:2", ["xs:486"]),
    ("Crown_Yellow+so:3", ["xs:487"]), ("Crown_Yellow+hue:-50+so:4", ["xs:488"]),  # Mu cuoi
    ("Disc_Black+so:1", ["xs:473"]), ("Disc_Blue+so:2", ["xs:474"]),
    ("Disc_Yellow+so:3", ["xs:475"]), ("Disc_Yellow+hue:-45+so:4", ["xs:476"]),   # Ban dap
    ("Belt_Brown+so:1", ["xs:489"]), ("Belt_LightBlue+so:2", ["xs:490"]),
    ("Belt_White+so:3", ["xs:491"]), ("Belt_LightBlue+hue:70+so:4", ["xs:492"]),  # Yen cuong
    # ---- type 20..24 ----
    ("Box_Brown", ["xs:519"]),           # Ruong giao hang
    ("Box_Red", ["xs:520", "potion:212"]),  # Ruong cuop hang, Hop banh dac biet
    ("Tag_Purple+hue:-90+so:4", ["xs:532", "xs:533", "xs:534"]),   # Ve goi Pokemon ho tro 4x
    ("Tag_Purple+hue:-170+so:5", ["xs:535", "xs:536", "xs:537"]),  # 5x
    ("Tag_Purple+hue:70+so:6", ["xs:538", "xs:539", "xs:540"]),    # 6x
    ("Tag_White", ["xs:558", "potion:13", "potion:32", "potion:105"]),  # The mua ban, ve, phieu
    ("Tag_Purple+hue:30+so:2", ["xs:562"]), ("Tag_Purple+hue:30+so:3", ["xs:563"]),  # Ve xe giao hang
    ("Key_Brown+so:5", ["xs:559"]), ("Key_White+so:6", ["xs:560"]),
    ("Key_Brown+mau:FFD040+so:7", ["xs:561"]),                     # Ve Thap Pokemon 5x/6x/7x
    ("Machine_Green", ["xs:580", "gem:95"]),                        # Sach ky nang pet (cu)
    # ---- type 25, 27, 21, 30: Pokemon (chan dung) ----
    ("dex:0077", ["xs:564", "potion:27", "potion:37", "thu:16"]),   # Ponyta lua, Sao goi Ponyta
    ("dex:0078", ["xs:565", "potion:38", "thu:0"]),                 # Rapidash
    ("dex:0078s", ["xs:566"]),                                       # Rapidash Shiny
    ("dex:0163", ["item:689,690", "thu:30", "thu:37"]),             # Hoothoot
    ("dex:0164", ["item:691,692", "thu:38"]),                       # Noctowl
    ("dex:0164s", ["item:693"]),
    ("dex:0147", ["item:694,695", "thu:31", "thu:36"]),             # Dratini
    ("dex:0148", ["item:696,697", "thu:35"]),                       # Dragonair
    ("dex:0149", ["item:698", "thu:50"]),                           # Dragonite
    ("dex:0173", ["item:699", "thu:32", "thu:39"]),                 # Cleffa
    ("dex:0035", ["item:700,701", "thu:40", "potion:79"]),          # Clefairy
    ("dex:0036", ["item:702", "potion:78"]),                        # Clefable
    ("dex:0036s", ["item:703"]),
    ("dex:0041", ["item:704,705", "thu:8", "thu:33", "thu:41", "potion:84"]),  # Zubat
    ("dex:0042", ["item:706,707", "thu:42"]),                       # Golbat
    ("dex:0169", ["item:708"]),                                     # Crobat
    ("dex:0627", ["item:709,710", "thu:34", "thu:43"]),             # Rufflet
    ("dex:0628", ["item:711,712", "thu:44"]),                       # Braviary
    ("dex:0628s", ["item:713"]),
    ("dex:0417", ["xs:585", "thu:18", "thu:46", "ds:110>241"]),     # Pachirisu
    ("dex:0225", ["xs:586"]),                                       # Delibird
    ("dex:0100s", ["xs:698"]),                                      # Voltorb vang
    ("dex:0667", ["xs:731"]),                                       # Litleo
    ("dex:0058", ["xs:732"]),                                       # Growlithe
    ("dex:0403s", ["xs:733"]),                                      # Shinx vang
    ("dex:0585", ["xs:750"]),                                       # Deerling
    ("dex:0399", ["xs:816"]),                                       # Bidoof
    ("dex:0245", ["xs:807", "xs:808", "xs:809", "xs:810", "xs:817", "xs:818", "xs:819",
                  "xs:827", "xs:828", "potion:143", "thu:47"]),     # Suicune ho tro / thu cuoi
    ("dex:0879", ["xs:860"]),                                       # Skin Copperajah
    ("dex:0675", ["xs:861"]),                                       # Skin Pangoro
    ("dex:0716", ["xs:862"]),                                       # Skin Xerneas
    ("dex:0323", ["xs:863"]),                                       # Skin Camerupt
    ("dex:0745", ["xs:864"]),                                       # Skin Lycanroc
    ("dex:0483", ["xs:865"]),                                       # Skin Dialga
    # ---- trung (Egg, to mau theo loai) ----
    ("Egg+hue:-150", ["xs:572"]),            # Trung Hoothoot
    ("Egg+hue:110", ["xs:573"]),             # Trung Dratini
    ("Egg+hue:-110", ["xs:574"]),            # Trung Cleffa
    ("Egg+hue:160", ["xs:575"]),             # Trung Zubat
    ("Egg+hue:-60", ["xs:576"]),             # Trung Rufflet
    ("Egg+hue:-90", ["xs:839", "potion:227"]),   # Trung Ponyta
    ("Egg+hue:-75+sang", ["xs:840"]),        # Trung Dragonite
    ("Egg+hue:90", ["xs:841", "potion:166"]),    # Trung Suicune
    ("Egg+xam", ["xs:842", "potion:146"]),       # Trung Piloswine
    ("Egg+hue:140+toi", ["xs:843", "potion:145"]),  # Trung Spectrier
    ("Egg+hue:70+sang", ["potion:24"]),          # Trung Articuno
    ("Egg", ["potion:89"]),                      # Trung (nguyen lieu nau an)
    # ---- type 28 ----
    ("Gummi_Pink", ["xs:577", "potion:245"]),    # Keo Poke
    ("Gummi_Royal", ["xs:578", "potion:250"]),   # Keo hiem
    ("Herb_Green", ["xs:579", "potion:251", "potion:172", "potion:186"]),  # Bac ha, La dong
    ("Wand_Red", ["xs:589"]),                    # Choi lua
    ("Money_Yellow", ["xs:590", "dp:0>237"]),    # Dong xu vang, Xu
    ("Money_Gray", ["xs:591"]),                  # Dong xu bac
    ("Money_Gray+mau:50A0FF", ["xs:592"]),       # Dong xu xanh
    ("Money_Gray+mau:FF5050", ["xs:594"]),       # Dong xu do
    ("Money_Gray+mau:60D060", ["xs:595"]),       # Dong xu xanh la
    ("Box_Pink", ["item:735", "potion:0", "potion:35", "potion:144", "potion:220"]),  # Ruong may man, hop qua
    ("Chest_Red", ["item:752"]),                 # Ruong nguyen lieu cao cap
    ("Chest_Blue", ["item:753", "potion:68"]),   # Ruong nguyen lieu so cap, Ruong bac
    ("Chest_Gold", ["xs:601", "potion:67"]),     # Ruong kho bau, Ruong vang
    ("Medicine_Yellow", ["xs:604", "potion:152"]),  # Thuoc hoi sinh
    ("Scale_Pink", ["xs:627", "potion:93"]),     # Vay trai tim
    ("Tag_White+mau:FF4040", ["xs:705", "potion:76"]),  # The do, Bao li xi
    ("Tag_White+mau:FFD040", ["xs:706", "potion:11", "potion:12"]),  # The vang, Ve gio vang
    ("Crystal_Yellow", ["xs:734", "potion:230"]),  # Da cau vong tinh khiet, Sao vang
    ("Silk_Pink", ["xs:735", "gem:33"]),         # To lua cap
    ("Ore_White", ["xs:736", "gem:34"]),         # Bac cap
    ("Ore_Blue", ["xs:737", "gem:35"]),          # Thuy tinh cap
    ("Stick_Pink", ["xs:738", "gem:36"]),        # Go sua cap
    ("Scale_Black", ["xs:739", "gem:37"]),       # Da cung cap
    ("Rock_Gray", ["xs:740", "gem:19", "gem:47"]),   # Da tach nguyen lieu 5x, Quang sat
    ("Rock_Blue", ["xs:741", "gem:25", "gem:48"]),   # 6x, Thuy tinh tho
    ("Rock_Purple", ["xs:742", "gem:24", "gem:49"]),  # 7x, Quang bac
    ("Stone_White", ["xs:743", "gem:38"]),       # Da thuoc tinh thuong, Da ngu hop
    ("Stone_Gold", ["xs:744"]),                  # Da thuoc tinh cao cap
    ("Stick_White", ["xs:745", "gem:65"]),       # Xuong
    ("Orb_Purple", ["xs:746", "gem:11"]),        # Ngoc hoang hon
    ("Stone_Purple", ["xs:747", "gem:76"]),      # Da thang bac, Da tien giai
    ("Rock_Red", ["xs:748", "gem:84"]),          # Nham thach, Hoa thach 1
    ("Flower_Pink", ["xs:766", "potion:44", "gem:56"]),  # Nhanh hoa dao
    ("Tag_Purple", ["xs:767", "potion:39", "potion:106"]),  # The doi ten, the chiem Gym
    # ---- type 29: bo Emboar / Swampert (manh = toi), canh ----
    ("Cape_Gray+" + EMBOAR, ["item:860"]), ("Cape_Gray+" + EMBOAR + "+toi", ["item:848"]),
    ("Belt_White+" + EMBOAR, ["item:861"]), ("Belt_White+" + EMBOAR + "+toi", ["item:849"]),
    ("Band_Gray+" + EMBOAR, ["item:862"]), ("Band_Gray+" + EMBOAR + "+toi", ["item:850"]),
    ("Crown_Gray+" + EMBOAR, ["item:863"]), ("Crown_Gray+" + EMBOAR + "+toi", ["item:851"]),
    ("Scarf_2_White+" + EMBOAR, ["item:864"]), ("Scarf_2_White+" + EMBOAR + "+toi", ["item:852"]),
    ("Claw_White+" + EMBOAR, ["item:865"]), ("Claw_White+" + EMBOAR + "+toi", ["item:853"]),
    ("Cape_Gray+" + SWAMPERT, ["item:866"]), ("Cape_Gray+" + SWAMPERT + "+toi", ["item:854"]),
    ("Belt_White+" + SWAMPERT, ["item:867"]), ("Belt_White+" + SWAMPERT + "+toi", ["item:855"]),
    ("Band_Gray+" + SWAMPERT, ["item:868"]), ("Band_Gray+" + SWAMPERT + "+toi", ["item:856"]),
    ("Crown_Gray+" + SWAMPERT, ["item:869"]), ("Crown_Gray+" + SWAMPERT + "+toi", ["item:857"]),
    ("Scarf_2_White+" + SWAMPERT, ["item:870"]), ("Scarf_2_White+" + SWAMPERT + "+toi", ["item:858"]),
    ("Claw_White+" + SWAMPERT, ["item:871"]), ("Claw_White+" + SWAMPERT + "+toi", ["item:859"]),
    ("Herb_Blue", ["xs:834"]),                   # Canh Articuno
    ("Herb_White", ["xs:835"]),                   # Canh Lugia
    ("Herb_Red", ["xs:836", "nv:6"]),            # Canh Moltres, Long duoi
    ("Herb_Yellow", ["xs:837"]),                 # Canh Togekiss
    ("Herb_Blue+hue:60", ["xs:838"]),            # Canh Crobat

    # ---- thuoc (anh 5500+K, data_potion + data_shop) ----
    ("Medicine_Red", ["potion:1"]),              # Thuoc hoi mau
    ("Medicine_Orange", ["potion:2", "potion:9"]),   # Sieu thuoc hoi mau
    ("Medicine_Purple", ["potion:3"]),           # Thuoc hoi mau cuc manh
    ("Medicine_Red+so:9", ["potion:10", "potion:162"]),  # Thuoc hoi mau toi da / chien truong
    ("Bottle_DarkBlue", ["potion:4"]),           # Ether
    ("Bottle_Purple", ["potion:5"]),              # Ether lon
    ("Bottle_DarkBlue+so:9", ["potion:6", "dp:95>238", "dp:96>238", "dp:127>239"]),  # Ether toi da / 7k / 15k / chien truong
    ("Medicine_Green", ["potion:7"]),            # Elixir
    ("Medicine_Green+so:9", ["potion:8"]),       # Elixir toi da (Ether 7k/15k tach sang o 238)
    ("Scarf_Blue", ["potion:16"]), ("Scarf_Yellow", ["potion:17"]),
    ("Scarf_Green", ["potion:18"]), ("Scarf_Purple", ["potion:19"]),
    ("Gummi_Gold", ["potion:20"]),               # Thuoc khoi phuc tiem nang / Keo tiem nang
    ("Gummi_Magenta", ["potion:21"]),            # Hoi phuc toan dien / Keo ky nang
    ("Bottle_Gray", ["potion:22", "potion:23"]),  # Binh tang toc thuong / bac
    ("Bottle_Gold", ["potion:88"]),              # Binh tang toc vang
    ("Thorn_Yellow", ["potion:26"]),             # Re nang luong
    ("Flame_Orb", ["potion:28", "potion:198", "potion:200"]),  # Nen, long den, lua
    ("Flame_Orb+so:2", ["potion:201"]), ("Flame_Orb+so:3", ["potion:202"]),
    ("Sack_Gray", ["potion:30", "gem:72"]),      # Bot my, Bot trang
    ("Medicine_Purple+so:9", ["potion:31", "potion:163"]),  # Thuoc X
    ("Ring_Target+mau:FFC040", ["potion:33"]),   # Huy hieu lap doi
    ("Gummi_Gold_2", ["potion:36"]),             # Keo kinh nghiem L
    ("Gummi_Blue", ["potion:40"]),               # Keo kinh nghiem M
    ("Gummi_Green", ["potion:62"]),              # Keo kinh nghiem S
    ("Fruit_Tan", ["dp:62>240", "potion:207"]),  # Bi ma, Bi ngo
    ("Box_Yellow", ["potion:42"]),               # Hop may man / than ky
    ("Flower_Gold", ["potion:43", "gem:43"]),    # Hoa mai
    ("Flower_Red", ["potion:45", "potion:181", "gem:55"]),  # Hoa hong
    ("dex:0059", ["potion:46", "thu:1"]),        # Arcanine
    ("dex:0128", ["potion:47", "thu:2"]),        # Tauros
    ("dex:0262", ["potion:48", "thu:3"]),        # Mightyena
    ("dex:0018", ["potion:49", "thu:4"]),        # Pidgeot
    ("dex:0146", ["potion:65", "thu:10", "thu:25"]),  # Moltres
    ("dex:0234", ["dp:36>236", "thu:14"]),       # Stantler
    ("Bow_Red", ["potion:50", "potion:123"]),    # Non noel, Bom Ponyta
    ("dex:0182", ["potion:51"]),                 # Ao Bellossom
    ("Scarf_2_White", ["potion:52", "potion:216", "potion:217", "potion:167", "potion:168"]),  # Ao dai
    ("dex:0792", ["potion:53", "potion:165"]),   # Ao Lunala
    ("dex:0448", ["potion:54"]),                 # Ao Lucario
    ("dex:0384", ["potion:55"]),                 # Ao Rayquaza
    ("Cape_Gray+sang", ["potion:56"]),           # Ao giao su
    ("Cape_Red", ["potion:58", "potion:59"]),    # Noel
    ("Bottle_Purple+so:5", ["potion:60"]),       # Binh tang luc 5%
    ("Paper_News", ["potion:63", "potion:170", "gem:77", "nv:4"]),  # La thu, Giay
    ("Sticky_Barb+mau:FFB040", ["potion:64"]),   # Phao hoa
    ("Gummi_White", ["potion:66", "potion:99"]),  # Banh deo, Kem
    ("Heart_Pink+mau:D08040", ["potion:73"]),    # Nhan trai tim dong
    ("Heart_Pink+mau:E0E0F0", ["potion:74"]),    # bac
    ("Heart_Pink+mau:FFD040", ["potion:75"]),    # vang
    ("Note_Gold", ["potion:77", "gem:51"]),      # Loa, Manh sao
    ("dex:0491", ["potion:80", "potion:81"]),    # Ao Darkrai
    ("dex:0429", ["potion:82"]),                 # Non Mismagius
    ("dex:0710", ["potion:83"]),                 # Dau Pumpkaboo
    ("Apple_Red", ["potion:85"]),                # Thuc an tuoi
    ("Banana_Yellow", ["potion:86"]),            # Thuc an kho
    ("Apple_Gold", ["potion:87"]),               # Thuc an tong hop
    ("Gummi_Red", ["potion:91", "potion:233", "nv:5"]),  # Thit, Thit mo, Thit heo
    ("Chestnut_Brown", ["potion:92"]),           # Banh nuong
    ("dex:0027", ["potion:94", "thu:17"]),       # Pet Sandshrew
    ("Disc_Blue", ["potion:95"]),                # Dia chieu (TM)
    ("dex:0029", ["potion:96", "thu:19"]),       # Pet Nidoran
    ("Cape_Orange", ["potion:97"]),              # Phi phong world cup
    ("Cape_Gray+mau:F0D020", ["potion:98"]),     # Ao brazin
    ("Silk_Gray", ["potion:100", "gem:28"]),     # Vai
    ("Iron_Ball+sang", ["potion:101", "gem:29"]),  # Sat cap
    ("Ore_Purple", ["potion:102", "gem:30"]),    # Ngoc cap
    ("Scale_Black+mau:C08850", ["potion:104", "gem:32"]),  # Da mem cap
    ("Tag_White+mau:60C0FF", ["potion:107"]),    # The giao hang
    ("Seed_Brown", ["potion:108"]),              # Hat mam
    ("Gummi_Brown", ["potion:112"]),             # Socola
    ("Bow_Blue", ["potion:113"]), ("Bow_Brown", ["potion:114"]), ("Bow_Cobalt", ["potion:115"]),
    ("Bow_Green", ["potion:116"]), ("Bow_Lime", ["potion:117"]), ("Bow_Minty", ["potion:118"]),
    ("Bow_Orange", ["potion:119"]), ("Bow_Purple", ["potion:120"]), ("Bow_Silver", ["potion:121"]),
    ("Bow_SkyBlue", ["potion:122"]), ("Bow_Viridian", ["potion:124"]), ("Bow_Yellow", ["potion:125"]),
    ("Bow_Violet", ["potion:128", "potion:130", "potion:132", "potion:134", "potion:136",
                    "potion:138", "potion:140"]),   # Long thoi trang nu
    ("Bow_Cobalt+so:2", ["potion:131", "potion:133", "potion:137", "potion:139"]),  # Long thoi trang nam
    ("Sack_LightBlue", ["potion:141", "gem:73"]),  # Tui nhat ngoc, Bot xanh
    ("Stick_Pink+mau:FF5050", ["potion:142"]),   # Gay giang sinh
    ("dex:0383", ["potion:150"]),                # Ao Groudon
    ("dex:0382", ["potion:151"]),                # Ao Kyogre
    ("dex:0380", ["potion:154"]),                # Ao Latias
    ("dex:0381", ["potion:155"]),                # Ao Latios
    ("dex:0791", ["potion:164"]),                # Ao Solgaleo
    ("Medicine_Green+so:2", ["potion:156"]),     # X Phong thu
    ("Seed_Red", ["potion:157"]),                # Bom lua
    ("Medicine_Orange+so:2", ["potion:158"]),    # X Tan cong
    ("Medicine_Purple+so:2", ["potion:159"]),    # Thuoc san trum
    ("Apricorn_White", ["ds:180>242", "potion:247"]),  # Bong da nam (tach khoi o 162) / Qua bong bac
    ("Apricorn_Black", ["ds:181>243"]),          # Bong da nu (tach khoi o 163)
    ("dex:0393", ["potion:169", "thu:22"]),      # Pet Piplup
    ("Box_Tan", ["potion:171"]),                 # Hop banh thap cam
    ("Flame_Orb+mau:A0A0A0", ["potion:174"]),    # Thuoc phao (Ether chien truong da tach sang 239)
    ("Seed_Green+mau:80C060", ["potion:175", "potion:199"]),  # Banh tet, Dau xanh
    ("Seed_White", ["potion:177"]),              # Gao nep
    ("Box_Green", ["potion:180"]),               # Banh chung
    ("Paper_News+mau:D0A060", ["potion:191", "gem:93"]),  # Ban do
    ("dex:0240", ["potion:192"]),                # Ao Magby
    ("Gummi_Orange", ["potion:208"]),            # Keo
    ("Paper_News+mau:D0A060+so:1", ["potion:213"]),
    ("Paper_News+mau:D0A060+so:2", ["potion:214"]),
    ("Paper_News+mau:D0A060+so:3", ["potion:215"]),  # Manh ban do
    ("Box_Purple", ["potion:222"]), ("Box_LightPurple", ["potion:223"]),  # Hop qua halloween
    ("Cape_Gray+mau:A0E0FF", ["potion:231", "potion:232"]),  # Bang tuyet
    ("Apricorn_Red", ["potion:246"]),            # Poke Ball
    ("Apricorn_Yellow", ["potion:248"]),         # Qua bong vang
    ("dex:0477", ["potion:252"]), ("dex:0354", ["potion:253"]),
    ("dex:0065", ["potion:254"]), ("dex:0487", ["potion:255"]),  # Ao Dusknoir/Banette/Alakazam/Giratina
    ("Fruit_Green", ["potion:14", "gem:79"]),    # Dua hau, Mang cau

    # ---- ngoc / nguyen lieu (anh 6500+K) ----
    ("Orb_White", ["gem:0"]), ("Orb_LightBlue", ["gem:1"]), ("Orb_Blue", ["gem:2"]),  # Bao hiem
    ("Piece_Gray", ["gem:3"]), ("Piece_Blue", ["gem:4"]), ("Piece_Yellow", ["gem:5"]),
    ("Piece_Red", ["gem:41"]), ("Piece_Red+sang", ["gem:42"]),  # Da may man 1..5
    ("Bottle_LightBlue+so:1", ["gem:6"]), ("Bottle_LightBlue+so:2", ["gem:7"]),
    ("Bottle_LightBlue+so:3", ["gem:8"]), ("Bottle_Gold+so:1", ["gem:17"]),
    ("Bottle_Gold+so:2", ["gem:39"]), ("Bottle_Gold+so:3", ["gem:40"]),
    ("Bottle_Gold+so:4", ["gem:71"]),            # Luyen kim duoc
    ("Orb_Yellow", ["gem:9"]), ("Orb_Red", ["gem:10"]), ("Orb_LightBlue+hue:-20+sang", ["gem:12"]),
    ("Orb_Green", ["gem:13"]),                   # Ngoc kham theo chi so
    ("Fang_LightBlue", ["gem:14"]),              # Duc tuong kham
    ("Orb_Pink", ["gem:15"]), ("Crystal_Blue", ["gem:16"]),
    ("Silk_White+sang", ["gem:18"]),             # Soi bong
    ("Rock_Green", ["gem:20"]),                  # Ngoc tho
    ("Scale_Black+mau:C08850+toi", ["gem:22"]),  # Da mem tho
    ("Silk_LightPurple", ["gem:23"]),            # Soi to
    ("Stick_Pink+toi", ["gem:26"]),              # Go sua tho
    ("Scale_Black+toi", ["gem:27"]),             # Da cung tho
    ("Flower_Purple", ["gem:44"]), ("Flower_Brown", ["gem:45"]), ("Herb_Green+toi", ["gem:46"]),  # Manh lan/cuc/truc
    ("Rock_Yellow", ["gem:50"]),                 # Da tach dac biet
    ("Note_Gold+mau:FF6060", ["gem:52"]), ("Note_Gold+mau:60A0FF", ["gem:53"]),
    ("Note_Gold+mau:60D060", ["gem:54"]),        # Manh quat/senh/dan
    ("Paper_News+so:1", ["gem:57"]), ("Paper_News+so:2", ["gem:58"]),
    ("Paper_News+so:3", ["gem:59"]), ("Paper_News+so:4", ["gem:60"]),  # Manh ngu/tieu/canh/doc
    ("Scale_Green", ["gem:61", "nv:8"]),         # Manh long, Da ech
    ("Scale_Black+mau:FFC040", ["gem:62"]),      # Manh lan
    ("Scale_LightBlue+toi", ["gem:63"]),         # Manh quy
    ("Herb_Red+sang", ["gem:64"]),               # Manh phung
    ("Rock_Brown", ["gem:66"]),
    ("Slab_Gold+mau:60D060", ["gem:67"]), ("Slab_Gold", ["gem:68"]),
    ("Slab_Gold+mau:60A0FF", ["gem:69"]), ("Slab_Gold+mau:FF6050", ["gem:70"]),  # Lenh bai
    ("Sack_Red", ["gem:74"]), ("Sack_LightBlue+hue:-80", ["gem:75"]),  # Bot do / xanh la
    ("Herb_White+toi", ["gem:78"]),              # Thuoc
    ("Fruit_Purple", ["gem:80"]), ("Fruit_Brown", ["gem:81"]),
    ("Fruit_Yellow+hue:-25", ["gem:82"]), ("Fruit_Yellow", ["gem:83"]),  # Sung/Dua/Du du/Xoai
    ("Crystal_Red", ["gem:85"]),                 # Hoa thach 2
    ("Orb_Yellow+so:1", ["gem:86"]), ("Orb_Yellow+so:2", ["gem:87"]), ("Orb_Yellow+so:3", ["gem:88"]),
    ("Orb_Yellow+so:4", ["gem:89"]), ("Orb_Yellow+so:5", ["gem:90"]), ("Orb_Yellow+so:6", ["gem:91"]),
    ("Orb_Yellow+so:7", ["gem:92"]),             # Ngoc 1..7 sao
    ("Bottle_Purple+so:9", ["gem:94"]),          # Thuoc hoa hinh

    # ---- icon thu cuoi / pet (anh 7500+K, Animal.idImgIcon + PetTemplate.idImgPaint) ----
    ("dex:0428s", ["thu:13"]), ("dex:0144", ["thu:20", "thu:28"]), ("dex:0831", ["thu:45"]),
    ("dex:0221", ["thu:48"]), ("dex:0897", ["thu:49"]), ("dex:0250", ["thu:51"]),
    ("dex:0357", ["thu:52"]), ("dex:0227", ["thu:53"]), ("dex:0330", ["thu:54"]),
    ("dex:0473", ["thu:55"]), ("dex:0244", ["thu:56"]), ("dex:0896", ["thu:57"]),
    ("dex:0607", ["thu:5"]), ("dex:0276", ["thu:6"]), ("dex:0165", ["thu:7"]), ("dex:0370", ["thu:9"]),
    ("dex:0270", ["thu:11"]), ("dex:0120", ["thu:12"]), ("dex:0615", ["thu:15"]), ("dex:0129", ["thu:21"]),
    ("dex:0179", ["thu:23"]), ("dex:0385", ["thu:24"]), ("dex:0121", ["thu:26"]), ("dex:0337", ["thu:27"]),
    ("dex:0190", ["thu:29"]),

    # ---- vat pham nhiem vu (anh 8000+K, ItemQuest.ICON_IMAGE) ----
    ("Thorn_Green", ["nv:0"]),                   # Noc bo cap
    ("Scale_Black+mau:70B050", ["nv:1"]),        # Ran
    ("Thorn_Brown", ["nv:3"]),                   # Ret do
    ("Claw_White", ["nv:7"]),                    # Mong ca sau
    ("Fang_White", ["nv:9"]),                    # Cua ga

    # ---- trai cay tren cay (anh 8200+K, tob_fruit.id1/id2; 13 = trai heo) ----
    ("Chestnut_Brown+xanh+treo", ["trai:0"]), ("Chestnut_Brown+treo", ["trai:1"]),   # Qua nguyen lieu
    ("Apple_Gold+xanh+treo", ["trai:2"]),                                            # Qua bac/vang (non)
    ("Apple_Gold+treo", ["trai:3"]), ("Apricorn_White+treo", ["trai:4"]),             # Qua vang / bac
    ("Berry_Oran+xanh+treo", ["trai:5"]), ("Berry_Oran+treo", ["trai:6"]),           # Qua exp
    ("Berry_Starf+xanh+treo", ["trai:7"]), ("Berry_Starf+treo", ["trai:8"]),         # Qua may man
    ("Berry_Enigma+xanh+treo", ["trai:9"]), ("Berry_Enigma+treo", ["trai:10"]),      # Qua lkd
    ("Berry_Petaya+xanh+treo", ["trai:11"]), ("Berry_Petaya+treo", ["trai:12"]),     # Qua cau vong
    ("Fruit_Brown+xam+toi+treo", ["trai:13"]),                                       # Trai heo

    # ---- noi dung moi (tools/pokemon/sinh_noi_dung.py, docs/pokemon/NOI_DUNG_DAY_DU.md) ----
    # nguyen lieu nang cap, Huy chuong Gym, hop dot kich, vat pham su kien (data_item 1034..1061)
    ("Piece_Yellow", ["nd:bui_sao"]), ("Piece_Red", ["nd:manh_sao"]), ("Ore_Purple", ["nd:manh_thien_thach"]),
    ("Scale_Green", ["nd:vay_rong"]), ("Crystal_Yellow", ["nd:manh_tinh_hoa"]), ("Crown_Yellow", ["nd:huy_chuong_gym"]),
    ("dex:0145", ["nd:long_vu_zapdos"]), ("dex:0144", ["nd:long_vu_articuno"]), ("dex:0244", ["nd:nanh_lua_entei"]),
    ("dex:0243", ["nd:long_to_raikou"]), ("dex:0491", ["nd:manh_bong_toi"]), ("dex:0245", ["nd:pha_le_suicune"]),
    ("dex:0384", ["nd:vay_rayquaza"]), ("dex:0249", ["nd:long_bac_lugia"]), ("dex:0383", ["nd:ngoc_do_groudon"]),
    ("dex:0382", ["nd:ngoc_lam_kyogre"]), ("Box_Blue", ["nd:hop_tham_gia_dot_kich"]), ("Chest_Gold", ["nd:hop_vang_dot_kich"]),
    ("Crystal_Blue", ["nd:tinh_the_he"]), ("Bow_Orange", ["nd:huy_hieu_ngay_cong_dong"]), ("Paper_News", ["nd:tem_nghien_cuu"]),
    ("Crown_Gray", ["nd:huy_chuong_mua_giai"]), ("Tag_Purple", ["nd:co_vung"]), ("Box_Red", ["nd:qua_le_30_4_1_5"]),
    ("Box_Yellow", ["nd:qua_le_quoc_khanh_2_9"]), ("Flower_Pink", ["nd:qua_le_phu_nu_viet_nam_20_10"]),
    ("Note_Gold", ["nd:qua_le_nha_giao_20_11"]), ("Tag_White", ["nd:phieu_giam_gia_trang_tri"]),
    # tieu hao moi (data_noi_dung_vat_pham trang_thai can_code)
    ("Medicine_Purple", ["nd:chua_doc"]), ("Medicine_Red", ["nd:chua_bong"]), ("Medicine_Yellow", ["nd:chua_te"]),
    ("Medicine_Orange", ["nd:chua_ngu"]), ("Bottle_DarkBlue", ["nd:chua_bang"]), ("Bottle_Gold", ["nd:chua_tat_ca"]),
    ("Herb_Yellow", ["nd:hoi_sinh_2"]), ("Gummi_Sky+sang", ["nd:x_toc_do"]), ("Specs_Yellow", ["nd:x_chinh_xac"]),
    ("Specs_Red", ["nd:x_chi_mang"]), ("Sack_Gray", ["nd:xua_quai_1"]), ("Sack_LightBlue", ["nd:xua_quai_2"]),
    ("Sack_Red", ["nd:xua_quai_3"]), ("Gummi_Gold", ["nd:keo_hiem"]),
    ("Berry_Oran", ["nd:qua_oran"]), ("Berry_Sitrus", ["nd:qua_sitrus"]), ("Berry_Lum", ["nd:qua_lum"]),
    ("Berry_Occa", ["nd:qua_cheri"]), ("Berry_Payapa", ["nd:qua_chesto"]), ("Berry_Kebia", ["nd:qua_pecha"]),
    ("Berry_Rindo", ["nd:qua_rawst"]), ("Berry_Yache", ["nd:qua_aspear"]), ("Berry_Leppa", ["nd:qua_leppa"]),
    ("Apple_Red", ["nd:giu_thuc_an_thua"]), ("Egg_Sea", ["nd:giu_chuong_vo_so"]), ("Claw_White+mau:F0C040", ["nd:giu_mong_nhanh"]),
    ("Band_Green", ["nd:giu_bang_tap_trung"]), ("Specs_DarkBlue", ["nd:giu_kinh_ngam"]), ("Belt_Brown", ["nd:giu_dai_luc"]),
    ("Rock_Gray", ["nd:giu_da_vinh_cuu"]), ("Egg", ["nd:giu_trung_may_man"]),
    # ngoc he 18 he (HePokemon 0..17)
    ("Gummi_White", ["nd:ngoc_he_0"]),
    ("Gummi_Red", ["nd:ngoc_he_1"]),
    ("Gummi_Blue", ["nd:ngoc_he_2"]),
    ("Gummi_Yellow", ["nd:ngoc_he_3"]),
    ("Gummi_Grass", ["nd:ngoc_he_4"]),
    ("Gummi_Sky", ["nd:ngoc_he_5"]),
    ("Gummi_Orange", ["nd:ngoc_he_6"]),
    ("Gummi_Purple", ["nd:ngoc_he_7"]),
    ("Gummi_Brown", ["nd:ngoc_he_8"]),
    ("Gummi_Clear", ["nd:ngoc_he_9"]),
    ("Gummi_Pink", ["nd:ngoc_he_10"]),
    ("Gummi_Green", ["nd:ngoc_he_11"]),
    ("Gummi_Gray", ["nd:ngoc_he_12"]),
    ("Gummi_Magenta", ["nd:ngoc_he_13"]),
    ("Gummi_Royal", ["nd:ngoc_he_14"]),
    ("Gummi_Black", ["nd:ngoc_he_15"]),
    ("Gummi_Silver", ["nd:ngoc_he_16"]),
    ("Orb_Pink", ["nd:ngoc_he_17"]),
    # trang tri theo mua 1..6: danh hieu, khung, trang phuc, hieu ung (0 chi so)
    ("Tag_Purple+so:1", ["nd:trang_tri_danh_hieu_1"]), ("Slab_Gold+so:1", ["nd:trang_tri_khung_1"]),
    ("Cape_Red+so:1", ["nd:trang_tri_trang_phuc_1"]), ("Orb_White+so:1", ["nd:trang_tri_hieu_ung_1"]),
    ("Tag_Purple+so:2", ["nd:trang_tri_danh_hieu_2"]), ("Slab_Gold+so:2", ["nd:trang_tri_khung_2"]),
    ("Cape_Red+so:2", ["nd:trang_tri_trang_phuc_2"]), ("Orb_White+so:2", ["nd:trang_tri_hieu_ung_2"]),
    ("Tag_Purple+so:3", ["nd:trang_tri_danh_hieu_3"]), ("Slab_Gold+so:3", ["nd:trang_tri_khung_3"]),
    ("Cape_Red+so:3", ["nd:trang_tri_trang_phuc_3"]), ("Orb_White+so:3", ["nd:trang_tri_hieu_ung_3"]),
    ("Tag_Purple+so:4", ["nd:trang_tri_danh_hieu_4"]), ("Slab_Gold+so:4", ["nd:trang_tri_khung_4"]),
    ("Cape_Red+so:4", ["nd:trang_tri_trang_phuc_4"]), ("Orb_White+so:4", ["nd:trang_tri_hieu_ung_4"]),
    ("Tag_Purple+so:5", ["nd:trang_tri_danh_hieu_5"]), ("Slab_Gold+so:5", ["nd:trang_tri_khung_5"]),
    ("Cape_Red+so:5", ["nd:trang_tri_trang_phuc_5"]), ("Orb_White+so:5", ["nd:trang_tri_hieu_ung_5"]),
    ("Tag_Purple+so:6", ["nd:trang_tri_danh_hieu_6"]), ("Slab_Gold+so:6", ["nd:trang_tri_khung_6"]),
    ("Cape_Red+so:6", ["nd:trang_tri_trang_phuc_6"]), ("Orb_White+so:6", ["nd:trang_tri_hieu_ung_6"]),
]


# ---------------------------------------------------------------- PNG (stdlib)

ADAM7 = [(0, 0, 8, 8), (4, 0, 8, 8), (0, 4, 4, 8), (2, 0, 4, 4), (0, 2, 2, 4), (1, 0, 2, 2), (0, 1, 1, 2)]


def _bo_loc(tho, q, so_hang, dong_byte, bpp):
    hang = []
    truoc = bytearray(dong_byte)
    for _ in range(so_hang):
        loc = tho[q]
        d = bytearray(tho[q + 1:q + 1 + dong_byte])
        q += 1 + dong_byte
        for i in range(dong_byte):
            a = d[i - bpp] if i >= bpp else 0
            b = truoc[i]
            c = truoc[i - bpp] if i >= bpp else 0
            if loc == 1:
                d[i] = (d[i] + a) & 255
            elif loc == 2:
                d[i] = (d[i] + b) & 255
            elif loc == 3:
                d[i] = (d[i] + ((a + b) >> 1)) & 255
            elif loc == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                d[i] = (d[i] + (a if pa <= pb and pa <= pc else (b if pb <= pc else c))) & 255
        truoc = d
        hang.append(d)
    return hang, q


def doc_png(dl):
    """-> list hang, moi hang list (r, g, b, a). Ho tro 8 bit, bang mau 1/2/4/8 bit, xen ke Adam7."""
    if dl[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("khong phai PNG")
    p, idat, plte, trns = 8, b"", None, None
    while p < len(dl):
        n, loai = struct.unpack(">I4s", dl[p:p + 8])
        k = dl[p + 8:p + 8 + n]
        p += 12 + n
        if loai == b"IHDR":
            w, h, bd, ct, _, _, xen = struct.unpack(">IIBBBBB", k)
        elif loai == b"PLTE":
            plte = k
        elif loai == b"tRNS":
            trns = k
        elif loai == b"IDAT":
            idat += k
        elif loai == b"IEND":
            break
    if bd == 16:
        raise ValueError("PNG 16 bit chua ho tro")
    kenh = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[ct]
    bpp = max(1, kenh * bd // 8)
    tho = zlib.decompress(idat)

    def px(d, x):
        if bd < 8:
            moi = 8 // bd
            v = (d[x // moi] >> (8 - bd * (x % moi + 1))) & ((1 << bd) - 1)
        else:
            v = d[x * kenh]
        if ct == 3:
            return tuple(plte[v * 3:v * 3 + 3]) + ((trns[v] if trns and v < len(trns) else 255),)
        if ct == 0:
            g = v * 255 // ((1 << bd) - 1)
            return (g, g, g, 255)
        if ct == 6:
            return tuple(d[x * 4:x * 4 + 4])
        if ct == 2:
            r, g, b = d[x * 3:x * 3 + 3]
            a = 0 if trns and len(trns) == 6 and struct.unpack(">HHH", trns) == (r, g, b) else 255
            return (r, g, b, a)
        g, a = d[x * 2:x * 2 + 2]
        return (g, g, g, a)

    anh = [[(0, 0, 0, 0)] * w for _ in range(h)]
    if xen == 0:
        hang, _ = _bo_loc(tho, 0, h, (w * kenh * bd + 7) // 8, bpp)
        for y in range(h):
            anh[y] = [px(hang[y], x) for x in range(w)]
    else:
        q = 0
        for x0, y0, dx, dy in ADAM7:
            sw, sh = (w - x0 + dx - 1) // dx, (h - y0 + dy - 1) // dy
            if sw <= 0 or sh <= 0:
                continue
            hang, q = _bo_loc(tho, q, sh, (sw * kenh * bd + 7) // 8, bpp)
            for j in range(sh):
                for i in range(sw):
                    anh[y0 + j * dy][x0 + i * dx] = px(hang[j], i)
    return anh


def ghi_png(anh):
    h, w = len(anh), len(anh[0])
    tho = b"".join(b"\x00" + bytes(c for p in r for c in p) for r in anh)

    def khoi(loai, d):
        return struct.pack(">I", len(d)) + loai + d + struct.pack(">I", zlib.crc32(loai + d) & 0xffffffff)
    return (b"\x89PNG\r\n\x1a\n" + khoi(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + khoi(b"IDAT", zlib.compress(tho, 9)) + khoi(b"IEND", b""))


# ---------------------------------------------------------------- chu 3x5 (so tren icon + nhan xem truoc)

FONT = {
    "0": "111101101101111", "1": "010110010010111", "2": "111001111100111", "3": "111001111001111",
    "4": "101101111001001", "5": "111100111001111", "6": "111100111101111", "7": "111001010010010",
    "8": "111101111101111", "9": "111101111001111", "A": "010101111101101", "B": "110101110101110",
    "C": "011100100100011", "D": "110101101101110", "E": "111100110100111", "F": "111100110100100",
    "G": "011100101101011", "H": "101101111101101", "I": "111010010010111", "J": "001001001101010",
    "K": "101101110101101", "L": "100100100100111", "M": "101111111101101", "N": "110101101101101",
    "O": "010101101101010", "P": "110101110100100", "Q": "010101101110011", "R": "110101110101101",
    "S": "011100010001110", "T": "111010010010010", "U": "101101101101111", "V": "101101101101010",
    "W": "101101111111101", "X": "101101010101101", "Y": "101101010010010", "Z": "111001010100111",
    " ": "000000000000000", "-": "000000111000000", ":": "000010000010000", ".": "000000000000010",
    ">": "100010001010100", "/": "001001010100100", "+": "000010111010000", "_": "000000000000111",
    ",": "000000000010100", "|": "010010010010010",
}


def ve_chu(anh, x, y, s, mau=(255, 255, 255, 255), vien=None):
    h, w = len(anh), len(anh[0])

    def dat(xx, yy, m):
        if 0 <= yy < h and 0 <= xx < w:
            anh[yy][xx] = m
    for ch in s.upper():
        m = FONT.get(ch, FONT[" "])
        diem = [(x + i, y + j) for j in range(5) for i in range(3) if m[j * 3 + i] == "1"]
        if vien:
            for xx, yy in diem:
                for dx in (-1, 0, 1):
                    for dy in (-1, 0, 1):
                        dat(xx + dx, yy + dy, vien)
        for xx, yy in diem:
            dat(xx, yy, mau)
        x += 4


# ---------------------------------------------------------------- bien doi

def _moi_diem(anh, f):
    return [[f(p) if p[3] else p for p in r] for r in anh]


def bien_doi(anh, lenh):
    if lenh.startswith("hue:"):
        do = float(lenh[4:]) / 360.0

        def f(p):
            h, l, s = colorsys.rgb_to_hls(p[0] / 255.0, p[1] / 255.0, p[2] / 255.0)
            r, g, b = colorsys.hls_to_rgb((h + do) % 1.0, l, s)
            return (int(r * 255), int(g * 255), int(b * 255), p[3])
        return _moi_diem(anh, f)
    if lenh.startswith("mau:"):
        m = [int(lenh[4 + i:6 + i], 16) for i in (0, 2, 4)]

        def f(p):
            l = (p[0] * 299 + p[1] * 587 + p[2] * 114) / 255000.0
            return tuple(min(255, int(c * l * 1.25 + 40 * l * l)) for c in m) + (p[3],)
        return _moi_diem(anh, f)
    if lenh == "toi":
        return _moi_diem(anh, lambda p: (p[0] * 6 // 10, p[1] * 6 // 10, p[2] * 6 // 10, p[3]))
    if lenh == "sang":
        return _moi_diem(anh, lambda p: tuple(c + (255 - c) * 35 // 100 for c in p[:3]) + (p[3],))
    if lenh == "xam":
        return _moi_diem(anh, lambda p: (lambda l: (l, l, l, p[3]))((p[0] * 299 + p[1] * 587 + p[2] * 114) // 1000))
    if lenh == "xanh":
        return _moi_diem(anh, lambda p: (
            (p[0] * 4 + 110 * 6) // 10, (p[1] * 4 + 190 * 6) // 10, (p[2] * 4 + 80 * 6) // 10, p[3]))
    if lenh.startswith("so:"):
        anh = [list(r) for r in anh]
        so = lenh[3:]
        ve_chu(anh, len(anh[0]) - 4 * len(so) - 1 + 1, len(anh) - 6, so, (255, 240, 120, 255), (40, 24, 8, 255))
        return anh
    if lenh == "treo":
        w = len(anh[0])
        moi = [[(0, 0, 0, 0)] * w for _ in range(10)] + [list(r) for r in anh]
        # cuong: tim diem dac cao nhat o giua roi ve len tren
        x = w // 2
        for y in range(10, len(moi)):
            if moi[y][x][3]:
                break
            moi[y][x] = (96, 64, 32, 255)
        for y in range(1, 10):
            moi[y][x] = (96, 64, 32, 255)
        for i, j in ((x + 1, 3), (x + 2, 3), (x + 2, 2), (x + 3, 2), (x + 1, 4)):
            if i < w:
                moi[j][i] = (72, 150, 56, 255)
        return moi
    raise ValueError("bien doi la: " + lenh)


def thu_nho_chan_dung(anh):
    """40x40 (nen gradient) -> 20x20 trung binh 2x2, vien toi 1px."""
    out = []
    for y in range(len(anh) // 2):
        r = []
        for x in range(len(anh[0]) // 2):
            ps = [anh[y * 2 + j][x * 2 + i] for j in (0, 1) for i in (0, 1)]
            r.append(tuple(sum(p[c] for p in ps) // 4 for c in range(3)) + (255,))
        out.append(r)
    n = len(out)
    for i in range(n):
        for (x, y) in ((i, 0), (i, n - 1), (0, i), (n - 1, i)):
            out[y][x] = (32, 32, 40, 255)
    return out


# ---------------------------------------------------------------- tai

def tai(url, dich, loi):
    if os.path.exists(dich):
        return True
    os.makedirs(os.path.dirname(dich), exist_ok=True)
    req = urllib.request.Request(url, headers={"User-Agent": "htbb-pokemon-tool"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            data = r.read()
    except Exception as e:
        loi.append("tai %s: %s" % (url, e))
        return False
    open(dich, "wb").write(data)
    return True


def anh_nguon(nguon, loi):
    phan = nguon.split("+")
    goc = phan[0]
    if goc.startswith("dex:"):
        dex = goc[4:]
        shiny = dex.endswith("s")
        so = dex.rstrip("s")
        duong = "%s/0000/0001" % so if shiny else so
        dich = os.path.join(TAI, "Portrait", dex + ".png")
        if not tai(NGUON_CHAN_DUNG % duong, dich, loi):
            return None
        anh = thu_nho_chan_dung(doc_png(open(dich, "rb").read()))
    else:
        dich = os.path.join(TAI, "Item", goc + ".None.png")
        if not tai(NGUON_ITEM % goc, dich, loi):
            return None
        anh = doc_png(open(dich, "rb").read())
    for lenh in phan[1:]:
        anh = bien_doi(anh, lenh)
    return anh


# ---------------------------------------------------------------- dung cho

def tach_id(s):
    ra = []
    for phan in s.split(","):
        if "-" in phan:
            a, b = phan.split("-")
            ra.extend(range(int(a), int(b) + 1))
        else:
            ra.append(int(phan))
    return ra


def phan_tich(loi):
    """-> dict ket qua: item_type, item_id, xs, dai{ten:{K:n}}, dp, ds"""
    kq = {"type": [], "item": {}, "xs": {}, "dai": {k: {} for k in DAI}, "dp": {}, "ds": {}, "nd": {}}
    for n, (nguon, dung) in enumerate(ICON):
        for d in dung:
            loai, gt = d.split(":", 1)
            if loai == "type":
                kq["type"].append((int(gt), n))
            elif loai == "item":
                for i in tach_id(gt):
                    if i in kq["item"]:
                        loi.append("item %d gan 2 lan (n=%d, n=%d)" % (i, kq["item"][i], n))
                    kq["item"][i] = n
            elif loai == "xs":
                x = int(gt)
                if x in kq["xs"]:
                    loi.append("xs %d gan 2 lan" % x)
                kq["xs"][x] = n
            elif loai in DAI:
                k = int(gt)
                if not 0 <= k < DAI[loai][1]:
                    loi.append("%s:%d ngoai dai" % (loai, k))
                if k in kq["dai"][loai] and kq["dai"][loai][k] != n:
                    loi.append("%s:%d gan 2 lan (n=%d va n=%d)" % (loai, k, kq["dai"][loai][k], n))
                kq["dai"][loai].setdefault(k, n)
            elif loai == "nd":
                if gt in kq["nd"]:
                    loi.append("nd:%s gan 2 lan" % gt)
                kq["nd"][gt] = n
            elif loai in ("dp", "ds"):
                ident, k = gt.split(">")
                kq[loai][int(ident)] = int(k)
                if int(k) in kq["dai"]["potion"] and kq["dai"]["potion"][int(k)] != n:
                    loi.append("o thuoc %s dung cho 2 icon" % k)
                kq["dai"]["potion"][int(k)] = n
            else:
                loi.append("dung cho la: " + d)
    return kq


def ghi_sql(kq, loi):
    theo_type = {}
    with open(BANG_VP, encoding="utf8") as f:
        for r in csv.DictReader(f):
            theo_type.setdefault(int(r["type"]), []).append(int(r["id"]))
    dong = []
    for t, n in kq["type"]:
        ids = theo_type.get(t, [])
        if not ids:
            loi.append("type %d khong co trong bang_vat_pham.csv" % t)
            continue
        dong.append("UPDATE data_item SET xstart=%d WHERE id IN (%s); -- %s"
                    % (ID_DAU + n, ",".join(map(str, ids)), ICON[n][0]))
    # item: chay truoc xs: (mot nhom icon cu tach theo tung vat pham, vd pet theo cap tien hoa)
    theo_n = {}
    for i, n in kq["item"].items():
        theo_n.setdefault(n, []).append(i)
    for n in sorted(theo_n):
        ids = sorted(theo_n[n])
        dong.append("UPDATE data_item SET xstart=%d WHERE id %s; -- %s"
                    % (ID_DAU + n, ("=%d" % ids[0]) if len(ids) == 1 else "IN (%s)" % ",".join(map(str, ids)),
                       ICON[n][0]))
    for x in sorted(kq["xs"]):
        n = kq["xs"][x]
        dong.append("UPDATE data_item SET xstart=%d WHERE xstart=%d; -- %s" % (ID_DAU + n, x, ICON[n][0]))
    for ident in sorted(kq["dp"]):
        dong.append("UPDATE data_potion SET idImage=%d WHERE id=%d; -- tach o anh thuoc"
                    % (kq["dp"][ident], ident))
    for ident in sorted(kq["ds"]):
        dong.append("UPDATE data_shop SET idImg=%d WHERE id=%d; -- tach o anh thuoc" % (kq["ds"][ident], ident))
    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_icon_pokemon.py - KHONG SUA TAY.\n")
        f.write("-- Icon PMD o dai anh %d+ (map/icon_pokemon/). Chay SAU pokemon_vat_pham_eevee.sql.\n" % ID_DAU)
        f.write("-- Thu tu: trang bi theo type, vat pham theo id, roi theo icon cu (xstart < 866),\n")
        f.write("-- cuoi cung tach o anh thuoc dung chung (data_potion.idImage / data_shop.idImg).\n")
        f.write("-- SO_DONG: %d (test KiemIconPokemon dem lai)\n" % len(dong))
        for d in dong:
            f.write(d + "\n")
    return len(dong)


def ghi_java(kq):
    with open(RA_JAVA, "w", encoding="utf8", newline="\n") as f:
        f.write("package com.htbb.pokemon;\n\n")
        f.write("/**\n * SINH TU tools/pokemon/sinh_icon_pokemon.py - KHONG SUA TAY.\n *\n")
        f.write(" * Chi so anh cu (dai khong co cot DB) -> so thu tu icon PMD n (anh map/icon_pokemon/n.png,\n")
        f.write(" * id 14000+n). -1 = giu anh cu. Dung qua IconPokemon.doiAnh khi bat che do Pokemon.\n */\n")
        f.write("final class IconPokemonBang {\n\n    private IconPokemonBang() {\n    }\n")
        for ten in DAI:
            dau, so, _, _ = DAI[ten]
            mang = [kq["dai"][ten].get(k, -1) for k in range(so)]
            f.write("\n    /** Anh %d+K. */\n    static final int %s_DAU = %d;\n" % (dau, TEN_JAVA[ten], dau))
            f.write("    static final short[] %s = {" % TEN_JAVA[ten])
            for i, v in enumerate(mang):
                f.write(("\n        " if i % 16 == 0 else " ") + "%d," % v)
            f.write("\n    };\n")
        f.write("}\n")


def ghi_csv(kq):
    dung_n = {}
    for n, (_, dung) in enumerate(ICON):
        dung_n[n] = " ".join(dung)
    with open(RA_CSV, "w", encoding="utf8", newline="\n") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["n", "id_anh", "nguon", "dung_cho"])
        for n, (nguon, dung) in enumerate(ICON):
            w.writerow([n, ID_DAU + n, nguon, " ".join(dung)])


# ---------------------------------------------------------------- xem truoc

def anh_cu(loai, k):
    if loai in DAI:
        dau, _, tm, mau = DAI[loai]
        p = os.path.join(GOC, tm, mau % k)
    else:
        p = os.path.join(GOC, "map", "icon", "%d.png" % k)
    try:
        return doc_png(open(p, "rb").read())
    except Exception:
        return None


def xem_truoc(anh_moi, kq):
    o = []  # (nhan, cu, moi)
    for x in sorted(kq["xs"]):
        o.append(("XS %d" % x, anh_cu("xs", x), anh_moi[kq["xs"][x]]))
    for ten in DAI:
        for k in sorted(kq["dai"][ten]):
            o.append(("%s %d" % (ten.upper(), k), anh_cu(ten, k), anh_moi[kq["dai"][ten][k]]))
    moc = [(nguon, n) for n, (nguon, dung) in enumerate(ICON)
           if any(d.startswith("item:") or d.startswith("type:") for d in dung)]
    for nguon, n in moc:
        nhan = [d for d in ICON[n][1] if d.startswith("item:") or d.startswith("type:")][0]
        o.append((nhan.replace("item:", "IT ").replace("type:", "TYPE ")[:13], None, anh_moi[n]))
    cot, rong, cao, zoom = 10, 96, 66, 2
    hang = (len(o) + cot - 1) // cot
    W, H = cot * rong, hang * cao + 12
    nen = [[(36, 36, 44, 255)] * W for _ in range(H)]
    ve_chu(nen, 4, 3, "CU | MOI  - XEM TRUOC ICON POKEMON (sinh_icon_pokemon.py)", (255, 230, 120, 255))
    for idx, (nhan, cu, moi) in enumerate(o):
        x0, y0 = (idx % cot) * rong, (idx // cot) * cao + 12
        for i in range(rong):
            nen[y0][x0 + i] = (80, 80, 92, 255)
        for j in range(cao):
            nen[y0 + j][x0] = (80, 80, 92, 255)
        for so_o, a in ((0, cu), (1, moi)):
            if a is None:
                continue
            ox = x0 + 3 + so_o * 46
            tl = zoom if len(a) <= 26 and len(a[0]) <= 22 else 1
            for j, r in enumerate(a[:52 // tl]):
                for i, p in enumerate(r[:44 // tl]):
                    if p[3] < 100:
                        continue
                    for aa in range(tl):
                        for bb in range(tl):
                            nen[y0 + 3 + j * tl + aa][ox + i * tl + bb] = (p[0], p[1], p[2], 255)
        ve_chu(nen, x0 + 3, y0 + cao - 7, nhan)
    open(RA_XEM, "wb").write(ghi_png(nen))
    return len(o)


def main():
    loi = []
    if len(ICON) > SO_TOI_DA:
        loi.append("qua %d icon" % SO_TOI_DA)
    kq = phan_tich(loi)
    os.makedirs(RA_ANH, exist_ok=True)
    anh_moi = []
    for n, (nguon, _) in enumerate(ICON):
        try:
            a = anh_nguon(nguon, loi)
        except Exception as e:
            loi.append("%s: %s" % (nguon, e))
            a = None
        anh_moi.append(a)
        if a is None:
            continue
        h, w = len(a), len(a[0])
        la_trai = "treo" in nguon
        if (w > 24 or h > 24) and not (la_trai and w <= 24 and h <= 26):
            loi.append("%s %dx%d qua lon cho o vat pham" % (nguon, w, h))
        dich = os.path.join(RA_ANH, "%d.png" % n)
        # Ghi lai chi khi khac DIEM ANH (giu nguyen byte file cu da phuc vu cho client).
        try:
            giong = doc_png(open(dich, "rb").read()) == [[tuple(p) for p in r] for r in a]
        except Exception:
            giong = False
        if not giong:
            open(dich, "wb").write(ghi_png(a))
    # xoa anh thua (bang bi rut ngan)
    for f in os.listdir(RA_ANH):
        m = re.match(r"^(\d+)\.png$", f)
        if m and int(m.group(1)) >= len(ICON):
            os.remove(os.path.join(RA_ANH, f))
    so_dong = ghi_sql(kq, loi)
    ghi_java(kq)
    ghi_csv(kq)
    print("Da ghi %d icon -> map/icon_pokemon/, %d dong SQL -> %s, %s, %s"
          % (len(ICON), so_dong, os.path.relpath(RA_SQL, GOC), os.path.relpath(RA_JAVA, GOC),
             os.path.relpath(RA_CSV, GOC)))
    for ten in DAI:
        print("  dai %-6s %d/%d o anh cu doi sang PMD" % (ten, len(kq["dai"][ten]), DAI[ten][1]))
    if "--xem-truoc" in sys.argv:
        print("Xem truoc: %d o -> %s" % (xem_truoc(anh_moi, kq), os.path.relpath(RA_XEM, GOC)))
    for l in loi:
        print("LOI:", l)
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
