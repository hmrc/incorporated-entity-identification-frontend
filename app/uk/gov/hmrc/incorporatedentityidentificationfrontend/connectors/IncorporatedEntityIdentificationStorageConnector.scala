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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.IncorporatedEntityIdentificationHttpParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompaniesHouseProfile, SuccessfullyStored, StorageResult}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorporatedEntityIdentificationStorageConnector @Inject()(http: HttpClient,
                                                                 appConfig: AppConfig
                                                                )(implicit ec: ExecutionContext) {

  def storeCompaniesHouseProfile(journeyId: String,
                                 companiesHouseInformation: CompaniesHouseProfile
                                )(implicit hc: HeaderCarrier): Future[StorageResult] = {

    val uri = "companies-house-profile"

    http.PUT[JsObject, StorageResult](appConfig.backendStorageUrl(journeyId, uri), Json.toJsObject(companiesHouseInformation))

  }

  def storeCtutr(journeyId: String,
                 ctutr: String
                )(implicit hc: HeaderCarrier): Future[StorageResult] = {

    val ctutrKey = "ctutr"
    val uri = "ctutr"
    val jsonBody = Json.obj(ctutrKey -> ctutr)

    http.PUT[JsObject, StorageResult](appConfig.backendStorageUrl(journeyId, uri), jsonBody)

  }

}
