package de.dnpm.dip.atc.impl


import scala.util.Success
import scala.util.matching.Regex
import scala.io.Source
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.OptionValues._
import cats.Id
import de.dnpm.dip.coding.{
  Coding,
  CodeSystemProvider
}
import de.dnpm.dip.coding.atc.ATC


class Tests extends AnyFlatSpec
{

  private val group = "([A-Z]{1}[0-9]{2}[A-Z]{2})".r


  val cspTry = CodeSystemProvider.getInstance[Id]

  "CodeSystemProvider[Any,...]" must "have been successfully loaded" in {
    cspTry must be (a [Success[_]])
  }

  it must "have the expected CodingSystem.uri" in {
   cspTry.get.uri must be (Coding.System[ATC].uri)
  }



  val catalogTry = ATC.Catalogs.getInstance[Id]
  lazy val catalogs = catalogTry.get


  "ATC.Catalogs" must "have been successfully loaded" in {
    catalogTry must be (a [Success[_]])
  }


  it must "contain children for 'Proteinkinase-Inhibitoren'" in {

    val atc = catalogs.latest

    atc.concepts
      .find(_.display contains "Proteinkinase-Inhibitoren")
      .map(atc.childrenOf).value must not be empty
      
  }


  "Parsing DDD-Info" must "have succeeded" in {
   
    import ATC.extensions._

    catalogs.latest
      .concepts
      .filter(_.ddd.isDefined) must not be empty

  } 


/*
  "Dummy test to print JSON catalogs" must "have run" in {

    import java.io.FileWriter
    import play.api.libs.json.Json

    catalogs.versions.toList.foreach {
      v =>

        val w = new FileWriter(s"/home/lucien/Downloads/ATC_$v.json")

        w.write(Json.prettyPrint(Json.toJson(catalogs.get(v).get)))

        w.close
    }

  }
*/

}
