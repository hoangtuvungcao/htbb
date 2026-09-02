import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CharCapNhat.updateNearChar co hai vong lap duyet danh sach doi tuong (self.nearChars va
 * self.getAllPlayerMap()) de gui vi tri cho client. Ca hai deu duyet TU CUOI VE DAU
 * (for k = size-1; k >= 0; --k) va boc try/catch RIENG cho moi phan tu.
 *
 * LOI (su co NPC hoa tieu/xa phu mat tich luc dong nguoi, 02/09/2026): catch cua CA HAI vong
 * deu "break" thay vi "continue". Mot loi RIENG LE o BAT KY doi tuong nao (vi du mot nguoi
 * choi vua thoat giua chung, con so du lieu chua kip don - de xay ra hon khi dong nguoi nhu
 * su kien boss the gioi) lam GAY LUON vong lap, bo qua het cac doi tuong CON LAI trong danh
 * sach. Vi danh sach duyet tu cuoi ve dau, va cac bot NPC dat vao map tu luc khoi dong nam o
 * DAU danh sach (chi so THAP nhat, them truoc nguoi choi that), ho la nhom bi bo qua ĐẦU
 * TIÊN moi khi co loi xay ra o mot doi tuong nao do co chi so cao hon. Sua: doi "break" thanh
 * "continue" o ca hai cho - mot doi tuong loi khong con lam hong nhung doi tuong khac.
 */
public final class KiemNearCharBoQuaLoiRiengLe {

    private KiemNearCharBoQuaLoiRiengLe() {
    }

    public static void chay() {
        Kiem.nhom("CharCapNhat.updateNearChar - loi rieng le khong duoc lam gay ca vong lap");

        final String s = doc("src/real/CharCapNhat.java");
        Kiem.dung("doc duoc CharCapNhat.java", !s.isEmpty());

        final String than = doan(s, "static void updateNearChar(Char self) {", "\n    static void subUpdate(Char self) {");
        Kiem.dung("tim thay than updateNearChar", !than.isEmpty());

        Kiem.dung("tim thay vong lap self.nearChars", than.contains("for (int j = self.nearChars.size() - 1; j >= 0; --j) {"));
        Kiem.sai("vong lap self.nearChars: catch KHONG con 'break'",
                than.contains("} catch (final Exception e) {\n                break;\n            }"));
        Kiem.dung("vong lap self.nearChars: catch dung 'continue'",
                than.contains("} catch (final Exception e) {") && than.contains("\n                continue;\n            }"));

        Kiem.dung("tim thay vong lap getAllPlayerMap", than.contains("final Vector<Char> players = self.getAllPlayerMap();"));
        Kiem.sai("vong lap getAllPlayerMap: catch KHONG con 'break' ngay sau dong LOI TIM NEARCHAR",
                than.contains("System.out.println(\"LOI TIM NEARCHAR TRONG CHAR \");\n                    break;"));
        Kiem.dung("vong lap getAllPlayerMap: catch dung 'continue' ngay sau dong LOI TIM NEARCHAR",
                than.contains("System.out.println(\"LOI TIM NEARCHAR TRONG CHAR \");\n                    continue;"));

        // Doi chieu tong so "break;" con lai trong than ham: phai la 0 (khong con cho nao
        // trong hai vong lap nay dung break nua). Dem tho, chi ap dung trong pham vi than ham
        // updateNearChar da cat o tren nen khong dam vao ham khac.
        Kiem.bang("khong con 'break;' nao trong than updateNearChar (ca hai vong da doi het)",
                Integer.valueOf(0), Integer.valueOf(soLanXuatHien(than, "break;")));
    }

    private static int soLanXuatHien(final String trong, final String mau) {
        int dem = 0;
        int viTri = 0;
        while (true) {
            viTri = trong.indexOf(mau, viTri);
            if (viTri < 0) {
                break;
            }
            dem++;
            viTri += mau.length();
        }
        return dem;
    }

    private static String doan(final String ma, final String tu, final String den) {
        final int bd = ma.indexOf(tu);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf(den, bd + tu.length());
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
