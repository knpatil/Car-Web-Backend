package com.kpatil.pricing.repository;

import com.kpatil.pricing.entity.Price;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends CrudRepository<Price, Long> {

}