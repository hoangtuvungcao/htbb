#include "PeerovaSubsystem.h"

#include "Async/Async.h"
#include "Dom/JsonObject.h"
#include "HAL/FileManager.h"
#include "Misc/Paths.h"
#include "Serialization/JsonReader.h"
#include "Serialization/JsonSerializer.h"
#include "Serialization/JsonWriter.h"

DEFINE_LOG_CATEGORY_STATIC(LogPeerova, Log, All);

void UPeerovaSubsystem::Initialize(FSubsystemCollectionBase& Collection)
{
	Super::Initialize(Collection);
	bInitialized = true;
}

void UPeerovaSubsystem::Deinitialize()
{
	Shutdown();
	bInitialized = false;
	Super::Deinitialize();
}

TStatId UPeerovaSubsystem::GetStatId() const
{
	RETURN_QUICK_DECLARE_CYCLE_STAT(UPeerovaSubsystem, STATGROUP_Tickables);
}

bool UPeerovaSubsystem::IsTickable() const
{
	return bInitialized && Node && static_cast<bool>(*Node) && !IsTemplate();
}

void UPeerovaSubsystem::Tick(float DeltaTime)
{
	peerova::Event Ev;
	for (int32 i = 0; i < MaxEventsPerTick; ++i)
	{
		// Node may be reset by a delegate handler (e.g. Shutdown() inside OnDisconnected).
		if (!Node || !Node->poll(Ev, 0))
		{
			break;
		}
		const int32 Peer = static_cast<int32>(Ev.peer);
		switch (Ev.type)
		{
		case peerova::EventType::Connected:
			OnConnected.Broadcast(Peer);
			break;
		case peerova::EventType::Message:
		{
			TArray<uint8> Data(Ev.data.data(), static_cast<int32>(Ev.data.size()));
			OnMessage.Broadcast(Peer, Data, static_cast<int32>(Ev.channel), Ev.reliable);
			break;
		}
		case peerova::EventType::Disconnected:
			OnDisconnected.Broadcast(Peer, FString(UTF8_TO_TCHAR(Ev.text().c_str())));
			break;
		case peerova::EventType::ConnectFailed:
			OnConnectFailed.Broadcast(FString(UTF8_TO_TCHAR(Ev.text().c_str())));
			break;
		default:
			break;
		}
	}
}

void UPeerovaSubsystem::Host(int32 Port, const FString& ExtraConfigJson)
{
	Shutdown();
	const uint32 MyGen = ++Generation;
	bHostPending = true;

	// Build the config on the game thread (FJson* is fine off-thread, but keep it simple).
	TSharedPtr<FJsonObject> Cfg = MakeShared<FJsonObject>();
	if (!ExtraConfigJson.IsEmpty())
	{
		TSharedRef<TJsonReader<>> Reader = TJsonReaderFactory<>::Create(ExtraConfigJson);
		TSharedPtr<FJsonObject> Extra;
		if (FJsonSerializer::Deserialize(Reader, Extra) && Extra.IsValid())
		{
			Cfg = Extra;
		}
		else
		{
			UE_LOG(LogPeerova, Warning, TEXT("Host: ExtraConfigJson is not a JSON object, ignored"));
		}
	}
	Cfg->SetNumberField(TEXT("port"), Port);
	if (!Cfg->HasField(TEXT("key_file")))
	{
		// Persistent server identity, kept out of the packaged content.
		const FString KeyDir = FPaths::Combine(FPaths::ProjectSavedDir(), TEXT("Peerova"));
		IFileManager::Get().MakeDirectory(*KeyDir, true);
		Cfg->SetStringField(TEXT("key_file"), FPaths::ConvertRelativePathToFull(FPaths::Combine(KeyDir, TEXT("server.key"))));
	}
	FString Json;
	TSharedRef<TJsonWriter<>> Writer = TJsonWriterFactory<>::Create(&Json);
	FJsonSerializer::Serialize(Cfg.ToSharedRef(), Writer);
	std::string ConfigUtf8(TCHAR_TO_UTF8(*Json));

	TWeakObjectPtr<UPeerovaSubsystem> WeakThis(this);
	AsyncTask(ENamedThreads::AnyBackgroundThreadNormalTask, [WeakThis, MyGen, ConfigUtf8 = MoveTemp(ConfigUtf8)]() mutable
	{
		auto Started = std::make_unique<peerova::Node>(peerova::Node::server(ConfigUtf8));
		AsyncTask(ENamedThreads::GameThread, [WeakThis, MyGen, Started = MoveTemp(Started)]() mutable
		{
			UPeerovaSubsystem* Self = WeakThis.Get();
			if (!Self || Self->Generation != MyGen)
			{
				return; // subsystem gone or superseded: Started is freed here
			}
			Self->bHostPending = false;
			if (!*Started)
			{
				Self->LastError = UTF8_TO_TCHAR(Started->error.c_str());
				UE_LOG(LogPeerova, Error, TEXT("Host failed: %s"), *Self->LastError);
				Self->OnConnectFailed.Broadcast(Self->LastError);
				return;
			}
			Self->CachedTicket = UTF8_TO_TCHAR(Started->ticket().c_str());
			Self->Node = MoveTemp(Started);
			Self->OnHostStarted.Broadcast(Self->CachedTicket);
		});
	});
}

bool UPeerovaSubsystem::Join(const FString& Ticket, const FString& ConfigJson)
{
	Shutdown();
	++Generation;
	auto Client = std::make_unique<peerova::Node>(
		peerova::Node::client(std::string(TCHAR_TO_UTF8(*Ticket.TrimStartAndEnd())), std::string(TCHAR_TO_UTF8(*ConfigJson))));
	if (!*Client)
	{
		LastError = UTF8_TO_TCHAR(Client->error.c_str());
		UE_LOG(LogPeerova, Error, TEXT("Join failed: %s"), *LastError);
		OnConnectFailed.Broadcast(LastError);
		return false;
	}
	Node = MoveTemp(Client);
	return true;
}

bool UPeerovaSubsystem::Send(int32 Peer, const TArray<uint8>& Data, int32 Channel, bool bReliable)
{
	if (!Node || !*Node || Peer < 0 || Channel < 0 || Channel > 255)
	{
		return false;
	}
	if (!Node->send(static_cast<uint32>(Peer), Data.GetData(), static_cast<uint32>(Data.Num()), static_cast<uint8>(Channel), bReliable))
	{
		LastError = UTF8_TO_TCHAR(Node->error.c_str());
		UE_LOG(LogPeerova, Warning, TEXT("Send failed: %s"), *LastError);
		return false;
	}
	return true;
}

bool UPeerovaSubsystem::SendString(int32 Peer, const FString& Text, int32 Channel, bool bReliable)
{
	FTCHARToUTF8 Utf8(*Text);
	TArray<uint8> Data(reinterpret_cast<const uint8*>(Utf8.Get()), Utf8.Length());
	return Send(Peer, Data, Channel, bReliable);
}

void UPeerovaSubsystem::Disconnect(int32 Peer)
{
	if (Node && Peer >= 0)
	{
		Node->disconnect(static_cast<uint32>(Peer));
	}
}

void UPeerovaSubsystem::Shutdown()
{
	++Generation; // discard any pending Host() result
	bHostPending = false;
	Node.reset();
	CachedTicket.Reset();
}

FString UPeerovaSubsystem::GetTicket() const
{
	return CachedTicket;
}

int32 UPeerovaSubsystem::GetPeerCount() const
{
	return Node ? static_cast<int32>(Node->peer_count()) : 0;
}

bool UPeerovaSubsystem::IsActive() const
{
	return Node && static_cast<bool>(*Node);
}

FString UPeerovaSubsystem::BytesToUtf8String(const TArray<uint8>& Data)
{
	FUTF8ToTCHAR Conv(reinterpret_cast<const ANSICHAR*>(Data.GetData()), Data.Num());
	return FString(Conv.Length(), Conv.Get());
}
