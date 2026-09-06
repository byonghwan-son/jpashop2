package jpabook.jpashop2.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jpabook.jpashop2.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * **쿼리 방식 선택 권장 순서**
 * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
 * 2. 필요하면 페치 조인으로 성능을 최적화 한다. ➡️ 대부분의 성능 이슈가 해결된다.
 * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
 * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
 */
@Repository
@RequiredArgsConstructor
public class OrderRepository {

  private final EntityManager em;

  public Order save(Order order) {
    em.persist(order);
    return order;
  }

  public Order findOne(Long id) {
    return em.find(Order.class, id);
  }

  // todo: 주문 내역 조회
  public List<Order> findAll(OrderSearch orderSearch) {
    String jpql = "select o From Order o join fetch o.member m";
    boolean isFirstCondition = true;
    //주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      jpql += " where";
      isFirstCondition = false;
      jpql += " o.status = :status";
    }
    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      if (isFirstCondition) {
        jpql += " where";
      } else {
        jpql += " and";
      }
      jpql += " m.name like :name";
    }

    TypedQuery<Order> query = em.createQuery(jpql, Order.class)
        .setMaxResults(1000); //최대 1000건

    if (orderSearch.getOrderStatus() != null) {
      query = query.setParameter("status", orderSearch.getOrderStatus());
    }

    if (StringUtils.hasText(orderSearch.getMemberName())) {
      query = query.setParameter("name", orderSearch.getMemberName());
    }

    return query.getResultList();
  }

  public List<Order> findAllWithMemberDelivery() {
    return em.createQuery(
        "select o from Order o" +
            " join fetch o.member m" +
            " join fetch o.delivery d", Order.class
    ).getResultList();
  }


}
