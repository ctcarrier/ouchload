package com.ouchload.boot

import akka.config.Supervision
import Supervision._
import cc.spray.connectors.Initializer
import akka.actor.Actor._
import cc.spray.HttpService._
import com.mongodb.ServerAddress
import com.mongodb.casbah.MongoConnection
import cc.spray.{HttpService, RootService}
import akka.event.slf4j.Logging
import com.ouchload.endpoint.LoadEndpoint
import com.ouchload.service.{LoadService, LoadServiceImpl}
import com.ouchload.ReportingActor
import akka.actor.{Scheduler, Supervisor}
import java.util.concurrent.TimeUnit
import com.ouchload.job.{Sync, TaskExecutingActor}

/**
 * @author chris_carrier
 */

class Boot extends Initializer with Logging {

  log.info("Running Initializer")

  val akkaConfig = akka.config.Config.config

  val mongoUrl = akkaConfig.getString("mongodb.url", "localhost")
  val mongoDbName = akkaConfig.getString("mongodb.database", "gattling")
  val loadJobCollection = akkaConfig.getString("mongodb.loadJob.collection", "gattlingJobs")

  val urlList = mongoUrl.split(",").toList.map(new ServerAddress(_))
  val db = urlList match {
    case List(s) => MongoConnection(s)(mongoDbName)
    case s: List[String] => MongoConnection(s)(mongoDbName)
    case _ => MongoConnection("localhost")(mongoDbName)
  }

  val reportingActor = actorOf[ReportingActor].start
  val loadServiceImpl: LoadService = new LoadServiceImpl(db(loadJobCollection))
  val taskExecutingActor = actorOf(new TaskExecutingActor(loadServiceImpl)).start

  Scheduler.schedule(taskExecutingActor, Sync, 1, 5, TimeUnit.SECONDS) //TODO: set job timer to a proper time

  // ///////////// INDEXES for collections go here (include all lookup fields)
  //  configsCollection.ensureIndex(MongoDBObject("customerId" -> 1), "idx_customerId")
  val loadModule = new LoadEndpoint {val service = loadServiceImpl}

  val loadService = actorOf(new HttpService(loadModule.restService))
  val rootService = actorOf(new RootService(loadService))

  // Start all actors that need supervision, including the root service actor.
  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(loadService, Permanent),
        Supervise(rootService, Permanent)
      )
    )
  )
}

