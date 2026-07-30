<?php
require_once __DIR__ . '/inc/functions.php';
header('Content-Type: application/json; charset=utf-8');

if (!currentUser()) {
    http_response_code(401);
    echo json_encode(['error' => 'not_logged_in']);
    exit;
}

$maDon = (string)($_GET['ma_don'] ?? '');
$don = $maDon !== '' ? layDonNapMoc($maDon) : null;

if (!$don || (int)$don['userID'] !== (int)currentUser()['id']) {
    http_response_code(404);
    echo json_encode(['error' => 'not_found']);
    exit;
}

echo json_encode(['trang_thai' => $don['trang_thai']]);
