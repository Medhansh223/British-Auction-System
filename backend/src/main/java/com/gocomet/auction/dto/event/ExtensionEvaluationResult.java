package com.gocomet.auction.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionEvaluationResult {

    private boolean extensionTriggered;
    private Instant previousCloseTime;
    private Instant newCloseTime;
    private boolean cappedByForcedClose;
    private String reason;
}
