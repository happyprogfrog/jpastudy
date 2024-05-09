package jpabook.jpastudy.api;

import jpabook.jpastudy.domain.Address;
import jpabook.jpastudy.domain.Order;
import jpabook.jpastudy.domain.OrderStatus;
import jpabook.jpastudy.repository.order.OrderRepository;
import jpabook.jpastudy.repository.order.OrderSearch;
import jpabook.jpastudy.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpastudy.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // v1: 엔티티 직접 노출 -> Hibernate5Module 등록 필요
    @GetMapping("/api/v1/simple-orders")
    public List<Order> getOrdersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // LAZY 강제 초기화
            order.getDelivery().getAddress(); // LAZY 강제 초기화
        }

        return all;
    }

    // v2: 엔티티를 조회해서 DTO 변환 (지연로딩으로 최악의 경우 쿼리 1 + N + N 호출)
    @GetMapping("api/v2/simple-orders")
    public Result gerOrdersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());

        return new Result(result);
    }

    // v3: 엔티티를 조회해서 DTO 변환 (페치 조인 최적화로 쿼리 1번 호출)
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> getOrdersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());

        return result;
    }

    // v4: JPA 에서 DTO 로 바로 조회
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> getOrdersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
