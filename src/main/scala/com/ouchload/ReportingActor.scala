package com.ouchload

import akka.actor.Actor
import akka.event.slf4j.Logging

/**
 * @author chris_carrier
 * @version 12/3/11
 */


class ReportingActor extends Actor with Logging {

  def receive = {

    case l: List[LoadResult] => l.foreach(x => log.info("%s status code %s times" format (x.statusCode, x.count)))

    case _ => {
      log.error("Something went wrong")
    }
  }

}