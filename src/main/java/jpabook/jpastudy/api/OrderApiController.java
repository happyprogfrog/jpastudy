package jpabook.jpastudy.api;

import jpabook.jpastudy.domain.Address;
import jpabook.jpastudy.domain.Order;
import jpabook.jpastudy.domain.OrderItem;
import jpabook.jpastudy.domain.OrderStatus;
import jpabook.jpastudy.repository.order.OrderRepository;
import jpabook.jpastudy.repository.order.OrderSearch;
import jpabook.jpastudy.repository.order.query.OrderFlatDto;
import jpabook.jpastudy.repository.order.query.OrderItemQueryDto;
import jpabook.jpastudy.repository.order.query.OrderQueryDto;
import jpabook.jpastudy.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // v1: 엔티티를 조회해서 그대로 반환
    @GetMapping("/api/v1/orders")
    public List<Order> getOrdersV1 () {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName(); // LAZY 강제 초기화
            order.getDelivery().getAddress(); // LAZY 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); // Lazy 강제 초기화
        }

        return all;
    }

    // v2: 엔티티 조회 후 DTO 변환
    @GetMapping("/api/v2/orders")
    public List<OrderDto> getOrdersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    // v3: 페치 조인으로 쿼리 수 최적화
    @GetMapping("/api/v3/orders")
    public List<OrderDto> getOrdersV3() {
        List<Order> orders = orderRepository.fineAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    // v3.1: 컬렉션 페이징과 한계 돌파
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> getOrdersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    // v4. JPA 에서 DTO 직접 조회
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> getOrdersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    // v5. JPA 에서 DTO 직접 조회 - 컬렉션 조회 최적화
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> getOrdersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    // v6. JPA 에서 DTO 직접 조회 - 플랫 데이터 최적화
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> getOrdersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
                .entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
            orderItems = order.getOrderItems().stream()
                    .map(o -> new OrderItemDto(o))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName; // 상품명
        private int orderPrice;  // 주문 가격
        private int count;       // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName(); // LAZY 초기화
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
