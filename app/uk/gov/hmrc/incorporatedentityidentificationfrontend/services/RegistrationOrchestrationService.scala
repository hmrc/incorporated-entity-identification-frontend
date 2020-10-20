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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationPass, RegistrationNotCalled, RegistrationStatus}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(incorporatedEntityInformationService: IncorporatedEntityInformationService,
                                                 registrationConnector: RegistrationConnector
                                                )(implicit ec: ExecutionContext) {

  def register(journeyId: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = for {
    registrationStatus <- incorporatedEntityInformationService.retrieveBusinessVerificationStatus(journeyId).flatMap {
      case Some(BusinessVerificationPass) => for {
        optCompanyProfile <- incorporatedEntityInformationService.retrieveCompanyProfile(journeyId)
        optCtutr <- incorporatedEntityInformationService.retrieveCtutr(journeyId)
        registrationStatus <-
          (optCompanyProfile, optCtutr) match {
            case (Some(companyProfile), Some(ctutr)) =>
              registrationConnector.register(companyProfile.companyNumber, ctutr)
            case _ =>
              throw new InternalServerException(s"Missing required data for registration in database for $journeyId")

          }
      } yield registrationStatus
      case Some(_) =>
        Future.successful(RegistrationNotCalled)
      case None =>
        throw new InternalServerException(s"Missing business verification state in database for $journeyId")
    }
    _ <- incorporatedEntityInformationService.storeRegistrationStatus(journeyId, registrationStatus)
  } yield registrationStatus

}
