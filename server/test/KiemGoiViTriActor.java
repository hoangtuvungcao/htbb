import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CHARACTERIZATION TEST: goi tin vi tri actor (Message 4) khong duoc phep chua ban ghi
 * GHI DO.
 *
 * LOI DA XAY RA THAT (05/09/2026, nguoi choi bao "hinh nhan vat cua toi va cua moi nguoi
 * khac luc an luc hien", 1 giay 1-2 lan, o MOI ban do, ke ca nhan vat cua chinh minh):
 *
 * Char.writeActorPos ghi THANG tung truong vao m.dos va chi bat IOException. Nhieu dong
 * trong than ham co the nem loi KHAC ma khong ai bat - posNPCInVilage[inCountry] ngoai
 * bien (mang luon dai 3), getMonsterTemplate() tra null, getIdCharThanThu()/canFocus()
 * NPE. Khi do vai truong DAU da nam trong goi, cac truong SAU thi khong.
 *
 * Goi van duoc gui: CharCapNhat.updateNearChar bat Exception roi "continue", sau do van
 * "if (havedata) self.sendMessage(mActor)". Client doc ban ghi thieu byte nen LECH NHIP -
 * no lay byte dau cua actor ke tiep lam toa do, va toan bo phan con lai cua goi thanh rac.
 * Vi vay trieu chung khong phai "mat mot NPC" ma la MAT SACH moi thu trong goi. Tick sau
 * (Map.DELAY_UPDATE_MAP = 300ms) neu khong dinh loi thi ve lai -> nhap nhay ~1-2 lan/giay.
 *
 * Phep kiem gom hai lop:
 *   1. Quet ma nguon: writeActorPos phai ghi vao bo dem tam va bat Exception (khong phai
 *      rieng IOException); hai cho doc posNPCInVilage[...] phai chan bien.
 *   2. So hoc that: dung chinh DataOutputStream de chung minh ban ghi ghi do lam lech nhip
 *      goi, con cach ghi qua bo dem tam thi khong.
 */
public final class KiemGoiViTriActor {

    private static final String F_CHAR = "src/real/Char.java";
    private static final String F_CAP_NHAT = "src/real/CharCapNhat.java";

    private KiemGoiViTriActor() {
    }

    public static void chay() {
        Kiem.nhom("Goi vi tri actor (Message 4) - khong duoc gui ban ghi ghi do");

        kiemMaNguon();
        kiemLechNhip();
    }

    private static void kiemMaNguon() {
        final String ch = doc(F_CHAR);
        Kiem.dung("doc duoc " + F_CHAR, !ch.isEmpty());
        if (ch.isEmpty()) {
            return;
        }

        final int dau = ch.indexOf("public Message writeActorPos(final Message m, final Actor a) {");
        Kiem.dung("tim thay writeActorPos", dau >= 0);
        if (dau < 0) {
            return;
        }
        final String than = ch.substring(dau, Math.min(ch.length(), dau + 3200));

        // Ghi vao bo dem tam, KHONG ghi thang vao goi that.
        Kiem.dung("writeActorPos ghi vao bo dem tam (ByteArrayOutputStream)",
                than.contains("new java.io.ByteArrayOutputStream("));
        Kiem.dung("writeActorPos chi noi vao goi that khi ban ghi hoan chinh",
                than.contains("m.dos.write(dem.toByteArray())"));
        Kiem.sai("writeActorPos KHONG con ghi thang truong dau vao m.dos",
                than.contains("m.dos.writeByte(a.cat)"));

        // Phai bat MOI loai loi, khong chi IOException - day chinh la cho lot truoc day.
        Kiem.dung("writeActorPos bat Exception (khong chi rieng IOException)",
                than.contains("} catch (final Exception ex) {"));
        Kiem.dung("hong thi tra ve goi nguyen ven, bo qua dung actor do",
                than.contains("return m;"));

        // Chan bien mang posNPCInVilage (luon dai 3) o CA HAI cho doc.
        Kiem.dung("writeActorPos chan bien posNPCInVilage theo inCountry",
                than.contains("this.inCountry < aa.posNPCInVilage.length"));

        final String cn = doc(F_CAP_NHAT);
        Kiem.dung("doc duoc " + F_CAP_NHAT, !cn.isEmpty());
        if (!cn.isEmpty()) {
            Kiem.dung("CharCapNhat chan bien posNPCInVilage theo inCountry",
                    cn.contains("self.inCountry < aa.posNPCInVilage.length"));
            Kiem.sai("CharCapNhat KHONG con doc posNPCInVilage ma khong chan bien",
                    cn.contains("if (aa.posNPCInVilage != null) {"));
        }
    }

    /**
     * Chung minh bang so hoc vi sao mot ban ghi ghi do lam HONG CA GOI chu khong chi hong
     * mot actor: client doc dung so byte co dinh cho moi ban ghi, nen thieu byte la moi
     * ban ghi sau do deu doc sai.
     *
     * Dung ban ghi rut gon 8 byte (cat 1, type 1, id 2, x 2, y 2) cho de theo doi - ty le
     * lech nhip khong doi khi them truong.
     */
    private static void kiemLechNhip() {
        // --- Cach CU: ghi thang, actor thu hai hong giua chung (thieu 2 byte toa do y) ---
        final ByteArrayOutputStream goiCu = new ByteArrayOutputStream();
        final DataOutputStream cu = new DataOutputStream(goiCu);
        try {
            ghiDayDu(cu, 0, 1, 100, 500, 600);   // actor 1 - hoan chinh
            cu.writeByte(0);                      // actor 2 - ghi do: cat...
            cu.writeByte(1);                      // ...type...
            cu.writeShort(101);                   // ...id, roi NEM LOI truoc khi ghi x, y
            ghiDayDu(cu, 0, 1, 102, 700, 800);   // actor 3 - hoan chinh
        } catch (final IOException e) {
            Kiem.dung("dung goi mo phong duoc", false);
            return;
        }
        final byte[] mangCu = goiCu.toByteArray();
        Kiem.bang("goi cu: 3 ban ghi nhung thieu 4 byte (2 x short toa do)",
                Integer.valueOf(8 * 3 - 4), Integer.valueOf(mangCu.length));
        Kiem.sai("goi cu KHONG chia het cho kich thuoc ban ghi -> client chac chan lech nhip",
                mangCu.length % 8 == 0);

        // Doc lai dung cach client doc (7 byte moi ban ghi) -> actor thu hai sai toa do.
        final int[][] docCu = docGoi(mangCu);
        Kiem.bang("goi cu doc ra duoc 2 ban ghi (mat han 1)", Integer.valueOf(2),
                Integer.valueOf(docCu.length));
        Kiem.bang("goi cu: ban ghi thu 2 co id dung 101", Integer.valueOf(101),
                Integer.valueOf(docCu[1][2]));
        Kiem.sai("goi cu: toa do x cua ban ghi 2 KHONG con la 0 nhu mong doi (da lech nhip)",
                docCu[1][3] == 0);

        // --- Cach MOI: bo qua han actor hong, khong ghi byte nao ---
        final ByteArrayOutputStream goiMoi = new ByteArrayOutputStream();
        final DataOutputStream moi = new DataOutputStream(goiMoi);
        try {
            ghiDayDu(moi, 0, 1, 100, 500, 600);
            // actor 2 hong -> bo dem tam bi vut, khong noi gi vao goi
            ghiDayDu(moi, 0, 1, 102, 700, 800);
        } catch (final IOException e) {
            Kiem.dung("dung goi mo phong duoc (moi)", false);
            return;
        }
        final byte[] mangMoi = goiMoi.toByteArray();
        Kiem.bang("goi moi: dung 2 ban ghi tron ven", Integer.valueOf(8 * 2),
                Integer.valueOf(mangMoi.length));
        Kiem.dung("goi moi chia het cho kich thuoc ban ghi -> client khong lech nhip",
                mangMoi.length % 8 == 0);

        final int[][] docMoi = docGoi(mangMoi);
        Kiem.bang("goi moi doc ra 2 ban ghi", Integer.valueOf(2), Integer.valueOf(docMoi.length));
        Kiem.bang("goi moi: ban ghi 1 dung id", Integer.valueOf(100), Integer.valueOf(docMoi[0][2]));
        Kiem.bang("goi moi: ban ghi 1 dung x", Integer.valueOf(500), Integer.valueOf(docMoi[0][3]));
        Kiem.bang("goi moi: ban ghi 2 dung id (actor hong khong lam hong actor sau)",
                Integer.valueOf(102), Integer.valueOf(docMoi[1][2]));
        Kiem.bang("goi moi: ban ghi 2 dung x", Integer.valueOf(700), Integer.valueOf(docMoi[1][3]));
    }

    private static void ghiDayDu(final DataOutputStream d, final int cat, final int type,
            final int id, final int x, final int y) throws IOException {
        d.writeByte(cat);
        d.writeByte(type);
        d.writeShort(id);
        d.writeShort(x);
        d.writeShort(y);
    }

    /** Doc goi theo dung cach client doc: 8 byte co dinh cho moi ban ghi. */
    private static int[][] docGoi(final byte[] goi) {
        final int soBanGhi = goi.length / 8;
        final int[][] ra = new int[soBanGhi][4];
        for (int i = 0; i < soBanGhi; i++) {
            final int o = i * 8;
            ra[i][0] = goi[o];
            ra[i][1] = goi[o + 1];
            ra[i][2] = ((goi[o + 2] & 0xFF) << 8) | (goi[o + 3] & 0xFF);
            ra[i][3] = ((goi[o + 4] & 0xFF) << 8) | (goi[o + 5] & 0xFF);
        }
        return ra;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
