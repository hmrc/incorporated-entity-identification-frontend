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

package services

import connectors.mocks.MockRegistrationConnector
import helpers.TestConstants._
import play.api.test.Helpers._
import services.mocks.MockStorageService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
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
        "the business entity is successfully verified and then registered" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegisterLimitedCompany(testCompanyNumber, testCtutr, testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany())) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationLimitedCompany(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }

        "when the business entity is verified but fails to register" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegisterLimitedCompany(testCompanyNumber, testCtutr, testRegime)(Future.successful(RegistrationFailed))
          mockStoreRegistrationStatus(testJourneyId, RegistrationFailed)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany())) mustBe {
            RegistrationFailed
          }
          verifyRegistrationLimitedCompany(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationFailed)
        }

        "the business has an IR-CT enrolment and then registers" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(CtEnrolled)))
          mockRegisterLimitedCompany(testCompanyNumber, testCtutr, testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany())) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationLimitedCompany(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }
      }

      "store a registration state of registration not called" when {
        "business verification status is BusinessVerificationNotEnoughInformationToChallenge" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToChallenge)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany())) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }

        "business verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany())) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }

        "business verification status is BusinessVerificationFail" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany())) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }
      }

      "throw an Internal Server Exception" when {
        "there is no ctutr in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany()))
          )
        }

        "there is no company profile in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany()))
          )
        }

        "there is no business verification response in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany()))
          )
        }

        "there is nothing in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigLimitedCompany()))
          )
        }
      }

      "register without business verification" when {
        "identifiers match but business verification check is false" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRegisterLimitedCompany(testCompanyNumber, testCtutr, testRegime)(Future.successful(Registered(testSafeId)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigLimitedCompanyWithoutBV())) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationLimitedCompany(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }
      }
    }


    "the business entity is Registered Society" should {
      "store the registration response" when {
        "the business entity is successfully verified and then registered" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegisterRegisteredSociety(testCompanyNumber, testCtutr, testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety())) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationRegisteredSociety(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }

        "when the business entity is verified but fails to register" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegisterRegisteredSociety(testCompanyNumber, testCtutr, testRegime)(Future.successful(RegistrationFailed))
          mockStoreRegistrationStatus(testJourneyId, RegistrationFailed)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety())) mustBe {
            RegistrationFailed
          }
          verifyRegistrationRegisteredSociety(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationFailed)
        }

        "the business has an IR-CT enrolment and then registers" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(CtEnrolled)))
          mockRegisterRegisteredSociety(testCompanyNumber, testCtutr, testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety())) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationRegisteredSociety(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }
      }

      "store a registration state of registration not called" when {
        "business verification status is BusinessVerificationNotEnoughInformationToChallenge" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToChallenge)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety())) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }

        "business verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety())) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }

        "business verification status is BusinessVerificationFail" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
          mockStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety())) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        }
      }

      "throw an Internal Server Exception" when {
        "there is no ctutr in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety()))
          )
        }

        "there is no company profile in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety()))
          )
        }

        "there is no business verification response in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety()))
          )
        }

        "there is nothing in the database" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))
          mockRetrieveCtutr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.register(testJourneyId, testJourneyConfigRegisteredSociety()))
          )
        }
      }
      "register without business verification" when {
        "identifiers match but business verification check is false" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
          mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
          mockRegisterRegisteredSociety(testCompanyNumber, testCtutr, testRegime)(Future.successful(Registered(testSafeId)))
          mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
          mockStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.register(testJourneyId, testJourneyConfigRegisteredSocietyWithoutBV())) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationRegisteredSociety(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        }
      }
    }
  }

}
