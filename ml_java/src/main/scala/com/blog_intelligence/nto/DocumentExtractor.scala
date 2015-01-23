package com.blog_intelligence.nto

import java.io.{FileReader, File}

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.Constants._
import de.hpi.smm.data_reader.{DataReader, DataBaseReader}

case class DataBaseConfiguration(
	host: String,
	port: String,
	username: String,
	password: String,
	database: String
)

case class ReadingResult(demandDocuments: java.util.List[Document], productDocuments: java.util.List[Document])

class DocumentExtractor {

	def readFromCSV(demand_file: File, product_file: File, classification_file: File): ReadingResult = {
		val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
			new FileReader(classification_file.getAbsolutePath))
		new DataReader(classifiedPosts, demand_file, product_file, false).getReadingResult
	}

	def readFromDB(config: DataBaseConfiguration): ReadingResult = {
		new DataBaseReader(config).readFromDB()
	}
}
