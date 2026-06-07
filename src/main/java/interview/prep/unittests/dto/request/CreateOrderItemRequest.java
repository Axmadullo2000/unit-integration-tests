package interview.prep.unittests.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateOrderItemRequest {
    @NotBlank(message = "Event name is required")
    private String eventName;

    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
}
