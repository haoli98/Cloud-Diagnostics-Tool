from flask import Flask, render_template, redirect, session
import redis
import time
from app.forms import LoginForm


app = Flask(__name__)
app.config['SECRET_KEY'] = 'test-key'

r = redis.Redis(host='40.87.94.171', port=6379, db=0)
pubsub = r.pubsub()
# channel = "fsr32"  #  Would be hl677 for your channel
# pubsub.subscribe(channel)
# pubsub.get_message()

test = "not set"
messages = []

@app.route('/')
def hello_world():
    print(test)
    return 'Hello, World!'

@app.route('/dashboard')
def dashboard():
    return render_template('index.html', title='Dashboard')

@app.route('/data')
def data():
    print("in data")
    channel = session['channel']
    msg = pubsub.get_message()
    if msg:
        data = msg['data']
        messages.append(data)
        print(data)

    suggestions_list=["fuc"]
    return render_template('data.html', suggestions=messages)

@app.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm()
    if form.validate_on_submit():

        print(form.username.data)
        channel = form.username.data
        test = "set"

        pubsub.subscribe(channel)
        pubsub.get_message()
        
        print(test)
        session['channel'] = channel
        return redirect("/dashboard")

    return render_template('login.html', title='Sign In', form=form)
