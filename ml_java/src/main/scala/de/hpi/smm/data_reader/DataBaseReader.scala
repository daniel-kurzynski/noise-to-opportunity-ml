package de.hpi.smm.data_reader

import java.sql.Connection

import com.blog_intelligence.nto.{Document, ReadingResult, DataBaseConfiguration}

class DataBaseReader(dataBaseConfiguration: DataBaseConfiguration) {

	var connection:Connection = null

	private def connect(): Unit = {

	}

	private def disconnect(): Unit = {
		connection.close()
	}

	def readFromDB(): ReadingResult = {
		new ReadingResult(new java.util.ArrayList[Document](), new java.util.ArrayList[Document]())
	}

}

object DataBaseReader {
	val CONFIG = new DataBaseConfiguration("141.89.225.134","30315","SMA1415", "Popcorn54","SMA1415.CLASSIFIED_POSTS")
}
