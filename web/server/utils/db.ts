/**
 * Kết nối MySQL/MariaDB.
 *
 * Hai database dùng chung với game server:
 *   - account : bảng team_user (login server CheckLoginSocket.jar kiểm tra)
 *   - kpah_db : dữ liệu game (nhân vật, kích hoạt, bảng xếp hạng...)
 *
 * Luôn dùng prepared statement (`?`). Mật khẩu được so/ghi bằng hàm PASSWORD()
 * của MariaDB ngay trong câu SQL để KHỚP với login server.
 */
import mysql from 'mysql2/promise'
import { bocPoolNovaDB } from './novadb'
import type { Pool, PoolConnection, RowDataPacket, ResultSetHeader } from 'mysql2/promise'

const pools = new Map<string, Pool>()

function taoPool(dbName: string): Pool {
  const c = useHtbbConfig()
  const laAccount = dbName === c.db.account && c.db.accountUser !== null
  // CSDL là NovaDB thì SQL đi qua lớp đệm đổi giờ/PASSWORD()/collation (xem novadb.ts)
  return bocPoolNovaDB(mysql.createPool({
    host: c.db.host,
    port: c.db.port,
    user: laAccount ? c.db.accountUser! : c.db.user,
    password: laAccount ? (c.db.accountPass ?? '') : c.db.pass,
    database: dbName,
    charset: 'utf8mb4',
    waitForConnections: true,
    connectionLimit: 10,
    // Trả DATETIME dạng chuỗi 'YYYY-MM-DD HH:MM:SS' đúng như trong DB, tránh lệch
    // múi giờ khi driver tự đổi sang Date.
    dateStrings: true,
    // BIGINT trả về số khi an toàn, chuỗi khi vượt quá Number.MAX_SAFE_INTEGER.
    supportBigNumbers: true,
    bigNumberStrings: false,
    decimalNumbers: true,
  }))
}

export function db(dbName: string): Pool {
  let p = pools.get(dbName)
  if (!p) {
    p = taoPool(dbName)
    pools.set(dbName, p)
  }
  return p
}

/** DB tài khoản (team_user). */
export const dbAccount = () => db(useHtbbConfig().db.account)
/** DB game (nhân vật, kích hoạt, BXH). */
export const dbGame = () => db(useHtbbConfig().db.game)

export type Row = RowDataPacket & Record<string, any>
type Executor = Pool | PoolConnection

/** SELECT nhiều dòng. */
export async function q<T = Row>(ex: Executor, sql: string, params: unknown[] = []): Promise<T[]> {
  const [rows] = await ex.query<RowDataPacket[]>(sql, params)
  return rows as unknown as T[]
}

/** SELECT một dòng (hoặc null). */
export async function q1<T = Row>(ex: Executor, sql: string, params: unknown[] = []): Promise<T | null> {
  const rows = await q<T>(ex, sql, params)
  return rows[0] ?? null
}

/** SELECT một giá trị (cột đầu tiên của dòng đầu tiên), hoặc null. */
export async function qv<T = any>(ex: Executor, sql: string, params: unknown[] = []): Promise<T | null> {
  const [rows] = await ex.query<RowDataPacket[]>({ sql, rowsAsArray: true }, params)
  const r = (rows as unknown as unknown[][])[0]
  return r === undefined ? null : (r[0] as T)
}

/** INSERT/UPDATE/DELETE. */
export async function exec(ex: Executor, sql: string, params: unknown[] = []): Promise<ResultSetHeader> {
  const [res] = await ex.query<ResultSetHeader>(sql, params)
  return res
}

/** Chạy trong một transaction; lỗi thì rollback rồi ném tiếp. */
export async function transaction<T>(pool: Pool, fn: (conn: PoolConnection) => Promise<T>): Promise<T> {
  const conn = await pool.getConnection()
  try {
    await conn.beginTransaction()
    const kq = await fn(conn)
    await conn.commit()
    return kq
  } catch (e) {
    try { await conn.rollback() } catch { /* bỏ qua */ }
    throw e
  } finally {
    conn.release()
  }
}

/** Bảng có tồn tại không? Nhớ kết quả trong 60 giây. */
const coBangCache = new Map<string, { co: boolean, luc: number }>()
export async function coBang(pool: Pool, ten: string): Promise<boolean> {
  const key = `${pool === dbGame() ? 'g' : 'a'}:${ten}`
  const c = coBangCache.get(key)
  if (c && Date.now() - c.luc < 60_000) return c.co
  let co = false
  try {
    // Tên bảng là hằng số trong mã nguồn, không bao giờ lấy từ người dùng.
    if (!/^[A-Za-z0-9_]+$/.test(ten)) throw new Error('ten bang khong hop le')
    await pool.query(`SELECT 1 FROM \`${ten}\` LIMIT 1`)
    co = true
  } catch {
    co = false
  }
  coBangCache.set(key, { co, luc: Date.now() })
  return co
}
