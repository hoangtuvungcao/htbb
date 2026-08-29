import java.io.DataInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Nang con thu skin len cho chan nhan vat lut sau vao than no, sua ngay TRONG DU LIEU.
 *
 * VI SAO PHAI SUA O DU LIEU: client ve con thu ngay tai CHAN nhan vat roi nhac RIENG nhan
 * vat len bang Char.dy_horse — mot bang CO DINH, va ca ban Unity lan ban JAR deu chi co
 * DUNG MOT DONG {-20,-15,-15,-15}. Nen khong co cach nao bao client ngoi thap xuong tu
 * phia server. Cai duy nhat server quyet duoc la TOA DO CAC KHUNG cua con thu, va do
 * chinh la thu nay sua. Nho vay no chay tren MOI client, khong can dung ban nao.
 *
 * CAI GIA PHAI TRA: nang con thu len bao nhieu thi chan no roi mat dat bay nhieu. Nen co
 * tham so dayToiThieu chan lai: khong bao gio nang qua muc lam day con thu cao hon moc do.
 * De doi chieu: Poro co day +7, Lan +4 — tuc day tu +2 tro len van nhin binh thuong.
 *
 * DOI XUNG VOI tools/XemSkinNhuTrongGame.java: chay cai do truoc va sau de nhin ket qua.
 *
 * Cach dung:
 *   java CanChoNgoiSkinThu map/skinthu 5 12 2
 *                          thu-muc     so  lut-muon  day-toi-thieu
 */
public final class CanChoNgoiSkinThu {

    /** Char.dy_horse[0] — nhac nhan vat len theo huong {xuong, len, trai, phai}. */
    private static final int[] NHAC_NGUOI = { -20, -15, -15, -15 };

    /** Char.getFrameHorse: xuong +0, len +7, ngang +14. Ba khoi, moi khoi 7 khung. */
    private static final int[] KHOI = { 0, 7, 14 };
    private static final int[] NHAC_THEO_KHOI = { NHAC_NGUOI[0], NHAC_NGUOI[1], NHAC_NGUOI[2] };
    private static final String[] TEN_KHOI = { "xuong", "len  ", "ngang" };
    private static final int KHUNG_MOI_KHOI = 7;

    public static void main(final String[] thamSo) throws Exception {
        if (thamSo.length < 4) {
            System.out.println("Dung: java CanChoNgoiSkinThu <thu-muc> <so-skin>"
                    + " <lut-muon> <day-toi-thieu>");
            System.exit(2);
        }
        final File thuMuc = new File(thamSo[0]);
        final int soSkin = Integer.parseInt(thamSo[1]);
        final int lutMuon = Integer.parseInt(thamSo[2]);
        final int dayToiThieu = Integer.parseInt(thamSo[3]);

        System.out.println("Muon chan nguoi lut " + lutMuon + "px vao than thu,"
                + " day con thu khong duoc cao hon " + dayToiThieu + "px duoi vach chan.");
        for (int n = 0; n < soSkin; n++) {
            can(new File(thuMuc, String.valueOf(n)), n, lutMuon, dayToiThieu);
        }
    }

    private static void can(final File tep, final int soHieu, final int lutMuon,
                            final int dayToiThieu) throws Exception {
        if (!tep.isFile()) {
            System.out.println("  skin " + soHieu + ": khong thay " + tep);
            return;
        }
        final byte[] b = Files.readAllBytes(tep.toPath());

        // Doc bang anh con de biet chieu cao tung o, roi do khung bao cua tung khoi.
        final DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(b));
        final int soAnh = in.readByte() & 0xFF;
        final int[] cao = new int[soAnh];
        for (int i = 0; i < soAnh; i++) {
            in.readUnsignedByte();
            in.readUnsignedByte();
            in.readUnsignedByte();
            in.readUnsignedByte();
            cao[i] = in.readUnsignedByte();
        }
        final int soKhung = in.readShort();

        // Ghi lai vi tri byte cua tung o "dy" de con sua tai cho.
        final int[][] viTriDy = new int[soKhung][];
        final int[][] idAnh = new int[soKhung][];
        int vt = 1 + soAnh * 5 + 2;
        for (int f = 0; f < soKhung; f++) {
            final int soManh = b[vt] & 0xFF;
            vt++;
            viTriDy[f] = new int[soManh];
            idAnh[f] = new int[soManh];
            for (int k = 0; k < soManh; k++) {
                viTriDy[f][k] = vt + 2;
                idAnh[f][k] = b[vt + 4] & 0xFF;
                vt += 7;
            }
        }

        final StringBuilder bao = new StringBuilder("  skin " + soHieu + ":");
        boolean coSua = false;
        for (int khoi = 0; khoi < KHOI.length; khoi++) {
            int tren = Integer.MAX_VALUE;
            int duoi = Integer.MIN_VALUE;
            for (int f = KHOI[khoi]; f < KHOI[khoi] + KHUNG_MOI_KHOI && f < soKhung; f++) {
                for (int k = 0; k < viTriDy[f].length; k++) {
                    final int dy = doc16(b, viTriDy[f][k]);
                    tren = Math.min(tren, dy);
                    duoi = Math.max(duoi, dy + cao[idAnh[f][k]]);
                }
            }
            if (tren == Integer.MAX_VALUE) {
                continue;
            }
            final int lutHienTai = NHAC_THEO_KHOI[khoi] - tren;
            int nang = lutMuon - lutHienTai;          // duong = phai nang con thu len
            if (nang <= 0) {
                bao.append("  ").append(TEN_KHOI[khoi]).append(" du (lut ").append(lutHienTai).append(")");
                continue;
            }
            // Chan lai: nang qua thi day con thu cao hon moc cho phep.
            final int nangToiDa = duoi - dayToiThieu;
            if (nang > nangToiDa) {
                nang = Math.max(0, nangToiDa);
            }
            if (nang == 0) {
                bao.append("  ").append(TEN_KHOI[khoi]).append(" khong nang duoc (day da o ")
                        .append(duoi).append(")");
                continue;
            }
            for (int f = KHOI[khoi]; f < KHOI[khoi] + KHUNG_MOI_KHOI && f < soKhung; f++) {
                for (int k = 0; k < viTriDy[f].length; k++) {
                    ghi16(b, viTriDy[f][k], doc16(b, viTriDy[f][k]) - nang);
                }
            }
            coSua = true;
            bao.append("  ").append(TEN_KHOI[khoi]).append(" nang ").append(nang)
                    .append("px (lut ").append(lutHienTai).append("->").append(lutHienTai + nang)
                    .append(", day ").append(duoi).append("->").append(duoi - nang).append(")");
        }
        if (coSua) {
            Files.write(Paths.get(tep.getPath()), b);
        }
        System.out.println(bao);
    }

    private static int doc16(final byte[] b, final int vt) {
        return (short) (((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF));
    }

    private static void ghi16(final byte[] b, final int vt, final int v) {
        b[vt] = (byte) (v >> 8);
        b[vt + 1] = (byte) v;
    }

    private CanChoNgoiSkinThu() {
    }
}
