package se.uddtronic.placeholder.ingestion;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.samskivert.mustache.Mustache;
import jakarta.servlet.http.HttpServletRequest;
import se.uddtronic.placeholder.ingestion.dto.ErrorResponse;
import se.uddtronic.placeholder.mocks.MockData;
import se.uddtronic.placeholder.mocks.MocksService;
import se.uddtronic.placeholder.mocks.ValidationAction;
import tools.jackson.databind.ObjectMapper;

@Controller
public class DataIngestionController implements ErrorController {

    private final DataStorageService dataStorageService;
    private final RequestCounterService requestCounterService;
    private final ObjectMapper objectMapper;
    private final MocksService mocksService;
    private final TemplateContextBuilder templateContextBuilder;
    private final OpenApiValidationService openApiValidationService;

    public DataIngestionController(DataStorageService dataStorageService,
            RequestCounterService requestCounterService, ObjectMapper objectMapper,
            MocksService mocksService, TemplateContextBuilder templateContextBuilder,
            OpenApiValidationService openApiValidationService) {
        this.dataStorageService = dataStorageService;
        this.requestCounterService = requestCounterService;
        this.objectMapper = objectMapper;
        this.mocksService = mocksService;
        this.templateContextBuilder = templateContextBuilder;
        this.openApiValidationService = openApiValidationService;
    }

    @RequestMapping("/error")
    public ResponseEntity<Object> handleError(@RequestBody(required = false) String data,
            HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        if (status != null && Integer.parseInt(status.toString()) == 404) {
            String originalPath = (String) request.getAttribute("jakarta.servlet.error.request_uri");
            String originalMethod = (String) request.getAttribute("jakarta.servlet.error.method");
            String queryString = (String) request.getAttribute("jakarta.servlet.error.query_string");

            Optional<MockData> matchingMock = mocksService.getMocks().stream()
                    .filter(mock -> originalPath != null && originalPath.matches(mock.getPath())
                            && mock.getMethod().equalsIgnoreCase(originalMethod))
                    .max(Comparator.comparingInt(MockData::getPriority));

            int responseStatus;
            ResponseEntity<Object> response;
            List<String> validationErrors = Collections.emptyList();

            if (matchingMock.isPresent()) {
                MockData mock = matchingMock.get();
                validationErrors = openApiValidationService.validateRequest(mock, request, data);

                MockData mockToUse = mock;
                boolean shouldFail = false;
                if (!validationErrors.isEmpty() && mock.getValidation() != null) {
                    ValidationAction action = mock.getValidation().getAction();
                    if (action == ValidationAction.FAIL) {
                        shouldFail = true;
                    } else if (action == ValidationAction.CUSTOM_ERROR && mock.getValidation().getCustomErrorResponse() != null) {
                        mockToUse = new MockData();
                        mockToUse.setPath(mock.getPath());
                        mockToUse.setResponse(mock.getValidation().getCustomErrorResponse());
                        mockToUse.setVariables(mock.getVariables());
                    }
                }

                if (shouldFail) {
                    responseStatus = 422;
                    Map<String, Object> errorBody = Map.of(
                        "error", "Validation failed",
                        "details", validationErrors
                    );
                    response = ResponseEntity.status(responseStatus)
                            .header("Content-Type", "application/json")
                            .body(errorBody);
                } else {
                    responseStatus = mockToUse.getResponse().getStatus();

                    String responseBodyStr;
                    if (mockToUse.getResponse().getFile() != null) {
                        try {
                            responseBodyStr = Files.readString(Paths.get(mockToUse.getResponse().getFile()));
                        } catch (java.io.IOException _) {
                            responseBodyStr = "{}";
                        }
                    } else if (mockToUse.getResponse().getJson() != null) {
                        responseBodyStr = mockToUse.getResponse().getJson().toString();
                    } else {
                        responseBodyStr = "{}";
                    }

                    Map<String, Object> context = templateContextBuilder.buildContext(request, mockToUse, data, originalPath);
                    try {
                        responseBodyStr = Mustache.compiler().defaultValue("").compile(responseBodyStr).execute(context);
                        responseBodyStr = responseBodyStr.replaceAll("\"#num#([^\"]*)\"", "$1");
                    } catch (Exception _) {
                        // Ignore and just use response template as is
                    }

                    response = ResponseEntity.status(responseStatus)
                            .header("Content-Type", "application/json")
                            .body(responseBodyStr);
                }
            } else {
                responseStatus = HttpStatus.NOT_FOUND.value();
                response = new ResponseEntity<>(new ErrorResponse(
                        HttpStatus.NOT_FOUND.getReasonPhrase(), responseStatus),
                        HttpStatus.NOT_FOUND);
            }

            Map<String, String> headers = Collections.list(request.getHeaderNames()).stream()
                    .collect(Collectors.toMap(h -> h, request::getHeader));
            String contentType = request.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String dataToStore = data == null ? "" : data;
            if (data != null && contentType.contains("json")) {
                try {
                    Object json = objectMapper.readValue(data, Object.class);
                    dataToStore = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                } catch (Exception _) {
                    // Invalid JSON, just store as is
                }
            }

            String path = originalPath;
            if (queryString != null) {
                path += "?" + queryString;
            }

            dataStorageService.storeData(dataToStore, contentType, path, headers, originalMethod,
                    request.getParameterMap(), responseStatus, validationErrors);
            requestCounterService.increment();

            return response;
        } else {
            HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            return new ResponseEntity<>(
                    new ErrorResponse(httpStatus.getReasonPhrase(), httpStatus.value()),
                    httpStatus);
        }
    }
}
