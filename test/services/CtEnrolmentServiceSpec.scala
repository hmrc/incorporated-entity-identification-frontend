/*
 * Copyright 2025 HM Revenue & Customs
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

import helpers.TestConstants._
import play.api.test.Helpers._
import services.mocks.{MockStorageService, MockValidateIncorporatedEntityDetailsService}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.RegisteredSociety
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CtEnrolled, DetailsMatched, DetailsMismatch, SuccessfullyStored}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.CtEnrolmentService
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.CtEnrolmentService.{Enrolled, EnrolmentMismatch, NoEnrolmentFound}
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CtEnrolmentServiceSpec extends UnitSpec with MockStorageService with MockValidateIncorporatedEntityDetailsService with FeatureSwitching {

  object TestCtEnrolmentService extends CtEnrolmentService(
    mockStorageService,
    mockValidateIncorporatedEntityDetailsService
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "checkCtEnrolment" when {
    "the user has an IR-CT enrolment" when {
      "the company profile is stored" when {
        "the enrolment CTUTR matches what is stored" should {
          s"return $Enrolled" in {
            mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
            mockValidateIncorporatedEntityDetails(testCompanyNumber, Some(testCtutr))(Future.successful(DetailsMatched))
            mockStoreIdentifiersMatch(testJourneyId, DetailsMatched)(Future.successful(SuccessfullyStored))
            mockStoreCtutr(testJourneyId, testCtutr)(Future.successful(SuccessfullyStored))
            mockStoreBusinessVerificationStatus(testJourneyId, CtEnrolled)(Future.successful(SuccessfullyStored))

            val testEnrolments = Enrolments(Set(testIrCtEnrolment))
            val res = await(TestCtEnrolmentService.checkCtEnrolment(testJourneyId, testEnrolments, testJourneyConfig(RegisteredSociety)))

            res mustBe Enrolled

            verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = DetailsMatched)
            verifyStoreCtutr(testJourneyId, testCtutr)
            verifyStoreBusinessVerificationStatus(testJourneyId, CtEnrolled)
          }
        }
        "the enrolment CTUTR does not match what is stored" should {
          s"return $EnrolmentMismatch" in {
            mockRetrieveCompanyProfile(testJourneyId)(Future.successful(Some(testCompanyProfile)))
            mockValidateIncorporatedEntityDetails(testCompanyNumber, Some(testCtutr))(Future.successful(DetailsMismatch))

            val testEnrolments = Enrolments(Set(testIrCtEnrolment))
            val res = await(TestCtEnrolmentService.checkCtEnrolment(testJourneyId, testEnrolments, testJourneyConfig(RegisteredSociety)))

            res mustBe EnrolmentMismatch
          }
        }
      }
      "the company profile is not stored" should {
        "throw an exception" in {
          mockRetrieveCompanyProfile(testJourneyId)(Future.successful(None))

          val testEnrolments = Enrolments(Set(testIrCtEnrolment))
          intercept[InternalServerException](await(TestCtEnrolmentService.checkCtEnrolment(testJourneyId, testEnrolments, testJourneyConfig(RegisteredSociety))))
        }
      }
    }
    "the user does not have an IR-CT enrolment" should {
      s"return $NoEnrolmentFound" in {
        val testEnrolments = Enrolments(Set.empty)
        val res = await(TestCtEnrolmentService.checkCtEnrolment(testJourneyId, testEnrolments, testJourneyConfig(RegisteredSociety)))

        res mustBe NoEnrolmentFound
      }
    }
  }
}
