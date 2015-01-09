package de.hpi.smm.app

import java.io.{File, FileReader}

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.data_reader.DataReader
import org.scalatra.{Ok, BadRequest}

import scala.util.Random
import de.hpi.smm.classification.PostClassifier
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

case class Prediction(percentage: Int, text: String)

class N20Servlet extends N20DemoStack  with JacksonJsonSupport {
	protected implicit val jsonFormats: Formats = DefaultFormats
	val DUMMIES = List(
		Prediction(90, "Problems with your taxes? [TaxographyXYZ] might be the right thing for you!"),
		Prediction(65, "Online Banking einfach selber machen mit [BankingXYZ]."),
		Prediction(45, "Kostenlose Kreditkarten mit [KreditKartXYZ]")
	)

	val FOR_ALL_POSTS = false

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader("../webapp_python/data/classification.json"))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS)

	val featureExtractorBuilder = new FeatureExtractorBuilder(dataReader)
	val postClassifier = new PostClassifier(featureExtractorBuilder)

	get("/") {
		contentType = "text/html"
		jade("template")
	}

	val r = new Random
	get("/predictions") {
		val text = params("text")
		try {
			val demandClassification  = postClassifier.classifyDemand(text)
			val productClassification = postClassifier.classifyProduct(text)
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
