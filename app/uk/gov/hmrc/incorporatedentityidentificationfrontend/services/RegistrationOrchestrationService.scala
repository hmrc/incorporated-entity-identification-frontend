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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BvPass

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(incorporatedEntityInformationService: IncorporatedEntityInformationService,
                                                 registrationConnector: RegistrationConnector
                                                )(implicit ec: ExecutionContext) {

  def register(journeyId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    val companyData = for {
      optCompanyProfile <- incorporatedEntityInformationService.retrieveCompanyProfile(journeyId)
      optCtutr <- incorporatedEntityInformationService.retrieveCtutr(journeyId)
      optBusinessVerificationStatus <- incorporatedEntityInformationService.retrieveBusinessVerificationStatus(journeyId)
    } yield (optCompanyProfile, optCtutr, optBusinessVerificationStatus)

    companyData.map {
      case (Some(companyProfile), Some(ctutr), Some(businessVerificationStatus)) if businessVerificationStatus == BvPass =>
        registrationConnector.register(companyProfile.companyNumber, ctutr).flatMap {
          registrationResponse =>
            incorporatedEntityInformationService.storeRegistrationStatus(journeyId, registrationResponse)
        }
      case (Some(_), Some(_), Some(_)) =>
        throw new InternalServerException(s"Invalid Business Verification State for $journeyId")
      case _ =>
        throw new InternalServerException(s"Missing date in database for $journeyId")
    }
  }

}
