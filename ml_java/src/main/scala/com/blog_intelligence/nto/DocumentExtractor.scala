package com.blog_intelligence.nto

import java.io.File

import de.hpi.smm.data_reader.DataBaseReader

case class DataBaseConfiguration(
	host: String,
	port: String,
	username: String,
	password: String,
	database: String
);

case class ReadingResult(demandDocuments: java.util.List[Document], productDocuments: java.util.List[Document])

class DocumentExtractor {

	def readFromCSV(demand_file: File, product_file: File, classification_file: File): ReadingResult = {
		println("I am doing nothing.")
		ReadingResult(new java.util.ArrayList[Document](), new java.util.ArrayList[Document]())
	}

	def readFromDB(config: DataBaseConfiguration): ReadingResult = {
		println("I am doing nothing.")
		new DataBaseReader(config).readFromDB()
	}
}
