package de.hpi.smm.amazon_crawling

import java.io.FileInputStream
import java.util.Properties
import javax.xml.parsers.{DocumentBuilderFactory, DocumentBuilder}
import org.w3c.dom.Document

import scala.collection.JavaConverters._
import com.amazon.advertising.api.sample.SignedRequestsHelper
import scala.collection.mutable

/**
 * Created by Daniel on 16.12.2014.
 */
class AmazonCrawler {

	private val ENDPOINT = "ecs.amazonaws.de"
	private val KEY_FILE = "src/main/resources/keys.conf"

	val p = new Properties()
	p.load(new FileInputStream(KEY_FILE))

	val AWS_SECRET_KEY = p.getProperty("AWS_SECRET_KEY", "NO_KEY_THERE")
	val AWS_ACCESS_KEY_ID = p.getProperty("AWS_ACCESS_KEY_ID", "NO_KEY_THERE")

	private def request(params: mutable.Map[String, String]): Document={
		val requestHelper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY)
		val requestUrl = requestHelper.sign(params.asJava)

		println(requestUrl)

		val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		val document = documentBuilder.parse(requestUrl)


		return document
	}

	private def descriptions(keywords: String, searchIndex: String): List[String] ={
		val params = mutable.Map(
			"Service" -> "AWSECommerceService",
			"AssociateTag" -> "PutYourAssociateTagHere",
			"Version" -> "2011-08-01",
			"Operation" -> "ItemSearch",
			"Keywords" -> keywords,
			"SearchIndex" -> searchIndex,
			"ResponseGroup" -> "Small"
		)
		val document = request(params);

		val idNodes = document.getElementsByTagName("ASIN")

		for(i<-0 to idNodes.getLength-1){
			val id = idNodes.item(i).getTextContent
			println(id)
			//description(id)
		}

		val descriptions = List()

		return descriptions
	}

	private def description(id: String): String ={
		val params = mutable.Map(
			"Service" -> "AWSECommerceService",
			"AssociateTag" -> "PutYourAssociateTagHere",
			"Version" -> "2011-08-01",
			"Operation" -> "ItemLookup",
			"ItemId" -> id,
			"ResponseGroup" -> "EditorialReview"
		)
		val document = request(params);

		return new String()
	}
}


object AmazonCrawler{
	def main(args: Array[String]): Unit = {
		val amazonCrawler = new AmazonCrawler
		val descriptions = amazonCrawler.descriptions("HCM","Books")
		descriptions.foreach(println)
	}
}
