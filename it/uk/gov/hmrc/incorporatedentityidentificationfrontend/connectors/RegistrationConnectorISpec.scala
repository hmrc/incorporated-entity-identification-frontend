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

import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{Registered, RegistrationFailed}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.RegisterStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class RegistrationConnectorISpec extends ComponentSpecHelper with RegisterStub {

  private val registrationConnector = app.injector.instanceOf[RegistrationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "registerLimitedCompany" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)(OK, testSuccessfulRegistrationJson)

        val result = await(registrationConnector.registerLimitedCompany(testCompanyNumber, testCtutr, testRegime))

        result mustBe Registered(testSafeId)
        verifyLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)
      }
    }
    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        stubLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)(OK, testFailedRegistrationJson)

        val result = await(registrationConnector.registerLimitedCompany(testCompanyNumber, testCtutr, testRegime))

        result mustBe RegistrationFailed
        verifyLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)
      }
    }
  }
  "registerRegisteredSociety" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)(OK, testSuccessfulRegistrationJson)

        val result = await(registrationConnector.registerRegisteredSociety(testCompanyNumber, testCtutr, testRegime))

        result mustBe Registered(testSafeId)
        verifyRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)
      }
    }
    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        stubRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)(OK, testFailedRegistrationJson)

        val result = await(registrationConnector.registerRegisteredSociety(testCompanyNumber, testCtutr, testRegime))

        result mustBe RegistrationFailed
        verifyRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)
      }
    }
  }

}
