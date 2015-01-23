import java.io.File;
import java.util.List;

import de.hpi.smm.classification.Classification;
import de.hpi.smm.classification.NTOAnalyzer;
import de.hpi.smm.classification.NTOAnalyzerBuilder;

public class TryPostClassifier {

	public static void main(String[] args) {
		File classificationFile = new File("classification.json");
		File postFile = new File("linked_in_posts.csv");
		File brochuresFile = new File("brochures.csv");
		String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. Do you have any recommendations for a good crm system to handle them?";

		NTOAnalyzer analyzer = NTOAnalyzerBuilder.build(
				classificationFile,
				brochuresFile,
				postFile);
		
		Classification demandClassification = analyzer.classifyDemand(post);
		System.out.println(demandClassification.cls() + " with prob: " + demandClassification.classificationOutput().prob());
		
		List<Classification> classificationList = analyzer.classifyProductAsJavaList(post);
		System.out.println(classificationList.get(0).cls() + " with prob: " + classificationList.get(0).classificationOutput().prob());

	}
}
