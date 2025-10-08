package com.carsil.userapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        String dev = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiError body = apiError(HttpStatus.UNPROCESSABLE_ENTITY, "Datos inválidos", dev, path(request));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        ApiError body = apiError(HttpStatus.BAD_REQUEST, "JSON malformado o tipo inválido",
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage(),
                path(request));
        return ResponseEntity.badRequest().body(body);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          @NonNull HttpHeaders headers,
                                                                          @NonNull HttpStatusCode status,
                                                                          @NonNull WebRequest request) {
        ApiError body = apiError(HttpStatus.BAD_REQUEST, "Parámetro requerido faltante",
                ex.getParameterName() + " es obligatorio", path(request));
        return ResponseEntity.badRequest().body(body);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleTypeMismatch(org.springframework.beans.TypeMismatchException ex,
                                                        @NonNull HttpHeaders headers,
                                                        @NonNull HttpStatusCode status,
                                                        @NonNull WebRequest request) {
        String dev = ex instanceof MethodArgumentTypeMismatchException matme
                ? "Parámetro '" + matme.getName() + "' espera " + typeName(matme.getRequiredType()) +
                " pero recibió '" + matme.getValue() + "'"
                : ex.getMessage();

        ApiError body = apiError(HttpStatus.BAD_REQUEST, "Tipo de parámetro inválido", dev, path(request));
        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        ApiError body = apiError(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        ApiError body = apiError(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        String dev = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        ApiError body = apiError(HttpStatus.BAD_REQUEST, "Parámetros inválidos", dev, req.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        String dev = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        ApiError body = apiError(HttpStatus.CONFLICT, "Conflicto de datos", dev, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        ApiError body = apiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        var status = HttpStatus.UNAUTHORIZED;

        String userMsg = ex.getMessage();

        String devMsg = (ex.getCause() != null)
                ? ex.getCause().getMessage()
                : "Detalle no disponible o no especificado.";

        var body = ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(userMsg)
                .developerMessage(devMsg)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }


    private ApiError apiError(HttpStatus status, String userMsg, String devMsg, String path) {
        return ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(userMsg)
                .developerMessage(devMsg)
                .path(path)
                .build();
    }

    private String path(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        String desc = request.getDescription(false);
        return desc != null && desc.startsWith("uri=") ? desc.substring(4) : desc;
    }

    private String typeName(Class<?> c) {
        return c == null ? "tipo desconocido" : c.getSimpleName();
    }
}