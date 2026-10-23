package game.pokemon;

import java.util.Hashtable;

/**
 * MÔ TẢ VẬT PHẨM DẠNG "THUỐC" (0.9.14): loại, mô tả thật, dùng từ túi được hay không (và lý do) cho từng id potions[].
 * Máy chủ gửi bảng (com.htbb.pokemon.MoTaVatPham) qua gói -104 kiểu {@link #KIEU} lúc vào bản đồ (client &gt;= 914).
 *
 * Trước 0.9.14 thẻ vật phẩm ghi MỌI thuốc là "Bình thuốc / Hồi HP cho Pokémon. Gán vào nút thuốc HP..." - Sáo gọi
 * Tauros, vé giờ vàng, hộp quà, nguyên liệu sự kiện đều ra chữ đó. Chưa nhận bảng (máy chủ cũ): chỉ bình HP / PP có
 * chữ HP / PP, vật phẩm khác ghi "Vật phẩm" và không có mô tả bịa.
 *
 * <pre>
 * -104 S-&gt;C kiểu 9: u8 9, UTF khối dòng "id \t loại \t mô tả \t 1|0 \t lý do" nối bằng "\n" (nhiều gói).
 * </pre>
 */
public final class MoTaVatPham {
    private MoTaVatPham() {
    }

    public static final int KIEU = 9;
    public static final int[] HP = {1, 2, 3, 21, 22, 93, 94};
    public static final int[] PP = {4, 5, 6, 23, 24, 95, 96};

    /** id (Integer) -&gt; String[4] {loại, mô tả, "1"/"0", lý do} */
    static final Hashtable BANG = new Hashtable();
    /** số dòng đã nhận (kiểm thử) */
    public static int soDong;

    static boolean trong(int[] a, int v) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == v) {
                return true;
            }
        }
        return false;
    }

    public static boolean laHp(int id) {
        return trong(HP, id);
    }

    public static boolean laPp(int id) {
        return trong(PP, id);
    }

    /** Chỉ bình HP / PP gán vào nút thuốc / ô nhanh. */
    public static boolean ganONhanh(int id) {
        return laHp(id) || laPp(id);
    }

    /** Nạp một khối của gói -104 kiểu 9. */
    public static void nap(String khoi) {
        if (khoi == null) {
            return;
        }
        int i = 0;
        int n = khoi.length();
        while (i < n) {
            int e = khoi.indexOf('\n', i);
            if (e < 0) {
                e = n;
            }
            napDong(khoi.substring(i, e));
            i = e + 1;
        }
    }

    static void napDong(String d) {
        String[] f = new String[5];
        int k = 0, i = 0;
        while (k < 4) {
            int e = d.indexOf('\t', i);
            if (e < 0) {
                return;
            }
            f[k++] = d.substring(i, e);
            i = e + 1;
        }
        f[4] = d.substring(i);
        try {
            int id = Integer.parseInt(f[0].trim());
            BANG.put(new Integer(id), new String[]{f[1], f[2], f[3], f[4]});
            soDong = BANG.size();
        } catch (Throwable e) {
            // dòng hỏng: bỏ qua
        }
    }

    static String[] dong(int id) {
        return (String[]) BANG.get(new Integer(id));
    }

    public static boolean coBang() {
        return !BANG.isEmpty();
    }

    public static String loai(int id) {
        String[] d = dong(id);
        if (d != null && d[0].length() > 0) {
            return d[0];
        }
        return ganONhanh(id) ? "Bình thuốc" : "Vật phẩm";
    }

    /** null = không có mô tả (không bịa chữ HP cho vật phẩm khác). */
    public static String moTa(int id) {
        String[] d = dong(id);
        if (d != null && d[1].length() > 0) {
            return d[1];
        }
        if (laHp(id)) {
            return "Hồi HP cho Pokémon. Gán vào nút thuốc HP ở HUD để dùng nhanh.";
        }
        if (laPp(id)) {
            return "Hồi PP cho Pokémon. Gán vào nút thuốc PP ở HUD để dùng nhanh.";
        }
        return null;
    }

    public static boolean dungDuoc(int id) {
        String[] d = dong(id);
        return d == null || !"0".equals(d[2]);
    }

    /** Lý do không dùng từ túi (null = dùng được). */
    public static String lyDo(int id) {
        if (dungDuoc(id)) {
            return null;
        }
        String[] d = dong(id);
        return d[3].length() > 0 ? d[3] : "Không dùng trực tiếp từ túi.";
    }

    /** Tên gọn (bỏ phần mô tả cũ gắn sau dòng mới / ". " trong tên data_potion). */
    public static String tenGon(String t) {
        if (t == null) {
            return null;
        }
        int n = t.indexOf('\n');
        if (n > 0) {
            t = t.substring(0, n);
        }
        int c = t.indexOf(". ");
        if (c > 0) {
            t = t.substring(0, c);
        }
        t = t.trim();
        while (t.endsWith(".")) {
            t = t.substring(0, t.length() - 1).trim();
        }
        return t;
    }

    public static void xoa() {
        BANG.clear();
        soDong = 0;
    }
}
