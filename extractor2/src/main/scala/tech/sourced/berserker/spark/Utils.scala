package tech.sourced.berserker.spark

import java.io.{File, IOException}
import java.util.UUID

import org.apache.log4j.Logger

import scala.util.control.NonFatal

/* from Apache Spark, used for FS operations from Workers
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

object Utils {

  val MAX_DIR_CREATION_ATTEMPTS: Int = 10

  /**
   * Execute a block of code that returns a value, re-throwing any non-fatal uncaught
   * exceptions as IOException. This is used when implementing Externalizable and Serializable's
   * read and write methods, since Java's serializer will not report non-IOExceptions properly;
   * see SPARK-4080 for more context.
   */
  def tryOrIOException[T](block: => T): T = {
    try {
      block
    } catch {
      case e: IOException =>
        logError("Exception encountered", e)
        throw e
      case NonFatal(e) =>
        logError("Exception encountered", e)
        throw new IOException(e)
    }
  }

  def logError(msg: => String, throwable: Throwable) {
    val log = Logger.getLogger(getClass.getName)
    log.error(msg, throwable)
  }

    def createTempDir(
        root: String = System.getProperty("java.io.tmpdir"),
        namePrefix: String = "spark"): File = {
      val dir = createDirectory(root, namePrefix)
      dir
    }

    def createDirectory(root: String, namePrefix: String = "spark"): File = {
      var attempts = 0
      val maxAttempts = MAX_DIR_CREATION_ATTEMPTS
      var dir: File = null
      while (dir == null) {
        attempts += 1
        if (attempts > maxAttempts) {
          throw new IOException("Failed to create a temp directory (under " + root + ") after " +
            maxAttempts + " attempts!")
        }
        try {
          dir = new File(root, namePrefix + "-" + UUID.randomUUID.toString)
          if (dir.exists() || !dir.mkdirs()) {
            dir = null
          }
        } catch {
          case e: SecurityException => dir = null;
        }
      }

      dir.getCanonicalFile
    }

}
