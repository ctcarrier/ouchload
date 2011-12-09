package com.timing

import org.specs2.Specification
import org.specs2.main.ArgumentsArgs._
import org.specs2.matcher.MustThrownMatchers
import akka.event.slf4j.Logging

/**
 * @author chris_carrier
 * @version 12/8/11
 */


class TimingSpec extends Specification with Logging with TimingSupport with MustThrownMatchers {

  val sleepTime = 100l

  def is = args() ^
    "The TimingSupport should" ^
    "time a code block with a single execution" ! timingTest() ^
    "time a code block with 10 executions" ! timingTest(10) ^
    "time multiple code blocks with different keys" ! timingTestMultipleKeys() ^
    end

    def timingTest(executionCount: Int = 1) = {

      val start = System.currentTimeMillis
      val testObj = new TimingSupport() with Logging {
        for (i <- 0 until executionCount) {
          withTiming("test") {
            Thread.sleep(sleepTime)
          }
        }
      }
      val stats = testObj.getStats
        val stop = System.currentTimeMillis
        val outerTotal = stop - start

        stats must not be empty
        stats.head.averageTime must beGreaterThanOrEqualTo(sleepTime) and beLessThanOrEqualTo(outerTotal)
        stats.head.totalTime must beGreaterThanOrEqualTo(sleepTime * executionCount) and beLessThanOrEqualTo(outerTotal)
    }

    def timingTestMultipleKeys() = {

      val testObj = new TimingSupport() with Logging {
        for (i <- 0 until 1) {
          withTiming("test1") {
            Thread.sleep(sleepTime)
          }
        }

        for (i <- 0 until 10) {
          withTiming("test2") {
            Thread.sleep(sleepTime)
          }
        }
      }

      val stats = testObj.getStats

        stats must not be empty
      stats.size must be equalTo 2

      log.info(stats(0).toString)
      log.info(stats(1).toString)

      stats.filter(s => s.key.equals(Some("test1"))).size must be equalTo 1
      stats.filter(s => s.key.equals(Some("test2"))).size must be equalTo 1

    }
}