package com.timing

import event._
import java.util.UUID
import com.timing.event.TimingStatistics._
import akka.event.slf4j.Logging


/**
 * @author chris_carrier
 * @version 12/7/11
 */




class Timing extends Logging {

  val time = System.currentTimeMillis()
  var timingEvents: List[TimingEvent] = List()
}

trait TimingSupport { self: Logging =>

  val timing = new Timing

  def withTiming[A](s: String)(f: => A): A = {
    val uuid = UUID.randomUUID()
    timing.timingEvents = TimingEvent(Start, Some(s), System.currentTimeMillis(), Some(uuid)) :: timing.timingEvents
    val result = f
    timing.timingEvents = TimingEvent(Stop, Some(s), System.currentTimeMillis(), Some(uuid)) :: timing.timingEvents
    result
  }


  def getStats: List[TimingStatistics] = {

    val events = timing.timingEvents
    events
  }
}