package interview.prep.unittests.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;
    private String eventName;
    private LocalDateTime eventDate;
    private String venue;
    private String seatNumber;
    private BigDecimal price;
    private LocalDateTime createdAt;
}
