package com.gocomet.auction.repository;

import com.gocomet.auction.entity.RfqEntity;
import com.gocomet.auction.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RfqRepository extends JpaRepository<RfqEntity, Long> {

    Optional<RfqEntity> findByReferenceId(String referenceId);

    List<RfqEntity> findAllByOrderByCreatedAtDesc();

    List<RfqEntity> findByStatusOrderByCreatedAtDesc(AuctionStatus status);

    @Query("SELECT r FROM RfqEntity r WHERE r.status IN ('ACTIVE', 'EXTENDED') AND r.bidCloseTime <= :now")
    List<RfqEntity> findExpiredActiveRfqs(@Param("now") Instant now);

    @Query("SELECT r FROM RfqEntity r WHERE r.status IN ('ACTIVE', 'EXTENDED') AND r.forcedBidCloseTime <= :now")
    List<RfqEntity> findForceCloseExpiredRfqs(@Param("now") Instant now);
}
