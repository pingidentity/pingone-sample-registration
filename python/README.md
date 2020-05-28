# Client Registration Python Sample Guide

This sample demonstrates how to:
 - **register a new user**
 - **update user password by application itself**
 - **recover a forgotten password**
 
 using PingOne for Customers (Ping14C) [Authentication](https://apidocs.pingidentity.com/pingone/customer/v1/api/guide/p1-a_overview/)) and [Management API](https://apidocs.pingidentity.com/pingone/customer/v1/api/guide/p1_overview/) services.

It uses `client_credentials` grant type to obtain an access token, thereby it bypasses the authentication flow steps and call the `/{environmentId}/as/token` endpoint directly to acquire the token.

# Content
- [Prerequisites](#prerequisites)
- [Setup & Running](#setup--running)
- [Libraries Used](#libraries-used)
- [Developer Notes](#developer-notes)

# Prerequisites
You will need the following things:
- PingOne for Customers Account  - If you don’t have an existing one, please register for a Free Trial Account here: https://developer.pingidentity.com
- A Worker application instance. Instructions for 
creating one can be found [here](https://apidocs.pingidentity.com/pingone/customer/v1/api/guide/p1_gettingStarted/#Configure-an-application-connection). 
- To have installed [Python 3](https://www.python.org/downloads/)

# Setup & Running
1. Copy this source code: `git clone https://github.com/pingidentity/pingone-customers-sample-registration.git`, then enter the python directory with `cd python`

2. If you have already different python projects, try to keep their dependencies separate by creating isolated python virtual environments for them.
Otherwise, you can skip this step.
    So, if you don't use an IDE that is able to configure a virtual environment ([virtualenv](http://www.virtualenv.org/en/latest/index.html)), then you can create one with: `python3 -m venv _venv`
    where `_venv` is a path to a new virtual environment
    Once a virtual environment has been created, it then should be “activated” via: `source _venv/bin/activate`

3. Install all requirements using: `pip3 install -r requirements.txt`

4. Grab the following Worker application configuration information from the admin console to replace placeholders in [config.cfg](config.cfg) with it: **ENVIRONMENT_ID**, **CLIENT_ID**, **CLIENT_SECRET** , **OAUTH_REDIRECT_PATH**
5. Start an application using: `python3 app.py`

## Libraries Used
- [Flask Message Flashing](http://flask.pocoo.org/docs/1.0/patterns/flashing/)
- [Requests-OAuthlib](https://requests-oauthlib.readthedocs.io/en/latest/index.html)
- [PyJWT](https://pyjwt.readthedocs.io/en/latest/usage.html)

## Developer Notes
###  SSL Layer
1. Since OAuth2 works through an SSL layer, if your server is not configured for HTTPS, the `fetch_token` method will raise an [oauthlib.oauth2.rfc6749.errors.InsecureTransportError](https://requests-oauthlib.readthedocs.io/en/latest/examples/real_world_example.html). 
So, while testing, you can disable the OAuth2 check by uncommenting the `os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = 'true'` in [app.py](app.py). 
For more information check [this](http://requests-oauthlib.readthedocs.org/en/latest/examples/real_world_example.html) article.
1. For simplicity, "on the fly" certificates are used, which are useful to quickly serve an application over HTTPS without having to deal with certificates: ``ssl_context='adhoc'``. But note that each time the server runs, a different certificate is generated on the fly through pyOpenSSL.
You can generate a self-signed certificate (e.g. with openssl) and by setting the `ssl_context` argument in app.run() to a tuple with the filenames of the certificate and private key files that will be the same every time you launch your server
for production please use real certificates

### CSRF Protection
CSRF protection is enabled globally in this sample.  It requires a secret key to securely sign the token. By default this will use the Flask app's `SECRET_KEY`. If you'd like to use a separate token you can set `WTF_CSRF_SECRET_KEY`.

### Code Convention
To check coding conventions for the Python code see [Style Guide for Python Code](https://www.python.org/dev/peps/pep-0008/)
 
### Flask Important Notes 
1. `SECRET_KEY` is critical in applications [config.cfg](config.cfg): this variable needs to exist in our config for sessions to function properly. 
1. Unlike cookie-based sessions, Flask sessions are handled and stored on the server-side. A session object is simply a dict which is accessible throughout the application a global level, referring to a single 'logged-in user'.
1. Flask will suppress any server error with a generic error page unless it is in debug mode. As such to enable just the interactive debugger without the code reloading, you have to invoke run() with `debug=True` and `use_reloader=False`. 
Setting `use_debugger` to True without being in debug mode won’t catch any exceptions because there won’t be any to catch.
1. If you have seen an error like `“OSError: [Errno 8] Exec format error”` when running `python3 app.py` in debug mode (that is by default), then just add the following line (shebang) at the top of app.py:
`#!/usr/bin/env python3` and make the file executable: `chmod +x flaskblog.py`. Then try again `python3 app.py`.
    This may be because Flask is trying to run `app.py` directly on your system rather than with the python binary `python3 app.py`, that is not working since `app.py` isn't executable.
1. Comment `ENV='development'` in [config.cfg](config.cfg) to disable [Debug Mode](http://flask.pocoo.org/docs/1.0/quickstart/#debug-mode).
