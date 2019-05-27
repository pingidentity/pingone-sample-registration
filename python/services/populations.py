from .configuration import Configuration
__all__ = 'PopulationsManagement'


class PopulationsManagement(Configuration):

    def __init__(self, oauth_session, app):
        """
        :param oauth_session: instance of OAuth 2 extension to :class:`requests_oauthlib.OAuth2Session`.
        :param app: flask application
        """
        Configuration.__init__(self, app.config)
        self.oauth_session = oauth_session

    def get_all(self):
        response = self.oauth_session.get(self.environment_url + '/populations')
        response.raise_for_status()  # throw exception if request does not return 2xx
        return response
