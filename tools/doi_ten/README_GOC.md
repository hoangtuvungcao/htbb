# htbb — Huyền Thoại Bảo Bối (monorepo)

Private server + client + website của game nhập vai Pokémon **Huyền Thoại Bảo Bối (HTBB)**.
Gộp từ 3 repo cũ, giữ nguyên lịch sử git (`git log --follow <tệp>` đi qua được lúc gộp).

| Thư mục | Nội dung | Chạy ở (cwd) | Lệnh chính |
|---|---|---|---|
| `server/` | game server + login server (Java 8): `src/`, `test/`, `lib/`, `map/`, `cMap*/`, `loginServer/`, `htbb-server.jar` | `server/` | test: xem `.github/workflows/server-test.yml`; Windows: `build.bat`, `test.bat` |
| `client_jar/` | client J2ME (MIDP) + `shared/mod` (mod dùng chung với APK) | `client_jar/` | `MAY_CHU=127.0.0.1:19129 HTBB_ASSETS=pc sh client_jar/build.sh 97` |
| `client_apk/` | Android: `pokemon/` = APK HTBB (sinh từ code jar qua `port-j2me.py`); `app/` = module NQSH-H2 cũ | `client_apk/` | `MAY_CHU=10.0.2.2:19129 sh client_apk/build-pokemon.sh` |
| `client_pc_ios/` | Unity 6000.5.9f1 (PC Windows, iOS). `ClientUnity/` = dự án Unity cũ (còn test server soi vào) | — | Unity `-projectPath client_pc_ios`; CI: `client-*.yml` |
| `web/` | website + trang quản trị Nuxt 4 (Node 22) — website duy nhất (web PHP cũ đã bỏ) | `web/` | `cd web && npm ci && npm run build && npm test` |
| `sql/` | toàn bộ SQL + migration (`sql/README.md`); `sql/htbb_day_du.sql` = CSDL đầy đủ | — | |
| `docs/` | tài liệu (`docs/pokemon/` thiết kế game, `docs/client/` client) | — | |
| `tools/` | công cụ sinh dữ liệu (`tools/pokemon/`), phát hành client (`tools/phat-hanh/`), đổi tên (`tools/doi_ten/`) | **gốc repo** | `python3 -I tools/pokemon/<x>.py`; `sh tools/pokemon/chay_tren_novadb.sh` |
| `vps/` | deploy VPS: systemd `htbb-*.service`, `deploy-auto.sh`, `server.ini.tmpl`, sao lưu | VPS: `/opt/htbb` = bản checkout repo này | `vps/CHUYEN_TEN_HTBB.md` |

Quy ước đường dẫn: trong tài liệu và công cụ, đường dẫn viết **từ gốc repo** (`server/src/...`, `docs/pokemon/...`).
Test server chạy ở `server/` nên đọc `../docs`, `../sql`, `../vps`.

Workflow GitHub Actions ở `.github/workflows/` có tiền tố thành phần (`server-`, `web-`, `client-`) và lọc theo đường dẫn.
`server-deploy.yml` chỉ chạy khi biến repo `HTBB_VPS_MONOREPO=1` (bật sau khi VPS đã chuyển sang bản checkout này).
