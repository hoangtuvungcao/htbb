import com.htbb.log.TheoDoiBatThuong;

/**
 * com.htbb.log.TheoDoiBatThuong: dem nap map / keo vi tri theo nhan vat cho bao cao van hanh
 * (vps/theo-doi/bao-cao-ngay.sh doc dong NAP_MAP_LAP / KEO_VE). Phai: duoi nguong im lang, cham nguong MOT dong,
 * im lang trong thoi gian cho, cua so moi dem lai, bang khoa bi chan tren, khong bao gio nem loi.
 */
final class KiemTheoDoiBatThuong {

    private KiemTheoDoiBatThuong() {
    }

    static void chay() {
        Kiem.nhom("TheoDoiBatThuong - nap map lien tuc / vi tri bi keo ve");
        final TheoDoiBatThuong t = new TheoDoiBatThuong("NAP_MAP_LAP", 6, 30000L, 300000L, 16);
        String d = null;
        for (int i = 0; i < 5; i++) {
            d = t.ghiNhan("kazuhas4", 107, null, 1000L + i * 1000L);
        }
        Kiem.bang("5 lan < nguong 6: im lang", null, d);
        d = t.ghiNhan("kazuhas4", 107, null, 6000L);
        Kiem.bang("lan thu 6 trong 30 giay: mot dong", "kazuhas4 lan=6/5s cung_map=107", d);
        Kiem.bang("lan 7 ngay sau: im lang (cho 5 phut)", null, t.ghiNhan("kazuhas4", 107, null, 6500L));
        // cua so moi sau 30 giay nhung van trong 5 phut im lang
        for (int i = 0; i < 6; i++) {
            d = t.ghiNhan("kazuhas4", 107, null, 40000L + i * 100L);
        }
        Kiem.bang("cham nguong lai trong 5 phut: van im lang", null, d);
        for (int i = 0; i < 6; i++) {
            d = t.ghiNhan("kazuhas4", 107, null, 400000L + i * 100L);
        }
        Kiem.dung("sau 5 phut: bao lai", d != null && d.startsWith("kazuhas4 lan=6/"));

        // nhieu map + ly do (KEO_VE)
        final TheoDoiBatThuong k = new TheoDoiBatThuong("KEO_VE", 3, 60000L, 300000L, 16);
        k.ghiNhan("an", 1, "toc_do", 0L);
        k.ghiNhan("an", 2, "o_cam", 10L);
        d = k.ghiNhan("an", 2, "toc_do", 20L);
        Kiem.bang("nhieu map + gom ly do khong lap", "an lan=3/1s map_cuoi=2 (nhieu map) ly_do=toc_do,o_cam", d);

        // het cua so: dem lai tu dau
        final TheoDoiBatThuong c = new TheoDoiBatThuong("X", 3, 1000L, 0L, 16);
        c.ghiNhan("b", 5, null, 0L);
        c.ghiNhan("b", 5, null, 500L);
        Kiem.bang("lan 3 sau khi het cua so 1s: dem lai, im lang", null, c.ghiNhan("b", 5, null, 1500L));

        // bang khoa bi chan tren (16), ten rong / null bo qua
        for (int i = 0; i < 100; i++) {
            t.ghiNhan("nv" + i, 1, null, 500000L);
        }
        Kiem.dung("bang khoa toi da 16", t.soKhoa() <= 16);
        Kiem.bang("ten null: bo qua", null, t.ghiNhan(null, 1, null, 1L));
        Kiem.bang("ten rong: bo qua", null, t.ghiNhan("", 1, null, 1L));
        boolean nem = false;
        try {
            TheoDoiBatThuong.NAP_MAP.ghi(null, 0, null);
            TheoDoiBatThuong.KEO_VE.ghi("kiem", 0, "toc_do");
        } catch (final RuntimeException e) {
            nem = true;
        }
        Kiem.sai("ghi() khong nem loi", nem);
        Kiem.bang("hai bo dem dung cho bao cao", "NAP_MAP_LAP KEO_VE",
                TheoDoiBatThuong.NAP_MAP.loai() + " " + TheoDoiBatThuong.KEO_VE.loai());
    }
}
