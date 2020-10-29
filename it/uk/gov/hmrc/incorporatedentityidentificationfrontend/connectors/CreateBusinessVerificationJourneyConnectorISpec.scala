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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers.{CREATED, NOT_FOUND, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.{testContinueUrl, testCtutr, testJourneyId}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{BusinessVerificationStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class CreateBusinessVerificationJourneyConnectorISpec extends ComponentSpecHelper with BusinessVerificationStub with IncorporatedEntityIdentificationStub {

  private lazy val createBusinessVerificationJourneyConnector = app.injector.instanceOf[CreateBusinessVerificationJourneyConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "createBusinessVerificationJourneyConnector" should {
    "return the redirectUri" when {
      "the journey creation has been successful" in {
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(CREATED, Json.obj("redirectUri" -> testContinueUrl))

        val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr))

        result mustBe Some(testContinueUrl)
      }

    }
    "return None" when {
      "the journey creation has been unsuccessful" in {
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(NOT_FOUND, Json.obj("redirectUri" -> testContinueUrl))

        val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testCtutr))

        result mustBe None
      }
    }
  }

}
