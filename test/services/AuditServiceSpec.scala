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

import connectors.mocks.MockAuditConnector
import helpers.TestConstants._
import play.api.test.Helpers._
import org.scalatest.matchers.must.Matchers
import services.mocks.{MockIncorporationEntityInformationService, MockJourneyService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig, Registered, RegistrationNotCalled}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.AuditService
import utils.UnitSpec
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{LimitedCompany, RegisteredSociety}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends UnitSpec with Matchers with MockIncorporationEntityInformationService with MockJourneyService with MockAuditConnector {

  object TestService extends AuditService(
    mockAuditConnector,
    mockJourneyService,
    mock[AppConfig],
    mockIncorporationEntityInformationService
  )
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testJourneyConfigLimitedCompany: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = Some(defaultServiceName),
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl
    ),
    LimitedCompany
  )

  val testJourneyConfigRegisteredSociety: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = Some(defaultServiceName),
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl
    ),
    RegisteredSociety
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
        verifySendExplicitAuditUkCompany

        auditEventCaptor.getValue mustBe testUkCompanySuccessfulAuditEventJson
      }
    }

    "not send an audit event" when {
      "the entity type is Registered Society" in {
        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigRegisteredSociety))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testPassedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(Registered(testSafeId))))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifyNoAuditSent

      }
    }

    "send an event for a Limited Company" when {
      "the business entity does not have its details matched" in {

        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(Some(testCtutr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(Some(testUnchallengedBusinessVerificationStatus)))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(Some(RegistrationNotCalled)))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditUkCompany

        auditEventCaptor.getValue mustBe testDetailsNotFoundAuditEventJson
      }
    }

    "send an event for a Limited Company" when {
      "the business entity has a UTR mismatch" in {

        mockGetJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(testJourneyConfigLimitedCompany))
        mockRetrieveCompanyNumber(testJourneyId)(Future.successful(testCompanyProfile.companyNumber))
        mockRetrieveCtutr(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveBusinessVerificationResponse(testJourneyId)(Future.successful(None))
        mockRetrieveRegistrationStatus(testJourneyId)(Future.successful(None))

        await(TestService.auditJourney(testJourneyId, testAuthInternalId)) mustBe()
        verifySendExplicitAuditUkCompany

        auditEventCaptor.getValue mustBe testDetailsUtrMismatchAuditEventJson
      }
    }


  }
}