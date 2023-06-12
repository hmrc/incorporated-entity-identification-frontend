/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

trait MockAuditConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  override protected def beforeEach(): Unit = {
    reset(mockAuditConnector)
    super.beforeEach()
  }

  val auditEventCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])

  def verifySendExplicitAuditUkCompany(): Unit =
    verify(mockAuditConnector, times(1)).sendExplicitAudit(
      ArgumentMatchers.eq("IncorporatedEntityRegistration"),
      auditEventCaptor.capture()
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    )

  def verifySendExplicitAuditRegisterSociety(): Unit =
    verify(mockAuditConnector, times(1)).sendExplicitAudit(
      ArgumentMatchers.eq("RegisteredSocietyRegistration"),
      auditEventCaptor.capture()
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    )

  def verifySendExplicitAuditCIO(): Unit =
    verify(mockAuditConnector, times(1)).sendExplicitAudit(
      ArgumentMatchers.eq("CIOEntityRegistration"),
      auditEventCaptor.capture()
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    )

  def verifyNoAuditSent(): Unit =
    verify(mockAuditConnector, times(0)).sendExplicitAudit(
      ArgumentMatchers.eq("IncorporatedEntityRegistration"),
      auditEventCaptor.capture()
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    )

}
