package de.hpi.smm.domain

import de.hpi.smm.Main.DemandCounts

import scala.collection.mutable

class DemandCountsCounter extends mutable.HashMap[String, DemandCounts] {


	var demandPostNumber: Int = 0
	var noDemandPostNumber: Int = 0
	def newDemandPost(): Unit = demandPostNumber += 1
	def newNoDemandPost(): Unit = noDemandPostNumber += 1


	private def calculateRatios(): List[(String, DemandCounts, Double, Double)] = {
		toList.map { case (word, currentCounts) =>
			val demandProb   = currentCounts.demand.toDouble / demandPostNumber
			val noDemandProb = currentCounts.noDemandCount.toDouble / noDemandPostNumber
			val relation = demandProb / noDemandProb

			val demandMissingProb   = (demandPostNumber - currentCounts.demand).toDouble / demandPostNumber
			val noDemandMissingProb = (noDemandPostNumber - currentCounts.noDemandCount).toDouble / noDemandPostNumber
			val missingRelation = noDemandMissingProb / demandMissingProb
			(word, currentCounts, if (relation.isInfinite) 0 else relation, if (missingRelation.isInfinite) 0 else missingRelation)
		}
	}

	def takeTopOccurrence(i: Int): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._3).take(10)
	}
	def takeTopOccurrence(threshold: Double): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._3).takeWhile { t => t._3 > threshold }
	}
	def takeTopNotOccurrence(i: Int): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._4).take(10)
	}
	def takeTopNotOccurrence(threshold: Double): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._4).takeWhile { t => t._4 > threshold }
	}

}
