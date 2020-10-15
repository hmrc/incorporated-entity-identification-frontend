/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate
import java.util.UUID

import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BvPass, _}


object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testCompanyNumber: String = "12345678"
  val testCtutr: String = "1234567890"
  val testDateOfIncorporation: String = LocalDate.now().toString
  val testCompanyName: String = "ABC Limited"
  val testIdentifiersMatch: Boolean = true
  val testCompanyProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation)
  val testSafeId: String = UUID.randomUUID().toString
  val testSuccessfulRegistrationStatus: RegistrationStatus = Registered(testSafeId)
  val testFailedRegistrationStatus: RegistrationStatus = RegistrationFailed
  val testPassedBusinessVerificationStatus: BusinessVerificationStatus = BvPass
  val testFailedBusinessVerificationStatus: BusinessVerificationStatus = BvFail
  val testUnchallengedBusinessVerificationStatus: BusinessVerificationStatus = BvUnchallenged
  val testIncorporatedEntityInformation: IncorporatedEntityInformation =
    IncorporatedEntityInformation(
      testCompanyProfile,
      testCtutr,
      testIdentifiersMatch,
      testPassedBusinessVerificationStatus,
      testSuccessfulRegistrationStatus
    )
}
