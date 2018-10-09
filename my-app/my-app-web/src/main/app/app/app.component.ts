/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation } from '@angular/core';
import { NuxeoService } from './nuxeo.service';

/*
 * App Component
 * Top Level Component
 */
@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: [
    './app.style.css'
  ],
  templateUrl: './app.template.html'
})
export class App {
  url = 'https://twitter.com/AngularClass';

  constructor() { }

  ngOnInit() {
    console.log('Initial App Statement.');
  }
}
