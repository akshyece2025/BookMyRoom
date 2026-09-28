package com.app.guesthouse.bookmyroom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Checks that bad requests get clean HTTP error codes instead of stack traces. */
@SpringBootTest
class ApiErrorTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listRoomsReturns200() throws Exception {
        mvc.perform(get("/api/rooms")).andExpect(status().isOk());
    }

    @Test
    void unknownRoomReturns404() throws Exception {
        mvc.perform(get("/api/rooms/999999")).andExpect(status().isNotFound());
    }

    @Test
    void invalidRoomReturns400() throws Exception {
        String body = "{\"roomNumber\":\"\",\"type\":\"DOUBLE\",\"capacity\":0,\"ratePerNight\":-5}";
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void badMonthReturns400() throws Exception {
        mvc.perform(get("/api/reports/occupancy").param("month", "October"))
                .andExpect(status().isBadRequest());
    }
}
