package de.hpi.smm.data_reader

import java.sql.{DriverManager, Connection}

import com.blog_intelligence.nto.RawDocument
import de.hpi.smm.nlp.NLP
import com.blog_intelligence.nto.{Document, ReadingResult, DataBaseConfiguration}

class DataBaseReader(dataBaseConfiguration: DataBaseConfiguration) {

	private val driver = "com.sap.db.jdbc.Driver"
	private val host = dataBaseConfiguration.host
	private val port = dataBaseConfiguration.port
	private val url = f"jdbc:sap://$host%s:$port%s"


	var connection:Connection = null

	private def connect(): Unit = {
		try {
			// make the connection
			Class.forName(driver)
			connection = DriverManager.getConnection(url, dataBaseConfiguration.username, dataBaseConfiguration.password)
		} catch {
			case e: Throwable => e.printStackTrace()
		}
	}

	private def disconnect(): Unit = {
		connection.close()
	}

	private def readPosts(): ReadingResult = {
		val demandPosts = new java.util.LinkedList[Document]()
		val productPosts = new java.util.LinkedList[Document]()

		try {
			// create the statement, and run the select query
			val statement = connection.createStatement()
			val database = dataBaseConfiguration.database
			val query = f"SELECT POST_ID, POST_TITLE, POST_TEXT, DEMAND_CLASS, PRODUCT_CLASS FROM $database%s"
			val resultSet = statement.executeQuery(query)
			while ( resultSet.next() ) {
				val id = resultSet.getString("POST_ID")
				val title = resultSet.getString("POST_TITLE")
				val text = resultSet.getString("POST_TEXT")
				val demandClass = resultSet.getString("DEMAND_CLASS")
				val productClass = resultSet.getString("PRODUCT_CLASS")

				val rawPost = RawDocument(id, title, text, null)
				val sentences = NLP.detectSentences(rawPost)

				demandPosts.add(Document(id, title, text, sentences, demandClass))
				productPosts.add(Document(id, title, text, sentences, productClass))

			}
		} catch {
			case e: Throwable => e.printStackTrace()
		}
		new ReadingResult(demandPosts, productPosts)
	}

	def readFromDB(): ReadingResult = {
		connect()
		val result = readPosts()
		disconnect()
		result
	}

	private def insertDocumentIntoDatabase(document: Document, classColumn: String, className: String): Unit = {
		val id = document.id
		val title = document.title
		val text = document.text

		val columns = f"POST_ID, POST_TITLE, POST_TEXT, $classColumn"
		val values = f"'$id%s','$title%s','$text%s','$className%s'"

		val query = f"INSERT INTO CLASSIFIED_POSTS ($columns%s) VALUES ($values%s) ON DUPLICATE KEY UPDATE ID = '$id%s'"

		val statement = connection.createStatement()
		statement.execute(query)
	}

	def insertDemandIntoDatabase(documents: List[Document]): Unit = {
		connect()

		documents.foreach { document =>
			insertDocumentIntoDatabase(document, "DEMAND_CLASS", document.documentClass)
		}

		disconnect()
	}

	def insertProductIntoDatabase(documents: List[Document]): Unit = {
		connect()

		documents.foreach { document =>
			insertDocumentIntoDatabase(document, "PRODUCT_CLASS", document.documentClass)
		}

		disconnect()
	}

}

object DataBaseReader {
	def main(args: Array[String]) {
		val config = new DataBaseConfiguration("141.89.225.134","30315","SMA1415", "Popcorn54","SMA1415.CLASSIFIED_POSTS");

		val reader = new DataBaseReader(config)
		reader.readFromDB()

	}
}
