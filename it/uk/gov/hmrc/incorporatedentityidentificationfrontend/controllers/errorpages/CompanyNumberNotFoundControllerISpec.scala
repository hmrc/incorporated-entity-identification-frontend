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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.AuthStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.errorpages.CompanyNumberNotFoundTests
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.{routes => appRoutes}


class CompanyNumberNotFoundControllerISpec extends ComponentSpecHelper with CompanyNumberNotFoundTests with AuthStub {

  "GET /error/company-name-not-found" when {
    "return ok" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/error/company-name-not-found")

      result.status mustBe OK
    }

    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/error/company-name-not-found")

        testCompanyNumberNotFoundView(result, authStub, insertConfig)
        testServiceName(testDefaultServiceName, result, authStub, insertConfig)
      }

      "there is a serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = Some(testCallingServiceName),
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/error/company-name-not-found")

        testCompanyNumberNotFoundView(result, authStub, insertConfig)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"$baseUrl/$testJourneyId/error/company-name-not-found")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /error/company-name-not-found" should {
    "redirect to Capture Company Number Page" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result = post(s"$baseUrl/$testJourneyId/error/company-name-not-found")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(appRoutes.CaptureCompanyNumberController.show(testJourneyId).url)
      )
    }
  }

}