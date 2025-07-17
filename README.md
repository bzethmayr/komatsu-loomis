# komatsu-loomis
3 exercises for fullstack AEM role

## Exercise 1
 
1.  Create a service that will run once every 2 minutes and only on the author environment.
2.  This service will find all pages that have been published and will set a property named `processedDate` to the current time.
   + "have been" is loosely bound - binding newer end to current time, and older end to where?
   + older end could be the last run?
   + however, the more correct condition would be any pages whose publish date exceeds their processedDate, since not all runs will necessarily occur / complete.
3.  Provide unit tests with at least 80% test coverage.
    + significant mocking is indicated
    * Check the generated JaCoCo report to view the test coverage.
    * This report is viewable at `core/target/site/jacoco/index.html`

```
This is implemented mostly in komatsu-test/core/src/main/.../core/schedulers/PublishedPageProcessingTask.java
`com.bzethmayr.komatsu.test.core.schedulers.PublishedPageProcessingTask`
but also has touches in komatsu-test/ui.config/src/main/.../osgiconfig/config.author
We could restrict by config presence, but we should not in any case be modifying most content on the publisher,
so explicit scheduling (or not) is done instead.
`git diff 4be71508d18852b9322232ddfefd2bee41cfedaa 95aa2abcf18ec85237fa6dbd1281cf9dcb6d2202`
recovers the changes vs prior state (bare archetype 54)
```
 
## Exercise 2
 
1.  Create a servlet that will output the first and last name of the author who last modified the targeted page.
    + okay, we may need to surface the author more conveniently?
    + targets pages
    + "first and last name" is satisfied by "whole name" which is one pole of a best-practice debate
    + will let AEM's representation cast the deciding vote.
    + "a servlet" so, this model is not indicated to be adapted from, just constructed
1.  It will also contain a list of any child pages that were also modified by this user.
     + children of this page, this could be done by walk or by search.
     + BETTER TO SEARCH, walk not being able to take advantage of indices
     + also modified ever, or also last modified by? stated as "also modified (ever)". would probably rather be "last modified by"
         - at slight risk, going to treat as "last modified by"
     + for these to be useful, need to include sufficient to allow link construction, minimum title and path
        + the notion of GraphQL occurs for this to avoid the question
1.  Based on the extension, the servlet should return the output in either XML or JSON format.
    + just need to stay out of its way on this - no, we have to do something
1.  Provide unit tests with at least 80% test coverage.
     + A unit test can indicate whether we're good against our mocks
     + we'd probably want an integration test against our deployed env actually
     + We can kick those after the deploy in the Integration Test phase, oldschool
     * Check the generated JaCoCo report to view the test coverage.
     * This report is viewable at `core/target/site/jacoco/index.html`

```
This is going to target cq:Page jcr:content cq:lastModifiedBy since that is where author edits qua author edits persist,
the jcr:lastModifiedBy property could well reflect a non-author action.
We can initially use the Page object model?
a Sling Model isn't indicated... it would simplify the servlet right out of existence.
SO... just a pojo? well.. that leaves even more risk surface given no runtime environment. I can verify model resolution.
So I guess it gets to be a model. I can put POJOs in it if I want?

Well blech. The default get servlet would serialize per extension. But this isn't that. I guess we pull in a mapper?
Would be better to just expose the model via selector I think, but that way doesn't include a servlet
branch was `exercise-2`

```
 
 
## Exercise 3
 
1.  Create a component that contains a form with a text input and a submit button.
    * "contains a form"
1.  The label for the input and the text of the submit button must be authorable.
    + `inputLabel`, `submitText`
1.  On desktop, the component should have a light grey background and black text.
    + this is a CSS query thing?
1.  On mobile, the component should have a black background with white text.
     + this is a CSS query thing?
        - Is it really? we're not given a width cutoff.
        - wider than tall vs taller than wide could confuse a vertical monitor.
        - Can we... trust the client metadata like a simp?
            + well, we can try
1.  When the submit button is click, the component should 
    display the title, description, image, and last modified date 
    for each page whose title or description contain the text from the input field.
     - sounds like a dynamic action because components don't get to call for page refreshes.
     - but this one contains a form. In a legacy case that could indicate that the form actually does get submitted.
1.  If no pages are returned, it should instead display text alerting the user that their term returned zero results.
    + "(alert sign) Your term returned zero results" I guess. Editability not indicated.
1.  Provide unit tests with at least 80% test coverage.
    + This one would generate some ancillary unit tests but have mostly integration tests in fact, as there is frontend behavior of specification
    * Check the generated JaCoCo report to view the test coverage.
    * This report is viewable at `core/target/site/jacoco/index.html`

```
the search may be a servlet? I think it will be.
the component is seriously looking at getting skipped. along with picking flavor-of-ajax.
we have a search implied and we have probably a pojo model implied
search can signal no results by empty collection, preferably to 404.
the publish user is specifying the search term, this should not be a selector
starting from the end
Mobile distinction is coming in last
Image? this wanted to display the image for the page. Do all pages have those? 
cutting it at component stub (no ajax) and servlet implementation
branch was exercise-3
...
Speculative scope on further branches
it would be easier in shell, but, if we combine the component template and the JS kinda sloppy-like...
alright. we could now sort-of approach the JS portion
approach wouldn't integrate well with the build, I don't think, at least not yet
going to stick within the subset and use the css too
got the media query sorted
got the fetch interaction sorted
`git diff 05bac101d32bdb3534af6ba3266cac47cb667650` should recover all of this
```

----

+ Maven build indicated
+ we're branching off main
+ AND BLOCKED
  + Getting hold of that JAR is only for the elect

----

Not blocked, writing to FileVault structure, but, can't verify anything.
OKAY - going blind.
at https://github.com/adobe/aem-project-archetype/tree/develop we see 
- that the master branch was abandoned three years ago
+ that the release/nn series is still current
+ release/53 is indicated
    + implying AEM 6.5.17.0+	Java 11	Maven 3.3.9+

```shell
mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.3.1:generate \
-D aemVersion=6.5.17 \
-D archetypeGroupId=com.adobe.aem \
-D archetypeArtifactId=aem-project-archetype \
-D archetypeVersion=54 \
-D appTitle="Komatsu Test" \
-D appId="komatsutest" \
-D groupId="com.bzethmayr.komatsu" \
-D artifactId="komatsu-test" \
-D package="com.bzethmayr.komatsu.test" \
-D version="0.0.1-SNAPSHOT" \
-D includeDispatcherConfig=n
```
```text
mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.3.1:generate -D aemVersion=6.5.17 -D archetypeGroupId=com.adobe.aem -D archetypeArtifactId=aem-project-archetype -D archetypeVersion=54 -D appTitle="Komatsu Test" -D appId="komatsutest" -D groupId="com.bzethmayr.komatsu" -D artifactId="komatsu-test" -D package="com.bzethmayr.komatsu.test" -D version="0.0.1-SNAPSHOT" -D includeDispatcherConfig=n
```

## Round 1
Errors relating to file permissions in Dispatcher config. No dispatcher requirement in exercises, so removing dispatcher config

## Round 2
It's gone and installed the cloud forms vs local SDK.

## Round 3
Even if we could get lift from the forms components we'd need to be able to RUN THEM to do so, so... no, removing those

## Round 4
NPM really cranked the fans for a minute there...

