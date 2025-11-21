export type ChatMessage = {
  sender: "user" | "ai";
  text: string | null;
  image?: string | null;
  keywords?: string[];
  recommendations?: any[];
  topic?: string;
  mergedSentence?: string;
  imageDescriptionKo?: string;
  timestamp?: string;
};

export function convertNewChatToMessages(data: any): ChatMessage[] {
  return data.messages.map((m: any) => ({
    messageId: m.messageId,
    sessionId: m.sessionId,
    sender: m.sender,
    text: m.text,
    imageBase64: m.imageBase64,
    keywords: m.keywords || [],
    recommendations: m.recommendations || [],
    mergedSentence: m.mergedSentence,
    interpretedSentence: m.interpretedSentence,
    timestamp: m.timestamp
  }));
}
