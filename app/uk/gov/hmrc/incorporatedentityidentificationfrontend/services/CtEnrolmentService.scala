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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.ValidateIncorporatedEntityDetailsHttpParser.DetailsMatched
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CtEnrolled, JourneyConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.CtEnrolmentService._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.EnrolmentUtils.getCtEnrolment

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CtEnrolmentService @Inject()(storageService: StorageService,
                                   validateIncorporatedEntityDetailsService: ValidateIncorporatedEntityDetailsService
                                  )(implicit ec: ExecutionContext) extends FeatureSwitching {

  def checkCtEnrolment(journeyId: String, enrolments: Enrolments, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[CtEnrolmentState] =
    getCtEnrolment(enrolments) match {
      case Some(ctutr) =>
        storageService.retrieveCompanyProfile(journeyId).flatMap {
          case Some(companyProfile) =>
            validateIncorporatedEntityDetailsService.validateIncorporatedEntityDetails(companyProfile.companyNumber, Some(ctutr)).flatMap {
              case DetailsMatched if journeyConfig.businessVerificationCheck =>
                for {
                  _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = true)
                  _ <- storageService.storeCtutr(journeyId, ctutr)
                  _ <- storageService.storeBusinessVerificationStatus(journeyId, CtEnrolled)
                } yield Enrolled
              case DetailsMatched if !journeyConfig.businessVerificationCheck =>
                for {
                  _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = true)
                  _ <- storageService.storeCtutr(journeyId, ctutr)
                } yield Enrolled
              case _ =>
                Future.successful(EnrolmentMismatch)
            }
          case _ =>
            throw new InternalServerException("No data stored")
        }
      case None =>
        Future.successful(NoEnrolmentFound)
    }
}

object CtEnrolmentService {

  sealed trait CtEnrolmentState

  case object Enrolled extends CtEnrolmentState

  case object NoEnrolmentFound extends CtEnrolmentState

  case object EnrolmentMismatch extends CtEnrolmentState

}
