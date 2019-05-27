import logging

from services.configuration import Configuration
from services.auth import AuthClient
from services.passwords import PasswordsManagement
from services.populations import PopulationsManagement
from services.users import UsersManagement

__version__ = '0.0.0'

logging.getLogger('ping_oauth_lib').addHandler(logging.NullHandler())
