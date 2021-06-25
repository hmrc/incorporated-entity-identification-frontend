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

import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{Registered, RegistrationFailed}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.RegisterStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class RegistrationConnectorISpec extends ComponentSpecHelper with RegisterStub {

  private val registrationConnector = app.injector.instanceOf[RegistrationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "register" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubRegister(testCompanyNumber, testCtutr)(OK, Registered(testSafeId))

        val result = await(registrationConnector.register(testCompanyNumber, testCtutr))

        result mustBe Registered(testSafeId)
      }
    }
    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        stubRegister(testCompanyNumber, testCtutr)(OK, RegistrationFailed)

        val result = await(registrationConnector.register(testCompanyNumber, testCtutr))

        result mustBe RegistrationFailed
      }
    }
  }

}
