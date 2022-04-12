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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val companyNameKey: String = "companyName"
  val companyNumberKey: String = "companyNumber"
  val testCHRN: String = "AB12345"
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

  val testIncorporatedEntityInformation: IncorporatedEntityInformation =
    IncorporatedEntityInformation(
      testCompanyProfile,
      Some(testCtutr),
      Some(testCHRN),
      testIdentifiersMatch,
      Some(BusinessVerificationPass),
      Registered(testSafeId)
    )
  val testContinueUrl = "/test"
  val testSignOutUrl = "/signOutUrl"
  val testAccessibilityUrl = "/accessibility"
  val testCharityNumber = "CE123456"

  val success: String = "success"
  val failure: String = "fail"
  val notCalled: String = "not called"
  val notRequested: String = "not requested"
  val notEnoughInfoToChallenge: String = "Not Enough Information to challenge"
  val notEnoughInfoToCall: String = "Not Enough Information to call BV"
  val ctEnrolled: String = "Enrolled"

  def testLimitedCompanyAuditEventJson(isMatch: Boolean, bvStatus: String, regStatus: String): JsObject =
    Json.obj(
      "callingService" -> defaultServiceName,
      "businessType" -> "UK Company",
      "companyNumber" -> testCompanyProfile.companyNumber,
      "isMatch" -> isMatch,
      "VerificationStatus" -> bvStatus,
      "RegisterApiStatus" -> regStatus,
      "CTUTR" -> testCtutr
    )

  val testDetailsUtrMismatchAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> notRequested,
    "RegisterApiStatus" -> notCalled
  )

  def testRegisteredSocietyAuditEventJson(isMatch: Boolean, bvStatus: String, regStatus: String): JsObject =
    Json.obj(
      "callingService" -> defaultServiceName,
      "businessType" -> "Registered Society",
      "companyNumber" -> testCompanyProfile.companyNumber,
      "isMatch" -> isMatch,
      "VerificationStatus" -> bvStatus,
      "RegisterApiStatus" -> regStatus,
      "CTUTR" -> testCtutr
    )

  val testDetailsUtrMismatchRegisteredSocietyAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "Registered Society",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> notRequested,
    "RegisterApiStatus" -> notCalled
  )

  val testCIODefaultAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "CIO",
    "companyNumber" -> testCharityNumber,
    "identifiersMatch" -> false,
    "RegisterApiStatus" -> "not called"
  )

  def testCIOAuditEventJson(bvStatus: String, regStatus: String): JsObject =
    Json.obj(
      "callingService" -> defaultServiceName,
      "businessType" -> "CIO",
      "companyNumber" -> testCharityNumber,
      "isMatch" -> "unmatchable",
      "VerificationStatus" -> bvStatus,
      "RegisterApiStatus" -> regStatus
    )

  val testIrCtEnrolment: Enrolment = Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", testCtutr)), "Activated", None)

  def testJourneyConfigLimitedCompanyWithoutBV(): JourneyConfig = testJourneyConfig(LimitedCompany).copy(businessVerificationCheck = false)

  def testJourneyConfig(businessEntity: BusinessEntity): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl),
    businessEntity = businessEntity,
    businessVerificationCheck = true,
    regime = testRegime
  )

  def testJourneyConfigRegisteredSocietyWithoutBV(): JourneyConfig = testJourneyConfig(RegisteredSociety).copy(businessVerificationCheck = false)

}
