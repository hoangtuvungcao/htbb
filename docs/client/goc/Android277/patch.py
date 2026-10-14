"""Vá APK KPAH_277 gốc thành bản NQSH-H2.

    python -I patch.py <smali-src gốc> <thư mục ra> <modsmali> <package mới> <tên app> <versionCode>

Chép smali-src sang thư mục ra rồi:
- thêm NqshMod (đã biên dịch ra smali) vào game/render/screen
- chèn lời gọi NqshMod ở đầu ServerListScr.loadSv / doUpdateServer, GameService.login,
  MessageHandler.onMessage, GameMidlet.platformRequest; đổi link IP ở GameMidlet.url
- đổi package (cài song song với bản TeaMobi) và tên app
"""
import os
import re
import shutil
import sys

src, out, modsmali, pkg, ten, ban = sys.argv[1:7]
if os.path.exists(out):
    shutil.rmtree(out)
shutil.copytree(src, out)

MOD = "Lgame/render/screen/NqshMod;"
AG = "Lgame/render/screen/AutoGuard;"      # hook của các tính năng mod (code chung với bản jar)
PM = "Lgame/render/screen/PauseMenu;"
S = os.path.join(out, "smali")
SCR = os.path.join(S, "game", "render", "screen")

# PauseMenu của APK là code chết (menu thật là MenuWindows) -> thay nguyên lớp bằng PauseMenu của mod
for f in os.listdir(SCR):
    if f == "PauseMenu.smali" or f.startswith("PauseMenu$"):
        os.remove(os.path.join(SCR, f))
for f in os.listdir(os.path.join(modsmali, "smali", "game", "render", "screen")):
    shutil.copy(os.path.join(modsmali, "smali", "game", "render", "screen", f), os.path.join(SCR, f))


def chan(goi):
    """Gọi guard trả Z: true = bỏ qua method gốc."""
    return [goi, "move-result v0", "if-eqz v0, :nqsh_tiep", "return-void", ":nqsh_tiep"]

# (file, khai báo method, đoạn chèn sau dòng .locals)
VA = [
    ("game/render/screen/ServerListScr.smali", ".method public static loadSv(Z)V",
     ["invoke-static {}, %s->apDungServer()V" % MOD, "return-void"]),
    ("game/render/screen/ServerListScr.smali", ".method public doUpdateServer()V",
     ["invoke-static {}, %s->capNhat()V" % MOD, "return-void"]),
    ("game/networklogic/GameService.smali", ".method public login(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
     ["invoke-static {p1, p2, p3}, %s->loginGuard(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z" % MOD,
      "move-result v0", "if-eqz v0, :nqsh_tiep", "return-void", ":nqsh_tiep"]),
    ("game/networklogic/MessageHandler.smali", ".method public onMessage(Lnetwork/Message;)V",
     ["invoke-static {p1}, %s->goiGuard(Lnetwork/Message;)Z" % MOD,
      "move-result v0", "if-eqz v0, :nqsh_tiep", "return-void", ":nqsh_tiep"]),
    ("com/teamobi/kpah/GameMidlet.smali", ".method public platformRequest(Ljava/lang/String;)V",
     ["invoke-static {p1}, %s->suaLink(Ljava/lang/String;)Ljava/lang/String;" % MOD, "move-result-object p1"]),

    # ---- hook các tính năng mod (giống bản jar: base-v53.jar + InjectGuards trong Jar276/build.sh) ----
    # mỗi khung hình: tick mọi auto (Auto up, Vận tiêu, Vận may, Lên thành, Tiện ích, việc chế đồ...)
    ("game/render/screen/GameScr.smali", ".method public update()V",
     ["invoke-static {}, %s->autoCheDoTick()V" % PM]),
    # popup của server: auto đọc kết quả / nuốt thông báo
    ("game/render/GCanvas.smali", ".method public static startOKDlg(Ljava/lang/String;Z)V",
     chan("invoke-static {p0}, %s->autoEatDlg(Ljava/lang/String;)Z" % AG)),
    # menu NPC: nuôi pet / vận may đọc danh sách; việc chế đồ ẩn menu
    ("game/render/screen/GameScr.smali", ".method public onMenuOption(IB[Ljava/lang/String;)V",
     chan("invoke-static {p1, p2, p3}, %s->menuGuard(IB[Ljava/lang/String;)Z" % AG)[:-1]
     + [":nqsh_tiep"] + chan("invoke-static {}, %s->autoHideUi()Z" % AG)[:2]
     + ["if-eqz v0, :nqsh_tiep2", "return-void", ":nqsh_tiep2"]),
    ("game/render/screen/GameScr.smali", ".method public showWindow(IZ[B)V",
     chan("invoke-static {p1, p2, p3}, %s->cuaSoGuard(IZ[B)Z" % AG)),
    # 13 locals -> p1..p5 là v14..v18: phải dùng /range
    ("game/render/screen/GameScr.smali", ".method public onActorMove(BSSSSBIBBZ)V",
     chan("invoke-static/range {p1 .. p5}, %s->viTriGuard(BSSSS)Z" % AG)),
    ("game/render/screen/GameScr.smali", ".method public onAdd2Party(Lnetwork/Message;)V",
     chan("invoke-static {p1}, %s->nhomGuard(Lnetwork/Message;)Z" % AG)),
    ("game/render/screen/GameScr.smali", ".method public onMap(SSSSLjava/lang/String;[B)V",
     chan("invoke-static {p1}, %s->mapGuard(S)Z" % AG)),
    # nút "Đập" (jar: ScreenDapDo$7, APK: ScreenDapDo$3)
    ("game/render/screen/ScreenDapDo$3.smali", ".method public perform()V",
     chan("invoke-static {}, %s->dapGuard()Z" % AG)),
]

# ---- thay lời gọi trong nút "Đập" (jar: InjectReplaceCall) ----
THAY = [
    ("game/render/screen/ScreenDapDo$3.smali",
     "invoke-virtual/range {v2 .. v8}, Lgame/networklogic/GameService;->doRequestThemThuocTinh(SIIIII)V",
     "invoke-static/range {v2 .. v8}, %s->themDong(Lgame/networklogic/GameService;SIIIII)V" % AG),
    ("game/render/screen/ScreenDapDo$3.smali",
     "invoke-virtual {v0, v1, v3}, Lgame/render/screen/Menu;->startAt(Ljava/util/Vector;I)V",
     "invoke-static {v0, v1, v3}, %s->startAtDapDo(Lgame/render/screen/Menu;Ljava/util/Vector;I)V" % AG),
    # tab "Khác" của MenuWindows: thêm mục "Auto (mod)" (ô thứ 10 đang trống) ...
    ("game/render/screen/MenuWindows.smali",
     "    iput-object v1, p0, Lgame/render/screen/MenuWindows;->tabKhac:[Ljava/lang/String;\n",
     "    invoke-static {v1}, %s->themMucMod([Ljava/lang/String;)[Ljava/lang/String;\n\n"
     "    move-result-object v1\n\n"
     "    iput-object v1, p0, Lgame/render/screen/MenuWindows;->tabKhac:[Ljava/lang/String;\n" % MOD),
    # ... và bấm ô đó thì mở menu mod (đọc lại selectClan vào v1 nên không đụng thanh ghi khác)
    ("game/render/screen/MenuWindows.smali",
     "    iget v1, v0, Lgame/render/screen/MenuWindows;->selectClan:I\n\n    packed-switch v1, :pswitch_data_0\n",
     "    iget v1, v0, Lgame/render/screen/MenuWindows;->selectClan:I\n\n"
     "    invoke-static {v1}, %s->moMucKhac(I)Z\n\n"
     "    move-result v1\n\n"
     "    if-eqz v1, :nqsh_khac\n\n"
     "    return-void\n\n"
     "    :nqsh_khac\n"
     "    iget v1, v0, Lgame/render/screen/MenuWindows;->selectClan:I\n\n"
     "    packed-switch v1, :pswitch_data_0\n" % MOD),
]
for rel, cu, moi in THAY:
    p = os.path.join(S, rel)
    s = open(p, encoding="utf-8").read()
    assert s.count(cu) == 1, (rel, cu, s.count(cu))
    open(p, "w", encoding="utf-8").write(s.replace(cu, moi))
    print("thay", rel, cu.strip()[:60])

# cuối GCanvas.paint (một return-void duy nhất): vẽ thanh trạng thái các auto
p = os.path.join(S, "game", "render", "GCanvas.smali")
s = open(p, encoding="utf-8").read()
i = s.index(".method protected paint(Lgame/core/j2me/Graphics;)V\n")
j = s.index(".end method", i)
assert s[i:j].count("    return-void\n") == 1
k = s.index("    return-void\n", i)
s = s[:k] + "    invoke-static {p1}, %s->paint(Lgame/core/j2me/Graphics;)V\n\n" % AG + s[k:]
open(p, "w", encoding="utf-8").write(s)
print("va", "game/render/GCanvas.smali", "paint -> AutoGuard.paint")

# Trang chủ server gửi lúc đăng nhập (GameMidlet.url): link trỏ thẳng IP -> tên miền
p = os.path.join(S, "game", "networklogic", "MessageRecieve0.smali")
s = open(p, encoding="utf-8").read()
s, n = re.subn(r"\n    sput-object (v\d+), Lcom/teamobi/kpah/GameMidlet;->url:Ljava/lang/String;\n",
              lambda m: "\n    invoke-static {%s}, %s->suaLink(Ljava/lang/String;)Ljava/lang/String;\n"
                        "    move-result-object %s\n%s" % (m.group(1), MOD, m.group(1), m.group(0)[1:]), s)
assert n == 1, n
open(p, "w", encoding="utf-8").write(s)
print("va", "game/networklogic/MessageRecieve0.smali", "GameMidlet.url")

for rel, khai, chen in VA:
    p = os.path.join(S, rel)
    s = open(p, encoding="utf-8").read()
    i = s.index(khai + "\n")
    m = re.compile(r"\n    \.locals (\d+)\n").search(s, i)
    assert m and int(m.group(1)) >= 1 and s.find(".end method", i) > m.start(), (rel, khai)
    them = "\n    # NQSH-H2\n" + "".join("    %s\n" % l for l in chen)
    s = s[:m.end()] + them + s[m.end():]
    open(p, "w", encoding="utf-8").write(s)
    print("va", rel, khai.split()[-1])

p = os.path.join(out, "AndroidManifest.xml")
s = open(p, encoding="utf-8").read()
assert s.count('package="com.teamobi.kpah"') == 1
s = s.replace('package="com.teamobi.kpah"', 'package="%s"' % pkg)
# Tự cập nhật (NqshCapNhat): quyền mở trình cài APK + FileProvider chia sẻ file APK đã tải
s = s.replace('<uses-permission android:name="android.permission.INTERNET"/>',
              '<uses-permission android:name="android.permission.INTERNET"/>\n'
              '    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>', 1)
s = s.replace("</application>",
              '    <provider android:name="androidx.core.content.FileProvider" android:authorities="%s.capnhat" '
              'android:exported="false" android:grantUriPermissions="true">\n'
              '            <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/nqsh_capnhat"/>\n'
              '        </provider>\n    </application>' % pkg, 1)
assert "REQUEST_INSTALL_PACKAGES" in s and "nqsh_capnhat" in s
open(p, "w", encoding="utf-8").write(s)
os.makedirs(os.path.join(out, "res", "xml"), exist_ok=True)
open(os.path.join(out, "res", "xml", "nqsh_capnhat.xml"), "w", encoding="utf-8").write(
    '<?xml version="1.0" encoding="utf-8"?>\n<paths>\n    <cache-path name="capnhat" path="capnhat/"/>\n</paths>\n')

p = os.path.join(out, "apktool.yml")
s = open(p, encoding="utf-8").read()
s, n = re.subn(r"(\n  versionCode: )\d+", r"\g<1>" + str(int(ban)), s)
assert n == 1
open(p, "w", encoding="utf-8").write(s)

p = os.path.join(out, "res", "values", "strings.xml")
s = open(p, encoding="utf-8").read()
s, n = re.subn(r'<string name="app_name">[^<]*</string>', '<string name="app_name">%s</string>' % ten, s)
assert n == 1
open(p, "w", encoding="utf-8").write(s)
print("package", pkg, "ten", ten, "versionCode", ban)
