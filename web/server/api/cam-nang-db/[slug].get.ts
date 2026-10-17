/** Một trang cẩm nang trong CSDL (nội dung Markdown đã chuyển HTML an toàn). null nếu không có. */
export default defineEventHandler(async event => (await trangDb(String(getRouterParam(event, 'slug') ?? ''))) ?? null)
