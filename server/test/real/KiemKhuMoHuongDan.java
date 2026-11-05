package real;

import io.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * KHU MO HUONG DAN (bao loi ruby Lv56 05/11/2026 "vao map nay khong lam duoc gi a"): o Khu mo (map 17, MaterialMap)
 * danh quang khi chua mac Cuoc tra ve IM LANG. Gio: danh khi chua co cuoc -> nhac (toi da 1 lan / 6 giay), vao mo ->
 * the huong dan. Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemKhuMoHuongDan {

    private KiemKhuMoHuongDan() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    static final class NV extends Char {
        final List<Message> goi = new ArrayList<Message>();

        NV(final int id) {
            super(null);
            charDBID = id;
            this.id = (short) (id % 30000);
            charname = "kiemkhumo" + id;
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
        }
    }

    public static void chay() {
        final GemTemplate[] gemCu = Map.gemTemplate;
        try {
            if (Map.gemTemplate == null) {
                Map.gemTemplate = new GemTemplate[0];
            }
            final java.util.Map<Integer, Long> luc = new HashMap<Integer, Long>();
            bang("khu mo: lan dau nhac", Boolean.TRUE, Boolean.valueOf(MaterialMap.nenNhac(luc, 7, 1_000L)));
            bang("khu mo: danh lien tiep trong 6 giay khong nhac lai", Boolean.FALSE, Boolean.valueOf(MaterialMap.nenNhac(luc, 7, 4_000L)));
            bang("khu mo: nguoi khac van duoc nhac", Boolean.TRUE, Boolean.valueOf(MaterialMap.nenNhac(luc, 8, 4_000L)));
            bang("khu mo: qua 6 giay nhac lai", Boolean.TRUE, Boolean.valueOf(MaterialMap.nenNhac(luc, 7, 1_000L + MaterialMap.NHAC_MS)));

            final NV p = new NV(880_301);
            final boolean lan1 = MaterialMap.nhacCanCuoc(p);
            final int sau1 = p.goi.size();
            final boolean lan2 = MaterialMap.nhacCanCuoc(p);
            bang("khu mo: chua mac cuoc -> gui 1 thong bao", "true/1", lan1 + "/" + sau1);
            bang("khu mo: don thu hai ngay sau khong spam", "false/1", lan2 + "/" + p.goi.size());

            bang("khu mo: thong bao noi can Cuoc + mua o Machamp", Boolean.TRUE,
                    Boolean.valueOf(MaterialMap.CAN_CUOC.contains("Cuốc") && MaterialMap.CAN_CUOC.contains("Machamp")));
            bang("khu mo: huong dan noi cach ve (Pidgeot)", Boolean.TRUE, Boolean.valueOf(MaterialMap.HUONG_DAN.contains("Pidgeot")));

            final String src = KiemCheTaoNguyenTuDoc.doc("src/real/MaterialMap.java");
            bang("khu mo: doAttackMonster nhac khi itemAx null (khong con return im lang)", Boolean.TRUE,
                    Boolean.valueOf(src.contains("if (p.itemAx == null) {\n            nhacCanCuoc(p);\n            return;")));
            final int iJoin = src.indexOf("public void playerJoin(Char player)");
            bang("khu mo: playerJoin gui huong dan", Boolean.TRUE,
                    Boolean.valueOf(iJoin > 0 && src.indexOf("HUONG_DAN", iJoin) > iJoin));
            bang("ve khu mo (91): mo ta nhac mac Cuoc", Boolean.TRUE,
                    Boolean.valueOf(KiemCheTaoNguyenTuDoc.doc("src/com/htbb/pokemon/MoTaVatPham.java").contains("phải mặc Cuốc")));
        } catch (final Exception e) {
            bang("KiemKhuMoHuongDan khong nem loi", "", String.valueOf(e));
        } finally {
            Map.gemTemplate = gemCu;
        }
    }
}
