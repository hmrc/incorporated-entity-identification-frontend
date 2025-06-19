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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.views

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureCtutr => messages}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

import scala.concurrent.Future


trait CaptureCtutrViewTests {
  this: ComponentSpecHelper =>

  def testCaptureCtutrView(result: => WSResponse,
                           authStub: => StubMapping,
                           insertJourneyConfig: => Future[InsertOneResult]): Unit = {

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
      doc.title mustBe expectedTitle(doc, messages.title)
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have the correct first line" in {
      doc.getParagraphs.eq(1).text mustBe messages.p1
    }

    "have the correct second line" in {
      doc.getParagraphs.eq(2).text mustBe messages.p2
    }

    "have the correct label" in {
      doc.getLabelElement.eq(0).text mustBe messages.label
    }

    "have the correct hint" in {
      doc.getHintText mustBe messages.hint
    }

    "have an input text field" in {
      val textInputs: Elements = doc.getTextFieldInput("ctutr")
      textInputs.size() mustBe 1

      textInputs.first.attr("type") mustBe "text"
    }

    "have a continue and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.continue
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

    "have the correct technical help link and text" in {

      doc.getTechnicalHelpLinkText mustBe Base.getHelp

      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCaptureOptionalCtutrView(result: => WSResponse,
                                   authStub: => StubMapping,
                                   insertJourneyConfig: => Future[InsertOneResult]): Unit = {

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
      doc.title mustBe expectedTitle(doc, messages.title, Some(messages.registered_society_title))
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.registered_society_heading
    }

    "have the correct first line" in {
      doc.getParagraphs.eq(1).text mustBe messages.line
    }

    "have an input text field" in {
      val textInputs: Elements = doc.getTextFieldInput("ctutr")
      textInputs.size() mustBe 1

      textInputs.first.attr("type") mustBe "text"
    }

    "have the correct details summary" in {
      doc.getDetailsSummaryText mustBe messages.noCtutr
    }

    "have the correct details drop down" in {
      doc.getParagraphs.eq(2).text mustBe messages.dropdown_line_1
      doc.getParagraphs.eq(3).text mustBe messages.dropdown_link_1
      doc.getParagraphs.eq(4).text mustBe messages.dropdown_link_2
    }

    "have a continue and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a back link" in {
      val backLinks: Elements = doc.getBackLinks

      backLinks.size mustBe 1

      backLinks.first.text mustBe Base.back
    }
  }

  def testCaptureCtutrErrorMessagesNoCtutr(result: => WSResponse,
                                           authStub: => StubMapping,
                                           insertJourneyConfig: => Future[InsertOneResult]): Unit = {
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
      doc.getErrorSummaryBody.text mustBe messages.Error.noCtutrEntered
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noCtutrEntered
    }
  }

  def testCaptureCtutrErrorMessagesInvalidCtutr(result: => WSResponse,
                                                authStub: => StubMapping,
                                                insertJourneyConfig: => Future[InsertOneResult]): Unit = {
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
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidCtutrEntered
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidCtutrEntered
    }
  }

  def testServiceName(serviceName: String,
                      result: => WSResponse,
                      authStub: => StubMapping,
                      insertJourneyConfig: => Future[InsertOneResult]): Unit = {

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
