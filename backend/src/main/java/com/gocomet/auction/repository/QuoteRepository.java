package com.gocomet.auction.repository;

import com.gocomet.auction.entity.QuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<QuoteEntity, Long> {

    List<QuoteEntity> findByRfqIdOrderByTotalAmountAscSubmittedAtAsc(Long rfqId);

    List<QuoteEntity> findByRfqIdOrderBySubmittedAtDesc(Long rfqId);

    Optional<QuoteEntity> findFirstByRfqIdOrderByTotalAmountAscSubmittedAtAsc(Long rfqId);

    long countByRfqId(Long rfqId);

    List<QuoteEntity> findByRfqIdAndCarrierNameIgnoreCase(Long rfqId, String carrierName);
}
