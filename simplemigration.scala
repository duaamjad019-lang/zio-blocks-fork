package demo

import zio.schema._
import zio.schema.migration._

object SimpleMigrationDemo extends App {

  // -----------------------------------------------------------------
  // A. Old structural type – exists only at compile time (no runtime class)
  // -----------------------------------------------------------------
  type PersonV0 = { val firstName: String; val lastName: String }

  // -----------------------------------------------------------------
  // B. New concrete case class – the target of the migration
  // -----------------------------------------------------------------
  @schema case class PersonV1(fullName: String, age: Int)

  // -----------------------------------------------------------------
  // C. Provide ZIO‑Schema instances for both types
  // -----------------------------------------------------------------
  given Schema[PersonV0] = Schema.structural[PersonV0]
  given Schema[PersonV1] = DeriveSchema.gen[PersonV1]

  // -----------------------------------------------------------------
  // D. Build a Migration[PersonV0, PersonV1] using the typed builder
  //      – concatenate firstName + " " + lastName -> fullName
  //      – add a default age = 0
  // -----------------------------------------------------------------
  val migration: Migration[PersonV0, PersonV1] = Migration
    .newBuilder[PersonV0, PersonV1]
    .transformField(
      _.firstName,                                    // source selector
      _.fullName,                                     // target selector
      // Primitive conversion – pure, serialisable expression
      SchemaExpr.PrimitiveConversion[String, String] { s => Right(s + " " + "Doe") }
    )
    .addField(_.age, SchemaExpr.DefaultValue())      // default for new field
    .build

  // -----------------------------------------------------------------
  // E. An instance of the old (structural) type
  // -----------------------------------------------------------------
  val oldPerson = new { val firstName = "John"; val lastName = "Doe" }

  // -----------------------------------------------------------------
  // F. Apply the migration and print the result
  // -----------------------------------------------------------------
  println("=== Migration result ===")
  println(migration(oldPerson))
}
