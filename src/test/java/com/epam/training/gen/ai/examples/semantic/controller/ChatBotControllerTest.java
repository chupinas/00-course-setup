package com.epam.training.gen.ai.examples.semantic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
public class ChatBotControllerTest {

    @Mock
    private ChatCompletionService chatCompletionService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatBotController chatBotController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatBotController).build();
    }

    @Test
    public void testGetChatbotResponse_ValidInput() throws Exception {
        // Arrange
        String userInput = "I want to find top-10 books about world history";

        ChatHistory history = new ChatHistory();
        history.addUserMessage(userInput);

        Kernel kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .build();

        InvocationContext invocationContext = InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withTemperature(1.0)
                        .build())
                .build();

        when(chatCompletionService.getChatMessageContentsAsync(history, kernel, invocationContext))
                .thenReturn(any());
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"Here are the top 10 books about world history...\"]");

        // Act & Assert
        mockMvc.perform(get("/chat")
                        .param("input", userInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.input", is(userInput)))
                .andExpect(jsonPath("$.response", is("[\"Here are the top 10 books about world history...\"]")));

        // Verify that the service was called
        verify(chatCompletionService, times(1)).getChatMessageContentsAsync(history, kernel, invocationContext);
    }

    @Test
    public void testGetChatbotResponse_EmptyInput() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/chat")
                        .param("input", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.input", is("")))
                .andExpect(jsonPath("$.response", is("[]")));

        // Verify that the service was not called for empty input
        verifyNoInteractions(chatCompletionService);
    }
}