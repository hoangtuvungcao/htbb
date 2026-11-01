//! Server identity key file (`pvk_<b64url secret>`). Same key ⇒ same ticket.

use std::path::Path;

use crate::crypto::Keypair;
use crate::util::{b64_decode, b64_encode};

pub const KEY_PREFIX: &str = "pvk_";

/// Load the key, or create it (mode 0600 on Unix). Returns (key, created).
pub fn load_or_create(path: &Path) -> Result<(Keypair, bool), String> {
    if let Ok(s) = std::fs::read_to_string(path) {
        let b = s
            .trim()
            .strip_prefix(KEY_PREFIX)
            .and_then(b64_decode)
            .ok_or("file key hỏng")?;
        let arr: [u8; 32] = b.try_into().map_err(|_| "file key hỏng")?;
        return Ok((Keypair::from_secret(arr), false));
    }
    let k = Keypair::generate();
    std::fs::write(
        path,
        format!("{KEY_PREFIX}{}\n", b64_encode(&k.secret_bytes())),
    )
    .map_err(|e| format!("không ghi được {}: {e}", path.display()))?;
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        let _ = std::fs::set_permissions(path, std::fs::Permissions::from_mode(0o600));
    }
    Ok((k, true))
}
