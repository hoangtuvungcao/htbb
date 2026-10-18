/** Danh sách IP quản trị (shared/utils/ipChoPhep.ts). */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { docLuatIp, ipDuocPhep } from '../shared/utils/ipChoPhep.ts'

test('rỗng = cho tất cả', () => {
  assert.ok(ipDuocPhep('1.2.3.4', docLuatIp('')))
})
test('IPv4, CIDR, IPv6, ::ffff:', () => {
  const l = docLuatIp('203.0.113.5, 10.0.0.0/8 ::1')
  assert.ok(ipDuocPhep('203.0.113.5', l))
  assert.ok(!ipDuocPhep('203.0.113.6', l))
  assert.ok(ipDuocPhep('10.200.3.4', l))
  assert.ok(ipDuocPhep('::ffff:10.0.0.1', l))
  assert.ok(ipDuocPhep('::1', l))
  assert.ok(!ipDuocPhep('11.0.0.1', l))
  assert.ok(!ipDuocPhep('', l))
})
test('mục sai bị bỏ; chỉ toàn mục sai = chặn hết (không mở toang)', () => {
  const l = docLuatIp('999.1.1.1, abc')
  assert.deepEqual(l.loi, ['999.1.1.1', 'abc'])
  assert.ok(!ipDuocPhep('1.2.3.4', l))
  assert.ok(ipDuocPhep('192.168.1.9', docLuatIp('192.168.1.0/24, bad')))
  assert.ok(ipDuocPhep('8.8.8.8', docLuatIp('0.0.0.0/0')))
})
