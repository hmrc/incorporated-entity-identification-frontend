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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.IncorporatedEntityIdentificationStorageConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompaniesHouseProfile, StorageResult}

import scala.concurrent.Future

@Singleton
class CompaniesHouseProfileStorageService @Inject()(storageConnector: IncorporatedEntityIdentificationStorageConnector) {

  def storeCompaniesHouseProfile(journeyId: String,
                                 companiesHouseProfile: CompaniesHouseProfile
                                )(implicit hc: HeaderCarrier): Future[StorageResult] =
    storageConnector.storeCompaniesHouseProfile(journeyId, companiesHouseProfile)

  def retrieveCompaniesHouseProfile(journeyId: String)(implicit hc: HeaderCarrier): Future[CompaniesHouseProfile] = {
    //storageConnector.retrieveCompaniesHouseProfile(journeyId) TODO uncomment when backend retrieval API is built
    val testCompanyName = "Test Company Ltd"
    val testCompanyNumber = "12345678"
    Future.successful(CompaniesHouseProfile(testCompanyName, testCompanyNumber))
  }

}
