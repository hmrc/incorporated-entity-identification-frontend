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

package services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, _}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationStatus, CompanyProfile, RegistrationStatus, StorageResult}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.IncorporatedEntityInformationService

import scala.concurrent.{ExecutionContext, Future}

trait MockIncorporationEntityInformationService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockIncorporationEntityInformationService: IncorporatedEntityInformationService = mock[IncorporatedEntityInformationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockIncorporationEntityInformationService)
  }

  def mockRetrieveCtutr(journeyId: String)
                       (response: Future[Option[String]]): OngoingStubbing[_] = {
    when(mockIncorporationEntityInformationService.retrieveCtutr(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveCompanyProfile(journeyId: String)
                                (response: Future[Option[CompanyProfile]]): OngoingStubbing[_] = {
    when(mockIncorporationEntityInformationService.retrieveCompanyProfile(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveCompanyNumber(journeyId: String)
                                (response: Future[String]): OngoingStubbing[_] = {
    when(mockIncorporationEntityInformationService.retrieveCompanyNumber(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier],ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(response)
  }

  def mockRetrieveBusinessVerificationResponse(journeyId: String)
                                              (response: Future[Option[BusinessVerificationStatus]]): OngoingStubbing[_] = {
    when(mockIncorporationEntityInformationService.retrieveBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }
  def mockRetrieveRegistrationStatus(journeyId: String)
                                              (response: Future[Option[RegistrationStatus]]): OngoingStubbing[_] = {
    when(mockIncorporationEntityInformationService.retrieveRegistrationStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveIdentifiersMatch(journeyId: String)
                                  (response: Future[Option[Boolean]]): OngoingStubbing[_] =
    when(mockIncorporationEntityInformationService.retrieveIdentifiersMatch(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreRegistrationResponse(journeyId: String, registrationStatus: RegistrationStatus)
                                   (response: Future[StorageResult]): OngoingStubbing[_] = {
    when(mockIncorporationEntityInformationService.storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyStoreRegistrationResponse(journeyId: String, registrationStatus: RegistrationStatus): Unit = {
    verify(mockIncorporationEntityInformationService).storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

}
