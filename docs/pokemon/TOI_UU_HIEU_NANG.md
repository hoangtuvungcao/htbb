# Tối ưu hiệu năng máy chủ game — thread, CPU, RAM, CSDL

Ngày 18/10/2026, nhánh `pokemon/chuyen-doi`. Bổ sung cho [SAN_SANG_PRODUCTION.md](SAN_SANG_PRODUCTION.md) mục 4
(đo tải trước đó). **Chưa commit, chưa deploy.**

> Đang đo bổ sung — bảng mục 1 sẽ cập nhật số 150/300 bot (đợt B) khi chạy xong.

## Tóm tắt

| | Trước | Sau |
|---|---|---|
| Thread khi rỗng | **3.086** | **190** |
| CPU khi rỗng (trung bình / p95, % một lõi) | 45,8 / 74,4 | **6,8 / 12,8** |
| Thread ở 50 bot | 3.194 | 284 |
| CPU ở 50 bot (trung bình / p95) | 49,6 / 73,2 | **12,0 / 21,0** |
| Đăng nhập p95 ở 50 bot (đợt B, nhiều IP) | 1.399 ms | **114 ms** |

Chi tiết và cách đo bên dưới.
