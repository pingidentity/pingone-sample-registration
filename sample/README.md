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
    - PingOne's : `p1:reset:userPassword`, `p1:set:env:userPassword` - to change user password by the user

Most of PingOne platform scopes are self-explanatory, but if you need more details about them please check ["Configure access through scopes"](https://apidocs.pingidentity.com/pingone/customer/v1/api/auth/p1-a_AuthActivities/p1-a_AccessServices/) part.

3. Enable both applications in Ping14C admin console.

4. Configure your spring application configuration `application.yml` by replacing all `<...>` placeholders with the following information:
    - `<environment_id>` with your environment ID
    - Non-interactive (or Web) Application configuration in `oauth2.client` path copying over data from corresponding application from Ping14C admin console:
      - `<client_credentials_client_id>` with your client id (in `client-id` variable)
      - `<client_credentials_client_secret>` with your client secret (in `client-secret` variable)
    - Native (or Web) Application configuration in `spring.security.oauth2.client` path 
      - `<authorization_code_client_id>` with your client id (in `clientId` variable)
      - `<authorization_code_client_client_credentials_client_secret>` with your client secret (in `clientSecret` variable)


## PingOne for Customers API used in this sample
### Authentication API:
|    Endpoint   |    Description   |
| ------------- |------------- |
| [`POST /{environmentId}/as/authorize`](https://apidocs.pingidentity.com/pingone/customer/v1/api/auth/p1-a_Authorize/#Authorization-request-with-a-code-grant)  | Authorization request with a code grant (__spring__ uses under the hood) |
| [`POST /{environmentId}/as/token`](https://apidocs.pingidentity.com/pingone/customer/v1/api/auth/p1-a_Authorize/#Obtain-an-access-token)  | Obtain an access token by presenting its authorization grant (__spring__ uses under the hood) |
| [`GET /{environmentID}/as/.well-known/openid-configuration`](https://apidocs.pingidentity.com/pingone/customer/v1/api/auth/p1-a_Authorize/#Obtain-OpenID-provider-configuration-information)  | Get OpenID Connect provider metadata document for the issuer (__spring__ uses under the hood) |
| [`GET /{environmentId}/as/userinfo`](https://apidocs.pingidentity.com/pingone/customer/v1/api/auth/p1-a_Authorize/#UserInfo-endpoint)  | Get token claims about the authenticated end user ( used for `Show User Information` button) ||

### Management API:
| Service Name  |    Endpoint   |    Description   |
| ------------- | ------------- |------------- |
| Populations   | [`GET /environments/{environmentId}/populations`](https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Populations/#Get-populations)  |Get all populations for a new user registration |
| Password policies  | [`GET /environments/{environmentId}/passwordPolicies`](https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Passwords/#Get-one-password-policy)  |Get all password policies for an environment to get the default one. It will be used for password verification on the client side   |
| User password management  | [`PUT /environments/{environmentId}/users/{userId}/password`](https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Users/p1_Password/#Update-a-users-password)  | Update a password: self-change password update and administrative-change reset of user password |
|  | [`POST /environments/{environmentId}/users/{userId}/password`](https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Users/p1_Password/#Recover-password)  | Recover a forgotten password |
| Users | [`GET /environments/{environmentId}/users?filter=name.family%20eq%20%22Smith%22%20and%20name.given%20sw%20%22W%22`](https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Users/#Users)  | Find a user by his name or email for further usage of his ID |
|  | [`POST /environments/{environmentId}/users`](https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Users/#Users)  | Create new user |


## Developer Tips

__application.yml__
+ `authorizationGrantType` or `authorization-grant-type`: OAuth 2.0 defines four authorization grant types, but Spring Boot supports only 3: authorization_code, implicit, and client_credentials.

__pom.xml__
+ [`<artifactId>spring-boot-devtools</artifactId>`](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html) - set of tools that can make the application development experience a little more pleasant.

