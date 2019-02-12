# PingOne for Customers Spring Boot SDK
This SDK using PingOne for Customers (Ping14C) API with a help of spring boot framework allows you to:
- manage your organization’s users and applications
- implement users authorization and authentication 

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

## OAuth 2.0 Basics
 
### OAuth 2.0 roles:

+ Resource owner (the User) – An entity capable of granting access to a protected resource. When the resource owner is a person, it is referred to as an end-user.
+ Resource server (the API server) – The server hosting the protected resources, capable of accepting and responding to protected resource requests using access tokens.
+ Client – An application making protected resource requests on behalf of the resource owner and with its authorization.
+ Authorization server – The server issuing access tokens to the client after successfully authenticating the resource owner and obtaining authorization.

### OAuth 2.0 Grant Types:

When a client application wants access to the resources of a resource owner hosted on a resource server, the client application must first obtain an authorization grant.
OAuth2 provides several authorization grants. Each grant type serves a different purpose and is used in a different way. Depending on what type of service you are building, you might need to use one or more of these grant types to make your application work.

The grant types defined are:
- Authorization Code
- Implicit
- Resource Owner Password Credentials
- Client Credentials

### Authorization request customization

+ `acr_values` - A string that designates whether the authentication request includes steps for a single-factor or multi-factor authentication flow. The value specified must be the name of a sign-on policy for which the application has a sign-on policy assignment. 
The acr_values parameter values are sign-on policy names and should be listed in order of preference. Only scopes from one resource access grant can be minted in an access token (except for scopes for the OpenID Connect platform resource).
+ `prompt` - A string that specifies whether the user is prompted to login for re-authentication. The prompt parameter
 can be used as a way to check for existing authentication, verifying that the user is still present for the current session. For prompt=none, the user is never prompted to login to re-authenticate, which can result in an error if authentication is required. For prompt=login, if time since last login is greater than the max-age, then the current session is stashed away in the flow state and treated in the flow as if there was no previous existing session. When the flow completes, if the flow’s user is the same as the user from the stashed away session, the stashed away session is updated with the new flow data and persisted (preserving the existing session ID). If the flow’s user is not the same as the user from the stashed away session, the stashed away session is deleted (logout) and the new session is persisted.

## Spring Nuances

__CSRF Protection__
A general requirement for proper CSRF prevention is to ensure your website uses proper HTTP verbs: be certain that your application is using PATCH, POST, PUT, and/or DELETE for anything that modifies state. CSRF protection is enabled by default with our Java Configuration. 
Also, make sure you use the `_csrf` request attribute to obtain the current `CsrfToken` in your forms.
```
<input type="hidden"
    name="${_csrf.parameterName}"
    value="${_csrf.token}"/>
```


## Notes:
1. For more information about Spring OAuth2 Boot please check [Spring Security Reference about OAuth 2.0 Client](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2client).
2. If you want to force logout in all sessions of a user then use `sessionRegistry.getAllSessions().expireNow();`.
This requires `ConcurrentSessionFilter` (or any other filter in the chain), that checks SessionInformation and calls all logout handlers and then do redirect.
2. Thymeleaf LEGACYHTM5 configuration (`spring.thymeleaf.mode=LEGACYHTML5`) will allow you to use more casual HTML5 tags if you want to. Otherwise, Thymeleaf will be very strict and may not parse your HTML. For instance, if you do not close an input tag, Thymeleaf will not parse your HTML.
3. We use [`pattern`](https://html.spec.whatwg.org/multipage/input.html#the-pattern-attribute) attribute for password input HTML elements. It allows us to define our own rule to validate the input value using Regular Expressions ([RegEx](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions)). It works on most browsers - those that support JavaScript 1.5 (Firefox, Chrome, Safari, Opera 7 and Internet Explorer 8 and higher), but very old browsers may not recognise these patterns.
One thing that would be good to know about here is a [lookahead assertion](https://www.rexegg.com/regex-disambiguation.html#lookarounds)(`(?= … )`groups) 


