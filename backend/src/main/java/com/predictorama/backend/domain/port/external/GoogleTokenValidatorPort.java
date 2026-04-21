package com.predictorama.backend.domain.port.external;

import com.predictorama.backend.domain.entity.GoogleUserInfo;

public interface GoogleTokenValidatorPort {
    GoogleUserInfo validate(String idToken);
}