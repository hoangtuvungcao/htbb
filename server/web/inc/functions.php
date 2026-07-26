<?php
require_once __DIR__ . '/db.php';

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

/** Escape HTML để in ra an toàn. */
function e(?string $s): string
{
    return htmlspecialchars((string)$s, ENT_QUOTES, 'UTF-8');
}

/** Tạo/đọc CSRF token. */
function csrfToken(): string
{
    if (empty($_SESSION['csrf'])) {
        $_SESSION['csrf'] = bin2hex(random_bytes(16));
    }
    return $_SESSION['csrf'];
}

function csrfCheck(?string $token): bool
{
    return is_string($token) && !empty($_SESSION['csrf']) && hash_equals($_SESSION['csrf'], $token);
}

/** Kiểm tra username: 4-20 ký tự, chỉ a-z 0-9 (khớp với server). */
function validUsername(string $u): bool
{
    return (bool)preg_match('/^[a-z0-9]{' . USERNAME_MIN . ',' . USERNAME_MAX . '}$/', $u);
}

function validPassword(string $p): bool
{
    $len = strlen($p);
    return $len >= PASSWORD_MIN && $len <= PASSWORD_MAX;
}

/** Tài khoản đang đăng nhập trên website (nếu có). */
function currentUser(): ?array
{
    return $_SESSION['user'] ?? null;
}

function requireLogin(): void
{
    if (!currentUser()) {
        header('Location: login.php');
        exit;
    }
}

/** Flash message qua session. */
function setFlash(string $type, string $msg): void
{
    $_SESSION['flash'] = ['type' => $type, 'msg' => $msg];
}

function takeFlash(): ?array
{
    if (!empty($_SESSION['flash'])) {
        $f = $_SESSION['flash'];
        unset($_SESSION['flash']);
        return $f;
    }
    return null;
}

/** Tài khoản đã kích hoạt (có trong 5h_active) chưa? */
function isActivated(string $username): bool
{
    try {
        $st = dbGame()->prepare('SELECT 1 FROM `5h_active` WHERE username = ? LIMIT 1');
        $st->execute([$username]);
        return (bool)$st->fetchColumn();
    } catch (Throwable $ex) {
        return false;
    }
}

/** Kích hoạt tài khoản: thêm vào 5h_active (khớp hàm insert_active của server). */
function activateAccount(int $userId, string $username): bool
{
    if (isActivated($username)) {
        return true;
    }
    try {
        $st = dbGame()->prepare('INSERT INTO `5h_active` (userID, username) VALUES (?, ?)');
        return $st->execute([$userId, $username]);
    } catch (Throwable $ex) {
        return false;
    }
}
