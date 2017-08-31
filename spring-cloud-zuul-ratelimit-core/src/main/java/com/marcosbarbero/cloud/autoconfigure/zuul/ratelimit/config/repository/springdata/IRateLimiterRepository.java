package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRateLimiterRepository extends CrudRepository<Rate, String> {

}
