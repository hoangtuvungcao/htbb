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
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
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
