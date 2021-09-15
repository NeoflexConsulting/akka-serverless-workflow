package org.fluminous.jq.filter

import io.circe.Json

final case class JsonArrayTemplate(values: Seq[Either[Json, Filter]]) extends Filter {
  override def transform(input: Json): Option[Json] = {
    Option(Json.fromValues(values.flatMap(valueToJson(input))))
  }

  private def valueToJson(input: Json)(value: Either[Json, Filter]): Option[Json] = {
    value.fold(Option(_), _.transform(input))
  }
}