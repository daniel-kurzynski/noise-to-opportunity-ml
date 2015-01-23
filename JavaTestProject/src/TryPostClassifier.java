import java.io.File;

import com.blog_intelligence.nto.*;

public class TryPostClassifier {

	public static void main(String[] args) {

		ReadingResult documents = new DocumentExtractor().readFromCSV(
				new File("../n2o_data/linked_in_posts.csv"),
				new File("../n2o_data/brochures.csv"),
				new File("../n2o_data/classification_updates/latest.json"));

		NTOClassifier classifier = new NTOClassifier();

		classifier.trainDemand(documents.demandDocuments());
		classifier.trainProduct(documents.productDocuments());

		String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. Do you have any recommendations for a good crm system to handle them?";
		double value = classifier.predictDemand(post);

		System.out.println(value);
	}
}
