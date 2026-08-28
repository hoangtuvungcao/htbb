package real;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * Doc lai goi 26 do chinh server vua dung, BANG DUNG TRINH TU CUA CLIENT 2.7.6.
 *
 * Vi sao can: goi 26 dai ~155 KB, gom 9 khoi noi duoi nhau khong co moc phan
 * cach. Lech MOT byte o khoi dau la moi khoi sau doc ra rac, ma ben client moi
 * khoi lai boc trong try/catch RONG -> khong bao loi gi het, chi la thu cuoi
 * khong hien, hanh trang trong. Nhin log server khong the biet duoc.
 *
 * Trinh tu duoi day chep tu ban dich nguoc client 2.7.6
 * (game/networklogic/MessageHandler.java, case 26). Doc het ma con du byte,
 * hoac thieu byte, tuc la hai ben khong khop -> in ra cho biet lech o dau.
 *
 * Chi in MOT lan cho moi chuoi phien ban, de khong lam day log.
 */
public final class KiemGoi26 {

    private KiemGoi26() {
    }

    private static final java.util.Set<String> daKiem =
            java.util.Collections.synchronizedSet(new java.util.HashSet<String>());

    /** Id anh thu cuoi tool phai co trong khoi 5 thi client moi ve duoc linh thu. */
    public static final int[] ID_THU_CUOI_TOOL = { 8756, 8762, 8764, 8765, 8934, 8936, 8975, 8976 };

    /**
     * In mot dong ket luan cho moi chuoi phien ban, chi mot lan.
     *
     * @param khung mang byte DA DONG KHUNG cua Message (co byte lenh + do dai o dau)
     * @param ver   phien ban client se nhan goi nay
     */
    public static void kiem(final byte[] khung, final String ver) {
        final String khoa = (ver == null) ? "(khong ro)" : ver;
        if (!daKiem.add(khoa)) {
            return;
        }
        System.out.println("[GOI26] ver=" + khoa + " " + phanTich(khung));
    }

    /**
     * Doc het goi va tra ve mot dong mo ta. Khong in, khong nho trang thai — de
     * kiem thu goi thang duoc.
     *
     * @param khung mang byte DA DONG KHUNG cua Message
     */
    public static String phanTich(final byte[] khung) {
        final StringBuilder sb = new StringBuilder();
        try {
            if (khung == null || khung.length < 3) {
                return "goi rong hoac qua ngan";
            }
            // Bo phan dong khung: [-128][lenh][do dai 4 byte] hoac [lenh][do dai 2 byte].
            final int batDau = (khung[0] == -128) ? 6 : 3;
            final DataInputStream d = new DataInputStream(
                    new ByteArrayInputStream(khung, batDau, khung.length - batDau));
            sb.append("tong=").append(khung.length - batDau).append("B");

            final int soMonster = d.readUnsignedByte();
            final int lenEffSh = d.readShort();
            sb.append(" monster=").append(soMonster).append(" effpack=").append(lenEffSh).append("B");
            if (lenEffSh < 0) {
                return ket(sb, "effpack am -> client nem NegativeArraySizeException, MAT CA GOI 26");
            }
            d.skipBytes(lenEffSh);

            final int n3 = d.readUnsignedByte();
            for (int i = 0; i < n3; ++i) {
                final int len = d.readShort();
                if (len < 0) {
                    return ket(sb, "hieu ung dong o thu " + i + " do dai am -> client dut o day");
                }
                d.skipBytes(len);
            }
            sb.append(" effdong=").append(n3);

            final int n4 = d.readUnsignedByte();
            for (int i = 0; i < n4; ++i) {
                d.readShort();
                final int len = d.readShort();
                if (len < 0) {
                    return ket(sb, "pet o thu " + i + " do dai am -> client dut o day");
                }
                d.skipBytes(len);
            }
            sb.append(" pet=").append(n4);

            // Client 2.7.6 doc so luong nay bang SHORT. Ban cu ghi bang byte -> lech.
            final int n5 = d.readShort();
            sb.append(" eff=").append(n5);
            if (n5 < 0) {
                return ket(sb, "so hieu ung am -> client bo qua CA khoi, ALL_DATA_EFF rong,"
                        + " KHONG ve duoc thu cuoi tool");
            }
            final java.util.Set<Integer> idDaGui = new java.util.HashSet<Integer>();
            for (int i = 0; i < n5; ++i) {
                idDaGui.add(Integer.valueOf(d.readShort()));
                final int len = d.readShort();
                if (len < 0) {
                    return ket(sb, "hieu ung o thu " + i + " do dai am -> client dut o day");
                }
                d.skipBytes(len);
            }
            final StringBuilder thieu = new StringBuilder();
            for (int i = 0; i < ID_THU_CUOI_TOOL.length; ++i) {
                if (!idDaGui.contains(Integer.valueOf(ID_THU_CUOI_TOOL[i]))) {
                    thieu.append(thieu.length() == 0 ? "" : ",").append(ID_THU_CUOI_TOOL[i]);
                }
            }
            sb.append(" thucuoi=").append(thieu.length() == 0 ? "du" : ("THIEU " + thieu));

            final int soHang = d.readByte();
            sb.append(" dxhorse=").append(soHang);
            if (soHang < 0) {
                return ket(sb, "so hang dx_horse am -> client dung bang du phong 1 hang,"
                        + " thu cuoi tool nao co indexdxy > 0 se nem loi luc ve");
            }
            for (int i = 0; i < soHang; ++i) {
                d.skipBytes(8);
            }
            // Client lap CO DINH 2 lan cho moi bang: WP, PP, AVT.
            final String[] tenBang = { "WP", "PP", "AVT" };
            for (int bang = 0; bang < 3; ++bang) {
                for (int i = 0; i < 2; ++i) {
                    final int len = d.readByte();
                    if (len < 0) {
                        return ket(sb, "bang " + tenBang[bang] + " hang " + i + " do dai am");
                    }
                    d.skipBytes(len);
                }
            }

            final int con = d.available();
            if (con != 0) {
                return ket(sb, "doc xong van con thua " + con + " byte -> hai ben KHONG khop");
            }
            return ket(sb, "khop het, khong thua byte nao");
        } catch (final Exception e) {
            return ket(sb, "DOC DUT GIUA CHUNG: " + e);
        }
    }

    private static String ket(final StringBuilder sb, final String ketLuan) {
        return sb.append(" | ").append(ketLuan).toString();
    }
}
