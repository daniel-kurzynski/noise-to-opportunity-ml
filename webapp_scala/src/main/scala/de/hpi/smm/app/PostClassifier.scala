package de.hpi.smm.app

import scala.util.Random

case class Classification(cls: String, prob: Double)

class PostClassifier {
	val r = new Random
	def classifyDemand(text: String): Classification = {
		Classification("demand", r.nextDouble())
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		List(
			Classification("HCM" , 0.8),
			Classification("ECOM", 0.6),
			Classification("CRM" , 0.4),
			Classification("LVM" , 0.2)
		)
	}
}
