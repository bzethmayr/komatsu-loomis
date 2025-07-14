# komatsu-loomis
3 exercises for fullstack AEM role

## Task 0, added
1. obtain 6.5 and
2. set up maven to deploy to it
   + will require multiple pom
   + all-in plugin to deploy
   - Jenkins is out-of-scope.
3. First two are in OSGi bundle
   - using 0.0-SNAPSHOT
   - will want v Sling libs
   - will want Mockito, hopefully not need PowerMockito...
   - okay vs Maven Central I bet
4. Last is in JCR package
5. An integration test is indicated
6. A front-end build is indicated

## Exercise 1
 
1.  Create a service that will run once every 2 minutes and only on the author environment.
2.  This service will find all pages that have been published and will set a property named `processedDate` to the current time.
   + "have been" is loosely bound - binding newer end to current time, and older end to where?
   + older end could be the last run?
   + however, the more correct condition would be any pages whose publish date exceeds their processedDate, since not all runs will necessarily occur / complete.
4.  Provide unit tests with at least 80% test coverage.
    + significant mocking is indicated
    * Check the generated JaCoCo report to view the test coverage.
    * This report is viewable at `core/target/site/jacoco/index.html`
 
 
## Exercise 2
 
1.  Create a servlet that will output the first and last name of the author who last modified the targeted page.
    + okay, we may need to surface the author more conveniently?
3.  It will also contain a list of any child pages that were also modified by this user.
     + children of this page, this could be done by walk or by search. BETTER TO SEARCH, walk not being able to take advantage of indices
     + also modified ever, or also last modified by? stated as "also modified ever". would probably rather be "last modified by"
         - at slight risk, going to treat as "last modified by"
5.  Based on the extension, the servlet should return the output in either XML or JSON format.
    + just need to stay out of its way on this
6.  Provide unit tests with at least 80% test coverage.
     + A unit test can indicate whether we're good against our mocks
     + we'd probably want an integration test against our deployed env actually
     + We can kick those after the deploy in the Integration Test phase, oldschool
    * Check the generated JaCoCo report to view the test coverage.
    * This report is viewable at `core/target/site/jacoco/index.html`
 
 
## Exercise 3
 
1.  Create a component that contains a form with a text input and a submit button.
    * "contains a form"
3.  The label for the input and the text of the submit button must be authorable.
4.  On desktop, the component should have a light grey background and black text.
     + this is a CSS query thing?
5.  On mobile, the component should have a black background with white text.
     + this is a CSS query thing?
        - Is it really? we're not given a width cutoff.
        - wider than tall vs taller than wide could confuse a vertical monitor.
        - Can we... trust the client metadata like a simp?
            + well, we can try
7.  When the submit button is click, the component should display the title, description, image, and last modified date for each page whose title or description contain the text from the input field.
     - sounds like a dynamic action because components don't get to call for page refreshes.
     - but this one contains a form. In a legacy case that could indicate that the form actually does get submitted.
9.  If no pages are returned, it should instead display text alerting the user that their term returned zero results.
    + "(alert sign) Your term returned zero results" I guess. Editability not indicated.
11.  Provide unit tests with at least 80% test coverage.
    + This one would generate some ancillary unit tests but have mostly integration tests in fact, as there is frontend behavior of specification
    * Check the generated JaCoCo report to view the test coverage.
    * This report is viewable at `core/target/site/jacoco/index.html`
