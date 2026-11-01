//! Platforms without a TUN backend yet.

use std::io;
use std::net::Ipv4Addr;

pub struct Device;

fn unsupported() -> io::Error {
    io::Error::new(io::ErrorKind::Unsupported, "chế độ VPN chưa hỗ trợ hệ điều hành này (hỗ trợ: Linux, Windows, macOS)")
}

impl Device {
    pub fn open() -> io::Result<(Device, String)> {
        Err(unsupported())
    }
    pub fn recv(&self, _buf: &mut [u8]) -> io::Result<Option<usize>> {
        Err(unsupported())
    }
    pub fn send(&self, _pkt: &[u8]) -> io::Result<()> {
        Err(unsupported())
    }
}

pub fn configure(_dev: &Device, _name: &str, _addr: Ipv4Addr, _prefix: u8, _peer: Ipv4Addr, _mtu: u32) -> io::Result<()> {
    Err(unsupported())
}
