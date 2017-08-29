Spring Cloud Zuul RateLimit [![Build Status](https://travis-ci.org/marcosbarbero/spring-cloud-zuul-ratelimit.svg?branch=master)](https://travis-ci.org/marcosbarbero/spring-cloud-zuul-ratelimit) [![Coverage Status](https://coveralls.io/repos/github/marcosbarbero/spring-cloud-zuul-ratelimit/badge.svg?branch=master)](https://coveralls.io/github/marcosbarbero/spring-cloud-zuul-ratelimit?branch=master)
---
Module to enable rate limit per service in Netflix Zuul.  
There are five built-in rate limit approaches:
 - Authenticated User
   - Uses the authenticated username or 'anonymous'
 - Request Origin 
   - Uses the user origin request
 - URL
   - Uses the request path of the upstream service
 - Global configuration per service: 
   - This one does not validate the request Origin, Authenticated User or URI
   - To use this approach just don't set param 'type'
   
>Note: It is possible to combine Authenticated User, Request Origin and URL just adding 
multiple values to the list

Usage
---
>This project is available on maven central

Add the dependency on pom.xml
```
<dependency>
    <groupId>com.marcosbarbero.cloud</groupId>
    <artifactId>spring-cloud-zuul-ratelimit</artifactId>
    <version>1.2.0.RELEASE</version>
</dependency>
```

In case you are using Redis there will be needed to add the following dependency
```
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-redis</artifactId>
 </dependency>
```

In case you are using Consul there will be needed to add the following dependency
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul</artifactId>
</dependency>
```

Sample configuration
```
zuul:
  ratelimit:
    key-prefix: your-prefix #default value ${spring.application.name:rate-limit-application}
    enabled: true #default false
    repository: REDIS
    behind-proxy: true #default false
    policies:
      myServiceId:
        limit: 10
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user
          - origin
          - url
```

Available implementations
---
There are three implementations provided:  
 * `InMemoryRateLimiter` - uses ConcurrentHashMap as data storage
 * `ConsulRateLimiter` - uses [Consul](https://www.consul.io/) as data storage
 * `RedisRateLimiter` - uses [Redis](https://redis.io/) as data storage
 
### Using
|Property name| Value |Implementation|
|-------------|:-------:|--------------|
|zuul.ratelimit.repository|CONSUL|ConsulRateLimiter|
|zuul.ratelimit.repository|REDIS|RedisRateLimiter|
|zuul.ratelimit.repository|IN_MEMORY|InMemoryRateLimiter|

>Note: InMemoryRateLimiter is the default implementation if no repository is set it will be used by default.

Contributing
---
Spring Cloud Zuul Rate Limit is released under the non-restrictive Apache 2.0 license, and follows a very 
standard Github development process, using Github tracker for issues and merging pull requests into master. 
If you want to contribute even something trivial please do not hesitate, but follow the guidelines below.

### Adding Project Lombok Agent
This project uses [Project Lombok](http://projectlombok.org/features/index.html)
to generate getters and setters etc. Compiling from the command line this
shouldn't cause any problems, but in an IDE you need to add an agent
to the JVM. Full instructions can be found in the Lombok website. The
sign that you need to do this is a lot of compiler errors to do with
missing methods and fields.

### Code of Conduct
This project adheres to the Contributor Covenant 
[code of conduct](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/blob/master/docs/code-of-conduct.adoc). 
By participating, you are expected to uphold this code. Please report unacceptable behavior to marcos.hgb@gmail.com.

Footnote
---
Any doubt open an [issue](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/issues).  
Any fix send me a [Pull Request](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/pulls).
