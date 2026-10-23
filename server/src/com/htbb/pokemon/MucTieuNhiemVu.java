package com.htbb.pokemon;

/**
 * MUC TIEU NHIEM VU (nhanh sua-tien-do-nv, logic thuan - real.MucTieuNhiemVuGame noi vao game): chu "phai lam gi, o
 * dau, tra ai" cho so Nhiem vu (-64). Sinh tu CHINH du lieu dem cua may chu (kieu, mau quai, so luong, vat pham) + ban
 * do quai THAT SU xuat hien (bang sinh quai luc chay), khong tu chu scontent (co the lech: "Hạ 20 Weedle ở Pewter" khi
 * Weedle chi o Lavender).
 *
 * <pre>
 *   muc   : "Hạ 20 Weedle ở Lavender" / "Hạ Litwick ở Cerulean, nhặt 5 Gói hàng" / "Hạ 25 Pokémon hoang dã chênh không quá 5 cấp"
 *   ngan  : "Weedle ở Lavender" / "Gói hàng (Litwick, Cerulean)" - duoi ten cho client &lt; 915 (dong 1 cua hang)
 *   tra   : "Machamp (Pewter)"
 *   moTa  : "Mục tiêu: Hạ 20 Weedle ở Lavender · Trả: Machamp (Pewter)" (+ ly do khoa ban do) - o chi tiet moi client
 * </pre>
 */
public final class MucTieuNhiemVu {

    /** Kieu nhiem vu cu (InfoQuestTemplate.type). */
    public static final int GIET = 0, GIAO = 1, THU = 2, NOI = 3, GIET_CAP = 4;
    /** Ban client doc duoi goi -64 (muc / tra / khoa / co) va nhan ten goc. */
    public static final int BAN_CLIENT = 915;
    /** Co duoi -64: bit0 cua ai (o khe doi), bit1 ban do muc tieu dang khoa. */
    public static final int CO_CUA_AI = 1, CO_KHOA = 2;
    /** Ten them vao toi da (client cu cat dong 1 theo be rong - giu ngan). */
    public static final int NGAN_TOI_DA = 34;

    private MucTieuNhiemVu() {
    }

    /** Mot muc tieu: ten quai, so can, ten vat pham (thu thap; null = giet), ban do (ten, "" = khong ro). */
    public static final class Muc {
        public final String quai;
        public final int can;
        public final String vatPham;
        public final String banDo;

        public Muc(final String quai, final int can, final String vatPham, final String banDo) {
            this.quai = quai == null ? "" : quai.trim();
            this.can = can;
            this.vatPham = vatPham == null ? null : vatPham.trim();
            this.banDo = banDo == null ? "" : banDo.trim();
        }
    }

    static String o(final String banDo) {
        return banDo == null || banDo.isEmpty() ? "" : " ở " + banDo;
    }

    /**
     * Cau muc tieu day du. {@code goc}: scontent (dung cho noi chuyen / giao do / kieu khong dem duoc).
     * {@code chenhCap}: deltaLv cua kieu 4.
     */
    public static String muc(final int kieu, final Muc[] ds, final int chenhCap, final int canCap, final String goc) {
        final String g = goc == null ? "" : goc.trim();
        if (kieu == GIET_CAP) {
            final int n = ds != null && ds.length > 0 ? ds[0].can : 0;
            return "Hạ " + (n > 0 ? n + " " : "") + "Pokémon hoang dã chênh không quá " + chenhCap + " cấp";
        }
        if (ds == null || ds.length == 0 || kieu != GIET && kieu != THU) {
            return g;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ds.length; i++) {
            final Muc m = ds[i];
            if (i > 0) {
                sb.append("; ");
            }
            if (kieu == GIET) {
                sb.append(i == 0 ? "Hạ " : "hạ ").append(m.can).append(' ').append(m.quai).append(o(m.banDo));
            } else {
                sb.append(i == 0 ? "Hạ " : "hạ ").append(m.quai.isEmpty() ? "quái" : m.quai).append(o(m.banDo)).append(", nhặt ")
                        .append(m.can).append(' ').append(m.vatPham == null || m.vatPham.isEmpty() ? "vật phẩm" : m.vatPham);
            }
        }
        return sb.toString();
    }

    /** Duoi ten ngan (client &lt; 915 chi hien ten + loai + x/N): "Weedle ở Lavender", "Gói hàng (Litwick, Cerulean)". */
    public static String ngan(final int kieu, final Muc[] ds, final int chenhCap) {
        if (kieu == GIET_CAP) {
            return "quái chênh tối đa " + chenhCap + " cấp";
        }
        if (ds == null || ds.length == 0) {
            return "";
        }
        final Muc m = ds[0];
        String s;
        if (kieu == GIET) {
            s = m.quai + o(m.banDo);
        } else if (kieu == THU) {
            s = (m.vatPham == null || m.vatPham.isEmpty() ? "vật phẩm" : m.vatPham) + " (" + m.quai
                    + (m.banDo.isEmpty() ? "" : ", " + m.banDo) + ")";
        } else {
            return "";
        }
        if (ds.length > 1) {
            s += " +" + (ds.length - 1);
        }
        return s.length() > NGAN_TOI_DA ? s.substring(0, NGAN_TOI_DA).trim() : s;
    }

    /** "Machamp (Pewter)" / "Machamp" / "". */
    public static String tra(final String npc, final String noi) {
        final String n = npc == null ? "" : npc.trim();
        if (n.isEmpty()) {
            return "";
        }
        return noi == null || noi.trim().isEmpty() ? n : n + " (" + noi.trim() + ")";
    }

    /** Ten ngan NPC tu ten day du data_npc: tu viet hoa CUOI ("Y tá Chansey" -> Chansey, "Golem bán hàng 1" -> Golem). */
    public static String tenNgan(final String day) {
        if (day == null) {
            return "";
        }
        final String[] t = day.trim().split("\\s+");
        for (int i = t.length - 1; i >= 0; i--) {
            if (!t[i].isEmpty() && Character.isUpperCase(t[i].charAt(0))) {
                return t[i];
            }
        }
        return day.trim();
    }

    /**
     * Mo ta o the chi tiet (client moi va cu: Quest.decript): "Mục tiêu: … · Trả: …" + khoa + phan chi duong (buoc dia
     * diem). {@code xong}: da du (nhom 1) -> "Xong! Về trả …".
     */
    public static String moTa(final String muc, final String tra, final String khoa, final boolean xong, final String them) {
        final StringBuilder sb = new StringBuilder();
        if (xong) {
            sb.append(tra.isEmpty() ? "Xong! Về nhận thưởng." : "Xong! Về trả " + tra + ".");
        } else {
            sb.append("Mục tiêu: ").append(muc.isEmpty() ? "xem lời NPC" : muc);
            if (!tra.isEmpty()) {
                sb.append(" · Trả: ").append(tra);
            }
            sb.append('.');
        }
        if (them != null && !them.trim().isEmpty()) {
            sb.append(' ').append(them.trim());
        }
        if (!xong && khoa != null && !khoa.isEmpty()) {
            sb.append(' ').append(khoa);
        }
        return sb.toString();
    }

    /** Ten hien cho client cu: "Chứng tỏ sức mạnh · Weedle ở Lavender" (ngan rong = ten goc). */
    public static String tenCu(final String ten, final String ngan) {
        final String t = ten == null ? "" : ten;
        return ngan == null || ngan.isEmpty() ? t : t + " · " + ngan;
    }

    /** Cau nhac cua ai khi ha quai muc tieu ma chua nhan (hop 95). */
    public static String nhacCuaAi(final String quai, final String tenCuaAi, final int so, final int tong, final String moTaBuoc) {
        return "Bạn đang hạ " + quai + " nhưng chưa nhận cửa ải «" + tenCuaAi + "» (bước " + so + "/" + tong + ": " + moTaBuoc
                + "). Chỉ tính quái hạ SAU khi nhận.\nNhận ngay?";
    }
}
