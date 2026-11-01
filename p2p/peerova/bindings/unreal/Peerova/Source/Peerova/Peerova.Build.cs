// Peerova Unreal module. Native library layout (see Source/ThirdParty/PeerovaLibrary/README.md):
//   ThirdParty/PeerovaLibrary/include/peerova.h
//   ThirdParty/PeerovaLibrary/lib/Win64/peerova.dll + peerova.dll.lib   (MSVC import library)
//   ThirdParty/PeerovaLibrary/lib/Linux/libpeerova.so
//   ThirdParty/PeerovaLibrary/lib/Mac/libpeerova.dylib                    (universal, id @rpath/libpeerova.dylib)
//   ThirdParty/PeerovaLibrary/lib/Android/<arm64-v8a|armeabi-v7a|x86_64>/libpeerova.so  (packaged by Peerova_APL.xml)
//   ThirdParty/PeerovaLibrary/lib/IOS/libpeerova.a                        (static, arm64)
using System.IO;
using UnrealBuildTool;

public class Peerova : ModuleRules
{
	public Peerova(ReadOnlyTargetRules Target) : base(Target)
	{
		PCHUsage = PCHUsageMode.UseExplicitOrSharedPCHs;
		CppStandard = CppStandardVersion.Cpp17;

		PublicDependencyModuleNames.AddRange(new string[] { "Core", "CoreUObject", "Engine" });
		PrivateDependencyModuleNames.AddRange(new string[] { "Projects", "Json" });

		string ThirdParty = Path.GetFullPath(Path.Combine(ModuleDirectory, "..", "ThirdParty", "PeerovaLibrary"));
		string LibRoot = Path.Combine(ThirdParty, "lib");
		PublicIncludePaths.Add(Path.Combine(ThirdParty, "include"));

		if (Target.Platform == UnrealTargetPlatform.Win64)
		{
			string Dir = Path.Combine(LibRoot, "Win64");
			// MSVC import library for peerova.dll (generate it from lib/Win64/peerova.def if you only have the DLL:
			// `lib /def:peerova.def /machine:x64 /out:peerova.dll.lib`). A MinGW libpeerova.dll.a does not work here.
			PublicAdditionalLibraries.Add(Path.Combine(Dir, "peerova.dll.lib"));
			PublicDelayLoadDLLs.Add("peerova.dll");
			// Copied next to the executable (packaged game) / plugin binaries (editor).
			RuntimeDependencies.Add("$(BinaryOutputDir)/peerova.dll", Path.Combine(Dir, "peerova.dll"));
		}
		else if (Target.Platform == UnrealTargetPlatform.Linux)
		{
			string Dir = Path.Combine(LibRoot, "Linux");
			// Link via -L/-l (not by full path) so DT_NEEDED is just "libpeerova.so" and the
			// copy next to the binary is found through $ORIGIN.
			PublicSystemLibraryPaths.Add(Dir);
			PublicSystemLibraries.Add("peerova");
			RuntimeDependencies.Add("$(BinaryOutputDir)/libpeerova.so", Path.Combine(Dir, "libpeerova.so"));
		}
		else if (Target.Platform == UnrealTargetPlatform.Mac)
		{
			string Dylib = Path.Combine(LibRoot, "Mac", "libpeerova.dylib");
			PublicDelayLoadDLLs.Add(Dylib);
			RuntimeDependencies.Add("$(BinaryOutputDir)/libpeerova.dylib", Dylib);
		}
		else if (Target.Platform == UnrealTargetPlatform.Android)
		{
			// UBT links per architecture by full path; each .so must carry SONAME libpeerova.so
			// (the Android builds from Peerova do), otherwise the absolute path ends up in libUnreal.so.
			foreach (string Abi in new string[] { "arm64-v8a", "armeabi-v7a", "x86_64" })
			{
				string So = Path.Combine(LibRoot, "Android", Abi, "libpeerova.so");
				if (File.Exists(So))
				{
					PublicAdditionalLibraries.Add(So);
				}
			}
			// Copies the .so files into the APK's lib/<abi>/ and loads them.
			AdditionalPropertiesForReceipt.Add("AndroidPlugin", Path.Combine(ModuleDirectory, "Peerova_APL.xml"));
		}
		else if (Target.Platform == UnrealTargetPlatform.IOS)
		{
			PublicAdditionalLibraries.Add(Path.Combine(LibRoot, "IOS", "libpeerova.a"));
			PublicFrameworks.Add("Security");
			PublicSystemLibraries.Add("resolv");
		}
	}
}
