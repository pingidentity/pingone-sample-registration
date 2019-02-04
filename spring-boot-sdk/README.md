# PingOne for Customers Spring Boot SDK
The intention of this sample is to give developer a bird's eye view of the popular authentication protocol like OIDC and some PingOne for Customers API's.

This samples show some PingOne for Customers Authentication and Management API services usage, that allows you to manage your organization’s users and applications, and of course - users authorization and authentication.

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

Authorization Code
Implicit
Resource Owner Password Credentials
Client Credentials

### Authorization request customization

+ `acr_values` - A string that designates whether the authentication request includes steps for a single-factor or multi-factor authentication flow. The value specified must be the name of a sign-on policy for which the application has a sign-on policy assignment. 
The acr_values parameter values are sign-on policy names and should be listed in order of preference. Only scopes from one resource access grant can be minted in an access token (except for scopes for the OpenID Connect platform resource).
+ `prompt` - A string that specifies whether the user is prompted to login for re-authentication. The prompt parameter
 can be used as a way to check for existing authentication, verifying that the user is still present for the current session. For prompt=none, the user is never prompted to login to re-authenticate, which can result in an error if authentication is required. For prompt=login, if time since last login is greater than the max-age, then the current session is stashed away in the flow state and treated in the flow as if there was no previous existing session. When the flow completes, if the flow’s user is the same as the user from the stashed away session, the stashed away session is updated with the new flow data and persisted (preserving the existing session ID). If the flow’s user is not the same as the user from the stashed away session, the stashed away session is deleted (logout) and the new session is persisted.

### Spring Tips

__application.yml__
+ `authorizationGrantType` or `authorization-grant-type`: The OAuth 2.0 Authorization Framework defines four Authorization Grant types. The supported values are authorization_code, implicit, and client_credentials.

__pom.xml__
+ `<artifactId>spring-boot-devtools</artifactId>` - [set of tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html) that can make the application development experience a little more pleasant.
I.e: `spring.thymeleaf.cache` controls thymeleaf template engine compiled templates cache to avoid repeatedly parsing template files; applications automatically restart whenever files on the classpath change.

+
## Notes:
1. We are taking an advantage of [security auto-configuration Spring](https://docs.spring.io/spring-security-oauth2-boot/docs/current/reference/htmlsingle/) provides by using `spring-security-oauth2-autoconfigure`.
1. If you want to force logout in all sessions of a user then use `sessionRegistry.getAllSessions().expireNow();`.
This requires `ConcurrentSessionFilter` (or any other filter in the chain), that checks SessionInformation and calls all logout handlers and then do redirect.
2. Thymeleaf LEGACYHTM5 configuration (`spring.thymeleaf.mode=LEGACYHTML5`) will allow you to use more casual HTML5 tags if you want to. Otherwise, Thymeleaf will be very strict and may not parse your HTML. For instance, if you do not close an input tag, Thymeleaf will not parse your HTML.
3. If you want to have some custom validation logic on certain input( for example password ), you can check out [@ValidPassword](/src/main/java/com/pingidentity/spring/example/vaidators/ValidPassword.java) annotation.
4. We use pattern attribute for password input HTML elements. It allows us to define our own rule to validate the input value using Regular Expressions ([RegEx](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions)). It works on most browsers - those that support JavaScript 1.5 (Firefox, Chrome, Safari, Opera 7 and Internet Explorer 8 and higher), but very old browsers may not recognise these patterns.


For more information please check [Spring Security Reference about OAuth 2.0 Client](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2client).
