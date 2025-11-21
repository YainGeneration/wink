export type ChatMessage = {
  messageId: number;
  sessionId: number;
  sender: "user" | "ai";
  text: string | null;
  imageBase64: string[] | null;   // 배열로 바뀜
  keywords: string[];
  recommendations: any[];
  mergedSentence: string | null;
  interpretedSentence: string | null;
  timestamp: string;
};




export function convertSummaryToMessages(summary: any): ChatMessage[] {
  return [
    {
      messageId: 0,
      sessionId: 0,
      sender: "ai",
      text: summary.interpretedSentence || summary.summary || null,
      imageBase64: null,
      keywords: summary.keywords || [],
      recommendations: summary.recommendations || [],
      mergedSentence: summary.mergedSentence,
      interpretedSentence: summary.interpretedSentence,
      timestamp: ""
    }
  ];
}
