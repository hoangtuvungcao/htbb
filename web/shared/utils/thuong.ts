/**
 * Bảng các loại phần thưởng của giftcode, bộ kiểm tra cú pháp, và phần THUẦN (không
 * đụng CSDL) của danh mục bộ chọn vật phẩm.
 *
 * Nằm ở shared/utils nên CHẠY Y HỆT ở cả hai phía:
 *   - trình duyệt: báo lỗi cú pháp ngay khi gõ / chọn (chỉ để tiện);
 *   - máy chủ   : chốt chặn THẬT trước khi ghi xuống bảng giftcode.
 * Tệp này cố ý không dùng auto-import nào: nó tự đứng được, không phụ thuộc Nuxt.
 *
 * Nguồn sự thật là src/data/DatabaseGiftCode.java của repo game (khối if-else từ
 * dòng ~333). Sửa chuỗi phần thưởng ở đó thì nhớ sửa cả bảng dưới đây.
 */

/** [số phần tối thiểu (kể cả tên loại), cú pháp, mô tả, ví dụ đã kiểm qua kiemTraItem()] */
export type LoaiThuongDong = readonly [number, string, string, string]

/**
 * Các loại phần thưởng server hiểu, kèm số phần tối thiểu (kể cả tên loại).
 * Thiếu phần là server ném lỗi giữa chừng: người chơi đã nhận được nửa số quà
 * mà dòng giftcode_log lẫn việc trừ lượt đều không chạy — nhận lại được vô hạn.
 *
 * Ghi chú các bẫy (đã đọc từ mã nguồn Java của server):
 *  - EGG: tên loại gây hiểu sai. createTrungPhuongHoang() chỉ làm đúng một việc:
 *    ++potions[n] cho BỐN giá trị loai, không có nhánh else — loai khác thì người
 *    chơi KHÔNG nhận gì mà vẫn thấy "Chúc mừng… nhận được trứng phượng hoàng". Nó
 *    cũng không gửi createCharInventoryMessage nên hành trang không tự làm mới.
 *    Dùng PT:<id>:<số lượng> thì vừa đủ mọi id vừa làm mới đúng.
 *  - VKTB/VKTBMAX (05/09/2026): tham số is8x luôn là true ở cả hai nhánh, nhưng thân
 *    hàm createVuKhiThoiTrangTHANBINH_NEW không hề đọc is8x — iditem cố định
 *    "758 + charClass" (bậc thấp nhất, level 60). Muốn chọn đúng bậc 60/70/80 thì PHẢI
 *    dùng VKTBLV — đường DUY NHẤT còn đúng. Xem test/KiemGiftcodeVuKhiThanBinh.java (repo game).
 *  - PP/PP8X tự chọn id trong danh sách cố định {584..597,840} (PP8X ép về 593) —
 *    KHÔNG BAO GIỜ ra id 750. Muốn đúng Phi Phong Huyền Vũ thì dùng PPR/PPV/PPRK/PPVK
 *    với id=750. Tên "PPV" gây hiểu sai: PPR/PPV gọi CÙNG createtemCoat(), khác nhau
 *    DUY NHẤT ở cờ ismax — vĩnh viễn hay có hạn là do thoiHan (phần thứ 4) quyết định.
 *    "khang" (PPRK/PPVK) CHỈ có tác dụng thật với id=750 (Item.createAttributeItemCoat).
 *  - DIEMDT: cùng kho điểm mà NPC "đổi thưởng" tiêu (Char.diemDoiThuong). Trần 1000
 *    điểm/ngày chỉ áp cho nguồn ĐÁNH QUÁI, không chặn phát bằng giftcode.
 */
const LOAI_THUONG: Record<string, LoaiThuongDong> = {
  ITEM: [3, "ITEM:id:soLuong", "Vật phẩm theo id template", "ITEM:848:1"],
  DB: [4, "DB:id:soLuong:thoiHan", "Vật phẩm, thời hạn tính bằng phút (0 = vĩnh viễn)", "DB:905:1:0"],
  GEM: [3, "GEM:idNgoc:soLuong:khoa", "Ngọc; phần khoá < 0 là ngọc khoá", "GEM:73:100:1"],
  PT: [3, "PT:idBinh:soLuong", "Bình / vật phẩm trong ô potion", "PT:9:100"],
  PET: [4, "PET:id:soLuong:thoiHan", "Thú nuôi có thời hạn", "PET:12:1:0"],
  PETV: [2, "PETV:id", "Thú nuôi vĩnh viễn", "PETV:12"],
  EGG: [3, "EGG:loai:soLuong", "Chỉ 4 loại: 16 = Phượng hoàng, 17 = Trứng PH băng, 18 = Trứng hoả kỳ lân, 19 = Sách kỹ năng pet (không phải trứng). Loại khác: không nhận gì. Nên dùng PT thay thế", "EGG:17:1"],
  DH: [4, "DH:idDanhHieu:soLuong:thoiHan", "Danh hiệu; id 0 = ngẫu nhiên", "DH:8767:1:0"],
  DANHHIEU2: [4, "DANHHIEU2:id:soLuong:thoiHan", "Danh hiệu (bản 2)", "DANHHIEU2:8767:1:0"],
  HLT: [3, "HLT:id:soLuong", "Huyết linh thảo", "HLT:0:10"],
  TRANGPHUC: [4, "TRANGPHUC:id:soLuong:soNgay", "Trang phục thời trang", "TRANGPHUC:833:1:30"],
  TT: [5, "TT:id:soLuong:soNgay:khoa", "Thời trang (0 ngày = vĩnh viễn)", "TT:833:1:0:0"],
  CAITRANG: [4, "CAITRANG:id:soLuong:plus", "Cải trang — 903 Bát Giới, 904 Sa Tăng", "CAITRANG:903:1:10"],
  TBT: [4, "TBT:level:soBo:thoiHan", "Bộ 5 trang bị thú (Giáp/Hộ uyển/Nón/Bàn đạp/Yên cương) cùng cấp, LUÔN Hoàn Mỹ - Nhất phẩm (ItemChangeColor.createItemColorAnimal, atb tính lại đúng công thức tier đó, không phải đồ Trắng thô) — level PHẢI đúng 30/35/…/85 (khác thì lặng lẽ không tạo gì); hệ ngũ hành (heItem) tự gán theo đúng chuỗi kích của checkKick5AttAnimal() (14→18→17→15→16→14) nên CẢ 5 MÓN đều kích được ngũ hành của món trước, không món nào bị bỏ sót", "TBT:60:1:0"],
  AOTT: [4, "AOTT:id:_:thoiHan", "Áo thời trang mua ở shop", "AOTT:265:_:0"],
  AOSHOP: [4, "AOSHOP:id:_:thoiHan", "Áo thời trang rơi thẳng vào hành trang", "AOSHOP:725:1:0"],
  MATNA: [4, "MATNA:option:_:thoiHan", "Mặt nạ, option -1 = ngẫu nhiên", "MATNA:-1:_:43200"],
  MATNA2: [4, "MATNA2:id:_:thoiHan", "Mặt nạ theo id, option ngẫu nhiên", "MATNA2:837:_:43200"],
  MATNAID: [5, "MATNAID:id:option:thoiHan:khoa", "Mặt nạ theo id, TỰ CHỌN option (0-4, tương ứng mức +5%/+5%/+10%/+30%/+50% có sẵn của MATNA/MATNA2 — option 4 là \"Thuộc tính Lân\", xem Item.createOptionMatNa) và TỰ CHỌN khoá (1 = khoá, 0 = không khoá — MATNA/MATNA2 không có tuỳ chọn này, luôn khoá)", "MATNAID:745:4:0:1"],
  MNT: [2, "MNT:thoiHan", "Mặt nạ thỏ (837)", "MNT:30"],
  MNC: [2, "MNC:thoiHan", "Mặt nạ cáo (838)", "MNC:30"],
  VKTT: [4, "VKTT:_:_:thoiHan", "Vũ khí thời trang", "VKTT:_:_:0"],
  VKMAX: [4, "VKMAX:_:_:thoiHan", "Vũ khí thời trang max chỉ số", "VKMAX:_:_:0"],
  VKID: [3, "VKID:id:thoiHan", "Vũ khí thời trang theo id", "VKID:715:0"],
  VKTB: [4, "VKTB:_:_:thoiHan", "Vũ khí thần binh — LƯU Ý: dù tên gợi ý \"8x\" nhưng hiện chỉ tạo được bậc thấp nhất (level 60, do is8x bị bỏ quên trong code). Muốn chọn đúng bậc 70/80 thì dùng VKTBLV bên dưới", "VKTB:_:_:0"],
  VKTBMAX: [4, "VKTBMAX:_:_:thoiHan", "Vũ khí thần binh max chỉ số — CÙNG LỖI như VKTB ở trên: chỉ ra được bậc level 60. Muốn \"8x\" (level 80) + max chỉ số thì dùng VKTBLV:80:...", "VKTBMAX:_:_:0"],
  VKTBLV: [4, "VKTBLV:level:tinhLuyen:soNgay", "Vũ khí thần binh CHỌN ĐÚNG BẬC — level PHẢI đúng 60, 70 hoặc 80 (giá trị khác thì server lặng lẽ không tạo gì cả); tinhLuyen 0-99 (0 = không); LUÔN max chỉ số (isMax cố định true). Đây là cách duy nhất tạo \"vũ khí thần binh 8x max chỉ số\": level=80", "VKTBLV:80:0:0"],
  NLT: [4, "NLT:_:_:thoiHan", "Nguyệt linh trượng", "NLT:_:_:0"],
  NLTMAX: [4, "NLTMAX:_:_:thoiHan", "Nguyệt linh trượng max", "NLTMAX:_:_:0"],
  TK: [4, "TK:id:_:thoiHan", "Thần khí — id là id vũ khí RIÊNG của từng lớp nhân vật, mã dùng chung nhiều lớp sẽ lỗi giữa chừng và mất phần thưởng phía sau (xem vps/sql-giftcode.md)", "TK:890:_:0"],
  TKMAX: [4, "TKMAX:id:_:thoiHan", "Thần khí max — cùng lưu ý id theo lớp như TK", "TKMAX:890:_:0"],
  TKF: [5, "TKF:id:soLuong:mau:khoa", "Thần khí full — id 890/891/892", "TKF:890:1:0:0"],
  TKMAXF: [6, "TKMAXF:id:soLuong:soDong:he:khoa", "Thần khí max full, số dòng 1–29, hệ -1..4", "TKMAXF:890:1:9:-1:0"],
  PP: [4, "PP:_:_:thoiHan", "Phi phong max bạo kích (cả 2 hệ) — tự chọn 1 trong 15 id cố định theo cấp độ (584-597, 840), KHÔNG BAO GIỜ ra Phi Phong Huyền Vũ (id 750). Muốn tặng đúng Huyền Vũ thì dùng PPV ở dưới với id=750", "PP:_:_:0"],
  PP8X: [4, "PP8X:_:_:thoiHan", "Phi phong 8x max — CỐ ĐỊNH ra id 593, cũng không phải Huyền Vũ. Xem PPV:750 ở dưới", "PP8X:_:_:0"],
  PPR: [5, "PPR:id:soLuong:thoiHan:he", "Phi phong theo id, chỉ số NGẪU NHIÊN — he: 0=phép, 1=vật lý; thoiHan 0=vĩnh viễn (không phải PPV mới vĩnh viễn — xem PPV). vd id 750 = Phi Phong Huyền Vũ", "PPR:750:1:0:0"],
  PPV: [5, "PPV:id:soLuong:thoiHan:he", "Phi phong theo id, chỉ số MAX (khác PPR đúng một chỗ này, KHÔNG phải \"vĩnh viễn\" — vĩnh viễn do thoiHan=0 quyết định, giống hệt PPR). vd id 750 = Phi Phong Huyền Vũ max chỉ số", "PPV:750:1:0:0"],
  PPRK: [6, "PPRK:id:soLuong:thoiHan:he:khang", "Phi phong chọn hệ kháng, chỉ số ngẫu nhiên — LƯU Ý: khang CHỈ có tác dụng thật với id=750 (Phi Phong Huyền Vũ); id khác thì bị bỏ qua hoàn toàn, không có gì xảy ra. khang: 0=hoả,1=thuỷ,2=mộc,3=kim,4=thổ, -1=ngẫu nhiên", "PPRK:750:1:0:0:2"],
  PPVK: [6, "PPVK:id:soLuong:thoiHan:he:khang", "Phi phong chọn hệ kháng, chỉ số MAX — cùng lưu ý như PPRK: chỉ có tác dụng thật với id=750 (Phi Phong Huyền Vũ)", "PPVK:750:1:0:0:2"],
  PPLUCK: [5, "PPLUCK:id:_:_:_", "Phi phong may mắn", "PPLUCK:593:1:1:1"],
  HOVELAN: [4, "HOVELAN:id:_:thoiHan", "Hộ vệ lệnh lân", "HOVELAN:882:5:0"],
  CHOI: [5, "CHOI:id:_:thoiHan:_", "Chổi", "CHOI:1:_:0:_"],
  HKL: [2, "HKL:thoiHan", "Hoả kỳ lân", "HKL:30"],
  BT: [4, "BT:_:_:thoiHan", "Pet bạch thú", "BT:_:_:0"],
  RONGBANG: [2, "RONGBANG:thoiHan", "Rồng băng", "RONGBANG:0"],
  SUTU: [2, "SUTU:thoiHan", "Sư tử", "SUTU:0"],
  SUTUBANG: [2, "SUTUBANG:thoiHan", "Sư tử băng", "SUTUBANG:0"],
  LANT: [2, "LANT:thoiHan", "Lân", "LANT:0"],
  DBT: [2, "DBT:thoiHan", "Poro", "DBT:0"],
  DK: [1, "DK", "Đường khang", "DK"],
  DKB: [2, "DKB:thoiHan", "Đường khang băng", "DKB:0"],
  DKT: [2, "DKT:thoiHan", "Đường khang có thời hạn", "DKT:0"],
  TL: [2, "TL:thoiHan", "Tuần lộc", "TL:0"],
  PHDS: [1, "PHDS", "Phượng hoàng đa sắc vĩnh viễn", "PHDS"],
  PHT: [2, "PHT:thoiHan", "Phượng hoàng", "PHT:30"],
  PHDST: [2, "PHDST:thoiHan", "Phượng hoàng đa sắc", "PHDST:30"],
  PHMT: [2, "PHMT:thoiHan", "Phượng hoàng mộc", "PHMT:30"],
  PHKT: [2, "PHKT:thoiHan", "Phượng hoàng kim", "PHKT:30"],
  PHTT: [2, "PHTT:thoiHan", "Phượng hoàng thổ", "PHTT:30"],
  PHBT: [2, "PHBT:thoiHan", "Phượng hoàng băng", "PHBT:30"],
  DN: [3, "DN:_:diemNap", "Cộng điểm nạp VIP", "DN:500000:500000"],
  MN: [3, "MN:_:luongNap", "Cộng lượng nạp", "MN:_:500000"],
  DIEMDT: [2, "DIEMDT:soDiem", "Cộng điểm đổi thưởng (NPC “đổi thưởng”)", "DIEMDT:500"],
}

export function loaiThuong(): Record<string, LoaiThuongDong> {
  return LOAI_THUONG
}

/**
 * Chỉ bỏ " \t\n\r\0\x0B" ở hai đầu. Cố ý KHÔNG dùng String.prototype.trim(): hàm đó bỏ
 * cả khoảng trắng Unicode (NBSP, U+2028...), làm một chuỗi dán từ nơi khác "trông sạch"
 * sau khi cắt — trong khi ký tự lạ đó lẽ ra phải để bộ lọc ký tự phía sau bắt được.
 */
function catKhoangTrang(s: string): string {
  return s.replace(/^[ \t\n\r\0\x0B]+|[ \t\n\r\0\x0B]+$/g, '')
}

/**
 * Kiểm tra chuỗi phần thưởng. Trả về [chuoiDaChuan, mangLoi].
 *
 * VÌ SAO PHẢI LỌC KÝ TỰ CHẶT: server ghép thẳng chuỗi phần thưởng vào câu SQL khi
 * ghi nhật ký nhận mã (`insert into giftcode_log set ... item = '<item>'`), không
 * dùng tham số. Một dấu nháy đơn lọt vào đây là hỏng câu lệnh của server.
 */
export function kiemTraItem(itemVao: string): [string, string[]] {
  const item = catKhoangTrang(String(itemVao ?? ''))
  if (item === '') return ['', []]
  const loi: string[] = []
  if (!/^[A-Za-z0-9:,_\- ]+$/.test(item)) {
    loi.push('Chuỗi phần thưởng chỉ được chứa chữ, số, dấu hai chấm, dấu phẩy, gạch dưới và dấu trừ. '
      + 'Dấu nháy hay ký tự lạ sẽ làm hỏng câu SQL mà server tự ghép khi ghi nhật ký nhận mã.')
    return [item, loi]
  }

  const bang = LOAI_THUONG
  const phan: string[] = []
  for (const motTho of item.split(',')) {
    const mot = catKhoangTrang(motTho)
    if (mot === '') continue
    const tach = mot.split(':').map(catKhoangTrang)
    const loaiPT = tach[0]!.toUpperCase()

    const dong = Object.prototype.hasOwnProperty.call(bang, loaiPT) ? bang[loaiPT] : undefined
    if (!dong) {
      loi.push(`Không có loại phần thưởng “${loaiPT}”. Server sẽ bỏ qua nó, người chơi mất quà mà không báo lỗi.`)
      phan.push(mot)
      continue
    }
    const [toiThieu, cuPhap] = dong
    if (tach.length < toiThieu) {
      loi.push(`“${mot}” thiếu tham số — đúng phải là ${cuPhap} (${toiThieu} phần, đang có ${tach.length}).`)
    } else {
      // Từng phần sau loại PHẢI là số nguyên (có thể âm, vd GEM:...:khoa). Server đọc
      // bằng Integer.parseInt(tachTime[i]) — một phần không phải số ném
      // NumberFormatException giữa lúc phát thưởng. Từ vá 03/09/2026 server tự trừ lượt
      // và ghi lượt-đã-dùng TRƯỚC khi phát nên không còn nhận lại vô hạn được, nhưng
      // CHÍNH người chơi đó mất quyền lợi (mã coi như đã dùng mà không nhận được gì) —
      // báo ngay từ lúc nhập để admin sửa trước.
      for (let i = 1; i < tach.length; i++) {
        if (tach[i] === '_') continue // cho phép "_" làm tham số bỏ qua, dùng ở nhiều cú pháp trên
        if (!/^-?[0-9]+$/.test(tach[i]!)) {
          loi.push(`“${mot}”: phần thứ ${i + 1} ("${tach[i]}") phải là số nguyên (hoặc "_" để bỏ qua nếu cú pháp cho phép) — đúng phải là ${cuPhap}.`)
        }
      }
    }
    tach[0] = loaiPT
    phan.push(tach.join(':'))
  }
  return [phan.join(','), loi]
}

// ============================================================================
//  Phần thuần của danh mục bộ chọn vật phẩm
// ============================================================================

/** Số ô thuốc của nhân vật — phải khớp Char.SO_O_THUOC bên Java. */
export const DM_SO_O_THUOC = 162

/**
 * 12 id mảnh sưu tập Bát Giới / Sa Tăng — `ITEM:id:soLuong` cộng dồn số lượng này
 * vào MỘT item duy nhất (`otherAtt[65]`, trần 30000) thay vì tạo nhiều item rời như
 * id thường (từ 31/08/2026, xem `Char.createItemSoLuong`). Trước đó bộ chọn khoá ô
 * "Số lượng" về 1 cho id khác vì server chỉ tạo đúng 1 món — lỗi đã sửa tận gốc ở
 * Java, ô khoá đã bỏ. Giữ lại vì `Item.isManhSuuTap()` vẫn cần đúng 12 id này.
 */
export function dmIdManhSuuTap(): number[] {
  return [848, 849, 850, 851, 852, 853, 854, 855, 856, 857, 858, 859]
}

/**
 * Các `data_item.type` bị LOẠI khỏi bộ chọn: đồ người chơi tự chế ở NPC thợ rèn
 * thần bí (MapCheTao.doCreateItem). Không phải thứ đem phát bằng giftcode; giữ lại
 * thì danh sách phồng từ 183 lên 910 món và mấy món thật sự cần phát bị chìm mất.
 *
 *   Vũ khí        3..7    Map.isWeapone()
 *   Trang bị      0,1,2   áo, quần, nón;  8..11 nhẫn, dây chuyền, giày, găng;  12 ngọc trang bị
 *   Trang bị thú  14..18  Map.isWearingAnimal()
 *
 * Vẫn phát được nếu thật sự cần: gõ tay `ITEM:<id>:<số lượng>`.
 */
export function dmLoaiTypeDoChe(): number[] {
  return [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18]
}

/**
 * Các id trong `data_item` mà CỘT `type` KHÔNG KHỚP HÀNH VI THẬT của id đó trong
 * game — loại khỏi MỌI nhóm của bộ chọn (kể cả nhóm bắt hết "Vật phẩm khác"):
 *
 *   950–954  type 19 (Phi phong), tên "Skin ...". SkinLinhThu.ID_ITEM chỉ có
 *            {905..909}; 950–954 có style = 0 y hệt "Phi phong Lăng Vân" (584) — là một
 *            phi phong THƯỜNG, chỉ là tên hứa đổi hình thú cưỡi mà nó không làm.
 *   728      type 27 (Thú nuôi), tên "Gậy Giáng Sinh". Mọi thú nuôi thật có atb0 = 5;
 *            728 thì không, và Item.java có GAY_GIANG_SINH = 728 xử lý nó như VŨ KHÍ lễ hội.
 *   883–886  type 21, "trứng giả" — bản sao đổi tên của Hộ vệ lệnh (630-638), ITEM: của
 *            chúng mở menu thuê hộ vệ ngẫu nhiên. Trứng THẬT là ô thuốc PT:71/72/115/74.
 *            (Đã xoá khỏi data_item 05/09/2026; id vẫn giữ ở đây phòng ai lỡ chèn lại.)
 *            KHÔNG loại 887 "Trứng rồng băng": Item.isTrungRongBang() bắt đúng 887.
 *
 * Danh sách CHỌN TAY dựa trên bằng chứng đọc mã nguồn — không tự dò theo atb0/tên vì
 * sẽ bắt nhầm món hợp lệ. Vẫn phát được 728/950-954 bằng gõ tay ITEM:<id>:<số lượng>.
 */
export function dmIdRac(): number[] {
  return [728, 883, 884, 885, 886, 950, 951, 952, 953, 954]
}

/**
 * Id năm viên "Cánh" (Lam Băng, Bạch Linh, Xích Vũ, Hồng Vân, Tử Ảnh). Từng bị gộp
 * nhầm vào "Mảnh cải trang" vì cùng type 29, nhưng Cánh là hệ trang trí RIÊNG
 * (`Item.isCanh()` = 898..902, `Char.wModel.CanhModel`). Không phải hàng lỗi, chỉ
 * xếp sai nhóm — nên tách nhóm riêng chứ không loại.
 */
export function dmIdCanh(): number[] {
  return [898, 899, 900, 901, 902]
}

/** Tên nhóm theo `data_item.type` (hằng số Item.TYPE_*, bổ sung type Java không đặt hằng). */
export function dmTenLoaiVatPham(): Record<number, string> {
  return {
    0: 'Áo', 1: 'Quần', 2: 'Nón / khăn',
    3: 'Kiếm', 4: 'Đại đao', 5: 'Bút',
    6: 'Búa', 7: 'Cung', 8: 'Nhẫn',
    9: 'Dây chuyền', 10: 'Giày', 11: 'Găng',
    12: 'Ngọc trang bị', 13: 'Cuốc / chổi', 14: 'Giáp thú cưỡi',
    15: 'Hộ uyển thú cưỡi', 16: 'Nón thú cưỡi', 17: 'Bàn đạp thú cưỡi',
    18: 'Yên cương thú cưỡi', 19: 'Phi phong', 20: 'Rương vận tiêu',
    21: 'Hộ vệ lệnh / trứng thú cưỡi', 22: 'Thẻ mua bán',
    23: 'Xa tiêu lệnh', 24: 'Lệnh bài trấn yêu', 25: 'Hoả kỳ lân',
    26: 'Trứng thú', 27: 'Thú nuôi', 28: 'Đan / thảo dược',
    29: 'Mảnh cải trang', 30: 'Skin thú cưỡi',
  }
}

/**
 * Tên có chứa từ này không, bỏ qua hoa/thường. Các lời gọi truyền từ khoá BỎ CHỮ ĐẦU
 * (`dmCoTu(ten, 'rứng')`) để khớp cả "Trứng" lẫn "trứng" bất kể cách gập chữ hoa;
 * toàn bộ tên trong data_item / data_potion đều viết "Trứng" (đã đối chiếu dữ liệu thật).
 */
export function dmCoTu(ten: string, tu: string): boolean {
  return tu !== '' && ten.toLowerCase().includes(tu.toLowerCase())
}

/**
 * Bỏ xuống dòng trong tên để hiện gọn một dòng. Cột `name` của game vừa là TÊN vừa
 * là MÔ TẢ, xuống dòng bằng ký tự thật (xem "Áo Nghê Thường").
 */
export function dmMotDong(ten: string | null | undefined): string {
  let s = String(ten ?? '').replace(/\r\n|\r|\n/g, ' · ')
  s = s.replace(/\s+/gu, ' ')
  return catKhoangTrang(s)
}

/** Một lựa chọn của ô chọn trong bộ chọn (giữ đúng thứ tự — mảng, không phải object). */
export interface LuaChonTruong {
  value: string
  label: string
}

/**
 * Một tham số cần điền cho loại phần thưởng, theo đúng thứ tự tham số của server.
 *   ma       - tên trường
 *   nhan     - nhãn hiện ra
 *   macDinh  - giá trị mặc định
 *   minSo    - sàn của ô số (mặc định '1'); '0' cho "0 = vĩnh viễn"
 *   chon     - nếu có, danh sách lựa chọn thay cho ô số
 *   an       - tham số server BẮT BUỘC có trong chuỗi nhưng KHÔNG đọc giá trị
 *              (vd "_" của TK/TKMAX): không hiện ô nhập, chỉ gắn giá trị cố định.
 */
export interface TruongThuong {
  ma: string
  nhan?: string
  macDinh: string
  minSo?: string
  chon?: LuaChonTruong[]
  an?: boolean
  khongCheo?: boolean
}

const KHOA_DUONG: LuaChonTruong[] = [
  { value: '1', label: 'Khoá — không giao dịch được (nên dùng)' },
  { value: '0', label: 'Không khoá' },
]

/**
 * Các trường cần điền cho từng loại phần thưởng bộ chọn sinh ra.
 * Nguồn sự thật là src/data/DatabaseGiftCode.java.
 */
export function dmTruongCuaLoai(loai: string): TruongThuong[] {
  switch (loai) {
    case 'ITEM':
      // ITEM:id:soLuong -> Char.createItemSoLuong. KHOÁ SẴN, không có tham số để mở:
      // Char.java đặt `newItem.lock = 1` — quà ITEM luôn không giao dịch được.
      return [{ ma: 'sl', nhan: 'Số lượng', macDinh: '1' }]

    case 'DB':
      // DB:id:soLuong:thoiHan(phút) -> lặp soLuong lần GiftCode.AddItem(p, id, 0, 0, phút).
      // Có THỜI HẠN (phút, 0 = vĩnh viễn) và đi qua đúng đường mọi nguồn phát skin dùng.
      // Dùng cho nhóm "Skin thú cưỡi" từ 12/09/2026. Khoá sẵn như ITEM.
      return [
        { ma: 'sl', nhan: 'Số lượng', macDinh: '1' },
        { ma: 'phut', nhan: 'Thời hạn (phút, 0 = vĩnh viễn)', macDinh: '0', minSo: '0' },
      ]

    case 'PT':
      // PT:id:soLuong -> player.potions[id] += soLuong. Không có khái niệm khoá.
      return [{ ma: 'sl', nhan: 'Số lượng', macDinh: '1' }]

    case 'GEM':
      // GEM:id:soLuong:khoa -> doAddGemItem(id, soLuong, khoa < 0). Mặc định KHOÁ.
      // Ô này KHÔNG được để trống: thiếu phần thứ tư thì parseInt ném lỗi và khối catch
      // bên Java đặt khoá = true — tình cờ đúng, nhưng dựa vào một khối catch là thứ đổi
      // nghĩa ngay lần ai đó sửa chỗ đó. Bộ chọn luôn ghi đủ bốn phần.
      return [
        { ma: 'sl', nhan: 'Số lượng', macDinh: '1' },
        {
          ma: 'khoa', nhan: 'Khoá', macDinh: '-1', khongCheo: true, chon: [
            { value: '-1', label: 'Khoá — không giao dịch được (nên dùng)' },
            { value: '0', label: 'Không khoá' },
          ],
        },
      ]

    case 'TT':
      // TT:id:soLuong:soNgay:khoa -> Char.createThoiTrang(p, id, soNgay, khoa), lặp
      // soLuong lần. soNgay tính bằng NGÀY (khác PET/TK dùng PHÚT). `khoa` gán THẲNG vào
      // newItem.lock (1 = khoá) — KHÁC chiều dấu "âm là khoá" của GEM.
      return [
        { ma: 'sl', nhan: 'Số lượng', macDinh: '1' },
        { ma: 'ngay', nhan: 'Số ngày (0 = vĩnh viễn)', macDinh: '0', minSo: '0' },
        { ma: 'khoa', nhan: 'Khoá', macDinh: '1', khongCheo: true, chon: KHOA_DUONG },
      ]

    case 'PET':
      // PET:id:soLuong:thoiHan(phút) -> lặp Char.createPet(id, phút). id là CHỈ SỐ MẢNG
      // Map.petTemplate (0..30). Mặc định 7 ngày: 0 rơi vào nhánh hiển thị đặc biệt
      // (Pet.getTimeExpire) không hẳn "vĩnh viễn" — muốn vĩnh viễn thì dùng PETV.
      return [
        { ma: 'sl', nhan: 'Số lượng', macDinh: '1' },
        { ma: 'phut', nhan: 'Thời hạn (phút)', macDinh: '10080' },
      ]

    case 'PETV':
      // PETV:id -> Char.createPetVinhVien(id). Đúng 2 phần, server CHỈ đọc tachTime[1].
      return []

    case 'TK':
    case 'TKMAX':
      // TK:id:_:thoiHan(phút). Phần "_" là chỗ trống chưa dùng (server gọi createThanKhi
      // với ma_vat=0 ghi cứng) nhưng vẫn PHẢI có vì server truy cập tachTime[3].
      return [
        { ma: '_', an: true, macDinh: '_' },
        { ma: 'phut', nhan: 'Thời hạn (phút, 0 = vĩnh viễn)', macDinh: '0', minSo: '0' },
      ]

    case 'DH':
      // DH:id:soLuong:thoiHan(phút) -> player.addDanhHieu(id, phút) lặp soLuong lần.
      // id=0 là "ngẫu nhiên" nhưng CHỈ trong 34/48 danh hiệu.
      return [
        { ma: 'sl', nhan: 'Số lượng', macDinh: '1' },
        { ma: 'phut', nhan: 'Thời hạn (phút, 0 = vĩnh viễn)', macDinh: '0', minSo: '0' },
      ]

    case 'PPRK':
    case 'PPVK':
      // khang chỉ có tác dụng thật với id=750 (Phi Phong Huyền Vũ).
      return [
        { ma: 'sl', nhan: 'Số lượng', macDinh: '1' },
        { ma: 'phut', nhan: 'Thời hạn (phút, 0 = vĩnh viễn)', macDinh: '0', minSo: '0' },
        { ma: 'he', nhan: 'Hệ', macDinh: '0', chon: [{ value: '0', label: 'Phép' }, { value: '1', label: 'Vật lý' }] },
        {
          ma: 'khang', nhan: 'Kháng', macDinh: '-1', chon: [
            { value: '-1', label: 'Ngẫu nhiên' }, { value: '0', label: 'Hoả' }, { value: '1', label: 'Thuỷ' },
            { value: '2', label: 'Mộc' }, { value: '3', label: 'Kim' }, { value: '4', label: 'Thổ' },
          ],
        },
      ]

    case 'MATNA':
      // MATNA:option:_:thoiHan -> Map.doCreateMatNa(). "option" chính là id chọn ở ô
      // "Vật phẩm" (-1 = ngẫu nhiên). Phần "_" không được đọc nhưng vẫn phải có.
      return [
        { ma: '_', an: true, macDinh: '_' },
        { ma: 'phut', nhan: 'Thời hạn (phút, 0 = vĩnh viễn)', macDinh: '0', minSo: '0' },
      ]

    case 'MATNAID':
      // MATNAID:id:option:thoiHan:khoa -> Map.doCreateMatNa2(); id (mẫu mặt nạ) TÁCH khỏi
      // option (0-4, option 4 = "Thuộc tính Lân"); khoá TỰ CHỌN được.
      return [
        {
          ma: 'option', nhan: 'Hiệu ứng (option)', macDinh: '4', chon: [
            { value: '0', label: 'Option 0 (+5%)' }, { value: '1', label: 'Option 1 (+5%)' },
            { value: '2', label: 'Option 2 (+10%)' }, { value: '3', label: 'Option 3 (+30%)' },
            { value: '4', label: 'Thuộc tính Lân (+50%)' },
          ],
        },
        { ma: 'phut', nhan: 'Thời hạn (phút, 0 = vĩnh viễn)', macDinh: '0', minSo: '0' },
        { ma: 'khoa', nhan: 'Khoá', macDinh: '1', khongCheo: true, chon: KHOA_DUONG },
      ]
  }
  return []
}

/**
 * Ghép chuỗi đúng thứ tự tham số của máy chủ: [loai, id, ...giá trị các trường].
 * Bộ chọn trên trang chỉ ghép chuỗi qua hàm này, và chuỗi ghép ra luôn được chạy lại
 * qua kiemTraItem() trước khi cho bấm Thêm. Trường trống -> '' (nút Thêm bị khoá).
 */
export function dmGhepChuoi(loai: string, id: string | number, giaTri: Record<string, string>): string {
  const sId = String(id ?? '').trim()
  if (!loai || sId === '') return ''
  const phan = [loai, sId]
  for (const t of dmTruongCuaLoai(loai)) {
    const v = String((t.an ? t.macDinh : (giaTri[t.ma] ?? t.macDinh)) ?? '').trim()
    if (v === '') return ''
    phan.push(v)
  }
  return phan.join(':')
}

/**
 * Nối một chuỗi vào cuối ô "Phần thưởng vật phẩm". Bỏ dấu phẩy lẻ ở cuối để không
 * sinh ra phần rỗng — kiemTraItem() bỏ qua phần rỗng, nhưng chuỗi lưu lại trông như lỗi.
 */
export function dmNoiThem(cu: string, s: string): string {
  if (s === '') return cu
  const c = catKhoangTrang(cu ?? '').replace(/,+\s*$/, '')
  return c === '' ? s : `${c},${s}`
}

/**
 * Đơn vị của tham số tên "thoiHan" — ĐÃ XÁC MINH bằng cách đọc thẳng hàm Java xử lý
 * từng loại (createRongLon/createSuTu/... nhân 24*60*60000 = NGÀY; doCreateHoaKyLanTime
 * nhân 60000 = PHÚT). Loại không có ở đây: in số thô, KHÔNG bịa đơn vị.
 */
export function dmDonViThoiHanLoai(): Record<string, string> {
  return {
    HKL: 'phút', PET: 'phút', TK: 'phút', TKMAX: 'phút',
    DH: 'phút', DANHHIEU2: 'phút', MATNA: 'phút', MATNA2: 'phút',
    RONGBANG: 'ngày', SUTU: 'ngày', SUTUBANG: 'ngày',
    DKB: 'ngày', DKT: 'ngày', TL: 'ngày',
    PHT: 'ngày', PHDST: 'ngày', PHMT: 'ngày',
    PHKT: 'ngày', PHTT: 'ngày', PHBT: 'ngày',
  }
}

/** Tên mã giftcode hợp lệ: chữ, số, gạch dưới, 2–50 ký tự (server ghép thẳng vào SQL). */
export const GIFTCODE_TEN_RE = /^[A-Za-z0-9_]{2,50}$/
