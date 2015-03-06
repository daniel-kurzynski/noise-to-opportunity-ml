from flask import Flask, request, render_template,session
from active_learner import active_learner
import simplejson as json
from flask.ext.compress import Compress
import numpy as np

compress = Compress()
app = Flask(__name__)

learner = active_learner()

@app.route("/")
def hello():
	return render_template("index.html")

@app.route("/posts")
def posts():
	tagger = request.args.get('tagger', 'anonymous')
	posts = learner.predicted_posts(type = "uncertain")
	certain = learner.predicted_posts(type = "certain")
	tagged = learner.determine_tagged_posts(tagger=tagger)
	conflicted = learner.determine_conflicted_posts()
	posts.extend(certain)
	posts.extend(tagged)
	posts.extend(conflicted)
	posts = np.random.choice(posts, 10, False)
	return render_template("posts.jinja2", posts = posts, json=json)

@app.route("/uncertain_posts")
def uncertain_posts():
	posts = learner.predicted_posts(type = "uncertain")
	return render_template("posts.jinja2", posts = posts, json=json)

@app.route("/certain_posts")
def certain_posts():
	posts = learner.predicted_posts(type = "certain")
	return render_template("posts.jinja2", posts = posts, json=json)

@app.route("/all_tagged_posts")
def all_tagged_posts():
	posts = learner.determine_tagged_posts()
	return render_template("posts.jinja2", posts = posts, json=json)

@app.route("/tagged_by_others_posts")
def tagged_by_others_posts():
	tagger = request.args.get('tagger', 'anonymous')
	print tagger
	posts = learner.determine_tagged_posts(tagger=tagger)
	return render_template("posts.jinja2", posts = posts, json=json)

@app.route("/conflicted_posts")
def conflicted_posts():
	posts = learner.determine_conflicted_posts()
	return render_template("posts.jinja2", posts = posts, json=json)

@app.route("/post/<post_id>")
def post(post_id):
	posts = learner.post(post_id)
	return render_template("posts.jinja2", posts = posts, json=json)

@app.route("/analyze_post")
def analyze_post():
	post = request.args.get('post', None)
	if post == None:
		return render_template("analyze_results.jinja2", results = [])
	return render_template("analyze_results.jinja2", results = [post])

@app.route('/classify_post/<post_id>', methods=['POST'])
def tag_post(post_id):
	tagger=request.form['tagger']
	if 'demand' in request.form:
		learner.tag_post(tagger, post_id, 'demand', request.form['demand'])
	if 'category' in request.form:
		learner.tag_post(tagger, post_id, 'category', request.form['category'])
	return ""

if __name__ == "__main__":
	app.debug = True
	compress.init_app(app)
	app.run("0.0.0.0")
