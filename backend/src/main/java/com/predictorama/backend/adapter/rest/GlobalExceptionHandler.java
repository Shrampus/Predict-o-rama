package com.predictorama.backend.adapter.rest;

import com.predictorama.backend.adapter.external.footballdata.FootballDataApiException;
import com.predictorama.backend.adapter.rest.dto.ApiErrorResponse;
import com.predictorama.backend.domain.exception.AlreadyMemberException;
import com.predictorama.backend.domain.exception.GroupAccessDeniedException;
import com.predictorama.backend.domain.exception.GroupMemberNotFoundException;
import com.predictorama.backend.domain.exception.GroupNotFoundException;
import com.predictorama.backend.domain.exception.InvalidCredentialsException;
import com.predictorama.backend.domain.exception.InvalidGoogleTokenException;
import com.predictorama.backend.domain.exception.InvalidPredictionException;
import com.predictorama.backend.domain.exception.TournamentAlreadyLinkedException;
import com.predictorama.backend.domain.exception.UsernameTakenException;
import com.predictorama.backend.domain.exception.TournamentNotLinkedException;
import com.predictorama.backend.domain.exception.TournamentNotFoundException;
import com.predictorama.backend.domain.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(code, message));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidGoogleToken(InvalidGoogleTokenException ex) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_TOKEN", ex.getMessage());
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameTaken(UsernameTakenException ex) {
        return error(HttpStatus.CONFLICT, "USERNAME_TAKEN", ex.getMessage());
    }

    @ExceptionHandler(AlreadyMemberException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyMember(AlreadyMemberException ex) {
        return error(HttpStatus.CONFLICT, "ALREADY_MEMBER", ex.getMessage());
    }

    @ExceptionHandler(GroupAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleGroupAccessDenied(GroupAccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "GROUP_ACCESS_DENIED", ex.getMessage());
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleGroupNotFound(GroupNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "GROUP_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(GroupMemberNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleGroupMemberNotFound(GroupMemberNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "GROUP_MEMBER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(TournamentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTournamentNotFound(TournamentNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "TOURNAMENT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(TournamentAlreadyLinkedException.class)
    public ResponseEntity<ApiErrorResponse> handleTournamentAlreadyLinked(TournamentAlreadyLinkedException ex) {
        return error(HttpStatus.CONFLICT, "TOURNAMENT_ALREADY_LINKED", ex.getMessage());
    }

    @ExceptionHandler(TournamentNotLinkedException.class)
    public ResponseEntity<ApiErrorResponse> handleTournamentNotLinked(TournamentNotLinkedException ex) {
        return error(HttpStatus.NOT_FOUND, "TOURNAMENT_NOT_LINKED", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_INPUT", ex.getMessage());
    }

    @ExceptionHandler(InvalidPredictionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPrediction(InvalidPredictionException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_PREDICTION", ex.getMessage());
    }

    @ExceptionHandler(FootballDataApiException.class)
    public ResponseEntity<ApiErrorResponse> handleFootballDataApi(FootballDataApiException ex) {
        return error(HttpStatus.BAD_GATEWAY, "FOOTBALL_DATA_API_ERROR", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }
}
