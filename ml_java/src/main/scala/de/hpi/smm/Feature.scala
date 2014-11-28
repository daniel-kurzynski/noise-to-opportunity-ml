package de.hpi.smm

abstract class Feature {

	def extract(post: Post): Double
}
