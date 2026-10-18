package game.pokemon;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Ve NGOAI HINH TRANG BI quanh than Pokemon (HTBB docs/pokemon/NGOAI_HINH_TRANG_BI.md), ban 2 "it ma dep":
 *  - TOI DA 2 phu kien: do doi dau (mu / no / vuong mien) + MOT mon "noi bat" nhat trong khan / vong / do cam /
 *    sau lung (do hiem roi bac cao nhat). Cac mon con lai chi gop vao vong sang duoi chan.
 *  - KHONG BAO GIO ve len mat: mu dat tren dinh dau (chong 1 px); do o co dat giua diem dau va tam than (gan
 *    than, duoi mieng), chi ve TRUOC than khi nhin xuong - nhin ngang / quay lung thi ve SAU than; co qua ngan
 *    (dau sat tam than) hoac loai khong co co (bang ngoai le trong phukien.bin: sau, ca, cuc tron...) thi bo.
 *  - Co phu kien theo co than: be ngang do o co toi da ~1/3 be ngang con Pokemon (khong vua thi dung ban nho,
 *    van khong vua thi bo); loai to (cao >= 64 px) ve gap doi.
 *  - Vong sang duoi chan: elip mem, alpha thap, mau bac tot nhat (bo du 11 mon: sang hon). Vien sang quanh than
 *    chi o +15; hat lap lanh chi tu +13 (1 hat) / +15 (2 hat), bay o hai ben, khong de len than.
 * Phu kien dat theo diem neo PMD tung khung (PokeBody.neo); jar khong co neo (ban nhe) chi ve vong sang.
 */
public final class NgoaiHinhVe {
    /** Ve ngoai hinh cua NGUOI KHAC (cai dat phia client "Phụ kiện người khác", RMS cua NgoaiHinhMenu). */
    public static boolean hien = true;

    private static final int[] NEO = new int[8];
    /** sin * 64 cho 16 goc. */
    private static final int[] SIN16 = {0, 24, 45, 59, 64, 59, 45, 24, 0, -24, -45, -59, -64, -59, -45, -24};
    /** Thu tu uu tien mon "noi bat" khi bang diem: khan, do cam, vong, sau lung. */
    private static final int[] UU_TIEN = {NgoaiHinhCua.KHAN, NgoaiHinhCua.TAY, NgoaiHinhCua.VONG, NgoaiHinhCua.LUNG};

    private NgoaiHinhVe() {
    }

    private static int sin16(int i) {
        return SIN16[i & 15];
    }

    /** He so phong phu kien: loai to ve gap doi. */
    static int heSo(int dex, boolean shiny) {
        return PokeBody.rongHinh(dex, shiny) >= 46 || PokeBody.caoTrenChan(dex, shiny, 100) >= 64 ? 2 : 1;
    }

    /** Ban kinh ngang vong sang duoi chan theo be ngang hinh. */
    static int banKinh(int dex, boolean shiny) {
        int r = PokeBody.rongHinh(dex, shiny) * 2 / 5;
        return r < 8 ? 8 : r > 30 ? 30 : r;
    }

    /** Co cua loai cam o (bang ngoai le). */
    private static boolean biCam(int dex, int o) {
        int nl = PhuKien.ngoaiLe(dex);
        switch (o) {
            case NgoaiHinhCua.MU:
                return (nl & PhuKien.NL_MU) != 0;
            case NgoaiHinhCua.KHAN:
            case NgoaiHinhCua.VONG:
                return (nl & PhuKien.NL_CO) != 0;
            case NgoaiHinhCua.TAY:
                return (nl & PhuKien.NL_TAY) != 0;
            case NgoaiHinhCua.LUNG:
                return (nl & PhuKien.NL_LUNG) != 0;
            default:
                return true;   // da he, kinh: chi gop vao vong sang (khong che mat)
        }
    }

    /** Mon "noi bat" thu hai (ngoai mu) se ve, -1 = khong co. On dinh theo trang bi, khong doi theo huong. */
    static int monThuHai(NgoaiHinhCua n, int dex) {
        int chon = -1, diem = -1;
        for (int i = 0; i < UU_TIEN.length; i++) {
            int o = UU_TIEN[i];
            if (n.kieu[o] <= 0 || biCam(dex, o)) {
                continue;
            }
            int d = n.hiem[o] * 16 + n.bac[o];
            if (d > diem) {
                diem = d;
                chon = o;
            }
        }
        return chon;
    }

    public static void veSau(Graphics g, NgoaiHinhCua n, int dex, boolean shiny, int x, int y, int yChan, int dir, int dongTac, int tick) {
        if (n == null) {
            return;
        }
        // vong sang mem duoi chan (moi bac >= 1; bo du 11 mon sang hon)
        if (n.bacTot >= 1) {
            int rw = banKinh(dex, shiny), rh = rw / 3 + 1;
            int mau = n.bacTot >= 10 ? 0xFFD860 : PhuKien.mauBac(n.bacTot, tick);
            int a = 40 + n.bacTot * 4 + (n.bo >= 11 ? 30 : n.bo >= 7 ? 12 : 0);
            Image e = PhuKien.elip(rw, rh, mau, a > 120 ? 120 : a);
            if (e != null) {
                g.drawImage(e, x - rw, yChan - rh, Graphics.TOP | Graphics.LEFT);
            }
        }
        if (n.congTot >= 15) {
            PokeBody.veVien(g, dex, shiny, x, y, dir, dongTac, tick, n.bacTot >= 10 ? PhuKien.cauVong(tick / 12) : PhuKien.mauBac(n.bacTot, tick));
        }
        veLop(g, n, dex, shiny, x, y, dir, dongTac, tick, PhuKien.SAU);
    }

    public static void veTruoc(Graphics g, NgoaiHinhCua n, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick) {
        if (n == null) {
            return;
        }
        veLop(g, n, dex, shiny, x, y, dir, dongTac, tick, PhuKien.TRUOC);
        if (n.congTot < 13) {
            return;
        }
        int rw = banKinh(dex, shiny);
        int cao = PokeBody.caoTrenChan(dex, shiny, 100);
        int so = n.congTot >= 15 ? 2 : 1;
        int cb = n.bacTot >= 1 ? PhuKien.mauBac(n.bacTot, tick) : 0xFFF2A8;
        for (int i = 0; i < so; i++) {
            int t = tick + i * 23;
            int pha = t % 40;
            if (pha >= 28) {
                continue;   // nghi giua hai lan: thua thot
            }
            int ben = ((t / 40 + i) & 1) == 0 ? -1 : 1;
            int px = x + ben * (rw + 2);
            int py = y - 4 - pha * cao / 40;
            g.setColor(pha < 18 ? 0xFFFFFF : cb);
            g.drawLine(px - 1, py, px + 1, py);
            g.drawLine(px, py - 1, px, py + 1);
        }
    }

    /** Ve cac phu kien (toi da 2) cua lop SAU / TRUOC. */
    static void veLop(Graphics g, NgoaiHinhCua n, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick, int lop) {
        if (!PhuKien.nap() || !PokeBody.neo(dex, shiny, dir, dongTac, tick, 100, NEO)) {
            return;
        }
        int k = heSo(dex, shiny);
        int rong = PokeBody.rongHinh(dex, shiny);
        int hai = monThuHai(n, dex);
        // sau than: mon thu hai (neu thuoc lop sau) truoc, roi mu (khong bao gio sau than)
        if (hai >= 0) {
            veO(g, n, hai, x, y, dir, k, rong, tick, lop);
        }
        if (n.kieu[NgoaiHinhCua.MU] > 0 && !biCam(dex, NgoaiHinhCua.MU)) {
            veO(g, n, NgoaiHinhCua.MU, x, y, dir, k, rong, tick, lop);
        }
    }

    static void veO(Graphics g, NgoaiHinhCua n, int o, int x, int y, int dir, int k, int rong, int tick, int lop) {
        int kieu = n.kieu[o];
        int huong = dir == 0 ? 0 : dir == 1 ? 1 : 2;
        boolean lat = dir == 3;
        boolean co = o == NgoaiHinhCua.KHAN || o == NgoaiHinhCua.VONG;
        // do o co: be ngang toi da ~1/3 than; mu: toi da ~1/2
        int toiDa = co ? rong * 36 / 100 : rong * 55 / 100;
        PhuKien.Hinh h = PhuKien.hinh(o, kieu, huong, false);
        if (h != null && (h.rong - 2) * k > toiDa) {
            h = PhuKien.hinh(o, kieu, huong, true);
            if (h != null && (h.rong - 2) * k > toiDa && co) {
                h = null;
            }
        }
        if (h == null) {
            return;
        }
        int l = h.lop;
        if (o == NgoaiHinhCua.TAY && (NEO[7] & 2) != 0) {
            l = PhuKien.SAU;
        }
        if (l != lop) {
            return;
        }
        int px, py;
        switch (h.neo) {
            case PhuKien.N_DINH:
                px = NEO[0];
                py = NEO[2];
                break;
            case PhuKien.N_DAU:
                px = NEO[0];
                py = NEO[1];
                break;
            case PhuKien.N_TAY:
                px = NEO[3];
                py = NEO[4];
                break;
            case PhuKien.N_TAM:
                px = NEO[5];
                py = NEO[6];
                break;
            default: {
                // co (ngay duoi cam): trung binh cua "ngay duoi diem dau PMD" va "giua dinh dau -> chan" - diem dau
                // PMD lech theo loai (luc o mat, luc o mieng), dinh dau thi on dinh; lech ve tam than theo chieu ngang
                px = NEO[0] + (NEO[5] - NEO[0]) / 2;
                py = (NEO[1] + k + NEO[2] / 2) / 2;
                if (py < NEO[2] + 4 * k) {
                    py = NEO[2] + 4 * k;
                }
                if (py > NEO[6] + 2 * k) {
                    py = NEO[6] + 2 * k;
                }
                if (co && py > -3 * k) {
                    return;   // co sat mat dat: loai qua thap, khong ve
                }
                break;
            }
        }
        int dx = h.dx * k, dy = h.dy * k;
        if (lat) {
            dx = -dx;
        }
        int ax = lat ? h.rong - 1 - h.ax : h.ax;
        Image img = PhuKien.anh(h, n.hiem[o], lat, k, (tick / 8) & 3);
        if (img == null) {
            return;
        }
        g.drawImage(img, x + px + dx - ax * k, y + py + dy - h.ay * k, Graphics.TOP | Graphics.LEFT);
    }

    /** Thu tu huong xoay tren the trang bi: xuong, phai, len, trai. */
    private static final int[] XOAY = {0, 3, 1, 2};

    /** Huong ve Pokemon tren the trang bi (doi moi 1,5 giay) - xem trang bi tung phia. */
    public static int huongThe(int tick) {
        return XOAY[(tick / 38) & 3];
    }

    /**
     * The tom tat kieu Pokemon (xem trang bi nguoi khac): dai thong tin tren cung o giua bang Trang bi - bac tot
     * nhat (o mau bac), +N cao nhat, bo trang bi, trang phuc. cx = tam ngang, y = mep tren.
     */
    public static void veThe(Graphics g, NgoaiHinhCua n, int cx, int y) {
        if (n == null) {
            return;
        }
        game.render.screen.Font f = game.render.screen.Font.smallFont;
        if (f == null) {
            return;
        }
        String dong1 = n.bacTot >= 0 ? PhuKien.tenBac(n.bacTot) + (n.congTot > 0 ? " +" + n.congTot : "") : "Chưa mặc";
        String dong2 = n.bo >= 4 ? "Bộ " + n.bo + "/11" : "";
        if (n.trangPhuc > 0) {
            dong2 = dong2.length() > 0 ? dong2 + " · " + PhuKien.tenTrangPhuc(n.trangPhuc) : PhuKien.tenTrangPhuc(n.trangPhuc);
        }
        int w = f.getWidth(dong1) + 14;
        int w2 = dong2.length() > 0 ? f.getWidth(dong2) + 8 : 0;
        if (w2 > w) {
            w = w2;
        }
        int h = dong2.length() > 0 ? 20 : 11;
        GiaoDien.khoi(g, cx - w / 2, y, w, h, 3, 0x0B1222);
        int mau = n.bacTot >= 0 ? PhuKien.mauBac(n.bacTot, game.render.Canvas.gameTick) : 0x7080A0;
        g.setColor(mau);
        g.fillRect(cx - w / 2 + 3, y + 3, 5, 5);
        f.drawString(g, dong1, cx + 4, y, game.render.screen.Font.CENTER);
        if (dong2.length() > 0) {
            f.drawString(g, dong2, cx, y + 9, game.render.screen.Font.CENTER);
        }
    }
}
