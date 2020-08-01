package com.kpatil.pricing.repository;

import com.kpatil.pricing.entity.Price;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PriceRepositoryTest {

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void testInitialDatabaseStatusShouldReturnEmptyList() {
        Iterable<Price> prices = priceRepository.findAll();
        assertThat(prices.iterator().hasNext()).isEqualTo(false);
    }

    @Test
    public void test_save_FindById_Update_Delete() {
        Price price = new Price(null, "USD", BigDecimal.valueOf(27999.98), 1L);
        price = priceRepository.save(price); // POST
        Long id = price.getId();
        Optional<Price> priceAdded = priceRepository.findById(id);
        assert priceAdded.isPresent();
        assertThat(priceAdded.get().getCurrency()).isEqualTo("USD");
        assertThat(priceAdded.get().getPrice()).isEqualTo(BigDecimal.valueOf(27999.98));

        price.setCurrency("EURO");
        price.setPrice(BigDecimal.valueOf(19999.00));

        price = priceRepository.save(price);  // PUT
        Optional<Price> updatedPrice = priceRepository.findById(id); // GET
        assert updatedPrice.isPresent();
        assertThat(updatedPrice.get().getCurrency()).isEqualTo("EURO");
        assertThat(updatedPrice.get().getPrice()).isEqualTo(BigDecimal.valueOf(19999.00));

        priceRepository.delete(price);  // DELETE

        Optional<Price> deletedPrice = priceRepository.findById(id); // GET
        assert deletedPrice.isEmpty();
    }
}