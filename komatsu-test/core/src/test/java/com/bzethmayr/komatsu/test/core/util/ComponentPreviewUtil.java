package com.bzethmayr.komatsu.test.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentPreviewUtil {

    private static String inTag(final String tagName, final String... parts) {
        final int firstSpace = tagName.indexOf(" ");
        return Stream.of(
                List.of("<", tagName, ">"),
                List.of(parts),
                List.of("</", firstSpace < 0 ? tagName : tagName.substring(0, firstSpace), ">")
            )
            .flatMap(List::stream)
            .collect(Collectors.joining());
    }

    private static String inStyledHtmlBody(final String title, final String headInsert, final String... bodyParts) {
        return inTag("html",
                inTag("head", inTag("title", title), headInsert),
                inTag("body", bodyParts)
            );
    }

    /**
     * This wants a CLI entry point, but not first...
     * @param args I miss getopts
     */
    public static void main(final String... args) {
        System.out.println(combineInHtml(args[0]));
    }

    /**
     * We want to jam the HTML and the JS together.
     * We have no intention to invoke the CSS compiler.
     * The template can be "the worst HTML page".
     * @return an HTML page as a string
     */
    public static String combineInHtml(
            final String title,
            final String cssBody,
            final String jsBody,
            final String templateBody
    ) {
        return inStyledHtmlBody(title,
                inTag("style type=\"txt/css\"", "\n", cssBody, "\n"),
                "\n", templateBody, "\n",
                inTag("script type=\"text/javascript\"", "\n", jsBody, "\n")
            );
    }

    public static String combineInHtml(
            final String title,
            final Path cssPath,
            final Path jsPath,
            final Path templatePath
    ) {
        try {
            return combineInHtml(
                    title,
                    Files.readString(cssPath),
                    Files.readString(jsPath),
                    Files.readString(templatePath)
            );
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static String combineInHtml(
            final String componentName
    ) {
        String localPath = new File(".")
                .getAbsolutePath();
        Path walker = Path.of(localPath)
                .normalize()
                .toAbsolutePath();
        while (!walker.endsWith("komatsu-test") && walker.getNameCount() > 1) {
            walker = walker.getParent();
        }
        if (!walker.endsWith("komatsu-test")) {
            throw new IllegalStateException("lost");
        }
        return combineInHtml(
                componentName + " Preview",
                walker.resolve(Path.of("ui.frontend", "src", "main", "webpack",
                        "components", "_" + componentName + ".scss")),
                walker.resolve(Path.of("ui.frontend", "src", "main", "webpack",
                        "components", "_" + componentName + ".js")),
                walker.resolve(Path.of("ui.apps", "src", "main", "content", "jcr_root",
                        "apps", "komatsutest", "components", componentName, componentName + ".html"))
        );
    }
}
