package com.ouchload.dsl

import com.ouchload.LoadManager
import com.ouchload.job.LoaderJob

/**
 * @author chris_carrier
 * @version 12/11/11
 */



case class Context(url: Option[String], connections: Option[Int])