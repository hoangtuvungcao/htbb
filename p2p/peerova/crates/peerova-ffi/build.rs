// Give the shared library a proper SONAME on Linux/Android so apps link to
// "libpeerova.so" instead of embedding the build machine's absolute path.
fn main() {
    let os = std::env::var("CARGO_CFG_TARGET_OS").unwrap_or_default();
    if os == "linux" || os == "android" {
        println!("cargo:rustc-cdylib-link-arg=-Wl,-soname,libpeerova.so");
    }
}
