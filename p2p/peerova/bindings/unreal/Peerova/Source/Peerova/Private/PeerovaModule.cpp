#include "PeerovaModule.h"

#include "HAL/PlatformProcess.h"
#include "Interfaces/IPluginManager.h"
#include "Misc/Paths.h"

DEFINE_LOG_CATEGORY_STATIC(LogPeerovaModule, Log, All);

static bool GPeerovaLoaded = false;

// Windows and Mac delay-load the library (see Peerova.Build.cs); other platforms link it directly.
#if PLATFORM_WINDOWS
#define PEEROVA_DLL_NAME TEXT("peerova.dll")
#elif PLATFORM_MAC
#define PEEROVA_DLL_NAME TEXT("libpeerova.dylib")
#endif

void FPeerovaModule::StartupModule()
{
#if defined(PEEROVA_DLL_NAME)
	// Delay-loaded: try next to the executable first (packaged game / RuntimeDependencies copy),
	// then the plugin's ThirdParty folder (editor).
	TArray<FString> Candidates;
	Candidates.Add(FPaths::Combine(FPlatformProcess::GetModulesDirectory(), PEEROVA_DLL_NAME));
	if (TSharedPtr<IPlugin> Plugin = IPluginManager::Get().FindPlugin(TEXT("Peerova")))
	{
		const FString Lib = FPaths::Combine(Plugin->GetBaseDir(), TEXT("Source/ThirdParty/PeerovaLibrary/lib"));
#if PLATFORM_WINDOWS
		Candidates.Add(FPaths::Combine(Lib, TEXT("Win64"), PEEROVA_DLL_NAME));
#elif PLATFORM_MAC
		Candidates.Add(FPaths::Combine(Lib, TEXT("Mac"), PEEROVA_DLL_NAME));
#endif
	}
	for (const FString& Path : Candidates)
	{
		if (FPaths::FileExists(Path) && (LibHandle = FPlatformProcess::GetDllHandle(*Path)) != nullptr)
		{
			UE_LOG(LogPeerovaModule, Log, TEXT("Loaded %s"), *Path);
			break;
		}
	}
	GPeerovaLoaded = LibHandle != nullptr;
	if (!GPeerovaLoaded)
	{
		UE_LOG(LogPeerovaModule, Error, TEXT("Peerova: could not load %s"), PEEROVA_DLL_NAME);
	}
#else
	GPeerovaLoaded = true; // linked directly (Linux -lpeerova, Android .so, iOS static lib)
#endif
}

void FPeerovaModule::ShutdownModule()
{
	if (LibHandle)
	{
		FPlatformProcess::FreeDllHandle(LibHandle);
		LibHandle = nullptr;
	}
	GPeerovaLoaded = false;
}

bool FPeerovaModule::IsLibraryLoaded()
{
	return GPeerovaLoaded;
}

IMPLEMENT_MODULE(FPeerovaModule, Peerova)
