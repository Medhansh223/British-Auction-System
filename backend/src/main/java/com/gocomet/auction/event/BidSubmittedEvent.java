package com.gocomet.auction.event;

import com.gocomet.auction.entity.QuoteEntity;
import com.gocomet.auction.entity.RfqEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BidSubmittedEvent extends ApplicationEvent {

    private final RfqEntity rfq;
    private final QuoteEntity quote;
    private final boolean wasExtended;
    private final String extensionReason;

    public BidSubmittedEvent(Object source, RfqEntity rfq, QuoteEntity quote, boolean wasExtended, String extensionReason) {
        super(source);
        this.rfq = rfq;
        this.quote = quote;
        this.wasExtended = wasExtended;
        this.extensionReason = extensionReason;
    }
}
