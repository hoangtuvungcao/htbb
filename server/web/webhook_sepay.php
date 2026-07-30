<?php
/**
 * Webhook nhận thông báo biến động số dư từ SePay (https://sepay.vn) khi tài khoản
 * TPBank của bạn nhận được tiền. Cấu hình URL này trong SePay > Cấu hình > Webhooks,
 * và dán API Key bạn đặt ở đó vào SEPAY_WEBHOOK_APIKEY trong config.php.
 *
 * Luôn trả về HTTP 200 (kể cả khi không khớp đơn nào) để SePay không lặp lại gửi mãi.
 */
require_once __DIR__ . '/inc/functions.php';
header('Content-Type: application/json; charset=utf-8');

function respond(bool $ok, string $msg = ''): void
{
    http_response_code(200);
    echo json_encode(['success' => $ok, 'message' => $msg]);
    exit;
}

if (SEPAY_WEBHOOK_APIKEY === '') {
    // Chưa cấu hình SePay -> bỏ qua toàn bộ webhook, dùng xác nhận thủ công.
    respond(false, 'SePay chưa được cấu hình');
}

// Vai cau hinh Apache khong tu chuyen tiep header Authorization cho PHP, nen thu
// them REDIRECT_HTTP_AUTHORIZATION / getallheaders() truoc khi bao unauthorized.
$auth = $_SERVER['HTTP_AUTHORIZATION']
    ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION']
    ?? (function_exists('getallheaders') ? (getallheaders()['Authorization'] ?? '') : '');
if (!preg_match('/apikey\s+(\S+)/i', $auth, $m) || !hash_equals(SEPAY_WEBHOOK_APIKEY, $m[1])) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'unauthorized']);
    exit;
}

$raw = file_get_contents('php://input');
$data = json_decode($raw, true);
if (!is_array($data)) {
    respond(false, 'invalid payload');
}

$transferType = $data['transferType'] ?? '';
if ($transferType !== 'in') {
    respond(true, 'ignored (not incoming)');
}

$amount = (int)($data['transferAmount'] ?? 0);
$content = (string)($data['content'] ?? '') . ' ' . (string)($data['code'] ?? '');

if (!preg_match('/KP[A-Z0-9]{6,10}/i', $content, $m)) {
    respond(false, 'no order code found in content');
}
$maDon = strtoupper($m[0]);

$don = layDonNapMoc($maDon);
if (!$don) {
    respond(false, 'order not found: ' . $maDon);
}
if ($don['trang_thai'] !== 'cho_thanh_toan') {
    respond(true, 'already processed');
}
if ($amount < (int)$don['amount']) {
    respond(false, 'amount too low');
}

$st = dbGame()->prepare("UPDATE tob_nap_moc SET trang_thai='da_thanh_toan', paid_at=NOW() WHERE id = ? AND trang_thai='cho_thanh_toan'");
$st->execute([$don['id']]);

respond(true, 'confirmed ' . $maDon);
