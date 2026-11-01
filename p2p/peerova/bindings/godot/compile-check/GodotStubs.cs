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
        public Error EmitSignal(StringName signal, params Variant[] args) { SignalSink?.Invoke(signal.Name, args); RaiseGeneratedEvent(signal.Name, args); return Error.Ok; }
        // Stand-in for the C# events Godot's source generator creates for [Signal] delegates.
        protected virtual void RaiseGeneratedEvent(string signal, Variant[] args) { }
    }
    public partial class Node : GodotObject
    {
        public virtual void _Ready() { }
        public virtual void _Process(double delta) { }
        public virtual void _ExitTree() { }
        public class SignalName { }
        public readonly List<Node> Children = new List<Node>();
        public void AddChild(Node child) => Children.Add(child);
    }
    public partial class CanvasItem : Node
    {
        public void QueueRedraw() { }
        public virtual void _Draw() { }
        public void DrawCircle(Vector2 position, float radius, Color color) { }
    }
    public partial class Node2D : CanvasItem { public Vector2 Position { get; set; } }
    public struct Vector2
    {
        public float X, Y;
        public Vector2(float x, float y) { X = x; Y = y; }
        public static readonly Vector2 Zero = new Vector2(0, 0);
        public static Vector2 operator +(Vector2 a, Vector2 b) => new Vector2(a.X + b.X, a.Y + b.Y);
        public static Vector2 operator -(Vector2 a, Vector2 b) => new Vector2(a.X - b.X, a.Y - b.Y);
        public static Vector2 operator *(Vector2 a, float f) => new Vector2(a.X * f, a.Y * f);
    }
    public struct Color { public float R, G, B, A; public Color(float r, float g, float b, float a = 1f) { R = r; G = g; B = b; A = a; } }
    public static class Input { public static Vector2 GetVector(string negX, string posX, string negY, string posY) => Vector2.Zero; }
    public static class Mathf { public static float Cos(float f) => MathF.Cos(f); public static float Sin(float f) => MathF.Sin(f); }
    public static class GD { public static void Print(params object[] a) => Console.WriteLine(string.Concat(a)); public static void PushError(string m) => Console.WriteLine("ERROR " + m); }
    public static class OS
    {
        public static string GetName() => OperatingSystem.IsWindows() ? "Windows" : OperatingSystem.IsMacOS() ? "macOS" : "Linux";
        public static string GetExecutablePath() => Environment.ProcessPath;
        public static string[] GetCmdlineUserArgs() => Array.Empty<string>();
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
            public static readonly Godot.StringName RoomCodeReady = "RoomCodeReady";
        }

        HostStartedEventHandler _hostStarted; RoomCodeReadyEventHandler _roomCodeReady; ConnectedEventHandler _connected;
        MessageEventHandler _message; DisconnectedEventHandler _disconnected; ConnectFailedEventHandler _connectFailed;
        public event HostStartedEventHandler HostStarted { add => _hostStarted += value; remove => _hostStarted -= value; }
        public event RoomCodeReadyEventHandler RoomCodeReady { add => _roomCodeReady += value; remove => _roomCodeReady -= value; }
        public event ConnectedEventHandler Connected { add => _connected += value; remove => _connected -= value; }
        public event MessageEventHandler Message { add => _message += value; remove => _message -= value; }
        public event DisconnectedEventHandler Disconnected { add => _disconnected += value; remove => _disconnected -= value; }
        public event ConnectFailedEventHandler ConnectFailed { add => _connectFailed += value; remove => _connectFailed -= value; }

        protected override void RaiseGeneratedEvent(string signal, Godot.Variant[] a)
        {
            switch (signal)
            {
                case "HostStarted": _hostStarted?.Invoke((string)a[0].Obj); break;
                case "RoomCodeReady": _roomCodeReady?.Invoke((string)a[0].Obj); break;
                case "Connected": _connected?.Invoke((long)a[0].Obj); break;
                case "Message": _message?.Invoke((long)a[0].Obj, (long)a[1].Obj, (byte[])a[2].Obj, (bool)a[3].Obj); break;
                case "Disconnected": _disconnected?.Invoke((long)a[0].Obj); break;
                case "ConnectFailed": _connectFailed?.Invoke((string)a[0].Obj); break;
            }
        }
    }
}
