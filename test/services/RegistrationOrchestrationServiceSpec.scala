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
import services.mocks.MockStorageService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{Registered, RegistrationFailed, RegistrationNotCalled, SuccessfullyStored}
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
        "the business entity is successfully verified and then registered" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
          mockRegisterLimitedCompany(testCompanyNumber, testCtutr)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, LimitedCompany)) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationLimitedCompany(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }

        "when the business entity is verified but fails to register" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
          mockRegisterLimitedCompany(testCompanyNumber, testCtutr)(Future.successful(RegistrationFailed))
          mockStoreRegistrationStatus(testJourneyId, RegistrationFailed)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, LimitedCompany)) mustBe {
            RegistrationFailed
          }
          verifyRegistrationLimitedCompany(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationFailed)
        }

        "the business has an IR-CT enrolment and then registers" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testCtEnrolledStatus)))
          mockRegisterLimitedCompany(testCompanyNumber, testCtutr)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, LimitedCompany)) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationLimitedCompany(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }
      }

      "store a registration state of registration not called" when {
        "the business entity did not pass verification" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testFailedBusinessVerificationStatus)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, LimitedCompany)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }

        "the business entity was not challenged to verify" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testUnchallengedBusinessVerificationStatus)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, LimitedCompany)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }
      }

      "throw an Internal Server Exception" when {
        "there is no ctutr in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, LimitedCompany))
          )
        }

        "there is no company profile in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, LimitedCompany))
          )
        }

        "there is no business verification response in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, LimitedCompany))
          )
        }

        "there is nothing in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, LimitedCompany))
          )
        }
      }
    }

    "the business entity is Registered Society" should {
      "store the registration response" when {
        "the business entity is successfully verified and then registered" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
          mockRegisterRegisteredSociety(testCompanyNumber, testCtutr)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, RegisteredSociety)) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationRegisteredSociety(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }

        "when the business entity is verified but fails to register" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
          mockRegisterRegisteredSociety(testCompanyNumber, testCtutr)(Future.successful(RegistrationFailed))
          mockStoreRegistrationStatus(testJourneyId, RegistrationFailed)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, RegisteredSociety)) mustBe {
            RegistrationFailed
          }
          verifyRegistrationRegisteredSociety(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationFailed)
        }

        "the business has an IR-CT enrolment and then registers" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testCtEnrolledStatus)))
          mockRegisterRegisteredSociety(testCompanyNumber, testCtutr)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, RegisteredSociety)) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationRegisteredSociety(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }
      }

      "store a registration state of registration not called" when {
        "the business entity did not pass verification" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testFailedBusinessVerificationStatus)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, RegisteredSociety)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }

        "the business entity was not challenged to verify" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testUnchallengedBusinessVerificationStatus)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, RegisteredSociety)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }
      }

      "throw an Internal Server Exception" when {
        "there is no ctutr in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, RegisteredSociety))
          )
        }

        "there is no company profile in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, RegisteredSociety))
          )
        }

        "there is no business verification response in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, RegisteredSociety))
          )
        }

        "there is nothing in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, RegisteredSociety))
          )
        }
      }
    }
  }

}
