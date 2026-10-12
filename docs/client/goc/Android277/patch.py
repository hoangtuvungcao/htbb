"""Vá APK KPAH_277 gốc thành bản NQSH-H2.

    python -I patch.py <smali-src gốc> <thư mục ra> <modsmali> <package mới> <tên app>

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

src, out, modsmali, pkg, ten = sys.argv[1:6]
if os.path.exists(out):
    shutil.rmtree(out)
shutil.copytree(src, out)

MOD = "Lgame/render/screen/NqshMod;"
S = os.path.join(out, "smali")

for f in os.listdir(os.path.join(modsmali, "smali", "game", "render", "screen")):
    shutil.copy(os.path.join(modsmali, "smali", "game", "render", "screen", f), os.path.join(S, "game", "render", "screen", f))

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
]

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
open(p, "w", encoding="utf-8").write(s)

p = os.path.join(out, "res", "values", "strings.xml")
s = open(p, encoding="utf-8").read()
s, n = re.subn(r'<string name="app_name">[^<]*</string>', '<string name="app_name">%s</string>' % ten, s)
assert n == 1
open(p, "w", encoding="utf-8").write(s)
print("package", pkg, "ten", ten)
