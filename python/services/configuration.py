class Configuration(object):

    def __init__(self, config):
        self.api_uri = config.get('API_URL')
        self.authentication_uri = config.get('AUTH_URL')
        self.environment_id = config.get('ENVIRONMENT_ID')
        self.client_id = config.get('CLIENT_ID')
        self.client_secret = config.get('CLIENT_SECRET')
        self.grant_type = config.get('GRANT_TYPE')
        self.redirect_path = config.get('OAUTH_REDIRECT_PATH')
        self.prompt = config.get('PROMPT')
        self.max_age = config.get('MAX_AGE')
        self.logout_uri = config.get('LOGOUT_URI')

        self.environment_url = '{}environments/{}'.format(self.api_uri, self.environment_id)
        # Client authentication methods supported by the token endpoint.
        # Options are none, client_secret_basic, and client_secret_post.
        self.tokenEndpointAuthMethod = config.get('TOKEN_AUTH_METHOD') or 'client_secret_basic'
        # Whether to send the `client_id` in the body of the upstream request. This is required
        # if the client is not authenticating with the authorization server as described in
        # `Section 3.2.1`: https://tools.ietf.org/html/rfc6749#section-3.2.1
        self.include_client_id = self.tokenEndpointAuthMethod != 'client_secret_basic'
