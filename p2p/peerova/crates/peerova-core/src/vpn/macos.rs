//! macOS backend: kernel `utun` via a PF_SYSTEM control socket.
//! Every packet carries a 4-byte address-family header (network order).
//! Configured with `ifconfig` + `route`. Needs root.

use std::ffi::{c_int, c_short, c_uint, c_ulong, c_void};
use std::fs::File;
use std::io::{self, Read, Write};
use std::net::Ipv4Addr;
use std::os::fd::{AsRawFd, FromRawFd};

const PF_SYSTEM: c_int = 32;
const AF_SYSTEM: u8 = 32;
const SOCK_DGRAM: c_int = 2;
const SYSPROTO_CONTROL: c_int = 2;
const AF_SYS_CONTROL: u16 = 2;
const UTUN_OPT_IFNAME: c_int = 2;
/// _IOWR('N', 3, struct ctl_info)
const CTLIOCGINFO: c_ulong = 0xc064_4e03;
const AF_INET: u32 = 2;
const AF_INET6: u32 = 30;
const POLLIN: c_short = 0x001;
const HDR: usize = 4;

#[repr(C)]
struct CtlInfo {
    ctl_id: u32,
    ctl_name: [u8; 96],
}

#[repr(C)]
struct SockaddrCtl {
    sc_len: u8,
    sc_family: u8,
    ss_sysaddr: u16,
    sc_id: u32,
    sc_unit: u32,
    sc_reserved: [u32; 5],
}

#[repr(C)]
struct PollFd {
    fd: c_int,
    events: c_short,
    revents: c_short,
}

extern "C" {
    fn socket(domain: c_int, ty: c_int, protocol: c_int) -> c_int;
    fn ioctl(fd: c_int, request: c_ulong, ...) -> c_int;
    fn connect(fd: c_int, addr: *const c_void, len: u32) -> c_int;
    fn getsockopt(fd: c_int, level: c_int, name: c_int, val: *mut c_void, len: *mut u32) -> c_int;
    fn poll(fds: *mut PollFd, nfds: c_uint, timeout: c_int) -> c_int;
}

pub struct Device {
    file: File,
}

fn os_err(what: &str) -> io::Error {
    let e = io::Error::last_os_error();
    io::Error::new(e.kind(), format!("{what}: {e} (cần chạy bằng sudo)"))
}

impl Device {
    /// Create the next free `utunN`.
    pub fn open() -> io::Result<(Device, String)> {
        // SAFETY: plain socket(2) call.
        let fd = unsafe { socket(PF_SYSTEM, SOCK_DGRAM, SYSPROTO_CONTROL) };
        if fd < 0 {
            return Err(os_err("tạo utun"));
        }
        // SAFETY: fd is a fresh, valid descriptor we exclusively own (closed on drop).
        let file = unsafe { File::from_raw_fd(fd) };
        let mut info = CtlInfo { ctl_id: 0, ctl_name: [0; 96] };
        let name = b"com.apple.net.utun_control";
        info.ctl_name[..name.len()].copy_from_slice(name);
        // SAFETY: CTLIOCGINFO reads/writes the ctl_info we own.
        if unsafe { ioctl(fd, CTLIOCGINFO, &mut info as *mut CtlInfo) } < 0 {
            return Err(os_err("utun CTLIOCGINFO"));
        }
        let addr = SockaddrCtl {
            sc_len: std::mem::size_of::<SockaddrCtl>() as u8,
            sc_family: AF_SYSTEM,
            ss_sysaddr: AF_SYS_CONTROL,
            sc_id: info.ctl_id,
            sc_unit: 0, // 0 = kernel picks the next free utunN
            sc_reserved: [0; 5],
        };
        // SAFETY: addr is a valid sockaddr_ctl of the stated length.
        if unsafe { connect(fd, &addr as *const SockaddrCtl as *const c_void, std::mem::size_of::<SockaddrCtl>() as u32) } < 0 {
            return Err(os_err("utun connect"));
        }
        let mut buf = [0u8; 32];
        let mut len = buf.len() as u32;
        // SAFETY: buf/len describe a writable buffer we own.
        if unsafe { getsockopt(fd, SYSPROTO_CONTROL, UTUN_OPT_IFNAME, buf.as_mut_ptr() as *mut c_void, &mut len) } < 0 {
            return Err(os_err("utun UTUN_OPT_IFNAME"));
        }
        let end = buf[..len as usize].iter().position(|&b| b == 0).unwrap_or(len as usize);
        Ok((Device { file }, String::from_utf8_lossy(&buf[..end]).to_string()))
    }

    /// One IP packet (AF header stripped), or `None` after ~250 ms without traffic.
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
        let mut tmp = vec![0u8; buf.len() + HDR];
        match (&self.file).read(&mut tmp) {
            Ok(n) if n > HDR => {
                buf[..n - HDR].copy_from_slice(&tmp[HDR..n]);
                Ok(Some(n - HDR))
            }
            Ok(_) => Ok(None),
            Err(e) if matches!(e.kind(), io::ErrorKind::Interrupted | io::ErrorKind::WouldBlock) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn send(&self, pkt: &[u8]) -> io::Result<()> {
        let af = if pkt.first().is_some_and(|b| b >> 4 == 6) { AF_INET6 } else { AF_INET };
        let mut d = Vec::with_capacity(HDR + pkt.len());
        d.extend_from_slice(&af.to_be_bytes());
        d.extend_from_slice(pkt);
        (&self.file).write(&d).map(|_| ())
    }
}

/// `ifconfig utunN A PEER netmask M mtu X up` + `route add -net NET/P -interface utunN`.
pub fn configure(_dev: &Device, name: &str, addr: Ipv4Addr, prefix: u8, peer: Ipv4Addr, mtu: u32) -> io::Result<()> {
    let mask = u32::MAX << (32 - prefix);
    let net = Ipv4Addr::from(u32::from(addr) & mask);
    run("ifconfig", &[name, "inet", &addr.to_string(), &peer.to_string(), "netmask", &Ipv4Addr::from(mask).to_string(), "mtu", &mtu.to_string(), "up"])?;
    run("route", &["-q", "-n", "add", "-net", &format!("{net}/{prefix}"), "-interface", name])
}

fn run(cmd: &str, args: &[&str]) -> io::Result<()> {
    let st = std::process::Command::new(cmd).args(args).status()?;
    if st.success() {
        Ok(())
    } else {
        Err(io::Error::other(format!("{cmd} {}: thất bại", args.join(" "))))
    }
}
