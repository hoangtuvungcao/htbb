<?php
if (PHP_SAPI !== 'cli') { http_response_code(403); exit; }
require_once __DIR__ . '/../inc/lenh_nhan_vat.php';
function check($ok) { if (!$ok) throw new RuntimeException('Test failed'); }
check(tachTenGiamLevel(" Alice\r\nbob,ALICE;charlie\n") === ['ALICE', 'bob', 'charlie']);
check(tachTenGiamLevel('one') === ['one']);
foreach ([' ,; ', str_repeat('a', 51), implode(',', range(1, 101))] as $bad) {
    $rejected = false;
    try { tachTenGiamLevel($bad); } catch (InvalidArgumentException $e) { $rejected = true; }
    check($rejected);
}
check(count(tachTenGiamLevel(implode(',', range(1, 100)))) === 100);
check(moTaYeuCauLenh(['loai' => LENH_LOAI_GIAM_CAP_DO, 'cap_do' => 10]) === 'giảm 10 level');
echo "PASS: name separators, duplicates, empty/oversized lists, limits and command description\n";