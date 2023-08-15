# Client Registration Java Sample Guide
The intention of this sample is to give developer a bird's eye view of the popular authentication protocol like OIDC 
and some PingOne Authentication and Management API services usage, that allows you to manage your organization’s users and applications, and of course - users authorization and authentication.

It samples such flows like - **register a new user**, **update user password** by logged in user or by application itself, and **recover a forgotten password** scenario.

## Implementation Tutorial Video

A tutorial video detailing the implementation of this sample application is available on YouTube: [https://youtu.be/PbtvtXv3ZnE](https://youtu.be/PbtvtXv3ZnE)

## Installation Steps
1. Add PingOne `spring-boot-sdk` artifact to your pom:
```xml
    <dependency>
      <groupId>com.pingidentity.samples</groupId>
      <artifactId>spring-boot-sdk</artifactId>
      <version>${sdk.version}</version>
    </dependency>
```
You may want to add additional dependency to make your application development experience a little more pleasant, like [`<artifactId>spring-boot-devtools</artifactId>`](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html)
Since we are using Thymeleaf template engine, you can benefit from `spring.thymeleaf.cache` that controls compiled templates cache to avoid repeatedly parsing template files.

2. Until we are storing `spring-boot-sdk` jar in GitHub with [GitHub Maven Plugins](https://github.com/github/maven-plugins)(that should not be a case until at least [October of 2019](https://rawgit.com/)), please add this server configuration to your maven `settings.xml`:
```xml
<server>
  <id>github</id>
  <password>OAUTH2TOKEN</password>
</server>
```
where `OAUTH2TOKEN` is a [personal access token](https://help.github.com/en/articles/creating-a-personal-access-token-for-the-command-line) you need to create (unless you have some) if you have Two-factor Authentication, or
```xml
<server>
  <id>github</id>
  <username>GitHubLogin</username>
  <password>GitHubPassw0rd</password>
</server>
```
in a simple user:password case.

Please don't forget to set `OAUTH2TOKEN` as environment variable(if you are using it) for login failures prevention:
```bash
 export GITHUB_OAUTH_TOKEN={OAUTH2TOKEN}
``` 

3. Create two [applications](https://apidocs.pingidentity.com/pingone/platform/v1/api/#application-management) through PingOne admin console with the following configurations:
 - __Worker__ Application with default options.
    This application type supports only the *OPENID_CONNECT* protocol. Note that this application (with `client_credentials` grant type) will inherit [the same roles as the user who creates the instance](https://apidocs.pingidentity.com/pingone/platform/v1/api/#post-create-application-oidc-protocol---worker-app). These roles can be edited after the application instance is created
    (see [Application role assignments](https://apidocs.pingidentity.com/pingone/platform/v1/api/#application-role-assignments) and [User role assignments](https://apidocs.pingidentity.com/pingone/platform/v1/api/#user-role-assignments)).
    
    When retrieving access tokens for __Worker__ application, the authorization service checks to make sure the user or client has at least one role assignment. If not, the token request is rejected. If at least one role assignment exists, the authorization service creates a token with no scopes except for the requested OIDC scopes. 
    When accessing platform APIs with this token, it retrieves the actor’s entitlements, which ensures that clients and users can access only the resources that their role assignments allow.
 
 - __Single Page or Web Application__ (with *Authorization Code* or *Implicit* Grant Type) with such list of OIDC and PingOne platform `scope`'s:
    - OIDC: `openid,profile,phone,email,address`
    - PingOne's : `p1:reset:userPassword` - to change user password by the user

Most of PingOne platform scopes are self-explanatory, but if you need more details about them please check ["Resouce Scopes"](https://apidocs.pingidentity.com/pingone/platform/v1/api/#resource-scopes) part.

4. Enable both applications in Ping14C admin console.

5. Configure your spring application configuration `application.yml` by replacing all `<...>` placeholders with the following information:
    - `<environment_id>` with your environment ID
    - __Worker Application__ configuration in `oauth2.client` path copying over data from corresponding application from Ping14C admin console:
      - `<client_credentials_client_id>` with your client id (in `client-id` variable)
      - `<client_credentials_client_secret>` with your client secret (in `client-secret` variable)
    - __Single Page or Web Application__ configuration in `spring.security.oauth2.client` path 
      - `<authorization_code_client_id>` with your client id (in `clientId` variable)
      - `<authorization_code_client_client_credentials_client_secret>` with your client secret (in `clientSecret` variable)
    - (optional) to enable __SSL__ change following properties `server.ssl.enabled: true`, `server.port: 8433` and fill certificate related params:
      - `<key store type>` the format used for the keystore (i.e PKCS12, JKS file)
      - `<path to key>` the path to the keystore containing the certificate
      - `<key store password>` the password used to generate the certificate
      - `<key alias>` the alias mapped to self-signed certificate
<br>_`TIP:`_ ["how to create self-signed SSL certificate"](https://oracle-base.com/articles/linux/create-self-signed-ssl-certificates)

6. Adjust other parameters to your needs:
    - `client-authentication-method` - The method used to authenticate the client with the PingOne as a Provider. The supported values are `basic` and `post`.
    - `authorization-grant-type` -  The OAuth 2.0 Authorization Framework defines four [Authorization Grant](https://tools.ietf.org/html/rfc6749#section-1.3) types. The supported values are `authorization_code`, `implicit`, and `client_credentials`.
    - `redirect-uri-template` - The client’s registered redirect URI that the PingOne Authorization Server redirects the end-user’s user-agent to after the end-user has authenticated and authorized access to the client.
        
        The default redirect URI template is `{baseUrl}/login/oauth2/code/{registrationId}`, where `registrationId` is a unique identifier for the client registration (in our case it is `pingidentity`).
    - `scope` - [PingOne self-management or OpenID Connect (OIDC)](https://apidocs.pingidentity.com/pingone/platform/v1/api/#access-services-through-scopes-and-roles) scopes 
        that can be specified for your *SPA or web application* only, since the *worker application* uses role assignments to determine the actions a user or client has access to perform. 
        
        Per [OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749#page-23) PingOne authorization server will ignore the scopes it does't support that is requested by the client. 
        If the issued access token scope is different from the one requested by the client, the authorization server will include the "scope" response parameter to inform the client of the actual scope granted.
                   



## PingOne API Endpoints used in this sample
### Authentication API:
|    Endpoint   |    Description   |
| ------------- |------------- |
| [`POST /{environmentId}/as/authorize`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#get-authorize-authorization_code) | Authorization request with a code grant (__spring__ uses under the hood). `prompt=login` parameter is used by default |
| [`POST /{environmentId}/as/token`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#post-authorize-authorization_code)  | Obtain an access token by presenting its authorization grant (__spring__ uses under the hood) |
| [`GET /{environmentID}/as/.well-known/openid-configuration`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#get-discovery-openid-configuration)  | Get OpenID Connect provider metadata document for the issuer (__spring__ uses under the hood) |
| [`GET /{environmentId}/as/userinfo`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#get-userinfo)  | Get token claims about the authenticated end user ( used for `Show User Information` button) ||

### Management API:
| Service Name  |    Endpoint   |    Description   |
| ------------- | ------------- |------------- |
| Populations   | [`GET /environments/{environmentId}/populations`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#get-read-all-populations)  | Get all populations for a new user registration |
| Password policies  | [`GET /environments/{environmentId}/passwordPolicies`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#get-read-all-password-policies)  | Get all password policies for an environment to get the default one. It will be used for password verification on the client side   |
| User password management  | [`PUT /environments/{environmentId}/users/{userId}/password`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#put-update-password-self)  | Update a password: self-change password update and administrative-change reset of user password |
|  | [`POST /environments/{environmentId}/users/{userId}/password`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#post-password-recover)  | Recover a forgotten password |
| Users | [`GET /environments/{environmentId}/users?filter=name.family%20eq%20%22Smith%22%20and%20name.given%20sw%20%22W%22`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#get-read-user-or-users)  | Find a user by his name or email for further usage of his ID |
|  | [`POST /environments/{environmentId}/users`](https://apidocs.pingidentity.com/pingone/platform/v1/api/#post-create-user)  | Create new user |


## Developer Tips

__application.yml__
+ `authorizationGrantType` or `authorization-grant-type`: OAuth 2.0 defines four authorization grant types, but Spring Boot supports only 3: authorization_code, implicit, and client_credentials.
+ `filterPopulationByDescription`: whether to do an automatic filtering of a population to which the user will belong to while registration.
That selection is based on combination of population description and user email domain. For example: if the user typed `user@example.com` as his email, then it will be registered in the first found population, that contains `example.com` text in its description. Also, population description can have a couple of domains (separated by comma), by which the search was be looking through: i.e `example.com, examples.com, new-example.com`.
Is set to false, by default. 


__pom.xml__
+ [`<artifactId>spring-boot-devtools</artifactId>`](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html): set of tools that can make the application development experience a little more pleasant.
+ [`<artifactId>spring-boot-sdk</artifactId>`](https://github.com/pingidentity/pingone-spring-boot-tools/tree/mvn-repo/com/pingidentity/samples/spring-boot-sdk): PingOne spring-boot SDK that is temporarily stored in github raw

