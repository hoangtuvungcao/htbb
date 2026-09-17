#!/usr/bin/env python3
"""
Va 1 byte trong jar CLIENT J2ME: setCharPet voi pet type 5 (pet-tool) phai gan myPet = doi tuong Pet
vua tao, khong phai AnimalMove doc tu goi tin.

VI SAO (16/09/2026). GameScr.setCharPet (Unity: GameScr.cs ~6353; jar 2.7.5: abj.a(af,hw); jar 2.0.9:
N.umaruchan(aG,fb)) voi type == 5 lam:
    o = new Pet(owner, imgID); setInfoCharPet(pet, owner); actors.addElement(o);
"pet" la AnimalMove doc tu goi 15, KHONG nam trong actors, nhung setInfoCharPet gan owner.myPet = pet.
Tilemap.reset() (moi lan nap map) lai vo dieu kien actors.addElement(mainChar.myPet) -> AnimalMove do
duoc ve: xin anh 5200+imgID (imgID la MA HIEU UNG 24-28/35-44/60/67-69, map/pet/<id>.png khong co ->
server tra anh trong suot) va ve BONG -> mot cai bong lo lung di theo nhan vat ben canh con pet that.
Ban Unity da sua 09/09/2026 (setInfoCharPet(o, ...)); jar J2ME khong co ma nguon nen va bytecode.

Bytecode nhanh type 5 giong nhau o ca hai jar (chi khac chi so constant pool):
    new #Pet; dup; aload_2; aload_1; getfield #imgID; invokespecial #<init>; astore_3;
    aload_1; aload_2; invokestatic #setInfoCharPet; aload_0; getfield #actors; aload_3;
    invokevirtual #addElement; return
Doi "aload_1" (0x2B) ngay sau "astore_3" (0x4E) thanh "aload_3" (0x2D). Kieu hop le vi Pet extends
AnimalMove; offset 29 khong phai dich nhay nen StackMap CLDC khong doi.

Dung:
    python tools/va_mypet_client.py <jar>            # va, luu backup <jar>.truoc_va_pet
    python tools/va_mypet_client.py --kiem <jar>     # chi kiem: da va chua / co tim thay mau khong
"""
import io
import re
import shutil
import sys
import zipfile

# BB ii ii 59 2C 2B B4 ii ii B7 ii ii 4E [2B|2D] 2C B8 ii ii 2A B4 ii ii 2D B6 ii ii B1
MAU = re.compile(
    rb"\xBB..\x59\x2C\x2B\xB4..\xB7..\x4E([\x2B\x2D])\x2C\xB8..\x2A\xB4..\x2D\xB6..\xB1",
    re.DOTALL,
)


def tim(jar_bytes):
    """Tra ve [(ten_class, vi_tri_byte_can_va, byte_hien_tai)] trong moi class khop mau."""
    ket = []
    with zipfile.ZipFile(io.BytesIO(jar_bytes)) as z:
        for ten in z.namelist():
            if not ten.endswith(".class"):
                continue
            d = z.read(ten)
            for m in MAU.finditer(d):
                ket.append((ten, m.start(1), m.group(1)))
    return ket


def main(argv):
    chi_kiem = "--kiem" in argv
    duong = [a for a in argv[1:] if not a.startswith("--")]
    if len(duong) != 1:
        print(__doc__)
        return 2
    jar = duong[0]
    goc = open(jar, "rb").read()
    khop = tim(goc)
    if not khop:
        print("KHONG tim thay mau setCharPet type 5 trong", jar, "- khong doi gi.")
        return 1
    if len(khop) != 1:
        print("Tim thay", len(khop), "cho khop, khong dam va:", khop)
        return 1
    ten, vi_tri, hien_tai = khop[0]
    trang_thai = "DA VA (aload_3)" if hien_tai == b"\x2D" else "CHUA VA (aload_1)"
    print("%s: %s @byte %d -> %s" % (jar, ten, vi_tri, trang_thai))
    if chi_kiem or hien_tai == b"\x2D":
        return 0

    backup = jar + ".truoc_va_pet"
    shutil.copyfile(jar, backup)
    ra = io.BytesIO()
    with zipfile.ZipFile(io.BytesIO(goc)) as zin, zipfile.ZipFile(ra, "w") as zout:
        for info in zin.infolist():
            d = zin.read(info.filename)
            if info.filename == ten:
                d = d[:vi_tri] + b"\x2D" + d[vi_tri + 1:]
            zout.writestr(info, d, compress_type=info.compress_type)
    open(jar, "wb").write(ra.getvalue())
    sau = tim(open(jar, "rb").read())
    if len(sau) == 1 and sau[0][2] == b"\x2D":
        print("Da va", ten, "trong", jar, "- backup:", backup)
        return 0
    print("VA THAT BAI, khoi phuc tu backup")
    shutil.copyfile(backup, jar)
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
