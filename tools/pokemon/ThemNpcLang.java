import com.htbb.pokemon.DinhDangBanDo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Dat Y ta Chansey (NPC tinh loai 0, truoc cua Trung tam Pokemon) va Ong Snorlax (loai 8, gan Truong lang Xatu) vao ca
 * ba lang khoi dau (cMap 0 Pewter / 70 Blackthorn / 80 Cianwood) - huong dan tan thu (buoc hoi phuc) va chuoi chinh
 * (C01.2-C01.4 Snorlax, C01.4 / C01.5 Chansey, ~45 buoc Snorlax ve sau) can hai NPC nay o lang; truoc day Chansey chi o
 * nha 100 (Pewter) va Hoenn, Snorlax chi o san mo dau 105 (khong quay lai duoc) va Hoenn cap 90.
 *
 * Server gui nguyen tep cMap cho client khi doi ban do (Map.idMapSend2Client, MessageCreator goi 12) nen client 0.9.7
 * cung thay NPC moi. Chay lai khong them trung (loai da co tren ban do thi bo qua). Sau khi chay: cap nhat
 * data_npc.ban_do (sql/pokemon_ten_hien.sql + sql/htbb.sql (khoi them_huong_dan_tan_thu.sql); test/KiemNpc doi chieu).
 *
 * <pre>
 *   javac -cp server/build/classes -d /tmp/htbb-tools tools/pokemon/ThemNpcLang.java    (lop server da bien dich)
 *   (cd server && java -cp /tmp/htbb-tools:build/classes ThemNpcLang)
 * </pre>
 */
public class ThemNpcLang {

    /** {ban do, loai NPC, o x, o y} - o di duoc, khong tren cong, cach NPC khac >= 2 o (da xem luoi ban do). */
    static final int[][] DAT = {
        {0, 0, 38, 46}, {0, 8, 48, 12},
        {70, 0, 35, 48}, {70, 8, 37, 9},
        {80, 0, 21, 51}, {80, 8, 16, 9},
        // 0.9.8: Arcanine gac cong (loai 4, real.NpcDichVu.LOI_DI) o khu xa khong co cong / tau dan vao - diem "Về làng"
        // va diem den (o ngay duoi Arcanine): Nui Silver 42 (khong co cong), Chan nui Silver 41, Duong Victory 118
        {42, 4, 34, 41}, {41, 4, 30, 13}, {118, 4, 43, 22},
        // 30/10/2026: New Bark 10 (Arcanine "Đến New Bark", diem den 97,2) - cum Johto 10/11/13/14/15 chi noi voi
        // 113/114 (cap 72+) nen truoc day la duong mot chieu: khong hoi mau / mua thuoc / ve lang (tru So tay 1 luot/ngay).
        // Arcanine (Về làng + khu xa) canh diem den, Chansey (hoi phuc + thuoc) duoi loi doc.
        {10, 4, 94, 2}, {10, 0, 91, 8},
        // 30/10/2026: Dong co phia tay 21 / Tau S.S. Anne 22 (va ban sao 721..724 / 741..744) khong co cong / NPC nao dan
        // vao: Arcanine "Đến ..." (khoa ban do cap 61 / 65 + CA-10.3) + Arcanine ben trong de "Về làng" (diem den ngay duoi)
        {21, 4, 36, 2}, {22, 4, 30, 27},
    };

    public static void main(final String[] a) throws Exception {
        for (final int[] d : DAT) {
            final Path p = Paths.get("cMap", String.valueOf(d[0]));
            final byte[] b = Files.readAllBytes(p);
            final DinhDangBanDo m = DinhDangBanDo.doc(b);
            boolean co = false;
            for (final DinhDangBanDo.Diem n : m.npc) {
                co |= n.v == d[1];
            }
            if (co) {
                System.out.println("cMap/" + d[0] + ": da co NPC loai " + d[1]);
                continue;
            }
            if (!m.coNpc) {
                throw new IllegalStateException("cMap/" + d[0] + " khong co khoi NPC");
            }
            m.npc.add(new DinhDangBanDo.Diem(d[2], d[3], d[1]));
            final byte[] moi = m.ghi();
            if (DinhDangBanDo.doc(moi).npc.size() != m.npc.size()) {
                throw new IllegalStateException("ghi lai cMap/" + d[0] + " sai");
            }
            Files.write(p, moi);
            System.out.println("cMap/" + d[0] + ": them NPC loai " + d[1] + " o " + d[2] + "," + d[3]);
        }
    }
}
