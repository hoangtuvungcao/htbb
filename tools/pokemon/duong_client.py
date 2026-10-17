"""Duong toi thu muc Assets cua client Unity (PC) cho cac tool Pokemon.

Client PC chinh thuc nay la htbb-client/pc (repo htbb-client, nam canh HTBB/). HTBB/ClientUnity la ban
cu, se xoa sau khi gop xong. Trong luc chuyen tiep, tool SINH tai nguyen/ma ghi vao MOI thu muc con ton
tai (pc truoc, ClientUnity sau) de hai ban khong lech nhau; tool chi DOC thi dung thu muc dau tien.

Bien moi truong:
  HTBB_CLIENT_ASSETS  duong Assets cu the (vd /duong/htbb-client/pc/Assets) - chi dung DUNG thu muc nay
  HTBB_CLIENT_CU=0    bo ghi vao HTBB/ClientUnity/Assets (chi ghi vao pc)
"""
import os

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PC = os.path.normpath(os.path.join(GOC, "..", "htbb-client", "pc", "Assets"))
CU = os.path.join(GOC, "ClientUnity", "Assets")


def cac_assets():
    """Cac thu muc Assets can ghi (it nhat mot phan tu)."""
    rieng = os.environ.get("HTBB_CLIENT_ASSETS")
    if rieng:
        return [rieng]
    ds = []
    if os.path.isdir(PC):
        ds.append(PC)
    if os.path.isdir(CU) and os.environ.get("HTBB_CLIENT_CU", "1") != "0":
        ds.append(CU)
    return ds or [CU]


def assets():
    """Thu muc Assets chinh (de doc)."""
    return cac_assets()[0]


def scripts(a):
    """Thu muc ma game C# trong mot Assets: pc dung Scripts/Assembly-CSharp, ban cu Scripts/Game."""
    p = os.path.join(a, "Scripts", "Assembly-CSharp")
    return p if os.path.isdir(p) else os.path.join(a, "Scripts", "Game")


def resources(a, *con):
    return os.path.join(a, "Resources", *con)


def chep_sang_ban_khac(ra_chinh, ten_tep, *con):
    """Chep cac tep vua ghi trong ra_chinh (= resources(cac_assets()[0], *con)) sang cac Assets con lai."""
    import shutil
    for a in cac_assets()[1:]:
        dich = resources(a, *con)
        if os.path.normpath(dich) == os.path.normpath(ra_chinh):
            continue
        os.makedirs(dich, exist_ok=True)
        for t in ten_tep:
            nguon = os.path.join(ra_chinh, t)
            if os.path.isfile(nguon):
                shutil.copyfile(nguon, os.path.join(dich, t))
