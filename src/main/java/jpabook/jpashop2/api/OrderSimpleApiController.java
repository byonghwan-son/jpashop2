package jpabook.jpashop2.api;

import jpabook.jpashop2.domain.Address;
import jpabook.jpashop2.domain.Order;
import jpabook.jpashop2.domain.OrderStatus;
import jpabook.jpashop2.repository.OrderRepository;
import jpabook.jpashop2.repository.OrderSearch;
import jpabook.jpashop2.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop2.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xxxToOne (OneToOne, ManyToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

  private final OrderRepository orderRepository;
  private final OrderSimpleQueryRepository orderSimpleQueryRepository;

  @GetMapping("/api/v1/simple-orders")
  public List<Order> ordersV1() {
    return orderRepository.findAll(new OrderSearch());
  }

  /**
   * 엔티티를 DTO로 변환하는 일반적인 방법이다.
   * 쿼리가 총 1 + N + N번 실행된다. (v1과 쿼리수 결과는 같다.)
   * `order` 조회 1번(order 조회 결과 수가 N이 된다.)
   * `order -> member` 지연 로딩 조회 N 번
   * `order -> delivery` 지연 로딩 조회 N 번
   * 예) order의 결과가 4개면 최악의 경우 1 + 4 + 4번 실행된다.(최악의 경우)
   * 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략한다.
   */
  @GetMapping("/api/v2/simple-orders")
  public List<SimpleOrderDto> ordersV2() {
    List<Order> orders = orderRepository.findAll(new OrderSearch());
    return orders.stream()
        .map(SimpleOrderDto::new)
        .collect(Collectors.toList());
  }

  /**
   * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
   * 페치 조인으로 `order -> member` , `order -> delivery` 는 이미 조회 된 상태 이므로 지연로딩X
   */
  @GetMapping("/api/v3/simple-orders")
  public List<SimpleOrderDto> orderV3() {
    List<Order> orders = orderRepository.findAllWithMemberDelivery();
    return orders.stream()
        .map(SimpleOrderDto::new)
        .collect(Collectors.toList());
  }

  /**
   * 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
   * `new` 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
   * SELECT 절에서 원하는 데이터를 직접 선택하므로 DB ➡️ 애플리케이션 네트웍 용량 최적화(생각보다 미비)
   * 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
   */
  @GetMapping("/api/v4/simple-orders")
  public List<OrderSimpleQueryDto> orderV4() {
    return orderSimpleQueryRepository.findOrderDtos();
  }

  @Data
  static class SimpleOrderDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;


    public SimpleOrderDto(Order o) {
      orderId = o.getId();
      name = o.getMember().getName();
      orderDate = o.getOrderDate();
      orderStatus = o.getStatus();
      address = o.getDelivery().getAddress();
    }
  }
}
