#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Buoc 6 cua gop_monorepo.py: sua duong dan sau khi 3 repo da nam chung mot cay.

    python3 -I tools/doi_ten/sua_sau_gop.py <monorepo> [--thu]

Quy uoc thu muc chay (cwd) trong monorepo:
  * server (game, test Kiem, build.bat...): cwd = server/   -> "docs/x" trong test thanh "../docs/x"
  * tools/ (python, sh, java sinh du lieu):  cwd = goc monorepo -> "src/x" thanh "server/src/x"
  * client_jar/, client_apk/, client_pc_ios/, web/: cwd = chinh thu muc do (nhu repo cu)
Chay lai bao nhieu lan cung duoc: moi luat chi khop dang CU.
"""
import os
import re
import sys
from pathlib import Path

DAY = Path(__file__).resolve().parent
sys.path.insert(0, str(DAY))
sys.dont_write_bytecode = True
import ban_do_gop  # noqa: E402

G = Path(sys.argv[1]).resolve()
THU = '--thu' in sys.argv
DOI = []          # (tep, so cho)

# Ten cap 1 cua repo HTBB cu (de nhan ra duong dan tuong doi goc HTBB)
TOP_HTBB = {'src', 'test', 'lib', 'map', 'cMap', 'cMap_pokemon', 'loginServer', 'docs', 'sql', 'tools', 'vps',
            'ClientUnity', 'build', 'chay_novadb', 'server.ini', 'README.md', '.gitignore', '.github', '.gitattributes',
            'run.bat', 'build.bat', 'test.bat', 'capnhat.bat', 'xay-client.bat', 'htbb-server.jar', 'log', 'backup'}
TOP_CLIENT = {'j2me', 'android', 'pc', 'shared', 'phat-hanh', 'docs', 'README.md'}
TOP_WEB = {'app', 'server', 'shared', 'scripts', 'deploy', 'public', 'nuxt.config.ts', 'package.json', 'README.md',
           '.env.example', 'tests'}


def doc(p):
    with open(G / p, encoding='utf-8', newline='') as f:     # giu nguyen CRLF (.bat)
        return f.read()


def ghi_tep(p, s):
    with open(p, 'w', encoding='utf-8', newline='') as f:
        f.write(s)


def ghi(p, s, cu=None):
    if cu is None:
        cu = doc(p)
    if s != cu:
        DOI.append((p, sum(1 for a, b in zip(cu.splitlines(), s.splitlines()) if a != b) or 1))
        if not THU:
            ghi_tep(G / p, s)


def moi_goc(repo, p):
    """Duong dan (tuong doi goc repo cu) -> duong dan tuong doi goc monorepo."""
    if repo == 'HTBB' and p.split('/')[0] in ('.gitignore', '.gitattributes'):
        return p        # gop ve goc monorepo
    if repo == 'HTBB' and (p == 'log' or p.startswith('log/') or p == 'backup' or p.startswith('backup/')):
        return p        # log/backup tren VPS o goc /opt/htbb
    if p and '/' not in p:                      # ten thu muc tran ("docs") - doi nhu thu muc
        thu = ban_do_gop.doi(repo, p + '/x')
        if thu != ban_do_gop.doi(repo, '') + p + '/x':
            return thu[:-2]
    return ban_do_gop.doi(repo, p)


def tuong_doi(dich, tu):
    r = os.path.relpath(dich, tu) if tu not in ('', '.') else dich
    return r


# ------------------------------------------------------------------ 1. .gitignore / .gitattributes goc
def doi_mau_ignore(repo, dong, dich_goc):
    d = dong.rstrip('\n')
    if not d.strip() or d.lstrip().startswith('#'):
        return [d]
    phu = d.startswith('!')
    m = d[1:] if phu else d
    tien = '!' if phu else ''
    than = m.rstrip('/')
    neo = m.startswith('/') or ('/' in than and not m.startswith('**/'))
    if neo:
        p = m.lstrip('/')
        dau = p.split('/')[0]
        if any(c in dau for c in '*?['):
            moi = ban_do_gop.doi(repo, p)          # mau o goc repo -> thu muc '' cua repo
        else:
            moi = moi_goc(repo, p)
        return [tien + '/' + moi]
    p = m[3:] if m.startswith('**/') else m
    return [tien + '/' + g + '**/' + p for g in dich_goc]


def gitignore():
    ra = ['# .gitignore GOC monorepo htbb - sinh boi tools/doi_ten/sua_sau_gop.py tu .gitignore cua 3 repo cu.',
          '# web/.gitignore (Nuxt) va .gitignore trong tung thanh phan van co hieu luc.', '',
          '# ---- VPS: /opt/htbb la ban checkout monorepo, log + sao luu nam o goc ----',
          '/log/', '/backup/', '', '# ---- may dev ----', '/doi_ten_sao_luu/', '']
    nguon = [('HTBB', 'server/.gitignore', ['server/', 'docs/', 'sql/', 'tools/', 'vps/', 'client_pc_ios/ClientUnity/']),
             ('htbb-client', 'docs/client/gitignore.cu', ['client_jar/', 'client_apk/', 'client_pc/', 'client_pc_ios/', 'tools/phat-hanh/', 'docs/client/'])]
    co = False
    for repo, tep, dich in nguon:
        if not (G / tep).exists():
            continue
        co = True
        ra.append('# ======== tu %s/.gitignore ========' % repo)
        for d in doc(tep).splitlines():
            ra += doi_mau_ignore(repo, d, dich)
        ra.append('')
    if not co:
        return
    ghi('.gitignore', '\n'.join(ra) + '\n', cu=doc('.gitignore') if (G / '.gitignore').exists() else '')
    for repo, tep, _ in nguon:
        if (G / tep).exists() and not THU:
            (G / tep).unlink()


def gitattributes():
    tep = 'server/.gitattributes'
    if not (G / tep).exists():
        return
    ra = ['# .gitattributes GOC monorepo (tu HTBB/.gitattributes; web/ va client_jar/ con tep rieng).']
    for d in doc(tep).splitlines():
        s = d.strip()
        if not s or s.startswith('#'):
            ra.append(d)
            continue
        mau, _, thuoc_tinh = s.partition(' ')
        if '/' in mau.rstrip('/'):
            mau = moi_goc('HTBB', mau.lstrip('/'))
        ra.append('%s %s' % (mau, thuoc_tinh.strip()))
    ghi('.gitattributes', '\n'.join(ra) + '\n', cu='')
    if not THU:
        (G / tep).unlink()


# ------------------------------------------------------------------ 2. don rac nho
def don():
    for p in ['server/KPAH2_patched.jar', 'server/lib/NQSH_5h.jar']:     # symlink tuong thich: monorepo la checkout moi
        if (G / p).is_symlink():
            DOI.append((p, 'xoa symlink'))
            if not THU:
                (G / p).unlink()
    a, b = G / 'sql/web_cms.sql', G / 'sql/web/web_cms.sql'
    if a.exists() and b.exists() and a.read_bytes() == b.read_bytes():
        DOI.append(('sql/web/web_cms.sql', 'xoa ban trung (giu sql/web_cms.sql)'))
        if not THU:
            b.unlink()
            try:
                b.parent.rmdir()
            except OSError:
                pass


# ------------------------------------------------------------------ 3. chuoi duong dan trong Java (server/test, cwd = server/)
RE_CHUOI = re.compile(r'(?<![\\\w])"((?:\.\./)?)((?:HTBB-Web|htbb-client|HTBB)/)?([A-Za-z_.][\w.-]*)((?:/[^"\s]*)?)"')


BO_SAU = re.compile(r'(?:contains|indexOf|startsWith|endsWith|Kiem\.dung|Kiem\.bang|matches|replace\w*)\(\s*$|\+\s*$')


def doi_chuoi_duong(m, cwd, ten_tep=''):
    truoc = m.string[max(0, m.start() - 40):m.start()]
    if BO_SAU.search(truoc) or '\\' in m.group(0):                    # chuoi de so/khop noi dung, hoac hau to noi chuoi: khong phai duong dan
        return m.group(0)
    len_cha, repo_ten, dau, duoi = m.group(1), m.group(2), m.group(3), m.group(4)
    if len_cha and repo_ten:                        # "../HTBB-Web/server/utils/x.ts"
        repo = repo_ten.rstrip('/')
        top = {'HTBB': TOP_HTBB, 'htbb-client': TOP_CLIENT, 'HTBB-Web': TOP_WEB}[repo]
        if dau not in top:
            return m.group(0)
        p = dau + duoi
    elif not len_cha and not repo_ten and dau in TOP_HTBB:
        repo, p = 'HTBB', dau + duoi
    else:
        return m.group(0)
    moi = tuong_doi(moi_goc(repo, p.rstrip('/')), cwd).replace('\\', '/')
    if p.endswith('/'):
        moi += '/'
    return '"%s"' % moi


def java_server():
    for f in sorted((G / 'server/test').rglob('*.java')):
        rel = str(f.relative_to(G))
        s = doc(rel)
        s2 = RE_CHUOI.sub(lambda m: doi_chuoi_duong(m, 'server', f.name), s)
        # cong cu tools/ nhan thu muc GOC monorepo (test chay o server/)
        s2 = re.sub(r'(\.invoke\(null, )"\."(?=[,)])', r'\1".."', s2)
        for tep, cu, moi in VA_TAY:             # va tay cua chinh tep nay ap ngay (chay lai = khong doi)
            if tep == rel:
                s2 = s2.replace(cu, moi)
        ghi(rel, s2, cu=s)


# ------------------------------------------------------------------ 4. lien ket trong tai lieu .md
def nguoc(n):
    """Duong dan moi -> (repo, duong dan cu). Chon luat co tien to moi DAI nhat."""
    tot = None
    for repo, luat in ban_do_gop.LUAT.items():
        for cu, moi in luat:
            if moi.endswith('/') or moi == '':
                if n.startswith(moi):
                    o = (cu if cu.endswith('/') or cu == '' else '') + n[len(moi):]
                    if ban_do_gop.doi(repo, o) == n and (tot is None or len(moi) > tot[0]):
                        tot = (len(moi), repo, o)
            elif n == moi and (tot is None or len(moi) > tot[0]):
                tot = (len(moi), repo, cu)
            elif moi.endswith('-') and n.startswith(moi):          # .github/workflows/server-x.yml
                o = cu + n[len(moi):]
                if ban_do_gop.doi(repo, o) == n and (tot is None or len(moi) > tot[0]):
                    tot = (len(moi), repo, o)
    return (tot[1], tot[2]) if tot else (None, None)


RE_LK = re.compile(r'(\[[^\]]*\]\()([^)\s]+)(\))')


def dich_moi(repo, o, lk):
    """Lien ket lk trong tep cu (repo, o) -> duong dan dich trong monorepo (hoac None)."""
    d = os.path.normpath(os.path.join(os.path.dirname(o), lk)).replace('\\', '/')
    if d.startswith('../'):
        phan = d.split('/')
        if phan[0] == '..' and len(phan) > 1 and phan[1] in ban_do_gop.LUAT:
            return moi_goc(phan[1], '/'.join(phan[2:]))
        return None
    return moi_goc(repo, d)


def lien_ket_md():
    bo = ('node_modules/', '.nuxt/', '.output/', 'tools/pokemon/pmd_raw')
    so_hong = []
    for f in sorted(G.rglob('*.md')):
        rel = str(f.relative_to(G))
        if any(b in rel for b in bo) or rel.startswith('.git/'):
            continue
        repo, o = nguoc(rel)
        if repo is None:
            continue
        with open(f, encoding='utf-8', errors='surrogateescape', newline='') as fh:
            s = fh.read()

        def thay(m):
            lk = m.group(2)
            if re.match(r'^(https?:|mailto:|#|<|data:)', lk, re.I):
                return m.group(0)
            duong, _, neo = lk.partition('#')
            if not duong:
                return m.group(0)
            duong_g = duong.replace('%20', ' ')
            if (f.parent / duong_g).exists():
                return m.group(0)                     # van dung (vd. cung thu muc, cung di chuyen)
            dm = dich_moi(repo, o, duong_g)
            if dm is None or not (G / dm).exists():
                so_hong.append('%s -> %s' % (rel, lk))
                return m.group(0)
            moi = os.path.relpath(dm, os.path.dirname(rel) or '.').replace(' ', '%20')
            if duong.endswith('/') and not moi.endswith('/'):
                moi += '/'
            return m.group(1) + moi + ('#' + neo if neo else '') + m.group(3)
        s2 = RE_LK.sub(thay, s)
        if s2 != s:
            DOI.append((rel, 'lien ket'))
            if not THU:
                with open(f, 'w', encoding='utf-8', errors='surrogateescape', newline='') as fh:
                    fh.write(s2)
    for h in so_hong:
        print('   [lien ket hong tu truoc / chua giai] %s' % h)


# ------------------------------------------------------------------ 5. client (jar / apk / pc / phat-hanh)
DUOI_VB = ('.sh', '.py', '.gradle', '.java', '.cs', '.yml', '.yaml', '.md', '.conf', '.example', '.txt', '.properties', '.pro', '.mjs', '.ts', '.vue', '.json', '.bat')
LUAT_CLIENT_RIENG = {
    # cwd = client_jar/
    'client_jar/': [(r'\.\./shared/', 'shared/'), (r'\.\./pc/Assets', '../client_pc_ios/Assets'),
                    (r'(?<!tools/)\.\./phat-hanh/', '../tools/phat-hanh/'), (r'\.\./\.\./\.\./HTBB/docs/pokemon', '../../../docs/pokemon')],
    # rootProject = client_apk/
    'client_apk/': [(r"\.\./j2me\b", '../client_jar'), (r'\.\./shared/', '../client_jar/shared/'),
                    (r'(?<!tools/)\.\./phat-hanh/', '../tools/phat-hanh/')],
    # cwd = client_pc/ (client Java desktop, port tu code jar nhu APK)
    'client_pc/': [(r"\.\./j2me\b", '../client_jar'), (r'\.\./shared/', '../client_jar/shared/'),
                   (r'(?<!tools/)\.\./phat-hanh/', '../tools/phat-hanh/')],
    # Application.dataPath = client_pc_ios/Assets
    'client_pc_ios/Assets/': [(r'"\.\./\.\./shared/', '"../../client_jar/shared/'), (r'"\.\./\.\./phat-hanh"', '"../../tools/phat-hanh"')],
    # cwd = goc monorepo (truoc la goc htbb-client)
    'tools/phat-hanh/': [(r'cd "\$\(dirname "\$0"\)/\.\."', 'cd "$(dirname "$0")/../.."')],
}
# Duong dan tu GOC htbb-client cu -> goc monorepo (tai lieu, workflow, script phat-hanh, thong bao)
LUAT_CLIENT_GOC = [
    (r'(?<![\w./$-])j2me/', 'client_jar/'),
    (r'(?<![\w./$-])android/', 'client_apk/'),
    (r'(?<![\w./$-])phat-hanh/', 'tools/phat-hanh/'),
    (r'(?<![\w./$-])shared/(mod|banner|brand)', r'client_jar/shared/\1'),
    (r'(?<![\w./$-])pc-java/', 'client_pc/'),
    (r'(?<![\w./$-])pc/Assets', 'client_pc_ios/Assets'),
    (r'`pc/`', '`client_pc_ios/`'),
    (r'(?<![\w./-])htbb-client/pc/', 'client_pc_ios/'),
    (r'(?<![\w./-])htbb-client/j2me/', 'client_jar/'),
    (r'(?<![\w./-])htbb-client/shared/', 'client_jar/shared/'),
    (r'(?<![\w./-])htbb-client/android/', 'client_apk/'),
    (r'(?<![\w./-])HTBB/ClientUnity/', 'client_pc_ios/ClientUnity/'),
]


def client():
    goc = ['client_jar', 'client_apk', 'client_pc', 'client_pc_ios', 'tools/phat-hanh', 'docs/client']
    tep = []
    for g in goc:
        for f in (G / g).rglob('*'):
            r = str(f.relative_to(G))
            if f.is_file() and not f.is_symlink() and f.suffix.lower() in DUOI_VB and '/ClientUnity/' not in '/' + r \
                    and '/build/' not in r and '/out/' not in r and '/Library/' not in r:
                tep.append(r)
    tep += [str(f.relative_to(G)) for f in (G / '.github/workflows').glob('client-*')]
    for r in sorted(tep):
        try:
            s = doc(r)
        except UnicodeDecodeError:
            continue
        s2 = s
        for a, b in LUAT_CLIENT_GOC:             # truoc: luat rieng sinh ra "shared/" tuong doi cuc bo
            if re.match(r'client_jar/[^/]+\.sh$', r) and 'shared/' in a:
                continue                        # build.sh cd vao client_jar/: "shared/" la thu muc con cua chinh no
            s2 = re.sub(a, b, s2)
        for tien, luat in LUAT_CLIENT_RIENG.items():
            if r.startswith(tien):
                for a, b in luat:
                    s2 = re.sub(a, b, s2)
        ghi(r, s2, cu=s)


# ------------------------------------------------------------------ 6. tools/ (cwd = goc monorepo)
TOP_SERVER = ('src', 'test', 'lib', 'map', 'cMap', 'cMap_pokemon', 'loginServer', 'build', 'chay_novadb', 'server.ini',
              'htbb-server.jar')
RE_SV = '|'.join(re.escape(t) for t in sorted(TOP_SERVER, key=len, reverse=True))


def doi_doan(d):
    """Mot doan duong dan tu goc HTBB cu -> goc monorepo (giu nguyen neu khong phai cua HTBB)."""
    dau = re.split(r'[/\\]', d, 1)[0]
    if dau in TOP_SERVER:
        return 'server' + ('\\' if '\\' in d and '/' not in d else '/') + d
    if dau == 'ClientUnity':
        return 'client_pc_ios/' + d
    return d


def chuoi_tools(m):
    q, d = m.group(1), m.group(2)
    if d.startswith('../htbb-client/') or d.startswith('../HTBB-Web/'):
        repo, p = d[3:].split('/', 1)
        return q + moi_goc(repo, p) + q
    if '/' not in d and '\\' not in d and ';' not in d and ':' not in d:
        return m.group(0)                       # chu tran ("map", "src") de cho luat join(GOC, ...)
    if ';' in d or (':' in d and '://' not in d):
        sep = ';' if ';' in d else ':'
        return q + sep.join(doi_doan(x) for x in d.split(sep)) + q
    return q + doi_doan(d) + q


def tools():
    for f in sorted((G / 'tools').rglob('*')):
        r = str(f.relative_to(G))
        if not f.is_file() or f.is_symlink() or f.suffix not in ('.py', '.java', '.mjs', '.sh') or \
                any(x in r for x in ('tools/phat-hanh/', 'tools/doi_ten/', 'pmd_raw', '__pycache__', 'chay_tren_novadb.sh')):
            continue
        s = doc(r)
        s2 = re.sub(r'''(?<![\w\\])(["'])(\.\./HTBB-Web/[^"'\s]+|(?:\.\./(?:htbb-client|HTBB-Web)/)?(?:%s|ClientUnity)(?:[/\\;:][^"'\s]*)?)\1''' % RE_SV,
                    chuoi_tools, s)
        # os.path.join(GOC, "src", ...) -> os.path.join(GOC, "server", "src", ...)
        s2 = re.sub(r'(join\(\s*GOC\s*,\s*)"(%s)"' % RE_SV, r'\1"server", "\2"', s2)
        s2 = re.sub(r'(join\(\s*GOC\s*,\s*)"ClientUnity"', r'\1"client_pc_ios", "ClientUnity"', s2)
        if f.suffix == '.java':                 # hang so thu muc tran cua cong cu Java
            s2 = re.sub(r'(?<![\w\\])"(cMap|cMap_pokemon)"', r'"server/\1"', s2)
        ghi(r, s2, cu=s)


# ------------------------------------------------------------------ 7. web/ (Nuxt) - repo game nay la thu muc cha ('..')
def web():
    for f in sorted((G / 'web').rglob('*')):
        r = str(f.relative_to(G))
        if not f.is_file() or f.is_symlink() or any(x in r for x in ('/node_modules/', '/.nuxt/', '/.output/')) \
                or f.suffix not in ('.ts', '.mjs', '.js', '.vue', '.md', '.example', '.json'):
            continue
        if f.name in ('package-lock.json',):
            continue
        s = doc(r)
        s2 = s.replace("process.env.HTBB_DIR ?? '../HTBB')", "process.env.HTBB_DIR ?? '..')")
        s2 = s2.replace("join(goc, '..', 'HTBB')", "join(goc, '..')")
        s2 = s2.replace('HTBB_DIR=../HTBB ', 'HTBB_DIR=.. ')
        s2 = s2.replace("'../../HTBB/src/", "'../../server/src/")
        s2 = re.sub(r"(join\(\s*(?:HTBB|htbb)\s*,\s*')([^']+)'", lambda m: m.group(1) + doi_doan(m.group(2)) + "'", s2)
        s2 = s2.replace('(bản sao `HTBB/sql/web_cms.sql`)', '').replace('(bản sao: <code>HTBB/sql/web_cms.sql</code>)', '')
        s2 = s2.replace('server/sql/web_cms.sql', 'sql/web_cms.sql')
        ghi(r, s2, cu=s)


# ------------------------------------------------------------------ 8. workflow (.github/workflows o goc, loc theo duong dan)
PATHS_SERVER = ['server/**', 'sql/**', 'docs/**', 'tools/**', 'vps/**', 'client_pc_ios/ClientUnity/**',
                'web/shared/data/**', 'web/server/utils/**', '.github/workflows/server-test.yml']


def yaml_paths(ds, thut='    '):
    return ''.join('%s  - %s\n' % (thut, "'%s'" % d) for d in ds)


VA_WORKFLOW = [
    ('.github/workflows/server-test.yml',
     "on:\n  push:\n    branches: ['**']\n  pull_request:\n",
     "on:\n  push:\n    branches: ['**']\n    paths:\n" + yaml_paths(PATHS_SERVER) + "  pull_request:\n    paths:\n" + yaml_paths(PATHS_SERVER)),
    ('.github/workflows/server-test.yml',
     "  kiem-thu:\n    runs-on: ubuntu-latest\n    timeout-minutes: 10\n",
     "  kiem-thu:\n    runs-on: ubuntu-latest\n    timeout-minutes: 10\n    defaults:\n      run:\n        working-directory: server   # monorepo: test chay o server/\n"),
    ('.github/workflows/server-deploy.yml',
     "on:\n  push:\n    branches: [main]\n",
     "on:\n  push:\n    branches: [main]\n    paths:\n" + yaml_paths(['server/**', 'vps/**', 'sql/**', '.github/workflows/server-deploy.yml'])),
    ('.github/workflows/server-deploy.yml',
     "  deploy:\n    runs-on: ubuntu-latest\n",
     "  deploy:\n    # Chi bat sau khi VPS da la ban checkout monorepo (vps/CHUYEN_TEN_HTBB.md): Settings > Variables HTBB_VPS_MONOREPO=1\n"
     "    if: ${{ vars.HTBB_VPS_MONOREPO == '1' }}\n    runs-on: ubuntu-latest\n"),
    ('.github/workflows/web-deploy.yml',
     "on:\n  push:\n    branches: [main]\n  pull_request:\n",
     "on:\n  push:\n    branches: [main]\n    paths: ['web/**', '.github/workflows/web-deploy.yml']\n  pull_request:\n    paths: ['web/**', '.github/workflows/web-deploy.yml']\n"),
    ('.github/workflows/web-deploy.yml',
     "  build:\n    runs-on: ubuntu-24.04\n    timeout-minutes: 20\n",
     "  build:\n    runs-on: ubuntu-24.04\n    timeout-minutes: 20\n    defaults:\n      run:\n        working-directory: web   # monorepo\n"),
    ('.github/workflows/web-deploy.yml', "          node-version: 22\n          cache: npm\n",
     "          node-version: 22\n          cache: npm\n          cache-dependency-path: web/package-lock.json\n"),
    ('.github/workflows/web-deploy.yml', "          name: htbb-web-build\n          path: htbb-web-build.tgz\n",
     "          name: htbb-web-build\n          path: web/htbb-web-build.tgz\n"),
]


def workflows():
    for f in sorted((G / '.github/workflows').glob('client-*.yml')):
        r = str(f.relative_to(G))
        s = doc(r)
        s2 = s.replace('PROJECT_PATH: pc\n', 'PROJECT_PATH: client_pc_ios\n').replace('projectPath: pc\n', 'projectPath: client_pc_ios\n')
        s2 = s2.replace("'pc/Packages/**'", "'client_pc_ios/Packages/**'").replace("'pc/ProjectSettings/**'", "'client_pc_ios/ProjectSettings/**'")
        s2 = s2.replace('path: pc/Library', 'path: client_pc_ios/Library').replace('(pc/)', '(client_pc_ios/)')
        ghi(r, s2, cu=s)
    for tep, cu, moi in VA_WORKFLOW:
        if not (G / tep).exists():
            continue
        s = doc(tep)
        if cu in s and moi not in s:
            ghi(tep, s.replace(cu, moi, 1), cu=s)
        elif moi not in s:
            print('   [CANH BAO] workflow %s: khong thay %r' % (tep, cu[:50]))


# ------------------------------------------------------------------ 9. VPS: /opt/htbb = ban checkout MONOREPO, game o /opt/htbb/server
CHUA_O_GOC = r'(?!log\b|backup\b|vps\b|web\b|sql\b|docs\b|tools\b|server\b|client_|\.)'


def vps_doi(s):
    # bo loi roi ve /opt/kpah (thu muc kieu cu, khac bo cuc) - monorepo la checkout moi, khong co ban tuong thich
    s = re.sub(r'^(\s*)(\w+)=(\$\{\w+:-)?(/opt/htbb(?:-web)?[^;\s]*?)(\}?); \[ -d /opt/htbb(?:-web)? \] \|\| \2=\S*  # TUONG-THICH-TEN-CU: bo sau 1 ban$',
               r'\1\2=\3\4\5', s, flags=re.M)
    s = re.sub(r'/opt/htbb/' + CHUA_O_GOC + r'([A-Za-z_*][\w.*-]*)', r'/opt/htbb/server/\1', s)
    s = re.sub(r'WorkingDirectory=/opt/htbb$', 'WorkingDirectory=/opt/htbb/server', s, flags=re.M)
    s = re.sub(r'\$(DIR|THU_MUC)/(server\.ini|htbb-server[\w.]*\.jar|HTBB_NEW\.jar|loginServer|trang_thai\.json|lib/|map/|cMap)',
               r'$\1/server/\2', s)
    s = s.replace('git -C "$DIR" checkout "$CU" -- htbb-server.jar', 'git -C "$DIR" checkout "$CU" -- server/htbb-server.jar')
    s = s.replace("CAN_RESTART_REGEX='^(htbb-server\\.jar|lib/|map/|cMap/|loginServer/|",
                  "CAN_RESTART_REGEX='^(server/htbb-server\\.jar|server/lib/|server/map/|server/cMap/|server/cMap_pokemon/|server/loginServer/|")
    s = s.replace('HTBB_SERVER_DIR="/opt/htbb"', 'HTBB_SERVER_DIR="/opt/htbb/server"')
    return s


def vps():
    tep = []
    for g in ('vps', 'docs', 'server/test', 'server/src', 'web/deploy', 'tools/pokemon', '.github/workflows'):
        for f in (G / g).rglob('*'):
            if f.is_file() and not f.is_symlink() and f.suffix in ('.sh', '.service', '.conf', '.cnf', '.tmpl', '.example', '.md',
                                                                   '.java', '.bat', '.yml', '.txt') and 'map_cu' not in str(f):
                tep.append(str(f.relative_to(G)))
    tep += ['server/README.md', 'web/README.md', 'web/.env.example']
    for r in sorted(set(tep)):
        if not (G / r).exists() or r == 'vps/CHUYEN_TEN_HTBB.md':     # so tay viet san cho bo cuc monorepo
            continue
        try:
            s = doc(r)
        except UnicodeDecodeError:
            continue
        ghi(r, vps_doi(s), cu=s)


# ------------------------------------------------------------------ 10. tools/pokemon/chay_tren_novadb.sh: chay o server/
def chay_novadb():
    r = 'tools/pokemon/chay_tren_novadb.sh'
    if not (G / r).exists():
        return
    s = doc(r)
    s2 = s
    if 'cd "$(dirname "$0")/../../server"' not in s2:
        s2 = s2.replace('set -e\n', 'set -e\ncd "$(dirname "$0")/../../server"   # monorepo: game doc server.ini, map/, lib/ tu server/\n', 1)
    s2 = s2.replace('"server/build/classes:', '"build/classes:').replace('server/lib/', 'lib/')
    s2 = s2.replace('$(cd .. && pwd -P)/novadb-data', '$(cd ../.. && pwd -P)/novadb-data')
    s2 = re.sub(r'(?<![./\w])sql/htbb_day_du\.sql', '../sql/htbb_day_du.sql', s2)
    s2 = re.sub(r'(?<![./\w])vps/server\.ini\.tmpl > server\.ini', '../vps/server.ini.tmpl > server.ini', s2)
    s2 = s2.replace('# Game server doc server.ini o HTBB/:', '# Game server doc server.ini o server/:')
    ghi(r, s2, cu=s)


# Sua tay sau luat chung: (tep, chuoi cu, chuoi moi). Chi ap khi con chuoi cu.
VA_TAY = [
    # ---- tools/ chay tu goc monorepo ----
    ('tools/giai-ma-core.bat', 'cd /d "%~dp0\\.."', 'cd /d "%~dp0\\..\\server"'),
    ('tools/pokemon/cai_map_j2me.sh', 'J=${1:-$HTBB/../htbb-client/j2me}', 'J=${1:-$HTBB/client_jar}'),
    ('tools/pokemon/cai_map_j2me.sh', 'cp "$HTBB/map/pokemon_tile/', 'cp "$HTBB/server/map/pokemon_tile/'),
    # ---- server/xay-client.bat: du an Unity cu nam o client_pc_ios/ClientUnity, cong cu o ../tools ----
    ('server/xay-client.bat', 'set "DUAN=%GOC%\\ClientUnity"', 'set "DUAN=%GOC%\\..\\client_pc_ios\\ClientUnity"'),
    ('server/xay-client.bat', '"%GOC%\\tools\\ChuyenTaiNguyen.java"', '"%GOC%\\..\\tools\\ChuyenTaiNguyen.java"'),
    ('server/xay-client.bat', '"%GOC%\\tools\\RutTaiNguyenUnity.java"', '"%GOC%\\..\\tools\\RutTaiNguyenUnity.java"'),
    ('server/xay-client.bat', 'Ket qua trong ClientUnity\\Build\\', 'Ket qua trong ..\\client_pc_ios\\ClientUnity\\Build\\'),
    # nhan cua dau van tay = duong dan tu GOC monorepo (tools/pokemon/kiem_item_mo_coi.py chay o goc); doc tep thi them ../
    ('server/test/KiemItemMoCoi.java', '{"../sql/htbb_day_du.sql", "../sql/pokemon_noi_dung.sql", "../sql/pokemon_vat_pham_lau_dai.sql",\n'
     '        "../sql/pokemon_lau_dai.sql", "../sql/pokemon_doi_chu.sql", "../sql/pokemon_nhiem_vu.sql", "../docs/pokemon/noi_dung_roi.csv",\n'
     '        "../docs/pokemon/nhiem_vu_moi.csv"}',
     '{"sql/htbb_day_du.sql", "sql/pokemon_noi_dung.sql", "sql/pokemon_vat_pham_lau_dai.sql",\n'
     '        "sql/pokemon_lau_dai.sql", "sql/pokemon_doi_chu.sql", "sql/pokemon_nhiem_vu.sql", "docs/pokemon/noi_dung_roi.csv",\n'
     '        "docs/pokemon/nhiem_vu_moi.csv"}'),
    ('client_jar/tools/KiemPokeDuLieu.java', 'new File(docs, "../../cMap_pokemon")', 'new File(docs, "../../server/cMap_pokemon")'),
    ('server/test/KiemItemMoCoi.java', 'final byte[] b = Files.readAllBytes(Paths.get(f));',
     'final byte[] b = Files.readAllBytes(Paths.get("..", f));'),
]


def va_tay():
    for tep, cu, moi in VA_TAY:
        if not (G / tep).exists():
            print('   [KHONG THAY] %s' % tep)
            continue
        s = doc(tep)
        if cu in s:
            ghi(tep, s.replace(cu, moi), cu=s)
        elif moi not in s:
            print('   [CANH BAO] khong ap duoc va tay o %s: %r' % (tep, cu[:60]))


def readme_goc():
    if not (G / 'README.md').exists():
        DOI.append(('README.md', 'tao moi'))
        if not THU:
            ghi_tep(G / 'README.md', (DAY / 'README_GOC.md').read_text(encoding='utf-8'))


def main():
    readme_goc()
    gitignore()
    gitattributes()
    don()
    java_server()
    lien_ket_md()
    client()
    tools()
    web()
    workflows()
    vps()
    chay_novadb()
    va_tay()
    for p, n in DOI:
        print('   sua %-70s %s' % (p, n))
    print('   %s: %d tep' % ('CHAY THU' if THU else 'da sua', len(DOI)))


if __name__ == '__main__':
    main()
