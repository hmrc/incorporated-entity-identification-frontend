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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.views

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureCompanyNumber => messages}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._

import scala.concurrent.Future

trait CaptureCompanyNumberTests {
  this: ComponentSpecHelper =>

  def testCaptureCompanyNumberView(result: => WSResponse,
                                   authStub: => StubMapping,
                                   insertJourneyConfig: => Future[WriteResult]): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      Jsoup.parse(result.body)
    }

    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have a sign out link that redirects to correct page" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      if (doc.getServiceName.text.equals("Entity Validation Service")){
        doc.title mustBe s"${messages.title} - $testDefaultServiceName - GOV.UK"
      } else {
        doc.title mustBe s"${messages.title} - $testCallingServiceName - GOV.UK"
      }

    }

    "have the correct link" in {
      doc.getParagraphs.eq(1).text mustBe messages.line_1
    }

    "have a correct details hint" in {
      doc.getHintText mustBe messages.hint
    }

    "have a save and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a back link" in {
      val backLinks: Elements = doc.getBackLinks

      backLinks.size mustBe 1

      backLinks.first.text mustBe Base.back
    }

    "have a link to the service's accessibility statement" in {
      val footerLinks: Elements = doc.getFooterLinks

      footerLinks.size() must be >= 2

      footerLinks.eq(1).attr("href") mustBe testAccessibilityUrl
    }

  }

  def testCaptureCompanyNumberEmpty(result: => WSResponse,
                                    authStub: => StubMapping,
                                    insertJourneyConfig: => Future[WriteResult]): Unit = {
    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      Jsoup.parse(result.body)
    }

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.emptyCompanyNumber
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.emptyCompanyNumber
    }
  }

  def testCaptureCompanyNumberWrongLength(result: => WSResponse,
                                          authStub: => StubMapping,
                                          insertJourneyConfig: => Future[WriteResult]): Unit = {
    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      Jsoup.parse(result.body)
    }

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.lengthCompanyNumber
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.lengthCompanyNumber
    }
  }

  def testCaptureCompanyNumberWrongFormat(result: => WSResponse,
                                          authStub: => StubMapping,
                                          insertJourneyConfig: => Future[WriteResult]): Unit = {
    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      Jsoup.parse(result.body)
    }

    "have the correct title" in {
      doc.title mustBe s"${Base.Error.error}${messages.title} - $testDefaultServiceName - GOV.UK"
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.formatCompanyNumber
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.formatCompanyNumber
    }
  }

  def testServiceName(serviceName: String,
                      result: => WSResponse,
                      authStub: => StubMapping,
                      insertJourneyConfig: => Future[WriteResult]): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the service name" in {
      doc.getServiceName.text mustBe serviceName
    }

  }

}
