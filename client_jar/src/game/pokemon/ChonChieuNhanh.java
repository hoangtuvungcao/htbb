package game.pokemon;

import game.model.Char;
import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.model.QuickSlot;
import game.model.skill.SkillManager;
import game.render.Canvas;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.RMS;
import javax.microedition.lcdui.Graphics;

/**
 * Bảng GÁN CHIÊU vào thanh ô nhanh (mọi bản: J2ME, PC, Android).
 *
 * - Chạm ô trống (dấu +) trên thanh ô nhanh -> bảng liệt kê chiêu đã học (hình chiêu màu hệ, tên, PP, hồi chiêu,
 *   cấp); chạm một chiêu = gán vào ô đó, lưu RMS ngay.
 * - Giữ lâu một ô (>= OChieuNhanh.GIU_LAU_MS) -> cùng bảng + nút "Bỏ trống" và "Đổi chỗ" (sang ô trái / phải).
 * - Thanh ô nhanh chỉ lưu ở máy (RMS.saveQuickSlot), server không lưu -> không gửi gói tin.
 *
 * Bàn phím (J2ME): lên / xuống / trái / phải chọn, phím giữa / phím mềm trái = gán, phím mềm phải = đóng.
 * Gọi: chamO(o, giuLau) từ GameScr.updatePoint (thanh ô của jar) hoặc Hud (Android HD).
 */
public final class ChonChieuNhanh extends Dialog {
    private static ChonChieuNhanh me;
    /** giữ ô của thanh jar: ô đang nhấn + lúc nhấn (đo giữ lâu khi thả) */
    private static int oNhan = -1;
    private static long lucNhan;

    private int o;
    private boolean coChinh;
    private int[] ds = new int[0];
    private int chon;
    private int cuon;
    private int[] bc;
    /** 0.9.11: đang kéo núm thanh cuộn (hàng đầu lúc nhấn, py lúc nhấn) */
    private boolean keoNum;
    private int cuonNumDau, pyNumDau;
    private int lop;

    private ChonChieuNhanh() {
        this.left = new Command("Gán", new IAction() {
            public void perform() {
                ganChon();
            }
        });
        this.center = new Command("", this.left.action);
        this.right = new Command("Đóng", new IAction() {
            public void perform() {
                dong();
            }
        });
    }

    public static ChonChieuNhanh gI() {
        if (me == null) {
            me = new ChonChieuNhanh();
        }
        return me;
    }

    public static boolean dangMo() {
        return me != null && Canvas.currentDialog == me;
    }

    /**
     * Chạm (đã thả) ô 'o' của bộ ô đang dùng. Ô trống -> mở bảng gán, true. giuLau -> mở bảng chỉnh ô, true.
     * false = ô có chiêu / thuốc, chạm thường (người gọi dùng ô như cũ).
     */
    public static boolean chamO(int o, boolean giuLau) {
        if (o < 0 || o >= OChieuNhanh.SO_O || MainChar.quickSlot == null || Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return false;
        }
        QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][o];
        boolean trong = qs == null || qs.quickslotType < 0;
        if (!trong && !giuLau) {
            return false;
        }
        gI().mo(o, !trong);
        return true;
    }

    /**
     * Thanh ô của jar (GameScr.updatePoint, mỗi nhịp có con trỏ): cùng vùng chạm với GameScr (5 ô bề ngang buoc,
     * cao hSlot, đáy màn - 20). Nhấn giữ trong vùng: ghi ô + lúc nhấn; thả: ô trống / giữ lâu -> mở bảng.
     * true = đã xử lý cú chạm -> người gọi bỏ qua (không dùng phím ô).
     */
    public static boolean thanhJar(int buoc, int hSlot) {
        GameScr gs = Canvas.gameScr;
        // thanh đang ẩn / đang chat / đang nhắm NPC (vùng thanh là nút "Giao tiếp"): để GameScr xử lý như cũ
        boolean anThanh = gs == null || gs.hideGUI == 2 || gs.chatMode || Canvas.currentDialog != null
                || (gs.focusedActor != null && gs.focusedActor.catagory == 2);
        int wSlot = buoc * OChieuNhanh.SO_O;
        int x0 = Canvas.hw - (wSlot / 2), y0 = (Canvas.h - hSlot) - 20;
        if (anThanh || buoc <= 0 || !Canvas.isPointer(x0, y0, wSlot, hSlot)) {
            if (!Canvas.isPointerDown) {
                oNhan = -1;
            }
            return false;
        }
        int o = Math.min(OChieuNhanh.SO_O - 1, (Canvas.px - x0) / buoc);
        return oThanh(o);
    }

    static boolean oThanh(int o) {
        long now = System.currentTimeMillis();
        if (Canvas.isPointerDown && !Canvas.isPointerClick) {
            if (oNhan != o) {
                oNhan = o;
                lucNhan = now;
            }
            return false;
        }
        if (!Canvas.isPointerClick) {
            return false;
        }
        boolean giu = oNhan == o && now - lucNhan >= OChieuNhanh.GIU_LAU_MS;
        oNhan = -1;
        return chamO(o, giu);
    }

    /** Mở bảng gán / chỉnh ô nhanh o (màn Nhân vật -> Kỹ năng: một chạm). */
    public static void moO(int o) {
        if (o < 0 || o >= OChieuNhanh.SO_O || MainChar.quickSlot == null || Canvas.gameScr == null || Canvas.gameScr.mainChar == null) {
            return;
        }
        QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][o];
        gI().mo(o, qs != null && qs.quickslotType >= 0);
    }

    private void mo(int o, boolean coChinh) {
        this.o = o;
        this.coChinh = coChinh;
        MainChar mc = Canvas.gameScr.mainChar;
        this.lop = mc.clazz;
        int soChieu = this.lop >= 0 && this.lop < PokeChieu.TEN.length ? PokeChieu.TEN[this.lop].length : 0;
        this.ds = OChieuNhanh.ganDuoc(this.lop, Char.skillLevelLearnt, SkillManager.SKILL_CAN_BUFF_TO_USER, soChieu);
        this.chon = 0;
        QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][o];
        for (int i = 0; i < this.ds.length; i++) {
            if (qs != null && qs.quickslotType == QuickSlot.TYPE_SKILL && qs.skillType == this.ds[i]) {
                this.chon = i;
            }
        }
        this.cuon = 0;
        this.bc = null;
        this.anNutLenh = moi();   // bảng mới: X + chạm hàng thay "Gán" / "Đóng"
        Canvas.isPointerClick = false;
        Canvas.isPointerDown = false;
        AmThanh.phat(AmThanh.MO);
        show();
    }

    /**
     * Bảng mới (vùng chạm VungCham, nút khối NRO): máy có chữ thật (PC / APK) HOẶC máy cảm ứng (J2ME cảm ứng, J2ME
     * Loader) - 0.9.7: trước chỉ theo chữ thật nên J2ME cảm ứng rơi vào bảng cũ chỉ dùng phím. Máy chỉ phím: bảng cũ.
     */
    static boolean moi() {
        return ChuUI.bo != null || Canvas.instance != null && Canvas.instance.hasPointerEvents();
    }

    private void dong() {
        if (Canvas.currentDialog == this) {
            Canvas.endDlg();
        }
        AmThanh.phat(AmThanh.HUY);
    }

    private void ganChon() {
        if (this.chon >= 0 && this.chon < this.ds.length) {
            gan(this.ds[this.chon]);
        }
    }

    private void gan(int sk) {
        QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][this.o];
        qs.setIsSkill(sk, this.lop, OChieuNhanh.laBuff(this.lop, sk));
        RMS.saveQuickSlot();
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.endDlg();
    }

    private void boTrong() {
        MainChar.quickSlot[GameScr.indexTabSlot][this.o].setIsNothing();
        RMS.saveQuickSlot();
        AmThanh.phat(AmThanh.XAC_NHAN);
        Canvas.endDlg();
    }

    /** Đổi chỗ ô đang chỉnh với ô bên cạnh (huong -1 trái / +1 phải); bảng theo ô vừa dời. */
    private void doiCho(int huong) {
        int b = this.o + huong;
        if (b < 0 || b >= OChieuNhanh.SO_O) {
            return;
        }
        OChieuNhanh.doiCho(MainChar.quickSlot[GameScr.indexTabSlot], this.o, b);
        this.o = b;
        RMS.saveQuickSlot();
        AmThanh.phat(AmThanh.XAC_NHAN);
    }

    private String[] nutChan() {
        if (moi()) {   // bảng mới: đóng bằng X trên đầu (không lặp nút "Đóng")
            return this.coChinh ? new String[]{"Bỏ trống", "Dời sang trái", "Dời sang phải"} : new String[0];
        }
        return this.coChinh ? new String[]{"Bỏ trống", "< Dời trái", "Dời phải >", "Đóng"} : new String[]{"Đóng"};
    }

    private void bamNutChan(int i) {
        if (!this.coChinh) {
            dong();
            return;
        }
        switch (i) {
            case 0:
                boTrong();
                break;
            case 1:
                doiCho(-1);
                break;
            case 2:
                doiCho(1);
                break;
            default:
                dong();
                break;
        }
    }

    public void update() {
        int n = this.ds.length;
        this.bc = OChieuNhanh.boCuc(Canvas.w, Canvas.h, n);
        int cot = this.bc[4];
        int hang = this.bc[5];
        if (n > 0) {
            if (Canvas.isKeyPressed(2)) {
                this.chon = Math.max(0, this.chon - cot);
            } else if (Canvas.isKeyPressed(8)) {
                this.chon = Math.min(n - 1, this.chon + cot);
            } else if (Canvas.isKeyPressed(4)) {
                this.chon = Math.max(0, this.chon - 1);
            } else if (Canvas.isKeyPressed(6)) {
                this.chon = Math.min(n - 1, this.chon + 1);
            }
            int r = this.chon / cot;
            if (r < this.cuon) {
                this.cuon = r;
            } else if (r >= this.cuon + hang) {
                this.cuon = r - hang + 1;
            }
        }
        if (keoThanh(n, hang)) {
            super.update();
            return;
        }
        if (Canvas.isPointerClick && Math.abs(Canvas.pyLast - Canvas.py) <= 10 && Math.abs(Canvas.pxLast - Canvas.px) <= 10) {
            int px = Canvas.px, py = Canvas.py;
            String[] nut = nutChan();
            for (int i = 0; i < nut.length; i++) {
                if (OChieuNhanh.trong(OChieuNhanh.nutChan(this.bc, i, nut.length), px, py)) {
                    Canvas.isPointerClick = false;
                    bamNutChan(i);
                    return;
                }
            }
            int i = OChieuNhanh.chiSoTai(this.bc, n, this.cuon, px, py);
            if (i >= 0) {
                Canvas.isPointerClick = false;
                this.chon = i;
                gan(this.ds[i]);
                return;
            }
            boolean trongBang = px >= this.bc[0] && px < this.bc[0] + this.bc[2] && py >= this.bc[1] && py < this.bc[1] + this.bc[3];
            if (!trongBang && Canvas.collisionCmdBar() < 0) {
                Canvas.isPointerClick = false;
                dong();   // chạm ngoài bảng = đóng
                return;
            }
        } else if (Canvas.isPointerClick && n > 0) {
            // vuốt dọc trong danh sách: cuộn
            int d = (Canvas.pyLast - Canvas.py) / Math.max(1, this.bc[6]);
            this.cuon = Math.max(0, Math.min(OChieuNhanh.cuonToiDa(this.bc, n), this.cuon + d));
            Canvas.isPointerClick = false;
        }
        super.update();
    }

    /** Thanh cuộn của lưới (như paint): {x, rộng, y rãnh, cao rãnh, cao núm}; null = không có thanh. */
    int[] thanh(int n, int hang) {
        int cmax = OChieuNhanh.cuonToiDa(this.bc, n);
        if (cmax <= 0) {
            return null;
        }
        int hThanh = hang * this.bc[6];
        return new int[]{this.bc[0] + this.bc[2] - 6, 3, this.bc[7], hThanh, Math.max(8, hThanh * hang / (hang + cmax))};
    }

    /**
     * 0.9.11: kéo NÚM thanh cuộn - núm theo con trỏ, hàng theo tỉ lệ cùng chiều (trước: cú kéo trên thanh bị coi như
     * vuốt nội dung = ngược chiều); chạm rãnh = nhảy tới chỗ chạm. Trả true khi cú nhấn / thả thuộc về thanh.
     */
    boolean keoThanh(int n, int hang) {
        int[] t = thanh(n, hang);
        int cmax = OChieuNhanh.cuonToiDa(this.bc, n);
        if (this.keoNum) {
            if (t != null && (Canvas.dangGiu || Canvas.isPointerDown)) {
                this.cuon = ThanhCuon.keo(this.cuonNumDau, this.pyNumDau, Canvas.py, t[3], t[4], cmax);
                return true;
            }
            this.keoNum = false;
            Canvas.isPointerClick = false;   // cú thả của lượt kéo núm: không chọn hàng / không vuốt
            return true;
        }
        if (t != null && (Canvas.dangGiu || Canvas.isPointerDown) && ThanhCuon.trung(Canvas.pxLast, Canvas.pyLast, t[0], t[1], t[2], t[3])) {
            int c = this.cuon;
            if (!ThanhCuon.trenNum(Canvas.pyLast, t[2], t[3], t[4], c, cmax)) {
                c = ThanhCuon.toi(Canvas.pyLast, t[2], t[3], t[4], cmax);
            }
            this.keoNum = true;
            this.cuonNumDau = c;
            this.pyNumDau = Canvas.pyLast;
            this.cuon = ThanhCuon.keo(c, this.pyNumDau, Canvas.py, t[3], t[4], cmax);
            return true;
        }
        return false;
    }

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        int n = this.ds.length;
        if (this.bc == null) {
            this.bc = OChieuNhanh.boCuc(Canvas.w, Canvas.h, n);
        }
        if (moi()) {
            veMoi(g, n);
            return;
        }
        int x = this.bc[0], y = this.bc[1], w = this.bc[2], h = this.bc[3];
        int cot = this.bc[4], hang = this.bc[5], hHang = this.bc[6], yDau = this.bc[7];
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, 0x90050B18);
        GiaoDien.bang(g, x, y, w, h);
        GiaoDien.dai(g, x + 4, y + 4, w - 8, 20, (this.coChinh ? "Chỉnh ô " : "Gán chiêu vào ô ") + (this.o + 1));
        Font f = Font.blackFont;
        MainChar mc = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        if (n == 0 || mc == null) {
            f.drawString(g, "Chưa học chiêu nào để gán.", x + w / 2, yDau + 6, Font.CENTER);
        } else {
            int wCot = (w - 12) / cot;
            long now = System.currentTimeMillis();
            QuickSlot hienTai = MainChar.quickSlot[GameScr.indexTabSlot][this.o];
            for (int r = 0; r < hang; r++) {
                for (int c = 0; c < cot; c++) {
                    int i = (r + this.cuon) * cot + c;
                    if (i >= n) {
                        break;
                    }
                    int sk = this.ds[i];
                    int rx = x + 6 + c * wCot, ry = yDau + r * hHang;
                    Canvas.resetTrans(g);
                    boolean dangGan = hienTai != null && hienTai.quickslotType == QuickSlot.TYPE_SKILL && hienTai.skillType == sk;
                    if (i == this.chon) {
                        GiaoDien.chonSang(g, rx, ry + 1, wCot - 2, hHang - 2);
                    } else {
                        GiaoDien.khoi(g, rx, ry + 1, wCot - 2, hHang - 2, 4, (r & 1) == 0 ? GiaoDien.HANG_CHAN : GiaoDien.HANG_LE);
                    }
                    int oc = hHang - 6;
                    KieuO.oChieuSk(g, rx + 3, ry + 3, oc, oc, this.lop, sk, false);
                    String ten = PokeChieu.ten(this.lop, sk);
                    if (ten == null) {
                        ten = "Chiêu " + sk;
                    }
                    int cap = Char.skillLevelLearnt != null && sk < Char.skillLevelLearnt.length ? Char.skillLevelLearnt[sk] : 0;
                    int pp = 0;
                    try {
                        pp = SkillManager.ppAnToan(sk, cap, this.lop);
                    } catch (Throwable e) {
                        pp = 0;
                    }
                    long hoi = 0;
                    try {
                        hoi = SkillManager.getSkillCooldown((byte) sk, (byte) this.lop, cap);
                    } catch (Throwable e) {
                        hoi = 0;
                    }
                    long con = 0;
                    if (mc.timeLastUseSkills != null && sk < mc.timeLastUseSkills.length && sk < mc.coolDownSkill.length) {
                        con = mc.coolDownSkill[sk] - (now - mc.timeLastUseSkills[sk]);
                    }
                    int tx = rx + oc + 8;
                    int wChu = wCot - oc - 14;
                    String phu = "PP " + pp + "  Hồi " + OChieuNhanh.giay(hoi) + "  Cấp " + cap + (con > 0 ? "  (" + OChieuNhanh.giay(con) + ")" : "");
                    if (hHang >= 30) {
                        f.drawString(g, GiaoDien.catChu(f, (dangGan ? "* " : "") + ten, wChu), tx, ry + 2, 0);
                        f.drawString(g, GiaoDien.catChu(f, phu, wChu), tx, ry + 15, 0);
                    } else {
                        f.drawString(g, GiaoDien.catChu(f, ten + "  " + phu, wChu), tx, ry + 5, 0);
                    }
                }
            }
            int cmax = OChieuNhanh.cuonToiDa(this.bc, n);
            if (cmax > 0) {
                Canvas.resetTrans(g);
                int hThanh = hang * hHang;
                int hNut = Math.max(8, hThanh * hang / (hang + cmax));
                int yNut = yDau + (hThanh - hNut) * this.cuon / cmax;
                g.setColor(GiaoDien.XANH_NHAT);
                g.fillRect(x + w - 6, yDau, 3, hThanh);
                g.setColor(GiaoDien.XANH);
                g.fillRect(x + w - 6, yNut, 3, hNut);
            }
        }
        String[] nut = nutChan();
        for (int i = 0; i < nut.length; i++) {
            Canvas.resetTrans(g);
            int[] r = OChieuNhanh.nutChan(this.bc, i, nut.length);
            boolean khoa = this.coChinh && ((i == 1 && this.o == 0) || (i == 2 && this.o == OChieuNhanh.SO_O - 1));
            int mau = i == nut.length - 1 ? GiaoDien.DO : khoa ? GiaoDien.XAM : GiaoDien.XANH;
            GiaoDien.khoi(g, r[0], r[1], r[2], r[3], 6, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, r[0] + 1, r[1] + 1, r[2] - 2, r[3] - 2, 5, GiaoDien.pha(mau, GiaoDien.TRANG, 1, 3), mau);
            Font.borderFont.drawString(g, GiaoDien.catChu(Font.borderFont, nut[i], r[2] - 6), r[0] + r[2] / 2, r[1] + (r[3] - Font.borderFont.getHeight()) / 2, Font.CENTER);
        }
        Font.nenSang = false;
        super.paint(g);
    }

    /** 0.9.5 PC / APK: bảng sáng chung (đầu đỏ mảnh + X), hàng thẻ chạm được, icon chiêu PNG, nút phẳng. */
    private void veMoi(Graphics g, int n) {
        int x = this.bc[0], y = this.bc[1], w = this.bc[2], h = this.bc[3];
        int cot = this.bc[4], hang = this.bc[5], hHang = this.bc[6], yDau = this.bc[7];
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, ChuDe.PHU_NEN);
        Bang.khung(g, x, y, w, h, (this.coChinh ? "Chỉnh ô " : "Gán chiêu vào ô ") + (this.o + 1), null, 0, null, new IAction() {
            public void perform() {
                dong();
            }
        });
        MainChar mc = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        if (n == 0 || mc == null) {
            ChuDe.chuGiua(g, "Chưa học chiêu nào để gán.", x + w / 2, yDau, hHang, ChuDe.THUONG, ChuDe.CHU_PHU, 2);
        } else {
            int wCot = (w - 12) / cot;
            long now = System.currentTimeMillis();
            QuickSlot hienTai = MainChar.quickSlot[GameScr.indexTabSlot][this.o];
            int k = Math.max(2, Bang.cach());
            for (int r = 0; r < hang; r++) {
                for (int c = 0; c < cot; c++) {
                    final int i = (r + this.cuon) * cot + c;
                    if (i >= n) {
                        break;
                    }
                    final int sk = this.ds[i];
                    int rx = x + 6 + c * wCot, ry = yDau + r * hHang;
                    boolean dangGan = hienTai != null && hienTai.quickslotType == QuickSlot.TYPE_SKILL && hienTai.skillType == sk;
                    Bang.hang(g, rx + k / 2, ry + k / 2, wCot - k, hHang - k, r, i == this.chon, new IAction() {
                        public void perform() {
                            ChonChieuNhanh.this.chon = i;
                            gan(sk);
                        }
                    }, "chon-chieu-" + i);
                    int oc = hHang - k - 8;
                    KieuO.oChieuSk(g, rx + k / 2 + 4, ry + k / 2 + 4, oc, oc, this.lop, sk, false);
                    String ten = PokeChieu.ten(this.lop, sk);
                    if (ten == null) {
                        ten = "Chiêu " + (sk + 1);
                    }
                    int cap = Char.skillLevelLearnt != null && sk < Char.skillLevelLearnt.length ? Char.skillLevelLearnt[sk] : 0;
                    int pp = 0;
                    long hoi = 0;
                    try {
                        pp = SkillManager.getSkillMP(sk, cap, this.lop);
                        hoi = SkillManager.getSkillCooldown((byte) sk, (byte) this.lop, cap);
                    } catch (Throwable e) {
                        // bảng chiêu chưa nạp
                    }
                    long con = 0;
                    if (mc.timeLastUseSkills != null && sk < mc.timeLastUseSkills.length && sk < mc.coolDownSkill.length) {
                        con = mc.coolDownSkill[sk] - (now - mc.timeLastUseSkills[sk]);
                    }
                    int tx = rx + k / 2 + oc + 10;
                    int wChu = wCot - k - oc - 16;
                    String nhan = dangGan ? "Đang ở ô này" : null;
                    int wNhan = nhan == null ? 0 : ChuDe.rong(nhan, ChuDe.NHAN_NHO) + 6;
                    ChuDe.chu(g, ChuDe.cat(ten, ChuDe.NHAN, wChu - wNhan), tx, ry + k / 2 + 3, ChuDe.NHAN, ChuDe.CHU, 0);
                    if (nhan != null) {
                        ChuDe.chu(g, nhan, rx + wCot - k / 2 - 6, ry + k / 2 + 4, ChuDe.NHAN_NHO, ChuDe.LA_TOI, 1);
                    }
                    StringBuffer b = new StringBuffer(KieuO.tenHe(PokeChieu.he(this.lop, sk))).append(" · Cấp ").append(cap);
                    if (pp > 0) {
                        b.append(" · PP ").append(pp);
                    }
                    if (hoi > 0) {
                        b.append(" · hồi ").append(OChieuNhanh.giay(hoi));
                    }
                    if (con > 0) {
                        b.append(" (còn ").append(OChieuNhanh.giay(con)).append(')');
                    }
                    ChuDe.chu(g, ChuDe.cat(b.toString(), ChuDe.PHU, wChu), tx, ry + k / 2 + 3 + ChuDe.cao(ChuDe.NHAN), ChuDe.PHU, ChuDe.CHU_PHU, 0);
                }
            }
            int cmax = OChieuNhanh.cuonToiDa(this.bc, n);
            if (cmax > 0) {
                int hThanh = hang * hHang;
                int hNut = Math.max(8, hThanh * hang / (hang + cmax));
                int yNut = yDau + (hThanh - hNut) * this.cuon / cmax;
                GiaoDien.khoi(g, x + w - 6, yDau, 3, hThanh, 1, ChuDe.KE);
                GiaoDien.khoi(g, x + w - 6, yNut, 3, hNut, 1, ChuDe.XANH);
            }
        }
        String[] nut = nutChan();
        for (int i = 0; i < nut.length; i++) {
            final int j = i;
            int[] r = OChieuNhanh.nutChan(this.bc, i, nut.length);
            boolean khoa = (i == 1 && this.o == 0) || (i == 2 && this.o == OChieuNhanh.SO_O - 1);
            Bang.nut(g, r[0], r[1], r[2], r[3], nut[i], khoa ? Bang.TAT : i == 0 ? Bang.TRUNG : Bang.PHU, new IAction() {
                public void perform() {
                    bamNutChan(j);
                }
            }, "chon-nut-" + i);
        }
    }
}
