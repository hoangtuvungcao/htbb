# KPAH — Khí Phách Anh Hùng (private server)

Private server cho game mobile MMORPG "Khí Phách Anh Hùng". Gồm 3 phần chạy độc lập:

- **Java game server** — `src/` (packages `real`, `data`, `server`, `com.kpah`,
  `ThanKhi`, `util`, `Constant`, `io`), build bằng NetBeans/Ant (`build.xml`,
  `nbproject/`). Jar deploy thật là `KPAH2_patched.jar` ở gốc repo.
- **Client C#** — `Client/` (bản port từ client J2ME/MIDP gốc).
- **Web PHP** — `web/` (đăng nhập, nạp tiền, PDO + MySQL). Xem `web/README.md`.

Tài nguyên map/sprite nằm ở `map/` và `cMap/`. Thư viện `.jar` bên thứ ba nằm ở
`libs/` (dùng bởi build NetBeans/Ant chính thức) và `lib/` (dùng khi compile tay
một file bằng `javac`, xem hướng dẫn build ở dưới) — cả hai đều đang được dùng,
không phải trùng lặp thừa.

## Thư mục

| Thư mục | Nội dung |
|---|---|
| `src/` | Source Java game server |
| `Client/` | Source client C# |
| `web/` | Website PHP (login, nạp tiền, admin) |
| `sql/` | Script SQL: fix dữ liệu, migrate, dump mốc (fix/migrate/backup) |
| `vps/` | Script deploy & vận hành trên VPS (systemd service, backup DB, deploy-auto) |
| `map/`, `cMap/` | Tài nguyên bản đồ, sprite |
| `loginServer/` | Server đăng nhập riêng |
| `tools/` | Tool dev rời (cắt sprite, tạo icon, xem sprite...) |
| `docs/` | Tài liệu tham khảo phụ (bảng id thuộc tính...) |
| `nbproject/` | Project NetBeans (Ant build) |

## Build & vận hành

- Hướng dẫn build/setup VPS đầy đủ: [HUONG_DAN_BUILD_VPS.md](HUONG_DAN_BUILD_VPS.md)
- Hướng dẫn VPS qua FRP: [HUONG_DAN_VPS_FRP.md](HUONG_DAN_VPS_FRP.md)
- Setup website: [web/README.md](web/README.md)

**Lưu ý quan trọng**: mỗi lần push lên nhánh `main` sẽ tự động deploy lên VPS sản
xuất (`.github/workflows/deploy.yml`). Đổi `KPAH2_patched.jar`, `lib/`, `libs/`,
`map/`, `cMap/`, hoặc `loginServer/` sẽ khiến server live tự restart (có cảnh báo
60 giây trong game trước khi tắt) — xem `vps/deploy-auto.sh`.
