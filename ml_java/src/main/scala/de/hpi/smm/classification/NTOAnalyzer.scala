package de.hpi.smm.classification


import de.hpi.smm.FeatureExtractorBuilder

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class Classification(cls: String, classificationOutput: ClassificationOutput)

class NTOAnalyzer(featureExtractorBuilder: FeatureExtractorBuilder) {

	val demandClassifier = new Classifier("demand",
		featureExtractorBuilder.posts,
		featureExtractorBuilder.buildForDemand(),
		featureExtractorBuilder.dataReader)

	val productClassifier = new MultiProductClassifier(
		featureExtractorBuilder.brochures,
		featureExtractorBuilder.postForCategory,
		featureExtractorBuilder.dataReader)

//	val CRMClassifier = new ProductClassifier("CRM",
//		featureExtractorBuilder.brochures,
//		featureExtractorBuilder.postForCategory,
//		featureExtractorBuilder.dataReader)
//
//	val ECOMClassifier = new ProductClassifier("ECOM",
//		featureExtractorBuilder.brochures,
//		featureExtractorBuilder.postForCategory,
//		featureExtractorBuilder.dataReader)
//
//	val HCMClassifier = new ProductClassifier("HCM",
//		featureExtractorBuilder.brochures,
//		featureExtractorBuilder.postForCategory,
//		featureExtractorBuilder.dataReader)
//
//	val LVMClassifier = new ProductClassifier("LVM",
//		featureExtractorBuilder.brochures,
//		featureExtractorBuilder.postForCategory,
//		featureExtractorBuilder.dataReader)

	def classifyDemand(text: String): Classification = {
		Classification("demand", demandClassifier.classProbability(text))
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		List[Classification](
//			Classification("HCM" , HCMClassifier.classProbability(text)),
//			Classification("ECOM", ECOMClassifier.classProbability(text)),
//			Classification("CRM" , CRMClassifier.classProbability(text)),
//			Classification("LVM" , LVMClassifier.classProbability(text))
		).sortBy(-_.classificationOutput.prob)
	}
}
