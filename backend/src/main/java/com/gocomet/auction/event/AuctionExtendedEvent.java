package com.gocomet.auction.event;

import com.gocomet.auction.entity.RfqEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class AuctionExtendedEvent extends ApplicationEvent {

    private final RfqEntity rfq;
    private final Instant previousCloseTime;
    private final Instant newCloseTime;
    private final String reason;
    private final boolean cappedByForcedClose;

    public AuctionExtendedEvent(Object source, RfqEntity rfq, Instant previousCloseTime,
                                Instant newCloseTime, String reason, boolean cappedByForcedClose) {
        super(source);
        this.rfq = rfq;
        this.previousCloseTime = previousCloseTime;
        this.newCloseTime = newCloseTime;
        this.reason = reason;
        this.cappedByForcedClose = cappedByForcedClose;
    }
}
