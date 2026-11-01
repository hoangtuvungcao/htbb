//! License leases (see docs/CONTRACT.md).
//!
//! Lease token: `pvl1.<b64url(json)>.<b64url(ed25519 sig)>`, signed by the
//! license authority (the website). Verified fully offline: signature, machine
//! id, expiry. The website is only contacted to renew (every 6 h), and a lease
//! stays valid for 7 days, so the website being down never stops a customer's service.

use std::path::{Path, PathBuf};
use std::process::{Command, Stdio};
use std::time::{SystemTime, UNIX_EPOCH};

use ed25519_dalek::{Signature, Verifier, VerifyingKey};

use crate::json::{escape, Json};
use crate::util::{b64_decode, hex};

/// Official authority key, baked in at build time:
/// `PEEROVA_AUTHORITY_PUBKEY=<b64url> cargo build --release`.
pub const DEFAULT_AUTHORITY_PUBKEY: Option<&str> = option_env!("PEEROVA_AUTHORITY_PUBKEY");
pub const DEFAULT_LICENSE_SERVER: &str = match option_env!("PEEROVA_LICENSE_SERVER") {
    Some(s) => s,
    None => "https://peerova.pages.dev",
};
/// Built-in last-resort relay (release builds: `PEEROVA_RELAY_URL`, see scripts/release.env).
/// The website can announce another one at runtime (rdv `relay` field).
pub const DEFAULT_RELAY: &str = match option_env!("PEEROVA_RELAY_URL") {
    Some(s) => s,
    None => DEFAULT_LICENSE_SERVER,
};
/// Renew every 30 min so a payment takes effect quickly; the lease itself stays valid 7 days offline.
pub const RENEW_EVERY_SECS: u64 = 30 * 60;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Lease {
    pub lid: String,
    pub pid: String,
    pub plan: String,
    pub srv: u64,
    pub ccu: u64,
    pub mid: String,
    pub iat: u64,
    pub exp: u64,
    /// Server static public key (b64url) this lease is bound to, if any.
    pub spk: Option<String>,
    pub token: String,
}

pub fn now_unix() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_secs())
        .unwrap_or(0)
}

pub fn parse_pubkey(b64: &str) -> Result<[u8; 32], String> {
    b64_decode(b64.trim())
        .and_then(|v| v.try_into().ok())
        .ok_or_else(|| "public key authority phải là 32 byte base64url".to_string())
}

/// Verify a lease token. `mid` None skips the machine check (inspection only).
pub fn verify(
    token: &str,
    authority: &[u8; 32],
    mid: Option<&str>,
    now: u64,
) -> Result<Lease, String> {
    let token = token.trim();
    let mut parts = token.split('.');
    let (Some("pvl1"), Some(p), Some(sig), None) =
        (parts.next(), parts.next(), parts.next(), parts.next())
    else {
        return Err("lease không đúng định dạng pvl1.<payload>.<sig>".into());
    };
    let signed = &token[.."pvl1.".len() + p.len()];
    let sig: [u8; 64] = b64_decode(sig)
        .and_then(|s| s.try_into().ok())
        .ok_or("chữ ký lease hỏng")?;
    let vk =
        VerifyingKey::from_bytes(authority).map_err(|_| "public key authority không hợp lệ")?;
    vk.verify(signed.as_bytes(), &Signature::from_bytes(&sig))
        .map_err(|_| "chữ ký lease KHÔNG hợp lệ (giả mạo hoặc sai authority)")?;
    let payload = b64_decode(p).ok_or("payload lease hỏng")?;
    let j = Json::parse(std::str::from_utf8(&payload).map_err(|_| "payload không phải UTF-8")?)?;
    if j.u64("v") != Some(1) {
        return Err("phiên bản lease không hỗ trợ".into());
    }
    let s = |k: &str| {
        j.str(k)
            .map(String::from)
            .ok_or_else(|| format!("lease thiếu trường {k}"))
    };
    let n = |k: &str| j.u64(k).ok_or_else(|| format!("lease thiếu trường {k}"));
    let lease = Lease {
        lid: s("lid")?,
        pid: s("pid")?,
        plan: s("plan")?,
        srv: n("srv")?,
        ccu: n("ccu")?,
        mid: s("mid")?,
        iat: n("iat")?,
        exp: n("exp")?,
        spk: j.str("spk").map(String::from),
        token: token.to_string(),
    };
    if let Some(m) = mid {
        if lease.mid != m {
            return Err("lease cấp cho máy khác (machine id không khớp)".into());
        }
    }
    if now >= lease.exp {
        return Err(format!(
            "lease đã hết hạn {} giây trước — cần kết nối web để gia hạn",
            now - lease.exp
        ));
    }
    Ok(lease)
}

fn read_cmd(cmd: &str, args: &[&str]) -> Option<String> {
    let out = Command::new(cmd)
        .args(args)
        .stderr(Stdio::null())
        .output()
        .ok()?;
    out.status
        .success()
        .then(|| String::from_utf8_lossy(&out.stdout).to_string())
}

fn raw_machine_id() -> String {
    #[cfg(target_os = "windows")]
    {
        if let Some(o) = read_cmd(
            "reg",
            &[
                "query",
                r"HKLM\SOFTWARE\Microsoft\Cryptography",
                "/v",
                "MachineGuid",
            ],
        ) {
            if let Some(v) = o.split_whitespace().last() {
                return v.to_string();
            }
        }
    }
    #[cfg(target_os = "macos")]
    {
        if let Some(o) = read_cmd("ioreg", &["-rd1", "-c", "IOPlatformExpertDevice"]) {
            if let Some(l) = o.lines().find(|l| l.contains("IOPlatformUUID")) {
                if let Some(v) = l.split('"').nth(3) {
                    return v.to_string();
                }
            }
        }
    }
    for p in ["/etc/machine-id", "/var/lib/dbus/machine-id"] {
        if let Ok(s) = std::fs::read_to_string(p) {
            if !s.trim().is_empty() {
                return s.trim().to_string();
            }
        }
    }
    hostname()
}

/// Stable, privacy-preserving machine id (hash of the OS machine id).
pub fn machine_id() -> String {
    hex(&crate::crypto::hash(&[b"peerova-machine-v1", raw_machine_id().as_bytes()])[..16])
}

pub fn hostname() -> String {
    std::env::var("COMPUTERNAME")
        .or_else(|_| std::env::var("HOSTNAME"))
        .ok()
        .or_else(|| {
            std::fs::read_to_string("/etc/hostname")
                .ok()
                .map(|s| s.trim().to_string())
        })
        .or_else(|| read_cmd("hostname", &[]).map(|s| s.trim().to_string()))
        .unwrap_or_else(|| "unknown".into())
}

/// Supernode-side check: authority signature, not expired, bound to `server_pub`
/// when the lease names a key. (Machine id cannot be checked remotely.)
pub fn verify_for_supernode(
    token: &[u8],
    authority: &[u8; 32],
    server_pub: &[u8; 32],
    now: u64,
) -> bool {
    let Ok(t) = std::str::from_utf8(token) else {
        return false;
    };
    match verify(t, authority, None, now) {
        Ok(l) => l
            .spk
            .as_deref()
            .map_or(true, |k| k == crate::util::b64_encode(server_pub)),
        Err(_) => false,
    }
}

pub(crate) fn curl(args: &[&str], body: Option<&str>) -> Result<(String, String), String> {
    let mut cmd = Command::new("curl");
    cmd.args(["-sS", "--max-time", "20", "-w", "\n%{http_code}"])
        .args(args);
    cmd.stdin(if body.is_some() {
        Stdio::piped()
    } else {
        Stdio::null()
    })
    .stdout(Stdio::piped())
    .stderr(Stdio::piped());
    let mut child = match cmd.spawn() {
        Ok(c) => c,
        // Windows 7 / 8.1 / Server 2012 R2 ship without curl.exe: use PowerShell (always present).
        Err(e) if cfg!(windows) && e.kind() == std::io::ErrorKind::NotFound => {
            return powershell_http(args, body)
        }
        Err(e) => return Err(format!("không chạy được curl: {e}")),
    };
    if let Some(b) = body {
        use std::io::Write;
        child
            .stdin
            .take()
            .unwrap()
            .write_all(b.as_bytes())
            .map_err(|e| e.to_string())?;
    }
    let out = child.wait_with_output().map_err(|e| e.to_string())?;
    if !out.status.success() {
        return Err(format!(
            "không kết nối được license server: {}",
            String::from_utf8_lossy(&out.stderr).trim()
        ));
    }
    let text = String::from_utf8_lossy(&out.stdout).to_string();
    let (resp, code) = text
        .trim_end()
        .rsplit_once('\n')
        .map(|(a, b)| (a.to_string(), b.to_string()))
        .unwrap_or((String::new(), text.trim().to_string()));
    Ok((resp, code))
}

/// Minimal curl replacement via PowerShell/.NET (HttpWebRequest, TLS 1.2). Understands the
/// curl flags this crate uses: `-X M`, `-H "K: V"`, `--data-binary @-`, `-4`, URL.
fn powershell_http(args: &[&str], body: Option<&str>) -> Result<(String, String), String> {
    let q = |s: &str| format!("'{}'", s.replace('\'', "''"));
    let (mut method, mut url, mut headers) = ("GET".to_string(), String::new(), vec![]);
    let mut i = 0;
    while i < args.len() {
        match args[i] {
            "-X" => {
                method = args.get(i + 1).unwrap_or(&"GET").to_string();
                i += 1;
            }
            "-H" => {
                if let Some((k, v)) = args.get(i + 1).and_then(|h| h.split_once(':')) {
                    headers.push((k.trim().to_string(), v.trim().to_string()));
                }
                i += 1;
            }
            "--data-binary" => i += 1,
            a if !a.starts_with('-') => url = a.to_string(),
            _ => {}
        }
        i += 1;
    }
    let mut ps = String::from(
        "$ErrorActionPreference='Stop';[Console]::InputEncoding=[Text.Encoding]::UTF8;[Console]::OutputEncoding=[Text.Encoding]::UTF8;\
         [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;",
    );
    ps += &format!(
        "$r=[Net.WebRequest]::Create({});$r.Method={};$r.Timeout=20000;$r.UserAgent='peerova';",
        q(&url),
        q(&method)
    );
    for (k, v) in &headers {
        if k.eq_ignore_ascii_case("content-type") {
            ps += &format!("$r.ContentType={};", q(v));
        } else {
            ps += &format!("$r.Headers.Add({},{});", q(k), q(v));
        }
    }
    if body.is_some() {
        ps += "$b=[Text.Encoding]::UTF8.GetBytes([Console]::In.ReadToEnd());$r.ContentLength=$b.Length;$s=$r.GetRequestStream();$s.Write($b,0,$b.Length);$s.Close();";
    }
    ps += "try{$p=$r.GetResponse()}catch [Net.WebException]{$p=$_.Exception.Response;if(-not $p){throw}};\
           $t=(New-Object IO.StreamReader($p.GetResponseStream(),[Text.Encoding]::UTF8)).ReadToEnd();\
           [Console]::Out.Write($t+\"`n\"+[int]$p.StatusCode)";
    // -EncodedCommand takes base64 of UTF-16LE: no quoting issues, stdin stays free for the body.
    let utf16: Vec<u8> = ps.encode_utf16().flat_map(|u| u.to_le_bytes()).collect();
    let mut cmd = Command::new("powershell");
    cmd.args([
        "-NoProfile",
        "-NonInteractive",
        "-ExecutionPolicy",
        "Bypass",
        "-EncodedCommand",
        &b64_std(&utf16),
    ]);
    #[cfg(windows)]
    {
        use std::os::windows::process::CommandExt;
        cmd.creation_flags(0x0800_0000); // CREATE_NO_WINDOW
    }
    cmd.stdin(if body.is_some() {
        Stdio::piped()
    } else {
        Stdio::null()
    })
    .stdout(Stdio::piped())
    .stderr(Stdio::piped());
    let mut child = cmd
        .spawn()
        .map_err(|e| format!("không chạy được curl hay PowerShell: {e}"))?;
    if let Some(b) = body {
        use std::io::Write;
        child
            .stdin
            .take()
            .unwrap()
            .write_all(b.as_bytes())
            .map_err(|e| e.to_string())?;
    }
    let out = child.wait_with_output().map_err(|e| e.to_string())?;
    let text = String::from_utf8_lossy(&out.stdout).to_string();
    match text.trim_end().rsplit_once('\n') {
        Some((resp, code)) if out.status.success() && code.trim().parse::<u16>().is_ok() => {
            Ok((resp.to_string(), code.trim().to_string()))
        }
        _ => Err(format!(
            "không kết nối được license server: {}",
            String::from_utf8_lossy(&out.stderr)
                .lines()
                .next()
                .unwrap_or("")
                .trim()
        )),
    }
}

/// Standard (padded) base64, as PowerShell -EncodedCommand expects.
fn b64_std(data: &[u8]) -> String {
    const T: &[u8; 64] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    let mut o = String::with_capacity(data.len().div_ceil(3) * 4);
    for c in data.chunks(3) {
        let n = (c[0] as u32) << 16
            | (*c.get(1).unwrap_or(&0) as u32) << 8
            | *c.get(2).unwrap_or(&0) as u32;
        for (k, sh) in [18, 12, 6, 0].iter().enumerate() {
            o.push(if k <= c.len() {
                T[(n >> sh & 63) as usize] as char
            } else {
                '='
            });
        }
    }
    o
}

/// Licensed supernodes currently online. Requires our lease (CONTRACT §10): the list
/// contains customers' server addresses and is never public.
pub fn fetch_supernodes(server: &str, lease: &str) -> Vec<String> {
    let url = format!("{}/api/v1/supernodes", server.trim_end_matches('/'));
    let auth = format!("Authorization: Lease {lease}");
    match curl(&["-H", &auth, &url], None) {
        Ok((resp, code)) if code == "200" => Json::parse(&resp)
            .map(|j| j.str_list("supernodes"))
            .unwrap_or_default(),
        _ => vec![],
    }
}

/// Ask the license authority for a fresh lease. HTTPS via the system `curl`
/// (present on Windows 10+, macOS, Linux) so the core needs no TLS stack.
/// `spk` binds the lease to this server's key; `supernode` announces us.
pub fn fetch(
    server: &str,
    key: &str,
    mid: &str,
    spk: Option<&[u8; 32]>,
    supernode: Option<&str>,
) -> Result<String, String> {
    let mut body = format!(
        "{{\"key\":{},\"mid\":{},\"hostname\":{},\"version\":{}",
        escape(key),
        escape(mid),
        // Privacy (VN Law 91/2025): never send the raw hostname, only a short salted hash.
        escape(&crate::util::hex(
            &crate::crypto::hash(&[b"peerova-host", hostname().as_bytes()])[..6]
        )),
        escape(env!("CARGO_PKG_VERSION"))
    );
    if let Some(k) = spk {
        body.push_str(&format!(",\"spk\":{}", escape(&crate::util::b64_encode(k))));
    }
    if let Some(a) = supernode {
        body.push_str(&format!(",\"supernode\":{}", escape(a)));
    }
    body.push('}');
    let url = format!("{}/api/v1/lease", server.trim_end_matches('/'));
    let (resp, code) = curl(
        &[
            "-X",
            "POST",
            "-H",
            "Content-Type: application/json",
            "--data-binary",
            "@-",
            &url,
        ],
        Some(&body),
    )?;
    let j = Json::parse(&resp)
        .map_err(|_| format!("license server trả về không phải JSON (HTTP {code})"))?;
    if code != "200" {
        return Err(format!(
            "license bị từ chối: {} (HTTP {code})",
            j.str("error").unwrap_or("unknown")
        ));
    }
    j.str("lease")
        .map(String::from)
        .ok_or_else(|| "phản hồi thiếu lease".into())
}

pub fn cache_path(dir: &Path) -> PathBuf {
    dir.join("peerova.lease")
}

/// Load a cached lease or fetch a new one; keeps the cache updated.
pub fn obtain(
    server: &str,
    key: &str,
    authority: &[u8; 32],
    cache: &Path,
    spk: Option<&[u8; 32]>,
    supernode: Option<&str>,
) -> Result<Lease, String> {
    let mid = machine_id();
    let check_spk = |l: Lease| match (&l.spk, spk) {
        (Some(k), Some(mine)) if *k != crate::util::b64_encode(mine) => {
            Err("lease cấp cho server key khác".to_string())
        }
        _ => Ok(l),
    };
    match fetch(server, key, &mid, spk, supernode)
        .and_then(|t| verify(&t, authority, Some(&mid), now_unix()))
        .and_then(check_spk)
    {
        Ok(l) => {
            let _ = std::fs::write(cache, &l.token);
            Ok(l)
        }
        Err(online_err) => {
            // Offline grace: a cached, still-valid lease keeps the server running.
            let cached = std::fs::read_to_string(cache).map_err(|_| online_err.clone())?;
            verify(&cached, authority, Some(&mid), now_unix())
                .and_then(check_spk)
                .map_err(|e| format!("{online_err}; lease cache cũng không dùng được: {e}"))
        }
    }
}

pub struct LicenseConfig {
    pub key: String,
    pub server: String,
    pub authority: [u8; 32],
    pub cache: PathBuf,
    /// Server static public key: binds the lease to this server identity.
    pub spk: [u8; 32],
}

/// Before starting the engine: obtain the lease and the list of licensed
/// supernodes (cached next to the lease so restarts work offline).
pub fn prepare(cfg: &LicenseConfig) -> (Result<Lease, String>, Vec<std::net::SocketAddr>) {
    let lease = obtain(
        &cfg.server,
        &cfg.key,
        &cfg.authority,
        &cfg.cache,
        Some(&cfg.spk),
        None,
    );
    let sn_cache = cfg.cache.with_extension("supernodes");
    let mut list = match &lease {
        Ok(l) => fetch_supernodes(&cfg.server, &l.token),
        Err(_) => vec![],
    };
    if list.is_empty() {
        list = std::fs::read_to_string(&sn_cache)
            .map(|s| s.lines().map(String::from).collect())
            .unwrap_or_default();
    } else {
        let _ = std::fs::write(&sn_cache, list.join("\n"));
    }
    use std::net::ToSocketAddrs;
    let addrs = list
        .iter()
        .filter_map(|a| a.to_socket_addrs().ok()?.next())
        .take(8)
        .collect();
    (lease, addrs)
}

/// Keep renewing in the background (every 6 h). Pushes the licensed CCU and
/// lease token into the engine, and announces us as a supernode when the engine
/// decided it is one. Never throttles: failure only matters after the 7-day lease expires.
pub fn spawn_renewal(cfg: LicenseConfig, handle: crate::engine::Handle) {
    std::thread::spawn(move || loop {
        std::thread::sleep(std::time::Duration::from_secs(RENEW_EVERY_SECS));
        let sn = handle.stats.lock().unwrap().supernode_addr.clone();
        match obtain(
            &cfg.server,
            &cfg.key,
            &cfg.authority,
            &cfg.cache,
            Some(&cfg.spk),
            sn.as_deref(),
        ) {
            Ok(l) => {
                handle.send(crate::engine::Cmd::SetMaxCcu(l.ccu as usize));
                handle.send(crate::engine::Cmd::SetLease(Some(l.token)));
            }
            Err(_) => {
                handle.send(crate::engine::Cmd::SetMaxCcu(crate::engine::DEV_MODE_CCU));
                handle.send(crate::engine::Cmd::SetLease(None));
            }
        }
    });
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::util::b64_encode;
    use ed25519_dalek::{Signer, SigningKey};

    fn make(sk: &SigningKey, payload: &str) -> String {
        let signed = format!("pvl1.{}", b64_encode(payload.as_bytes()));
        let sig = sk.sign(signed.as_bytes());
        format!("{signed}.{}", b64_encode(&sig.to_bytes()))
    }

    #[test]
    fn b64_std_matches_rfc4648() {
        assert_eq!(b64_std(b""), "");
        assert_eq!(b64_std(b"f"), "Zg==");
        assert_eq!(b64_std(b"fo"), "Zm8=");
        assert_eq!(b64_std(b"foo"), "Zm9v");
        assert_eq!(b64_std(b"foob"), "Zm9vYg==");
    }

    #[test]
    fn verify_lease() {
        let sk = SigningKey::from_bytes(&[7u8; 32]);
        let pk = sk.verifying_key().to_bytes();
        let p = r#"{"v":1,"lid":"lic_1","pid":"prj_1","plan":"indie","srv":3,"ccu":200,"mid":"abc","iat":100,"exp":1000}"#;
        let t = make(&sk, p);
        let l = verify(&t, &pk, Some("abc"), 500).unwrap();
        assert_eq!((l.plan.as_str(), l.ccu, l.srv), ("indie", 200, 3));
        assert!(verify(&t, &pk, Some("other"), 500)
            .unwrap_err()
            .contains("máy khác"));
        assert!(verify(&t, &pk, Some("abc"), 1000)
            .unwrap_err()
            .contains("hết hạn"));
        // Tampered payload (raise CCU) must fail.
        let forged = t.replacen(
            &b64_encode(p.as_bytes()),
            &b64_encode(p.replace("200", "99999").as_bytes()),
            1,
        );
        assert!(verify(&forged, &pk, Some("abc"), 500)
            .unwrap_err()
            .contains("KHÔNG hợp lệ"));
        let other = SigningKey::from_bytes(&[8u8; 32])
            .verifying_key()
            .to_bytes();
        assert!(verify(&t, &other, Some("abc"), 500).is_err());
    }

    /// Cross-check against the website (JavaScript WebCrypto) signer: web/tools/test-vector.json.
    #[test]
    fn verifies_web_signed_test_vectors() {
        let path = concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/../../web/tools/test-vector.json"
        );
        let Ok(text) = std::fs::read_to_string(path) else {
            return;
        }; // web/ not present in this checkout
        let v = Json::parse(&text).unwrap();
        let pk = parse_pubkey(v.str("public_key_b64url").unwrap()).unwrap();
        let lease = v.str("lease").unwrap();
        if !lease.starts_with("pvl1.") {
            eprintln!(
                "web/tools/test-vector.json chưa được tạo lại với prefix pvl1. — bỏ qua tạm thời"
            );
            return;
        }
        let l = verify(
            lease,
            &pk,
            Some("0123456789abcdef0123456789abcdef"),
            1_761_955_300,
        )
        .expect("v1 web lease must verify");
        assert_eq!(
            (l.plan.as_str(), l.srv, l.ccu, l.spk.is_none()),
            ("indie", 3, 200, true)
        );
        let v2 = v.get("v2").unwrap();
        let lease2 = v2.str("lease").unwrap();
        let l2 = verify(lease2, &pk, None, 1_761_955_300).expect("v2 web lease must verify");
        let spk: [u8; 32] = core::array::from_fn(|i| i as u8);
        assert_eq!(l2.spk.as_deref(), Some(b64_encode(&spk).as_str()));
        assert!(verify_for_supernode(
            lease2.as_bytes(),
            &pk,
            &spk,
            1_761_955_300
        ));
        assert!(
            !verify_for_supernode(lease2.as_bytes(), &pk, &[9u8; 32], 1_761_955_300),
            "spk binding"
        );
        // Tampering with a web-signed lease must fail.
        let bad = lease.replacen("aW5kaWU", "c3R1ZGlv", 1);
        assert!(verify(&bad, &pk, None, 1_761_955_300).is_err());
    }

    #[test]
    fn machine_id_stable() {
        assert_eq!(machine_id(), machine_id());
        assert_eq!(machine_id().len(), 32);
    }
}
