package com.gocomet.auction.strategy;

import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.enums.ExtensionTriggerType;

public interface ExtensionTriggerStrategy {

    ExtensionTriggerType getTriggerType();

    boolean shouldExtend(BidSubmissionContext context);

    String buildExtensionReason(BidSubmissionContext context);
}
