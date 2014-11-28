package de.hpi.smm

case class Post(id: String, title: String, text: String, tokens: List[String]) {
	def data: String = s"$title $text"
}
