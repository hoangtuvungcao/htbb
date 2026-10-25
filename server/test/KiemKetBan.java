import com.htbb.pokemon.LoiMoiKetBan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * KET BAN (101): "dong y" phai co loi moi that (bot bat_thuong / bt_ket_ban, 25/10/2026).
 *
 * LOI. MapXaHoi.doAddFriend nhanh ok=1 them hai nguoi vao danh sach ban cua nhau ngay, khong kiem co loi moi nao:
 * gui 101 + id nhan vat nguoi kia + 1 = ep ket ban (bo qua tuy chon khong nhan loi moi), gui lap = danh sach trung lap.
 *
 * SUA. LoiMoiKetBan: ok=0 ghi loi moi cho nguoi nhan; ok=1 chi hop le khi co loi moi con han tu dung nguoi moi (dung
 * mot lan), va khong them khi da la ban.
 */
public final class KiemKetBan {

    private KiemKetBan() {
    }

    public static void chay() {
        Kiem.nhom("Ket ban: dong y can loi moi that, khong them trung");
        LoiMoiKetBan.xoaHet();
        final long t = 1000000L;
        Kiem.sai("khong co loi moi -> khong dong y duoc", LoiMoiKetBan.dungLoiMoi(2, 1, t));
        LoiMoiKetBan.ghi(2, 1, t);
        Kiem.sai("loi moi cua nguoi KHAC -> khong dong y voi nguoi 3", LoiMoiKetBan.dungLoiMoi(2, 3, t + 10));
        Kiem.dung("co loi moi con han -> dong y", LoiMoiKetBan.dungLoiMoi(2, 1, t + 1000));
        Kiem.sai("loi moi chi dung mot lan", LoiMoiKetBan.dungLoiMoi(2, 1, t + 2000));
        LoiMoiKetBan.ghi(2, 1, t);
        Kiem.sai("loi moi het han (qua " + LoiMoiKetBan.HAN_MS + " ms)", LoiMoiKetBan.dungLoiMoi(2, 1, t + LoiMoiKetBan.HAN_MS + 1));
        Kiem.dung("da la ban", LoiMoiKetBan.daLaBan(new java.util.Vector<Integer>(Arrays.asList(5, 7)), 7));
        Kiem.sai("chua la ban", LoiMoiKetBan.daLaBan(new java.util.Vector<Integer>(Arrays.asList(5, 7)), 8));
        Kiem.sai("danh sach null", LoiMoiKetBan.daLaBan(null, 8));
        LoiMoiKetBan.xoaHet();

        final String xh = doc("src/real/MapXaHoi.java");
        final int ok1 = xh.indexOf("} else if (ok == 1) {");
        final int dung = xh.indexOf("LoiMoiKetBan.dungLoiMoi(player.charDBID, p.charDBID");
        final int them = xh.indexOf("p.friendListID.add(player.charDBID);");
        Kiem.dung("doAddFriend ok=1 kiem loi moi TRUOC khi them ban", ok1 > 0 && dung > ok1 && dung < them);
        Kiem.dung("doAddFriend ok=0 ghi loi moi", xh.indexOf("LoiMoiKetBan.ghi(p.charDBID, player.charDBID") > 0
                && xh.indexOf("LoiMoiKetBan.ghi(p.charDBID, player.charDBID") < ok1);
        Kiem.dung("doAddFriend ok=1 khong them trung (daLaBan)", xh.indexOf("LoiMoiKetBan.daLaBan(player.friendListID, p.charDBID)") > ok1);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
