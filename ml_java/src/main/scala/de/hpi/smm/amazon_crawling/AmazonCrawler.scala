package de.hpi.smm.amazon_crawling

import java.io.{File, FileInputStream, FileWriter}
import java.util.Properties

import scala.collection.mutable
import scala.collection.JavaConverters._

import au.com.bytecode.opencsv.CSVWriter
import com.amazon.advertising.api.sample.SignedRequestsHelper
import javax.xml.parsers.DocumentBuilderFactory
import org.jsoup.{HttpStatusException, Jsoup}
import org.w3c.dom.Document

/**
 * Created by Daniel on 16.12.2014.
 */

class AmazonCrawler {

	def saveDescriptionsInFile(searches : List[AmazonDescriptionSearch]):Unit = {
		val writer = new CSVWriter(new FileWriter(new File("../n2o_data/amazon/descriptions.csv")),
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

		searches.foreach((search: AmazonDescriptionSearch)=>{
			search.descriptions().foreach((desctiption: String)=>{
				writer.writeNext(Array(desctiption, search.productName))
			})
		})

		writer.close()
	}
}


object AmazonCrawler{
	def main(args: Array[String]): Unit = {
		val amazonCrawler = new AmazonCrawler
		val searches = List(
			new AmazonDescriptionSearch(List("HCM", "SAP Human Resources"),"Books","HCM"),
			new AmazonDescriptionSearch(List("CRM"),"Books","CRM"),
			new AmazonDescriptionSearch(List("ECOM"),"Books","ECOM"),
			new AmazonDescriptionSearch(List("LVM"),"Books","LVM")
		)

		amazonCrawler.saveDescriptionsInFile(searches);

	}
}
