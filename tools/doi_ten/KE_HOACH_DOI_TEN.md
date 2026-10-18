# Kế hoạch: gộp monorepo `htbb` + đổi tên KPAH → HTBB + đổi tên lớp kiếm hiệp → Pokémon

**Cập nhật 18/10/2026 (diễn tập lần 2, bảng tên theo THUAT_NGU.md).** Ba việc chạy chung MỘT lần (giai đoạn B), có diễn tập trên bản nháp:

1. **Gộp** HTBB, htbb-client, HTBB-Web → `~/Downloads/pokemon/htbb/` (giữ lịch sử git). 3 thư mục cũ không bị sửa.
2. **Đổi tên KPAH → HTBB** (phần dưới, mục 1–5 cũ): chạy trên BẢN CHÉP của 3 repo trước khi gộp.
3. **Đổi tên lớp/tệp/thư mục kiếm hiệp → Pokémon** theo `doi_ten_lop.tsv` (99 dòng: 66 đổi, 33 giữ có lý do).

## A. Bố cục monorepo (`ban_do_gop.py`)

| Cũ | Mới |
|---|---|
| HTBB: `src lib test map cMap* loginServer *.bat htbb-server.jar README.md` | `server/` |
| HTBB: `sql/`, `docs/`, `tools/`, `vps/`, `.claude/` | giữ ở gốc |
| HTBB: `web/` (PHP cũ) | **bỏ hẳn** (19/10/2026) — website duy nhất là HTBB-Web → `web/`; xem `docs/pokemon/BO_WEB_PHP.md` |
| HTBB: `ClientUnity/` | `client_pc_ios/ClientUnity/` — **GIỮ**, xem B |
| htbb-client: `j2me/` + `shared/` | `client_jar/` + `client_jar/shared/` |
| htbb-client: `android/`, `pc/`, `phat-hanh/`, `docs/`, `README.md` | `client_apk/`, `client_pc_ios/`, `tools/phat-hanh/`, `docs/client/`, `docs/client/README.md` |
| HTBB-Web | `web/`; `server/sql/` → `sql/web/` (bản trùng `web_cms.sql` bị bỏ, giữ `sql/web_cms.sql`) |
| `.github/workflows/*` | `.github/workflows/{server,client,web}-*.yml` + `paths:` + `working-directory` |
| `.gitignore`, `.gitattributes` | GỘP về gốc (mẫu được neo lại theo thư mục mới); `web/.gitignore` giữ |

Lịch sử: `git filter-branch --index-filter` (có sẵn trong git 2.43; không có git-filter-repo) đổi đường dẫn MỌI
commit của từng repo theo bảng trên (đã kiểm: 18 969 đường dẫn trong toàn lịch sử, 0 va chạm), rồi một commit gộp
3 cha (`commit-tree`, cây = hợp 3 cây) cho mỗi nhánh `main`, `pokemon/chuyen-doi`. Nhánh phụ cũ → `cu/<repo>/<nhánh>`.
`git log --follow server/src/real/Map.java` đi qua lúc gộp (115 commit). `.git` mới ~260 MB.
Thay đổi CHƯA commit của 3 repo (của các agent khác) + đổi tên + sửa đường dẫn để nguyên CHƯA COMMIT trong `htbb/`.

Quy ước chạy: test/game chạy ở `server/` (test đọc `../docs`, `../sql`...); công cụ `tools/` chạy ở gốc
(`server/src/...`); `client_jar/`, `client_apk/`, `web/` chạy trong chính thư mục đó.

## B. Không phải rác — giữ lại

- **`client_pc_ios/ClientUnity/`**: KHÔNG phải bản trùng của `pc/`: 278 tệp `.cs`, nhiều tệp khác nhau
  (GameScr 875 dòng, WindowInfoScr 722, MFont 168...), không có `XayDung.cs` bên pc; ~95 phép kiểm server soi vào nó
  (thử thay bằng pc: 130 hỏng thay vì 61). Bỏ được sau khi chuyển các test đó sang `client_pc_ios/Assets` (việc riêng).
  Ảnh `Resources/poke*` hai bên giống hệt.
- Đã bỏ: `sql/web/web_cms.sql` (trùng byte), 2 symlink tương thích (`server/KPAH2_patched.jar`, `server/lib/NQSH_5h.jar`
  — checkout mới không cần), `.gitignore` riêng của client/HTBB (đã gộp).
- Tệp bị ignore được CHÉP cho máy dev: `server/server.ini`, `server/chay_novadb/` (không pid), các `.txt` runtime,
  `docs/pokemon/map_cu`, `map_moi/kiem` (test đọc), `tools/pokemon/pmd_raw*` (đầu vào sinh dữ liệu), `web/node_modules`
  (hardlink). Không chép: `build/`, `out/`, `.gradle`, `.nuxt`, `.output` (sinh lại được).

## C. Đổi tên lớp (`doi_ten_lop.py` + `doi_ten_lop.tsv`, theo `docs/pokemon/THUAT_NGU.md`)

112 dòng: 79 ĐỔI, 33 GIỮ. Diễn tập: 275 tệp/thư mục `git mv`, 1 968 chỗ thay trong 293 tệp (server, J2ME, `shared/mod`,
`port-android.py`, PC Unity, ClientUnity, tools, test, docs, ghi chú SQL/CSV). Khái niệm giống hệt chữ người chơi thấy
(THUAT_NGU mục 4): `MapBangHoi→MapDoi`, `TraVanTieu→TraHang`, `MonsterVanTieu→QuaiGiaoHang`, `AutoVanTieu→AutoGiaoHang`,
`CongThanh→CongGym`, `LenThanh→LenGym`, `ThanThu→PokemonHuyenThoai`, `doRideAnimal→doCuoiThu`,
`doUnRideHorse→doXuongThuCuoi`, `ThanKhi→BaoVatHuyenThoai`, `BaiLuyenCong→BaiLuyenTap`...; boss theo loài thật
(id quái trong code + `bang_quai.csv`): `BossLinhXa→BossSerperior`, `BossSonTinhThuyTinh→BossKyogreGroudon`,
`TruRong→BossRayquaza`, `Skill_Kiem_*→Skill_Charmander_*`...
**Giữ** (lý do trong tsv, kèm tên đề xuất): 15 lớp bị bytecode `lib/htbb-core.jar` tham chiếu (`BossTruRong`,
`BossDracula`, `BossNguoiTuyet`, `BossMatQuy`, `CharThanthu`, `MapLienDau→MapGiaiLienMinh`, `MapLoiDai→MapSanDau`,
`MapChauBau`, `MapChienTruongMoba`, `RegionVantienTran`...): jar tạo bằng `new` và `instanceof` theo tên cũ nên lớp
đệm cùng tên không an toàn — muốn đổi phải ánh xạ lại cả jar (ASM/jarjar, việc riêng). Giữ thêm: `KhoXuLuong` (cột
CSDL), `GameService.doRideAnimal` client (xử lý gói tin), thư mục tài nguyên nạp theo chuỗi / client đã cache
(`res/wpsplash/*`, `map/thanthu`, `map/horse`...), tên CSDL/bảng/cột. Unity: `.cs` + `.cs.meta` đi cặp (GUID giữ,
MonoBehaviour duy nhất `Main`/`CapNhat` không đổi). J2ME: ProGuard chỉ giữ `game.GameMidlet`. Không có tên lớp trong
gói tin / RMS / CSDL.

## D. Diễn tập (18/10/2026, `--thu` trên bản nháp, toàn bộ chuỗi gộp + 2 lần đổi tên)

| Bước | Kết quả |
|---|---|
| test server (`cd server`, javac + `Kiem`) | 3/9 280 hỏng = **trùng khớp từng dòng** với bản chép HTBB trước gộp (cùng thời điểm; bản chép 3/9 275) |
| jar J2ME (`client_jar/build.sh 97`, `HTBB_ASSETS=pc`) | OK: thường / thử / release |
| `KiemPokeDuLieu` | OK (kể cả hồ câu cá, đọc `server/cMap_pokemon`) |
| APK `client_apk/build-pokemon.sh` | OK `HTBB-android-thu.apk` |
| C# `client_pc_ios/Assets/Scripts`, `ClientUnity/Assets/Scripts` (dotnet + UnityEngine.dll) | OK, 0 lỗi |
| web `nuxt build` (Node 22) / `npm test` | OK / 60 đạt, 0 hỏng |
| đổi tên lớp chạy lần 2 | 0 thay đổi; `sua_sau_gop.py` chạy lần 2: 0 thay đổi |
| `doi_ten_kpah.py --kiem` (trên bản chép) | 0 SÓT |

## E. Giai đoạn B (khi có "chạy đi")

`sh HTBB/tools/doi_ten/chay_giai_doan_b.sh --that [--bo-qua-tien-trinh-khac]`:
1. dừng stack cũ (`HTBB/chay_novadb/DUNG_HAN` + kill PID trong `*.pid`: chay_lap, game, login, NovaDB) — từ chối nếu còn
   TeamServer của agent khác (NovaDB chỉ có 1 user MySQL);
2. `gop_monorepo.py --sao-luu` (tar 3 repo + `novadb-data` → `~/Downloads/pokemon/sao_luu_gop_<giờ>.tar`), chép +
   đổi tên KPAH trên bản chép, viết lại lịch sử, gộp, phủ cây làm việc, `sua_sau_gop.py`, chép tệp cục bộ;
3. `doi_ten_lop.py --apply` (+ chạy lại phải 0);
4. `chuyen_csdl.sh --apply --khoi-dong` từ `htbb/` (`kpah_db.novadb→htbb_db.novadb`, user `htbb`, biên dịch
   `server/build/classes`, bật NovaDB + login + `server/chay_novadb/chay_lap.sh`);
5. `kiem_tat_ca.sh` (bảng D); 6. bot `BotTai` đăng nhập 127.0.0.1:19129.
**Đóng băng**: agent khác phải ngừng sửa 3 repo cũ trước bước 2 (bản chụp lấy lúc đó; sửa sau đó không vào `htbb/`).
Quay lại: `chuyen_csdl.sh --quay-lai`, xoá `DUNG_HAN`, bật lại stack cũ từ `HTBB/`, `rm -rf htbb htbb.lam htbb.kiem`.

## F. GitHub (chủ repo tự chạy — script không đụng remote)

```bash
cd ~/Downloads/pokemon/htbb
gh repo create hoanghoa0903/htbb --private --source . --remote origin      # KHÔNG --push
git push -u origin pokemon/chuyen-doi main 'refs/heads/cu/*:refs/heads/cu/*'
# secrets: không đọc ra được -> nhập lại (tên server: VPS_SSH_KEY DB_USER DB_PASSWORD DB_USER_NAP DB_PASS_NAP DB_USER1 DB_PASSWORD1;
#   web: VPS_SSH_KEY_WEB + HTBB_* ; client: UNITY_*, HTBB_KEYSTORE_B64, HTBB_KS_PASS, HTBB_TAI_TOKEN...)
for v in $(gh variable list -R hoanghoa0903/KPAH-Web --json name -q '.[].name'); do
  gh variable set "$(echo $v | sed 's/^KPAH_/HTBB_/')" -R hoanghoa0903/htbb --body "$(gh variable get $v -R hoanghoa0903/KPAH-Web)"; done
gh variable set HTBB_VPS_MONOREPO --body 1 -R hoanghoa0903/htbb   # CHỈ sau khi VPS chuyển xong (vps/CHUYEN_TEN_HTBB.md)
# sau khi chạy ổn: lưu trữ repo cũ (chỉ đọc), KHÔNG xoá
gh repo archive hoanghoa0903/KPAH; gh repo archive hoanghoa0903/KPAH-Web; gh repo archive hoanghoa0903/kpah-mod-pb
```
Không đổi `kpah-server/nqsh-h2-tai` (APK NQSH-H2 cũ đọc cứng URL raw). Với monorepo thì `gh repo rename` các repo cũ
(mục 4 phía dưới) không còn cần.

VPS: `vps/CHUYEN_TEN_HTBB.md` (viết lại cho monorepo): checkout mới `/opt/htbb` (sparse `server vps sql
web/deploy`), CHÉP dữ liệu chạy từ `/opt/kpah` (không sửa `/opt/kpah` — đường lui), `htbb_db` bằng mysqldump,
đổi user, unit `htbb-*` (WorkingDirectory `/opt/htbb/server`), Apache chỉ còn làm reverse proxy cho Nuxt (không vhost PHP), cron; deploy tự động chỉ
bật khi đặt `HTBB_VPS_MONOREPO=1`.

## G. Công cụ mới trong thư mục này

`ban_do_gop.py` (bảng đường dẫn), `loc_lich_su.py` (index-filter), `gop_monorepo.py` (bước 1–7), `sua_sau_gop.py`
(đường dẫn sau gộp, idempotent), `doi_ten_lop.py` + `.tsv`, `kiem_tat_ca.sh`, `chay_giai_doan_b.sh`, `README_GOC.md`.

---

# (Phần cũ) Đổi tên KPAH → HTBB (gồm cả hạ tầng)

Yêu cầu: "xoá all kpah, chuyển qua pokemon hết, htbb" — bỏ nốt các tên KPAH đã cố ý giữ ở lần đổi tên trước
(commit "Doi ten KPAH -> HTBB"): tên CSDL, biến `KPAH_*`, `/opt/kpah`, unit `kpah-*`, `KPAH2_patched.jar`...

Công cụ (thư mục này; script đổi tên tự bỏ qua `tools/doi_ten/` và `vps/CHUYEN_TEN_HTBB.md`):

| Tệp | Việc |
|---|---|
| `doi_ten_kpah.py` | Sửa 3 repo + `git mv` + symlink tương thích + sửa manifest jar. Mặc định CHẠY THỬ; `--apply`; `--kiem`; `--quay-lai <tar>`; `--da-doi-repo`; `--chi-tiet` |
| `chuyen_csdl.sh` | Dữ liệu máy dev (NovaDB): `kpah_db.novadb` → `htbb_db.novadb`, user NovaDB `kpah` → `htbb`, sửa `server.ini`/ini login. Mặc định CHẠY THỬ; `--apply [--khoi-dong]`; `--quay-lai` |
| `../../vps/CHUYEN_TEN_HTBB.md` | Sổ tay VPS: MariaDB `htbb_db` (mysqldump, giữ `kpah_db`), user Linux, `/opt/htbb` + symlink, unit `htbb-*` + `Alias=`, Apache, cron, quay lại |

Chạy (ở `~/Downloads/pokemon`): `python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py` (thử) → `--apply` → `--kiem`.
Script chạy lại bao nhiêu lần cũng được (đã thử: lần 2 = 0 thay đổi; `--quay-lai` trả về y hệt từng byte).

## 1. Kiểm kê (18/10/2026, tính cả tệp chưa commit của các agent khác)

Grep `kpah|khí phách|khiphach|nqsh` (không phân biệt hoa thường), tệp văn bản trong git + tệp mới chưa ignore:

| Repo | Tệp có | Dòng có | Tên tệp/thư mục có kpah/nqsh |
|---|---|---|---|
| HTBB | 189 | 1158 | `KPAH2_patched.jar`, `lib/NQSH_5h.jar`, `tools/giai-ma-nqsh.bat`, `vps/kpah-{game,login}.service`, `vps/kpah-logrotate.conf`, `vps/kpah-mariadb.cnf` (`web/server/NQSH2.txt` đã bỏ cùng web PHP) |
| htbb-client | 55 | 286 | `phat-hanh/kpah-tai.conf`; `NqshMod.java`, `NqshCapNhat.java`, `res/xml/nqsh_capnhat.xml` (module cũ, giữ) |
| HTBB-Web | 85 | 449 | `deploy/kpah-web.service`, `deploy/apache-kpah-web.conf` |
| Dữ liệu dev | — | — | `novadb-data/kpah_db.novadb(+wal/shm)`; `kpah_db_bot`, `kpah_db_hst` (của agent kiểm thử/hệ sinh thái đang chạy) |

Kết quả chạy thử `doi_ten_kpah.py` (số chỗ đổi / số tệp):

| Nhóm | Nội dung | Chỗ | Tệp | Cách làm |
|---|---|---|---|---|
| (1) CSDL | `kpah_db` (JDBC trong test, `server.ini.tmpl`/`.example`, SQL, script vps/win, web `config.ts` mặc định, admin web, tài liệu, `kpah_db_hst/_nap/_bot`) | 330 | 106 | → `htbb_db`. VPS: `sinh-server-ini.sh` vẫn sinh `kpah_db` tới khi có `/etc/htbb/da-chuyen-csdl`; `backup_db.sh` cũng theo dấu đó |
| (2) ENV | `KPAH_*` (web `config.ts`, `.env.example`, `deploy.yml`, `deploy-web.sh` whitelist, mã TS/MJS, `KPAH_BACKUP_DICH`, tài liệu) | 485 | 70 | → `HTBB_*`; fallback 1 bản: `env()` đọc `KPAH_` khi thiếu `HTBB_`; `process.env.HTBB_X ?? process.env.KPAH_X`; workflow `secrets.HTBB_X \|\| secrets.KPAH_X`; `deploy-web.sh` nhận cả `KPAH_*` và ghi thành `HTBB_*` |
| (3) Đường dẫn/dịch vụ | `/opt/kpah`, `/opt/kpah-web`, `kpah-game/login/web`, user `kpah`/`kpah-web`, `/etc/kpah-web.env`, logrotate, `90-kpah.cnf`, `/etc/ssl/kpah`, conf Apache `kpah-*`, khoá `/tmp/kpah-*.lock`, frp/VPS docs | 601 | 103 | → `htbb`. Biến đầu script (`DIR/OUT/GOC/USER_SV/SV_GAME/...`) rơi về tên cũ nếu máy chưa chuyển; unit mới có `Alias=kpah-*.service` |
| (4) Jar | `KPAH2_patched.jar` (ExecStart, build/capnhat/run.bat, deploy-auto/update/capnhat.sh, admin), `lib/NQSH_5h.jar` (Class-Path, CI, script, tool), `build/nqsh-src`, `giai-ma-nqsh.bat` | 186 | 76 | → `htbb-server.jar`, `lib/htbb-core.jar`, `build/core-src`, `giai-ma-core.bat`. MANIFEST viết lại (chỉ Class-Path, giữ mọi class). Symlink tên cũ **trong git** cho 1 bản |
| (5) Repo GitHub | `hoanghoa0903/KPAH`, `KPAH-Web`, `kpah-mod-pb` (tài liệu, echo); `kpah-server/nqsh-h2-tai` | 0 | 0 | KHÔNG đổi remote. Giữ chữ tới khi chủ repo chạy lệnh mục 4, rồi `--da-doi-repo` |
| (6) Mã/chú thích | chú thích Java/C#/PHP/TS, tên test trong chuỗi (`KiemTaiLieuCauHinh`, `KiemChotCSDL`, `KiemLocBiMat`...), log, `[[project-kpah-nqsh-...]]` | 176 | 48 | → htbb (chuỗi kiểm và tệp được kiểm đổi cùng lúc) |
| (7) Người chơi/quản trị thấy | README "trước đây là KPAH", bot tên `pronqsh`, admin web "client NQSH-H2 của mình", artifact iOS `HTBB276-NQSH-H2-*`, `rootProject.name 'nqsh-h2-android'`, Unity `unity.KPAH.KPAH` | 24 | 17 | → HTBB / `probaoboi` / `com.htbb.pokemon` |

Tổng: HTBB 174 tệp, htbb-client 13, HTBB-Web 85 + 10 `git mv` + 2 symlink + 10 miếng vá tương thích:
`config.ts env()`, `deploy-web.sh` (đổi khoá cũ), `htbb-web.service` (nạp cả `/etc/kpah-web.env` và `/etc/htbb-web.env`,
`Alias=`), `.env.example`, `sinh-server-ini.sh` (giữ `kpah_db` tới khi có dấu chuyển), `backup_db.sh`,
`chay_tren_novadb.sh` (còn `kpah_db` mà chưa có `htbb_db` thì DỪNG — không thì nó nạp `sql/htbb_day_du.sql`, ghi đè cả
bảng của CSDL `account`),
`htbb-game.service`/`htbb-login.service` (`Alias=`), `android/build.sh` (thư mục khoá cũ `Desktop/KPAH-All`).

### Không đổi — có lý do (lệnh `--kiem` liệt kê, 0 dòng "SOT")

| Còn lại | Số | Lý do |
|---|---|---|
| dòng có dấu `TUONG-THICH-TEN-CU` | 94 | lối rơi 1 bản phát hành (xoá ở giai đoạn 2 của sổ tay VPS) |
| symlink `KPAH2_patched.jar`, `lib/NQSH_5h.jar` | 2 | deploy-auto.sh cũ + unit cũ + script ngoài repo |
| `real.UtilKPAH`, gói `logNQSH` | 18 | nằm trong bytecode `htbb-core.jar` (không có source); đổi = phải sửa nhị phân |
| khoá RMS `nqshlogin/nqshIP/nqshQuickSlot/ipnqsh(auto)/nqshImgPotion(New)` | 65 | dữ liệu đã lưu trên máy người chơi (mật khẩu, IP, ô nhanh) |
| khoá RMS/PlayerPrefs `kpahmay` | 8 | mã máy; đổi = mọi máy có mã mới = lách được cấm theo máy |
| `kpah-private.site` | 16 | tên miền DNS thật |
| `kpah-server/nqsh-h2-tai` | 29 | repo phát hành kênh NQSH-H2 cũ; APK cũ đọc cứng `raw.githubusercontent.com/...` |
| tiền tố URL `/kpah/`, `/var/www/html/kpah` | 41 | link cũ đã phát tán; `RedirectMatch ^/kpah/` giữ; Apache vẫn khoá `/var/www/html/kpah/admin` |
| `/NQSH2.txt` | 13 | URL danh sách server mà client cũ đã phát hành tải về |
| `KPAH_276*`, `kpah275555*`, `KPAH-VER3`, `build/emu/kpah.jar`, `KhiPhachAnhHung`, `suite-Khi Phach Anh Hung` | 33 | tên tệp/thư mục GỐC của TeaMobi trên máy (đổi trong tài liệu = hướng dẫn sai) |
| `TEN_GAME_CU = "Khí Phách Anh Hùng"` (server + Unity) | 2 | tên hiển thị khi `sv.che_do_pokemon=0`; test `KiemHinhPokemon` cần khác `TEN_GAME` |
| `indexOf("KPAH")` + chú thích (TaiDanhSach) | 2 | bộ lọc ẩn tên server cũ trong danh sách |
| `"teamobi", "kpah"` (quy tắc đăng ký web + Java sinh ra, có sha256) | 2 | tên cấm đăng ký chống mạo danh. Nên **thêm** `"htbb"` (báo agent đăng ký; sinh lại bằng `scripts/sinh-quy-tac-dang-ky-java.mjs`) |
| `g=kpah` (my.teamobi.com) | 2 | tham số API bên ngoài |
| `(159,'kpah')` trong `sql/htbb_day_du.sql` (bảng `bad_word`) | 1 | từ cấm trong chat (chặn quảng cáo tên server cũ); CSDL dev cũng chỉ có đúng dòng này chứa kpah (đã quét mọi cột text của `kpah_db` + `account`) |
| repo GitHub chưa đổi (`hoanghoa0903/KPAH`, `KPAH-Web`, `kpah-mod-pb`) | 8 | chờ `gh repo rename` → `--da-doi-repo` |
| NQSH-H2 khác (htbb-client: `android/app` + `NqshMod/NqshCapNhat`, `phat-hanh.yml` cũ, `NQSH=1`, `CHO_PHEP_NQSH`, `nqsh-h2.top`, `com.nqsh.h2`, `NQSH_TAI_TOKEN`; tài liệu kênh cũ) | 211 | sản phẩm/kênh cũ riêng (người chơi NQSH-H2 cũ tự cập nhật qua đó) + chốt chặn "không phát hành nhầm sang NQSH-H2". Không phải tên KPAH; bỏ hẳn kênh này là việc riêng |
| `HTBB/.claude/settings.json` (12 chỗ) | — | cấu hình Claude Code: script không đụng. Muốn đổi thì chủ repo sửa tay |

## 2. Rủi ro

1. **Deploy tự động khi push `main`.** `deploy-auto.sh` đang chạy là bản CŨ khi `git pull` lần đầu: nó kiểm
   `unzip -t KPAH2_patched.jar` → giải bằng symlink trong git. Unit cũ `-jar KPAH2_patched.jar` → symlink →
   manifest mới trỏ `lib/htbb-core.jar`. Đã thử: `java -cp KPAH2_patched.jar real.UtilKPAH` tìm được lớp trong jar lib.
2. **CSDL lệch.** `htbb_db` chép sẵn mà server tự nhảy sang → mất dữ liệu ghi sau lúc chép. Vì vậy chỉ chuyển khi có
   `/etc/htbb/da-chuyen-csdl` (tạo trong cửa sổ bảo trì, sau khi dừng game và chép xong).
3. **Lớp chặn /admin.** Web PHP đã bỏ: các lớp chặn của Apache nay là `<Location>` đặt trước `ProxyPass` sang
   Nuxt (không còn `<Directory /opt/kpah/web>`). Sổ tay bắt gỡ `DocumentRoot /opt/kpah/web` + các conf PHP cũ rồi chạy
   lại các script Apache, và `curl /admin/` phải ra 301/401.
4. **Bí mật GitHub không chép được.** Secret phải nhập lại dưới tên `HTBB_*`; trong lúc chờ, workflow dùng `KPAH_*`.
5. **Agent khác đang chạy trên cùng NovaDB** (lúc kiểm kê: 2 TeamServer ngoài stack, dùng `kpah_db_hst`/`kpah_db_bot`).
   NovaDB chỉ có MỘT user MySQL: bật lại với user `htbb` là các tiến trình đó mất CSDL. `chuyen_csdl.sh` từ chối khi
   thấy tiến trình lạ (trừ khi `--bo-qua-tien-trinh-khac`); `kpah_db_bot/_hst` chỉ đổi khi `--ca-csdl-phu`.
   Muốn hoãn đổi user: `NOVA_USER_MOI=kpah sh chuyen_csdl.sh ...` (và `NOVA_USER=kpah` cho `chay_tren_novadb.sh`).
6. **Thay đổi chưa commit của agent khác** nằm lẫn trong cùng tệp: script đọc nội dung hiện tại, chỉ thay chuỗi;
   bản gốc của mọi tệp nó ghi được lưu ở `~/Downloads/pokemon/doi_ten_sao_luu/truoc_<giờ>.tar`. `git mv` chỉ đưa
   đổi tên vào index (không commit). Lệnh/đường dẫn của agent khác còn trỏ `lib/NQSH_5h.jar` vẫn chạy nhờ symlink.
7. **Đang chạy `chay_lap.sh`** (vòng tự bật lại dùng `lib/NQSH_5h.jar`): script sửa luôn tệp này (nằm ngoài git).
8. **Máy Windows của chủ repo:** symlink git thành tệp văn bản nhỏ (vô hại); `build.bat`/`capnhat.bat` đã dùng tên mới.
   Thư mục `Desktop\KPAH-All` → README dùng ví dụ chung; `android/build.sh` vẫn tìm khoá ở thư mục cũ nếu có.
9. **`--apply` ghi chuỗi vào tài liệu lịch sử** (vd. "đã chạy trên CSDL `kpah_db_nap`" → `htbb_db_nap`): chấp nhận.

## 3. Thứ tự

Máy dev (giai đoạn B — khi có lệnh "chạy đi"):

1. Dừng stack của repo (`chay_novadb/DUNG_HAN` + kill PID trong `chay_novadb/*.pid`), không đụng tiến trình agent khác.
2. `python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py --apply` → `--kiem` (0 SOT).
3. `sh HTBB/tools/doi_ten/chuyen_csdl.sh --apply --khoi-dong` (NovaDB user `htbb`, `htbb_db`, login, `chay_lap.sh`).
4. Kiểm: test server (`javac` vào thư mục riêng, `lib/htbb-core.jar`) = mốc 65 hỏng (+0 mới); `nuxt build` (Node 22);
   `npm test` web; jar J2ME + APK (`android/build-pokemon.sh`); `KiemPokeDuLieu`; `BotTai` đăng nhập vào stack mới;
   grep lại.

VPS (chủ server, theo `vps/CHUYEN_TEN_HTBB.md`):

1. Giai đoạn 0: push code lên `main` — hạ tầng cũ vẫn chạy nhờ lối rơi; kiểm sau một lần bảo trì.
2. Giai đoạn 1 (cửa sổ bảo trì): dừng → `mysqldump kpah_db | mysql htbb_db` + đối chiếu số dòng → quyền → đổi user
   Linux → `mv /opt/kpah /opt/htbb` + symlink → unit `htbb-*` (+alias) → `touch /etc/htbb/da-chuyen-csdl` + sinh lại
   `server.ini` → logrotate/MariaDB/Apache → cron → bật + kiểm.
3. Giai đoạn 2 (≥ 7 ngày): bỏ `kpah_db`, symlink, alias, dòng `TUONG-THICH-TEN-CU`, secrets `KPAH_*`.
4. Giai đoạn 3 (tuỳ chọn): user CSDL `kpah` → `htbb`.

## 4. Đổi tên repo GitHub (chủ repo tự chạy khi muốn — script KHÔNG chạy)

```bash
gh repo rename HTBB        --repo hoanghoa0903/KPAH
gh repo rename HTBB-Web    --repo hoanghoa0903/KPAH-Web
gh repo rename htbb-client --repo hoanghoa0903/kpah-mod-pb
git -C ~/Downloads/pokemon/HTBB        remote set-url origin https://github.com/hoanghoa0903/HTBB
git -C ~/Downloads/pokemon/HTBB-Web    remote set-url origin https://github.com/hoanghoa0903/HTBB-Web
git -C ~/Downloads/pokemon/htbb-client remote set-url origin https://github.com/hoanghoa0903/htbb-client
# VPS (git clone trong /opt/htbb):
ssh root@180.93.57.30 "sudo -u htbb git -C /opt/htbb remote set-url origin git@github.com:hoanghoa0903/HTBB.git"
python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py --da-doi-repo --apply     # sửa chữ trong tài liệu
```

GitHub tự chuyển hướng tên cũ (git + web), secrets/variables/Actions đi theo repo. **Không** đổi
`kpah-server/nqsh-h2-tai` (APK NQSH-H2 cũ đọc cứng URL raw).

## 5. Quay lại

- Repo: `python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py --quay-lai ~/Downloads/pokemon/doi_ten_sao_luu/truoc_<giờ>.tar`
  (trả nội dung từng tệp, gỡ symlink, `git mv` ngược). Đã thử: kết quả giống hệt bản trước khi chạy.
- Dữ liệu dev: `sh HTBB/tools/doi_ten/chuyen_csdl.sh --quay-lai` (đổi `htbb_db*.novadb` → `kpah_db*`, ini về
  `kpah_db`/`kpah`); bản sao `.novadb` gốc ở `novadb-data/sao_luu_doi_ten_<giờ>/`. Bật lại: `NOVA_USER=kpah sh tools/pokemon/chay_tren_novadb.sh`.
- VPS: mục "Quay lại" trong `vps/CHUYEN_TEN_HTBB.md` (`kpah_db` không bị đụng; code mới chạy được trên hạ tầng cũ).
