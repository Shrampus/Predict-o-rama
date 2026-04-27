package com.predictorama.backend.adapter.rest;

import com.predictorama.backend.adapter.external.footballdata.FootballDataApiException;
import com.predictorama.backend.adapter.rest.dto.ApiErrorResponse;
import com.predictorama.backend.domain.exception.InvalidPredictionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleFootballDataApi_returnsBadGatewayWithStructuredErrorBody() {
        String message = "football-data.org is unavailable";

        ResponseEntity<ApiErrorResponse> response =
                handler.handleFootballDataApi(new FootballDataApiException(message));
        ApiErrorResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("FOOTBALL_DATA_API_ERROR");
        assertThat(body.getMessage()).isEqualTo(message);
    }

    @Test
    void handleInvalidPrediction_returnsBadRequestWithStructuredErrorBody() {
        String message = "Prediction must include groupId";

        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidPrediction(new InvalidPredictionException(message));
        ApiErrorResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("INVALID_PREDICTION");
        assertThat(body.getMessage()).isEqualTo(message);
    }

    @Test
    void handleValidation_returnsBadRequestWithJoinedConstraintMessages() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("dto", "All point values must be positive integers")
        ));

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex);
        ApiErrorResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("INVALID_INPUT");
        assertThat(body.getMessage()).isEqualTo("All point values must be positive integers");
    }

    @Test
    void handleUnexpected_returnsInternalErrorWithSafeMessage() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("sensitive backend details"));
        ApiErrorResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.getMessage()).isEqualTo("An unexpected error occurred");
    }
}
