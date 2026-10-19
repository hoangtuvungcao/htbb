package game.pokemon;

import game.model.Char;
import game.model.IAction;
import game.model.MainChar;
import game.model.PartyInfo;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.Font;
import game.render.screen.ListScr;
import game.render.screen.MessageScr;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * ĐỘI / BẠN BÈ (0.9.3): tab Đội (thành viên nhóm, nút Rời / Giải tán / Đuổi) | Bạn bè (danh sách bạn của máy chủ:
 * chạm = Nhắn tin / Xoá bạn bằng menu nút to). Thay "Menu -> Khác -> Nhóm / Bạn bè".
 */
public final class ManDoi extends ManBang {
    public static ManDoi me;

    public static ManDoi gI() {
        if (me == null) {
            me = new ManDoi();
        }
        return me;
    }

    public static void mo(int tab) {
        if (Canvas.gameScr == null) {
            return;
        }
        try {
            GameService.gI().getFriendList();   // máy chủ gửi lại danh sách bạn (gói 13 kiểu 0 -> ListScr.freindList)
        } catch (Throwable e) {
            // chưa nối máy chủ
        }
        gI().tab = tab;
        gI().moBang();
    }

    protected String tieuDe() {
        return "Đội và bạn bè";
    }

    protected String[] tabs() {
        return new String[]{"Đội (" + Char.party.size() + ")", "Bạn bè"};
    }

    static MainChar mc() {
        return Canvas.gameScr.mainChar;
    }

    /** 0.9.6: tấm hẹp, cao theo số người (tối thiểu 3 hàng + nút). */
    protected int rongMuon() {
        return ChuDe.dt() ? 470 : 420;
    }

    protected int caoMuon(int wNoi) {
        int k = Bang.cach(), nh = Bang.caoHang();
        if (this.tab == 0) {
            return Math.max(3, Char.party.size()) * (nh + k) + 8 + nh + k;
        }
        java.util.Vector b = ListScr.gI().freindList;
        return Math.max(4, b == null ? 0 : b.size()) * (nh + k) + 8;
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        int k = Bang.cach();
        int nh = Bang.caoHang();
        if (this.tab == 0) {
            final Vector p = Char.party;
            int hl = h - 8 - (p.size() > 0 ? nh + k : 0);
            if (p.size() == 0) {
                Bang.trong(g, x, y, w, h, "Chưa vào đội", "Chạm một người chơi ngoài bản đồ rồi chọn Mời vào đội.");
                return;
            }
            Bang.danhSach(g, this.ds, x, y + 2, w, hl, p.size(), nh, new Bang.Hang() {
                public void ve(Graphics g, int i, int x, int y, int w, int h) {
                    Object o = p.elementAt(i);
                    String ten = o instanceof PartyInfo ? ((PartyInfo) o).name : ((Char) o).name;
                    int lv = o instanceof PartyInfo ? ((PartyInfo) o).lv : ((Char) o).level;
                    int lop = o instanceof PartyInfo ? ((PartyInfo) o).clazz : ((Char) o).clazz;
                    final int id = o instanceof PartyInfo ? ((PartyInfo) o).id : ((Char) o).ID;
                    boolean truong = id == mc().IDMasterParty;
                    Bang.hang(g, x, y, w, h, i, false, mc().ID == mc().IDMasterParty && id != mc().ID ? new IAction() {
                        public void perform() {
                            HopXacNhan.hoiNguyHiem("Đuổi khỏi đội", "Mời thành viên này rời đội?", "Đuổi", new IAction() {
                                public void perform() {
                                    GameService.gI().kickOutParty(id, (byte) 0);
                                }
                            });
                        }
                    } : null, "doi-" + i);
                    int s = h - 6;
                    PokeBody.paintChanDung(g, LoaiPokemonClient.khoiDau(Math.max(0, Math.min(4, lop))), false, x + 3 + s / 2, y + 3 + s / 2, s);
                    ManNhanVat.chuHang(g, x, y, w, h, s, ten, "Lv." + lv, truong ? "Đội trưởng" : "Thành viên");
                }
            });
            boolean truong = mc().ID == mc().IDMasterParty;
            Bang.nut(g, x + w / 6, y + h - 2 - nh, w * 2 / 3, nh, truong ? "Giải tán đội" : "Rời đội", Bang.NGUY, new IAction() {
                public void perform() {
                    final boolean tr = ManDoi.mc().ID == ManDoi.mc().IDMasterParty;
                    HopXacNhan.hoiNguyHiem(tr ? "Giải tán đội" : "Rời đội", tr ? "Giải tán đội của bạn?" : "Rời khỏi đội?", tr ? "Giải tán" : "Rời đội", new IAction() {
                        public void perform() {
                            if (tr) {
                                GameService.gI().kickOutParty(0, (byte) 1);
                            } else {
                                GameService.gI().kickOutParty(ManDoi.mc().ID, (byte) 2);
                            }
                        }
                    });
                }
            }, "roi-doi");
            return;
        }
        final Vector b = ListScr.gI().freindList;
        if (b == null || b.size() == 0) {
            Bang.trong(g, x, y, w, h, "Chưa có bạn", "Chạm một người chơi ngoài bản đồ rồi chọn Kết bạn.");
            return;
        }
        Bang.danhSach(g, this.ds, x, y + 2, w, h - 4, b.size(), nh, new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                final Char c = (Char) b.elementAt(i);
                Bang.hang(g, x, y, w, h, i, false, new IAction() {
                    public void perform() {
                        ManDoi.this.hanhDongBan(c);
                    }
                }, "ban-" + i);
                int s = h - 6;
                PokeBody.paintChanDung(g, LoaiPokemonClient.khoiDau(Math.max(0, Math.min(4, c.clazz))), false, x + 3 + s / 2, y + 3 + s / 2, s);
                ManNhanVat.chuHang(g, x, y, w, h, s, c.name, "Lv." + c.level, "Chạm: nhắn tin / xoá bạn");
            }
        });
    }

    void hanhDongBan(final Char c) {
        Vector v = new Vector();
        v.addElement(new game.model.Command("Nhắn tin", new IAction() {
            public void perform() {
                Canvas.inputDlg.setInfo("Nhắn tới " + c.name, new IAction() {
                    public void perform() {
                        String t = Canvas.inputDlg.tfInput.getText();
                        if (t != null && t.trim().length() > 0) {
                            GameService.gI().sendMsgPrivate(c.name, t);
                            MessageScr.gI().addTab(t, c.name, 0);
                        }
                        Canvas.endDlg();
                    }
                }, 0, 100, true);
                Canvas.inputDlg.show();
            }
        }));
        v.addElement(new game.model.Command("Xoá bạn", new IAction() {
            public void perform() {
                HopXacNhan.hoiNguyHiem("Xoá bạn", "Xoá " + c.name + " khỏi danh sách bạn?", "Xoá bạn", new IAction() {
                    public void perform() {
                        GameService.gI().removeFriend(c.name);
                        ListScr.gI().freindList.removeElement(c);
                    }
                });
            }
        }));
        MenuNguCanh.mo(v, false);
    }
}
