package jpabook.jpastudy.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpastudy.domain.Order;
import jpabook.jpastudy.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        // 검색로직...
        return null;
    }
}
