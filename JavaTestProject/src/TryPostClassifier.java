import java.io.File;
import java.util.List;

import com.blog_intelligence.nto.DocumentExtractor;
import com.blog_intelligence.nto.Document;
import com.blog_intelligence.nto.NTOClassifier;

public class TryPostClassifier {

	public static void main(String[] args) {

		File classificationFile = new File("classification.json");
		List<Document> posts = new DocumentExtractor().readFromCSV(new File("linked_in_posts.csv"), classificationFile, "demand");
		List<Document> brochures = new DocumentExtractor().readFromCSV(new File("brochures.csv"), classificationFile, "category");

		NTOClassifier classifier = new NTOClassifier();

		classifier.trainDemand(posts);
		classifier.trainProduct(brochures);

		String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. Do you have any recommendations for a good crm system to handle them?";

		double value = classifier.predictDemand(null);

		System.out.println(value);
	}
}
