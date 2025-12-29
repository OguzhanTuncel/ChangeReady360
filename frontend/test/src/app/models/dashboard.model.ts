/**
 * Dashboard Models
 * Interfaces für Dashboard-KPIs, Readiness-Daten und Stakeholder-Gruppen
 */

export interface DashboardKpis {
  readinessScore: number; // Gesamt-Readiness in Prozent (0-100)
  readinessTrend: number; // Trend in Prozentpunkten (z.B. +6%)
  stakeholderCount: number; // Gesamtanzahl Stakeholder
  stakeholderGroupsCount: number; // Anzahl Stakeholder-Gruppen
  criticsCount: number; // Anzahl Kritiker
  openMeasuresCount: number; // Anzahl offener Maßnahmen
  overdueMeasuresCount: number; // Anzahl überfälliger Maßnahmen
}

export interface ReadinessData {
  overallReadiness: number; // Gesamt-Readiness (0-100)
  promoters: number; // Anzahl Promoter
  neutrals: number; // Anzahl Neutrale
  critics: number; // Anzahl Kritiker
}

export interface StakeholderGroupSummary {
  id: string;
  name: string;
  icon: string;
  readiness: number; // Readiness in Prozent (0-100)
  trend: number; // Trend in Prozentpunkten (z.B. +5% oder -3%)
  participantCount: number; // Anzahl Teilnehmer in dieser Gruppe
  status: 'ready' | 'attention' | 'critical'; // Status-Badge
}

export interface DashboardData {
  kpis: DashboardKpis;
  readiness: ReadinessData;
  stakeholderGroups: StakeholderGroupSummary[];
}


