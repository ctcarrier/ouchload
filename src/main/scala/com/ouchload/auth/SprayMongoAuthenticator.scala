package com.ouchload.auth

import org.bson.types.ObjectId
import cc.spray._
import cc.spray.http.{BasicHttpCredentials, HttpCredentials}
import com.mongodb.casbah.MongoConnection._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.Imports._
import com.novus.salat._
import com.novus.salat.global._
import akka.event.EventHandler

/**
 * @author chris_carrier
 * @version 10/19/11
 */


object FromMongoUserPassAuthenticator extends UserPassAuthenticator[BasicUserContext] {
  def apply(userPass: Option[(String, String)]) = {
    EventHandler.info(this, "Mongo auth")
    userPass.flatMap {
      case (user, pass) => {
        val db = MongoConnection()("mycotrack")("users")
        val userResult = db.findOne(MongoDBObject("username" -> user) ++ ("password" -> pass))
        userResult.map(grater[BasicUserContext].asObject(_))
      }
      case _ => None
    }
  }
  
}

case class BasicUserContext(_id: ObjectId, username: String, password: String)