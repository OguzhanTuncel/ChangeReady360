/**
 * Stakeholder Models
 * Interfaces für Stakeholder-Gruppen, Details und Trends
 */

export interface StakeholderGroup {
  id: string;
  name: string;
  icon: string;
  readiness: number; // Readiness in Prozent (0-100)
  trend: number; // Trend in Prozentpunkten (z.B. +5% oder -3%)
  participantCount: number; // Anzahl Teilnehmer in dieser Gruppe
  promoters: number; // Anzahl Promoter
  neutrals: number; // Anzahl Neutrale
  critics: number; // Anzahl Kritiker
  status: 'ready' | 'attention' | 'critical'; // Status-Badge
  impact: 'Hoch' | 'Niedrig' | 'Kritisch' | 'Strategisch'; // Betroffenheit
}

export interface StakeholderGroupDetail extends StakeholderGroup {
  description?: string;
  lastUpdated?: Date;
  historicalReadiness?: ReadinessHistoryPoint[]; // Für Trend-Berechnung
}

export interface ReadinessHistoryPoint {
  date: Date;
  readiness: number;
}

export interface StakeholderPerson {
  id: string;
  name: string;
  email?: string;
  department: string;
  role?: string;
  readiness?: number;
  category: 'promoter' | 'neutral' | 'critic';
}

export interface StakeholderKpis {
  total: number; // Gesamtanzahl Stakeholder
  promoters: number; // Anzahl Promoter
  neutrals: number; // Anzahl Neutrale
  critics: number; // Anzahl Kritiker
}


