import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Kiem thu CAMERA CO DINH phia server (com.htbb.mang.VungNhinCoDinh): be rong man client gui trong goi dang
 * nhap (session.w) quyet dinh tam them / bo quai (Char.setSizeScreen: rangeAddMonster = w/2 + 50). Client sua
 * doi gui w khong lo khong duoc nhan quai ca ban do -> server kep ve 640 (be rong logic co dinh).
 */
public final class KiemVungNhinCoDinh {

    public static void chay() {
        Kiem.nhom("Camera co dinh - kep be rong man client gui len");
        Kiem.bang("640 (client chuan) giu nguyen", (short) 640, com.htbb.mang.VungNhinCoDinh.kepRong(640));
        Kiem.bang("320 giu nguyen", (short) 320, com.htbb.mang.VungNhinCoDinh.kepRong(320));
        Kiem.bang("240 (may J2ME nho) giu nguyen", (short) 240, com.htbb.mang.VungNhinCoDinh.kepRong(240));
        Kiem.bang("853 (APK cu man rong) -> 640", (short) 640, com.htbb.mang.VungNhinCoDinh.kepRong(853));
        Kiem.bang("1400 (Unity 21:9) -> 640", (short) 640, com.htbb.mang.VungNhinCoDinh.kepRong(1400));
        Kiem.bang("32767 (client sua doi) -> 640", (short) 640, com.htbb.mang.VungNhinCoDinh.kepRong(Short.MAX_VALUE));
        Kiem.bang("am -> 320 mac dinh", (short) 320, com.htbb.mang.VungNhinCoDinh.kepRong(-5));
        Kiem.bang("0 -> 320 mac dinh", (short) 320, com.htbb.mang.VungNhinCoDinh.kepRong(0));
        final int tamToiDa = com.htbb.mang.VungNhinCoDinh.RONG_TOI_DA / 2 + 50;
        Kiem.bang("tam them quai toi da (w/2 + 50)", 370, tamToiDa);

        Kiem.nhom("Camera co dinh - noi vao LoginHandler / Char.setSizeScreen");
        final String login = doc("src/real/cmd/LoginHandler.java");
        Kiem.dung("LoginHandler kep session.w ngay khi doc goi dang nhap",
                login.contains("session.w = com.htbb.mang.VungNhinCoDinh.kepRong(dis.readShort());"));
        Kiem.sai("LoginHandler khong con gan thang session.w = dis.readShort()",
                login.contains("session.w = dis.readShort();"));
        final String chr = doc("src/real/Char.java");
        final int i = chr.indexOf("public void setSizeScreen(short w, final short h) {");
        Kiem.dung("co Char.setSizeScreen", i >= 0);
        final int j = i < 0 ? -1 : chr.indexOf("rangeAddMonster[0]", i);
        Kiem.dung("setSizeScreen kep w TRUOC khi tinh rangeAddMonster", i >= 0 && j > i
                && chr.substring(i, j).contains("w = com.htbb.mang.VungNhinCoDinh.kepRong(w);"));
    }

    private static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
