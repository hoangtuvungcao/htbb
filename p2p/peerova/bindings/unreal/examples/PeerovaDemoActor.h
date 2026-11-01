// How to use: copy PeerovaDemoActor.h/.cpp into your game module's Source folder (your module must list
// "Peerova" in its Build.cs dependencies), rename PEEROVADEMO_API to YOURGAME_API, build, and drop
// an APeerovaDemoActor into a level. Set bHost on one instance (the host prints its ticket to the log
// and screen); on the other set JoinTicket to that ticket (or the 6-character connect code) and play.
//
// Position-sync + chat demo on UPeerovaSubsystem:
//   * every client sends its owner pawn's location 20 times per second on channel 1, unreliable;
//   * SendChat() sends a line on channel 0, reliable; the host forwards chat lines to the other clients;
//   * received positions are stored in RemotePositions (peer id -> location) for you to visualize.
// Not yet compiled inside Unreal; core_demo.cpp contains the same logic, compiled and run without Unreal.
#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Actor.h"

#include "PeerovaDemoActor.generated.h"

class UPeerovaSubsystem;

UCLASS()
class PEEROVADEMO_API APeerovaDemoActor : public AActor
{
	GENERATED_BODY()

public:
	APeerovaDemoActor();

	/** Host a session (true) or join JoinTicket (false). */
	UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = "Peerova Demo")
	bool bHost = true;

	/** Host port (0 = random free port). */
	UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = "Peerova Demo")
	int32 Port = 47000;

	/** Ticket ("pvt_...") or 6-character connect code of the host, for bHost = false. */
	UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = "Peerova Demo")
	FString JoinTicket;

	/** Latest known location of each remote peer (peer id -> location). */
	UPROPERTY(BlueprintReadOnly, Category = "Peerova Demo")
	TMap<int32, FVector> RemotePositions;

	/** Sends a chat line to every connected peer (reliable, channel 0). */
	UFUNCTION(BlueprintCallable, Category = "Peerova Demo")
	void SendChat(const FString& Line);

protected:
	virtual void BeginPlay() override;
	virtual void EndPlay(const EEndPlayReason::Type Reason) override;
	virtual void Tick(float DeltaSeconds) override;

private:
	UFUNCTION() void HandleHostStarted(const FString& Ticket);
	UFUNCTION() void HandleConnected(int32 Peer);
	UFUNCTION() void HandleMessage(int32 Peer, const TArray<uint8>& Data, int32 Channel, bool bReliable);
	UFUNCTION() void HandleDisconnected(int32 Peer, const FString& Reason);
	UFUNCTION() void HandleConnectFailed(const FString& Reason);

	UPeerovaSubsystem* Net() const;

	TSet<int32> Peers;
	float SendAccumulator = 0.f;
};
