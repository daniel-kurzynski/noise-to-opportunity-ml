package de.hpi.smm.classification

import com.blog_intelligence.nto.Document
import de.hpi.smm.data_reader.DataReader
import weka.classifiers.Evaluation

class GroupedProductClassifier(val brochures: List[Document], val posts: List[Document], classNames: List[String]) {

	val classNamesWithoutNone = classNames.filter { className => className != "None"}

	val classValues = classNames.zipWithIndex.toMap

	val classifiers = classNamesWithoutNone.map { className =>
		className -> new ProductClassifier(className, brochures, posts)
	}.toMap

	def classProbability(text: String): List[Classification] = {

		var result = classNamesWithoutNone.map { className =>
			Classification(className,classifiers(className).classProbability(text))
		}

		val maxDistValue = result.map { classification =>
			classification.classificationOutput.prob
		}.max

		result ::= Classification("None",ClassificationOutput(maxDistValue, new Array[Array[Any]](0)))

		result.sortBy(-_.classificationOutput.prob)
	}

	def validate(): Evaluation = {

		var confusionMatrix = Array.ofDim[Int](classNames.length,classNames.length)
		posts.foreach { post =>
			val actualClassValue = classValues(post.documentClass)
			val predicetedClass = classProbability(post.wholeText)(0).cls
			val predictedClassValue = classValues(predicetedClass)

			confusionMatrix(actualClassValue)(predictedClassValue) += 1
		}

		throw new RuntimeException("Not implemented yet!")
	}
}
