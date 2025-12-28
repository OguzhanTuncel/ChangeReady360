import { Component, Input, OnChanges, SimpleChanges, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrendDataPoint } from '../../models/reporting.model';

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './line-chart.component.html',
  styleUrl: './line-chart.component.css'
})
export class LineChartComponent implements OnChanges, OnInit {
  @Input() dataPoints: TrendDataPoint[] = [];
  @Input() width: number = 800;
  @Input() height: number = 400;
  @Input() showTarget: boolean = true;

  viewBox: string = '';
  padding: number = 60;
  chartWidth: number = 0;
  chartHeight: number = 0;
  xScale: number = 0;
  yScale: number = 0;
  actualPath: string = '';
  targetPath: string = '';
  areaPath: string = '';
  insight: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dataPoints'] || changes['width'] || changes['height']) {
      this.calculateChart();
    }
  }

  ngOnInit(): void {
    this.calculateChart();
  }

  private calculateChart(): void {
    if (this.dataPoints.length === 0) {
      this.viewBox = `0 0 ${this.width} ${this.height}`;
      return;
    }

    this.viewBox = `0 0 ${this.width} ${this.height}`;
    this.chartWidth = this.width - (this.padding * 2);
    this.chartHeight = this.height - (this.padding * 2);

    // Calculate scales
    const dates = this.dataPoints.map(d => d.date.getTime());
    const minDate = Math.min(...dates);
    const maxDate = Math.max(...dates);
    const dateRange = maxDate - minDate || 1;
    this.xScale = this.chartWidth / dateRange;

    const values = this.dataPoints.flatMap(d => [d.actualValue, d.targetValue || 0]);
    const minValue = Math.min(...values);
    const maxValue = Math.max(...values);
    const valueRange = maxValue - minValue || 100;
    this.yScale = this.chartHeight / valueRange;

    // Generate paths
    this.generatePaths();
    this.calculateInsight();
  }

  private generatePaths(): void {
    const actualPoints: string[] = [];
    const targetPoints: string[] = [];
    const areaPoints: string[] = [];

    this.dataPoints.forEach((point, index) => {
      const x = this.padding + ((point.date.getTime() - Math.min(...this.dataPoints.map(d => d.date.getTime()))) * this.xScale);
      const actualY = this.padding + this.chartHeight - ((point.actualValue - Math.min(...this.dataPoints.map(d => d.actualValue))) * this.yScale);
      const targetY = point.targetValue 
        ? this.padding + this.chartHeight - ((point.targetValue - Math.min(...this.dataPoints.map(d => d.actualValue))) * this.yScale)
        : null;

      if (index === 0) {
        actualPoints.push(`M ${x} ${actualY}`);
        areaPoints.push(`M ${x} ${actualY}`);
        if (targetY !== null) {
          targetPoints.push(`M ${x} ${targetY}`);
        }
      } else {
        actualPoints.push(`L ${x} ${actualY}`);
        areaPoints.push(`L ${x} ${actualY}`);
        if (targetY !== null) {
          targetPoints.push(`L ${x} ${targetY}`);
        }
      }
    });

    // Close area path
    const lastPoint = this.dataPoints[this.dataPoints.length - 1];
    const lastX = this.padding + ((lastPoint.date.getTime() - Math.min(...this.dataPoints.map(d => d.date.getTime()))) * this.xScale);
    areaPoints.push(`L ${lastX} ${this.padding + this.chartHeight}`);
    areaPoints.push(`L ${this.padding} ${this.padding + this.chartHeight}`);
    areaPoints.push('Z');

    this.actualPath = actualPoints.join(' ');
    this.targetPath = targetPoints.join(' ');
    this.areaPath = areaPoints.join(' ');
  }

  private calculateInsight(): void {
    if (this.dataPoints.length < 2) {
      this.insight = '';
      return;
    }

    const first = this.dataPoints[0];
    const last = this.dataPoints[this.dataPoints.length - 1];
    const trend = last.actualValue - first.actualValue;

    if (trend > 0 && last.targetValue && last.actualValue >= last.targetValue) {
      this.insight = 'Positive Entwicklung - Zielwerte werden übertroffen';
    } else if (trend > 0) {
      this.insight = 'Positive Entwicklung';
    } else if (trend < 0) {
      this.insight = 'Aufmerksamkeit erforderlich';
    } else {
      this.insight = 'Stabile Entwicklung';
    }
  }

  getYAxisLabels(): number[] {
    return [0, 25, 50, 75, 100];
  }

  getYPosition(value: number): number {
    const values = this.dataPoints.map(d => d.actualValue);
    const minValue = Math.min(...values);
    const maxValue = Math.max(...values);
    const valueRange = maxValue - minValue || 100;
    const yScale = this.chartHeight / valueRange;
    return this.padding + this.chartHeight - ((value - minValue) * yScale);
  }
}

