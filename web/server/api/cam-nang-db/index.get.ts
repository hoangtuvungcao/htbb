/** Ghi đè + trang thêm của cẩm nang (cho thanh bên, tìm kiếm). */
export default defineEventHandler(async () => (await dsTrangDb()).map(t => ({ slug: t.slug, ten: t.ten, nhom: t.nhom, icon: t.icon, moTa: t.moTa, tuKhoa: t.tuKhoa, thuTu: t.thuTu, hien: t.hien, coSan: t.coSan, muc: t.muc })))
