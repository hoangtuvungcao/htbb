/**
 * Trang Log game (bảng tách theo kỳ) + trang Kinh tế (loại tiền mua shop).
 *   - logGameDsKy: server ghi tob_log_use_luong_<tuần>_<tháng>_<năm>; bảng không hậu tố chỉ là bảng mẫu (luôn rỗng).
 *   - tienMuaShop: cột money của tob_log_buy_shop là ảnh số dư trước/sau, không phải tên tiền.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { LOG_GAME_BANG, logGameCauSql, logGameDsKy } from '../server/utils/logGame.ts'
import { tienMuaShop } from '../shared/utils/kinhTeCanhBao.ts'

test('log theo kỳ: chỉ nhận đúng mẫu, mới nhất trước', () => {
  const ds = logGameDsKy('tob_log_use_luong', 'tuan', [
    'tob_log_use_luong', 'tob_log_use_luong_4_10_2026', 'tob_log_use_luong_1_11_2026', 'tob_log_use_luong_5_9_2026',
    'tob_log_use_luong_x', 'tob_log_use_luong_1_13_2026', 'tob_log_use_luongabc_1_1_2026',
  ])
  assert.deepEqual(ds.map(d => d.bang), ['tob_log_use_luong_1_11_2026', 'tob_log_use_luong_4_10_2026', 'tob_log_use_luong_5_9_2026'])
  assert.equal(ds[1]!.nhan, 'Tuần 4 · 10/2026')
  assert.deepEqual(logGameDsKy('tob_nap_xai_luong', 'thang', ['tob_nap_xai_luong_62025', 'tob_nap_xai_luong_102026']).map(d => d.bang),
    ['tob_nap_xai_luong_102026', 'tob_nap_xai_luong_62025'])
})

test('log theo kỳ: bảng kỳ chỉ thay FROM khi đúng mẫu của chính mục đó', () => {
  assert.equal(LOG_GAME_BANG.tob_log_use_luong!.theoKy, 'tuan')
  assert.equal(logGameCauSql('tob_log_use_luong', '', '', 1, 'tob_log_use_luong_4_10_2026').bangThat, 'tob_log_use_luong_4_10_2026')
  // tên lạ / tiêm SQL / kỳ của mục khác -> bảng mẫu
  assert.equal(logGameCauSql('tob_log_use_luong', '', '', 1, 'tob_log_use_luong_1_1_2026`; DROP TABLE x; --').bangThat, 'tob_log_use_luong')
  assert.equal(logGameCauSql('tob_log_all_item', '', '', 1, 'tob_log_use_luong_4_10_2026').bangThat, 'tob_log_all_item')
  // xổ số của bản gốc đã bỏ: khoá lạ lùi về bảng mặc định
  assert.equal(logGameCauSql('tob_log_xoso', '', '', 1).bangThat, 'tob_log_all_item')
})

test('mua shop: loại tiền suy từ số dư giảm', () => {
  assert.equal(tienMuaShop('Xu_40100099_40100099_Luong_20000_20000_lk_30002_29972'), 'luong_khoa')
  assert.equal(tienMuaShop('Xu_40100141_40100141_Luong_20000_19990'), 'luong')
  assert.equal(tienMuaShop('Xu_500_300_Luong_20_20'), 'xu')
  assert.equal(tienMuaShop('Xu_1_1_Luong_2_2_lk_3_3'), '')
  assert.equal(tienMuaShop('luong'), 'luong')
  assert.equal(tienMuaShop('luonglock'), 'luong_khoa')
  assert.equal(tienMuaShop(null), '')
})
