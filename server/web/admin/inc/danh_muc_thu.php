<?php
/**
 * DANH MUC THU NUOI (PET/PETV) cho o chon phan thuong giftcode.
 *
 * TEP NAY DUOC SINH RA, DUNG SUA TAY.
 *   Nguon   : mang real.Map.petTemplate (src/real/Map.java, loadPetTemplate)
 *   Sinh lai: java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhDanhMucThu
 *
 * KHOA nay LA CHI SO MANG, khong phai id vat pham - Char.createPet() va
 * createPetVinhVien() doc thang Map.petTemplate[idpet] khong kiem bien. Xem
 * tools/SinhDanhMucThu.java de biet vi sao phai sinh dung 0..29 tu chinh mang do.
 *
 * test/KiemDanhMucThu.java doi chieu tep nay voi mang Java, nen tep lech la
 * bo kiem do chu khong am tham sai.
 */

/** @return array<int,string> chi so 0..29 => ten thu (KHONG phai id vat pham) */
function danhMucThu(): array
{
    return [
        0 => 'Lồng đèn xếp',
        1 => 'Thanh tước',
        2 => 'Cánh cam',
        3 => 'Dơi quỷ',
        4 => 'Tim bay',
        5 => 'Lồng đèn hoa sen',
        6 => 'Lồng đèn ngôi sao',
        7 => 'Pet Hoa tuyết',
        8 => 'Pet Giáp Ngọ',
        9 => 'Pet Armadillo băng',
        10 => 'Pet Armadillo sét',
        11 => 'Pet Armadillo độc',
        12 => 'Lồng đèn cá chép',
        13 => 'Chim cánh cụt',
        14 => 'Pet ất mùi',
        15 => 'Ngôi sao băng',
        16 => 'Phượng hoàng lửa',
        17 => 'Phượng hoàng băng',
        18 => 'Lồng đèn',
        19 => 'Lồng đèn Ngôi sao',
        20 => 'Lồng đèn Trăng khuyết',
        21 => 'Lồng đèn Rồng xanh',
        22 => 'Khỉ vàng',
        23 => 'Cú',
        24 => 'Rồng',
        25 => 'Thỏ bay',
        26 => 'Dơi',
        27 => 'Đại bàng',
        28 => 'Lồng đèn hoa sen',
        29 => 'Bạch Thử',
    ];
}
