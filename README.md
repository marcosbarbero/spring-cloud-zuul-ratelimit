Spring Cloud Zuul RateLimit
---
Module to enable rate limit per service in Netflix Zuul.  
There are four built-in rate limit approaches:
 - Authenticated User: *Uses the authenticated username or 'anonymous'*
 - Request Origin: *Uses the user origin request*
 - URL: *Uses the request path of the upstream service*
   - Can be combined with Authenticated User, Request Origin or both
 - Authenticated User and Request Origin: *Combines the authenticated user and the Request Origin*
 - Global configuration per service: *This one doesn't validate the request Origin or the Authenticated User*
   - To use this approach just don't set param 'type'

Adding Project Lombok Agent
---

This project uses [Project Lombok](http://projectlombok.org/features/index.html)
to generate getters and setters etc. Compiling from the command line this
shouldn't cause any problems, but in an IDE you need to add an agent
to the JVM. Full instructions can be found in the Lombok website. The
sign that you need to do this is a lot of compiler errors to do with
missing methods and fields.     
   
Usage
---
>This project is available on maven central

Add the dependency on pom.xml
```
<dependency>
    <groupId>com.marcosbarbero.cloud</groupId>
    <artifactId>spring-cloud-zuul-ratelimit</artifactId>
    <version>1.0.4.RELEASE</version>
</dependency>
```

Sample configuration

```
zuul:
  ratelimit:
    enabled: true #default false
    behind-proxy: true #default false
    policies:
      myServiceId:
        limit: 10
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user 
          - origin 
```

Footnote
---
This project currently works only with redis so in order to make this strategy work you'll need to have a redis up and running and configure it's connection.

Any doubt open an [issue](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/issues).  
Any fix send me a [Pull Request](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/pulls).
