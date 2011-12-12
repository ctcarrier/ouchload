package com.ouchload.service

import com.ouchload.LoadManager
import akka.dispatch.Future
import java.util.Date
import com.ouchload.job._

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
/**
 * @author chris_carrier
 * @version 12/10/11
 */


trait LoadService {

  def saveTask(lt: LoaderTask): Option[LoaderTask]
  def getLoadTask(objectId: String): Option[LoaderTask]
  def getNewJob: Option[LoaderTask]
}

class LoadServiceImpl(db: MongoCollection) extends LoadService {

  def saveTask(lt: LoaderTask): Option[LoaderTask] = {
    val dbo = grater[LoaderTask].asDBObject(lt)
    db += dbo
    val result: LoaderTask = lt.copy(_id = Some(dbo.get("_id").asInstanceOf[ObjectId]))
    Some(result)
  }

  def getLoadTask(objectId: String): Option[LoaderTask] = {

    val dbo = db.findOne(MongoDBObject("_id" -> new ObjectId(objectId)))

    dbo.map(x => grater[LoaderTask].asObject(x))
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