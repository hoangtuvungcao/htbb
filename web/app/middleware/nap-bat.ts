/**
 * Trang nạp tiền chỉ mở khi server bật HTBB_NAP_BAT (mặc định tắt trên nhánh Pokémon,
 * quyết định Q10). Chặn thật nằm ở server/middleware/tat-nap.ts; đây chỉ để không hiện
 * một trang không dùng được.
 *
 * Bản DEMO (HTBB_NAP_DEMO=1): người được xem demo (useNapDemo) vào được trang — banner DEMO, không QR thật.
 */
export default defineNuxtRouteMiddleware(async () => {
  const { data: site } = await useSite()
  if (site.value?.napBat) return
  if (import.meta.client && (await useNapDemo().tai()).demo) return
  return abortNavigation(createError({
    statusCode: 403,
    statusMessage: 'Máy chủ này phi thương mại nên tính năng nạp tiền đã tắt.',
    fatal: true,
  }))
})
