package de.hpi.smm.domain

import scala.collection.mutable

case class ProductCounts(var crm: Int = 0, var ecom: Int = 0, var hcm: Int = 0, var lvm: Int = 0)

class ProductCountsCounter extends mutable.HashMap[String, ProductCounts] {
}
