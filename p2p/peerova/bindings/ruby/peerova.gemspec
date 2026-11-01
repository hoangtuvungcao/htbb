Gem::Specification.new do |s|
  s.name        = "peerova"
  s.version     = "0.1.0"
  s.summary     = "Direct P2P networking (no relay, end-to-end encrypted) — Ruby binding over the peerova C ABI"
  s.description = "Fiddle (Ruby stdlib) binding for libpeerova. No compiler or native extension needed; " \
                  "ship libpeerova.so / peerova.dll / libpeerova.dylib next to your app or set PEEROVA_LIB."
  s.authors     = ["Peerova"]
  s.license     = "Proprietary"
  s.files       = ["lib/peerova.rb"]
  s.require_paths = ["lib"]
  s.required_ruby_version = ">= 3.0" # endless method definitions
  s.add_dependency "fiddle", ">= 1.0" # stdlib gem; listed for Ruby >= 3.5 where it became a bundled gem
end
