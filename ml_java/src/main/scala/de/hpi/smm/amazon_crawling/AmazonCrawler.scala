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
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_ESCAPE_CHARACTER)

		searches.foreach((search: AmazonDescriptionSearch)=>{
			search.search().foreach((result: Result)=>{
				writer.writeNext(Array(result.title, result.description, search.productName))
			})
		})

		writer.close()
	}
}


object AmazonCrawler{
	def main(args: Array[String]): Unit = {
		val amazonCrawler = new AmazonCrawler
		val searches = List(
			new AmazonDescriptionSearch(List(("sap hcm",2)),"Books","HCM", List("crm", "ecom", "lvm")),
			new AmazonDescriptionSearch(List(("sap crm",2)),"Books","CRM", List("hcm", "ecom", "lvm")),
			new AmazonDescriptionSearch(List(("ecommerce",2)),"Books","ECOM", List("hcm", "crm", "lvm")),
			new AmazonDescriptionSearch(List(("landscape virtualization management",1)),"Books","LVM", List("hcm", "crm", "ecom"))
		)

		amazonCrawler.saveDescriptionsInFile(searches);

	}
}
