package com.gocomet.auction.factory;

import com.gocomet.auction.constant.ExceptionMessageConstants;
import com.gocomet.auction.enums.ExtensionTriggerType;
import com.gocomet.auction.strategy.ExtensionTriggerStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ExtensionStrategyFactory {

    private final Map<ExtensionTriggerType, ExtensionTriggerStrategy> strategyMap = new EnumMap<>(ExtensionTriggerType.class);

    public ExtensionStrategyFactory(List<ExtensionTriggerStrategy> strategies) {
        for (ExtensionTriggerStrategy strategy : strategies) {
            strategyMap.put(strategy.getTriggerType(), strategy);
        }
    }

    public ExtensionTriggerStrategy getStrategy(ExtensionTriggerType triggerType) {
        if (triggerType == null) {
            triggerType = ExtensionTriggerType.ANY_BID;
        }
        ExtensionTriggerStrategy strategy = strategyMap.get(triggerType);
        if (strategy == null) {
            throw new IllegalArgumentException(
            String.format(ExceptionMessageConstants.MSG_UNKNOWN_TRIGGER_STRATEGY, triggerType));
        }
        return strategy;
    }
}
