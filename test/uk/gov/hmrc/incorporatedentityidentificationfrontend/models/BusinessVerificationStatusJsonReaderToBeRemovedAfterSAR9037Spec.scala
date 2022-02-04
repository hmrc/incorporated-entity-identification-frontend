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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationStatus._

class BusinessVerificationStatusJsonReaderToBeRemovedAfterSAR9037Spec extends AnyFlatSpec {

  "BusinessVerificationStatus json" should "have form {\"verificationStatus\":\"theValue\"}" in {
    BusinessVerificationStatus.format.writes(BusinessVerificationFail) should be( Json.obj(businessVerificationStatusKey -> businessVerificationFailKey))
    BusinessVerificationStatus.format.writes(BusinessVerificationPass) should be( Json.obj(businessVerificationStatusKey -> businessVerificationPassKey))
  }

  "BusinessVerificationUnchallenged" should "be serialized into into UNCHALLENGED" in {
    BusinessVerificationStatus.format.writes(BusinessVerificationUnchallenged) should be( Json.obj(businessVerificationStatusKey -> "UNCHALLENGED"))
  }

  "{\"verificationStatus\":\"UNCHALLENGED\"}" should "be deserialized into BusinessVerificationUnchallenged" in {
    val toBeParsed = Json.obj(BusinessVerificationStatus.businessVerificationStatusKey -> "UNCHALLENGED")
    BusinessVerificationStatus.format.reads(toBeParsed) should be(JsSuccess(BusinessVerificationUnchallenged))
  }

}
