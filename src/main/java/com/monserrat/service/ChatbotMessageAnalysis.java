package com.monserrat.service;

import java.math.BigDecimal;

public record ChatbotMessageAnalysis(
        String intent,
        BigDecimal confidence,
        String directResponse,
        String visitorName
) {
    public ChatbotMessageAnalysis(String intent, BigDecimal confidence, String directResponse) {
        this(intent, confidence, directResponse, null);
    }

    public boolean hasDirectResponse() {
        return directResponse != null && !directResponse.isBlank();
    }

    public boolean hasVisitorName() {
        return visitorName != null && !visitorName.isBlank();
    }
}
