Noise-To-Opportunity Conversion
=======================

This is part of the Social Media Analysis seminar at Hasso-Plattner-Institute, Potsdam, Germany.

by Daniel Kurzynski, Dimitri Korsch, Stefan Bunk


### Description
This tool is prototype to show a new approach for companies to find potential customers in social networks. By listening to noise from social network posts, we identify users, which express a demand for a certain product. We achieve this identification with a two-stage text categorization classifier: First, we detect whether the post expresses a demand for some product in general. Second, we detect, which product the post is about. By using the company's brochures, we minimize the integration effort of our system.

### Usage

It is a maven project. We recommend using it by installing it to your local repository. 

```xml
<dependency>
	<groupId>com.blog_intelligence</groupId>
	<artifactId>nto</artifactId>
	<version>1.0</version>
</dependency>
```

####Classes

There are three important classes:

- Document: Represents the object to learn and classifier: post oder brochure.
- NTOClassifier: Predicts demand (```predictDemand```) and product (```predictProduct``` for each document.
- DocumentExtractor: Reads documents from file or database.
 

####Example Code

```java
/**
 * Reading training data
 */
DocumentExtractor documentExtractor = new DocumentExtractor(
		new File("stopwords.txt"),
		new File("german-fast.tagger")
);

// Adapt files here if necessary.
ReadingResult csvDocs = documentExtractor.readFromCSV(
		new File("linked_in_posts.csv"),
		new File("brochures.csv"),
		new File("classification.json")
);

// Load documents from database. Can be used in the same way as csvDocs, or even combined with csvDocs.
ReadingResult dbDocs = documentExtractor.readFromDB(CONFIG);
// Like this:
List<Document> combined = new ArrayList<>();
combined.addAll(csvDocs.demandDocuments());
combined.addAll(dbDocs.demandDocuments());

/**
 * Building classifier
 */
NTOClassifier classifier = new NTOClassifier(
		new File("stopwords.txt"),
		new File("german-fast.tagger")
);

// Training
classifier.trainDemand(csvDocs.demandDocuments());
classifier.trainProduct(csvDocs.productDocuments());

//Prediction
String post = "Hi! I am the CTO of Startup Inc. Lately, I have problems organising my customers. " +
				"Do you have any recommendations for a good crm system to handle them?";

double probDemand = classifier.predictDemand(post);
System.out.println("Demand probability " + probDemand);

List<ProductClassification> probsProduct = classifier.predictProduct(post);
for (ProductClassification classification : probsProduct) {
	System.out.println(classification.product() + ": " + classification.prob());
}

```

The [complete example](https://github.com/kurzy/noise-to-opportunity-ml/blob/master/examples/JavaExampleProject/src/JavaExample.java) can be found in the expample folder. 

####Presistency
You can persist the classifier model by calling: ```persistDemand``` and ```persistProducts``` on the NTOClassifier.

```java
// Persisting for next run
classifier.persistDemand(DEMAND_MODEL_FILE);
classifier.persistProducts(PRODUCT_MODEL_FILE);

//Load persisted model
classifier.loadDemand(DEMAND_MODEL_FILE);
classifier.loadProduct(PRODUCT_MODEL_FILE);

```
