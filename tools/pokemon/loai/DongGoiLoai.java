import game.pokemon.PokeDuLieu;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Dong goi sprite tung loai thanh GOI TAI THEO NHU CAU cho may chu (server/map/loai/&lt;NNNN&gt;.goi).
 *
 * Duong ong (KHONG dung client_pc_ios that, chi thu muc nhap):
 *   tools/pokemon/pmd_raw/&lt;NNNN&gt;/   --DongGoiSprite.dongGoi-->   &lt;tam&gt;/poke/&lt;NNNN&gt;_{anh,dl,cd}.bytes (dinh dang Unity)
 *                                   --DongGoiPokeJ2me.dongGoi--> &lt;tam&gt;/pk/&lt;NNNN&gt;.dl + &lt;NNNN&gt;_&lt;a&gt;.png (dinh dang jar)
 *                                   --chanDung 40x40-->          &lt;tam&gt;/pk/&lt;NNNN&gt;_c.png
 *   roi gom cac tep jar cua loai vao MOT tep goi (big-endian, DataOutputStream):
 *     'H' 'T' 'P' 'K', u8 phienBan = 1, i16 dex, u8 shiny (0), u8 n,
 *     n x (writeUTF tenTep vd "0016.dl" / "0016_0.png" / "0016_c.png", i32 len, len byte)
 *   Ten tep va noi dung GIONG HET client_jar/res/pk (PokeBody doc duoc nguyen ven).
 *
 * Bien dich + chay (o goc repo, can JDK; tools/pokemon/loai/README.md):
 *   B=/tmp/dg_loai; mkdir -p $B
 *   javac -nowarn -encoding UTF-8 -d $B client_jar/src/game/pokemon/PokeDuLieu.java \
 *       client_jar/src/game/pokemon/PokeDongTac.java client_jar/src/game/pokemon/HieuUngBang.java \
 *       client_jar/tools/DongGoiPokeJ2me.java tools/pokemon/DongGoiSprite.java tools/pokemon/loai/DongGoiLoai.java
 *   java -Djava.awt.headless=true -cp $B DongGoiLoai 1 251          # dong goi dai dex (mac dinh 1..251)
 *   java -Djava.awt.headless=true -cp $B DongGoiLoai --kiem          # kiem moi goi trong server/map/loai
 *
 * Cau hinh khung: DongGoiPokeJ2me.XA (loai chi la quai / ban dong hanh nhin xa: it khung hon, danh xa dung chung
 * anh danh gan) - giu goi nho (~ 30 KB / loai) vi J2ME tai qua mang + cache RMS 1 MB.
 */
public final class DongGoiLoai {

    static final byte[] MAGIC = {'H', 'T', 'P', 'K'};
    static final int PHIEN_BAN = 1;

    private DongGoiLoai() {
    }

    static String khoa(final int dex) {
        return String.format("%04d", dex);
    }

    public static void main(final String[] a) throws Exception {
        final File goc = new File(".").getCanonicalFile();
        final File raw = new File(goc, "tools/pokemon/pmd_raw");
        final File ra = new File(goc, "server/map/loai");
        if (a.length > 0 && a[0].equals("--kiem")) {
            System.exit(kiemHet(ra) == 0 ? 0 : 1);
            return;
        }
        final int tu = a.length > 0 ? Integer.parseInt(a[0]) : 1;
        final int den = a.length > 1 ? Integer.parseInt(a[1]) : 251;
        final File tam = Files.createTempDirectory("dg_loai").toFile();
        final File poke = new File(tam, "poke");
        poke.mkdirs();
        ra.mkdirs();
        int so = 0, loi = 0;
        long tong = 0;
        final List<String> khongCo = new ArrayList<String>();
        for (int dex = tu; dex <= den; dex++) {
            final String k = khoa(dex);
            final File tm = new File(raw, k);
            if (!new File(tm, "AnimData.xml").isFile()) {
                khongCo.add(k);
                continue;
            }
            final File pk = new File(tam, "pk_" + k);
            pk.mkdirs();
            try {
                DongGoiSprite.dongGoi(tm, k, poke);
                DongGoiPokeJ2me.cau = DongGoiPokeJ2me.XA;
                DongGoiPokeJ2me.dongGoi(poke, pk, k, k, false);
                DongGoiPokeJ2me.ghiAnh(DongGoiPokeJ2me.chanDung(ImageIO.read(new File(poke, k + "_cd.bytes")), false),
                        new File(pk, k + "_c.png"));
                final Map<String, byte[]> tep = new LinkedHashMap<String, byte[]>();
                tep.put(k + ".dl", Files.readAllBytes(new File(pk, k + ".dl").toPath()));
                final String[] ds = pk.list();
                Arrays.sort(ds);
                for (final String t : ds) {
                    if (t.endsWith(".png") && t.startsWith(k + "_")) {
                        tep.put(t, Files.readAllBytes(new File(pk, t).toPath()));
                    }
                }
                final byte[] b = ghiGoi(dex, false, tep);
                Files.write(new File(ra, k + ".goi").toPath(), b);
                tong += b.length;
                so++;
            } catch (final Exception e) {
                loi++;
                System.out.println("LOI " + k + ": " + e);
            }
        }
        System.out.println("Da dong goi " + so + " loai -> " + ra + ", tong " + tong + " byte (trung binh "
                + (so > 0 ? tong / so : 0) + " byte/loai), loi " + loi + ", khong co sprite: " + khongCo);
        System.exit(loi == 0 && kiemHet(ra) == 0 ? 0 : 1);
    }

    static byte[] ghiGoi(final int dex, final boolean shiny, final Map<String, byte[]> tep) throws IOException {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(bo);
        o.write(MAGIC);
        o.writeByte(PHIEN_BAN);
        o.writeShort(dex);
        o.writeByte(shiny ? 1 : 0);
        if (tep.size() > 255) {
            throw new IOException("qua 255 tep");
        }
        o.writeByte(tep.size());
        for (final Map.Entry<String, byte[]> e : tep.entrySet()) {
            o.writeUTF(e.getKey());
            o.writeInt(e.getValue().length);
            o.write(e.getValue());
        }
        o.flush();
        return bo.toByteArray();
    }

    /** Doc mot goi: ten tep -&gt; byte. Nem IOException neu sai dinh dang. */
    static Map<String, byte[]> docGoi(final byte[] b, final int[] dexShiny) throws IOException {
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
        final byte[] m = new byte[4];
        in.readFully(m);
        if (!Arrays.equals(m, MAGIC)) {
            throw new IOException("sai magic");
        }
        if (in.readUnsignedByte() != PHIEN_BAN) {
            throw new IOException("sai phien ban");
        }
        dexShiny[0] = in.readShort();
        dexShiny[1] = in.readUnsignedByte();
        final int n = in.readUnsignedByte();
        final Map<String, byte[]> kq = new LinkedHashMap<String, byte[]>();
        for (int i = 0; i < n; i++) {
            final String ten = in.readUTF();
            final int len = in.readInt();
            if (len < 0 || len > b.length) {
                throw new IOException("do dai hong: " + ten);
            }
            final byte[] d = new byte[len];
            in.readFully(d);
            kq.put(ten, d);
        }
        if (in.read() != -1) {
            throw new IOException("thua byte cuoi goi");
        }
        return kq;
    }

    /** Kiem moi .goi: parse, .dl doc duoc bang PokeDuLieu cua jar, moi anh dong tac + chan dung co va giai ma duoc. */
    static int kiemHet(final File ra) {
        final String[] ds = ra.list();
        if (ds == null) {
            System.out.println("KIEM: khong co " + ra);
            return 1;
        }
        Arrays.sort(ds);
        int so = 0, loi = 0;
        long tong = 0;
        for (final String t : ds) {
            if (!t.endsWith(".goi")) {
                continue;
            }
            final String k = t.substring(0, t.length() - 4);
            try {
                final byte[] b = Files.readAllBytes(new File(ra, t).toPath());
                tong += b.length;
                final int[] ds2 = new int[2];
                final Map<String, byte[]> tep = docGoi(b, ds2);
                if (!khoa(ds2[0]).equals(k) || ds2[1] != 0) {
                    throw new IOException("dex trong goi " + ds2[0] + " khac ten tep");
                }
                final PokeDuLieu dl = PokeDuLieu.doc(tep.get(k + ".dl"));
                if (dl == null) {
                    throw new IOException(".dl khong doc duoc");
                }
                for (int i = 0; i < PokeDuLieu.SO_DONG_TAC; i++) {
                    final String anh = k + "_" + dl.anhCua(i) + ".png";
                    final byte[] p = tep.get(anh);
                    final BufferedImage img = p == null ? null : ImageIO.read(new ByteArrayInputStream(p));
                    if (img == null) {
                        throw new IOException("thieu / hong " + anh);
                    }
                }
                final byte[] c = tep.get(k + "_c.png");
                if (c == null || ImageIO.read(new ByteArrayInputStream(c)) == null) {
                    throw new IOException("thieu chan dung");
                }
                so++;
            } catch (final Exception e) {
                loi++;
                System.out.println("KIEM LOI " + t + ": " + e.getMessage());
            }
        }
        System.out.println("KIEM: " + so + " goi hop le, " + loi + " loi, tong " + tong + " byte");
        return loi;
    }
}
