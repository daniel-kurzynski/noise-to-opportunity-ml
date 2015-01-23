import java.io.File;
import java.util.List;

import com.blog_intelligence.nto.*;
import de.hpi.smm.data_reader.DataBaseReader;

public class TryPostClassifier {

	static DataBaseConfiguration CONFIG = new DataBaseConfiguration("141.89.225.134","30315","SMA1415", "Popcorn54","SMA1415.CLASSIFIED_POSTS");

	public static void main(String[] args) {
		/**
		 * Reading training data
		 */
		DocumentExtractor documentExtractor = new DocumentExtractor();

		ReadingResult documents = documentExtractor.readFromCSV(
				new File("../n2o_data/linked_in_posts.csv"),
				new File("../n2o_data/brochures.csv"),
				new File("../n2o_data/classification_updates/latest.json"));

		ReadingResult dbDocs = documentExtractor.readFromDB(CONFIG);

		/**
		 * Building classifier
		 */
		NTOClassifier classifier = new NTOClassifier();

		classifier.trainDemand(documents.demandDocuments());
		classifier.trainProduct(documents.productDocuments());

		/**
		 * Predicting demand and product
		 */
		String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. Do you have any recommendations for a good crm system to handle them?";

		double probDemand = classifier.predictDemand(post);
		System.out.println("Demand probability " + probDemand);

		List<NTOClassifier.Classification> probsProduct = classifier.predictProduct(post);
		for (NTOClassifier.Classification classification : probsProduct) {
			System.out.println(classification.product() + ": " + classification.prob());
		}
	}
}
