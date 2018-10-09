import { Component } from '@angular/core';
import { Document } from './document';
import { NuxeoService } from '../nuxeo.service';

@Component({
  selector: 'search',
  // Our list of styles in our component. We may add more to compose many styles together
  styleUrls: [ './search.style.css' ],
  // Every Angular template is first compiled by the browser before Angular runs it's compiler
  templateUrl: './search.template.html',
  directives: [Document]
})
export class Search {
  searchValue = '';
  documents = undefined;

  loading = false;
  error = undefined;

  // TypeScript public modifiers
  constructor(public nuxeo: NuxeoService) {

  }

  searchDocuments(value: String) {
    console.log('Search: ' + value);

    this.loading = true;
    this.error = undefined;
    this.documents = undefined;

    this.nuxeo.repository().query({
      query: `Select * from Document where ecm:fulltext LIKE '${value}' or dc:title LIKE '%${value}%' and ecm:isProxy = 0 and ecm:currentLifeCycleState <> 'deleted'`
    }, {
      enrichers: {'document': ['thumbnail']}
    }).then((docs) => {
      this.documents = docs.entries;
      console.log(docs.entries[0]);
      this.loading = false;
    }).catch((error) => {
      console.log(error);
      this.error = `${error}. Ensure Nuxeo is running on port 8080.`;
      this.loading = false;
    });
  }

}
