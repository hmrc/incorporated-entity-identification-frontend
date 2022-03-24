/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.ValidateIncorporatedEntityDetailsHttpParser.IncorporatedEntityDetailsValidationResult
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.ValidateIncorporatedEntityDetailsService

import scala.concurrent.Future

trait MockValidateIncorporatedEntityDetailsService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockValidateIncorporatedEntityDetailsService: ValidateIncorporatedEntityDetailsService = mock[ValidateIncorporatedEntityDetailsService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockValidateIncorporatedEntityDetailsService)
  }

  def mockValidateIncorporatedEntityDetails(companyNumber: String,
                                            ctutr: Option[String])
                                           (response: Future[IncorporatedEntityDetailsValidationResult]): OngoingStubbing[_] = {
    when(mockValidateIncorporatedEntityDetailsService.validateIncorporatedEntityDetails(
      ArgumentMatchers.eq(companyNumber),
      ArgumentMatchers.eq(ctutr)
    )(ArgumentMatchers.any[HeaderCarrier])).thenReturn(response)
  }
}
