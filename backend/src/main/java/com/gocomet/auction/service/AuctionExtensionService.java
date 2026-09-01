package com.gocomet.auction.service;

import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.dto.event.ExtensionEvaluationResult;
import com.gocomet.auction.entity.RfqEntity;

public interface AuctionExtensionService {

    ExtensionEvaluationResult evaluateAndApplyExtension(BidSubmissionContext context);

    boolean isInsideTriggerWindow(RfqEntity rfq, java.time.Instant timestamp);
}
