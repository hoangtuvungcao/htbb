import com.htbb.log.DemCongSaiO;
import data.QuyTacDangKy;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Char;
import real.Map;
import real.cmd.DangKyTrongGame;

/**
 * Ba de xuat cua bot hoi quy (chu duyet 25/10/2026):
 *
 * 1) Chiem Gym: trong gio chiem Gym (Map.getTown) truoc day MOI cua hang NPC tren may chu bi khoa va moi thi tran mat an
 *    toan (LuatPkGame.vungPvp tra true o moi ban do -> ngam PvP, nguoi o Pewter mat HP). Nay chi ban do Gym (201 + ban
 *    sao, cua hang trong Gym) bi han che.
 * 2) Xin qua cong sai o: truoc kick ngay; nay tu choi + nap lai vi tri may chu, kick khi > 10 lan / 30 giay.
 * 3) Dang ky trong game bi chan theo MAY: cau bao rieng (khong con cau "mang di dong" cua han muc IP).
 */
final class KiemSuaDeXuatBot {

    private KiemSuaDeXuatBot() {
    }

    static void chay() {
        Kiem.nhom("De xuat bot 1: chiem Gym chi han che trong ban do Gym, thi tran van an toan, cua hang van mo");
        try {
            kiemChiemGym();
        } catch (final Throwable t) {
            t.printStackTrace(System.out);
            Kiem.dung("kiemChiemGym chay het: " + t, false);
        }
        Kiem.nhom("De xuat bot 2: xin qua cong sai o - tu choi + tra vi tri, chi kick khi spam");
        kiemCongSaiO();
        Kiem.nhom("De xuat bot 3: dang ky bi chan theo may co cau bao rieng");
        kiemDangKyMay();
    }

    // ------------------------------------------------------------------ 1

    private static Map banDo(final int id, final int load) {
        final Map m = new Map();
        m.mapId = id;
        m.mapIDLoadMap = load;
        return m;
    }

    private static boolean goi(final String lop, final String ham, final Class<?>[] kieu, final Object... ts) throws Exception {
        final Method mt = Class.forName(lop).getDeclaredMethod(ham, kieu);
        mt.setAccessible(true);
        return ((Boolean) mt.invoke(null, ts)).booleanValue();
    }

    private static boolean vungPvp(final Map m, final Char p) throws Exception {
        return goi("real.LuatPkGame", "vungPvp", new Class<?>[]{Map.class, Char.class}, m, p);
    }

    private static boolean khuAnToan(final Char p) throws Exception {
        return goi("real.BanHuongGame", "khuAnToan", new Class<?>[]{Char.class}, p);
    }

    private static void kiemChiemGym() throws Exception {
        final boolean[] cuTown = Map.getTown.clone();
        final boolean[] cuWar = Map.nwar.clone();
        try {
            final Map pewter = banDo(0, 0);
            final Map pewterSao = banDo(3001, 0);
            final Map lang70 = banDo(70, 70);
            final Map dongCo = banDo(1, 1);
            final Map gym = banDo(Map.idMapTown, Map.idMapTown);
            final Map gymSao = banDo(4201, Map.idMapTown);
            final Map cuaHang = banDo(-1, -1);
            cuaHang.isMapOffline = true; // nhu OfflineMap(-1) (khong dung that: constructor nap tile)

            final Char p = new Char(null);
            p.inCountry = 0;
            p.myCountry = 0;

            // ---- NGOAI gio chiem Gym: khong doi
            java.util.Arrays.fill(Map.getTown, false);
            java.util.Arrays.fill(Map.nwar, false);
            for (final Map m : new Map[]{pewter, dongCo, gym, gymSao}) {
                p.map = m;
                Kiem.sai("ngoai gio: mua ban duoc o map " + m.mapId, Map.camGiaoDichChiemGym(p));
            }
            p.map = pewter;
            Kiem.sai("ngoai gio: Pewter khong la vung PvP", vungPvp(pewter, p));
            Kiem.dung("ngoai gio: Pewter la khu an toan", khuAnToan(p));
            Kiem.dung("ngoai gio: ban do Gym 201 van la vung PvP nhu cu", vungPvp(gym, p));
            Kiem.sai("ngoai gio: ban sao Gym khong them PvP", vungPvp(gymSao, p));

            // ---- TRONG gio chiem Gym o vung 0
            Map.getTown[0] = true;
            for (final Map m : new Map[]{pewter, pewterSao, lang70, dongCo}) {
                p.map = m;
                Kiem.sai("chiem Gym: cua hang mo o map " + m.mapId + " (load " + m.mapIDLoadMap + ")", Map.camGiaoDichChiemGym(p));
                Kiem.sai("chiem Gym: map " + m.mapId + " KHONG thanh vung PvP", vungPvp(m, p));
            }
            for (final Map m : new Map[]{pewter, pewterSao, lang70}) {
                p.map = m;
                Kiem.dung("chiem Gym: thi tran " + m.mapId + " van la khu an toan (khong ngam PvP)", khuAnToan(p));
            }
            p.map = gym;
            Kiem.dung("chiem Gym: trong Gym 201 khoa mua ban", Map.camGiaoDichChiemGym(p));
            Kiem.dung("chiem Gym: trong Gym 201 la vung PvP", vungPvp(gym, p));
            p.map = gymSao;
            Kiem.dung("chiem Gym: ban sao Gym khoa mua ban", Map.camGiaoDichChiemGym(p));
            Kiem.dung("chiem Gym: ban sao Gym la vung PvP", vungPvp(gymSao, p));
            p.map = cuaHang;
            p.mapID = 205;
            Kiem.dung("chiem Gym: cua hang trong Gym (offline, mapID 205) khoa", Map.camGiaoDichChiemGym(p));
            p.mapID = 50;
            Kiem.sai("chiem Gym: cua hang trong lang (offline, mapID 50) mo", Map.camGiaoDichChiemGym(p));

            // vung khac (nuoc 1) khong dang chiem: Gym cua vung do khong khoa
            p.inCountry = 1;
            p.map = gym;
            Kiem.sai("chiem Gym vung 0: nguoi o Gym vung 1 mua ban duoc", Map.camGiaoDichChiemGym(p));
            Kiem.dung("Gym 201 van la PvP o moi vung (id 201)", vungPvp(gym, p));
            p.inCountry = -1;
            Kiem.sai("inCountry -1: khong loi, khong khoa", Map.camGiaoDichChiemGym(p));

            // chien thanh (nwar) cung chi PvP trong Gym
            java.util.Arrays.fill(Map.getTown, false);
            Map.nwar[2] = true;
            p.inCountry = 2;
            p.map = pewter;
            Kiem.sai("chien thanh: Pewter khong thanh PvP", vungPvp(pewter, p));
            Kiem.dung("chien thanh: ban sao Gym la PvP", vungPvp(gymSao, p));
            p.map = gym;
            Kiem.sai("chien thanh (khong phai chiem Gym): mua ban trong Gym nhu cu", Map.camGiaoDichChiemGym(p));
        } finally {
            System.arraycopy(cuTown, 0, Map.getTown, 0, cuTown.length);
            System.arraycopy(cuWar, 0, Map.nwar, 0, cuWar.length);
        }
        final String om = doc("src/real/OfflineMap.java");
        Kiem.sai("OfflineMap: het khoa toan may chu getTown[0] || getTown[1] || getTown[2]",
                om.contains("getTown[0] || getTown[1] || getTown[2]") || om.contains("!getTown[0] && !getTown[1] && !getTown[2]"));
        Kiem.dung("OfflineMap: mua + ban dung Map.camGiaoDichChiemGym", om.split("camGiaoDichChiemGym\\(player\\)", -1).length - 1 >= 2);
        Kiem.dung("MapEpNgoc: dung Map.camGiaoDichChiemGym", doc("src/real/MapEpNgoc.java").contains("Map.camGiaoDichChiemGym(player)"));
    }

    // ------------------------------------------------------------------ 2

    private static void kiemCongSaiO() {
        final DemCongSaiO d = new DemCongSaiO(DemCongSaiO.NGUONG, DemCongSaiO.CUA_SO_MS, DemCongSaiO.LOG_CACH_MS, 64);
        final long t0 = 1000000L;
        DemCongSaiO.KetQua k = d.ghiNhan("a", t0);
        Kiem.sai("1 lan sai o: khong kick", k.kick);
        Kiem.dung("1 lan sai o: ghi log", k.ghiLog);
        Kiem.bang("1 lan sai o: lan=1", 1, k.lan);
        k = d.ghiNhan("a", t0 + 100L);
        Kiem.sai("lan 2 ngay sau: khong ghi log lai (gioi han)", k.ghiLog);
        for (int i = 3; i <= 10; i++) {
            k = d.ghiNhan("a", t0 + i * 1000L);
        }
        Kiem.bang("10 lan / 10 giay: van khong kick", Boolean.FALSE, Boolean.valueOf(k.kick));
        Kiem.dung("trong dot: log toi da moi 10 giay", k.ghiLog); // lan 10 o t0+10s
        k = d.ghiNhan("a", t0 + 11000L);
        Kiem.dung("lan 11 trong 30 giay: kick", k.kick);
        Kiem.dung("kick: luon ghi log", k.ghiLog);
        Kiem.bang("kick: xoa bo dem (dang nhap lai tu 0)", 1, d.ghiNhan("a", t0 + 12000L).lan);

        // cua so truot: 1 lan / 4 giay (vd lag keo dai) khong bao gio kick
        boolean kick = false;
        for (int i = 0; i < 60; i++) {
            kick |= d.ghiNhan("b", t0 + i * 4000L).kick;
        }
        Kiem.sai("1 lan / 4 giay trong 4 phut: khong kick (<= 8 lan / 30 giay)", kick);
        Kiem.bang("nhan vat khac tach biet", 1, d.ghiNhan("c", t0).lan);

        final String xl = doc("src/real/MapXuLyLenh.java");
        final int iKhoi = xl.indexOf("if (!onPos && contentMap) {");
        final int iHet = xl.indexOf("Map toMap = (Map) RealController.mapList.get(toMapID);", iKhoi);
        final String khoi = iKhoi > 0 && iHet > iKhoi ? xl.substring(iKhoi, iHet) : "";
        Kiem.dung("doChangeMap: nhanh sai o goi congSaiO", khoi.contains("congSaiO(map, player, toMapID, toTX, toTY"));
        Kiem.sai("doChangeMap: nhanh sai o khong kick truc tiep", khoi.contains("kickPlayer"));
        Kiem.dung("doChangeMap: tu choi = nap lai vi tri may chu", khoi.contains("toMapID = player.mapID;")
                && khoi.contains("toTX = player.x / 16;") && khoi.contains("checklocation = false;"));
        final int iHam = xl.indexOf("static boolean congSaiO(");
        final String ham = iHam > 0 ? xl.substring(iHam, xl.indexOf("static void doChangeMap(", iHam)) : "";
        Kiem.dung("congSaiO: kick chi khi sai.kick", ham.indexOf("if (!sai.kick)") > 0 && ham.indexOf("if (!sai.kick)") < ham.indexOf("kickPlayer"));
        Kiem.dung("congSaiO: log [CONG_SAI_O] co gioi han", ham.contains("\"CONG_SAI_O\"") && ham.contains("if (sai.ghiLog)"));
        // cong kiem 29/10/2026 (cot_truyen): tu choi khong im lang - bao ly do cho nguoi choi truoc khi nap lai
        final int iKhongKick = ham.indexOf("if (!sai.kick)");
        Kiem.dung("congSaiO: tu choi (khong kick) bao ly do lyDoCongSaiO", iKhongKick > 0
                && ham.indexOf("ThongBao.rieng(player, lyDoCongSaiO(player, toMapID))", iKhongKick) > iKhongKick
                && ham.indexOf("ThongBao.rieng(player, lyDoCongSaiO(player, toMapID))", iKhongKick) < ham.indexOf("return false;", iKhongKick));
        final String ld = doc("src/real/MapLienDau.java");
        Kiem.sai("MapLienDau: het kick ngay 'mapliendau 5/6'", ld.contains("\"mapliendau 5\"") || ld.contains("\"mapliendau 6\""));
        Kiem.dung("MapLienDau: dung chung congSaiO", ld.contains("MapXuLyLenh.congSaiO(this, player, toMapID, toTX, toTY"));
    }

    // ------------------------------------------------------------------ 3

    private static void kiemDangKyMay() {
        Kiem.bang("may: quaGio -> mayQuaGio", "mayQuaGio", DangKyTrongGame.maHanMucMay("quaGio"));
        Kiem.bang("may: quaNgay -> mayQuaNgay", "mayQuaNgay", DangKyTrongGame.maHanMucMay("quaNgay"));
        Kiem.bang("may: null -> null", null, DangKyTrongGame.maHanMucMay(null));
        for (final String ma : new String[]{"quaGio", "quaNgay", "mayQuaGio", "mayQuaNgay"}) {
            Kiem.dung("laHanMuc(" + ma + ")", DangKyTrongGame.laHanMuc(ma));
        }
        Kiem.sai("laHanMuc(trungTen)", DangKyTrongGame.laHanMuc("trungTen"));
        final String ipGio = QuyTacDangKy.thongBao("quaGio");
        final String ipNgay = QuyTacDangKy.thongBao("quaNgay");
        final String mGio = QuyTacDangKy.thongBao("mayQuaGio");
        final String mNgay = QuyTacDangKy.thongBao("mayQuaNgay");
        Kiem.dung("IP gio: van cau mang di dong", ipGio.contains("mạng di động"));
        Kiem.dung("IP ngay: van cau mang di dong", ipNgay.contains("mạng di động"));
        Kiem.bang("may ngay: cau rieng", "Thiết bị này đã tạo đủ tài khoản hôm nay. Thử lại vào ngày mai hoặc liên hệ hỗ trợ.", mNgay);
        Kiem.dung("may gio: cau rieng noi 'Thiết bị', khong 'mạng di động' / 'Wi-Fi'", mGio.startsWith("Thiết bị")
                && !mGio.contains("mạng di động") && !mGio.contains("Wi-Fi"));
        Kiem.dung("may: khong con tra ve ma (co trong JSON)", !mGio.equals("mayQuaGio") && !mNgay.equals("mayQuaNgay"));
        final String xl = doc("src/real/cmd/DangKyTrongGame.java");
        Kiem.dung("kiemHanMuc: nhanh may doi ma", xl.contains("return maHanMucMay(vuotHanMucHienTai(Math.max(demMay[0]"));
        Kiem.dung("kiemHanMuc: nhanh IP giu ma IP", xl.contains("kq = vuotHanMucHienTai(Math.max(demIp[0]"));
    }

    private static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
