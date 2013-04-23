package com.gensler.scalavro.io.complex

import scala.util.{ Try, Success, Failure }
import scala.reflect.runtime.universe._

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import com.gensler.scalavro.types._
import com.gensler.scalavro.types.complex._
import com.gensler.scalavro.error._
import com.gensler.scalavro.io.AvroTypeIO.Implicits._

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

class AvroSetIOSpec extends FlatSpec with ShouldMatchers {

  val setType = AvroType[Set[Person]]
  val io = setType.io

  "AvroSetIO" should "be available with the AvroTypeIO implicits in scope" in {
    io.isInstanceOf[AvroSetIO[_]] should be (true)
  }

  it should "read and write sets" in {
    val s1 = Set(
      Person("Russel", 45),
      Person("Whitehead", 53),
      Person("Wittgenstein", 22),
      Person("Godel", 37),
      Person("Church", 17),
      Person("Turing", 24))

    val out = new ByteArrayOutputStream
    io.write(s1, out)

    val in = new ByteArrayInputStream(out.toByteArray)
    io read in should equal (Success(s1))
  }

}