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
# gets new data and puts in message arr
def data():
    print("in data")
    channel = session['channel']
    msg = pubsub.get_message()
    new_memory = 0
    new_disk = 0
    if msg:
        data = msg['data']
        messages.append(data)
        print(data)
    suggestions_list=["fuc"]
    memory_usage_array = []
    disk_usage_array = []

    for x in messages:
        string_x = str(x)
        
        if string_x.find("Memory") != -1:
            start = string_x.find("(") + 1
            end = string_x.find("%")
            percent = string_x[start:end]
            memory_usage_array.append(percent)
            new_memory = percent
        if string_x.find("Disk") != -1:
            start = string_x.find("(") + 1
            end = string_x.find("%")
            percent = string_x[start:end]
            disk_usage_array.append(percent)
            new_disk = percent
    
    memory_string = str(new_memory)
    disk_string = str(new_disk)

    # return render_template('index.html', suggestions=messages, disk_array = disk_usage_array, memory_array = memory_usage_array )
    return  "Memory Usage: " + memory_string + "%\n | Disk Usage: " + disk_string + "%\n"
    # return render_template('data.html', suggestions=messages, disk_array = disk_usage_array, memory_array = memory_usage_array )

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
