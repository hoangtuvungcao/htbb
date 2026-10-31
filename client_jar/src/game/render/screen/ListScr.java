package game.render.screen;

import game.model.Char;
import game.model.ChatInfo;
import game.model.ClanInfo;
import game.model.Command;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.ViewTextDlg;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class ListScr extends Screen {
    public static ListScr me;
    public static final byte FRIEND = 0;
    public static final byte CLAN = 1;
    public static final byte MSG_CLAN = 2;
    public static final byte STRONGER = 4;
    public static final byte RIGHER = 3;
    public static final byte TOP_CLAN = 5;
    public static final byte MANAGER_LIST = 6;
    public static final byte TOP = 7;
    public static final byte TOP_CUC_BO = 8;
    public Vector freindList;
    public Vector list;
    private int w;
    private int h;
    private int height;
    private int type;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int selected = 0;
    private String title;
    private Screen lastScr;
    private String[] nameManager;
    public byte page = 0;
    int pa = 0;
    boolean trans = false;
    int x0 = 0;
    int dir0 = 1;
    int a0 = 25;
    int x1 = 0;
    int dir1 = 1;
    int x2 = 0;
    int dir2 = 1;

    public static ListScr gI() {
        if (me != null) {
            return me;
        }
        ListScr listScr = new ListScr();
        me = listScr;
        return listScr;
    }

    @Override
    public void switchToMe() {
        if (Canvas.currentScreen == this) {
            return;
        }
        selected = 0;
        this.page = (byte) 0;
        cmtoY = 0;
        cmy = 0;
        if (this.lastScr == null) {
            this.lastScr = Canvas.gameScr;
        }
        super.switchToMe();
        init();
    }

    @Override
    public void init() {
        this.w = Canvas.w - 41;
        this.h = Canvas.h - 56;
        cmyLim = ((this.list == null ? 0 : this.list.size()) * this.height) - (this.h - 32);
        if (cmyLim < 0) {
            cmyLim = 0;
        }
    }

    public ListScr() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                ListScr.this.lastScr.switchToMe();
                if (ListScr.this.list != null) {
                    ListScr.this.list.removeAllElements();
                }
                ListScr.this.lastScr = null;
            }
        });
        this.center = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                if (ListScr.this.list != null && ListScr.selected >= 0 && ListScr.selected < ListScr.this.list.size()) {
                    ListScr.this.doSelected(ListScr.this.list.elementAt(ListScr.selected));
                }
            }
        });
        this.left = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                ListScr.this.doMenu();
            }
        });
    }

    protected void doMenu() {
        Vector menu = new Vector();
        if (this.type == 4 || this.type == 3 || this.type == 5 || this.type == 1) {
            if (this.type == 5) {
                menu.addElement(new Command("Danh sách quản lý đội.", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().requestQuanLyBang(((ClanInfo) ListScr.this.list.elementAt(ListScr.selected)).id);
                    }
                }));
            }
            menu.addElement(new Command("Xem thêm", new IAction() {
                @Override
                public void perform() {
                    Canvas.startWaitDlg();
                    if (ListScr.this.type == 1) {
                        GameService gameServiceGI = GameService.gI();
                        short s = ((Char) ListScr.this.list.elementAt(ListScr.selected)).idClan;
                        ListScr listScr = ListScr.this;
                        byte b = (byte) (listScr.page + 1);
                        listScr.page = b;
                        gameServiceGI.requestClanList(s, b);
                        return;
                    }
                    GameService gameServiceGI2 = GameService.gI();
                    int i = ListScr.this.type;
                    ListScr listScr2 = ListScr.this;
                    byte b2 = (byte) (listScr2.page + 1);
                    listScr2.page = b2;
                    gameServiceGI2.requestTopStronger_Righer(i, b2);
                }
            }));
        }
        if (this.type == 0) {
            menu.addElement(new Command("Xóa bạn", new AnonymousClass6()));
        }
        if (this.type == 1) {
            if (((Char) this.list.elementAt(0)).idClan == Canvas.gameScr.mainChar.idClan) {
                menu.addElement(new Command("Nhắn tin toàn đội", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.inputDlg.setInfo("Nội dung", new IAction() {
                            @Override
                            public void perform() {
                                String str = Canvas.inputDlg.tfInput.getText();
                                if (!str.equals("")) {
                                    Canvas.startOKDlg("Đã gửi tin");
                                    GameService.gI().msgClanAll(str, (byte) 0, 0);
                                }
                            }
                        }, 0, 100, false);
                        Canvas.inputDlg.show();
                    }
                }));
            }
            if (((Char) this.list.elementAt(selected)).idClan == Canvas.gameScr.mainChar.idClan && ((Canvas.gameScr.mainChar.isMaster == 0 || Canvas.gameScr.mainChar.isMaster == 1) && !((Char) this.list.elementAt(selected)).name.equals(Canvas.gameScr.mainChar.name))) {
                menu.addElement(new Command("Mời khỏi đội", new AnonymousClass10()));
            }
        } else if (this.type == 2 && Canvas.gameScr.mainChar.isMaster == 0) {
            menu.addElement(new Command("Xóa tin nhắn", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().msgClanAll(null, (byte) 2, ((ChatInfo) ListScr.this.list.elementAt(ListScr.selected)).ID);
                    ListScr.this.list.removeElementAt(ListScr.selected);
                    ListScr.selected--;
                    if (ListScr.selected < 0) {
                        ListScr.selected = 0;
                    }
                }
            }));
        }
        if (menu.size() > 0) {
            Canvas.menu.startAt(menu, 0);
        }
    }

    class AnonymousClass6 implements IAction {
        AnonymousClass6() {
        }

        @Override
        public void perform() {
            if (ListScr.this.freindList.size() == 0) {
                Canvas.startOKDlg("Danh sách trống");
            } else {
                Canvas.startYesNoDlg("Bạn có muốn xóa người này không ?", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().removeFriend(((Char) ListScr.this.list.elementAt(ListScr.selected)).name);
                        ListScr.this.list.removeElementAt(ListScr.selected);
                        ListScr.this.freindList.removeElementAt(ListScr.selected);
                        ListScr.selected--;
                        if (ListScr.selected < 0) {
                            ListScr.selected = 0;
                        }
                        ListScr.this.setPos();
                        ListScr.cmyLim = (ListScr.this.list.size() * ListScr.this.height) - (ListScr.this.h - 32);
                        if (ListScr.cmyLim < 0) {
                            ListScr.cmyLim = 0;
                        }
                        Canvas.startOKDlg("Đã xóa bạn");
                    }
                });
            }
        }
    }

    class AnonymousClass10 implements IAction {
        AnonymousClass10() {
        }

        @Override
        public void perform() {
            Canvas.startYesNoDlg(new StringBuffer("Bạn có chắc muốn mời ").append(((Char) ListScr.this.list.elementAt(ListScr.selected)).name).append(" khỏi đội không ?").toString(), new IAction() {
                @Override
                public void perform() {
                    GameService.gI().requestEvictionClan(((Char) ListScr.this.list.elementAt(ListScr.selected)).name);
                    ListScr.this.list.removeElementAt(ListScr.selected);
                    ListScr.selected--;
                    Canvas.endDlg();
                    ListScr.this.setPos();
                }
            });
        }
    }

    public void doSelected(final Object ch) {
        if (this.type == 2) {
            ViewTextDlg.gI().setInfo(((ChatInfo) ch).content, "TIN NHẮN");
        } else if (this.type == 5) {
            GameService.gI().requestClanInfo(((ClanInfo) ch).id);
        } else {
            Canvas.inputDlg.setInfo("Nội dung tin nhắn", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().sendMsgPrivate(((Char) ch).name, Canvas.inputDlg.tfInput.getText());
                    Canvas.endDlg();
                }
            }, 0, 100, false);
            Canvas.inputDlg.show();
        }
    }

    public void setInfo(Vector list, int type, String title) {
        selected = 0;
        if (list.size() == 0) {
            this.page = (byte) -1;
            if (this.list == null) {
                this.list = list; // danh sách rỗng lần đầu: tránh NullPointer ở init/updateKey/paint
            }
        } else {
            this.list = list;
        }
        this.type = type;
        if (type == 2) {
            this.height = 30;
        } else {
            this.height = 40;
        }
        this.title = title;
        setPos();
        if (this.list != null) {
            cmyLim = (this.list.size() * this.height) - (this.h - 32);
        }
        if (cmyLim < 0) {
            cmyLim = 0;
        }
        if (type == 6 && this.nameManager == null) {
            this.nameManager = new String[]{"Đội trưởng", "Đội phó", "Cố vấn"};
        }
    }

    public void setPos() {
        if (this.type != 2 && this.type != 5) {
            for (int i = 0; i < this.list.size(); i++) {
                Char ch = (Char) this.list.elementAt(i);
                ch.x = (short) 25;
                ch.y = (short) ((((i * this.height) + (this.height / 2)) + (ch.height / 2)) - 5);
            }
        }
    }

    @Override
    public void update() {
        if (Math.abs(cmtoY - cmy) < 15 && cmy < 0) {
            cmtoY = 0;
        }
        if (Math.abs(cmtoY - cmy) < 10 && cmy > cmyLim) {
            cmtoY = cmyLim;
        }
    }

    @Override
    public void updateKey() {
        if (game.pokemon.ManCu.bat() && (Canvas.isPointerClick || Canvas.isPointerDown)) {
            game.pokemon.ManCu.phimKhongCham(new Runnable() {
                public void run() {
                    ListScr.this.updateKey();
                }
            });
            return;
        }
        if (this.list == null || this.list.size() == 0) {
            selected = 0;
            super.updateKey(); // chỉ phím mềm (Đóng)
            return;
        }
        boolean changeFocus = false;
        if (Canvas.isPointer(20, 46, this.w, this.h - 24)) {
            if (Canvas.isPointerDown) {
                if (!this.trans) {
                    this.pa = cmy;
                    this.trans = true;
                }
                cmtoY = this.pa + (Canvas.pyLast - Canvas.py);
                if (cmtoY < -30) {
                    cmtoY = -30;
                }
                if (cmtoY > cmyLim + 30) {
                    cmtoY = cmyLim + 30;
                }
            }
            if (Canvas.isPointerClick) {
                Canvas.isPointerClick = false;
                this.trans = false;
                int dy = Canvas.pyLast - Canvas.py;
                if (Math.abs(dy) < 10) {
                    int aa = ((cmtoY + Canvas.py) - 46) / this.height;
                    if (aa == selected) {
                        if (this.center != null) {
                            this.center.action.perform();
                        } else if (this.left != null) {
                            this.left.action.perform();
                        }
                    }
                    selected = aa;
                    if (selected < 0) {
                        selected = 0;
                    }
                    if (selected >= this.list.size()) {
                        selected = this.list.size() - 1;
                    }
                    this.x0 = 0;
                    changeFocus = true;
                }
            }
        }
        if (Canvas.isKeyPressed(2)) {
            selected--;
            if (selected < 0) {
                selected = this.list.size() - 1;
            }
            this.x0 = 0;
            changeFocus = true;
        } else if (Canvas.isKeyPressed(8)) {
            selected++;
            if (selected >= this.list.size()) {
                selected = 0;
            }
            changeFocus = true;
            this.x0 = 0;
        }
        if (changeFocus) {
            cmtoY = (selected * this.height) - ((this.h - 31) / 2);
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

    /** 31/10/2026: máy có con trỏ - tấm chung, hàng chạm được (chạm = Chọn), nút Menu khi có việc. */
    void veCham(Graphics g) {
        this.anNutLenh = true;
        boolean coMenu = this.type == 0 || this.type == 1 || this.type == 3 || this.type == 4 || this.type == 5
                || this.type == 2 && Canvas.gameScr.mainChar.isMaster == 0;
        int[] c = game.pokemon.ManCu.tam(g, this, this.title == null ? "" : this.title, game.pokemon.ManCu.viec(this.right),
                coMenu && this.list != null && this.list.size() > 0 ? new Command[]{this.left} : null);
        game.pokemon.ManCu.tiepQuanMenu(this.title);
        final Vector l = this.list;
        game.pokemon.ManCu.danhSach(g, c, l == null ? 0 : l.size(), selected, new game.pokemon.ManCu.Dong() {
            public String chinh(int i) {
                Object o = l.elementAt(i);
                if (o instanceof ClanInfo) {
                    ClanInfo k = (ClanInfo) o;
                    return k.name + " - Cấp " + (k.level > -1 ? String.valueOf((int) k.level) : "?");
                }
                if (o instanceof ChatInfo) {
                    return ((ChatInfo) o).name;
                }
                Char ac = (Char) o;
                String chuc = ListScr.this.type == 6 && ac.isMaster < 3 && ListScr.this.nameManager != null ? " - " + ListScr.this.nameManager[ac.isMaster] : "";
                return ac.name + chuc;
            }

            public String phu(int i) {
                Object o = l.elementAt(i);
                if (o instanceof ClanInfo) {
                    ClanInfo k = (ClanInfo) o;
                    return "Đội trưởng " + k.master + " - " + (int) k.members + " thành viên - quỹ " + game.pokemon.TienTe.gon(k.money) + " xu";
                }
                if (o instanceof ChatInfo) {
                    return ((ChatInfo) o).content;
                }
                Char ac = (Char) o;
                return "Cấp " + (ac.level > -1 ? String.valueOf((int) ac.level) : "?") + " - " + game.pokemon.TienTe.chuGia(ac.charXu, ac.luong)
                        + (ListScr.this.type == 7 ? " - CT " + ac.congTrang + " - LT " + ac.lienTram : "");
            }

            public void cham(int i) {
                ListScr.selected = i;
                if (ListScr.this.center != null && ListScr.this.center.action != null) {
                    ListScr.this.center.action.perform();
                }
            }
        }, "Chưa có ai ở đây");
    }

    public void paint(Graphics g) {
        if (this.lastScr != null) {
            this.lastScr.paint(g);
        }
        if (game.pokemon.ManCu.bat()) {
            veCham(g);
            return;
        }
        Canvas.resetTrans(g);
        Res.paintDlgFull(g, 20, 20, this.w, this.h);
        // pokemon: tiêu đề chữ trắng viền (borderFont) giữa dải đỏ, chạy chữ khi dài; chừa Poké Ball bên trái
        int xTieuDe = 38;
        int wTieuDe = this.w + 20 - 4 - xTieuDe;
        g.setClip(xTieuDe, 20, wTieuDe, 22);
        String tieuDe = this.title == null ? "" : game.pokemon.GiaoDien.tieuDe(this.title);
        int www = Font.borderFont.getWidth(tieuDe);
        if (www > wTieuDe) {
            if (this.a0 > xTieuDe + 4 || this.a0 < xTieuDe + wTieuDe - www - 4) {
                this.a0 = xTieuDe + 2;
                this.dir0 = 1;
            }
            if (this.dir0 == 1) {
                this.a0--;
                if (this.a0 <= xTieuDe + wTieuDe - www - 4) {
                    this.dir0 = -1;
                }
            } else {
                this.a0++;
                if (this.a0 >= xTieuDe + 2) {
                    this.dir0 = 1;
                }
            }
            Font.borderFont.drawString(g, tieuDe, this.a0, 25, 0);
        } else {
            Font.borderFont.drawString(g, tieuDe, xTieuDe + wTieuDe / 2, 25, Font.CENTER);
        }
        Canvas.resetTrans(g);
        g.translate(20, 47);
        g.setClip(4, 0, this.w - 8, this.h - 31);
        g.translate(0, -cmy);
        if (this.list == null || this.list.size() == 0) {
            // pokemon: danh sách rỗng -> dòng báo thay ô chọn trống
            game.pokemon.GiaoDien.veTrong(g, this.w / 2, cmy + (this.h - 31) / 2 - 6, this.w - 20, "Chưa có ai ở đây", "Hãy quay lại sau nhé - bảng sẽ cập nhật khi có người tham gia.");
            super.paint(g);
            return;
        }
        Res.paintSelected(g, 4, selected * this.height, this.w - 9, this.height);
        if (this.type == 5) {
            paintTopClan(g);
        } else if (this.type == 2) {
            paintMsgClan(g);
        } else {
            paintChar(g);
        }
        super.paint(g);
    }

    private void paintTopClan(Graphics g) {
        int ww;
        int aa = cmy / this.height;
        int bb = aa + (this.h / this.height) + 1;
        if (bb >= this.list.size()) {
            bb = this.list.size();
        }
        int i = aa;
        while (i < bb) {
            ClanInfo c = (ClanInfo) this.list.elementAt(i);
            int zz = 0;
            if (c.id != -1 && GameData.getImgIcon(c.id) != null) {
                zz = 12;
                GameData.paintIcon(g, (short) (c.id + 1000), 16, (i * this.height) + 12);
            }
            String temp = new StringBuffer(String.valueOf(c.name)).append(" - Cấp độ: ").append(c.level > -1 ? new StringBuffer(String.valueOf((int) c.level)).toString() : "?").toString();
            Font.normalFont[0].drawString(g, game.pokemon.GiaoDien.catChu(Font.normalFont[0], temp, this.w - 22 - zz), 11 + zz, (i * this.height) + 4, 0);
            int wchar = Font.normalFont[0].getWidth(temp);
            if (c.nationID > -1) {
                g.drawRegion(Res.imgNation, 0, c.nationID * 11, 11, 11, 0, 11 + zz + 17 + wchar, (i * this.height) + 4, 0);
            }
            String str = new StringBuffer("Đội trưởng: ").append(c.master).append(" - Tiền: ").append(c.money).append("xu - Thành viên: ").append((int) c.members).toString();
            if (i == selected && (ww = Font.arialFontW.getWidth(str)) > this.w - 10 && selected == i) {
                if (this.dir1 == 1) {
                    this.x1--;
                    if (this.x1 < (this.w - 20) - ww) {
                        this.dir1 = -1;
                    }
                } else {
                    this.x1++;
                    if (this.x1 > 5) {
                        this.dir1 = 1;
                    }
                }
            }
            // pokemon: cắt ".." thay chữ chạy (không tràn / bị xén)
            Font.arialFontW.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFontW, str, this.w - 22), 11, (i * this.height) + 20, 0);
            i++;
        }
    }

    private void paintMsgClan(Graphics g) {
        String string;
        int aa = cmy / this.height;
        int bb = aa + (this.h / this.height) + 1;
        if (bb >= this.list.size()) {
            bb = this.list.size();
        }
        for (int i = bb - 1; i >= aa; i--) {
            ChatInfo c = (ChatInfo) this.list.elementAt(i);
            Font.borderFont.drawString(g, c.name, 7, i * this.height, 0);
            if (Font.arialFontW.getWidth(c.content) > this.w - 10) {
                string = new StringBuffer(String.valueOf(Font.arialFont.splitFontBStrInLine(c.content, this.w - 10)[0])).append("...").toString();
            } else {
                string = c.content;
            }
            String str = game.pokemon.GiaoDien.catChu(Font.arialFontW, string, this.w - 14);   // 31/10/2026: đo đúng font vẽ
            Font.arialFontW.drawString(g, str, 7, (i * this.height) + 15, 0);
        }
    }

    private void paintChar(Graphics g) {
        int aa = cmy / this.height;
        int bb = aa + (this.h / this.height) + 1;
        if (bb >= this.list.size()) {
            bb = this.list.size();
        }
        int i = aa;
        while (i < bb) {
            Char ac = (Char) this.list.elementAt(i);
            // pokemon: chân dung Pokémon trong vòng tròn thay hình nhân vật kiếm hiệp (paintCharList)
            int dex = ac.pokeDex > 0 ? ac.pokeDex : game.pokemon.LoaiPokemonClient.duDoan(ac.clazz, ac.level);
            int yTam = i * this.height + this.height / 2;
            if (dex <= 0 || !game.pokemon.PokeBody.paintChanDung(g, dex, ac.pokeShiny, 25, yTam, 32)) {
                game.pokemon.GiaoDien.pokeBall(g, 25, yTam, 9, 0);
            }
            String tenLoai = dex > 0 ? game.pokemon.LoaiPokemonClient.ten(dex) : "";
            String str = new StringBuffer("Cấp độ: ").append(ac.level > -1 ? new StringBuffer(String.valueOf((int) ac.level)).toString() : "?").append(tenLoai.length() > 0 ? " - " : "").append(tenLoai).toString();
            int zz = 0;
            if (ac.idClan != -1 && GameData.getImgIcon(ac.idClan) != null) {
                zz = 8;
                GameData.paintIcon(g, (short) (ac.idClan + 1000), 45, (i * this.height) + 12);
            }
            String money = game.pokemon.TienTe.chuGia(ac.charXu, ac.luong);
            int ww0 = Font.normalFont[0].getWidth(new StringBuffer(String.valueOf(ac.name)).append(money).append((this.type != 6 || ac.isMaster >= 3) ? "" : new StringBuffer(" - ").append(this.nameManager[ac.isMaster]).toString()).append(this.type == 7 ? new StringBuffer(" - CT: ").append(ac.congTrang).append(" - LT: ").append(ac.lienTram).toString() : "").toString()) + 30;
            if (ww0 > this.w - 55 && selected == i) {
                if (this.dir2 == 1) {
                    this.x2--;
                    if (this.x2 < (this.w - 65) - ww0) {
                        this.dir2 = -1;
                    }
                } else {
                    this.x2++;
                    if (this.x2 > 5) {
                        this.dir2 = 1;
                    }
                }
            }
            g.setClip(45, cmy, this.w - 49, this.h - 31);
            String dong1 = new StringBuffer(String.valueOf(ac.name)).append(" - ").append(money).append((this.type != 6 || ac.isMaster >= 3) ? "" : new StringBuffer(" - ").append(this.nameManager[ac.isMaster]).toString()).append(this.type == 7 ? new StringBuffer(" - CT: ").append(ac.congTrang).append(" - LT: ").append(ac.lienTram).toString() : "").toString();
            Font.normalFont[0].drawString(g, game.pokemon.GiaoDien.catChu(Font.normalFont[0], dong1, this.w - 52 - zz), 45 + zz, (i * this.height) + 5, 0);
            int wchar = Font.normalFont[0].getWidth(new StringBuffer(String.valueOf(ac.name)).append(" - ").append(money).append((this.type != 6 || ac.isMaster >= 3) ? "" : new StringBuffer(" - ").append(this.nameManager[ac.isMaster]).toString()).append(this.type == 7 ? new StringBuffer(" - CT: ").append(ac.congTrang).append(" - LT: ").append(ac.lienTram).toString() : "").toString());
            if (ac.nationID > -1) {
                g.drawRegion(Res.imgNation, 0, ac.nationID * 11, 11, 11, 0, 45 + zz + (i == selected ? this.x2 : 0) + wchar + 5, (i * this.height) + 5, 0);
            }
            int ww = Font.arialFontW.getWidth(str);
            if (ww > this.w - 45 && selected == i) {
                if (this.dir0 == 1) {
                    this.x0--;
                    if (this.x0 < (this.w - 55) - ww) {
                        this.dir0 = -1;
                    }
                } else {
                    this.x0++;
                    if (this.x0 > 5) {
                        this.dir0 = 1;
                    }
                }
            }
            Font.arialFontW.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFontW, str, this.w - 52), 45, (i * this.height) + 20, 0);
            g.setClip(0, cmy, this.w - 4, this.h - 31);
            i++;
        }
    }
}
