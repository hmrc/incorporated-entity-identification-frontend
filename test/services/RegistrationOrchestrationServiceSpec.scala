/*
 * Copyright 2021 HM Revenue & Customs
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
import services.mocks.MockIncorporationEntityInformationService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{Registered, RegistrationFailed, RegistrationNotCalled, SuccessfullyStored}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.RegistrationOrchestrationService
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationOrchestrationServiceSpec extends UnitSpec with MockIncorporationEntityInformationService with MockRegistrationConnector {

  object TestService extends RegistrationOrchestrationService(
    mockIncorporationEntityInformationService,
    mockRegistrationConnector
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "register" should {
    "store the registration response" when {
      "the business entity is successfully verified and then registered" in {
        mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
        mockRegister(testCompanyNumber, testCtutr)(Future.successful(Registered(testSafeId)))
        mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

        await(TestService.register(testJourneyId)) mustBe {
          Registered(testSafeId)
        }
        verifyRegistration(testCompanyNumber, testCtutr)
        verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
      }

      "when the business entity is verified but fails to register" in {
        mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
        mockRegister(testCompanyNumber, testCtutr)(Future.successful(RegistrationFailed))
        mockStoreRegistrationResponse(testJourneyId, RegistrationFailed)(Future.successful(SuccessfullyStored))

        await(TestService.register(testJourneyId)) mustBe {
          RegistrationFailed
        }
        verifyRegistration(testCompanyNumber, testCtutr)
        verifyStoreRegistrationResponse(testJourneyId, RegistrationFailed)
      }

      "the business has an IR-CT enrolment and then registers" in {
        mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testCtEnrolledStatus)))
        mockRegister(testCompanyNumber, testCtutr)(Future.successful(Registered(testSafeId)))
        mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

        await(TestService.register(testJourneyId)) mustBe {
          Registered(testSafeId)
        }
        verifyRegistration(testCompanyNumber, testCtutr)
        verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
      }
    }
  }

  "store a registration state of registration not called" when {
    "the business entity did not pass verification" in {
      mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
      mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
      mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testFailedBusinessVerificationStatus)))
      mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

      await(TestService.register(testJourneyId)) mustBe {
        RegistrationNotCalled
      }
      verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
    }

    "the business entity was not challenged to verify" in {
      mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
      mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
      mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testUnchallengedBusinessVerificationStatus)))
      mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

      await(TestService.register(testJourneyId)) mustBe {
        RegistrationNotCalled
      }
      verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
    }
  }

  "throw an Internal Server Exception" when {
    "there is no ctutr in the database" in {
      mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
      mockRetrieveCtutr(testJourneyId)(Future.successful(None))
      mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))

      intercept[InternalServerException](
        await(TestService.register(testJourneyId))
      )
    }

    "there is no company profile in the database" in {
      mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
      mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
      mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))

      intercept[InternalServerException](
        await(TestService.register(testJourneyId))
      )
    }

    "there is no business verification response in the database" in {
      mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
      mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
      mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

      intercept[InternalServerException](
        await(TestService.register(testJourneyId))
      )
    }

    "there is nothing in the database" in {
      mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
      mockRetrieveCtutr(testJourneyId)(Future.successful(None))
      mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

      intercept[InternalServerException](
        await(TestService.register(testJourneyId))
      )
    }
  }

}
