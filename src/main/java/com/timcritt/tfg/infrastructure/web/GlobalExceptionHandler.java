package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.infrastructure.web.dto.ErrorResponse;
import com.timcritt.tfg.application.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlreadyHasRoleException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyHasRole(AlreadyHasRoleException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ActiveInvitationExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveInvitation(ActiveInvitationExistsException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(InvitationNotFoundException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(EmailSendFailedException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendFailed(EmailSendFailedException ex, HttpServletRequest req) {
        // Log the failure internally but return 201 Created so callers can't enumerate emails
        log.warn("Email send failed for request to {}: {}", req.getRequestURI(), ex.getMessage());
        log.debug("Email send failure stacktrace", ex);
        ErrorResponse body = new ErrorResponse(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(), "If the email is registered, a reset email has been sent.", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDBConflict(DataIntegrityViolationException ex, HttpServletRequest req) {

        String message = "Constraint violation: resource already exists or invalid data.";
        ErrorResponse body = new ErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    @ExceptionHandler(NewPasswordNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNewPasswordNotValid(NewPasswordNotValidException ex, HttpServletRequest req) {
        String message = "password not valid";
        ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Internal server error", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
