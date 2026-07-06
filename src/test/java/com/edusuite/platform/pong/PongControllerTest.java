package com.edusuite.platform.pong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PongController.class)
@AutoConfigureMockMvc(addFilters = false)
class PongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PongService pongService;

    @Test
    void getPongReturnsReceivedCorrelationIds() throws Exception {
        when(pongService.getReceivedCorrelationIds()).thenReturn(List.of("a", "b", "c"));

        mockMvc.perform(get("/api/v1/public/pong"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("a")))
                .andExpect(content().string(containsString("b")))
                .andExpect(content().string(containsString("c")));
    }

    @Test
    void getPongReturnsEmptyListWhenNothingReceived() throws Exception {
        when(pongService.getReceivedCorrelationIds()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/public/pong"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
