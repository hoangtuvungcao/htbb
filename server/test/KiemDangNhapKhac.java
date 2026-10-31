import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 31/10/2026 (nhật ký: hai client cùng tài khoản đá nhau ~3 s một lần): phiên CŨ bị đóng vì tài khoản đăng nhập nơi khác phải
 * nhận lý do (lệnh 37 "Tài khoản của bạn vừa đăng nhập ở thiết bị khác.") TRƯỚC khi đóng (Session.dongVoiLyDo) - client
 * nhận ra câu này thì không tự nối lại (client_pc/test/KiemDangNhapKhac kiểm phía client). Mọi chỗ đá phiên cũ ở
 * LoginHandler / SelectCharHandler đi qua dongVoiLyDo, không còn disconnect trơn.
 */
public class KiemDangNhapKhac {
    static String doc(String tep) {
        for (String goc : new String[]{"src/real/cmd/", "server/src/real/cmd/"}) {
            Path p = Paths.get(goc + tep);
            if (Files.exists(p)) {
                try {
                    return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    public static void chay() {
        Kiem.nhom("Dang nhap o thiet bi khac: phien cu nhan ly do truoc khi bi dong (client khong tu noi lai)");
        Kiem.bang("cau ly do (client so chuoi 'dang nhap o thiet bi khac')", "Tài khoản của bạn vừa đăng nhập ở thiết bị khác.",
                real.cmd.LoginHandler.DANG_NHAP_KHAC);
        Kiem.dung("chuoi ly do khong bi tu dien Pokemon doi",
                com.htbb.pokemon.ChuPokemon.doi(real.cmd.LoginHandler.DANG_NHAP_KHAC).contains("đăng nhập ở thiết bị khác"));
        for (String tep : new String[]{"SelectCharHandler.java", "LoginHandler.java"}) {
            String s = doc(tep);
            if (s == null) {
                Kiem.dung("doc duoc " + tep, false);
                continue;
            }
            Kiem.dung(tep + ": khong con da phien cu bang disconnect tron (getSession().disconnect)", !s.contains("getSession().disconnect("));
            Kiem.dung(tep + ": da phien cu qua dongVoiLyDo(DANG_NHAP_KHAC)", s.contains("dongVoiLyDo("));
        }
    }

    public static void main(String[] a) {
        chay();
        System.exit(Kiem.tongKet() == 0 ? 0 : 1);
    }
}
