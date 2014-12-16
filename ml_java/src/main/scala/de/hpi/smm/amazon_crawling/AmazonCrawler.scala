package de.hpi.smm.amazon_crawling

import java.io.{File, FileWriter, FileInputStream}
import java.util.Properties

import au.com.bytecode.opencsv.CSVWriter

import scala.collection.mutable
import scala.collection.JavaConverters._

import com.amazon.advertising.api.sample.SignedRequestsHelper
import javax.xml.parsers.DocumentBuilderFactory
import org.jsoup.{HttpStatusException, Jsoup}
import org.w3c.dom.Document

/**
 * Created by Daniel on 16.12.2014.
 */
class AmazonCrawler {

	private val ENDPOINT = "ecs.amazonaws.com"
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
			"Version" -> "2013-08-01",
			"Operation" -> "ItemSearch",
			"Keywords" -> keywords,
			"SearchIndex" -> searchIndex,
			"ResponseGroup" -> "Small"
		)

		var descriptionStrings = List[String]()

		for(itemPage<-1 to 10){
			params.put("ItemPage", itemPage.toString);

			val document = request(params);

			val urlNodes = document.getElementsByTagName("DetailPageURL")

			for(i<-0 to urlNodes.getLength-1){
				try{
					val url = urlNodes.item(i).getTextContent
					val descriptionString = description(url)
					if(descriptionString.length>100)
						descriptionStrings = descriptionString :: descriptionStrings
					println("Success: " + url)
				}
				catch{
					case e: HttpStatusException => {
						println(e.getMessage + ", Status: " + e.getStatusCode + ", URL: " + e.getUrl)
					}
					case e: Exception => {
						println(e.getMessage)
					}
				}
			}
		}

		return descriptionStrings
	}

	private def description(url: String): String ={
		val document = Jsoup.connect(url).get()
		val description = document.select("noscript")

		return description.text()
	}

	private def saveDescriptionsInFile(searchStrings: List[String], searchIndex: String, product: String):Unit = {
		val writer = new CSVWriter(new FileWriter(new File("../n2o_data/amazon/"+product+".csv")),
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

		searchStrings.foreach((keywords: String)=>{
			val descriptionStrings = descriptions(keywords, searchIndex)
			descriptionStrings.foreach((descriptionString: String) => writer.writeNext(Array(descriptionString, product)))
		})

		writer.close()
	}
}


object AmazonCrawler{
	def main(args: Array[String]): Unit = {
		val amazonCrawler = new AmazonCrawler
		amazonCrawler.saveDescriptionsInFile(List("HCM", "SAP Human Resources"),"Books","HCM")
		amazonCrawler.saveDescriptionsInFile(List("CRM"),"Books","CRM")
		amazonCrawler.saveDescriptionsInFile(List("ECOM"),"Books","ECOM")
		amazonCrawler.saveDescriptionsInFile(List("LVM"),"Books","LVM")
	}
}
