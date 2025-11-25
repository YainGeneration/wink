import { type ChatMessage, type ChatSession } from "../components/UserChat";

export function convertNewChatToMessages(data:any): ChatMessage[] {
    return [
      {
        sender: "user",
        text: data.inputText,
        imageBase64: data.imageBase64 ? [data.imageBase64] : [],
      },
      {
        sender: "ai",
        text: data.aiMessage,
        keywords: data.keywords,
        recommendations: data.recommendations,
        mergedSentence: data.mergedSentence,
        interpretedSentence: data.interpretedSentence,
        englishText: data.englishText,
        englishCaption: data.englishCaption,
        imageDescriptionKo: data.imageDescriptionKo,
      }
    ]
}
