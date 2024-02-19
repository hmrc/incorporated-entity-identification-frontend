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

package repositories.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.mongodb.scala.result.InsertOneResult
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.JourneyConfigRepository

import scala.concurrent.Future

trait MockJourneyConfigRepository extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockJourneyConfigRepository: JourneyConfigRepository = mock[JourneyConfigRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockJourneyConfigRepository)
  }

  def mockInsertJourneyConfig(journeyId: String,
                              authInternalId: String,
                              journeyConfig: JourneyConfig)
                             (response: Future[InsertOneResult]): OngoingStubbing[_] = {
    when(mockJourneyConfigRepository.insertJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId),
      ArgumentMatchers.eq(journeyConfig)
    )).thenReturn(response)
  }

  def mockFindJourneyConfig(journeyId: String, authInternalId: String)(response: Future[Option[JourneyConfig]]): OngoingStubbing[_] = {
    when(mockJourneyConfigRepository.findJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId)
    )
    ).thenReturn(response)
  }

  def verifyInsertJourneyConfig(journeyId: String,
                                authInternalId: String,
                                journeyConfig: JourneyConfig): Unit =
    verify(mockJourneyConfigRepository).insertJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId),
      ArgumentMatchers.eq(journeyConfig)
    )

  def verifyFindJourneyConfig(journeyId: String, authInternalId: String): Unit =
    verify(mockJourneyConfigRepository).findJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId)
    )

}
