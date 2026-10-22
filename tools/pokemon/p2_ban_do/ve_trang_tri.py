"""P2 (nhanh ban do): den long Poke Ball (server/map/longden.png, 6 khung), tuong Poke Ball (rong.png,
rong1.png), ruong mini-game (box.sh). Tu ve (CC0).  python3 tools/pokemon/p2_ban_do/ve_trang_tri.py <thu muc ra>
"""
import math,sys
from PIL import Image,ImageDraw
OUT=sys.argv[1]
K=(24,20,28,255)
def ball(d,cx,cy,r,top,bot,band=K,btn=(250,250,240,255),glow=None,outline=K,hi=(255,255,255,200)):
    d.ellipse((cx-r,cy-r,cx+r,cy+r),fill=bot,outline=outline)
    d.pieslice((cx-r,cy-r,cx+r,cy+r),180,360,fill=top,outline=outline)
    d.rectangle((cx-r,cy-1,cx+r,cy+1),fill=band)
    rb=max(3,r//3); d.ellipse((cx-rb,cy-rb,cx+rb,cy+rb),fill=band)
    d.ellipse((cx-rb+2,cy-rb+2,cx+rb-2,cy+rb-2),fill=glow or btn)
    d.ellipse((cx-r//2-2,cy-r+3,cx-r//2+2,cy-r+6),fill=hi)
# --- longden: 6 khung 80x60, den long Poke Ball treo tren cot, dung dua + nen lap loe
W,H,NF=80,60,6
sheet=Image.new('RGBA',(W,H*NF),(0,0,0,0))
for k in range(NF):
    f=Image.new('RGBA',(W,H),(0,0,0,0)); d=ImageDraw.Draw(f)
    a=math.sin(2*math.pi*k/NF)*0.22
    # cot go + xa ngang
    d.rectangle((14,6,17,59),fill=(120,72,40,255),outline=K)
    d.rectangle((14,6,62,9),fill=(140,86,48,255),outline=K)
    d.rectangle((10,56,22,59),fill=(96,60,34,255),outline=K)
    px,py=56,9; L=16
    cx=int(px+math.sin(a)*L); cy=int(py+math.cos(a)*L)+13
    d.line((px,py,cx,cy-13),fill=K)
    # quang sang
    gl=Image.new('RGBA',(W,H),(0,0,0,0)); gd=ImageDraw.Draw(gl)
    rr=20+(k%2)
    gd.ellipse((cx-rr,cy-rr,cx+rr,cy+rr),fill=(255,214,90,60)); gd.ellipse((cx-15,cy-15,cx+15,cy+15),fill=(255,226,120,70))
    f=Image.alpha_composite(f,gl); d=ImageDraw.Draw(f)
    glow=(255,236,120,255) if k%2==0 else (255,206,80,255)
    ball(d,cx,cy,12,(226,56,58,255),(246,240,226,255),glow=glow)
    # tua
    for dx in (-4,0,4):
        d.line((cx+dx,cy+12,cx+dx+int(math.sin(a)*3),cy+19),fill=(250,196,40,255))
    d.rectangle((cx-5,cy+12,cx+5,cy+13),fill=(250,196,40,255),outline=None)
    sheet.paste(f,(0,k*H))
sheet.save(OUT+'/longden.png',optimize=True)
# --- rong / rong1: tuong Poke Ball tren be (128x69): da xam / vang
for name,top,bot,base in [('rong',(150,156,166,255),(206,210,214,255),(120,124,132,255)),('rong1',(214,170,52,255),(244,224,150,255),(150,116,40,255))]:
    f=Image.new('RGBA',(128,69),(0,0,0,0)); d=ImageDraw.Draw(f)
    d.ellipse((14,50,114,68),fill=(0,0,0,60))
    d.rectangle((34,46,94,62),fill=base,outline=K); d.rectangle((28,58,100,66),fill=base,outline=K)
    d.line((36,49,92,49),fill=(255,255,255,90))
    ball(d,64,25,22,top,bot,band=(60,62,70,255),btn=bot)
    sheet=f; f.save(OUT+'/'+name+'.png',optimize=True)
