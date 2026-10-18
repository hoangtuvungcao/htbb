import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Tinh DIEM NEO phu kien (dau / dinh dau / tay / tam than) cho tung khung, tung huong cua moi loai
 * Pokemon, tu anh *-Offsets.png cua PMDCollab (moi khung co toi da 4 diem mau: DEN = dau, XANH LA = tam
 * than, DO / XANH DUONG = hai tay). Dung de ve trang bi len than Pokemon (mu/no o dinh dau, khan o co,
 * do cam tay o tay, ao choang sau than) - xem docs/pokemon/NGOAI_HINH_TRANG_BI.md.
 *
 * Ra: &lt;Assets&gt;/Resources/poke/&lt;khoa&gt;_neo.bytes (cung thu muc ra voi DongGoiSprite). Dinh dang (big-endian):
 * <pre>
 *   u8 'N', u8 phienBan = 1, u8 soDongTac = 6
 *   lap soDongTac lan: u8 soKhung (= so khung PMD cua dong tac, nhu _dl.bytes)
 *       lap huong h = 0..3 (DOWN, UP, LEFT, RIGHT), lap khung f = 0..soKhung-1: 8 byte
 *       i8 dauX, i8 dauY, i8 dinhY, i8 tayX, i8 tayY, i8 tamX, i8 tamY, u8 co
 * </pre>
 * Toa do tinh so voi DIEM CHAN (tam khung + dxChan/dyChan cua huong, giong DongGoiSprite.tinhChan),
 * don vi pixel goc (ti le 100). Nho vay ban jar cat khung / bot khung van dung duoc (DongGoiPokeJ2me
 * chon lai dung khung da giu). dinhY = mep tren cua dau ngay tren diem dau (do theo cot pixel lien tuc).
 * co: bit0 = lay tu Offsets.png (khong thi uoc luong tu hop bao hinh), bit1 = tay khuat sau than.
 *
 * Chay (o thu muc HTBB/, can JDK, sau tools/pokemon/tai_sprite_pmd.py):
 *   javac -d build/tools tools/pokemon/DongGoiSprite.java tools/pokemon/TinhNeoPoke.java
 *   java -cp build/tools TinhNeoPoke
 */
public final class TinhNeoPoke {

    static final int PHIEN_BAN = 1;
    static final int BYTE_MOI_KHUNG = 8;

    private TinhNeoPoke() {
    }

    public static void main(final String[] args) throws Exception {
        final File goc = new File(".").getCanonicalFile();
        final File raw = new File(goc, "tools/pokemon/pmd_raw");
        final List<File> cacRa = DongGoiSprite.thuMucRa(goc);
        final String[] khoa = raw.list();
        if (khoa == null) {
            System.err.println("Khong co " + raw);
            System.exit(1);
        }
        Arrays.sort(khoa);
        int so = 0;
        int loi = 0;
        int khungOffsets = 0;
        int khungUocLuong = 0;
        long tong = 0;
        for (final String k : khoa) {
            final File tm = new File(raw, k);
            if (!new File(tm, "AnimData.xml").isFile()) {
                continue;
            }
            try {
                final int[] dem = new int[2];
                final byte[] b = tinh(tm, dem);
                for (final File ra : cacRa) {
                    ra.mkdirs();
                    Files.write(new File(ra, k + "_neo.bytes").toPath(), b);
                }
                khungOffsets += dem[0];
                khungUocLuong += dem[1];
                tong += b.length;
                so++;
            } catch (final Exception e) {
                loi++;
                System.out.println("LOI " + k + ": " + e.getMessage());
            }
        }
        System.out.println("Da tinh neo " + so + " loai -> " + cacRa + " (" + tong + " byte; khung tu Offsets "
                + khungOffsets + ", uoc luong " + khungUocLuong + "), loi: " + loi);
        System.exit(loi == 0 ? 0 : 1);
    }

    /** Du lieu neo cua mot loai. dem[0] += so khung lay tu Offsets, dem[1] += so khung uoc luong. */
    static byte[] tinh(final File tm, final int[] dem) throws Exception {
        final Map<String, DongGoiSprite.Anim> anims = DongGoiSprite.docAnimData(new File(tm, "AnimData.xml"));
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(bo);
        o.writeByte('N');
        o.writeByte(PHIEN_BAN);
        o.writeByte(DongGoiSprite.DONG_TAC.length);
        for (int i = 0; i < DongGoiSprite.DONG_TAC.length; i++) {
            DongGoiSprite.DongTac t = null;
            for (int j = 0; j < DongGoiSprite.DONG_TAC[i].length && t == null; j++) {
                final DongGoiSprite.Anim a = anims.get(DongGoiSprite.DONG_TAC[i][j]);
                if (a != null) {
                    t = new DongGoiSprite.DongTac();
                    t.anim = a;
                }
            }
            if (t == null) {
                throw new IOException("thieu dong tac " + i);
            }
            t.nguon = ImageIO.read(new File(tm, t.anim.ten + "-Anim.png"));
            if (t.nguon == null) {
                throw new IOException("khong doc duoc " + t.anim.ten + "-Anim.png");
            }
            t.soHang = t.nguon.getHeight() / t.anim.cao;
            DongGoiSprite.tinhChan(t, new File(tm, t.anim.ten + "-Shadow.png"));
            final File fo = new File(tm, t.anim.ten + "-Offsets.png");
            BufferedImage off = fo.isFile() ? ImageIO.read(fo) : null;
            if (off != null && (off.getWidth() < t.anim.thoiLuong.length * t.anim.rong
                    || off.getHeight() < (t.soHang == 1 ? 1 : 8) * t.anim.cao)) {
                off = null;   // kich thuoc khong khop anh dong tac: bo, uoc luong
            }
            final int n = t.anim.thoiLuong.length;
            o.writeByte(n);
            for (int h = 0; h < 4; h++) {
                for (int f = 0; f < n; f++) {
                    final int[] p = motKhung(t, off, h, f);
                    if ((p[7] & 1) != 0) {
                        dem[0]++;
                    } else {
                        dem[1]++;
                    }
                    for (int q = 0; q < BYTE_MOI_KHUNG; q++) {
                        o.writeByte(q < 7 ? i8(p[q]) : p[q]);
                    }
                }
            }
        }
        o.flush();
        return bo.toByteArray();
    }

    static int i8(final int v) {
        return Math.max(-128, Math.min(127, v));
    }

    /** {dauX, dauY, dinhY, tayX, tayY, tamX, tamY, co} cua khung f, huong h (so voi diem chan). */
    static int[] motKhung(final DongGoiSprite.DongTac t, final BufferedImage off, final int h, final int f) {
        final int w = t.anim.rong;
        final int c = t.anim.cao;
        final int x0 = f * w;
        final int y0 = DongGoiSprite.hangNguon(t, h) * c;
        // hop bao hinh (alpha >= 128) cua khung
        int bx0 = w, by0 = c, bx1 = -1, by1 = -1;
        for (int y = 0; y < c; y++) {
            for (int x = 0; x < w; x++) {
                if (dac(t.nguon, x0 + x, y0 + y)) {
                    bx0 = Math.min(bx0, x);
                    by0 = Math.min(by0, y);
                    bx1 = Math.max(bx1, x);
                    by1 = Math.max(by1, y);
                }
            }
        }
        if (bx1 < 0) {
            bx0 = bx1 = w / 2;
            by0 = by1 = c / 2;
        }
        int[] dau = null, tam = null, tayDo = null, tayLam = null;
        if (off != null) {
            for (int y = 0; y < c; y++) {
                for (int x = 0; x < w; x++) {
                    final int px = off.getRGB(x0 + x, y0 + y);
                    if ((px >>> 24) == 0) {
                        continue;
                    }
                    final int r = (px >> 16) & 0xFF, g = (px >> 8) & 0xFF, b = px & 0xFF;
                    if (r < 64 && g < 64 && b < 64) {
                        dau = new int[]{x, y};
                    } else if (g > 160 && r < 96 && b < 96) {
                        tam = new int[]{x, y};
                    } else if (r > 160 && g < 96 && b < 96) {
                        tayDo = new int[]{x, y};
                    } else if (b > 160 && r < 96 && g < 96) {
                        tayLam = new int[]{x, y};
                    }
                }
            }
        }
        int co = dau != null && tam != null ? 1 : 0;
        final int bw = bx1 - bx0 + 1, bh = by1 - by0 + 1;
        if (tam == null) {
            tam = new int[]{(bx0 + bx1) / 2, (by0 + by1) / 2};
        }
        if (dau == null) {
            dau = new int[]{(bx0 + bx1) / 2, by0 + bh / 4};
        }
        if (tayDo == null && tayLam == null) {
            tayDo = new int[]{bx0 + bw / 5, tam[1]};
            tayLam = new int[]{bx1 - bw / 5, tam[1]};
        } else if (tayDo == null) {
            tayDo = tayLam;
        } else if (tayLam == null) {
            tayLam = tayDo;
        }
        // tay "phia truoc": nhin xuong lay tay ben trai man hinh (tay phai cua Pokemon), nhin ngang lay
        // tay thap hon (gan nguoi xem hon), nhin len thi tay khuat sau than
        int[] tay;
        if (h == 0) {
            tay = tayDo[0] <= tayLam[0] ? tayDo : tayLam;
        } else if (h == 1) {
            tay = tayDo[0] >= tayLam[0] ? tayDo : tayLam;
            co |= 2;
        } else {
            tay = tayDo[1] >= tayLam[1] ? tayDo : tayLam;
        }
        // dinh dau: tu diem dau di len theo cot (x-1..x+1) toi khi het pixel lien tuc
        int dinh = dau[1];
        for (int dxc = -1; dxc <= 1; dxc++) {
            final int x = dau[0] + dxc;
            if (x < 0 || x >= w) {
                continue;
            }
            int y = dau[1];
            while (y > 0 && dac(t.nguon, x0 + x, y0 + y - 1)) {
                y--;
            }
            if (dac(t.nguon, x0 + x, y0 + y) && y < dinh) {
                dinh = y;
            }
        }
        if (dinh == dau[1] && !dac(t.nguon, x0 + dau[0], y0 + dau[1])) {
            dinh = Math.max(by0, dau[1] - bh / 4);
        }
        final int chanX = w / 2 + t.dx[h];
        final int chanY = c / 2 + t.dy[h];
        return new int[]{dau[0] - chanX, dau[1] - chanY, dinh - chanY, tay[0] - chanX, tay[1] - chanY,
            tam[0] - chanX, tam[1] - chanY, co};
    }

    static boolean dac(final BufferedImage b, final int x, final int y) {
        return x >= 0 && y >= 0 && x < b.getWidth() && y < b.getHeight() && (b.getRGB(x, y) >>> 24) >= 128;
    }
}
