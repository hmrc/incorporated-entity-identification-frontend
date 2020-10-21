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

package repositories.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.JourneyConfigRepository

import scala.concurrent.{ExecutionContext, Future}

trait MockJourneyConfigRepository extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockJourneyConfigRepository: JourneyConfigRepository = mock[JourneyConfigRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockJourneyConfigRepository)
  }

  def mockInsertJourneyConfig(journeyId: String, journeyConfig: JourneyConfig)
                             (response: Future[WriteResult]): OngoingStubbing[_] = {
    when(mockJourneyConfigRepository.insertJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(journeyConfig)
    )).thenReturn(response)
  }

 def mockFindById(journeyId: String)(response: Future[Option[JourneyConfig]]): OngoingStubbing[_] = {
    when(mockJourneyConfigRepository.findById(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.any[ReadPreference]
    )(ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(response)
  }

  def verifyInsertJourneyConfig(journeyId: String, journeyConfig: JourneyConfig): Unit =
    verify(mockJourneyConfigRepository).insertJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(journeyConfig)
    )

  def verifyFindById(journeyId: String): Unit =
    verify(mockJourneyConfigRepository).findById(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.any[ReadPreference]
    )(ArgumentMatchers.any[ExecutionContext])

}
