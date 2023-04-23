package ro.unibuc.fmi.airlliantquartz.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ro.unibuc.fmi.airlliantmodel.exception.ApiError;
import ro.unibuc.fmi.airlliantmodel.exception.ApiException;
import ro.unibuc.fmi.airlliantmodel.exception.ExceptionStatus;

import java.net.ConnectException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<List<ApiError>> handleException(Exception e) {

        log.error(e.toString());

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder().build()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );

    }

    @ExceptionHandler(value = {ApiException.class})
    public ResponseEntity<List<ApiError>> handleApiException(ApiException e) {

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(e.getExceptionStatus().getHttpStatus())) {
            log.error(e.getErrorMessage());
        } else {
            log.info(e.getErrorMessage());
        }

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getErrorMessage())
                        .errorCode(e.getExceptionStatus().getErrorCode())
                        .build()),
                e.getExceptionStatus().getHttpStatus()
        );

    }

    @ExceptionHandler(value = {ConnectException.class})
    public ResponseEntity<List<ApiError>> handleConnectException(ConnectException e) {

        log.error("{} HTTP Status {}", e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getMessage())
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_INTERNAL_ERROR)
                        .build()),
                HttpStatus.SERVICE_UNAVAILABLE
        );

    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    public ResponseEntity<List<ApiError>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {

        log.info("{} HTTP Status {}", e.getMessage(), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getMessage())
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                        .build()),
                HttpStatus.BAD_REQUEST
        );

    }

    @ExceptionHandler(value = {DateTimeParseException.class})
    public ResponseEntity<List<ApiError>> handleDateTimeParseException(DateTimeParseException e) {

        log.info("{} HTTP Status {}", e.getMessage(), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getMessage())
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                        .build()),
                HttpStatus.BAD_REQUEST
        );

    }

    @ExceptionHandler(value = {DateTimeException.class})
    public ResponseEntity<List<ApiError>> handleDateTimeException(DateTimeException e) {

        log.info("{} HTTP Status {}", e.getMessage(), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getMessage())
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                        .build()),
                HttpStatus.BAD_REQUEST
        );

    }

    @ExceptionHandler(value = {InvalidFormatException.class})
    public ResponseEntity<List<ApiError>> handleInvalidFormatException(InvalidFormatException e) {

        log.info("{} HTTP Status {}", e.getMessage(), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getMessage())
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                        .build()),
                HttpStatus.BAD_REQUEST
        );

    }

    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ResponseEntity<List<ApiError>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {

        log.info("{} HTTP Status {}", e.getMessage(), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getMessage())
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                        .build()),
                HttpStatus.BAD_REQUEST
        );

    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<List<ApiError>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {

        log.info("{} HTTP Status {}", e.getMessage(), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                Collections.singletonList(ApiError.builder()
                        .errorMessage(e.getMessage())
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                        .build()),
                HttpStatus.BAD_REQUEST
        );

    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<List<ApiError>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

        if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof Exception) {

            Exception exception = (Exception) e.getCause().getCause();

            log.info("{} HTTP Status {}", exception.getMessage(), HttpStatus.BAD_REQUEST);

            return new ResponseEntity<>(
                    Collections.singletonList(ApiError.builder()
                            .errorMessage(exception.getMessage())
                            .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                            .build()),
                    HttpStatus.BAD_REQUEST
            );

        } else {

            log.error(e.toString());

            return new ResponseEntity<>(
                    Collections.singletonList(ApiError.builder()
                            .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                            .build()),
                    HttpStatus.BAD_REQUEST
            );

        }
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<List<ApiError>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        List<String> failedValidationList = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        failedValidationList.forEach(validation -> log.info("Validation failed with message: {}", validation));

        List<ApiError> apiErrorList = failedValidationList.stream()
                .map(validation -> ApiError.builder()
                        .errorMessage(validation)
                        .errorCode(ExceptionStatus.ErrorCode.AIRLLIANT_BAD_REQUEST)
                        .build()
                )
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                apiErrorList,
                HttpStatus.BAD_REQUEST
        );

    }

}