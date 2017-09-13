package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.repository.springdata;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;

import org.springframework.data.repository.CrudRepository;

public interface RateLimiterRepository extends CrudRepository<Rate, String> {

}
