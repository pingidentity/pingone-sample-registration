import re
from .configuration import Configuration

__all__ = 'PasswordsManagement'

class PasswordsManagement(Configuration):

    def __init__(self, oauth_session, app):
        """
        :param oauth_session: instance of OAuth 2 extension to :class:`requests_oauthlib.OAuth2Session`.
        :param app: flask application
        """
        Configuration.__init__(self, app.config)
        self.oauth_session = oauth_session

    def get_password_pattern(self):
        password_pattern = None
        password_policies = self.oauth_session.get(self.environment_url + '/passwordPolicies')
        password_policies.raise_for_status()  # throw exception if request does not return 2xx
        default_password_policy = list(
            # Get password validation pattern from default password policy,
            filter(lambda policy: policy.get('default') is True
                                  and policy.get('minCharacters') and policy.get('maxRepeatedCharacters')
                                  # or standard one - if the default one doesn't have necessary properties( "minCharacters" and "maxRepeatedCharacters")
                                  or policy.get('name') == 'Standard',
                   password_policies.json()['_embedded']['passwordPolicies']))

        # Get password validation pattern(regex) based by policy
        # Example: ^(?:(?=(?:.*[ABCDEFGHIZ]){3,4})(?=(?:.*[123456890]){1,4})(?=.*[abcdefghijklmnopqrstuvwxyz])(?=(?:.*[~!@#\$%\^&\*\(\)\-_=\+\[\]\{\}\|;:,\.<>/\?]){1,4}))(?!.*(.)\1{3,}).{6,20}$
        # ^                         start of the password
        # (?:                       non-capturing group to assert the whole password phrase
        # (?=                       lookahead assertion for the following group of characters
        # (?:.*[ABCDEFGHIZ]){3, 4}) must contains from 3 to 4 uppercase characters
        #     .....
        # (?!.*(.)\1 {3, })         allow up to three repeated characters
        # .                         match anything with previous condition checking
        # {6, 20}                   length at least 6 characters and maximum of 20
        # $                         the end of the password
        #
        # NOTE: Unlike Standard C, all unrecognized escape sequences are left in the string unchanged, i.e., the backslash is left in the result.
        # See https://docs.python.org/3/reference/lexical_analysis.html#string-and-bytes-literals
        # See https://www.regular-expressions.info/python.html
        # See https://www.rexegg.com/regex-lookarounds.html#password for better understanding some parts in this regex

        if default_password_policy:
            password_pattern = '^(?:'
            # Construct lookahead assertion for each policy "minCharacters" group
            for pattern, number in default_password_policy[0].get('minCharacters').items():
                # Escape all special for javascript characters
                password_pattern += '(?=(?:.*[' + \
                                    re.sub('[\\{\\}\\(\\)\\[\\]\\.\\+\\*\\?\\^\\$\\\\|-]',
                                           #   inserts the entire regex match of the capturing group
                                           r'\\\g<0>',
                                           pattern) + ']){'
                password_pattern += str(number) + ',})'

            password_pattern += ')'
            # Set how many consecutive characters are allowed
            password_pattern += '(?!.*(.)\\1{' + str(default_password_policy[0].get('maxRepeatedCharacters')) + ',})'
            # Set how many characters password should have
            password_pattern += '.{' + str(default_password_policy[0].get('length').get('min')) + ',' + str(
                default_password_policy[0].get('length').get('max'))
            password_pattern += '}$'

        return password_pattern

    def change_password(self, user_id, current_password, new_password):
        headers = {
            'Content-Type': 'application/vnd.pingidentity.password.reset+json'
        }
        data = {
            'currentPassword': current_password,
            'newPassword': new_password
        }
        response = self.oauth_session.put(
            self.environment_url + '/users/' + user_id + '/password',
            json=data, headers=headers)
        response.raise_for_status()
        return response

    def set_password(self, user_id, password, force_change='false'):
        """
        :param user_id: user id
        :param password: user password
        :param force_change: specifies whether the user must change the current password on the next login.
        :return:
        """
        headers = {
            'Content-Type': 'application/vnd.pingidentity.password.set+json'
        }
        data = {
            'value': password,
            'forceChange': force_change
        }
        response = self.oauth_session.put(
            self.environment_url + '/users/' + user_id + '/password',
            json=data, headers=headers)
        response.raise_for_status()
        return response

    def send_recovery_code(self, user_id):
        headers = {
            'Content-Type': 'application/vnd.pingidentity.password.sendRecoveryCode+json'
        }
        response = self.oauth_session.post(
            self.environment_url + '/users/' + user_id + '/password',
            headers=headers)
        response.raise_for_status()
        return response

    def recover_password(self, user_id, recovery_code, new_password):
        headers = {
            'Content-Type': 'application/vnd.pingidentity.password.recover+json'
        }
        data = {
            'recoveryCode': recovery_code,
            'newPassword': new_password
        }
        response = self.oauth_session.post(self.environment_url + '/users/' + user_id + '/password',
                                           json=data, headers=headers)
        response.raise_for_status()
        return response
