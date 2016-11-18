package de.m7w3.signal.store

object Directories {


}

object XDGDirectories {
  val HOME = sys.env("HOME")

  val XDG_DATA_HOME_VAR = "XDG_DATA_HOME"
  val XDG_DATA_HOME_DEFAULT = s"$HOME/.local/share"
  val XDG_DATA_HOME = sys.env.getOrElse(XDG_DATA_HOME_VAR, XDG_DATA_HOME_DEFAULT)

  val XDG_CONFIG_HOME_VAR = "XDG_CONFIG_HOME"
  val XDG_CONFIG_HOME_DEFAULT = s"$HOME/.local/share"
  

  // $XDG_DATA_HOME -> $HOME/.local/share
  // $XDG_DATA_DIRS -> /usr/local/share/:/usr/share/
  // $XDG_CONFIG_HOME -> $HOME/.config
  // $XDG_CONFIG_DIRS -> /etc/xdg
  // $XDG_CACHE_HOME -> $HOME/.cache
  // $XDG_RUNTIME_DIR -> warn
}
