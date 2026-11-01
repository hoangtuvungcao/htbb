//! Windows backend: Wintun (WireGuard project's signed L3 TUN driver).
//! `wintun.dll` is loaded at runtime — next to peerova.exe first, then System32 —
//! and the address is set with `netsh` (works back to Windows 7 / Server 2012 R2).
//! Needs Administrator.

use std::ffi::c_void;
use std::io;
use std::net::Ipv4Addr;
use std::os::windows::process::CommandExt;
use std::sync::OnceLock;
use std::time::Duration;

type Handle = *mut c_void;

const RING_CAPACITY: u32 = 0x40_0000;
const WAIT_TIMEOUT: u32 = 258;
const ERROR_ACCESS_DENIED: i32 = 5;
const ERROR_HANDLE_EOF: i32 = 38;
const ERROR_NO_MORE_ITEMS: i32 = 259;
const LOAD_LIBRARY_SEARCH_SYSTEM32: u32 = 0x800;
const CREATE_NO_WINDOW: u32 = 0x0800_0000;
const ADAPTER_NAME: &str = "Peerova";

#[repr(C)]
struct Guid {
    d1: u32,
    d2: u16,
    d3: u16,
    d4: [u8; 8],
}

#[link(name = "kernel32")]
extern "system" {
    fn LoadLibraryW(name: *const u16) -> Handle;
    fn LoadLibraryExW(name: *const u16, file: Handle, flags: u32) -> Handle;
    fn GetProcAddress(module: Handle, name: *const u8) -> *mut c_void;
    fn WaitForSingleObject(h: Handle, ms: u32) -> u32;
}

#[link(name = "iphlpapi")]
extern "system" {
    fn ConvertInterfaceLuidToIndex(luid: *const u64, index: *mut u32) -> u32;
}

struct Api {
    create: unsafe extern "system" fn(*const u16, *const u16, *const Guid) -> Handle,
    open: unsafe extern "system" fn(*const u16) -> Handle,
    close: unsafe extern "system" fn(Handle),
    luid: unsafe extern "system" fn(Handle, *mut u64),
    start: unsafe extern "system" fn(Handle, u32) -> Handle,
    end: unsafe extern "system" fn(Handle),
    event: unsafe extern "system" fn(Handle) -> Handle,
    recv: unsafe extern "system" fn(Handle, *mut u32) -> *mut u8,
    release: unsafe extern "system" fn(Handle, *const u8),
    alloc: unsafe extern "system" fn(Handle, u32) -> *mut u8,
    send: unsafe extern "system" fn(Handle, *const u8),
}

fn wide(s: &str) -> Vec<u16> {
    s.encode_utf16().chain(Some(0)).collect()
}

fn load() -> Result<Api, String> {
    let mut module: Handle = std::ptr::null_mut();
    if let Some(dir) = std::env::current_exe().ok().and_then(|p| p.parent().map(|d| d.join("wintun.dll"))) {
        use std::os::windows::ffi::OsStrExt;
        let w: Vec<u16> = dir.as_os_str().encode_wide().chain(Some(0)).collect();
        // SAFETY: NUL-terminated wide path.
        module = unsafe { LoadLibraryW(w.as_ptr()) };
    }
    if module.is_null() {
        // System32 only — never the current directory (DLL planting).
        // SAFETY: NUL-terminated wide name.
        module = unsafe { LoadLibraryExW(wide("wintun.dll").as_ptr(), std::ptr::null_mut(), LOAD_LIBRARY_SEARCH_SYSTEM32) };
    }
    if module.is_null() {
        return Err("không tìm thấy wintun.dll — đặt wintun.dll (có trong gói Peerova cho Windows) cùng thư mục với peerova.exe".into());
    }
    macro_rules! sym {
        ($name:literal) => {{
            // SAFETY: valid module handle and NUL-terminated symbol name.
            let p = unsafe { GetProcAddress(module, concat!($name, "\0").as_ptr()) };
            if p.is_null() {
                return Err(format!("wintun.dll thiếu hàm {} (cần Wintun 0.14 trở lên)", $name));
            }
            // SAFETY: signature matches wintun.h for this export.
            unsafe { std::mem::transmute::<*mut c_void, _>(p) }
        }};
    }
    Ok(Api {
        create: sym!("WintunCreateAdapter"),
        open: sym!("WintunOpenAdapter"),
        close: sym!("WintunCloseAdapter"),
        luid: sym!("WintunGetAdapterLUID"),
        start: sym!("WintunStartSession"),
        end: sym!("WintunEndSession"),
        event: sym!("WintunGetReadWaitEvent"),
        recv: sym!("WintunReceivePacket"),
        release: sym!("WintunReleaseReceivePacket"),
        alloc: sym!("WintunAllocateSendPacket"),
        send: sym!("WintunSendPacket"),
    })
}

fn api() -> io::Result<&'static Api> {
    static API: OnceLock<Result<Api, String>> = OnceLock::new();
    API.get_or_init(load).as_ref().map_err(|e| io::Error::new(io::ErrorKind::NotFound, e.clone()))
}

pub struct Device {
    api: &'static Api,
    adapter: Handle,
    session: Handle,
    event: Handle,
    /// Interface index (for netsh).
    index: u32,
}

// SAFETY: Wintun adapter/session handles are thread-safe: receive and send may run
// concurrently from different threads (documented in wintun.h).
unsafe impl Send for Device {}
unsafe impl Sync for Device {}

fn elevation_hint(e: io::Error, what: &str) -> io::Error {
    if e.raw_os_error() == Some(ERROR_ACCESS_DENIED) {
        io::Error::new(io::ErrorKind::PermissionDenied, format!("{what}: cần chạy với quyền Administrator (chuột phải → Run as administrator)"))
    } else {
        io::Error::new(e.kind(), format!("{what}: {e}"))
    }
}

impl Device {
    /// Create adapter "Peerova" (or "Peerova 2", … if that name is in use by another instance).
    pub fn open() -> io::Result<(Device, String)> {
        let api = api()?;
        let tunnel_type = wide("Peerova");
        let mut last = io::Error::other("không tạo được adapter");
        for i in 0..8u8 {
            let name = if i == 0 { ADAPTER_NAME.to_string() } else { format!("{ADAPTER_NAME} {}", i + 1) };
            let wname = wide(&name);
            // SAFETY: NUL-terminated name; a non-null result is an adapter handle we must close.
            let existing = unsafe { (api.open)(wname.as_ptr()) };
            if !existing.is_null() {
                // Name taken (another Peerova VPN on this machine): try the next one.
                // SAFETY: handle from WintunOpenAdapter.
                unsafe { (api.close)(existing) };
                continue;
            }
            // Stable GUID per name → Windows keeps the same network profile across runs.
            let guid = Guid { d1: 0x7065_6572, d2: 0x6f76, d3: 0x6100, d4: [0x56, 0x50, 0x4e, 0x00, 0x00, 0x00, 0x00, i] };
            // SAFETY: NUL-terminated strings and a valid GUID for the duration of the call.
            let adapter = unsafe { (api.create)(wname.as_ptr(), tunnel_type.as_ptr(), &guid) };
            if adapter.is_null() {
                last = elevation_hint(io::Error::last_os_error(), "tạo adapter Wintun");
                if last.kind() == io::ErrorKind::PermissionDenied {
                    return Err(last);
                }
                continue;
            }
            let mut luid = 0u64;
            let mut index = 0u32;
            // SAFETY: valid adapter handle; out-pointers we own.
            unsafe {
                (api.luid)(adapter, &mut luid);
                ConvertInterfaceLuidToIndex(&luid, &mut index);
            }
            // SAFETY: valid adapter handle.
            let session = unsafe { (api.start)(adapter, RING_CAPACITY) };
            if session.is_null() {
                let e = elevation_hint(io::Error::last_os_error(), "mở phiên Wintun");
                // SAFETY: handle from WintunCreateAdapter (closing it removes the adapter).
                unsafe { (api.close)(adapter) };
                return Err(e);
            }
            // SAFETY: valid session handle; the event is owned by the session.
            let event = unsafe { (api.event)(session) };
            return Ok((Device { api, adapter, session, event, index }, name));
        }
        Err(last)
    }

    /// One IP packet, or `None` after ~250 ms without traffic.
    pub fn recv(&self, buf: &mut [u8]) -> io::Result<Option<usize>> {
        loop {
            let mut size = 0u32;
            // SAFETY: valid session; the returned packet is released below.
            let p = unsafe { (self.api.recv)(self.session, &mut size) };
            if !p.is_null() {
                let n = (size as usize).min(buf.len());
                // SAFETY: Wintun guarantees `size` readable bytes at `p` until release.
                unsafe {
                    std::ptr::copy_nonoverlapping(p, buf.as_mut_ptr(), n);
                    (self.api.release)(self.session, p);
                }
                return Ok(Some(n));
            }
            let e = io::Error::last_os_error();
            match e.raw_os_error() {
                Some(ERROR_NO_MORE_ITEMS) => {
                    // SAFETY: event handle owned by the live session.
                    if unsafe { WaitForSingleObject(self.event, 250) } == WAIT_TIMEOUT {
                        return Ok(None);
                    }
                }
                Some(ERROR_HANDLE_EOF) => return Err(io::Error::new(io::ErrorKind::UnexpectedEof, "adapter Wintun đã đóng")),
                _ => return Err(e),
            }
        }
    }

    pub fn send(&self, pkt: &[u8]) -> io::Result<()> {
        // SAFETY: valid session; on success we own `pkt.len()` writable bytes until SendPacket.
        unsafe {
            let p = (self.api.alloc)(self.session, pkt.len() as u32);
            if p.is_null() {
                return Err(io::Error::last_os_error()); // ring full: drop (like a NIC would)
            }
            std::ptr::copy_nonoverlapping(pkt.as_ptr(), p, pkt.len());
            (self.api.send)(self.session, p);
        }
        Ok(())
    }

    pub fn index(&self) -> u32 {
        self.index
    }
}

impl Drop for Device {
    fn drop(&mut self) {
        // SAFETY: handles from StartSession/CreateAdapter, released exactly once.
        // Closing an adapter we created removes it from the system.
        unsafe {
            (self.api.end)(self.session);
            (self.api.close)(self.adapter);
        }
    }
}

/// `netsh interface ipv4 set address … static A MASK` + MTU (by interface index, so a
/// renamed adapter still works).
pub fn configure(dev: &Device, name: &str, addr: Ipv4Addr, prefix: u8, _peer: Ipv4Addr, mtu: u32) -> io::Result<()> {
    let ifname = if dev.index() != 0 { dev.index().to_string() } else { name.to_string() };
    let mask = Ipv4Addr::from(u32::MAX << (32 - prefix)).to_string();
    let a = addr.to_string();
    let set_addr = ["interface", "ipv4", "set", "address", &format!("name={ifname}"), "static", &a, &mask];
    // A brand-new adapter can take a moment to appear in the IP stack.
    let mut res = netsh(&set_addr);
    for _ in 0..20 {
        if res.is_ok() {
            break;
        }
        std::thread::sleep(Duration::from_millis(500));
        res = netsh(&set_addr);
    }
    res?;
    netsh(&["interface", "ipv4", "set", "subinterface", &ifname, &format!("mtu={mtu}"), "store=active"])
}

fn netsh(args: &[&str]) -> io::Result<()> {
    let out = std::process::Command::new("netsh").args(args).creation_flags(CREATE_NO_WINDOW).output()?;
    if out.status.success() {
        return Ok(());
    }
    let msg = String::from_utf8_lossy(&out.stdout).trim().to_string();
    Err(io::Error::other(format!("netsh {}: thất bại {msg}", args.join(" "))))
}
