<?php
/*
 * ============================================================
 *  Chuyen "Sach ky nang pet" sang POTION 111
 *
 *  Truoc:  item data_item 722 (chiem 1 o hanh trang)
 *          + gem 269 (ban xep chong chay song song)
 *  Sau:    potion 111 (xep chong trong tui binh, mot duong duy nhat)
 *
 *  CHAY KHI GAME SERVER DA TAT. Server dang chay giu du lieu nhan vat
 *  trong RAM va se ghi de len bang tob_char luc luu/thoat.
 *
 *  Chay thu (khong ghi gi):   php migrate_sach_pet_to_potion.php
 *  Chay that:                 php migrate_sach_pet_to_potion.php --apply
 * ============================================================
 */

$APPLY     = in_array('--apply', $argv, true);
$POTION_ID = 111;   // vi tri trong CSV cot tob_char.potion (162 truong)
$GEM_ID    = 269;   // vi tri trong CSV cot tob_gem_new.soluong / slock
$ITEM_ID   = '722'; // template id trong data_item
$N_POTION  = 162;

$db = new mysqli('localhost', 'root', '', 'kpah_db');
if ($db->connect_error) {
    fwrite(STDERR, "Khong ket noi duoc DB: " . $db->connect_error . "\n");
    exit(1);
}
$db->set_charset('utf8mb4');

echo $APPLY ? "=== CHE DO GHI THAT (--apply) ===\n\n"
            : "=== CHAY THU, KHONG GHI GI (them --apply de ghi that) ===\n\n";

/*
 * Bo cac o chua item 722 ra khoi mot cot hanh trang.
 * Cot la cac ban ghi noi bang '>', moi ban ghi dang "<dbinfo>|<attribute>",
 * trong do dbinfo la CSV va truong thu 2 (chi so 1) la template id.
 * Chi bo o nao that su la item (co dau '|' va du so truong) de khong dinh
 * nham phan model trang bi duoc noi vao cuoi chuoi.
 */
function boSachRaKhoiCot($chuoi, &$soQuyenBo)
{
    $soQuyenBo = 0;
    if ($chuoi === null || $chuoi === '') {
        return $chuoi;
    }
    $giu = [];
    foreach (explode('>', $chuoi) as $o) {
        $vachDung = strpos($o, '|');
        if ($vachDung !== false) {
            $truong = explode(',', substr($o, 0, $vachDung));
            if (count($truong) >= 15 && isset($truong[1]) && $truong[1] === '722') {
                $soQuyenBo++;
                continue;
            }
        }
        $giu[] = $o;
    }
    return implode('>', $giu);
}

function layTruong($csv, $chiSo)
{
    if ($csv === null || $csv === '') {
        return 0;
    }
    $t = explode(',', $csv);
    return isset($t[$chiSo]) ? (int) $t[$chiSo] : 0;
}

function datTruong($csv, $chiSo, $giaTri)
{
    $t = explode(',', $csv);
    if (!isset($t[$chiSo])) {
        return null;
    }
    $t[$chiSo] = (string) $giaTri;
    return implode(',', $t);
}

/* ---------- 1. Dinh nghia potion 111 ---------- */
echo "1) data_potion id=111 -> 'Sách kỹ năng pet'\n";
if ($APPLY) {
    $st = $db->prepare(
        "UPDATE data_potion
            SET idImage=95, delay=0, isTrade=1,
                name='Sách kỹ năng pet', name2='sachkynangpet',
                price=0, recovered=0
          WHERE id=?"
    );
    $st->bind_param('i', $POTION_ID);
    $st->execute();
    echo "   da cap nhat ({$st->affected_rows} dong)\n";
    $st->close();
}

/* ---------- 2. Gop so luong ve potion 111 ---------- */
echo "\n2) Gop sach tu item 722 + gem 269 vao potion 111\n";

$gem = [];
$r = $db->query("SELECT owner, soluong, slock FROM tob_gem_new");
while ($h = $r->fetch_assoc()) {
    $gem[(int) $h['owner']] = $h;
}
$r->free();

$r = $db->query("SELECT id, charname, potion, inven, bag, tuido FROM tob_char ORDER BY id");
$tongSach = 0;
$loi      = 0;
while ($c = $r->fetch_assoc()) {
    $id = (int) $c['id'];

    $truongPotion = explode(',', (string) $c['potion']);
    if (count($truongPotion) !== $N_POTION) {
        printf("   [BO QUA] %-12s cot potion co %d truong, can %d\n",
               $c['charname'], count($truongPotion), $N_POTION);
        $loi++;
        continue;
    }

    $tuGem = 0;
    if (isset($gem[$id])) {
        $tuGem = layTruong($gem[$id]['soluong'], $GEM_ID)
               + layTruong($gem[$id]['slock'], $GEM_ID);
    }

    $boInven = $boBag = $boTuido = 0;
    $invenMoi = boSachRaKhoiCot($c['inven'], $boInven);
    $bagMoi   = boSachRaKhoiCot($c['bag'],   $boBag);
    $tuidoMoi = boSachRaKhoiCot($c['tuido'], $boTuido);
    $tuItem   = $boInven + $boBag + $boTuido;

    $cu  = (int) $truongPotion[$POTION_ID];
    $moi = $cu + $tuGem + $tuItem;

    if ($tuGem === 0 && $tuItem === 0 && $cu === $moi) {
        printf("   %-12s khong co sach, bo qua\n", $c['charname']);
        continue;
    }

    printf("   %-12s potion111: %d -> %d   (gem %d + item %d)\n",
           $c['charname'], $cu, $moi, $tuGem, $tuItem);
    $tongSach += $moi - $cu;

    if ($APPLY) {
        $truongPotion[$POTION_ID] = (string) $moi;
        $potionMoi = implode(',', $truongPotion);

        $st = $db->prepare(
            "UPDATE tob_char SET potion=?, inven=?, bag=?, tuido=? WHERE id=?"
        );
        $st->bind_param('ssssi', $potionMoi, $invenMoi, $bagMoi, $tuidoMoi, $id);
        $st->execute();
        $st->close();
    }
}
$r->free();
echo "   Tong so quyen chuyen sang potion: $tongSach\n";

/* ---------- 3. Xoa sach o kho gem ---------- */
echo "\n3) Dua stack gem 269 ve 0\n";
foreach ($gem as $owner => $h) {
    $sl = layTruong($h['soluong'], $GEM_ID);
    $lk = layTruong($h['slock'], $GEM_ID);
    if ($sl === 0 && $lk === 0) {
        continue;
    }
    printf("   owner %-4d soluong %d -> 0, slock %d -> 0\n", $owner, $sl, $lk);
    if ($APPLY) {
        $slMoi = datTruong($h['soluong'], $GEM_ID, 0);
        $lkMoi = datTruong($h['slock'], $GEM_ID, 0);
        if ($slMoi === null || $lkMoi === null) {
            echo "   [BO QUA] owner $owner: CSV gem khong du truong\n";
            $loi++;
            continue;
        }
        $st = $db->prepare("UPDATE tob_gem_new SET soluong=?, slock=? WHERE owner=?");
        $st->bind_param('ssi', $slMoi, $lkMoi, $owner);
        $st->execute();
        $st->close();
    }
}

/* ---------- 4. Sua giftcode ---------- */
echo "\n4) Sua giftcode con tro toi item 722 / gem 269\n";
$r = $db->query("SELECT id, giftcode, item FROM giftcode WHERE item LIKE '%722%' OR item LIKE '%269%'");
while ($g = $r->fetch_assoc()) {
    $phanMoi = [];
    foreach (explode(',', $g['item']) as $p) {
        $t = explode(':', $p);
        if ($t[0] === 'DB' && isset($t[1]) && $t[1] === $ITEM_ID) {
            $phanMoi[] = 'PT:' . $POTION_ID . ':' . $t[2];
        } elseif ($t[0] === 'ITEM' && isset($t[1]) && $t[1] === $ITEM_ID) {
            $phanMoi[] = 'PT:' . $POTION_ID . ':' . $t[2];
        } elseif ($t[0] === 'GEM' && isset($t[1]) && (int) $t[1] === $GEM_ID) {
            $phanMoi[] = 'PT:' . $POTION_ID . ':' . $t[2];
        } else {
            $phanMoi[] = $p;
        }
    }
    $moi = implode(',', $phanMoi);
    printf("   [%s] %s\n        -> %s\n", $g['giftcode'], $g['item'], $moi);
    if ($APPLY) {
        $st = $db->prepare("UPDATE giftcode SET item=? WHERE id=?");
        $st->bind_param('si', $moi, $g['id']);
        $st->execute();
        $st->close();
    }
}
$r->free();

/* ---------- 5. Xoa item 722 ---------- */
echo "\n5) Xoa data_item id=722\n";
$conSot = [];
foreach (['inven', 'bag', 'tuido', 'equip'] as $cot) {
    $q = $db->query("SELECT COUNT(*) n FROM tob_char WHERE IFNULL($cot,'') LIKE '%,722,%'");
    $n = (int) $q->fetch_assoc()['n'];
    $q->free();
    if ($n > 0) {
        $conSot[] = "$cot=$n";
    }
}
if ($conSot && $APPLY) {
    echo "   [DUNG LAI] van con nhan vat giu item 722: " . implode(', ', $conSot) . "\n";
    echo "   Khong xoa data_item de tranh loi thieu template luc load.\n";
    $loi++;
} else {
    if ($conSot) {
        echo "   (chay thu) sau khi ghi that thi cac cot nay se sach: " . implode(', ', $conSot) . "\n";
    }
    if ($APPLY) {
        $db->query("DELETE FROM data_item WHERE id=722");
        echo "   da xoa ({$db->affected_rows} dong)\n";
    }
}

echo "\n=== " . ($APPLY ? "XONG" : "KET THUC CHAY THU") . ", so loi: $loi ===\n";
$db->close();
exit($loi > 0 ? 1 : 0);
