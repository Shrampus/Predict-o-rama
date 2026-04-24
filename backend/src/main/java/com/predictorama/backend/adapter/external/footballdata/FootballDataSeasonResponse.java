package com.predictorama.backend.adapter.external.footballdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FootballDataSeasonResponse {
    private Integer id;
    private String startDate;
    private String endDate;
}
