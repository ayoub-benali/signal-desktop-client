package de.m7w3.signal

import java.io.File

import net.harawata.appdirs.AppDirsFactory

object Directories {
  lazy val defaultProfileDir: File =
    new File(AppDirsFactory.getInstance().getUserDataDir(App.NAME, null, App.AUTHOR))


}
