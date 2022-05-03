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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.http.Status.FORBIDDEN
import play.api.libs.json.Json
import play.api.test.Helpers.{CREATED, NOT_FOUND, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.{testContinueUrl, testCtutr, testJourneyId, testLimitedCompanyJourneyConfig, testLimitedCompanyJourneyConfigWithServiceName}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{BusinessVerificationStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyCreated, NotEnoughEvidence, UserLockedOut}

class CreateBusinessVerificationJourneyConnectorISpec extends ComponentSpecHelper with BusinessVerificationStub with IncorporatedEntityIdentificationStub {

  private lazy val createBusinessVerificationJourneyConnector = app.injector.instanceOf[CreateBusinessVerificationJourneyConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "createBusinessVerificationJourneyConnector" should {
    val redirectUri = Json.obj("redirectUri" -> testContinueUrl)

    "return the redirectUri and therefore no BV status" when {
      "the journey creation has been successful with service name" in {
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId, testLimitedCompanyJourneyConfigWithServiceName)(CREATED, redirectUri)

        val result = await(createBusinessVerificationJourneyConnector
          .createBusinessVerificationJourney(testJourneyId, testCtutr, testLimitedCompanyJourneyConfigWithServiceName))

        result mustBe Right(JourneyCreated(testContinueUrl))
      }
      "the journey creation has been successful with default service name" in {
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId, testLimitedCompanyJourneyConfigWithServiceName)(CREATED, redirectUri)

        val result = await(createBusinessVerificationJourneyConnector
          .createBusinessVerificationJourney(testJourneyId, testCtutr, testLimitedCompanyJourneyConfig))

        result mustBe Right(JourneyCreated(testContinueUrl))
      }

    }
    "return the redirectUri and therefore no BV status" when {
      "the journey creation has been successful" in {
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId, testLimitedCompanyJourneyConfigWithServiceName)(CREATED, redirectUri)

        val result = await(createBusinessVerificationJourneyConnector
          .createBusinessVerificationJourney(testJourneyId, testCtutr, testLimitedCompanyJourneyConfigWithServiceName))

        result mustBe Right(JourneyCreated(testContinueUrl))
      }

    }
    "return no redirect URL and an appropriate BV status" when {
      "the journey creation has been unsuccessful because BV cannot find the record" in {
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId, testLimitedCompanyJourneyConfigWithServiceName)(NOT_FOUND, redirectUri)

        val result = await(createBusinessVerificationJourneyConnector
          .createBusinessVerificationJourney(testJourneyId, testCtutr, testLimitedCompanyJourneyConfig))

        result mustBe Left(NotEnoughEvidence)
      }
      "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId,
          testLimitedCompanyJourneyConfigWithServiceName)(FORBIDDEN, redirectUri)

        val result = await(createBusinessVerificationJourneyConnector
          .createBusinessVerificationJourney(testJourneyId, testCtutr, testLimitedCompanyJourneyConfigWithServiceName))

        result mustBe Left(UserLockedOut)
      }
    }
  }

}
