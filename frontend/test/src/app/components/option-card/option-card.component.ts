import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-option-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './option-card.component.html',
  styleUrl: './option-card.component.css'
})
export class OptionCardComponent {
  @Input() id: string = '';
  @Input() title: string = '';
  @Input() description: string = '';
  @Input() icon: string = '';
  @Input() selected: boolean = false;
  @Output() selectedChange = new EventEmitter<string>();

  onSelect() {
    this.selectedChange.emit(this.id);
  }
}

