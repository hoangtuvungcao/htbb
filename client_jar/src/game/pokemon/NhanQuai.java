package game.pokemon;

import game.model.Monster;
import game.model.MonsterTemplate;
import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.HudRong;
import game.pokemon.PokeBody;
import javax.microedition.lcdui.Graphics;

/**
 * Nhãn trên đầu quái (lớp thế giới, Monster.paintName): "Lv N Tên" đặt sát đỉnh hình thật (chiều cao hình Pokémon
 * PokeBody.caoTrenChan đã ghi vào Monster.height lúc vẽ, như hàng tên người chơi Char.dyHangTen) + thanh HP nhỏ khi
 * đang là mục tiêu hoặc vừa trúng đòn. Màn rộng: nhãn luôn hiện; màn nhỏ: chỉ mục tiêu / vừa trúng (đỡ rối).
 */
public final class NhanQuai {
    private NhanQuai() {
    }

    /** Số tick (40 ms) thanh HP còn hiện sau cú trúng cuối. */
    static final int TICK_HP = 75;

    public static void ve(Graphics g, Monster m) {
        if (m.hp <= 0 || m.state == 8 || m.state == 1 || m.state == 5 || !m.isPaint() || Canvas.gameScr == null || Canvas.gameScr.hideGUI == 2) {
            return;
        }
        int t = m.monster_type;
        boolean laPoke = MonsterTemplate.laPokemon(t);
        boolean chon = Canvas.gameScr.focusedActor == m;
        int tuoi = m.pokeTrungLuc < 0 ? 9999 : Canvas.gameTick - m.pokeTrungLuc;
        if (tuoi < 0) {
            tuoi += 10001;
        }
        boolean vuaTrung = tuoi < TICK_HP;
        boolean rong = HudRong.bat();
        if (!chon && !vuaTrung && !(rong && laPoke)) {
            return;
        }
        // đỉnh hình THẬT (như Char.dyHangTen: PokeBody.caoHinh = nửa khung + lệch chân), không phải khung có lề trong suốt
        int cao = 0;
        if (laPoke) {
            int tl = MonsterTemplate.pokeTiLe[t] <= 0 ? 100 : MonsterTemplate.pokeTiLe[t];
            cao = PokeBody.caoHinh(MonsterTemplate.pokeDex[t], MonsterTemplate.pokeShiny[t]) * tl / 100;
        }
        if (cao <= 0) {
            cao = m.height < 16 ? 16 : m.height;
        }
        int yDinh = m.y + m.dy + m.z - cao;
        if (NhanTheGioi.bat()) {
            // 0.9.5: nhãn ở lớp giao diện (chữ nhỏ viền tối, xếp chỗ chống chồng) - NhanTheGioi
            int hp = (chon || vuaTrung) && m.maxhp > 0 ? (int) ((long) (m.hp < 0 ? 0 : m.hp) * 1000 / m.maxhp) : -1;
            String ten = m.getDisplayName();
            NhanTheGioi.quai(m, m.level > 0 ? NhanTheGioi.lv(m.level) : null, ten == null ? "" : ten, m.x, yDinh - 1, m.y + m.dy + m.z, hp, chon, vuaTrung);
            return;
        }
        int y = yDinh - 3;
        if (chon || vuaTrung) {
            if (m.maxhp > 0) {
                int w = 26;
                GiaoDien.thanh(g, m.x - w / 2, y - 4, w, 4, m.hp, m.maxhp, 0);
                y -= 4;
            }
        }
        String s = nhan(m);
        if (s.length() > 0) {
            Font f = Font.smallFont;
            int w = f.getWidth(s), h = f.getHeight();
            int x0 = m.x - w / 2, y0 = y - h;
            // quái đứng sát nhau: nhãn thường chồng nhãn đã vẽ trong khung này thì bỏ (mục tiêu / vừa trúng luôn vẽ)
            if (!chon && !vuaTrung && chongNhan(x0, y0, w, h)) {
                return;
            }
            ghiNhan(x0, y0, w, h);
            f.drawString(g, s, m.x, y0, Font.CENTER);
        }
    }

    private static final int TOI_DA = 32;
    private static final int[] NX = new int[TOI_DA], NY = new int[TOI_DA], NW = new int[TOI_DA], NH = new int[TOI_DA];
    private static int soNhan, tickNhan = -1;

    private static boolean chongNhan(int x, int y, int w, int h) {
        if (tickNhan != Canvas.gameTick) {
            return false;
        }
        for (int i = 0; i < soNhan; i++) {
            if (x < NX[i] + NW[i] && NX[i] < x + w && y < NY[i] + NH[i] && NY[i] < y + h) {
                return true;
            }
        }
        return false;
    }

    private static void ghiNhan(int x, int y, int w, int h) {
        if (tickNhan != Canvas.gameTick) {
            tickNhan = Canvas.gameTick;
            soNhan = 0;
        }
        if (soNhan < TOI_DA) {
            NX[soNhan] = x;
            NY[soNhan] = y;
            NW[soNhan] = w;
            NH[soNhan] = h;
            soNhan++;
        }
    }

    /** "Lv N Tên" nhớ theo quái (không tạo chuỗi mỗi khung). */
    static String nhan(Monster m) {
        String ten = m.getDisplayName();
        if (ten == null) {
            ten = "";
        }
        if (m.nhanTen == ten && m.nhanLv == m.level && m.nhan != null) {
            return m.nhan;
        }
        m.nhanTen = ten;
        m.nhanLv = m.level;
        m.nhan = m.level > 0 ? new StringBuffer(ten.length() + 6).append("Lv").append((int) m.level).append(' ').append(ten).toString() : ten;
        return m.nhan;
    }
}
