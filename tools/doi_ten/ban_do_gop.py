# -*- coding: utf-8 -*-
"""Ban do duong dan khi gop 3 repo (HTBB, htbb-client, HTBB-Web) vao monorepo htbb/.
Dung chung cho: viet lai lich su (filter-branch), chep cay lam viec, sua lien ket tai lieu."""

# (tien to cu, tien to moi). Luat dau tien khop thang. Tien to cu '' = moi thu con lai.
# Tien to ket thuc bang '/' = thu muc; khong = dung ten tep.
LUAT = {
    'HTBB': [
        ('.github/workflows/', '.github/workflows/server-'),
        ('.claude/', '.claude/'),
        ('ClientUnity/', 'client_pc_ios/ClientUnity/'),
        ('docs/', 'docs/'),
        ('sql/', 'sql/'),
        ('tools/', 'tools/'),
        ('vps/', 'vps/'),
        # (web/ PHP cu da bo khoi HTBB - website duy nhat la HTBB-Web -> web/. Lich su cu cua
        #  HTBB/web/ roi vao server/web/ theo luat '' ben duoi; cay cuoi khong con thu muc do.)
        ('', 'server/'),
    ],
    'htbb-client': [
        ('.github/workflows/', '.github/workflows/client-'),
        ('README.md', 'docs/client/README.md'),
        ('.gitignore', 'docs/client/gitignore.cu'),
        ('docs/', 'docs/client/'),
        ('j2me/', 'client_jar/'),
        ('shared/', 'client_jar/shared/'),
        ('android/', 'client_apk/'),
        ('pc/', 'client_pc_ios/'),
        ('pc-java/', 'client_pc/'),
        ('phat-hanh/', 'tools/phat-hanh/'),
        ('', 'docs/client/goc/'),
    ],
    'HTBB-Web': [
        ('.github/workflows/', '.github/workflows/web-'),
        ('server/sql/', 'sql/web/'),
        ('', 'web/'),
    ],
}
# Ten thu muc (bao) moi repo nam o dau trong monorepo - de doi "../HTBB/x", "HTBB/x"...
TEN_REPO = {'HTBB': 'HTBB', 'htbb-client': 'htbb-client', 'HTBB-Web': 'HTBB-Web'}


def doi(repo, p):
    """Duong dan trong repo cu -> duong dan trong monorepo."""
    for cu, moi in LUAT[repo]:
        if cu == '':
            return moi + p
        if cu.endswith('/'):
            if p.startswith(cu):
                return moi + p[len(cu):]
        elif p == cu:
            return moi
    raise ValueError(p)
