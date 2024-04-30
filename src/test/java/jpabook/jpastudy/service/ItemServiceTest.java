package jpabook.jpastudy.service;

import jpabook.jpastudy.domain.item.Item;
import jpabook.jpastudy.exception.NotEnoughStockException;
import jpabook.jpastudy.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepository itemRepository;

    @Test
    public void 아이템_저장() {
        // given
        Item item = new Item();
        item.setName("아이템 A");
        item.setPrice(1000);
        item.setStockQuantity(20);

        // when
        Long saveId = itemService.saveItem(item);

        // then
        assertEquals(item, itemService.findOne(saveId));
    }

    @Test
    public void 재화_증가() {
        // given
        Item item = new Item();
        item.setName("아이템 A");
        item.setPrice(1000);
        item.setStockQuantity(20);

        // when
        item.addStock(10);

        // then
        assertEquals(item.getStockQuantity(), 30);
    }

    @Test
    public void 재화_감소() {
        // given
        Item item = new Item();
        item.setName("아이템 A");
        item.setPrice(1000);
        item.setStockQuantity(20);

        // when
        item.removeStock(10);

        // then
        assertEquals(item.getStockQuantity(), 10);
    }

    @Test(expected = NotEnoughStockException.class)
    public void 재화_감소_예외() throws Exception {
        // given
        Item item = new Item();
        item.setName("아이템 A");
        item.setPrice(1000);
        item.setStockQuantity(20);

        // when
        item.removeStock(30);

        // then
        fail("예외가 발생해야 한다");
    }
}