package de.hpi.smm.app

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{Ok, BadRequest}

import scala.util.Random
import org.scalatra.json._

case class Prediction(percentage: Int, text: String)

class N20Servlet extends N20DemoStack with JacksonJsonSupport {
	protected implicit val jsonFormats: Formats = DefaultFormats

	import ClassificationPreparation.ntoClassifier

	get("/") {
		contentType = "text/html"
		jade("template")
	}

	val r = new Random
	get("/predictions") {
		val text = params("text")
		try {
			val demandClassification  = ntoClassifier.predictDemandExtendedOutput(text)
			val productClassification = ntoClassifier.predictProduct(text)
			contentType = formats("json")
			Ok(Map(
				"demand" -> demandClassification,
				"product" -> productClassification
			))
		}
		catch {
			case e: Throwable =>
				BadRequest(e)
		}
	}

}
