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

import play.api.http.Status.{UNAUTHORIZED, BAD_GATEWAY, BAD_REQUEST}
import play.api.libs.json.Json
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{Registered, RegistrationFailed}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.RegisterStub
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RegistrationConnector

class RegistrationConnectorISpec extends ComponentSpecHelper with RegisterStub {

  private val registrationConnector = app.injector.instanceOf[RegistrationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val singleRegistrationFailure = Json.arr(Json.obj(
    "code" -> "PARTY_TYPE_MISMATCH",
    "reason" -> "The remote endpoint has indicated there is Party Type mismatch"
  ))

  private val multipleRegistrationFailure = Json.arr(Json.obj(
    "code" -> "INVALID_REGIME",
    "reason" -> "Request has not passed validation.  Invalid regime"
  ),
    Json.obj(
      "code" -> "INVALID_PAYLOAD",
      "reason" -> "Request has not passed validation. Invalid payload."
    ))

  "registerLimitedCompany" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(OK, testSuccessfulRegistrationJson)

        val result = await(registrationConnector.registerLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig))

        result mustBe Registered(testSafeId)
        verifyLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)
      }
    }
    "return RegistrationFailed" when {
      "the registration has not been successful" in {

        stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(OK, testFailedRegistrationJson(singleRegistrationFailure))

        val result = await(registrationConnector.registerLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig))

        result match {
          case RegistrationFailed(Some(failures)) => failures mustBe testRegistrationFailure
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
        verifyLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)
      }
      "multiple failures have been returned" in {

        stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(OK, testFailedRegistrationJson(multipleRegistrationFailure))

        val result = await(registrationConnector.registerLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig))

        result match {
          case RegistrationFailed(Some(failures)) => failures mustBe testMultipleRegistrationFailure
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
      }
    }

    "throws InternalServerException" when {
      "the registration http status is different from Ok and INTERNAL_SERVER_ERROR" in {
        stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(UNAUTHORIZED, Json.obj())

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig))
        }
        actualException.getMessage mustBe s"Unexpected response from Register API - status = 401, body = {}"
      }

      "the registration http status is 502 (Bad Gateway)" in {
        // body shape is irrelevant for unexpected status; use placeholder json
        stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(BAD_GATEWAY, Json.obj("html" -> "downstream"))

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig))
        }
        actualException.getMessage mustBe s"Unexpected response from Register API - status = 502, body = {\"html\":\"downstream\"}"
      }

      "the registration returns 400 with unexpected json shape" in {
        stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(BAD_REQUEST, Json.obj("oops" -> 1))

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig))
        }
        actualException.getMessage mustBe s"Unexpected response from Register API - status = 400, body = {\"oops\":1}"
      }
    }
  }

  "registerRegisteredSociety" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(OK, testSuccessfulRegistrationJson)

        val result = await(registrationConnector.registerRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig))

        result mustBe Registered(testSafeId)

        verifyRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)
      }
    }
    "return RegistrationFailed" when {
      "the registration has not been successful" in {

        stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(OK, testFailedRegistrationJson(singleRegistrationFailure))

        val result = await(registrationConnector.registerRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig))

        result match {
          case RegistrationFailed(Some(failures)) => failures mustBe testRegistrationFailure
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
        verifyRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)
      }

      "multiple failures have been returned" in {

        stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(OK, testFailedRegistrationJson(multipleRegistrationFailure))

        val result = await(registrationConnector.registerRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig))

        result match {
          case RegistrationFailed(Some(failures)) => failures mustBe testMultipleRegistrationFailure
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
      }

    }

    "throws InternalServerException" when {
      "the registration http status is different from Ok and INTERNAL_SERVER_ERROR" in {
        stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(UNAUTHORIZED, Json.obj())

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig))
        }
        actualException.getMessage mustBe s"Unexpected response from Register API - status = 401, body = {}"
      }

      "the registration http status is 502 (Bad Gateway)" in {
        stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(BAD_GATEWAY, Json.obj("html" -> "downstream"))

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig))
        }
        actualException.getMessage mustBe s"Unexpected response from Register API - status = 502, body = {\"html\":\"downstream\"}"
      }

      "the registration returns 400 with unexpected json shape" in {
        stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(BAD_REQUEST, Json.obj("oops" -> 1))

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig))
        }
        actualException.getMessage mustBe s"Unexpected response from Register API - status = 400, body = {\"oops\":1}"
      }
    }
  }

}
