# PingOne for Customers User Registration Sample
The intention of this sample is to give developer a bird's eye view of the popular authentication protocol like OIDC 
and some PingOne for Customers (Ping14C) Authentication and Management API services usage, that allows you to manage your organization’s users and applications, and of course - users authorization and authentication.

It samples such flows like - register a new user, update user password by logged in user or by application itself, and recover a forgotten password scenario.

## Installation Steps
1. Add Ping14C `spring-boot-sdk` artifact to your pom:
```xml
    <dependency>
      <groupId>com.pingidentity.samples</groupId>
      <artifactId>spring-boot-sdk</artifactId>
      <version>${sdk.version}</version>
    </dependency>
```
You may want to add additional dependency to make your application development experience a little more pleasant, like [`<artifactId>spring-boot-devtools</artifactId>`](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html)
Since we are using Thymeleaf template engine, you can benefit from `spring.thymeleaf.cache` that controls compiled templates cache to avoid repeatedly parsing template files.

2. Create two applications through Ping14C admin console with the following configurations:
 - Non-interactive or Web Application (for Client Credentials Application Type) with such list of PingOne platform `scope`'s:
    - `p1:create:env:user` - to create a new user
    - `p1:read:env:user` - to find a user by email or name before resetting its password
    - `p1:delete:env:user` - to delete a user when its passwords doesn't meet password policy requirements and application stop the registration process. User creation and password setting are 2 different endpoints. 
    - `p1:read:env:population` - to get environment population a new user will belong to (registration flow) 
    - `p1:set:env:userPassword` - to set user password on new user registration
    - `p1:read:env:passwordPolicy` - to get default password policy for password client side verification
    - `p1:recover:env:userPassword` - to recover a forgotten password
 
 - Native or Web Application (for Authorization Code Application Type) with such list of OIDC and PingOne platform `scope`'s:
    - OIDC: `openid,profile,phone,email,address`
    - PingOne's : `p1:reset:self:userPassword`, `p1:set:env:userPassword` - to change user password by the user

Most of PingOne platform scopes are self-explanatory, but if you need more details about them please check ["Configure access through scopes"](https://apidocs.pingidentity.com/pingone/customer/v1/api/auth/p1-a_AuthActivities/p1-a_AccessServices/) part.

3. Enable both applications in Ping14C admin console.

4. Configure your spring application configuration `application.yml` by replacing all `<...>` placeholders with the following information:
    - your environment ID in `ping.environmentId`
    - Non-interactive (or Web) Application configuration in `oauth2.client` path copying over data from corresponding application from Ping14C admin console:
      - `clientId`
      - `clientSecret`
      - `accessTokenUri`
      - `userAuthorizationUri`
      - `scopes` (adjust it to your use cases)
    - Native (or Web) Application configuration in `spring.security.oauth2.client` path 
      - `spring.security.oauth2.client.registration.pingidentity`: `client-id`,`client-secret`, `provider` (can be renamed to other one, but should be correlative with `spring.security.oauth2.client.provider` name)
      - `spring.security.oauth2.client.provider.pingidentity`: `issuer-uri`


## Developer Tips

__application.yml__
+ `authorizationGrantType` or `authorization-grant-type`: OAuth 2.0 defines four authorization grant types, but Spring Boot supports only 3: authorization_code, implicit, and client_credentials.

__pom.xml__
+ [`<artifactId>spring-boot-devtools</artifactId>`](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html) - set of tools that can make the application development experience a little more pleasant.

