
package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CaptureCompanyNumberTests


class CaptureCompanyNumberControllerISpec extends ComponentSpecHelper with CaptureCompanyNumberTests {

  val testCompanyNumber = "01234567"

  "GET /company-number" should {
    lazy val result: WSResponse = get("/company-number")

    "return OK" in {
      result.status mustBe OK
    }
    "return a view which" should {
      testCaptureCompanyNumberView(result)
    }
  }

  "POST /company-number" when {

    "the company number is correct" should {

      "redirect to the Confirm Business Name page" in {
        lazy val result = post("/company-number")("companyNumber" -> testCompanyNumber)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ConfirmBusinessNameController.show().url)
        )
      }
    }

    "the company number is missing" should {
      lazy val result = post("/company-number")("companyNumber" -> "")
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureCompanyNumberEmpty(result)

    }

    "the company number has more than 8 " should {
      lazy val result = post("/company-number")("companyNumber" -> "0123456789")
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureCompanyNumberWrongLength(result)
    }

    "company number is not in the correct format" should {
      lazy val result = post("/company-number")("companyNumber" -> "13E!!!%")
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureCompanyNumberWrongFormat(result)
    }
  }
}




