
package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WireMockMethods

trait BusinessVerificationStub extends WireMockMethods {

  def stubCreateBusinessVerificationJourney(ctutr: String,
                                            journeyId: String
                                           )(status: Int,
                                             body: JsObject = Json.obj()): StubMapping = {

    val postBody = Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> "vat",
      "identifiers" -> Json.arr(
        Json.obj(
          "ctUtr" -> ctutr
        )
      ),
      "continueUrl" -> routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url
    )

    when(method = POST, uri = "/verification-question/journey", postBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def stubRetrieveBusinessVerificationResult(journeyId: String)
                                            (status: Int,
                                             body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/verification-question/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

  def stubCreateBusinessVerificationJourneyFromStub(ctutr: String,
                                                    journeyId: String
                                                   )(status: Int,
                                                     body: JsObject = Json.obj()): StubMapping = {

    val postBody = Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> "vat",
      "identifiers" -> Json.arr(
        Json.obj(
          "ctUtr" -> ctutr
        )
      ),
      "continueUrl" -> routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url
    )

    when(method = POST, uri = "/identify-your-incorporated-business/test-only/verification-question/journey", postBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def stubRetrieveBusinessVerificationResultFromStub(journeyId: String)
                                                    (status: Int,
                                                     body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/identify-your-incorporated-business/test-only/verification-question/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

}
