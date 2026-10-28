import { InterviewMessage } from './models';

export interface CollectedItem {
  question: string;
  answer: string;
  skipped: boolean;
  questionKey: string | null;
}

export function collectedFrom(messages: InterviewMessage[] | null | undefined): CollectedItem[] {
  const items: CollectedItem[] = [];
  let pendingQuestion = '';
  for (const message of messages ?? []) {
    if (message.role === 'ASSISTANT') {
      pendingQuestion = message.content;
      continue;
    }
    if (message.role === 'USER') {
      items.push({
        question: pendingQuestion,
        answer: message.content,
        skipped: message.skipped,
        questionKey: message.questionKey,
      });
      pendingQuestion = '';
    }
  }
  return items;
}
