package de.hpi.smm

import de.hpi.smm.amazon_crawling.{Result, AmazonDescriptionSearch}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
//
@RunWith(classOf[JUnitRunner])
class AmazonDescriptionSearchTest extends FunSuite {
	test("containsFilterWord filters title") {
		val search = new AmazonDescriptionSearch(List(), "searchIndex", "product", List("filteredWord"), "language")
		val result = new Result("filteredWord anotherWord", "anotherWord")
		assert(search.containsFilterWord(result))
	}

	test("containsFilterWord filters description") {
		val search = new AmazonDescriptionSearch(List(), "searchIndex", "product", List("filteredWord"), "language")
		val result = new Result("filteredWord anotherWord", "anotherWord")
		assert(search.containsFilterWord(result))
	}

	test("containsFilterWord filters not, if filtered word is not contained") {
		val search = new AmazonDescriptionSearch(List(), "searchIndex", "product", List("filteredWord"), "language")
		val result = new Result("anotherWord anotherWord2", "anotherWord anotherWord2")
		assert(!search.containsFilterWord(result))
	}
}
