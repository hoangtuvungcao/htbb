package game.render.screen;

import game.model.ClanInfo;
import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class ViewInfoScr extends Screen {
    private static ViewInfoScr me;
    private Vector text;
    private short w;
    private short h;
    private Screen lastScr;
    private String title;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int selected = 0;
    public static int lastSe = 0;
    private int type = 0;
    private int nationID = -1;
    private short idClan = -1;
    private String status = "";
    int xa = 0;
    int dir = 1;

    public static ViewInfoScr gI() {
        if (me != null) {
            return me;
        }
        ViewInfoScr viewInfoScr = new ViewInfoScr();
        me = viewInfoScr;
        return viewInfoScr;
    }

    @Override
    public void switchToMe() {
        if (this.lastScr == null) {
            this.lastScr = Canvas.currentScreen;
        }
        super.switchToMe();
    }

    public ViewInfoScr() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                ViewInfoScr.this.lastScr.switchToMe();
                ViewInfoScr.this.lastScr = null;
                ViewInfoScr.this.text.removeAllElements();
                ViewInfoScr.me = null;
            }
        });
        this.left = new Command("Menu", new AnonymousClass2());
        this.w = (short) 150;
        this.h = (short) 170;
        lastSe = ((this.h - 30) / 2) / 14;
    }

    class AnonymousClass2 implements IAction {
        AnonymousClass2() {
        }

        @Override
        public void perform() {
            Vector menu = new Vector();
            if (ViewInfoScr.this.type == 0) {
                if (Canvas.gameScr.mainChar.isMaster == 0) {
                    menu.addElement(new Command("Đặt khẩu hiệu", new AnonymousClass3(this)));
                }
                menu.addElement(new Command("Danh sách thành viên", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg();
                        GameService.gI().requestClanList(ViewInfoScr.this.idClan, (byte) 0);
                    }
                }));
            }
            Canvas.menu.startAt(menu, 0);
        }
    }

    class AnonymousClass3 implements IAction {
        final AnonymousClass2 this$1;

        AnonymousClass3(AnonymousClass2 anonymousClass2) {
            this.this$1 = anonymousClass2;
        }

        @Override
        public void perform() {
            Canvas.inputDlg.setInfo("Khẩu hiệu", new IAction() {
                @Override
                public void perform() {
                    String str = Canvas.inputDlg.tfInput.getText();
                    if (!str.equals("")) {
                        GameService.gI().setSologan(str);
                        ViewInfoScr.this.status = str;
                        Canvas.endDlg();
                        ViewInfoScr.this.xa = 0;
                        ViewInfoScr.this.dir = 1;
                    }
                }
            }, 0, 50, false);
            Canvas.inputDlg.show();
        }
    }

    public void setInfo(String title, String str) {
        this.title = title;
        this.type = 2;
        this.text = new Vector();
        String[] tex = Font.normalFont[0].splitFontBStrInLine(str, this.w - 15);
        for (String str2 : tex) {
            this.text.addElement(str2);
        }
        setLim();
    }

    private void setLim() {
        cmyLim = ((this.text.size() * 14) - (this.h - 25)) + 20;
        if (cmyLim < 0) {
            cmyLim = 0;
        }
        cmtoY = 0;
        cmy = 0;
        selected = lastSe;
    }

    public void setInfo(ClanInfo clan) {
        this.title = clan.name.toUpperCase();
        this.type = 0;
        this.idClan = clan.id;
        this.text = new Vector();
        this.status = clan.status;
        this.text.addElement(new StringBuffer("Đội trưởng: ").append(clan.master).toString());
        this.text.addElement(new StringBuffer("Cấp độ: ").append((int) clan.level).toString());
        this.text.addElement(new StringBuffer("Quỹ: ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, clan.money)).toString());
        this.text.addElement(new StringBuffer("Thành viên: ").append((int) clan.members).toString());
        this.text.addElement(new StringBuffer("Điểm cống hiến: ").append(clan.dedicationPoint).toString());
        this.text.addElement(new StringBuffer("Điểm kinh ngiệm: ").append(clan.xp).toString());
        this.text.addElement(new StringBuffer("Thành lập: ").append(clan.date).toString());
        this.nationID = clan.nationID;
        if (clan.dissolved) {
            String[] str = Font.normalFont[0].splitFontBStrInLine(clan.strDissolved, this.w - 15);
            for (String str2 : str) {
                this.text.addElement(str2);
            }
        }
        setLim();
    }

    @Override
    public void updateKey() {
        boolean changeFocus = false;
        if (Canvas.keyHold[2]) {
            selected--;
            if (selected < lastSe) {
                selected = lastSe;
            }
            changeFocus = true;
        } else if (Canvas.keyHold[8]) {
            changeFocus = true;
            if (cmy < cmyLim) {
                selected++;
            }
            if (selected > (this.text.size() - lastSe) + 1) {
                selected = (this.text.size() - lastSe) + 1;
            }
        }
        if (changeFocus) {
            cmtoY = (selected * 14) - ((this.h - 25) / 2);
            if (cmtoY < 0) {
                cmtoY = 0;
            }
            if (cmtoY > cmyLim) {
                cmtoY = cmyLim;
            }
        }
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
        super.updateKey();
        if (this.lastScr != null) {
            this.lastScr.update();
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.lastScr != null) {
            this.lastScr.paint(g);
        }
        if (game.pokemon.ManCu.bat() && this.text != null) {
            // 31/10/2026: tấm chung (Thông tin đội / chữ dài), nút Menu khi là đội
            this.anNutLenh = true;
            int[] c = game.pokemon.ManCu.tam(g, this, this.title == null ? "" : game.pokemon.GiaoDien.tieuDe(this.title),
                    game.pokemon.ManCu.viec(this.right), this.type == 0 ? new Command[]{this.left} : null);
            game.pokemon.ManCu.tiepQuanMenu(this.title);
            int n = this.text.size();
            boolean coKh = this.type == 0 && this.status != null && this.status.length() > 0;
            String[] d = new String[n + (coKh ? 2 : 0)];
            int[] k = new int[d.length];
            for (int i = 0; i < n; i++) {
                d[i] = (String) this.text.elementAt(i);
            }
            if (coKh) {
                d[n] = "Khẩu hiệu";
                k[n] = 1;
                d[n + 1] = this.status;
            }
            game.pokemon.ManCu.vanBan(g, c, d, k);
            return;
        }
        Canvas.resetTrans(g);
        g.translate(Canvas.hw - (this.w / 2), Canvas.hh - (this.h / 2));
        Res.paintDlgFull(g, 0, 0, this.w, this.h);
        g.setClip(0, 0, this.w, this.h);
        Font.borderFont.drawString(g, game.pokemon.GiaoDien.tieuDe(this.title), (this.w >> 1) + 6, 6, 2); // pokemon: chữ trắng viền giữa dải đỏ
        g.setClip(0, 27, this.w, this.h - 30);
        g.translate(0, -cmy);
        if (this.type == 2) {
            paintText(g);
        } else {
            paintTextNormal(g);
        }
        super.paint(g);
    }

    private void paintTextNormal(Graphics g) {
        int y0 = 34;
        if (this.nationID > -1) {
            // 31/10/2026: ảnh hệ 11x33 (3 ô 11 px) - vùng 12x12 cũ ngoài ảnh: máy J2ME thật ném IllegalArgumentException
            javax.microedition.lcdui.Image im = Res.imgNation;
            int o = im == null ? 0 : Math.min(12, im.getWidth());
            int ho = im == null ? 0 : Math.min(12, im.getHeight() / 3);
            if (im != null && ho > 0 && (this.nationID + 1) * ho <= im.getHeight()) {
                g.drawRegion(im, 0, this.nationID * ho, o, ho, 0, 5, 34, 20);
            }
            y0 = 34 + 15;
        }
        for (int i = 0; i < this.text.size(); i++) {
            String str = (String) this.text.elementAt(i);
            Font.arialFontW.drawString(g, str, 5, y0, 0);
            y0 += 14;
        }
    }

    private void paintText(Graphics g) {
        int y0 = 34;
        if (this.idClan != -1) {
            GameData.paintIcon(g, (short) (this.idClan + 1000), this.w / 2, 34);
            y0 = 34 + 6;
        }
        if (this.type == 0 && !this.status.equals("")) {
            int ww = Font.arialFontW.getWidth(this.status);
            if (ww > this.w) {
                if (this.dir == 1) {
                    this.xa--;
                } else {
                    this.xa++;
                }
                if (Math.abs(this.xa) > ((ww / 2) - (this.w / 2)) + 10) {
                    this.dir *= -1;
                }
            }
            g.setClip(3, 0, this.w - 6, this.h);
            Font.borderFont.drawString(g, this.status, (this.w >> 1) + this.xa, y0, 2);
            y0 += 14;
        }
        for (int i = 0; i < this.text.size(); i++) {
            String str = (String) this.text.elementAt(i);
            Font.arialFontW.drawString(g, str, 5, y0, 0);
            y0 += 14;
        }
    }
}
