// UPeerovaSubsystem — one Peerova node (host or client) per game instance, exposed to Blueprint.
//
// Blueprint:  Get Game Instance -> Get Subsystem (Peerova) -> Host(47000) / Join(Ticket)
//             bind OnHostStarted(Ticket), OnConnected(Peer), OnMessage(Peer, Data, Channel, bReliable),
//             OnDisconnected(Peer, Reason), OnConnectFailed(Reason)
// C++:        GetGameInstance()->GetSubsystem<UPeerovaSubsystem>()->SendString(Peer, TEXT("hi"));
//
// Events are polled on the game thread every tick, so delegates always fire on the game thread.
// Host() runs the (blocking ≤ ~3 s) server start on a background thread.
#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Tickable.h"

#include <memory>

#include "PeerovaCore.h" // header-only, engine-agnostic C++ wrapper over peerova.h

#include "PeerovaSubsystem.generated.h"

DECLARE_DYNAMIC_MULTICAST_DELEGATE_OneParam(FPeerovaHostStarted, const FString&, Ticket);
DECLARE_DYNAMIC_MULTICAST_DELEGATE_OneParam(FPeerovaConnected, int32, Peer);
DECLARE_DYNAMIC_MULTICAST_DELEGATE_FourParams(FPeerovaMessage, int32, Peer, const TArray<uint8>&, Data, int32, Channel, bool, bReliable);
DECLARE_DYNAMIC_MULTICAST_DELEGATE_TwoParams(FPeerovaDisconnected, int32, Peer, const FString&, Reason);
DECLARE_DYNAMIC_MULTICAST_DELEGATE_OneParam(FPeerovaConnectFailed, const FString&, Reason);

UCLASS()
class PEEROVA_API UPeerovaSubsystem : public UGameInstanceSubsystem, public FTickableGameObject
{
	GENERATED_BODY()

public:
	// USubsystem
	virtual void Initialize(FSubsystemCollectionBase& Collection) override;
	virtual void Deinitialize() override;

	// FTickableGameObject
	virtual void Tick(float DeltaTime) override;
	virtual TStatId GetStatId() const override;
	virtual bool IsTickable() const override;
	virtual bool IsTickableWhenPaused() const override { return true; }
	virtual ETickableTickType GetTickableTickType() const override { return ETickableTickType::Conditional; }

	/** Starts hosting. ExtraConfigJson: optional JSON object merged into the config
	 *  (e.g. {"license":"PVA-...","upnp":false,"token":"secret"}). Fires OnHostStarted or OnConnectFailed. */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	void Host(int32 Port = 47000, const FString& ExtraConfigJson = TEXT(""));

	/** Joins a host by ticket. Fires OnConnected or OnConnectFailed. ConfigJson e.g. {"token":"secret"}. */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	bool Join(const FString& Ticket, const FString& ConfigJson = TEXT(""));

	/** Reliable = ordered + retransmitted per channel (0..255); unreliable = datagram ≤ 1180 bytes. */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	bool Send(int32 Peer, const TArray<uint8>& Data, int32 Channel = 0, bool bReliable = true);

	/** Sends UTF-8 text. */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	bool SendString(int32 Peer, const FString& Text, int32 Channel = 0, bool bReliable = true);

	UFUNCTION(BlueprintCallable, Category = "Peerova")
	void Disconnect(int32 Peer);

	/** Frees the node (host or client). Safe to call at any time, also while Host() is pending. */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	void Shutdown();

	/** Host ticket ("pvt_..."), empty if not hosting. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	FString GetTicket() const;

	UFUNCTION(BlueprintPure, Category = "Peerova")
	int32 GetPeerCount() const;

	UFUNCTION(BlueprintPure, Category = "Peerova")
	bool IsActive() const;

	UFUNCTION(BlueprintPure, Category = "Peerova")
	bool IsHostPending() const { return bHostPending; }

	/** Decodes UTF-8 bytes (e.g. OnMessage Data) to a string. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	static FString BytesToUtf8String(const TArray<uint8>& Data);

	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaHostStarted OnHostStarted;

	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaConnected OnConnected;

	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaMessage OnMessage;

	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaDisconnected OnDisconnected;

	/** Client connect failure, or Host() failure. */
	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaConnectFailed OnConnectFailed;

	/** Max events dispatched per tick (rest stay queued for the next tick). */
	UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = "Peerova")
	int32 MaxEventsPerTick = 4096;

private:
	std::unique_ptr<peerova::Node> Node;
	bool bInitialized = false;
	bool bHostPending = false;
	/** Bumped by Shutdown()/Host()/Join() so a late background Host() result is discarded. */
	uint32 Generation = 0;
	FString CachedTicket;
	FString LastError;
};
