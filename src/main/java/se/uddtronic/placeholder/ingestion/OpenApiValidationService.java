package se.uddtronic.placeholder.ingestion;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.model.Request.Method;

import jakarta.servlet.http.HttpServletRequest;
import se.uddtronic.placeholder.mocks.MockData;
import se.uddtronic.placeholder.mocks.ValidationConfig;

@Service
public class OpenApiValidationService {

    private final Map<String, OpenApiInteractionValidator> validatorCache = new ConcurrentHashMap<>();

    public List<String> validateRequest(MockData mock, HttpServletRequest request, String body) {
        ValidationConfig validation = mock.getValidation();
        if (validation == null || validation.getOpenApiSpec() == null || validation.getOpenApiSpec().isEmpty()) {
            return Collections.emptyList();
        }

        String specPath = validation.getOpenApiSpec();
        OpenApiInteractionValidator validator = validatorCache.computeIfAbsent(specPath,
            path -> OpenApiInteractionValidator.createFor(path).build());

        String path = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        if (path == null) {
            path = request.getRequestURI();
        }
        if (path == null) {
            path = "/";
        }

        String methodStr = (String) request.getAttribute("jakarta.servlet.error.method");
        if (methodStr == null) {
            methodStr = request.getMethod();
        }
        if (methodStr == null) {
            methodStr = "GET";
        }

        Method method;
        try {
            method = Method.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException _) {
            method = Method.GET;
        }

        SimpleRequest.Builder requestBuilder = new SimpleRequest.Builder(method, path);

        Collections.list(request.getHeaderNames()).forEach(headerName ->
            requestBuilder.withHeader(headerName, Collections.list(request.getHeaders(headerName)))
        );

        request.getParameterMap().forEach((paramName, paramValues) -> {
            for (String val : paramValues) {
                requestBuilder.withQueryParam(paramName, val);
            }
        });

        if (body != null && !body.isEmpty()) {
            requestBuilder.withBody(body);
        }

        ValidationReport report = validator.validateRequest(requestBuilder.build());

        if (report.hasErrors()) {
            return report.getMessages().stream()
                    .map(ValidationReport.Message::getMessage)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
