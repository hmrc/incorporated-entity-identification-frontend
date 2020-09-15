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
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.IncorporatedEntityIdentificationStorageHttpParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompanyProfile, StorageResult}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorporatedEntityIdentificationStorageConnector @Inject()(http: HttpClient,
                                                                 appConfig: AppConfig
                                                                )(implicit ec: ExecutionContext) {

  def storeCompanyProfile(journeyId: String,
                          companiesHouseInformation: CompanyProfile
                                )(implicit hc: HeaderCarrier): Future[StorageResult] = {

    val dataKey = "company-profile"

    http.PUT[JsObject, StorageResult](
      url = appConfig.backendStorageUrl(journeyId = journeyId, optDataKey = Some(dataKey)),
      body = Json.toJsObject(companiesHouseInformation))

  }

  def storeCtutr(journeyId: String,
                 ctutr: String
                )(implicit hc: HeaderCarrier): Future[StorageResult] = {

    val dataKey = "ctutr"
    val jsonBody = JsString(ctutr)

    http.PUT[JsValue, StorageResult](appConfig.backendStorageUrl(journeyId, optDataKey = Some(dataKey)), jsonBody)

  }

}
