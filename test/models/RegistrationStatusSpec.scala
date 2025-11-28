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

package models

import utils.UnitSpec
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.http.InternalServerException

class RegistrationStatusSpec extends UnitSpec {

  "RegistrationStatus writes" should {
    "write Registered with bpId" in {
      val json = Json.toJson[RegistrationStatus](Registered("BPID-1")).as[JsObject]
      (json \ RegistrationStatus.registrationStatusKey).as[String] mustBe RegistrationStatus.RegisteredKey
      (json \ RegistrationStatus.registeredBusinessPartnerIdKey).as[String] mustBe "BPID-1"
    }
    "write RegistrationFailed with failures array when present" in {
      val json = Json.toJson[RegistrationStatus](RegistrationFailed(Some(Array(Failure("A","x"), Failure("B","y"))))).as[JsObject]
      (json \ RegistrationStatus.registrationStatusKey).as[String] mustBe RegistrationStatus.RegistrationFailedKey
      (json \ RegistrationStatus.registrationFailuresKey).as[Seq[JsObject]].size mustBe 2
    }
    "write RegistrationFailed without failures when None" in {
      val json = Json.toJson[RegistrationStatus](RegistrationFailed(None)).as[JsObject]
      (json \ RegistrationStatus.registrationStatusKey).as[String] mustBe RegistrationStatus.RegistrationFailedKey
      (json \ RegistrationStatus.registrationFailuresKey).toOption mustBe None
    }
    "write RegistrationNotCalled" in {
      val json = Json.toJson[RegistrationStatus](RegistrationNotCalled).as[JsObject]
      (json \ RegistrationStatus.registrationStatusKey).as[String] mustBe RegistrationStatus.RegistrationNotCalledKey
    }
  }

  "RegistrationStatus reads" should {
    "read Registered with bpId" in {
      val json = Json.obj(
        RegistrationStatus.registrationStatusKey -> RegistrationStatus.RegisteredKey,
        RegistrationStatus.registeredBusinessPartnerIdKey -> "BPID-2"
      )
      json.as[RegistrationStatus] mustBe Registered("BPID-2")
    }
    "read RegistrationFailed with failures present" in {
      val json = Json.obj(
        RegistrationStatus.registrationStatusKey -> RegistrationStatus.RegistrationFailedKey,
        RegistrationStatus.registrationFailuresKey -> Json.arr(
          Json.obj("code" -> "A", "reason" -> "x"),
          Json.obj("code" -> "B", "reason" -> "y")
        )
      )
      json.as[RegistrationStatus] match {
        case RegistrationFailed(Some(arr)) =>
          arr.toSeq mustBe Seq(Failure("A","x"), Failure("B","y"))
        case other => fail(s"Unexpected value: $other")
      }
    }
    "read RegistrationFailed with no failures when field missing" in {
      val json = Json.obj(RegistrationStatus.registrationStatusKey -> RegistrationStatus.RegistrationFailedKey)
      json.as[RegistrationStatus] mustBe RegistrationFailed(None)
    }
    "read RegistrationNotCalled" in {
      val json = Json.obj(RegistrationStatus.registrationStatusKey -> RegistrationStatus.RegistrationNotCalledKey)
      json.as[RegistrationStatus] mustBe RegistrationNotCalled
    }
  }

  "Invalid cases" should {
    "throw InternalServerException for invalid reads discriminator" in {
      val json = Json.obj(RegistrationStatus.registrationStatusKey -> "UNKNOWN")
      val ex = intercept[InternalServerException] { json.as[RegistrationStatus] }
      ex.getMessage must include ("Invalid registration status")
    }
  }
}
