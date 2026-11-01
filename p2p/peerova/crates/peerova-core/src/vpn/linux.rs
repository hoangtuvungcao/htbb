//! Linux TUN backend: `/dev/net/tun` + ioctl TUNSETIFF, configured with iproute2.

use std::ffi::{c_int, c_short, c_ulong};
use std::fs::File;
use std::io::{self, Read, Write};
use std::net::Ipv4Addr;
use std::os::fd::AsRawFd;

const TUNSETIFF: c_ulong = 0x4004_54ca;
const IFF_TUN: i16 = 0x0001;
const IFF_NO_PI: i16 = 0x1000;
const POLLIN: c_short = 0x001;

#[repr(C)]
struct IfReq {
    name: [u8; 16],
    flags: i16,
    pad: [u8; 22],
}

#[repr(C)]
struct PollFd {
    fd: c_int,
    events: c_short,
    revents: c_short,
}

extern "C" {
    fn ioctl(fd: c_int, request: c_ulong, ...) -> c_int;
    fn poll(fds: *mut PollFd, nfds: c_ulong, timeout: c_int) -> c_int;
}

pub struct Device {
    file: File,
}

impl Device {
    /// Create `pv0`, `pv1`, … (kernel picks the number).
    pub fn open() -> io::Result<(Device, String)> {
        let name = "pv%d";
        let file = std::fs::OpenOptions::new().read(true).write(true).open("/dev/net/tun")?;
        let mut req = IfReq { name: [0; 16], flags: IFF_TUN | IFF_NO_PI, pad: [0; 22] };
        req.name[..name.len()].copy_from_slice(name.as_bytes());
        // SAFETY: TUNSETIFF reads/writes a struct ifreq we own for the duration of the call.
        let rc = unsafe { ioctl(file.as_raw_fd(), TUNSETIFF, &mut req as *mut IfReq) };
        if rc < 0 {
            let e = io::Error::last_os_error();
            return Err(io::Error::new(e.kind(), format!("tạo TUN: {e} (cần root / CAP_NET_ADMIN)")));
        }
        let end = req.name.iter().position(|&b| b == 0).unwrap_or(16);
        Ok((Device { file }, String::from_utf8_lossy(&req.name[..end]).to_string()))
    }

    /// One IP packet, or `None` after ~250 ms without traffic.
    pub fn recv(&self, buf: &mut [u8]) -> io::Result<Option<usize>> {
        let mut p = PollFd { fd: self.file.as_raw_fd(), events: POLLIN, revents: 0 };
        // SAFETY: one valid pollfd for the duration of the call.
        let rc = unsafe { poll(&mut p, 1, 250) };
        if rc < 0 {
            let e = io::Error::last_os_error();
            return if e.kind() == io::ErrorKind::Interrupted { Ok(None) } else { Err(e) };
        }
        if rc == 0 {
            return Ok(None);
        }
        match (&self.file).read(buf) {
            Ok(n) => Ok(Some(n)),
            Err(e) if matches!(e.kind(), io::ErrorKind::Interrupted | io::ErrorKind::WouldBlock) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn send(&self, pkt: &[u8]) -> io::Result<()> {
        (&self.file).write(pkt).map(|_| ())
    }
}

/// Assign address/prefix, MTU and bring the interface up (iproute2).
pub fn configure(_dev: &Device, name: &str, addr: Ipv4Addr, prefix: u8, _peer: Ipv4Addr, mtu: u32) -> io::Result<()> {
    run(&["addr", "add", &format!("{addr}/{prefix}"), "dev", name])?;
    run(&["link", "set", "dev", name, "mtu", &mtu.to_string(), "up"])
}

fn run(args: &[&str]) -> io::Result<()> {
    let st = std::process::Command::new("ip").args(args).status()?;
    if st.success() {
        Ok(())
    } else {
        Err(io::Error::other(format!("ip {}: thất bại", args.join(" "))))
    }
}
