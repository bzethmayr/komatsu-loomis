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
        form: '[data-cmp-hook-exercisethree="form"]',
        input:  '[data-cmp-hook-exercisethree="input"]',
        submit: '[data-cmp-hook-exercisethree="submit"]',
        output: '[data-cmp-hook-exercisethree="output"]'
    };

    function exercisethree(config) {

        function findOnlyChild(root, selector) {
            var children = config.element.querySelectorAll(selector);
            return children.length == 1 ? children[0] : null;
        }

        function appendDdt(dl, term, definition) {
            var dt = document.createElement("dt");
            dt.append(term);
            var dd = document.createElement("dd");
            dd.append(definition);
            dl.append(dt);
            dl.append(dd);
        }

        function init(config) {
            // Best practice:
            // To prevents multiple initialization, remove the main data attribute that
            // identified the component.
            config.element.removeAttribute("data-cmp-is");

            var form = findOnlyChild(config.element, selectors.form);
            var input = findOnlyChild(config.element, selectors.input);
            var submit = findOnlyChild(config.element, selectors.submit);
            var output = findOnlyChild(config.element, selectors.output);

            if (console && console.log) {
                console.log(
                    "exercisethree component JavaScript debugging",
                    "\nForm is\n", form,
                    "\nInput is\n", input,
                    "\nSubmit:\n", submit,
                    "\nOutput:\n", output
                );
            }
                form.addEventListener("submit", (event) => {
                    event.preventDefault();
                    fetch("/exercisethree.json?" + new URLSearchParams("searchTerm", input.value))
                        .then(response => response.json())
                        .then(json => {
                            console.log(JSON.stringify(json));
                            if (json.length) {
                                output.innerHTML = "";
                                var listing = document.createElement("ul");
                                json.forEach(found => {
                                    var item = document.createElement("li");
                                    var itemDl = document.createElement("dl");
                                    appendDdt(itemDl, "title", found.title);
                                    appendDdt(itemDl, "description", found.description);
                                    appendDdt(itemDl, "last modified", found.lastModified);
                                    item.append(itemDl);
                                    if (found.image && found.image.src) {
                                        var image = document.createElement("img");
                                        image.src = found.image.src;
                                        if (found.image.srcset) image.srcset = found.image.srcset;
                                        if (found.lazyLoading) image.loading = lazy;
                                        if (found.image.width) image.width = found.image.width;
                                        if (found.image.height) image.height = found.image.height;
                                        if (found.image.sizes) image.sizes = found.image.sizes;
                                        image.alt = found.image.alt;
                                        if (found.image.title) image.title = found.image.title;
                                        item.append(image);
                                    }
                                    listing.append(item);
                                });
                                output.append(listing);
                            } else {
                                output.innerHTML = "⚠ Your term returned zero results";
                            }
                        });
                });
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
