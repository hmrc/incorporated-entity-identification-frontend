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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.errorpages.CtutrMismatchViewTests
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.{routes => appRoutes}
import scala.concurrent.ExecutionContext.Implicits.global


class CtutrMismatchControllerISpec extends ComponentSpecHelper with CtutrMismatchViewTests with AuthStub {

  override def afterEach(): Unit = {
    super.afterEach()
    journeyConfigRepository.drop
  }

  "GET /error/could-not-confirm-business" when {
    "return ok" in {
      await(insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result: WSResponse = get(s"/$testJourneyId/error/could-not-confirm-business")

      result.status mustBe OK
    }

    "return a view which" should {
      lazy val insertConfig = insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None)
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result: WSResponse = get(s"/$testJourneyId/error/could-not-confirm-business")

      testCtutrMismatchView(result, authStub, insertConfig)
    }
    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None)
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result: WSResponse = get(s"/$testJourneyId/error/could-not-confirm-business")

        testCtutrMismatchView(result, authStub, insertConfig)
        testServiceName(testDefaultServiceName, result, authStub, insertConfig)
      }

      "there is a serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = Some(testCallingServiceName))
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result: WSResponse = get(s"/$testJourneyId/error/could-not-confirm-business")

        testCtutrMismatchView(result, authStub, insertConfig)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }
    }
  }

  "POST /error/could-not-confirm-business" should {
    "redirect to Capture Company Number Page" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result = post(s"/$testJourneyId/error/could-not-confirm-business")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(appRoutes.CaptureCompanyNumberController.show(testJourneyId).url)
      )
    }
  }

}

