package com.gocomet.auction.strategy;

import com.gocomet.auction.constant.RfqConstants;
import com.gocomet.auction.dto.event.BidSubmissionContext;
import com.gocomet.auction.enums.ExtensionTriggerType;
import org.springframework.stereotype.Component;

@Component
public class LowestBidderChangeExtensionStrategy implements ExtensionTriggerStrategy {

    @Override
    public ExtensionTriggerType getTriggerType() {
        return ExtensionTriggerType.L1_CHANGE;
    }

    @Override
    public boolean shouldExtend(BidSubmissionContext context) {
        if (context == null || context.getNewQuote() == null) {
            return false;
        }

        return context.getNewQuote().getSupplierRank() == RfqConstants.TOP_RANK_L1
        && context.isLowestBidderChanged();
    }

    @Override
    public String buildExtensionReason(BidSubmissionContext context) {
        return String.format(
        "Auction extended by %d minutes: New Lowest Bidder (L1) established! Carrier '%s' submitted a new winning quote of $%s.",
        context.getRfq().getExtensionDurationMinutes(),
        context.getNewQuote().getCarrierName(),
        context.getNewQuote().getTotalAmount().toPlainString()
    );
    }
}
