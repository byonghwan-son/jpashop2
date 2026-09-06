package jpabook.jpashop2.api;

import jpabook.jpashop2.api.data.WrappedResult;
import jpabook.jpashop2.domain.Address;
import jpabook.jpashop2.domain.Order;
import jpabook.jpashop2.domain.OrderItem;
import jpabook.jpashop2.domain.OrderStatus;
import jpabook.jpashop2.repository.OrderRepository;
import jpabook.jpashop2.repository.OrderSearch;
import jpabook.jpashop2.repository.order.query.OrderFlatDto;
import jpabook.jpashop2.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop2.repository.order.query.OrderQueryDto;
import jpabook.jpashop2.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제
 * <p>
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가
 * 능)
 * <p>
 * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
 * - 페이징 가능
 * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
 * - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능...
 *
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {

  private final OrderRepository orderRepository;
  private final OrderQueryRepository orderQueryRepository;

  /**
   * 엔티티 노출은 하지 말자.
   */
  @GetMapping("/api/v1/orders")
  public WrappedResult orderV1() {
    List<Order> all = orderRepository.findAll(new OrderSearch());
    for (Order order : all) {
      order.getMember().getName();
      order.getDelivery().getAddress();

      List<OrderItem> orderItems = order.getOrderItems();
      orderItems.stream().forEach(o -> o.getItem().getName());
    }

    return new WrappedResult(all.size(), all);
  }

  @GetMapping("/api/v2/orders")
  public WrappedResult orderV2() {
    List<Order> orders = orderRepository.findAll(new OrderSearch());
    List<OrderDto> result = orders.stream()
        .map(OrderDto::new)
        .collect(toList());
    return new WrappedResult(result.size(), result);
  }

  @GetMapping("/api/v3/orders")
  public WrappedResult orderV3() {
    List<Order> orders = orderRepository.findAllWithFetchJoin(new OrderSearch());
    List<OrderDto> result = orders.stream()
        .map(OrderDto::new)
        .collect(toList());
    return new WrappedResult(result.size(), result);
  }

  @GetMapping("/api/v3.1/orders")
  public WrappedResult orderV3_page(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "10") int limit
  ) {
    List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
    List<OrderDto> result = orders.stream()
        .map(OrderDto::new)
        .collect(toList());
    return new WrappedResult(result.size(), result);
  }

  @GetMapping("/api/v4/orders")
  public WrappedResult orderV4() {
    List<OrderQueryDto> result = orderQueryRepository.findOrderQueryDtos();

    return new WrappedResult(result.size(), result);
  }

  @GetMapping("/api/v5/orders")
  public WrappedResult orderV5() {
    List<OrderQueryDto> result = orderQueryRepository.findAllByDto_optimization();

    return new WrappedResult(result.size(), result);
  }

  @GetMapping("/api/v6/orders")
  public WrappedResult orderV6() {
    List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

    List<OrderQueryDto> collect = flats.stream()
        .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
            mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
        )).entrySet().stream()
        .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
            e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
            e.getKey().getAddress(), e.getValue()))
        .collect(toList());

    return new WrappedResult(collect.size(), collect);
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
      name = order.getMember().getName();
      orderDate = order.getOrderDate();
      orderStatus = order.getStatus();
      address = order.getDelivery().getAddress();
      orderItems = order.getOrderItems().stream()
          .map(OrderItemDto::new)
          .collect(toList());
    }
  }

  @Data
  static class OrderItemDto {
    private String itemName;
    private int orderPrice;
    private int count;

    public OrderItemDto(OrderItem orderItem) {
      itemName = orderItem.getItem().getName();
      orderPrice = orderItem.getOrderPrice();
      count = orderItem.getCount();
    }
  }
}
