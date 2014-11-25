from flask import Flask, request, render_template,session
from active_learner import active_learner
app = Flask(__name__)

learner = active_learner()

@app.route("/")
def hello():
	return render_template("index.html")

@app.route("/uncertain_posts")
def unclassified_posts():
	posts = learner.determine_uncertain_posts()
	return render_template("posts.jinja2", posts = posts)

@app.route("/tagged_posts")
def tagged_posts():
	posts = learner.determine_tagged_posts()
	return render_template("posts.jinja2", posts = posts)

@app.route("/conflicted_posts")
def conflicted_posts():
	posts = learner.determine_conflicted_posts()
	print posts
	return render_template("posts.jinja2", posts = posts)

@app.route("/post/<post_id>")
def post(post_id):
	posts = learner.post(post_id)
	return render_template("posts.jinja2", posts = posts)


@app.route('/classify_post/<post_id>', methods=['POST'])
def tag_post(post_id):
	print request.form
	if 'demand' in request.form:
		learner.tag_post(post_id, 'demand', request.form['demand'])
	if 'category' in request.form:
		learner.tag_post(post_id, 'category', request.form['category'])
	return ""

if __name__ == "__main__":
	app.debug = True
	app.run()
