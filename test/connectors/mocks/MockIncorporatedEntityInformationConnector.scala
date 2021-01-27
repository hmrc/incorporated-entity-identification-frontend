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

package connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.IncorporatedEntityInformationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.StorageResult

import scala.concurrent.Future

trait MockIncorporatedEntityInformationConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockIncorporatedEntityInformationConnector: IncorporatedEntityInformationConnector = mock[IncorporatedEntityInformationConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockIncorporatedEntityInformationConnector)
  }

  def mockRetrieveIncorporatedEntityInformation[T](journeyId: String,
                                                   dataKey: String
                                                  )(response: Future[Option[T]]): OngoingStubbing[_] =
    when(mockIncorporatedEntityInformationConnector.retrieveIncorporatedEntityInformation[T](
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey)
    )(ArgumentMatchers.any[Reads[T]],
      ArgumentMatchers.any[Manifest[T]],
      ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(response)

  def verifyRetrieveIncorporatedEntityInformation[T](journeyId: String, dataKey: String): Unit =
    verify(mockIncorporatedEntityInformationConnector).retrieveIncorporatedEntityInformation[T](
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey)
    )(ArgumentMatchers.any[Reads[T]],
      ArgumentMatchers.any[Manifest[T]],
      ArgumentMatchers.any[HeaderCarrier])

  def mockStoreData[T](journeyId: String,
                       dataKey: String,
                       data: T
                      )(response: Future[StorageResult]): OngoingStubbing[_] =
    when(mockIncorporatedEntityInformationConnector.storeData[T](
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey),
      ArgumentMatchers.eq(data)
    )(ArgumentMatchers.any[Writes[T]],
      ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(response)

  def verifyStoreData[T](journeyId: String,
                         dataKey: String,
                         data: T): Unit =
    verify(mockIncorporatedEntityInformationConnector).storeData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey),
      ArgumentMatchers.eq(data)
    )(ArgumentMatchers.any[Writes[T]],
      ArgumentMatchers.any[HeaderCarrier])

}
