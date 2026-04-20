package com.predictorama.backend.adapter.external.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.predictorama.backend.domain.entity.GoogleUserInfo;
import com.predictorama.backend.domain.exception.InvalidGoogleTokenException;
import com.predictorama.backend.domain.port.external.GoogleTokenValidatorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenValidatorAdapter implements GoogleTokenValidatorPort {
    private final GoogleIdTokenVerifier verifier;

    @Override
    public GoogleUserInfo validate(String idToken){

        try{
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new InvalidGoogleTokenException(null);
            }
            var payload = googleIdToken.getPayload();
            return new GoogleUserInfo(payload.getSubject(), payload.getEmail(), (String) payload.get("name"));
        } catch (Exception e) {
            log.error("Google token validation failed", e);
            throw new InvalidGoogleTokenException("Invalid Google token", e);
        }


    };
}

