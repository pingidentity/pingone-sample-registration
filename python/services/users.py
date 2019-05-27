from .configuration import Configuration
from urllib.parse import quote

__all__ = 'UsersManagement'

class UsersManagement(Configuration):

    def __init__(self, oauth_session, app):
        """
        :param oauth_session: instance of OAuth 2 extension to :class:`requests_oauthlib.OAuth2Session`.
        :param app: flask application
        """
        Configuration.__init__(self, app.config)
        self.oauth_session = oauth_session

    def create(self, recovery_code, username, population_id):
        headers = {
            'Content-type': 'application/json'
        }
        data = {
            'email': recovery_code,
            'username': username,
            'population': {
                'id': population_id
            }
        }
        response = self.oauth_session.post('{}/users'.format(self.environment_url),
                                           json=data, headers=headers)
        response.raise_for_status() # throw exception if request does not return 2xx
        return response

    def delete(self, user_id):
        response = self.oauth_session.delete('{}/users/{}'.format(self.environment_url, user_id))
        response.raise_for_status()
        return response

    def find(self, username):
        response = self.oauth_session.get(
            '{}/users?filter={}'.format(self.environment_url,
                                        quote('email eq "{}" or username eq "{}"'.format(username, username))))
        response.raise_for_status()
        return response
