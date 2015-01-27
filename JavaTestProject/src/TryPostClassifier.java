import java.io.File;
import java.util.List;

import com.blog_intelligence.nto.*;

public class TryPostClassifier {

	static DataBaseConfiguration CONFIG = new DataBaseConfiguration(
			"141.89.225.134", 				// ip
			"30315",						// port
			"SMA1415", 						// user
			"Popcorn54",					// password
			"SMA1415.CLASSIFIED_POSTS"		// database
	);

	static File DEMAND_MODEL_FILE = new File("demand.model");
	static File PRODUCT_MODEL_FILE = new File("product.model");

	public static void main(String[] args) {
		NTOClassifier classifier;
		if (DEMAND_MODEL_FILE.exists() && PRODUCT_MODEL_FILE.exists()) {
			System.out.println("reading from model file");
			long l1 = System.currentTimeMillis();
			classifier = readFromModel();
			long l2 = System.currentTimeMillis();
			System.out.println(" in " + (l2 - l1) + " ms.");
		} else {
			System.out.print("Creating new model");
			long l1 = System.currentTimeMillis();
			classifier = buildModelFromScratch();
			long l2 = System.currentTimeMillis();
			System.out.println(" in " + (l2 - l1) + " ms.");
		}

		/**
		 * Predicting demand and product
		 */
		String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. " +
				"Do you have any recommendations for a good crm system to handle them?";

		double probDemand = classifier.predictDemand(post);
		System.out.println("Demand probability " + probDemand);

		List<NTOClassifier.Classification> probsProduct = classifier.predictProduct(post);
		for (NTOClassifier.Classification classification : probsProduct) {
			System.out.println(classification.product() + ": " + classification.prob());
		}
	}

	public static NTOClassifier readFromModel() {

		NTOClassifier classifier = new NTOClassifier();

		classifier.loadDemand(DEMAND_MODEL_FILE);
		classifier.loadProduct(PRODUCT_MODEL_FILE);

		return classifier;
	}

	public static NTOClassifier buildModelFromScratch() {
		/**
		 * Reading training data
		 */
		DocumentExtractor documentExtractor = new DocumentExtractor();

		ReadingResult documents = documentExtractor.readFromCSV(
				new File("../JavaTestProject/linked_in_posts.csv"),
				new File("../JavaTestProject/brochures.csv"),
				new File("../JavaTestProject/classification.json"));

		// ReadingResult dbDocs = documentExtractor.readFromDB(CONFIG);

		/**
		 * Building classifier
		 */
		NTOClassifier classifier = new NTOClassifier();

		classifier.trainDemand(documents.demandDocuments());
		classifier.trainProduct(documents.productDocuments());

		classifier.persistDemand(DEMAND_MODEL_FILE);
		classifier.persistProducts(PRODUCT_MODEL_FILE);

		return classifier;
	}

}
