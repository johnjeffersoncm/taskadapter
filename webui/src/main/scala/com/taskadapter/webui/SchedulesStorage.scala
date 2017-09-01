package com.taskadapter.webui

import java.io.File

import com.taskadapter.web.uiapi.{ConfigId, Schedule}

import scala.collection.mutable._

class SchedulesStorage(rootDir: File) {
  val dataFolder = new File(rootDir, "schedules")
  val storage = new Storage(dataFolder, "schedule", "json")

  def store(result: Schedule): Unit = {
    storage.storeNewItemWithAutogeneratedName(result)
  }

  def getSchedules(configId: ConfigId): Seq[Schedule] = {
    getSchedules().filter(r => r.configId == configId)
  }

  def getSchedules(): Seq[Schedule] = {
    storage.getItems[Schedule]()
  }

  def get(id: String): Option[Schedule] = {
    storage.get[Schedule](s => s.id == id)
  }

  def delete(id: String): Unit = {
    storage.delete[Schedule](s => s.id == id)
  }
}
