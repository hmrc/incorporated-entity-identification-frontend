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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig, Registered, RegistrationNotCalled}
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
      signOutUrl = testSignOutUrl
    ),
    LimitedCompany,
    businessVerificationCheck = true
  )

  val testJourneyConfigRegisteredSociety: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = Some(defaultServiceName),
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl
    ),
    RegisteredSociety,
    businessVerificationCheck = true
  )

  val testJourneyConfigCIO: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = Some(defaultServiceName),
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl
    ),
    CharitableIncorporatedOrganisation,
    businessVerificationCheck = true
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

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testUkCompanySuccessfulAuditEventJson
      }
      "the business entity does not have its details matched" in {

        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testUnchallengedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testDetailsNotFoundAuditEventJson
      }
      "the business entity has a UTR mismatch" in {

        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testDetailsUtrMismatchAuditEventJson
      }

      "the business entity is successfully verified but registration fails" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditUkCompany()

        auditEventCaptor.getValue mustBe testDetailsRegistrationStatusMissingAuditEventJson
      }

      "the business entity verification is undefined and registration is not called" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
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

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testRegisteredSocietyAuditEventJson
      }

      "the business entity does not have its details matched" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testUnchallengedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testDetailsNotFoundRegisteredSocietyAuditEventJson
      }

      "the business entity has a UTR mismatch" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditRegisterSociety()

        auditEventCaptor.getValue mustBe testDetailsUtrMismatchRegisteredSocietyAuditEventJson
      }
    }
    "send an event for a CIO" in {
      mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigCIO))
      mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCharityNumber))
      mockRetrieveCtutr(testJourneyId)(Future.successful(None))
      mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
      mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testUnchallengedBusinessVerificationStatus)))
      mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

      await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
      verifySendExplicitAuditCIO()

      auditEventCaptor.getValue mustBe testCIOAuditEventJson
    }
  }
}