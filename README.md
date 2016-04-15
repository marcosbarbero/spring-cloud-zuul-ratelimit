Spring Cloud Zuul RateLimit
---
Module to enable rate limit per service in Netflix Zuul.  
There are four built-in rate limit approachs:
 - Authenticated User: *Use the authenticated username or 'anonymous'*
 - Origin: *Use the user origin request*
 - Authenticated User and Origin IP: *Combine the authenticated user and de Origin*
 - Global configuration per service: *This one doesn't validate the request Origin or the Authenticated User*
   - To use this approach just don't set param 'type'

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
        type: #optional
          - user 
          - origin 
```

Any doubt open an [issue](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/issues).  
Any fix send me a [Pull Request](https://github.com/marcosbarbero/spring-cloud-starter-zuul-ratelimit/pulls).
