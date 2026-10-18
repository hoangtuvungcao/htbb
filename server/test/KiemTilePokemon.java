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
 * Bo tile ban do moi (CC0, sinh boi tools/pokemon/DungBoTileMoi.java; thay bo G7.A1 cua DoiTileMap).
 *
 * VI SAO: map chi luu CHI SO tile, kieu di duoc/chan nam o .type theo chi so. Anh tile, .type cua
 * server, ban Unity va ban J2ME lech nhau la ban do ve sai hoac di xuyen tuong ma khong bao loi.
 * Client Unity to mau ban do nho bang mang co do dai co dinh (232/116/216) - qua so tile la tran
 * mang. Server gui t_hang/t_thanh trong goi dang nhap bang writeShort nen anh > 32767 byte se hong goi.
 */
public final class KiemTilePokemon {

    private static final String[] BO = {"t", "t_hang", "t_thanh"};
    private static final String[] NHO = {"t_small", "t_hang_s", "t_thanh_s"};

    /** Mang mau ban do nho cua client Unity (Tilemap.mangMau / mangMauHang / mangMauThanh): so tile toi da. */
    private static final int[] TOI_DA = {232, 116, 216};

    public static void chay() {
        Kiem.nhom("Tile ban do Pokemon (bo moi CC0: map/pokemon_tile, Resources/poke_tile, cMap_pokemon/*.type)");
        List<String> csv = null;
        try {
            csv = Files.readAllLines(new File("../docs/pokemon/bang_tile_moi.csv").toPath(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            // bao o duoi
        }
        Kiem.dung("co docs/pokemon/bang_tile_moi.csv", csv != null);
        for (int b = 0; b < BO.length; b++) {
            final String ten = BO[b];
            try {
                final File fMoi = new File(DataGame.THU_MUC_TILE_POKEMON + ten + ".png");
                final BufferedImage moi = ImageIO.read(fMoi);
                final byte[] kieu = Files.readAllBytes(new File("cMap_pokemon/" + ten + ".type").toPath());
                Kiem.bang(ten + ": rong 16", 16, moi.getWidth());
                Kiem.bang(ten + ": so tile = so kieu trong cMap_pokemon", kieu.length, moi.getHeight() / 16);
                Kiem.dung(ten + ": so tile <= mang mau ban do nho client (" + TOI_DA[b] + ")", kieu.length <= TOI_DA[b]);
                Kiem.dung(ten + ": so tile < 254 (254/255 = o trong)", kieu.length < 254);
                final byte[] client = Files.readAllBytes(
                    new File("../client_pc_ios/ClientUnity/Assets/Resources/poke_tile/" + ten + ".bytes").toPath());
                Kiem.dung(ten + ": ban client trung ban server", Arrays.equals(client, Files.readAllBytes(fMoi.toPath())));
                Kiem.dung(ten + ": kieu Unity (poke_map/kieu_*) trung server", Arrays.equals(kieu,
                    Files.readAllBytes(new File("../client_pc_ios/ClientUnity/Assets/Resources/poke_map/kieu_" + ten + ".bytes").toPath())));
                Kiem.dung(ten + ": kieu J2ME (tools/pokemon/map_j2me) trung server", Arrays.equals(kieu,
                    Files.readAllBytes(new File("../tools/pokemon/map_j2me/" + ten + ".type").toPath())));
                if (!ten.equals("t")) {
                    Kiem.dung(ten + ": vua goi dang nhap (<= 32767 byte)", fMoi.length() <= Short.MAX_VALUE);
                }
                final BufferedImage nho = ImageIO.read(new File(DataGame.THU_MUC_TILE_POKEMON + NHO[b] + ".png"));
                Kiem.bang(NHO[b] + ": 1 pixel moi tile", kieu.length, nho.getHeight());
                if (csv != null) {
                    int dong = 0;
                    int lechKieu = 0;
                    for (final String s : csv) {
                        final String[] o = s.split(",", 5);
                        if (o.length >= 4 && o[0].equals(ten)) {
                            dong++;
                            final int i = Integer.parseInt(o[1]);
                            if (i >= kieu.length || Integer.parseInt(o[3]) != (kieu[i] & 255)) {
                                lechKieu++;
                            }
                        }
                    }
                    Kiem.bang(ten + ": bang_tile_moi.csv du dong", kieu.length, dong);
                    Kiem.bang(ten + ": kieu trong csv khop .type", 0, lechKieu);
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
