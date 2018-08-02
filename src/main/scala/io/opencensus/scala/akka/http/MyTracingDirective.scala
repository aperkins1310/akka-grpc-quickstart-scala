package io.opencensus.scala.akka.http

import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.server.{Directive1, ExceptionHandler}
import akka.http.scaladsl.server.Directives.{extractRequest, handleExceptions, mapResponse, provide}
import io.opencensus.scala.Tracing
import io.opencensus.scala.akka.http.propagation.AkkaB3FormatPropagation
import io.opencensus.scala.akka.http.utils.{EndSpanResponse, MyEndSpanResponse}
import io.opencensus.scala.http.{HttpAttributes, ServiceAttributes, ServiceData}
import io.opencensus.scala.akka.http.trace.HttpAttributes._
import io.opencensus.scala.http.propagation.Propagation
import io.opencensus.trace.{Span, Status}

import scala.util.control.NonFatal

object MyTracingDirective extends TracingDirective {
    override protected def tracing: Tracing = Tracing
    override protected def propagation: Propagation[HttpHeader, HttpRequest] =
      AkkaB3FormatPropagation

  def traceRequest(serviceData: ServiceData): Directive1[Span] =
    extractRequest.flatMap { req =>
      val span = buildSpan(req, serviceData)
      recordSuccess(span) & recordException(span) & provide(span)
    }

  private def buildSpan(req: HttpRequest, serviceData: ServiceData): Span = {
    val name = req.uri.path.toString()

    val span = propagation
      .extractContext(req)
      .fold(
        { error =>
          logger.debug("Extracting of parent context failed", error)
          tracing.startSpan(name)
        },
        tracing.startSpanWithRemoteParent(name, _)
      )


    ServiceAttributes.setAttributesForService(span, serviceData)
    HttpAttributes.setAttributesForRequest(span, req)

    span
  }

  private def recordSuccess(span: Span) =
    mapResponse(EndSpanResponse.forServer(tracing, _, span))

  private def recordException(span: Span) =
    handleExceptions(ExceptionHandler {
      case NonFatal(ex) =>
        tracing.endSpan(span, Status.INTERNAL)
        throw ex
    })
}
