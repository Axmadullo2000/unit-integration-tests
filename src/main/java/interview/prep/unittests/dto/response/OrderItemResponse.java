package interview.prep.unittests.dto.response;

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
public class OrderItemResponse {
    private Long id;
    private String eventName;
    private LocalDateTime eventDate;
    private String venue;
    private String seatNumber;
    private BigDecimal price;
    private LocalDateTime createdAt;
}
