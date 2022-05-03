/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait BusinessVerificationStub extends WireMockMethods {

  def stubCreateBusinessVerificationJourney(ctutr: String,
                                            journeyId: String,
                                            journeyConfig: JourneyConfig
                                           )(status: Int,
                                             body: JsObject = Json.obj()): StubMapping = {

    val postBody = Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> journeyConfig.regime.toLowerCase,
      "identifiers" -> Json.arr(
        Json.obj(
          "ctUtr" -> ctutr
        )
      ),
      "continueUrl" -> routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url,
      "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl,
      "deskproServiceName" -> journeyConfig.pageConfig.deskProServiceId
    )

    when(method = POST, uri = "/business-verification/journey", postBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def verifyCreateBusinessVerificationJourney(postData: JsObject): Unit =
    WiremockHelper.verifyPost(uri = "/business-verification/journey", optBody = Some(postData.toString()))

  def stubRetrieveBusinessVerificationResult(journeyId: String)
                                            (status: Int,
                                             body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/business-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

  def stubCreateBusinessVerificationJourneyFromStub(ctutr: String,
                                                    journeyId: String,
                                                    journeyConfig: JourneyConfig
                                                   )(status: Int,
                                                     body: JsObject = Json.obj()): StubMapping = {

    val postBody = Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> journeyConfig.regime.toLowerCase,
      "identifiers" -> Json.arr(
        Json.obj(
          "ctUtr" -> ctutr
        )
      ),
      "continueUrl" -> routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url,
      "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl,
      "deskproServiceName" -> journeyConfig.pageConfig.deskProServiceId
    )

    when(method = POST, uri = "/identify-your-incorporated-business/test-only/business-verification/journey", postBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def verifyCreateBusinessVerificationJourneyFromStub(postData: JsObject): Unit =
    WiremockHelper.verifyPost(uri = "/identify-your-incorporated-business/test-only/business-verification/journey", optBody = Some(postData.toString()))

  def stubRetrieveBusinessVerificationResultFromStub(journeyId: String)
                                                    (status: Int,
                                                     body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/identify-your-incorporated-business/test-only/business-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

}
