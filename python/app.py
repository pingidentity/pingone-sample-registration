import logging
import logging.config
import os
from datetime import timedelta

from flask import Flask, render_template, request, session, flash, Response
from flask_wtf.csrf import CSRFProtect
from requests import HTTPError

from services.populations import PopulationsManagement
from services.users import UsersManagement
from services.passwords import PasswordsManagement
from services.auth import AuthClient

os.environ['DEBUG'] = 'true'
os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = 'true'

logging.config.fileConfig('logging.ini')

app = Flask(__name__)
app.config.from_pyfile('config.cfg')
app.config.update({'SECRET_KEY': os.urandom(24)})

# Enable CSRF protection globally for a Flask app
csrf = CSRFProtect(app)
auth = AuthClient(app)


@app.before_request
def make_session_permanent():
    session.permanent = True
    # set a session timeout period
    app.permanent_session_lifetime = timedelta(minutes=15)


@app.route('/', endpoint='index')
@auth.callback
@csrf.exempt
def index():
    return render_template('index.html')


@app.route('/password/forgot', endpoint='forgot_password', methods=['GET'])
@auth.token_required
@csrf.exempt
def forgot_password():
    return render_template('forgot_password.html')


@app.route('/password/forgot', endpoint='forgot_password_post', methods=['POST'])
@auth.token_required
def forgot_password():
    try:
        passwords = PasswordsManagement(auth.get_oauth_session(), app)
        users = UsersManagement(auth.get_oauth_session(), app)
        response = users.find(request.form.get('username'))
        if response.json()['_embedded']['users'][0]:
            user = response.json()['_embedded']['users'][0]
            passwords.send_recovery_code(user['id'])
            return render_template('recover_password.html',
                                   passwordPattern=passwords.get_password_pattern(),
                                   user_id=user.get('id'))
        else:
            return render_template('forgot_password.html',
                                   error_message="There is no such user with a name: " + request.form.get('username'))
    except HTTPError as error:
        log_error('Could not recover ' + request.form.get('username') + 's password because of: ', error)
        return render_template('forgot_password.html')


@app.route('/password/recover', endpoint='recover_password', methods=['POST'])
@auth.token_required
def recover_password():
    user_id = request.form.get('userId')
    try:
        passwords = PasswordsManagement(auth.get_oauth_session(), app)
        passwords.recover_password(user_id,
                                   request.form.get('recoveryCode'),
                                   request.form.get('password'))
        flash('Password was successfully recovered.', 'success')

    except HTTPError as error:
        log_error('Password recovery failed because of: ', error)
        return render_template('recover_password.html', user_id=user_id)

    return render_template('index.html')


@app.route('/register', endpoint='register')
@auth.token_required
def register():
    try:
        populations = PopulationsManagement(auth.get_oauth_session(), app)
        passwords = PasswordsManagement(auth.get_oauth_session(), app)
        all_populations = populations.get_all()
        password_pattern = passwords.get_password_pattern()
        return render_template('register.html', passwordPattern=password_pattern,
                               populations=all_populations.json()['_embedded'][
                                   'populations'])

    except HTTPError as error:
        log_error('Could not get all populations because of: ', error)
    return render_template('register.html')


@app.route('/register', endpoint='register_post', methods=['POST'])
@auth.token_required
def register():
    user_id = None
    try:
        passwords = PasswordsManagement(auth.get_oauth_session(), app)
        users = UsersManagement(auth.get_oauth_session(), app)
        response = users.create(request.form.get('email'), request.form.get('username'),
                                request.form.get('population'))
        user_id = response.json()['id']
        passwords.set_password(user_id, request.form.get('password'))
        flash('User "' + str(response.json().get('username')) + '" was successfully registered. ', 'success')
        return render_template('index.html')
    except HTTPError as error:
        log_error('User was not registered because of: ', error)
        populations = PopulationsManagement(auth.get_oauth_session(), app)
        response = populations.get_all()
        password_pattern = passwords.get_password_pattern()
        if user_id:
            users.delete(user_id)

    return render_template('register.html', passwordPattern=password_pattern,
                           populations=response.json()['_embedded'][
                               'populations'])


def log_error(message, error):
    error_details = error.response.json().get('details')
    error_msg = error_details[0].get('message') if error_details else error.response.json().get('message')
    flash(message + str(error_msg), 'danger')


@app.errorhandler(Exception)
def handle_error(error):
    if hasattr(error, 'code'):
        if error.code < 400:
            return Response.force_type(error, request.environ)
        elif error.code == 404:
            flash(str(error), 'danger')
            logging.error(str(error))
            return render_template('index.html'), 404
    flash(str(error), 'danger')
    logging.exception('Something went wrong. {}'.format(str(error)))
    return render_template('index.html'), 500


if __name__ == "__main__":
    app.run(port=8080, host='localhost', use_debugger=True, use_reloader=True, ssl_context='adhoc')
