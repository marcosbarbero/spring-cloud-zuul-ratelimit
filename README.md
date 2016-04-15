Spring Cloud Zuul RateLimit
---
Module to enable rate limit per service in Netflix Zuul

Usage
---
```
zuul:
  ratelimit:
    enabled: true #default false
    policies:
      the-service-id:
        limit: 10
        refresh-interval: 60 #default value (in seconds)
        types: # if none is set the policy will be applied at service level
          - user #use the authenticated username or 'anonymous'
          - origin #use the user origin request
```

Any doubt open an [issue](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/issues).  
Any fix send me a [Pull Request](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/pulls).
