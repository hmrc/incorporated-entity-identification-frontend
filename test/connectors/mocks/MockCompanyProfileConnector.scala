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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.CompanyProfileConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CompanyProfile

import scala.concurrent.Future

trait MockCompanyProfileConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockCompanyProfileConnector: CompanyProfileConnector = mock[CompanyProfileConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCompanyProfileConnector)
  }

  def mockGetCompanyProfile(companyNumber: String)(response: Future[Option[CompanyProfile]]): OngoingStubbing[_] =
    when(mockCompanyProfileConnector.getCompanyProfile(
      ArgumentMatchers.eq(companyNumber)
    )(ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(response)

  def verifyGetCompanyProfile(companyNumber: String): Unit =
    verify(mockCompanyProfileConnector).getCompanyProfile(
      ArgumentMatchers.eq(companyNumber)
    )(ArgumentMatchers.any[HeaderCarrier])

}
