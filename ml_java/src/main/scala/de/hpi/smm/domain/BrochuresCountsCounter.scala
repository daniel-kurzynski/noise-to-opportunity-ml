package de.hpi.smm.domain

import scala.collection.mutable

case class BrochureCounts(var crm: Int = 0, var ecom: Int = 0, var hcm: Int = 0, var lvm: Int = 0)

class BrochuresCountsCounter extends mutable.HashMap[String, BrochureCounts] {
	val classCounts = mutable.Map[String, Int]().withDefaultValue(0)

	def overallBrochures: Int = classCounts.values.sum

  private def non_infinity(value: Double): Double = {
    if (value.isInfinite) 0 else value
  }

	private def calculateRatios(): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
		toList.map { case (word, currentCounts) =>
			val crmProb  = (currentCounts.crm.toDouble + 1) / (classCounts("CRM") + 1)
			val ecomProb = (currentCounts.ecom.toDouble + 1) / (classCounts("ECOM") + 1)
			val hcmProb  = (currentCounts.hcm.toDouble + 1) / (classCounts("HCM") + 1)
			val lvmProb  = (currentCounts.lvm.toDouble + 1) / (classCounts("LVM") + 1)


      val crmRelation  = crmProb  / (ecomProb + hcmProb + lvmProb)
      val ecomRelation = ecomProb / (crmProb + hcmProb + lvmProb)
      val hcmRelation  = hcmProb  / (ecomProb + crmProb + lvmProb)
      val lvmRelation  = lvmProb  / (ecomProb + hcmProb + crmProb)

      val crmMissingProb  = 1 - crmProb
      val ecomMissingProb = 1 - ecomProb
      val hcmMissingProb  = 1 - hcmProb
      val lvmMissingProb  = 1 - lvmProb

      val crmMissingRelation  = (ecomMissingProb + hcmMissingProb + lvmMissingProb) / crmMissingProb
      val ecomMissingRelation = (crmMissingProb + hcmMissingProb + lvmMissingProb) / ecomMissingProb
      val hcmMissingRelation  = (ecomMissingProb + crmMissingProb + lvmMissingProb) / hcmMissingProb
      val lvmMissingRelation  = (ecomMissingProb + hcmMissingProb + crmMissingProb) / lvmMissingProb

      // val demandMissingProb   = (classCounts("demand") - currentCounts.demand).toDouble / classCounts("demand")

			(word, currentCounts, non_infinity(crmRelation), non_infinity(ecomRelation), non_infinity(hcmRelation), non_infinity(lvmRelation),
        non_infinity(crmMissingRelation), non_infinity(ecomMissingRelation), non_infinity(hcmMissingRelation), non_infinity(lvmMissingRelation))

//			val relation = crmProb / noDemandProb
//
//			val demandMissingProb   = (classCounts("demand") - currentCounts.demand).toDouble / classCounts("demand")
//			val noDemandMissingProb = (classCounts("no-demand") - currentCounts.noDemandCount).toDouble / classCounts("no-demand")
//			val missingRelation = noDemandMissingProb / demandMissingProb
//			(word, currentCounts, if (relation.isInfinite) 0 else relation, if (missingRelation.isInfinite) 0 else missingRelation)
		}
	}

  def takeCRM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._3).take(i)
  }
  def takeECOM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._4).take(i)
  }
  def takeHCM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._5).take(i)
  }
  def takeLVM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._6).take(i)
  }


  def takeNonCRM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._7).take(i)
  }
  def takeNonECOM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._8).take(i)
  }
  def takeNonHCM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._9).take(i)
  }
  def takeNonLVM(i: Int): List[(String, BrochureCounts, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    calculateRatios().sortBy(-_._10).take(i)
  }
}
