/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package c10n.testproject.scala

import c10n.{C10NMessages, C10N}
import java.util.Locale
import c10n.annotations.{Ru, En, DefaultC10NAnnotations}

/**
 *
 * @author rodion
 */
object Main {
  def main(args: Array[String]) {
    /*
     * Use default locale bindings:
     * @En -> Locale.ENGLISH
     * @Ru -> Locale("ru")
     */
    C10N.configure(new DefaultC10NAnnotations())

    val gui: Gui = C10N.get(classOf[Gui])

    // assume a certain locale for illustration purposes
    Locale.setDefault(Locale.ENGLISH)
    //Locale.setDefault(new Locale("ru"))

    println("Welcome, " + gui.hello + "!")
    println("Click " + gui.menu.file + " to start.")
    println("Click " + gui.menu.print.pageSettings + " to open page settings.")
    println("Click " + gui.menu.print.execute("Canon iP90v") + " to print")
    println("Push the " + gui.menu.print.redButton + " for something special")
  }

  @C10NMessages
  trait Gui {
    @En("C10N user")
    @Ru("Polzovatel' C10N")
    def hello: String
    def menu: Menu
  }

  @C10NMessages
  trait Menu {
    @En("File Menu")
    @Ru("Menyu-fayl")
    def file: String
    def print: PrintMenu
  }

  @C10NMessages
  trait PrintMenu extends ExtendedPrintMenu{
    @En("Page Settings ...")
    @Ru("Nastroiki stranitsy ...")
    def pageSettings: String = "default values are ignored, sorry."

    @En("Print with {0}")
    @Ru("Pechat' na {0}")
    def execute(printerName: String): String
  }

  trait ExtendedPrintMenu{
    @En("red button")
    @Ru("krasnaya knopka")
    def redButton: String
  }
}

