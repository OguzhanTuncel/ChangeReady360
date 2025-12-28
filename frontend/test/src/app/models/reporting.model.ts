/**
 * Reporting Models
 * Interfaces für Management Summary, Trend-Daten und Reporting-Funktionen
 */

export interface ManagementSummary {
  overallReadiness: number; // Gesamt-Readiness in Prozent (0-100)
  readinessTrend: number; // Trend in Prozentpunkten (z.B. +6% oder -3%)
  stakeholderCount: number; // Anzahl Stakeholder
  activeMeasuresCount: number; // Anzahl aktiver Maßnahmen
  date: Date; // Stand-Datum
}

export interface DepartmentReadiness {
  id: string;
  name: string;
  readiness: number; // Readiness in Prozent (0-100)
  color: string; // Farbcodierung für Visualisierung
}

export interface TrendDataPoint {
  date: Date;
  actualValue: number; // Ist-Wert (Readiness in Prozent)
  targetValue?: number; // Zielwert (optional, für Prognose)
}

export interface TrendChartData {
  dataPoints: TrendDataPoint[];
  insight?: string; // Trend-Insight (z.B. "Positive Entwicklung - Zielwerte werden übertroffen")
}

export interface ReportingData {
  summary: ManagementSummary;
  departments: DepartmentReadiness[];
  trend: TrendChartData;
}

