<?php
/**
 * Chặn liệt kê thư mục.
 *
 * VÌ SAO CẦN: `/var/www/html/kpah` là symlink tới `/opt/kpah/web`, và Apache trên
 * VPS bật `Options Indexes` (apache2.conf, khối `<Directory /var/www/>`). Không có
 * tệp index thì mở thư mục này qua trình duyệt sẽ ra danh sách đầy đủ mọi tệp bên
 * trong — lộ nguyên cây thư mục cho người ngoài biết chính xác cần dò cái gì.
 *
 * KHÔNG chặn được bằng `.htaccess`: cùng khối cấu hình đó đặt `AllowOverride None`
 * nên tệp .htaccess bị bỏ qua hoàn toàn. Cách chắc chắn mà vẫn nằm trong repo (tự
 * đi theo mỗi lần deploy) là đặt một tệp index để Apache phục vụ nó thay cho danh
 * sách.
 */
http_response_code(403);
exit("Khong co gi o day.\n");
