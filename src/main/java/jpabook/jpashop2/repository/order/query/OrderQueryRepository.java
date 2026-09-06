package jpabook.jpashop2.repository.order.query;

import jakarta.persistence.EntityManager;
import jpabook.jpashop2.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

  private final EntityManager em;

  public List<OrderQueryDto> findOrderQueryDtos() {
    // N + 1 문제 발생
    List<OrderQueryDto> results = findOrders(); // 1조회 => N개 추출

    results.forEach(o -> {
      List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());  // N번 조회
      o.setOrderItems(orderItems);
    });

    return results;
  }

  private List<OrderItemQueryDto> findOrderItems(Long orderId) {
    return em.createQuery(
            "select new jpabook.jpashop2.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
        .setParameter("orderId", orderId)
        .getResultList();
  }

  private List<OrderQueryDto> findOrders() {
    return em.createQuery(
            "select new jpabook.jpashop2.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderQueryDto.class)
        .getResultList();
  }

  public List<OrderQueryDto> findAllByDto_optimization() {
    List<OrderQueryDto> results = findOrders(); // 1조회 => N개 추출

    Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(results));

    results.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

    return results;
  }

  private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
    List<OrderItemQueryDto> orderItems = em.createQuery(
            "select new jpabook.jpashop2.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
        .setParameter("orderIds", orderIds)
        .getResultList();

    return orderItems.stream()
        .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
  }

  private List<Long> toOrderIds(List<OrderQueryDto> results) {
    return results.stream()
        .map(OrderQueryDto::getOrderId)
        .collect(Collectors.toList());
  }

  public List<OrderFlatDto> findAllByDto_flat() {
    return em.createQuery("select new jpabook.jpashop2.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
            " from Order o" +
            " join o.member m" +
            " join o.delivery d" +
            " join o.orderItems oi" +
            " join oi.item i", OrderFlatDto.class)
        .getResultList();
  }
}
