package com.ouchload.job

import org.bson.types.ObjectId

/**
 * @author chris_carrier
 * @version 12/10/11
 */

sealed trait LoaderJobState{
   val name: String
}
case class NewState(name: String = "new") extends LoaderJobState
case class ProcessingState(name: String = "processing") extends LoaderJobState
case class ErrorState(name: String = "error") extends LoaderJobState
case class FinishedState(name: String = "finished") extends LoaderJobState

case class LoaderTask(_id: Option[ObjectId] = None, jobs: List[LoaderJob], state: String = "new")
case class LoaderJob(url: String, connections: Int)