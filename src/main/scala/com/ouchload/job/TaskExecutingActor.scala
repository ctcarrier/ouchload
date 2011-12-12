package com.ouchload.job

import com.ouchload.LoadManager
import com.timing.result.{SimpleResultFormatting, Slf4jResultHandling}
import com.ouchload.service.LoadService
import akka.event.slf4j.Logging
import akka.actor.{Scheduler, Actor}
import Actor._
import java.util.concurrent.TimeUnit

/**
 * @author chris_carrier
 * @version 12/11/11
 */

case object Sync

class TaskExecutingActor(service: LoadService) extends Actor with Logging {

  def receive = {

    case Sync => {
      val taskReportingActor = actorOf[TaskReportingActor].start
      self.link(taskReportingActor)

      val lt = service.getNewJob
      lt.foreach(x => {
        val lm = new LoadManager(x) with SimpleResultFormatting with Slf4jResultHandling with Logging
        log.info("Starting jobs")
        lm.start
        Scheduler.schedule(taskReportingActor, lm, 100, 100, TimeUnit.MILLISECONDS)
      })
    }
  }

}