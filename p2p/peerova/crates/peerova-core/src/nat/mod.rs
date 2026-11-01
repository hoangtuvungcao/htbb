//! Ask the router to open a UDP port: NAT-PMP/PCP first (fast), then UPnP-IGD.

pub mod natpmp;
pub mod upnp;

use std::net::{Ipv4Addr, SocketAddrV4};
use std::time::Duration;

use crate::netinfo;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum MapMethod {
    NatPmp,
    Upnp,
}

#[derive(Debug, Clone)]
pub struct Mapping {
    pub method: MapMethod,
    pub external: SocketAddrV4,
    /// 0 = permanent.
    pub lifetime: u32,
}

pub const LEASE_SECS: u32 = 3600;

pub fn map_udp_port(local_ip: Ipv4Addr, port: u16, log: &mut dyn FnMut(String)) -> Option<Mapping> {
    map_port(local_ip, port, false, log)
}

/// Map a UDP or TCP port: NAT-PMP/PCP first, then UPnP-IGD.
pub fn map_port(local_ip: Ipv4Addr, port: u16, tcp: bool, log: &mut dyn FnMut(String)) -> Option<Mapping> {
    let proto = if tcp { "TCP" } else { "UDP" };
    if let Some(gw) = netinfo::default_gateway_v4() {
        match natpmp::external_address(gw) {
            Ok(ext_ip) => match natpmp::map(gw, tcp, port, port, LEASE_SECS) {
                Ok((ext_port, life)) => {
                    return Some(Mapping {
                        method: MapMethod::NatPmp,
                        external: SocketAddrV4::new(ext_ip, ext_port),
                        lifetime: life,
                    })
                }
                Err(e) => log(format!("NAT-PMP map thất bại: {e}")),
            },
            Err(e) => log(format!("NAT-PMP: {e}")),
        }
    }
    match upnp::discover(local_ip, Duration::from_millis(2500)) {
        Ok(igd) => {
            let ext_ip = match igd.external_ip() {
                Ok(ip) => ip,
                Err(e) => {
                    log(format!("UPnP: {e}"));
                    return None;
                }
            };
            match igd.add_mapping(proto, port, port, local_ip, LEASE_SECS) {
                Ok(life) => Some(Mapping {
                    method: MapMethod::Upnp,
                    external: SocketAddrV4::new(ext_ip, port),
                    lifetime: life,
                }),
                Err(e) => {
                    log(format!("UPnP map {proto} thất bại: {e}"));
                    None
                }
            }
        }
        Err(e) => {
            log(format!("UPnP: {e}"));
            None
        }
    }
}
