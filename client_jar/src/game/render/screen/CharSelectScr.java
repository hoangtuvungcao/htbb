package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.pokemon.LoaiPokemonClient;
import game.pokemon.PokeBody;
import game.pokemon.PokeDuLieu;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import network.Session_ME;

public class CharSelectScr extends Screen {
    public static CharSelectScr me;
    public MainChar[] charInfos;
    MainChar[] chars;
    public int selected = 0;
    public Command cmdSelect;
    public Command cmdNew;
    public Command cmdDel;
    public Command cmdClose;
    public static Image imgBg;

    public static CharSelectScr gI() {
        if (me != null) {
            return me;
        }
        CharSelectScr charSelectScr = new CharSelectScr();
        me = charSelectScr;
        return charSelectScr;
    }

    public void setCharList(MainChar[] charInfos) {
        if (!Session_ME.gI().connected) {
            return;
        }
        Canvas.loginScr.savePass();
        if (!Canvas.gameScr.isNewVersionAvailable) {
            Canvas.endDlg();
        }
        this.charInfos = charInfos;
        this.chars = new MainChar[charInfos.length];
        this.chars = charInfos;
        this.cmdSelect = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                game.pokemon.ManTai.batDau(game.pokemon.ManTai.NHAN_VAT, null);
                game.pokemon.TuNoiLai.daChon(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB);
                GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 1);
                Canvas.gameScr.mainChar.imgWpa = CharSelectScr.this.chars[CharSelectScr.this.selected].imgWpa;
                Canvas.gameScr.mainChar.dxWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dxWear;
                Canvas.gameScr.mainChar.dyWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dyWear;
                ChangScr.gI().switchToMe();
                CharSelectScr.this.chars = null;
                CharSelectScr.me = null;
            }
        });
        this.cmdNew = new Command("Tạo mới", new IAction() {
            @Override
            public void perform() {
                CreateCharScr.gI().switchToMe();
            }
        });
        this.cmdDel = new Command("Xóa", new AnonymousClass3());
        this.cmdClose = new Command("Thoát", new IAction() {
            @Override
            public void perform() {
                Session_ME.gI().close();
                Canvas.loginScr.switchToMe();
            }
        });
        this.right = this.cmdClose;
        chonThe(this.selected < 0 || this.selected > 2 ? 0 : this.selected);
    }

    /** 0.9.4 (TuNoiLai, luồng game): vào lại đúng Pokémon idDb; false = không có trong danh sách. */
    public boolean tuChonLai(int idDb) {
        if (this.charInfos == null || this.chars == null || this.cmdSelect == null) {
            return false;
        }
        for (int i = 0; i < this.charInfos.length; i++) {
            if (this.charInfos[i] != null && this.charInfos[i].IDDB == idDb) {
                this.selected = i;
                chonThe(i);
                this.cmdSelect.action.perform();
                return true;
            }
        }
        return false;
    }

    class AnonymousClass3 implements IAction {
        AnonymousClass3() {
        }

        @Override
        public void perform() {
            if (CharSelectScr.this.cmdDel.caption.equals("Xóa")) {
                Canvas.startYesNoDlg("Bạn thật sự muốn xóa Pokémon này?", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg("Xin chờ..", true);
                        GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 2);
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            } else if (CharSelectScr.this.cmdDel.caption.equals("Khôi phục")) {
                Canvas.startYesNoDlg("Bạn muốn khôi phục Pokémon này?", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg("Xin chờ..", true);
                        GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 3);
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            }
        }
    }

    /**
     * pokemon: du doan Pokemon cua nhan vat o man chon (lenh 13 KHONG co class va -90 chi gui sau
     * khi vao game): suy class tu loai vu khi dang mac, weaponType 3..7 = class 0..4. Khong mac vu
     * khi thi -1 (ve paper doll cu). Eevee da chon nhanh van hien Eevee.
     */
    public static int duDoanDex(MainChar c, int lastLv) {
        if (c == null || c.weaponType < 3 || c.weaponType > 7) {
            return -1;
        }
        int cap = lastLv <= 0 ? (c.lastLv <= 0 ? 1 : c.lastLv) : lastLv;
        return LoaiPokemonClient.duDoan(c.weaponType - 3, cap);
    }

    /** pokemon: bề ngang một thẻ Pokédex (3 thẻ vừa màn). */
    private static int rongThe() {
        int w = (Canvas.w - 20) / 3 - 4;
        return w > 72 ? 72 : w;
    }

    private static int xThe(int i) {
        int cw = rongThe();
        return Canvas.hw - (3 * cw + 8) / 2 + i * (cw + 4);
    }

    private static int yThe() {
        return Canvas.hh - 62;
    }

    private static final int CAO_THE = 104;

    @Override
    public void paint(Graphics g) {
        if (game.pokemon.TuNoiLai.dangNoi) {
            // 0.9.5: đang tự kết nối lại - giữ màn tải (không lộ màn chọn Pokémon trước khi tự chọn lại)
            game.pokemon.ManTai.ve(g, Math.max(game.pokemon.ManTai.buoc, game.pokemon.ManTai.DANG_NHAP));
            return;
        }
        if (game.pokemon.KhungVao.moi()) {
            veMoi(g);
            return;
        }
        // pokemon: banner; Pokémon khởi đầu cùng dòng với nhân vật đang chọn được chiếu sáng
        int chonDong = -1;
        try {
            // lệnh 13 không gửi class: suy từ loại vũ khí như duDoanDex (weaponType 3..7 = class 0..4)
            if (this.chars != null && this.selected >= 0 && this.selected < this.chars.length && this.chars[this.selected] != null) {
                int vk = this.chars[this.selected].weaponType;
                chonDong = vk >= 3 && vk <= 7 ? vk - 3 : -1;
            }
        } catch (Throwable e) {
        }
        ServerListScr.gI().paintBg(g, chonDong, true);
        Canvas.resetTrans(g);
        // pokemon: 3 thẻ kiểu Pokédex - dải đỏ số thứ tự, Pokémon trong vòng sáng, tên, Lv, huy hiệu hệ
        int cw = rongThe();
        int xKhung = xThe(0) - 8;
        int wKhung = 3 * cw + 8 + 16;
        game.pokemon.GiaoDien.bangMo(g, xKhung, yThe() - 30, wKhung, CAO_THE + 76);
        game.pokemon.GiaoDien.dai(g, xKhung + 4, yThe() - 26, wKhung - 8, 18, "Chọn Pokémon");
        for (int i = 0; i < 3; i++) {
            int x = xThe(i);
            int y = yThe();
            boolean chon = i == this.selected;
            boolean co = this.charInfos != null && i < this.charInfos.length && this.chars != null && i < this.chars.length;
            if (chon) {
                game.pokemon.GiaoDien.khoi(g, x - 2, y - 2, cw + 4, CAO_THE + 4, 7, game.pokemon.GiaoDien.VANG);
            }
            game.pokemon.GiaoDien.khoi(g, x, y, cw, CAO_THE, 5, game.pokemon.GiaoDien.VIEN);
            final int the = i;
            game.pokemon.VungCham.them(g, x, y, cw, CAO_THE, new IAction() {
                public void perform() {
                    CharSelectScr.this.chamThe(the);
                }
            }, "the-" + i);
            if (!co) {
                game.pokemon.GiaoDien.khoi(g, x + 2, y + 2, cw - 4, CAO_THE - 4, 4, chon ? 0xDCE8F6 : 0xB9C6D8);
                int cx = x + cw / 2;
                int cy = y + 42;
                g.setColor(chon ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.XAM_TOI);
                g.fillRect(cx - 9, cy - 2, 18, 5);
                g.fillRect(cx - 2, cy - 9, 5, 18);
                Font.blackFont.drawString(g, "Tạo mới", cx, y + CAO_THE - 26, 2);
                continue;
            }
            MainChar c = this.chars[i];
            int lv = this.charInfos[i].lastLv > 0 ? this.charInfos[i].lastLv : 1;
            game.pokemon.GiaoDien.khoiDoc(g, x + 2, y + 2, cw - 4, CAO_THE - 4, 4, game.pokemon.GiaoDien.TRANG, 0xE4ECF7);
            game.pokemon.GiaoDien.khoiDoc(g, x + 2, y + 2, cw - 4, 14, 3, game.pokemon.GiaoDien.DO_SANG, game.pokemon.GiaoDien.DO_TOI);
            Font.smallFontColor[0].drawString(g, new StringBuffer("no").append(i + 1).toString(), x + 5, y + 5, 0);
            if (this.charInfos[i].using != 1) {
                Font.smallFontColor[1].drawString(g, "khoa", x + cw - 4, y + 5, 1);
            }
            // vòng sáng sau Pokémon
            int cx = x + cw / 2;
            g.setColor(chon ? 0xBFE0FF : game.pokemon.GiaoDien.XANH_NHAT);
            g.fillArc(cx - 22, y + 46, 44, 14, 0, 360);
            c.x = (short) cx;
            c.y = (short) (y + 54);
            int dexDoan = duDoanDex(c, lv);
            g.setClip(x + 2, y + 16, cw - 4, 44);
            if (dexDoan <= 0 || !PokeBody.paint(g, dexDoan, false, c.x, c.y, 0, PokeDuLieu.DUNG, Canvas.gameTick, 150)) {
                c.paintAvatar(g, c.x, c.y);
            }
            Canvas.resetTrans(g);
            String ten = this.charInfos[i].name == null ? "" : this.charInfos[i].name;
            while (ten.length() > 2 && Font.blackFont.getWidth(ten) > cw - 6) {
                ten = ten.substring(0, ten.length() - 1);
            }
            Font.blackFont.drawString(g, ten, cx, y + 61, 2);
            Font.arialFont.drawString(g, new StringBuffer("Cấp ").append(lv).toString(), cx, y + 74, 2);
            if (c.weaponType >= 3 && c.weaponType <= 7) {
                String he = LoaiPokemonClient.HE_KHOI_DAU[c.weaponType - 3];
                String chu = he.startsWith("Hệ ") ? he.substring(3) : he;
                int wb = Font.borderFont.getWidth(chu) + 10;
                if (wb > cw - 6) {
                    wb = cw - 6;
                }
                game.pokemon.GiaoDien.vien(g, cx - wb / 2, y + CAO_THE - 17, wb, 14, game.pokemon.GiaoDien.mauHe(he), chu);
            }
        }
        int yDuoi = yThe() + CAO_THE + 8;
        if (this.charInfos != null && this.selected < this.charInfos.length && this.chars != null && this.selected < this.chars.length) {
            MainChar ci = this.charInfos[this.selected];
            String dong = new StringBuffer(String.valueOf(ci.name)).append(" - Cấp ").append((int) (ci.lastLv > 0 ? ci.lastLv : (short) 1)).toString();
            int dex = duDoanDex(this.chars[this.selected], ci.lastLv);
            if (dex > 0) {
                dong = new StringBuffer(dong).append(" - ").append(LoaiPokemonClient.ten(dex)).toString();
            }
            // pokemon: cắt ".." cho vừa khung (chừa chỗ cờ vùng bên trái)
            dong = game.pokemon.GiaoDien.catChu(Font.borderFont, dong, Canvas.w - 46);
            Font.borderFont.drawString(g, dong, Canvas.hw, yDuoi, 2);
            if (ci.using == 0) {
                Font.borderFont.drawString(g, new StringBuffer("Còn lại: ").append((int) ci.day).append(" ngày").toString(), Canvas.hw, yDuoi + 14, 2);
            }
            if (ci.nationID != -1) {
                int ww = Font.borderFont.getWidth(dong);
                g.drawRegion(Res.imgNation, 0, ci.nationID * 11, 11, 11, 0, (Canvas.hw - (ww / 2)) - 15, yDuoi + 2, 20);
            }
        } else {
            Font.borderFont.drawString(g, game.pokemon.GiaoDien.catChu(Font.borderFont, "Ô trống: bấm để tạo Pokémon", Canvas.w - 24), Canvas.hw, yDuoi, 2);
        }
        super.paint(g);
    }

    private IAction viecVao, viecTao, viecXoa, viecThoat;

    private boolean coO(int i) {
        return this.charInfos != null && i >= 0 && i < this.charInfos.length && this.chars != null && i < this.chars.length;
    }

    /**
     * 0.9.5 PC / APK: Pokémon đang chọn đứng TO trên đường banner (tên + cấp dưới chân), hàng 3 thẻ gọn trong bảng sáng,
     * nút thật Vào game / Tạo mới / Xoá (X trên đầu bảng = về đăng nhập). Không vẽ chip phím mềm.
     */
    private void veMoi(Graphics g) {
        this.anNutLenh = true;
        if (this.viecVao == null) {
            this.viecVao = new IAction() {
                public void perform() {
                    if (coO(CharSelectScr.this.selected)) {
                        CharSelectScr.this.cmdSelect.action.perform();
                    }
                }
            };
            this.viecTao = new IAction() {
                public void perform() {
                    CharSelectScr.this.cmdNew.action.perform();
                }
            };
            this.viecXoa = new IAction() {
                public void perform() {
                    if (coO(CharSelectScr.this.selected)) {
                        CharSelectScr.this.cmdDel.action.perform();
                    }
                }
            };
            this.viecThoat = new IAction() {
                public void perform() {
                    CharSelectScr.this.cmdClose.action.perform();
                }
            };
        }
        ServerListScr.gI().paintBg(g, -1, false);
        Canvas.resetTrans(g);
        game.pokemon.KhungVao.logoDuoi = 0;   // màn này không vẽ logo: bảng lên trên, Pokémon đứng giữa đường
        game.pokemon.KhungVao.logoTrai = Canvas.hw;
        game.pokemon.KhungVao.logoPhai = Canvas.hw;
        int sel = this.selected;
        boolean co = coO(sel);
        // Pokémon đang chọn: to, giữa đường
        int[] p = game.pokemon.Banner.viTriKhoiDau(2);
        int t = p[2] * 3 / 2;
        if (co) {
            MainChar c = this.chars[sel];
            MainChar ci = this.charInfos[sel];
            int lv = ci.lastLv > 0 ? ci.lastLv : 1;
            int dex = duDoanDex(c, lv);
            g.setColor(0x16301F);
            g.fillArc(p[0] - 16 * t / 100, p[1] - 4 * t / 100, 32 * t / 100, 9 * t / 100, 0, 360);
            int nhun = (Canvas.gameTick % 24) < 12 ? 0 : -1;
            if (dex <= 0 || !PokeBody.paint(g, dex, false, p[0], p[1] + nhun, 0, PokeDuLieu.DUNG, Canvas.gameTick, t)) {
                c.x = (short) p[0];
                c.y = (short) p[1];
                c.paintAvatar(g, c.x, c.y);
            }
            Canvas.resetTrans(g);
            String ten = ci.name == null ? "" : ci.name;
            String dong = ten + " · Cấp " + lv;
            if (dex > 0) {
                dong = dong + " · " + LoaiPokemonClient.ten(dex);
            }
            if (ci.using == 0) {
                dong = dong + " · còn " + (int) ci.day + " ngày";
            }
            int ht = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN) + 6;
            int wt = Math.min(Canvas.w - 20, game.pokemon.ChuDe.rong(dong, game.pokemon.ChuDe.NHAN) + 20);
            int yt = Math.min(Canvas.h - Screen.deltaY - ht - 4, p[1] + 6);
            game.pokemon.ChuDe.the(g, p[0] - wt / 2, yt, wt, ht, ht / 2);
            game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(dong, game.pokemon.ChuDe.NHAN, wt - 16), p[0], yt, ht, game.pokemon.ChuDe.NHAN,
                    game.pokemon.ChuDe.CHU, 2);
        }
        // bảng: hàng 3 thẻ + nút
        int hb = game.pokemon.KhungVao.caoNut(), k = game.pokemon.KhungVao.khe();
        int lh = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN), lp = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU);
        int hChip = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN_NHO) + 2;
        int hThe = 44 + lh + lp + hChip + 12;
        int dau = game.pokemon.ChuDe.caoDau() + 2 * game.pokemon.ChuDe.DEM;
        int hc = dau + hThe + k + hb;
        int[] r = game.pokemon.KhungVao.dat(360, hc, hc, 220);
        int[] n = game.pokemon.Bang.khung(g, r[0], r[1], r[2], r[3], "Chọn Pokémon", null, 0, null, this.viecThoat);
        int x0 = n[0], w = n[2], y = n[1];
        int wThe = (w - 2 * k) / 3;
        boolean conTrong = false;
        for (int i = 0; i < 3; i++) {
            int x = x0 + i * (wThe + k);
            boolean dang = i == sel;
            boolean coI = coO(i);
            if (!coI) {
                conTrong = true;
            }
            final int the = i;
            game.pokemon.Bang.hang(g, x, y, wThe, hThe, 0, dang, new IAction() {
                public void perform() {
                    CharSelectScr.this.chamThe(the);
                }
            }, "the-" + i);
            int cx = x + wThe / 2;
            if (!coI) {
                game.pokemon.GiaoDien.khoi(g, cx - 14, y + 10, 28, 28, 14, game.pokemon.ChuDe.NEN_PHU_TOI);
                game.pokemon.ChuDe.chuGiua(g, "+", cx, y + 10, 28, game.pokemon.ChuDe.TIEU_DE, game.pokemon.ChuDe.CHU_PHU, 2);
                game.pokemon.ChuDe.chuGiua(g, "Ô trống", cx, y + 44, lh, game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU_PHU, 2);
                game.pokemon.ChuDe.chuGiua(g, "Tạo Pokémon mới", cx, y + 44 + lh, lp, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 2);
                continue;
            }
            MainChar c = this.chars[i];
            MainChar ci = this.charInfos[i];
            int lv = ci.lastLv > 0 ? ci.lastLv : 1;
            int dex = duDoanDex(c, lv);
            g.setClip(x + 2, y + 2, wThe - 4, 42);
            g.setColor(dang ? 0xCFE3FA : 0xE4ECF7);
            g.fillArc(cx - 18, y + 34, 36, 9, 0, 360);
            if (dex <= 0 || !PokeBody.paint(g, dex, false, cx, y + 40, 0, PokeDuLieu.DUNG, Canvas.gameTick, 100)) {
                c.x = (short) cx;
                c.y = (short) (y + 40);
                c.paintAvatar(g, c.x, c.y);
            }
            Canvas.resetTrans(g);
            g.setClip(0, 0, Canvas.w, Canvas.h);
            int yy = y + 44;
            game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(ci.name == null ? "" : ci.name, game.pokemon.ChuDe.NHAN, wThe - 8), cx, yy, lh,
                    game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 2);
            yy += lh;
            game.pokemon.ChuDe.chuGiua(g, ci.using != 1 ? "Cấp " + lv + " · khoá" : "Cấp " + lv, cx, yy, lp, game.pokemon.ChuDe.PHU,
                    ci.using != 1 ? game.pokemon.ChuDe.DO : game.pokemon.ChuDe.CHU_PHU, 2);
            yy += lp + 2;
            if (c.weaponType >= 3 && c.weaponType <= 7) {
                String he = LoaiPokemonClient.HE_KHOI_DAU[c.weaponType - 3];
                String chu = he.startsWith("Hệ ") ? he.substring(3) : he;
                int wb = Math.min(wThe - 8, game.pokemon.ChuDe.rong(chu, game.pokemon.ChuDe.NHAN_NHO) + 12);
                game.pokemon.GiaoDien.khoi(g, cx - wb / 2, yy, wb, hChip, hChip / 2, game.pokemon.GiaoDien.mauHe(he));
                game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(chu, game.pokemon.ChuDe.NHAN_NHO, wb - 6), cx, yy, hChip, game.pokemon.ChuDe.NHAN_NHO,
                        0xFFFFFF, 2);
            }
        }
        y += hThe + k;
        String xoa = co && this.charInfos[sel].using != 1 ? "Khôi phục" : "Xoá";
        game.pokemon.KhungVao.hangNut(g, x0, y, w, new String[]{xoa, "Tạo mới", "Vào game"},
                new int[]{co ? game.pokemon.Bang.TRUNG : game.pokemon.Bang.TAT, conTrong ? game.pokemon.Bang.TRUNG : game.pokemon.Bang.TAT,
                    co ? game.pokemon.Bang.CHINH : game.pokemon.Bang.TAT},
                new Object[]{this.viecXoa, this.viecTao, this.viecVao}, "chon");
    }

    /**
     * Chọn thẻ i (phím trái / phải hoặc chạm): đặt lệnh theo thẻ MỚI (Chọn + Xóa/Khôi phục nếu có Pokémon, Tạo mới nếu
     * ô trống). Bản cũ đặt lệnh theo thẻ cũ rồi mới đổi -> chạm ô trống vẫn giữ "Chọn" (chạm lần nữa chọn ô không có).
     */
    void chonThe(int i) {
        if (i < 0 || i > 2) {
            return;
        }
        this.selected = i;
        boolean co = this.chars != null && i < this.chars.length && this.charInfos != null && i < this.charInfos.length;
        if (co) {
            this.center = this.cmdSelect;
            this.left = this.cmdDel;
            this.cmdDel.caption = this.charInfos[i].using == 1 ? "Xóa" : "Khôi phục";
        } else {
            this.center = this.cmdNew;
            this.left = null;
        }
    }

    /** Chạm thẻ: thẻ đang chọn = thực hiện lệnh chính (vào game / tạo mới), thẻ khác = chọn thẻ đó. */
    void chamThe(int i) {
        if (i == this.selected && this.center != null && this.center.action != null) {
            this.center.action.perform();
        } else {
            chonThe(i);
        }
    }

    @Override
    public void update() {
        Canvas.loginScr.updateCamera();
        for (int i = 0; i < this.chars.length; i++) {
            this.chars[i].update();
        }
        if (Canvas.isKeyPressed(4)) {
            chonThe(this.selected <= 0 ? 2 : this.selected - 1);
        } else if (Canvas.isKeyPressed(6)) {
            chonThe(this.selected >= 2 ? 0 : this.selected + 1);
        }
        super.update();
    }
}
