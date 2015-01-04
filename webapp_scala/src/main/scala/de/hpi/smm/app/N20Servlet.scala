package de.hpi.smm.app

import org.scalatra._
import scalate.ScalateSupport
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import scala.util.Random

case class Prediction(percentage: Int, text: String)

class N20Servlet extends N20DemoStack  with JacksonJsonSupport {
	protected implicit val jsonFormats: Formats = DefaultFormats
	val DUMMIES = List(
		Prediction(90, "Problems with your taxes? [TaxographyXYZ] might be the right thing for you!"),
		Prediction(65, "Online Banking einfach selber machen mit [BankingXYZ]."),
		Prediction(45, "Kostenlose Kreditkarten mit [KreditKartXYZ]")
	)
	get("/") {
		contentType = "text/html"
		jade("template")
	}

	val r = new Random
	get("/predictions") {
		contentType = formats("json")
		r.shuffle(DUMMIES)
	}
}
