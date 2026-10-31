"""Cap nhat muc taiGame (web_cai_dat) cho ban client moi - goi boi tools/phat-hanh/len-tat-ca.sh tren VPS.

python3 cap_nhat_trang_tai.py <json_cu> <BAN> <so_phien_ban> <jar> <exe> <appimage> <apk>  (ghi chu lay tu bien NK)
In ra lenh SQL UPDATE (doc SHA256SUMS.txt + kich thuoc tep trong /opt/htbb/downloads).
"""
import datetime
import json
import os
import sys

THU_MUC = '/opt/htbb/downloads/'
json_cu, ban, so, f_jar, f_exe, f_lin, f_apk = sys.argv[1:8]
d = json.load(open(json_cu, encoding='utf-8'))
sha = {}
for dong in open(THU_MUC + 'SHA256SUMS.txt'):
    h, f = dong.split()
    sha[f.lstrip('*')] = h
tep = {'jar': f_jar, 'windows': f_exe, 'linux': f_lin, 'android': f_apk}
for b in d['ban']:
    f = tep.get(b.get('ma'))
    if not f:
        continue
    b.update(url='/tai/' + f, phienBan=so + ' (Open test)', capNhat=datetime.date.today().isoformat(),
             nhatKy=os.environ.get('NK', ''), sha256=sha[f], ban=int(ban),
             dungLuong='%.1f MB' % (os.path.getsize(THU_MUC + f) / 1048576))
s = json.dumps(d, ensure_ascii=False).replace('\\', '\\\\').replace("'", "\\'")
print("UPDATE web_cai_dat SET gia_tri='%s', cap_nhat_boi='len-tat-ca-%s' WHERE khoa='taiGame';" % (s, ban))
