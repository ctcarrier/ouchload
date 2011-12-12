package com.ouchload

import dispatch._
import akka.routing.{Routing, CyclicIterator}
import job.{LoaderJob, LoaderTask}
import Routing._
import akka.dispatch.Dispatchers
import akka.event.EventHandler
import java.net.{URL, UnknownHostException}
import akka.dispatch._
import akka.actor._
import com.timing.TimingSupport
import com.timing.result.{SimpleResultFormatting, Slf4jResultHandling}
import akka.event.slf4j.Logging
import akka.actor.Actor._

/**
 * @author chris_carrier
 * @version 12/1/11
 */

object ImplicitDefs {

  implicit def loadInstance2LoadDsl(l: LoadInstance): LoadDsl = LoadDsl(l)

  implicit def loadDsl2LoadInstance(dsl: LoadDsl): LoadInstance = dsl.instance

  implicit def loadDsl2LoadInstanceList(dsl: LoadDsl): List[LoadInstance] = List(dsl.instance)

  implicit def loadInstance2LoadInstanceList(l: LoadInstance): List[LoadInstance] = List(l)
}

case class LoadDsl(instance: LoadInstance) {


  def using(connParam: Int) = LoadDsl(instance.copy(connectionCount = Some(connParam)))

  def ~(i: List[LoadInstance]) = instance :: i

  def connections = this
}

case class LoadInstance(url: String, connectionCount: Option[Int] = None)

class LoadManager(loaderTask: LoaderTask)
  extends TimingSupport {

  val reportingActor = Actor.registry.actorsFor[ReportingActor].head
  val loadingActor = actorOf(new LoadingActor).start

  def start = {
    val allFutures = loaderTask.jobs.map(job => (loadingActor ? job).mapTo[List[Int]])
    Future.sequence(allFutures, 300000)
  }


  class LoadingActor extends Actor {
    def receive = {
      case job: LoaderJob => {
        val actors = List.fill(job.connections)(actorOf(new RequestingActor).start)
        val futures: List[Future[Int]] = actors.map(x => (x ? job.url).mapTo[Int])

        val futureList = Future.sequence(futures, 30000)
        futureList.onComplete(f => {
          f.result.get match {
            case list: List[Int] => {
              val result = list.groupBy(x => x).map(xs => LoadResult(xs._1, xs._2.size))
              EventHandler.info(this, "Messaging reporter: " + result)
              reportingActor ! result
              EventHandler.info(this, "Messaging reporter: " + result)
            }
            case _ => EventHandler.error(this, "Something went wrong")
          }
        })

        self.channel ! futureList.get
      }
    }
  }

  class RequestingActor extends Actor {

    def receive = {
      case x: String => {

        val request = :/(x)

        withTiming(x) {
          val response = try {
            HttpRegistry.http(request >|)
            200
          } catch {
            case e: UnknownHostException => 404
            case e: dispatch.StatusCode => e.code
          }


          self.channel ! response
          self.stop
        }
      }
      case _ => EventHandler.info(this, "received unknown message")
    }
  }

}

object HttpRegistry {
  val http = new Http with thread.Safety with NoLogging
}