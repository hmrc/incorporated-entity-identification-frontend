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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.views.errorpages

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{BetaBanner, Header, CtutrNotFound => messages}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._

import scala.concurrent.Future

trait CtutrNotFoundViewTests {
  this: ComponentSpecHelper =>

  def testCtutrNotFoundView(result: => WSResponse,
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

    "have the correct heading" in {
      doc.getH1Elements.first.text mustBe messages.heading
    }

    "have the correct content" in {
      doc.getParagraphs.eq(1).text mustBe messages.line1
      doc.getParagraphs.eq(2).text mustBe messages.line2
      doc.getParagraphs.eq(3).text mustBe messages.line3a + messages.line3Link + messages.line3b
      doc.getElementById("contact-corporation-tax-team").attr("href") mustBe "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/corporation-tax-enquiries"
      doc.getParagraphs.eq(4).text mustBe messages.line4 + messages.line4Link
      doc.getElementById("try-again").attr("href") mustBe routes.CtutrNotFoundController.tryAgain(testJourneyId).url
        }

    "have a link to the service's accessibility statement" in {
      val footerLinks: Elements = doc.getFooterLinks

      footerLinks.size() mustBe 9

      footerLinks.eq(1).attr("href") mustBe testAccessibilityUrl
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
