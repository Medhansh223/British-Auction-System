package com.gocomet.auction.service;

import com.gocomet.auction.dto.request.CreateRfqRequestDto;
import com.gocomet.auction.dto.response.RfqResponseDto;
import com.gocomet.auction.enums.AuctionStatus;

import java.util.List;

public interface RfqManagementService {

    RfqResponseDto createRfq(CreateRfqRequestDto request);

    RfqResponseDto getRfqById(Long rfqId);

    RfqResponseDto getRfqByReferenceId(String referenceId);

    List<RfqResponseDto> getAllRfqs(AuctionStatus status);

    void refreshAuctionStatuses();
}
