package real;

import com.htbb.pokemon.MoTaVatPham;
import io.Message;

/**
 * Gui bang mo ta vat pham dang thuoc (com.htbb.pokemon.MoTaVatPham) xuong client &gt;= {@link MoTaVatPham#BAN_CLIENT},
 * mot lan moi phien (luc vao ban do dau tien).
 *
 * <pre>
 * -104 S-&gt;C kieu 9: u8 9, UTF khoi dong. Moi dong "id \t loai \t mo ta \t 1|0 dung tu tui \t ly do", noi bang "\n".
 * </pre>
 * Nhieu goi (moi goi &lt;= {@link #DONG_MOI_GOI} dong) cho UTF khong qua 64 KB. Client &lt; 914 khong nhan (kieu 9 se
 * hien thanh thong bao) - client cu giu the vat pham cu.
 */
public final class MoTaVatPhamGame {

    static final int DONG_MOI_GOI = 30;

    private MoTaVatPhamGame() {
    }

    public static boolean coClient(final Char p) {
        return p != null && p.getSession() != null && p.getSession().banClient >= MoTaVatPham.BAN_CLIENT;
    }

    /** Cac khoi van ban se gui (kiem thu). */
    public static java.util.List<String> khoi() {
        final java.util.List<String> ds = new java.util.ArrayList<String>();
        final String[][] b = MoTaVatPham.bang();
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (final String[] d : b) {
            if (n > 0) {
                sb.append('\n');
            }
            sb.append(d[0]).append('\t').append(d[1]).append('\t').append(d[2]).append('\t').append(d[3]).append('\t').append(d[4]);
            if (++n >= DONG_MOI_GOI) {
                ds.add(sb.toString());
                sb = new StringBuilder();
                n = 0;
            }
        }
        if (n > 0) {
            ds.add(sb.toString());
        }
        return ds;
    }

    public static void gui(final Char p) {
        if (!coClient(p) || p.daGuiMoTaVatPham) {
            return;
        }
        p.daGuiMoTaVatPham = true;
        for (final String s : khoi()) {
            try {
                final Message m = new Message(DungVatPham.LENH);
                m.dos.writeByte(MoTaVatPham.KIEU_GOI);
                m.dos.writeUTF(s);
                p.sendMessage(m);
            } catch (final Exception ignored) {
            }
        }
    }
}
