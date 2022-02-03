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
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe (())
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testLimitedCompanySuccessfulAuditEventJson
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToChallenge" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToChallenge)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe (testLimitedCompanyUnmatchedDefaultAuditEventJson ++ testAuditVerificationStatusNotEnoughInformationToChallengeJson)
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe  (testLimitedCompanyUnmatchedDefaultAuditEventJson ++ testAuditVerificationStatusNotEnoughInformationToCallBVJson)
      }
      "the business entity verification status is BusinessVerificationFail" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe  (testLimitedCompanyUnmatchedDefaultAuditEventJson ++ testAuditVerificationStatusFailedJson)
      }
      "the business entity has a UTR mismatch" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testDetailsUtrMismatchAuditEventJson
      }
      "the business entity is successfully verified but registration fails" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationFailed)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testDetailsRegistrationStatusMissingAuditEventJson
      }
      "the business entity verification is undefined and registration is called" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testDetailsBusinessVerificationStatusMissingAuditEventJson
      }
    }

    "send an event for a Registered Society" when {
      "the business entity is successfully verified and then registered" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToChallenge" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToChallenge)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe (testRegisteredSocietyUnmatchedDefaultAuditEventJson ++ testAuditVerificationStatusNotEnoughInformationToChallengeJson)
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe (testRegisteredSocietyUnmatchedDefaultAuditEventJson ++ testAuditVerificationStatusNotEnoughInformationToCallBVJson)
      }
      "the business entity verification status is BusinessVerificationFail" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe (testRegisteredSocietyUnmatchedDefaultAuditEventJson ++ testAuditVerificationStatusFailedJson)
      }
      "the business entity has a UTR mismatch" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe(())
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testDetailsUtrMismatchRegisteredSocietyAuditEventJson
      }
    }
    "send an event for a CIO" when {
      "the business entity verification status is BusinessVerificationNotEnoughInformationToChallenge" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigCIO))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCharityNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToChallenge)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe (())
        verifySendExplicitAuditCIO()

        auditEventCaptor.getValue mustBe (testCIODefaultAuditEventJson ++ testAuditVerificationStatusNotEnoughInformationToChallengeJson)
      }
      "the business entity verification status is BusinessVerificationNotEnoughInformationToCallBV" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigCIO))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCharityNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe (())
        verifySendExplicitAuditCIO()

        auditEventCaptor.getValue mustBe (testCIODefaultAuditEventJson ++ testAuditVerificationStatusNotEnoughInformationToCallBVJson)
      }
      "the business entity verification status is BusinessVerificationFail" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigCIO))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCharityNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe (())

        verifySendExplicitAuditCIO()

        auditEventCaptor.getValue mustBe (testCIODefaultAuditEventJson ++ testAuditVerificationStatusFailedJson)
      }
    }
  }
}