import data.CharInboxMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * DOC THU (goi 110) KHONG DUOC XOA THU NGUOI KHAC (bot bat_thuong / bt_thu, 25/10/2026).
 *
 * LOI. Map.doReadMessage doc byte chi so thu trong hop (0, 1, 2...) roi goi Database.delInboxMsg(chiSo) =
 * "delete from tob_inbox where id=" + chiSo: xoa dong co id TRUNG CHI SO - thuong la thu dang cho cua mot nguoi choi
 * khac (thu cho nguoi offline). Nguoi choi co n thu, bam doc tung thu -> xoa thu id 0..n-1 cua ai do.
 *
 * SUA. CharInboxMessage.idCanXoa(hop, chiSo) tra idMsg (id dong that cua thu do) hoac -1; doReadMessage xoa bang
 * delInboxMsg(idThu, charname) (them dieu kien dung nguoi nhan).
 */
public final class KiemDocThu {

    private KiemDocThu() {
    }

    static CharInboxMessage thu(final int id) {
        final CharInboxMessage t = new CharInboxMessage();
        t.idMsg = id;
        t.sender = "a";
        t.info = "b";
        return t;
    }

    public static void chay() {
        Kiem.nhom("Doc thu (110): xoa dung thu cua minh theo id dong, khong theo chi so");
        final List<CharInboxMessage> hop = new ArrayList<>();
        hop.add(thu(9001));
        hop.add(thu(9002));
        hop.add(thu(9003));
        Kiem.bang("chi so 0 -> id dong 9001 (khong phai id 0)", 9001, CharInboxMessage.idCanXoa(hop, 0));
        Kiem.bang("chi so 2 -> id dong 9003 (khong phai id 2)", 9003, CharInboxMessage.idCanXoa(hop, 2));
        Kiem.bang("chi so am -> -1", -1, CharInboxMessage.idCanXoa(hop, -1));
        Kiem.bang("chi so vuot hop -> -1", -1, CharInboxMessage.idCanXoa(hop, 3));
        Kiem.bang("hop null -> -1", -1, CharInboxMessage.idCanXoa(null, 0));
        Kiem.bang("thu chua co id dong -> -1", -1, CharInboxMessage.idCanXoa(java.util.Collections.singletonList(thu(0)), 0));

        final String map = doc("src/real/Map.java");
        final int i = map.indexOf("void doReadMessage(");
        final String than = i < 0 ? "" : map.substring(i, Math.min(map.length(), i + 700));
        Kiem.dung("doReadMessage dung CharInboxMessage.idCanXoa", than.contains("CharInboxMessage.idCanXoa("));
        Kiem.dung("doReadMessage xoa kem ten nguoi nhan", than.contains("delInboxMsg(idThu, player.charname)"));
        Kiem.sai("doReadMessage KHONG con xoa theo chi so (delInboxMsg(index))", than.contains("delInboxMsg(index)"));
        final String db = doc("src/data/Database.java");
        Kiem.dung("Database co delInboxMsg(id, charname) voi dieu kien charname",
                db.contains("delete from tob_inbox where id=? and charname=?"));
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
