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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationPass, BusinessVerificationUnchallenged}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class BusinessVerificationControllerISpec extends ComponentSpecHelper with AuthStub
  with BusinessVerificationStub with IncorporatedEntityIdentificationStub {

  "GET /business-verification-result" should {
    "redirect to /:journeyId/register if BV status is stored successfully" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
      stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)

      lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

      result.status mustBe SEE_OTHER
      result.header(LOCATION) mustBe Some(routes.RegistrationController.register(testJourneyId).url)
    }

    "throw an exception when the query string is missing" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))

      lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result")

      result.status mustBe INTERNAL_SERVER_ERROR
    }
  }

  "GET /:journeyId/start-business-verification" should {
    "redirect to business verification redirectUri" when {
      "business verification returns a journey to redirect to" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCtutr(testJourneyId)(OK, testCtutr)
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(CREATED, Json.obj("redirectUri" -> testContinueUrl))

        lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(testContinueUrl)
      }
    }

    "store a verification state of UNCHALLENGED and redirect to the registration controller" when {
      "business verification does not have enough information to create a verification journey" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCtutr(testJourneyId)(OK, testCtutr)
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(NOT_FOUND)
        stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)

        lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.RegistrationController.register(testJourneyId).url)
      }
    }
  }
}
