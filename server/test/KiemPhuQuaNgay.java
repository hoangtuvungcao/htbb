import com.htbb.pokemon.LauDai;

import data.InfoQuestTemplate;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Char;
import real.Map;
import real.NewCharQuest;
import real.NhiemVuLauDaiGame;

/**
 * PHU QUA NGAY (30/10/2026, bot phu kin): nhiem vu phu lau dai (P-*, chuoi theo thi tran lam mot lan) dang lam GIU tien do
 * khi qua ngay. Truoc day ca dang nhap ngay moi (CharNapDuLieu) lan doi ngay luc dang choi (CharCapNhat) deu xoa subQuest
 * im lang (di san phu hang ngay cua ban goc): bot ha 10/35 Kricketot, qua 0h, nhiem vu phu ve "co the nhan" tu dau.
 * He cu (tat sv.lau_dai) van huy nhu ban goc.
 */
public final class KiemPhuQuaNgay {

    private KiemPhuQuaNgay() {
    }

    static final short ID = 31555;

    public static void chay() {
        Kiem.nhom("Nhiem vu phu lau dai giu tien do qua ngay (NhiemVuLauDaiGame.huyPhuQuaNgay)");
        final boolean cu = LauDai.bat();
        final InfoQuestTemplate t = new InfoQuestTemplate();
        t.id = ID;
        t.type = 0;
        t.ma = "P-99.0.1";
        t.name = "Thử phụ qua ngày";
        t.main_sub = 1;
        t.totalMonsKilled.add((short) 35);
        Map.allSubQuest.put(ID, t);
        final real.GemTemplate[] gemCu = Map.gemTemplate;
        if (Map.gemTemplate == null) {
            Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            final Char p = new Char(null);
            p.subQuest = new NewCharQuest(ID, (byte) 1);
            p.subQuest.state_quest = NewCharQuest.WORKING;
            p.subQuest.monsterKilled[0] = 10;

            LauDai.dat(true);
            Kiem.sai("lau dai: qua ngay khong huy phu dang lam", NhiemVuLauDaiGame.huyPhuQuaNgay(p));
            Kiem.bang("lau dai: van dung nhiem vu phu", Short.valueOf(ID), Short.valueOf(p.subQuest.id_quest));
            Kiem.bang("lau dai: van dang lam", Short.valueOf(NewCharQuest.WORKING), Short.valueOf(p.subQuest.state_quest));
            Kiem.bang("lau dai: giu 10/35 quai da ha", Short.valueOf((short) 10), Short.valueOf(p.subQuest.monsterKilled[0]));

            LauDai.dat(false);
            Kiem.dung("he cu (phu hang ngay): qua ngay huy nhu ban goc", NhiemVuLauDaiGame.huyPhuQuaNgay(p));
            Kiem.bang("he cu: khe phu trong", Short.valueOf((short) -1), Short.valueOf(p.subQuest.id_quest));
            Kiem.sai("khe trong: khong huy lan nua", NhiemVuLauDaiGame.huyPhuQuaNgay(p));

            // ca hai duong qua ngay di qua ham chung (khong con xoa subQuest truc tiep)
            for (final String f : new String[]{"src/real/CharCapNhat.java", "src/real/CharNapDuLieu.java"}) {
                final String s = new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
                Kiem.dung(f + " goi huyPhuQuaNgay", s.contains("NhiemVuLauDaiGame.huyPhuQuaNgay(self)"));
                Kiem.sai(f + " khong tu xoa subQuest", s.contains("self.subQuest.reset()"));
            }
        } catch (final Exception e) {
            Kiem.dung("khong ngoai le: " + e, false);
        } finally {
            LauDai.dat(cu);
            Map.allSubQuest.remove(ID);
            Map.gemTemplate = gemCu;
        }
    }
}
