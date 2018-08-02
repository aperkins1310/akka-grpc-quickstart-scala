package com.example.helloworld

//#import
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import io.opencensus.scala.Tracing
import io.opencensus.trace.config.TraceConfig
import io.opencensus.trace.{Sampler}
import io.opencensus.trace.samplers.Samplers

//#import

//#client-request-reply
object GreeterClient {

  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("HelloWorldClient")
    implicit val mat = ActorMaterializer()
    implicit val ec = sys.dispatcher

//    val span = Tracing.startSpan("SPANNNNN")
//    println(span.getContext.getTraceId.toString)
    lazy val samplingRate: Double = 1
    def sampler: Sampler          = Samplers.probabilitySampler(samplingRate)

    def registerSamplerConfig(): Unit = {
      val traceConfig: TraceConfig = io.opencensus.trace.Tracing.getTraceConfig
      println(s"Setting sampler for OpenCensus tracing to $sampler")
      traceConfig.updateActiveTraceParams(traceConfig.getActiveTraceParams.toBuilder.setSampler(sampler).build)
    }

    registerSamplerConfig()



    val client = new GreeterServiceClient(
      GrpcClientSettings("127.0.0.1", 8080)
        .withOverrideAuthority("foo.test.google.fr")
          .withCertificate("ca.pem"))

    val names =

      if (args.isEmpty) List("Alice", "Bob")
      else args.toList

    names.foreach(singleRequestReply)
//    span.end()

    //#client-request-reply
    if (args.nonEmpty)
      names.foreach(streamingBroadcast)
    //#client-request-reply

    def singleRequestReply(name: String): Unit = {
      println(s"Performing request: $name")
      val reply = client.sayHello(HelloRequest(name))
      reply.onComplete {
        case Success(msg) =>
          println(msg)
        case Failure(e) =>
          println(s"Error: $e")
      }
    }

    //#client-request-reply
    //#client-stream
    def streamingBroadcast(name: String): Unit = {
      println(s"Performing streaming requests: $name")

      val requestStream: Source[HelloRequest, NotUsed] =
        Source
          .tick(1.second, 1.second, "tick")
          .zipWithIndex
          .map { case (_, i) => i }
          .map(i => HelloRequest(s"$name-$i"))
          .mapMaterializedValue(_ => NotUsed)

      val responseStream: Source[HelloReply, NotUsed] = client.sayHelloToAll(requestStream)
      val done: Future[Done] =
        responseStream.runForeach(reply => println(s"$name got streaming reply: ${reply.message}"))

      done.onComplete {
        case Success(_) =>
          println("streamingBroadcast done")
        case Failure(e) =>
          println(s"Error streamingBroadcast: $e")
      }
    }
    //#client-stream
    //#client-request-reply

  }

}
//#client-request-reply
