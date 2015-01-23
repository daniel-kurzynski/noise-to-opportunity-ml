package de.hpi.smm.domain

import java.util

import com.blog_intelligence.nto.Document
import weka.core.{DenseInstance, Attribute, Instances}
import scala.collection.JavaConverters._
/**
 * Created by Daniel on 19.01.2015.
 */
class WekaConverter(classNames: List[String]) {

	val attributes = new util.ArrayList[Attribute]()

	val classNamesVector = new util.ArrayList[String]()
	classNamesVector.addAll(classNames.asJavaCollection)

	val textAttribute = new Attribute("text", null.asInstanceOf[util.ArrayList[String]])
	val classAttribute = new Attribute("@@class@@", classNamesVector)

	attributes.add(textAttribute)
	attributes.add(classAttribute)

	def convertToInstances(documents: List[Document]):Instances={
		val instances = new Instances("classifier", attributes,0)
		instances.setClassIndex(classAttribute.index())

		documents.foreach { brochure =>
			instances.add(buildInstance(brochure))
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

}
