package uk.gov.hmrc.incorporatedentityidentificationfrontend.utils

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}

import scala.collection.JavaConverters._

object ViewSpecHelper {

  implicit class ElementExtensions(element: Element) {

    lazy val content: Element = element.getElementsByTag("article").asScala.head

    lazy val getParagraphs: Elements = element.getElementsByTag("p")

    lazy val getBulletPoints: Elements = element.getElementsByTag("li")

    lazy val getH1Elements: Elements = element.getElementsByTag("h1")

    lazy val getH2Elements: Elements = element.getElementsByTag("h2")

    lazy val getFormElements: Elements = element.getElementsByClass("form-field-group")

    lazy val getLabelElement: Elements = element.getElementsByTag("label")

    lazy val getLegendElement: Elements = element.getElementsByTag("legend")

    lazy val getErrorSummaryTitle: Elements = element.getElementsByClass("govuk-error-summary__title")

    lazy val getErrorSummaryBody: Elements = element.getElementsByClass("govuk-error-summary__body")

    lazy val getFieldErrorMessage: Elements = element.getElementsByClass("govuk-error-message")

    lazy val getSubmitButton: Elements = element.getElementsByClass("govuk-button")

    lazy val getHintText: String = element.select(s"""span[class=govuk-hint]""").text()

    lazy val getForm: Elements = element.select("form")

    lazy val getSummaryListRows: Elements = element.getElementsByClass("govuk-summary-list__row")

    def getSpan(id: String): Elements = element.select(s"""span[id=$id]""")

    def getLink(id: String): Elements = element.select(s"""a[id=$id]""")

    def getTextFieldInput(id: String): Elements = element.select(s"""input[id=$id]""")

    def getBulletPointList: Elements = element.select("ul[class=list list-bullet]")

    def getSummaryListQuestion: String = element.getElementsByClass("govuk-summary-list__key").text

    def getSummaryListAnswer: String = element.getElementsByClass("govuk-summary-list__value").text

    def getSummaryListChangeLink: String = element.select("dd.govuk-summary-list__actions > a").attr("href")

    def getSummaryListChangeText: String = element.select("dd.govuk-summary-list__actions > a").text
  }

  def text(text: String): HavePropertyMatcher[Elements, String] =
    (element: Elements) => HavePropertyMatchResult(
      element.text() == text,
      "text",
      text,
      element.text()
    )

}
