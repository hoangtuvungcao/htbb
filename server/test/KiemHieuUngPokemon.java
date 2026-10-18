import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Doi chieu hieu ung chieu Pokemon (viec K3) giua docs/pokemon/bang_chieu.csv,
 * docs/pokemon/bang_hieu_ung.csv, bang client Resources/poke/hu_bang.bytes va cac anh
 * hu_*.bytes (deu sinh boi tools/pokemon/tai_hieu_ung_pmd.py).
 *
 * VI SAO: client tra (lop, chieu) trong hu_bang.bytes roi cat khung theo rong/cao/soCot;
 * them chieu moi vao bang_chieu.csv ma quen chay lai tool, hoac anh khong khop bang khung,
 * thi chieu do am tham quay ve hieu ung cu hoac ve lech khung ma khong bao loi.
 */
public final class KiemHieuUngPokemon {

    private static final String F_CHIEU = "../docs/pokemon/bang_chieu.csv";
    private static final String F_HU = "../docs/pokemon/bang_hieu_ung.csv";
    private static final String THU_MUC = "../client_pc_ios/ClientUnity/Assets/Resources/poke/";
    private static final String F_BANG_CLIENT = THU_MUC + "hu_bang.bytes";
    private static final String F_CHAR = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/Char.cs";

    public static void chay() {
        Kiem.nhom("Hieu ung chieu Pokemon (K3: bang_hieu_ung.csv, hu_*.bytes)");
        final List<String> chieu = doc(F_CHIEU);
        final List<String> hu = doc(F_HU);
        final List<String> client = doc(F_BANG_CLIENT);
        Kiem.dung("co " + F_CHIEU, chieu != null);
        Kiem.dung("co " + F_HU, hu != null);
        Kiem.dung("co " + F_BANG_CLIENT, client != null);
        if (chieu == null || hu == null || client == null) {
            return;
        }
        final Map<String, String[]> theoKhoa = new HashMap<>();
        for (int i = 1; i < hu.size(); i++) {
            final String[] o = hu.get(i).split(",", -1);
            if (o.length >= 12) {
                theoKhoa.put(o[0] + "," + o[1], o);
            }
        }
        final Map<String, String> dongClient = new HashMap<>();
        for (final String s : client) {
            final String t = s.trim();
            if (t.isEmpty() || t.startsWith("#")) {
                continue;
            }
            final String[] o = t.split(",", -1);
            if (o.length == 10) {
                dongClient.put(o[0] + "," + o[1], t);
            }
        }
        int thieu = 0;
        int lechClient = 0;
        int soChieu = 0;
        for (int i = 1; i < chieu.size(); i++) {
            final String[] o = chieu.get(i).split(",", -1);
            if (o.length < 5 || o[0].trim().isEmpty()) {
                continue;
            }
            soChieu++;
            final String k = o[0] + "," + o[2];
            final String[] h = theoKhoa.get(k);
            if (h == null || !h[2].equals(o[4])) {
                thieu++;
                Kiem.dung("bang_hieu_ung.csv co chieu " + k + " (" + o[4] + ")", false);
                continue;
            }
            // class,so_hieu_skill,ten_moi,particle,file_client,rong,cao,so_khung,so_hang,so_cot,kieu_ve,tick
            final String ten = h[4].replace(".bytes", "");
            final String mongDoi = String.join(",", h[0], h[1], ten, h[5], h[6], h[7], h[8], h[9], h[10], h[11]);
            if (!mongDoi.equals(dongClient.get(k))) {
                lechClient++;
                Kiem.bang("hu_bang.bytes dong " + k, mongDoi, dongClient.get(k));
            }
        }
        Kiem.dung("so chieu trong bang_chieu.csv > 0", soChieu > 0);
        Kiem.bang("so chieu thieu particle", Integer.valueOf(0), Integer.valueOf(thieu));
        Kiem.bang("so dong hu_bang.bytes lech bang_hieu_ung.csv", Integer.valueOf(0), Integer.valueOf(lechClient));
        Kiem.bang("hu_bang.bytes khong thua dong", Integer.valueOf(soChieu), Integer.valueOf(dongClient.size()));

        final Map<String, Boolean> daKiem = new HashMap<>();
        long tong = 0;
        int anhLoi = 0;
        for (final String[] h : theoKhoa.values()) {
            if (daKiem.containsKey(h[4])) {
                continue;
            }
            daKiem.put(h[4], Boolean.TRUE);
            final File f = new File(THU_MUC + h[4]);
            final int[] wh = kichThuocPng(f);
            if (wh == null) {
                anhLoi++;
                Kiem.dung(h[4] + " ton tai va la PNG", false);
                continue;
            }
            tong += f.length();
            final int rong = Integer.parseInt(h[5]);
            final int cao = Integer.parseInt(h[6]);
            final int n = Integer.parseInt(h[7]);
            final int huong = Integer.parseInt(h[8]);
            final int cot = Integer.parseInt(h[9]);
            final boolean dung;
            if (huong == 4) {
                dung = cot == n && wh[0] == rong * n && wh[1] == cao * 4;
            } else {
                dung = huong == 1 && cot > 0 && wh[0] == rong * cot && wh[1] == cao * ((n + cot - 1) / cot);
            }
            if (!dung) {
                anhLoi++;
                Kiem.dung(h[4] + " " + wh[0] + "x" + wh[1] + " khop khung " + rong + "x" + cao
                        + " x" + n + " huong " + huong + " cot " + cot, false);
            }
        }
        Kiem.bang("so anh hu_*.bytes loi/lech khung", Integer.valueOf(0), Integer.valueOf(anhLoi));
        Kiem.dung("tong hu_*.bytes < 3 MB (" + tong + " byte)", tong > 0 && tong < 3L * 1024 * 1024);

        final List<String> ch = doc(F_CHAR);
        boolean hook = false;
        if (ch != null) {
            for (final String s : ch) {
                hook |= s.contains("PokeHieuUng.layChieu(");
            }
        }
        Kiem.dung("Char.setCurrentSkillByID goi PokeHieuUng.layChieu", hook);
    }

    /** {rong, cao} cua PNG, null neu khong phai PNG. */
    private static int[] kichThuocPng(final File f) {
        if (!f.isFile()) {
            return null;
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            final byte[] dau = new byte[16];
            in.readFully(dau);
            final byte[] chuan = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
            for (int i = 0; i < chuan.length; i++) {
                if (dau[i] != chuan[i]) {
                    return null;
                }
            }
            return new int[]{in.readInt(), in.readInt()};
        } catch (final IOException e) {
            return null;
        }
    }

    private static List<String> doc(final String duongDan) {
        try {
            return Files.readAllLines(Paths.get(duongDan), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return null;
        }
    }

    private KiemHieuUngPokemon() {
    }
}
