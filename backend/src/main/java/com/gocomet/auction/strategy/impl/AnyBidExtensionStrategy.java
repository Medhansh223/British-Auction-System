package com.gocomet.auction.strategy.impl;

import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.enums.ExtensionTriggerType;
import com.gocomet.auction.strategy.ExtensionTriggerStrategy;
import org.springframework.stereotype.Component;

@Component
public class AnyBidExtensionStrategy implements ExtensionTriggerStrategy {

    @Override
    public ExtensionTriggerType getTriggerType() {
        return ExtensionTriggerType.ANY_BID;
    }

    @Override
    public boolean shouldExtend(BidSubmissionContext context) {

        return context != null && context.getNewQuote() != null;
    }

    @Override
    public String buildExtensionReason(BidSubmissionContext context) {
        return String.format(
        "Auction extended by %d minutes: Bid received from '%s' ($%s) during the last %d-minute trigger window.",
        context.getRfq().getExtensionDurationMinutes(),
        context.getNewQuote().getCarrierName(),
        context.getNewQuote().getTotalAmount().toPlainString(),
        context.getRfq().getTriggerWindowMinutes()
    );
    }
}
