import java.io.DataInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Chinh do "lut" cua chan nhan vat vao than con thu skin, sua ngay TRONG DU LIEU.
 *
 * VI SAO PHAI SUA O DU LIEU: client ve con thu ngay tai CHAN nhan vat roi nhac RIENG nhan
 * vat len bang Char.dy_horse — mot bang CO DINH, va ca ban Unity lan ban JAR deu chi co
 * DUNG MOT DONG {-20,-15,-15,-15}. Nen khong co cach nao bao client cho nguoi ngoi cao
 * hay thap hon tu phia server. Cai duy nhat server quyet duoc la TOA DO CAC KHUNG cua con
 * thu, va do chinh la thu nay sua. Nho vay no chay tren MOI client, khong can dung ban
 * nao, khong phai dong goi lai jar client.
 *
 * HAI CHIEU: dat lutMuon lon hon hien tai thi NANG con thu len (nguoi lun sau vao than
 * no, thay it nguoi hon); nho hon thi HA xuong (thay nhieu nguoi hon, nhung ha qua thi ho
 * ra khoang trang duoi mong).
 *
 * DAT MOC TUYET DOI, KHONG DICH TUONG DOI: chay xong thi ca ba khoi (xuong / len / ngang)
 * deu ve dung lutMuon, ke ca khi truoc do chung khac nhau. Nen chay hai lan voi hai so
 * khac nhau KHONG dua file ve nhu cu. Muon ve ban goc: git checkout -- map/skinthu
 *
 * CAI GIA PHAI TRA khi NANG: nang bao nhieu thi chan con thu roi mat dat bay nhieu. Tham
 * so dayToiThieu chan lai, khong bao gio nang qua muc lam day con thu cao hon moc do.
 * (Chi ap khi nang; ha xuong thi day cang sat dat, khong can chan.)
 *
 * DO TRUOC KHI SUA: chay tools/XemSkinNhuTrongGame de xem so lut hien tai, va de so voi
 * chinh thu tool cua game — Poro (map/d_effect/56) lut 12px, Heo (64) 15px, Lan (62) 19px
 * huong xuong. Nam skin dang o 11..14px, tuc trong khoang binh thuong.
 * test/KiemSkinThuCuoi.kiemChoNgoiSkin chot lai khoang nay.
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
                    + " <lut-muon | mau:<tep>> <day-toi-thieu>");
            System.out.println("  vd: java CanChoNgoiSkinThu map/skinthu 5 12 2");
            System.out.println("  vd: java CanChoNgoiSkinThu map/skinthu 5 mau:map/d_effect/56 2");
            System.exit(2);
        }
        final File thuMuc = new File(thamSo[0]);
        final int soSkin = Integer.parseInt(thamSo[1]);
        final int dayToiThieu = Integer.parseInt(thamSo[3]);

        // Muc tieu co the dat rieng cho tung khoi. Lay theo MOT CON THU TOOL CO SAN cua
        // game thi dung hon la tu nghi ra so: moi huong mot khac, va con thu that thi da
        // duoc can san cho khop voi cach client ve nguoi cuoi.
        final int[] lutMuon = new int[KHOI.length];
        if (thamSo[2].startsWith("mau:")) {
            final File mau = new File(thamSo[2].substring(4));
            if (!mau.isFile()) {
                System.out.println("Khong thay tep mau " + mau);
                System.exit(2);
            }
            final int[] cuaMau = doLut(mau);
            System.arraycopy(cuaMau, 0, lutMuon, 0, KHOI.length);
            System.out.print("Can theo mau " + mau + ", lut cua no:");
            for (int k = 0; k < KHOI.length; k++) {
                System.out.print("  " + TEN_KHOI[k].trim() + " " + lutMuon[k] + "px");
            }
            System.out.println();
        } else {
            final int mot = Integer.parseInt(thamSo[2]);
            java.util.Arrays.fill(lutMuon, mot);
            System.out.println("Muon chan nguoi lut " + mot + "px vao than thu o ca ba huong.");
        }
        System.out.println("Day con thu khong duoc cao hon " + dayToiThieu + "px duoi vach chan.");
        for (int n = 0; n < soSkin; n++) {
            can(new File(thuMuc, String.valueOf(n)), n, lutMuon, dayToiThieu);
        }
    }

    /** Do do "lut" cua tung khoi trong mot tep du lieu thu cuoi. */
    private static int[] doLut(final File tep) throws Exception {
        final byte[] b = Files.readAllBytes(tep.toPath());
        final int soAnh = b[0] & 0xFF;
        int vt = 1 + soAnh * 5;
        final int soKhung = ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
        vt += 2;
        final int[] tren = new int[KHOI.length];
        java.util.Arrays.fill(tren, Integer.MAX_VALUE);
        for (int f = 0; f < soKhung; f++) {
            final int soManh = b[vt] & 0xFF;
            vt++;
            for (int k = 0; k < soManh; k++) {
                for (int kh = 0; kh < KHOI.length; kh++) {
                    if (f >= KHOI[kh] && f < KHOI[kh] + KHUNG_MOI_KHOI) {
                        tren[kh] = Math.min(tren[kh], doc16(b, vt + 2));
                    }
                }
                vt += 7;
            }
        }
        final int[] ra = new int[KHOI.length];
        for (int kh = 0; kh < KHOI.length; kh++) {
            ra[kh] = (tren[kh] == Integer.MAX_VALUE) ? 0 : NHAC_THEO_KHOI[kh] - tren[kh];
        }
        return ra;
    }

    private static void can(final File tep, final int soHieu, final int[] lutMuon,
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
            int nang = lutMuon[khoi] - lutHienTai;          // duong = nang thu len, am = ha xuong
            if (nang == 0) {
                bao.append("  ").append(TEN_KHOI[khoi]).append(" dung roi (lut ")
                        .append(lutHienTai).append(")");
                continue;
            }
            // Chan lai: nang qua thi day con thu roi cao hon moc cho phep, nhin nhu bay.
            // Chi ap khi NANG; ha xuong thi day cang thap cang sat dat, khong can chan.
            if (nang > 0) {
                final int nangToiDa = duoi - dayToiThieu;
                if (nang > nangToiDa) {
                    nang = Math.max(0, nangToiDa);
                }
                if (nang == 0) {
                    bao.append("  ").append(TEN_KHOI[khoi]).append(" khong nang duoc (day da o ")
                            .append(duoi).append(")");
                    continue;
                }
            }
            for (int f = KHOI[khoi]; f < KHOI[khoi] + KHUNG_MOI_KHOI && f < soKhung; f++) {
                for (int k = 0; k < viTriDy[f].length; k++) {
                    ghi16(b, viTriDy[f][k], doc16(b, viTriDy[f][k]) - nang);
                }
            }
            coSua = true;
            bao.append("  ").append(TEN_KHOI[khoi]).append(nang > 0 ? " nang " : " ha ")
                    .append(Math.abs(nang))
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
