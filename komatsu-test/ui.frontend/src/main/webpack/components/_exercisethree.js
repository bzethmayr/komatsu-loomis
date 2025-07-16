// Example of how a component should be initialized via JavaScript
// This script logs the value of the component's text property model message to the console

(function() {
    "use strict";

    // Best practice:
    // For a good separation of concerns, don't rely on the DOM structure or CSS selectors,
    // but use dedicated data attributes to identify all elements that the script needs to
    // interact with.
    var selectors = {
        self:      '[data-cmp-is="exercisethree"]',
        input:  '[data-cmp-hook-exercisethree="input"]',
        submit: '[data-cmp-hook-exercisethree="submit"]',
        output: '[data-cmp-hook-exercisethree="output"]'
    };

    function exercisethree(config) {

        function init(config) {
            // Best practice:
            // To prevents multiple initialization, remove the main data attribute that
            // identified the component.
            config.element.removeAttribute("data-cmp-is");

            var input = config.element.querySelectorAll(selectors.input);
            input = input.length == 1 ? input[0] : null;

            var submit = config.element.querySelectorAll(selectors.submit);
            submit = submit.length == 1 ? submit[0] : null;

            var output = config.element.querySelectorAll(selectors.output);
            output = output.length == 1 ? output[0] : null;

            if (console && console.log) {
                console.log(
                    "exercisethree component JavaScript debugging",
                    "\nInput:\n", input,
                    "\nSubmit:\n", submit,
                    "\nOutput:\n", output
                );
            }
        }

        if (config && config.element) {
            init(config);
        }
    }

    // Best practice:
    // Use a method like this mutation observer to also properly initialize the component
    // when an author drops it onto the page or modified it with the dialog.
    function onDocumentReady() {
        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new exercisethree({ element: elements[i] });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body             = document.querySelector("body");
        var observer         = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function(addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function(element) {
                                new exercisethree({ element: element });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }

}());
