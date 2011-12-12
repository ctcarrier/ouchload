package com.ouchload.service

import com.ouchload.LoadManager
import com.mongodb.casbah.{MongoCollection, MongoDB}
import com.novus.salat._
import com.novus.salat.global._
import org.bson.types.ObjectId
import akka.dispatch.Future
import com.mongodb.casbah.commons.MongoDBObject
import java.util.Date
import com.ouchload.job._
import com.mongodb.casbah.Imports._

/**
 * @author chris_carrier
 * @version 12/10/11
 */


trait LoadService {

  def saveTask(lt: LoaderTask): Option[LoaderTask]
  def getLoadJob(objectId: String)
  def getNewJob: Option[LoaderTask]
}

class LoadServiceImpl(db: MongoCollection) extends LoadService {

  def saveTask(lt: LoaderTask): Option[LoaderTask] = {
    val dbo = grater[LoaderTask].asDBObject(lt)
    db += dbo
    val result: LoaderTask = lt.copy(_id = Some(dbo.get("_id").asInstanceOf[ObjectId]))
    Some(result)
  }

  def getLoadJob(objectId: String) {
    db.findOne(MongoDBObject("_id" -> objectId))
  }

  def getNewJob: Option[LoaderTask] = {
    val query = MongoDBObject("state" -> "new")

    val newState = "processed"
    //val nextStateInfo = LoaderJobState(newState.name, new Date())
    val update = $set(("state" -> newState))

    val task = db.findAndModify(query, null, null, false, update, false, false) // need this to return the new (updated) doc

    task.map(f => grater[LoaderTask].asObject(f))
  }

}