package jpabook.jpastudy.service;

import jpabook.jpastudy.domain.item.Book;
import jpabook.jpastudy.domain.item.Item;
import jpabook.jpastudy.domain.item.Movie;
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
        Movie movie = new Movie();
        movie.setName("아이템 A");
        movie.setPrice(1000);
        movie.setStockQuantity(20);

        // when
        Long saveId = itemService.saveItem(movie);

        // then
        assertEquals(movie, itemService.findOne(saveId));
    }

    @Test
    public void 재화_증가() {
        // given
        Movie movie = new Movie();
        movie.setName("아이템 A");
        movie.setPrice(1000);
        movie.setStockQuantity(20);

        // when
        movie.addStock(10);

        // then
        assertEquals(movie.getStockQuantity(), 30);
    }

    @Test
    public void 재화_감소() {
        // given
        Movie movie = new Movie();
        movie.setName("아이템 A");
        movie.setPrice(1000);
        movie.setStockQuantity(20);

        // when
        movie.removeStock(10);

        // then
        assertEquals(movie.getStockQuantity(), 10);
    }

    @Test(expected = NotEnoughStockException.class)
    public void 재화_감소_예외() throws Exception {
        // given
        Movie movie = new Movie();
        movie.setName("아이템 A");
        movie.setPrice(1000);
        movie.setStockQuantity(20);

        // when
        movie.removeStock(30);

        // then
        fail("예외가 발생해야 한다");
    }
}