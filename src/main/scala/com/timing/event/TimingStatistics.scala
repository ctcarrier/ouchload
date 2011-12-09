package com.timing.event

/**
 * @author chris_carrier
 * @version 12/7/11
 */

object TimingStatistics {

  implicit def timingEventList2TimingStatisticsList(list: List[TimingEvent]): List[TimingStatistics] = {

    val keyTimeMap: Map[Option[String], List[Option[Long]]] = list.groupBy(x => x.key)
      .map(y => y._1 -> y._2.groupBy(z => z.uuid)
      .map(xs => {
        val eventStart = xs._2.filter(p => p.eventType == Start)
        val eventStop = xs._2.filter(p => p.eventType == Stop)
      
      (eventStart, eventStop) match {
        case (start :: Nil, stop :: Nil ) => Some(stop.timestamp - start.timestamp)
        case _ => None
      }
      }
    )).asInstanceOf[Map[Option[String], List[Option[Long]]]]

    keyTimeMap.map(xs => TimingStatistics(xs._1, xs._2.flatten.sum / xs._2.size, xs._2.flatten.sum)).toList
  }
}

case class TimingStatistics(key: Option[String], averageTime: Long, totalTime: Long)