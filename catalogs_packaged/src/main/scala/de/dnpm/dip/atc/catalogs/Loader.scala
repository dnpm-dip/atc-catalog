package de.dnpm.dip.atc.catalogs


import cats.Eval
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
      (2020 to 2025).map(_.toString).toList
    )

/*
  override def inputStreams: NonEmptyList[(String,Eval[InputStream])] = 
    versions.map( v =>
      v -> Eval.later(this.getClass.getClassLoader.getResourceAsStream(s"ATC_$v.csv"))
    )
*/ 

  override def catalogs = //: NonEmptyList[(String,Eval[CodeSystem[ATC]])] = 
    versions.map(
      v => v -> Eval.later(
        ATCCatalogsImpl.TsvParser.parse(v,this.getClass.getClassLoader.getResourceAsStream(s"ATC_$v.csv"))
      )
    )
  
}
