package com.magiccode.tradeingestion.listener;

import com.magiccode.tradeingestion.model.Deal;
import com.magiccode.tradeingestion.repository.DealRepository;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DealMessageListenerIntegrationTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private DealRepository dealRepository;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        dealRepository.deleteAll();

        // Create a test deal
        testDeal = new Deal(
            UUID.randomUUID(),
            "TEST-DEAL",
            "TEST-CP",
            "AAPL",
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            "USD",
            "NEW",
            1L,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void testDealMessageProcessing() throws JMSException {
        // Convert deal to JSON
        String dealJson = String.format(
            "{\"id\":\"%s\", \"dealId\":\"%s\", \"clientId\":\"%s\", " +
            "\"instrumentId\":\"%s\", \"quantity\":%s, \"price\":%s, \"currency\":\"%s\", " +
            "\"status\":\"%s\", \"version\":%d, \"dealDate\":\"%s\", \"createdAt\":\"%s\", \"updatedAt\":\"%s\", \"processedAt\":\"%s\"}",
            testDeal.getId(),
            testDeal.getDealId(),
            testDeal.getClientId(),
            testDeal.getInstrumentId(),
            testDeal.getQuantity(),
            testDeal.getPrice(),
            testDeal.getCurrency(),
            testDeal.getStatus(),
            testDeal.getVersion(),
            testDeal.getDealDate(),
            testDeal.getCreatedAt(),
            testDeal.getUpdatedAt(),
            testDeal.getProcessedAt()
        );

        // Send the message to the queue
        jmsTemplate.send("deals", (Session session) -> {
            TextMessage message = session.createTextMessage(dealJson);
            message.setStringProperty("messageType", "DEAL");
            return message;
        });

        // Wait for message processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify the deal was saved
        Deal savedDeal = dealRepository.findById(testDeal.getId()).orElse(null);
        assertNotNull(savedDeal, "Deal should be saved in the database");
        assertEquals(testDeal.getInstrumentId(), savedDeal.getInstrumentId(), "Instrument ID should match");
        assertEquals(testDeal.getQuantity(), savedDeal.getQuantity(), "Quantity should match");
        assertEquals(testDeal.getPrice(), savedDeal.getPrice(), "Price should match");
    }
} 