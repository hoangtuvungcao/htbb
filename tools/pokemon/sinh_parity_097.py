# Sinh du_lieu/parity_097.txt tu main 1a705be6 (ban 0.9.7): python3 tools/pokemon/sinh_parity_097.py > du_lieu/parity_097.txt (chay o goc repo)
import re, subprocess, sys
R='1a705be6'
def show(p): return subprocess.run(['git','show',R+':'+p],capture_output=True,text=True).stdout
out=['# DOI CHIEU 0.9.7 (main 1a705be6) - server/test/KiemParity.java: moi muc phai con trong ban hien tai.',
     '# Dong "muc = thay bang: ly do" = co y doi / bo (chu game duyet). Sinh lai: python3 tools/pokemon/sinh_parity_097.py > du_lieu/parity_097.txt','']
def sec(ten, tep, muc):
    out.append('[%s] %s' % (ten, tep))
    for m in sorted(set(muc)): out.append(m)
    out.append('')
for f in ['server/src/real/MapXuLyLenh.java','server/src/real/MapDoMenu.java']:
    sec('case', f, re.findall(r'case (-?\d+):', show(f)))
sec('gm', 'server/src/real/MapChat.java', re.findall(r'(?:startsWith|equals)\("([a-z_]+)', show('server/src/real/MapChat.java')))
sec('khoa', 'sql/htbb.sql', re.findall(r'new DinhNghiaKhoa\("([^"]+)"', show('server/src/com/htbb/cauhinh/DanhMucSinh.java')))
sec('bang', 'sql/htbb.sql',  # 0.9.7 con la sql/htbb_day_du.sql
    ['CREATE TABLE IF NOT EXISTS `%s`' % t if False else t for t in re.findall(r'CREATE TABLE (?:IF NOT EXISTS )?`([a-z0-9_]+)`', show('sql/htbb_day_du.sql'))])
sec('hud', 'client_jar/src/game/render/screen/HudRong.java', re.findall(r'"(hud-[a-z0-9-]+)"', show('client_jar/src/game/render/screen/HudRong.java')))
m=re.search(r'static final String\[\] TEN = \{(.*?)\};', show('client_jar/src/game/pokemon/ManMenu.java'), re.S)
sec('chuoi', 'client_jar/src/game/pokemon/ManMenu.java', re.findall(r'"([^"]+)"', m.group(1)))
m=re.search(r'String\[\] MA = \{(.*?)\};', show('client_pc/src/htbb/pc/BanPhim.java'), re.S)
sec('chuoi', 'client_pc/src/htbb/pc/BanPhim.java', re.findall(r'"([^"]+)"', m.group(1)))
print('\n'.join(out))
