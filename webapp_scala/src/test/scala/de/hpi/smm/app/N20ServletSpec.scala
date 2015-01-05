package de.hpi.smm.app

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class N20ServletSpec extends ScalatraSpec { def is =
  "GET / on N20Servlet"                     ^
    "should return status 200"                  ! root200^
                                                end

  addServlet(classOf[N20Servlet], "/*")

  def root200 = get("/") {
    status must_== 200
  }
}
