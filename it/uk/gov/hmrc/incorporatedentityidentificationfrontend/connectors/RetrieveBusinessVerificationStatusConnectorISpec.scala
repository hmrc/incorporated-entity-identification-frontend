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
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.testBusinessVerificationJourneyId
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationPass, BusinessVerificationUnchallenged}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.BusinessVerificationStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class RetrieveBusinessVerificationStatusConnectorISpec extends ComponentSpecHelper with BusinessVerificationStub {

  private val retrieveBusinessVerificationStatusConnector = app.injector.instanceOf[RetrieveBusinessVerificationStatusConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "retrieveBusinessVerificationStatus" should {
    "return BvPass" in {
      stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))

      val result = await(retrieveBusinessVerificationStatusConnector.retrieveBusinessVerificationStatus(testBusinessVerificationJourneyId))

      result mustBe BusinessVerificationPass
    }
    "return BvFail" in {
      stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "FAIL"))

      val result = await(retrieveBusinessVerificationStatusConnector.retrieveBusinessVerificationStatus(testBusinessVerificationJourneyId))

      result mustBe BusinessVerificationFail
    }
  }

}
