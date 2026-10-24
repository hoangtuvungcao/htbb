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
# 0.9.9: LOI VAO bang Tu dong danh + nut Cai dat tren HUD (PC / J2ME HudRong, APK Hud) + giftcode CHI o Menu -> Thu/Qua (0.9.8 mat o Menu tren HUD man rong - chu game bao "menu AUTO bi mat"): doan ma
# phai con nguyen trong tep (KiemParity loai "ma" = chua dung chuoi). Khong sinh tu git: giu tay o day.
LOI_VAO_TU_DONG = [
    ('client_jar/src/game/render/screen/HudRong.java', ['"hud-tu-dong"', '"hud-tu-dong-cai-dat"', 'BangTuDong.mo();   // nhấn giữ viên AUTO = cài đặt', 'case 31:',
        '"hud-cai-dat"', 'moCaiDat();   // 0.9.9: nút Cài đặt hàng trên']),
    # 0.9.16: Nhiem vu cua toi + So tay huan luyen dung TRUOC Tu dong danh trong hang vien phu (Tu dong van luon co)
    ('client_jar/src/game/pokemon/ManMenu.java', ['static final int[] PHU = {I_NV_CUA_TOI, I_SO_TAY, I_TU_DONG,', 'return PHU;', 'game.render.screen.CauGiaoDien.moTuDong();']),
    ('client_jar/src/game/render/screen/CauGiaoDien.java', ['public static void moTuDong()', 'BangTuDong.mo();']),
    ('client_jar/src/game/render/screen/MenuNhanh.java', ['case TU_DONG:', 'BangTuDong.ve(g);', 'BangTuDong.tick();']),
    ('client_jar/src/game/render/screen/GameScr.java', ['new Command("Tự động đánh"']),
    ('client_jar/shared/mod/game/render/screen/PauseMenu.java', ['Nen.moBangTuDong();']),
    ('client_jar/shared/mod/game/render/screen/AutoGuard.java', ['Nen.menuNhanhVe(graphics)', 'Nen.menuNhanhTick()']),
    ('client_apk/pokemon/src/main/java/com/htbb/pokemon/hd/Hud.java', ['case N_TU_DONG:', 'HdCau.moTuDong()', 'case N_TD_CAI:', 'case N_CAI_DAT:']),
    ('client_apk/pokemon/src/main/java/com/htbb/pokemon/hd/HudBoTri.java', ['int[] tren = {N_TU_DONG, N_TD_CAI, N_CAI_DAT,']),
    ('client_apk/pokemon/src/main/java/game/render/screen/HdCau.java', ['Class.forName("game.render.screen.BangTuDong")']),
    ('client_jar/src/game/render/screen/BangTuDong.java', ['"td-bat"', '"td-chieu-', '"td-thuoc-hp"', '"td-thuoc-pp"', '"td-vung"', '"td-hoi"', '"td-hoi-sinh"', '"td-nhat"', '"td-trang"', '"Trang chiêu dùng"']),
]
LOI_VAO_TU_DONG.append(('client_jar/src/game/pokemon/ManMenu.java', ['HanhDong.moGiftcode();']))
for tep, ds in LOI_VAO_TU_DONG:
    sec('ma', tep, ds)
print('\n'.join(out))
