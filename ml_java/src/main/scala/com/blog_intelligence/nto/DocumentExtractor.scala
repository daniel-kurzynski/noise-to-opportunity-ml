package com.blog_intelligence.nto

import java.io.File

import de.hpi.smm.data_reader.{DataBaseReader, DataReader}

case class DataBaseConfiguration(
	host: String,
	port: String,
	username: String,
	password: String,
	database: String
)

case class ReadingResult(demandDocuments: java.util.List[Document], productDocuments: java.util.List[Document])

class DocumentExtractor {

	def readFromCSV(demandFile: File, productFile: File, classificationFile: File): ReadingResult = {
		new DataReader(demandFile, productFile, classificationFile).getReadingResult
	}

	def readFromDB(config: DataBaseConfiguration): ReadingResult = {
		new DataBaseReader(config).readFromDB()
	}
}
