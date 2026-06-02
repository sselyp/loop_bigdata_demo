package com.bigdata.etl.engine;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class EngineAdapterFactory {

    private final Map<String, ComputeEngineAdapter> adapters = new HashMap<>();

    public EngineAdapterFactory(List<ComputeEngineAdapter> adapterList) {
        for (ComputeEngineAdapter adapter : adapterList) {
            // Register for common types
            adapters.put(adapter.getClass().getSimpleName(), adapter);
        }
    }

    public ComputeEngineAdapter getAdapter(String engineType) {
        for (ComputeEngineAdapter adapter : adapters.values()) {
            if (adapter.supports(engineType.toUpperCase())) {
                return adapter;
            }
        }
        throw new IllegalArgumentException("No adapter available for engine type: " + engineType);
    }
}
