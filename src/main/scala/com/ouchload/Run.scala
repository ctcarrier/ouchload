package com.ouchload



/**
 * @author chris_carrier
 * @version 12/2/11
 */


object Run {

  def main(args: Array[String]) {

    new Executor()
    sys.exit(1)
  }
}

class Executor extends Loader {

  testWith(5) connections "www.efgdfgdfgdfgxample.com"
}