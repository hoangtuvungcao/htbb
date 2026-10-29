package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.Main;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Menu extends Main {
    public boolean showMenu;
    public Vector menuItems;
    public int selected;
    public int menuX;
    public int menuY;
    public int menuW;
    public int menuH;
    public int menuTemY;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int xc;
    public static Image img = null;
    boolean trans;
    int cvyb;
    int cdyb;
    int ctoyBag;
    int pa = 0;
    boolean transX = false;
    int cyBag = 0;
    int cylim = 0;
    /** 0.9.11: đang kéo núm thanh cuộn */
    boolean keoNum;
    int cuonNumDau, pyNumDau;

    public Menu() {
        this.left = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                Menu.this.showMenu = false;
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.XAC_NHAN);   // pokemon: tieng chon
                if (Menu.this.selected != -1) {
                    ((Command) Menu.this.menuItems.elementAt(Menu.this.selected)).action.perform();
                }
            }
        });
        this.center = new Command("", this.left.action);
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Menu.this.showMenu = false;
                game.pokemon.AmThanh.phat(game.pokemon.AmThanh.HUY);   // pokemon: tieng dong
            }
        });
    }

    public void setIndex() {
        if (this.menuItems != null) {
            this.selected = GameScr.r.nextInt(this.menuItems.size());
            this.ctoyBag = (this.selected * this.hang) - (this.hang * (this.soHang / 2));
            if (this.ctoyBag < 0) {
                this.ctoyBag = 0;
            }
            if (this.ctoyBag > this.cylim) {
                this.ctoyBag = this.cylim;
            }
        }
    }

    public void startAt(Vector menuItems, int pos) {
        if (menuItems.size() <= 0) {
            return;
        }
        img = null;
        this.menuItems = menuItems;
        this.menuW = 0;
        this.menuH = 0;
        for (int i = 0; i < menuItems.size(); i++) {
            Command c = (Command) menuItems.elementAt(i);
            int w = Font.normalFont[0].getWidth(c.caption);
            if (w > this.menuW) {
                this.menuW = w;
            }
            this.menuH += Screen.ITEM_HEIGHT;
        }
        this.menuW += 20;   // pokemon: chỗ cho mũi tên chọn
        this.coHinh = false;
        for (int i = 0; i < menuItems.size(); i++) {
            if (game.pokemon.GiaoDien.hinhMuc(((Command) menuItems.elementAt(i)).caption) != null) {
                this.coHinh = true;     // có ít nhất một mục có biểu tượng: chừa cột biểu tượng
                break;
            }
        }
        if (this.coHinh) {
            this.menuW += 16;
        }
        if (this.menuW < 100) {
            this.menuW = 100;
        }
        if (this.menuW > Canvas.w - 14) {
            this.menuW = Canvas.w - 14;
        }
        // mục không vừa bề ngang: ngắt hai dòng (hàng cao gấp đôi), số hàng thấy theo chiều cao màn
        this.hang = Screen.ITEM_HEIGHT;
        this.soHang = 4;
        this.dong = null;
        if (!Canvas.isBB && Font.blackFont != null) {
            int wc = this.menuW - (this.coHinh ? 23 : 7) - 8;
            String[][] d = new String[menuItems.size()][];
            boolean co = false;
            for (int i = 0; i < menuItems.size(); i++) {
                d[i] = ngatHai(Font.blackFont, ((Command) menuItems.elementAt(i)).caption, wc);
                co |= d[i] != null;
            }
            if (co) {
                this.dong = d;
                this.hang = Font.blackFont.getHeight() * 2 + 6;
                this.soHang = Math.max(2, Math.min(4, (Canvas.h - 70) / this.hang));
                this.menuH = this.hang * menuItems.size();
            }
        }
        if (this.menuH > this.hang * this.soHang) {
            this.menuH = this.hang * this.soHang;
        }
        this.menuH += 4;
        this.menuY = (Canvas.h - 27) - this.menuH;
        if (pos == 0) {
            this.menuX = 7;
        } else if (pos == 1) {
            this.menuX = (Canvas.w - this.menuW) - 7;
        } else if (pos == 2) {
            this.menuX = (Canvas.w >> 1) - (this.menuW >> 1);
            this.menuY = (Canvas.h - this.menuH) >> 1;
        } else {
            this.menuX = (Canvas.w >> 1) - (this.menuW >> 1);
        }
        if (Canvas.h < 200) {
            this.menuY += 10;
        }
        this.menuTemY = Canvas.h - this.hang;
        this.showMenu = true;
        game.pokemon.AmThanh.phat(game.pokemon.AmThanh.MO);   // pokemon: tieng mo menu
        this.selected = 0;
        this.cylim = (this.menuItems.size() - this.soHang) * this.hang;
        if (this.cylim < 0) {
            this.cylim = 0;
        }
        this.selected = 0;
        this.cyBag = 0;
        this.ctoyBag = 0;
        if (Canvas.isBB) {
            Canvas.clearKeyPressed();
        }
    }

    private void setKey(int dir) {
        this.selected += dir;
        if (this.selected < 0) {
            this.selected = this.menuItems.size() - 1;
        }
        if (this.selected > this.menuItems.size() - 1) {
            this.selected = 0;
        }
        this.ctoyBag = (this.selected * this.hang) - (this.hang * (this.soHang / 2));
        if (this.ctoyBag < 0) {
            this.ctoyBag = 0;
        }
        if (this.ctoyBag > this.cylim) {
            this.ctoyBag = this.cylim;
        }
    }

    public void updateMenuKey() {
        if (Canvas.isKeyPressed(2)) {
            setKey(-1);
        } else if (Canvas.isKeyPressed(8)) {
            setKey(1);
        }
        if (keoThanh()) {
            super.updateKey();
            return;
        }
        if (Canvas.isPointerDown && Canvas.isPointer(this.menuX, this.menuY, this.menuW, this.menuH)) {
            if (!this.trans) {
                this.pa = this.cyBag;
                this.trans = true;
                this.cyBag = this.ctoyBag;
            }
            int b = this.menuTemY + 2;
            int a = ((this.ctoyBag + Canvas.py) - b) / this.hang;
            if (a < 0) {
                a = 0;
            }
            if (a > this.menuItems.size() - 1) {
                a = this.menuItems.size() - 1;
            }
            this.selected = a;
            if (Math.abs(Canvas.pyLast - Canvas.py) != 0) {
                this.ctoyBag = this.pa + (Canvas.pyLast - Canvas.py);
                if (this.ctoyBag < 0) {
                    this.ctoyBag = 0;
                }
                if (this.ctoyBag > this.cylim) {
                    this.ctoyBag = this.cylim;
                }
                this.selected = -1;
            }
        }
        if (Canvas.isPointerClick) {
            this.trans = false;
            if (Canvas.isPointer(this.menuX, this.menuY, this.menuW, this.menuH)) {
                Canvas.isPointerClick = false;
                int b2 = this.menuTemY + 2;
                int a2 = ((this.ctoyBag + Canvas.py) - b2) / this.hang;
                if (a2 < 0) {
                    a2 = 0;
                }
                if (a2 > this.menuItems.size() - 1) {
                    a2 = this.menuItems.size() - 1;
                }
                if (Math.abs(Canvas.pyLast - Canvas.py) <= 10 && this.cyBag == this.ctoyBag) {
                    this.selected = a2;
                    if (this.selected != -1 && this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
        }
        super.updateKey();
    }

    private boolean coHinh;
    /**
     * Cao một hàng + số hàng thấy. Mục quá dài cho màn hẹp (vd hạt giống vườn ~60 ký tự trên 240x320) được NGẮT HAI
     * DÒNG thay vì cắt "..": khi có mục như vậy mọi hàng cao gấp đôi (chọn / cuộn / chạm vẫn theo một bước cố định).
     */
    int hang = 1, soHang = 4;   // đặt ở startAt (Screen.ITEM_HEIGHT cần phông đã nạp)
    /** Hai dòng đã ngắt của từng mục (null = một dòng) - tính một lần lúc mở. */
    String[][] dong;

    /** Ngắt s thành tối đa hai dòng rộng <= w (theo khoảng trắng; dòng hai cắt ".." nếu vẫn dài). null = vừa một dòng. */
    static String[] ngatHai(Font f, String s, int w) {
        if (s == null || f.getWidth(s) <= w) {
            return null;
        }
        int cat = -1;
        for (int i = s.indexOf(' '); i > 0; i = s.indexOf(' ', i + 1)) {
            if (f.getWidth(s.substring(0, i)) > w) {
                break;
            }
            cat = i;
        }
        String a, b;
        if (cat <= 0) {
            int n = s.length() - 1;
            while (n > 1 && f.getWidth(s.substring(0, n)) > w) {
                n--;
            }
            a = s.substring(0, n);
            b = s.substring(n);
        } else {
            a = s.substring(0, cat);
            b = s.substring(cat + 1);
        }
        return new String[]{a, game.pokemon.GiaoDien.catChu(f, b.trim(), w)};
    }

    /** Thanh cuộn (như paintMenu): {x, rộng, y rãnh, cao rãnh, cao núm}; null = không có. */
    int[] thanh() {
        if (this.cylim <= 0 || this.menuItems == null || this.menuItems.size() == 0) {
            return null;
        }
        int hThanh = this.menuH - 6;
        int hNut = hThanh * this.soHang / this.menuItems.size();
        if (hNut < 8) {
            hNut = 8;
        }
        return new int[]{this.menuX + this.menuW - 4, 3, this.menuY + 3, hThanh, Math.min(hNut, hThanh)};
    }

    /**
     * 0.9.11: kéo NÚM thanh cuộn - núm theo con trỏ, danh sách theo tỉ lệ cùng chiều (trước: kéo trên thanh = vuốt nội
     * dung, ngược chiều); chạm rãnh = nhảy tới chỗ chạm. Trả true khi cú nhấn / thả thuộc về thanh.
     */
    boolean keoThanh() {
        int[] t = thanh();
        if (this.keoNum) {
            if (t != null && (Canvas.dangGiu || Canvas.isPointerDown)) {
                this.ctoyBag = this.cyBag = game.pokemon.ThanhCuon.keo(this.cuonNumDau, this.pyNumDau, Canvas.py, t[3], t[4], this.cylim);
                return true;
            }
            this.keoNum = false;
            this.trans = false;
            Canvas.isPointerClick = false;   // cú thả của lượt kéo núm: không chọn mục
            return true;
        }
        if (t != null && (Canvas.dangGiu || Canvas.isPointerDown)
                && game.pokemon.ThanhCuon.trung(Canvas.pxLast, Canvas.pyLast, t[0], t[1], t[2], t[3])) {
            int c = this.ctoyBag;
            if (!game.pokemon.ThanhCuon.trenNum(Canvas.pyLast, t[2], t[3], t[4], c, this.cylim)) {
                c = game.pokemon.ThanhCuon.toi(Canvas.pyLast, t[2], t[3], t[4], this.cylim);
            }
            this.keoNum = true;
            this.cuonNumDau = c;
            this.pyNumDau = Canvas.pyLast;
            this.selected = -1;
            this.ctoyBag = this.cyBag = game.pokemon.ThanhCuon.keo(c, this.pyNumDau, Canvas.py, t[3], t[4], this.cylim);
            return true;
        }
        return false;
    }

    public void paintMenu(Graphics g) {
        Canvas.resetTrans(g);
        if (this.cyBag != this.ctoyBag) {
            this.cvyb = (this.ctoyBag - this.cyBag) << 2;
            this.cdyb += this.cvyb;
            this.cyBag += this.cdyb >> 4;
            this.cdyb &= 15;
            if (this.cyBag < 0) {
                this.cyBag = 0;
            }
        }
        // pokemon: hộp thoại trắng viền tối, mục đang chọn nền vàng + mũi tên ▶, chữ tối
        game.pokemon.GiaoDien.hop(g, this.menuX - 4, this.menuY - 4, this.menuW + 8, this.menuH + 8);
        g.setClip(this.menuX, this.menuY, this.menuW, this.menuH);
        g.translate(this.menuX + 5, this.menuTemY + 2);
        for (int i = 0; i < this.menuItems.size(); i++) {
            int yy = (i * this.hang) - this.cyBag;
            if (yy < -2 * this.hang || yy > this.menuH + this.hang) {
                continue;   // ngoài khung (vùng cắt cũng bỏ) - không đo / vẽ chữ thừa
            }
            if (this.selected != -1 && i == this.selected) {
                game.pokemon.GiaoDien.chonSang(g, -3, yy, this.menuW - 4, this.hang);
                game.pokemon.GiaoDien.muiTen(g, 0, yy + (this.hang >> 1) - 3, 6, game.pokemon.GiaoDien.VIEN);
            } else if (i > 0) {
                g.setColor(game.pokemon.GiaoDien.XANH_NHAT);
                g.fillRect(0, yy, this.menuW - 10, 1);
            }
            String cap = ((Command) this.menuItems.elementAt(i)).caption;
            if (Canvas.isBB) {
                Font.blackFont.drawString(g, cap, Canvas.hw, 3 + yy, 2);
            } else {
                // pokemon: biểu tượng theo nội dung mục + chữ cắt ".." thay vì bị xén ở mép
                int xc = 7;
                if (this.coHinh) {
                    game.pokemon.GiaoDien.veHinhMuc(g, 6, yy + (this.hang - 13) / 2, cap);
                    xc = 23;
                }
                String[] hai = this.dong != null && i < this.dong.length ? this.dong[i] : null;
                if (hai != null) {
                    int fh = Font.blackFont.getHeight();
                    Font.blackFont.drawString(g, hai[0], xc, 3 + yy, 0);
                    Font.blackFont.drawString(g, hai[1], xc, 3 + yy + fh, 0);
                } else {
                    Font.blackFont.drawString(g, game.pokemon.GiaoDien.catChu(Font.blackFont, cap, this.menuW - xc - 8), xc,
                            yy + (this.hang - Screen.ITEM_HEIGHT) / 2 + 3, 0);
                }
            }
        }
        // thanh cuộn nhỏ khi danh sách dài hơn khung
        if (this.cylim > 0) {
            Canvas.resetTrans(g);
            int hThanh = this.menuH - 6;
            int hNut = hThanh * this.soHang / this.menuItems.size();
            if (hNut < 8) {
                hNut = 8;
            }
            int yNut = this.menuY + 3 + (hThanh - hNut) * this.cyBag / this.cylim;
            g.setColor(game.pokemon.GiaoDien.XANH_NHAT);
            g.fillRect(this.menuX + this.menuW - 4, this.menuY + 3, 3, hThanh);
            g.setColor(game.pokemon.GiaoDien.XANH);
            g.fillRect(this.menuX + this.menuW - 4, yNut, 3, hNut);
        }
        super.paint(g);
    }

    public void updateMenu() {
        if (this.menuTemY > this.menuY) {
            int delta = (this.menuTemY - this.menuY) >> 1;
            if (delta < 1) {
                delta = 1;
            }
            this.menuTemY -= delta;
        }
        this.menuTemY = this.menuY;
    }
}
