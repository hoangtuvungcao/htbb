"""P2 (nhanh ban do): ve 3 trang thai o vuon 32x32 (goi g.sh jar / g.bytes Unity, lop Ground). Tu ve (CC0).
    python3 tools/pokemon/p2_ban_do/ve_dat.py <thu muc ra>
"""
import sys,random
from PIL import Image,ImageDraw
O=sys.argv[1]; K=(42,28,22,255)
def plot(base,furrow,hi,grass,seed,tuft=False,wet=False):
    random.seed(seed)
    im=Image.new('RGBA',(32,32),(0,0,0,0)); d=ImageDraw.Draw(im)
    d.rounded_rectangle((0,0,31,31),6,fill=grass,outline=K)
    d.rounded_rectangle((3,3,28,28),4,fill=base,outline=(furrow[0]-20,furrow[1]-14,furrow[2]-10,255))
    if not tuft:
        for y in range(7,27,5):
            d.line((5,y,26,y),fill=furrow); d.line((5,y-1,26,y-1),fill=hi)
    for _ in range(14):
        x,y=random.randint(5,26),random.randint(5,26); d.point((x,y),fill=furrow if random.random()<.6 else hi)
    if tuft:
        for _ in range(7):
            x,y=random.randint(6,24),random.randint(7,25)
            d.line((x,y,x-1,y-3),fill=(84,140,46,255)); d.line((x+1,y,x+2,y-3),fill=(110,170,60,255))
    if wet:
        for _ in range(4):
            x,y=random.randint(7,24),random.randint(8,24); d.point((x,y),fill=(150,200,230,255)); d.point((x+1,y),fill=(220,240,255,255))
    for (x,y) in [(4,1),(12,0),(22,1),(1,13),(30,20),(15,30),(26,30)]:
        d.point((x,y),fill=(120,176,64,255))
    return im
G=(106,160,56,255)
plot((184,119,74,255),(140,84,52,255),(212,154,104,255),G,1).save(O+'/g0.png')
plot((196,150,98,255),(150,108,66,255),(214,176,120,255),G,2,tuft=True).save(O+'/g1.png')
plot((138,85,52,255),(99,57,31,255),(164,110,72,255),G,3,wet=True).save(O+'/g2.png')
def hop(open_):
    W=22; H=20 if open_ else 17
    im=Image.new('RGBA',(W,H),(0,0,0,0)); d=ImageDraw.Draw(im)
    R=(216,56,58,255); Rd=(150,30,40,255); C=(240,232,214,255); Cd=(196,186,166,255)
    if open_:
        d.rectangle((1,0,20,6),fill=Rd,outline=K); d.line((3,2,18,2),fill=R)
        d.rectangle((1,7,20,19),fill=C,outline=K); d.rectangle((2,8,19,10),fill=(255,214,90,255))
        d.rectangle((1,11,20,12),fill=K); d.line((2,17,19,17),fill=Cd)
        d.ellipse((8,9,13,14),fill=K); d.ellipse((9,10,12,13),fill=(255,255,255,255))
        for x,y in [(5,6),(11,4),(16,6)]: d.point((x,y),fill=(255,240,140,255))
    else:
        d.rectangle((1,0,20,7),fill=R,outline=K); d.line((3,2,9,2),fill=(255,140,140,255))
        d.rectangle((1,8,20,16),fill=C,outline=K); d.line((2,14,19,14),fill=Cd)
        d.rectangle((1,7,20,8),fill=K)
        d.ellipse((8,5,13,10),fill=K); d.ellipse((9,6,12,9),fill=(255,255,255,255))
    return im
hop(False).save(O+'/b0.png'); hop(True).save(O+'/b1.png')
