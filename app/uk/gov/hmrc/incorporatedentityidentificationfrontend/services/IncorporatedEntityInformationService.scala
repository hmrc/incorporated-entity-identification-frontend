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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.IncorporatedEntityInformationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompanyProfile, IncorporatedEntityInformation, StorageResult}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.IncorporatedEntityInformationService._

import scala.concurrent.Future

@Singleton
class IncorporatedEntityInformationService @Inject()(connector: IncorporatedEntityInformationConnector) {

  def retrieveCompanyProfile(journeyId: String
                            )(implicit hc: HeaderCarrier): Future[Option[CompanyProfile]] =
    connector.retrieveIncorporatedEntityInformation[CompanyProfile](journeyId, companyProfileKey)

  def storeCompanyProfile(journeyId: String,
                          companyProfile: CompanyProfile
                         )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData(journeyId, companyProfileKey, companyProfile)

  def storeCtutr(journeyId: String,
                 ctutr: String
                )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData(journeyId, ctutrKey, ctutr)

  def retrieveIncorporatedEntityInformation(journeyId: String
                                           )(implicit hc: HeaderCarrier): Future[Option[IncorporatedEntityInformation]] =
    connector.retrieveIncorporatedEntityInformation(journeyId)
}

object IncorporatedEntityInformationService {
  val companyProfileKey: String = "company-profile"
  val ctutrKey: String = "ctutr"
}
