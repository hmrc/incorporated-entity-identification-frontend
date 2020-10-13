/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.{testCtutr, testInternalId, testJourneyId}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class CaptureBusinessVerificationResultControllerISpec extends ComponentSpecHelper with AuthStub
  with BusinessVerificationStub with IncorporatedEntityIdentificationStub {

  "GET /business-verification-result" should {
    "return Ok" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = get(s"/business-verification-result")

      result.status mustBe OK
    }
  }

  "POST /:journeyId/business-verification-result" should {
    "redirect to returned redirectUri" in {
      stubRetrieveCtutr(testJourneyId)(OK, testCtutr)
      stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(CREATED, Json.obj("redirectUri" -> "/test"))

      lazy val result = post(s"/$testJourneyId/business-verification-result")()

      result.status mustBe SEE_OTHER
    }
    "return Not Implemented" in {
      stubRetrieveCtutr(testJourneyId)(OK, testCtutr)
      stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(NOT_FOUND)

      lazy val result = post(s"/$testJourneyId/business-verification-result")()

      result.status mustBe NOT_IMPLEMENTED
    }


  }
}
