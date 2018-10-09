import { Injectable } from '@angular/core';
let nuxeo = require('nuxeo/dist/nuxeo');

@Injectable()
export class NuxeoService {
  // XXX Declared here to prevent from mixin style compilation error when using the service.
  login: any;
  operation: any;
  request: any;
  repository: any;
  batchUpload: any;
  users: any;
  groups: any;
  directory: any;
  workflows: any;
  requestAuthenticationToken: any;
  // ---

  private instance: any;

  constructor() {
    this.instance = new nuxeo({
      baseURL:  `${location.origin}/nuxeo/`,
      auth: {
        username: 'Administrator',
        password: 'Administrator',
        method: 'basic'
      }
    });

    // Mixin Nuxeo JS Client prototype with NuxeoService to use it the same way.
    Object.getOwnPropertyNames(nuxeo.prototype).forEach(name => {
      if (/^_|constructor/.test(name)) {
        return;
      }

      NuxeoService.prototype[name] = function (...args: any[]) {
        return this.instance[name].apply(this.instance, args);
      };
    });
  }
}
