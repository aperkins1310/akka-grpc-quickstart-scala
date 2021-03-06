package io.opencensus.scala.akka.http.utils

import akka.http.scaladsl.model.HttpResponse
import io.opencensus.scala.Tracing
import io.opencensus.scala.akka.http.trace.HttpAttributes._
import io.opencensus.scala.http.{HttpAttributes, StatusTranslator}
import io.opencensus.trace.Span

private[http] object MyEndSpanResponse {

  def forServer(
      tracing: Tracing,
      response: HttpResponse,
      span: Span
  ): HttpResponse =
    end(tracing, response, span, "response sent")

  def forClient(
      tracing: Tracing,
      response: HttpResponse,
      span: Span
  ): HttpResponse =
    end(tracing, response, span, "response received")

  private def end(
      tracing: Tracing,
      response: HttpResponse,
      span: Span,
      responseAnnotation: String
  ): HttpResponse = {

    HttpAttributes.setAttributesForResponse(span, response)
    span.addAnnotation(responseAnnotation)

    tracing.endSpan(span, StatusTranslator.translate(response.status.intValue()))
    response
  }
}
