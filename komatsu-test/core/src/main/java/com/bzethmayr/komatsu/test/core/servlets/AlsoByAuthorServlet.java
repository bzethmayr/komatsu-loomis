package com.bzethmayr.komatsu.test.core.servlets;

import com.bzethmayr.komatsu.test.core.models.AlsoByAuthorModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Optional;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "komatsutest/components/page",
        selectors = "aba",
        extensions = {"json", "xml"}
)
public class AlsoByAuthorServlet extends SlingSafeMethodsServlet {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    protected void doGet(
            final SlingHttpServletRequest request,
            final SlingHttpServletResponse response
    ) throws IOException {
        final AlsoByAuthorModel model = Optional.of(request.getResource())
                .map(r -> r.adaptTo(AlsoByAuthorModel.class))
                .orElse(null);

        final String extension = request.getRequestPathInfo().getExtension();
        if (model == null || model.getAuthor() == null) {
            response.setStatus(404);
        } else if ("xml".equalsIgnoreCase(extension)) {
            response.setContentType("application/xml");
            xmlMapper.writeValue(response.getWriter(), model);
        } else if ("json".equalsIgnoreCase(extension)) {
            response.setContentType("application/json");
            jsonMapper.writeValue(response.getWriter(), model);
        } else {
            response.setStatus(400);
        }
    }

}
