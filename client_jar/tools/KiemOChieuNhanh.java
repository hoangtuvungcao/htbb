import game.pokemon.OChieuNhanh;

/**
 * Kiểm thử thuần Java cho bảng gán chiêu vào thanh ô nhanh (game.pokemon.OChieuNhanh, dùng bởi ChonChieuNhanh):
 * chiêu gán được, bố cục nằm trọn trong màn (640x360 PC / Android, 240x320 / 176x208 J2ME), chạm trúng đúng chiêu,
 * nút chân trong bảng, đổi chỗ ô, chữ hồi chiêu.
 *
 *   O=$(mktemp -d); javac -d $O client_jar/src/game/pokemon/OChieuNhanh.java client_jar/tools/KiemOChieuNhanh.java
 *   java -cp $O KiemOChieuNhanh      # mã thoát 1 = hỏng
 */
public final class KiemOChieuNhanh {
    static int dat, hong;

    static void bang(String mo, Object mong, Object that) {
        if (mong.equals(that)) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG " + mo + ": mong " + mong + " thuc te " + that);
        }
    }

    static void dung(String mo, boolean v) {
        bang(mo, Boolean.TRUE, Boolean.valueOf(v));
    }

    static String chuoi(int[] a) {
        StringBuffer b = new StringBuffer("[");
        for (int i = 0; i < a.length; i++) b.append(i > 0 ? "," : "").append(a[i]);
        return b.append("]").toString();
    }

    public static void main(String[] x) {
        // SkillManager.SKILL_CAN_BUFF_TO_USER của jar
        byte[][] buff = {new byte[]{-1, 0}, new byte[]{0, -1}, new byte[]{0, -1, 1, 0}, new byte[]{0, -1}, new byte[]{0, -1}};
        bang("lop 0: chua hoc / buff tu dung bi bo", "[0,2,5,8]",
                chuoi(OChieuNhanh.ganDuoc(0, new byte[]{1, 0, 2, -1, 1, 1, 0, 0, 3, 0, 0}, buff, 9)));
        bang("lop 2: 11 chieu, buff 5 tu dung bi bo", "[0,1,4,6,7,10]",
                chuoi(OChieuNhanh.ganDuoc(2, new byte[]{1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 2}, buff, 11)));
        bang("chua co cap hoc", "[]", chuoi(OChieuNhanh.ganDuoc(1, null, buff, 9)));
        dung("lop 2 chieu 7 la buff", OChieuNhanh.laBuff(2, 7));
        dung("lop 0 chieu 6 khong la buff", !OChieuNhanh.laBuff(0, 6));

        int[][] man = {{640, 360}, {240, 320}, {176, 208}, {320, 240}};
        for (int m = 0; m < man.length; m++) {
            int W = man[m][0], H = man[m][1];
            for (int n = 0; n <= 11; n++) {
                int[] bc = OChieuNhanh.boCuc(W, H, n);
                String ten = W + "x" + H + " n=" + n;
                dung(ten + " bang trong man", bc[0] >= 0 && bc[1] >= 0 && bc[0] + bc[2] <= W && bc[1] + bc[3] <= H - OChieuNhanh.DAY_PHIM + 4);
                dung(ten + " bang canh giua ngang", Math.abs(bc[0] + bc[2] / 2 - W / 2) <= 1);
                dung(ten + " hang chieu tren nut chan", bc[7] + bc[5] * bc[6] <= OChieuNhanh.nutChan(bc, 0, 4)[1]);
                for (int k = 1; k <= 4; k++) {
                    for (int i = 0; i < k; i++) {
                        int[] r = OChieuNhanh.nutChan(bc, i, k);
                        dung(ten + " nut chan " + i + "/" + k + " trong bang", r[0] >= bc[0] && r[0] + r[2] <= bc[0] + bc[2] && r[1] + r[3] <= bc[1] + bc[3] && r[2] > 10);
                    }
                }
                // chạm giữa từng ô chiêu đang hiện -> đúng chỉ số
                int cot = bc[4], wCot = (bc[2] - 12) / cot;
                boolean ok = true;
                for (int i = 0; i < Math.min(n, bc[4] * bc[5]); i++) {
                    int px = bc[0] + 6 + (i % cot) * wCot + wCot / 2, py = bc[7] + (i / cot) * bc[6] + bc[6] / 2;
                    if (OChieuNhanh.chiSoTai(bc, n, 0, px, py) != i) ok = false;
                }
                dung(ten + " cham trung dung chieu", ok);
                bang(ten + " cham ngoai = -1", Integer.valueOf(-1), Integer.valueOf(OChieuNhanh.chiSoTai(bc, n, 0, bc[0] - 3, bc[1] + 2)));
            }
        }
        int[] bc = OChieuNhanh.boCuc(176, 208, 11);
        dung("man nho 11 chieu: co cuon", OChieuNhanh.cuonToiDa(bc, 11) > 0);
        int px = bc[0] + 10, py = bc[7] + 2;
        bang("cuon 2 hang: cham hang dau = chieu thu 2*cot", Integer.valueOf(2 * bc[4]), Integer.valueOf(OChieuNhanh.chiSoTai(bc, 11, 2, px, py)));

        String[] o = {"a", "b", "c", "d", "e"};
        OChieuNhanh.doiCho(o, 0, 1);
        bang("doi cho 0-1", "b", o[0]);
        OChieuNhanh.doiCho(o, 4, 5);
        bang("doi cho ngoai mang khong doi", "e", o[4]);
        bang("giay 800", "0.8s", OChieuNhanh.giay(800));
        bang("giay 1500", "1.5s", OChieuNhanh.giay(1500));
        bang("giay 12000", "12s", OChieuNhanh.giay(12000));
        bang("giay 0", "0s", OChieuNhanh.giay(0));
        System.out.println("KiemOChieuNhanh: DAT " + dat + ", HONG " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }
}
