package de.dnpm.dip.atc.catalogs


import cats.Eval
import cats.data.NonEmptyList
import de.dnpm.dip.coding.CodeSystem
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.atc.impl.ATCCatalogsImpl



class ClassPathLoaderProvider extends ATCCatalogsImpl.LoaderSPI
{
  override def getInstance =
    new ClassPathLoader
}


class ClassPathLoader extends ATCCatalogsImpl.Loader
{

  private val versions =
    NonEmptyList.fromListUnsafe(
      (2020 to 2025).map(_.toString).toList
    )

  override def catalogs: NonEmptyList[(String,Eval[CodeSystem[ATC]])] =
    versions.map(
      version => version -> Eval.later {
        val stream = this.getClass.getClassLoader.getResourceAsStream(s"ATC_$version.csv")
        require(stream != null, s"Classpath resource ATC_$version.csv not found")
        ATCCatalogsImpl.TsvParser.parse(version, stream)
      }
    )
  
}
