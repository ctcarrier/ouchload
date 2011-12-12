package com.ouchload.job

import akka.actor.Actor
import com.ouchload.LoadManager
import com.timing.result.{SimpleResultFormatting, Slf4jResultHandling}
import com.ouchload.service.LoadService
import akka.event.slf4j.Logging

/**
 * @author chris_carrier
 * @version 12/11/11
 */

case object Sync

class TaskExecutingActor(service: LoadService) extends Actor {

  def receive = {

    case Sync => {
      val lt = service.getNewJob
      lt.foreach(x => {
        val lm = new LoadManager(x) with SimpleResultFormatting with Slf4jResultHandling with Logging
        lm.start.await
        lm.handleResults
      })
    }
  }

}