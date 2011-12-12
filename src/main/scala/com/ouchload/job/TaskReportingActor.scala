package com.ouchload.job

import akka.actor.Actor
import com.ouchload.LoadManager
import com.timing.result.TimingResultHandling
import akka.event.EventHandler
import akka.event.slf4j.Logging

/**
 * @author chris_carrier
 * @version 12/12/11
 */


class TaskReportingActor extends Actor with Logging {

  def receive = {

    case lm: LoadManager with TimingResultHandling => lm.handleResults
    case _ => log.info("ERROR!")
  }

}