import io.Message;
import io.Session;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.CharManager;
import real.ChieuBiDong;

/**
 * Bao loi 05/11/2026 (dong Squirtle): "skill con rua chu dong hay bi dong ma dung khong duoc, tang diem lau roi ma khong
 * cai vao auto duoc". Rut vao mai (o 5) la chieu BI DONG (SKILL_CAN_BUFF_TO_USER = -1): hieu ung luon co (CharChiSo +X%
 * phong thu) nhung khong cho nao noi cho nguoi choi. Kiem:
 * <ul>
 * <li>moi dong dung MOT chieu bi dong, dung o ChieuBiDong.O, khop CharManager.isBuffActive;</li>
 * <li>Squirtle: o 5 bi dong, o 4 (Pha mai) chu dong; hieu ung theo cap = SKILL_DAM_PERCENT;</li>
 * <li>lenh 51 voi hieu ung bi dong: KHONG ghi hieu ung / tru PP, tra cau "Rút vào mai là chiêu bị động ..." (khong con
 * "Không thể sử dụng" chung chung); hieu ung khong bi dong khong bi anh huong;</li>
 * <li>cong diem vao chieu bi dong: bao "bi dong, luon co hieu luc"; chieu khac khong bao;</li>
 * <li>client_jar: PokeChieu.O_BI_DONG va SkillManager.SKILL_CAN_BUFF_TO_USER khop may chu; bang ky nang ghi "Kiểu"
 * (Chủ động / Bổ trợ / Bị động), bang Auto cu bao cau thay vi im lang.</li>
 * </ul>
 */
public final class KiemChieuBiDong {

    private KiemChieuBiDong() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static short idKe = 27600;

    public static void chay() {
        Kiem.nhom("Chieu bi dong (Rut vao mai...): noi ro 'bi dong, luon co hieu luc' (ChieuBiDong)");
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            bang();
            lenh51();
            congDiem();
            client();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
        }
    }

    static void bang() {
        for (int c = 0; c < CharManager.EFF_BUFF_SKILL.length; c++) {
            int so = 0;
            int o = -1;
            for (int b = 0; b < CharManager.EFF_BUFF_SKILL[c].length; b++) {
                final boolean bd = ChieuBiDong.la(c, b + 4);
                Kiem.bang("dong " + c + " o " + (b + 4) + ": bi dong <=> !isBuffActive", Boolean.valueOf(!CharManager.isBuffActive(c,
                        CharManager.EFF_BUFF_SKILL[c][b]) && CharManager.SKILL_CAN_BUFF_TO_USER[c][b] == -1), Boolean.valueOf(bd));
                if (bd) {
                    so++;
                    o = b + 4;
                }
            }
            Kiem.bang("dong " + c + ": dung mot chieu bi dong", Integer.valueOf(1), Integer.valueOf(so));
            Kiem.bang("dong " + c + ": o bi dong = ChieuBiDong.O", Integer.valueOf(ChieuBiDong.O[c]), Integer.valueOf(o));
            Kiem.bang("dong " + c + ": ten = MessageCreator.SKILL_NAME", real.MessageCreator.SKILL_NAME[c][o], ChieuBiDong.TEN[c]);
        }
        Kiem.dung("Squirtle o 5 (Rut vao mai) bi dong", ChieuBiDong.la(3, 5));
        Kiem.dung("Squirtle o 4 (Pha mai) KHONG bi dong", !ChieuBiDong.la(3, 4));
        Kiem.dung("o tan cong (6) khong bi dong", !ChieuBiDong.la(3, 6));
        Kiem.bang("Rut vao mai cap 9 = +SKILL_DAM_PERCENT[3][5][9]% phong thu",
                "+" + CharManager.SKILL_DAM_PERCENT[3][5][9] + "% phòng thủ", ChieuBiDong.hieuUng(3, 9));
        Kiem.bang("chua hoc: 0%", Integer.valueOf(0), Integer.valueOf(ChieuBiDong.phanTram(3, 0)));
        Kiem.dung("cau phu noi khong cai vao Tu dong danh", ChieuBiDong.cauPhu(3, 9).contains("Tự động đánh"));
    }

    static Char nhanVat(final int lop) {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.id = idKe++;
        p.charname = "kiembd" + p.id;
        p.lvDetail.lv = 64;
        p.charClass = (byte) lop;
        p.maxhp = 1000;
        p.hp = 1000;
        p.maxmp = 500;
        p.mp = 500;
        p.skill[4] = 6;
        p.skill[5] = 9;
        return p;
    }

    static Message goi51(final int charID, final int eff) throws Exception {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(b);
        d.writeShort(charID);
        d.writeByte(0);
        d.writeByte(eff);
        d.writeShort(0);
        return new Message(51, b.toByteArray());
    }

    static String chu() {
        final StringBuilder s = new StringBuilder();
        for (final Message m : GOI) {
            s.append(new String(m.toByteArray(), StandardCharsets.UTF_8)).append('\n');
        }
        return s.toString();
    }

    static void lenh51() throws Exception {
        GOI.clear();
        final Char p = nhanVat(3);
        final int mp0 = p.mp;
        p.setBuff2Char(goi51(p.id, 20)); // Rut vao mai = hieu ung 20 cua Squirtle
        final String s = chu();
        Kiem.dung("lenh 51 Rut vao mai: bao 'Rút vào mai là chiêu bị động'", s.contains("Rút vào mai là chiêu bị động"));
        Kiem.dung("... kem hieu ung that (+65% phong thu o cap 9)", s.contains(ChieuBiDong.hieuUng(3, 9)));
        Kiem.dung("... khong con 'Không thể sử dụng' chung chung", !s.contains("Không thể sử dụng"));
        Kiem.bang("khong tru PP", Integer.valueOf(mp0), Integer.valueOf(p.mp));
        Kiem.bang("khong ghi hieu ung 20", Integer.valueOf(-1), Integer.valueOf(p.arrayBuff[1]));
        // hieu ung khong thuoc dong (vd 25): van "Khong the su dung"
        GOI.clear();
        p.setBuff2Char(goi51(p.id, 25));
        Kiem.dung("hieu ung khong cua dong: van 'Không thể sử dụng'", chu().contains("Không thể sử dụng"));
        // Pha mai (19) chu dong: van dung duoc nhu cu
        GOI.clear();
        p.setBuff2Char(goi51(p.id, 19));
        Kiem.bang("Pha mai (chu dong) van dung duoc: hieu ung 19", Integer.valueOf(19), Integer.valueOf(p.arrayBuff[0]));
        Kiem.dung("Pha mai: khong bao 'bi dong'", !chu().contains("bị động"));
    }

    static void congDiem() {
        GOI.clear();
        final Char p = nhanVat(3);
        real.HocChieuGame.baoBiDong(p, 5);
        final String s = chu();
        Kiem.dung("cong diem Rut vao mai: bao bi dong + cap", s.contains("Rút vào mai là chiêu bị động (cấp 9)"));
        GOI.clear();
        real.HocChieuGame.baoBiDong(p, 4);
        real.HocChieuGame.baoBiDong(p, 7);
        Kiem.bang("cong diem chieu chu dong: khong bao", Integer.valueOf(0), Integer.valueOf(GOI.size()));
        GOI.clear();
        final Char c = nhanVat(0);
        real.HocChieuGame.baoBiDong(c, 4);
        Kiem.dung("Charmander Man khoi (o 4): bao bi dong", chu().contains("Màn khói là chiêu bị động"));
    }

    static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static void client() {
        final String pc = doc("../client_jar/src/game/pokemon/PokeChieu.java");
        final Matcher m = Pattern.compile("O_BI_DONG = \\{([^}]*)\\}").matcher(pc);
        Kiem.dung("client PokeChieu.O_BI_DONG co", m.find());
        if (m.find(0)) {
            final String[] a = m.group(1).split(",");
            final StringBuilder sv = new StringBuilder();
            final StringBuilder cl = new StringBuilder();
            for (int i = 0; i < ChieuBiDong.O.length; i++) {
                sv.append(ChieuBiDong.O[i]).append(',');
                cl.append(i < a.length ? a[i].trim() : "?").append(',');
            }
            Kiem.bang("client O_BI_DONG = may chu ChieuBiDong.O", sv.toString(), cl.toString());
        }
        final String sm = doc("../client_jar/src/game/model/skill/SkillManager.java");
        final Matcher b = Pattern.compile("SKILL_CAN_BUFF_TO_USER = \\{(.*?)\\};").matcher(sm);
        Kiem.dung("client SKILL_CAN_BUFF_TO_USER co", b.find());
        if (b.find(0)) {
            final StringBuilder sv = new StringBuilder();
            for (final byte[] r : CharManager.SKILL_CAN_BUFF_TO_USER) {
                for (final byte x : r) {
                    sv.append(x).append(',');
                }
                sv.append('|');
            }
            final StringBuilder cl = new StringBuilder();
            final Matcher h = Pattern.compile("\\{([-0-9, ]+)\\}").matcher(b.group(1));
            while (h.find()) {
                for (final String x : h.group(1).split(",")) {
                    cl.append(x.trim()).append(',');
                }
                cl.append('|');
            }
            Kiem.bang("client SKILL_CAN_BUFF_TO_USER = may chu", sv.toString(), cl.toString());
        }
        final String bc = doc("../client_jar/src/game/pokemon/BangChieu.java");
        Kiem.dung("bang ky nang ghi 'Kiểu' (Chủ động / Bổ trợ / Bị động)", bc.contains("\"Kiểu\"") && pc.contains("\"Bị động\""));
        final String ba = doc("../client_jar/src/game/render/screen/BangAuto.java");
        Kiem.dung("bang Auto cu: bam chieu bi dong -> hop thoai (khong im lang)", ba.contains("PokeChieu.cauBiDong"));
    }
}
