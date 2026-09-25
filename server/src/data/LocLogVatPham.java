package data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bo loc cho bang tob_log_all_item: CHI ghi nhung loai (aclog) nam trong GIU.
 *
 * VI SAO CAN. Do tren ban sao production ngay 02/09/2026: bang co 5,79 trieu dong (550 MB),
 * nhung hon 5,6 trieu la rac - rieng 'luckGOLD' (mo hop may man) 3,2 trieu dong trong MOT
 * ngay, 'bossdie' 1,6 trieu, 'joinLucky' 460 nghin, 'truxu' 240 nghin... Ma nguon co gan 300
 * loai aclog, phan lon chi ghi "nguoi choi vua lam X" - khong ai doc lai bao gio, nhung lam
 * bang phinh, hang doi Logdata.query xep hang, va bat vps/don-log.sh xoa hang trieu dong moi
 * dem tren mot bang MyISAM khoa toan bang khi xoa.
 *
 * TIEU CHI GIU (yeu cau ngay 02/09/2026): chi con log phuc vu dung ba viec
 *   1. Truy vat pham hiem bi mat hay doi chu: nghien/ban tu dong/admin xoa (matdo), trao doi,
 *      ban gian hang, vut ra dat, het han, tich thu, thao do khi hanh trang day, chuyen giua
 *      tui va hanh trang, mo trung tao pet (itemtrungpet, them 13/09/2026).
 *   2. Loi va nghi van gian lan: dung danh sach web/admin/chandoan.php dang dem, cong cac cho
 *      ghi "hack..." trong ma nguon.
 *   3. Tai san tang bat thuong: nap tien, moc nap, giftcode, moi lan nhan xu, chuyen luong,
 *      len cap (kem exp truoc/sau), anh chup lv/exp/xu dinh ky, thuong top, luong bang hoi,
 *      thang cuoc.
 * Vi la danh sach GIU (allow-list) nen loai moi them vao ma nguon MAC DINH KHONG ghi - muon
 * ghi phai them vao day. Do la co y: mac dinh "ghi" thi bang lai phinh nhu cu.
 *
 * CACH DUNG. Ca bay ham ghi vao tob_log_all_item trong Database (saveOrtherLog va sau ham
 * saveLog* chuyen biet) deu hoi boQua(aclog) truoc khi ghep cau INSERT. Bo qua la bo ca dong
 * trong bang lan dong trong UserLogger - hai cho do cung mot nguon.
 *
 * Danh sach nay duoc chep lai trong vps/don-log.sh (bien LOAI_GIU) de xoa not dong cu tren
 * production; test/KiemLocLogVatPham.java ep hai ban phai khop, ep moi ten o day van con la
 * aclog that trong ma nguon (doi ten la bo loc chet am tham), va ep moi loai chandoan.php
 * dang dem deu co mat o day.
 */
public final class LocLogVatPham {

    public static final Set<String> GIU = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            // --- 1. Vat pham roi tay / doi chu ---
            "matdo",                    // nghien, ban tu dong, admin xoa, server xoa luc nap (saveLogMatVatPham)
            "trade",                    // trao doi giua hai nguoi choi (saveLogTradeItem)
            "sitemu",                   // ban trong gian hang nguoi choi (saveLogSellItem)
            "delI",                     // vut trang bi ra dat
            "timeout",                  // vat pham het han, server xoa
            "inven2tui", "tui2inven",   // chuyen giua hanh trang va tui - "do bien mat" hoa ra o tui kia
            "uwea",                     // thao do khi hanh trang day
            "uride",                    // xuong ngua khi hanh trang day
            "XOAPET",                   // xoa pet
            "XOATC",                    // xoa thoi trang
            "itemtrungpet",             // mo trung tao pet (Map.java) - them 13/09/2026 de
                                        // truy duoc cac ca "mo trung xong mat pet" (loi dua
                                        // chay Char.doCreatePet, da va bang synchronized)
            "tichthu", "tichthuchoi", "logchoi", "thugay", // admin / server tich thu
            "downlv",                   // bi tut cap
            "caitaocaitrang",           // cai tao cai trang: tru diem xai/nap, doi % Tinh Hoa (real/CaiTaoCaiTrang)
            "hoahinhpet",               // hoa hinh pet Lan: tru binh 268, cap Lan, va luc het han (data/Pet)
            // --- 2. Loi va nghi van gian lan (khop web/admin/chandoan.php + NghiVanGianLan) ---
            "loiinvent",                // luu hanh trang that bai - server tu khoa nhan vat
            "loisave",                  // luu nhan vat tu dong that bai
            "hackc", "hackR", "hackbitem", "hackchonchar", // cac nghi van gian lan
            "hcm",                      // hack chuyen map
            "ngoairange", "ngoairange1", "stGive", // giao the ngoai tam / vi tri giao the
            "ffbug", "hklbug",          // loi da biet bi khai thac
            "scamchat",                 // admin cam chat
            "iftrung",                  // kiem tra so trung khi quay so
            // --- 3. Tai san tang bat thuong ---
            "naptien",                  // nap tien (saveLogNapMoney)
            "nhanlk",                   // nhan luong khoa tu nap
            "mocNap", "mocnap",         // qua moc nap
            "nhanluong",                // chuyen luong giua nguoi choi
            "nhanxu",                   // moi lan nhan xu, kem so du va nguon
            "logxu",                    // anh chup xu / xu mat / xu tim luc thoat
            "autosave",                 // anh chup lv / exp / xu dinh ky
            "lenlv", "lenlevel",        // len cap kem exp truoc va sau (lenlevel la lenh admin)
            "putGiftCode", "gcode",     // nhap giftcode
            "quatop", "quatopct", "top100", "tophl", // thuong dua top
            "thuongchiemthanh",         // thuong chiem thanh: hang chuc nghin luong
            "salaryC", "salaryCN",      // luong bang hoi
            "txs", "xoso",              // thang cuoc
            "idruongmm",                // admin doi id ruong may man
            "tratieu", "motieu",        // hoan thanh van tieu (CharSuKien.doResponseTieu): ghi ro exp/
                                        // do/luong/bot xanh lam/ngoc nhan duoc. Them 16/09/2026 - truoc do
                                        // KHONG nam trong GIU nen saveOrtherLog() luon tra ve som, chua
                                        // tung ghi xuong DB (giong ly do them "khanglan" cung ngay), nen
                                        // khong tra cuu lai duoc nguoi choi da nhan gi khi bi bao "khong
                                        // nhan duoc gi ca". "motieu" = bi cuop tieu, "tratieu" = tra tieu binh thuong.
            "thaden",                   // Trung Thu 2026: THA DEN THANH CONG (tra luong hoac xu, khong tinh
                                        // dot den mien phi - RealController.doDotDen). Them 25/09/2026 theo
                                        // yeu cau nguoi dung "them log tha den" - truoc do dung chung aclog
                                        // "dotden" (KHONG nam trong GIU nen chua tung ghi xuong DB), tach
                                        // rieng ten de KHONG keo theo dot den mien phi (tan suat cao hon
                                        // nhieu, moi 3 phut/nguoi) vao cung mot loai duoc giu.
            // --- 4. Tieu nguyen lieu Than Khi (kho gem rieng, khong qua hanh trang) ---
            "taothankhi",               // tao Than Khi: ghi ten vat pham, cap/che do, nguyen lieu da tieu (ThoRenThanBi.CreateThanKhi)
            "nangcapthankhi",           // nang cap Than Khi: ghi +cu -> +moi, ti le, thanh/bai, nguyen lieu da tieu (ThoRenThanBi.UpgradeThanKhi)
            // --- 5. Che tao trang bi thuong (vu khi/giap/nhan/day chuyen/ngoc...) ---
            "chetaotrangbi",            // xac nhan tao do o Map.doCreateNewItem: nguyen lieu da tieu (gemUse) + vat pham ra doi;
                                        // TRUOC la aclog "tao" chung, bi loai bo 02/09/2026 vi la nguon rac lon nhat -
                                        // doi ten rieng de giu duoc CHI dong nay ma khong an rac "tao" khac
            // --- 6. Hop nguyen lieu (5 vien cap thap -> 1 vien cap cao, NPC map 40/41/42) ---
            "hopnguyenlieu",            // MapDoMenu.java (3 NPC: -10/-11/-12): ghi so luong hop + ten nguyen lieu ket qua;
                                        // TRUOC la aclog "hop" chung, cung bi loai bo 02/09/2026 nhu "tao" - doi ten rieng
                                        // vi day la buoc TRUNG GIAN de lam nguyen lieu bac cao (vd Ngoc cap 5 -> cap 6)
                                        // truoc khi dem che trang bi - thieu dong nay thi khong biet nguyen lieu bac thap
                                        // co bi hop len bac cao roi moi tieu hay khong
            // --- 7. PK trong Thanh: mien nhiem hieu ung lan (Bang/Set/Doc/Lua) ---
            "khanglan",                 // CharTrangBi.checkNewEffectItem: nguoi choi bi lan nham vao MIEN NHIEM
                                        // (qua xa cap / dung bi khang / dang khoa cooldown hetHanLan). Them
                                        // 16/09/2026 - truoc do KHONG nam trong GIU nen saveOrtherLog() luon
                                        // tra ve som, dong nay CHUA TUNG duoc ghi xuong DB dau, du code goi no.
            // --- 8. Tru xu (doi xung voi "nhanxu") ---
            "truxu", "truthieu"         // Char.subXu(): moi lan tru xu (mua do/nang cap/phi...) va truong hop
                                        // tru nhieu hon so dang co. TUNG bi loai bo 02/09/2026 (nam trong
                                        // nhom rac 5,6 trieu dong luc do) - bat lai 16/09/2026 theo yeu cau
                                        // nguoi dung "log chi tiet hon" cho xu; day la tag TAN SUAT CAO NHAT
                                        // dang bat (gan moi hanh dong ton xu deu goi subXu), nhung don-log.sh
                                        // van tu xoa dong qua GIU_NGAY=15 ngay (xem BANG_LOG o tren) nen khong
                                        // phinh vo han nhu truoc.
    )));

    /** true = KHONG ghi. Chuoi la, null, hay khong nam trong GIU deu bi bo. */
    public static boolean boQua(final String aclog) {
        return aclog == null || !GIU.contains(aclog);
    }

    private LocLogVatPham() {
    }
}
