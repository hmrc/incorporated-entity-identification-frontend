/*
 * Copyright 2024 HM Revenue & Customs
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
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.StorageService

import scala.concurrent.{ExecutionContext, Future}

trait MockStorageService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockStorageService: StorageService = mock[StorageService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStorageService)
  }

  def mockRetrieveCtutr(journeyId: String)
                       (response: Future[Option[String]]): OngoingStubbing[_] = {
    when(mockStorageService.retrieveCtutr(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveCHRN(journeyId: String)
                      (response: Future[Option[String]]): OngoingStubbing[_] = {
    when(mockStorageService.retrieveCHRN(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveCompanyProfile(journeyId: String)
                                (response: Future[Option[CompanyProfile]]): OngoingStubbing[_] = {
    when(mockStorageService.retrieveCompanyProfile(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveCompanyNumber(journeyId: String)
                               (response: Future[String]): OngoingStubbing[_] = {
    when(mockStorageService.retrieveCompanyNumber(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(response)
  }

  def mockRetrieveBusinessVerificationResponse(journeyId: String)
                                              (response: Future[Option[BusinessVerificationStatus]]): OngoingStubbing[_] = {
    when(mockStorageService.retrieveBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveRegistrationStatus(journeyId: String)
                                    (response: Future[Option[RegistrationStatus]]): OngoingStubbing[_] = {
    when(mockStorageService.retrieveRegistrationStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def mockRetrieveIdentifiersMatch(journeyId: String)
                                  (response: Future[Option[IncorporatedEntityDetailsMatching]]): OngoingStubbing[_] =
    when(mockStorageService.retrieveIdentifiersMatch(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus)
                                 (response: Future[StorageResult]): OngoingStubbing[_] = {
    when(mockStorageService.storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus): Unit = {
    verify(mockStorageService).storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

  def mockStoreIdentifiersMatch(journeyId: String, identifiersMatch: IncorporatedEntityDetailsMatching)
                               (response: Future[StorageResult]): OngoingStubbing[_] = {
    when(mockStorageService.storeIdentifiersMatch(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(identifiersMatch)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyStoreIdentifiersMatch(journeyId: String, identifiersMatch: IncorporatedEntityDetailsMatching): Unit = {
    verify(mockStorageService).storeIdentifiersMatch(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(identifiersMatch)
    )(ArgumentMatchers.any[HeaderCarrier])
  }


  def mockStoreCtutr(journeyId: String, ctutr: String)
                    (response: Future[StorageResult]): OngoingStubbing[_] = {
    when(mockStorageService.storeCtutr(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(ctutr)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyStoreCtutr(journeyId: String, ctutr: String): Unit = {
    verify(mockStorageService).storeCtutr(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(ctutr)
    )(ArgumentMatchers.any[HeaderCarrier])
  }


  def mockStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus)
                                         (response: Future[StorageResult]): OngoingStubbing[_] = {
    when(mockStorageService.storeBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(businessVerificationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus): Unit = {
    verify(mockStorageService).storeBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(businessVerificationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

}
