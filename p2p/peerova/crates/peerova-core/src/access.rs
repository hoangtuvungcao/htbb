//! Enterprise access tokens: short-lived, scoped, signed by the customer's own key.
//!
//! `pva1.<b64url(json)>.<b64url(ed25519 sig)>`, payload:
//! `{"v":1,"sub":"alice@corp","ports":["tcp:443","udp:10000-10100"],"exp":<unix>,"jti":"<id>"}`
//!
//! The company's SSO / IdP backend issues tokens with the private access key;
//! peerova servers only hold the public key, verify offline, enforce expiry, the
//! port scopes and a revocation list. No vendor involvement.

use std::collections::HashSet;

use ed25519_dalek::{Signature, Signer, SigningKey, Verifier, VerifyingKey};

use crate::json::{escape, Json};
use crate::tunnel::Proto;
use crate::util::{b64_decode, b64_encode};

const PREFIX: &str = "pva1";

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AccessToken {
    pub sub: String,
    pub ports: Vec<String>,
    pub exp: u64,
    pub jti: String,
}

impl AccessToken {
    /// Scopes: `*`, `tcp:*`, `udp:*`, `tcp:443`, `udp:10000-10100`.
    pub fn allows(&self, proto: Proto, port: u16) -> bool {
        scope_allows(&self.ports, proto, port)
    }
}

pub fn scope_allows(scopes: &[String], proto: Proto, port: u16) -> bool {
    let want = match proto {
        Proto::Tcp => "tcp",
        Proto::Udp => "udp",
    };
    scopes.iter().any(|s| {
        if s == "*" {
            return true;
        }
        let Some((p, range)) = s.split_once(':') else { return false };
        if !p.eq_ignore_ascii_case(want) {
            return false;
        }
        if range == "*" {
            return true;
        }
        match range.split_once('-') {
            Some((a, b)) => matches!((a.parse::<u16>(), b.parse::<u16>()), (Ok(a), Ok(b)) if (a..=b).contains(&port)),
            None => range.parse::<u16>() == Ok(port),
        }
    })
}

pub fn public_key(seed: &[u8; 32]) -> [u8; 32] {
    SigningKey::from_bytes(seed).verifying_key().to_bytes()
}

pub fn issue(seed: &[u8; 32], sub: &str, ports: &[String], ttl_secs: u64, now: u64) -> String {
    let jti = crate::util::hex(&crate::crypto::random_bytes::<8>());
    let ports_json: Vec<String> = ports.iter().map(|p| escape(p)).collect();
    let payload = format!(
        "{{\"v\":1,\"sub\":{},\"ports\":[{}],\"exp\":{},\"jti\":{}}}",
        escape(sub),
        ports_json.join(","),
        now + ttl_secs,
        escape(&jti)
    );
    let signed = format!("{PREFIX}.{}", b64_encode(payload.as_bytes()));
    let sig = SigningKey::from_bytes(seed)
        .sign(signed.as_bytes())
        .to_bytes();
    format!("{signed}.{}", b64_encode(&sig))
}

pub fn verify(
    token: &[u8],
    authority: &[u8; 32],
    revoked: &HashSet<String>,
    now: u64,
) -> Result<AccessToken, String> {
    let token = std::str::from_utf8(token)
        .map_err(|_| "token không phải UTF-8")?
        .trim();
    let mut parts = token.split('.');
    let (Some(PREFIX), Some(p), Some(sig), None) =
        (parts.next(), parts.next(), parts.next(), parts.next())
    else {
        return Err("access token sai định dạng".into());
    };
    let signed = &token[..PREFIX.len() + 1 + p.len()];
    let sig: [u8; 64] = b64_decode(sig)
        .and_then(|s| s.try_into().ok())
        .ok_or("chữ ký token hỏng")?;
    VerifyingKey::from_bytes(authority)
        .map_err(|_| "access public key không hợp lệ")?
        .verify(signed.as_bytes(), &Signature::from_bytes(&sig))
        .map_err(|_| "chữ ký access token không hợp lệ")?;
    let j = Json::parse(
        std::str::from_utf8(&b64_decode(p).ok_or("payload hỏng")?).map_err(|_| "payload hỏng")?,
    )?;
    let t = AccessToken {
        sub: j.str("sub").unwrap_or("").to_string(),
        ports: j.str_list("ports"),
        exp: j.u64("exp").ok_or("token thiếu exp")?,
        jti: j.str("jti").unwrap_or("").to_string(),
    };
    if now >= t.exp {
        return Err(format!("access token của {} đã hết hạn", t.sub));
    }
    if revoked.contains(&t.jti) || revoked.contains(&t.sub) {
        return Err(format!("access token của {} đã bị thu hồi", t.sub));
    }
    Ok(t)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn issue_verify_scope_revoke() {
        let seed = [3u8; 32];
        let pk = public_key(&seed);
        let t = issue(
            &seed,
            "alice",
            &["tcp:443".into(), "udp:10000-10100".into()],
            3600,
            1000,
        );
        let none = HashSet::new();
        let a = verify(t.as_bytes(), &pk, &none, 2000).unwrap();
        assert_eq!(a.sub, "alice");
        assert!(a.allows(Proto::Tcp, 443) && !a.allows(Proto::Tcp, 22));
        assert!(a.allows(Proto::Udp, 10050) && !a.allows(Proto::Udp, 10101));
        assert!(verify(t.as_bytes(), &pk, &none, 5000)
            .unwrap_err()
            .contains("hết hạn"));
        let revoked: HashSet<String> = ["alice".to_string()].into();
        assert!(verify(t.as_bytes(), &pk, &revoked, 2000)
            .unwrap_err()
            .contains("thu hồi"));
        assert!(verify(t.as_bytes(), &public_key(&[4u8; 32]), &none, 2000).is_err());
        let forged = t.replacen('.', ".x", 1);
        assert!(verify(forged.as_bytes(), &pk, &none, 2000).is_err());
        assert!(scope_allows(&["*".into()], Proto::Udp, 1));
        assert!(
            scope_allows(&["tcp:*".into()], Proto::Tcp, 8080)
                && !scope_allows(&["tcp:*".into()], Proto::Udp, 8080)
        );
    }
}
