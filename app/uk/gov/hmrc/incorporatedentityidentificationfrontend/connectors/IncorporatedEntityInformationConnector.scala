/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{Json, Reads, Writes}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.IncorporatedEntityIdentificationStorageHttpParser.IncorporatedEntityIdentificationStorageHttpReads
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.RemoveIncorporatedEntityDetailsHttpParser.{RemoveIncorporatedEntityDetailsHttpReads, SuccessfullyRemoved}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{IncorporatedEntityInformation, StorageResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class IncorporatedEntityInformationConnector @Inject()(http: HttpClientV2,
                                                       appConfig: AppConfig
                                                      )(implicit ec: ExecutionContext) extends HttpReadsInstances {

  def retrieveIncorporatedEntityInformation[DataType](journeyId: String,
                                                      dataKey: String
                                                     )(implicit dataTypeReads: Reads[DataType],
                                                       classTag: ClassTag[DataType],
                                                       hc: HeaderCarrier): Future[Option[DataType]] =
    http.get(url"${appConfig.incorporatedEntityInformationUrl(journeyId)}/$dataKey").execute[Option[DataType]]

  def retrieveIncorporatedEntityInformation(journeyId: String
                                           )(implicit hc: HeaderCarrier): Future[Option[IncorporatedEntityInformation]] =
    http.get(url"${appConfig.incorporatedEntityInformationUrl(journeyId)}").execute[Option[IncorporatedEntityInformation]]

  def storeData[DataType](journeyId: String, dataKey: String, data: DataType
                         )(implicit dataTypeWriter: Writes[DataType], hc: HeaderCarrier): Future[StorageResult] =
    http.put(url"${appConfig.incorporatedEntityInformationUrl(journeyId)}/$dataKey").withBody(Json.toJson(data)).execute[StorageResult]

  def removeIncorporatedEntityDetailsField(journeyId: String,
                                           dataKey: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http.delete(url"${appConfig.incorporatedEntityInformationUrl(journeyId)}/$dataKey")(hc)
      .execute[SuccessfullyRemoved.type](RemoveIncorporatedEntityDetailsHttpReads, ec)

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http.delete(url"${appConfig.incorporatedEntityInformationUrl(journeyId)}")(hc)
      .execute[SuccessfullyRemoved.type](RemoveIncorporatedEntityDetailsHttpReads, ec)

}
