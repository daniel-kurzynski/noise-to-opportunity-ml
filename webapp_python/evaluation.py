from active_learner import active_learner

if __name__ == "__main__":
	learner = active_learner()
	print learner.evaluate_classifier()
	cm = learner.conf_matrix()
	print cm
