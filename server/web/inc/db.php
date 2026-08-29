<?php
require_once __DIR__ . '/../config.php';

/**
 * Trả về PDO kết nối tới 1 database, cache theo tên DB.
 */
function db(string $name): PDO
{
    static $pool = [];
    if (isset($pool[$name])) {
        return $pool[$name];
    }
    $dsn = sprintf('mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4', DB_HOST, DB_PORT, $name);

    // Login server có file cấu hình riêng và CÓ THỂ dùng người dùng database khác
    // với game server. config.php chỉ đặt DB_ACCOUNT_USER khi hai bên thật sự
    // khác nhau; lúc đó database tài khoản phải dùng đúng cặp của login server,
    // nếu không sẽ nhận "Access denied" dù mật khẩu game server hoàn toàn đúng.
    $nguoiDung = ($name === DB_ACCOUNT && defined('DB_ACCOUNT_USER')) ? DB_ACCOUNT_USER : DB_USER;
    $matKhau   = ($name === DB_ACCOUNT && defined('DB_ACCOUNT_USER')) ? DB_ACCOUNT_PASS : DB_PASS;

    $pdo = new PDO($dsn, $nguoiDung, $matKhau, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
    $pool[$name] = $pdo;
    return $pdo;
}

/** DB tài khoản (team_user) */
function dbAccount(): PDO { return db(DB_ACCOUNT); }

/** DB game (nhân vật, kích hoạt, BXH) */
function dbGame(): PDO { return db(DB_GAME); }
