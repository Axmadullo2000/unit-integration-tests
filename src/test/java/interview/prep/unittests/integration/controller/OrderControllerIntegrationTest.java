package interview.prep.unittests.integration.controller;

import interview.prep.unittests.dto.request.CreateOrderItemRequest;
import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderStatusRequest;
import interview.prep.unittests.entity.OrderStatus;
import interview.prep.unittests.helper.OrderItemTestDataHelper;
import interview.prep.unittests.helper.OrderTestDataHelper;
import interview.prep.unittests.helper.UserTestDataHelper;
import interview.prep.unittests.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        accessToken = registerDefaultUserAndGetToken();
    }

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrder {

        @Test
        @DisplayName("should create order with items")
        void shouldCreateOrderWithItems() throws Exception {
            // given
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest(2);

            // when/then
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.userId").isNumber())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].eventName").value("Rock Concert"))
                    .andExpect(jsonPath("$.items[0].seatNumber").value("A-101"))
                    .andExpect(jsonPath("$.items[1].seatNumber").value("A-102"));
        }

        @Test
        @DisplayName("should calculate total price correctly")
        void shouldCalculateTotalPrice() throws Exception {
            // given - items with different prices
            List<CreateOrderItemRequest> items = List.of(
                    OrderItemTestDataHelper.createCreateOrderItemRequest("A-1", new BigDecimal("100.00")),
                    OrderItemTestDataHelper.createCreateOrderItemRequest("A-2", new BigDecimal("150.00")),
                    OrderItemTestDataHelper.createCreateOrderItemRequest("A-3", new BigDecimal("50.00"))
            );
            CreateOrderRequest request = CreateOrderRequest.builder().items(items).build();

            // when/then
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalPrice").value(300.00))
                    .andExpect(jsonPath("$.items", hasSize(3)));
        }

        @Test
        @DisplayName("should return 400 for empty items")
        void shouldReturn400ForEmptyItems() throws Exception {
            // given
            CreateOrderRequest request = CreateOrderRequest.builder()
                    .items(Collections.emptyList())
                    .build();

            // when/then
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.items").exists());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // given
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest();

            // when/then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/orders")
    class GetMyOrders {

        @Test
        @DisplayName("should return only current user's orders")
        void shouldReturnOnlyCurrentUserOrders() throws Exception {
            // given - create order for current user
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest();
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // create order for another user
            String anotherToken = registerAndGetToken(UserTestDataHelper.createRegisterRequest("another@example.com"));
            mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(anotherToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // when/then - current user should only see their order
            mockMvc.perform(get("/api/orders")
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("should return empty list when no orders")
        void shouldReturnEmptyList() throws Exception {
            // when/then
            mockMvc.perform(get("/api/orders")
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderById {

        @Test
        @DisplayName("should return order when owned by user")
        void shouldReturnOrderWhenOwned() throws Exception {
            // given - create order
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest(2);
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            // when/then
            mockMvc.perform(get("/api/orders/{id}", orderId)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.items", hasSize(2)));
        }

        @Test
        @DisplayName("should return 401 when accessing other user's order")
        void shouldReturn401ForOtherUserOrder() throws Exception {
            // given - create order for current user
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest();
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            // register another user
            String anotherToken = registerAndGetToken(UserTestDataHelper.createRegisterRequest("another@example.com"));

            // when/then - another user tries to access
            mockMvc.perform(get("/api/orders/{id}", orderId)
                            .header("Authorization", bearerToken(anotherToken)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value(containsString("order")));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // when/then
            mockMvc.perform(get("/api/orders/{id}", 99999)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("PUT /api/orders/{id}")
    class UpdateOrder {

        @Test
        @DisplayName("should update order items")
        void shouldUpdateOrderItems() throws Exception {
            // given - create order with 1 item
            CreateOrderRequest createRequest = OrderTestDataHelper.createCreateOrderRequest(1);
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            // update with 3 items
            UpdateOrderRequest updateRequest = OrderTestDataHelper.createUpdateOrderRequest(3);

            // when/then
            mockMvc.perform(put("/api/orders/{id}", orderId)
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.items", hasSize(3)));
        }

        @Test
        @DisplayName("should recalculate total price")
        void shouldRecalculateTotalPrice() throws Exception {
            // given - create order with 1 item at $150
            CreateOrderRequest createRequest = OrderTestDataHelper.createCreateOrderRequest(1);
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalPrice").value(150.00))
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            // update with items totaling $300
            List<CreateOrderItemRequest> newItems = List.of(
                    OrderItemTestDataHelper.createCreateOrderItemRequest("B-1", new BigDecimal("200.00")),
                    OrderItemTestDataHelper.createCreateOrderItemRequest("B-2", new BigDecimal("100.00"))
            );
            UpdateOrderRequest updateRequest = UpdateOrderRequest.builder().items(newItems).build();

            // when/then
            mockMvc.perform(put("/api/orders/{id}", orderId)
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").value(300.00));
        }
    }

    @Nested
    @DisplayName("PATCH /api/orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status")
        void shouldUpdateOrderStatus() throws Exception {
            // given - create order (status = PENDING)
            CreateOrderRequest createRequest = OrderTestDataHelper.createCreateOrderRequest();
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            UpdateOrderStatusRequest statusRequest = OrderTestDataHelper.createUpdateOrderStatusRequest(OrderStatus.CONFIRMED);

            // when/then
            mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statusRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("should return 400 for invalid status")
        void shouldReturn400ForInvalidStatus() throws Exception {
            // given - create order
            CreateOrderRequest createRequest = OrderTestDataHelper.createCreateOrderRequest();
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            // when/then - send invalid status value
            mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"INVALID_STATUS\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/orders/{id}")
    class DeleteOrder {

        @Test
        @DisplayName("should delete order and its items")
        void shouldDeleteOrderAndItems() throws Exception {
            // given - create order with items
            CreateOrderRequest createRequest = OrderTestDataHelper.createCreateOrderRequest(3);
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            // when
            mockMvc.perform(delete("/api/orders/{id}", orderId)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNoContent());

            // then - verify order is deleted
            mockMvc.perform(get("/api/orders/{id}", orderId)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 401 when deleting other user's order")
        void shouldReturn401ForOtherUserOrder() throws Exception {
            // given - create order for current user
            CreateOrderRequest createRequest = OrderTestDataHelper.createCreateOrderRequest();
            String createResponse = mockMvc.perform(post("/api/orders")
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

            // register another user
            String anotherToken = registerAndGetToken(UserTestDataHelper.createRegisterRequest("another@example.com"));

            // when/then - another user tries to delete
            mockMvc.perform(delete("/api/orders/{id}", orderId)
                            .header("Authorization", bearerToken(anotherToken)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // when/then
            mockMvc.perform(delete("/api/orders/{id}", 99999)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNotFound());
        }
    }
}
