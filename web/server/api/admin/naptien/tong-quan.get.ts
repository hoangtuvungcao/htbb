/**
 * Tổng quan / đối soát nạp tiền: thống kê theo trạng thái, đơn đã thanh toán > 10 phút chưa
 * được cộng, đơn đã cộng nhưng server chưa lưu nhân vật, webhook lỗi 7 ngày, tiền theo ngày,
 * sổ cái, nhịp server game, cảnh báo giấy phép.
 */
export default defineAdminHandler(async () => {
  const n = useHtbbConfig().nap
  const canhBao = {
    napBat: n.bat,
    daCoGiayPhep: n.daCoGiayPhep,
    sandbox: n.sandbox,
    sepay: n.sepayApiKey !== '',
    casso: n.cassoSecret !== '',
    taiKhoanMacDinh: n.accountNo === '0000000000',
    yeuCauBat: n.yeuCauBat,
    lyDoTat: n.lyDoTat,
    jobSepay: n.bat && n.sepayApiToken !== '',
  }
  if (!(await napCoBang())) return { coBang: false, canhBao }
  const ds = await napDoiSoat()
  const nhip = /bat;(\d+)/.exec(ds.cauHinh['trang_thai.server'] ?? '')
  return {
    coBang: true,
    canhBao,
    ...ds,
    serverGame: nhip ? { lanCuoiMs: Number(nhip[1]), soGiay: Math.round((Date.now() - Number(nhip[1])) / 1000) } : null,
    tran: await napTran(),
  }
})
