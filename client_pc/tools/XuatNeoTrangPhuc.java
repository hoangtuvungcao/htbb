import java.io.PrintWriter;

/**
 * Ghi BẢNG NEO TRANG PHỤC (du_lieu/neo_trang_phuc.csv) của các loài người chơi (5 dòng khởi đầu x 3 dạng) từ CHÍNH hàm
 * client dùng lúc vẽ (game.pokemon.PokeBody.neoTrangPhuc -> NeoTrangPhuc.tinh, tính từ điểm ảnh từng khung): mỗi động tác x
 * hướng x khung. Để soát / so sánh khi đổi sprite; client không đọc tệp này (tự tính từ điểm ảnh khung đang vẽ, nhớ đệm).
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/tools/XuatNeoTrangPhuc.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O XuatNeoTrangPhuc du_lieu/neo_trang_phuc.csv
 */
public class XuatNeoTrangPhuc {
    public static void main(String[] a) throws Exception {
        int[] dex = {4, 5, 6, 1, 2, 3, 7, 8, 9, 133, 134, 135, 136, 172, 25, 26};
        String[] dt = {"dung", "chay", "danh_gan", "danh_xa", "trung_don", "guc_nga"};
        String[] huong = {"xuong", "len", "trai", "phai"};
        PrintWriter w = new PrintWriter(a[0], "UTF-8");
        w.println("dex,dong_tac,huong,khung,dinh_y,dau_x,dau_trai,dau_phai,mat_y,mat_tren,co_x,co_y,lung_x,lung_y,eo_y,than_trai,than_phai,day_y");
        int[] n = new int[game.pokemon.NeoTrangPhuc.SO];
        int dong = 0;
        for (int d : dex) {
            for (int t = 0; t < 6; t++) {
                int[] tt = game.pokemon.PokeBody.thongTinDongTac(d, false, t);
                if (tt == null) {
                    continue;
                }
                int sum = 0;
                for (int f = 0; f < tt[0]; f++) {
                    int tick = (sum * 25 + 59) / 60;
                    sum += tt[1 + f];
                    if (tick * 60 / 25 >= sum) {
                        continue;
                    }
                    for (int h = 0; h < 4; h++) {
                        if (!game.pokemon.PokeBody.neoTrangPhuc(d, false, h, t, tick, 100, n)) {
                            continue;
                        }
                        StringBuilder b = new StringBuilder().append(d).append(',').append(dt[t]).append(',').append(huong[h]).append(',').append(f);
                        for (int i = 0; i < n.length; i++) {
                            b.append(',').append(n[i] == game.pokemon.NeoTrangPhuc.KHONG ? "" : String.valueOf(n[i]));
                        }
                        w.println(b);
                        dong++;
                    }
                }
            }
        }
        w.close();
        System.out.println("neo trang phuc: " + dong + " dong -> " + a[0]);
    }
}
