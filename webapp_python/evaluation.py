from active_learner import active_learner
import matplotlib.pyplot as plt

if __name__ == "__main__":
	learner = active_learner()
	print learner.evaluate_classifier()
	cm = learner.conf_matrix()
	print cm
	# Show confusion matrix in a separate window
	plt.matshow(cm)
	plt.title('Confusion matrix')
	plt.colorbar()
	plt.ylabel('Actual demand')
	plt.xlabel('Predicted demand')
	plt.show()
