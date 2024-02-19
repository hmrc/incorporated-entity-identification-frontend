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

import connectors.mocks.MockAuditConnector
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import play.api.test.Helpers._
import services.mocks.{MockJourneyService, MockStorageService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.AuditService
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends UnitSpec with Matchers with MockStorageService with MockJourneyService with MockAuditConnector {

  object TestService extends AuditService(
    mockAuditConnector,
    mockJourneyService,
    mock[AppConfig],
    mockStorageService
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testJourneyConfigLimitedCompany: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = Some(defaultServiceName),
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl
    ),
    LimitedCompany,
    businessVerificationCheck = true,
    regime = testRegime
  )

  val testJourneyConfigRegisteredSociety: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = Some(defaultServiceName),
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl
    ),
    RegisteredSociety,
    businessVerificationCheck = true,
    regime = testRegime
  )

  val testJourneyConfigCIO: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = Some(defaultServiceName),
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl
    ),
    CharitableIncorporatedOrganisation,
    businessVerificationCheck = true,
    regime = testRegime
  )

  "auditJourney" should {
    "send an event for a Limited Company" when {
      "the business entity is successfully verified and then registered" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = success, regStatus = success)
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToChallenge" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToChallenge)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = notEnoughInfoToChallenge, regStatus = notCalled)
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = notEnoughInfoToCall, regStatus = notCalled)
      }
      "the business entity verification status is BusinessVerificationFail" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = failure, regStatus = notCalled)
      }
      "the business entity has a UTR mismatch" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMismatch)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testDetailsUtrMismatchAuditEventJson
      }
      "the business entity is successfully verified but registration fails" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationFailed(Some(testRegistrationFailure)))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = success, regStatus = failure)
      }
      "the business entity verification is undefined and registration is called" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = notRequested, regStatus = success)
      }
      "the business entity is not successfully verified" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = failure, regStatus = notCalled)
      }
      "the business entity is successfully verified as CT Enrolled and then registered" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(CtEnrolled)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanyAuditEventJson(isMatch = "true", bvStatus = ctEnrolled, regStatus = success)
      }
    }

    "send an event for a Registered Society" when {
      "the business entity is successfully verified and then registered" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson(isMatch = "true", bvStatus = success, regStatus = success)
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToChallenge" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToChallenge)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson(isMatch = "true", bvStatus = notEnoughInfoToChallenge, regStatus = notCalled)
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMismatch)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson(isMatch = "false", bvStatus = notEnoughInfoToCall, regStatus = notCalled)
      }
      "the business entity verification status is BusinessVerificationFail" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson(isMatch = "true", bvStatus = failure, regStatus = notCalled)
      }
      "the business entity has a UTR mismatch" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMismatch)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testDetailsUtrMismatchRegisteredSocietyAuditEventJson
      }

      "the business entity is not verified" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson(isMatch = "true", bvStatus = failure, regStatus = notCalled)
      }

      "the business entity is successfully verified as Ct enrolled and then registered" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsMatched)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(CtEnrolled)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson(isMatch = "true", bvStatus = ctEnrolled, regStatus = success)
      }

      "the business entity does not have a Ct Utr" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsNotProvided)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventWithoutCtUtrJson(
          isMatch = "unmatchable", bvStatus = notEnoughInfoToCall, regStatus = notCalled)
      }
    }
    "send an event for a CIO" when {
      "the business entity verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigCIO))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCharityNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsNotProvided)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditCIO()

        auditEventCaptor.getValue mustBe testCIOAuditEventJson(isMatch = "unmatchable", bvStatus = notEnoughInfoToCall, regStatus = notCalled)
      }
      "business verification is disabled" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigCIO.copy(businessVerificationCheck = false)))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCharityNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveCHRN(testJourneyId)(Future.successful(Some(testCHRN)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(DetailsNotProvided)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId))
        verifySendExplicitAuditCIO()

        auditEventCaptor.getValue mustBe testCIOAuditEventJson(isMatch = "unmatchable", bvStatus = notRequested, regStatus = notCalled)
      }
    }
  }
}