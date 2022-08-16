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
import play.api.test.Helpers._
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureCHRN => messages}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse

import scala.concurrent.Future


trait CaptureCHRNumberViewTests {
  this: ComponentSpecHelper =>

  def testCaptureCHRNView(result: => WSResponse,
                          authStub: => StubMapping,
                          insertJourneyConfig: => Future[InsertOneResult]): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      Jsoup.parse(result.body)
    }

    lazy val config = app.injector.instanceOf[AppConfig]

    "have a back link" in {
      val backLinks: Elements = doc.getBackLinks

      backLinks.size mustBe 1

      backLinks.first.text mustBe Base.back
    }

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getElementsByClass("govuk-link").get(1).attr("href") mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      if (doc.getServiceName.text.equals("Entity Validation Service")){
        doc.title mustBe s"${messages.title} - $testDefaultServiceName - GOV.UK"
      } else {
        doc.title mustBe s"${messages.title} - $testCallingServiceName - GOV.UK"
      }

    }

    "have the correct page header" in {
      val headers: Elements = doc.getElementsByTag("h1")

      headers.size mustBe >=(1)

      headers.first.text mustBe messages.heading
    }

    "have the correct inset text" in {

      val insetElements: Elements = doc.getElementsByClass("govuk-inset-text")

      insetElements.size mustBe 1

      insetElements.first.text mustBe messages.insetText
    }

    "have correct label in the form" in {
      doc.getLabelElement.first.text() mustBe messages.labelText
    }

    "have the correct hint text" in {
      doc.getParagraphs.get(1).text mustBe messages.hintText
    }

    "have an input text box with the identifier 'chrn'" in {

      val optInput: Option[Element] = Option(doc.getElementById("chrn"))

      optInput match {
        case Some(input) => input.attr("type") mustBe "text"
        case None => fail("""Input element "chrn" cannot be found""")
      }
    }

    "have a link to enable users to skip to check your answers page" in {

      val optLink: Option[Element] = Option(doc.getElementById("no-chrn"))

      optLink match {
        case Some(link) => link.text mustBe messages.noChrnLink
        case None => fail(s"""Link "no-chrn" cannot be found""")
      }
    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }
  }

  def testCaptureCHRNErrorMessagesNotEntered(result: => WSResponse,
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
      doc.getErrorSummaryBody.text mustBe messages.Error.noChrnEntered
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noChrnEntered
    }
  }

  def testCaptureCHRNErrorMessagesInvalidLength(result: => WSResponse,
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
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidLengthChrnEntered
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidLengthChrnEntered
    }
  }

  def testCaptureCHRNErrorMessagesInvalidFormat(result: => WSResponse,
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
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidChrnEntered
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidChrnEntered
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
