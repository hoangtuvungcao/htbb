# JNI looks up dev.peerova.Peerova$Event by name and calls its constructor from C.
-keep class dev.peerova.Peerova { native <methods>; }
-keep class dev.peerova.Peerova$Event { <init>(...); *; }
