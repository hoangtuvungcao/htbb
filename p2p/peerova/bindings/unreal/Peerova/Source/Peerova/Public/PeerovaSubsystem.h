// UPeerovaSubsystem — one Peerova node (host or client) per game instance, exposed to Blueprint.
//
// Blueprint:  Get Game Instance -> Get Subsystem (Peerova Subsystem) -> Host(47000) / Join(Ticket)
//             bind OnHostStarted(Ticket), OnConnected(Peer), OnMessage(Peer, Data, Channel, bReliable),
//             OnDisconnected(Peer, Reason), OnConnectFailed(Reason)
// C++:        GetGameInstance()->GetSubsystem<UPeerovaSubsystem>()->SendString(Peer, TEXT("hi"));
//
// Threading: call everything from the game thread. Events are polled (non-blocking) on the game
// thread every tick, so delegates always fire on the game thread. Host() runs the blocking (≤ ~3 s)
// server start on a background thread.
//
// Peer ids: the native id is an unsigned 32-bit value; Blueprint has no uint32, so it is exposed as
// int32 with the same bits (ids above 2147483647 appear negative). Treat it as an opaque handle: pass
// it back unchanged to Send / SendString / Disconnect, compare it, use it as a map key.
#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Tickable.h"

#include <memory>

#include "PeerovaCore.h" // header-only, engine-agnostic C++ wrapper over peerova.h

#include "PeerovaSubsystem.generated.h"

/** Host() succeeded; Ticket ("pvt_...") is what clients pass to Join(). */
DECLARE_DYNAMIC_MULTICAST_DELEGATE_OneParam(FPeerovaHostStarted, const FString&, Ticket);
/** A peer connected. Host: a client (one event per client). Client: the host. */
DECLARE_DYNAMIC_MULTICAST_DELEGATE_OneParam(FPeerovaConnected, int32, Peer);
/** A payload from Peer on Channel (0..255); bReliable tells which kind of send produced it. */
DECLARE_DYNAMIC_MULTICAST_DELEGATE_FourParams(FPeerovaMessage, int32, Peer, const TArray<uint8>&, Data, int32, Channel, bool, bReliable);
/** Peer is gone (closed, timed out, or disconnected); Reason is human-readable text. */
DECLARE_DYNAMIC_MULTICAST_DELEGATE_TwoParams(FPeerovaDisconnected, int32, Peer, const FString&, Reason);
/** Join() could not connect (timeout, wrong token, host full...), or Host() failed. */
DECLARE_DYNAMIC_MULTICAST_DELEGATE_OneParam(FPeerovaConnectFailed, const FString&, Reason);

/**
 * One Peerova node (host = server, or client) per game instance. Host() or Join(), then react to the
 * On* events and call Send / SendString. Calling Host() or Join() again replaces the current node.
 */
UCLASS()
class PEEROVA_API UPeerovaSubsystem : public UGameInstanceSubsystem, public FTickableGameObject
{
	GENERATED_BODY()

public:
	// USubsystem
	virtual void Initialize(FSubsystemCollectionBase& Collection) override;
	virtual void Deinitialize() override;

	// FTickableGameObject — Tick() dispatches up to MaxEventsPerTick pending events (also while paused).
	virtual void Tick(float DeltaTime) override;
	virtual TStatId GetStatId() const override;
	virtual bool IsTickable() const override;
	virtual bool IsTickableWhenPaused() const override { return true; }
	virtual ETickableTickType GetTickableTickType() const override { return ETickableTickType::Conditional; }

	/**
	 * Starts hosting (replaces any current node). Returns immediately; fires OnHostStarted(Ticket) or
	 * OnConnectFailed(Reason) on a later tick. While pending, IsHostPending() is true.
	 * @param Port             UDP port (default 47000; 0 = random free port).
	 * @param ExtraConfigJson  optional JSON object with more server options: "license" ("PVA-..."; without it
	 *                         dev mode = max 3 clients), "max_ccu", "token" (shared secret), "upnp" (default true),
	 *                         "private", "key_file" (default Saved/Peerova/server.key), and for a 6-character
	 *                         connect code "app" (project id from the Peerova website) + "share"
	 *                         {"name","public","max","mode","region"}. "port" in it is overridden by Port.
	 */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	void Host(int32 Port = 47000, const FString& ExtraConfigJson = TEXT(""));

	/**
	 * Joins a host (replaces any current node). Fires OnConnected(Peer) or OnConnectFailed(Reason) later.
	 * @param Ticket      the host's ticket ("pvt_...") or its 6-character connect code. A code is looked up on
	 *                    the Peerova website first, which blocks the game thread for that request.
	 * @param ConfigJson  optional, e.g. {"token":"secret","timeout_ms":20000} (timeout default 20000 ms).
	 * @return false (and OnConnectFailed fires immediately) for a malformed ticket, unknown code or bad config.
	 */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	bool Join(const FString& Ticket, const FString& ConfigJson = TEXT(""));

	/**
	 * Sends bytes to Peer.
	 * @param Channel    0..255; different channels never block each other.
	 * @param bReliable  true: ordered + retransmitted per channel, any size (chat, inventory, RPC, file chunks).
	 *                   false: datagram, may be lost or reordered, max 1180 bytes (positions, inputs, voice-like state).
	 * @return false if there is no node, Channel is out of range, Peer is unknown or an unreliable payload is
	 *         larger than 1180 bytes (see GetLastError()).
	 */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	bool Send(int32 Peer, const TArray<uint8>& Data, int32 Channel = 0, bool bReliable = true);

	/** Sends Text encoded as UTF-8. Same parameters and result as Send(). */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	bool SendString(int32 Peer, const FString& Text, int32 Channel = 0, bool bReliable = true);

	/** Closes the connection to Peer; both sides then get OnDisconnected. */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	void Disconnect(int32 Peer);

	/** Disconnects everyone and frees the node (host or client). Safe to call at any time, also while
	 *  Host() is pending. Called automatically when the game instance shuts down. */
	UFUNCTION(BlueprintCallable, Category = "Peerova")
	void Shutdown();

	/** Host ticket ("pvt_..."), empty if not hosting. Same key_file ⇒ same ticket. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	FString GetTicket() const;

	/**
	 * The host's 6-character connect code (e.g. "K7Q2XD"), or empty. Only set when hosting with "app" + "share"
	 * in ExtraConfigJson, and only once the Peerova website has assigned it (shortly after OnHostStarted) —
	 * read it again later while empty. Clients can pass it to Join() instead of the ticket.
	 */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	FString GetRoomCode() const;

	/** Number of connected peers. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	int32 GetPeerCount() const;

	/** True when a node (host or client) exists. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	bool IsActive() const;

	/** True between Host() and OnHostStarted / OnConnectFailed. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	bool IsHostPending() const { return bHostPending; }

	/** Text of the last Host/Join/Send failure (from the native library), empty if none. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	FString GetLastError() const { return LastError; }

	/** Decodes UTF-8 bytes (e.g. OnMessage Data) to a string. */
	UFUNCTION(BlueprintPure, Category = "Peerova")
	static FString BytesToUtf8String(const TArray<uint8>& Data);

	/** Hosting started; the parameter is the ticket to share with clients. */
	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaHostStarted OnHostStarted;

	/** A peer connected (host: a client; client: the host). */
	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaConnected OnConnected;

	/** A message arrived. Use BytesToUtf8String for text. */
	UPROPERTY(BlueprintAssignable, Category = "Peerova")
	FPeerovaMessage OnMessage;

	/** A peer disconnected. */
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
