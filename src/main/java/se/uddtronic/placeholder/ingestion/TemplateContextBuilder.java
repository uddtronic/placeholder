package se.uddtronic.placeholder.ingestion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import se.uddtronic.placeholder.mocks.MockData;
import tools.jackson.databind.ObjectMapper;

@Component
public class TemplateContextBuilder {

    private final ObjectMapper objectMapper;

    public TemplateContextBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildContext(HttpServletRequest request, MockData mock, String data, String originalPath) {
        Map<String, Object> context = new HashMap<>();

        Map<String, Object> paramsContext = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                paramsContext.put(k, v[0]);
            }
        });
        context.put("request", paramsContext);

        Map<String, String> requestHeaders = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(String::toLowerCase, request::getHeader));
        context.put("headers", requestHeaders);

        context.put("now", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (mock.getVariables() != null) {
            context.put("variables", mock.getVariables());
        }

        if (data != null && request.getContentType() != null && request.getContentType().contains("json")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonBody = objectMapper.readValue(data, Map.class);
                context.put("body", jsonBody);
            } catch (Exception _) {
                // Ignore
            }
        }

        if (originalPath != null) {
            Matcher matcher = Pattern.compile(mock.getPath()).matcher(originalPath);
            if (matcher.matches()) {
                List<String> groups = new ArrayList<>();
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    groups.add(matcher.group(i));
                }
                context.put("groups", groups);
            }
        }

        return context;
    }
}
