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

	def readFromCSV(data_file: File, classification_file: File, class_key: String): java.util.List[Document] = {
		println("I am doing nothing.")
		new java.util.ArrayList[Document]()
	}

	def readFromDB(config: DataBaseConfiguration): ReadingResult = {
		println("I am doing nothing.")
		new DataBaseReader(config).readFromDB()
	}
}
