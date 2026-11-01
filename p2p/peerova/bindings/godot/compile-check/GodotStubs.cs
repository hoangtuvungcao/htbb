// Minimal stand-ins for GodotSharp types used by the addon. Compile-check only.
using System;
using System.Collections.Generic;
namespace Godot
{
    public sealed class StringName
    {
        public readonly string Name; public StringName(string n) { Name = n; }
        public static implicit operator StringName(string s) => new StringName(s);
        public override string ToString() => Name;
    }
    public readonly struct Variant
    {
        public readonly object Obj; Variant(object o) { Obj = o; }
        public static implicit operator Variant(string v) => new Variant(v);
        public static implicit operator Variant(long v) => new Variant(v);
        public static implicit operator Variant(bool v) => new Variant(v);
        public static implicit operator Variant(byte[] v) => new Variant(v);
    }
    public enum Error { Ok }
    public class GodotObject
    {
        public Action<string, Variant[]> SignalSink; // test hook
        public Error EmitSignal(StringName signal, params Variant[] args) { SignalSink?.Invoke(signal.Name, args); return Error.Ok; }
    }
    public partial class Node : GodotObject
    {
        public virtual void _Ready() { }
        public virtual void _Process(double delta) { }
        public virtual void _ExitTree() { }
        public class SignalName { }
    }
    public static class GD { public static void Print(params object[] a) => Console.WriteLine(string.Concat(a)); public static void PushError(string m) => Console.WriteLine("ERROR " + m); }
    public static class OS
    {
        public static string GetName() => OperatingSystem.IsWindows() ? "Windows" : OperatingSystem.IsMacOS() ? "macOS" : "Linux";
        public static string GetExecutablePath() => Environment.ProcessPath;
    }
    public static class ProjectSettings
    {
        public static string GlobalizePath(string p) =>
            p.StartsWith("user://") ? System.IO.Path.Combine(System.IO.Path.GetTempPath(), p.Substring(7)) :
            p.StartsWith("res://") ? System.IO.Path.Combine(AppContext.BaseDirectory, p.Substring(6)) : p;
    }
    [AttributeUsage(AttributeTargets.Delegate)] public class SignalAttribute : Attribute { }
    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Property)] public class ExportAttribute : Attribute { }
    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Property)] public class ExportGroupAttribute : Attribute { public ExportGroupAttribute(string n) { } }
    [AttributeUsage(AttributeTargets.Class)] public class GlobalClassAttribute : Attribute { }
}

// What Godot's source generator emits for the [Signal] delegates (names drop "EventHandler").
namespace Peerova.GodotAddon
{
    public partial class PeerovaNode
    {
        public new class SignalName : Godot.Node.SignalName
        {
            public static readonly Godot.StringName HostStarted = "HostStarted";
            public static readonly Godot.StringName Connected = "Connected";
            public static readonly Godot.StringName Message = "Message";
            public static readonly Godot.StringName Disconnected = "Disconnected";
            public static readonly Godot.StringName ConnectFailed = "ConnectFailed";
        }
    }
}
