package com.ouchload.dsl

import com.ouchload.job.LoaderJob

/**
 * @author chris_carrier
 * @version 12/11/11
 */


class Interpreter(tree: List[Statement]) {
  def run(): LoaderJob = {
    walkTree(tree, Context(None, None))
  }

  private def walkTree(tree: List[Statement], context: Context): LoaderJob = {
    tree match {
      case Url(urlInput) :: rest => {
        walkTree(rest, context.copy(url = Some(urlInput)))
      }

      case Connections(conns) :: rest => {
        walkTree(rest, context.copy(connections = Some(conns)))
      }

      case Nil => LoaderJob(context.url.get, context.connections.get)
    }
  }
}