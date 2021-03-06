package com.gensler.scalavro.tests

import scala.util.{ Try, Success, Failure }
import scala.reflect.runtime.universe._

import com.gensler.scalavro.types._
import com.gensler.scalavro.types.primitive._
import com.gensler.scalavro.types.complex._

class AvroTypeSpec extends AvroSpec {

  // primitives
  "The AvroType companion object" should "return valid primitive avro types" in {
    AvroType[Boolean] should be (AvroBoolean)
    AvroType[Seq[Byte]] should be (AvroBytes)
    AvroType[Double] should be (AvroDouble)
    AvroType[Float] should be (AvroFloat)
    AvroType[Int] should be (AvroInt)
    AvroType[Long] should be (AvroLong)
    AvroType[Unit] should be (AvroNull)
    AvroType[String] should be (AvroString)
  }

  // arrays
  it should "return valid AvroArray types for Seqs" in {
    AvroType.fromType[Seq[Int]] match {
      case Success(avroType) => {
        // prettyPrint(avroType.schema)

        avroType.isInstanceOf[AvroArray[_, _]] should be (true)
        typeOf[avroType.scalaType] =:= typeOf[Seq[Int]] should be (true)
      }
      case Failure(cause) => throw cause
    }

    AvroType[IndexedSeq[String]] match {
      case avroType: AvroArray[_, _] => {
        // println(avroType.originalTypeTag.tpe)
        // prettyPrint(avroType.schema)
        avroType.originalTypeTag.tpe =:= typeOf[IndexedSeq[String]] should be (true)
      }
      case _ => fail
    }

    AvroType[Vector[Float]] match {
      case avroType: AvroArray[_, _] => {
        // println(avroType.originalTypeTag.tpe)
        // prettyPrint(avroType.schema)
        avroType.originalTypeTag.tpe =:= typeOf[Vector[Float]] should be (true)
      }
      case _ => fail
    }

    import scala.collection.mutable.ArrayBuffer
    AvroType[ArrayBuffer[Boolean]] match {
      case avroType: AvroArray[_, _] => {
        // println(avroType.originalTypeTag.tpe)
        // prettyPrint(avroType.schema)
        avroType.originalTypeTag.tpe =:= typeOf[ArrayBuffer[Boolean]] should be (true)
      }
      case _ => fail
    }

    AvroType.fromType[Seq[Seq[Seq[Byte]]]] match {
      case Success(avroType) => {
        // prettyPrint(avroType.schema)

        avroType.isInstanceOf[AvroArray[_, _]] should be (true)
        typeOf[avroType.scalaType] =:= typeOf[Seq[Seq[Seq[Byte]]]] should be (true)
      }
      case Failure(cause) => throw cause
    }
  }

  // sets
  it should "return valid AvroArray types for Sets" in {
    AvroType.fromType[Set[Int]] match {
      case Success(avroType) => {
        // prettyPrint(avroType.schema)

        avroType.isInstanceOf[AvroSet[_, _]] should be (true)
        typeOf[avroType.scalaType] =:= typeOf[Set[Int]] should be (true)
      }
      case Failure(cause) => throw cause
    }
  }

  // maps
  it should "return valid AvroMap types" in {
    AvroType.fromType[Map[String, Int]] match {
      case Success(avroType) => {
        // prettyPrint(avroType.schema)

        avroType.isInstanceOf[AvroMap[_, _]] should be (true)
        typeOf[avroType.scalaType] =:= typeOf[Map[String, Int]] should be (true)
      }
      case Failure(cause) => throw cause
    }

    AvroType.fromType[Map[String, Seq[Double]]] match {
      case Success(avroType) => {
        // prettyPrint(avroType.schema)

        avroType.isInstanceOf[AvroMap[_, _]] should be (true)
        typeOf[avroType.scalaType] =:= typeOf[Map[String, Seq[Double]]] should be (true)
      }
      case Failure(cause) => throw cause
    }
  }

  // unions
  it should "return valid AvroUnion types subtypes of Either[A, B]" in {
    AvroType[Either[Double, Int]] match {
      case avroType: AvroUnion[_, _] => {
        // prettyPrint(avroType.schema)

        avroType.union.contains[Double] should be (true)
        avroType.union.contains[Int] should be (true)
      }
      case _ => fail
    }

    AvroType[Either[Seq[Double], Map[String, Seq[Int]]]] match {
      case avroType: AvroUnion[_, _] => {
        // prettyPrint(avroType.schema)

        avroType.union.contains[Seq[Double]] should be (true)
        avroType.union.contains[Map[String, Seq[Int]]] should be (true)
      }
      case _ => fail
    }
  }

  it should "return valid AvroUnion types subtypes of Option[T]" in {
    AvroType[Option[String]] match {
      case avroType: AvroUnion[_, _] => {
        // prettyPrint(avroType.schema)

        avroType.union.contains[String] should be (true)
        avroType.union.contains[Unit] should be (true)
      }
      case _ => fail
    }
  }

  it should "return valid AvroUnion types subtypes of Union.not[A]" in {
    import com.gensler.scalavro.util.Union._
    AvroType[union[Int]#or[String]#or[Boolean]] match {
      case avroType: AvroUnion[_, _] => {
        // prettyPrint(avroType.schema)

        avroType.union.typeMembers should have size (3)

        avroType.union.contains[Int] should be (true)
        avroType.union.contains[String] should be (true)
        avroType.union.contains[Boolean] should be (true)
      }
      case _ => fail
    }
  }

  it should "return valid AvroUnion types supertypes of avro-typeable classes" in {
    AvroType[Alpha] match {
      case avroType: AvroUnion[_, _] => {
        // prettyPrint(avroType.schema)

        avroType.union.typeMembers should have size (2)
        avroType.union.contains[Gamma] should be (true)
        avroType.union.contains[Delta] should be (true)
        avroType.union.contains[AlphaCollection] should be (false)
      }
      case _ => fail
    }
  }

  it should "return valid AvroRecord types for case classes with abstract signatures" in {
    val avroType = AvroType[AlphaWrapper]

    // prettyPrint(avroType.schema)

    avroType.isInstanceOf[AvroRecord[_]] should be (true)
    typeOf[avroType.scalaType] =:= typeOf[AlphaWrapper] should be (true)
  }

  // fixed-length data
  it should "return valid AvroFixed types for subclasses of FixedData" in {

    val md5Type = AvroType[MD5]
    md5Type.isInstanceOf[AvroFixed[_]] should be (true)
    val md5Fixed = md5Type.asInstanceOf[AvroFixed[MD5]]
    md5Fixed.namespace should be (Some("com.gensler.scalavro.tests"))
    md5Fixed.name should be ("MD5")
    md5Fixed.fullyQualifiedName should be ("com.gensler.scalavro.tests.MD5")
    md5Fixed dependsOn md5Type should be (true)

    // prettyPrint(md5Type.schema)
    // prettyPrint(md5Type.parsingCanonicalForm)
  }

  // records
  it should "return valid AvroRecord types for product types" in {

    val personType = AvroType[Person]
    val santaListType = AvroType[SantaList]

    // prettyPrint(personType.schema)
    // prettyPrint(personType.parsingCanonicalForm)

    personType.isInstanceOf[AvroRecord[_]] should be (true)
    typeOf[personType.scalaType] =:= typeOf[Person] should be (true)
    val personRecord = personType.asInstanceOf[AvroRecord[Person]]
    personRecord.namespace should be (Some("com.gensler.scalavro.tests"))
    personRecord.name should be ("Person")
    personRecord.fullyQualifiedName should be ("com.gensler.scalavro.tests.Person")
    personType dependsOn personType should be (true)
    personType dependsOn santaListType should be (false)

    // prettyPrint(santaListType.schema)
    // prettyPrint(santaListType.parsingCanonicalForm)

    santaListType.isInstanceOf[AvroRecord[_]] should be (true)
    typeOf[santaListType.scalaType] =:= typeOf[SantaList] should be (true)
    val santaListRecord = santaListType.asInstanceOf[AvroRecord[SantaList]]
    santaListRecord.namespace should be (Some("com.gensler.scalavro.tests"))
    santaListRecord.name should be ("SantaList")
    santaListRecord.fullyQualifiedName should be ("com.gensler.scalavro.tests.SantaList")
    santaListType dependsOn personType should be (true)
    santaListType dependsOn santaListType should be (true)
  }

  it should "detect dependencies among AvroRecord types" in {
    import com.gensler.scalavro.error.CyclicTypeDependencyException
    evaluating { AvroType[A] } should produce[CyclicTypeDependencyException]
    evaluating { AvroType[B] } should produce[CyclicTypeDependencyException]
  }

}