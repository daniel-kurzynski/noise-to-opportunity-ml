package com.blog_intelligence.nto

import java.io.File

case class DataBaseConfiguration(
	host: String,
	port: String,
	username: String,
	password: String,
	database: String
);

class DocumentExtractor {

	def readFromCSV(data_file: File, classification_file: File, class_key: String): java.util.List[Document] = {
		println("I am doing nothing.")
		new java.util.ArrayList[Document]()
	}

	def readFromDB(host: String, port: Int, username: String, pwd: String): java.util.List[Document] = {
		println("I am doing nothing.")
		new java.util.ArrayList[Document]()
	}
}
