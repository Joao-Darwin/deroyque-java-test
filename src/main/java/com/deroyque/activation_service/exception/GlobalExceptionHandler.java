package com.deroyque.activation_service.exception;

import com.deroyque.activation_service.exception.exceptions.ActivationConflictException;
import com.deroyque.activation_service.exception.exceptions.ActivationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final String TRACE_ID_PROPERTY = "traceId";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos não passaram na validação."
        );

        problem.setType(URI.create(
                "https://api.exemplo.com/problems/validation-error"
        ));

        problem.setTitle("Dados inválidos");
        problem.setInstance(URI.create(request.getRequestURI()));

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()
                ))
                .toList();

        problem.setProperty(TRACE_ID_PROPERTY, MDC.get(TRACE_ID_PROPERTY));
        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(ActivationConflictException.class)
    public ProblemDetail handleActivationConflict(
            ActivationConflictException ex,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problem.setType(URI.create(
                "https://api.exemplo.com/problems/activation-conflict"
        ));

        problem.setTitle("Conflito na solicitação");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRACE_ID_PROPERTY, MDC.get(TRACE_ID_PROPERTY));

        return problem;
    }

    @ExceptionHandler(ActivationNotFoundException.class)
    public ProblemDetail handleActivationNotFound(
            ActivationNotFoundException ex,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problem.setType(URI.create(
                "https://api.exemplo.com/problems/activation-notfound"
        ));

        problem.setTitle("Solicitação de ativação não encontrada");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRACE_ID_PROPERTY, MDC.get(TRACE_ID_PROPERTY));

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado ao processar sua solicitação."
        );

        problem.setType(URI.create(
                "https://api.exemplo.com/problems/internal-server-error"
        ));

        problem.setTitle("Erro interno do servidor");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRACE_ID_PROPERTY, MDC.get(TRACE_ID_PROPERTY));

        return problem;
    }
}