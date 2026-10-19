package game.render.screen;

import game.networklogic.GameService;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Màn chuyển bản đồ (chờ server gửi bản đồ mới). Kiểu Pokémon cùng màn tải chính (Canvas: nền banner + Poké Ball
 * quay): nền banner (thiếu ảnh -> trời + đồi vẽ bằng code), thẻ trắng mờ giữa dưới có Poké Ball lắc / quay,
 * dòng "Đang tải bản đồ" + dấu chấm chạy, thanh tiến trình (ước lượng theo thời gian, chạy chậm dần tới ~95%;
 * xong thì server chuyển màn). Chỉ lệnh Graphics MIDP 2 (fillRect / fillArc), số nguyên.
 * 30 giây chưa có bản đồ: gửi lại yêu cầu đổi bản đồ (như cũ).
 */
public class ChangScr extends Screen {
    private static ChangScr me;
    int lastTime;
    private int map = -1;
    private int xMap;
    private int yMap;
    /** mốc bắt đầu chờ (ms) - cho thanh tiến trình */
    private long batDau;

    public static ChangScr gI() {
        if (me != null) {
            return me;
        }
        ChangScr changScr = new ChangScr();
        me = changScr;
        return changScr;
    }

    public void setMap(int map, int xMap, int yMap) {
        this.map = map;
        this.xMap = xMap;
        this.yMap = yMap;
    }

    @Override
    public void update() {
        if (this.map > -1 && (System.currentTimeMillis() / 1000) - ((long) this.lastTime) > 30) {
            this.lastTime = (int) (System.currentTimeMillis() / 1000);
            GameService.gI().changeMap(this.map, this.xMap, this.yMap);
        }
    }

    /** tên bản đồ lúc bắt đầu chờ: GameScr.nameMap khác đi = đã biết tên bản đồ đích */
    private String tenCu;

    @Override
    public void paint(Graphics g) {
        // 0.9.5: màn tải duy nhất (game.pokemon.ManTai) - đường vào game hiện dãy bước, đổi bản đồ hiện tên bản đồ đích
        if (!game.pokemon.ManTai.dangVao) {
            String t = GameScr.nameMap;
            game.pokemon.ManTai.tenMap = t != null && t.length() > 0 && !t.equals(this.tenCu) ? t : null;
        }
        game.pokemon.ManTai.ve(g, game.pokemon.ManTai.dangVao ? game.pokemon.ManTai.buoc : game.pokemon.ManTai.BAN_DO);
    }

    @Override
    public void switchToMe() {
        this.lastTime = (int) (System.currentTimeMillis() / 1000);
        this.batDau = System.currentTimeMillis();
        this.tenCu = GameScr.nameMap;
        if (!game.pokemon.ManTai.dangVao) {
            game.pokemon.ManTai.doiMap(-1);   // đổi bản đồ / dịch chuyển trong game
        } else if (game.pokemon.ManTai.dangMo()) {
            Canvas.endDlg();   // màn này vẽ màn tải - bỏ hộp chặn (một lớp, một vòng quay)
        }
        super.switchToMe();
    }
}
