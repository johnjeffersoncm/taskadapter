package com.taskadapter.webui

import java.io.{File, FilenameFilter}

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.taskadapter.connector.common.FileNameGenerator
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.writePretty
import org.slf4j.LoggerFactory

import scala.collection.mutable.Seq

class Storage(storageFolder: File, fileNamePrefix: String, fileNameExtension: String) {
  private val logger = LoggerFactory.getLogger(classOf[Storage])
  private val resultsFileFilter = new FilenameFilter {
    override def accept(dir: File, name: String): Boolean = name.startsWith(fileNamePrefix) && name.endsWith(fileNameExtension)
  }

  implicit val formats = DefaultFormats

  def get[T](elementId: String)(implicit man: Manifest[T]): Option[T] = {
    val file = getFileById(elementId)
    if (file.exists()) {
      Some(JsonConverter.convertFileToObject(file))
    } else {
      None
    }
  }

  def getByFilter[T](filter: (T) => Boolean)(implicit man: Manifest[T]): Option[T] = {
    val files = storageFolder.listFiles(resultsFileFilter)
    if (files != null) {
      files.foreach { file =>
        val obj = JsonConverter.convertFileToObject(file)
        if (filter(obj)) {
          return Some(obj)
        }
      }
    }
    None
  }

  def delete(elementId: String): Unit = {
    val file = getFileById(elementId)
    if (file.exists()) {
      file.delete()
    }
  }

  def deleteByFilter[T](filter: (T) => Boolean)(implicit man: Manifest[T]): Unit = {
    val files = storageFolder.listFiles(resultsFileFilter)
    if (files != null) {
      files.foreach { file =>
        val obj = JsonConverter.convertFileToObject(file)
        if (filter(obj)) {
          file.delete()
        }
      }
    }
  }

  def storeNewItemWithAutogeneratedName[T](result: T): Unit = {
    val file = FileNameGenerator.createSafeAvailableFile(storageFolder, s"${fileNamePrefix}-%d.$fileNameExtension", 10000000)
    store(result, file)
  }

  def store[T](result: T, elementId: String): Unit = {
    val file = getFileById(elementId)
    store(result, file)
  }

  private def store[T](result: T, file: File): Unit = {
    val jsonString = writePretty(result)
    storageFolder.mkdirs()
    Files.write(jsonString, file, Charsets.UTF_8)
    logger.debug(s"Saved $result to ${file.getAbsolutePath}")
  }

  private def getFileById(elementId: String): File = {
    val relativeFileName = s"$fileNamePrefix-$elementId.$fileNameExtension"
    new File(storageFolder, relativeFileName)
  }

  def getItems[T]()(implicit man: Manifest[T]): Seq[T] = {
    val files = storageFolder
      .listFiles(resultsFileFilter)
    if (files == null) return Seq()

    JsonConverter.convertFilesToObject[T](files)
  }
}
