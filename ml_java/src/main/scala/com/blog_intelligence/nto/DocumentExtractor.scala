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

	def readFromCSV(demandFile: File, productFile: File, classificationFile: File): ReadingResult = {
		new DataReader(demandFile, productFile).getReadingResult
	}

	def readFromDB(config: DataBaseConfiguration): ReadingResult = {
		new DataBaseReader(config).readFromDB()
	}
}
