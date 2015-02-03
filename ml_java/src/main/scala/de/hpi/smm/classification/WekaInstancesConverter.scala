package de.hpi.smm.classification

import java.util

import com.blog_intelligence.nto.{RawDocument, Document}
import de.hpi.smm.nlp.NLP
import weka.core.{DenseInstance, Instances, Attribute}
import scala.collection.JavaConverters._

class WekaInstancesConverter {

	var textAttribute = null.asInstanceOf[Attribute]
	var classAttribute = null.asInstanceOf[Attribute]
	var attributes = null.asInstanceOf[java.util.ArrayList[Attribute]]

	def fit(classNames: List[String]): Unit ={
		textAttribute = new Attribute("text", null.asInstanceOf[util.ArrayList[String]])
		classAttribute = new Attribute("@@class@@", classNames.asJava)

		attributes = new util.ArrayList[Attribute]()
		attributes.add(textAttribute)
		attributes.add(classAttribute)
	}

	def convert(name: String, documents : List[Document]):Instances = {
		val instances = new Instances(name, attributes, 0)
		instances.setClassIndex(classAttribute.index())
		documents.foreach { document =>
			instances.add(buildInstance(document))
		}
		instances
	}

	def buildInstance(document: Document): DenseInstance = {
		val text = document.textTokens.mkString(" ")

		val values = new Array[Double](attributes.size())
		values(classAttribute.index()) = classAttribute.indexOfValue(document.documentClass)
		values(textAttribute.index()) = textAttribute.addStringValue(text)

		new DenseInstance(1, values)
	}

	def buildPostInstance(text: String, className: String): DenseInstance = {
		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = NLP.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, className)

		buildInstance(post)

	}
}


