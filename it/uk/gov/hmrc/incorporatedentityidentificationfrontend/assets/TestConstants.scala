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
package uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.RegistrationStatus._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testCompanyNumber: String = "12345678"
  val companyNameKey: String = "companyName"
  val companyNumberKey: String = "companyNumber"
  val testCompanyName: String = "Test Company Ltd"
  val testCHRN: String = "AB12345"
  val testCtutr: String = "1234567890"
  val testJourneyId: String = UUID.randomUUID().toString
  val testSignOutUrl: String = "signOutUrl"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testDateOfIncorporation: String = LocalDate.now().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val GGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testDefaultServiceName: String = "Entity Validation Service"
  val testCallingServiceName: String = "Test Service"
  val testContinueUrl: String = "/test"
  val testAccessibilityUrl: String = "/accessibility"
  val testDefaultAccessibilityUrl: String = "/accessibility-statement/vat-registration"
  val testSafeId: String = UUID.randomUUID().toString
  val testRegime: String = "VATC"
  val testBusinessVerificationPassJson: JsObject = testBusinessVerificationJson(value = businessVerificationPassKey)
  val testBusinessVerificationFailJson: JsObject = testBusinessVerificationJson(value = businessVerificationFailKey)
  val testSuccessfulRegistration: RegistrationStatus = Registered(testSafeId)
  val testFailedRegistration: RegistrationStatus = RegistrationFailed
  val testSuccessfulRegistrationJson: JsObject = Json.obj(
    registrationStatusKey -> RegisteredKey,
    registeredBusinessPartnerIdKey -> testSafeId)
  val testFailedRegistrationJson: JsObject = Json.obj(registrationStatusKey -> RegistrationFailedKey)
  val testRegistrationNotCalledJson: JsObject = Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
  val testDeskProServiceId: String = "vrs"
  val IRCTEnrolmentKey = "IR-CT"
  val IRCTReferenceKey = "UTR"
  val testAddress: JsObject = Json.obj(
    "address_line_1" -> "testLine1",
    "address_line_2" -> "test town",
    "care_of" -> "test name",
    "country" -> "United Kingdom",
    "locality" -> "test city",
    "po_box" -> "123",
    "postal_code" -> "AA11AA",
    "premises" -> "1",
    "region" -> "test region"
  )
  val testCompanyProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation, testAddress)
  val testCioProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, "", Json.obj())

  val testLimitedCompanyJourneyConfig: JourneyConfig = createTestJourneyConfig(LimitedCompany)
  val testRegisteredSocietyJourneyConfig: JourneyConfig = createTestJourneyConfig(RegisteredSociety)
  val testCharitableIncorporatedOrganisationJourneyConfig: JourneyConfig = createTestJourneyConfig(CharitableIncorporatedOrganisation)

  private def createTestJourneyConfig(entityType: BusinessEntity): JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      PageConfig(None, testDeskProServiceId, testSignOutUrl, testAccessibilityUrl),
      entityType,
      businessVerificationCheck = true,
      testRegime)

  val testIncorporatedEntityFullJourneyDataJson: JsObject = {
    Json.obj(
      "companyProfile" -> testCompanyProfile,
      "ctutr" -> testCtutr,
      "identifiersMatch" -> true,
      "businessVerification" -> testBusinessVerificationPassJson,
      "registration" -> testSuccessfulRegistrationJson
    )
  }

  def testRegisterAuditEventJson(companyNumber: String,
                                 isMatch: Boolean,
                                 ctUtr: String,
                                 verificationStatus: String,
                                 registrationStatus: String): JsObject = {
    Json.obj(
      "callingService" -> "Entity Validation Service",
      "businessType" -> "UK Company",
      "companyNumber" -> companyNumber,
      "isMatch" -> isMatch,
      "CTUTR" -> ctUtr,
      "VerificationStatus" -> verificationStatus,
      "RegisterApiStatus" -> registrationStatus
    )
  }

  val testJourneyConfigWithoutAccessibilityUrlAsJsObject: JsObject =
    Json.obj(
      "continueUrl" -> testContinueUrl,
      "pageConfig" -> Json.obj(
        "deskProServiceId" -> testDeskProServiceId,
        "signOutUrl" -> testSignOutUrl
      ),
      "businessEntity" -> "LtdCompany",
      "businessVerificationCheck" -> true,
      "regime" -> testRegime
    )

  val testJourneyConfigWithDefaultAccessibilityUrl: JourneyConfig =
    JourneyConfig(
      continueUrl = testContinueUrl,
      pageConfig = PageConfig(
        None,
        testDeskProServiceId,
        testSignOutUrl,
        testDefaultAccessibilityUrl
      ),
      businessEntity = LimitedCompany,
      businessVerificationCheck = true,
      regime = testRegime
    )

  def testBusinessVerificationJson(value: String): JsObject = Json.obj(businessVerificationStatusKey -> value)

  def testDefaultIncorporatedEntityInformation(businessVerificationStatus: BusinessVerificationStatus): IncorporatedEntityInformation =
    IncorporatedEntityInformation(
      companyProfile = testCompanyProfile,
      optCtutr = Some(testCtutr),
      optChrn = Some(testCHRN),
      identifiersMatch = true,
      businessVerification = Some(businessVerificationStatus),
      registration = testSuccessfulRegistration
    )

}
