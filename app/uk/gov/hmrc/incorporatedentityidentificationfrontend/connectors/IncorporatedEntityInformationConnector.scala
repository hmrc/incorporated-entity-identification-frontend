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
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.IncorporatedEntityIdentificationStorageHttpParser.IncorporatedEntityIdentificationStorageHttpReads
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{IncorporatedEntityInformation, StorageResult}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorporatedEntityInformationConnector @Inject()(http: HttpClient,
                                                       appConfig: AppConfig
                                                      )(implicit ec: ExecutionContext) extends HttpReadsInstances {
  def retrieveIncorporatedEntityInformation[DataType](journeyId: String,
                                                      dataKey: String
                                                     )(implicit dataTypeReads: Reads[DataType],
                                                       manifest: Manifest[DataType],
                                                       hc: HeaderCarrier): Future[Option[DataType]] =
    http.GET[Option[DataType]](s"${appConfig.incorporatedEntityInformationUrl(journeyId)}/$dataKey")

  def retrieveIncorporatedEntityInformation(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[IncorporatedEntityInformation]] =
    http.GET[Option[IncorporatedEntityInformation]](appConfig.incorporatedEntityInformationUrl(journeyId))

  def storeData[DataType](journeyId: String, dataKey: String, data: DataType)(implicit dataTypeWriter: Writes[DataType], hc: HeaderCarrier): Future[StorageResult] = {
    http.PUT[DataType, StorageResult](s"${appConfig.incorporatedEntityInformationUrl(journeyId)}/$dataKey", data)
  }

}
