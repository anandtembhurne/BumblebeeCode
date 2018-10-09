describe('App', () => {

  beforeEach(() => {
    browser.get('/');
  });


  it('should have a title', () => {
    let subject = browser.getTitle();
    let result  = 'Angular2 Sample';
    expect(subject).toEqual(result);
  });

  it('should have header', () => {
    let subject = element(by.css('h1')).isPresent();
    expect(subject).toEqual(true);
  });

  it('should have <search>', () => {
    let subject = element(by.css('app search')).isPresent();
    expect(subject).toEqual(true);
  });

  it('should have search button', () => {
    let subject = element(by.css('button')).getText();
    let result  = 'Submit Value';
    expect(subject).toEqual(result);
  });

});
