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

package helpers

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.RegistrationStatus._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testAuthInternalId: String = UUID.randomUUID().toString
  val testServiceName: String = "Test Service"
  val defaultServiceName: String = "Entity Validation Service"
  val testRegime: String = "VATC"
  val testCompanyNumber: String = "12345678"
  val testCtutr: String = "1234567890"
  val testDateOfIncorporation: String = LocalDate.now().toString
  val testCompanyName: String = "ABC Limited"
  val testIdentifiersMatch: Boolean = true
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
  val testSafeId: String = UUID.randomUUID().toString

  private val testPassedBusinessVerificationStatusJson: JsObject = Json.obj(BusinessVerificationStatusKey -> BusinessVerificationPassKey)

  private val testFailedBusinessVerificationStatusJson: JsObject = Json.obj(BusinessVerificationStatusKey -> BusinessVerificationFailKey)

  private val testBusinessVerificationNotEnoughInformationToChallengeJson: JsObject =
    Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToChallengeKey)

  private val testBusinessVerificationNotEnoughInformationToCallBVJson: JsObject =
    Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToCallBVKey)

  val testRegistrationStatusJson: JsObject = Json.obj(
    registrationStatusKey -> RegisteredKey,
    registeredBusinessPartnerIdKey -> testSafeId
  )

  val testRegistrationNotCalledJson: JsObject = Json.obj(registrationStatusKey -> RegistrationNotCalledKey)

  val testIncorporatedEntityInformation: IncorporatedEntityInformation =
    IncorporatedEntityInformation(
      testCompanyProfile,
      Some(testCtutr),
      testIdentifiersMatch,
      Some(BusinessVerificationPass),
      Registered(testSafeId)
    )
  val testContinueUrl = "/test"
  val testSignOutUrl = "/signOutUrl"
  val testAccessibilityUrl = "/accessibility"
  val testCharityNumber = "CE123456"

  val testLimitedCompanySuccessfulAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> true,
    "VerificationStatus" -> testPassedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> "success",
    "CTUTR" -> testCtutr
  )

  val testLimitedCompanyUnmatchedDefaultAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "RegisterApiStatus" -> "not called",
    "CTUTR" -> testCtutr
  )

  val testDetailsUtrMismatchAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> testFailedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> "not called"
  )

  val testDetailsRegistrationStatusMissingAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> true,
    "VerificationStatus" -> testPassedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> "fail",
    "CTUTR" -> testCtutr
  )

  val testDetailsBusinessVerificationStatusMissingAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> true,
    "VerificationStatus" -> testFailedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> "success",
    "CTUTR" -> testCtutr
  )

  val testRegisteredSocietyAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "Registered Society",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> true,
    "VerificationStatus" -> testPassedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> "success",
    "CTUTR" -> testCtutr
  )

  val testRegisteredSocietyUnmatchedDefaultAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "Registered Society",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "RegisterApiStatus" -> "not called",
    "CTUTR" -> testCtutr
  )

  val testDetailsUtrMismatchRegisteredSocietyAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "Registered Society",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> testFailedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> "not called"
  )

  val testCIODefaultAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "CIO",
    "companyNumber" -> testCharityNumber,
    "identifiersMatch" -> false,
    "RegisterApiStatus" -> "not called"
  )

  val testAuditVerificationStatusNotEnoughInformationToChallengeJson: JsObject = Json.obj("VerificationStatus" -> testBusinessVerificationNotEnoughInformationToChallengeJson)

  val testAuditVerificationStatusNotEnoughInformationToCallBVJson: JsObject = Json.obj("VerificationStatus" -> testBusinessVerificationNotEnoughInformationToCallBVJson)

  val testAuditVerificationStatusFailedJson: JsObject = Json.obj("VerificationStatus" -> testFailedBusinessVerificationStatusJson)

  val testIrCtEnrolment: Enrolment = Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", testCtutr)), "Activated", None)

  def testJourneyConfigLimitedCompanyWithoutBV(): JourneyConfig = testJourneyConfigLimitedCompany().copy(businessVerificationCheck = false)

  def testJourneyConfigLimitedCompany(): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl),
    businessEntity = LimitedCompany,
    businessVerificationCheck = true,
    regime = testRegime
  )

  def testJourneyConfigRegisteredSocietyWithoutBV(): JourneyConfig = testJourneyConfigRegisteredSociety().copy(businessVerificationCheck = false)

  def testJourneyConfigRegisteredSociety(): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl),
    businessEntity = RegisteredSociety,
    businessVerificationCheck = true,
    regime = testRegime
  )

}
