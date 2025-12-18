export interface Question {
  id: string;
  text: string;
  categoryId: string;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  questions: Question[];
}

export interface Answer {
  questionId: string;
  value: number; // 1-5
}

export type AnalysisMode = 'quick-check' | 'comprehensive';
export type ContextType = 'general' | 'project';

export interface WizardState {
  analysisMode?: AnalysisMode;
  contextType?: ContextType;
  answers: Answer[];
  currentCategoryIndex: number;
}

export const MOCK_CATEGORIES: Category[] = [
  {
    id: 'leadership',
    name: 'Führung',
    description: 'Bewertung der Führungsqualität und -kompetenz',
    questions: [
      {
        id: 'leadership-1',
        text: 'Die Führungskräfte kommunizieren klar und transparent über Veränderungen.',
        categoryId: 'leadership'
      },
      {
        id: 'leadership-2',
        text: 'Die Führungsebene zeigt Engagement und Commitment für die Veränderung.',
        categoryId: 'leadership'
      },
      {
        id: 'leadership-3',
        text: 'Führungskräfte sind gut vorbereitet und haben die notwendigen Kompetenzen für die Veränderung.',
        categoryId: 'leadership'
      }
    ]
  },
  {
    id: 'communication',
    name: 'Kommunikation',
    description: 'Qualität und Effektivität der Kommunikation',
    questions: [
      {
        id: 'communication-1',
        text: 'Informationen über die Veränderung werden regelmäßig und rechtzeitig kommuniziert.',
        categoryId: 'communication'
      },
      {
        id: 'communication-2',
        text: 'Es gibt ausreichend Möglichkeiten, Fragen zu stellen und Feedback zu geben.',
        categoryId: 'communication'
      },
      {
        id: 'communication-3',
        text: 'Die Kommunikation ist klar, verständlich und zielgruppengerecht.',
        categoryId: 'communication'
      }
    ]
  },
  {
    id: 'culture',
    name: 'Kultur',
    description: 'Organisationskultur und Veränderungsbereitschaft',
    questions: [
      {
        id: 'culture-1',
        text: 'Die Organisationskultur unterstützt Innovation und Veränderung.',
        categoryId: 'culture'
      },
      {
        id: 'culture-2',
        text: 'Mitarbeitende zeigen eine positive Einstellung gegenüber Veränderungen.',
        categoryId: 'culture'
      },
      {
        id: 'culture-3',
        text: 'Es gibt eine Kultur des Lernens und der kontinuierlichen Verbesserung.',
        categoryId: 'culture'
      }
    ]
  },
  {
    id: 'processes',
    name: 'Prozesse',
    description: 'Effizienz und Anpassungsfähigkeit von Prozessen',
    questions: [
      {
        id: 'processes-1',
        text: 'Die bestehenden Prozesse sind flexibel genug, um Veränderungen zu unterstützen.',
        categoryId: 'processes'
      },
      {
        id: 'processes-2',
        text: 'Prozesse werden regelmäßig überprüft und optimiert.',
        categoryId: 'processes'
      },
      {
        id: 'processes-3',
        text: 'Es gibt klare Verantwortlichkeiten und Rollen für die Umsetzung von Veränderungen.',
        categoryId: 'processes'
      }
    ]
  },
  {
    id: 'it-tools',
    name: 'IT & Tools',
    description: 'Technologische Infrastruktur und Tools',
    questions: [
      {
        id: 'it-tools-1',
        text: 'Die IT-Infrastruktur unterstützt die geplanten Veränderungen.',
        categoryId: 'it-tools'
      },
      {
        id: 'it-tools-2',
        text: 'Mitarbeitende haben Zugang zu den notwendigen Tools und Systemen.',
        categoryId: 'it-tools'
      },
      {
        id: 'it-tools-3',
        text: 'Es gibt ausreichend Schulungen und Support für neue Tools.',
        categoryId: 'it-tools'
      }
    ]
  }
];

export const SCALE_LABELS = [
  'Stimme gar nicht zu',
  'Stimme eher nicht zu',
  'Weder noch',
  'Stimme eher zu',
  'Stimme voll zu'
];

export const ANALYSIS_MODES = [
  {
    id: 'quick-check' as AnalysisMode,
    title: 'Schnellcheck',
    description: '1 Person',
    icon: '⚡'
  },
  {
    id: 'comprehensive' as AnalysisMode,
    title: 'Umfassende Analyse',
    description: 'Alle Mitarbeitenden',
    icon: '📊'
  }
];

export const CONTEXT_TYPES = [
  {
    id: 'general' as ContextType,
    title: 'Allgemeine Change Readiness',
    description: 'Bewertung der allgemeinen Veränderungsbereitschaft der Organisation',
    icon: '🏢'
  },
  {
    id: 'project' as ContextType,
    title: 'Projektbezogene Change Readiness',
    description: 'Bewertung der Bereitschaft für ein spezifisches Projekt',
    icon: '🎯'
  }
];

