"""Duong toi thu muc Assets cua client Unity (client_pc_ios/Assets) cho cac tool Pokemon.

Bien moi truong:
  HTBB_CLIENT_ASSETS  duong Assets khac (vd ban sao de thu) - mac dinh client_pc_ios/Assets cua repo.
"""
import os

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PC = os.path.join(GOC, "client_pc_ios", "Assets")


def cac_assets():
    """Cac thu muc Assets can ghi (mot phan tu)."""
    return [os.environ.get("HTBB_CLIENT_ASSETS") or PC]


def assets():
    """Thu muc Assets chinh (de doc)."""
    return cac_assets()[0]


def scripts(a):
    """Thu muc ma game C# trong mot Assets."""
    return os.path.join(a, "Scripts", "Assembly-CSharp")


def resources(a, *con):
    return os.path.join(a, "Resources", *con)


def chep_sang_ban_khac(ra_chinh, ten_tep, *con):
    """Giu cho tuong thich: chi con mot ban client Unity nen khong chep di dau."""
    return None
