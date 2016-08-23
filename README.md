Spring Cloud Zuul RateLimit
---
Module to enable rate limit per service in Netflix Zuul.  
There are four built-in rate limit approachs:
 - Authenticated User: *Use the authenticated username or 'anonymous'*
 - Request Origin: *Use the user origin request*
 - Authenticated User and Request Origin: *Combine the authenticated user and the Request Origin*
 - Global configuration per service: *This one doesn't validate the request Origin or the Authenticated User*
   - To use this approach just don't set param 'type'

Usage
---

Add the dependency on pom.xml
```
<dependency>
    <groupId>com.marcosbarbero.cloud</groupId>
    <artifactId>spring-cloud-zuul-ratelimit</artifactId>
    <version>1.0.1.RELEASE</version>
</dependency>
```

Sample configuration

```
zuul:
  ratelimit:
    enabled: true #default false
    policies:
      myServiceId:
        limit: 10
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user 
          - origin 
```

Any doubt open an [issue](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/issues).  
Any fix send me a [Pull Request](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/pulls).
