import com.htbb.thongbao.ThongBao;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 0.9.6.1 thong bao nguoi choi (com.htbb.thongbao.ThongBao): cau chu ro rang (khong "bậc", gap Pokemon tach rieng,
 * boss ai/o dau), goi -104 dung bo cuc client doc (loai, chu 2 dong, kieu, hinh + id), ma loai trung client.
 */
public final class KiemThongBaoNguoiChoi {

    private KiemThongBaoNguoiChoi() {
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    public static void chay() {
        Kiem.nhom("Thong bao nguoi choi (0.9.6.1)");
        Kiem.bang("cau ca dong 1", "Câu được Vảy Magikarp ×1", ThongBao.duoc("Câu được", "Vảy Magikarp", 1, false));
        Kiem.bang("cau ca hiem", "Câu được đồ hiếm: Vảy Gyarados ×2", ThongBao.duoc("Câu được", "Vảy Gyarados", 2, true));
        Kiem.bang("the luc", "Thể lực còn 176", ThongBao.theLuc(176));
        Kiem.bang("gap Pokemon", "Bạn vừa gặp Magikarp!", ThongBao.gap("Magikarp", false));
        Kiem.bang("hai dong", "A\nB", ThongBao.noi("A", "B"));
        Kiem.bang("hai dong (phu rong)", "A", ThongBao.noi("A", " "));
        Kiem.bang("client cu: mot cau", "Câu được Vảy Magikarp ×1. Thể lực còn 176",
                ThongBao.motCau("Câu được Vảy Magikarp ×1", "Thể lực còn 176"));
        Kiem.bang("boss xuat hien", "Boss Gyarados đã xuất hiện ở Hồ Phẫn Nộ!", ThongBao.bossXuatHien("Gyarados", "Hồ Phẫn Nộ"));
        Kiem.bang("boss khong lap chu Boss", "Boss Trung Thu đã xuất hiện!", ThongBao.bossXuatHien("Boss Trung Thu", ""));
        Kiem.bang("boss bi ha", "Ash đã hạ gục Boss Người tuyết ở Núi Tuyết!", ThongBao.bossBiHa("Người tuyết", "Ash", "Núi Tuyết"));
        Kiem.bang("boss roi di", "Boss Gyarados đã rời Hồ Phẫn Nộ. Hẹn lần sau!", ThongBao.bossBienMat("Gyarados", "Hồ Phẫn Nộ"));
        try {
            final io.Message m = ThongBao.tao(ThongBao.HIEM, "Câu được đồ hiếm: Vảy Gyarados ×1", "Thể lực còn 120",
                    ThongBao.HINH_POKEMON, 130);
            final byte[] b = m.toByteArray();
            Kiem.bang("goi -104", -104, (int) b[0]);
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
            Kiem.bang("goi: loai HIEM", 3, (int) in.readByte());
            final String chu = in.readUTF();
            Kiem.dung("goi: chu 2 dong", chu.indexOf('\n') > 0 && chu.startsWith("Câu được"));
            Kiem.bang("goi: kieu thao tac 0", 0, (int) in.readByte());
            Kiem.bang("goi: hinh Pokemon", 2, (int) in.readByte());
            Kiem.bang("goi: dex", 130, (int) in.readShort());
            Kiem.bang("goi: het", 0, in.available());
        } catch (final Exception e) {
            Kiem.dung("goi -104 tao duoc: " + e, false);
        }
        // ma loai trung client (game.pokemon.ThongBao)
        final String cl = doc("../client_jar/src/game/pokemon/ThongBao.java");
        Kiem.dung("client cung ma loai", cl.contains("KET_QUA = 0, LOI = 1, TIN = 2, HIEM = 3, SU_KIEN = 4"));
        Kiem.dung("client cung ma hinh", cl.contains("HINH_KHONG = 0, HINH_ICON = 1, HINH_POKEMON = 2, HINH_BIEU_TUONG = 3"));
        // cau ca / dao khong con cau dinh liu "(bậc N). Gặp X! Thể lực còn N."
        final String hst = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.sai("cau ca khong con 'Câu được: ' + bac", hst.contains("new StringBuilder(\"Câu được: \")"));
        Kiem.sai("dao khong con '(bậc ' trong thong bao", hst.contains("\"Đào được: \""));
        for (final String f : new String[]{"real/BossSerperior", "data/BossMatQuy", "real/BossMoba", "real/BossDusknoir",
            "real/BossServine", "real/BossMeCung", "real/BossNguoiTuyet", "real/BossDracula", "real/BossGyarados", "real/BossDotKich"}) {
            final String s = doc("src/" + f + ".java");
            Kiem.dung(f + ": boss bao ai + o dau (ThongBao.bossXuatHien)", s.contains("ThongBao.bossXuatHien("));
            Kiem.sai(f + ": khong con 'X đã xuất hiện.' khong ban do", s.contains("+ \" đã xuất hiện.\""));
        }
    }
}
