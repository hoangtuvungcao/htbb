package game.pokemon;

import game.model.Actor;
import game.model.IAction;
import game.model.ItemQuest;
import game.model.Quest;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * SỔ NHIỆM VỤ (0.9.3, bảng chung Bang): tab Đang làm | Có thể nhận. Hàng = biểu tượng + tên + tiến độ (x/y); chạm =
 * thẻ chi tiết: mô tả, thanh tiến độ từng mục tiêu, phần thưởng, nút to "Đi tới NPC" (tìm đường nếu NPC ở bản đồ
 * này) và "Huỷ nhiệm vụ" (xác nhận đỏ). Nhiệm vụ mới nhận / trả ở NPC như cũ (menu ngữ cảnh khi chạm NPC).
 */
public final class ManNhiemVu extends Screen {
    public static ManNhiemVu me;
    int tab;
    Quest chon;
    final Cuon ds = new Cuon();

    public static ManNhiemVu gI() {
        if (me == null) {
            me = new ManNhiemVu();
        }
        return me;
    }

    public static void mo() {
        if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        ManNhiemVu m = gI();
        m.tab = dangLam().size() == 0 && GameScr.allQuestCanReceive.size() > 0 ? 1 : 0;
        m.chon = null;
        m.ds.den(0);
        m.left = null;
        m.center = null;
        m.right = null;
        Canvas.menu.showMenu = false;
        m.switchToMe();
        AmThanh.phat(AmThanh.MO);
    }

    /** Có nhiệm vụ mới nhận được (chấm đỏ ô Nhiệm vụ ở Menu). */
    public static boolean coMoi() {
        for (int i = 0; i < GameScr.allQuestCanReceive.size(); i++) {
            Quest q = (Quest) GameScr.allQuestCanReceive.elementAt(i);
            if (q.stateQuest == 0) {
                return true;
            }
        }
        Vector d = dangLam();
        for (int i = 0; i < d.size(); i++) {
            if (xong((Quest) d.elementAt(i))) {
                return true;   // đã đủ, về trả
            }
        }
        return false;
    }

    static Vector dangLam() {
        Vector v = new Vector();
        if (GameScr.mainQuest != null) {
            v.addElement(GameScr.mainQuest);
        }
        if (GameScr.subQuest != null) {
            v.addElement(GameScr.subQuest);
        }
        if (GameScr.clanQuest != null) {
            v.addElement(GameScr.clanQuest);
        }
        return v;
    }

    /** Tên NPC theo loại (NPC.NPCNAME, cùng chỉ số với Quest.idNpcReceive / idNpcResponse); null nếu ngoài bảng. */
    static String tenNpc(int loaiNpc) {
        return loaiNpc >= 0 && loaiNpc < game.model.NPC.NPCNAME.length ? game.model.NPC.NPCNAME[loaiNpc] : null;
    }

    /** "Blaziken (bản đồ này)" / "Blaziken" / "NPC nhiệm vụ". */
    static String noiNpc(int loaiNpc) {
        String t = tenNpc(loaiNpc);
        if (t == null) {
            return "NPC nhiệm vụ";
        }
        return npc(loaiNpc) != null ? t + " (bản đồ này)" : t;
    }

    /** Biểu tượng theo loại nhiệm vụ: đánh quái = chiêu, thu thập = túi, nói chuyện = chat, tiến hoá = Pokédex. */
    static int hinhLoai(Quest q) {
        switch (q.type) {
            case Quest.TYPE_KILL_MONS:
            case Quest.TYPE_KILL_MONSTER_LEVEL:
                return BieuTuong.CHIEU;
            case Quest.TYPE_GET_ITEM:
                return BieuTuong.TUI;
            case Quest.TYPE_TALK:
                return BieuTuong.CHAT;
            case Quest.TYPE_TRANSFORM:
                return BieuTuong.DEX;
            default:
                return BieuTuong.NHIEM_VU;
        }
    }

    /** Ô biểu tượng nhiệm vụ cạnh s: nền màu theo tuyến (chính đỏ, đội xanh lá, phụ xanh; xong = vàng). */
    static void veIcon(Graphics g, Quest q, int x, int y, int s) {
        int mau = xong(q) ? ChuDe.VANG : (q == GameScr.mainQuest ? ChuDe.DO : (q == GameScr.clanQuest ? ChuDe.LA : ChuDe.XANH));
        GiaoDien.khoi(g, x, y, s, s, ChuDe.BO_O, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, s - 2, s - 2, ChuDe.BO_O - 1, GiaoDien.pha(mau, GiaoDien.TRANG, 1, 3), mau);
        int k = s * 3 / 5;
        BieuTuong.ve(g, xong(q) ? BieuTuong.NHIEM_VU : hinhLoai(q), x + (s - k) / 2, y + (s - k) / 2, k, 0xFFFFFF, mau);
    }

    static String loai(Quest q) {
        return q == GameScr.mainQuest ? "Chính tuyến" : (q == GameScr.clanQuest ? "Đội" : "Phụ");
    }

    /** {đã, cần} tổng của các mục tiêu (đánh quái / thu thập); null nếu không đếm được. */
    static int[] tienDo(Quest q) {
        try {
            if (q.stateQuest == 2 && q.type == Quest.TYPE_KILL_MONS && q.totalMonskill != null) {
                int a = 0, b = 0;
                for (int i = 0; i < q.totalMonskill.length; i++) {
                    a += Math.min(q.allMonsKilled[i], q.totalMonskill[i]);
                    b += q.totalMonskill[i];
                }
                return new int[]{a, b};
            }
            if (q.stateQuest == 2 && q.type == Quest.TYPE_GET_ITEM && q.totalItemGet != null) {
                int a = 0, b = 0;
                for (int i = 0; i < q.totalItemGet.length; i++) {
                    a += Math.min(q.allItemGet[i], q.totalItemGet[i]);
                    b += q.totalItemGet[i];
                }
                return new int[]{a, b};
            }
        } catch (Throwable e) {
            // thiếu dữ liệu
        }
        return null;
    }

    static boolean xong(Quest q) {
        int[] t = tienDo(q);
        return q.stateQuest == 1 || t != null && t[1] > 0 && t[0] >= t[1];
    }

    static Actor npc(int loaiNpc) {
        GameScr gs = Canvas.gameScr;
        for (int i = 0; gs != null && i < gs.actors.size(); i++) {
            Actor a = (Actor) gs.actors.elementAt(i);
            try {
                if (a.isNPC() && a.getTypeNpc() == loaiNpc) {
                    return a;
                }
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        return null;
    }

    void dong() {
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.gameScr.switchToMe();
    }

    public void update() {
        boolean c = Canvas.isPointerClick, d = Canvas.isPointerDown;
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        try {
            Canvas.gameScr.update();
        } catch (Throwable e) {
            // bỏ qua
        }
        Canvas.isPointerClick = c;
        Canvas.isPointerDown = d;
        if (Canvas.currentScreen != this || Canvas.currentDialog != null) {
            return;
        }
        if (Canvas.isKeyPressed(4) || Canvas.isKeyPressed(6)) {
            this.tab = 1 - this.tab;
            this.chon = null;
            this.ds.den(0);
        }
        if (Canvas.isKeyPressed(2)) {
            this.ds.them(-(Bang.caoHang() + Bang.cach()));
        } else if (Canvas.isKeyPressed(8)) {
            this.ds.them(Bang.caoHang() + Bang.cach());
        }
    }

    public void updateKey() {
    }

    public boolean keyPress(int keyCode) {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            return false;
        }
        if (keyCode == -7 || keyCode == 27) {
            if (this.chon != null) {
                this.chon = null;
            } else {
                dong();
            }
            return true;
        }
        return false;
    }

    public void paint(Graphics g) {
        try {
            game.render.screen.GameScr.veNen(g);   // 0.9.5: chỉ lớp thế giới (HUD sau bảng không vẽ)
        } catch (Throwable e) {
            // bỏ qua
        }
        Bang.phuNen(g);
        Object[] vt = {new IAction() {
            public void perform() {
                ManNhiemVu.this.tab = 0;
                ManNhiemVu.this.chon = null;
                ManNhiemVu.this.ds.den(0);
            }
        }, new IAction() {
            public void perform() {
                ManNhiemVu.this.tab = 1;
                ManNhiemVu.this.chon = null;
                ManNhiemVu.this.ds.den(0);
            }
        }};
        int[] c = Bang.khung(g, "Nhiệm vụ", new String[]{"Đang làm", "Có thể nhận"}, this.tab, vt, new IAction() {
            public void perform() {
                ManNhiemVu.this.dong();
            }
        });
        int x = c[0], y = c[1], w = c[2], h = c[3];
        int k = Bang.cach();
        final Vector v = this.tab == 0 ? dangLam() : GameScr.allQuestCanReceive;
        boolean hai = w >= 400;
        int wl = hai ? w * 46 / 100 : w;
        if (!hai && this.chon != null) {
            veChiTiet(g, x, y, w, h);
            return;
        }
        Bang.the(g, x, y, wl, h);
        if (v.size() == 0) {
            Bang.trong(g, x, y, wl, h, this.tab == 0 ? "Chưa nhận nhiệm vụ" : "Chưa có nhiệm vụ mới",
                    this.tab == 0 ? "Nói chuyện với NPC có dấu chấm than để nhận nhiệm vụ." : "Lên cấp hoặc khám phá thêm bản đồ để mở nhiệm vụ mới.");
        } else {
            Bang.danhSach(g, this.ds, x + 4, y + 4, wl - 8, h - 8, v.size(), Bang.caoHang(), new Bang.Hang() {
                public void ve(Graphics g, int i, int x, int y, int w, int h) {
                    final Quest q = (Quest) v.elementAt(i);
                    Bang.hang(g, x, y, w, h, i, q == ManNhiemVu.this.chon, new IAction() {
                        public void perform() {
                            ManNhiemVu.this.chon = q;
                        }
                    }, "nv-" + i);
                    int s = h - 6;
                    veIcon(g, q, x + 3, y + 3, s);
                    int[] td = tienDo(q);
                    String so = xong(q) ? "Xong" : (td != null ? td[0] + "/" + td[1] : null);
                    String phu = ManNhiemVu.this.tab == 0 ? loai(q) + (xong(q) ? " · trả ở " + noiNpc(q.idNpcResponse) : "")
                            : (q.deltaLv > 0 ? "Cần cấp " + q.deltaLv + " · " : "") + "Nhận ở " + noiNpc(q.idNpcReceive);
                    ManNhanVat.chuHang(g, x, y, w, h, s, q.name, so, phu);
                }
            });
        }
        if (hai) {
            int xr = x + wl + k, wr = w - wl - k;
            if ((this.chon == null || !v.contains(this.chon)) && v.size() > 0) {
                this.chon = (Quest) v.elementAt(0);   // hai cột: luôn có nhiệm vụ đang xem (cái đầu tiên)
            }
            if (this.chon != null && v.contains(this.chon)) {
                veChiTiet(g, xr, y, wr, h);
            } else {
                Bang.the(g, xr, y, wr, h);
                Bang.trong(g, xr, y, wr, h, "Chọn nhiệm vụ để xem", null);
            }
        }
    }

    private void veChiTiet(Graphics g, int x, int y, int w, int h) {
        final Quest q = this.chon;
        int k = Bang.cach();
        Bang.the(g, x, y, w, h);
        int hd = Bang.co(26);
        Bang.nutLui(g, x + 4, y + 4, hd, new IAction() {
            public void perform() {
                ManNhiemVu.this.chon = null;
            }
        });
        veIcon(g, q, x + hd + 10, y + 4, hd);
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, q.name, w - 2 * hd - 22), x + 2 * hd + 16, y + 4 + (hd - Font.blackFont.getHeight()) / 2, 0);
        int nh = Bang.caoHang();
        int yn = y + h - 6 - nh;
        int yy = y + 4 + hd + k;
        boolean dangLam = dangLam().contains(q);
        final int idNpc = dangLam ? q.idNpcResponse : q.idNpcReceive;
        Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, (dangLam ? (xong(q) ? "Trả ở: " : "Báo lại: ") : "Nhận ở: ") + noiNpc(idNpc), w - 20),
                x + 10, yy, 0);
        yy += Font.blackFont.getHeight() + 3;
        yy = Bang.doan(g, Font.arialFont, q.decript, x + 10, yy, w - 20, false) + 2;
        // tiến độ từng mục tiêu
        try {
            if (q.stateQuest == 2 && q.type == Quest.TYPE_KILL_MONS && q.idMonsKill != null) {
                for (int i = 0; i < q.idMonsKill.length && yy + 14 < yn - k; i++) {
                    yy = thanhMuc(g, x + 10, yy, w - 20, Res.monsterTemplates[q.idMonsKill[i]].name, q.allMonsKilled[i], q.totalMonskill[i]);
                }
            } else if (q.stateQuest == 2 && q.type == Quest.TYPE_GET_ITEM && q.idItemGet != null) {
                for (int i = 0; i < q.idItemGet.length && yy + 14 < yn - k; i++) {
                    yy = thanhMuc(g, x + 10, yy, w - 20, ItemQuest.NAME_ITEM[q.idItemGet[i]], q.allItemGet[i], q.totalItemGet[i]);
                }
            }
        } catch (Throwable e) {
            // thiếu dữ liệu quái / vật phẩm
        }
        if (q.infoGift != null && q.infoGift.length() > 0 && yy + 14 < yn - k) {
            Font.blackFont.drawString(g, "Phần thưởng:", x + 10, yy + 2, 0);
            Bang.doan(g, Font.arialFont, q.infoGift, x + 10, yy + 4 + Font.blackFont.getHeight(), w - 20, false);
        }
        final Actor a = npc(idNpc);
        String tn = tenNpc(idNpc);
        int wb = dangLam && q == GameScr.mainQuest ? (w - 12 - k) / 2 : w - 12;
        if (dangLam && q == GameScr.mainQuest) {
            Bang.nut(g, x + 6, yn, wb, nh, "Huỷ nhiệm vụ", Bang.TRUNG, new IAction() {
                public void perform() {
                    HopXacNhan.hoiNguyHiem("Huỷ nhiệm vụ", "Từ bỏ " + q.name + "? Tiến độ sẽ mất.", "Huỷ nhiệm vụ", new IAction() {
                        public void perform() {
                            Canvas.gameScr.mainChar.resetQuest();
                            Canvas.gameScr.dellPotionQuest();
                            GameService.gI().receiveQuest(0, 0, 3);
                            ManNhiemVu.this.chon = null;
                        }
                    });
                }
            }, "huy-nv");
        }
        Bang.nut(g, x + w - 6 - wb, yn, wb, nh, a != null ? "Đi tới " + (tn != null ? tn : "NPC") : (tn != null ? tn + " ở bản đồ khác" : "NPC ở bản đồ khác"), a != null ? Bang.XAC_NHAN : Bang.TAT, new IAction() {
            public void perform() {
                try {
                    Canvas.gameScr.switchToMe();
                    Canvas.gameScr.findRoad2(a.x, a.y);
                    HanhDong.chamDat(a.x, a.y);
                } catch (Throwable e) {
                    // không tìm được đường
                }
            }
        }, "di-toi");
    }

    static int thanhMuc(Graphics g, int x, int y, int w, String ten, int da, int can) {
        Font.arialFont.drawString(g, GiaoDien.catChu(Font.arialFont, ten, w - 60), x, y, 0);
        Font.blackFont.drawString(g, Math.min(da, can) + "/" + can, x + w, y, 1);
        int yb = y + Font.arialFont.getHeight() + 1;
        GiaoDien.thanh(g, x, yb, w, 7, Math.min(da, can), Math.max(1, can), 2);
        return yb + 10;
    }
}
