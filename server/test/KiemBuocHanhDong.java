import com.htbb.pokemon.BuocHanhDong;
import data.InfoQuestTemplate;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.HanhDongNhiemVuGame;
import real.NewCharQuest;

/**
 * BUOC NOI CHUYEN HUA HANH DONG (nhanh sua-buoc-hanh-dong; khoi "sua_buoc_hanh_dong.sql" cua sql/htbb.sql). Bot choi that
 * cap 1-40 bao ~80 buoc -64 type 3 ghi "Chế tạo / Nâng +2 / Hộ tống / Giải câu đố / Quan sát ban đêm / Bán ở chợ / Nấu /
 * Chọn một con đường / Hạ 26 Gloom trong 8 phút" ma nghe NPC la xong.
 *
 * <ul>
 *   <li>Du lieu (phan A + khoi ap trong bo nho): moi buoc noi chuyen KHONG kiem that (khong vung dich, khong
 *       kiem_hanh_dong=1) chi ghi "Nói chuyện với X ở Y" (X dung tren Y), khong hua viec may chu khong kiem; buoc kiem that
 *       ghi dung viec may chu dem (so, quai, ban do sinh mo o cap buoc, khung gio); thuong khong doi; khoi chay lai an toan.</li>
 *   <li>Game (nhan vat gia): nghe NPC chi NHAN buoc; tien do chi dem SAU khi nhan; chi viec that (dung kieu, dung quai /
 *       loai, dung gio, trong gio) moi lam xong buoc.</li>
 * </ul>
 */
final class KiemBuocHanhDong {

    private KiemBuocHanhDong() {
    }

    static final String KHOI = "sua_buoc_hanh_dong.sql";
    /** Chu muc tieu hua mot hanh dong (dau chu scontent cu cua cac buoc nay). */
    static final Pattern P_HUA = Pattern.compile("^(?:Chế tạo|Nâng|Hộ tống|Giải|Quan sát|Bán|Nấu|Chọn|Hạ|Đánh bại|Ghi|Mặc|Thắng|"
            + "Về đích|Góp sức|Khảm|Phân rã|Cho|Trả lời|Thu|Tìm|Câu|Đào|Trồng|Dùng|Học|Đứng|Đến)\\b");
    static final Pattern P_NOI = Pattern.compile("^Nói chuyện với (\\S+) ở (.+)$");
    static final Set<String> COT_DUOC_SUA = new HashSet<String>(Arrays.asList("scontent", "content", "infogift", "infofinish",
            "muc_tieu_text", "dieu_kien", "dia_danh"));

    public static void chay() {
        Kiem.nhom("Buoc noi chuyen hua hanh dong: kiem that hoac chu that (khoi " + KHOI + ")");
        try {
            luat();
            duLieu();
            game();
            maNguon();
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.dung("KiemBuocHanhDong chay het: " + e, false);
        }
    }

    // ------------------------------------------------------------------ luat thuan

    static Map<String, String> dk(final String s) {
        return com.htbb.pokemon.NhiemVuMau.docDieuKien(s);
    }

    static void luat() {
        Kiem.dung("khong danh dau -> khong phai buoc hanh dong", BuocHanhDong.tao("X", "quan_sat", "1", 1, "dieu_kien_quan_sat=dem") == null);
        Kiem.dung("kieu khong dem duoc (che_tao) -> khong", BuocHanhDong.tao("X", "che_tao", "", 1, "kiem_hanh_dong=1") == null);
        Kiem.dung("quan_sat + danh dau -> co", BuocHanhDong.tao("X", "quan_sat", "1", 1, "dieu_kien_quan_sat=dem;kiem_hanh_dong=1") != null);
        Kiem.dung("dem: 19h, 23h, 0h, 5h dung", BuocHanhDong.dungGio("dem", 19) && BuocHanhDong.dungGio("dem", 23)
                && BuocHanhDong.dungGio("dem", 0) && BuocHanhDong.dungGio("dem", 5));
        Kiem.sai("dem: 6h, 12h, 18h sai", BuocHanhDong.dungGio("dem", 6) || BuocHanhDong.dungGio("dem", 12) || BuocHanhDong.dungGio("dem", 18));
        Kiem.dung("gio=5-7: 5h..7h dung, 4h / 8h sai", BuocHanhDong.dungGio("gio=5-7", 5) && BuocHanhDong.dungGio("gio=5-7", 7)
                && !BuocHanhDong.dungGio("gio=5-7", 4) && !BuocHanhDong.dungGio("gio=5-7", 8));
        Kiem.dung("thoi tiet khong kiem (chu da bo 'khi trời mưa')", BuocHanhDong.dungGio("thoi_tiet=Mưa", 3));
        Kiem.bang("nang cap: tien do = muc cao nhat", Integer.valueOf(3), Integer.valueOf(BuocHanhDong.sauNangCap(BuocHanhDong.sauNangCap(0, "3"), "1")));
        final long[] a = BuocHanhDong.thuThachGio(0, 0L, 1000L, 8);
        Kiem.dung("ha tinh gio: con dau bat dau dong ho", a[0] == 1 && a[1] == 1000L);
        final long[] b = BuocHanhDong.thuThachGio(5, 1000L, 1000L + 7 * 60000L, 8);
        Kiem.dung("ha tinh gio: trong gio thi cong", b[0] == 6 && b[1] == 1000L);
        final long[] c = BuocHanhDong.thuThachGio(5, 1000L, 1000L + 9 * 60000L, 8);
        Kiem.dung("ha tinh gio: qua gio thi dem lai tu con nay", c[0] == 1 && c[1] == 1000L + 9 * 60000L);
        final long[] d = BuocHanhDong.thuThachGio(5, 0L, 5000L, 8);
        Kiem.dung("ha tinh gio: vao lai game (mat luc bat dau) thi dem lai", d[0] == 1 && d[1] == 5000L);
    }

    // ------------------------------------------------------------------ du lieu

    static Map<String, String> tim(final List<Map<String, String>> ds, final String khoa, final String v) {
        for (final Map<String, String> d : ds) {
            if (v.equals(d.get(khoa))) {
                return d;
            }
        }
        return null;
    }

    static String tra(final String inf, final String phan) {
        final Matcher m = Pattern.compile(Pattern.quote(phan) + ":\\|([^|]*)\\|").matcher(inf == null ? "" : inf);
        return m.find() ? m.group(1).trim() : "";
    }

    static String duoiThuong(final String inf) {
        final int i = inf == null ? -1 : inf.indexOf("Phần thưởng:");
        return i < 0 ? "" : inf.substring(i);
    }

    static void duLieu() throws Exception {
        KiemMucTieuNhiemVu.napBang();
        final List<Map<String, String>> qm = KiemMucTieuNhiemVu.dongThuong("data_quest_moi");
        final List<Map<String, String>> ql = KiemMucTieuNhiemVu.dongThuong("data_quest_lau_dai");
        final List<Map<String, String>> nd = KiemMucTieuNhiemVu.dongThuong("data_nhiem_vu_noi_dung");
        final List<Map<String, String>> qm2 = KiemMucTieuNhiemVu.sao(qm);
        final List<Map<String, String>> ql2 = KiemMucTieuNhiemVu.sao(ql);
        final List<Map<String, String>> nd2 = KiemMucTieuNhiemVu.sao(nd);
        final String khoi = DayDu.phanNangCap(KHOI);
        Kiem.dung("co khoi " + KHOI + " o phan B", khoi.length() > 10000);
        int saiDang = 0;
        final Set<String> cot = new TreeSet<String>();
        final Pattern pCot = Pattern.compile("`(\\w+)` = ");
        for (final String l : khoi.split("\n")) {
            if (l.isEmpty() || l.startsWith("--")) {
                continue;
            }
            final Matcher m = KiemMucTieuNhiemVu.P_LENH.matcher(l);
            if (!m.matches()) {
                saiDang++;
                continue;
            }
            final Matcher c = pCot.matcher(m.group(2));
            while (c.find()) {
                cot.add(c.group(1));
            }
        }
        Kiem.bang("khoi chi gom UPDATE `bang` SET ... WHERE khoa AND gia tri cu (mot dong / lenh)", Integer.valueOf(0), Integer.valueOf(saiDang));
        cot.removeAll(COT_DUOC_SUA);
        Kiem.bang("khoi chi sua chu / dau kiem / dia danh (thuong, NPC, loai buoc giu nguyen)", "[]", cot.toString());
        final Map<String, List<Map<String, String>>> bang = new HashMap<String, List<Map<String, String>>>();
        bang.put("data_quest_moi", qm2);
        bang.put("data_quest_lau_dai", ql2);
        bang.put("data_nhiem_vu_noi_dung", nd2);
        final List<Integer> lan1 = KiemMucTieuNhiemVu.ap(khoi, bang);
        final List<Integer> lech = new ArrayList<Integer>();
        for (int i = 0; i < lan1.size(); i++) {
            if (lan1.get(i).intValue() != 1) {
                lech.add(Integer.valueOf(i + 1));
            }
        }
        Kiem.dung("khoi co lenh (" + lan1.size() + ")", lan1.size() > 200);
        Kiem.bang("moi lenh khop dung mot dong phan A (gia tri cu dung)", "[]", lech.toString());
        int lai = 0;
        for (final Integer n : KiemMucTieuNhiemVu.ap(khoi, bang)) {
            lai += n.intValue();
        }
        Kiem.bang("ap lan hai khong doi gi (chay lai an toan)", Integer.valueOf(0), Integer.valueOf(lai));

        final Set<String> khuVuc = new HashSet<String>();
        for (final Map<String, String> d : DayDu.dong("data_nhiem_vu_khu_vuc")) {
            khuVuc.add(d.get("ma"));
        }
        final Map<String, Map<String, String>> lauDai = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> l : ql2) {
            lauDai.put(l.get("id_so"), l);
        }
        final Map<String, Map<String, String>> noiDung = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> n : nd2) {
            noiDung.put(n.get("ma"), n);
        }
        final Map<String, Set<Integer>> npcO = new HashMap<String, Set<Integer>>();
        final Map<Integer, String> npcTen = new HashMap<Integer, String>();
        for (final Map<String, String> d : DayDu.dong("data_npc")) {
            final Set<Integer> s = npcO.computeIfAbsent(d.get("pokemon"), k -> new TreeSet<Integer>());
            for (final String x : d.get("ban_do").split(",")) {
                if (x.trim().matches("\\d+")) {
                    s.add(Integer.valueOf(x.trim()));
                }
            }
            npcTen.put(Integer.valueOf(d.get("type")), d.get("pokemon"));
        }
        final Map<String, Set<Integer>> theoTen = new HashMap<String, Set<Integer>>(KiemMucTieuNhiemVu.mapTheoTen);
        theoTen.put("làng", new TreeSet<Integer>(Arrays.asList(0, 70, 80)));
        theoTen.put("Làng khởi đầu", theoTen.get("làng"));
        final Map<Integer, Integer> dexMau = new HashMap<Integer, Integer>();
        final Map<Integer, List<Integer>> mauTheoDex = new HashMap<Integer, List<Integer>>();
        for (final Map<String, String> d : KiemMucTieuNhiemVu.dongThuong("data_monster")) {
            final int id = KiemMucTieuNhiemVu.so(d.get("id"));
            final int dex = KiemMucTieuNhiemVu.so(d.get("dex"));
            dexMau.put(Integer.valueOf(id), Integer.valueOf(dex));
            mauTheoDex.computeIfAbsent(Integer.valueOf(dex), k -> new ArrayList<Integer>()).add(Integer.valueOf(id));
        }

        final List<String> hua = new ArrayList<String>();
        final List<String> loiNoi = new ArrayList<String>();
        final List<String> loiKiem = new ArrayList<String>();
        final List<String> loiThuong = new ArrayList<String>();
        final List<String> loiSoTay = new ArrayList<String>();
        final List<String> kiemThat = new ArrayList<String>();
        int buoc = 0;
        final Pattern pQs = Pattern.compile("^Hạ 1 (.+?)( vào ban đêm \\(19:00–5:59\\)| lúc bình minh \\(5:00–7:59\\)"
                + "| vào buổi sáng \\(6:00–10:59\\)| lúc hoàng hôn \\(17:00–19:59\\))? ở (.+)$");
        final Pattern pGio = Pattern.compile("^Hạ (\\d+) (.+?) trong (\\d+) phút ở (.+)$");
        final Pattern pNang = Pattern.compile("^Nâng 1 món trang bị lên \\+(\\d+) ở chỗ Machamp$");
        for (final Map<String, String> q : qm2) {
            if (!"3".equals(q.get("type")) || !("0".equals(q.get("mainsub")) || "1".equals(q.get("mainsub")))) {
                continue;
            }
            final Map<String, String> l = lauDai.get(q.get("id"));
            if (l == null || khuVuc.contains(l.get("ma"))) {
                continue; // buoc dia diem: KhuVucNhiemVuGame kiem vi tri that
            }
            buoc++;
            final String ma = l.get("ma");
            final String sc = q.get("scontent").trim();
            final String nhan = q.get("id") + " " + ma + ": ";
            final Map<String, String> n = noiDung.get(ma);
            final BuocHanhDong b = n == null ? null : BuocHanhDong.tao(ma, l.get("kieu_muc_tieu"), l.get("muc_tieu_id"),
                    KiemMucTieuNhiemVu.so(l.get("so_luong")), n.get("dieu_kien"));
            if ("chinh".equals(l.get("loai")) && n != null && !sc.equals(n.get("muc_tieu_text").trim())) {
                loiSoTay.add(nhan + "So tay \"" + n.get("muc_tieu_text") + "\" != \"" + sc + "\"");
            }
            final Map<String, String> goc = tim(qm, "id", q.get("id"));
            if (!duoiThuong(goc.get("infogift")).equals(duoiThuong(q.get("infogift")))) {
                loiThuong.add(nhan + "chu phan thuong doi");
            }
            final String nv = tra(q.get("infogift"), "Nhiệm vụ");
            if (b == null) {
                // KHONG kiem that: chi duoc noi chuyen
                final String noiDungThoai = q.get("content");
                if (P_HUA.matcher(sc).find() || P_HUA.matcher(nv).find() || !sc.startsWith("Nói chuyện với ")
                        || noiDungThoai.contains("kênh trò chuyện") || noiDungThoai.contains("gõ đáp án")) {
                    hua.add(nhan + sc);
                }
                final Matcher m = P_NOI.matcher(sc);
                if (m.matches()) {
                    final Set<Integer> o = npcO.get(m.group(1));
                    final Set<Integer> y = theoTen.get(m.group(2).trim());
                    final String npcGiao = npcTen.get(Integer.valueOf(KiemMucTieuNhiemVu.so(q.get("npcrcv"))));
                    if (o == null || y == null || !KiemDiaDiemNpc.giao(o, y)) {
                        loiNoi.add(nhan + sc + " (NPC khong dung o do)");
                    } else if (!goc.get("scontent").trim().equals(sc) && !m.group(1).equals(npcGiao)) {
                        loiNoi.add(nhan + sc + " (NPC giao that la " + npcGiao + ")");
                    }
                }
                continue;
            }
            kiemThat.add(ma);
            final int can = b.can;
            final int lv = KiemMucTieuNhiemVu.so(q.get("lv"));
            if ("quan_sat".equals(b.kieu) || "thu_thach_gio".equals(b.kieu)) {
                final boolean qs = "quan_sat".equals(b.kieu);
                final Matcher m = (qs ? pQs : pGio).matcher(sc);
                if (!m.matches()) {
                    loiKiem.add(nhan + "\"" + sc + "\" khong dung dang");
                    continue;
                }
                final String tenQ = m.group(qs ? 1 : 2);
                final String noi = m.group(qs ? 3 : 4).trim();
                final List<Integer> mau = new ArrayList<Integer>();
                if (qs) {
                    final List<Integer> x = mauTheoDex.get(Integer.valueOf(KiemMucTieuNhiemVu.so(b.mucTieuId)));
                    if (x != null) {
                        mau.addAll(x);
                    }
                } else {
                    mau.add(Integer.valueOf(KiemMucTieuNhiemVu.so(b.mucTieuId)));
                }
                boolean coTen = false;
                boolean sinh = false;
                for (final Integer t : mau) {
                    coTen |= tenQ.equals(KiemMucTieuNhiemVu.tenQuai.get(t));
                    if (KiemMucTieuNhiemVu.sinhO(t.intValue(), noi) && KiemMucTieuNhiemVu.capMoO(t.intValue(), noi) <= lv + 3) {
                        sinh = true;
                    }
                }
                if (!coTen) {
                    loiKiem.add(nhan + tenQ + " khong phai quai may chu dem (" + b.mucTieuId + ")");
                }
                if (!sinh) {
                    loiKiem.add(nhan + tenQ + " khong sinh o " + noi + " mo o cap " + lv);
                }
                if (!tra(q.get("infogift"), "Địa điểm").equals(noi)) {
                    loiKiem.add(nhan + "Địa điểm != " + noi);
                }
                if (qs) {
                    final String khung = n.get("dieu_kien");
                    final String g = m.group(2) == null ? "" : m.group(2);
                    final boolean dem = khung.contains("dieu_kien_quan_sat=dem");
                    final boolean coGio = dem || khung.contains("dieu_kien_quan_sat=gio=");
                    if (dem != g.contains("ban đêm") || coGio == g.isEmpty()) {
                        loiKiem.add(nhan + "khung gio chu \"" + g + "\" != dieu kien " + khung);
                    }
                    final String loi = q.get("content");
                    if (!coGio && (loi.contains("trời mưa") || loi.contains("sương mù") || loi.contains("tuyết rơi")
                            || loi.contains("khi tuyết") || loi.contains("lúc mưa"))) {
                        loiKiem.add(nhan + "loi thoai hua thoi tiet (may chu khong kiem)");
                    }
                } else if (KiemMucTieuNhiemVu.so(m.group(1)) != can || KiemMucTieuNhiemVu.so(m.group(3)) != b.phut() || b.phut() <= 0) {
                    loiKiem.add(nhan + "\"" + sc + "\" != dem " + can + " trong " + b.phut() + " phut");
                }
            } else if ("nang_cap_cong".equals(b.kieu)) {
                final Matcher m = pNang.matcher(sc);
                if (!m.matches() || KiemMucTieuNhiemVu.so(m.group(1)) != can) {
                    loiKiem.add(nhan + "\"" + sc + "\" != nang len +" + can);
                }
            } else if ("nang_cap".equals(b.kieu) && !"Mặc 1 món trang bị".equals(sc)) {
                loiKiem.add(nhan + "\"" + sc + "\" != mac 1 mon");
            }
            if (!q.get("content").contains(sc.substring(0, 1).toLowerCase() + sc.substring(1)) && !"nang_cap".equals(b.kieu)) {
                loiKiem.add(nhan + "loi thoai khong nhac muc tieu that");
            }
        }
        Kiem.dung("co buoc noi chuyen de kiem (" + buoc + ")", buoc > 300);
        Kiem.bang("buoc noi chuyen khong kiem that: chi 'Nói chuyện với X ở Y', khong hua viec may chu khong kiem", "[]", hua.toString());
        Kiem.bang("'Nói chuyện với X ở Y': X la NPC giao va dung tren Y", "[]", loiNoi.toString());
        Kiem.bang("buoc kiem that: chu = viec may chu dem (so, quai, ban do sinh mo o cap buoc, khung gio)", "[]", loiKiem.toString());
        Kiem.bang("thuong (chu Phần thưởng) khong doi", "[]", loiThuong.toString());
        Kiem.bang("chuoi chinh: chu So tay = chu buoc", "[]", loiSoTay.toString());
        Kiem.bang("so buoc kiem that", Integer.valueOf(47), Integer.valueOf(kiemThat.size()));
        Kiem.dung("bot bao: C06.3 Nâng +2 / C08.2 + C17.2 quan sát đêm / C09.2 Hạ 26 Gloom trong 8 phút kiem that",
                kiemThat.containsAll(Arrays.asList("C06.3", "C08.2", "C17.2", "C09.2")));
        Kiem.sai("bot bao: chế tạo C06.2 (Bụi sao chi roi tu boss pho ban cap 20+) khong kiem that", kiemThat.contains("C06.2"));
        final Map<String, String> c094 = tim(qm2, "id", "1054");
        Kiem.bang("C09.4 câu đố: noi chuyen", "Nói chuyện với Torkoal ở Cerulean", c094.get("scontent"));
        Kiem.dung("C09.4 câu đố: nguoi choi dap trong hoi thoai", c094.get("content").endsWith(">0Là Snorlax!"));
        final Map<String, String> c034 = tim(qm2, "id", "1016");
        Kiem.bang("C03.4 hộ tống: noi chuyen", "Nói chuyện với Kecleon ở làng", c034.get("scontent"));
        final Map<String, String> c092 = tim(qm2, "id", "1052");
        Kiem.bang("C09.2: Gloom sinh o Fuchsia (khong phai Cerulean)", "Hạ 26 Gloom trong 8 phút ở Fuchsia", c092.get("scontent"));
    }

    // ------------------------------------------------------------------ game: nhan vat gia

    static InfoQuestTemplate mau(final short id, final String ma, final byte ms) {
        final InfoQuestTemplate t = new InfoQuestTemplate();
        t.id = id;
        t.type = 3;
        t.ma = ma;
        t.name = "Thử " + ma;
        t.shortContent = "Mục tiêu " + ma;
        t.main_sub = ms;
        t.idNpcRcv = 2;
        t.idNpcRes = 32000;
        t.idItemgift = new short[]{32000};
        t.potiongift = new short[]{32000};
        t.npotiongift = new short[]{32000};
        t.infofinish = "";
        return t;
    }

    static NewCharQuest q(final short id, final byte ms, final short trangThai) {
        final NewCharQuest q = new NewCharQuest(true);
        q.id_quest = id;
        q.main_sub = ms;
        q.state_quest = trangThai;
        q.monsterKilled = new short[]{0};
        q.nItem = new short[]{0};
        return q;
    }

    static void tra(final Char p, final short id, final byte ms) {
        try {
            p.OnQuest((byte) 1, id, ms);
        } catch (final RuntimeException e) {
            // goi tin / CSDL khong co trong test: trang thai da quyet dinh truoc do
        }
    }

    static boolean xong(final NewCharQuest q) {
        return q.state_quest == NewCharQuest.FINISH || q.state_quest == NewCharQuest.DONE;
    }

    static real.Monster quai(final int mau) {
        final real.Monster m = new real.Monster((byte) 1);
        m.idTemplate = mau;
        return m;
    }

    static void game() {
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        final boolean lauDaiCu = com.htbb.pokemon.LauDai.bat();
        com.htbb.pokemon.LauDai.dat(false); // nhan buoc khong can ho so CSDL
        final short idNc = 29001, idQs = 29002, idGio = 29003, idPhu = 29004, idDem = 29005;
        final Map<Short, InfoQuestTemplate> cuChinh = new HashMap<Short, InfoQuestTemplate>();
        final Map<Integer, real.MonsterTemplate> cuMau = new HashMap<Integer, real.MonsterTemplate>();
        final int gio = com.htbb.pokemon.DongHo.lich().get(java.util.Calendar.HOUR_OF_DAY);
        final int gioKhac = (gio + 12) % 24;
        try {
            final Map<String, BuocHanhDong> bd = new LinkedHashMap<String, BuocHanhDong>();
            bd.put("T.NC", BuocHanhDong.tao("T.NC", "nang_cap_cong", "", 2, "kiem_hanh_dong=1"));
            bd.put("T.QS", BuocHanhDong.tao("T.QS", "quan_sat", "451", 1, "dieu_kien_quan_sat=gio=" + gio + "-" + gio + ";kiem_hanh_dong=1"));
            bd.put("T.DEM", BuocHanhDong.tao("T.DEM", "quan_sat", "451", 1, "dieu_kien_quan_sat=gio=" + gioKhac + "-" + gioKhac + ";kiem_hanh_dong=1"));
            bd.put("T.GIO", BuocHanhDong.tao("T.GIO", "thu_thach_gio", "20", 3, "gioi_han_phut=8;kiem_hanh_dong=1"));
            bd.put("T.PHU", BuocHanhDong.tao("T.PHU", "nang_cap", "", 1, "kiem_hanh_dong=1"));
            HanhDongNhiemVuGame.dat(bd);
            for (final short id : new short[]{idNc, idQs, idGio, idDem}) {
                cuChinh.put(Short.valueOf(id), real.Map.allMainQuest.get(Short.valueOf(id)));
            }
            real.Map.allMainQuest.put(Short.valueOf(idNc), mau(idNc, "T.NC", (byte) 0));
            real.Map.allMainQuest.put(Short.valueOf(idQs), mau(idQs, "T.QS", (byte) 0));
            real.Map.allMainQuest.put(Short.valueOf(idGio), mau(idGio, "T.GIO", (byte) 0));
            real.Map.allMainQuest.put(Short.valueOf(idDem), mau(idDem, "T.DEM", (byte) 0));
            final InfoQuestTemplate phuCu = real.Map.allSubQuest.get(Short.valueOf(idPhu));
            real.Map.allSubQuest.put(Short.valueOf(idPhu), mau(idPhu, "T.PHU", (byte) 1));
            for (final int t : new int[]{7, 20}) {
                cuMau.put(Integer.valueOf(t), real.Map.monsterTemplates.get(Integer.valueOf(t)));
            }
            final real.MonsterTemplate sk = new real.MonsterTemplate();
            sk.id = 7;
            sk.dex = 451;
            real.Map.monsterTemplates.put(Integer.valueOf(7), sk);
            final real.MonsterTemplate gl = new real.MonsterTemplate();
            gl.id = 20;
            gl.dex = 44;
            real.Map.monsterTemplates.put(Integer.valueOf(20), gl);

            final Char p = new Char(null);
            p.charDBID = 987654;
            final InfoQuestTemplate tNc = real.Map.allMainQuest.get(Short.valueOf(idNc));
            Kiem.dung("buoc thuong (khong danh dau) khong bi chan", HanhDongNhiemVuGame.mau(mau((short) 1, "KHAC", (byte) 0)) == null);
            Kiem.dung("buoc hanh dong: nghe NPC khi chua nhan -> chua xong", HanhDongNhiemVuGame.chuaXong(q(idNc, (byte) 0, NewCharQuest.UN_RECEIVE), tNc));
            Kiem.bang("buoc hanh dong dang lam: NPC tra = -1 (khong dau nham)", Integer.valueOf(-1), Integer.valueOf(real.KhuVucNhiemVuGame.npcTra(tNc, 2)));
            Kiem.bang("buoc hanh dong da du: NPC tra = NPC giao", Integer.valueOf(2), Integer.valueOf(real.KhuVucNhiemVuGame.npcTra(tNc, 1)));

            // 1. nang cap +2: truoc khi nhan khong dem; nghe NPC = nhan, khong tra; chi nang that len +2 moi xong
            p.idMainQuest = idNc;
            p.mainQuest = q(idNc, (byte) 0, NewCharQuest.UN_RECEIVE);
            p.subQuest = new NewCharQuest(true);
            HanhDongNhiemVuGame.suKien(p, "nang_cap_cong", "5", 1);
            Kiem.bang("nang +5 TRUOC khi nhan: khong dem", Integer.valueOf(0), Integer.valueOf(p.mainQuest.monsterKilled[0]));
            final NewCharQuest q1 = p.mainQuest;
            tra(p, idNc, (byte) 0);
            Kiem.bang("nghe NPC (goi tra -64): da nhan, CHUA xong", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q1.state_quest));
            tra(p, idNc, (byte) 0);
            Kiem.bang("nghe NPC lan hai: van dang lam", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q1.state_quest));
            HanhDongNhiemVuGame.suKien(p, "che_tao", "", 1);
            HanhDongNhiemVuGame.suKien(p, "nang_cap", "100", 1);
            Kiem.bang("viec khac (che tao, mac do): khong dem", Integer.valueOf(0), Integer.valueOf(q1.monsterKilled[0]));
            HanhDongNhiemVuGame.suKien(p, "nang_cap_cong", "1", 1);
            Kiem.bang("nang len +1: tien do +1/+2, van dang lam", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q1.state_quest));
            Kiem.dung("tien do hien trong so: (+1/+2)", HanhDongNhiemVuGame.tienDo(q1, tNc).contains("(+1/+2)"));
            HanhDongNhiemVuGame.suKien(p, "nang_cap_cong", "2", 1);
            Kiem.dung("nang len +2: xong (tu tra)", xong(q1));

            // 2. quan sat: dung loai + dung gio moi tinh
            final InfoQuestTemplate tQs = real.Map.allMainQuest.get(Short.valueOf(idQs));
            p.idMainQuest = idQs;
            p.mainQuest = q(idQs, (byte) 0, NewCharQuest.UN_RECEIVE);
            final NewCharQuest q2 = p.mainQuest;
            HanhDongNhiemVuGame.khiHaQuai(p, quai(7));
            Kiem.bang("ha Skorupi TRUOC khi nhan: khong tinh", Integer.valueOf(NewCharQuest.UN_RECEIVE), Integer.valueOf(q2.state_quest));
            tra(p, idQs, (byte) 0);
            Kiem.bang("quan sat: nghe NPC = nhan", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q2.state_quest));
            HanhDongNhiemVuGame.khiHaQuai(p, quai(20));
            Kiem.bang("ha loai khac (Gloom): khong tinh", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q2.state_quest));
            HanhDongNhiemVuGame.khiHaQuai(p, quai(7));
            Kiem.dung("ha Skorupi dung khung gio: xong", xong(q2));
            Kiem.dung("tNc/tQs khong lan nhau", tQs != tNc);
            p.idMainQuest = idDem;
            p.mainQuest = q(idDem, (byte) 0, NewCharQuest.WORKING);
            HanhDongNhiemVuGame.khiHaQuai(p, quai(7));
            Kiem.bang("ha Skorupi NGOAI khung gio: khong tinh", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(p.mainQuest.state_quest));

            // 3. ha tinh gio: 3 Gloom trong 8 phut, qua gio dem lai
            p.idMainQuest = idGio;
            p.mainQuest = q(idGio, (byte) 0, NewCharQuest.WORKING);
            final NewCharQuest q3 = p.mainQuest;
            HanhDongNhiemVuGame.khiHaQuai(p, quai(20));
            HanhDongNhiemVuGame.khiHaQuai(p, quai(7));
            HanhDongNhiemVuGame.khiHaQuai(p, quai(20));
            Kiem.bang("ha tinh gio: 2 Gloom (Skorupi khong tinh)", Integer.valueOf(2), Integer.valueOf(q3.monsterKilled[0]));
            HanhDongNhiemVuGame.datBatDauThu(p, (byte) 0, com.htbb.pokemon.DongHo.bay() - 9 * 60000L);
            HanhDongNhiemVuGame.khiHaQuai(p, quai(20));
            Kiem.bang("qua 8 phut: dem lai tu con nay", Integer.valueOf(1), Integer.valueOf(q3.monsterKilled[0]));
            HanhDongNhiemVuGame.khiHaQuai(p, quai(20));
            Kiem.bang("chua du 3: van dang lam", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q3.state_quest));
            HanhDongNhiemVuGame.khiHaQuai(p, quai(20));
            Kiem.dung("3 Gloom trong gio: xong", xong(q3));

            // 4. nhiem vu phu: noi chuyen khong xong, mac do moi xong
            p.mainQuest = new NewCharQuest(true);
            p.subQuest = q(idPhu, (byte) 1, NewCharQuest.WORKING);
            final NewCharQuest q4 = p.subQuest;
            tra(p, idPhu, (byte) 1);
            Kiem.bang("phu: nghe NPC khong tra", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q4.state_quest));
            HanhDongNhiemVuGame.suKien(p, "nang_cap", "100", 1);
            Kiem.dung("phu: mac 1 mon -> xong (da tra: khe phu trong lai)", xong(q4) || q4.id_quest == -1 && p.subQuest.id_quest == -1);
            if (phuCu == null) {
                real.Map.allSubQuest.remove(Short.valueOf(idPhu));
            } else {
                real.Map.allSubQuest.put(Short.valueOf(idPhu), phuCu);
            }
            HanhDongNhiemVuGame.khiThoat(p);
        } finally {
            for (final Map.Entry<Short, InfoQuestTemplate> e : cuChinh.entrySet()) {
                if (e.getValue() == null) {
                    real.Map.allMainQuest.remove(e.getKey());
                } else {
                    real.Map.allMainQuest.put(e.getKey(), e.getValue());
                }
            }
            for (final Map.Entry<Integer, real.MonsterTemplate> e : cuMau.entrySet()) {
                if (e.getValue() == null) {
                    real.Map.monsterTemplates.remove(e.getKey());
                } else {
                    real.Map.monsterTemplates.put(e.getKey(), e.getValue());
                }
            }
            HanhDongNhiemVuGame.dat(null);
            com.htbb.pokemon.LauDai.dat(lauDaiCu);
            real.Map.gemTemplate = gemCu;
        }
    }

    // ------------------------------------------------------------------ ma nguon: diem noi

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
    }

    static void maNguon() throws Exception {
        final String ch = doc("src/real/Char.java");
        Kiem.dung("Char: tra -64 chinh + phu qua HanhDongNhiemVuGame.chuaXong",
                ch.contains("HanhDongNhiemVuGame.chuaXong(this.mainQuest, info)") && ch.contains("HanhDongNhiemVuGame.chuaXong(this.subQuest, info)"));
        final String ev = doc("src/real/NhiemVuEngine.java");
        Kiem.dung("NhiemVuEngine: su kien + ha quai + thoat noi HanhDongNhiemVuGame", ev.contains("HanhDongNhiemVuGame.suKien(p, kieu, thamSo, n);")
                && ev.contains("HanhDongNhiemVuGame.khiHaQuai(p, mt);") && ev.contains("HanhDongNhiemVuGame.khiThoat(p);"));
        Kiem.dung("NoiDungLauDai: nap buoc hanh dong", doc("src/real/NoiDungLauDai.java").contains("HanhDongNhiemVuGame.nap();"));
        Kiem.dung("diem moc that: nang cap +k (NangCapLauDaiGame) / mac do (DiemMocNhiemVu)",
                doc("src/real/NangCapLauDaiGame.java").contains("NhiemVuEngine.suKien(p, \"nang_cap_cong\", String.valueOf(k), 1);")
                && doc("src/real/DiemMocNhiemVu.java").contains("ban(p, \"nang_cap\", String.valueOf(idTemplate), 1);"));
        Kiem.dung("RoiLauDai: ha quai goi NhiemVuEngine.khiHaQuai", doc("src/real/RoiLauDai.java").contains("NhiemVuEngine.khiHaQuai(p, mt);"));
        Kiem.dung("MucTieuNhiemVuGame: tien do buoc hanh dong trong so", doc("src/real/MucTieuNhiemVuGame.java").contains("HanhDongNhiemVuGame.tienDo("));
    }
}
