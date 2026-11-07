/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package real;

import data.Database;

/**
 *
 *
 */
public class TraHang {

    public static void excecute(Char player) {
        try {
            if (player.monster == null) {
                return;
            }

            if (player.map.mapIDLoadMap != 118) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng tại vị trí này", ""));
                return;
            }

            if (player.x / 16 >= 51 && player.x / 16 <= 59 && player.y / 16 >= 16 && player.y / 16 <= 24) {
                if (player.monster != null && player.monster.isFinish()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng", ""));
                    player.monster.actorDie();
                    player.monster = null;
                    player.typeTieu = -1;
                    return;
                }

                final String ly = xeTaiCho(player, "Không thể trả hàng do quá xa");
                if (ly != null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage(ly, ""));
                    return;
                }

                giao(player);
                return;
            }

            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể trả hàng tại vị trí này", ""));
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * sua-xe-lac-map (bao loi 07/11/2026, ruby): xe co o canh nguoi choi de giao? null = duoc; nguoc lai cau bao. Dung chung
     * cho Dịch trạm (tren) va Kecleon (HoTongNhiemVuGame.traTaiKecleon).
     * <ul>
     *   <li>xe o ban do / khu / vung khac ma {@link MonsterVantieu#ganChu} (chu roi ban do luc dang canh xe - duong doi ban do
     *       chua dua xe, vd tau truoc ban sua nay): keo xe ve canh nguoi choi roi giao - chu game: khong de nguoi choi mat
     *       chuyen vi loi;</li>
     *   <li>xe o noi khac do chu bo xe di xa: bao xe dang o dau (ten ban do + o) thay cho "khác map" cu;</li>
     *   <li>cung ban do ma xa hon 120 px: {@code xa}.</li>
     * </ul>
     */
    static String xeTaiCho(final Char player, final String xa) {
        final MonsterVantieu m = player.monster;
        if (!XeHangChuyenMap.cungCho(player, m)) {
            if (m.ganChu && XeHangChuyenMap.choXeVao(player.map)) {
                final String tu = XeHangChuyenMap.noiXe(m);
                XeHangChuyenMap.keoXe(player);
                XeHangChuyenMap.ghi(player, "xe lac o " + tu + " -> keo ve cho giao " + player.mapID);
                XeHangChuyenMap.bao(player, "Xe hàng bị lạc ở " + tu + " đã được đưa về đây.");
                return null;
            }
            return "Xe hàng đang ở " + XeHangChuyenMap.noiXe(m) + ". Hãy quay lại dẫn xe hàng đi cùng tới đây rồi trả hàng.";
        }
        if (!Map.inRangeActor((LiveActor) player, (LiveActor) m, 120)) {
            return xa;
        }
        return null;
    }

    /**
     * Giao xe hang (da kiem vi tri / cung ban do / tam): Dịch trạm (tren) va Kecleon tạp hoá cho hang nhiem vu ho tong
     * (HoTongNhiemVuGame.traTaiKecleon) dung chung.
     */
    static void giao(Char player) {
        player.monster.isFinish = true;
        player.doResponseTieu(player, false, player.typeTieu, player.lvDetail.lv);
        Database.instance.addCharVT(player, 0, 0, 0);

        player.typeTieu = -1;
        if (Char.isSuKienWordcup2017()) {
            final int bongBac = Map.ranDom(1, 2);
            player.potions[160] += bongBac;
            DiemMocNhiemVu.khiNhanVatPhamSuKien(player, 160, bongBac);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        }
    }
}
