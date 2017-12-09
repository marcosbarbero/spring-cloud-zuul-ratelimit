Spring Cloud Zuul RateLimit [![Build Status](https://travis-ci.org/marcosbarbero/spring-cloud-zuul-ratelimit.svg?branch=master)](https://travis-ci.org/marcosbarbero/spring-cloud-zuul-ratelimit) 
[![Coverage Status](https://coveralls.io/repos/github/marcosbarbero/spring-cloud-zuul-ratelimit/badge.svg?branch=master)](https://coveralls.io/github/marcosbarbero/spring-cloud-zuul-ratelimit?branch=master) 
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
```xml
<dependency>
    <groupId>com.marcosbarbero.cloud</groupId>
    <artifactId>spring-cloud-zuul-ratelimit</artifactId>
    <version>1.3.4.RELEASE</version>
</dependency>
```

Add the following dependency accordingly to the chosen data storage: 

 1. Redis
  ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-redis</artifactId>
   </dependency>
  ```

 2. Consul
  ```xml
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul</artifactId>
  </dependency>
  ```

  3. Spring Data JPA
  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  ```
  
  4. InMemory  
  For `InMemory` implementation there's no need to add any extra dependency other than
   `com.marcosbarbero.cloud:spring-cloud-zuul-ratelimit` 

Sample configuration
```yaml
zuul:
  ratelimit:
    key-prefix: your-prefix 
    enabled: true 
    repository: REDIS 
    behind-proxy: true
    default-policy: #deprecated - please use "default-policy-list"
      limit: 10 #optional - request number limit per refresh interval window
      quota: 1000 #optional - request time limit per refresh interval window (in seconds)
      refresh-interval: 60 #default value (in seconds)
      type: #optional
        - user
        - origin
        - url
    default-policy-list: #optional - will apply unless specific policy exists
      - limit: 10 #optional - request number limit per refresh interval window
        quota: 1000 #optional - request time limit per refresh interval window (in seconds)
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user
          - origin
          - url
    policies: #deprecated - please use "policy-list"
      myServiceId:
        limit: 10 #optional - request number limit per refresh interval window
        quota: 1000 #optional - request time limit per refresh interval window (in seconds)
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user
          - origin
          - url
    policy-list:
      myServiceId:
        - limit: 10 #optional - request number limit per refresh interval window
          quota: 1000 #optional - request time limit per refresh interval window (in seconds)
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
 * `SpringDataRateLimiter` - uses [Spring Data](https://projects.spring.io/spring-data-jpa/) as data storage
 
Common application properties
---
Property namespace: __zuul.ratelimit__

|Property name| Values |Default Value|
|-------------|:-------|:-------------:|
|enabled       |true/false                   |false|
|behind-proxy  |true/false                   |false|
|key-prefix    |String                       |${spring.application.name:rate-limit-application}|
|repository    |CONSUL, REDIS, JPA, IN_MEMORY|IN_MEMORY|
|default-policy|[Policy](https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit/blob/master/spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/properties/RateLimitProperties.java#L64)| - |
|policies      |Map of [Policy](https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit/blob/master/spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/properties/RateLimitProperties.java#L64)| - |
|default-policy-list|List of [Policy](https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit/blob/master/spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/properties/RateLimitProperties.java#L64)| - |
|policy-list      |Map of Lists of [Policy](https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit/blob/master/spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/properties/RateLimitProperties.java#L64)| - |

Policy properties:

|Property name| Values |Default Value|
|-------------|:-------|:-------------:|
|limit           |number of calls      |  - |
|quota           |time of calls        |  - |
|refresh-interval|seconds              | 60 |
|type            | [ORIGIN, USER, URL] | [] |

Further Customization
---

If your application needs to control the key strategy beyond the options offered by the type property then you can 
supply a custom `RateLimitKeyGenerator` implementation adding further qualifiers or something entirely different:

```java
  @Bean
  public RateLimitKeyGenerator rateLimitKeyGenerator(final RateLimitProperties properties) {
      return new DefaultRateLimitKeyGenerator(properties) {
          @Override
          public String key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy) {
              return super.key(request, route, policy) + ":" + request.getMethod();
          }
      };
  }
```

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
