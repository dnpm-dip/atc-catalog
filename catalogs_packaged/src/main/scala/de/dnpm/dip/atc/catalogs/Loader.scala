package de.dnpm.dip.atc.catalogs


import java.io.InputStream
import cats.data.NonEmptyList
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
      (2020 to 2024).map(_.toString).toList
    )

  override def inputStreams: NonEmptyList[(String,InputStream)] = 
    versions.map( v =>
      v -> this.getClass.getClassLoader.getResourceAsStream(s"ATC_$v.csv")
    )
  
}
