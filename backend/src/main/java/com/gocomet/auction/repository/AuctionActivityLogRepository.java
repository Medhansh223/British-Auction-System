package com.gocomet.auction.repository;

import com.gocomet.auction.entity.AuctionActivityLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionActivityLogRepository extends JpaRepository<AuctionActivityLogEntity, Long> {

    List<AuctionActivityLogEntity> findByRfqIdOrderByCreatedAtDesc(Long rfqId);

    long countByRfqIdAndEventType(Long rfqId, com.gocomet.auction.enums.ActivityEventType eventType);
}
