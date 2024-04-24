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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers.{CREATED, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.testJourneyId
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.JourneyStub
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.JourneyConnector

class JourneyConnectorISpec extends ComponentSpecHelper with JourneyStub {

  private val journeyConnector = app.injector.instanceOf[JourneyConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "createJourney" should {
    "return the journeyId" in {
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      val result = await(journeyConnector.createJourney())

      result mustBe testJourneyId
    }
  }

}
