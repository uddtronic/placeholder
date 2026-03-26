package se.uddtronic.placeholder.ingestion;


import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import se.uddtronic.placeholder.ingestion.dto.ErrorResponse;
import se.uddtronic.placeholder.mocks.MockData;
import se.uddtronic.placeholder.mocks.MocksService;
import tools.jackson.databind.ObjectMapper;

@Controller
public class DataIngestionController implements ErrorController {

    private final DataStorageService dataStorageService;
    private final RequestCounterService requestCounterService;
    private final ObjectMapper objectMapper;
    private final MocksService mocksService;

    public DataIngestionController(DataStorageService dataStorageService,
            RequestCounterService requestCounterService, ObjectMapper objectMapper,
            MocksService mocksService) {
        this.dataStorageService = dataStorageService;
        this.requestCounterService = requestCounterService;
        this.objectMapper = objectMapper;
        this.mocksService = mocksService;
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

            if (matchingMock.isPresent()) {
                MockData mock = matchingMock.get();
                responseStatus = mock.getResponse().getStatus();
                response = ResponseEntity.status(responseStatus)
                        .header("Content-Type", "application/json")
                        .body(mock.getResponse().getJson().toString());
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
                } catch (Exception e) {
                    // Invalid JSON, just store as is
                }
            }

            String path = originalPath;
            if (queryString != null) {
                path += "?" + queryString;
            }

            dataStorageService.storeData(dataToStore, contentType, path, headers, originalMethod,
                    request.getParameterMap(), responseStatus);
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
