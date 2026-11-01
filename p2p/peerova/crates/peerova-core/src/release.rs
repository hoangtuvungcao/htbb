//! Release integrity: SHA256SUMS.txt is signed with a dedicated Ed25519 release key
//! (separate from the license authority). `peerova verify <file>` checks both the
//! signature of the sums file and the SHA-256 of the downloaded file.

use ed25519_dalek::{Signature, VerifyingKey};
use sha2::{Digest, Sha256};

use crate::util::{b64_decode, hex};

/// Embedded at build time: `PEEROVA_RELEASE_PUBKEY=<b64url 32 bytes>`.
pub const RELEASE_PUBKEY: Option<&str> = option_env!("PEEROVA_RELEASE_PUBKEY");

pub fn sha256_hex(data: &[u8]) -> String {
    hex(&Sha256::digest(data))
}

/// Verify `sig_b64` (b64url Ed25519 signature over the exact bytes of `sums`).
pub fn verify_sums(sums: &[u8], sig_b64: &str, pubkey: &[u8; 32]) -> Result<(), String> {
    let vk = VerifyingKey::from_bytes(pubkey).map_err(|_| "public key phát hành không hợp lệ")?;
    let sig: [u8; 64] = b64_decode(sig_b64.trim())
        .and_then(|v| v.try_into().ok())
        .ok_or("chữ ký phải là 64 byte base64url")?;
    vk.verify_strict(sums, &Signature::from_bytes(&sig))
        .map_err(|_| {
            "CHỮ KÝ KHÔNG HỢP LỆ — file SHA256SUMS bị sửa hoặc không phải bản chính thức"
                .to_string()
        })
}

/// Find the expected hash of `name` in a signed sums file (`<hex>  <name>` lines).
pub fn expected_hash<'a>(sums: &'a str, name: &str) -> Option<&'a str> {
    sums.lines().find_map(|l| {
        let (h, n) = l.split_once("  ")?;
        (n.trim() == name).then_some(h.trim())
    })
}

#[cfg(test)]
mod tests {
    use super::*;
    use ed25519_dalek::{Signer, SigningKey};

    #[test]
    fn sign_and_verify_sums() {
        let sk = SigningKey::from_bytes(&[8; 32]);
        let pk = sk.verifying_key().to_bytes();
        let file = b"binary";
        let sums = format!("{}  peerova.zip\n", sha256_hex(file));
        let sig = crate::util::b64_encode(&sk.sign(sums.as_bytes()).to_bytes());
        verify_sums(sums.as_bytes(), &sig, &pk).unwrap();
        assert_eq!(
            expected_hash(&sums, "peerova.zip"),
            Some(sha256_hex(file).as_str())
        );
        let tampered = sums.replace("peerova.zip", "evil.zip");
        assert!(verify_sums(tampered.as_bytes(), &sig, &pk).is_err());
        assert_eq!(
            sha256_hex(b"abc"),
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        );
    }
}
