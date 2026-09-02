package com.gocomet.auction.strategy;

import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.enums.ExtensionTriggerType;
import org.springframework.stereotype.Component;

@Component
public class AnyRankChangeExtensionStrategy implements ExtensionTriggerStrategy {

    @Override
    public ExtensionTriggerType getTriggerType() {
        return ExtensionTriggerType.ANY_RANK_CHANGE;
    }

    @Override
    public boolean shouldExtend(BidSubmissionContext context) {

        return context != null && context.isAnyRankChanged();
    }

    @Override
    public String buildExtensionReason(BidSubmissionContext context) {
        return String.format(
        "Auction extended by %d minutes: Supplier ranking changed (Carrier '%s' achieved rank L%d with $%s) during the trigger window.",
        context.getRfq().getExtensionDurationMinutes(),
        context.getNewQuote().getCarrierName(),
        context.getNewQuote().getSupplierRank(),
        context.getNewQuote().getTotalAmount().toPlainString()
    );
    }
}
