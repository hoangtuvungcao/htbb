import game.pokemon.PokeDongTac;
import game.pokemon.PokeDuLieu;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Dong goi tai nguyen Pokemon cua ClientUnity (HTBB) thanh ban nhe cho jar J2ME (chay tren JDK).
 *
 *   java -cp out/tool DongGoiPokeJ2me &lt;htbb-client/pc/Assets&gt; &lt;j2me/res&gt;   (hoac HTBB/ClientUnity/Assets cu)
 *
 * Doc  Resources/poke/&lt;khoa&gt;_anh.bytes + _dl.bytes (atlas PNG + du lieu khung ban 2, muc 7.1),
 *      Resources/poke/&lt;khoa&gt;_cd.bytes, vt_*.bytes, Resources/poke_tile/*.bytes,
 *      Scripts/Assembly-CSharp/ChuPokemonDuLieu.cs (ban cu: Scripts/Game/...) (tu dien chu).
 * Ghi res/pk/&lt;khoa&gt;.dl        du lieu khung, CUNG dinh dang _dl ban 2 (PokeDuLieu.doc doc duoc),
 *                                  them u8 anhDongTac[6] o cuoi
 *      res/pk/&lt;khoa&gt;_&lt;a&gt;.png     anh cua dong tac a: 4 hang huong x soKhung cot, khung da cat sat
 *                                  hinh (bo vien trong suot), PNG bang mau (palette) khi &lt;= 256 mau
 *      res/pk/&lt;khoa&gt;_c.png       chan dung 40x40
 *      res/pk/vt_*.png, res/pt/t*.png (tile PMD), res/pk/chu.bin (tu dien: u16 n, n x 2 writeUTF)
 *
 * Giam dung luong so voi ban Unity:
 *  - cat khung sat hinh (cung mot o cat cho moi khung + huong cua dong tac); dxChan/dyChan tinh lai
 *    sao cho diem chan van trung; dong tac co anh giong het dong tac truoc thi dung chung anh;
 *  - bot khung: moi dong tac giu toi da TOI_DA[a] khung (chon deu, gop thoi luong de tong thoi gian
 *    khong doi; dong tac khong lap giu khung cuoi).
 *
 * Them (ban jar):
 *  - .dl co them u16 caoTrenGoc (dat ten tren dau tu dinh khung GOC nhu Unity);
 *  - res/pk/bong.dl + bong_*.png + bong_c.png: bong den cua Eevee (0133) = hinh thay the khi loai
 *    can ve khong co sprite trong jar ("Ai day?", PokeBody.KHOA_BONG);
 *  - hieu ung chieu (viec K3): Resources/poke/hu_bang.bytes + hu_*.bytes -> res/pk/hu.bin (HieuUngBang)
 *    + res/pk/hu_*.png: khung cat sat hinh (o chung), bot khung (toi da TOI_DA_HU), PNG bang mau
 *    (alpha trong tRNS; qua 256 mau thi luong tu hoa median-cut).
 *
 * Ban NHE (doi so thu 3 = thu muc res nhe, vd j2me/res_nhe; build.sh GOI=nhe dung no) cho may that:
 *  - chi cac loai can thiet (docs/pokemon cua HTBB): dong nguoi choi, NPC (+ su kien), pet cu,
 *    pet_item (bang_thu), quai thuong cap &lt;= CAP_QUAI_NHE = 15 (cac map dau); bo shiny (tu dung ban
 *    thuong), bo thu cuoi / boss / quai cap cao (ve bang bong den);
 *  - bot khung: dung 2, chay 2, danh 3 (danh xa dung chung anh danh gan), trung don 1 (guc nga dung
 *    chung anh trung don);
 *  - chan dung 24x24 (thay 40x40), hieu ung chieu toi da 4 khung.
 */
public class DongGoiPokeJ2me {
    /** Mot cach dong goi (day du / nhe). */
    static final class Cau {
        /** So khung toi da: dung, chay, danh gan, danh xa, trung don, guc nga. */
        int[] toiDa;
        /** Dong tac i lay khung cua dong tac nguon[i] (anh giong het thi dung chung). */
        int[] nguon = {0, 1, 2, 3, 4, 5};
        /** Canh chan dung (0 = giu nguyen 40). */
        int chanDung;
        /** Khoa can giu (null = tat ca). */
        java.util.Set<String> chiGiu;
        int toiDaHu;
    }

    static final Cau DAY_DU = new Cau();
    static final Cau NHE = new Cau();
    static {
        DAY_DU.toiDa = new int[]{4, 4, 6, 6, 2, 3};
        DAY_DU.toiDaHu = 10;
        NHE.toiDa = new int[]{2, 2, 3, 3, 1, 1};
        NHE.nguon = new int[]{0, 1, 2, 2, 4, 4};   // danh xa = danh gan, guc nga = trung don
        NHE.chanDung = 24;
        NHE.toiDaHu = 4;
    }

    /** Ban nhe: quai thuong cap toi da (cac map dau). */
    static final int CAP_QUAI_NHE = 15;
    /** Loai lam bong den thay the. */
    static final String KHOA_GOC_BONG = "0133";
    /** Mau bong den (than) va vien sang. */
    static final int MAU_BONG = 0xFF26303E, MAU_VIEN_BONG = 0xFF5A6A80;

    static Cau cau = DAY_DU;

    static long tongPng = 0, tongDl = 0, tongPx = 0, tongPxGoc = 0;
    static int soKhoa = 0, soDungChung = 0, soPalette = 0, soArgb = 0;

    public static void main(String[] a) throws Exception {
        File assets = new File(a[0]);
        dongGoiHet(assets, new File(a[1]), DAY_DU);
        if (a.length > 2) {
            NHE.chiGiu = loaiNhe(thuMucDocs(assets));
            System.out.println("--- ban nhe: " + NHE.chiGiu.size() + " loai");
            dongGoiHet(assets, new File(a[2]), NHE);
        }
    }

    /**
     * Tu dien C#: client PC htbb-client/pc dat ma game o Scripts/Assembly-CSharp, ban cu HTBB/ClientUnity
     * o Scripts/Game.
     */
    static File tepTuDien(File assets) {
        File pc = new File(assets, "Scripts/Assembly-CSharp/ChuPokemonDuLieu.cs");
        return pc.isFile() ? pc : new File(assets, "Scripts/Game/ChuPokemonDuLieu.cs");
    }

    /**
     * docs/pokemon cua HTBB (bang loai cho ban nhe): bien HTBB_DOCS, hoac &lt;assets&gt;/../../docs/pokemon
     * (assets = HTBB/ClientUnity/Assets), hoac &lt;assets&gt;/../../../HTBB/docs/pokemon (assets = htbb-client/pc/Assets
     * voi HTBB nam canh htbb-client).
     */
    static File thuMucDocs(File assets) {
        String rieng = System.getenv("HTBB_DOCS");
        if (rieng != null && rieng.length() > 0) {
            return new File(rieng);
        }
        File cu = new File(assets, "../../docs/pokemon");
        return cu.isDirectory() ? cu : new File(assets, "../../../HTBB/docs/pokemon");
    }

    static void datLai() {
        tongPng = tongDl = tongPx = tongPxGoc = 0;
        soKhoa = soDungChung = soPalette = soArgb = 0;
    }

    static void dongGoiHet(File assets, File res, Cau c) throws Exception {
        cau = c;
        datLai();
        File poke = new File(assets, "Resources/poke");
        File pk = new File(res, "pk");
        File pt = new File(res, "pt");
        pk.mkdirs();
        if (c == DAY_DU) {
            pt.mkdirs();
        }
        for (File f : pk.listFiles()) {
            f.delete();
        }
        String[] ten = poke.list();
        Arrays.sort(ten);
        for (String t : ten) {
            if (t.endsWith("_dl.bytes") && !t.startsWith("hu_")) {
                String khoa = t.substring(0, t.length() - "_dl.bytes".length());
                if (c.chiGiu == null || c.chiGiu.contains(khoa)) {
                    dongGoi(poke, pk, khoa, khoa, false);
                }
            }
        }
        dongGoi(poke, pk, KHOA_GOC_BONG, "bong", true);
        long cd = 0, vt = 0, tile = 0;
        for (String t : ten) {
            if (t.endsWith("_cd.bytes")) {
                String khoa = t.substring(0, t.length() - "_cd.bytes".length());
                if (c.chiGiu == null || c.chiGiu.contains(khoa)) {
                    cd += ghiAnh(chanDung(ImageIO.read(new File(poke, t)), false), new File(pk, khoa + "_c.png"));
                }
                if (khoa.equals(KHOA_GOC_BONG)) {
                    cd += ghiAnh(chanDung(ImageIO.read(new File(poke, t)), true), new File(pk, "bong_c.png"));
                }
            } else if (t.startsWith("vt_") && t.endsWith(".bytes")) {
                vt += ghiAnh(ImageIO.read(new File(poke, t)), new File(pk, t.replace(".bytes", ".png")));
            }
        }
        long hu = dongGoiHieuUng(poke, pk, c.toiDaHu);
        File poTile = new File(assets, "Resources/poke_tile");
        for (String t : c == DAY_DU ? poTile.list() : new String[0]) {
            if (t.endsWith(".bytes")) {
                // giu nguyen tung byte: tile la anh 16 x (16n), ban jar doc bang Image.createImage
                byte[] b = Files.readAllBytes(new File(poTile, t).toPath());
                Files.write(new File(pt, t.replace(".bytes", ".png")).toPath(), b);
                tile += b.length;
            }
        }
        long chu = ghiTuDien(tepTuDien(assets), new File(pk, "chu.bin"));
        System.out.println("khoa: " + soKhoa + ", anh dong tac: " + (soPalette + soArgb) + " (palette " + soPalette
                + ", argb " + soArgb + "), dung chung anh: " + soDungChung);
        System.out.println("pixel: goc " + tongPxGoc + " -> " + tongPx);
        System.out.println("byte: anh dong tac " + tongPng + ", .dl " + tongDl + ", chan dung " + cd + ", vat the " + vt
                + ", tile " + tile + ", tu dien " + chu + ", hieu ung " + hu + " = " + (tongPng + tongDl + cd + vt + tile + chu + hu));
    }

    static void dongGoi(File poke, File pk, String khoaNguon, String khoa, boolean bong) throws Exception {
        byte[] bDl = Files.readAllBytes(new File(poke, khoaNguon + "_dl.bytes").toPath());
        PokeDuLieu dl = PokeDuLieu.doc(bDl);
        if (dl == null) {
            throw new IOException("du lieu khung hong: " + khoa);
        }
        BufferedImage atlas = argb(ImageIO.read(new File(poke, khoaNguon + "_anh.bytes")));
        if (bong) {
            atlas = lamBong(atlas);
        }
        tongPxGoc += (long) atlas.getWidth() * atlas.getHeight();
        int n = dl.dongTac.length;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(2);
        o.writeByte(n);
        int[] anhDongTac = new int[6];
        List<int[]> daCo = new ArrayList<>();   // pixel cua anh da ghi, index = so anh
        List<Integer> soAnh = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            PokeDongTac d = dl.dongTac[i];
            if (i < cau.nguon.length && cau.nguon[i] < n) {
                d = dl.dongTac[cau.nguon[i]];   // ban nhe: danh xa = danh gan, guc nga = trung don
            }
            int[] giu = chonKhung(d.soKhung, i < cau.toiDa.length ? cau.toiDa[i] : 2, PokeDuLieu.laDongTacLap(i));
            int[] tl = gopThoiLuong(d, giu);
            int rong = d.rongKhung, cao = d.caoKhung;
            // o cat chung: hop bao moi pixel khong trong suot cua moi khung giu lai, moi huong
            int x0 = rong, y0 = cao, x1 = -1, y1 = -1;
            for (int h = 0; h < 4; h++) {
                for (int f : giu) {
                    int sx = d.xAtlas + f * rong, sy = d.yHang(h);
                    for (int y = 0; y < cao; y++) {
                        for (int x = 0; x < rong; x++) {
                            if (sx + x < atlas.getWidth() && sy + y < atlas.getHeight()
                                    && (atlas.getRGB(sx + x, sy + y) >>> 24) >= 128) {
                                x0 = Math.min(x0, x);
                                y0 = Math.min(y0, y);
                                x1 = Math.max(x1, x);
                                y1 = Math.max(y1, y);
                            }
                        }
                    }
                }
            }
            if (x1 < 0) {
                x0 = y0 = 0;
                x1 = y1 = 0;
            }
            int cw = x1 - x0 + 1, ch = y1 - y0 + 1;
            int[] dx = new int[4], dy = new int[4];
            for (int h = 0; h < 4; h++) {
                // ban goc ve o (x - rong/2 - dxChan, y - cao/2 - dyChan); khung cat nam lech (x0, y0)
                dx[h] = rong / 2 + d.dxChan[h] - x0 - cw / 2;
                dy[h] = cao / 2 + d.dyChan[h] - y0 - ch / 2;
                if (dx[h] < -128 || dx[h] > 127 || dy[h] < -128 || dy[h] > 127) {
                    throw new IOException("dx/dy ngoai i8: " + khoa + " dong tac " + i);
                }
            }
            BufferedImage anh = new BufferedImage(cw * giu.length, ch * 4, BufferedImage.TYPE_INT_ARGB);
            for (int h = 0; h < 4; h++) {
                for (int k = 0; k < giu.length; k++) {
                    int sx = d.xAtlas + giu[k] * rong + x0, sy = d.yHang(h) + y0;
                    for (int y = 0; y < ch; y++) {
                        for (int x = 0; x < cw; x++) {
                            int c = atlas.getRGB(sx + x, sy + y);
                            anh.setRGB(k * cw + x, h * ch + y, (c >>> 24) >= 128 ? (c | 0xFF000000) : 0);
                        }
                    }
                }
            }
            int[] px = anh.getRGB(0, 0, anh.getWidth(), anh.getHeight(), null, 0, anh.getWidth());
            int[] dau = new int[px.length + 2];
            System.arraycopy(px, 0, dau, 2, px.length);
            dau[0] = anh.getWidth();
            dau[1] = anh.getHeight();
            int trung = -1;
            for (int j = 0; j < daCo.size(); j++) {
                if (Arrays.equals(daCo.get(j), dau)) {
                    trung = soAnh.get(j);
                }
            }
            if (trung >= 0) {
                anhDongTac[i] = trung;
                soDungChung++;
            } else {
                anhDongTac[i] = i;
                daCo.add(dau);
                soAnh.add(i);
                tongPng += ghiAnh(anh, new File(pk, khoa + "_" + i + ".png"));
                tongPx += px.length;
            }
            o.writeShort(cw);
            o.writeShort(ch);
            o.writeByte(giu.length);
            for (int t : tl) {
                o.writeByte(t);
            }
            o.writeShort(0);   // yAtlas
            o.writeShort(0);   // xAtlas (ban 2)
            for (int h = 0; h < 4; h++) {
                o.writeByte(dx[h]);
            }
            for (int h = 0; h < 4; h++) {
                o.writeByte(dy[h]);
            }
        }
        for (int i = 0; i < 6; i++) {
            o.writeByte(dl.coThat[i] ? 1 : 0);
        }
        for (int i = 0; i < 6; i++) {
            o.writeByte(anhDongTac[i]);
        }
        PokeDongTac dung = dl.layDongTac(PokeDuLieu.DUNG);
        o.writeShort(Math.max(0, Math.min(65535, dung.caoKhung / 2 + dung.dyChan[0])));
        byte[] kq = bo.toByteArray();
        // tu kiem: doc lai bang chinh PokeDuLieu cua jar
        PokeDuLieu lai = PokeDuLieu.doc(kq);
        if (lai == null || lai.dongTac.length != n || lai.caoTrenGoc != dung.caoKhung / 2 + dung.dyChan[0]) {
            throw new IOException("doc lai hong: " + khoa);
        }
        Files.write(new File(pk, khoa + ".dl").toPath(), kq);
        tongDl += kq.length;
        soKhoa++;
    }

    /** Chon toi da toiDa khung trong soKhung, deu nhau; dong tac khong lap luon giu khung cuoi. */
    static int[] chonKhung(int soKhung, int toiDa, boolean lap) {
        if (soKhung <= toiDa) {
            int[] r = new int[soKhung];
            for (int i = 0; i < soKhung; i++) {
                r[i] = i;
            }
            return r;
        }
        int[] r = new int[toiDa];
        for (int i = 0; i < toiDa; i++) {
            r[i] = lap ? (i * soKhung / toiDa) : (int) Math.round(i * (soKhung - 1) / (double) (toiDa - 1));
        }
        return r;
    }

    /** Thoi luong khung giu lai = tong thoi luong tu khung do toi truoc khung giu ke tiep (<= 255). */
    static int[] gopThoiLuong(PokeDongTac d, int[] giu) {
        int[] tl = new int[giu.length];
        for (int k = 0; k < giu.length; k++) {
            int den = k + 1 < giu.length ? giu[k + 1] : d.soKhung;
            int s = 0;
            for (int f = giu[k]; f < den; f++) {
                s += d.thoiLuong[f];
            }
            tl[k] = Math.max(1, Math.min(255, s));
        }
        return tl;
    }

    static BufferedImage argb(BufferedImage b) {
        BufferedImage r = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_ARGB);
        r.getGraphics().drawImage(b, 0, 0, null);
        return r;
    }

    /** Ghi PNG: bang mau (index 0 trong suot) khi <= 256 mau va alpha chi 0/255, khong thi ARGB. */
    static long ghiAnh(BufferedImage b, File ra) throws IOException {
        b = argb(b);
        int w = b.getWidth(), h = b.getHeight();
        int[] px = b.getRGB(0, 0, w, h, null, 0, w);
        Map<Integer, Integer> mau = new HashMap<>();
        mau.put(0, 0);
        boolean duoc = true;
        for (int i = 0; i < px.length && duoc; i++) {
            int c = px[i];
            int al = c >>> 24;
            if (al == 0) {
                px[i] = 0;
                continue;
            }
            if (al != 255) {
                duoc = false;
                break;
            }
            if (!mau.containsKey(c)) {
                if (mau.size() >= 256) {
                    duoc = false;
                    break;
                }
                mau.put(c, mau.size());
            }
        }
        byte[] kq;
        if (duoc) {
            int n = mau.size();
            byte[] r = new byte[n], g = new byte[n], bl = new byte[n], al = new byte[n];
            for (Map.Entry<Integer, Integer> e : mau.entrySet()) {
                int c = e.getKey(), i = e.getValue();
                r[i] = (byte) (c >> 16);
                g[i] = (byte) (c >> 8);
                bl[i] = (byte) c;
                al[i] = (byte) (i == 0 ? 0 : 255);
            }
            int bit = n <= 2 ? 1 : n <= 4 ? 2 : n <= 16 ? 4 : 8;
            IndexColorModel cm = new IndexColorModel(bit, n, r, g, bl, al);
            BufferedImage ib = new BufferedImage(w, h, bit == 8 ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_BINARY, cm);
            java.awt.image.WritableRaster wr = ib.getRaster();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = px[y * w + x];
                    wr.setSample(x, y, 0, (c >>> 24) == 0 ? 0 : mau.get(c));
                }
            }
            kq = png(ib);
            // tu kiem: doc lai phai ra dung tung pixel (ke ca trong suot)
            BufferedImage lai = argb(ImageIO.read(new ByteArrayInputStream(kq)));
            int[] p2 = lai.getRGB(0, 0, w, h, null, 0, w);
            for (int i = 0; i < px.length; i++) {
                int a1 = px[i] >>> 24 == 0 ? 0 : px[i], a2 = p2[i] >>> 24 == 0 ? 0 : p2[i];
                if (a1 != a2) {
                    throw new IOException("PNG palette doc lai sai: " + ra);
                }
            }
            byte[] kqArgb = png(b);
            if (kqArgb.length < kq.length) {
                kq = kqArgb;
                soArgb++;
            } else {
                soPalette++;
            }
        } else {
            kq = png(b);
            soArgb++;
        }
        try (FileOutputStream f = new FileOutputStream(ra)) {
            f.write(kq);
        }
        return kq.length;
    }

    /** PNG nen muc cao nhat (deflate 9) - nho hon ImageIO.write mac dinh. */
    static byte[] png(BufferedImage b) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        javax.imageio.ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        try (javax.imageio.stream.ImageOutputStream io = ImageIO.createImageOutputStream(o)) {
            w.setOutput(io);
            javax.imageio.ImageWriteParam p = w.getDefaultWriteParam();
            if (p.canWriteCompressed()) {
                p.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                p.setCompressionQuality(0.0f);
            }
            w.write(null, new javax.imageio.IIOImage(b, null, null), p);
        } finally {
            w.dispose();
        }
        return o.toByteArray();
    }

    /** Tu dien ChuPokemonDuLieu.BANG (C# sinh tu ban Java cua server) -> chu.bin. */
    static long ghiTuDien(File cs, File ra) throws IOException {
        String s = new String(Files.readAllBytes(cs.toPath()), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("new string\\[\\] \\{ \"((?:[^\"\\\\]|\\\\.)*)\", \"((?:[^\"\\\\]|\\\\.)*)\" \\}").matcher(s);
        List<String[]> ds = new ArrayList<>();
        while (m.find()) {
            ds.add(new String[]{boThoat(m.group(1)), boThoat(m.group(2))});
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeShort(ds.size());
        for (String[] d : ds) {
            o.writeUTF(d[0]);
            o.writeUTF(d[1]);
        }
        Files.write(ra.toPath(), bo.toByteArray());
        System.out.println("tu dien: " + ds.size() + " dong");
        return bo.size();
    }

    static String boThoat(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    // ------------------------------------------------------------------ ban nhe, bong den

    /** Cac khoa loai cho ban nhe, doc tu docs/pokemon/*.csv cua HTBB. */
    static java.util.Set<String> loaiNhe(File docs) throws IOException {
        java.util.Set<String> kq = new java.util.TreeSet<>();
        for (Map<String, String> r : docCsv(new File(docs, "kiem_ke_sprite.csv"))) {
            String dung = r.get("dung_cho");
            if ("nguoi_choi".equals(dung) || "npc".equals(dung) || "npc_su_kien".equals(dung) || "pet_cu".equals(dung)) {
                themDex(kq, r.get("dex"));
            }
        }
        for (Map<String, String> r : docCsv(new File(docs, "bang_thu.csv"))) {
            if ("pet_item".equals(r.get("loai"))) {
                themDex(kq, r.get("dex"));
            }
        }
        for (Map<String, String> r : docCsv(new File(docs, "bang_quai.csv"))) {
            try {
                if ("quai".equals(r.get("phan_loai")) && Integer.parseInt(r.get("cap").trim()) <= CAP_QUAI_NHE) {
                    themDex(kq, r.get("dex"));
                }
            } catch (NumberFormatException e) {
                // dong hong
            }
        }
        for (Map<String, String> r : docCsv(new File(docs, "bang_npc.csv"))) {
            themDex(kq, r.get("dex"));
        }
        for (Map<String, String> r : docCsv(new File(docs, "bang_npc_su_kien.csv"))) {
            themDex(kq, r.get("dex"));
        }
        for (Map<String, String> r : docCsv(new File(docs, "bang_pet_cu.csv"))) {
            themDex(kq, r.get("dex"));
        }
        return kq;
    }

    static void themDex(java.util.Set<String> s, String dex) {
        if (dex == null) {
            return;
        }
        try {
            int d = Integer.parseInt(dex.trim());
            if (d > 0) {
                s.add(String.format("%04d", d));
            }
        } catch (NumberFormatException e) {
            // o trong
        }
    }

    /** CSV don gian (co ngoac kep). */
    static List<Map<String, String>> docCsv(File f) throws IOException {
        List<Map<String, String>> kq = new ArrayList<>();
        if (!f.isFile()) {
            throw new IOException("thieu " + f);
        }
        List<String> dong = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        if (dong.isEmpty()) {
            return kq;
        }
        List<String> dau = tachCsv(dong.get(0));
        for (int i = 1; i < dong.size(); i++) {
            if (dong.get(i).trim().isEmpty()) {
                continue;
            }
            List<String> o = tachCsv(dong.get(i));
            Map<String, String> m = new HashMap<>();
            for (int j = 0; j < dau.size() && j < o.size(); j++) {
                m.put(dau.get(j), o.get(j));
            }
            kq.add(m);
        }
        return kq;
    }

    static List<String> tachCsv(String s) {
        List<String> kq = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        boolean ngoac = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                if (ngoac && i + 1 < s.length() && s.charAt(i + 1) == '"') {
                    b.append('"');
                    i++;
                } else {
                    ngoac = !ngoac;
                }
            } else if (c == ',' && !ngoac) {
                kq.add(b.toString());
                b.setLength(0);
            } else {
                b.append(c);
            }
        }
        kq.add(b.toString());
        return kq;
    }

    /** Bong den: moi pixel dac thanh MAU_BONG, pixel dac ke pixel trong suot thanh MAU_VIEN_BONG. */
    static BufferedImage lamBong(BufferedImage a) {
        a = argb(a);
        int w = a.getWidth(), h = a.getHeight();
        BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((a.getRGB(x, y) >>> 24) < 128) {
                    continue;
                }
                boolean vien = false;
                for (int k = 0; k < 4 && !vien; k++) {
                    int xx = x + (k == 0 ? -1 : k == 1 ? 1 : 0), yy = y + (k == 2 ? -1 : k == 3 ? 1 : 0);
                    vien = xx < 0 || yy < 0 || xx >= w || yy >= h || (a.getRGB(xx, yy) >>> 24) < 128;
                }
                r.setRGB(x, y, vien ? MAU_VIEN_BONG : MAU_BONG);
            }
        }
        return r;
    }

    /** Chan dung: bong den neu can, thu nho ve canh cau.chanDung (ban nhe). */
    static BufferedImage chanDung(BufferedImage a, boolean bong) {
        a = argb(a);
        if (bong) {
            a = lamBong(a);
        }
        int canh = cau.chanDung;
        if (canh <= 0 || canh == a.getWidth()) {
            return matNaTron(a);
        }
        int h2 = a.getHeight() * canh / a.getWidth();
        // lay mau gan nhat (giu bang mau pixel art, PNG bang mau nho), alpha ve 0/255
        BufferedImage r = new BufferedImage(canh, h2, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h2; y++) {
            for (int x = 0; x < canh; x++) {
                int c = a.getRGB((2 * x + 1) * a.getWidth() / (2 * canh), (2 * y + 1) * a.getHeight() / (2 * h2));
                r.setRGB(x, y, (c >>> 24) >= 128 ? (c | 0xFF000000) : 0);
            }
        }
        return matNaTron(r);
    }

    /**
     * Mat na tron (alpha 0/255) cho chan dung PMD: nen troi/co ve san (10-14 mau, khong tach duoc)
     * thanh khung tron co chu y; client ve them vong vien (game.pokemon.HuyHieuPoke dung sprite
     * dung yen, anh nay chi la duong lui khi loai thieu sprite).
     */
    static BufferedImage matNaTron(BufferedImage a) {
        int w = a.getWidth(), h = a.getHeight();
        int d = Math.min(w, h);
        BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int dx = 2 * x + 1 - w, dy = 2 * y + 1 - h;
                r.setRGB(x, y, dx * dx + dy * dy < d * d ? a.getRGB(x, y) : 0);
            }
        }
        return r;
    }

    // ------------------------------------------------------------------ hieu ung chieu (K3)

    static int soHu = 0;

    /**
     * hu_bang.bytes (lop,chieu,ten,rong,cao,soKhung,soHuong,soCot,kieuVe,tickMoiKhung) + anh
     * -> pk/hu.bin + pk/hu_*.png. Tra ve tong byte.
     */
    static long dongGoiHieuUng(File poke, File pk, int toiDa) throws Exception {
        File fb = new File(poke, "hu_bang.bytes");
        if (!fb.isFile()) {
            System.out.println("CANH BAO: khong co hu_bang.bytes - bo qua hieu ung chieu");
            return 0;
        }
        List<String> dong = Files.readAllLines(fb.toPath(), StandardCharsets.UTF_8);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        List<String[]> ds = new ArrayList<>();
        for (String d : dong) {
            d = d.trim();
            if (d.isEmpty() || d.charAt(0) == '#') {
                continue;
            }
            String[] c = d.split(",");
            if (c.length >= 10) {
                ds.add(c);
            }
        }
        o.writeByte(1);
        o.writeByte(ds.size());
        Map<String, int[]> daGhi = new HashMap<>();   // ten -> {rong, cao, soKhung, soHuong, soCot, tick, dx, dy}
        long tong = 0;
        long pxGoc = 0, pxMoi = 0;
        for (String[] c : ds) {
            String ten = c[2].trim();
            int[] tt = daGhi.get(ten);
            if (tt == null) {
                int rong = Integer.parseInt(c[3].trim()), cao = Integer.parseInt(c[4].trim());
                int soKhung = Integer.parseInt(c[5].trim()), soHuong = Integer.parseInt(c[6].trim());
                int soCot = Integer.parseInt(c[7].trim()), tick = Integer.parseInt(c[9].trim());
                BufferedImage goc = argb(ImageIO.read(new File(poke, ten + ".bytes")));
                pxGoc += (long) goc.getWidth() * goc.getHeight();
                int hang = soHuong >= 4 ? 4 : 1;
                int[] giu = chonKhung(soKhung, toiDa, false);
                // tick moi khung moi: giu tong thoi gian (don vi 1/50 s)
                int tickMoi = Math.max(1, Math.min(255, Math.round(soKhung * tick / (float) giu.length)));
                // o cat chung moi khung giu + moi huong
                int x0 = rong, y0 = cao, x1 = -1, y1 = -1;
                for (int hh = 0; hh < hang; hh++) {
                    for (int f : giu) {
                        int[] sxy = nguonHu(f, hh, rong, cao, soHuong, soCot);
                        for (int y = 0; y < cao; y++) {
                            for (int x = 0; x < rong; x++) {
                                int px = sxy[0] + x, py = sxy[1] + y;
                                if (px < goc.getWidth() && py < goc.getHeight() && (goc.getRGB(px, py) >>> 24) >= 8) {
                                    x0 = Math.min(x0, x);
                                    y0 = Math.min(y0, y);
                                    x1 = Math.max(x1, x);
                                    y1 = Math.max(y1, y);
                                }
                            }
                        }
                    }
                }
                if (x1 < 0) {
                    x0 = y0 = x1 = y1 = 0;
                }
                int cw = x1 - x0 + 1, ch = y1 - y0 + 1;
                int n = giu.length;
                int cot = hang == 4 ? n : Math.max(1, Math.min(n, 1024 / cw));
                int hangAnh = hang == 4 ? 4 : (n + cot - 1) / cot;
                BufferedImage anh = new BufferedImage(cot * cw, hangAnh * ch, BufferedImage.TYPE_INT_ARGB);
                for (int hh = 0; hh < hang; hh++) {
                    for (int k = 0; k < n; k++) {
                        int[] sxy = nguonHu(giu[k], hh, rong, cao, soHuong, soCot);
                        int dxA = hang == 4 ? k * cw : (k % cot) * cw;
                        int dyA = hang == 4 ? hh * ch : (k / cot) * ch;
                        for (int y = 0; y < ch; y++) {
                            for (int x = 0; x < cw; x++) {
                                int px = sxy[0] + x0 + x, py = sxy[1] + y0 + y;
                                int v = px < goc.getWidth() && py < goc.getHeight() ? goc.getRGB(px, py) : 0;
                                anh.setRGB(dxA + x, dyA + y, (v >>> 24) < 8 ? 0 : v);
                            }
                        }
                    }
                }
                pxMoi += (long) anh.getWidth() * anh.getHeight();
                tong += ghiAnhAlpha(anh, new File(pk, ten + ".png"));
                tt = new int[]{cw, ch, n, hang == 4 ? 4 : 1, cot, tickMoi, x0 - rong / 2, y0 - cao / 2};
                daGhi.put(ten, tt);
            }
            o.writeByte(Integer.parseInt(c[0].trim()));
            o.writeByte(Integer.parseInt(c[1].trim()));
            o.writeUTF(ten);
            o.writeShort(tt[0]);
            o.writeShort(tt[1]);
            o.writeByte(tt[2]);
            o.writeByte(tt[3]);
            o.writeByte(tt[4]);
            o.writeByte(c[8].trim().isEmpty() ? 'D' : c[8].trim().charAt(0));
            o.writeByte(tt[5]);
            o.writeShort(tt[6]);
            o.writeShort(tt[7]);
        }
        byte[] kq = bo.toByteArray();
        game.pokemon.HieuUngBang[] lai = game.pokemon.HieuUngBang.doc(kq);
        if (lai == null || lai.length != ds.size()) {
            throw new IOException("hu.bin doc lai hong");
        }
        Files.write(new File(pk, "hu.bin").toPath(), kq);
        soHu = ds.size();
        System.out.println("hieu ung: " + ds.size() + " chieu, " + daGhi.size() + " anh, pixel " + pxGoc + " -> " + pxMoi
                + ", byte " + (tong + kq.length));
        return tong + kq.length;
    }

    /** Goc khung f, hang huong hh trong anh hu_ cua Unity. */
    static int[] nguonHu(int f, int hh, int rong, int cao, int soHuong, int soCot) {
        if (soHuong >= 4) {
            return new int[]{f * rong, hh * cao};
        }
        return new int[]{f % soCot * rong, f / soCot * cao};
    }

    /**
     * PNG bang mau co alpha (tRNS) cho anh ban trong suot (hieu ung). Qua 256 mau RGBA thi luong tu
     * hoa median-cut (4 kenh). Chon ban nho hon giua bang mau va ARGB.
     */
    static long ghiAnhAlpha(BufferedImage b, File ra) throws IOException {
        int w = b.getWidth(), h = b.getHeight();
        int[] px = b.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < px.length; i++) {
            if ((px[i] >>> 24) == 0) {
                px[i] = 0;
            }
        }
        java.util.LinkedHashMap<Integer, Integer> dem = new java.util.LinkedHashMap<>();
        for (int c : px) {
            if (c != 0) {
                dem.merge(c, 1, Integer::sum);
            }
        }
        int[] bang = dem.size() <= 255 ? null : medianCut(dem, 255);
        List<Integer> mau = new ArrayList<>();
        mau.add(0);
        Map<Integer, Integer> chiSo = new HashMap<>();
        chiSo.put(0, 0);
        if (bang == null) {
            for (int c : dem.keySet()) {
                chiSo.put(c, mau.size());
                mau.add(c);
            }
        } else {
            for (int c : bang) {
                mau.add(c);
            }
            for (int c : dem.keySet()) {
                chiSo.put(c, ganNhat(bang, c) + 1);
            }
        }
        int n = mau.size();
        byte[] r = new byte[n], g = new byte[n], bl = new byte[n], al = new byte[n];
        for (int i = 0; i < n; i++) {
            int c = mau.get(i);
            r[i] = (byte) (c >> 16);
            g[i] = (byte) (c >> 8);
            bl[i] = (byte) c;
            al[i] = (byte) (c >>> 24);
        }
        int bit = n <= 2 ? 1 : n <= 4 ? 2 : n <= 16 ? 4 : 8;
        IndexColorModel cm = new IndexColorModel(bit, n, r, g, bl, al);
        BufferedImage ib = new BufferedImage(w, h, bit == 8 ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_BINARY, cm);
        java.awt.image.WritableRaster wr = ib.getRaster();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                wr.setSample(x, y, 0, chiSo.get(px[y * w + x]));
            }
        }
        byte[] kq = png(ib);
        if (bang == null) {
            byte[] kqArgb = png(b);
            if (kqArgb.length < kq.length) {
                kq = kqArgb;
            }
        }
        try (FileOutputStream f = new FileOutputStream(ra)) {
            f.write(kq);
        }
        return kq.length;
    }

    static int ganNhat(int[] bang, int c) {
        int best = 0;
        long bd = Long.MAX_VALUE;
        for (int i = 0; i < bang.length; i++) {
            long d = kc(bang[i], c);
            if (d < bd) {
                bd = d;
                best = i;
            }
        }
        return best;
    }

    static long kc(int a, int b) {
        long d = 0;
        for (int s = 0; s < 32; s += 8) {
            int x = ((a >>> s) & 0xFF) - ((b >>> s) & 0xFF);
            d += (long) x * x * (s == 24 ? 2 : 1);
        }
        return d;
    }

    /** Median-cut co trong so tren 4 kenh ARGB; tra ve toi da k mau (trung binh co trong so). */
    static int[] medianCut(Map<Integer, Integer> dem, int k) {
        List<List<int[]>> hop = new ArrayList<>();
        List<int[]> dau = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : dem.entrySet()) {
            dau.add(new int[]{e.getKey(), e.getValue()});
        }
        hop.add(dau);
        while (hop.size() < k) {
            int chon = -1, kenh = 0;
            long rong = -1;
            for (int i = 0; i < hop.size(); i++) {
                List<int[]> hp = hop.get(i);
                if (hp.size() < 2) {
                    continue;
                }
                for (int s = 0; s < 32; s += 8) {
                    int mn = 255, mx = 0;
                    long tong = 0;
                    for (int[] c : hp) {
                        int v = (c[0] >>> s) & 0xFF;
                        mn = Math.min(mn, v);
                        mx = Math.max(mx, v);
                        tong += c[1];
                    }
                    long dien = (long) (mx - mn) * (s == 24 ? 2 : 1) * (long) Math.sqrt(tong);
                    if (dien > rong) {
                        rong = dien;
                        chon = i;
                        kenh = s;
                    }
                }
            }
            if (chon < 0 || rong <= 0) {
                break;
            }
            List<int[]> hp = hop.remove(chon);
            final int ss = kenh;
            hp.sort((x, y) -> ((x[0] >>> ss) & 0xFF) - ((y[0] >>> ss) & 0xFF));
            long tong = 0;
            for (int[] c : hp) {
                tong += c[1];
            }
            long nua = 0;
            int cat = 1;
            for (int i = 0; i < hp.size() - 1; i++) {
                nua += hp.get(i)[1];
                if (nua * 2 >= tong) {
                    cat = i + 1;
                    break;
                }
                cat = i + 1;
            }
            hop.add(new ArrayList<>(hp.subList(0, cat)));
            hop.add(new ArrayList<>(hp.subList(cat, hp.size())));
        }
        int[] kq = new int[hop.size()];
        for (int i = 0; i < hop.size(); i++) {
            long[] t = new long[4];
            long so = 0;
            for (int[] c : hop.get(i)) {
                for (int s = 0; s < 4; s++) {
                    t[s] += (long) ((c[0] >>> (s * 8)) & 0xFF) * c[1];
                }
                so += c[1];
            }
            int v = 0;
            for (int s = 0; s < 4; s++) {
                v |= (int) Math.min(255, (t[s] + so / 2) / so) << (s * 8);
            }
            if ((v >>> 24) == 0) {
                v = 0x01000000 | (v & 0xFFFFFF);   // giu khac 0 (0 = o trong suot)
            }
            kq[i] = v;
        }
        return kq;
    }
}
