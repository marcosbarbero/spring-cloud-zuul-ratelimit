= Spring Cloud Zuul RateLimit image:https://app.travis-ci.com/marcosbarbero/spring-cloud-zuul-ratelimit.svg?branch=master["Build Status", link="https://app.travis-ci.com/github/marcosbarbero/spring-cloud-zuul-ratelimit"] image:https://coveralls.io/repos/github/marcosbarbero/spring-cloud-zuul-ratelimit/badge.svg?branch=master["Coverage Status", link="https://coveralls.io/github/marcosbarbero/spring-cloud-zuul-ratelimit?branch=master"] image:https://maven-badges.herokuapp.com/maven-central/com.marcosbarbero.cloud/spring-cloud-zuul-ratelimit/badge.svg["Maven Central", link="https://maven-badges.herokuapp.com/maven-central/com.marcosbarbero.cloud/spring-cloud-zuul-ratelimit"]
:toc:

:imagesdir: ./assets/images

== Overview
Module to enable rate limit per service in Netflix Zuul.

There are five built-in rate limit approaches:

    * Authenticated User
    ** Uses the authenticated username or 'anonymous'
    * Request Origin
    ** Uses the user origin request
    * URL
    ** Uses the request path of the downstream service
    * URL Pattern
    ** Uses the request Ant path pattern to the downstream service
    * ROLE
    ** Uses the authenticated user roles
    * Request method
    ** Uses the HTTP request method
    * Request header
    ** Uses the HTTP request header
    * Global configuration per service:
    ** This one does not validate the request Origin, Authenticated User or URI
    ** To use this approach just don't set param 'type'

[NOTE]
====
It is possible to combine Authenticated User, Request Origin, URL, ROLE and Request Method just adding multiple values to the list 
====

== Usage

[NOTE]
====
Latest version: image:https://maven-badges.herokuapp.com/maven-central/com.marcosbarbero.cloud/spring-cloud-zuul-ratelimit/badge.svg["Maven Central",link="https://maven-badges.herokuapp.com/maven-central/com.marcosbarbero.cloud/spring-cloud-zuul-ratelimit"]
====

[NOTE]
====
If you are using Spring Boot version `1.5.x` you *MUST* use Spring Cloud Zuul RateLimit version `1.7.x`.
Please take a look at the
link:https://mvnrepository.com/artifact/com.marcosbarbero.cloud/spring-cloud-zuul-ratelimit[Maven Central] and pick the
latest artifact in this version line.
====

Add the dependency on pom.xml

[source, xml]
----
<dependency>
    <groupId>com.marcosbarbero.cloud</groupId>
    <artifactId>spring-cloud-zuul-ratelimit</artifactId>
    <version>${latest-version}</version>
</dependency>
----

Add the following dependency accordingly to the chosen data storage:


* Redis

[source, xml]
----
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
----

* Consul

[source, xml]
----
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-consul</artifactId>
</dependency>
----

* Spring Data JPA

[source, xml]
----
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
----

This implementation also requires a database table, bellow here you can find a sample script:

[source, sql]
----
CREATE TABLE rate (
  rate_key VARCHAR(255) NOT NULL,
  remaining BIGINT,
  remaining_quota BIGINT,
  reset BIGINT,
  expiration TIMESTAMP,
  PRIMARY KEY(rate_key)
);
----

* Bucket4j JCache

[source, xml]
----
<dependency>
     <groupId>com.github.vladimir-bukhtoyarov</groupId>
     <artifactId>bucket4j-core</artifactId>
</dependency>
<dependency>
     <groupId>com.github.vladimir-bukhtoyarov</groupId>
     <artifactId>bucket4j-jcache</artifactId>
</dependency>
<dependency>
     <groupId>javax.cache</groupId>
     <artifactId>cache-api</artifactId>
</dependency>
----

* Bucket4j Hazelcast (depends on Bucket4j JCache)

[source, xml]
----
<dependency>
     <groupId>com.github.vladimir-bukhtoyarov</groupId>
     <artifactId>bucket4j-hazelcast</artifactId>
</dependency>
<dependency>
     <groupId>com.hazelcast</groupId>
     <artifactId>hazelcast</artifactId>
</dependency>
----

* Bucket4j Infinispan (depends on Bucket4j JCache)

[source, xml]
----
<dependency>
     <groupId>com.github.vladimir-bukhtoyarov</groupId>
     <artifactId>bucket4j-infinispan</artifactId>
</dependency>
<dependency>
     <groupId>org.infinispan</groupId>
     <artifactId>infinispan-core</artifactId>
</dependency>
----

* Bucket4j Ignite (depends on Bucket4j JCache)

[source, xml]
----
<dependency>
     <groupId>com.github.vladimir-bukhtoyarov</groupId>
     <artifactId>bucket4j-ignite</artifactId>
</dependency>
<dependency>
     <groupId>org.apache.ignite</groupId>
     <artifactId>ignite-core</artifactId>
</dependency>
----

Sample YAML configuration
[source, yaml]
----
zuul:
  ratelimit:
    key-prefix: your-prefix
    enabled: true
    repository: REDIS
    behind-proxy: true
    add-response-headers: true
    deny-request:
      response-status-code: 404 #default value is 403 (FORBIDDEN)
      origins:
        - 200.187.10.25
        - somedomain.com
    default-policy-list: #optional - will apply unless specific policy exists
      - limit: 10 #optional - request number limit per refresh interval window
        quota: 1000 #optional - request time limit per refresh interval window (in seconds)
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user
          - origin
          - url
          - http_method
    policy-list:
      myServiceId:
        - limit: 10 #optional - request number limit per refresh interval window
          quota: 1000 #optional - request time limit per refresh interval window (in seconds)
          refresh-interval: 60 #default value (in seconds)
          type: #optional
            - user
            - origin
            - url
        - type: #optional value for each type
            - user=anonymous
            - origin=somemachine.com
            - url=/api #url prefix
            - role=user
            - http_method=get #case insensitive
            - http_header=customHeader
        - type:
            - url_pattern=/api/*/payment
----

Sample Properties configuration
[source, properties]
----
zuul.ratelimit.enabled=true
zuul.ratelimit.key-prefix=your-prefix
zuul.ratelimit.repository=REDIS
zuul.ratelimit.behind-proxy=true
zuul.ratelimit.add-response-headers=true

zuul.ratelimit.deny-request.response-status-code=404
zuul.ratelimit.deny-request.origins[0]=200.187.10.25
zuul.ratelimit.deny-request.origins[1]=somedomain.com

zuul.ratelimit.default-policy-list[0].limit=10
zuul.ratelimit.default-policy-list[0].quota=1000
zuul.ratelimit.default-policy-list[0].refresh-interval=60

# Adding multiple rate limit type
zuul.ratelimit.default-policy-list[0].type[0]=user
zuul.ratelimit.default-policy-list[0].type[1]=origin
zuul.ratelimit.default-policy-list[0].type[2]=url
zuul.ratelimit.default-policy-list[0].type[3]=http_method

# Adding the first rate limit policy to "myServiceId"
zuul.ratelimit.policy-list.myServiceId[0].limit=10
zuul.ratelimit.policy-list.myServiceId[0].quota=1000
zuul.ratelimit.policy-list.myServiceId[0].refresh-interval=60
zuul.ratelimit.policy-list.myServiceId[0].type[0]=user
zuul.ratelimit.policy-list.myServiceId[0].type[1]=origin
zuul.ratelimit.policy-list.myServiceId[0].type[2]=url

# Adding the second rate limit policy to "myServiceId"
zuul.ratelimit.policy-list.myServiceId[1].type[0]=user=anonymous
zuul.ratelimit.policy-list.myServiceId[1].type[1]=origin=somemachine.com
zuul.ratelimit.policy-list.myServiceId[1].type[2]=url_pattern=/api/*/payment
zuul.ratelimit.policy-list.myServiceId[1].type[3]=role=user
zuul.ratelimit.policy-list.myServiceId[1].type[4]=http_method=get
zuul.ratelimit.policy-list.myServiceId[1].type[5]=http_header=customHeader
----

Both 'quota' and 'refresh-interval', can be expressed with https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-conversion-duration[Spring Boot's duration formats]:

    * A regular long representation (using seconds as the default unit)
    * The standard ISO-8601 format used by java.time.Duration (e.g. PT30M means 30 minutes)
    * A more readable format where the value and the unit are coupled (e.g. 10s means 10 seconds)

== Available implementations

There are eight implementations provided:

[cols=2*, options="header"]
|===
|Implementation        | Data Storage

|ConsulRateLimiter     | https://www.consul.io/[Consul]

|RedisRateLimiter      | https://redis.io/[Redis]

|SpringDataRateLimiter | https://projects.spring.io/spring-data-jpa/[Spring Data]

|Bucket4jJCacheRateLimiter

.4+.^|https://github.com/vladimir-bukhtoyarov/bucket4j[Bucket4j]

|Bucket4jHazelcastRateLimiter

|Bucket4jIgniteRateLimiter

|Bucket4jInfinispanRateLimiter

|===

Bucket4j implementations require the relevant bean with `@Qualifier("RateLimit")`:

 * `JCache` - javax.cache.Cache
 * `Hazelcast` - com.hazelcast.map.IMap
 * `Ignite` - org.apache.ignite.IgniteCache
 * `Infinispan` - org.infinispan.functional.ReadWriteMap

== Common application properties

Property namespace: __zuul.ratelimit__

|===
|Property name| Values |Default Value

|enabled             |true/false                   |false
|behind-proxy        |true/false                   |false
|response-headers    |NONE, STANDARD, VERBOSE      |VERBOSE
|key-prefix          |String                       |${spring.application.name:rate-limit-application}
|repository          |CONSUL, REDIS, JPA, BUCKET4J_JCACHE, BUCKET4J_HAZELCAST, BUCKET4J_INFINISPAN, BUCKET4J_IGNITE| -
|deny-request        |link:./spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/properties/RateLimitProperties.java#L296[DenyRequest]| -
|default-policy-list |List of link:./spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/properties/RateLimitProperties.java#L190[Policy]| -
|policy-list         |Map of Lists of link:./spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/properties/RateLimitProperties.java#L82[Policy]| -
|postFilterOrder     |int                          |FilterConstants.SEND_RESPONSE_FILTER_ORDER - 10
|preFilterOrder      |int                          |FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER

|===

Deny Request properties

|===
|Property name| Values |Default Value

|origins              |list of origins to have the access denied | -
|response-status-code |the http status code to be returned on a denied request | 403 (FORBIDDEN)

|===

Policy properties:

|===
|Property name| Values |Default Value

|limit           |number of requests      |  -
|quota           |time of requests        |  -
|refresh-interval|seconds                 | 60
|type            | [ORIGIN, USER, URL, URL_PATTERN, ROLE, HTTP_METHOD, HTTP_HEADER] | []
|breakOnMatch    |true/false              |false

|===

== Further Customization

This section details how to add custom implementations

=== Key Generator

If the application needs to control the key strategy beyond the options offered by the type property then it can
be done just by creating a custom link:./spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/RateLimitKeyGenerator.java[`RateLimitKeyGenerator`] beanfootnote:[By declaring a new `RateLimitKeyGenerator`, you replace the `DefaultRateLimitKeyGenerator`.]
implementation adding further qualifiers or something entirely different:

[source, java]
----
  @Bean
  public RateLimitKeyGenerator ratelimitKeyGenerator(RateLimitProperties properties, RateLimitUtils rateLimitUtils) {
      return new DefaultRateLimitKeyGenerator(properties, rateLimitUtils) {
          @Override
          public String key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy) {
              return super.key(request, route, policy) + ":" + request.getMethod();
          }
      };
  }
----

=== Error Handling
This framework uses 3rd party applications to control the rate limit access and these libraries are out of control of this framework.
If one of the 3rd party applications fails, the framework will handle this failure in the
link:./spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/repository/DefaultRateLimiterErrorHandler.java[`DefaultRateLimiterErrorHandler`] class
which will log the error upon failure.

If there is a need to handle the errors differently, it can be achieved by defining a custom
link:./spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/config/repository/RateLimiterErrorHandler.java[`RateLimiterErrorHandler`]
beanfootnote:[By declaring a new `RateLimitErrorHandler`, you replace the `DefaultRateLimitErrorHandler`.], e.g:

[source, java]
----
  @Bean
  public RateLimiterErrorHandler rateLimitErrorHandler() {
    return new DefaultRateLimiterErrorHandler() {
        @Override
        public void handleSaveError(String key, Exception e) {
            // custom code
        }

        @Override
        public void handleFetchError(String key, Exception e) {
            // custom code
        }

        @Override
        public void handleError(String msg, Exception e) {
            // custom code
        }
    }
  }
----

=== Event Handling
If the application needs to be notified when a rate limit access was exceeded then it can be done by listening
to link:./spring-cloud-zuul-ratelimit-core/src/main/java/com/marcosbarbero/cloud/autoconfigure/zuul/ratelimit/support/RateLimitExceededEvent.java[`RateLimitExceededEvent`] event:

[source, java]
----
    @EventListener
    public void observe(RateLimitExceededEvent event) {
        // custom code
    }
----

== Contributing
Spring Cloud Zuul Rate Limit is released under the non-restrictive Apache 2.0 license, and follows a very
standard Github development process, using Github tracker for issues and merging pull requests into master.
If you want to contribute even something trivial please do not hesitate, but follow the guidelines below.

=== Code of Conduct
This project adheres to the Contributor Covenant
link:CODE_OF_CONDUCT.md[code of conduct].
By participating, you are expected to uphold this code. Please report unacceptable behavior to marcos.hgb@gmail.com.

=== Acknowledgement

image::jetbrains_logo.png[Jetbrains, 150, link="https://www.jetbrains.com/?from=spring-cloud-zuul-ratelimit"]

== Footnote
Any doubt open an https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/issues[issue].
Any fix send me a https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/pulls[Pull Request].
