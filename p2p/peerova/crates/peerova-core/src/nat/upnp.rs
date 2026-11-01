//! Minimal UPnP-IGD client: SSDP discovery + SOAP over a hand-written HTTP/1.1 client.

use std::io::{Read, Write};
use std::net::{Ipv4Addr, SocketAddr, TcpStream, ToSocketAddrs, UdpSocket};
use std::time::{Duration, Instant};

const SSDP_ADDR: &str = "239.255.255.250:1900";
const SEARCH_TARGETS: &[&str] = &[
    "urn:schemas-upnp-org:device:InternetGatewayDevice:1",
    "urn:schemas-upnp-org:device:InternetGatewayDevice:2",
    "urn:schemas-upnp-org:service:WANIPConnection:1",
    "urn:schemas-upnp-org:service:WANPPPConnection:1",
];

#[derive(Debug, Clone)]
pub struct Igd {
    pub location: String,
    pub service_type: String,
    host: String,
    port: u16,
    path: String,
}

struct Url {
    host: String,
    port: u16,
    path: String,
}

fn parse_url(url: &str) -> Option<Url> {
    let rest = url.strip_prefix("http://")?;
    let (hostport, path) = match rest.find('/') {
        Some(i) => (&rest[..i], &rest[i..]),
        None => (rest, "/"),
    };
    let (host, port) = match hostport.rsplit_once(':') {
        Some((h, p)) => (h.to_string(), p.parse().ok()?),
        None => (hostport.to_string(), 80),
    };
    Some(Url { host, port, path: path.to_string() })
}

fn header<'a>(head: &'a str, name: &str) -> Option<&'a str> {
    head.lines().find_map(|l| {
        let (k, v) = l.split_once(':')?;
        k.trim().eq_ignore_ascii_case(name).then(|| v.trim())
    })
}

fn dechunk(body: &[u8]) -> Vec<u8> {
    let mut out = Vec::new();
    let mut p = 0;
    while p < body.len() {
        let Some(eol) = body[p..].windows(2).position(|w| w == b"\r\n") else { break };
        let line = String::from_utf8_lossy(&body[p..p + eol]);
        let size = usize::from_str_radix(line.split(';').next().unwrap_or("").trim(), 16).unwrap_or(0);
        p += eol + 2;
        if size == 0 || p + size > body.len() {
            break;
        }
        out.extend_from_slice(&body[p..p + size]);
        p += size + 2;
    }
    out
}

fn http(host: &str, port: u16, method: &str, path: &str, extra: &[(&str, String)], body: &str) -> Result<(u16, String), String> {
    let addr: SocketAddr = (host, port)
        .to_socket_addrs()
        .map_err(|e| e.to_string())?
        .next()
        .ok_or("không phân giải được host")?;
    let mut s = TcpStream::connect_timeout(&addr, Duration::from_secs(3)).map_err(|e| e.to_string())?;
    s.set_read_timeout(Some(Duration::from_secs(4))).ok();
    s.set_write_timeout(Some(Duration::from_secs(4))).ok();
    let mut req = format!("{method} {path} HTTP/1.1\r\nHost: {host}:{port}\r\nConnection: close\r\nUser-Agent: peerova/0.1 UPnP/1.1\r\n");
    for (k, v) in extra {
        req.push_str(&format!("{k}: {v}\r\n"));
    }
    req.push_str(&format!("Content-Length: {}\r\n\r\n{body}", body.len()));
    s.write_all(req.as_bytes()).map_err(|e| e.to_string())?;

    let mut raw = Vec::new();
    let mut buf = [0u8; 4096];
    loop {
        match s.read(&mut buf) {
            Ok(0) => break,
            Ok(n) => raw.extend_from_slice(&buf[..n]),
            Err(_) if !raw.is_empty() => break,
            Err(e) => return Err(e.to_string()),
        }
    }
    let split = raw.windows(4).position(|w| w == b"\r\n\r\n").ok_or("phản hồi HTTP hỏng")?;
    let head = String::from_utf8_lossy(&raw[..split]).to_string();
    let mut body = raw[split + 4..].to_vec();
    if header(&head, "Transfer-Encoding").is_some_and(|v| v.eq_ignore_ascii_case("chunked")) {
        body = dechunk(&body);
    }
    let status = head
        .split_whitespace()
        .nth(1)
        .and_then(|c| c.parse().ok())
        .ok_or("không đọc được status HTTP")?;
    Ok((status, String::from_utf8_lossy(&body).to_string()))
}

/// Text inside the first `<tag>` (ignores namespace prefixes like `<u:tag>`).
fn tag<'a>(xml: &'a str, name: &str) -> Option<&'a str> {
    let mut from = 0;
    while let Some(i) = xml[from..].find('<') {
        let start = from + i + 1;
        let end = start + xml[start..].find('>')?;
        let raw = &xml[start..end];
        let local = raw.split_whitespace().next().unwrap_or("");
        let local = local.rsplit(':').next().unwrap_or(local);
        if local == name && !raw.starts_with('/') {
            let close = xml[end + 1..].find("</")?;
            return Some(xml[end + 1..end + 1 + close].trim());
        }
        from = end + 1;
    }
    None
}

fn find_service(xml: &str) -> Option<(String, String)> {
    let mut best: Option<(String, String)> = None;
    for block in xml.split("<service>").skip(1) {
        let block = block.split("</service>").next().unwrap_or("");
        let (Some(st), Some(cu)) = (tag(block, "serviceType"), tag(block, "controlURL")) else { continue };
        if st.contains("WANIPConnection") {
            return Some((st.to_string(), cu.to_string()));
        }
        if st.contains("WANPPPConnection") && best.is_none() {
            best = Some((st.to_string(), cu.to_string()));
        }
    }
    best
}

fn igd_from_location(location: &str) -> Result<Igd, String> {
    let u = parse_url(location).ok_or("LOCATION không hợp lệ")?;
    let (status, xml) = http(&u.host, u.port, "GET", &u.path, &[], "")?;
    if status != 200 {
        return Err(format!("tải mô tả thiết bị lỗi HTTP {status}"));
    }
    let (service_type, control) = find_service(&xml).ok_or("router không có dịch vụ WANIP/WANPPPConnection")?;
    let base = tag(&xml, "URLBase")
        .map(|b| b.trim_end_matches('/').to_string())
        .unwrap_or_else(|| format!("http://{}:{}", u.host, u.port));
    let full = if control.starts_with("http") {
        control
    } else if control.starts_with('/') {
        format!("{base}{control}")
    } else {
        format!("{base}/{control}")
    };
    let c = parse_url(&full).ok_or("controlURL không hợp lệ")?;
    Ok(Igd { location: location.to_string(), service_type, host: c.host, port: c.port, path: c.path })
}

/// Find the Internet Gateway Device on the LAN.
pub fn discover(local_ip: Ipv4Addr, timeout: Duration) -> Result<Igd, String> {
    let sock = UdpSocket::bind((local_ip, 0)).map_err(|e| e.to_string())?;
    sock.set_multicast_ttl_v4(2).ok();
    for st in SEARCH_TARGETS {
        let msg = format!(
            "M-SEARCH * HTTP/1.1\r\nHOST: {SSDP_ADDR}\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\nST: {st}\r\n\r\n"
        );
        sock.send_to(msg.as_bytes(), SSDP_ADDR).map_err(|e| e.to_string())?;
    }
    let deadline = Instant::now() + timeout;
    let mut locations: Vec<String> = Vec::new();
    let mut buf = [0u8; 2048];
    while let Some(left) = deadline.checked_duration_since(Instant::now()) {
        sock.set_read_timeout(Some(left.max(Duration::from_millis(10)))).ok();
        let Ok((n, _)) = sock.recv_from(&mut buf) else { break };
        let text = String::from_utf8_lossy(&buf[..n]);
        if let Some(loc) = header(&text, "LOCATION") {
            if !locations.iter().any(|l| l == loc) {
                locations.push(loc.to_string());
                // First good answer wins; keep collecting only if it fails.
                if let Ok(igd) = igd_from_location(loc) {
                    return Ok(igd);
                }
            }
        }
    }
    if locations.is_empty() {
        Err("không tìm thấy router UPnP (UPnP tắt hoặc mạng chặn multicast)".into())
    } else {
        Err(format!("tìm thấy {} thiết bị UPnP nhưng không có IGD dùng được", locations.len()))
    }
}

impl Igd {
    fn soap(&self, action: &str, args: &str) -> Result<String, String> {
        let body = format!(
            "<?xml version=\"1.0\"?>\r\n<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" \
             s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>\
             <u:{action} xmlns:u=\"{st}\">{args}</u:{action}></s:Body></s:Envelope>\r\n",
            st = self.service_type
        );
        let headers = [
            ("Content-Type", "text/xml; charset=\"utf-8\"".to_string()),
            ("SOAPAction", format!("\"{}#{action}\"", self.service_type)),
        ];
        let (status, resp) = http(&self.host, self.port, "POST", &self.path, &headers, &body)?;
        if status != 200 {
            let code = tag(&resp, "errorCode").unwrap_or("?");
            let desc = tag(&resp, "errorDescription").unwrap_or("");
            return Err(format!("{action} lỗi HTTP {status}, UPnP {code} {desc}"));
        }
        Ok(resp)
    }

    pub fn external_ip(&self) -> Result<Ipv4Addr, String> {
        let r = self.soap("GetExternalIPAddress", "")?;
        tag(&r, "NewExternalIPAddress")
            .and_then(|s| s.parse().ok())
            .ok_or_else(|| "router không trả IP ngoài".into())
    }

    pub fn add_udp_mapping(&self, ext_port: u16, int_port: u16, client: Ipv4Addr, lease: u32) -> Result<u32, String> {
        self.add_mapping("UDP", ext_port, int_port, client, lease)
    }

    pub fn add_mapping(&self, proto: &str, ext_port: u16, int_port: u16, client: Ipv4Addr, lease: u32) -> Result<u32, String> {
        let args = |lease: u32| {
            format!(
                "<NewRemoteHost></NewRemoteHost><NewExternalPort>{ext_port}</NewExternalPort>\
                 <NewProtocol>{proto}</NewProtocol><NewInternalPort>{int_port}</NewInternalPort>\
                 <NewInternalClient>{client}</NewInternalClient><NewEnabled>1</NewEnabled>\
                 <NewPortMappingDescription>peerova</NewPortMappingDescription>\
                 <NewLeaseDuration>{lease}</NewLeaseDuration>"
            )
        };
        match self.soap("AddPortMapping", &args(lease)) {
            Ok(_) => Ok(lease),
            // 725 = OnlyPermanentLeasesSupported
            Err(e) if e.contains("725") => self.soap("AddPortMapping", &args(0)).map(|_| 0),
            Err(e) => Err(e),
        }
    }

    pub fn delete_udp_mapping(&self, ext_port: u16) -> Result<(), String> {
        self.delete_mapping("UDP", ext_port)
    }

    pub fn delete_mapping(&self, proto: &str, ext_port: u16) -> Result<(), String> {
        let args = format!(
            "<NewRemoteHost></NewRemoteHost><NewExternalPort>{ext_port}</NewExternalPort><NewProtocol>{proto}</NewProtocol>"
        );
        self.soap("DeletePortMapping", &args).map(|_| ())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_service_and_tags() {
        let xml = "<root><URLBase>http://192.168.1.1:5000/</URLBase><device><serviceList>\
            <service><serviceType>urn:schemas-upnp-org:service:Layer3Forwarding:1</serviceType><controlURL>/l3f</controlURL></service>\
            <service><serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType><controlURL>/ctl/IPConn</controlURL></service>\
            </serviceList></device></root>";
        let (st, cu) = find_service(xml).unwrap();
        assert!(st.contains("WANIPConnection"));
        assert_eq!(cu, "/ctl/IPConn");
        let resp = "<s:Envelope><s:Body><u:GetExternalIPAddressResponse xmlns:u=\"x\">\
            <NewExternalIPAddress>113.161.2.3</NewExternalIPAddress></u:GetExternalIPAddressResponse></s:Body></s:Envelope>";
        assert_eq!(tag(resp, "NewExternalIPAddress"), Some("113.161.2.3"));
    }

    #[test]
    fn dechunks() {
        assert_eq!(dechunk(b"5\r\nhello\r\n6\r\n world\r\n0\r\n\r\n"), b"hello world");
    }

    #[test]
    fn parses_urls() {
        let u = parse_url("http://192.168.1.1:1900/rootDesc.xml").unwrap();
        assert_eq!((u.host.as_str(), u.port, u.path.as_str()), ("192.168.1.1", 1900, "/rootDesc.xml"));
    }
}
