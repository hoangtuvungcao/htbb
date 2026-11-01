// See PeerovaDemoActor.h for how to use this example.
#include "PeerovaDemoActor.h"

#include "Engine/Engine.h"
#include "Engine/GameInstance.h"
#include "GameFramework/Pawn.h"
#include "Kismet/GameplayStatics.h"
#include "PeerovaSubsystem.h"

static constexpr int32 ChatChannel = 0;      // reliable, ordered
static constexpr int32 PositionChannel = 1;  // unreliable, latest value wins (max 1180 bytes)
static constexpr float SendInterval = 0.05f; // 20 Hz

APeerovaDemoActor::APeerovaDemoActor()
{
	PrimaryActorTick.bCanEverTick = true;
}

UPeerovaSubsystem* APeerovaDemoActor::Net() const
{
	UGameInstance* GI = GetGameInstance();
	return GI ? GI->GetSubsystem<UPeerovaSubsystem>() : nullptr;
}

void APeerovaDemoActor::BeginPlay()
{
	Super::BeginPlay();
	UPeerovaSubsystem* N = Net();
	if (!N)
	{
		return;
	}
	// Events are dispatched by the subsystem's tick on the game thread.
	N->OnHostStarted.AddDynamic(this, &APeerovaDemoActor::HandleHostStarted);
	N->OnConnected.AddDynamic(this, &APeerovaDemoActor::HandleConnected);
	N->OnMessage.AddDynamic(this, &APeerovaDemoActor::HandleMessage);
	N->OnDisconnected.AddDynamic(this, &APeerovaDemoActor::HandleDisconnected);
	N->OnConnectFailed.AddDynamic(this, &APeerovaDemoActor::HandleConnectFailed);

	if (bHost)
	{
		// Optional: TEXT("{\"license\":\"PVA-...\"}") as the second argument (without it: max 3 clients).
		N->Host(Port);
	}
	else if (!N->Join(JoinTicket, TEXT("{\"timeout_ms\":20000}")))
	{
		UE_LOG(LogTemp, Error, TEXT("Peerova join failed: %s"), *N->GetLastError());
	}
}

void APeerovaDemoActor::EndPlay(const EEndPlayReason::Type Reason)
{
	if (UPeerovaSubsystem* N = Net())
	{
		N->OnHostStarted.RemoveAll(this);
		N->OnConnected.RemoveAll(this);
		N->OnMessage.RemoveAll(this);
		N->OnDisconnected.RemoveAll(this);
		N->OnConnectFailed.RemoveAll(this);
		N->Shutdown(); // clean shutdown: disconnects peers, frees the node
	}
	Super::EndPlay(Reason);
}

void APeerovaDemoActor::Tick(float DeltaSeconds)
{
	Super::Tick(DeltaSeconds);
	UPeerovaSubsystem* N = Net();
	if (!N || Peers.Num() == 0)
	{
		return;
	}
	SendAccumulator += DeltaSeconds;
	if (SendAccumulator < SendInterval)
	{
		return;
	}
	SendAccumulator = 0.f;

	APawn* Pawn = UGameplayStatics::GetPlayerPawn(this, 0);
	const FVector Loc = Pawn ? Pawn->GetActorLocation() : GetActorLocation();
	// 3 floats = 12 bytes, far below the 1180-byte unreliable limit.
	const float Xyz[3] = { static_cast<float>(Loc.X), static_cast<float>(Loc.Y), static_cast<float>(Loc.Z) };
	TArray<uint8> Data(reinterpret_cast<const uint8*>(Xyz), sizeof(Xyz));
	for (int32 Peer : Peers)
	{
		N->Send(Peer, Data, PositionChannel, /*bReliable=*/false);
	}
}

void APeerovaDemoActor::SendChat(const FString& Line)
{
	if (UPeerovaSubsystem* N = Net())
	{
		for (int32 Peer : Peers)
		{
			N->SendString(Peer, Line, ChatChannel, /*bReliable=*/true);
		}
	}
}

void APeerovaDemoActor::HandleHostStarted(const FString& Ticket)
{
	UE_LOG(LogTemp, Log, TEXT("Peerova ticket: %s"), *Ticket);
	if (GEngine)
	{
		GEngine->AddOnScreenDebugMessage(-1, 30.f, FColor::Green, FString::Printf(TEXT("Ticket: %s"), *Ticket));
	}
}

void APeerovaDemoActor::HandleConnected(int32 Peer)
{
	Peers.Add(Peer); // Peer may be negative: it is the native uint32 id carried in an int32
	UE_LOG(LogTemp, Log, TEXT("Peerova peer %u connected"), static_cast<uint32>(Peer));
	if (!bHost)
	{
		SendChat(TEXT("hello from Unreal"));
	}
}

void APeerovaDemoActor::HandleMessage(int32 Peer, const TArray<uint8>& Data, int32 Channel, bool bReliable)
{
	if (Channel == PositionChannel && !bReliable && Data.Num() == 12)
	{
		float Xyz[3];
		FMemory::Memcpy(Xyz, Data.GetData(), sizeof(Xyz));
		RemotePositions.Add(Peer, FVector(Xyz[0], Xyz[1], Xyz[2]));
	}
	else if (Channel == ChatChannel)
	{
		const FString Text = UPeerovaSubsystem::BytesToUtf8String(Data);
		UE_LOG(LogTemp, Log, TEXT("<%u> %s"), static_cast<uint32>(Peer), *Text);
		if (bHost) // forward to the other clients
		{
			const FString Forwarded = FString::Printf(TEXT("<%u> %s"), static_cast<uint32>(Peer), *Text);
			for (int32 Other : Peers)
			{
				if (Other != Peer)
				{
					Net()->SendString(Other, Forwarded, ChatChannel, true);
				}
			}
		}
	}
}

void APeerovaDemoActor::HandleDisconnected(int32 Peer, const FString& Reason)
{
	Peers.Remove(Peer);
	RemotePositions.Remove(Peer);
	UE_LOG(LogTemp, Log, TEXT("Peerova peer %u left: %s"), static_cast<uint32>(Peer), *Reason);
}

void APeerovaDemoActor::HandleConnectFailed(const FString& Reason)
{
	UE_LOG(LogTemp, Error, TEXT("Peerova connect failed: %s"), *Reason);
}
