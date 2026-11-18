package com.scamshield.common;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.FieldError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.scamshield.auth.AuthService;
import com.scamshield.scamdetector.exception.ScamDetectorException;
import com.scamshield.scamdetector.service.EvidenceVerificationService;
import com.scamshield.scamdetector.util.FileValidationUtil;
import com.scamshield.urlscanner.SafeURLValidator;
import com.scamshield.urlscanner.URLScannerService;
import com.scamshield.jobfinder.provider.ConfiguredJobProvider.JobProviderException;
import com.scamshield.jobfinder.service.JobSearchRateLimiter;
import com.scamshield.jobfinder.service.SavedJobService;
import com.scamshield.resume.util.ResumeException;
import com.scamshield.jobmatching.service.JobMatchingException;
import com.scamshield.reports.exception.ReportException;
import com.scamshield.profile.util.ProfileException;
import com.scamshield.assistant.exception.AssistantException;
import com.scamshield.notifications.NotificationException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return response(HttpStatus.BAD_REQUEST, "Please check the highlighted fields.", fieldErrors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
        return response(HttpStatus.BAD_REQUEST, "Please check the request parameters.", fieldErrors, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleMethodValidation(
            HandlerMethodValidationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Please check the request parameters.", Map.of(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleParameterTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "One or more request parameters are invalid.", Map.of(), request);
    }

    @ExceptionHandler(AuthService.InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            AuthService.InvalidCredentialsException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(AuthService.EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(
            AuthService.EmailAlreadyExistsException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), Map.of("email", exception.getMessage()), request);
    }

    @ExceptionHandler(AuthService.InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPassword(
            AuthService.InvalidPasswordException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of("password", exception.getMessage()), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequest(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.", Map.of(), request);
    }

    @ExceptionHandler(ScamDetectorException.class)
    public ResponseEntity<ApiError> handleScamDetector(
            ScamDetectorException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ResumeException.class)
    public ResponseEntity<ApiError> handleResume(
            ResumeException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler({
        FileValidationUtil.InvalidFileException.class,
        EvidenceVerificationService.UnsafeUrlException.class,
        SafeURLValidator.InvalidUrlException.class
    })
    public ResponseEntity<ApiError> handleUnsafeScamDetectorInput(
            RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(URLScannerService.URLScanNotFoundException.class)
    public ResponseEntity<ApiError> handleMissingUrlScan(
            URLScannerService.URLScanNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(SavedJobService.SavedJobNotFoundException.class)
    public ResponseEntity<ApiError> handleMissingSavedJob(
            SavedJobService.SavedJobNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(JobProviderException.class)
    public ResponseEntity<ApiError> handleJobProvider(
            JobProviderException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatus().value());
        return response(status, exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(JobSearchRateLimiter.RateLimitException.class)
    public ResponseEntity<ApiError> handleJobSearchRateLimit(
            JobSearchRateLimiter.RateLimitException exception, HttpServletRequest request) {
        return response(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(JobMatchingException.class)
    public ResponseEntity<ApiError> handleJobMatching(
            JobMatchingException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ApiError> handleReport(
            ReportException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ProfileException.class)
    public ResponseEntity<ApiError> handleProfile(
            ProfileException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(AssistantException.class)
    public ResponseEntity<ApiError> handleAssistant(
            AssistantException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiError> handleNotification(
            NotificationException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadTooLarge(
            MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return response(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "The uploaded file exceeds the configured size limit.",
                Map.of(),
                request);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(DuplicateKeyException exception, HttpServletRequest request) {
        return response(
                HttpStatus.CONFLICT,
                "An account with this email already exists.",
                Map.of("email", "An account with this email already exists."),
                request);
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status, String message, Map<String, String> fieldErrors, HttpServletRequest request) {
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), message, fieldErrors, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
