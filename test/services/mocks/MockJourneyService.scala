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
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.JourneyService

import scala.concurrent.Future

trait MockJourneyService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockJourneyService: JourneyService = mock[JourneyService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockJourneyService)
  }

  def mockGetJourneyConfig(journeyId: String,
                            authInternalId: String)
                           (response: Future[JourneyConfig]): OngoingStubbing[_] =
    when(mockJourneyService.getJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId)
    )).thenReturn(response)
}
