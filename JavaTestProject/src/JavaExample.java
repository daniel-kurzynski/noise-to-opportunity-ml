import com.blog_intelligence.nto.*;
import de.hpi.smm.classification.ExtendedNTOClassifier;
import de.hpi.smm.classification.ExtendedNTOClassifier.FullPrediction;
import de.hpi.smm.classification.ExtendedNTOClassifierBuilder;
import scala.Tuple2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaExample {

	static DataBaseConfiguration CONFIG = new DataBaseConfiguration(
			"141.89.225.134", 				// ip
			"30315",						// port
			"SMA1415", 						// user
			"Popcorn54",					// password
			"SMA1415.CLASSIFIED_POSTS"		// database
	);

	static File DEMAND_MODEL_FILE = new File("demand.model");
	static File PRODUCT_MODEL_FILE = new File("product.model");

	private static void predictSingleDoc(){
		NTOClassifier classifier;
		if (DEMAND_MODEL_FILE.exists() && PRODUCT_MODEL_FILE.exists()) {
			System.out.print("Reading from model file");
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

		System.out.println(post);

		double probDemand = classifier.predictDemand(post);
		System.out.println("Demand probability " + probDemand);

		List<ProductClassification> probsProduct = classifier.predictProduct(post);
		for (ProductClassification classification : probsProduct) {
			System.out.println(classification.product() + ": " + classification.prob());
		}
	}

	public static void predictMultipleDocs() {
		/**
		 * Building classifier
		 */
		ExtendedNTOClassifier classifier = ExtendedNTOClassifierBuilder.build(
				new File("classification.json"),
				new File("brochures.csv"),
				new File("linked_in_posts.csv"));


		/**
		 * Fill posts
		 */
		List<String> posts = new ArrayList<>();
		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. " +
				"Do you have any recommendations for a good crm system to handle them?");

		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems reach my customers. " +
				"Do you have any recommendations for a commerce software?");

		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems virtual resources. " +
				"Do you have any recommendations for a good virtualization software?");

		posts.add("Hi! I am the CTO of Startup Inc. Lately, I have problems organising my employees. " +
				"Do you have any recommendations for a good HR software?");

		/**
		 * Predict for each class the most certain
		 */
		for(String productClass: new String[] {"CRM", "ECOM", "HCM", "LVM"}){
			List<Tuple2<String, FullPrediction>> classifications = classifier.extractMostCertainPosts(
					3,				// top most
					productClass, 	// for this class
					posts);			// from these posts

			System.out.println("best posts for " + productClass);
			for(Tuple2<String, FullPrediction> classification: classifications){
				System.out.println("Text: " + classification._1());
				System.out.println("Demand prob: " + classification._2().demandProb());
				System.out.println("Product prob: " + classification._2().productProb());
				System.out.println("===================================");
			}
		}


	}

	public static void main(String[] args) {

//		predictSingleDoc();
		predictMultipleDocs();

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

		// Adapt files here if necessary.
		ReadingResult csvDocs = documentExtractor.readFromCSV(
				new File("linked_in_posts.csv"),
				new File("brochures.csv"),
				new File("classification.json"));

		// Load documents from database. Can be used in the same way as csvDocs, or even combined with csvDocs.
		// ReadingResult dbDocs = documentExtractor.readFromDB(CONFIG);
		// Like this:
		// List<Document> combined = new ArrayList<>();
		// combined.addAll(csvDocs.demandDocuments());
		// combined.addAll(dbDocs.demandDocuments());

		/**
		 * Building classifier
		 */
		NTOClassifier classifier = new NTOClassifier();

		// Training
		classifier.trainDemand(csvDocs.demandDocuments());
		classifier.trainProduct(csvDocs.productDocuments());

		// Persisting for next run
		classifier.persistDemand(DEMAND_MODEL_FILE);
		classifier.persistProducts(PRODUCT_MODEL_FILE);

		return classifier;
	}

}
