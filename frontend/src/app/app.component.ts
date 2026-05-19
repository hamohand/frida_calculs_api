import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FamilyFormComponent } from './components/family-form/family-form.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FamilyFormComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'frida-calculs-front';
}
