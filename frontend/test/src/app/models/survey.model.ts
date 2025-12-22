// Survey Data Models

export type ParticipantType = 'PMA' | 'AFFECTED';

export type LikertValue = 1 | 2 | 3 | 4 | 5 | null;

export interface SurveyQuestion {
  id: string;
  text: string;
  category: string;
  subcategory: string;
  onlyPMA: boolean;
  reverse: boolean;
  order: number;
}

export interface SurveyCategory {
  name: string;
  subcategories: {
    name: string;
    questions: SurveyQuestion[];
  }[];
}

export interface SurveyTemplate {
  id: string;
  name: string;
  description?: string;
  version: string;
  active: boolean;
  categories: SurveyCategory[];
  createdAt: Date;
  updatedAt: Date;
}

export interface SurveyAnswer {
  questionId: string;
  value: LikertValue;
}

export interface SurveyResponse {
  id: string;
  templateId: string;
  participantType: ParticipantType;
  answers: SurveyAnswer[];
  submittedAt?: Date;
  createdAt: Date;
}

export interface SurveyInstance {
  id: string;
  templateId: string;
  template: SurveyTemplate;
  participantType?: ParticipantType;
  answers: SurveyAnswer[];
  submittedAt?: Date;
  createdAt: Date;
}

export interface SurveyResult {
  category: string;
  subcategory: string;
  average: number;
  answeredCount: number;
  totalCount: number;
  reverseItems: string[];
}
