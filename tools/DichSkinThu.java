import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Dich toa do khung trong mot tep du lieu thu cuoi (map/skinthu/<n>, map/d_effect/<id>)
 * theo TUONG DOI, sua tai cho.
 *
 *   java DichSkinThu <tep> khoi <xuong|len|ngang|tatca> dy <n> [dx <n>]
 *   java DichSkinThu <tep> anh <id,id,...> dx <n> [dy <n>]
 *
 * Khac voi CanChoNgoiSkinThu (dat MOC TUYET DOI theo "lut") va CanNgangSkinThu (tu tim so
 * dich theo do phu chan nguoi), cong cu nay chi cong them mot so vao dx/dy - dung khi da
 * do duoc con so muon sua bang tools/XemSkinNhuTrongGame hay tools/ThuChoNgoi.
 *
 * "khoi" = 7 khung mot huong theo Char.getFrameHorse: xuong 0..6, len 7..13, ngang 14..20.
 * "anh"  = chi dich cac manh dung anh con co id trong danh sach (moi khung, moi khoi) - dung
 *          de tach rieng mot bo phan (vi du doi rong khoi xuong cua Kim Cang Long, xem
 *          docs/skin-linh-thu.md muc 9) ma khong dong toi phan con lai.
 *
 * Dinh dang tep (xem tools/XemSkinNhuTrongGame): byte soAnh; soAnh x {id,x,y,w,h};
 * short soKhung; moi khung: byte soManh; moi manh: short dx, short dy, byte idAnh, byte lat,
 * byte tren.
 *
 * Muon ve ban goc: git checkout -- <tep>
 */
public final class DichSkinThu {

    public static void main(final String[] t) throws Exception {
        if (t.length < 5) {
            System.out.println("Dung: java DichSkinThu <tep> khoi <xuong|len|ngang|tatca> dy <n> [dx <n>]");
            System.out.println("      java DichSkinThu <tep> anh <id,id,...> dx <n> [dy <n>]");
            System.exit(2);
        }
        final String tep = t[0];
        final String cach = t[1];
        int dx = 0;
        int dy = 0;
        for (int i = 3; i + 1 < t.length; i += 2) {
            if (t[i].equals("dx")) {
                dx = Integer.parseInt(t[i + 1]);
            } else if (t[i].equals("dy")) {
                dy = Integer.parseInt(t[i + 1]);
            } else {
                System.out.println("Khong hieu tham so " + t[i]);
                System.exit(2);
            }
        }

        int khungTu = 0;
        int khungDen = Integer.MAX_VALUE;
        final java.util.Set<Integer> anh = new java.util.HashSet<Integer>();
        if (cach.equals("khoi")) {
            if (t[2].equals("xuong")) {
                khungTu = 0; khungDen = 7;
            } else if (t[2].equals("len")) {
                khungTu = 7; khungDen = 14;
            } else if (t[2].equals("ngang")) {
                khungTu = 14; khungDen = 21;
            } else if (!t[2].equals("tatca")) {
                System.out.println("Khoi phai la xuong | len | ngang | tatca");
                System.exit(2);
            }
        } else if (cach.equals("anh")) {
            for (final String s : t[2].split(",")) {
                anh.add(Integer.valueOf(s.trim()));
            }
        } else {
            System.out.println("Cach phai la 'khoi' hoac 'anh'");
            System.exit(2);
        }

        final byte[] b = Files.readAllBytes(Paths.get(tep));
        final int soAnh = b[0] & 0xFF;
        int vt = 1 + soAnh * 5;
        final int soKhung = ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
        vt += 2;
        int soManhSua = 0;
        for (int f = 0; f < soKhung; f++) {
            final int soManh = b[vt] & 0xFF;
            vt++;
            for (int k = 0; k < soManh; k++) {
                final boolean chon = cach.equals("khoi")
                        ? (f >= khungTu && f < khungDen)
                        : anh.contains(Integer.valueOf(b[vt + 4] & 0xFF));
                if (chon) {
                    ghi16(b, vt, doc16(b, vt) + dx);
                    ghi16(b, vt + 2, doc16(b, vt + 2) + dy);
                    soManhSua++;
                }
                vt += 7;
            }
        }
        if (soManhSua == 0) {
            System.out.println(tep + ": khong manh nao khop, khong ghi gi.");
            return;
        }
        Files.write(Paths.get(tep), b);
        System.out.println(tep + ": da dich " + soManhSua + " manh (" + cach + " " + t[2]
                + ") dx " + (dx >= 0 ? "+" : "") + dx + ", dy " + (dy >= 0 ? "+" : "") + dy + ".");
    }

    private static int doc16(final byte[] b, final int vt) {
        return (short) (((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF));
    }

    private static void ghi16(final byte[] b, final int vt, final int v) {
        b[vt] = (byte) (v >> 8);
        b[vt + 1] = (byte) v;
    }

    private DichSkinThu() {
    }
}
