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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.views

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, CaptureCompanyNumber => messages}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._

trait CaptureCompanyNumberTests {
  this: AnyWordSpec with Matchers =>

  def testCaptureCompanyNumberView(result: => WSResponse, authStub: => StubMapping): Unit = {
    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }

    "have a view with the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct first line" in {
      doc.getParagraphs.first.text mustBe messages.line_1 + " " + messages.linktext
    }

    "have the correct link" in {
      doc.getLink("companies-house").text mustBe messages.linktext
    }

    "have a correct details hint" in {
      doc.getHintText mustBe messages.hint
    }

    "have a save and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a save and come back later button" in {
      doc.getSubmitButton.get(1).text mustBe Base.saveAndComeBack
    }
  }

  def testCaptureCompanyNumberEmpty(result: => WSResponse, authStub: => StubMapping): Unit = {
    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.emptyCompanyNumber
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.emptyCompanyNumber
    }
  }

  def testCaptureCompanyNumberWrongLength(result: => WSResponse, authStub: => StubMapping): Unit = {
    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.lengthCompanyNumber
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.lengthCompanyNumber
    }
  }

  def testCaptureCompanyNumberWrongFormat(result: => WSResponse, authStub: => StubMapping): Unit = {
    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.formatCompanyNumber
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.formatCompanyNumber
    }
  }


}
