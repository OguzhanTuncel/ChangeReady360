import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-donut-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './donut-chart.component.html',
  styleUrl: './donut-chart.component.css'
})
export class DonutChartComponent implements OnChanges {
  @Input() value: number = 0; // Readiness value (0-100)
  @Input() size: number = 200; // Size of the chart in pixels
  @Input() strokeWidth: number = 20; // Width of the donut stroke

  radius: number = 0;
  circumference: number = 0;
  offset: number = 0;
  color: string = '#31749B'; // Default blue

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['value'] || changes['size'] || changes['strokeWidth']) {
      this.calculateChart();
    }
  }

  ngOnInit(): void {
    this.calculateChart();
  }

  private calculateChart(): void {
    // Calculate radius (accounting for stroke width)
    this.radius = (this.size - this.strokeWidth) / 2;
    this.circumference = 2 * Math.PI * this.radius;
    
    // Calculate offset (how much of the circle to show)
    // value is 0-100, so we need to calculate the dash offset
    const percentage = Math.max(0, Math.min(100, this.value));
    const progress = percentage / 100;
    this.offset = this.circumference * (1 - progress);

    // Determine color based on value
    if (percentage >= 75) {
      this.color = '#56A080'; // Green
    } else if (percentage >= 50) {
      this.color = '#DFB55E'; // Orange/Gold
    } else {
      this.color = '#DC2626'; // Red
    }
  }

  getViewBox(): string {
    return `0 0 ${this.size} ${this.size}`;
  }

  getCenter(): number {
    return this.size / 2;
  }
}

