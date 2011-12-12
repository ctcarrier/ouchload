package com.ouchload

import dispatch._
import akka.actor.Actor._
import akka.routing.{Routing, CyclicIterator}
import Routing._
import akka.dispatch.Dispatchers
import akka.event.EventHandler
import java.net.{URL, UnknownHostException}
import akka.dispatch._
import akka.actor._

/**
 * @author chris_carrier
 * @version 12/1/11
 */


trait Loader {

  def testWith(p: PimpedInt): PimpedInt = p
}

object Actors {
  val reportingActor = actorOf[ReportingActor].start
}

class PimpedInt(i: Int) {

  def connections(url: String) = {


    val actors = List.fill(i)(actorOf(new RequestingActor).start)
    val futures: List[Future[Int]] = actors.map(x => (x ? url).mapTo[Int])

    val futureList = Future.sequence(futures, 1000)
    futureList.onComplete(f => {
      f.result.get match {
        case list: List[Int] => {
             val result = list.groupBy(x => x).map(xs => LoadResult(xs._1, xs._2.size))
          EventHandler.info(this, "Messaging reporter: " + result)
          Actors.reportingActor ! result
          Actors.reportingActor ! PoisonPill
          //actors.foreach(x => x ! PoisonPill)
//          list.groupBy(x => x).foreach(xs => println("%s : %s" format (xs._1.toString, xs._2.size.toString)))
        }
        case _ => EventHandler.error(this, "Something went wrong")
      }
    })

    //futureList.get
    Actor.registry.shutdownAll()
  }
}

object PimpedInt {

  implicit def intToPimp(i: Int): PimpedInt = new PimpedInt(i)
}

class RequestingActor extends Actor {

  implicit val http = new Http with thread.Safety

  def receive = {
    case x: String => {

      val request = :/(x)

      val response =  try {
        http(request >|)
        200
      } catch {
        case e: UnknownHostException => 404
        case e: dispatch.StatusCode => e.code
      }

      self.channel ! response
      self.stop
    }
    case _ => EventHandler.info(this, "received unknown message")
  }
}