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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.views.errorpages

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, NotFound => messages}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._

import scala.concurrent.Future

trait CompanyNumberNotFoundTests {
  this: ComponentSpecHelper =>

  def testCompanyNumberNotFoundView(result: => WSResponse,
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
      doc.getH1Elements.first.text mustBe messages.heading
    }

    "have the correct paragraph 1" in {
      doc.getParagraphs.eq(1).text mustBe messages.line1 + messages.line1link + "."
    }

    "have the correct paragraph 2" in {
      doc.getParagraphs.eq(2).text mustBe messages.line2 + messages.line2link + messages.line2b
    }

    "have a try again button" in {
      doc.getSubmitButton.first.text mustBe Base.tryAgain
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
