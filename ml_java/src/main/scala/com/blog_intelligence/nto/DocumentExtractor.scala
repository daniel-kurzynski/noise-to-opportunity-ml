package com.blog_intelligence.nto

import java.io.File

import de.hpi.smm.data_reader.{DataBaseReader, DataReader}
import de.hpi.smm.nlp.NLP

case class DataBaseConfiguration(
	host: String,
	port: String,
	username: String,
	password: String,
	database: String
)

case class ReadingResult(demandDocuments: java.util.List[Document], productDocuments: java.util.List[Document])

class DocumentExtractor(stopWordsFile: File, posModelFile: File) {

	val nlp = new NLP(stopWordsFile, posModelFile)
	def readFromCSV(demandFile: File, productFile: File, classificationFile: File): ReadingResult = {
		new DataReader(demandFile, productFile, classificationFile, nlp).getReadingResult
	}

	def readFromDB(config: DataBaseConfiguration): ReadingResult = {
		new DataBaseReader(config, nlp).readFromDB()
	}
}
