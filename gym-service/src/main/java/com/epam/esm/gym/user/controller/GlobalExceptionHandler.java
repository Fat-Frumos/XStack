package com.epam.esm.gym.user.controller;

import com.epam.esm.gym.jms.dto.MessageResponse;
import com.epam.esm.gym.user.exception.InvalidJwtAuthenticationException;
import com.epam.esm.gym.user.exception.TokenNotFoundException;
import com.epam.esm.gym.user.exception.UserAlreadyExistsException;
import com.epam.esm.gym.user.exception.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Global exception handler for managing various exceptions thrown across the application.
 * This class uses `@RestControllerAdvice` to provide centralized exception handling
 * and custom responses for different types of exceptions. It includes handlers for missing
 * request parameters, unsupported request methods, validation errors, and runtime exceptions.
 *
 * @author Pavlo Poliak
 * @version 1.0.0
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String INTERNAL_MESSAGE = "An unexpected error occurred ";
    private static final String MISSING_MESSAGE = "Required request parameter '%s' is not present";
    private static final String NOT_SUPPORTED_MESSAGE = "Request method '%s' not supported";

    /**
     * Handles {@link IllegalArgumentException} exceptions.
     *
     * @param ex the {@link IllegalArgumentException} to handle
     * @return a {@link ResponseEntity} containing a {@link MessageResponse} with the error message
     * and HTTP status {@link HttpStatus#BAD_REQUEST}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String exceptionMessage = ex.getMessage();
        String message = exceptionMessage.contains("No enum constant")
                ? String.format("Invalid value: %s",
                exceptionMessage.substring(exceptionMessage.lastIndexOf('.') + 1))
                : exceptionMessage;
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    /**
     * Handles {@link UserAlreadyExistsException} exceptions.
     *
     * @param ex the {@link UserAlreadyExistsException} to handle
     * @return a {@link ResponseEntity} containing a {@link MessageResponse} with the error message
     * and HTTP status {@link HttpStatus#BAD_REQUEST}
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles missing request parameter exceptions by returning a bad request response
     * with a message indicating which parameter is missing. This method is triggered
     * when a required parameter is not provided in the request. It formats the message
     * using the name of the missing parameter extracted from the exception.
     *
     * @param ex The {@link MissingServletRequestParameterException} instance containing details
     *           of the missing parameter.
     * @return A {@link ResponseEntity} containing the error message and a bad request status.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<MessageResponse> handleMissingParams(
            MissingServletRequestParameterException ex) {
        String message = String.format(MISSING_MESSAGE, ex.getParameterName());
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    /**
     * Handles exceptions related to unsupported HTTP request methods. It returns a method
     * not allowed status with a message indicating which request method is not supported.
     * This method is invoked when a client sends a request using an HTTP method that is
     * not permitted by the endpoint. The message is formatted with the unsupported HTTP method.
     *
     * @param ex The {@link HttpRequestMethodNotSupportedException} instance containing details
     *           of the unsupported method.
     * @return A {@link ResponseEntity} with the error message and a method not allowed status.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MessageResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        String message = String.format(NOT_SUPPORTED_MESSAGE, ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new MessageResponse(message));
    }

    /**
     * Handles validation exceptions thrown when method arguments fail validation. It constructs
     * a message listing all the fields with validation errors by iterating over the binding result
     * errors. The errors are formatted and concatenated into a single error message.
     * This method returns a bad request response with the detailed validation errors.
     *
     * @param ex The {@link MethodArgumentNotValidException} containing validation errors.
     * @return A {@link ResponseEntity} with the validation error messages and a bad request status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return String.format("%s: %s", fieldName, errorMessage);
                })
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    /**
     * Handles runtime exceptions and other generic exceptions by logging the error and returning
     * a response with an internal server error status. This method catches unexpected errors
     * not specifically handled elsewhere. It logs the exception message and provides a generic
     * error message to the client indicating an unexpected error occurred.
     *
     * @param ex The {@link RuntimeException} or {@link Exception} instance.
     * @return A {@link ResponseEntity} with a generic error message and an internal server error status.
     */
    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ResponseEntity<MessageResponse> handleException(Exception ex) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new MessageResponse(String.format(INTERNAL_MESSAGE)));
    }

    /**
     * Handles runtime exceptions that occur during request processing.
     * This method captures any {@link RuntimeException} thrown during request handling and returns
     * a standardized error response with an HTTP status of {@code 500 Internal Server Error}.
     *
     * @param ex the exception that was thrown
     * @return a {@link ResponseEntity} containing a {@link MessageResponse} with an error message
     */
    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<MessageResponse> handleRuntimeException(Exception ex) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new MessageResponse(String.format(INTERNAL_MESSAGE)));
    }


    /**
     * Handles cases where the request body is missing or invalid.
     * This method captures {@link HttpMessageNotReadableException} and returns a standardized error response
     * with an HTTP status of {@code 400 Bad Request}.
     *
     * @param ex the exception that was thrown
     * @return a {@link ResponseEntity} containing a {@link MessageResponse} with an error message
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponse> handleMissingRequestBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Request body is missing or invalid."));
    }

    /**
     * Handles exceptions when an entity or user is not found in the database. This method
     * returns a not found status with the exception's message. It handles exceptions like
     * {@link EntityNotFoundException}. The response includes
     * the detailed message from the exception.
     *
     * @param ex The {@link EntityNotFoundException} instance.
     * @return A {@link ResponseEntity} with the exception message and a not found status.
     */
    @ResponseBody
    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles exceptions when an entity or user is not found in the database. This method
     * returns a not found status with the exception's message. It handles exceptions like
     * {@link UserNotFoundException}. The response includes
     * the detailed message from the exception.
     *
     * @param ex The {@link UserNotFoundException} instance.
     * @return A {@link ResponseEntity} with the exception message and a not found status.
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles validation exceptions related to constraint violations in method arguments.
     * It generates a response with a bad request status and a message containing all unique
     * constraint violation messages. The messages are sorted and concatenated into a single
     * error message. This method provides detailed feedback for constraint violations encountered.
     *
     * @param ex The {@link ConstraintViolationException} containing the constraint violations.
     * @return A {@link ResponseEntity} with the constraint violation messages and a bad request status.
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse> handleValidationExceptions(
            ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .sorted().distinct()
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(new MessageResponse(message), BAD_REQUEST);
    }

    /**
     * Handles exceptions related to JWT authentication, specifically {@link InvalidJwtAuthenticationException}
     * and {@link TokenNotFoundException}.
     *
     * <p>This method processes {@link SignatureException} thrown due to invalid JWT signatures or token
     * issues. It logs the error and returns a {@link ResponseEntity} with an error message and HTTP status
     * {@link HttpStatus#UNAUTHORIZED}.
     * </p>
     *
     * @param ex the {@link SignatureException} thrown during JWT authentication.
     * @return a {@link ResponseEntity} containing a {@link MessageResponse} with an error message and
     * HTTP status {@link HttpStatus#UNAUTHORIZED}.
     */
    @ResponseBody
    @ExceptionHandler({
            InvalidJwtAuthenticationException.class,
            TokenNotFoundException.class,
            SignatureException.class,
            AuthenticationException.class})
    public ResponseEntity<MessageResponse> handleSignatureException(RuntimeException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
