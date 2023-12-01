package de.dnpm.dip.atc.impl


import java.time.Year
import java.io.{
  InputStream,
  File,
  FileInputStream
}
import scala.util.Try
import scala.util.matching.Regex
import cats.Applicative
import cats.data.NonEmptyList
import scala.collection.concurrent.{
  Map,
  TrieMap
}
import de.dnpm.dip.util.{
  Logging,
  SPI,
  SPILoader
}
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI,
  Version
}
import de.dnpm.dip.coding.atc.{
  ATC,
  Kinds
}



class ATCCodeSystemProviderSPI extends CodeSystemProviderSPI
{

  def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
    new ATCCatalogsImpl.Facade[F]
}


class ATCCatalogsSPIImpl extends ATC.CatalogsSPI
{

  def getInstance[F[_]]: ATC.Catalogs[F,Applicative[F]] = {
    new ATCCatalogsImpl.Facade[F]
  }

}


object ATCCatalogsImpl
{

  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
  trait Loader
  {
    def inputStreams: NonEmptyList[(String,InputStream)]
  }

  trait LoaderSPI extends SPI[Loader]

  private object Loader extends SPILoader[LoaderSPI]


  private class DefaultLoader extends Loader with Logging
  {
    val sysProp = "dnpm.dip.catalogs.dir"

    val fileName = """ATC_(\d{4})\.csv""".r
       
    override def inputStreams: NonEmptyList[(String,InputStream)] = {

      Option(System.getProperty(sysProp)).map(new File(_)) match {

        case None => {
          val msg = s"Please define the directory from which to load ATC catalogs using System Property $sysProp"
          log.error(msg)

          throw new NoSuchElementException(msg)
        }

        case Some(dir) =>
          NonEmptyList.fromListUnsafe(
            dir.listFiles(f => fileName.matches(f.getName))
              .toList
              .map(f =>
                f.getName match {
                  case fileName(year) => (year, new FileInputStream(f)) 
                } 
              )
          )

      }

    }
  }
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------


  object TsvParser
  {
    import scala.io.Source
    import scala.util.matching.Regex
    import CodeSystem.Concept

    private val separator = "\t"
    private val group     = "([A-Z]{1}[0-9]{2}[A-Z]{2})".r.unanchored
    private val substance = "([A-Z]{1}[0-9]{2}[A-Z]{2}[0-9]{2})".r.unanchored

    def parse(version: String, in: InputStream): CodeSystem[ATC] = {

      val src =
        Source.fromInputStream(in)

      val (concepts,lastGroup,lastSubstances) =
        src.getLines()
          .filter(line => group.findPrefixOf(line).isDefined)
          .map(_.replace(s"$separator$separator",separator).split(separator))
          .foldLeft[
            (Seq[Concept[ATC]],Option[Concept[ATC]],Set[Concept[ATC]])
          ](
            (Seq.empty,None,Set.empty)
          ){
            case ((acc,currentGroup,substances),csv) =>
  
              val name = csv(1)

              val ddd = Try(csv(2)).toOption
  
              csv(0) match {
  
                // If parsing a substance, i.e. child of the current group,
                // update the respective accumulator value accordingly...
                case substance(code) =>
                  (
                   acc,
                   currentGroup.map(
                     grp => grp.copy(children = grp.children.map(_ + Code[ATC](code)))
                   ),
                   substances +
                     Concept[ATC](
                       Code(code),
                       name,
                       Some(version),
                       Map(ATC.Kind.name -> Set(Kinds.Substance.toString)) ++ ddd.map(v => ATC.DDD.name -> Set(v)),
                       currentGroup.map(_.code),
                       None
                     )
                  )
  
                // Else if parsing a group, the current group's end has been reached,
                // so append it and its child substances to the accumulator sequence
                // and reset the group and substance accumulators, respectively
                case group(code) =>
                  (
                    acc ++ currentGroup ++ substances,
                    Some(
                     Concept[ATC](
                       Code(code),
                       name,
                       Some(version),
                       Map(ATC.Kind.name -> Set(Kinds.Group.toString)) ++ ddd.map(v => ATC.DDD.name -> Set(v)),
                       None,
                       Some(Set.empty)
                     )
                    ),
                    Set.empty
                  )
              }
  
          }
  
      src.close
  
      CodeSystem[ATC](
        Coding.System[ATC].uri,
        "ATC",
        Some("ATC-Klassifikation"),
        None,
        Some(version),
        ATC.properties,
        concepts ++ lastGroup ++ lastSubstances  // in above fold, last group and substances
                                                 // are not appended to acc sequence
      )
  
    }

  }

  private val loader =
    Loader.getInstance.getOrElse(new DefaultLoader)



  private val catalogs: Map[String,CodeSystem[ATC]] =
    TrieMap.from( 
      loader.inputStreams
        .toList
        .map {
          case (version,in) =>
            version -> TsvParser.parse(version,in)
        }
    )


  private [impl] class Facade[F[_]] extends ATC.Catalogs[F,Applicative[F]]
  {
    import cats.syntax.functor._

    val uri: java.net.URI =
      Coding.System[ATC].uri

    override val versionOrdering =
      Version.OrderedByYear

    override def versions(
      implicit F: Applicative[F]
    ): F[NonEmptyList[String]] =
      F.pure(
        NonEmptyList.fromListUnsafe(
          catalogs.keys.toList
        )
      )

    override def latestVersion(
      implicit F: Applicative[F]
    ): F[String] =
      F.pure(
        catalogs.keys.max(versionOrdering)
      )

    override def get(
      version: String
    )(
      implicit F: Applicative[F]
    ): F[Option[CodeSystem[ATC]]] =
      F.pure(
        catalogs.get(version) 
      )

    override def latest(
      implicit F: Applicative[F]
    ): F[CodeSystem[ATC]] =
      latestVersion.map(catalogs(_))

  }

}
