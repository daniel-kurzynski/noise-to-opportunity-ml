from flask import Flask, request, render_template,session
from active_learner import active_learner
app = Flask(__name__)

learner = active_learner()

@app.route("/")
def hello():
	return render_template("index.html")

@app.route("/damand_labeled_posts")
def damand_labeled_posts():
	posts = learner.damand_labeled_posts()
	return render_template("damand_labeled_posts.json", posts = posts)

@app.route("/uncertainty_posts")
def unclassified_posts():
	posts = learner.determine_uncertain_posts()
	return render_template("uncertain_posts.json", posts = posts)

@app.route("/damand_certain_posts")
def damand_certain_posts():
	posts = learner.determine_certain_posts()
	return render_template("uncertain_posts.json", posts = posts)


@app.route("/post/<post_id>")
def post(post_id):
	posts = learner.post(post_id)
	return render_template("uncertain_posts.json", posts = posts)


@app.route('/classify_post/<post_id>', methods=['POST'])
def tag_post(post_id):
	print request.form
	if 'demand' in request.form:
		learner.tag_demand(post_id, request.form['demand'])
	if 'category' in request.form:
		learner.tag_category(post_id, request.form['category'])
	return ""

if __name__ == "__main__":
	app.debug = True
	app.run()
