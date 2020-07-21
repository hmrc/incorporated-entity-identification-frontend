
package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CaptureCompanyNumberTests


class CaptureCompanyNumberControllerISpec extends ComponentSpecHelper with CaptureCompanyNumberTests {

  "GET /company-number" should {
    lazy val result: WSResponse = get("/company-number")

    "return OK" in {
      result.status mustBe OK
    }
    "return a view which" should {
      testCaptureCompanyNumberView(result)
    }
  }

  "POST /company-number" should {
    lazy val result = post("/company-number")()

    "return NotImplemented" in {
      result.status mustBe NOT_IMPLEMENTED
    }
  }

}




