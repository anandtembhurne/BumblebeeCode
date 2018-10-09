import {
  beforeEachProviders,
  inject,
  it
} from '@angular/core/testing';

// Load the implementations that should be tested
import { App } from './app.component';

describe('App', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEachProviders(() => [
    App
  ]);

  it('should have a url', inject([ App ], (app) => {
    expect(app.url).toEqual('https://twitter.com/AngularClass');
  }));
});
