import ChatBubble from "./ChatBubble";

export type ChatMessage = {
  messageId?: number;
  sessionId?: number;
  sender: "user" | "ai";
  text?: string | null;
  imageBase64?: string[] | null;
  keywords?: string[];
  recommendations?: any[];
  mergedSentence?: string | null;
  interpretedSentence?: string | null;
  englishText?: string | null;
  englishCaption?: string | null;
  imageDescriptionKo?: string | null;
  timestamp?: string;
  nearbyMysic?: string[];
};


export type ChatSession = {
  sessionId: number;
  type?: "MY" | "SPACE";
  topic: string;
  latest?: boolean;
  messages?: ChatMessage[];
  nearbyMusic?: {
    songId: string;
    title: string;
    artist: string;
    albumCover: string;
  }[];

  // new-chat 구조
  inputText?: string;
  imageBase64?: string;
  aiMessage?: string;
  mergedSentence?: string;
  interpretedSentence?: string;
  englishText?: string;
  englishCaption?: string;
  imageDescriptionKo?: string;
  keywords?: string[];
  recommendations?: any[];
  timestamp?: string;
  location?: string;
};

type UserChatProps = {
  sessions: ChatSession[];
};

// sessions 기반으로만 렌더링
export default function UserChat({ sessions }: UserChatProps) {
  console.log("UserChat sessions:", sessions);

  return (
    <div style={{ marginBottom: "160px" }}>
      {sessions.map((session, i) => {
        // 1) old chat → messages 배열 존재
        if (session.messages && session.messages.length > 0) {
          return session.messages.map((m, idx) => (
            <ChatBubble
              key={`${i}-${idx}`}
              sender={m.sender}
              text={m.text}
              image={m.imageBase64}
              keywords={m.keywords}
              recommendations={m.recommendations}
              topic={session.topic}
              mergedSentence={m.mergedSentence}
              imageDescriptionKo={m.imageDescriptionKo ?? m.interpretedSentence}
              nearbyMusic={session.nearbyMusic ?? []}
            />
          ));
        }

        // 2) new chat → 단일 세션 데이터
        return (
          <ChatBubble
            key={i}
            sender="ai"
            type={session.type}
            text={session.interpretedSentence ?? session.aiMessage ?? ""}
            image={session.imageBase64 ? [session.imageBase64] : null}
            keywords={session.keywords ?? []}
            recommendations={session.recommendations ?? []}
            topic={session.topic}
            mergedSentence={session.mergedSentence ?? null}
            imageDescriptionKo={session.imageDescriptionKo ?? null}
            nearbyMusic={session.nearbyMusic ?? []}
          />
        );
      })}
    </div>
  );
}
