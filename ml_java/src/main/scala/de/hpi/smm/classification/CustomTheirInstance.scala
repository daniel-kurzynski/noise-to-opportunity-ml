package de.hpi.smm.classification

import weka.core.DenseInstance

class CustomTheirInstance(weight: Double, attValues: Array[Double], val theirClassification: String) extends DenseInstance(weight, attValues) {


	def this(instance: DenseInstance, theirClassification: String) = {
		this(instance.weight(), instance.toDoubleArray, theirClassification)

	}

	override def copy(): Object = {
		val result: CustomTheirInstance = new CustomTheirInstance(this, theirClassification)
		result.m_Dataset = m_Dataset
		result
	}
}
