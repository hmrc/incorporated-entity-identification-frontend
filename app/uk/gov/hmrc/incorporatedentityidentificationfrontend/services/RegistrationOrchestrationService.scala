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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationPass, CtEnrolled, RegistrationNotCalled, RegistrationStatus}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(storageService: StorageService,
                                                 registrationConnector: RegistrationConnector
                                                )(implicit ec: ExecutionContext) {

  def register(journeyId: String, businessEntity: BusinessEntity)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = for {
    registrationStatus <- storageService.retrieveBusinessVerificationStatus(journeyId).flatMap {
      case Some(BusinessVerificationPass) | Some(CtEnrolled) => for {
        optCompanyProfile <- storageService.retrieveCompanyProfile(journeyId)
        optCtutr <- storageService.retrieveCtutr(journeyId)
        registrationStatus <-
          (optCompanyProfile, optCtutr) match {
            case (Some(companyProfile), Some(ctutr)) =>
              businessEntity match {
                case LimitedCompany => registrationConnector.registerLimitedCompany(companyProfile.companyNumber, ctutr)
                case RegisteredSociety => registrationConnector.registerRegisteredSociety(companyProfile.companyNumber, ctutr)
                case CharitableIncorporatedOrganisation => Future.successful(RegistrationNotCalled) //Not currently registered
              }
            case _ =>
              throw new InternalServerException(s"Missing required data for registration in database for $journeyId")

          }
      } yield registrationStatus
      case Some(_) =>
        Future.successful(RegistrationNotCalled)
      case None =>
        throw new InternalServerException(s"Missing business verification state in database for $journeyId")
    }
    _ <- storageService.storeRegistrationStatus(journeyId, registrationStatus)
  } yield registrationStatus

}
