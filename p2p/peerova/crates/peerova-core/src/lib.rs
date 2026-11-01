//! peerova-core — P2P tunnel core.
//!
//! Zero third-party dependencies, zero relay. Connectivity strategies:
//! IPv6 direct, router port mapping (NAT-PMP/PCP, UPnP-IGD), own reflection
//! (STUN-like) and coordinated UDP hole punching via supernodes that are just
//! other reachable peerova servers — or fully manual join codes.

pub mod access;
pub mod conn;
pub mod crypto;
pub mod engine;
mod fuzz;
pub mod json;
pub mod keyfile;
pub mod license;
pub mod lobby;
pub mod nat;
pub mod pathcache;
pub mod netinfo;
pub mod node;
pub mod rdv;
pub mod relay;
pub mod release;
pub mod ticket;
pub mod tunnel;
pub mod util;
pub mod vpn;
pub mod wire;

pub use engine::{Access, ClientConfig, Cmd, Engine, EngineEvent, Handle, ServerConfig};
pub use node::Node;
pub use ticket::{JoinCode, Ticket};
pub use wire::{CandKind, Candidate, Packet};
