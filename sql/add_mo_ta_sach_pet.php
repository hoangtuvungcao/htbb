<?php
/*
 * Them mo ta cho potion 111 (Sach ky nang pet) - hien "Thong tin" khi bam vao
 * o trong tui binh dang trong khi hien gio.
 *
 * Chay thu (khong ghi gi):  php add_mo_ta_sach_pet.php
 * Chay that:                php add_mo_ta_sach_pet.php --apply
 */
$APPLY = in_array('--apply', $argv, true);

$db = new mysqli('localhost', 'root', '', 'kpah_db');
if ($db->connect_error) {
    fwrite(STDERR, "Khong ket noi duoc DB: " . $db->connect_error . "\n");
    exit(1);
}
$db->set_charset('utf8mb4');

$ten = "Sách kỹ năng pet\nDùng để pet học kỹ năng.";

echo $APPLY ? "=== GHI THAT ===\n" : "=== CHAY THU ===\n";
echo "Ten moi se dat cho potion 111:\n---\n$ten\n---\n\n";

if ($APPLY) {
    $st = $db->prepare("UPDATE data_potion SET name=? WHERE id=111");
    $st->bind_param('s', $ten);
    $st->execute();
    echo "Da cap nhat ({$st->affected_rows} dong)\n";
    $st->close();
}

$r = $db->query("SELECT id, name, name2, idImage FROM data_potion WHERE id=111");
$row = $r->fetch_assoc();
echo "Trang thai hien tai trong DB:\n";
print_r($row);

$db->close();
