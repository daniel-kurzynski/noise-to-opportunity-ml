import java.io.File;

import com.blog_intelligence.nto.DocumentExtractor;
import com.blog_intelligence.nto.NTOClassifier;
import com.blog_intelligence.nto.ReadingResult;

public class TryPostClassifier {

	public static void main(String[] args) {

		ReadingResult documents = new DocumentExtractor().readFromCSV(
				new File("linked_in_posts.csv"),
				new File("brochures.csv"),
				new File("classification.json"));

		NTOClassifier classifier = new NTOClassifier();

		classifier.trainDemand(documents.demandDocuments());
		classifier.trainProduct(documents.productDocuments());

		String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. Do you have any recommendations for a good crm system to handle them?";

		double value = classifier.predictDemand(null);

		System.out.println(value);
	}
}
