<?php
/**
 * Thoát khỏi khu quản trị.
 *
 * Chỉ xoá phiên ADMIN, không đụng tới $_SESSION['user'] — người đang đăng nhập
 * trang công khai bằng cùng trình duyệt vẫn ở nguyên đó.
 */
require_once __DIR__ . '/inc/boot.php';

if (adminUser()) {
    ghiNhatKy('dang_xuat', adminName(), 'rời trang quản trị');
}

unset($_SESSION['kpah_admin']);
session_regenerate_id(true);

setFlash('success', 'Đã thoát khỏi khu quản trị.');
header('Location: index.php');
exit;
