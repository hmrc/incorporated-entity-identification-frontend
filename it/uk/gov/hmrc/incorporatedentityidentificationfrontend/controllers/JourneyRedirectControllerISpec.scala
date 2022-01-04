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
package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.LimitedCompany
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.AuthStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class JourneyRedirectControllerISpec extends ComponentSpecHelper with AuthStub {

  "GET /journey/redirect/:journeyId" should {
    "redirect to the journey config continue url" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      await(journeyConfigRepository.insertJourneyConfig(testJourneyId, testInternalId, JourneyConfig(testContinueUrl, PageConfig(None, testDeskProServiceId, testSignOutUrl), LimitedCompany, true)))

      lazy val result = get(s"$baseUrl/journey/redirect/$testJourneyId")

      result.status mustBe SEE_OTHER
      result.header(LOCATION) mustBe Some(testContinueUrl + s"?journeyId=$testJourneyId")
    }

    "return NOT_FOUND" when {
      "the journeyId does not match what is stored in the journey config database" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          businessEntity = LimitedCompany,
          businessVerificationCheck = true
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/journey/redirect/$testJourneyId")

        result.status mustBe NOT_FOUND
      }

      "the auth internal ID does not match what is stored in the journey config database" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId + "1",
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          businessEntity = LimitedCompany,
          businessVerificationCheck = true
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/journey/redirect/$testJourneyId")

        result.status mustBe NOT_FOUND
      }

      "neither the journey ID or auth internal ID are found in the journey config database" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId + "1",
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          businessEntity = LimitedCompany,
          businessVerificationCheck = true
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/journey/redirect/$testJourneyId")

        result.status mustBe NOT_FOUND
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"$baseUrl/journey/redirect/$testJourneyId")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

}
