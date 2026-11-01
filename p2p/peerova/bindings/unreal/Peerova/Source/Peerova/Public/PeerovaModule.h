#pragma once

#include "Modules/ModuleManager.h"

/** Loads the native peerova library (Windows/Mac: delay-loaded DLL/dylib) at startup. */
class FPeerovaModule : public IModuleInterface
{
public:
	virtual void StartupModule() override;
	virtual void ShutdownModule() override;

	/** True when the native library is available (always true on platforms that link it directly). */
	static bool IsLibraryLoaded();

private:
	void* LibHandle = nullptr;
};
