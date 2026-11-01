//! Local network discovery using only std.

use std::net::{IpAddr, Ipv4Addr, Ipv6Addr, UdpSocket};

/// Primary outgoing IPv4 of this machine. `connect()` on UDP only asks the OS
/// routing table; no packet is sent. Documentation ranges are used as targets.
pub fn local_ipv4() -> Option<Ipv4Addr> {
    for target in ["192.0.2.1:9", "198.51.100.1:9"] {
        let Ok(s) = UdpSocket::bind("0.0.0.0:0") else {
            continue;
        };
        if s.connect(target).is_err() {
            continue;
        }
        if let Ok(a) = s.local_addr() {
            if let IpAddr::V4(ip) = a.ip() {
                if !ip.is_unspecified() {
                    return Some(ip);
                }
            }
        }
    }
    None
}

/// Primary global IPv6 (2000::/3) of this machine, if the ISP provides one.
pub fn global_ipv6() -> Option<Ipv6Addr> {
    for target in ["[2001:db8::1]:9", "[2400::1]:9"] {
        let Ok(s) = UdpSocket::bind("[::]:0") else {
            return None;
        };
        if s.connect(target).is_err() {
            continue;
        }
        if let Ok(a) = s.local_addr() {
            if let IpAddr::V6(ip) = a.ip() {
                if is_global_v6(&ip) {
                    return Some(ip);
                }
            }
        }
    }
    None
}

pub fn is_global_v6(ip: &Ipv6Addr) -> bool {
    let s = ip.segments();
    (s[0] & 0xe000) == 0x2000 && !(s[0] == 0x2001 && s[1] == 0x0db8)
}

pub fn is_cgnat_v4(ip: &Ipv4Addr) -> bool {
    let o = ip.octets();
    o[0] == 100 && (o[1] & 0xc0) == 64
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum V4Class {
    Public,
    Private,
    Cgnat,
    Other,
}

pub fn classify_v4(ip: &Ipv4Addr) -> V4Class {
    if is_cgnat_v4(ip) {
        V4Class::Cgnat
    } else if ip.is_private() {
        V4Class::Private
    } else if ip.is_loopback() || ip.is_link_local() || ip.is_unspecified() || ip.is_broadcast() {
        V4Class::Other
    } else {
        V4Class::Public
    }
}

/// Default IPv4 gateway. Linux: /proc/net/route. Elsewhere: guess x.y.z.1.
pub fn default_gateway_v4() -> Option<Ipv4Addr> {
    if let Ok(table) = std::fs::read_to_string("/proc/net/route") {
        for line in table.lines().skip(1) {
            let f: Vec<&str> = line.split_whitespace().collect();
            if f.len() > 2 && f[1] == "00000000" {
                if let Ok(gw) = u32::from_str_radix(f[2], 16) {
                    if gw != 0 {
                        return Some(Ipv4Addr::from(gw.to_le_bytes()));
                    }
                }
            }
        }
    }
    local_ipv4().map(|ip| {
        let o = ip.octets();
        Ipv4Addr::new(o[0], o[1], o[2], 1)
    })
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn classify() {
        assert_eq!(classify_v4(&"100.72.1.1".parse().unwrap()), V4Class::Cgnat);
        assert_eq!(
            classify_v4(&"192.168.1.5".parse().unwrap()),
            V4Class::Private
        );
        assert_eq!(classify_v4(&"14.160.1.1".parse().unwrap()), V4Class::Public);
        assert!(is_global_v6(&"2402:800:6314::1".parse().unwrap()));
        assert!(!is_global_v6(&"fe80::1".parse().unwrap()));
        assert!(!is_global_v6(&"fd00::1".parse().unwrap()));
    }
}
