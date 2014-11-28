package de.hpi.smm

case class RawPost(id: String, title: String, text: String) {
	def wholeText: String = s"$title $text"
}
case class Post(id: String, title: String, text: String, tokens: List[String]) {
	def wholeText: String = s"$title $text"
}
