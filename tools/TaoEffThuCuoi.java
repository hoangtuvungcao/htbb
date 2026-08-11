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
    // ---- Bo cuc atlas: CO DINH, khong duoc doi neu khong muon moi nguoi tat app ----
    /** Khung ao chua con thu: moi frame duoc dung vao khung nay truoc khi xep vao atlas. */
    private static final int WB = 64, HB = 64;
    /** So o moi dong tren atlas: 256 / 64 = 4, luoi 4x4 lap day dung 256x256. */
    private static final int O_MOI_DONG = ATLAS / WB;
    /** Chieu cao o bong do. */
    private static final int CAO_BONG = 24;
    /** 15 o than (3 huong x 5 frame) + 1 o bong. */
    private static final int SO_ANH_CON = 3 * 5 + 1;
    /** Chi so anh con cua bong do. */
    private static final int ID_BONG = SO_ANH_CON - 1;

    /** O chua NGUYEN con thu cua mot frame. */
    private static int[] oThan(final int nhom, final int f) {
        final int j = nhom * SO_GOC + f;
        return new int[]{(j % O_MOI_DONG) * WB, (j / O_MOI_DONG) * HB};
    }

    /** O chua bong do: o thu 16, cho trong con lai cua luoi. */
    private static int[] oBong() {
        final int j = 3 * SO_GOC;
        return new int[]{(j % O_MOI_DONG) * WB, (j / O_MOI_DONG) * HB};
    }
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
        // Khoang o duoc dung trong hang, dang --oxuong=0-2. Can co vi bang sprite AI
        // nhieu khi don HAI goc nhin vao cung mot hang (tien loc: hang "Down" thuc ra la
        // 3 con nhin truoc roi 3 con nhin sau), lay ca hang thi frame chay bi mat mat.
        final int[] oTu = {0, 0, 0};
        final int[] oDen = {-1, -1, -1};
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
            else if (a.startsWith("--oxuong=")) docKhoang(a.substring(9), oTu, oDen, 0);
            else if (a.startsWith("--olen=")) docKhoang(a.substring(7), oTu, oDen, 1);
            else if (a.startsWith("--ongang=")) docKhoang(a.substring(9), oTu, oDen, 2);
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
            final int tu = Math.max(0, oTu[nhom]);
            final int den = oDen[nhom] < 0 ? cot.size() - 1 : Math.min(oDen[nhom], cot.size() - 1);
            final int soO = den - tu + 1;
            if (soO < 1) {
                System.out.println("Khoang o cua nhom " + nhom + " rong");
                return;
            }
            int cy2 = -1;
            for (int f = 0; f < SO_GOC; f++) {
                // Lay SO_GOC frame LIEN TIEP roi lap lai vong tron neu khoang ngan hon.
                // Lay lien tiep dung theo quy uoc cua linh thu that: heo bang dung 5 anh
                // lien tiep, slot 0-1 la dung yen, slot 2-3-4 la chay. Neu khoang chi co
                // 3 o thi thanh 0,1,2,0,1 — ba frame chay van khac nhau het.
                final int[] c = cot.get(tu + f % soO);
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
            System.out.println("  nhom " + nhom + " (hang " + hang[nhom] + ", o " + tu + ".."
                    + den + " trong " + cot.size() + "): " + soO + " frame nguon, vach dat y="
                    + cy2);
            // Nam frame cua mot huong phai xap xi cao bang nhau. Bang sprite AI hay co mot
            // o ve con thu nho han (phuc thu o 14: 17px trong khi bon o kia 28-34px); tool
            // luu nguyen con nen o do se hien dung nhu vay -> con thu phong to thu nho theo
            // nhip di. Khong tu sua duoc o day (thu nho/phong to lai la lam nhoe), phai doi
            // frame khac trong --lay.
            int caoMin = Integer.MAX_VALUE, caoMax = 0;
            for (int f = 0; f < SO_GOC; f++) {
                if (khung[nhom][f] == null) continue;
                final int c = khung[nhom][f][3] - khung[nhom][f][1] + 1;
                caoMin = Math.min(caoMin, c);
                caoMax = Math.max(caoMax, c);
            }
            if (caoMax > 0 && caoMax - caoMin > caoMax * 0.25) {
                System.out.println("  CANH BAO: nhom " + nhom + " lech co frame (" + caoMin
                        + ".." + caoMax + " px = " + (100 * (caoMax - caoMin) / caoMax)
                        + "%) -> con thu se phong to thu nho khi di. Doi frame trong --lay.");
            }
        }
        if (caoNhat == 0) {
            System.out.println("Khong lay duoc frame nao");
            return;
        }

        // MOT co chuan cho CA 5 skin: thu nho sao cho frame cao nhat dung bang --cao.
        // Frame nao vuot o 64x64 thi ha ti le cho vua.
        //
        // --cao=0: GIU NGUYEN kich thuoc (ti le 1.0). Dung khi anh nguon da la pixel-art
        // o do phan giai that: moi ti le khac 1 (nhat la ti le le nhu 0.75) deu noi suy
        // lai luoi pixel va lam vien nhoe — do la khac biet chinh giua sprite nhin "net"
        // va sprite nhin "mo mo". Chi thu nho khi frame that su lon hon kich thuoc muon co.
        double tiLe = (caoDich <= 0) ? 1.0 : (double) caoDich / caoNhat;
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
                    if (nho.getWidth() > WB || nho.getHeight() > HB) vuot++;
                    anh[nhom * SO_GOC + f] = nho;
                }
            }
            if (vuot == 0) break;
            tiLe *= 0.96;
            if (tiLe < 0.02) {
                System.out.println("Khong nhet noi vao khung " + WB + "x" + HB);
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
        // ================= MOI FRAME LUU NGUYEN CON =================
        // Rong Bang tiet kiem cho bang cach luu DAU mot lan roi dung cho ca 7 frame, chi
        // thay THAN. Ban dau tool cung lam vay: cat khung ao tai y = 42, nua tren lay tu
        // frame 0 cua huong roi dung cho ca 7 frame. Cach do CHI DUNG khi bang sprite ve
        // dung luat "dau dung yen, chi chan buoc" — Rong Bang ve dung the.
        //
        // Bang sprite o day thi khong: 5 frame trong mot hang ve lai ca con, chieu cao
        // khung bao lech nhau toi 17 diem. Duong cat co dinh roi vao cho khac nhau tren
        // tung con, do duoc tren 75 frame cua ca 5 skin:
        //   - frame CAO HON frame 0: phan nho len tren duong cat bi thay bang nua tren cua
        //     frame 0 -> mat han 1211/48659 diem (2.49%), cho te nhat cut 16 hang (gau truc
        //     huong len, frame 4: mat 26.5% so diem cua frame do)
        //   - frame THAP HON frame 0: dau cua chinh no nam duoi duong cat van hien, lai bi
        //     dan them nua tren cua frame 0 -> HAI DAU, 4589 diem thua (9.43%); phuc thu
        //     huong xuong frame 3 con ho han 5 hang trong, dau bay roi khoi than
        //   - tong cong 28.75% so diem khac han bang goc
        // Vi vay bo han kieu tach nua tren/nua duoi: moi frame luu NGUYEN CON trong mot o
        // 64x64. Doi lai la mat hieu ung "dau dung yen", con thu se dong dung y nhu bang
        // sprite ve — muon co lai hieu ung do thi phai sua BANG SPRITE cho 5 frame cung
        // chieu cao va cung phan than tren, khong phai sua tool.
        //
        // Nhip nhun 1 diem so le van giu nguyen (chuyen sang dy, xem THO_XUONG ben duoi).
        final BufferedImage atlas = new BufferedImage(ATLAS, ATLAS, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = atlas.createGraphics();
        for (int nhom = 0; nhom < 3; nhom++) {
            for (int f = 0; f < SO_GOC; f++) {
                final BufferedImage a = anh[nhom * SO_GOC + f];
                if (a == null) continue;
                // Can giua ngang va SAT DAY o: nho sat day ma ca 5 frame dung tren cung mot
                // vach dat, chi than nhun len xuong theo tu the — khong bi truot chan.
                final int[] o = oThan(nhom, f);
                g.drawImage(a, o[0] + (WB - a.getWidth()) / 2, o[1] + HB - a.getHeight(), null);
            }
        }
        // Bong do: linh thu that con nao cung co, la mot phan RIENG dat co dinh ngay toa do
        // nhan vat va giu nguyen suot 21 frame (Rong Bang anh0 27x14 dung 21 lan).
        final int rongBong = Math.max(8, (int) Math.round(rongThuc * 0.8));
        final int caoBong = Math.max(4, (int) Math.round(rongBong * 0.30));
        final int[] ob = oBong();
        g.setPaint(new java.awt.Color(0, 0, 0, 80));
        g.fillOval(ob[0] + (WB - rongBong) / 2, ob[1] + (CAO_BONG - caoBong) / 2,
                rongBong, caoBong);
        g.dispose();
        ImageIO.write(atlas, "png", new File("map/d_effect/" + id + ".png"));

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(SO_ANH_CON);
        for (int nhom = 0; nhom < 3; nhom++) {          // 0..14 o than, nguyen con
            for (int f = 0; f < SO_GOC; f++) {
                final int[] o = oThan(nhom, f);
                ghiAnhCon(dos, nhom * SO_GOC + f, o[0], o[1], WB, HB);
            }
        }
        final int[] ob2 = oBong();                      // 15    o bong
        ghiAnhCon(dos, ID_BONG, ob2[0], ob2[1], WB, CAO_BONG);

        dos.writeShort(3 * SO_SLOT);
        for (int nhom = 0; nhom < 3; nhom++) {
            for (int slot = 0; slot < SO_SLOT; slot++) {
                final int f = slot < SO_GOC ? slot : slot - SO_GOC;
                if (khung[nhom][f] == null || anh[nhom * SO_GOC + f] == null) {
                    dos.writeByte(0);
                    continue;
                }
                // Nhip nhun: nang ca con len 1 diem o frame le, y nhu Rong Bang.
                final int nhunF = slot % 2 == 1 ? 1 : 0;
                // Day khung ao nam duoi toa do nhan vat THO_XUONG diem. Con thu phai tho
                // xuong duoi chan nhan vat, khong duoc dung ngay tai do: client nang nhan
                // vat len khi cuoi (dy_horse_tool, Rong Bang -22 khi nhin truoc) nen duoi
                // chan phai con than va chan con thu.
                final int dayKhung = THO_XUONG[nhom] - nhunF;
                dos.writeByte(2);
                // Phan 1: bong do — ve truoc nen nam duoi, va KHONG nhun theo than.
                ghiPhan(dos, -WB / 2, -CAO_BONG / 2, ID_BONG);
                // Phan 2: nguyen con thu cua frame nay.
                ghiPhan(dos, -WB / 2, dayKhung - HB, nhom * SO_GOC + f);
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
        System.out.println("Xong: d_effect/" + id + ".png (" + ATLAS + "x" + ATLAS + ", "
                + SO_ANH_CON + " anh con: 15 o than nguyen con + 1 bong"
                + ", con thu to nhat " + rongThuc + "x" + caoThuc + ") + d_effect/" + id
                + " (" + bos.size() + " byte, 21 frame), ti le "
                + String.format("%.3f", tiLe) + " -> id gui client = " + (8700 + id));
    }

    /** Ghi mo ta mot anh con: 5 byte khong dau id, x, y, rong, cao. */
    private static void ghiAnhCon(final DataOutputStream dos, final int id, final int x,
                                   final int y, final int w, final int h) throws Exception {
        dos.writeByte(id);
        dos.writeByte(x);
        dos.writeByte(y);
        dos.writeByte(w);
        dos.writeByte(h);
    }

    /** Ghi mot phan cua frame: short dx, short dy, byte idAnh, byte flip, byte onTop. */
    private static void ghiPhan(final DataOutputStream dos, final int dx, final int dy,
                                 final int idAnh) throws Exception {
        dos.writeShort(dx);
        dos.writeShort(dy);
        dos.writeByte(idAnh);
        dos.writeByte(0);   // flip: client tu lat khi di sang phai
        dos.writeByte(0);   // onTop = 0 -> nam trong danh sach ve sau cung
    }

    /** Doc tham so dang "0-2" hoac "3" thanh khoang o. */
    private static void docKhoang(final String s, final int[] tu, final int[] den, final int nhom) {
        final int gach = s.indexOf('-');
        if (gach < 0) {
            tu[nhom] = Integer.parseInt(s.trim());
            den[nhom] = tu[nhom];
        } else {
            tu[nhom] = Integer.parseInt(s.substring(0, gach).trim());
            den[nhom] = Integer.parseInt(s.substring(gach + 1).trim());
        }
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
