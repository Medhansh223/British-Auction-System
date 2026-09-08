# Low Level Design (LLD)

The following diagram illustrates the low-level object-oriented architecture of the British Auction system. It highlights the relationships between Controllers, Services, the Database (Repositories/Entities), and the Strategy Pattern implementation for dynamic auction extensions.

```mermaid
classDiagram
    %% Controllers
    class RfqController {
        +createRfq(request)
        +getRfqById(id)
        +getAllRfqs(status)
    }
    class QuoteController {
        +submitQuote(rfqId, request)
        +getQuotesForRfq(rfqId)
    }
    class AuctionAuditController {
        +getAuditLogs(rfqId)
    }

    %% Service Interfaces
    class RfqManagementService {
        <<interface>>
        +createRfq(request) RfqResponseDto
        +getRfqById(id) RfqResponseDto
        +getAllRfqs(status) List~RfqResponseDto~
        +refreshAuctionStatuses() void
    }
    class QuoteSubmissionService {
        <<interface>>
        +submitQuote(rfqId, request) QuoteResponseDto
        +getQuotesForRfq(rfqId) List~QuoteResponseDto~
    }
    class AuctionExtensionService {
        <<interface>>
        +evaluateAndApplyExtension(context) ExtensionEvaluationResult
        +isInsideTriggerWindow(rfq, timestamp) boolean
    }
    class SupplierRankingService {
        <<interface>>
        +reindexAllQuotesForRfq(rfqId) void
    }
    class AuctionAuditLogService {
        <<interface>>
        +getLogsForRfq(rfqId) List~AuctionActivityLogResponseDto~
        +onBidSubmitted(event) void
        +onAuctionExtended(event) void
    }

    %% Service Implementations
    class RfqManagementServiceImpl {
        -RfqRepository rfqRepository
    }
    class QuoteSubmissionServiceImpl {
        -QuoteRepository quoteRepository
        -RfqManagementService rfqService
        -SupplierRankingService rankingService
        -AuctionExtensionService extensionService
        -ApplicationEventPublisher eventPublisher
    }
    class AuctionExtensionServiceImpl {
        -RfqRepository rfqRepository
        -ExtensionStrategyFactory strategyFactory
        -ApplicationEventPublisher eventPublisher
    }
    class SupplierRankingServiceImpl {
        -QuoteRepository quoteRepository
    }
    class AuctionAuditLogServiceImpl {
        -AuctionActivityLogRepository logRepository
    }

    %% Strategy & Factory
    class ExtensionStrategyFactory {
        -Map~ExtensionTriggerType, ExtensionTriggerStrategy~ strategyMap
        +getStrategy(type) ExtensionTriggerStrategy
    }
    class ExtensionTriggerStrategy {
        <<interface>>
        +getTriggerType() ExtensionTriggerType
        +shouldExtend(context) boolean
        +buildExtensionReason(context) String
    }
    class AnyBidExtensionStrategy {
        +getTriggerType() ExtensionTriggerType
        +shouldExtend(context) boolean
        +buildExtensionReason(context) String
    }
    class AnyRankChangeExtensionStrategy {
        +getTriggerType() ExtensionTriggerType
        +shouldExtend(context) boolean
        +buildExtensionReason(context) String
    }
    class LowestBidderChangeExtensionStrategy {
        +getTriggerType() ExtensionTriggerType
        +shouldExtend(context) boolean
        +buildExtensionReason(context) String
    }

    %% Entities
    class RfqEntity {
        -Long id
        -String referenceId
        -Instant bidStartTime
        -Instant bidCloseTime
        -Instant forcedBidCloseTime
        -Integer triggerWindowMinutes
        -Integer extensionDurationMinutes
        -ExtensionTriggerType extensionStrategy
        -AuctionStatus status
        -Integer extensionCount
    }
    class QuoteEntity {
        -Long id
        -RfqEntity rfq
        -String carrierName
        -BigDecimal totalAmount
        -String currentRank
        -Instant submissionTime
    }
    class AuctionActivityLogEntity {
        -Long id
        -RfqEntity rfq
        -ActivityEventType eventType
        -String description
        -Instant timestamp
    }

    %% Repositories
    class RfqRepository { <<interface>> }
    class QuoteRepository { <<interface>> }
    class AuctionActivityLogRepository { <<interface>> }

    %% Core Relationships - Interface Implementation
    RfqManagementService <|.. RfqManagementServiceImpl
    QuoteSubmissionService <|.. QuoteSubmissionServiceImpl
    AuctionExtensionService <|.. AuctionExtensionServiceImpl
    SupplierRankingService <|.. SupplierRankingServiceImpl
    AuctionAuditLogService <|.. AuctionAuditLogServiceImpl

    %% Strategy Implementations
    ExtensionTriggerStrategy <|.. AnyBidExtensionStrategy
    ExtensionTriggerStrategy <|.. AnyRankChangeExtensionStrategy
    ExtensionTriggerStrategy <|.. LowestBidderChangeExtensionStrategy

    %% Dependencies - Controller to Service
    RfqController --> RfqManagementService : delegates
    QuoteController --> QuoteSubmissionService : delegates
    AuctionAuditController --> AuctionAuditLogService : delegates

    %% Dependencies - Service to Service
    QuoteSubmissionServiceImpl --> SupplierRankingService : invokes ranking
    QuoteSubmissionServiceImpl --> AuctionExtensionService : invokes extension check
    QuoteSubmissionServiceImpl --> RfqManagementService : fetches RFQ details
    AuctionExtensionServiceImpl --> ExtensionStrategyFactory : resolves strategy

    %% Dependencies - Factory to Strategy
    ExtensionStrategyFactory o-- ExtensionTriggerStrategy : maintains instances

    %% Dependencies - Repositories
    RfqManagementServiceImpl --> RfqRepository : uses
    QuoteSubmissionServiceImpl --> QuoteRepository : uses
    AuctionExtensionServiceImpl --> RfqRepository : uses
    SupplierRankingServiceImpl --> QuoteRepository : uses
    AuctionAuditLogServiceImpl --> AuctionActivityLogRepository : uses

    %% Entities Composition
    QuoteEntity "0..*" *-- "1" RfqEntity : belongs to
    AuctionActivityLogEntity "0..*" *-- "1" RfqEntity : belongs to
```
