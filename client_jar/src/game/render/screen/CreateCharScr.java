package game.render.screen;

import game.model.CharClassInfo;
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

public class CreateCharScr extends Screen {
    public static CreateCharScr me;
    public static final CharClassInfo[][] clazz = {new CharClassInfo[]{new CharClassInfo((byte) 0, "Dòng Charmander", "Nam", "Than lửa", "Miền Bắc", "Lửa", 3, 0), new CharClassInfo((byte) 0, "Dòng Charmander", "Nữ", "Than lửa", "Miền Bắc", "Lửa", 3, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 1, "Dòng Bulbasaur", "Nam", "Hạt giống thần", "Miền Bắc", "Cỏ", 4, 0), new CharClassInfo((byte) 1, "Dòng Bulbasaur", "Nữ", "Hạt giống thần", "Miền Bắc", "Cỏ", 4, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 2, "Dòng Eevee", "Nam", "Khăn lụa", "Miền Nam", "Thường", 5, 0), new CharClassInfo((byte) 2, "Dòng Eevee", "Nữ", "Khăn lụa", "Miền Nam", "Thường", 5, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 3, "Dòng Squirtle", "Nam", "Nước thần", "Miền Nam", "Nước", 6, 0), new CharClassInfo((byte) 3, "Dòng Squirtle", "Nữ", "Nước thần", "Miền Nam", "Nước", 6, 0)}, new CharClassInfo[]{new CharClassInfo((byte) 4, "Dòng Pichu", "Nam", "Nam châm", "Miền Bắc", "Điện", 7, 0), new CharClassInfo((byte) 4, "Dòng Pichu", "Nữ", "Nam châm", "Miền Bắc", "Điện", 7, 0)}};
    public static String[] hairName = {"Đầu đinh", "Lông búi", "Lông cột cao", "Lông chéo", "Lông xù", "Lông ngang vai"};
    static String[] gender = {"Nam", "Nữ"};
    public static int selectGender = 0;
    public static int selectNation = 0;
    Command cmdClear;
    int currentClazz;
    int selected;
    /** pokemon: đã báo "ký tự không nhận" (một lần mỗi lần mở màn) */
    boolean daBaoKyTu;
    public MainChar ch = new MainChar();
    public TField tfChat = new TField(this);

    public static CreateCharScr gI() {
        if (me != null) {
            return me;
        }
        CreateCharScr createCharScr = new CreateCharScr();
        me = createCharScr;
        return createCharScr;
    }

    @Override
    public void switchToMe() {
        loadImg();
        super.switchToMe();
        init();   // pokemon: bố cục theo cỡ màn HIỆN TẠI (cửa sổ / màn đổi cỡ khi đang ở màn khác)
        // pokemon: mở màn = ô Tên đang gõ (không kẹt ở dòng Vùng); bỏ cú chạm còn sót từ màn trước
        this.selected = 0;
        this.tfChat.isFocus = true;
        this.right = this.cmdClear;
        this.daBaoKyTu = false;
        Canvas.isPointerClick = false;
    }

    /** pokemon: phím là chữ gõ (bàn phím QWERTY / PC / Android), không phải phím số điều khiển. */
    static boolean laChuGo(int keyCode) {
        if (keyCode >= 65 && keyCode <= 90 || keyCode >= 97 && keyCode <= 122) {
            return true;
        }
        return TField.isQwerty && keyCode >= 32 && keyCode != 127;
    }

    /** pokemon: đưa con trỏ gõ về ô Tên (dòng 0). */
    void chonOTen() {
        this.selected = 0;
        this.tfChat.isFocus = true;
        this.right = this.cmdClear;
    }

    @Override
    public void init() {
        this.tfChat.x = Canvas.hw - 28;
        this.tfChat.y = Canvas.hh - 24;
        // pokemon: ô tên nằm trong bảng chọn Pokémon khởi đầu (xem paintPokemon)
        this.tfChat.x = xBang() + 44;
        this.tfChat.y = yBang() + 26 + canhAnh() + 6 + 58 + 4;
        this.tfChat.width = rongBang() - 54;
    }

    private static int rongBang() {
        int w = Canvas.w - 12;
        return w > 210 ? 210 : w;
    }

    private static int xBang() {
        return Canvas.hw - rongBang() / 2;
    }

    private static int yBang() {
        int y = Canvas.hh - 100;
        int duoi = Canvas.h - Screen.deltaY - 4 - caoBang();
        if (y > duoi) {
            y = duoi;
        }
        return y < 2 ? 2 : y;
    }

    private static int caoBang() {
        return 147 + canhAnh();
    }

    private static int canhAnh() {
        int c = (rongBang() - 20) / 5 - 6;
        return c > 30 ? 30 : c;
    }

    private static int xAnh(int i) {
        int k = (rongBang() - 20) / 5;
        return xBang() + 10 + k * i + k / 2;
    }

    public CreateCharScr() {
        this.tfChat.width = 80;
        this.tfChat.height = Screen.ITEM_HEIGHT + 2;
        this.tfChat.isFocus = true;
        this.tfChat.setIputType(3);
        // pokemon: khong co gioi tinh/kieu toc - luon gui gender = 0, headStyle = 0 (muc 4.2)
        selectGender = 0;
        this.ch.state = (byte) 0;
        this.currentClazz = 0;
        this.ch.hatStyle = (short) -1;
        this.ch.currentHead = clazz[this.currentClazz][selectGender].headID;
        this.ch.bodyStyle = clazz[this.currentClazz][selectGender].bodyID;
        this.ch.legStyle = clazz[this.currentClazz][selectGender].legID;
        this.ch.weaponType = clazz[this.currentClazz][selectGender].weaponType;
        this.ch.weaponStyle = clazz[this.currentClazz][selectGender].weaponImageID;
        this.left = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                CharSelectScr.gI().switchToMe();
                CreateCharScr.me = null;
            }
        });
        this.cmdClear = new Command("Xóa", new IAction() {
            @Override
            public void perform() {
                CreateCharScr.this.tfChat.clear();
            }
        });
        this.center = new Command("Tạo", new IAction() {
            @Override
            public void perform() {
                if (CreateCharScr.this.tfChat.getText().length() < 4 || CreateCharScr.this.tfChat.getText().length() > 15) {
                    Canvas.startOKDlg("Tên Pokémon phải từ 4 đến 15 ký tự");
                } else {
                    GameService.gI().createChar(CreateCharScr.this.tfChat.getText(), CreateCharScr.this.currentClazz, 0, 0, CreateCharScr.selectNation);
                    Canvas.startWaitDlg("Xin chờ", true);
                }
            }
        });
        init();
    }

    private void changChar() {
        this.ch.currentHead = clazz[this.currentClazz][selectGender].headID;
        this.ch.bodyStyle = clazz[this.currentClazz][selectGender].bodyID;
        this.ch.legStyle = clazz[this.currentClazz][selectGender].legID;
        this.ch.weaponType = clazz[this.currentClazz][selectGender].weaponType;
        this.ch.weaponStyle = clazz[this.currentClazz][selectGender].weaponImageID;
    }

    private void loadImg() {
        GameService.gI().doRequestWeapone(0, this.ch.weaponType, this.ch.weaponStyle, this.ch.weaponIndex);
    }

    @Override
    public boolean keyPress(int keyCode) {
        boolean isChangeClass = false;
        // pokemon: chữ gõ / xoá khi đang ở dòng khác (Pokémon, Vùng) -> vào ô Tên (không nuốt mất phím)
        if (this.selected != 0 && (laChuGo(keyCode) || keyCode == -8 || keyCode == 8)) {
            chonOTen();
        }
        if (this.selected == 0) {
            if (keyCode > 32 && keyCode != 127 && laChuGo(keyCode) && !(keyCode >= 48 && keyCode <= 57
                    || keyCode >= 65 && keyCode <= 90 || keyCode >= 97 && keyCode <= 122)) {
                // pokemon: ký tự không nhận (dấu tiếng Việt, ký hiệu): báo rõ một lần thay vì nuốt im lặng
                if (!this.daBaoKyTu && Canvas.currentDialog == null) {
                    this.daBaoKyTu = true;
                    Canvas.startOKDlg("Tên chỉ gồm chữ cái KHÔNG DẤU (a-z, A-Z) và số 0-9, dài 4-15 ký tự.");
                }
                return true;
            }
            this.tfChat.keyPressed(keyCode);
        }
        // pokemon: chi 3 dong - 0 ten, 1 Pokemon khoi dau (class), 2 lanh tho (dong goc 4)
        int dongCuoi = 2;
        if (keyCode == -2) {
            this.selected++;
            if (this.selected > dongCuoi) {
                this.selected = 0;
            }
            if (this.selected == 0) {
                this.tfChat.isFocus = true;
            } else {
                this.tfChat.isFocus = false;
            }
            this.right = this.selected == 0 ? this.cmdClear : null;
        }
        if (keyCode == -1) {
            this.selected--;
            if (this.selected < 0) {
                this.selected = dongCuoi;
            }
            if (this.selected == 0) {
                this.tfChat.isFocus = true;
            } else {
                this.tfChat.isFocus = false;
            }
            this.right = this.selected == 0 ? this.cmdClear : null;
        }
        int selected = this.selected;
        if (selected == 2) {
            selected = 4;
        }
        if (keyCode == -3) {
            if (selected == 1) {
                this.currentClazz++;
                if (this.currentClazz >= 5) {
                    this.currentClazz = 0;
                }
                isChangeClass = true;
            }
            if (selected == 4) {
                selectNation--;
                if (selectNation < 0) {
                    selectNation = 1;
                }
            }
        }
        if (keyCode == -4) {
            if (selected == 1) {
                this.currentClazz--;
                if (this.currentClazz < 0) {
                    this.currentClazz = 4;
                }
                isChangeClass = true;
            }
            if (selected == 4) {
                selectNation++;
                if (selectNation > 1) {
                    selectNation = 0;
                }
            }
        }
        if (isChangeClass) {
            this.ch.bodyStyle = clazz[this.currentClazz][selectGender].bodyID;
            this.ch.legStyle = clazz[this.currentClazz][selectGender].legID;
            this.ch.currentHead = clazz[this.currentClazz][selectGender].headID;
            this.ch.weaponType = clazz[this.currentClazz][selectGender].weaponType;
            this.ch.weaponStyle = clazz[this.currentClazz][selectGender].weaponImageID;
            loadImg();
        }
        return super.keyPress(keyCode);
    }

    private IAction viecTao, viecLui, viecVung;

    /**
     * 0.9.5 PC / APK: 5 Pokémon khởi đầu TRÊN ĐƯỜNG banner là nút chọn (chạm = chọn, vòng sáng + nhảy); bảng chỉ còn
     * thông tin Pokémon đang chọn, ô Tên, Vùng, Tạo / Quay lại - đặt bằng KhungVao nên không che đội hình / logo.
     */
    private void veMoi(Graphics g) {
        this.anNutLenh = true;
        if (this.viecTao == null) {
            this.viecTao = new IAction() {
                public void perform() {
                    if (CreateCharScr.this.center != null && CreateCharScr.this.center.action != null) {
                        CreateCharScr.this.center.action.perform();
                    }
                }
            };
            this.viecLui = new IAction() {
                public void perform() {
                    if (CreateCharScr.this.left != null && CreateCharScr.this.left.action != null) {
                        CreateCharScr.this.left.action.perform();
                    }
                }
            };
            this.viecVung = new IAction() {
                public void perform() {
                    selectNation = selectNation == 0 ? 1 : 0;
                    CreateCharScr.this.selected = 2;
                    CreateCharScr.this.tfChat.isFocus = false;
                    CreateCharScr.this.right = null;
                }
            };
        }
        ServerListScr.gI().paintBg(g, this.currentClazz, true);
        // Pokémon trên đường = nút chọn; tên nổi trên Pokémon đang chọn
        for (int i = 0; i < 5; i++) {
            int[] p = game.pokemon.Banner.viTriKhoiDau(i);
            int t = p[2];
            final int khoi = i;
            int rw = Math.max(game.pokemon.VungCham.MIN, 40 * t / 100), rh = Math.max(game.pokemon.VungCham.MIN, 44 * t / 100);
            game.pokemon.VungCham.them(g, p[0] - rw / 2, p[1] - rh + 4 * t / 100, rw, rh, new IAction() {
                public void perform() {
                    CreateCharScr.this.chonKhoiDau(khoi);
                }
            }, "khoi-dau-" + i);
        }
        {
            int[] p = game.pokemon.Banner.viTriKhoiDau(this.currentClazz);
            String ten = LoaiPokemonClient.TEN_KHOI_DAU[this.currentClazz];
            int wt = game.pokemon.ChuDe.rong(ten, game.pokemon.ChuDe.NHAN_NHO) + 12;
            int ht = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN_NHO) + 4;
            int yt = p[1] - 44 * p[2] / 100 - ht - 2;
            game.pokemon.ChuDe.the(g, p[0] - wt / 2, yt, wt, ht, ht / 2);
            game.pokemon.ChuDe.chuGiua(g, ten, p[0], yt, ht, game.pokemon.ChuDe.NHAN_NHO, game.pokemon.ChuDe.CHU, 2);
        }
        int hb = game.pokemon.KhungVao.caoNut(), k = game.pokemon.KhungVao.khe();
        int hn = game.pokemon.KhungVao.caoNhan(), ho = game.pokemon.KhungVao.caoO();
        int lh = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN), lp = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU);
        int hVung = Math.max(game.pokemon.VungCham.MIN, lp + 10);
        int dau = game.pokemon.ChuDe.caoDau() + 2 * game.pokemon.ChuDe.DEM;
        int hc = dau + lh + lp + lp + k + hn + ho + k + hVung + k + hb;
        int[] r = game.pokemon.KhungVao.logoVaBang(g, 300, hc, hc, 160);
        int[] n = game.pokemon.Bang.khung(g, r[0], r[1], r[2], r[3], "Tạo Pokémon", null, 0, null, null);
        int x = n[0] + 2, w = n[2] - 4, y = n[1];
        int c = this.currentClazz;
        String he = LoaiPokemonClient.HE_KHOI_DAU[c];
        String chuHe = he.startsWith("Hệ ") ? he.substring(3) : he;
        int wHe = game.pokemon.ChuDe.rong(chuHe, game.pokemon.ChuDe.NHAN_NHO) + 12;
        game.pokemon.ChuDe.chu(g, LoaiPokemonClient.TEN_KHOI_DAU[c], x, y, game.pokemon.ChuDe.NHAN, game.pokemon.ChuDe.CHU, 0);
        int xHe = x + game.pokemon.ChuDe.rong(LoaiPokemonClient.TEN_KHOI_DAU[c], game.pokemon.ChuDe.NHAN) + 6;
        if (xHe + wHe <= x + w) {
            int hHe = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN_NHO) + 2;
            game.pokemon.GiaoDien.khoi(g, xHe, y + (lh - hHe) / 2, wHe, hHe, hHe / 2, game.pokemon.GiaoDien.mauHe(he));
            game.pokemon.ChuDe.chuGiua(g, chuHe, xHe + wHe / 2, y + (lh - hHe) / 2, hHe, game.pokemon.ChuDe.NHAN_NHO, 0xFFFFFF, 2);
        }
        y += lh;
        game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat(LoaiPokemonClient.VAI_TRO[c], game.pokemon.ChuDe.PHU, w), x, y, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 0);
        y += lp;
        game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat("Chạm Pokémon trên đường để chọn", game.pokemon.ChuDe.PHU, w), x, y, game.pokemon.ChuDe.PHU,
                game.pokemon.ChuDe.XANH, 0);
        y += lp + k;
        y = game.pokemon.KhungVao.oNhap(g, this.tfChat, "Tên (4-15 chữ cái không dấu, số)", x, y, w, false) + k;
        // vùng: chạm = đổi
        game.pokemon.Bang.hang(g, x, y, w, hVung, 0, this.selected == 2, this.viecVung, "vung");
        game.pokemon.ChuDe.chuGiua(g, "Vùng: " + GameScr.nameCountry[selectNation], x + 10, y, hVung, game.pokemon.ChuDe.THUONG, game.pokemon.ChuDe.CHU, 0);
        game.pokemon.ChuDe.chuGiua(g, "đổi", x + w - 10, y, hVung, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.XANH, 1);
        y += hVung + k;
        game.pokemon.KhungVao.hangNut(g, x, y, w, new String[]{"Quay lại", "Tạo"}, new int[]{game.pokemon.Bang.TRUNG, game.pokemon.Bang.CHINH},
                new Object[]{this.viecLui, this.viecTao}, "tao");
    }

    @Override
    public void paint(Graphics g) {
        if (game.pokemon.KhungVao.moi()) {
            veMoi(g);
            return;
        }
        // pokemon: banner, Pokémon khởi đầu đang chọn có cột sáng + hào quang màu hệ
        ServerListScr.gI().paintBg(g, this.currentClazz, true);
        Canvas.resetTrans(g);
        paintPokemon(g);
        this.tfChat.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        super.paint(g);
    }

    /**
     * pokemon: màn tạo nhân vật - 5 Pokémon khởi đầu thay 5 class (bảng 4.1, Unity CreateCharScr.cs).
     * Bảng xanh, hàng 5 chân dung (chạm để chọn), Pokémon đang chọn đứng trong vòng sáng + tên + huy hiệu
     * hệ + vai trò, ô tên và lãnh thổ. Dòng đang chọn (phím lên/xuống) có viền vàng.
     */
    private void paintPokemon(Graphics g) {
        int x0 = xBang();
        int y0 = yBang();
        int W = rongBang();
        game.pokemon.GiaoDien.bangMo(g, x0, y0, W, caoBang());
        game.pokemon.GiaoDien.dai(g, x0 + 4, y0 + 4, W - 8, 17, "Chọn Pokémon khởi đầu");
        int c = canhAnh();
        int yAnh = y0 + 26;
        for (int i = 0; i < 5; i++) {
            int dex = LoaiPokemonClient.khoiDau(i);
            int cx = xAnh(i);
            boolean dangChon = i == this.currentClazz;
            // huy hiệu tròn màu hệ (HuyHieuPoke); đang chọn: vòng vàng dày
            if (dangChon) {
                g.setColor(game.pokemon.GiaoDien.VANG);
                g.fillArc(cx - c / 2 - 3, yAnh - 3, c + 6, c + 6, 0, 360);
            }
            final int khoi = i;
            game.pokemon.VungCham.them(g, cx - c / 2 - 2, yAnh - 2, c + 4, c + 4, new IAction() {
                public void perform() {
                    CreateCharScr.this.chonKhoiDau(khoi);
                }
            }, "khoi-dau-" + i);
            if (!PokeBody.paintChanDung(g, dex, false, cx, yAnh + c / 2, c)) {
                game.pokemon.GiaoDien.khoi(g, cx - c / 2, yAnh, c, c, 3, game.pokemon.GiaoDien.mauHe(LoaiPokemonClient.HE_KHOI_DAU[i]));
            }
        }
        if (this.selected == 1) {
            g.setColor(game.pokemon.GiaoDien.VANG);
            g.drawRect(x0 + 6, yAnh - 4, W - 13, c + 7);
        }
        // Pokémon đang chọn
        int yGiua = yAnh + c + 6;
        game.pokemon.GiaoDien.khoi(g, x0 + 6, yGiua, W - 12, 58, 4, game.pokemon.GiaoDien.TRANG);
        int dex = LoaiPokemonClient.khoiDau(this.currentClazz);
        int xp = x0 + 34;
        g.setColor(0xBFE0FF);
        g.fillArc(xp - 22, yGiua + 40, 44, 14, 0, 360);
        g.setClip(x0 + 8, yGiua + 2, 54, 54);
        if (!PokeBody.paint(g, dex, false, xp, yGiua + 48, 0, PokeDuLieu.DUNG, Canvas.gameTick, 150)) {
            this.ch.paintAvatar(g, (short) xp, (short) (yGiua + 48));
        }
        Canvas.resetTrans(g);
        int xc = x0 + 64;
        Font.blackFont.drawString(g, LoaiPokemonClient.TEN_KHOI_DAU[this.currentClazz], xc, yGiua + 4, 0);
        game.pokemon.GiaoDien.huyHieuHe(g, xc, yGiua + 21, LoaiPokemonClient.HE_KHOI_DAU[this.currentClazz]);
        Font.arialFont.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFont, LoaiPokemonClient.VAI_TRO[this.currentClazz], x0 + W - 10 - xc), xc, yGiua + 40, 0);
        // chạm thẻ Pokémon đang chọn / mũi tên = Pokémon kế tiếp
        game.pokemon.VungCham.them(g, x0 + 6, yGiua, W - 12, 58, new IAction() {
            public void perform() {
                CreateCharScr.this.chonKhoiDau((CreateCharScr.this.currentClazz + 1) % 5);
            }
        }, "khoi-dau-ke");
        // mũi tên trái / phải đổi Pokémon
        g.setColor(game.pokemon.GiaoDien.XANH);
        g.fillTriangle(x0 + W - 22, yGiua + 10, x0 + W - 22, yGiua + 22, x0 + W - 15, yGiua + 16);
        // ô tên
        int yTen = this.tfChat.y;
        Font.normalFont[0].drawString(g, "Tên:", x0 + 10, yTen + 3, 0);
        if (this.selected == 0) {
            game.pokemon.GiaoDien.muiTen(g, x0 + 4, yTen + 5, 8, game.pokemon.GiaoDien.VANG);
        }
        // lãnh thổ
        int yVung = yTen + 26;
        if (this.selected == 2) {
            game.pokemon.GiaoDien.chon(g, x0 + 6, yVung - 3, W - 12, 19);
        }
        game.pokemon.VungCham.them(g, x0 + 6, yVung - 3, W - 12, 19, new IAction() {
            public void perform() {
                CreateCharScr.this.chamVung();
            }
        }, "vung");
        Font.normalFont[0].drawString(g, new StringBuffer("Vùng: ").append(GameScr.nameCountry[selectNation]).toString(), x0 + 12, yVung, 0);
        g.setColor(game.pokemon.GiaoDien.VANG);
        g.fillTriangle(x0 + W - 34, yVung + 6, x0 + W - 28, yVung + 1, x0 + W - 28, yVung + 11);
        g.fillTriangle(x0 + W - 12, yVung + 6, x0 + W - 18, yVung + 1, x0 + W - 18, yVung + 11);
    }

    /** Chọn Pokémon khởi đầu i (chạm chân dung / thẻ). */
    void chonKhoiDau(int i) {
        if (this.currentClazz != i) {
            this.currentClazz = i;
            changChar();
            loadImg();
        }
        this.selected = 1;
        this.tfChat.isFocus = false;
        this.right = null;
    }

    /** Chạm dòng Vùng: lần đầu chỉ chọn dòng, lần nữa mới đổi vùng (không đổi nhầm). */
    void chamVung() {
        if (this.selected == 2) {
            selectNation = selectNation == 0 ? 1 : 0;
        }
        this.selected = 2;
        this.tfChat.isFocus = false;
        this.right = null;
    }

    /** pokemon: chạm chân dung để chọn Pokémon, chạm dòng vùng để đổi vùng. */
    private void chamPokemon() {
        if (!Canvas.isPointerClick || Canvas.currentDialog != null) {
            return;
        }
        int c = canhAnh();
        int yAnh = yBang() + 26;
        for (int i = 0; i < 5; i++) {
            if (Canvas.isPointer(xAnh(i) - c / 2 - 2, yAnh - 2, c + 4, c + 4)) {
                Canvas.isPointerClick = false;
                if (this.currentClazz != i) {
                    this.currentClazz = i;
                    changChar();
                    loadImg();
                }
                this.selected = 1;
                this.tfChat.isFocus = false;
                return;
            }
        }
        int yVung = this.tfChat.y + 26;
        if (Canvas.isPointer(xBang() + 6, yVung - 3, rongBang() - 12, 19)) {
            Canvas.isPointerClick = false;
            // pokemon: chạm lần đầu chỉ CHỌN dòng Vùng, chạm lần nữa mới đổi vùng (không đổi nhầm)
            if (this.selected == 2) {
                selectNation = selectNation == 0 ? 1 : 0;
            }
            this.selected = 2;
            this.tfChat.isFocus = false;
            this.right = null;
            return;
        }
        // chạm vào hàng ô Tên (cả nhãn "Tên:") = gõ tên
        if (Canvas.isPointer(xBang() + 6, this.tfChat.y - 2, rongBang() - 12, this.tfChat.height + 4) && this.selected != 0) {
            chonOTen();
        }
    }

    @Override
    public void update() {
        if (!game.pokemon.KhungVao.moi()) {
            chamPokemon();
        }
        this.tfChat.update();
        if (this.tfChat.isFocus && this.selected != 0) {
            chonOTen();   // pokemon: TField tự bật focus khi chạm ô -> đồng bộ dòng đang chọn
        }
        this.ch.update();
        super.update();
    }
}
