import { SurveyTemplate, SurveyCategory } from '../models/survey.model';

// Standard ChangeReady360 Fragebogen-Vorlage (exakt nach Vorgabe)
export const STANDARD_SURVEY_TEMPLATE: SurveyTemplate = {
  id: 'standard-v1',
  name: 'ChangeReady360 – Standard',
  description: 'Standard-Fragebogen zur Erfassung der Change-Readiness',
  version: '1.0',
  active: true,
  categories: [
    {
      name: 'A) Kommunikation',
      subcategories: [
        {
          name: '1) Unterstützung',
          questions: [
            {
              id: 'A1.1',
              text: 'Ich weiß, wer meine Ansprechpartner bei Fragen, Unsicherheiten und Problemen bezüglich des Change sind',
              category: 'A) Kommunikation',
              subcategory: '1) Unterstützung',
              onlyPMA: false,
              reverse: false,
              order: 1
            },
            {
              id: 'A1.2',
              text: 'Ich erlebe die Ansprechpartner des Change (PM und/oder FKs) als verfügbar, wenn ich Probleme oder Fragen rund um den Change habe',
              category: 'A) Kommunikation',
              subcategory: '1) Unterstützung',
              onlyPMA: false,
              reverse: false,
              order: 2
            },
            {
              id: 'A1.3',
              text: 'Ich werde ermutigt, beim Change mitzugehen',
              category: 'A) Kommunikation',
              subcategory: '1) Unterstützung',
              onlyPMA: false,
              reverse: false,
              order: 3
            }
          ]
        },
        {
          name: '2) Offener Umgang',
          questions: [
            {
              id: 'A2.1',
              text: 'Ich fühle mich wohl dabei, Unsicherheiten und Bedenken bezüglich des Change zu äußern',
              category: 'A) Kommunikation',
              subcategory: '2) Offener Umgang',
              onlyPMA: false,
              reverse: false,
              order: 4
            }
          ]
        }
      ]
    },
    {
      name: 'B) Persönliche Vorteile/Nachteile',
      subcategories: [
        {
          name: '1) Allgemein',
          questions: [
            {
              id: 'B1.1',
              text: 'Ich glaube, dass es sich für mich lohnt, wenn die Organisation diesen Change umsetzt.',
              category: 'B) Persönliche Vorteile/Nachteile',
              subcategory: '1) Allgemein',
              onlyPMA: false,
              reverse: false,
              order: 5
            }
          ]
        },
        {
          name: '2) Verbesserung/Erleichterung der Arbeit',
          questions: [
            {
              id: 'B2.1',
              text: 'Ich glaube, dass der Change meine Arbeit erleichtern wird.',
              category: 'B) Persönliche Vorteile/Nachteile',
              subcategory: '2) Verbesserung/Erleichterung der Arbeit',
              onlyPMA: false,
              reverse: false,
              order: 6
            },
            {
              id: 'B2.2',
              text: 'Ich glaube, dass mir meine Arbeit nach dem Change mehr gefallen wird.',
              category: 'B) Persönliche Vorteile/Nachteile',
              subcategory: '2) Verbesserung/Erleichterung der Arbeit',
              onlyPMA: false,
              reverse: false,
              order: 7
            }
          ]
        },
        {
          name: '3) Mehr arbeiten müssen',
          questions: [
            {
              id: 'B3.1',
              text: 'Ich glaube, dass ich nach dem Change einen höheren Workload haben werde',
              category: 'B) Persönliche Vorteile/Nachteile',
              subcategory: '3) Mehr arbeiten müssen',
              onlyPMA: false,
              reverse: false,
              order: 8
            }
          ]
        },
        {
          name: '4) Statusverlust',
          questions: [
            {
              id: 'B4.1',
              text: 'Ich befürchte, dass ich bei nach dem Change einen Teil meines Status in der Organisation verlieren werde.',
              category: 'B) Persönliche Vorteile/Nachteile',
              subcategory: '4) Statusverlust',
              onlyPMA: false,
              reverse: true,
              order: 9
            }
          ]
        },
        {
          name: '5) Verlust von Beziehungen',
          questions: [
            {
              id: 'B5.1',
              text: 'Der Change wird viele der persönlichen Beziehungen beeinträchtigen, die ich aufgebaut habe.',
              category: 'B) Persönliche Vorteile/Nachteile',
              subcategory: '5) Verlust von Beziehungen',
              onlyPMA: false,
              reverse: true,
              order: 10
            }
          ]
        },
        {
          name: '6) Verlust von Job',
          questions: [
            {
              id: 'B6.1',
              text: 'Der Change stellt ein Risiko für meine Zukunft in diesem Job dar.',
              category: 'B) Persönliche Vorteile/Nachteile',
              subcategory: '6) Verlust von Job',
              onlyPMA: false,
              reverse: true,
              order: 11
            }
          ]
        }
      ]
    },
    {
      name: 'C) Kompetenzen und Ressourcen',
      subcategories: [
        {
          name: '1) Kompetenzen – Allgemein',
          questions: [
            {
              id: 'C1.1',
              text: 'Ich glaube, dass ich über die nötigen Fähigkeiten verfüge, um den Change mitzugehen',
              category: 'C) Kompetenzen und Ressourcen',
              subcategory: '1) Kompetenzen – Allgemein',
              onlyPMA: false,
              reverse: false,
              order: 12
            },
            {
              id: 'C1.2',
              text: 'Ich verfüge über die nötigen Fähigkeiten, um die mit dem Change verbundenen Anforderungen in meiner Arbeit zu erfüllen.',
              category: 'C) Kompetenzen und Ressourcen',
              subcategory: '1) Kompetenzen – Allgemein',
              onlyPMA: false,
              reverse: false,
              order: 13
            }
          ]
        },
        {
          name: '2) Kompetenzen – Lernbereitschaft',
          questions: [
            {
              id: 'C2.1',
              text: 'Ich glaube, die Fähigkeiten erlernen zu können, die erforderlich sind, um den Change mitgehen zu können',
              category: 'C) Kompetenzen und Ressourcen',
              subcategory: '2) Kompetenzen – Lernbereitschaft',
              onlyPMA: false,
              reverse: false,
              order: 14
            },
            {
              id: 'C2.2',
              text: 'Ich kann die nötigen Fähigkeiten erlernen, die erforderlich sind, um die mit dem Change verbundenen Anforderungen in meiner Arbeit zu erfüllen.',
              category: 'C) Kompetenzen und Ressourcen',
              subcategory: '2) Kompetenzen – Lernbereitschaft',
              onlyPMA: false,
              reverse: false,
              order: 15
            }
          ]
        },
        {
          name: '3) Ressourcen – Zeit',
          questions: [
            {
              id: 'C3.1',
              text: 'Ich glaube, genug Zeit zu haben, um den Change mitgehen zu können',
              category: 'C) Kompetenzen und Ressourcen',
              subcategory: '3) Ressourcen – Zeit',
              onlyPMA: false,
              reverse: false,
              order: 16
            }
          ]
        },
        {
          name: '4) Ressourcen – Equipment',
          questions: [
            {
              id: 'C4.1',
              text: 'Ich glaube, das nötige Equipment zu haben, um den Change mitgehen zu können',
              category: 'C) Kompetenzen und Ressourcen',
              subcategory: '4) Ressourcen – Equipment',
              onlyPMA: false,
              reverse: false,
              order: 17
            }
          ]
        }
      ]
    },
    {
      name: 'D) Einstellung/Haltung',
      subcategories: [
        {
          name: '1) Commitment allgemein',
          questions: [
            {
              id: 'D1.1',
              text: 'Ich stehe dem Change positiv gegenüber und bin bereit, ihn aktiv mitzugehen.',
              category: 'D) Einstellung/Haltung',
              subcategory: '1) Commitment allgemein',
              onlyPMA: false,
              reverse: false,
              order: 18
            }
          ]
        },
        {
          name: '2) Überzeugung, ob sinnvoll',
          questions: [
            {
              id: 'D2.1',
              text: 'Ich halte den Change für sinnvoll',
              category: 'D) Einstellung/Haltung',
              subcategory: '2) Überzeugung, ob sinnvoll',
              onlyPMA: false,
              reverse: false,
              order: 19
            }
          ]
        },
        {
          name: '3) Lernbereitschaft',
          questions: [
            {
              id: 'D3.1',
              text: 'Ich bin bereit dazu, die nötigen Fähigkeiten zu erlernen, die erforderlich sind, um die mit dem Change verbundenen Anforderungen in meiner Arbeit zu erfüllen.',
              category: 'D) Einstellung/Haltung',
              subcategory: '3) Lernbereitschaft',
              onlyPMA: false,
              reverse: false,
              order: 20
            }
          ]
        }
      ]
    },
    {
      name: 'E) Verständnis',
      subcategories: [
        {
          name: '1) Was?',
          questions: [
            {
              id: 'E1.1',
              text: 'Ich verstehe das Ziel des Change',
              category: 'E) Verständnis',
              subcategory: '1) Was?',
              onlyPMA: false,
              reverse: false,
              order: 21
            }
          ]
        },
        {
          name: '2) Warum?',
          questions: [
            {
              id: 'E2.1',
              text: 'Ich verstehe, warum der Change wichtig/notwendig/angemessen/sinnvoll ist.',
              category: 'E) Verständnis',
              subcategory: '2) Warum?',
              onlyPMA: false,
              reverse: false,
              order: 22
            }
          ]
        },
        {
          name: '3) Auswirkungen',
          questions: [
            {
              id: 'E3.1',
              text: 'Ich verstehe, welche konkreten Auswirkungen der Change auf mich und meine Arbeit haben wird',
              category: 'E) Verständnis',
              subcategory: '3) Auswirkungen',
              onlyPMA: false,
              reverse: false,
              order: 23
            }
          ]
        },
        {
          name: '4) Aufgabenverständnis (nur PMA)',
          questions: [
            {
              id: 'E4.1',
              text: 'Ich verstehe, welche Art von Aufgaben für mich bei der Umsetzung des Change auf mich zukommen werden (nur PMA)',
              category: 'E) Verständnis',
              subcategory: '4) Aufgabenverständnis (nur PMA)',
              onlyPMA: true,
              reverse: false,
              order: 24
            }
          ]
        }
      ]
    },
    {
      name: 'F) Vertrauen in Organisation',
      subcategories: [
        {
          name: '',
          questions: [
            {
              id: 'F1.1',
              text: 'Ich glaube, dass unser Unternehmen fähig dazu ist, den Change erfolgreich umzusetzen',
              category: 'F) Vertrauen in Organisation',
              subcategory: '',
              onlyPMA: false,
              reverse: false,
              order: 25
            }
          ]
        }
      ]
    }
  ],
  createdAt: new Date(),
  updatedAt: new Date()
};

