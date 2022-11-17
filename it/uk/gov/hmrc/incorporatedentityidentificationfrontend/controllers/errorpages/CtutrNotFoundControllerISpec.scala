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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages

import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{NO_CONTENT, OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.{routes => appRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.LimitedCompany
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.errorpages.CtutrNotFoundViewTests

class CtutrNotFoundControllerISpec extends ComponentSpecHelper
  with AuthStub
  with CtutrNotFoundViewTests
  with IncorporatedEntityIdentificationStub {

  "GET /error/details-not-found-ctutr" should {
    "return OK" in {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testLimitedCompanyJourneyConfig
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/error/details-not-found-ctutr")

      result.status mustBe OK
    }
    "return a view" when {
      "there is no service name passed in the journey config" should {
        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/error/details-not-found-ctutr")

        testCtutrNotFoundView(result, authStub, insertConfig)
        testServiceName(testDefaultServiceName, result, authStub, insertConfig)
      }
      "there is a service name passed in the journey config" should {
        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = JourneyConfig(
            continueUrl = testContinueUrl,
            pageConfig = PageConfig(
              optServiceName = Some(testCallingServiceName),
              deskProServiceId = testDeskProServiceId,
              signOutUrl = testSignOutUrl,
              accessibilityUrl = testAccessibilityUrl
            ),
            businessEntity = LimitedCompany,
            businessVerificationCheck = true,
            regime = testRegime
          )
        )

        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/error/details-not-found-ctutr")

        testCtutrNotFoundView(result, authStub, insertConfig)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }
    }
  }

  "GET /error/try-again" should {
    "delete journey data and redirect to Capture Company Number page" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRemoveAllData(testJourneyId)(NO_CONTENT)

      val result = get(s"$baseUrl/$testJourneyId/error/try-again")

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(appRoutes.CaptureCompanyNumberController.show(testJourneyId).url)
      )
    }
  }
}
