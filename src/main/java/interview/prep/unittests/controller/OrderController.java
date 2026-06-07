package interview.prep.unittests.controller;

import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderStatusRequest;
import interview.prep.unittests.dto.response.OrderResponse;
import interview.prep.unittests.security.AuthenticatedUser;
import interview.prep.unittests.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthenticatedUser authenticatedUser;

    public OrderController(OrderService orderService, AuthenticatedUser authenticatedUser) {
        this.orderService = orderService;
        this.authenticatedUser = authenticatedUser;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = authenticatedUser.getCurrentUserId();
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        Long userId = authenticatedUser.getCurrentUserId();
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        Long userId = authenticatedUser.getCurrentUserId();
        OrderResponse order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateOrderRequest request) {
        Long userId = authenticatedUser.getCurrentUserId();
        OrderResponse order = orderService.updateOrder(id, userId, request);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateOrderStatusRequest request) {
        Long userId = authenticatedUser.getCurrentUserId();
        OrderResponse order = orderService.updateOrderStatus(id, userId, request);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        Long userId = authenticatedUser.getCurrentUserId();
        orderService.deleteOrder(id, userId);
        return ResponseEntity.noContent().build();
    }
}
