import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Dung sprite THU CUOI theo he thong "thu cuoi tool" — chinh la he thong dang ve
 * con Rong Bang, Poro, Lan, Su Tu... va ca hoa hinh.
 *
 * Duong di: Animal.getIdImageTool() tra ve id dang 8700+N -> server gui trong goi
 * thong tin nhan vat -> client Char.setHorse(idhorse,...) -> Char.getPartEff(idhorse)
 * -> DataSkillEff. Du lieu frame duoc gui SAN o goi login (message 26, danh sach
 * MessageCreator.id_eff_tool_ngu_long) con anh thi client xin theo id luc can
 * (DataGame.sendByteImg). Vi vay he nay KHONG phu thuoc bo anh cache — khac han
 * duong imgHorse.
 *
 * Dinh dang file data (Client/DataSkillEff.loadData), khac DataEffect o cho moi
 * part co them 2 byte flip/onTop va so buoc sequence la 1 byte:
 *
 *   byte  soAnhCon
 *     moi anh con: 5 byte KHONG DAU = id, x, y, w, h   (nen moi so phai <= 255)
 *   short soFrame
 *     moi frame: byte soPart, roi moi part = short dx, short dy, byte idAnhCon,
 *                byte flip, byte onTop
 *   byte  soBuoc, roi moi buoc = short chi so frame
 *   byte  indexStartSkill
 *   byte n0 + n0 byte  -> frameChar[0]
 *   byte n1 + n1 byte  -> frameChar[1]
 *   byte n3 + n3 byte  -> frameChar[3]
 *
 * BO CUC 21 FRAME — day la cho de lam sai nhat. Client KHONG dung 18 frame ma
 * dung dung 21, chia 7 frame moi huong (Char.getFrameHorse()):
 *
 *   DOWN       -> Fhorse            (slot 0..6)
 *   UP         -> Fhorse + 7        (slot 7..13)
 *   LEFT/RIGHT -> Fhorse + 14       (slot 14..20)
 *
 * Trong moi huong, Fhorse chay theo hai bang cua Char:
 *   dung yen : STANDFRAME -> Fhorse = 0, 1
 *   di chuyen: horseMove {2,2,2,3,3,4,4,4} -> Fhorse = 2, 3, 4
 * Slot 5 va 6 khong bao gio duoc dung khi di/dung nen ta cho tro lai anh cua slot
 * 0 va 1 — Rong Bang lam dung y nhu vay. Neu chi ghi 18 frame thi huong ngang voi
 * Fhorse = 4 se doi frame 18 khong ton tai, paintTopHorse return luon, con thu bien
 * mat dung tick do -> NHAY LIEN TUC khi di chuyen.
 *
 * HUONG CUA ANH GOC: client lat ngang khi dir == RIGHT ((dir == RIGHT) ? 2 : 0),
 * nghia la anh luu san phai nhin ve BEN TRAI. Rong Bang dung dung the. Cac bang
 * sprite AI o day ve con thu nhin sang phai nen phai lat lai, khong thi con thu se
 * di lui — nguoc huong voi nhan vat.
 *
 *   javac -d out tools/CatSpriteSheet.java tools/TaoDataSprite.java tools/TaoEffThuCuoi.java
 *   java -cp out TaoEffThuCuoi <bang.png> <id d_effect> --xuong=N --len=N --ngang=N
 *        [--botrai=0] [--nguong=48] [--cao=60] [--lomdom=40] [--khonglat]
 *
 * Ghi ra map/d_effect/<id>.png + map/d_effect/<id>. Id phai nam trong
 * id_eff_tool_ngu_long va khong duoc trung id nao dang dung.
 */
public class TaoEffThuCuoi {

    /** So slot frame moi huong ma client mong doi. */
    private static final int SO_SLOT = 7;
    /** So anh that moi huong; slot 5, 6 dung lai anh cua slot 0, 1. */
    private static final int SO_GOC = 5;
    /** Toa do anh con luu bang byte khong dau nen atlas toi da 256x256. */
    private static final int ATLAS = 256;
    /**
     * Canh mot o trong luoi co dinh, 4 cot x 4 dong = 256x256: 15 o cho than con thu,
     * o thu 16 cho bong do.
     */
    private static final int O = 64;
    private static final int SO_COT = ATLAS / O;
    /** Chi so o chua bong do (o cuoi cung cua luoi). */
    private static final int O_BONG = 15;
    /**
     * So diem con thu tho xuong duoi toa do nhan vat: xuong, len, ngang.
     *
     * PHAI la hang so, khong duoc tinh theo chieu cao anh — xem giai thich o LUOI CO DINH.
     * Ba so nay lay tu do dac linh thu that: Rong Bang tho xuong +18 / +24 / +5, heo bang
     * +7 / +4 / +5.
     */
    private static final int[] THO_XUONG = {10, 8, 4};

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Dung: java TaoEffThuCuoi <bang.png> <id> --xuong=N --len=N "
                    + "--ngang=N [--botrai=0] [--nguong=48] [--cao=60] [--lomdom=40] [--khonglat]");
            return;
        }
        final File sheet = new File(args[0]);
        final int id = Integer.parseInt(args[1]);
        // nguong = -1: chi lay theo kenh trong suot. Cac bang sprite o day co nen
        // TRONG SUOT va con thu thi mau TOI, nen loc theo do sang (nguong 48) se cat
        // mat 28%-53% than va vien con thu, chi con lai nhung mang sang roi rac —
        // do la ly do skin nhin "mat net khong ro". Chi dat nguong duong khi bang
        // sprite co nen den dac.
        int hXuong = -1, hLen = -1, hNgang = -1, boTrai = 0, nguong = -1, caoDich = 60, lomDom = 40;
        // So o that su cua tung hang. Khi lay du ca phan toi cua con thu thi cac frame
        // trong mot hang dinh vao nhau (bong, trang tri cham nhau) nen do khe trong de
        // tach se ra sai so frame; chi dinh tay thi chia deu, chac an voi bang ke luoi.
        final int[] soCot = {0, 0, 0};
        // Do dam vien: sprite goc trong game deu co vien toi nen doc ro o co nho, con
        // anh AI thu nho thi vien mem, nhin nhoe. 0 = khong ve vien.
        double damVien = 0.45;
        boolean lat = true;
        for (int i = 2; i < args.length; i++) {
            final String a = args[i];
            if (a.startsWith("--xuong=")) hXuong = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--len=")) hLen = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--ngang=")) hNgang = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--botrai=")) boTrai = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--nguong=")) nguong = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--cao=")) caoDich = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--lomdom=")) lomDom = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--cotxuong=")) soCot[0] = Integer.parseInt(a.substring(11));
            else if (a.startsWith("--cotlen=")) soCot[1] = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--cotngang=")) soCot[2] = Integer.parseInt(a.substring(11));
            else if (a.startsWith("--vien=")) damVien = Double.parseDouble(a.substring(7));
            else if (a.equals("--khonglat")) lat = false;
        }
        if (hXuong < 0 || hLen < 0 || hNgang < 0) {
            System.out.println("Thieu --xuong / --len / --ngang");
            return;
        }

        final BufferedImage goc = ImageIO.read(sheet);
        if (goc == null) {
            System.out.println("Khong doc duoc anh: " + sheet);
            return;
        }
        final boolean[][] coHinh = CatSpriteSheet.locNen(goc, nguong, boTrai, 0);
        final int daXoa = boLomDom(coHinh, lomDom);
        if (daXoa > 0) {
            System.out.println("  xoa " + daXoa + " dom nhieu (< " + lomDom + " diem)");
        }
        final List<int[]> hangs = CatSpriteSheet.timDai(coHinh, true);

        // Lay khung bao cua 5 frame goc cho tung huong, kem khung bao CHUNG cua ca
        // huong. Khung chung la moc: nho no ma tung frame giu nguyen do lech tu nhien
        // trong bang goc, khong bi keo ve giua o -> khong rung lac giua cac frame.
        final int[] hang = {hXuong, hLen, hNgang};
        final int[][][] khung = new int[3][SO_GOC][];
        final boolean[][][][] mat = new boolean[3][SO_GOC][][];
        final int[] mocY = new int[3];
        /** true = hang khong co nhip nhun san, phai tu them. */
        final boolean[] nhun = new boolean[3];
        int caoNhat = 0;
        for (int nhom = 0; nhom < 3; nhom++) {
            if (hang[nhom] >= hangs.size()) {
                System.out.println("Khong co hang " + hang[nhom] + " (bang chi co "
                        + hangs.size() + " hang)");
                return;
            }
            final int[] h = hangs.get(hang[nhom]);
            final List<int[]> cot = CatSpriteSheet.timCot(coHinh, h[0], h[1], -1, soCot[nhom]);
            if (cot.isEmpty()) {
                System.out.println("Hang " + hang[nhom] + " khong co frame");
                return;
            }
            // Moc NGANG lay tam hinh cua tung frame — nhu vay dx = -w/2, dung y cong
            // thuc client dung khi lat anh cho huong phai (num = -(dx + w)) nen con
            // thu dung dung cho ca hai ben. Moc DOC thi lay chung ca hang: moi frame
            // trong hang dung tren cung mot vach dat, nho vay giu duoc nhip nhun len
            // xuong tu nhien ma khong bi giat.
            final int soO = cot.size();
            int cy2 = -1;
            for (int f = 0; f < SO_GOC; f++) {
                // Lay deu SO_GOC frame trong hang de khong cat mat cuoi vong lap.
                final int[] c = cot.get(f * soO / SO_GOC);
                final boolean[][] rieng = locConThu(coHinh, c[0], c[1], h[0], h[1]);
                final int[] k = CatSpriteSheet.khungBao(rieng, c[0], c[1], h[0], h[1]);
                if (k == null) continue;
                khung[nhom][f] = k;
                mat[nhom][f] = rieng;
                cy2 = Math.max(cy2, k[3]);
                caoNhat = Math.max(caoNhat, k[3] - k[1] + 1);
            }
            if (cy2 < 0) {
                System.out.println("Hang " + hang[nhom] + " khong lay duoc frame nao");
                return;
            }
            mocY[nhom] = cy2;
            System.out.println("  nhom " + nhom + " (hang " + hang[nhom] + "): "
                    + soO + " frame nguon, vach dat y=" + cy2);
        }
        if (caoNhat == 0) {
            System.out.println("Khong lay duoc frame nao");
            return;
        }

        // MOT co chuan cho CA 5 skin: thu nho sao cho frame cao nhat dung bang --cao.
        // Frame nao vuot o 64x64 thi ha ti le cho vua.
        double tiLe = (double) caoDich / caoNhat;
        BufferedImage[] anh;
        while (true) {
            anh = new BufferedImage[3 * SO_GOC];
            int vuot = 0;
            for (int nhom = 0; nhom < 3; nhom++) {
                for (int f = 0; f < SO_GOC; f++) {
                    final int[] k = khung[nhom][f];
                    if (k == null) continue;
                    BufferedImage nho = CatSpriteSheet.thuNho(goc, mat[nhom][f], k, tiLe);
                    // Anh goc nhin sang phai; client chi lat khi dir == RIGHT nen anh
                    // luu san phai nhin sang trai.
                    if (lat && nhom == 2) nho = latNgang(nho);
                    if (damVien > 0) veVien(nho, damVien);
                    if (nho.getWidth() > O || nho.getHeight() > O) vuot++;
                    anh[nhom * SO_GOC + f] = nho;
                }
            }
            if (vuot == 0) break;
            tiLe *= 0.96;
            if (tiLe < 0.02) {
                System.out.println("Khong nhet noi vao o " + O + "x" + O);
                return;
            }
        }

        int caoThuc = 0, rongThuc = 0;
        for (final BufferedImage a : anh) {
            if (a == null) continue;
            caoThuc = Math.max(caoThuc, a.getHeight());
            rongThuc = Math.max(rongThuc, a.getWidth());
        }

        // ================= LUOI CO DINH =================
        // Anh con KHONG duoc xep khit theo kich thuoc that, ma moi anh chiem tron mot o
        // 64x64 co dinh tren luoi 4x4, va dx/dy la hang so. Nho vay file du lieu frame
        // GIONG NHAU TUNG BYTE giua ca 5 skin va giua moi lan lam lai sprite.
        //
        // Ly do: client co ba tang dem voi ba luat xoa khac nhau —
        //   ALL_DATA_EFF (byte du lieu)  ghi lai moi lan dang nhap  -> luon moi
        //   ALL_PART_EFF (da phan tich)  xoa khi ranh qua 200s      -> co the con cu
        //   listImgIcon  (anh png)       xoa khi bang > 100 muc VA ranh qua 120s
        // Truoc day moi lan doi sprite la doi luon kich thuoc va vi tri o, nen neu client
        // con giu ALL_PART_EFF cu thi no cat mot vung khac han tren anh moi -> hien ra hai
        // ba con chong len nhau trong mot khoi chu nhat. Voi luoi co dinh thi du lieu cu va
        // moi khong khac gi nhau, tang dem do het gay hai vinh vien; con tang anh thi may
        // chu tu day xuong (DataGame.taoGoiAnhDynamicEffect).
        //
        // Gia phai tra: con thu bi gioi han trong 64x64, va client blit tron o nen phan
        // trong suot cung duoc ve — khong dang ke.
        // Xem tung huong co nhip nhun san khong. PHAI do sau khi biet ti le: chenh lech
        // do o anh goc con phai nhan voi ti le ~0.33 moi ra so diem that, nen 2 diem goc
        // chi con 0.66 diem — chua thanh nhip.
        for (int nhom = 0; nhom < 3; nhom++) {
            int thap = Integer.MAX_VALUE, cao = -1;
            for (int f = 0; f < SO_GOC; f++) {
                if (khung[nhom][f] == null) continue;
                final int v = (int) Math.round((mocY[nhom] - khung[nhom][f][3]) * tiLe);
                thap = Math.min(thap, v);
                cao = Math.max(cao, v);
            }
            nhun[nhom] = cao - thap < 1;
            System.out.println("  huong " + nhom + (nhun[nhom] ? ": tu them nhip nhun"
                    : ": co nhip nhun san " + (cao - thap) + " diem"));
        }

        final BufferedImage atlas = new BufferedImage(ATLAS, ATLAS, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = atlas.createGraphics();
        for (int i = 0; i < anh.length; i++) {
            if (anh[i] == null) continue;
            final int nhom = i / SO_GOC, f = i % SO_GOC;
            final int ox = (i % SO_COT) * O, oy = (i / SO_COT) * O;
            // Can giua o theo be ngang. Theo chieu doc: vach dat cua hang nam dung o day
            // o, con tung frame giu nguyen do nhun len xuong tu nhien cua no.
            int caoTrenVach = (int) Math.round((mocY[nhom] - khung[nhom][f][3]) * tiLe);
            // Bao dam co nhip nhun: linh thu that nhun than 1 diem so le tung frame (heo
            // bang dy = -35, -34, -35, -34, -35). Bang sprite AI nhieu khi ve cac frame
            // dung y mot do cao, luc do con thu truot ngang nhu dan tren giay nen phai
            // tu them nhip. Chi them khi ban than hang khong co nhip san.
            if (nhun[nhom] && f % 2 == 1) {
                caoTrenVach++;
            }
            g.drawImage(anh[i], ox + (O - anh[i].getWidth()) / 2,
                    oy + O - caoTrenVach - anh[i].getHeight(), null);
        }
        // Bong do o o cuoi: linh thu that con nao cung co, la mot phan RIENG dat co dinh
        // ngay toa do nhan vat va giu nguyen suot 21 frame (Rong Bang img0 27x14, heo bang
        // img0 dy=-5). Thieu bong thi con thu nhin nhu dan len chu khong dung tren dat.
        final int rongBong = Math.max(8, (int) Math.round(rongThuc * 0.8));
        final int caoBong = Math.max(4, (int) Math.round(rongBong * 0.30));
        final int bx = (O_BONG % SO_COT) * O, by = (O_BONG / SO_COT) * O;
        g.setPaint(new java.awt.Color(0, 0, 0, 80));
        g.fillOval(bx + (O - rongBong) / 2, by + (O - caoBong) / 2, rongBong, caoBong);
        g.dispose();
        ImageIO.write(atlas, "png", new File("map/d_effect/" + id + ".png"));

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(anh.length + 1);              // 15 o than + 1 o bong
        for (int i = 0; i <= anh.length; i++) {
            dos.writeByte(i);
            dos.writeByte((i % SO_COT) * O);
            dos.writeByte((i / SO_COT) * O);
            dos.writeByte(O);
            dos.writeByte(O);
        }
        dos.writeShort(3 * SO_SLOT);
        for (int nhom = 0; nhom < 3; nhom++) {
            for (int slot = 0; slot < SO_SLOT; slot++) {
                final int f = slot < SO_GOC ? slot : slot - SO_GOC;
                final int idAnh = nhom * SO_GOC + f;
                if (khung[nhom][f] == null || anh[idAnh] == null) {
                    dos.writeByte(0);
                    continue;
                }
                dos.writeByte(2);
                // Phan 1: bong do. Dat truoc than de nam DUOI than, va dung mot toa do
                // cho ca 21 frame nen bong khong xe dich theo nhip nhun cua than.
                // O bong duoc ve hinh bau duc dung giua o nen -O/2 dua tam bong ve
                // dung toa do nhan vat.
                dos.writeShort(-O / 2);
                dos.writeShort(-O / 2);
                dos.writeByte(O_BONG);
                dos.writeByte(0);
                dos.writeByte(0);
                // Phan 2: than con thu.
                // dx = -O/2: can giua o. Dung dung nua o thi client lat anh cho huong
                // phai bang num = -(dx + w) cung ra -O/2, hai ben trung khop tuyet doi.
                dos.writeShort(-O / 2);
                // Day o nam duoi toa do nhan vat THO_XUONG diem. Con thu phai tho xuong
                // duoi chan nhan vat, khong duoc dung ngay tai do: client nang nhan vat
                // len khi cuoi (dy_horse_tool, Rong Bang -22 khi nhin truoc) nen duoi chan
                // phai con than va chan con thu, khong thi nhin nhu nguoi dung canh con
                // thu chu khong phai ngoi tren.
                dos.writeShort(-O + THO_XUONG[nhom]);
                dos.writeByte(idAnh);
                dos.writeByte(0);   // flip: client tu lat khi di sang phai
                dos.writeByte(0);   // onTop = 0 -> nam trong danh sach ve sau cung
            }
        }
        // Bo buoc sequence: bat chuoc Rong Bang, tro vao 3 frame chay cua huong ngang.
        dos.writeByte(9);
        for (final int b : new int[]{16, 16, 16, 17, 17, 17, 18, 18, 18}) {
            dos.writeShort(b);
        }
        dos.writeByte(0);   // indexStartSkill
        dos.writeByte(0);   // frameChar[0] rong
        dos.writeByte(0);   // frameChar[1] rong
        dos.writeByte(0);   // frameChar[3] rong
        dos.flush();
        try (FileOutputStream fo = new FileOutputStream(new File("map/d_effect/" + id))) {
            fo.write(bos.toByteArray());
        }
        System.out.println("Xong: d_effect/" + id + ".png (" + ATLAS + "x" + ATLAS + ", luoi "
                + SO_COT + "x" + SO_COT + " o " + O + ", con thu to nhat " + rongThuc + "x"
                + caoThuc + ") + d_effect/" + id + " (" + bos.size()
                + " byte, 21 frame), ti le " + String.format("%.3f", tiLe)
                + " -> id gui client = " + (8700 + id));
    }

    /**
     * Trong mot o, chi giu lai con thu cua o do va bo cac manh cua con ben canh.
     *
     * Chia o deu nhau thi duong cat hay di ngang qua con ke tiep, de lai mot lat mong
     * o ria frame — trong game thay nhu co manh rac bay theo. Cach loc: tim cac khoi
     * diem lien nhau trong o, chon khoi to nhat co phu qua tim o (con thu bao gio cung
     * nam giua o), roi giu them nhung khoi nam trong be ngang cua khoi chinh (tai,
     * duoi, do trang tri bi tach roi). Con lai coi la cua con ben canh.
     *
     * Tra ve mat na rieng cua o, khong sua mat na goc de o ben canh con dung duoc.
     */
    private static boolean[][] locConThu(final boolean[][] m, final int x1, final int x2,
                                          final int y1, final int y2) {
        final boolean[][] rieng = new boolean[m.length][];
        for (int y = 0; y < m.length; y++) rieng[y] = new boolean[m[y].length];
        final boolean[][] daXet = new boolean[y2 - y1 + 1][x2 - x1 + 1];
        final Deque<int[]> ngan = new ArrayDeque<>();
        final List<List<int[]>> khoi = new ArrayList<>();
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (!m[y][x] || daXet[y - y1][x - x1]) continue;
                final List<int[]> cum = new ArrayList<>();
                ngan.push(new int[]{x, y});
                daXet[y - y1][x - x1] = true;
                while (!ngan.isEmpty()) {
                    final int[] p = ngan.pop();
                    cum.add(p);
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            final int nx = p[0] + dx, ny = p[1] + dy;
                            if (nx < x1 || nx > x2 || ny < y1 || ny > y2) continue;
                            if (!m[ny][nx] || daXet[ny - y1][nx - x1]) continue;
                            daXet[ny - y1][nx - x1] = true;
                            ngan.push(new int[]{nx, ny});
                        }
                    }
                }
                khoi.add(cum);
            }
        }
        if (khoi.isEmpty()) return rieng;

        final int tim = (x1 + x2) / 2;
        List<int[]> chinh = null;
        for (final List<int[]> cum : khoi) {
            int a = Integer.MAX_VALUE, b = -1;
            for (final int[] p : cum) {
                a = Math.min(a, p[0]);
                b = Math.max(b, p[0]);
            }
            if (a > tim || b < tim) continue;
            if (chinh == null || cum.size() > chinh.size()) chinh = cum;
        }
        if (chinh == null) {
            for (final List<int[]> cum : khoi) {
                if (chinh == null || cum.size() > chinh.size()) chinh = cum;
            }
        }
        int cx1 = Integer.MAX_VALUE, cx2 = -1;
        for (final int[] p : chinh) {
            cx1 = Math.min(cx1, p[0]);
            cx2 = Math.max(cx2, p[0]);
        }
        for (final List<int[]> cum : khoi) {
            if (cum != chinh) {
                int a = Integer.MAX_VALUE, b = -1;
                for (final int[] p : cum) {
                    a = Math.min(a, p[0]);
                    b = Math.max(b, p[0]);
                }
                if (a < cx1 || b > cx2) continue;   // nam ngoai be ngang con thu
            }
            for (final int[] p : cum) rieng[p[1]][p[0]] = true;
        }
        return rieng;
    }

    /**
     * Lam toi cac diem nam o ria hinh de tao vien, khong lam hinh to ra.
     *
     * Sprite ve tay trong game (Rong Bang, heo bang...) deu co vien toi nen o co nho van
     * doc ro hinh. Anh AI thu nho tu 150px xuong 56px thi ria nhat dan ra nen nhin bet
     * vao nen. Chi lam toi diem dac nao co canh giap nen la du.
     */
    private static void veVien(final BufferedImage a, final double dam) {
        final int w = a.getWidth(), h = a.getHeight();
        final int[] goc = a.getRGB(0, 0, w, h, null, 0, w);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = goc[y * w + x];
                if (((p >>> 24) & 0xFF) < 200) continue;
                boolean giapNen = false;
                for (int k = 0; k < 4 && !giapNen; k++) {
                    final int nx = x + (k == 0 ? -1 : k == 1 ? 1 : 0);
                    final int ny = y + (k == 2 ? -1 : k == 3 ? 1 : 0);
                    giapNen = nx < 0 || ny < 0 || nx >= w || ny >= h
                            || ((goc[ny * w + nx] >>> 24) & 0xFF) < 40;
                }
                if (!giapNen) continue;
                final int r = (int) (((p >> 16) & 0xFF) * dam);
                final int g = (int) (((p >> 8) & 0xFF) * dam);
                final int b = (int) ((p & 0xFF) * dam);
                a.setRGB(x, y, (p & 0xFF000000) | (r << 16) | (g << 8) | b);
            }
        }
    }

    /** Lat anh theo chieu ngang. */
    private static BufferedImage latNgang(final BufferedImage a) {
        final BufferedImage r = new BufferedImage(a.getWidth(), a.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                r.setRGB(a.getWidth() - 1 - x, y, a.getRGB(x, y));
            }
        }
        return r;
    }

    /**
     * Xoa cac dom rieng le nho hon toiThieu diem khoi mat na.
     *
     * Bang sprite AI ray rac day dom bui quanh con thu. Dom nay khong nhin thay o
     * kich thuoc goc nhung khi thu nho xuong 60px thi thanh hat nhieu, va te hon la
     * no phinh khung bao ra khien con thu bi thu nho hon can thiet roi lech tam.
     */
    private static int boLomDom(final boolean[][] m, final int toiThieu) {
        if (toiThieu <= 1) return 0;
        final int h = m.length, w = m[0].length;
        final boolean[][] daXet = new boolean[h][w];
        final Deque<int[]> ngan = new ArrayDeque<>();
        final List<int[]> cum = new ArrayList<>();
        int xoa = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!m[y][x] || daXet[y][x]) continue;
                cum.clear();
                ngan.push(new int[]{x, y});
                daXet[y][x] = true;
                while (!ngan.isEmpty()) {
                    final int[] p = ngan.pop();
                    cum.add(p);
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            final int nx = p[0] + dx, ny = p[1] + dy;
                            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                            if (!m[ny][nx] || daXet[ny][nx]) continue;
                            daXet[ny][nx] = true;
                            ngan.push(new int[]{nx, ny});
                        }
                    }
                }
                if (cum.size() < toiThieu) {
                    for (final int[] p : cum) m[p[1]][p[0]] = false;
                    xoa++;
                }
            }
        }
        return xoa;
    }

}
