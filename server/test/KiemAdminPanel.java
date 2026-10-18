import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

/**
 * Bang dieu khien may chu (server.AdminPanel + server.quantri.*):
 *  - headless: moNeuCoManHinh() tra null, KHONG nem HeadlessException (VPS khong man hinh);
 *  - 8 trang + thanh phan dung duoc ma khong can JFrame (JPanel chay duoc khi headless), doi sang/toi;
 *  - ham thuan: dinh dang so, lam tron truc bieu do, kiem gia tri cau hinh, chuan thoi gian, lam sach kiem toan;
 *  - phat qua chi qua bo loc giftcode an toan (tu choi xu / luong mo);
 *  - anh Pokemon thieu tep -> Poke Ball ve san, khong loi.
 */
public final class KiemAdminPanel {

    private KiemAdminPanel() {
    }

    public static void chay() {
        Kiem.nhom("Bang dieu khien may chu (AdminPanel)");
        if (GraphicsEnvironment.isHeadless()) {
            Object p = "chua goi";
            try {
                p = server.AdminPanel.moNeuCoManHinh();
            } catch (final Throwable t) {
                p = t;
            }
            Kiem.bang("headless: moNeuCoManHinh() tra null, khong nem loi", null, p);
        } else {
            Kiem.dung("co man hinh: bo qua phan mo cua so (khong bat GUI trong kiem thu)", true);
        }
        int soTrang = -1;
        String loi = "";
        try {
            soTrang = server.AdminPanel.taoNoiDungThu();
        } catch (final Throwable t) {
            loi = t.toString();
            t.printStackTrace();
        }
        Kiem.bang("dung du 8 trang khong can JFrame " + loi, 8, soTrang);

        Kiem.bang("so kieu Viet Nam", "1.234.567", server.quantri.DinhDang.so(1234567));
        Kiem.bang("so am", "-1.000", server.quantri.DinhDang.so(-1000));
        Kiem.bang("so gon M", "2,5M", server.quantri.DinhDang.gon(2500000));
        Kiem.bang("dong ho", "01:02:05", server.quantri.DinhDang.dongHo(3725000L));
        Kiem.bang("tron truc 87 -> 100", 100.0, server.quantri.BieuDo.tronLen(87));
        Kiem.bang("tron truc 0.3 -> 0.5", 0.5, server.quantri.BieuDo.tronLen(0.3));

        final server.quantri.BieuDo.VongDem v = new server.quantri.BieuDo.VongDem(3);
        for (int i = 1; i <= 5; i++) {
            v.them(i);
        }
        Kiem.bang("vong dem giu 3 diem moi nhat", "3,4,5", (int) v.giaTri(0) + "," + (int) v.giaTri(1) + "," + (int) v.giaTri(2));

        final server.quantri.NguonCauHinh.Muc m = new server.quantri.NguonCauHinh.Muc();
        m.khoa = "sv.thu";
        m.kieu = "so";
        m.min = 0;
        m.max = 100;
        Kiem.dung("cau hinh: so trong khoang hop le", server.quantri.NguonCauHinh.kiem(m, "50") == null);
        Kiem.sai("cau hinh: so ngoai khoang bi tu choi", server.quantri.NguonCauHinh.kiem(m, "101") == null);
        Kiem.sai("cau hinh: chu trong o so bi tu choi", server.quantri.NguonCauHinh.kiem(m, "abc") == null);
        m.kieu = "co";
        Kiem.sai("cau hinh: co chi nhan 0/1", server.quantri.NguonCauHinh.kiem(m, "2") == null);

        Kiem.bang("chuan thoi gian", "2026-10-18 20:30:00", server.quantri.ThaoTacMayChu.chuanThoiGian("2026-10-18 20:30"));
        Kiem.bang("thoi gian sai -> null", null, server.quantri.ThaoTacMayChu.chuanThoiGian("18h30"));
        Kiem.bang("kiem toan lam sach xuong dong", "a b", server.quantri.KiemToanQuanTri.sach("a\nb"));
        try {
            Kiem.bang("mat khau kieu PASSWORD() cua MySQL", "*6BB4837EB74329105EE4568DDA7DC67ED2CA2AD9",
                    server.quantri.ThaoTacMayChu.matKhauSql("123456"));
        } catch (final Exception e) {
            Kiem.dung("matKhauSql: " + e, false);
        }

        Kiem.sai("tang qua: xu bi tu choi (chi duong giftcode an toan)",
                server.quantri.ThaoTacMayChu.kiemQua("XU:1000", 50) == null);
        Kiem.sai("tang qua: luong mo bi tu choi", server.quantri.ThaoTacMayChu.kiemQua("LUONG:10", 50) == null);

        final BufferedImage pb = server.quantri.AnhPokemon.chanDung(9999);
        Kiem.dung("thieu anh Pokemon -> Poke Ball ve san", pb != null && pb.getWidth() == 40);
        Kiem.dung("icon vat pham thieu -> khong null", server.quantri.AnhPokemon.vatPham("Khong_Co_Tep") != null);
        Kiem.bang("co tinh nang hien ten tieng Viet (khong lo khoa ky thuat)", "Thiết kế dài hạn",
                server.quantri.ThaoTacMayChu.tenCo("lau_dai"));
        // kinh te truc tiep: chot ngay uu tien, hom nay = so do song cho tong xu nhung giu xu vao/ra cua ngay
        final java.util.List<Object[]> ngay = new java.util.ArrayList<Object[]>();
        final String homNay = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        ngay.add(new Object[]{homNay, 100L, 7L, 5L});
        final java.util.Map<String, long[]> c = server.quantri.KinhTeTrucTiep.chuoiNgay(ngay, null, null);
        Kiem.bang("kinh te: mot ngay chot -> mot diem", 1, c.size());
        Kiem.bang("kinh te: gia tri xu cua ngay", 100L, c.get(homNay)[0]);
        Kiem.dung("ButtonEditor co moMenu (menu cu mo tu bang moi)", coHam(server.Panel.ButtonEditor.class, "moMenu"));
        Kiem.dung("AdminPanel giu addLog (ButtonEditor goi)", coHam(server.AdminPanel.class, "addLog"));
    }

    static boolean coHam(final Class<?> c, final String ten) {
        for (final java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(ten)) {
                return true;
            }
        }
        return false;
    }
}
