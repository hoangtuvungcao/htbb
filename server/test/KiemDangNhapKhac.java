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
        // 31/10/2026 (kiem 2 client that tren may chu local): cho DA thuc te khi dang nhap lai la buoc danh sach nhan vat
        // (MessageCreator.createCharListMessage) - truoc kickPlayer tron, client cu khong biet ly do -> tu noi lai, da nguoc
        String mc = doc("../MessageCreator.java");
        int i = mc == null ? -1 : mc.indexOf("createCharListMessage(");
        String doan = i < 0 ? "" : mc.substring(i, Math.min(mc.length(), i + 3000));
        Kiem.dung("MessageCreator.createCharListMessage: da phien cu kem ly do (dongVoiLyDo DANG_NHAP_KHAC)",
                doan.contains("dongVoiLyDo(real.cmd.LoginHandler.DANG_NHAP_KHAC"));
        // phien MOI khong bi ngat khi nhan vat cu con trong ~0,4 s cho da: cho nhan vat cu roi roi chon tiep
        String sc = doc("SelectCharHandler.java");
        Kiem.dung("SelectCharHandler: cho nhan vat cu roi (choRoi) truoc khi ngat phien moi",
                sc != null && sc.contains("static boolean choRoi(") && sc.indexOf("choRoi(p2,") > 0);
    }

    public static void main(String[] a) {
        chay();
        System.exit(Kiem.tongKet() == 0 ? 0 : 1);
    }
}
