package com.predictorama.backend.adapter.external.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.predictorama.backend.domain.entity.GoogleUserInfo;
import com.predictorama.backend.domain.port.external.GoogleTokenValidatorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenValidatorAdapter implements GoogleTokenValidatorPort {
    private final GoogleIdTokenVerifier verifier;

    @Value("${google.client_id}")
    private final String googleClientId;

    @Override
    GoogleUserInfo validate(String idToken){

        try{
            return verifier.verify(idToken);
        } catch {
            log.error("TODO");
        }


    };
}

