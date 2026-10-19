/**
 * Ba loi that BotChucNang bat duoc (19/10/2026): ket thuc chiem Gym, diem vao lang roi vao o chan, cau hinh dai bi cat.
 *
 * Ket thuc chiem Gym (19/10/2026, BotChucNang: chiem_thanh roi cho bi chan "trong thoi gian chiem Gym"):
 *
 * - GM over0 / over1 / over2 tung vung: truoc day MapTown.gameOverByAdmin la MOT so nguyen nen lenh sau ghi de
 *   lenh truoc (va "ktct" dat lai -1) -> chi vung cuoi ket thuc, hai vung kia van getTown = true toi 22h, moi
 *   giao dich / shop / ky gui bi chan "trong thoi gian chiem Gym". Nay moi vung mot co (MapTown.adminKetThuc).
 * - Thoi luong: 1 tieng ke tu luc bat dau (MapTown.conChay) cho ca lich chuan lan gio admin dat.
 * - Khong bat dau lai trong cung moc gio ngay sau khi vua ket thuc (checkTimeGetTown).
 */
public final class KiemLoiBotChucNang {

    private KiemLoiBotChucNang() {
    }

    public static void chay() {
        Kiem.nhom("Chiem Gym: ket thuc tung vung + thoi luong 1 tieng");
        final long bay = 1_800_000_000_000L;
        final long gio = real.MapTown.THOI_LUONG_CHIEM_THANH;
        Kiem.dung("co moc: 59 phut sau bat dau van chay", real.MapTown.conChay(bay - gio + 60000L, bay, false, 20, false));
        Kiem.sai("co moc: 1 tieng sau bat dau thi het", real.MapTown.conChay(bay - gio, bay, false, 20, false));
        Kiem.sai("co moc, lich chuan: bat dau 20h (GM bd / het gio admin roi ktct) khong keo toi 22h",
                real.MapTown.conChay(bay - gio - 1000L, bay, false, 21, false));
        Kiem.dung("khong moc, gio admin: cho GM ket thuc", real.MapTown.conChay(0L, bay, true, 23, false));
        Kiem.dung("khong moc, lich chuan 21h: con chay", real.MapTown.conChay(0L, bay, false, 21, false));
        Kiem.sai("khong moc, lich chuan 22h: het", real.MapTown.conChay(0L, bay, false, 22, false));

        final long[] cu = real.MapTown.timeBatDauChiemThanh.clone();
        final int cuAdmin = real.MapTown.gameOverByAdmin;
        try {
            for (int i = 0; i < 3; i++) {
                real.MapTown.danhDauBatDau(i);
            }
            real.MapTown.adminKetThuc(0);
            real.MapTown.adminKetThuc(1);
            real.MapTown.adminKetThuc(2);
            real.MapTown.gameOverByAdmin = -1; // "ktct" sau over0..2
            Kiem.dung("over0, over1, over2 roi ktct: ca ba vung van co co ket thuc",
                    real.MapTown.adminKetThuc[0] && real.MapTown.adminKetThuc[1] && real.MapTown.adminKetThuc[2]);
            real.MapTown.danhDauBatDau(1);
            Kiem.sai("bat dau lai vung 1: bo co ket thuc cu", real.MapTown.adminKetThuc[1]);
        } finally {
            for (int i = 0; i < 3; i++) {
                real.MapTown.adminKetThuc[i] = false;
                real.MapTown.timeBatDauChiemThanh[i] = cu[i];
            }
            real.MapTown.gameOverByAdmin = cuAdmin;
        }
        diemVaoLang();
        Kiem.nhom("Cau hinh: khong chep gia tri server.ini dai qua cot CSDL (bi cat im lang)");
        final StringBuilder dai = new StringBuilder("admin");
        for (int i = 1; i <= 100; i++) {
            dai.append(String.format(",kt%03d", i));
        }
        Kiem.sai("sv.adminAccounts 100 ten (" + dai.length() + " ky tu) khong vua cot", server.CauHinh.vuaCotCsdl(dai.toString()));
        Kiem.dung("40 ten vua cot", server.CauHinh.vuaCotCsdl(dai.substring(0, 5 + 40 * 6)));
    }

    /** move2Map: diem vao (vd. diem vao lang ngau nhien) roi vao o chan -> doi sang o di duoc gan nhat. */
    static void diemVaoLang() {
        Kiem.nhom("move2Map: diem vao roi vao o chan -> o di duoc gan nhat (Map.oDiDuocGan)");
        final real.Map m = new real.Map();
        m.w = 5;
        m.type = new int[25];
        for (int i = 0; i < 25; i++) {
            m.type[i] = 2; // chan
        }
        m.type[3 * 5 + 4] = 0; // (4,3) di duoc
        m.type[2 * 5 + 2 - 1] = 0; // (1,2) di duoc
        Kiem.bang("o chan (2,2) -> o di duoc ke ben (1,2)", "1,2", ve(m.oDiDuocGan(2, 2, 8)));
        Kiem.bang("o di duoc giu nguyen", "4,3", ve(m.oDiDuocGan(4, 3, 8)));
        m.type[2 * 5 + 1] = 2;
        Kiem.bang("ban kinh 2 tim duoc (4,3)", "4,3", ve(m.oDiDuocGan(2, 2, 2)));
        Kiem.bang("ban kinh 1 khong co -> giu nguyen", "2,2", ve(m.oDiDuocGan(2, 2, 1)));
        m.type = null;
        Kiem.bang("khong co bo cuc -> giu nguyen", "7,9", ve(m.oDiDuocGan(7, 9, 8)));
    }

    static String ve(final int[] o) {
        return o[0] + "," + o[1];
    }
}
