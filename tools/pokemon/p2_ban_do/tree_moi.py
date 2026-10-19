"""P2 (nhanh ban do): dung lai 47 vat trang tri server/map/tree/<i> (lenh -48) tu Ninja Adventure (CC0)
TilesetNature / TilesetElement, giu dinh dang -48.  python3 tools/pokemon/p2_ban_do/tree_moi.py <server/map/tree> <thu muc ra>
"""
import os,struct,sys
from PIL import Image
N=os.environ.get('HTBB_NINJA_FULL') or os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),'pmd_raw_tile_moi','ninja_full')+os.sep
T=sys.argv[1]; OUT=sys.argv[2]
ts={n:Image.open(N+n+'.png').convert('RGBA') for n in ['TilesetNature','TilesetElement']}
R=[('TilesetNature',*r) for r in [(0,0,2,2),(9,8,1,3),(6,8,2,2),(2,0,2,2),(14,0,2,2),(4,0,2,2),(16,0,2,2),(18,0,2,2),(6,0,2,2),(12,0,2,2),(8,0,2,2),(10,0,2,2)]]
R+=[('TilesetElement',0,8,1,1),('TilesetElement',1,0,1,1),('TilesetNature',0,8,2,2),('TilesetNature',2,8,2,2),('TilesetElement',15,0,1,1),
    ('TilesetElement',4,0,1,1),('TilesetElement',8,1,1,1),('TilesetElement',0,5,1,2),('TilesetElement',0,3,2,2),('TilesetElement',5,3,2,2),
    ('TilesetNature',13,8,2,2),('TilesetNature',17,8,2,2),('TilesetNature',0,12,2,2),('TilesetNature',2,12,2,2),('TilesetNature',10,14,2,2),
    ('TilesetNature',8,16,2,2),('TilesetNature',10,16,2,2),('TilesetNature',21,10,2,3),('TilesetNature',21,14,2,3),('TilesetNature',13,14,1,2),
    ('TilesetNature',14,14,1,2),('TilesetNature',13,16,2,4),('TilesetNature',0,11,1,1),('TilesetNature',3,11,1,1),('TilesetNature',6,11,1,1),
    ('TilesetNature',1,11,1,1),('TilesetElement',2,3,2,2),('TilesetNature',8,14,2,2),('TilesetNature',16,10,5,3),('TilesetNature',16,14,5,3),
    ('TilesetNature',0,14,1,1),('TilesetNature',1,14,1,1),('TilesetNature',2,14,1,1),('TilesetNature',3,14,1,1),('TilesetNature',4,8,1,1)]
FIX={1:('TilesetElement',0,0,1,1),20:('TilesetNature',4,12,1,1),21:('TilesetNature',5,12,1,1),26:('TilesetNature',8,10,1,1),27:('TilesetNature',9,10,1,1),28:('TilesetNature',10,10,1,1),40:('TilesetNature',0,10,1,1),34:('TilesetNature',6,8,2,2),39:('TilesetNature',7,12,1,1)}
ids=sorted(int(f) for f in os.listdir(T))
assert len(R)>=len(ids),(len(R),len(ids))
os.makedirs(OUT,exist_ok=True)
for i,r in zip(ids,R):
    n,x,y,w,h=FIX.get(i,r)
    im=ts[n].crop((x*16,y*16,(x+w)*16,(y+h)*16)); bb=im.getbbox(); im=im.crop(bb)
    import io; b=io.BytesIO(); im.save(b,'PNG',optimize=True); png=b.getvalue()
    W,H=im.size
    data=struct.pack('>bbBBBB',-(W//2),-(H-4),0,0,0,0)
    open(os.path.join(OUT,str(i)),'wb').write(struct.pack('>H',len(png))+png+struct.pack('>H',len(data))+data)
    im.save(os.path.join(OUT,'_%d.png'%i))
print(len(ids))
