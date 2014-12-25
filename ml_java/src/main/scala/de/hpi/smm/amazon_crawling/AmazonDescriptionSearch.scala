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

class Result(var description: String, var title: String)

class AmazonDescriptionSearch(
		var searches: List[(String, Int)],
		var searchIndex: String,
		var productName: String,
		var filterWords: List[String],
		var language: String) {

	private var ENDPOINTS = Map(
		"en"-> "ecs.amazonaws.com",
		"de" -> "ecs.amazonaws.de"
	)

	private val KEY_FILE = "src/main/resources/keys.conf"

	private val userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
	private val p = new Properties()
	p.load(new FileInputStream(KEY_FILE))

	private val AWS_SECRET_KEY = p.getProperty("AWS_SECRET_KEY", "NO_KEY_THERE")
	private val AWS_ACCESS_KEY_ID = p.getProperty("AWS_ACCESS_KEY_ID", "NO_KEY_THERE")

	private def request(params: mutable.Map[String, String]): Document={
		val requestHelper = SignedRequestsHelper.getInstance(ENDPOINTS(language), AWS_ACCESS_KEY_ID, AWS_SECRET_KEY)
		val requestUrl = requestHelper.sign(params.asJava)
		println(requestUrl)

		val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		val document = documentBuilder.parse(requestUrl)

		return document
	}

	def search(): List[Result] = {
		var results = List[Result]()

		println("Descriptions for product " + productName)

		searches.foreach((search: (String, Int)) => {
			val keywords = search._1
			val numberOfPages = search._2

			results = results ::: searchForProducts(keywords, numberOfPages)
		})

		return results
	}

	private def searchForProducts(keywords: String, numberOfPages: Int): List[Result] ={
		val params = mutable.Map(
			"Service" -> "AWSECommerceService",
			"AssociateTag" -> "PutYourAssociateTagHere",
			"Version" -> "2013-08-01",
			"Operation" -> "ItemSearch",
			"Keywords" -> keywords,
			"SearchIndex" -> searchIndex,
			"ResponseGroup" -> "Small"
		)

		var results = List[Result]()

		for(itemPage<-1 to numberOfPages){
			params.put("ItemPage", itemPage.toString);

			val document = request(params);

			val urlNodes = document.getElementsByTagName("DetailPageURL")

			for(i<-0 to urlNodes.getLength-1){
				try{
					val url = urlNodes.item(i).getTextContent
					val result = parse(url)
					if(result.description.length>100)
						results = result :: results
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

		return results
	}

	def containsFilterWord(result: Result): Boolean ={
		filterWords.foreach((word: String) => {
			if(result.description.toLowerCase.contains(word.toLowerCase()))
				return true;
			if(result.title.toLowerCase.contains(word.toLowerCase()))
				return true;
		})
		return false;
	}

	private def parse(url: String): Result ={
		val document = Jsoup.connect(url).userAgent(userAgent).get()
		val description = document.select("noscript")

		return new Result(description.text(), document.title)
	}
}


