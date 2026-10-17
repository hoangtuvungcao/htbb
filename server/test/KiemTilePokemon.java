import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import com.htbb.pokemon.CheDoPokemon;
import com.htbb.pokemon.TrangTriPokemon;

import data.DataGame;

/**
 * Bo tile ban do PMD (G7.A, sinh boi tools/pokemon/DoiTileMap.java).
 *
 * VI SAO: map chi luu CHI SO tile, kieu di duoc/chan nam o cMap/*.type theo chi so. Bo tile moi
 * lech so tile hay thu tu la ban do ve sai/vang tile ma khong bao loi; lech mat na trong suot
 * thi tile phu (tileTop) che mat nen. Server gui t_hang/t_thanh trong goi dang nhap bang
 * writeShort nen anh > 32767 byte se lam hong goi.
 */
public final class KiemTilePokemon {

    private static final String[] BO = {"t", "t_hang", "t_thanh"};
    private static final String[] NHO = {"t_small", "t_hang_s", "t_thanh_s"};

    public static void chay() {
        Kiem.nhom("Tile ban do Pokemon (G7.A: map/pokemon_tile, Resources/poke_tile)");
        List<String> csv = null;
        try {
            csv = Files.readAllLines(new File("docs/pokemon/bang_tile.csv").toPath(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            // bao o duoi
        }
        Kiem.dung("co docs/pokemon/bang_tile.csv", csv != null);
        for (int b = 0; b < BO.length; b++) {
            final String ten = BO[b];
            try {
                final BufferedImage cu = ImageIO.read(new File("map/" + ten + ".png"));
                final File fMoi = new File(DataGame.THU_MUC_TILE_POKEMON + ten + ".png");
                final BufferedImage moi = ImageIO.read(fMoi);
                final byte[] kieu = Files.readAllBytes(new File("cMap/" + ten + ".type").toPath());
                Kiem.bang(ten + ": rong 16", 16, moi.getWidth());
                Kiem.bang(ten + ": cung chieu cao (so tile) voi ban goc", cu.getHeight(), moi.getHeight());
                Kiem.bang(ten + ": so tile = so kieu trong cMap", kieu.length, moi.getHeight() / 16);
                int lechMatNa = 0;
                for (int y = 0; y < cu.getHeight(); y++) {
                    for (int x = 0; x < 16; x++) {
                        if (((cu.getRGB(x, y) >>> 24) >= 128) != ((moi.getRGB(x, y) >>> 24) >= 128)) {
                            lechMatNa++;
                        }
                    }
                }
                Kiem.bang(ten + ": mat na trong suot giu nguyen", 0, lechMatNa);
                final byte[] client = Files.readAllBytes(
                    new File("ClientUnity/Assets/Resources/poke_tile/" + ten + ".bytes").toPath());
                Kiem.dung(ten + ": ban client trung ban server", Arrays.equals(client, Files.readAllBytes(fMoi.toPath())));
                if (!ten.equals("t")) {
                    Kiem.dung(ten + ": vua goi dang nhap (<= 32767 byte)", fMoi.length() <= Short.MAX_VALUE);
                }
                final BufferedImage nho = ImageIO.read(new File(DataGame.THU_MUC_TILE_POKEMON + NHO[b] + ".png"));
                Kiem.bang(NHO[b] + ": 1 pixel moi tile", kieu.length, nho.getHeight());
                if (csv != null) {
                    int dong = 0;
                    int lechKieu = 0;
                    for (final String s : csv) {
                        final String[] o = s.split(",", -1);
                        if (o.length >= 7 && o[0].equals(ten)) {
                            dong++;
                            final int i = Integer.parseInt(o[1]);
                            if (i >= kieu.length || Integer.parseInt(o[2]) != (kieu[i] & 255)) {
                                lechKieu++;
                            }
                            // Van nuoc PMD chi duoc dung cho o CHAN (o di duoc mau nuoc la "nuoc_nong").
                            final boolean coNuoc = (o[3].equals("nuoc") && !o[4].equals("doi_mau"))
                                || o[5].matches("(^|.* \\+ )nuoc=.*");
                            if (coNuoc && i < kieu.length && (kieu[i] & 255) != 2) {
                                lechKieu++;
                            }
                        }
                    }
                    Kiem.bang(ten + ": bang_tile.csv du dong", kieu.length, dong);
                    Kiem.bang(ten + ": kieu trong csv khop cMap", 0, lechKieu);
                }
            } catch (final Exception e) {
                Kiem.dung(ten + ": doc duoc anh tile (" + e + ")", false);
            }
        }
        kiemTrangTri();
        final boolean cu = CheDoPokemon.bat();
        try {
            CheDoPokemon.dat(false);
            Kiem.bang("tat che do: gui tile goc", "map/t_hang.png", DataGame.duongTileGuiClient()[0]);
            CheDoPokemon.dat(true);
            Kiem.bang("bat che do: gui tile Pokemon", DataGame.THU_MUC_TILE_POKEMON + "t_hang.png",
                DataGame.duongTileGuiClient()[0]);
            Kiem.bang("bat che do: t_thanh Pokemon", DataGame.THU_MUC_TILE_POKEMON + "t_thanh.png",
                DataGame.duongTileGuiClient()[1]);
            Kiem.bang("bat che do: cay ban do Pokemon", TrangTriPokemon.THU_MUC + "tree/6",
                TrangTriPokemon.duong("map/tree/6"));
            Kiem.bang("bat che do: tep khong co ban Pokemon thi giu goc", "map/tree/9999",
                TrangTriPokemon.duong("map/tree/9999"));
            CheDoPokemon.dat(false);
            Kiem.bang("tat che do: cay ban do goc", "map/tree/6", TrangTriPokemon.duong("map/tree/6"));
        } finally {
            CheDoPokemon.dat(cu);
        }
    }

    /**
     * A2: map/pokemon_trang_tri/tree/<i> giu dung dinh dang -48 (u16 + PNG + u16 + 6 byte) va
     * phan du lieu chi duoc doi dx/dy; moi tep goc deu co ban Pokemon cung kich thuoc mat na
     * (tru cay thay bang anh PMD).
     */
    private static void kiemTrangTri() {
        final File[] goc = new File("map/tree").listFiles();
        int thieu = 0;
        int sai = 0;
        for (final File f : goc == null ? new File[0] : goc) {
            try {
                final byte[] a = Files.readAllBytes(f.toPath());
                final File fm = new File(TrangTriPokemon.THU_MUC + "tree/" + f.getName());
                if (!fm.isFile()) {
                    thieu++;
                    continue;
                }
                final byte[] b = Files.readAllBytes(fm.toPath());
                final int la = ((a[0] & 255) << 8) | (a[1] & 255);
                final int lb = ((b[0] & 255) << 8) | (b[1] & 255);
                final byte[] da = Arrays.copyOfRange(a, 2 + la, a.length);
                final byte[] db = Arrays.copyOfRange(b, 2 + lb, b.length);
                final BufferedImage anh = ImageIO.read(new java.io.ByteArrayInputStream(b, 2, lb));
                if (anh == null || da.length != db.length || da[0] != db[0] || da[1] != db[1]
                    || !Arrays.equals(Arrays.copyOfRange(da, 4, da.length), Arrays.copyOfRange(db, 4, db.length))) {
                    sai++;
                }
            } catch (final Exception e) {
                sai++;
            }
        }
        Kiem.dung("trang tri: co map/tree", goc != null && goc.length > 0);
        Kiem.bang("trang tri: moi map/tree co ban Pokemon", 0, thieu);
        Kiem.bang("trang tri: dinh dang -48 + du lieu (tru dx/dy) giu nguyen", 0, sai);
        final File[] cay = new File("map/cay").listFiles();
        int thieuCay = 0;
        for (final File f : cay == null ? new File[0] : cay) {
            if (!new File(TrangTriPokemon.THU_MUC + "cay/" + f.getName()).isFile()) {
                thieuCay++;
            }
        }
        Kiem.bang("trang tri: moi map/cay co ban Pokemon", 0, thieuCay);
    }
}
