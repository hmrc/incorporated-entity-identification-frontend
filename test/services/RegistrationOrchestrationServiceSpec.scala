/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import connectors.mocks.MockRegistrationConnector
import helpers.TestConstants._
import play.api.test.Helpers._
import services.mocks.MockStorageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.RegistrationOrchestrationService
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationOrchestrationServiceSpec extends UnitSpec with MockStorageService with MockRegistrationConnector {

  object TestService extends RegistrationOrchestrationService(
    mockStorageService,
    mockRegistrationConnector
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "register" when {

    "the business entity is Limited Company" should {
      "store the registration response" when {
        "the business entity is successfully registered" in {

          mockRegisterLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig)(Future.successful(Registered(testSafeId)))

          await(TestService.register(testJourneyId, testLimitedCompanyJourneyConfig)) mustBe {
            Registered(testSafeId)
          }

          verifyRegistrationLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig)
        }

        "when the business entity fails to register" in {

          mockRegisterLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig)(Future.successful(RegistrationFailed(Some(testRegistrationFailure))))

          await(TestService.register(testJourneyId, testLimitedCompanyJourneyConfig)) mustBe {
            RegistrationFailed(Some(testRegistrationFailure))
          }

          verifyRegistrationLimitedCompany(testJourneyId, testLimitedCompanyJourneyConfig)
        }

      }

    }

    "the business entity is Registered Society" should {
      "store the registration response" when {
        "the business entity is successfully registered" in {

          mockRegisterRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig)(Future.successful(Registered(testSafeId)))

          await(TestService.register(testJourneyId, testRegisteredSocietyJourneyConfig)) mustBe {
            Registered(testSafeId)
          }

          verifyRegistrationRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig)
        }

        "when the business entity fails to register" in {

          mockRegisterRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig)(Future.successful(RegistrationFailed(Some(testRegistrationFailure))))

          await(TestService.register(testJourneyId, testRegisteredSocietyJourneyConfig)) mustBe {
            RegistrationFailed(Some(testRegistrationFailure))
          }

          verifyRegistrationRegisteredSociety(testJourneyId, testRegisteredSocietyJourneyConfig)
        }

      }

    }

    "the business entity is Charitable Incorporated Organisation" should {
      "store registration not called" when {
        "the registration service is invoked" in {

          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testCharitableIncorporatedOrganisationJourneyConfig)) mustBe {
            RegistrationNotCalled
          }

          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }
      }
    }
  }

}
