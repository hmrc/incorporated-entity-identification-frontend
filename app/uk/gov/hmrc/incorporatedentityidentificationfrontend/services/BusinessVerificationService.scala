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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.{CreateBusinessVerificationJourneyConnector, RetrieveBusinessVerificationStatusConnector}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationStatus

import scala.concurrent.Future

@Singleton
class BusinessVerificationService @Inject()(createBusinessVerificationJourneyConnector: CreateBusinessVerificationJourneyConnector,
                                            retrieveBusinessVerificationResultConnector: RetrieveBusinessVerificationStatusConnector) {

  def createBusinessVerificationJourney(journeyId: String, ctutr: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(journeyId, ctutr)

  def retrieveBusinessVerificationStatus(businessVerificationJourneyId: String)(implicit hc: HeaderCarrier): Future[BusinessVerificationStatus] =
    retrieveBusinessVerificationResultConnector.retrieveBusinessVerificationStatus(businessVerificationJourneyId)

}
