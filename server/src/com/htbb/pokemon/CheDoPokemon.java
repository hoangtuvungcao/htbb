package com.htbb.pokemon;

/**
 * Co {@code sv.che_do_pokemon} trong server.ini (quyet dinh Q8 o
 * docs/pokemon/KE_HOACH_CHUYEN_DOI.md). MOI cho re nhanh Pokemon trong src/ doc
 * qua {@link #bat()} - khong cho nao duoc tu doc server.ini lan nua.
 *
 * Mac dinh TAT khi thieu dong: server.ini cu (va bo test characterization chay
 * ngoai server) giu nguyen hanh vi game goc. Ban mau vps/server.ini.tmpl tren
 * nhanh pokemon/chuyen-doi dat =1.
 *
 * Chi doc MOT lan luc bat may (TeamServer). Doi gia tri phai khoi dong lai: bat
 * giua chung thi nhan vat dang online da nap he ngu hanh, quai da sinh voi he cu.
 */
public final class CheDoPokemon {

    /** Ten khoa trong server.ini. */
    public static final String KHOA = "sv.che_do_pokemon";

    private static volatile boolean bat = docTuTepCauHinh();

    /**
     * Doc som tu server.ini ngay khi lop duoc nap: khoi static cua real.Map nap san ban do gui client
     * TRUOC khi TeamServer doc cau hinh, neu cho TeamServer goi dat() thi Map da lay bo cuc cu.
     */
    private static boolean docTuTepCauHinh() {
        try (java.io.InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get("server.ini"))) {
            final java.util.Properties p = new java.util.Properties();
            p.load(in);
            return docGiaTri(p.getProperty(KHOA));
        } catch (Exception e) {
            return false;
        }
    }

    private CheDoPokemon() {
    }

    /** Che do Pokemon dang bat khong. Ham DUY NHAT moi cho re nhanh duoc dung. */
    public static boolean bat() {
        return bat;
    }

    /** Chi TeamServer (luc doc server.ini) va test duoc goi. */
    public static void dat(final boolean giaTri) {
        bat = giaTri;
    }

    /** "1" (co the co khoang trang) = bat; moi gia tri khac, ke ca null = tat. */
    public static boolean docGiaTri(final String giaTri) {
        return giaTri != null && "1".equals(giaTri.trim());
    }

    /**
     * Gia tri {@code he} ghi xuong client trong goi 3/5 cua NHAN VAT.
     *
     * Client cu chi hieu ngu hanh 0..4 (Monster.cs ve icon bang nh[he] - mang 5 o,
     * Char.cs so (he + 2) % 5). Khi bat che do Pokemon, Char.he la he Pokemon 0..17
     * nen phai doi ve ngu hanh truoc khi gui, khong thi client tinh sai/nem loi.
     * He Pokemon that di bang dex (lenh -90), client tu suy ra tu dex.
     * Tat che do: tra nguyen gia tri (dung y nhu truoc).
     */
    public static byte heGuiClient(final byte he) {
        if (!bat) {
            return he;
        }
        return (byte) HePokemon.nguHanhCu(he);
    }
}
