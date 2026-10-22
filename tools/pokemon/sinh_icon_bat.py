#!/usr/bin/env python3
"""Sinh tam icon pixel-art cho Hop PC / trao doi / Pokedex (0.9.8 bat-trade): client_jar/res/pk/bat_16.png, bat_32.png
(+ ban sao res_nhe/pk). Moi o 16 x 16 ve tay theo luoi ky tu duoi day; ban 32 = phong 2 lan giu net (nearest).

Thu tu o (client game.pokemon.IconBat): 0 sao day, 1 sao rong, 2 lap lanh sac mau, 3 Poke Ball, 4 Great Ball,
5 Ultra Ball, 6 Keo loai, 7 trao doi (hai mui ten), 8 khoa, 9 tien hoa (mui ten len).

Chay o goc monorepo: python3 tools/pokemon/sinh_icon_bat.py
"""
import os
from PIL import Image

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

MAU = {
    '.': None,
    'k': (40, 28, 24, 255),      # vien toi
    'y': (255, 214, 64, 255),    # vang
    'Y': (255, 244, 170, 255),   # vang sang
    'o': (214, 140, 20, 255),    # vang dam
    'g': (150, 156, 166, 255),   # xam
    'G': (206, 212, 220, 255),   # xam sang
    'r': (226, 58, 44, 255),     # do
    'R': (255, 130, 110, 255),   # do sang
    'w': (255, 255, 255, 255),
    'W': (222, 226, 232, 255),   # trang bong
    'b': (52, 104, 216, 255),    # xanh duong
    'B': (120, 170, 255, 255),
    'd': (36, 36, 44, 255),      # den
    'p': (236, 120, 190, 255),   # hong (keo)
    'P': (255, 190, 226, 255),
    't': (30, 170, 160, 255),    # teal
    'T': (120, 226, 214, 255),
    'c': (130, 230, 255, 255),   # lap lanh
    'C': (230, 250, 255, 255),
}

O = [
    # 0 sao day
    """
.......kk.......
......kyyk......
......kyyk......
.....kyYyyk.....
kkkkkyYYyyykkkkk
kyyyyyYyyyyyyyok
.kyyyyyyyyyyyok.
..kyyyyyyyyyok..
...kyyyyyyyok...
...kyyyyyyyok...
..kyyyyooyyyok..
..kyyyokkoyyok..
.kyyyok..koyyok.
.kyyok....koyok.
kyook......koook
kkkk........kkkk
""",
    # 1 sao rong
    """
.......kk.......
......kGGk......
......kGGk......
.....kG..Gk.....
kkkkkG....Gkkkkk
kGG..........GGk
.kG..........Gk.
..kG........Gk..
...kG......Gk...
...kG......Gk...
..kG...GG...Gk..
..kG..GkkG..Gk..
.kG..Gk..kG..Gk.
.kG.Gk....kG.Gk.
kGGGk......kGGGk
kkkk........kkkk
""",
    # 2 lap lanh (sac mau)
    """
.......k........
......kck.......
......kCk.......
.....kcCck......
..kkkcCwCckkk...
.kccCCwwwCCcck..
..kkkcCwCckkk...
.....kcCck....k.
......kCk....kck
......kck...kcCc
.......k.....kck
..k...........k.
.kck............
kcCck...........
.kck............
..k.............
""",
    # 3 Poke Ball
    """
.....kkkkkk.....
...kkrrrrrRkk...
..krrrrrrrRRrk..
.krrrrrrrrrRrrk.
.krrrrrrrrrrrrk.
krrrrrrkkrrrrrrk
krrrrrkwwkrrrrrk
kkkkkkkwwkkkkkkk
kwwwwwkwwkwwwwwk
kwwwwwwkkwwwwwWk
.kwwwwwwwwwwwWk.
.kwwwwwwwwwwWWk.
..kwwwwwwwwWWk..
...kkWWWWWWkk...
.....kkkkkk.....
................
""",
    # 4 Great Ball
    """
.....kkkkkk.....
...kkbbbbbBkk...
..krrbbbbbbBrk..
.krrrbbbbbbbrrk.
.kbrrbbbbbbrrbk.
kbbbbbbkkbbbbbbk
kbbbbbkwwkbbbbbk
kkkkkkkwwkkkkkkk
kwwwwwkwwkwwwwwk
kwwwwwwkkwwwwwWk
.kwwwwwwwwwwwWk.
.kwwwwwwwwwwWWk.
..kwwwwwwwwWWk..
...kkWWWWWWkk...
.....kkkkkk.....
................
""",
    # 5 Ultra Ball
    """
.....kkkkkk.....
...kkddddddkk...
..kyydddddddyk..
.kyyyddddddyyyk.
.kdyyyddddyyydk.
kdddddddkkddddk.
kddddddkwwkdddk.
kkkkkkkkwwkkkkkk
kwwwwwwkwwkwwwwk
kwwwwwwwkkwwwwWk
.kwwwwwwwwwwwWk.
.kwwwwwwwwwwWWk.
..kwwwwwwwwWWk..
...kkWWWWWWkk...
.....kkkkkk.....
................
""",
    # 6 Keo loai
    """
................
.kk..........kk.
kPpk........kpPk
kppPk..kk..kPppk
.kppkkkppkkkppk.
..kkpPPPPppkkk..
...kpPwPPpppk...
..kppPPPPppppk..
..kpppPPpppppk..
...kpppppppok...
..kkkpppppokkk..
.kppkkkookkkppk.
kPppk..kk..kppPk
kPpk........kpPk
.kk..........kk.
................
""",
    # 7 trao doi (hai mui ten)
    """
................
.........kk.....
.........ktk....
.kkkkkkkkkttk...
.kTTTTTTTTTttk..
.kttttttttttttk.
.kkkkkkkkkttk...
.........ktk....
....kk...kk.....
...krk..........
..krrkkkkkkkkkk.
.krrrrrrrrrrrRk.
..krrRRRRRRRRRk.
...krkkkkkkkkkk.
....kk..........
................
""",
    # 8 khoa
    """
................
.....kkkkkk.....
....kgGGGGgk....
...kgk....kgk...
...kGk....kGk...
...kGk....kGk...
..kkkkkkkkkkkk..
..kyyYYYYYYyyk..
..kyYYYkkYYYyk..
..kyYYkddkYYyk..
..kyyYYkkYYyyk..
..kyyyYkkYyyyk..
..koyyyyyyyyok..
..kooooooooook..
..kkkkkkkkkkkk..
................
""",
    # 9 tien hoa (mui ten len)
    """
.......kk.......
......kTTk......
.....kTttTk.....
....kTttttTk....
...kTttttttTk...
..kTttttttttTk..
.kkkkkttttkkkkk.
.....kttttk.....
.....kttttk.....
.....kttttk.....
.....kttttk.....
.....kttttk.....
.....kttttk.....
.....kttttk.....
.....kkkkkk.....
................
""",
]


def ve(luoi):
    dong = [d for d in luoi.strip('\n').split('\n')]
    assert len(dong) == 16, (len(dong), luoi)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, d in enumerate(dong):
        assert len(d) == 16, (y, d)
        for x, ch in enumerate(d):
            m = MAU[ch]
            if m:
                im.putpixel((x, y), m)
    return im


def main():
    o = [ve(l) for l in O]
    tam = Image.new('RGBA', (16 * len(o), 16), (0, 0, 0, 0))
    for i, im in enumerate(o):
        tam.paste(im, (16 * i, 0))
    for thu_muc in ('client_jar/res/pk', 'client_jar/res_nhe/pk'):
        d = os.path.join(GOC, thu_muc)
        os.makedirs(d, exist_ok=True)
        tam.save(os.path.join(d, 'bat_16.png'), optimize=True)
        tam.resize((tam.width * 2, 32), Image.NEAREST).save(os.path.join(d, 'bat_32.png'), optimize=True)
        print('da ghi', thu_muc, '/bat_16.png, bat_32.png (%d o)' % len(o))


if __name__ == '__main__':
    main()
