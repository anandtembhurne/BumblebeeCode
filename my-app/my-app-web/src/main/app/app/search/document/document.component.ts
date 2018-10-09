import { Input, Component } from '@angular/core';

@Component({
  selector: 'document',
  // Our list of styles in our component. We may add more to compose many styles together
  styleUrls: [ './document.style.css' ],
  // Every Angular template is first compiled by the browser before Angular runs it's compiler
  templateUrl: './document.template.html'
})
export class Document {
  @Input() document: Object = {
    title: 'dummy',
    uuid: 'xxx-xxx-xxx'
  };

  constructor() {

  }
}
