package de.hpi.smm.classification


import de.hpi.smm.FeatureExtractorBuilder

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class Classification(cls: String, classificationOutput: ClassificationOutput)

class NTOAnalyzer(featureExtractorBuilder: FeatureExtractorBuilder) {

	val demandClassifier = new Classifier("demand",
		featureExtractorBuilder.posts,
		featureExtractorBuilder.buildForDemand(),
		featureExtractorBuilder.dataReader)

	val CRMClassifier = new Classifier("CRM",
		featureExtractorBuilder.brochures,
		featureExtractorBuilder.buildForBrochures("CRM", 1.1, 6.0),
		featureExtractorBuilder.dataReader)

	val ECOMClassifier = new Classifier("ECOM",
		featureExtractorBuilder.brochures,
		featureExtractorBuilder.buildForBrochures("ECOM", 1.8, 100.0),
		featureExtractorBuilder.dataReader)

	val HCMClassifier = new Classifier("HCM",
		featureExtractorBuilder.brochures,
		featureExtractorBuilder.buildForBrochures("HCM", 2.0, 10.0),
		featureExtractorBuilder.dataReader)

	val LVMClassifier = new Classifier("LVM",
		featureExtractorBuilder.brochures,
		featureExtractorBuilder.buildForBrochures("LVM", 3.4, 10.0),
		featureExtractorBuilder.dataReader)

	def classifyDemand(text: String): Classification = {
		Classification("demand", demandClassifier.classProbability(text))
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		List(
			Classification("HCM" , HCMClassifier.classProbability(text)),
			Classification("ECOM", ECOMClassifier.classProbability(text)),
			Classification("CRM" , CRMClassifier.classProbability(text)),
			Classification("LVM" , LVMClassifier.classProbability(text))
		).sortBy(-_.classificationOutput.prob)
	}
}
